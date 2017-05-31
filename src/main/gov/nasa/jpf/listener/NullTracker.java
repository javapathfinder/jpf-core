/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.ARETURN;
import gov.nasa.jpf.jvm.bytecode.ASTORE;
import gov.nasa.jpf.jvm.bytecode.PUTFIELD;
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.jvm.bytecode.RETURN;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceFieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceInvokeInstruction;
import gov.nasa.jpf.vm.bytecode.InstructionInterface;
import gov.nasa.jpf.vm.bytecode.InvokeInstruction;
import gov.nasa.jpf.vm.bytecode.LocalVariableInstruction;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;
import gov.nasa.jpf.vm.bytecode.ReturnValueInstruction;
import gov.nasa.jpf.vm.bytecode.WriteInstruction;
import java.io.PrintWriter;

/**
 * trace where nulls come from - which is either a GETFIELD/STATIC, an
 * JVMInvokeInstruction, an LocalVariableInstruction or a missing init.
 * 
 * Record/accumulate the causes in an attribute and use the attribute 
 * to explain NPEs
 */
public class NullTracker extends ListenerAdapter {

  public static abstract class NullSource {
    protected InstructionInterface insn;
    protected ThreadInfo ti;
    protected ElementInfo ei;
    
    protected NullSource cause;
    
    NullSource (ThreadInfo ti, InstructionInterface insn, ElementInfo ei){
      this.ti = ti;
      this.insn = insn;
      this.ei = ei;
    }
    
    public void setCause (NullSource cause){
      this.cause = cause;
    }
    
    abstract void printOn (PrintWriter pw);
    
    void printInsnOn (PrintWriter pw){
      pw.printf("    instruction: [%04x] %s\n", insn.getPosition(), insn.toString());
    }
        
    void printThreadInfoOn (PrintWriter pw){
      pw.println("    executed by: " + ti.getName() + " (id=" + ti.getId() + ")");
    }
    
    void printMethodInfoOn (PrintWriter pw, String msg, InstructionInterface instruction){
      MethodInfo mi = instruction.getMethodInfo();
      ClassInfo ci = mi.getClassInfo();
      pw.println( msg + ci.getName() + '.' + mi.getLongName() + " (" + instruction.getFilePos() + ')');
    }
    
    void printCauseOn (PrintWriter pw){
      if (cause != null){
        pw.println("set by: ");
        cause.printOn(pw);
      }
    }
  }
  
  
  public static class LocalSource extends NullSource {
    protected LocalVarInfo local;
    
    public LocalSource (ThreadInfo ti, LocalVariableInstruction insn, LocalVarInfo local){
      super(ti, insn, null);
      this.local = local;
    }
    
    @Override
    void printOn (PrintWriter pw){
      printInsnOn(pw);
      if (local != null){
        pw.println("      for local: " + local.getName());
      } else {
        pw.println("     for local: #" + ((LocalVariableInstruction)insn).getLocalVariableSlot());
      }
      printMethodInfoOn(pw, "      in method: ", insn);
      printThreadInfoOn(pw);
      
      printCauseOn(pw);
    }
  }
  
  public static class FieldSource extends NullSource {
    public FieldSource (ThreadInfo ti, FieldInstruction insn, ElementInfo ei){
      super(ti,insn,ei);
    }
    
    @Override
    void printOn (PrintWriter pw){
      FieldInfo fi = ((FieldInstruction)insn).getFieldInfo();
      MethodInfo mi = insn.getMethodInfo();
            
      printInsnOn(pw);
      pw.println("      for field: " + fi.getFullName());
      printMethodInfoOn(pw, "      in method: ", insn);
      printThreadInfoOn(pw);
      
      printCauseOn(pw);
    }
  }

  public static class MethodSource extends NullSource {
    InvokeInstruction call;
    
    public MethodSource (ThreadInfo ti, InstructionInterface returnInsn, InvokeInstruction call, ElementInfo ei){
      super(ti,returnInsn,ei);
      this.call = call;
    }
    
    @Override
    void printOn (PrintWriter pw){            
      printInsnOn(pw);
      printMethodInfoOn(pw, "      of method: ", insn);
      
      if (ei != null){
        pw.println("     for object: " + ei);
      }
      printMethodInfoOn(pw, "      called by: ", call);
      printThreadInfoOn(pw);
      
      printCauseOn(pw);
    }    
  }
  
  public static class CtorSource extends MethodSource {
    public CtorSource (ThreadInfo ti, Instruction returnInsn, InvokeInstruction call, ElementInfo ei){
      super(ti,returnInsn,call, ei);
    }
    
    @Override
    void printOn (PrintWriter pw){ 
      printMethodInfoOn(pw, "   missing init: ", insn);
      
      if (ei != null){
        pw.println("     for object: " + ei);
      }
      printMethodInfoOn(pw, "      called by: ", call);
      printThreadInfoOn(pw);
      
      printCauseOn(pw);
    }    
  }

  //---------------------------------------------------------------------------------
  
  protected NullSource nullSource;
  
  public NullTracker (Config config, JPF jpf){
    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }
  
  protected void checkCtorSourcePre (ThreadInfo ti, ReturnInstruction insn){
    MethodInfo mi = insn.getMethodInfo();
    if (mi.isCtor()) {
      StackFrame callerFrame = null;
      InvokeInstruction call = null;
      ElementInfo ei = ti.getThisElementInfo();
      ClassInfo ci = ei.getClassInfo();
      int nInstance = ci.getNumberOfDeclaredInstanceFields();
      
      for (int i = 0; i < nInstance; i++) {
        FieldInfo fi = ci.getDeclaredInstanceField(i);
        if (fi.isReference()) {
          int ref = ei.getReferenceField(fi);
          if (ref == MJIEnv.NULL) {
            ei = ei.getModifiableInstance();  // why do we need this in a ctor?
            if (call == null) {
              callerFrame = ti.getCallerStackFrame();
              call = (InvokeInstruction) callerFrame.getPC();
            }
            NullSource attr = new CtorSource(ti, insn, call, ti.getThisElementInfo());
            ei.setFieldAttr(fi, attr);
          }
        }
      }
    }
  }
  
  protected void checkFieldSourcePre (ThreadInfo ti, WriteInstruction put){
    FieldInfo fi = put.getFieldInfo();
    if (fi.isReference()) {
      StackFrame frame = ti.getTopFrame();
      int valSlot = put.getValueSlot(frame);
      int ref = frame.getSlot(valSlot);

      if (ref == MJIEnv.NULL) { // field will be set to null
        ElementInfo ei = put.getElementInfo(ti);
        NullSource attr = new FieldSource(ti, (FieldInstruction)put, ei);

        NullSource cause = frame.getSlotAttr(valSlot, NullSource.class);
        if (cause != null) {
          attr.setCause(cause);
          frame.replaceSlotAttr(valSlot, cause, attr);
        } else {
          frame.addSlotAttr(valSlot, attr);
        }
      }
    }    
  }
  
  protected void checkMethodSourcePre (ThreadInfo ti, ReturnValueInstruction aret){
    StackFrame frame = ti.getTopFrame();
    int valSlot = aret.getValueSlot(frame);
    int ref = frame.getSlot(valSlot);
    
    if (ref == MJIEnv.NULL) {
      StackFrame callerFrame = ti.getCallerStackFrame();
      InvokeInstruction call = (InvokeInstruction) callerFrame.getPC();
      NullSource attr = new MethodSource(ti, aret, call, ti.getThisElementInfo());

      NullSource cause = frame.getSlotAttr(valSlot, NullSource.class);
      if (cause != null) {
        attr.setCause(cause);
        frame.replaceSlotAttr(valSlot,cause, attr);
      } else {
        frame.addSlotAttr(valSlot,attr);
      }
    }
  }
  
  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insn) {
    
    if (insn instanceof ARETURN){
      checkMethodSourcePre( ti, (ARETURN)insn);
      
    } else if (insn instanceof PUTFIELD || insn instanceof PUTSTATIC){
      checkFieldSourcePre( ti, (WriteInstruction) insn);
      
    } else if (insn instanceof RETURN){
      checkCtorSourcePre(ti, (RETURN) insn);
    }
  }

  
  protected void checkLocalSourcePost (ThreadInfo ti, LocalVariableInstruction insn){
    int slotIdx = insn.getLocalVariableSlot();
    StackFrame frame = ti.getTopFrame();
    int ref = frame.getSlot(slotIdx);
    if (ref == MJIEnv.NULL) {
      LocalVarInfo lv = insn.getLocalVarInfo();
      NullSource attr = new LocalSource(ti, insn, lv);

      NullSource cause = frame.getSlotAttr(slotIdx, NullSource.class);
      if (cause != null) {
        attr.setCause(cause);
        frame.replaceSlotAttr(slotIdx, cause, attr);
      } else {
        frame.addSlotAttr(slotIdx, attr);
      }
    }
  }
  
  @Override
  public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction insn){
    
    // we need to do LocalVariableInstruction post exec since it did overwrite the attr if it had an immediate operand
    if (insn instanceof ASTORE) {
      checkLocalSourcePost( ti, (LocalVariableInstruction)insn);
    }
  }
    
  @Override
  public void exceptionThrown(VM vm, ThreadInfo ti, ElementInfo thrownException) {
    if (thrownException.instanceOf("Ljava/lang/NullPointerException;")){
      StackFrame frame = ti.getTopFrame();
      Instruction insn = ti.getPC();
      
      if (insn instanceof InstanceFieldInstruction){  // field access on null object
        int objSlot = ((InstanceFieldInstruction)insn).getObjectSlot(frame);
        NullSource attr = frame.getSlotAttr( objSlot,NullSource.class);
        if (attr != null) {
          nullSource = attr;
        }
        
      } else if (insn instanceof InstanceInvokeInstruction) { // call on a null object
        int objSlot = ((InstanceInvokeInstruction)insn).getObjectSlot(frame);
        NullSource attr = frame.getSlotAttr( objSlot, NullSource.class);
        if (attr != null) {
          nullSource = attr;
        }
      }
    }
  }

  
  @Override
  public void publishPropertyViolation (Publisher publisher) {    
    if (nullSource != null){ // otherwise we don't have anything to report
      PrintWriter pw = publisher.getOut();
      publisher.publishTopicStart("NullTracker " + publisher.getLastErrorId());

      pw.println("null value set by: ");
      nullSource.printOn(pw);
    }
  }
}
