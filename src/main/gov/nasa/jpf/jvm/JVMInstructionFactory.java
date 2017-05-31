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
package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.Invocation;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;

import java.util.List;

/**
 * interface for bytecode creation
 *
 * this deliberately uses the abstract abstract public Instruction as return type to allow different instruction hierarchies in
 * extensions.
 *
 * This shouldn't impose runtime overhead since mandatory parameters are now passed in as factory method arguments. The only
 * drawback is that the compiler cannot check for abstract public Instruction class typos, but that seems less important than
 * allowing extension specific abstract public Instruction class hierarchies
 *
 * <2do> there are still direct references of LOOKUPSWITCH, TABLESWITCH. Once these are removed, .jvm does not assume a particular
 * abstract public Instruction hierarchy
 */
public abstract class JVMInstructionFactory implements Cloneable {

  protected static JVMInstructionFactory singleton;
  
  public static JVMInstructionFactory getFactory(){
    return singleton;
  }
  
  protected JVMInstructionFactory(){
    // we should check the singleton first
    singleton = this;
  }
  

  //--- the factory methods
  abstract public Instruction aconst_null ();

  abstract public Instruction aload (int localVarIndex);

  abstract public Instruction aload_0 ();

  abstract public Instruction aload_1 ();

  abstract public Instruction aload_2 ();

  abstract public Instruction aload_3 ();

  abstract public Instruction aaload ();

  abstract public Instruction astore (int localVarIndex);

  abstract public Instruction astore_0 ();

  abstract public Instruction astore_1 ();

  abstract public Instruction astore_2 ();

  abstract public Instruction astore_3 ();

  abstract public Instruction aastore ();

  abstract public Instruction areturn ();

  abstract public Instruction anewarray (String clsName);

  abstract public Instruction arraylength ();

  abstract public Instruction athrow ();

  abstract public Instruction baload ();

  abstract public Instruction bastore ();

  abstract public Instruction bipush (int b);

  abstract public Instruction caload ();

  abstract public Instruction castore ();

  abstract public Instruction checkcast (String clsName);

  abstract public Instruction d2f ();

  abstract public Instruction d2i ();

  abstract public Instruction d2l ();

  abstract public Instruction dadd ();

  abstract public Instruction daload ();

  abstract public Instruction dastore ();

  abstract public Instruction dcmpg ();

  abstract public Instruction dcmpl ();

  abstract public Instruction dconst_0 ();

  abstract public Instruction dconst_1 ();

  abstract public Instruction ddiv ();

  abstract public Instruction dload (int localVarIndex);

  abstract public Instruction dload_0 ();

  abstract public Instruction dload_1 ();

  abstract public Instruction dload_2 ();

  abstract public Instruction dload_3 ();

  abstract public Instruction dmul ();

  abstract public Instruction dneg ();

  abstract public Instruction drem ();

  abstract public Instruction dreturn ();

  abstract public Instruction dstore (int localVarIndex);

  abstract public Instruction dstore_0 ();

  abstract public Instruction dstore_1 ();

  abstract public Instruction dstore_2 ();

  abstract public Instruction dstore_3 ();

  abstract public Instruction dsub ();

  abstract public Instruction dup ();

  abstract public Instruction dup_x1 ();

  abstract public Instruction dup_x2 ();

  abstract public Instruction dup2 ();

  abstract public Instruction dup2_x1 ();

  abstract public Instruction dup2_x2 ();

  abstract public Instruction f2d ();

  abstract public Instruction f2i ();

  abstract public Instruction f2l ();

  abstract public Instruction fadd ();

  abstract public Instruction faload ();

  abstract public Instruction fastore ();

  abstract public Instruction fcmpg ();

  abstract public Instruction fcmpl ();

  abstract public Instruction fconst_0 ();

  abstract public Instruction fconst_1 ();

  abstract public Instruction fconst_2 ();

  abstract public Instruction fdiv ();

  abstract public Instruction fload (int localVarIndex);

  abstract public Instruction fload_0 ();

  abstract public Instruction fload_1 ();

  abstract public Instruction fload_2 ();

  abstract public Instruction fload_3 ();

  abstract public Instruction fmul ();

  abstract public Instruction fneg ();

  abstract public Instruction frem ();

  abstract public Instruction freturn ();

  abstract public Instruction fstore (int localVarIndex);

  abstract public Instruction fstore_0 ();

  abstract public Instruction fstore_1 ();

  abstract public Instruction fstore_2 ();

  abstract public Instruction fstore_3 ();

  abstract public Instruction fsub ();

  abstract public Instruction getfield (String fieldName, String clsName, String fieldDescriptor);

  abstract public Instruction getstatic (String fieldName, String clsName, String fieldDescriptor);

  abstract public Instruction goto_ (int targetPc);

  abstract public Instruction goto_w (int targetPc);

  abstract public Instruction i2b ();

  abstract public Instruction i2c ();

  abstract public Instruction i2d ();

  abstract public Instruction i2f ();

  abstract public Instruction i2l ();

  abstract public Instruction i2s ();

  abstract public Instruction iadd ();

  abstract public Instruction iaload ();

  abstract public Instruction iand ();

  abstract public Instruction iastore ();

  abstract public Instruction iconst_m1 ();

  abstract public Instruction iconst_0 ();

  abstract public Instruction iconst_1 ();

  abstract public Instruction iconst_2 ();

  abstract public Instruction iconst_3 ();

  abstract public Instruction iconst_4 ();

  abstract public Instruction iconst_5 ();

  abstract public Instruction idiv ();

  abstract public Instruction if_acmpeq (int targetPc);

  abstract public Instruction if_acmpne (int targetPc);

  abstract public Instruction if_icmpeq (int targetPc);

  abstract public Instruction if_icmpne (int targetPc);

  abstract public Instruction if_icmplt (int targetPc);

  abstract public Instruction if_icmpge (int targetPc);

  abstract public Instruction if_icmpgt (int targetPc);

  abstract public Instruction if_icmple (int targetPc);

  abstract public Instruction ifeq (int targetPc);

  abstract public Instruction ifne (int targetPc);

  abstract public Instruction iflt (int targetPc);

  abstract public Instruction ifge (int targetPc);

  abstract public Instruction ifgt (int targetPc);

  abstract public Instruction ifle (int targetPc);

  abstract public Instruction ifnonnull (int targetPc);

  abstract public Instruction ifnull (int targetPc);

  abstract public Instruction iinc (int localVarIndex, int incConstant);

  abstract public Instruction iload (int localVarIndex);

  abstract public Instruction iload_0 ();

  abstract public Instruction iload_1 ();

  abstract public Instruction iload_2 ();

  abstract public Instruction iload_3 ();

  abstract public Instruction imul ();

  abstract public Instruction ineg ();

  abstract public Instruction instanceof_ (String clsName);

  abstract public Instruction invokeinterface (String clsName, String methodName, String methodSignature);

  abstract public Instruction invokespecial (String clsName, String methodName, String methodSignature);

  abstract public Instruction invokestatic (String clsName, String methodName, String methodSignature);

  abstract public Instruction invokevirtual (String clsName, String methodName, String methodSignature);
  
  abstract public Instruction invokedynamic (int bootstrapIndex, String samMethodName, String functionType);

  abstract public Instruction ior ();

  abstract public Instruction irem ();

  abstract public Instruction ireturn ();

  abstract public Instruction ishl ();

  abstract public Instruction ishr ();

  abstract public Instruction istore (int localVarIndex);

  abstract public Instruction istore_0 ();

  abstract public Instruction istore_1 ();

  abstract public Instruction istore_2 ();

  abstract public Instruction istore_3 ();

  abstract public Instruction isub ();

  abstract public Instruction iushr ();

  abstract public Instruction ixor ();

  abstract public Instruction jsr (int targetPc);

  abstract public Instruction jsr_w (int targetPc);

  abstract public Instruction l2d ();

  abstract public Instruction l2f ();

  abstract public Instruction l2i ();

  abstract public Instruction ladd ();

  abstract public Instruction laload ();

  abstract public Instruction land ();

  abstract public Instruction lastore ();

  abstract public Instruction lcmp ();

  abstract public Instruction lconst_0 ();

  abstract public Instruction lconst_1 ();

  abstract public Instruction ldc (int v);

  abstract public Instruction ldc (float v);

  abstract public Instruction ldc (String v, boolean isClass);

  abstract public Instruction ldc_w (int v);

  abstract public Instruction ldc_w (float v);

  abstract public Instruction ldc_w (String v, boolean isClass);

  abstract public Instruction ldc2_w (long v);

  abstract public Instruction ldc2_w (double v);

  abstract public Instruction ldiv ();

  abstract public Instruction lload (int localVarIndex);

  abstract public Instruction lload_0 ();

  abstract public Instruction lload_1 ();

  abstract public Instruction lload_2 ();

  abstract public Instruction lload_3 ();

  abstract public Instruction lmul ();

  abstract public Instruction lneg ();

  abstract public Instruction lookupswitch (int defaultTargetPc, int nEntries);

  abstract public Instruction lor ();

  abstract public Instruction lrem ();

  abstract public Instruction lreturn ();

  abstract public Instruction lshl ();

  abstract public Instruction lshr ();

  abstract public Instruction lstore (int localVarIndex);

  abstract public Instruction lstore_0 ();

  abstract public Instruction lstore_1 ();

  abstract public Instruction lstore_2 ();

  abstract public Instruction lstore_3 ();

  abstract public Instruction lsub ();

  abstract public Instruction lushr ();

  abstract public Instruction lxor ();

  abstract public Instruction monitorenter ();

  abstract public Instruction monitorexit ();

  abstract public Instruction multianewarray (String clsName, int dimensions);

  abstract public Instruction new_ (String clsName);

  abstract public Instruction newarray (int typeCode);

  abstract public Instruction nop ();

  abstract public Instruction pop ();

  abstract public Instruction pop2 ();

  abstract public Instruction putfield (String fieldName, String clsName, String fieldDescriptor);

  abstract public Instruction putstatic (String fieldName, String clsName, String fieldDescriptor);

  abstract public Instruction ret (int localVarIndex);

  abstract public Instruction return_ ();

  abstract public Instruction saload ();

  abstract public Instruction sastore ();

  abstract public Instruction sipush (int val);

  abstract public Instruction swap ();

  abstract public Instruction tableswitch (int defaultTargetPc, int low, int high);

  abstract public Instruction wide ();

  //--- the JPF specific ones (only used in synthetic methods)
  abstract public Instruction invokecg (List<Invocation> invokes);

  abstract public Instruction invokeclinit (ClassInfo ci);

  abstract public Instruction directcallreturn ();

  abstract public Instruction executenative (NativeMethodInfo mi);

  abstract public Instruction nativereturn ();

  // this is never part of MethodInfo stored code
  abstract public Instruction runstart (MethodInfo miRun);

  abstract public Instruction finishclinit (ClassInfo ci);
}
