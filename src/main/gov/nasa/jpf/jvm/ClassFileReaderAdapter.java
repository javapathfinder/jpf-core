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

import gov.nasa.jpf.vm.ClassParseException;

/**
 * adapter class implementing the ClassFileReader interface
 */
public class ClassFileReaderAdapter implements ClassFileReader {

  @Override
  public void setClass(ClassFile cf, String clsName, String superClsName, int flags, int cpCount) throws ClassParseException {}

  @Override
  public void setInterfaceCount(ClassFile cf, int ifcCount) {}

  @Override
  public void setInterface(ClassFile cf, int ifcIndex, String ifcName) {}

  @Override
  public void setInterfacesDone(ClassFile cf) {};

  @Override
  public void setFieldCount(ClassFile cf, int fieldCount) {}

  @Override
  public void setField(ClassFile cf, int fieldIndex, int accessFlags, String name, String descriptor) {}

  @Override
  public void setFieldAttributeCount(ClassFile cf, int fieldIndex, int attrCount) {}

  @Override
  public void setFieldAttribute(ClassFile cf, int fieldIndex, int attrIndex, String name, int attrLength) {}

  @Override
  public void setFieldAttributesDone(ClassFile cf, int fieldIndex) {}

  @Override
  public void setFieldDone(ClassFile cf, int fieldIndex) {}

  @Override
  public void setFieldsDone(ClassFile cf) {}

  @Override
  public void setConstantValue(ClassFile cf, Object tag, Object value) {}

  @Override
  public void setMethodCount(ClassFile cf, int methodCount) {}

  @Override
  public void setMethod(ClassFile cf, int methodIndex, int accessFlags, String name, String descriptor) {}

  @Override
  public void setMethodAttributeCount(ClassFile cf, int methodIndex, int attrCount) {}

  @Override
  public void setMethodAttribute(ClassFile cf, int methodIndex, int attrIndex, String name, int attrLength) {}

  @Override
  public void setMethodAttributesDone(ClassFile cf, int methodIndex){}

  @Override
  public void setMethodDone(ClassFile cf, int methodIndex) {}

  @Override
  public void setMethodsDone(ClassFile cf) {}

  @Override
  public void setExceptionCount(ClassFile cf, Object tag, int exceptionCount) {}

  @Override
  public void setException(ClassFile cf, Object tag, int exceptionIndex, String exceptionType) {}

  @Override
  public void setExceptionsDone(ClassFile cf, Object tag) {}

  @Override
  public void setCode(ClassFile cf, Object tag, int maxStack, int maxLocals, int codeLength) {}

  @Override
  public void setExceptionHandlerTableCount(ClassFile cf, Object tag, int exceptionTableCount) {}

  @Override
  public void setExceptionHandler(ClassFile cf, Object tag, int exceptionIndex,
          int startPc, int endPc, int handlerPc, String catchType) {}

  @Override
  public void setExceptionHandlerTableDone(ClassFile cf, Object tag) {}

  @Override
  public void setCodeAttributeCount(ClassFile cf, Object tag, int attrCount) {}

  @Override
  public void setCodeAttribute(ClassFile cf, Object tag, int attrIndex, String name, int attrLength) {}

  @Override
  public void setCodeAttributesDone (ClassFile cf, Object tag) {}

  @Override
  public void setLineNumberTableCount(ClassFile cf, Object tag, int lineNumberCount) {}

  @Override
  public void setLineNumber(ClassFile cf, Object tag, int lineIndex, int lineNumber, int startPc) {}

  @Override
  public void setLineNumberTableDone(ClassFile cf, Object tag) {}

  @Override
  public void setLocalVarTableCount(ClassFile cf, Object tag, int localVarCount) {}

  @Override
  public void setLocalVar(ClassFile cf, Object tag, int localVarIndex,
          String varName, String descriptor, int scopeStartPc, int scopeEndPc, int slotIndex) {}

  @Override
  public void setLocalVarTableDone (ClassFile cf, Object tag) {}

  @Override
  public void setClassAttributeCount(ClassFile cf, int attrCount) {}

  @Override
  public void setClassAttribute(ClassFile cf, int attrIndex, String name, int attrLength) {}

  @Override
  public void setClassAttributesDone(ClassFile cf) {}

  @Override
  public void setSourceFile(ClassFile cf, Object tag, String pathName) {}

  @Override
  public void setInnerClassCount(ClassFile cf, Object tag, int innerClsCount) {}

  @Override
  public void setInnerClass(ClassFile cf, Object tag, int innerClsIndex,
          String outerName, String innerName, String innerSimpleName, int accessFlags) {}

  @Override
  public void setInnerClassesDone(ClassFile cf, Object tag) {}
  
  @Override
  public void setBootstrapMethodCount (ClassFile cf, Object tag, int count) {}
  
  @Override
  public void setBootstrapMethod (ClassFile cf, Object tag, int idx, int refKind, String cls, String mth, String descriptor, int[] cpArgs){}
  
  @Override
  public void setBootstrapMethodsDone (ClassFile cf, Object tag) {}
  
  @Override
  public void setEnclosingMethod(ClassFile cf, Object tag, String enclosingClass, String enclosingMethod, String descriptor) {}

  @Override
  public void setAnnotationCount(ClassFile cf, Object tag, int annotationCount){}
  @Override
  public void setAnnotation(ClassFile cf, Object tag, int annotationIndex, String annotationType){}
  @Override
  public void setAnnotationsDone(ClassFile cf, Object tag) {}

  //--- Java 8 type annotations
  @Override
  public void setTypeAnnotationCount(ClassFile cf, Object tag, int annotationCount){}
  @Override
  public void setTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int typeIndex, short[] typePath, String annotationType){}
  @Override
  public void setSuperTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int superTypeIdx, short[] typePath, String annotationType){}
  @Override
  public void setTypeParameterBoundAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int typeParamIdx, int boundIdx, short[] typePath, String annotationType){}
  @Override
  public void setTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, short[] typePath, String annotationType){}
  @Override
  public void setFormalParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int formalParamIdx, short[] typePath, String annotationType){}
  @Override
  public void setThrowsAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int throwsTypeIdx, short[] typePath, String annotationType){}
  @Override
  public void setVariableAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, long[] scopeEntries, short[] typePath, String annotationType){}
  @Override
  public void setExceptionParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int exceptionIndex, short[] typePath, String annotationType){}
  @Override
  public void setBytecodeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int offset, short[] typePath, String annotationType){}
  @Override
  public void setBytecodeTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, int offset, int typeArgIdx, short[] typePath, String annotationType){}
  @Override
  public void setTypeAnnotationsDone(ClassFile cf, Object tag) {}

  @Override
  public void setAnnotationValueCount(ClassFile cf, Object tag, int annotationIndex, int annotationCount) {}

  @Override
  public void setPrimitiveAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, Object val){}

  @Override
  public void setStringAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String s){}

  @Override
  public void setClassAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String typeName){}

  @Override
  public void setEnumAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String enumType, String enumValue){}

  @Override
  public void setAnnotationValueElementCount(ClassFile cf, Object tag, int annotationIndex, int valueIndex, 
          String elementName, int elementCount) {}

  @Override
  public void setAnnotationValueElementsDone(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName) {}

  @Override
  public void setAnnotationValuesDone(ClassFile cf, Object tag, int annotationIndex) {}

  @Override
  public void setParameterCount(ClassFile cf, Object tag, int parameterCount) {}

  @Override
  public void setParameterAnnotationCount(ClassFile cf, Object tag, int paramIndex, int annotationCount) {}

  @Override
  public void setParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, String annotationType) {}

  @Override
  public void setParameterAnnotationsDone(ClassFile cf, Object tag, int paramIndex) {}

  @Override
  public void setParametersDone(ClassFile cf, Object tag) {}

  @Override
  public void setSignature(ClassFile cf, Object tag, String signature) {}
}
