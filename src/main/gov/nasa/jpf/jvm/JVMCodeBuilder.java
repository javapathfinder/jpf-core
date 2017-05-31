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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.Invocation;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.bytecode.LookupSwitchInstruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.bytecode.TableSwitchInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * a special JVMByteCodeReader implementation that builds code arrays for
 * MethodInfos, setting index and pc on the fly
 */
public class JVMCodeBuilder implements JVMByteCodeReader {

  protected JVMInstructionFactory insnFactory;
  
  protected ClassFile cf;
  protected MethodInfo mi;

  // have to cache these to set switch entries
  // <2do> these should use interface types to avoid hardwiring our own instruction classes
  protected TableSwitchInstruction tableswitchInsn;
  protected LookupSwitchInstruction lookupswitchInsn;

  protected ArrayList<Instruction> code;

  protected int pc; // bytecode position within method code
  protected int idx; // instruction index within MethodInfo

  // flag to remember wide immediate operand modification
  boolean isWide;
  
  //--- for testing purposes
  protected JVMCodeBuilder (JVMInstructionFactory ifact){
    this.code = new ArrayList<Instruction>(64);
    this.insnFactory = ifact;
  }

  
  
  // this is dangerous - it enables reuse of CodeBuilders, but
  // you better make sure this does not get recursive or is used concurrently
  public void reset (ClassFile classFile, MethodInfo targetMethod){
    this.cf = classFile;
    this.mi = targetMethod;

    pc = 0;
    idx = 0;
    isWide = false;

    tableswitchInsn = null;
    lookupswitchInsn = null;

    code.clear();
  }
  
  protected void add(Instruction insn){
    insn.setMethodInfo(mi);
    insn.setLocation(idx++, pc);
    code.add(insn);
  }

  public void installCode(){
    Instruction[] a = code.toArray( new Instruction[code.size()]);
    mi.setCode(a);
  }

  public int getCodeSize(){
    return code.size();
  }

  //--- the factory methods

  @Override public void aconst_null() {
    add( insnFactory.aconst_null());
    pc++;
  }

  @Override public void aload(int localVarIndex) {
    add( insnFactory.aload(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void aload_0() {
    add( insnFactory.aload(0));
    pc++;
  }

  @Override public void aload_1() {
    add( insnFactory.aload(1));
    pc++;
  }

  @Override public void aload_2() {
    add( insnFactory.aload(2));
    pc++;
  }

  @Override public void aload_3() {
    add( insnFactory.aload(3));
    pc++;
  }

  @Override public void aaload() {
    add( insnFactory.aaload());
    pc++;
  }

  @Override public void astore(int localVarIndex) {
    add( insnFactory.astore(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void astore_0() {
    add( insnFactory.astore(0));
    pc++;
  }

  @Override public void astore_1() {
    add( insnFactory.astore(1));
    pc++;
  }

  @Override public void astore_2() {
    add( insnFactory.astore(2));
    pc++;
  }

  @Override public void astore_3() {
    add( insnFactory.astore(3));
    pc++;
  }

  @Override public void aastore() {
    add( insnFactory.aastore());
    pc++;
  }

  @Override public void areturn() {
    add( insnFactory.areturn());
    pc++;
  }

  @Override public void anewarray(int cpClassIndex) {
    String clsName = cf.classNameAt(cpClassIndex);
    add( insnFactory.anewarray(clsName));
    pc+=3;
  }

  @Override public void arraylength() {
    add( insnFactory.arraylength());
    pc++;
  }

  @Override public void athrow() {
    add( insnFactory.athrow());
    pc++;
  }

  @Override public void baload() {
    add( insnFactory.baload());
    pc++;
  }

  @Override public void bastore() {
    add( insnFactory.bastore());
    pc++;
  }

  @Override public void bipush(int b) {
    add( insnFactory.bipush(b));
    pc+=2;
  }

  @Override public void caload() {
    add( insnFactory.caload());
    pc++;
  }

  @Override public void castore() {
    add( insnFactory.castore());
    pc++;
  }

  @Override public void checkcast(int cpClassIndex) {
    String clsName = cf.classNameAt(cpClassIndex);
    add( insnFactory.checkcast(clsName));
    pc+=3;
  }

  @Override public void d2f() {
    add( insnFactory.d2f());
    pc++;
  }

  @Override public void d2i() {
    add( insnFactory.d2i());
    pc++;
  }

  @Override public void d2l() {
    add( insnFactory.d2l());
    pc++;
  }

  @Override public void dadd() {
    add( insnFactory.dadd());
    pc++;
  }

  @Override public void daload() {
    add( insnFactory.daload());
    pc++;
  }

  @Override public void dastore() {
    add( insnFactory.dastore());
    pc++;
  }

  @Override public void dcmpg() {
    add( insnFactory.dcmpg());
    pc++;
  }

  @Override public void dcmpl() {
    add( insnFactory.dcmpl());
    pc++;
  }

  @Override public void dconst_0() {
    add( insnFactory.dconst_0());
    pc++;
  }

  @Override public void dconst_1() {
    add( insnFactory.dconst_1());
    pc++;
  }

  @Override public void ddiv() {
    add( insnFactory.ddiv());
    pc++;
  }

  @Override public void dload(int localVarIndex) {
    add( insnFactory.dload(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void dload_0() {
    add( insnFactory.dload(0));
    pc++;
  }

  @Override public void dload_1() {
    add( insnFactory.dload(1));
    pc++;
  }

  @Override public void dload_2() {
    add( insnFactory.dload(2));
    pc++;
  }

  @Override public void dload_3() {
    add( insnFactory.dload(3));
    pc++;
  }

  @Override public void dmul() {
    add( insnFactory.dmul());
    pc++;
  }

  @Override public void dneg() {
    add( insnFactory.dneg());
    pc++;
  }

  @Override public void drem() {
    add( insnFactory.drem());
    pc++;
  }

  @Override public void dreturn() {
    add( insnFactory.dreturn());
    pc++;
  }

  @Override public void dstore(int localVarIndex) {
    add( insnFactory.dstore(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void dstore_0() {
    add( insnFactory.dstore(0));
    pc++;
  }

  @Override public void dstore_1() {
    add( insnFactory.dstore(1));
    pc++;
  }

  @Override public void dstore_2() {
    add( insnFactory.dstore(2));
    pc++;
  }

  @Override public void dstore_3() {
    add( insnFactory.dstore(3));
    pc++;
  }

  @Override public void dsub() {
    add( insnFactory.dsub());
    pc++;
  }

  @Override public void dup() {
    add( insnFactory.dup());
    pc++;
  }

  @Override public void dup_x1() {
    add( insnFactory.dup_x1());
    pc++;
  }

  @Override public void dup_x2() {
    add( insnFactory.dup_x2());
    pc++;
  }

  @Override public void dup2() {
    add( insnFactory.dup2());
    pc++;
  }

  @Override public void dup2_x1() {
    add( insnFactory.dup2_x1());
    pc++;
  }

  @Override public void dup2_x2() {
    add( insnFactory.dup2_x2());
    pc++;
  }

  @Override public void f2d() {
    add( insnFactory.f2d());
    pc++;
  }

  @Override public void f2i() {
    add( insnFactory.f2i());
    pc++;
  }

  @Override public void f2l() {
    add( insnFactory.f2l());
    pc++;
  }

  @Override public void fadd() {
    add( insnFactory.fadd());
    pc++;
  }

  @Override public void faload() {
    add( insnFactory.faload());
    pc++;
  }

  @Override public void fastore() {
    add( insnFactory.fastore());
    pc++;
  }

  @Override public void fcmpg() {
    add( insnFactory.fcmpg());
    pc++;
  }

  @Override public void fcmpl() {
    add( insnFactory.fcmpl());
    pc++;
  }

  @Override public void fconst_0() {
    add( insnFactory.fconst_0());
    pc++;
  }

  @Override public void fconst_1() {
    add( insnFactory.fconst_1());
    pc++;
  }

  @Override public void fconst_2() {
    add( insnFactory.fconst_2());
    pc++;
  }

  @Override public void fdiv() {
    add( insnFactory.fdiv());
    pc++;
  }

  @Override public void fload(int localVarIndex) {
    add( insnFactory.fload(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void fload_0() {
    add( insnFactory.fload(0));
    pc++;
  }

  @Override public void fload_1() {
    add( insnFactory.fload(1));
    pc++;
  }

  @Override public void fload_2() {
    add( insnFactory.fload(2));
    pc++;
  }

  @Override public void fload_3() {
    add( insnFactory.fload(3));
    pc++;
  }

  @Override public void fmul() {
    add( insnFactory.fmul());
    pc++;
  }

  @Override public void fneg() {
    add( insnFactory.fneg());
    pc++;
  }

  @Override public void frem() {
    add( insnFactory.frem());
    pc++;
  }

  @Override public void freturn() {
    add( insnFactory.freturn());
    pc++;
  }

  @Override public void fstore(int localVarIndex) {
    add( insnFactory.fstore(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void fstore_0() {
    add( insnFactory.fstore(0));
    pc++;
  }

  @Override public void fstore_1() {
    add( insnFactory.fstore(1));
    pc++;
  }

  @Override public void fstore_2() {
    add( insnFactory.fstore(2));
    pc++;
  }

  @Override public void fstore_3() {
    add( insnFactory.fstore(3));
    pc++;
  }

  @Override public void fsub() {
    add( insnFactory.fsub());
    pc++;
  }

  @Override public void getfield(int cpFieldRefIndex) {
    String fieldName = cf.fieldNameAt(cpFieldRefIndex);
    String clsName = cf.fieldClassNameAt(cpFieldRefIndex);
    String fieldDescriptor = cf.fieldDescriptorAt(cpFieldRefIndex);

    add( insnFactory.getfield(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }
  public void getfield(String fieldName, String clsName, String fieldDescriptor){
    add( insnFactory.getfield(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }

  @Override public void getstatic(int cpFieldRefIndex) {
    String fieldName = cf.fieldNameAt(cpFieldRefIndex);
    String clsName = cf.fieldClassNameAt(cpFieldRefIndex);
    String fieldDescriptor = cf.fieldDescriptorAt(cpFieldRefIndex);

    add( insnFactory.getstatic(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }
  public void getstatic(String fieldName, String clsName, String fieldDescriptor){
    add( insnFactory.getstatic(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }

  @Override public void goto_(int pcOffset) {
    add( insnFactory.goto_(pc + pcOffset));
    pc+=3;
  }

  @Override public void goto_w(int pcOffset) {
    add( insnFactory.goto_w(pc + pcOffset));
    pc+=5;
  }

  @Override public void i2b() {
    add( insnFactory.i2b());
    pc++;
  }

  @Override public void i2c() {
    add( insnFactory.i2c());
    pc++;
  }

  @Override public void i2d() {
    add( insnFactory.i2d());
    pc++;
  }

  @Override public void i2f() {
    add( insnFactory.i2f());
    pc++;
  }

  @Override public void i2l() {
    add( insnFactory.i2l());
    pc++;
  }

  @Override public void i2s() {
    add( insnFactory.i2s());
    pc++;
  }

  @Override public void iadd() {
    add( insnFactory.iadd());
    pc++;
  }

  @Override public void iaload() {
    add( insnFactory.iaload());
    pc++;
  }

  @Override public void iand() {
    add( insnFactory.iand());
    pc++;
  }

  @Override public void iastore() {
    add( insnFactory.iastore());
    pc++;
  }

  @Override public void iconst_m1() {
    add( insnFactory.iconst_m1());
    pc++;
  }

  @Override public void iconst_0() {
    add( insnFactory.iconst_0());
    pc++;
  }

  @Override public void iconst_1() {
    add( insnFactory.iconst_1());
    pc++;
  }

  @Override public void iconst_2() {
    add( insnFactory.iconst_2());
    pc++;
  }

  @Override public void iconst_3() {
    add( insnFactory.iconst_3());
    pc++;
  }

  @Override public void iconst_4() {
    add( insnFactory.iconst_4());
    pc++;
  }

  @Override public void iconst_5() {
    add( insnFactory.iconst_5());
    pc++;
  }

  @Override public void idiv() {
    add( insnFactory.idiv());
    pc++;
  }

  @Override public void if_acmpeq(int pcOffset) {
    add( insnFactory.if_acmpeq(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_acmpne(int pcOffset) {
    add( insnFactory.if_acmpne(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmpeq(int pcOffset) {
    add( insnFactory.if_icmpeq(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmpne(int pcOffset) {
    add( insnFactory.if_icmpne(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmplt(int pcOffset) {
    add( insnFactory.if_icmplt(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmpge(int pcOffset) {
    add( insnFactory.if_icmpge(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmpgt(int pcOffset) {
    add( insnFactory.if_icmpgt(pc + pcOffset));
    pc+=3;
  }

  @Override public void if_icmple(int pcOffset) {
    add( insnFactory.if_icmple(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifeq(int pcOffset) {
    add( insnFactory.ifeq(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifne(int pcOffset) {
    add( insnFactory.ifne(pc + pcOffset));
    pc+=3;
  }

  @Override public void iflt(int pcOffset) {
    add( insnFactory.iflt(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifge(int pcOffset) {
    add( insnFactory.ifge(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifgt(int pcOffset) {
    add( insnFactory.ifgt(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifle(int pcOffset) {
    add( insnFactory.ifle(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifnonnull(int pcOffset) {
    add( insnFactory.ifnonnull(pc + pcOffset));
    pc+=3;
  }

  @Override public void ifnull(int pcOffset) {
    add( insnFactory.ifnull(pc + pcOffset));
    pc+=3;
  }

  @Override public void iinc(int localVarIndex, int incConstant) {
    add( insnFactory.iinc(localVarIndex, incConstant));
    pc+=3;
    if (isWide){
      pc+=2;
      isWide = false;
    }
  }

  @Override public void iload(int localVarIndex) {
    add( insnFactory.iload(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void iload_0() {
    add( insnFactory.iload(0));
    pc++;
  }

  @Override public void iload_1() {
    add( insnFactory.iload(1));
    pc++;
  }

  @Override public void iload_2() {
    add( insnFactory.iload(2));
    pc++;
  }

  @Override public void iload_3() {
    add( insnFactory.iload(3));
    pc++;
  }

  @Override public void imul() {
    add( insnFactory.imul());
    pc++;
  }

  @Override public void ineg() {
    add( insnFactory.ineg());
    pc++;
  }

  @Override public void instanceof_(int cpClassIndex) {
    String clsName = cf.classNameAt(cpClassIndex);
    add( insnFactory.instanceof_(clsName));
    pc+=3;
  }

  @Override public void invokeinterface(int cpInterfaceMethodRefIndex, int count, int zero) {
    String clsName = cf.interfaceMethodClassNameAt(cpInterfaceMethodRefIndex);
    String methodName = cf.interfaceMethodNameAt(cpInterfaceMethodRefIndex);
    String methodSignature = cf.interfaceMethodDescriptorAt(cpInterfaceMethodRefIndex);

    add( insnFactory.invokeinterface(clsName, methodName, methodSignature));
    pc+=5;
  }
  public void invokeinterface(String clsName, String methodName, String methodSignature){
    add( insnFactory.invokeinterface(clsName, methodName, methodSignature));
    pc+=5;
  }

  @Override
  public void invokedynamic (int cpInvokeDynamicIndex){
    int bootstrapMethodIndex = cf.bootstrapMethodIndex(cpInvokeDynamicIndex);
    String samMethodName = cf.samMethodNameAt(cpInvokeDynamicIndex);
    String callSiteDescriptor = cf.callSiteDescriptor(cpInvokeDynamicIndex);
    add( insnFactory.invokedynamic(bootstrapMethodIndex, samMethodName, callSiteDescriptor));
    pc+=5;
  }
  
  @Override public void invokespecial(int cpMethodRefIndex) {
    String clsName = cf.methodClassNameAt(cpMethodRefIndex);
    String methodName = cf.methodNameAt(cpMethodRefIndex);
    String methodSignature = cf.methodDescriptorAt(cpMethodRefIndex);

    add( insnFactory.invokespecial(clsName, methodName, methodSignature));
    pc+=3;
  }
  public void invokespecial(String clsName, String methodName, String methodSignature){
    add( insnFactory.invokespecial(clsName, methodName, methodSignature));
    pc+=3;
  }

  @Override public void invokestatic(int cpMethodRefIndex) {
    String clsName = cf.methodClassNameAt(cpMethodRefIndex);
    String methodName = cf.methodNameAt(cpMethodRefIndex);
    String methodSignature = cf.methodDescriptorAt(cpMethodRefIndex);

    add( insnFactory.invokestatic(clsName, methodName, methodSignature));
    pc+=3;
  }
  public void invokestatic(String clsName, String methodName, String methodSignature){
    add( insnFactory.invokestatic(clsName, methodName, methodSignature));
    pc+=3;
  }

  @Override public void invokevirtual(int cpMethodRefIndex) {
    String clsName = cf.methodClassNameAt(cpMethodRefIndex);
    String methodName = cf.methodNameAt(cpMethodRefIndex);
    String methodSignature = cf.methodDescriptorAt(cpMethodRefIndex);

    add( insnFactory.invokevirtual(clsName, methodName, methodSignature));
    pc+=3;
  }
  public void invokevirtual(String clsName, String methodName, String methodSignature){
    add( insnFactory.invokevirtual(clsName, methodName, methodSignature));
    pc+=3;
  }

  @Override public void ior() {
    add( insnFactory.ior());
    pc++;
  }

  @Override public void irem() {
    add( insnFactory.irem());
    pc++;
  }

  @Override public void ireturn() {
    add( insnFactory.ireturn());
    pc++;
  }

  @Override public void ishl() {
    add( insnFactory.ishl());
    pc++;
  }

  @Override public void ishr() {
    add( insnFactory.ishr());
    pc++;
  }

  @Override public void istore(int localVarIndex) {
    add( insnFactory.istore(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void istore_0() {
    add( insnFactory.istore(0));
    pc++;
  }

  @Override public void istore_1() {
    add( insnFactory.istore(1));
    pc++;
  }

  @Override public void istore_2() {
    add( insnFactory.istore(2));
    pc++;
  }

  @Override public void istore_3() {
    add( insnFactory.istore(3));
    pc++;
  }

  @Override public void isub() {
    add( insnFactory.isub());
    pc++;
  }

  @Override public void iushr() {
    add( insnFactory.iushr());
    pc++;
  }

  @Override public void ixor() {
    add( insnFactory.ixor());
    pc++;
  }

  @Override public void jsr(int pcOffset) {
    add( insnFactory.jsr(pc + pcOffset));
    pc+=3;
  }

  @Override public void jsr_w(int pcOffset) {
    add( insnFactory.jsr_w(pc + pcOffset));
    pc+=5;
  }

  @Override public void l2d() {
    add( insnFactory.l2d());
    pc++;
  }

  @Override public void l2f() {
    add( insnFactory.l2f());
    pc++;
  }

  @Override public void l2i() {
    add( insnFactory.l2i());
    pc++;
  }

  @Override public void ladd() {
    add( insnFactory.ladd());
    pc++;
  }

  @Override public void laload() {
    add( insnFactory.laload());
    pc++;
  }

  @Override public void land() {
    add( insnFactory.land());
    pc++;
  }

  @Override public void lastore() {
    add( insnFactory.lastore());
    pc++;
  }

  @Override public void lcmp() {
    add( insnFactory.lcmp());
    pc++;
  }

  @Override public void lconst_0() {
    add( insnFactory.lconst_0());
    pc++;
  }

  @Override public void lconst_1() {
    add( insnFactory.lconst_1());
    pc++;
  }

  @Override public void ldc_(int cpIntOrFloatOrStringOrClassIndex) {
    Object v = cf.getCpValue(cpIntOrFloatOrStringOrClassIndex);
    switch (cf.getCpTag(cpIntOrFloatOrStringOrClassIndex)){
      case ClassFile.CONSTANT_INTEGER:
        add( insnFactory.ldc((Integer)v)); break;
      case ClassFile.CONSTANT_FLOAT:
        add( insnFactory.ldc((Float)v)); break;
      case ClassFile.CONSTANT_STRING:
        add( insnFactory.ldc((String)v, false)); break;
      case ClassFile.CONSTANT_CLASS:
        add( insnFactory.ldc((String)v, true)); break;
    }
    pc+=2;
  }

  @Override public void ldc_w_(int cpIntOrFloatOrStringOrClassIndex) {
    Object v = cf.getCpValue(cpIntOrFloatOrStringOrClassIndex);
    switch (cf.getCpTag(cpIntOrFloatOrStringOrClassIndex)){
      case ClassFile.CONSTANT_INTEGER:
        add( insnFactory.ldc_w((Integer) v)); break;
      case ClassFile.CONSTANT_FLOAT:
        add( insnFactory.ldc_w((Float) v)); break;
      case ClassFile.CONSTANT_STRING:
        add( insnFactory.ldc_w((String) v, false)); break;
      case ClassFile.CONSTANT_CLASS:
        add( insnFactory.ldc_w((String) v, true)); break;
    }
    pc+=3;
  }

  @Override public void ldc2_w(int cpLongOrDoubleIndex) {
    Object v = cf.getCpValue(cpLongOrDoubleIndex);
    if (v instanceof Long){
      add( insnFactory.ldc2_w((Long)v));
    } else {
      add( insnFactory.ldc2_w((Double)v));
    }
    pc+=3;
  }

  @Override public void ldiv() {
    add( insnFactory.ldiv());
    pc++;
  }

  @Override public void lload(int localVarIndex) {
    add( insnFactory.lload(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void lload_0() {
    add( insnFactory.lload(0));
    pc++;
  }

  @Override public void lload_1() {
    add( insnFactory.lload(1));
    pc++;
  }

  @Override public void lload_2() {
    add( insnFactory.lload(2));
    pc++;
  }

  @Override public void lload_3() {
    add( insnFactory.lload(3));
    pc++;
  }

  @Override public void lmul() {
    add( insnFactory.lmul());
    pc++;
  }

  @Override public void lneg() {
    add( insnFactory.lneg());
    pc++;
  }


  @Override public void lookupswitch(int defaultPcOffset, int nEntries) {
    Instruction insn = insnFactory.lookupswitch(pc + defaultPcOffset, nEntries);
    add( insn);

    lookupswitchInsn = (LookupSwitchInstruction)insn;

    if (cf != null){
      cf.parseLookupSwitchEntries(this, nEntries);
    }

    pc = ((pc+4)>>2)<<2; // opcode and padding
    pc += 8 + nEntries*8; // arguments and lookup table
  }
  @Override public void lookupswitchEntry(int index, int match, int pcOffset) {
    lookupswitchInsn.setTarget(index, match, pc + pcOffset);
  }

  @Override public void lor() {
    add( insnFactory.lor());
    pc++;
  }

  @Override public void lrem() {
    add( insnFactory.lrem());
    pc++;
  }

  @Override public void lreturn() {
    add( insnFactory.lreturn());
    pc++;
  }

  @Override public void lshl() {
    add( insnFactory.lshl());
    pc++;
  }

  @Override public void lshr() {
    add( insnFactory.lshr());
    pc++;
  }

  @Override public void lstore(int localVarIndex) {
    add( insnFactory.lstore(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void lstore_0() {
    add( insnFactory.lstore(0));
    pc++;
  }

  @Override public void lstore_1() {
    add( insnFactory.lstore(1));
    pc++;
  }

  @Override public void lstore_2() {
    add( insnFactory.lstore(2));
    pc++;
  }

  @Override public void lstore_3() {
    add( insnFactory.lstore(3));
    pc++;
  }

  @Override public void lsub() {
    add( insnFactory.lsub());
    pc++;
  }

  @Override public void lushr() {
    add( insnFactory.lushr());
    pc++;
  }

  @Override public void lxor() {
    add( insnFactory.lxor());
    pc++;
  }

  @Override public void monitorenter() {
    add( insnFactory.monitorenter());
    pc++;
  }

  @Override public void monitorexit() {
    add( insnFactory.monitorexit());
    pc++;
  }

  @Override public void multianewarray(int cpClassIndex, int dimensions) {
    add( insnFactory.multianewarray(cf.classNameAt(cpClassIndex), dimensions));
    pc+=4;
  }

  @Override public void new_(int cpClassIndex) {
    add( insnFactory.new_(cf.classNameAt(cpClassIndex)));
    pc+=3;
  }
  public void new_(String className) {
    add( insnFactory.new_(className));
    pc+=3;
  }

  @Override public void newarray(int typeCode) {
    add( insnFactory.newarray(typeCode));
    pc+=2;
  }

  @Override public void nop() {
    add( insnFactory.nop());
    pc++;
  }

  @Override public void pop() {
    add( insnFactory.pop());
    pc++;
  }

  @Override public void pop2() {
    add( insnFactory.pop2());
    pc++;
  }

  @Override public void putfield(int cpFieldRefIndex) {
    String fieldName = cf.fieldNameAt(cpFieldRefIndex);
    String clsName = cf.fieldClassNameAt(cpFieldRefIndex);
    String fieldDescriptor = cf.fieldDescriptorAt(cpFieldRefIndex);

    add( insnFactory.putfield(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }
  public void putfield(String fieldName, String clsName, String fieldDescriptor){
    add( insnFactory.putfield(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }


  @Override public void putstatic(int cpFieldRefIndex) {
    String fieldName = cf.fieldNameAt(cpFieldRefIndex);
    String clsName = cf.fieldClassNameAt(cpFieldRefIndex);
    String fieldDescriptor = cf.fieldDescriptorAt(cpFieldRefIndex);

    add( insnFactory.putstatic(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }
  public void putstatic(String fieldName, String clsName, String fieldDescriptor){
    add( insnFactory.putstatic(fieldName, clsName, fieldDescriptor));
    pc+=3;
  }


  @Override public void ret(int localVarIndex) {
    add( insnFactory.ret(localVarIndex));
    pc+=2;
    if (isWide){
      pc++;
      isWide = false;
    }
  }

  @Override public void return_() {
    add( insnFactory.return_());
    pc++;
  }

  @Override public void saload() {
    add( insnFactory.saload());
    pc++;
  }

  @Override public void sastore() {
    add( insnFactory.sastore());
    pc++;
  }

  @Override public void sipush(int val) {
    add( insnFactory.sipush(val));
    pc+=3;
  }

  @Override public void swap() {
    add( insnFactory.swap());
    pc++;
  }

  @Override public void tableswitch(int defaultPcOffset, int low, int high) {
    Instruction insn = insnFactory.tableswitch(pc + defaultPcOffset, low, high);
    add( insn);
    
    tableswitchInsn = (TableSwitchInstruction)insn;

    if (cf != null){
      cf.parseTableSwitchEntries(this, low, high);
    }

    pc = ((pc+4)>>2)<<2; // opcode and padding
    pc+=12 + (high-low+1)*4; // the fixed args and jump table
  }

  @Override public void tableswitchEntry(int value, int pcOffset) {
    tableswitchInsn.setTarget(value, pc + pcOffset);
  }

  @Override public void wide() {
    add( insnFactory.wide());
    pc++;
    isWide = true;
  }

  @Override public void unknown(int bytecode) {
    throw new JPFException("unknown bytecode: " + Integer.toHexString(bytecode));
  }


  //--- the JPF specific ones (only used in synthetic methods)
  public void invokecg(List<Invocation> invokes) {
    add (insnFactory.invokecg(invokes));
    pc++;
  }

  public void invokeclinit(ClassInfo ci) {
    add( insnFactory.invokeclinit(ci));
    pc++;
  }

  public void finishclinit (ClassInfo ci){
    add (insnFactory.finishclinit(ci));
    pc++;
  }

  public void directcallreturn(){
    add( insnFactory.directcallreturn());
    pc++;
  }

  public void executenative(NativeMethodInfo mi){
    add( insnFactory.executenative(mi));
    pc++;
  }

  public void nativereturn(){
    add( insnFactory.nativereturn());
    pc++;
  }

  public void runStart (MethodInfo mi){
    add( insnFactory.runstart(mi));
    pc++;
  }

}
