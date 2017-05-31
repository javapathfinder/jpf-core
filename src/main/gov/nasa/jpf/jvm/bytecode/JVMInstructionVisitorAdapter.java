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

import gov.nasa.jpf.vm.Instruction;

public abstract class JVMInstructionVisitorAdapter 
							implements JVMInstructionVisitor {

  @Override public void visit(Instruction ins) {}

  @Override public void visit(AALOAD ins) {}

  @Override public void visit(AASTORE ins) {}

  @Override public void visit(ACONST_NULL ins) {}

  @Override public void visit(ALOAD ins) {}

  @Override public void visit(ANEWARRAY ins) {}

  @Override public void visit(ARETURN ins) {}

  @Override public void visit(JVMArrayElementInstruction ins) {}
  
  @Override public void visit(ARRAYLENGTH ins) {}

  @Override public void visit(ArrayLoadInstruction ins) {}

  @Override public void visit(ArrayStoreInstruction ins) {}

  @Override public void visit(ASTORE ins) {}

  @Override public void visit(ATHROW ins) {}

  @Override public void visit(BALOAD ins) {}

  @Override public void visit(BASTORE ins) {}

  @Override public void visit(BIPUSH ins) {}

  @Override public void visit(CALOAD ins) {}

  @Override public void visit(CASTORE ins) {}

  @Override public void visit(CHECKCAST ins) {}

  @Override public void visit(D2F ins) {}

  @Override public void visit(D2I ins) {}

  @Override public void visit(D2L ins) {}

  @Override public void visit(DADD ins) {}

  @Override public void visit(DALOAD ins) {}

  @Override public void visit(DASTORE ins) {}

  @Override public void visit(DCMPG ins) {}

  @Override public void visit(DCMPL ins) {}

  @Override public void visit(DCONST ins) {}

  @Override public void visit(DDIV ins) {}

  @Override public void visit(DIRECTCALLRETURN ins) {}

  @Override public void visit(DLOAD ins) {}

  @Override public void visit(DMUL ins) {}

  @Override public void visit(DNEG ins) {}

  @Override public void visit(DREM ins) {}

  @Override public void visit(DRETURN ins) {}

  @Override public void visit(DSTORE ins) {}

  @Override public void visit(DSUB ins) {}

  @Override public void visit(DUP_X1 ins) {}

  @Override public void visit(DUP_X2 ins) {}

  @Override public void visit(DUP ins) {}

  @Override public void visit(DUP2_X1 ins) {}

  @Override public void visit(DUP2_X2 ins) {}

  @Override public void visit(DUP2 ins) {}

  @Override public void visit(F2D ins) {}

  @Override public void visit(F2I ins) {}

  @Override public void visit(FADD ins) {}

  @Override public void visit(FALOAD ins) {}

  @Override public void visit(FASTORE ins) {}

  @Override public void visit(FCMPL ins) {}

  @Override public void visit(FCONST ins) {}

  @Override public void visit(FDIV ins) {}

  @Override public void visit(JVMFieldInstruction ins) {}

  @Override public void visit(FLOAD ins) {}

  @Override public void visit(FMUL ins) {}

  @Override public void visit(FNEG ins) {}

  @Override public void visit(FREM ins) {}

  @Override public void visit(FRETURN ins) {}

  @Override public void visit(FSTORE ins) {}

  @Override public void visit(FSUB ins) {}

  @Override public void visit(GETFIELD ins) {}

  @Override public void visit(GETSTATIC ins) {}

  @Override public void visit(GOTO_W ins) {}

  @Override public void visit(GOTO ins) {}

  @Override public void visit(I2B ins) {}

  @Override public void visit(I2C ins) {}

  @Override public void visit(I2D ins) {}

  @Override public void visit(I2F ins) {}

  @Override public void visit(I2L ins) {}

  @Override public void visit(I2S ins) {}

  @Override public void visit(IADD ins) {}

  @Override public void visit(IALOAD ins) {}

  @Override public void visit(IAND ins) {}

  @Override public void visit(IASTORE ins) {}

  @Override public void visit(ICONST ins) {}

  @Override public void visit(IDIV ins) {}

  @Override public void visit(IF_ACMPEQ ins) {}

  @Override public void visit(IF_ACMPNE ins) {}

  @Override public void visit(IF_ICMPEQ ins) {}

  @Override public void visit(IF_ICMPGE ins) {}

  @Override public void visit(IF_ICMPGT ins) {}

  @Override public void visit(IF_ICMPLE ins) {}

  @Override public void visit(IF_ICMPLT ins) {}

  @Override public void visit(IF_ICMPNE ins) {}

  @Override public void visit(IFEQ ins) {}

  @Override public void visit(IFGE ins) {}

  @Override public void visit(IFGT ins) {}

  @Override public void visit(IfInstruction ins) {}

  @Override public void visit(IFLE ins) {}

  @Override public void visit(IFLT ins) {}

  @Override public void visit(IFNE ins) {}

  @Override public void visit(IFNONNULL ins) {}

  @Override public void visit(IFNULL ins) {}

  @Override public void visit(IINC ins) {}

  @Override public void visit(ILOAD ins) {}

  @Override public void visit(IMUL ins) {}

  @Override public void visit(INEG ins) {}

  @Override public void visit(JVMInstanceFieldInstruction ins) {}

  @Override public void visit(InstanceInvocation ins){}
	
  @Override public void visit(INSTANCEOF ins){}
	
  @Override public void visit(INVOKECG ins){}
	
  @Override public void visit(INVOKECLINIT ins){}
	
  @Override public void visit(JVMInvokeInstruction ins){}
	
  @Override public void visit(INVOKEINTERFACE ins){}
	
  @Override public void visit(INVOKESPECIAL ins){}
	
  @Override public void visit(INVOKESTATIC ins){}
	
  @Override public void visit(INVOKEVIRTUAL ins){}

  @Override public void visit(EXECUTENATIVE ins){}
	
  @Override public void visit(IOR ins){}
	
  @Override public void visit(IREM ins){}
	
  @Override public void visit(IRETURN ins){}
	
  @Override public void visit(ISHL ins){}
	
  @Override public void visit(ISHR ins){}
	
  @Override public void visit(ISTORE ins){}
	
  @Override public void visit(ISUB ins){}
	
  @Override public void visit(IUSHR ins){}
	
  @Override public void visit(IXOR ins){}
	
  @Override public void visit(JSR_W ins){}
	
  @Override public void visit(JSR ins){}
	
  @Override public void visit(L2D ins){}
	
  @Override public void visit(L2F ins){}
	
  @Override public void visit(L2I ins){}
	
  @Override public void visit(LADD ins){}
	
  @Override public void visit(LALOAD ins){}
	
  @Override public void visit(LAND ins){}
	
  @Override public void visit(LASTORE ins){}
	
  @Override public void visit(LCMP ins){}
	
  @Override public void visit(LCONST ins){}
	
  @Override public void visit(LDC_W ins){}
	
  @Override public void visit(LDC ins){}
	
  @Override public void visit(LDC2_W ins){}
	
  @Override public void visit(LDIV ins){}
	
  @Override public void visit(LLOAD ins){}
	
  @Override public void visit(LMUL ins){}
	
  @Override public void visit(LNEG ins){}
	
  @Override public void visit(JVMLocalVariableInstruction ins){}
	
  @Override public void visit(LockInstruction ins){}
	
  @Override public void visit(LongArrayLoadInstruction ins){}
	
  @Override public void visit(LongArrayStoreInstruction ins){}
	
  @Override public void visit(LOOKUPSWITCH ins){}
	
  @Override public void visit(LOR ins){}
	
  @Override public void visit(LREM ins){}
	
  @Override public void visit(LRETURN ins){}
	
  @Override public void visit(LSHL ins){}
	
  @Override public void visit(LSHR ins){}
	
  @Override public void visit(LSTORE ins){}
	
  @Override public void visit(LSUB ins){}
	
  @Override public void visit(LUSHR ins){}
	
  @Override public void visit(LXOR ins){}
	
  @Override public void visit(MONITORENTER ins){}
	
  @Override public void visit(MONITOREXIT ins){}
	
  @Override public void visit(MULTIANEWARRAY ins){}

  @Override public void visit(NATIVERETURN ins) {}
	
  @Override public void visit(NEW ins){}
	
  @Override public void visit(NEWARRAY ins){}
	
  @Override public void visit(NOP ins){}
	
  @Override public void visit(POP ins){}
	
  @Override public void visit(POP2 ins){}
	
  @Override public void visit(PUTFIELD ins){}
	
  @Override public void visit(PUTSTATIC ins){}
	
  @Override public void visit(RET ins){}
	
  @Override public void visit(RETURN ins){}
	
  @Override public void visit(JVMReturnInstruction ins){}
	
  @Override public void visit(SALOAD ins){}
	
  @Override public void visit(SASTORE ins){}
	
  @Override public void visit(SIPUSH ins){}
	
  @Override public void visit(JVMStaticFieldInstruction ins){}

  @Override public void visit(SWAP ins){}
	
  @Override public void visit(SwitchInstruction ins){}
	
  @Override public void visit(TABLESWITCH ins){}
	
  @Override public void visit(VirtualInvocation ins){}
	
  @Override public void visit(WIDE ins){}
	
}