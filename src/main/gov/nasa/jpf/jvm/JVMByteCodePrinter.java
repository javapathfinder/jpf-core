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

import java.io.PrintWriter;

/**
 * utility class that prints out bytecode in readable form
 */
public class JVMByteCodePrinter implements JVMByteCodeReader {

  PrintWriter pw;
  ClassFile cf; // need this to get the constpool entries

  String prefix;

  public JVMByteCodePrinter (PrintWriter pw, ClassFile cf, String prefix){
    this.pw = pw;
    this.cf = cf;
    this.prefix = prefix;
  }

  @Override
  public void aconst_null() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aconst_null");
  }

  @Override
  public void aload(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "aload", localVarIndex);
  }

  @Override
  public void aload_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aload_0");
  }

  @Override
  public void aload_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aload_1");
  }

  @Override
  public void aload_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aload_2");
  }

  @Override
  public void aload_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aload_3");
  }

  @Override
  public void aaload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aaload");
  }

  @Override
  public void astore(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "astore", localVarIndex);
  }

  @Override
  public void astore_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "astore_0");
  }

  @Override
  public void astore_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "astore_1");
  }

  @Override
  public void astore_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "astore_2");
  }

  @Override
  public void astore_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "astore_3");
  }

  @Override
  public void aastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "aastore");
  }

  @Override
  public void areturn() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "areturn");
  }

  @Override
  public void anewarray(int cpClassIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\")\n", prefix, cf.getPc(), "anewarray", cpClassIndex, cf.classNameAt(cpClassIndex));
  }

  @Override
  public void arraylength() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "arraylength");
  }

  @Override
  public void athrow() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "athrow");
  }

  @Override
  public void baload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "baload");
  }

  @Override
  public void bastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "bastore");
  }

  @Override
  public void bipush(int b) {
    pw.printf("%s%3d: %s %d\n", prefix, cf.getPc(), "bipush", b);
  }

  @Override
  public void caload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "caload");
  }

  @Override
  public void castore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "castore");
  }

  @Override
  public void checkcast(int cpClassIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\")\n", prefix, cf.getPc(), "checkcast", cpClassIndex, cf.classNameAt(cpClassIndex));
  }

  @Override
  public void d2f() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "d2f");
  }

  @Override
  public void d2i() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "d2i");
  }

  @Override
  public void d2l() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "d2l");
  }

  @Override
  public void dadd() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dadd");
  }

  @Override
  public void daload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "daload");
  }

  @Override
  public void dastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dastore");
  }

  @Override
  public void dcmpg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dcmpg");
  }

  @Override
  public void dcmpl() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dcmpl");
  }

  @Override
  public void dconst_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dconst_0");
  }

  @Override
  public void dconst_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dcont_1");
  }

  @Override
  public void ddiv() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ddiv");
  }

  @Override
  public void dload(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "dload", localVarIndex);
  }

  @Override
  public void dload_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dload_0");
  }

  @Override
  public void dload_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dload_1");
  }

  @Override
  public void dload_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dload_2");
  }

  @Override
  public void dload_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dload_3");
  }

  @Override
  public void dmul() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dmul");
  }

  @Override
  public void dneg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dneg");
  }

  @Override
  public void drem() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "drem");
  }

  @Override
  public void dreturn() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dreturn");
  }

  @Override
  public void dstore(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "dstore", localVarIndex);
  }

  @Override
  public void dstore_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dstore_0");
  }

  @Override
  public void dstore_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dstore_1");
  }

  @Override
  public void dstore_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dstore_2");
  }

  @Override
  public void dstore_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dstore_3");
  }

  @Override
  public void dsub() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dsub");
  }

  @Override
  public void dup() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup");
  }

  @Override
  public void dup_x1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup_x1");
  }

  @Override
  public void dup_x2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup_x2");
  }

  @Override
  public void dup2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup2");
  }

  @Override
  public void dup2_x1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup2_x1");
  }

  @Override
  public void dup2_x2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "dup2_x2");
  }

  @Override
  public void f2d() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "f2d");
  }

  @Override
  public void f2i() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "f2i");
  }

  @Override
  public void f2l() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "f2l");
  }

  @Override
  public void fadd() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fadd");
  }

  @Override
  public void faload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "faload");
  }

  @Override
  public void fastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fastore");
  }

  @Override
  public void fcmpg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fcmpg");
  }

  @Override
  public void fcmpl() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fcmpl");
  }

  @Override
  public void fconst_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fconst_0");
  }

  @Override
  public void fconst_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fconst_1");
  }

  @Override
  public void fconst_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fconst_2");
  }

  @Override
  public void fdiv() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fdiv");
  }

  @Override
  public void fload(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "fload", localVarIndex);
  }

  @Override
  public void fload_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fload_0");
  }

  @Override
  public void fload_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fload_1");
  }

  @Override
  public void fload_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fload_2");
  }

  @Override
  public void fload_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fload_3");
  }

  @Override
  public void fmul() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fmul");
  }

  @Override
  public void fneg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fneg");
  }

  @Override
  public void frem() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "frem");
  }

  @Override
  public void freturn() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "freturn");
  }

  @Override
  public void fstore(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "fstore", localVarIndex);
  }

  @Override
  public void fstore_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fstore_0");
  }

  @Override
  public void fstore_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fstore_1");
  }

  @Override
  public void fstore_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fstore_2");
  }

  @Override
  public void fstore_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fstore_3");
  }

  @Override
  public void fsub() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "fsub");
  }

  @Override
  public void getfield(int cpFieldRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "getfield", cpFieldRefIndex,
            cf.fieldClassNameAt(cpFieldRefIndex),
            cf.fieldNameAt(cpFieldRefIndex),
            cf.fieldDescriptorAt(cpFieldRefIndex));
  }

  @Override
  public void getstatic(int cpFieldRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "getstatic", cpFieldRefIndex,
            cf.fieldClassNameAt(cpFieldRefIndex),
            cf.fieldNameAt(cpFieldRefIndex),
            cf.fieldDescriptorAt(cpFieldRefIndex));
  }

  @Override
  public void goto_(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "goto", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void goto_w(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "goto_w", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void i2b() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2b");
  }

  @Override
  public void i2c() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2c");
  }

  @Override
  public void i2d() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2d");
  }

  @Override
  public void i2f() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2f");
  }

  @Override
  public void i2l() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2l");
  }

  @Override
  public void i2s() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "i2s");
  }

  @Override
  public void iadd() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iadd");
  }

  @Override
  public void iaload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iaload");
  }

  @Override
  public void iand() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iand");
  }

  @Override
  public void iastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iastore");
  }

  @Override
  public void iconst_m1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_m1");
  }

  @Override
  public void iconst_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_0");
  }

  @Override
  public void iconst_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_1");
  }

  @Override
  public void iconst_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_2");
  }

  @Override
  public void iconst_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_3");
  }

  @Override
  public void iconst_4() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_4");
  }

  @Override
  public void iconst_5() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iconst_5");
  }

  @Override
  public void idiv() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "idiv");
  }

  @Override
  public void if_acmpeq(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_acmpeq", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_acmpne(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_acmpne", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmpeq(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmpeq", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmpne(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmpne", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmplt(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmplt", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmpge(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmpge", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmpgt(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmpgt", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void if_icmple(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "if_icmple", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifeq(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifeq", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifne(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifne", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void iflt(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "iflt", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifge(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifge", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifgt(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifgt", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifle(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifle", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifnonnull(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifnonnull", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void ifnull(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "ifnull", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void iinc(int localVarIndex, int incConstant) {
    pw.printf("%s%3d: %s [%d] %+d\n", prefix, cf.getPc(), "iinc", localVarIndex, incConstant);
  }

  @Override
  public void iload(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "iload", localVarIndex);
  }

  @Override
  public void iload_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iload_0");
  }

  @Override
  public void iload_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iload_1");
  }

  @Override
  public void iload_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iload_2");
  }

  @Override
  public void iload_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iload_3");
  }

  @Override
  public void imul() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "imul");
  }

  @Override
  public void ineg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ineg");
  }

  @Override
  public void instanceof_(int cpClassIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\")\n", prefix, cf.getPc(), "instanceof", cpClassIndex, cf.classNameAt(cpClassIndex));
  }

  @Override
  public void invokeinterface(int cpInterfaceMethodRefIndex, int count, int zero) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\") %d\n", prefix, cf.getPc(), "invokeinterface", cpInterfaceMethodRefIndex,
            cf.methodClassNameAt(cpInterfaceMethodRefIndex),
            cf.methodNameAt(cpInterfaceMethodRefIndex),
            cf.methodDescriptorAt(cpInterfaceMethodRefIndex),
            count);
  }

  @Override
  public void invokespecial(int cpMethodRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "invokespecial", cpMethodRefIndex,
            cf.methodClassNameAt(cpMethodRefIndex),
            cf.methodNameAt(cpMethodRefIndex),
            cf.methodDescriptorAt(cpMethodRefIndex));
  }

  @Override
  public void invokestatic(int cpMethodRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "invokestatic", cpMethodRefIndex,
            cf.methodClassNameAt(cpMethodRefIndex),
            cf.methodNameAt(cpMethodRefIndex),
            cf.methodDescriptorAt(cpMethodRefIndex));
  }

  @Override
  public void invokevirtual(int cpMethodRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "invokevirtual", cpMethodRefIndex,
            cf.methodClassNameAt(cpMethodRefIndex),
            cf.methodNameAt(cpMethodRefIndex),
            cf.methodDescriptorAt(cpMethodRefIndex));
  }

  @Override
  public void invokedynamic (int cpInvokeDynamicIndex){
    pw.printf("%s%3d: %s @%d\n", prefix, cf.getPc(), "invokedynamic", cpInvokeDynamicIndex);
  }
  
  @Override
  public void ior() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ior");
  }

  @Override
  public void irem() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "irem");
  }

  @Override
  public void ireturn() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ireturn");
  }

  @Override
  public void ishl() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ishl");
  }

  @Override
  public void ishr() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ishr");
  }

  @Override
  public void istore(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "istore", localVarIndex);
  }

  @Override
  public void istore_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "istore_0");
  }

  @Override
  public void istore_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "istore_1");
  }

  @Override
  public void istore_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "istore_2");
  }

  @Override
  public void istore_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "istore_3");
  }

  @Override
  public void isub() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "isub");
  }

  @Override
  public void iushr() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "iushr");
  }

  @Override
  public void ixor() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ixor");
  }

  @Override
  public void jsr(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "jsr", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void jsr_w(int pcOffset) {
    pw.printf("%s%3d: %s %+d (%d)\n", prefix, cf.getPc(), "jsr_w", pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void l2d() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "l2d");
  }

  @Override
  public void l2f() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "l2f");
  }

  @Override
  public void l2i() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "l2i");
  }

  @Override
  public void ladd() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ladd");
  }

  @Override
  public void laload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "laload");
  }

  @Override
  public void land() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "land");
  }

  @Override
  public void lastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lastore");
  }

  @Override
  public void lcmp() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lcmp");
  }

  @Override
  public void lconst_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lconst_0");
  }

  @Override
  public void lconst_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lconst_1");
  }

  @Override
  public void ldc_(int cpIntOrFloatOrStringIndex) {
    pw.printf("%s%3d: %s @%d(%s)\n", prefix, cf.getPc(), "ldc", cpIntOrFloatOrStringIndex,
            cf.getCpValue(cpIntOrFloatOrStringIndex));
  }

  @Override
  public void ldc_w_(int cpIntOrFloatOrStringIndex) {
    pw.printf("%s%3d: %s @%d(%s)\n", prefix, cf.getPc(), "ldc_w", cpIntOrFloatOrStringIndex,
            cf.getCpValue(cpIntOrFloatOrStringIndex));
  }

  @Override
  public void ldc2_w(int cpLongOrDoubleIndex) {
    pw.printf("%s%3d: %s @%d(%s)\n", prefix, cf.getPc(), "ldc2_w", cpLongOrDoubleIndex,
            cf.getCpValue(cpLongOrDoubleIndex));
  }

  @Override
  public void ldiv() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "ldiv");
  }

  @Override
  public void lload(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "lload", localVarIndex);
  }

  @Override
  public void lload_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lload_0");
  }

  @Override
  public void lload_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lload_1");
  }

  @Override
  public void lload_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lload_2");
  }

  @Override
  public void lload_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lload_3");
  }

  @Override
  public void lmul() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lmul");
  }

  @Override
  public void lneg() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lneg");
  }

  @Override
  public void lookupswitch(int defaultPcOffset, int nEntries) {
    pw.printf("%s%3d: %s default:%+d\n", prefix, cf.getPc(), "lookupswitch", defaultPcOffset);
    cf.parseLookupSwitchEntries(this, nEntries);
  }
  @Override
  public void lookupswitchEntry(int index, int match, int pcOffset){
    pw.printf("%s      %d : %+d (%d)\n", prefix, match, pcOffset, (cf.getPc() + pcOffset));
  }


  @Override
  public void lor() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lor");
  }

  @Override
  public void lrem() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lrem");
  }

  @Override
  public void lreturn() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lreturn");
  }

  @Override
  public void lshl() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lshl");
  }

  @Override
  public void lshr() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lshr");
  }

  @Override
  public void lstore(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "lstore", localVarIndex);
  }

  @Override
  public void lstore_0() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lstore_0");
  }

  @Override
  public void lstore_1() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lstore_1");
  }

  @Override
  public void lstore_2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lstore_2");
  }

  @Override
  public void lstore_3() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lstore_3");
  }

  @Override
  public void lsub() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lsub");
  }

  @Override
  public void lushr() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lushr");
  }

  @Override
  public void lxor() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "lxor");
  }

  @Override
  public void monitorenter() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "monitorenter");
  }

  @Override
  public void monitorexit() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "monitorexit");
  }

  @Override
  public void multianewarray(int cpClassIndex, int dimensions) {
    pw.printf("%s%3d: %s @%d(\"%s\") dim: %d\n", prefix, cf.getPc(), "multianewarray",
            cpClassIndex, cf.classNameAt(cpClassIndex), dimensions);
  }

  @Override
  public void new_(int cpClassIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\")\n", prefix, cf.getPc(), "new",
            cpClassIndex, cf.classNameAt(cpClassIndex));
  }

  @Override
  public void newarray(int typeCode) {
    pw.printf("%s%3d: %s %s[]\n", prefix, cf.getPc(), "newarray", cf.getTypeName(typeCode));
  }

  @Override
  public void nop() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "nop");
  }

  @Override
  public void pop() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "pop");
  }

  @Override
  public void pop2() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "pop2");
  }

  @Override
  public void putfield(int cpFieldRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "putfield", cpFieldRefIndex,
            cf.fieldClassNameAt(cpFieldRefIndex),
            cf.fieldNameAt(cpFieldRefIndex),
            cf.fieldDescriptorAt(cpFieldRefIndex));
  }

  @Override
  public void putstatic(int cpFieldRefIndex) {
    pw.printf("%s%3d: %s @%d(\"%s\",\"%s\",\"%s\")\n", prefix, cf.getPc(), "putstatic", cpFieldRefIndex,
            cf.fieldClassNameAt(cpFieldRefIndex),
            cf.fieldNameAt(cpFieldRefIndex),
            cf.fieldDescriptorAt(cpFieldRefIndex));
  }

  @Override
  public void ret(int localVarIndex) {
    pw.printf("%s%3d: %s [%d]\n", prefix, cf.getPc(), "ret", localVarIndex);
  }

  @Override
  public void return_() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "return");
  }

  @Override
  public void saload() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "saload");
  }

  @Override
  public void sastore() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "sastore");
  }

  @Override
  public void sipush(int val) {
    pw.printf("%s%3d: %s %d\n", prefix, cf.getPc(), "sipush", val);
  }

  @Override
  public void swap() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "swap");
  }

  @Override
  public void tableswitch(int defaultPcOffset, int low, int high) {
    pw.printf("%s%3d: %s [%d..%d] default: %+d\n", prefix, cf.getPc(), "tableswitch", low, high, defaultPcOffset);
    cf.parseTableSwitchEntries(this, low, high);
  }
  @Override
  public void tableswitchEntry(int val, int pcOffset){
    pw.printf("%s      %d: %+d (%d)\n", prefix, val, pcOffset, (cf.getPc() + pcOffset));
  }

  @Override
  public void wide() {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "wide");
  }

  @Override
  public void unknown(int bytecode) {
    pw.printf("%s%3d: %s\n", prefix, cf.getPc(), "");
  }


}
