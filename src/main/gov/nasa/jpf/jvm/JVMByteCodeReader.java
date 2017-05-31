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

/**
 * interface to process bytecode
 */
public interface JVMByteCodeReader {

  void aconst_null();
  void aload(int localVarIndex);
  void aload_0();
  void aload_1();
  void aload_2();
  void aload_3();
  void aaload();
  void astore(int localVarIndex);
  void astore_0();
  void astore_1();
  void astore_2();
  void astore_3();
  void aastore();
  void areturn();
  void anewarray(int cpClassIndex);
  void arraylength();
  void athrow();

  void baload();
  void bastore();
  void bipush(int b);

  void caload();
  void castore();
  void checkcast(int cpClassIndex);

  void d2f();
  void d2i();
  void d2l();
  void dadd();
  void daload();
  void dastore();
  void dcmpg();
  void dcmpl();
  void dconst_0();
  void dconst_1();
  void ddiv();
  void dload(int localVarIndex);
  void dload_0();
  void dload_1();
  void dload_2();
  void dload_3();
  void dmul();
  void dneg();
  void drem();
  void dreturn();
  void dstore(int localVarIndex);
  void dstore_0();
  void dstore_1();
  void dstore_2();
  void dstore_3();
  void dsub();
  void dup();
  void dup_x1();
  void dup_x2();
  void dup2();
  void dup2_x1();
  void dup2_x2();

  void f2d();
  void f2i();
  void f2l();
  void fadd();
  void faload();
  void fastore();
  void fcmpg();
  void fcmpl();
  void fconst_0();
  void fconst_1();
  void fconst_2();
  void fdiv();
  void fload(int localVarIndex);
  void fload_0();
  void fload_1();
  void fload_2();
  void fload_3();
  void fmul();
  void fneg();
  void frem();
  void freturn();
  void fstore(int localVarIndex);
  void fstore_0();
  void fstore_1();
  void fstore_2();
  void fstore_3();
  void fsub();

  void getfield(int cpFieldRefIndex);
  void getstatic(int cpFieldRefIndex);
  void goto_(int pcOffset);
  void goto_w (int pcOffset);

  void i2b();
  void i2c();
  void i2d();
  void i2f();
  void i2l();
  void i2s();
  void iadd();
  void iaload();
  void iand();
  void iastore();
  void iconst_m1();
  void iconst_0();
  void iconst_1();
  void iconst_2();
  void iconst_3();
  void iconst_4();
  void iconst_5();
  void idiv();
  void if_acmpeq(int pcOffset);
  void if_acmpne(int pcOffset);
  void if_icmpeq(int pcOffset);
  void if_icmpne(int pcOffset);
  void if_icmplt(int pcOffset);
  void if_icmpge(int pcOffset);
  void if_icmpgt(int pcOffset);
  void if_icmple(int pcOffset);
  void ifeq(int pcOffset);
  void ifne(int pcOffset);
  void iflt(int pcOffset);
  void ifge(int pcOffset);
  void ifgt(int pcOffset);
  void ifle(int pcOffset);
  void ifnonnull(int pcOffset);
  void ifnull(int pcOffset);
  void iinc(int localVarIndex, int incConstant);
  void iload(int localVarIndex);
  void iload_0();
  void iload_1();
  void iload_2();
  void iload_3();
  void imul();
  void ineg();
  void instanceof_(int cpClassIndex);
  void invokeinterface (int cpInterfaceMethodRefIndex, int count, int zero);
  void invokedynamic (int cpInvokeDynamicIndex);
  void invokespecial (int cpMethodRefIndex);
  void invokestatic (int cpMethodRefIndex);
  void invokevirtual (int cpMethodRefIndex);
  void ior();
  void irem();
  void ireturn();
  void ishl();
  void ishr();
  void istore(int localVarIndex);
  void istore_0();
  void istore_1();
  void istore_2();
  void istore_3();
  void isub();
  void iushr();
  void ixor();

  void jsr(int pcOffset);
  void jsr_w(int pcOffset);

  void l2d();
  void l2f();
  void l2i();
  void ladd();
  void laload();
  void land();
  void lastore();
  void lcmp();
  void lconst_0();
  void lconst_1();
  void ldc_(int cpIntOrFloatOrStringIndex);
  void ldc_w_(int cpIntOrFloatOrStringIndex);
  void ldc2_w(int cpLongOrDoubleIndex);
  void ldiv();
  void lload(int localVarIndex);
  void lload_0();
  void lload_1();
  void lload_2();
  void lload_3();
  void lmul();
  void lneg();
  void lookupswitch(int defaultPcOffset, int nEntries);
  void lookupswitchEntry(int index, int match, int pcOffset);
  void lor();
  void lrem();
  void lreturn();
  void lshl();
  void lshr();
  void lstore(int localVarIndex);
  void lstore_0();
  void lstore_1();
  void lstore_2();
  void lstore_3();
  void lsub();
  void lushr();
  void lxor();

  void monitorenter();
  void monitorexit();
  void multianewarray(int cpClassIndex, int dimensions);

  void new_(int cpClassIndex);
  void newarray(int typeCode);
  void nop();

  void pop();
  void pop2();
  void putfield(int cpFieldRefIndex);
  void putstatic(int cpFieldRefIndex);

  void ret(int localVarIndex);
  void return_();

  void saload();
  void sastore();
  void sipush(int val);
  void swap();

  void tableswitch(int defaultPcOffset, int low, int high);
  void tableswitchEntry(int value, int pcOffset);

  void wide ();

  void unknown(int bytecode);
}
