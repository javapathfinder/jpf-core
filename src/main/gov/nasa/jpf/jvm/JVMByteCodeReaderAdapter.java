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

import gov.nasa.jpf.jvm.JVMByteCodeReader;

/**
 * empty implementation of a JVMByteCodeReader, to efficiently allow overriding
 * single methods
 */
public class JVMByteCodeReaderAdapter implements JVMByteCodeReader {

  @Override
  public void aconst_null() {}
  @Override
  public void aload(int localVarIndex) {}
  @Override
  public void aload_0() {}
  @Override
  public void aload_1() {}
  @Override
  public void aload_2() {}
  @Override
  public void aload_3() {}
  @Override
  public void aaload() {}
  @Override
  public void astore(int localVarIndex) {}
  @Override
  public void astore_0() {}
  @Override
  public void astore_1() {}
  @Override
  public void astore_2() {}
  @Override
  public void astore_3() {}
  @Override
  public void aastore() {}
  @Override
  public void areturn() {}
  @Override
  public void anewarray(int cpClassIndex) {}
  @Override
  public void arraylength() {}
  @Override
  public void athrow() {}

  @Override
  public void baload() {}
  @Override
  public void bastore() {}
  @Override
  public void bipush(int b) {}

  @Override
  public void caload() {}
  @Override
  public void castore() {}
  @Override
  public void checkcast(int cpClassIndex) {}

  @Override
  public void d2f() {}
  @Override
  public void d2i() {}
  @Override
  public void d2l() {}
  @Override
  public void dadd() {}
  @Override
  public void daload() {}
  @Override
  public void dastore() {}
  @Override
  public void dcmpg() {}
  @Override
  public void dcmpl() {}
  @Override
  public void dconst_0() {}
  @Override
  public void dconst_1() {}
  @Override
  public void ddiv() {}
  @Override
  public void dload(int localVarIndex) {}
  @Override
  public void dload_0() {}
  @Override
  public void dload_1() {}
  @Override
  public void dload_2() {}
  @Override
  public void dload_3() {}
  @Override
  public void dmul() {}
  @Override
  public void dneg() {}
  @Override
  public void drem() {}
  @Override
  public void dreturn() {}
  @Override
  public void dstore(int localVarIndex) {}
  @Override
  public void dstore_0() {}
  @Override
  public void dstore_1() {}
  @Override
  public void dstore_2() {}
  @Override
  public void dstore_3() {}
  @Override
  public void dsub() {}
  @Override
  public void dup() {}
  @Override
  public void dup_x1() {}
  @Override
  public void dup_x2() {}
  @Override
  public void dup2() {}
  @Override
  public void dup2_x1() {}
  @Override
  public void dup2_x2() {}

  @Override
  public void f2d() {}
  @Override
  public void f2i() {}
  @Override
  public void f2l() {}
  @Override
  public void fadd() {}
  @Override
  public void faload() {}
  @Override
  public void fastore() {}
  @Override
  public void fcmpg() {}
  @Override
  public void fcmpl() {}
  @Override
  public void fconst_0() {}
  @Override
  public void fconst_1() {}
  @Override
  public void fconst_2() {}
  @Override
  public void fdiv() {}
  @Override
  public void fload(int localVarIndex) {}
  @Override
  public void fload_0() {}
  @Override
  public void fload_1() {}
  @Override
  public void fload_2() {}
  @Override
  public void fload_3() {}
  @Override
  public void fmul() {}
  @Override
  public void fneg() {}
  @Override
  public void frem() {}
  @Override
  public void freturn() {}
  @Override
  public void fstore(int localVarIndex) {}
  @Override
  public void fstore_0() {}
  @Override
  public void fstore_1() {}
  @Override
  public void fstore_2() {}
  @Override
  public void fstore_3() {}
  @Override
  public void fsub() {}

  @Override
  public void getfield(int cpFieldRefIndex) {}
  @Override
  public void getstatic(int cpFieldRefIndex) {}
  @Override
  public void goto_(int pcOffset) {}
  @Override
  public void goto_w (int pcOffset) {}

  @Override
  public void i2b() {}
  @Override
  public void i2c() {}
  @Override
  public void i2d() {}
  @Override
  public void i2f() {}
  @Override
  public void i2l() {}
  @Override
  public void i2s() {}
  @Override
  public void iadd() {}
  @Override
  public void iaload() {}
  @Override
  public void iand() {}
  @Override
  public void iastore() {}
  @Override
  public void iconst_m1() {}
  @Override
  public void iconst_0() {}
  @Override
  public void iconst_1() {}
  @Override
  public void iconst_2() {}
  @Override
  public void iconst_3() {}
  @Override
  public void iconst_4() {}
  @Override
  public void iconst_5() {}
  @Override
  public void idiv() {}
  @Override
  public void if_acmpeq(int pcOffset) {}
  @Override
  public void if_acmpne(int pcOffset) {}
  @Override
  public void if_icmpeq(int pcOffset) {}
  @Override
  public void if_icmpne(int pcOffset) {}
  @Override
  public void if_icmplt(int pcOffset) {}
  @Override
  public void if_icmpge(int pcOffset) {}
  @Override
  public void if_icmpgt(int pcOffset) {}
  @Override
  public void if_icmple(int pcOffset) {}
  @Override
  public void ifeq(int pcOffset) {}
  @Override
  public void ifne(int pcOffset) {}
  @Override
  public void iflt(int pcOffset) {}
  @Override
  public void ifge(int pcOffset) {}
  @Override
  public void ifgt(int pcOffset) {}
  @Override
  public void ifle(int pcOffset) {}
  @Override
  public void ifnonnull(int pcOffset) {}
  @Override
  public void ifnull(int pcOffset) {}
  @Override
  public void iinc(int localVarIndex, int incConstant) {}
  @Override
  public void iload(int localVarIndex) {}
  @Override
  public void iload_0() {}
  @Override
  public void iload_1() {}
  @Override
  public void iload_2() {}
  @Override
  public void iload_3() {}
  @Override
  public void imul() {}
  @Override
  public void ineg() {}
  @Override
  public void instanceof_(int cpClassIndex) {}
  @Override
  public void invokeinterface (int cpInterfaceMethodRefIndex, int count, int zero) {}
  @Override
  public void invokedynamic (int cpInvokeDynamicIndex) {}
  @Override
  public void invokespecial (int cpMethodRefIndex) {}
  @Override
  public void invokestatic (int cpMethodRefIndex) {}
  @Override
  public void invokevirtual (int cpMethodRefIndex) {}
  @Override
  public void ior() {}
  @Override
  public void irem() {}
  @Override
  public void ireturn() {}
  @Override
  public void ishl() {}
  @Override
  public void ishr() {}
  @Override
  public void istore(int localVarIndex) {}
  @Override
  public void istore_0() {}
  @Override
  public void istore_1() {}
  @Override
  public void istore_2() {}
  @Override
  public void istore_3() {}
  @Override
  public void isub() {}
  @Override
  public void iushr() {}
  @Override
  public void ixor() {}

  @Override
  public void jsr(int pcOffset) {}
  @Override
  public void jsr_w(int pcOffset) {}

  @Override
  public void l2d() {}
  @Override
  public void l2f() {}
  @Override
  public void l2i() {}
  @Override
  public void ladd() {}
  @Override
  public void laload() {}
  @Override
  public void land() {}
  @Override
  public void lastore() {}
  @Override
  public void lcmp() {}
  @Override
  public void lconst_0() {}
  @Override
  public void lconst_1() {}
  @Override
  public void ldc_(int cpIntOrFloatOrStringIndex) {}
  @Override
  public void ldc_w_(int cpIntOrFloatOrStringIndex) {}
  @Override
  public void ldc2_w(int cpLongOrDoubleIndex) {}
  @Override
  public void ldiv() {}
  @Override
  public void lload(int localVarIndex) {}
  @Override
  public void lload_0() {}
  @Override
  public void lload_1() {}
  @Override
  public void lload_2() {}
  @Override
  public void lload_3() {}
  @Override
  public void lmul() {}
  @Override
  public void lneg() {}
  @Override
  public void lookupswitch(int defaultPcOffset, int nEntries) {}
  @Override
  public void lookupswitchEntry(int index, int match, int pcOffset) {}
  @Override
  public void lor() {}
  @Override
  public void lrem() {}
  @Override
  public void lreturn() {}
  @Override
  public void lshl() {}
  @Override
  public void lshr() {}
  @Override
  public void lstore(int localVarIndex) {}
  @Override
  public void lstore_0() {}
  @Override
  public void lstore_1() {}
  @Override
  public void lstore_2() {}
  @Override
  public void lstore_3() {}
  @Override
  public void lsub() {}
  @Override
  public void lushr() {}
  @Override
  public void lxor() {}

  @Override
  public void monitorenter() {}
  @Override
  public void monitorexit() {}
  @Override
  public void multianewarray(int cpClassIndex, int dimensions) {}

  @Override
  public void new_(int cpClassIndex) {}
  @Override
  public void newarray(int typeCode) {}
  @Override
  public void nop() {}

  @Override
  public void pop() {}
  @Override
  public void pop2() {}
  @Override
  public void putfield(int cpFieldRefIndex) {}
  @Override
  public void putstatic(int cpFieldRefIndex) {}

  @Override
  public void ret(int localVarIndex) {}
  @Override
  public void return_() {}

  @Override
  public void saload() {}
  @Override
  public void sastore() {}
  @Override
  public void sipush(int val) {}
  @Override
  public void swap() {}

  @Override
  public void tableswitch(int defaultPcOffset, int low, int high) {}
  @Override
  public void tableswitchEntry(int value, int pcOffset) {}

  @Override
  public void wide () {}

  @Override
  public void unknown(int bytecode) {}
}
