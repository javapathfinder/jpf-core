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

package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.JVMInstructionFactory;
import gov.nasa.jpf.util.Invocation;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;

import java.util.List;

/**
 * this is the new JVMInstructionFactory
 */
public class InstructionFactory extends JVMInstructionFactory {

  public InstructionFactory(){
    // nothing here
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("InstructionFactory " + this.getClass().getName() + " does not support cloning");
    }
  }

  //--- the factory methods
  @Override
  public Instruction aconst_null() {
    return new ACONST_NULL();
  }

  @Override
  public Instruction aload(int localVarIndex) {
    return new ALOAD(localVarIndex);
  }

  @Override
  public Instruction aload_0() {
    return new ALOAD(0);
  }

  @Override
  public Instruction aload_1() {
    return new ALOAD(1);
  }

  @Override
  public Instruction aload_2() {
    return new ALOAD(2);
  }

  @Override
  public Instruction aload_3() {
    return new ALOAD(3);
  }

  @Override
  public Instruction aaload() {
    return new AALOAD();
  }

  @Override
  public Instruction astore(int localVarIndex) {
    return new ASTORE(localVarIndex);
  }

  @Override
  public Instruction astore_0() {
    return new ASTORE(0);
  }

  @Override
  public Instruction astore_1() {
    return new ASTORE(1);
  }

  @Override
  public Instruction astore_2() {
    return new ASTORE(2);
  }

  @Override
  public Instruction astore_3() {
    return new ASTORE(3);
  }

  @Override
  public Instruction aastore() {
    return new AASTORE();
  }

  @Override
  public Instruction areturn() {
    return new ARETURN();
  }

  @Override
  public Instruction anewarray(String clsName){
    return new ANEWARRAY(clsName);
  }

  @Override
  public Instruction arraylength() {
    return new ARRAYLENGTH();
  }

  @Override
  public Instruction athrow() {
    return new ATHROW();
  }

  @Override
  public Instruction baload() {
    return new BALOAD();
  }

  @Override
  public Instruction bastore() {
    return new BASTORE();
  }

  @Override
  public Instruction bipush(int b) {
    return new BIPUSH(b);
  }

  @Override
  public Instruction caload() {
    return new CALOAD();
  }

  @Override
  public Instruction castore() {
    return new CASTORE();
  }

  @Override
  public Instruction checkcast(String clsName){
    return new CHECKCAST(clsName);
  }

  @Override
  public Instruction d2f() {
    return new D2F();
  }

  @Override
  public Instruction d2i() {
    return new D2I();
  }

  @Override
  public Instruction d2l() {
    return new D2L();
  }

  @Override
  public Instruction dadd() {
    return new DADD();
  }

  @Override
  public Instruction daload() {
    return new DALOAD();
  }

  @Override
  public Instruction dastore() {
    return new DASTORE();
  }

  @Override
  public Instruction dcmpg() {
    return new DCMPG();
  }

  @Override
  public Instruction dcmpl() {
    return new DCMPL();
  }

  @Override
  public Instruction dconst_0() {
    return new DCONST(0.0);
  }

  @Override
  public Instruction dconst_1() {
    return new DCONST(1.0);
  }

  @Override
  public Instruction ddiv() {
    return new DDIV();
  }

  @Override
  public Instruction dload(int localVarIndex) {
    return new DLOAD(localVarIndex);
  }

  @Override
  public Instruction dload_0() {
    return new DLOAD(0);
  }

  @Override
  public Instruction dload_1() {
    return new DLOAD(1);
  }

  @Override
  public Instruction dload_2() {
    return new DLOAD(2);
  }

  @Override
  public Instruction dload_3() {
    return new DLOAD(3);
  }

  @Override
  public Instruction dmul() {
    return new DMUL();
  }

  @Override
  public Instruction dneg() {
    return new DNEG();
  }

  @Override
  public Instruction drem() {
    return new DREM();
  }

  @Override
  public Instruction dreturn() {
    return new DRETURN();
  }

  @Override
  public Instruction dstore(int localVarIndex) {
    return new DSTORE(localVarIndex);
  }

  @Override
  public Instruction dstore_0() {
    return new DSTORE(0);
  }

  @Override
  public Instruction dstore_1() {
    return new DSTORE(1);
  }

  @Override
  public Instruction dstore_2() {
    return new DSTORE(2);
  }

  @Override
  public Instruction dstore_3() {
    return new DSTORE(3);
  }

  @Override
  public Instruction dsub() {
    return new DSUB();
  }

  @Override
  public Instruction dup() {
    return new DUP();
  }

  @Override
  public Instruction dup_x1() {
    return new DUP_X1();
  }

  @Override
  public Instruction dup_x2() {
    return new DUP_X2();
  }

  @Override
  public Instruction dup2() {
    return new DUP2();
  }

  @Override
  public Instruction dup2_x1() {
    return new DUP2_X1();
  }

  @Override
  public Instruction dup2_x2() {
    return new DUP2_X2();
  }

  @Override
  public Instruction f2d() {
    return new F2D();
  }

  @Override
  public Instruction f2i() {
    return new F2I();
  }

  @Override
  public Instruction f2l() {
    return new F2L();
  }

  @Override
  public Instruction fadd() {
    return new FADD();
  }

  @Override
  public Instruction faload() {
    return new FALOAD();
  }

  @Override
  public Instruction fastore() {
    return new FASTORE();
  }

  @Override
  public Instruction fcmpg() {
    return new FCMPG();
  }

  @Override
  public Instruction fcmpl() {
    return new FCMPL();
  }

  @Override
  public Instruction fconst_0() {
    return new FCONST(0.0f);
  }

  @Override
  public Instruction fconst_1() {
    return new FCONST(1.0f);
  }

  @Override
  public Instruction fconst_2() {
    return new FCONST(2.0f);
  }

  @Override
  public Instruction fdiv() {
    return new FDIV();
  }

  @Override
  public Instruction fload(int localVarIndex) {
    return new FLOAD(localVarIndex);
  }

  @Override
  public Instruction fload_0() {
    return new FLOAD(0);
  }

  @Override
  public Instruction fload_1() {
    return new FLOAD(1);
  }

  @Override
  public Instruction fload_2() {
    return new FLOAD(2);
  }

  @Override
  public Instruction fload_3() {
    return new FLOAD(3);
  }

  @Override
  public Instruction fmul() {
    return new FMUL();
  }

  @Override
  public Instruction fneg() {
    return new FNEG();
  }

  @Override
  public Instruction frem() {
    return new FREM();
  }

  @Override
  public Instruction freturn() {
    return new FRETURN();
  }

  @Override
  public Instruction fstore(int localVarIndex) {
    return new FSTORE(localVarIndex);
  }

  @Override
  public Instruction fstore_0() {
    return new FSTORE(0);
  }

  @Override
  public Instruction fstore_1() {
    return new FSTORE(1);
  }

  @Override
  public Instruction fstore_2() {
    return new FSTORE(2);
  }

  @Override
  public Instruction fstore_3() {
    return new FSTORE(3);
  }

  @Override
  public Instruction fsub() {
    return new FSUB();
  }

  @Override
  public Instruction getfield(String fieldName, String clsName, String fieldDescriptor){
    return new GETFIELD(fieldName, clsName, fieldDescriptor);
  }

  @Override
  public Instruction getstatic(String fieldName, String clsName, String fieldDescriptor){
    return new GETSTATIC(fieldName, clsName, fieldDescriptor);
  }


  @Override
  public Instruction goto_(int targetPc) {
    return new GOTO(targetPc);
  }

  @Override
  public Instruction goto_w(int targetPc) {
    return new GOTO_W(targetPc);
  }

  @Override
  public Instruction i2b() {
    return new I2B();
  }

  @Override
  public Instruction i2c() {
    return new I2C();
  }

  @Override
  public Instruction i2d() {
    return new I2D();
  }

  @Override
  public Instruction i2f() {
    return new I2F();
  }

  @Override
  public Instruction i2l() {
    return new I2L();
  }

  @Override
  public Instruction i2s() {
    return new I2S();
  }

  @Override
  public Instruction iadd() {
    return new IADD();
  }

  @Override
  public Instruction iaload() {
    return new IALOAD();
  }

  @Override
  public Instruction iand() {
    return new IAND();
  }

  @Override
  public Instruction iastore() {
    return new IASTORE();
  }

  @Override
  public Instruction iconst_m1() {
    return new ICONST(-1);
  }

  @Override
  public Instruction iconst_0() {
    return new ICONST(0);
  }

  @Override
  public Instruction iconst_1() {
    return new ICONST(1);
  }

  @Override
  public Instruction iconst_2() {
    return new ICONST(2);
  }

  @Override
  public Instruction iconst_3() {
    return new ICONST(3);
  }

  @Override
  public Instruction iconst_4() {
    return new ICONST(4);
  }

  @Override
  public Instruction iconst_5() {
    return new ICONST(5);
  }

  @Override
  public Instruction idiv() {
    return new IDIV();
  }

  @Override
  public Instruction if_acmpeq(int targetPc) {
    return new IF_ACMPEQ(targetPc);
  }

  @Override
  public Instruction if_acmpne(int targetPc) {
    return new IF_ACMPNE(targetPc);
  }

  @Override
  public Instruction if_icmpeq(int targetPc) {
    return new IF_ICMPEQ(targetPc);
  }

  @Override
  public Instruction if_icmpne(int targetPc) {
    return new IF_ICMPNE(targetPc);
  }

  @Override
  public Instruction if_icmplt(int targetPc) {
    return new IF_ICMPLT(targetPc);
  }

  @Override
  public Instruction if_icmpge(int targetPc) {
    return new IF_ICMPGE(targetPc);
  }

  @Override
  public Instruction if_icmpgt(int targetPc) {
    return new IF_ICMPGT(targetPc);
  }

  @Override
  public Instruction if_icmple(int targetPc) {
    return new IF_ICMPLE(targetPc);
  }

  @Override
  public Instruction ifeq(int targetPc) {
    return new IFEQ(targetPc);
  }

  @Override
  public Instruction ifne(int targetPc) {
    return new IFNE(targetPc);
  }

  @Override
  public Instruction iflt(int targetPc) {
    return new IFLT(targetPc);
  }

  @Override
  public Instruction ifge(int targetPc) {
    return new IFGE(targetPc);
  }

  @Override
  public Instruction ifgt(int targetPc) {
    return new IFGT(targetPc);
  }

  @Override
  public Instruction ifle(int targetPc) {
    return new IFLE(targetPc);
  }

  @Override
  public Instruction ifnonnull(int targetPc) {
    return new IFNONNULL(targetPc);
  }

  @Override
  public Instruction ifnull(int targetPc) {
    return new IFNULL(targetPc);
  }

  @Override
  public Instruction iinc(int localVarIndex, int incConstant) {
    return new IINC(localVarIndex, incConstant);
  }

  @Override
  public Instruction iload(int localVarIndex) {
    return new ILOAD(localVarIndex);
  }

  @Override
  public Instruction iload_0() {
    return new ILOAD(0);
  }

  @Override
  public Instruction iload_1() {
    return new ILOAD(1);
  }

  @Override
  public Instruction iload_2() {
    return new ILOAD(2);
  }

  @Override
  public Instruction iload_3() {
    return new ILOAD(3);
  }

  @Override
  public Instruction imul() {
    return new IMUL();
  }

  @Override
  public Instruction ineg() {
    return new INEG();
  }

  @Override
  public Instruction instanceof_(String clsName){
    return new INSTANCEOF(clsName);
  }

  @Override
  public Instruction invokeinterface(String clsName, String methodName, String methodSignature){
    return new INVOKEINTERFACE(clsName, methodName, methodSignature);
  }

  @Override
  public Instruction invokespecial(String clsName, String methodName, String methodSignature){
    return new INVOKESPECIAL(clsName, methodName, methodSignature);
  }

  @Override
  public Instruction invokestatic(String clsName, String methodName, String methodSignature){
    return new INVOKESTATIC(clsName, methodName, methodSignature);
  }

  @Override
  public Instruction invokevirtual(String clsName, String methodName, String methodSignature){
    return new INVOKEVIRTUAL(clsName, methodName, methodSignature);
  }

  @Override
  public Instruction invokedynamic(int bootstrapIndex, String samMethodName, String functionType){
    return new INVOKEDYNAMIC(bootstrapIndex, samMethodName, functionType);
  }

  @Override
  public Instruction ior() {
    return new IOR();
  }

  @Override
  public Instruction irem() {
    return new IREM();
  }

  @Override
  public Instruction ireturn() {
    return new IRETURN();
  }

  @Override
  public Instruction ishl() {
    return new ISHL();
  }

  @Override
  public Instruction ishr() {
    return new ISHR();
  }

  @Override
  public Instruction istore(int localVarIndex) {
    return new ISTORE(localVarIndex);
  }

  @Override
  public Instruction istore_0() {
    return new ISTORE(0);
  }

  @Override
  public Instruction istore_1() {
    return new ISTORE(1);
  }

  @Override
  public Instruction istore_2() {
    return new ISTORE(2);
  }

  @Override
  public Instruction istore_3() {
    return new ISTORE(3);
  }

  @Override
  public Instruction isub() {
    return new ISUB();
  }

  @Override
  public Instruction iushr() {
    return new IUSHR();
  }

  @Override
  public Instruction ixor() {
    return new IXOR();
  }

  @Override
  public Instruction jsr(int targetPc) {
    return new JSR(targetPc);
  }

  @Override
  public Instruction jsr_w(int targetPc) {
    return new JSR_W(targetPc);
  }

  @Override
  public Instruction l2d() {
    return new L2D();
  }

  @Override
  public Instruction l2f() {
    return new L2F();
  }

  @Override
  public Instruction l2i() {
    return new L2I();
  }

  @Override
  public Instruction ladd() {
    return new LADD();
  }

  @Override
  public Instruction laload() {
    return new LALOAD();
  }

  @Override
  public Instruction land() {
    return new LAND();
  }

  @Override
  public Instruction lastore() {
    return new LASTORE();
  }

  @Override
  public Instruction lcmp() {
    return new LCMP();
  }

  @Override
  public Instruction lconst_0() {
    return new LCONST(0);
  }

  @Override
  public Instruction lconst_1() {
    return new LCONST(1L);
  }

  @Override
  public Instruction ldc(int v){
    return new LDC(v);
  }
  @Override
  public Instruction ldc(float v){
    return new LDC(v);
  }
  @Override
  public Instruction ldc(String v, boolean isClass){
    return new LDC(v, isClass);
  }


  @Override
  public Instruction ldc_w(int v){
    return new LDC_W(v);
  }
  @Override
  public Instruction ldc_w(float v){
    return new LDC_W(v);
  }
  @Override
  public Instruction ldc_w(String v, boolean isClass){
    return new LDC_W(v, isClass);
  }

  @Override
  public Instruction ldc2_w(long v){
    return new LDC2_W(v);
  }
  @Override
  public Instruction ldc2_w(double v){
    return new LDC2_W(v);
  }

  @Override
  public Instruction ldiv() {
    return new LDIV();
  }

  @Override
  public Instruction lload(int localVarIndex) {
    return new LLOAD(localVarIndex);
  }

  @Override
  public Instruction lload_0() {
    return new LLOAD(0);
  }

  @Override
  public Instruction lload_1() {
    return new LLOAD(1);
  }

  @Override
  public Instruction lload_2() {
    return new LLOAD(2);
  }

  @Override
  public Instruction lload_3() {
    return new LLOAD(3);
  }

  @Override
  public Instruction lmul() {
    return new LMUL();
  }

  @Override
  public Instruction lneg() {
    return new LNEG();
  }

  @Override
  public Instruction lookupswitch(int defaultTargetPc, int nEntries) {
    return new LOOKUPSWITCH(defaultTargetPc, nEntries);
  }

  @Override
  public Instruction lor() {
    return new LOR();
  }

  @Override
  public Instruction lrem() {
    return new LREM();
  }

  @Override
  public Instruction lreturn() {
    return new LRETURN();
  }

  @Override
  public Instruction lshl() {
    return new LSHL();
  }

  @Override
  public Instruction lshr() {
    return new LSHR();
  }

  @Override
  public Instruction lstore(int localVarIndex) {
    return new LSTORE(localVarIndex);
  }

  @Override
  public Instruction lstore_0() {
    return new LSTORE(0);
  }

  @Override
  public Instruction lstore_1() {
    return new LSTORE(1);
  }

  @Override
  public Instruction lstore_2() {
    return new LSTORE(2);
  }

  @Override
  public Instruction lstore_3() {
    return new LSTORE(3);
  }

  @Override
  public Instruction lsub() {
    return new LSUB();
  }

  @Override
  public Instruction lushr() {
    return new LUSHR();
  }

  @Override
  public Instruction lxor() {
    return new LXOR();
  }

  @Override
  public Instruction monitorenter() {
    return new MONITORENTER();
  }

  @Override
  public Instruction monitorexit() {
    return new MONITOREXIT();
  }

  @Override
  public Instruction multianewarray(String clsName, int dimensions){
    return new MULTIANEWARRAY(clsName, dimensions);
  }

  @Override
  public Instruction new_(String clsName) {
    return new NEW(clsName);
  }

  @Override
  public Instruction newarray(int typeCode) {
    return new NEWARRAY(typeCode);
  }

  @Override
  public Instruction nop() {
    return new NOP();
  }

  @Override
  public Instruction pop() {
    return new POP();
  }

  @Override
  public Instruction pop2() {
    return new POP2();
  }

  @Override
  public Instruction putfield(String fieldName, String clsName, String fieldDescriptor){
    return new PUTFIELD(fieldName, clsName, fieldDescriptor);
  }

  @Override
  public Instruction putstatic(String fieldName, String clsName, String fieldDescriptor){
    return new PUTSTATIC(fieldName, clsName, fieldDescriptor);
  }

  @Override
  public Instruction ret(int localVarIndex) {
    return new RET(localVarIndex);
  }

  @Override
  public Instruction return_() {
    return new RETURN();
  }

  @Override
  public Instruction saload() {
    return new SALOAD();
  }

  @Override
  public Instruction sastore() {
    return new SASTORE();
  }

  @Override
  public Instruction sipush(int val) {
    return new SIPUSH(val);
  }

  @Override
  public Instruction swap() {
    return new SWAP();
  }

  @Override
  public Instruction tableswitch(int defaultTargetPc, int low, int high) {
    return new TABLESWITCH(defaultTargetPc, low, high);
  }

  @Override
  public Instruction wide() {
    return new WIDE();
  }

  
  //--- the JPF specific ones (only used in synthetic methods)
  @Override
  public Instruction invokecg(List<Invocation> invokes) {
    return new INVOKECG(invokes);
  }

  @Override
  public Instruction invokeclinit(ClassInfo ci) {
    return new INVOKECLINIT(ci);
  }

  @Override
  public Instruction directcallreturn(){
    return new DIRECTCALLRETURN();
  }

  @Override
  public Instruction executenative(NativeMethodInfo mi){
    return new EXECUTENATIVE(mi);
  }

  @Override
  public Instruction nativereturn(){
    return new NATIVERETURN();
  }

  // this is never part of MethodInfo stored code
  @Override
  public Instruction runstart(MethodInfo miRun){
    return new RUNSTART();
  }

  @Override
  public Instruction finishclinit(ClassInfo ci) {
    return new FINISHCLINIT(ci);
  }

}
