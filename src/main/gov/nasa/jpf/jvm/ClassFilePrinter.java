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

import gov.nasa.jpf.util.StructuredPrinter;
import gov.nasa.jpf.vm.ClassParseException;

/**
 * simple tool to print contents of a classfile
 *
 * <2do> use indentation level variable and formated output
 */
public class ClassFilePrinter extends StructuredPrinter implements ClassFileReader {


  public static void main(String[] args){
    ClassFilePrinter printer = new ClassFilePrinter();

    try {
      ClassFile cf = new ClassFile(args[0]);
      cf.parse(printer);

    } catch (ClassParseException cfx){
      cfx.printStackTrace();
    }
  }

  @Override
  public void setClass(ClassFile cf, String clsName, String superClsName, int flags, int cpCount) {
    printSectionHeader( "constpool");
    printCp(pw,cf);

    incIndent();
    printSectionHeader( "class");
    pw.printf("%sclass=%s\n", indent, clsName);
    pw.printf("%ssuperclass=%s\n", indent, superClsName);
    pw.printf("%sflags=0x%X\n", indent, flags);
  }

  //--- interfaces
  @Override
  public void setInterfaceCount(ClassFile cf, int ifcCount) {
    pw.printf("%sinterface count=%d\n", indent, ifcCount);
    incIndent();
  }

  @Override
  public void setInterface(ClassFile cf, int ifcIndex, String ifcName) {
    pw.printf("%s[%d]: %s\n", indent, ifcIndex, ifcName);
  }

  @Override
  public void setInterfacesDone(ClassFile cf){
    decIndent();
  }

  //--- fields
  @Override
  public void setFieldCount(ClassFile cf, int fieldCount) {
    printSectionHeader( "fields");
    pw.printf( "%sfield count=%d\n", indent, fieldCount);
  }

  @Override
  public void setField(ClassFile cf, int fieldIndex, int accessFlags, String name, String descriptor) {
    pw.printf("%s[%d]: %s, type=%s,flags=0x%X", indent, fieldIndex, name, descriptor, accessFlags);
  }

  @Override
  public void setFieldAttributeCount(ClassFile cf, int fieldIndex, int attrCount) {
    pw.printf(", attr count=%d\n", attrCount);
    incIndent();
  }

  @Override
  public void setFieldAttribute(ClassFile cf, int fieldIndex, int attrIndex, String name, int attrLength) {
    pw.printf("%s[%d]: %s", indent, attrIndex, name);

    if (name == ClassFile.CONST_VALUE_ATTR) {
      cf.parseConstValueAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR){
        cf.parseTypeAnnotationsAttr(this, null);

    } else if (name == ClassFile.SIGNATURE_ATTR){
      cf.parseSignatureAttr(this, null);

    } else {
      pw.printf(" ,length=%d,data=[",attrLength );
      printRawData(pw, cf, attrLength, 10);
      pw.println(']');
    }
  }

  @Override
  public void setFieldAttributesDone(ClassFile cf, int fieldIndex){
    decIndent();
  }

  @Override
  public void setFieldDone(ClassFile cf, int fieldIndex){
    pw.println();
  }

  @Override
  public void setFieldsDone(ClassFile cf){
  }

  @Override
  public void setConstantValue(ClassFile cf, Object tag, Object value) {
    pw.printf(" value=%s\n", value);
  }

  //--- methods
  @Override
  public void setMethodCount(ClassFile cf, int methodCount) {
    printSectionHeader( "methods");
    pw.printf( "%smethod count=%d\n", indent, methodCount);
  }

  @Override
  public void setMethod(ClassFile cf, int methodIndex, int accessFlags, String name, String descriptor) {
    pw.printf("%s[%d]: %s%s, flags=0x%X", indent, methodIndex, name, descriptor, accessFlags);
  }

  @Override
  public void setMethodAttributeCount(ClassFile cf, int methodIndex, int attrCount) {
    pw.printf(", attr count=%d\n", attrCount);
    incIndent();
  }

  @Override
  public void setMethodAttribute(ClassFile cf, int methodIndex, int attrIndex, String name, int attrLength) {
    pw.printf("%s[%d]: %s", indent, attrIndex, name);

    if (name == ClassFile.CODE_ATTR) {
      cf.parseCodeAttr(this, null);

    } else if (name == ClassFile.EXCEPTIONS_ATTR){
      cf.parseExceptionAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS_ATTR){
      cf.parseParameterAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR){
      cf.parseTypeAnnotationsAttr(this, null);
      
    } else if (name == ClassFile.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS_ATTR){
      cf.parseParameterAnnotationsAttr(this, null);

    } else if (name == ClassFile.SIGNATURE_ATTR){
      cf.parseSignatureAttr(this, null);

    } else {
      pw.printf(" ,length=%d,data=[", attrLength );
      printRawData(pw, cf, attrLength, 10);
      pw.println(']');
    }
  }

  @Override
  public void setMethodAttributesDone(ClassFile cf, int methodIndex){
    decIndent();
  }

  @Override
  public void setMethodDone(ClassFile cf, int methodIndex){
    pw.println();
  }

  @Override
  public void setMethodsDone(ClassFile cf){
  }

  @Override
  public void setExceptionCount(ClassFile cf, Object tag, int exceptionCount){
    pw.printf(", count=%d\n", exceptionCount);
    incIndent();
  }
  @Override
  public void setException(ClassFile cf, Object tag, int exceptionIndex, String exceptionType){
    pw.printf("%s[%d]: %s\n", indent, exceptionIndex, exceptionType);
  }
  @Override
  public void setExceptionsDone(ClassFile cf, Object tag){
    decIndent();
  }


  @Override
  public void setCode(ClassFile cf, Object tag, int maxStack, int maxLocals, int codeLength) {
    pw.printf(", maxStack=%d,maxLocals=%d,length=%d\n", maxStack, maxLocals,codeLength);
    incIndent();
    JVMByteCodePrinter bcPrinter = new JVMByteCodePrinter(pw, cf, indent);
    cf.parseBytecode(bcPrinter, tag, codeLength);
    decIndent();
  }

  @Override
  public void setExceptionHandlerTableCount(ClassFile cf, Object tag, int exceptionTableCount) {
    pw.printf("%sexception table count=%d\n", indent, exceptionTableCount);
    incIndent();
  }
  @Override
  public void setExceptionHandler(ClassFile cf, Object tag, int exceptionIndex,
          int startPc, int endPc, int handlerPc, String catchType) {
    pw.printf("%s[%d]: type=%s, range=[%d..%d], handler=%d\n", indent, exceptionIndex, catchType, startPc, endPc, handlerPc);
  }
  @Override
  public void setExceptionHandlerTableDone(ClassFile cf, Object tag){
    decIndent();
  }

  @Override
  public void setCodeAttributeCount(ClassFile cf, Object tag, int attrCount) {
    pw.printf("%scode attribute count=%d\n", indent, attrCount);
    incIndent();
  }
  @Override
  public void setCodeAttribute(ClassFile cf, Object tag, int attrIndex, String name, int attrLength) {
    pw.printf("%s[%d]: %s", indent, attrIndex, name);

    if (name == ClassFile.LINE_NUMBER_TABLE_ATTR) {
      cf.parseLineNumberTableAttr(this, tag);

    } else if (name == ClassFile.LOCAL_VAR_TABLE_ATTR) {
      cf.parseLocalVarTableAttr(this, tag);

    } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR){
        cf.parseTypeAnnotationsAttr(this, tag);

    } else {  // generic
      pw.printf(" ,length=%d,data=[", attrLength );
      printRawData(pw, cf, attrLength, 10);
      pw.println(']');
    }
  }
  @Override
  public void setCodeAttributesDone(ClassFile cf, Object tag){
    decIndent();
  }

  @Override
  public void setLineNumberTableCount(ClassFile cf, Object tag, int lineNumberCount) {
    pw.printf(", linenumber count=%d\n", lineNumberCount);
    incIndent();
  }
  @Override
  public void setLineNumber(ClassFile cf, Object tag, int lineIndex, int lineNumber, int startPc) {
    pw.printf("%s[%d]: line=%d, pc=%d\n", indent, lineIndex, lineNumber, startPc);
  }
  @Override
  public void setLineNumberTableDone(ClassFile cf, Object tag){
    decIndent();
  }

  @Override
  public void setLocalVarTableCount(ClassFile cf, Object tag, int localVarCount) {
    pw.printf(", localVar count=%d\n", localVarCount);
    incIndent();
  }
  @Override
  public void setLocalVar(ClassFile cf, Object tag, int localVarIndex,
          String varName, String descriptor, int scopeStartPc, int scopeEndPc, int slotIndex) {
    pw.printf("%s[%d]: %s, type=%s, scope=[%d..%d], slot=%d\n", indent, localVarIndex, varName, descriptor,
            scopeStartPc, scopeEndPc, slotIndex);
  }
  @Override
  public void setLocalVarTableDone(ClassFile cf, Object tag){
    decIndent();
  }

  //--- class attributes
  @Override
  public void setClassAttributeCount(ClassFile cf, int attrCount) {
    printSectionHeader( "class attributes");
    pw.printf("%sclass attribute count=%d\n", indent, attrCount);
    incIndent();
  }
  @Override
  public void setClassAttribute(ClassFile cf, int attrIndex, String name, int attrLength) {
    pw.printf("%s[%d]: %s", indent, attrIndex, name);

    if (name == ClassFile.SOURCE_FILE_ATTR) {
      cf.parseSourceFileAttr(this, null);

    } else if (name == ClassFile.DEPRECATED_ATTR) {

    } else if (name == ClassFile.INNER_CLASSES_ATTR) {
      cf.parseInnerClassesAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR){
      cf.parseTypeAnnotationsAttr(this, null);

    } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR){
      cf.parseAnnotationsAttr(this, null);

    } else if (name == ClassFile.SIGNATURE_ATTR){
      cf.parseSignatureAttr(this, null);

    } else if (name == ClassFile.ENCLOSING_METHOD_ATTR){
      cf.parseEnclosingMethodAttr(this, null);

    } else if (name == ClassFile.BOOTSTRAP_METHOD_ATTR){
      cf.parseBootstrapMethodAttr(this, null);

    } else {
      pw.printf(" ,length=%d,data=[", attrLength );
      printRawData(pw, cf, attrLength, 10);
      pw.println(']');
    }
  }
  @Override
  public void setClassAttributesDone(ClassFile cf){
    decIndent();
  }

  @Override
  public void setEnclosingMethod(ClassFile cf, Object tag, String enclosingClass, String enclosingMethod, String descriptor) {
    if (enclosingMethod != null){
      pw.printf(", enclosingClass=%s, method=%s%s\n", enclosingClass, enclosingMethod, descriptor);
    } else {
      pw.printf(", enclosingClass=%s\n", enclosingClass);
    }
  }

  
  @Override
  public void setSourceFile(ClassFile cf, Object tag, String pathName){
    pw.printf(", path=%s\n", pathName);
  }

  @Override
  public void setInnerClassCount(ClassFile cf, Object tag, int innerClsCount) {
    pw.printf( ", inner class count=%d\n", innerClsCount);
    incIndent();
  }
  @Override
  public void setInnerClass(ClassFile cf, Object tag, int innerClsIndex,
          String outerName, String innerName, String innerSimpleName, int accessFlags) {
    pw.printf("%s[%d]: %s, fullName=%s, outerClass=%s, flags=0x%X\n", indent, innerClsIndex,
            innerSimpleName, innerName, outerName, accessFlags);
  }
  @Override
  public void setInnerClassesDone(ClassFile cf, Object tag){
    decIndent();
  }

  @Override
  public void setBootstrapMethodCount (ClassFile cf, Object tag, int count) {
    pw.printf( ", bootstrap method count=%d\n", count);
    incIndent();
  }
  
  @Override
  public void setBootstrapMethod (ClassFile cf, Object tag, int idx, 
                                  int refKind, String cls, String mth, String descriptor, int[] cpArgs){
    String refTypeName = cf.getRefTypeName(refKind);
    pw.printf("%s[%d]: %s %s.%s%s\n", indent, idx, refTypeName, cls, mth, descriptor);
    incIndent();
    pw.printf("%smethod arg count: %d\n", indent, cpArgs.length);
    incIndent();
    for (int i=0; i<cpArgs.length; i++){
      int cpIdx = cpArgs[i];
      String arg = getBootstrapMethodArgAsString(cf, cpIdx);
      pw.printf("%s[%d]: %s\n", indent, i, arg);
    }
    decIndent();
    decIndent();
  }
  
  String getBootstrapMethodArgAsString (ClassFile cf, int cpIdx){
    StringBuilder sb = new StringBuilder();
    Object cpValue = cf.getCpValue(cpIdx);
    sb.append('@');
    sb.append(cpIdx);
    sb.append(" (");
    sb.append( cpValue);
    sb.append("): ");
    
    if (cpValue instanceof ClassFile.CpInfo){
      switch ((ClassFile.CpInfo)cpValue){
        case MethodType:
          sb.append( cf.methodTypeDescriptorAt(cpIdx));
          break;
        case MethodHandle:
          int methodRefIdx = cf.mhMethodRefIndexAt(cpIdx);
          
          sb.append( cf.getRefTypeName(cf.mhRefTypeAt(cpIdx)));
          sb.append(' ');
          sb.append( cf.methodClassNameAt(methodRefIdx));
          sb.append('.');
          sb.append( cf.methodNameAt(methodRefIdx));
          sb.append( cf.methodDescriptorAt(methodRefIdx));
          break;
        default:
          sb.append( cpValue.toString());
      }
    } else {
      sb.append( cpValue.toString());
    }
    
    return sb.toString();
  }
  
  @Override
  public void setBootstrapMethodsDone (ClassFile cf, Object tag) {
    decIndent();
  }
  
  @Override
  public void setAnnotationCount(ClassFile cf, Object tag, int annotationCount){
    pw.printf( " count=%d\n", annotationCount);
    incIndent();
  }
  @Override
  public void setAnnotation(ClassFile cf, Object tag, int annotationIndex, String annotationType){
    pw.printf("%s[%d]: %s", indent, annotationIndex, annotationType);
  }
  @Override
  public void setAnnotationsDone(ClassFile cf, Object tag){
    decIndent();
  }
  
  // Java 8 type annotations

  @Override
  public void setTypeAnnotationCount(ClassFile cf, Object tag, int annotationCount){
    pw.printf( " count=%d\n", annotationCount);
    incIndent();
  }

  @Override
  public void setTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                         int typeIndex, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, type index=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType), ClassFile.getTypePathEncoding(typePath), typeIndex);
  }
  @Override
  public void setSuperTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                     int superTypeIdx, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, super type index=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType),  ClassFile.getTypePathEncoding(typePath), superTypeIdx);
  }
  @Override
  public void setTypeParameterBoundAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType,
                                     int typeParamIdx, int boundIdx, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, type index=%d, bound=%d)", indent, annotationIndex, annotationType,
            ClassFile.getTargetTypeName(targetType),  ClassFile.getTypePathEncoding(typePath), typeParamIdx, boundIdx);
  }
  @Override
  public void setTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType,
                                short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s)", indent, annotationIndex, annotationType,
            ClassFile.getTargetTypeName(targetType), ClassFile.getTypePathEncoding(typePath));
  }
  @Override
  public void setFormalParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                           int formalParamIdx, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, formal param index=%d)", indent, annotationIndex, annotationType,
            ClassFile.getTargetTypeName(targetType),  ClassFile.getTypePathEncoding(typePath), formalParamIdx);
  }
  @Override
  public void setThrowsAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                  int throwsTypeIdx, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, throws index=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType),  ClassFile.getTypePathEncoding(typePath), throwsTypeIdx);
  }
  @Override
  public void setVariableAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                    long[] scopeEntries, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, scope=%s)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType), ClassFile.getTypePathEncoding(typePath), ClassFile.getScopeEncoding(scopeEntries));
    // 2do
  }
  @Override
  public void setExceptionParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                              int exceptionIndex, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, catch type index=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType),  ClassFile.getTypePathEncoding(typePath), exceptionIndex);
  }
  @Override
  public void setBytecodeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                    int offset, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, bytecode offset=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType), ClassFile.getTypePathEncoding(typePath), offset);
  }
  @Override
  public void setBytecodeTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                           int offset, int typeArgIdx, short[] typePath, String annotationType){
    pw.printf("%s[%d]: %s (%s, type path=%s, bytecode offset=%d, type arg=%d)", indent, annotationIndex, annotationType, 
            ClassFile.getTargetTypeName(targetType), ClassFile.getTypePathEncoding(typePath), offset, typeArgIdx);
  }
  
  @Override
  public void setTypeAnnotationsDone(ClassFile cf, Object tag) {
    decIndent();
  }
    
  @Override
  public void setAnnotationValueCount(ClassFile cf, Object tag, int annotationIndex, int nValuePairs){
    pw.printf(" valueCount=%d\n", nValuePairs);
    incIndent();
  }

  @Override
  public void setPrimitiveAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, Object val){
    if (arrayIndex < 0){
      pw.printf("%s[%d]: %s=%s\n", indent, annotationIndex, elementName, val);
    } else {
      if (arrayIndex==0) {
        pw.printf("%s[%d]: %s={", indent, valueIndex, elementName);
      }  else {
        pw.print(',');
      }
      pw.print(val);
    }
  }

  @Override
  public void setStringAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String s){
    if (arrayIndex < 0){
      pw.printf("%s[%d]: %s=\"%s\"\n", indent, annotationIndex, elementName, s);
    } else {
      if (arrayIndex==0) {
        pw.printf("%s[%d]: %s={", indent, valueIndex, elementName);
      }  else {
        pw.print(',');
      }
      pw.printf("\"%s\"", s);
    }
  }

  @Override
  public void setClassAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String typeName){
    if (arrayIndex < 0){
      pw.printf("%s[%d]: %s=class %s\n", indent, annotationIndex, elementName, typeName);
    } else {
      if (arrayIndex==0) {
        pw.printf("%s[%d]: %s={", indent, valueIndex, elementName);
      }  else {
        pw.print(',');
      }
      pw.printf("class %s", typeName);
    }
  }

  @Override
  public void setEnumAnnotationValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String enumType, String enumValue){
    if (arrayIndex < 0){
      pw.printf("%s[%d]: %s=%s.%s\n", indent, annotationIndex, elementName, enumType, enumValue);
    } else {
      if (arrayIndex==0) {
        pw.printf("%s[%d]: %s={", indent, valueIndex, elementName);
      }  else {
        pw.print(',');
      }
      pw.printf("%s.%s", enumType, enumValue);
    }
  }


  @Override
  public void setAnnotationValueElementCount(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int elementCount){
  }
  @Override
  public void setAnnotationValueElementsDone(ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName){
    pw.println("}");
  }

  @Override
  public void setAnnotationValuesDone(ClassFile cf, Object tag, int annotationIndex){
    decIndent();
  }

  @Override
  public void setParameterCount(ClassFile cf, Object tag, int parameterCount){
    pw.printf(" parameterCount=%d\n", parameterCount);
    incIndent();
  }

  @Override
  public void setParameterAnnotationCount(ClassFile cf, Object tag, int paramIndex, int annotationCount){
    pw.printf("%s[%d] count: %d\n", indent, paramIndex, annotationCount);
    incIndent();
  }

  @Override
  public void setParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, String annotationType){
    pw.printf("%s[%d]: %s", indent, annotationIndex, annotationType);
  }

  @Override
  public void setParameterAnnotationsDone(ClassFile cf, Object tag, int paramIndex){
    decIndent();
  }

  @Override
  public void setParametersDone(ClassFile cf, Object tag){
    decIndent();
  }


  @Override
  public void setSignature(ClassFile cf, Object tag, String signature){
    pw.printf(" %s\n", signature);
  }

  //--- internal stuff

  protected void printCp (PrintWriter pw, ClassFile cf){
    int nCpEntries = cf.getNumberOfCpEntries();

    for (int i=1; i<nCpEntries; i++){

      int j = cf.getDataPosOfCpEntry(i);

      pw.print("  [");
      pw.print(i);
      pw.print("]: ");

      if (j < 0) {
        pw.println("<unused>");
        continue;
      }

      switch (cf.u1(j)){
        case ClassFile.CONSTANT_UTF8:
          pw.print( "constant_utf8 {\"");
          pw.print( cf.getCpValue(i));
          pw.println("\"}");
          break;
        case ClassFile.CONSTANT_INTEGER:
          pw.print( "constant_integer {");
          pw.print( cf.getCpValue(i));
          pw.println("}");
          break;
        case ClassFile.CONSTANT_FLOAT:
          pw.print( "constant_float {");
          pw.print( cf.getCpValue(i));
          pw.println("}");
          break;
        case ClassFile.CONSTANT_LONG:
          pw.print( "constant_long {");
          pw.print( cf.getCpValue(i));
          pw.println("}");
          break;
        case ClassFile.CONSTANT_DOUBLE:
          pw.print( "constant_double {");
          pw.print( cf.getCpValue(i));
          pw.println("}");
          break;
        case ClassFile.CONSTANT_CLASS:
          pw.print("constant_class {name=#");
          pw.print( cf.u2(j+1));
          pw.print("(\"");
          pw.print( cf.classNameAt(i));
          pw.println("\")}");
          break;
        case ClassFile.CONSTANT_STRING:
          pw.print("constant_string {utf8=#");
          pw.print( cf.u2(j+1));
          pw.print("(\"");
          pw.print( cf.stringAt(i));
          pw.println("\")}");
          break;
        case ClassFile.FIELD_REF:
          printRef(pw, cf, i, j, "fieldref");
          break;
        case ClassFile.METHOD_REF:
          printRef(pw, cf, i, j, "methodref");
          break;
        case ClassFile.INTERFACE_METHOD_REF:
          printRef(pw, cf, i, j, "interface_methodref");
          break;
        case ClassFile.NAME_AND_TYPE:
          pw.print("name_and_type {name=#");
          pw.print( cf.u2(j+1));
          pw.print("(\"");
          pw.print(cf.utf8At(cf.u2(j+1)));
          pw.print("\"),desciptor=#");
          pw.print( cf.u2(j+3));
          pw.print("(\"");
          pw.print(cf.utf8At(cf.u2(j+3)));
          pw.println("\")}");
          break;
          
        case ClassFile.METHOD_HANDLE:
          pw.print("method_handle {");
          pw.print("(\"");
          pw.println("\")}");
          break;
          
        case ClassFile.METHOD_TYPE:
          pw.print("method_type {");
          pw.print("(\"");
          pw.println("\")}");
          break;
          
        case ClassFile.INVOKE_DYNAMIC:
          pw.print("invoke_dynamic {bootstrap=#");
          pw.print(cf.u2(j+1));
          pw.print("(\"");
          pw.println("\")}");
          break;
          
        default:
          pw.print("ERROR: illegal tag" + cf.u1(j));
      }
    }
    pw.println();
  }

  void printRef(PrintWriter pw, ClassFile cf, int cpIdx, int dataPos, String refType){
    pw.print(refType);
    pw.print(" {class=#");
    pw.print(cf.u2(dataPos + 1));
    pw.print("(\"");
    pw.print(cf.refClassNameAt(cpIdx));
    pw.print("\"),nameType=#");
    pw.print(cf.u2(dataPos + 3));
    pw.print("(\"");
    pw.print(cf.refNameAt(cpIdx));
    pw.print("\",\"");
    pw.print(cf.refDescriptorAt(cpIdx));
    pw.println("\")}");
  }

  void printRawData(PrintWriter pw, ClassFile cf, int dataLength, int maxBytes){
    int max = Math.min(dataLength, maxBytes);
    int max1 = max-1;
    for (int i=0; i<max1; i++){
      pw.printf("%02x ", cf.readUByte());
    }
    pw.printf("%02x", cf.readUByte());

    if (dataLength>maxBytes){
      pw.print("..");
    }
  }

  @Override
  public void setAnnotationFieldValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex, String elementName, int arrayIndex) {
    
  }
}
