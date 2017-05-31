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
import gov.nasa.jpf.jvm.bytecode.PUTFIELD;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.util.IntSet;
import gov.nasa.jpf.util.SortedArrayIntSet;
import gov.nasa.jpf.util.StateExtensionClient;
import gov.nasa.jpf.util.StateExtensionListener;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceFieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceInvokeInstruction;
import gov.nasa.jpf.vm.bytecode.InvokeInstruction;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * listener that keeps track of all operations on objects that are specified by
 * reference value or types
 */
public class ObjectTracker extends ListenerAdapter implements StateExtensionClient {
  
  static class Attr {
    // nothing here, just a tag
  }
  
  static final Attr ATTR = new Attr(); // we need only one
  
  enum OpType { NEW, CALL, PUT, GET, FREE };
  
  static class LogRecord {
    ElementInfo ei;
    ThreadInfo ti;
    Instruction insn;
    OpType opType;
    int stateId;
    
    LogRecord prev;
    
    LogRecord (OpType opType, ElementInfo ei, ThreadInfo ti, Instruction insn, LogRecord prev){
      this.opType = opType;
      this.ei = ei;
      this.ti = ti;
      this.insn = insn;
      this.prev = prev;
      this.stateId = ti.getVM().getStateId();
    }
    
    void printOn (PrintWriter pw){
      if (prev != null && stateId != prev.stateId){
        pw.printf("----------------------------------- [%d]\n", prev.stateId + 1);
      }
      
      pw.print(ti.getId());
      pw.print(": ");
      
      pw.printf("%-4s ", opType.toString().toLowerCase());
      pw.print(ei);
      pw.print('.');
      
      if (insn != null){        
        if (insn instanceof FieldInstruction){
          FieldInstruction finsn = (FieldInstruction)insn;
          
          String fname = finsn.getFieldName();
          pw.print(fname);
          
        } else if (insn instanceof InvokeInstruction){
          InvokeInstruction call = (InvokeInstruction)insn;
          
          String mthName = call.getInvokedMethodName();
          
          pw.print( Types.getDequalifiedMethodSignature(mthName));
        }
      }
      
      pw.println();
    }
  }
  
  protected LogRecord log; // needs to be state restored
  
  //--- log options  
  protected StringSetMatcher includeClasses, excludeClasses; // type name patterns
  protected IntSet trackedRefs;
  
  protected boolean logFieldAccess;
  protected boolean logCalls;

    
  
  //--- internal stuff
  
  public ObjectTracker (Config conf, JPF jpf) {
    includeClasses = StringSetMatcher.getNonEmpty(conf.getStringArray("ot.include"));
    excludeClasses = StringSetMatcher.getNonEmpty(conf.getStringArray("ot.exclude", new String[] { "*" }));

    trackedRefs = new SortedArrayIntSet();
    
    int[] refs = conf.getIntArray("ot.refs");
    if (refs != null){
      for (int i=0; i<refs.length; i++){
        trackedRefs.add(refs[i]);
      }
    }
    
    logCalls = conf.getBoolean("ot.log_calls", true);
    logFieldAccess = conf.getBoolean("ot.log_fields", true);
    
    registerListener(jpf);
    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }
    
  protected void log (OpType opType, ElementInfo ei, ThreadInfo ti, Instruction insn){
    log = new LogRecord( opType, ei, ti, insn,  log);
  }
  
  
  //--- Listener interface
  
  @Override
  public void classLoaded (VM vm, ClassInfo ci){
    if (StringSetMatcher.isMatch(ci.getName(), includeClasses, excludeClasses)){
      ci.addAttr(ATTR);
    }
  }
  
  @Override
  public void objectCreated (VM vm, ThreadInfo ti, ElementInfo ei) {
    ClassInfo ci = ei.getClassInfo();
    int ref = ei.getObjectRef();
    
    if (ci.hasAttr(Attr.class) || trackedRefs.contains(ref)){
      // it's new, we don't need to call getModifiable
      ei.addObjectAttr(ATTR);
      log( OpType.NEW, ei, ti, ti.getPC());
    }
  }
  
  @Override
  public void objectReleased (VM vm, ThreadInfo ti, ElementInfo ei) {
    if (ei.hasObjectAttr(Attr.class)){
      log( OpType.FREE, ei, ti, ti.getPC());      
    }
  }

  @Override
  public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
    
    if (logCalls && executedInsn instanceof InstanceInvokeInstruction){      
      if (nextInsn != executedInsn){ // otherwise we didn't enter
        InstanceInvokeInstruction call = (InstanceInvokeInstruction)executedInsn;

        int ref = call.getCalleeThis(ti);
        ElementInfo ei = ti.getElementInfo(ref);
        
        if (ei.hasObjectAttr(Attr.class)) {
          log( OpType.CALL, ei, ti, executedInsn);
        }
      }
      
    } else if (logFieldAccess && executedInsn instanceof InstanceFieldInstruction){
      if (nextInsn != executedInsn){ // otherwise we didn't enter
        InstanceFieldInstruction finsn = (InstanceFieldInstruction) executedInsn;

        StackFrame frame = ti.getTopFrame();
        int idx = finsn.getObjectSlot(frame);
        int ref = frame.getSlot(idx);
        ElementInfo ei = ti.getElementInfo(ref);
        
        if (ei.hasObjectAttr(Attr.class)) {
          OpType op = (executedInsn instanceof PUTFIELD) ? OpType.PUT : OpType.GET;
          log( op, ei, ti, executedInsn);
        }
      }
    }
  }

  //--- state store/restore
  
  @Override
  public Object getStateExtension () {
    return log;
  }

  @Override
  public void restore (Object stateExtension) {
    log = (LogRecord)stateExtension;
  }

  @Override
  public void registerListener (JPF jpf) {
    StateExtensionListener<Number> sel = new StateExtensionListener(this);
    jpf.addSearchListener(sel);
  }

  
  //--- reporting
  
  @Override
  public void publishPropertyViolation (Publisher publisher) {    
    if (log != null){ // otherwise we don't have anything to report
      PrintWriter pw = publisher.getOut();
      publisher.publishTopicStart("ObjectTracker " + publisher.getLastErrorId());
      printLogOn(pw);
    }
  }

  protected void printLogOn (PrintWriter pw){
    // the log can be quite long so we can't use recursion (Java does not optimize tail recursion)
    List<LogRecord> logRecs = new ArrayList<LogRecord>();
    for (LogRecord lr = log; lr != null; lr = lr.prev){
      logRecs.add(lr);
    }
    
    Collections.reverse(logRecs);
    
    for (LogRecord lr : logRecs){
      lr.printOn(pw);
    }
  }
}
