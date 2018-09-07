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

import java.io.File;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.BailOut;
import gov.nasa.jpf.util.BinaryClassSource;
import gov.nasa.jpf.vm.ClassParseException;

/**
 * class to read and dissect Java classfile contents (as specified by the Java VM
 * spec  http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#16628
 */
public class ClassFile extends BinaryClassSource {

  public static final int CONSTANT_UTF8 = 1;
  public static final int CONSTANT_INTEGER = 3;
  public static final int CONSTANT_FLOAT = 4;
  public static final int CONSTANT_LONG = 5;
  public static final int CONSTANT_DOUBLE = 6;
  public static final int CONSTANT_CLASS = 7;
  public static final int CONSTANT_STRING = 8;
  public static final int FIELD_REF = 9;
  public static final int METHOD_REF = 10;
  public static final int INTERFACE_METHOD_REF = 11;
  public static final int NAME_AND_TYPE = 12;
  public static final int METHOD_HANDLE = 15;
  public static final int METHOD_TYPE = 16;
  public static final int INVOKE_DYNAMIC = 18;

  public static final int REF_GETFIELD = 1;
  public static final int REF_GETSTATIC = 2;
  public static final int REF_PUTFIELD = 3;
  public static final int REF_PUTSTATIC = 4;
  public static final int REF_INVOKEVIRTUAL = 5;
  public static final int REF_INVOKESTATIC = 6;
  public static final int REF_INVOKESPECIAL = 7;
  public static final int REF_NEW_INVOKESPECIAL = 8;
  public static final int REF_INVOKEINTERFACE = 9;

  // used to store types in cpValue[]
  public static enum CpInfo {
    Unused_0,                 // 0
    ConstantUtf8,             // 1
    Unused_2,                 // 2
    ConstantInteger,          // 3
    ConstantFloat,            // 4
    ConstantLong,             // 5
    ConstantDouble,           // 6
    ConstantClass,            // 7
    ConstantString,           // 8
    FieldRef,                 // 9
    MethodRef,                // 10
    InterfaceMethodRef,       // 11
    NameAndType,              // 12
    Unused_13,
    Unused_14,
    MethodHandle,             // 15
    MethodType,               // 16
    Unused_17,
    InvokeDynamic             // 18
  }

  // <2do> this is going away
  String requestedTypeName; // the type name that caused this classfile to be loaded

  // the const pool
  int[] cpPos;     // cpPos[i] holds data start index for cp_entry i (0 is unused)
  Object[] cpValue; // cpValue[i] hold the String/Integer/Float/Double associated with corresponding cp_entries
  
  //--- ctors
  public ClassFile (byte[] data, int offset){
    super(data,offset);
  }

  public ClassFile (byte[] data){
    super(data,0);
  }

  public ClassFile (String typeName, byte[] data){
    super(data,0);
    
    this.requestedTypeName = typeName;
  }
  
  public ClassFile (String typeName, byte[] data, int offset){
    super(data, offset);
    
    this.requestedTypeName = typeName;
  }

  public ClassFile (File file) throws ClassParseException {
    super(file);
  }

  public ClassFile (String pathName)  throws ClassParseException {
    super( new File(pathName));
  }



  
  /**
   * set classfile data.  This is mainly provided to allow
   * on-the-fly classfile instrumentation with 3rd party libraries
   * 
   * BEWARE - like getData(), this method can cause parsing to fail if the
   * provided data does not conform to the VM specs. In particular, this
   * method should ONLY be called before executing parse(ClassFileReader) and
   * will otherwise throw a JPFException
   */
  public void setData(byte[] newData){
    if (cpPos != null){
      throw new JPFException("concurrent modification of ClassFile data");
    }
    
    data = newData;
  }
  
  /**
   * return the typename this classfile gets loaded for
   * <2do> this is going away
   */
  public String getRequestedTypeName(){
    return requestedTypeName;
  }


  //--- general attributes
  public static final String SYNTHETIC_ATTR = "Synthetic";
  public static final String DEPRECATED_ATTR = "Deprecated";
  public static final String SIGNATURE_ATTR = "Signature";
  public static final String RUNTIME_INVISIBLE_ANNOTATIONS_ATTR = "RuntimeInvisibleAnnotations";
  public static final String RUNTIME_VISIBLE_ANNOTATIONS_ATTR = "RuntimeVisibleAnnotations";
  public static final String RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR = "RuntimeVisibleTypeAnnotations";

  //--- standard field attributes
  public static final String CONST_VALUE_ATTR = "ConstantValue";

  protected final static String[] stdFieldAttrs = {
    CONST_VALUE_ATTR, SYNTHETIC_ATTR, DEPRECATED_ATTR, SIGNATURE_ATTR,
    RUNTIME_INVISIBLE_ANNOTATIONS_ATTR, RUNTIME_VISIBLE_ANNOTATIONS_ATTR, RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR };


  //--- standard method attributes
  public static final String CODE_ATTR = "Code";
  public static final String EXCEPTIONS_ATTR = "Exceptions";
  public static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS_ATTR = "RuntimeInvisibleParameterAnnotations";
  public static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS_ATTR = "RuntimeVisibleParameterAnnotations";
  public static final String ANNOTATIONDEFAULT_ATTR = "AnnotationDefault";

  protected final static String[] stdMethodAttrs = { 
    CODE_ATTR, EXCEPTIONS_ATTR, SYNTHETIC_ATTR, DEPRECATED_ATTR, SIGNATURE_ATTR,
    RUNTIME_INVISIBLE_ANNOTATIONS_ATTR, RUNTIME_VISIBLE_ANNOTATIONS_ATTR,
    RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS_ATTR,
    RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS_ATTR,
    RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR,
    ANNOTATIONDEFAULT_ATTR
  };


  //--- standard code attributes
  public static final String LINE_NUMBER_TABLE_ATTR = "LineNumberTable";
  public static final String LOCAL_VAR_TABLE_ATTR = "LocalVariableTable";

  protected final static String[] stdCodeAttrs = { LINE_NUMBER_TABLE_ATTR, LOCAL_VAR_TABLE_ATTR, RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR };


  //--- standard class attributes
  public static final String  SOURCE_FILE_ATTR = "SourceFile";
  public static final String  INNER_CLASSES_ATTR = "InnerClasses";
  public static final String  ENCLOSING_METHOD_ATTR = "EnclosingMethod";
  public static final String  BOOTSTRAP_METHOD_ATTR = "BootstrapMethods";
  
  protected final static String[] stdClassAttrs = {
    SOURCE_FILE_ATTR, DEPRECATED_ATTR, INNER_CLASSES_ATTR, DEPRECATED_ATTR, SIGNATURE_ATTR,
    RUNTIME_INVISIBLE_ANNOTATIONS_ATTR, RUNTIME_VISIBLE_ANNOTATIONS_ATTR, RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR,
    ENCLOSING_METHOD_ATTR, BOOTSTRAP_METHOD_ATTR };


  protected String internStdAttrName(int cpIdx, String name, String[] stdNames){
    for (int i=0; i<stdNames.length; i++){
      if (stdNames[i] == name) return name;
    }
    for (int i=0; i<stdNames.length; i++){
      String stdName = stdNames[i];
      if (stdName.equals(name)){
        cpValue[cpIdx] = stdName;
        return stdName;
      }
    }
    return name;
  }


  //--- constpool access

  //--- the primitive info cpValue
  public String utf8At(int utf8InfoIdx){
    //assert data[cpPos[utf8InfoIdx]] == 1 : "not a utf8_info tag";
    return (String) cpValue[utf8InfoIdx];
  }

  public int intAt(int intInfoIdx){
    //assert data[cpPos[intInfoIdx]] == 3 : "not a int_info tag";
    return (Integer) cpValue[intInfoIdx];
  }

  public float floatAt(int floatInfoIdx){
    //assert data[cpPos[floatInfoIdx]] == 4 : "not a float_info tag";
    return (Float) cpValue[floatInfoIdx];
  }

  public long longAt(int longInfoIdx){
    //assert data[cpPos[longInfoIdx]] == 5 : "not a long_info tag";
    return (Long) cpValue[longInfoIdx];
  }

  public double doubleAt(int doubleInfoIdx){
    //assert data[cpPos[doubleInfoIdx]] == 6 : "not a double_info tag";
    return (Double) cpValue[doubleInfoIdx];
  }

  //--- those two are delegated but resolved
  public String classNameAt(int classInfoIdx){
    //assert data[cpPos[classInfoIdx]] == 7 : "not a Class_info tag";
    return (String) cpValue[classInfoIdx];
  }

  public String stringAt(int stringInfoIdx){
    //assert data[cpPos[stringInfoIdx]] == 8 : "not a String_info tag";
    return (String) cpValue[stringInfoIdx];
  }

  //--- composite infos

  // the generic ones (if we don't care what kind of reference type this is)
  public String refClassNameAt(int cpIdx){
    return (String) cpValue[ u2(cpPos[cpIdx]+1)];
  }
  public String refNameAt(int cpIdx){
    return utf8At( u2( cpPos[ u2(cpPos[cpIdx]+3)]+1));
  }
  public String refDescriptorAt(int cpIdx){
    return utf8At( u2( cpPos[ u2(cpPos[cpIdx]+3)]+3));
  }

  public int mhRefTypeAt (int methodHandleInfoIdx){
    return u1(cpPos[methodHandleInfoIdx]+1);
  }
  public int mhMethodRefIndexAt  (int methodHandleInfoIdx){
    return u2(cpPos[methodHandleInfoIdx]+2);
  }
  
  // those could check ref types
  public String fieldClassNameAt(int fieldRefInfoIdx){
    //assert data[cpPos[fieldRefInfoIdx]] == 9 : "not a Fieldref_info tag";
    return (String) cpValue[ u2(cpPos[fieldRefInfoIdx]+1)];
  }
  public String fieldNameAt(int fieldRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[fieldRefInfoIdx]+3)]+1));
  }
  public String fieldDescriptorAt(int fieldRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[fieldRefInfoIdx]+3)]+3));
  }

  public String methodClassNameAt(int methodRefInfoIdx){
    return (String) cpValue[ u2(cpPos[methodRefInfoIdx]+1)];
  }
  public String methodNameAt(int methodRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[methodRefInfoIdx]+3)]+1));
  }
  public String methodDescriptorAt(int methodRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[methodRefInfoIdx]+3)]+3));
  }

  public String methodTypeDescriptorAt (int methodTypeInfoIdx){
    return utf8At( u2(cpPos[methodTypeInfoIdx]+1));
  }
  
  public String interfaceMethodClassNameAt(int ifcMethodRefInfoIdx){
    return (String) cpValue[ u2(cpPos[ifcMethodRefInfoIdx]+1)];
  }
  public String interfaceMethodNameAt(int ifcMethodRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[ifcMethodRefInfoIdx]+3)]+1));
  }
  public String interfaceMethodDescriptorAt(int ifcMethodRefInfoIdx){
    return utf8At( u2( cpPos[ u2(cpPos[ifcMethodRefInfoIdx]+3)]+3));
  }
  
  public int bootstrapMethodIndex (int cpInvokeDynamicIndex){
    return u2(cpPos[cpInvokeDynamicIndex]+1);
  }
  public String samMethodNameAt(int cpInvokeDynamicIndex) {
    return utf8At( u2( cpPos[ u2(cpPos[cpInvokeDynamicIndex]+3)]+1)); 
  }
  public String callSiteDescriptor(int cpInvokeDynamicIndex) {
    return utf8At( u2( cpPos[ u2(cpPos[cpInvokeDynamicIndex]+3)]+3)); 
  }
  
  public String getRefTypeName (int refCode){
    switch (refCode){
      case REF_GETFIELD:      return "getfield";
      case REF_GETSTATIC:     return "getstatic";
      case REF_PUTFIELD:      return "putfield";
      case REF_PUTSTATIC:     return "putstatic";
      case REF_INVOKEVIRTUAL: return "invokevirtual";
      case REF_INVOKESTATIC:  return "invokestatic";
      case REF_INVOKESPECIAL: return "invokespecial";
      case REF_NEW_INVOKESPECIAL: return "new-invokespecial";
      case REF_INVOKEINTERFACE: return "invokeinterface";
      default:
        return "<unknown>";
    }
  }
  
  public String getTypeName (int typeCode){
    switch(typeCode){
      case 4: return "boolean";
      case 5: return "char";
      case 6: return "float";
      case 7: return "double";
      case 8: return "byte";
      case 9: return "short";
      case 10: return "int";
      case 11: return "long";
      default:
        return "<unknown>";
    }
  }

  @Override
  public int getPos(){
    return pos;
  }

  public int getPc(){
    return pc;
  }

  //--- traverse/analyze the const pool (this is rather exotic)

  public int getNumberOfCpEntries(){
    return cpValue.length;
  }

  public Object getCpValue (int i){
    return cpValue[i];
  }

  public int getCpTag (int i){
    return data[cpPos[i]];
  }

  /**
   * the result can be used as input for u2(dataIndex)
   *
   * NOTE - this returns -1 for the dreaded unused extra entries associated
   * with ConstantDouble and ConstantLong
   */
  public int getDataPosOfCpEntry (int i){
    return cpPos[i];
  }

  //--- standard attributes

  public Object getConstValueAttribute(int dataPos){
    int cpIdx = u2(dataPos);
    Object v = cpValue[cpIdx];
    return v;
  }

  public String getSourceFileAttribute(int dataPos){
    // SourceFile_attribute { u2 attr_name_idx; u4 attr_length; u2 sourcefile_idx<utf8>; }

    int cpIdx = u2(dataPos + 6);
    Object v = cpValue[cpIdx];
    return (String)v;
  }

  
  //--- low level readers

  public final int u1(int dataIdx){
    return data[dataIdx] & 0xff;
  }

  public final int u2(int dataIdx){
    return ((data[dataIdx]&0xff) << 8) | (data[dataIdx+1]&0xff);
  }

  public final int i1(int dataIdx) {
    return data[dataIdx++];
  }

  public final int i2(int dataIdx) {
    int idx = dataIdx;
    return (data[idx++] << 8) | (data[idx]&0xff);
  }

  public final int readU2(){
    int idx = pos;
    pos += 2;
    return ((data[idx++]&0xff) << 8) | (data[idx]&0xff);
  }

  public final int readI2() {
    int idx = pos;
    pos += 2;
    return (data[idx++] << 8) | (data[idx]&0xff);
  }

  public final int readI4(){
    int idx = pos;
    pos += 4;
    byte[] data = this.data;

    return (data[idx++] <<24) | ((data[idx++]&0xff) << 16) | ((data[idx++]&0xff) << 8) | (data[idx]&0xff);
  }

  
  //--- reader notifications
  private void setClass(ClassFileReader reader, String clsName, String superClsName, int flags, int cpCount) throws ClassParseException {
    int p = pos;
    reader.setClass( this, clsName, superClsName, flags, cpCount);
    pos = p;
  }

  private void setInterfaceCount(ClassFileReader reader, int ifcCount){
    int p = pos;
    reader.setInterfaceCount( this, ifcCount);
    pos = p;
  }
  private void setInterface(ClassFileReader reader, int ifcIndex, String ifcName){
    int p = pos;
    reader.setInterface( this, ifcIndex, ifcName);
    pos = p;
  }
  private void setInterfacesDone(ClassFileReader reader){
    int p = pos;
    reader.setInterfacesDone( this);
    pos = p;
  }


  private void setFieldCount(ClassFileReader reader, int fieldCount){
    int p = pos;
    reader.setFieldCount( this, fieldCount);
    pos = p;

  }
  private void setField(ClassFileReader reader, int fieldIndex, int accessFlags, String name, String descriptor){
    int p = pos;
    reader.setField( this, fieldIndex, accessFlags, name, descriptor);
    pos = p;
  }
  private void setFieldAttributeCount(ClassFileReader reader, int fieldIndex, int attrCount){
    int p = pos;
    reader.setFieldAttributeCount( this, fieldIndex, attrCount);
    pos = p;
  }
  private void setFieldAttribute(ClassFileReader reader, int fieldIndex, int attrIndex, String name, int attrLength){
    int p = pos + attrLength;
    reader.setFieldAttribute( this, fieldIndex, attrIndex, name, attrLength);
    pos = p;
  }
  private void setFieldAttributesDone(ClassFileReader reader, int fieldIndex){
    int p = pos;
    reader.setFieldAttributesDone( this, fieldIndex);
    pos = p;
  }
  private void setFieldDone(ClassFileReader reader, int fieldIndex){
    int p = pos;
    reader.setFieldDone( this, fieldIndex);
    pos = p;
  }
  private void setFieldsDone(ClassFileReader reader){
    int p = pos;
    reader.setFieldsDone( this);
    pos = p;
  }
  private void setConstantValue(ClassFileReader reader, Object tag, Object value){
    int p = pos;
    reader.setConstantValue( this, tag, value);
    pos = p;
  }

  private void setMethodCount(ClassFileReader reader, int methodCount){
    int p = pos;
    reader.setMethodCount( this, methodCount);
    pos = p;
  }
  private void setMethod(ClassFileReader reader, int methodIndex, int accessFlags, String name, String descriptor){
    int p = pos;
    reader.setMethod( this, methodIndex, accessFlags, name, descriptor);
    pos = p;
  }
  private void setMethodAttributeCount(ClassFileReader reader, int methodIndex, int attrCount){
    int p = pos;
    reader.setMethodAttributeCount( this, methodIndex, attrCount);
    pos = p;
  }
  private void setMethodAttribute(ClassFileReader reader, int methodIndex, int attrIndex, String name, int attrLength){
    int p = pos + attrLength;
    reader.setMethodAttribute( this, methodIndex, attrIndex, name, attrLength);
    pos = p;
  }
  private void setMethodAttributesDone(ClassFileReader reader, int methodIndex){
    int p = pos;
    reader.setMethodAttributesDone( this, methodIndex);
    pos = p;
  }
  private void setMethodDone(ClassFileReader reader, int methodIndex){
    int p = pos;
    reader.setMethodDone( this, methodIndex);
    pos = p;
  }
  private void setMethodsDone(ClassFileReader reader){
    int p = pos;
    reader.setMethodsDone( this);
    pos = p;
  }
  private void setExceptionCount(ClassFileReader reader, Object tag, int exceptionCount){
    int p = pos;
    reader.setExceptionCount( this, tag, exceptionCount);
    pos = p;
  }
  private void setExceptionsDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setExceptionsDone( this, tag);
    pos = p;
  }
  private void setException(ClassFileReader reader, Object tag, int exceptionIndex, String exceptionType){
    int p = pos;
    reader.setException( this, tag, exceptionIndex, exceptionType);
    pos = p;
  }
  private void setCode(ClassFileReader reader, Object tag, int maxStack, int maxLocals, int codeLength){
    int p = pos + codeLength;
    reader.setCode( this, tag, maxStack, maxLocals, codeLength);
    pos = p;
  }
  private void setExceptionTableCount(ClassFileReader reader, Object tag, int exceptionTableCount){
    int p = pos;
    reader.setExceptionHandlerTableCount( this, tag, exceptionTableCount);
    pos = p;
  }
  private void setExceptionTableEntry(ClassFileReader reader, Object tag, int exceptionIndex,
          int startPc, int endPc, int handlerPc, String catchType){
    int p = pos;
    reader.setExceptionHandler( this, tag, exceptionIndex, startPc, endPc, handlerPc, catchType);
    pos = p;
  }
  private void setExceptionTableDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setExceptionHandlerTableDone( this, tag);
    pos = p;
  }

  private void setCodeAttributeCount(ClassFileReader reader, Object tag, int attrCount){
    int p = pos;
    reader.setCodeAttributeCount( this, tag, attrCount);
    pos = p;
  }
  private void setCodeAttribute(ClassFileReader reader, Object tag, int attrIndex, String name, int attrLength){
    int p = pos + attrLength;
    reader.setCodeAttribute( this, tag, attrIndex, name, attrLength);
    pos = p;
  }
  private void setCodeAttributesDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setCodeAttributesDone( this, tag);
    pos = p;
  }
          
  private void setLineNumberTableCount(ClassFileReader reader, Object tag, int lineNumberCount){
    int p = pos;
    reader.setLineNumberTableCount( this, tag, lineNumberCount);
    pos = p;
  }
  private void setLineNumber(ClassFileReader reader, Object tag, int lineIndex, int lineNumber, int startPc){
    int p = pos;
    reader.setLineNumber( this, tag, lineIndex, lineNumber, startPc);
    pos = p;
  }
  private void setLineNumberTableDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setLineNumberTableDone( this, tag);
    pos = p;
  }

  private void setLocalVarTableCount(ClassFileReader reader, Object tag, int localVarCount){
    int p = pos;
    reader.setLocalVarTableCount( this, tag, localVarCount);
    pos = p;
  }
  private void setLocalVar(ClassFileReader reader, Object tag, int localVarIndex, String varName, String descriptor,
                      int scopeStartPc, int scopeEndPc, int slotIndex){
    int p = pos;
    reader.setLocalVar( this, tag, localVarIndex, varName, descriptor, scopeStartPc, scopeEndPc, slotIndex);
    pos = p;
  }
  private void setLocalVarTableDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setLocalVarTableDone( this, tag);
    pos = p;
  }


  private void setClassAttributeCount(ClassFileReader reader, int attrCount){
    int p = pos;
    reader.setClassAttributeCount( this, attrCount);
    pos = p;
  }
  private void setClassAttribute(ClassFileReader reader, int attrIndex, String name, int attrLength){
    int p = pos + attrLength;
    reader.setClassAttribute( this, attrIndex, name, attrLength);
    pos = p;
  }
  private void setClassAttributesDone(ClassFileReader reader){
    int p = pos;
    reader.setClassAttributesDone(this);
    pos = p;
  }

  private void setSourceFile(ClassFileReader reader, Object tag, String pathName){
    int p = pos;
    reader.setSourceFile( this, tag, pathName);
    pos = p;
  }
  
  private void setBootstrapMethodCount (ClassFileReader reader, Object tag, int bootstrapMethodCount){
    int p = pos;
    reader.setBootstrapMethodCount( this, tag, bootstrapMethodCount);
    pos = p;    
  }
  private void setBootstrapMethod (ClassFileReader reader, Object tag, int idx, 
                                   int refKind, String cls, String mth, String descriptor, int[] cpArgs){
    int p = pos;
    reader.setBootstrapMethod( this, tag, idx, refKind, cls, mth, descriptor, cpArgs);
    pos = p;    
  }
  private void setBootstrapMethodsDone (ClassFileReader reader, Object tag){
    int p = pos;
    reader.setBootstrapMethodsDone( this, tag);
    pos = p;    
  }
  
  private void setInnerClassCount(ClassFileReader reader, Object tag, int innerClsCount){
    int p = pos;
    reader.setInnerClassCount( this, tag, innerClsCount);
    pos = p;
  }
  private void setInnerClass(ClassFileReader reader, Object tag, int innerClsIndex, String outerName, String innerName,
          String innerSimpleName, int accessFlags){
    int p = pos;
    reader.setInnerClass( this, tag, innerClsIndex, outerName, innerName, innerSimpleName, accessFlags);
    pos = p;
  }
  private void setEnclosingMethod(ClassFileReader reader, Object tag, String enclosingClass, String enclosedMethod, String descriptor){
    int p = pos;
	  reader.setEnclosingMethod( this, tag, enclosingClass, enclosedMethod, descriptor);
	  pos = p;
  }
  private void setInnerClassesDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setInnerClassesDone(this, tag);
    pos = p;
  }

  private void setAnnotationCount(ClassFileReader reader, Object tag, int annotationCount){
    int p = pos;
    reader.setAnnotationCount( this, tag, annotationCount);
    pos = p;
  }
  private boolean setAnnotation(ClassFileReader reader, Object tag, int annotationIndex, String annotationType){
    int p = pos;
    try {
      reader.setAnnotation( this, tag, annotationIndex, annotationType);
      pos = p;
      return true;
    } catch (SkipAnnotation sa) {
      this.skipAnnotation(false);
      return false;
    }
  }
  
  /*
   * This is largely lifted from AnnotationParser.java
   *   annotation {
   *     u2 type_index;
   *     u2 num_element_value_pairs;
   *     {
   *       u2 element_name_index;
   *       element_value value;
   *     } element_value_pairs[num_element_value_pairs]
   *   }
   */
  private void skipAnnotation(boolean skipTypeIndex) {
    if(skipTypeIndex) { // we may want to skip after reading the type name
      readU2();
    }
    int numKV = readU2();
    for(int i = 0; i < numKV; i++) {
      readU2(); // skip name
      skipMemberValue();
    }
  }

  /*
   * Skips an element_value
   */
  private void skipMemberValue() {
    int tag = readUByte();
    switch(tag) {
    case 'e': // Enum value
      pos += 4; // an enum value is a struct of two shorts, for 4 bytes total
      break;
    case '@':
      skipAnnotation(true);
      break;
    case '[':
      skipArray();
      break;
    default:
      pos += 2; // either two bye const val index or two byte class info index
    }
  }

  private void skipArray() {
    int len = readU2();
    for(int i = 0; i < len; i++) {
      skipMemberValue();
    }
  }

  private void setAnnotationsDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setAnnotationsDone(this, tag);
    pos = p;
  }

  private void setTypeAnnotationCount(ClassFileReader reader, Object tag, int annotationCount){
    int p = pos;
    reader.setTypeAnnotationCount( this, tag, annotationCount);
    pos = p;
  }
  private void setTypeAnnotationsDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setTypeAnnotationsDone(this, tag);
    pos = p;
  }

  
  private void setAnnotationValueCount(ClassFileReader reader, Object tag, int annotationIndex, int nValuePairs){
    int p = pos;
    reader.setAnnotationValueCount( this, tag, annotationIndex, nValuePairs);
    pos = p;
  }
  private void setPrimitiveAnnotationValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, Object val){
    int p = pos;
    reader.setPrimitiveAnnotationValue( this, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
    pos = p;
  }
  private void setStringAnnotationValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String s){
    int p = pos;
    reader.setStringAnnotationValue( this, tag, annotationIndex, valueIndex, elementName, arrayIndex, s);
    pos = p;
  }
  private void setClassAnnotationValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String typeName){
    int p = pos;
    reader.setClassAnnotationValue( this, tag, annotationIndex, valueIndex, elementName, arrayIndex, typeName);
    pos = p;
  }
  
  private void setAnnotationFieldValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex, String elementName, int arrayIndex) {
    int p = pos;
    reader.setAnnotationFieldValue( this, tag, annotationIndex, valueIndex, elementName, arrayIndex);
    pos = p;
  }
  
  private void setEnumAnnotationValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String enumType, String enumValue){
    int p = pos;
    reader.setEnumAnnotationValue( this, tag, annotationIndex, valueIndex, elementName, arrayIndex, enumType, enumValue);
    pos = p;
  }

  private void setAnnotationValueElementCount(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName, int elementCount){
    int p = pos;
    reader.setAnnotationValueElementCount(this, tag, annotationIndex, valueIndex, elementName, elementCount);
    pos = p;
  }
  private void setAnnotationValueElementsDone(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex,
          String elementName){
    int p = pos;
    reader.setAnnotationValueElementsDone(this, tag, annotationIndex, valueIndex, elementName);
    pos = p;
  }

  public void setAnnotationValuesDone(ClassFileReader reader, Object tag, int annotationIndex){
    int p = pos;
    reader.setAnnotationValuesDone(this, tag, annotationIndex);
    pos = p;
  }

  private void setParameterCount(ClassFileReader reader, Object tag, int parameterCount){
    int p = pos;
    reader.setParameterCount(this, tag, parameterCount);
    pos = p;
  }
  private void setParameterAnnotationCount(ClassFileReader reader, Object tag, int paramIndex, int annotationCount){
    int p = pos;
    reader.setParameterAnnotationCount(this, tag, paramIndex, annotationCount);
    pos = p;
  }

  private boolean setParameterAnnotation(ClassFileReader reader, Object tag, int annotationIndex, String annotationType){
    int p = pos;
    try {
      reader.setParameterAnnotation( this, tag, annotationIndex, annotationType);
      pos = p;
      return true;
    } catch(SkipAnnotation s) {
      this.skipAnnotation(false);
      return false;
    }
  }
  private void setParameterAnnotationsDone(ClassFileReader reader, Object tag, int paramIndex){
    int p = pos;
    reader.setParameterAnnotationsDone(this, tag, paramIndex);
    pos = p;
  }
  private void setParametersDone(ClassFileReader reader, Object tag){
    int p = pos;
    reader.setParametersDone(this, tag);
    pos = p;
  }

  public void setSignature(ClassFileReader reader, Object tag, String signature){
    int p = pos;
    reader.setSignature(this, tag, signature);
    pos = p;
  }

  //--- parsing

  /**
   * this is the main parsing routine that uses the ClassFileReader interface
   * to tell clients about the classfile contents
   *
   * ClassFile structure: http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#74353
   *   u4 magic;  // 0xcafebabe
   *   u2 minor_version;
   *   u2 major_version;
   *
   *   u2 constant_pool_count;
   *   cp_entry constant_pool[constant_pool_count-1];
   *   u2 access_flags;
   *
   *   u2 this_class;
   *   u2 super_class;
   *
   *   u2 interfaces_count;
   *   u2 interfaces[interfaces_count];
   *
   *   u2 fields_count;
   *   field_info fields[fields_count];
   *
   *   u2 methods_count;
   *   method_info methods[methods_count];
   *
   *   u2 attributes_count;
   *   attribute_info attributes[attributes_count];
   */
  public void parse( ClassFileReader reader)  throws ClassParseException {
    int cpIdx;

    try {
      // yeah, cafebabe
      int magic = readI4();
      if (magic != 0xCAFEBABE) {
        error("wrong magic: " + Integer.toHexString(magic));
      }

      // we don't do much with the version numbers yet
      int minor = readU2();
      int major = readU2();

      // get the const pool
      int cpCount = readU2();
      cpPos = new int[cpCount];
      cpValue = new Object[cpCount];
      parseCp(cpCount);

      // the class essentials
      int accessFlags = readU2();

      cpIdx = readU2();
      String clsName = (String) cpValue[cpIdx];

      cpIdx = readU2();
      String superClsName = (String) cpValue[cpIdx];


      setClass(reader, clsName, superClsName, accessFlags, cpCount);

      // interfaces
      int ifcCount = readU2();
      parseInterfaces(reader, ifcCount);

      // fields
      int fieldCount = readU2();
      parseFields(reader, fieldCount);

      // methods
      int methodCount = readU2();
      parseMethods(reader, methodCount);

      // class attributes
      int classAttrCount = readU2();
      parseClassAttributes(reader, classAttrCount);

    } catch (BailOut x){
      // nothing, just a control exception to shortcut the classfile parsing
    }
  }


  //--- constpool parsing

  public static String readModifiedUTF8String( byte[] data, int pos, int len) throws ClassParseException {
    
    int n = 0; // the number of chars in buf
    char[] buf = new char[len]; // it can't be more, but it can be less chars
    
    // \u0001 - \u007f             : single byte chars:  0xxxxxxx
    // \u0000 and \u0080 - \u07ff  : double byte chars:  110xxxxx, 10xxxxxx
    // \u0800 - \uffff             : tripple byte chars: 1110xxxx, 10xxxxxx, 10xxxxxx
    
    int max = pos+len;
    for (int i=pos; i<max; i++){
      int c = data[i] & 0xff;
      if ((c & 0x80) == 0){ // single byte char  0xxxxxxx
        buf[n++] = (char)c;
        
      } else {
        if ((c & 0x40) != 0){      // 11xxxxxx
          
          // for the sake of efficiency, we don't check for the trailing zero bit in the marker,
          // we just mask it out
          if ((c & 0x20) == 0) {   // 110xxxxx - double byte char
            buf[n++] = (char) (((c & 0x1f) << 6) | (data[++i] & 0x3f));
            
          } else {                 // 1110xxxx - tripple byte char
            buf[n++] = (char) (((c & 0x0f) << 12) | ((data[++i] & 0x3f) << 6) | (data[++i] & 0x3f));
          }
          
        } else {
          throw new ClassParseException("malformed modified UTF-8 input: ");
        }
      }
    }
    
    return new String(buf, 0, n);
  }

  
  // the protected methods are called automatically, the public parse..Attr() methods
  // are called optionally from the corresponding ClassFileReader.set..Attribute() method.
  // Note that these calls have to provide the ClassFileReader as an argument because
  // we might actually switch to another reader (e.g. MethodInfos for parseCodeAttr)

  protected void parseCp(int cpCount)  throws ClassParseException {
    int j = pos;

    byte[] data = this.data;
    int[] dataIdx = this.cpPos;
    Object[] values = this.cpValue;

    //--- first pass: store data index values and convert non-delegating constant values
    // cp_entry[0] is traditionally unused
    for (int i=1; i<cpCount; i++) {
      switch (data[j]){
        case 0:
          error("illegal constpool tag");

        case CONSTANT_UTF8:  // utf8_info { u1 tag; u2 length; u1 bytes[length]; }
          dataIdx[i] = j++;
          int len = ((data[j++]&0xff) <<8) | (data[j++]&0xff);

          String s = readModifiedUTF8String( data, j, len);
          values[i] = s;

          j += len;
          break;

        case 2:
          error("illegal constpool tag");

        case CONSTANT_INTEGER:  // Integer_info { u1 tag; u4 bytes; }
          dataIdx[i] = j++;

          int iVal = (data[j++]&0xff)<<24 | (data[j++]&0xff)<<16 | (data[j++]&0xff)<<8 | (data[j++]&0xff);
          values[i] = new Integer(iVal);
          break;

        case CONSTANT_FLOAT:  // Float_info  { u1 tag; u4 bytes; }
          dataIdx[i] = j++;

          int iBits = (data[j++]&0xff)<<24 | (data[j++]&0xff)<<16 | (data[j++]&0xff)<<8 | (data[j++]&0xff);
          float fVal = Float.intBitsToFloat(iBits);
          values[i] = new Float(fVal);
          break;

        case CONSTANT_LONG:  // Long_info { u1 tag; u4 high_bytes; u4 low_bytes; }
          dataIdx[i] = j++;
          long lVal =  (data[j++]&0xffL)<<56 | (data[j++]&0xffL)<<48 | (data[j++]&0xffL)<<40 | (data[j++]&0xffL)<<32
                    | (data[j++]&0xffL)<<24 | (data[j++]&0xffL)<<16 | (data[j++]&0xffL)<<8 | (data[j++]&0xffL);
          values[i] = new Long(lVal);

          dataIdx[++i] = -1;  // 8 byte cpValue occupy 2 index slots
          break;

        case CONSTANT_DOUBLE:  // Double_info  { u1 tag; u4 high_bytes; u4 low_bytes; }
          dataIdx[i] = j++;

          long lBits = (data[j++]&0xffL)<<56 | (data[j++]&0xffL)<<48 | (data[j++]&0xffL)<<40 | (data[j++]&0xffL)<<32
                    | (data[j++]&0xffL)<<24 | (data[j++]&0xffL)<<16 | (data[j++]&0xffL)<<8 | (data[j++]&0xffL);
          double dVal = Double.longBitsToDouble(lBits);
          values[i] = new Double(dVal);

          dataIdx[++i] = -1;  // 8 byte cpValue occupy 2 index slots
          break;

        case CONSTANT_CLASS:  // Class_info { u1 tag; u2 name_index<utf8>; }
          dataIdx[i] = j;
          values[i] = CpInfo.ConstantClass;

          j += 3;
          break;

        case CONSTANT_STRING:  // String_info { u1 tag; u2 string_index<utf8>; }
          dataIdx[i] = j;
          values[i] = CpInfo.ConstantString;

          j += 3;
          break;

        case FIELD_REF:  // Fieldref_info { u1 tag; u2 class_index; u2 name_and_type_index; }
          dataIdx[i] = j;
          values[i] = CpInfo.FieldRef;
          j += 5;
          break;

        case METHOD_REF: // Methodref_info  { u1 tag; u2 class_index; u2 name_and_type_index; }
          dataIdx[i] = j;
          values[i] = CpInfo.MethodRef;
          j += 5;
          break;

        case INTERFACE_METHOD_REF: // InterfaceMethodref_info { u1 tag; u2 class_index; u2 name_and_type_index; }
          dataIdx[i] = j;
          values[i] = CpInfo.InterfaceMethodRef;
          j += 5;
          break;

        case NAME_AND_TYPE: // NameAndType_info { u1 tag; u2 name_index<utf8>; u2 descriptor_index<utf8>; }
          dataIdx[i] = j;
          values[i] = CpInfo.NameAndType;

          j += 5;
          break;

        //--- the Java 8 ones
          
        case METHOD_HANDLE: // MethodHandle_info { u1 tag; u1 reference_kind; u2 reference_index<mthref>; }
          dataIdx[i] = j;
          values[i] = CpInfo.MethodHandle;
          j += 4;
          break;
          
        case METHOD_TYPE:  // MethodType_info { u1 tag;  u2 descriptor_index<utf8>; }
          dataIdx[i] = j;
          values[i] = CpInfo.MethodType;
          j += 3;
          break;

        case INVOKE_DYNAMIC: //  InvokeDynamic_info { u1 tag; u2 bootstrap_method_attr_index; u2 name_and_type_index; }
          dataIdx[i] = j;
          values[i] = CpInfo.InvokeDynamic;
          j += 5;
          break;
          
        default:
          error("illegal constpool tag: " + data[j]);
      }
    }

    pos = j;

    //--- second pass: store values of delegating constant values
    for (int i=1; i<cpCount; i++){
      Object v = cpValue[i];

      // we store string and class constants as their utf8 string values
      if (v == CpInfo.ConstantClass || v == CpInfo.ConstantString){
         cpValue[i] = cpValue[u2(cpPos[i]+1)];
      }
    }
  }

  protected void parseInterfaces(ClassFileReader reader, int ifcCount){

    setInterfaceCount(reader, ifcCount);

    for (int i=0; i<ifcCount; i++){
      int cpIdx = readU2();
      setInterface(reader, i, classNameAt(cpIdx));
    }

    setInterfacesDone(reader);
  }

  //--- fields
  protected void parseFields(ClassFileReader reader, int fieldCount) {

    setFieldCount(reader, fieldCount);

    for (int i=0; i<fieldCount; i++){
      int accessFlags = readU2();

      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      cpIdx = readU2();
      String descriptor = utf8At(cpIdx);

      setField(reader, i, accessFlags, name, descriptor);

      int attrCount = readU2();
      parseFieldAttributes(reader, i, attrCount);

      setFieldDone(reader, i);
    }

    setFieldsDone(reader);
  }

  protected void parseFieldAttributes(ClassFileReader reader, int fieldIdx, int attrCount){
    setFieldAttributeCount(reader, fieldIdx, attrCount);

    for (int i=0; i<attrCount; i++){
      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      name = internStdAttrName(cpIdx, name, stdFieldAttrs);

      int attrLength = readI4(); // actually U4, but we don't support 2GB attributes
      setFieldAttribute(reader, fieldIdx, i, name, attrLength);
    }

    setFieldAttributesDone(reader, fieldIdx);
  }

  /**
   * optionally called by reader to obtain a ConstantValue field attribute
   * 
   *   ConstantValue {u2 attrName<utf8>; u4 attrLength; u2 constIndex<class|string|int|float|long|double> }
   * 
   * pos is at constIndex
   */
  public void parseConstValueAttr(ClassFileReader reader, Object tag){
    int cpIdx = readU2();
    setConstantValue(reader, tag, cpValue[cpIdx]);
  }


  //--- methods
  protected void parseMethods(ClassFileReader reader, int methodCount) {

    setMethodCount(reader, methodCount);

    for (int i=0; i<methodCount; i++){
      int accessFlags = readU2();

      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      cpIdx = readU2();
      String descriptor = utf8At(cpIdx);

      setMethod(reader, i, accessFlags, name, descriptor);

      int attrCount = readU2();
      parseMethodAttributes(reader, i, attrCount);

      setMethodDone(reader, i);
    }

    setMethodsDone(reader);
  }

  protected void parseMethodAttributes(ClassFileReader reader, int methodIdx, int attrCount){
    setMethodAttributeCount(reader, methodIdx, attrCount);

    for (int i=0; i<attrCount; i++){
      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      name = internStdAttrName(cpIdx, name, stdMethodAttrs);

      int attrLength = readI4(); // actually U4, but we don't support 2GB attributes
      setMethodAttribute(reader, methodIdx, i, name, attrLength);
    }

    setMethodAttributesDone(reader, methodIdx);
  }

  public void parseExceptionAttr (ClassFileReader reader, Object tag){
    int exceptionCount = readU2();
    setExceptionCount(reader, tag, exceptionCount);

    for (int i=0; i<exceptionCount; i++){
      int cpIdx = readU2();
      String exceptionType = classNameAt(cpIdx);
      setException(reader, tag, i, exceptionType);
    }

    setExceptionsDone(reader, tag);
  }

  /**
   * (optionally) called by reader from within the setMethodAttribute() notification
   * This means we have recursive notification since this is a variable length
   * attribute that has variable length attributes
   *
   * Code_attribute { u2 attr_name_index<utf8>; u4 attr_length;
   *                  u2 max_stack; u2 max_locals;
   *                  u4 code_length; u1 code[code_length];
   *                  u2 exception_table_length;
   *                  { u2 start_pc; u2 end_pc; u2  handler_pc; u2  catch_type<class_entry>;
   *                  }	exception_table[exception_table_length];
   *                  u2 attributes_count;
   *                  attribute_info attributes[attributes_count];  }
   *
   * pos is at max_stack
   */
  public void parseCodeAttr (ClassFileReader reader, Object tag){
    int maxStack = readU2();
    int maxLocals = readU2();
    int codeLength = readI4();  // no code length > 2GB supported
    int codeStartPos = pos;

    setCode(reader, tag, maxStack, maxLocals, codeLength);

    int exceptionCount = readU2();
    setExceptionTableCount(reader, tag, exceptionCount);

    for (int i = 0; i < exceptionCount; i++) {
      int startPc = readU2();
      int endPc = readU2();
      int handlerPc = readU2();

      int cpIdx = readU2();
      String catchType = (String) cpValue[cpIdx]; // a Constant_class

      setExceptionTableEntry(reader, tag, i, startPc, endPc, handlerPc, catchType);
    }
    setExceptionTableDone(reader, tag);

    int attrCount = readU2();
    parseCodeAttrAttributes(reader, tag, attrCount);
  }


  protected void parseCodeAttrAttributes(ClassFileReader reader, Object tag, int attrCount){

    setCodeAttributeCount(reader, tag, attrCount);

    for (int i=0; i<attrCount; i++){
      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      name = internStdAttrName(cpIdx, name, stdCodeAttrs);

      int attrLength = readI4(); // actually U4, but we don't support 2GB attributes
      setCodeAttribute(reader, tag, i, name, attrLength);
    }

    setCodeAttributesDone(reader, tag);
  }

  /**
   * optionally called from ClassFileReader.setCodeAttribute() to parse LineNumberTables
   *   LineNumberTable { u2 attrName; u4 attrLength;
   *                     u2 lineCount;
   *                     { u2 startPc; u2 lineNumber; } [lineCount] };
   * pos is at lineCount
   */
  public void parseLineNumberTableAttr(ClassFileReader reader, Object tag){
    int lineCount = readU2();
    setLineNumberTableCount(reader, tag, lineCount);
    
    for (int i=0; i<lineCount; i++){
      int startPc = readU2();
      int lineNumber = readU2();
      setLineNumber(reader, tag, i, lineNumber, startPc);
    }

    setLineNumberTableDone(reader, tag);
  }

  
  /**
   * optionally called from ClassFileReader.setCodeAttribute() to parse LocalVarTables
   *   LocalVarTableTable { u2 attrName; u4 attrLength;
   *                        u2 localVarCount;
   *                        { u2 startPc; u2 lineNumber; } [lineCount] };
   * pos is at localVarCount
   */
  public void parseLocalVarTableAttr(ClassFileReader reader, Object tag){
    int localVarCount = readU2();
    setLocalVarTableCount(reader, tag, localVarCount);
    
    for (int i=0; i<localVarCount; i++){
      int startPc = readU2();
      int length = readU2();
      int cpIdx = readU2();
      String varName = (String) cpValue[cpIdx];
      cpIdx = readU2();
      String descriptor = (String)  cpValue[cpIdx];
      int slotIndex = readU2();
      
      setLocalVar(reader, tag, i, varName, descriptor, startPc, startPc+length-1, slotIndex );
    }

    setLocalVarTableDone(reader, tag);
  }

  //--- class
  protected void parseClassAttributes(ClassFileReader reader, int attrCount){

    setClassAttributeCount(reader, attrCount);

    for (int i=0; i<attrCount; i++){
      int cpIdx = readU2();
      String name = utf8At(cpIdx);

      name = internStdAttrName(cpIdx, name, stdClassAttrs);

      int attrLength = readI4(); // actually U4, but we don't support 2GB attributes
      setClassAttribute(reader, i, name, attrLength);
    }

    setClassAttributesDone(reader);
  }


  /**
   * (optionally) called by ClassFileReader from within setClassAttribute() notification
   *
   * InnerClass { u2 nameIdx<utf8>; u4 length; u2 sourceFile<utf8>; }
   */
  public void parseSourceFileAttr(ClassFileReader reader, Object tag){
    int cpIdx = readU2();
    String pathName = utf8At(cpIdx);
    setSourceFile(reader, tag, pathName);
  }

  /**
   * (optionally) called by ClassFileReader from within setClassAttribute() notification
   *
   * InnerClass { 
   *    u2 nameIdx<utf8>; 
   *    u4 length;
   *    u2 classCount;
   *    { u2 innerCls<cls>;
   *      u2 outerCls<cls>;
   *      u2 innerName<utf8>; 
   *      u2 innerAccessFlags;
   *    } classes[classCount] }
   * }
   * 
   * pos is at classCount
   */
  public void parseInnerClassesAttr(ClassFileReader reader, Object tag){
    int innerClsCount = readU2();    
    setInnerClassCount(reader, tag, innerClsCount);

    for (int i = 0; i < innerClsCount; i++) {
      int cpIdx = readU2();
      String innerClsName = (cpIdx != 0) ? (String) cpValue[cpIdx] : null;
      cpIdx = readU2();
      String outerClsName = (cpIdx != 0) ? (String) cpValue[cpIdx] : null;
      cpIdx = readU2();
      String innerSimpleName = (cpIdx != 0) ? (String) cpValue[cpIdx] : null;
      int accessFlags = readU2();

      setInnerClass(reader, tag, i, outerClsName, innerClsName, innerSimpleName, accessFlags);
    }

    setInnerClassesDone(reader, tag);
  }
  
  /**
   * EnclosingMethod_attribute {
   *   u2 attribute_name_index;
   *   u4 attribute_length;
   *   u2 class_index     -> Class_info { u1 tag; u2 name_index->utf8 }
   *   u2 method_index    -> NameAndType_info { u1 tag; u2 name_index->utf8; u2 descriptor_index->utf8 }
   * }
   */
  public void parseEnclosingMethodAttr(ClassFileReader reader, Object tag){
    String enclosedMethod = null;
    String descriptor = null;
    
    int cpIdx = readU2(); // start of Class_info
    String enclosingClass =  nameAt(cpIdx);
    
    cpIdx = readU2(); // start of NameAndType_info
    
    // check if this is inside a method - we also get EnclosingMethod_infos for
    // classes that are not immediately enclosed
    if (cpIdx != 0){
      enclosedMethod = nameAt(cpIdx);    
      descriptor = descriptorAt(cpIdx);
    }
    
    setEnclosingMethod(reader, tag, enclosingClass, enclosedMethod, descriptor);
  }
  
  /**
   * BootstrapMethods_attribute {
   *     u2 attribute_name_index;
   *     u4 attribute_length;
   *     u2 num_bootstrap_methods;
   *     {   u2 bootstrap_method_ref; -> MethodHandle
   *         u2 num_bootstrap_arguments;
   *         u2 bootstrap_arguments[num_bootstrap_arguments];
   *     } bootstrap_methods[num_bootstrap_methods];
   * }
   * 
   * pos is at num_bootstrap_methods
  */
  public void parseBootstrapMethodAttr (ClassFileReader reader, Object tag){
    int nBootstrapMethods = readU2();
    
    setBootstrapMethodCount(reader, tag, nBootstrapMethods);
    
    for (int i=0; i<nBootstrapMethods; i++){
      int cpMhIdx = readU2();
      int nArgs = readU2();
      int[] bmArgs = new int[nArgs];
      for (int j=0; j<nArgs; j++){
        bmArgs[j] = readU2();
      }
      
      // kind of this method handle
      int refKind = mhRefTypeAt(cpMhIdx);
      
      // CONSTANT_Methodref_info structure
      int mrefIdx = mhMethodRefIndexAt(cpMhIdx);
      
      String clsName = methodClassNameAt(mrefIdx);
      String mthName = methodNameAt(mrefIdx);
      String descriptor = methodDescriptorAt(mrefIdx);
      
      setBootstrapMethod(reader, tag, i, refKind, clsName, mthName, descriptor, bmArgs);
    }
    
    setBootstrapMethodsDone( reader, tag);
  }
  
  String nameAt(int nameTypeInfoIdx) {
    return utf8At(u2(cpPos[nameTypeInfoIdx] + 1));
  }
  
  String descriptorAt (int nameTypeInfoIdx){
    return utf8At( u2( cpPos[nameTypeInfoIdx]+3));
  }

// those are as per http://java.sun.com/docs/books/jvms/second_edition/ClassFileFormat-Java5.pdf

  /*
   *   element_value {
   *     u1 tag;
   *     union {
   *       u2 const_value_index;
   *       { u2 type_name_index; u2 const_name_index; } enum_const_value;
   *       u2 class_info_index;
   *       annotation annotation_value;
   *       { u2 num_values; element_value values[num_values]; } array_value;
   *     } value;
   *   }
   *   valid tags are primitve type codes B,C,D,F,I,J,S,Z
   *   plus:   's'=String, 'e'=enum, 'c'=class, '@'=annotation, '['=array
   */
  void parseAnnotationValue(ClassFileReader reader, Object tag, int annotationIndex, int valueIndex, String elementName, int arrayIndex){
    int cpIdx;
    Object val;

    int t = readUByte();
    switch (t){
      case 'Z':
        // booleans have to be treated differently since there is no CONSTANT_Boolean, i.e. values are
        // stored as CONSTANT_Integer in the constpool, i.e. the cpValue doesn't have the right type
        cpIdx = readU2();
        val = cpValue[cpIdx];
        val = Boolean.valueOf((Integer)val == 1);
        setPrimitiveAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
        break;        

      case 'B':
        cpIdx = readU2();
        val = cpValue[cpIdx];
        val = Byte.valueOf(((Integer)val).byteValue());
        setPrimitiveAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
        break;
        
      case 'C':
        cpIdx = readU2();
        val = cpValue[cpIdx];
        val = Character.valueOf((char)((Integer)val).shortValue());
        setPrimitiveAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
        break;
        
      case 'S':
        cpIdx = readU2();
        val = cpValue[cpIdx];
        val = Short.valueOf(((Integer)val).shortValue());
        setPrimitiveAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
        break;

      case 'I':
      case 'F':
      case 'D':
      case 'J':
        cpIdx = readU2();
        val = cpValue[cpIdx];
        setPrimitiveAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, val);
        break;

      case 's':
        cpIdx = readU2();
        String s = (String) cpValue[cpIdx];
        setStringAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, s);
        break;

      case 'e':
        cpIdx = readU2();
        String enumTypeName = (String)cpValue[cpIdx];
        cpIdx = readU2();
        String enumConstName = (String)cpValue[cpIdx];
        setEnumAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, enumTypeName, enumConstName);
        break;

      case 'c':
        cpIdx = readU2();
        String className = (String)cpValue[cpIdx];
        setClassAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex, className);
        break;

      case '@':
        parseAnnotation(reader, tag, -1, false);  // getting recursive here
        setAnnotationFieldValue(reader, tag, annotationIndex, valueIndex, elementName, arrayIndex);
        break;

      case '[':
        int arrayLen = readU2();
        setAnnotationValueElementCount(reader, tag, annotationIndex, valueIndex, elementName, arrayLen);
        for (int i=0; i<arrayLen; i++){
          parseAnnotationValue(reader, tag, annotationIndex, valueIndex, elementName, i);
        }
        setAnnotationValueElementsDone(reader, tag, annotationIndex, valueIndex, elementName);
        break;
    }
  }

  /*
   *   annotation {
   *     u2 type_index;
   *     u2 num_element_value_pairs;
   *     {
   *       u2 element_name_index;
   *       element_value value;
   *     } element_value_pairs[num_element_value_pairs]
   *   }
   */
  void parseAnnotation (ClassFileReader reader, Object tag, int annotationIndex, boolean isParameterAnnotation){
    int cpIdx = readU2();
    String annotationType = (String)cpValue[cpIdx];
    boolean parseValues;
    if (isParameterAnnotation){
      parseValues = setParameterAnnotation(reader, tag, annotationIndex, annotationType);
    } else {
      parseValues = setAnnotation(reader, tag, annotationIndex, annotationType);
    }
    if(parseValues) {
      parseAnnotationValues(reader, tag, annotationIndex);
    }
  }

  void parseAnnotationValues (ClassFileReader reader, Object tag, int annotationIndex){
    int nValuePairs = readU2();
    setAnnotationValueCount(reader, tag, annotationIndex, nValuePairs);

    for (int i=0; i<nValuePairs; i++){
      int cpIdx = readU2();
      String elementName = (String)cpValue[cpIdx];
      parseAnnotationValue(reader, tag, annotationIndex, i, elementName, -1);
    }

    setAnnotationValuesDone(reader, tag, annotationIndex);
  }
  
  /*
   * class, field, method annotation attributes (only one per target)
   *
   *  Runtime[In]VisibleAnnotations_attribute {
   *     u2 attribute_name_index;
   *     u4 attribute_length;
   *     u2 num_annotations;        << pos
   *     annotation annotations[num_annotations];
   *   }
   */
  public void parseAnnotationsAttr (ClassFileReader reader, Object tag){
    int numAnnotations = readU2();
    setAnnotationCount(reader, tag, numAnnotations);

    for (int i=0; i<numAnnotations; i++){
      parseAnnotation(reader, tag, i, false);
    }

    setAnnotationsDone(reader, tag);
  }

  
  // JSR 308 type annotation target types
  public static final int CLASS_TYPE_PARAMETER                 = 0x00;
  public static final int METHOD_TYPE_PARAMETER                = 0x01;
  public static final int CLASS_EXTENDS                        = 0x10;
  public static final int CLASS_TYPE_PARAMETER_BOUND           = 0x11;
  public static final int METHOD_TYPE_PARAMETER_BOUND          = 0x12;
  public static final int FIELD                                = 0x13;
  public static final int METHOD_RETURN                        = 0x14;
  public static final int METHOD_RECEIVER                      = 0x15;
  public static final int METHOD_FORMAL_PARAMETER              = 0x16;
  public static final int THROWS                               = 0x17;
  public static final int LOCAL_VARIABLE                       = 0x40;
  public static final int RESOURCE_VARIABLE                    = 0x41;
  public static final int EXCEPTION_PARAMETER                  = 0x42;
  public static final int INSTANCEOF                           = 0x43;
  public static final int NEW                                  = 0x44;
  public static final int CONSTRUCTOR_REFERENCE                = 0x45;
  public static final int METHOD_REFERENCE                     = 0x46;
  public static final int CAST                                 = 0x47;
  public static final int CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT = 0x48;
  public static final int METHOD_INVOCATION_TYPE_ARGUMENT      = 0x49;
  public static final int CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT  = 0x4a;
  public static final int METHOD_REFERENCE_TYPE_ARGUMENT       = 0x4b;  
  
  public static String getTargetTypeName (int targetType){
    switch (targetType){
      case CLASS_TYPE_PARAMETER: return "class type parameter";
      case METHOD_TYPE_PARAMETER: return "method type parameter";
      case CLASS_EXTENDS: return "super class";
      case CLASS_TYPE_PARAMETER_BOUND: return "class type parameter bound";
      case METHOD_TYPE_PARAMETER_BOUND: return "method type parameter bound";
      case FIELD: return "field";
      case METHOD_RETURN: return "method return";
      case METHOD_RECEIVER: return "method receiver";
      case METHOD_FORMAL_PARAMETER: return "method formal parameter";
      case THROWS: return "throws";
      case LOCAL_VARIABLE: return "local variable";
      case RESOURCE_VARIABLE: return "resource variable";
      case EXCEPTION_PARAMETER: return "exception parameter";
      case INSTANCEOF: return "instanceof";
      case NEW: return "new";
      case CONSTRUCTOR_REFERENCE: return "ctor reference";
      case METHOD_REFERENCE: return "method reference";
      case CAST: return "case";
      case METHOD_INVOCATION_TYPE_ARGUMENT: return "method invocation type argument";
      case CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT: return "ctor reference type argument";
      case METHOD_REFERENCE_TYPE_ARGUMENT: return "method reference type argument";
      default:
        return "<unknown target type 0x" + Integer.toHexString(targetType);
    }
  }
  
  public static String getTypePathEncoding (short[] typePath){
    if (typePath == null){
      return "()";
    }
    
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<typePath.length;i++){
      int e = typePath[i];
      sb.append('(');
      sb.append( Integer.toString((e>>8) & 0xff));
      sb.append( Integer.toString(e & 0xff));
      sb.append(')');
    }
    
    return sb.toString();
  }
  
  public static String getScopeEncoding (long[] scopeEntries){
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<scopeEntries.length;i++){
      long e = scopeEntries[i];
      int slotIndex = (int)(e & 0xffff);
      int length = (int)((e >> 16) & 0xffff);
      int startPc = (int)((e >> 32) & 0xffff);
      
      if (i>0){
        sb.append(',');
      }
      
      sb.append('[');
      sb.append( Integer.toString(startPc));
      sb.append("..");
      sb.append( Integer.toString(startPc + length-1));
      sb.append("]#");
      sb.append(slotIndex);
    }
    
    return sb.toString();
  }
  
  // JSR 308 type annotation, which adds 3 fields to the old annotation structure
  //
  //  type_annotation {
  //      u1 target_type;        // targeted program element (sec 3.2)
  //      union {                // ?? this is probably packed - variable size unions make no sense
  //          type_parameter_target;
  //          supertype_target;
  //          type_parameter_bound_target;
  //          empty_target;
  //          method_formal_parameter_target;
  //          throws_target;
  //          localvar_target;
  //          catch_target;
  //          offset_target;
  //          type_argument_target;
  //      } target_info;         // targeted program element (sec 3.3)
  //
  //      type_path target_path; // encoding of annotation position in compound type (array, generic, etc., sec 3.4)
  //
  //                             // standard annotation fields
  //      u2 type_index;         // the annotation type
  //      u2 num_element_value_pairs;
  //      {
  //          u2 element_name_index;
  //          element_value value;
  //      } element_value_pairs[num_element_value_pairs];
  //  }
  //
  //  struct type_path {
  //    u1              path_length;
  //    type_path_entry path[path_length];
  //  }
  //
  //  struct type_path_entry {
  //    u1 type_path_kind;
  //        // 0: deeper in array type
  //        // 1: deeper in nested type
  //        // 2: bound of wildcard typearg
  //        // 3: type argument of parameterized type
  //    u1 type_argument_index;
  //        // 0, if type_path_kind == 0,1,2
  //        // 0-based index of type arg in parameterized type if type_path_kind i== 3
  //  }
  
  int getTargetInfoSize (int targetType){
    int len = 3; // max static length are xx_TYPE_ARGUMENTs
    if (targetType == LOCAL_VARIABLE || targetType == RESOURCE_VARIABLE){
      len = Math.max( len, u2(pos) * 6); // three u2 values per entry
    }
    
    return len;
  }

  int getTypePathSize (short[] typePath){
    int typePathSize = 1;
    if (typePath != null) {
      typePathSize += typePath.length * 2;
    }
    return typePathSize;
  }
  
  
  short[] readTypePath (){
    short[] typePath = null;
    
    int pathLength = readUByte();
    if (pathLength > 0){
      typePath = new short[pathLength];
      for (int i=0; i<pathLength; i++){
        int pathKind = (short)readUByte();
        int argIdx = (short)readUByte();
        typePath[i]= (short)((pathKind << 8) | argIdx);
      }
    }
    
    return typePath;
  }

  String readAnnotationType (){
    int cpIdx = readU2();
    String annotationType = (String)cpValue[cpIdx];
    return annotationType;
  }

  void setTypeAnnotation (ClassFileReader reader, Object tag, int annotationIndex) {
    int targetType = readUByte();
    
    switch (targetType){
      case CLASS_TYPE_PARAMETER:
      case METHOD_TYPE_PARAMETER: {
        // type_parameter_target { u1 type_parameter_index; }
        int typeParamIdx = readUByte();
        reader.setTypeParameterAnnotation( this, tag, annotationIndex, targetType, typeParamIdx, readTypePath(), readAnnotationType());
        break;
      } 
      case CLASS_EXTENDS: {
        // supertype_target { u2 supertype_index; }
        int superTypeIdx = readU2();
        reader.setSuperTypeAnnotation( this, tag, annotationIndex, targetType, superTypeIdx, readTypePath(), readAnnotationType());
        break;
      }
      case CLASS_TYPE_PARAMETER_BOUND:
      case METHOD_TYPE_PARAMETER_BOUND: {
        // type_parameter_bound_target { u1 type_parameter_index; u1 bound_index; }
        int typeParamIdx = readUByte();
        int boundIdx = readUByte();
        reader.setTypeParameterBoundAnnotation(this, tag, annotationIndex, targetType, typeParamIdx, boundIdx, readTypePath(), readAnnotationType());
        break;
      }
      case METHOD_RETURN:
      case METHOD_RECEIVER:
      case FIELD:
        // empty_target {}
        reader.setTypeAnnotation( this, tag, annotationIndex, targetType, readTypePath(), readAnnotationType());
        break;
        
      case METHOD_FORMAL_PARAMETER: {
        // method_formal_parameter_target { u1 method_formal_parameter_index; }
        int formalParamIdx = readUByte();
        reader.setFormalParameterAnnotation( this, tag, annotationIndex, targetType, formalParamIdx, readTypePath(), readAnnotationType());
        break;
      }
      case THROWS: {
        // throws_target { u2 throws_type_index; }
        int throwsTypeIdx = readU2();
        reader.setThrowsAnnotation( this, tag, annotationIndex, targetType, throwsTypeIdx, readTypePath(), readAnnotationType());        
        break;
      } 
      case LOCAL_VARIABLE:
      case RESOURCE_VARIABLE: {
        // this can't just refer to a LocalVarInfo since those depend on debug compile options
        //
        //  localvar_target {
        //      u2 table_length;  // number of entries, not bytes
        //      {
        //          u2 start_pc;
        //          u2 length; // bytecode offset length
        //          u2 index;  // local var idx
        //      } table[table_length];
        //  }
        int tableLength = readU2();
        long[] scopeEntries = new long[tableLength];
        for (int i=0; i<tableLength; i++){
          int startPc = readU2();
          int length = readU2();
          int slotIdx = readU2();
          scopeEntries[i] = ((long)startPc << 32) | ((long)length << 16) | slotIdx;
        }
        reader.setVariableAnnotation( this, tag, annotationIndex, targetType, scopeEntries, readTypePath(), readAnnotationType());
        break;
      }
      case EXCEPTION_PARAMETER: {
        // catch_target { u2 exception_table_index; }
        int exceptionIdx = readU2();
        reader.setExceptionParameterAnnotation( this, tag, annotationIndex, targetType, exceptionIdx, readTypePath(), readAnnotationType());        
        break;
      }
      case INSTANCEOF:
      case METHOD_REFERENCE:
      case CONSTRUCTOR_REFERENCE:
      case NEW: {
        // offset_target { u2 offset; }   // insn offset within bytecode
        int offset = readU2();
        reader.setBytecodeAnnotation(this, tag, annotationIndex, targetType, offset, readTypePath(), readAnnotationType());
        break;
      }
      case CAST:
      case CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
      case METHOD_INVOCATION_TYPE_ARGUMENT:
      case CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
      case METHOD_REFERENCE_TYPE_ARGUMENT: {
        //  type_argument_target {
        //      u2 offset;
        //      u1 type_argument_index;
        //  }
        int offset = readU2();
        int typeArgIdx = readUByte();
        reader.setBytecodeTypeParameterAnnotation(this, tag, annotationIndex, targetType, offset, typeArgIdx, readTypePath(), readAnnotationType());
        break;
      }
      
      default:
        // <2do - report this to the reader
        throw new RuntimeException("unknown type annotation target: 0x" + Integer.toHexString(targetType));
    }
  }

  
  void parseTypeAnnotation (ClassFileReader reader, Object tag, int annotationIndex) {
   
    // this does the respective setXTypeAnnotation() reader callback
    //dumpData(pos, 16);
    setTypeAnnotation(reader, tag, annotationIndex);
    
    // now set the annotation value pairs
    parseAnnotationValues( reader, tag, annotationIndex);
  }
  
  /*
   * Runtime[In]VisibleTypeAnnotations_attribute {
   *    u2 attribute_name_index;
   *    u4 attribute_length;
   *    u2 num_annotations;
   *    type_annotation annotations[num_annotations];
   * }
   */
  public void parseTypeAnnotationsAttr (ClassFileReader reader, Object tag) {
    int numAnnotations = readU2();
    setTypeAnnotationCount(reader, tag, numAnnotations);

    for (int i=0; i<numAnnotations; i++){
      parseTypeAnnotation(reader, tag, i);
    }

    setTypeAnnotationsDone(reader, tag);
  }
  
  /*
   *   RuntimeInvisibleParameterAnnotations_attribute {
   *     u2 attribute_name_index;
   *     u4 attribute_length;
   *     u1 num_parameters; << pos
   *     {
   *       u2 num_annotations;
   *       annotation annotations[num_annotations];
   *     } parameter_annotations[num_parameters];
   *   }
   */
   public void parseParameterAnnotationsAttr(ClassFileReader reader, Object tag){
     int numParameters = readUByte();
     setParameterCount(reader, tag, numParameters);
     for (int i=0; i<numParameters; i++){
       int numAnnotations = readU2();

       setParameterAnnotationCount(reader, tag, i, numAnnotations);
       for (int j=0; j<numAnnotations; j++){
         parseAnnotation(reader, tag, j, true);
       }
       setParameterAnnotationsDone(reader, tag, i);
     }
     setParametersDone(reader, tag);
   }

  /**
   *  Signature_attribute {
   *    u2 attribute_name_index;
   *    u4 attr-length;
   *    u2 signature-index << pos
   *  }
   */
   public void parseSignatureAttr(ClassFileReader reader, Object tag){
     int cpIdx = readU2();
     setSignature(reader, tag, utf8At(cpIdx));
   }


  /**
   *    AnnotationDefault_attribute {
   *      u2 attribute_name_index;
   *      u4 attribute_length;
   *      element_value default_value; << pos
   *    }
   */
   public void parseAnnotationDefaultAttr(ClassFileReader reader, Object tag){
     parseAnnotationValue(reader, tag, -1, -1, null, -1);
   }


//   EnclosingMethod_attribute {
//     u2 attribute_name_index;
//     u4 attribute_length;
//     u2 class_index
//     u2 method_index;
//   }

//   LocalVariableTypeTable_attribute {  // Code attr
//     u2 attribute_name_index;
//     u4 attribute_length;
//     u2 local_variable_type_table_length;
//     {
//       u2 start_pc;
//       u2 length;
//       u2 name_index;
//       u2 signature_index;
//       u2 index;
//     } local_variable_type_table[local_variable_type_table_length];
//   }




  public void parseBytecode(JVMByteCodeReader reader, Object tag, int codeLength){
    int localVarIndex;
    int cpIdx;
    int constVal;
    int offset;
    int defaultOffset;

    boolean isWide = false; // modifier for Xload,Xstore,ret and iinc

    int startPos = pos;
    int endPos = pos+codeLength;
    int nextPos;


    while (pos < endPos){
      pc = pos - startPos;

      int opcode = readUByte();
      switch (opcode){
        case 0: // nop
          reader.nop();
          break;
        case 1:  // aconst_null
          reader.aconst_null();
          break;
        case 2: // iconst_m1
          reader.iconst_m1();
          break;
        case 3: // iconst_0
          reader.iconst_0();
          break;
        case 4: // iconst_1
          reader.iconst_1();
          break;
        case 5: // iconst_2
          reader.iconst_2();
          break;
        case 6: // iconst_3
          reader.iconst_3();
          break;
        case 7: // iconst_4
          reader.iconst_4();
          break;
        case 8: // iconst_5
          reader.iconst_5();
          break;
        case 9: // lconst_0
          reader.lconst_0();
          break;
        case 10: // lconst_1
          reader.lconst_1();
          break;
        case 11: // fconst_0
          reader.fconst_0();
          break;
        case 12: // fconst_1
          reader.fconst_1();
          break;
        case 13: // fconst_2
          reader.fconst_2();
          break;
        case 14: // dconst_0
          reader.dconst_0();
          break;
        case 15: // dconst_1
          reader.dconst_1();
          break;
        case 16: // bipush
          constVal = readByte();
          reader.bipush(constVal);
          break;
        case 17: // sipush
          constVal = readI2();
          reader.sipush(constVal);
          break;
        case 18: // ldc
          cpIdx = readUByte();
          reader.ldc_(cpIdx);
          break;
        case 19: // ldc_w
          cpIdx = readU2();
          reader.ldc_w_(cpIdx);
          break;
        case 20: // ldc2_w
          cpIdx = readU2();
          reader.ldc2_w(cpIdx);
          break;
        case 21: // iload
          localVarIndex = isWide ? readU2() : readUByte();
          reader.iload(localVarIndex);
          break;
        case 22: // lload
          localVarIndex = isWide ? readU2() : readUByte();
          reader.lload(localVarIndex);
          break;
        case 23: // fload
          localVarIndex = isWide ? readU2() : readUByte();
          reader.fload(localVarIndex);
          break;
        case 24: // dload
          localVarIndex = isWide ? readU2() : readUByte();
          reader.dload(localVarIndex);
          break;
        case 25: // aload
          localVarIndex = isWide ? readU2() : readUByte();
          reader.aload(localVarIndex);
          break;
        case 26: // iload_0
          reader.iload_0();
          break;
        case 27: // iload_1
          reader.iload_1();
          break;
        case 28: // iload_2
          reader.iload_2();
          break;
        case 29: // iload_3
          reader.iload_3();
          break;
        case 30: // lload_0
          reader.lload_0();
          break;
        case 31: // lload_1
          reader.lload_1();
          break;
        case 32: // lload_2
          reader.lload_2();
          break;
        case 33: // lload_3
          reader.lload_3();
          break;
        case 34: // fload_0
          reader.fload_0();
          break;
        case 35: // fload_1
          reader.fload_1();
          break;
        case 36: // fload_2
          reader.fload_2();
          break;
        case 37: // fload_3
          reader.fload_3();
          break;
        case 38: // dload_0
          reader.dload_0();
          break;
        case 39: // dload_1
          reader.dload_1();
          break;
        case 40: // dload_2
          reader.dload_2();
          break;
        case 41: // dload_3
          reader.dload_3();
          break;
        case 42: // aload_0
          reader.aload_0();
          break;
        case 43: // aload_1
          reader.aload_1();
          break;
        case 44: // aload_2
          reader.aload_2();
          break;
        case 45: // aload_3
          reader.aload_3();
          break;
        case 46: // iaload
          reader.iaload();
          break;
        case 47: // laload
          reader.laload();
          break;
        case 48: // faload
          reader.faload();
          break;
        case 49: // daload
          reader.daload();
          break;
        case 50: // aaload
          reader.aaload();
          break;
        case 51: // baload
          reader.baload();
          break;
        case 52: // caload
          reader.caload();
          break;
        case 53: // saload
          reader.saload();
          break;
        case 54: // istore
          localVarIndex = isWide ? readU2() : readUByte();
          reader.istore(localVarIndex);
          break;
        case 55: // lstore
          localVarIndex = isWide ? readU2() : readUByte();
          reader.lstore(localVarIndex);
          break;
        case 56: // fstore
          localVarIndex = isWide ? readU2() : readUByte();
          reader.fstore(localVarIndex);
          break;
        case 57: // dstore
          localVarIndex = isWide ? readU2() : readUByte();
          reader.dstore(localVarIndex);
          break;
        case 58: // astore
          localVarIndex = isWide ? readU2() : readUByte();
          reader.astore(localVarIndex);
          break;
        case 59: // istore_0
          reader.istore_0();
          break;
        case 60: // istore_1
          reader.istore_1();
          break;
        case 61: // istore_2
          reader.istore_2();
          break;
        case 62: // istore_3
          reader.istore_3();
          break;
        case 63: // lstore_0
          reader.lstore_0();
          break;
        case 64: // lstore_1
          reader.lstore_1();
          break;
        case 65: // lstore_2
          reader.lstore_2();
          break;
        case 66: // lstore_3
          reader.lstore_3();
          break;
        case 67: // fstore_0
          reader.fstore_0();
          break;
        case 68: // fstore_1
          reader.fstore_1();
          break;
        case 69: // fstore_2
          reader.fstore_2();
          break;
        case 70: // fstore_3
          reader.fstore_3();
          break;
        case 71: //dstore_0
          reader.dstore_0();
          break;
        case 72: //dstore_1
          reader.dstore_1();
          break;
        case 73: //dstore_2
          reader.dstore_2();
          break;
        case 74: //dstore_3
          reader.dstore_3();
          break;
        case 75: // astore_0
          reader.astore_0();
          break;
        case 76: // astore_1
          reader.astore_1();
          break;
        case 77: // astore_2
          reader.astore_2();
          break;
        case 78: // astore_3
          reader.astore_3();
          break;
        case 79: // iastore
          reader.iastore();
          break;
        case 80: // lastore
          reader.lastore();
          break;
        case 81: // fastore
          reader.fastore();
          break;
        case 82: // dastore
          reader.dastore();
          break;
        case 83: // aastore
          reader.aastore();
          break;
        case 84: // bastore
          reader.bastore();
          break;
        case 85: // castore
          reader.castore();
          break;
        case 86: // sastore
          reader.sastore();
          break;
        case 87: // pop
          reader.pop();
          break;
        case 88: // pop2
          reader.pop2();
          break;
        case 89: // dup
          reader.dup();
          break;
        case 90: // dup_x1
          reader.dup_x1();
          break;
        case 91: // dup_x2
          reader.dup_x2();
          break;
        case 92: // dup2
          reader.dup2();
          break;
        case 93: // dup2_x1
          reader.dup2_x1();
          break;
        case 94: // dup2_x2
          reader.dup2_x2();
          break;
        case 95: // swap
          reader.swap();
          break;
        case 96: // iadd
          reader.iadd();
          break;
        case 97: // ladd
          reader.ladd();
          break;
        case 98: // fadd
          reader.fadd();
          break;
        case 99: // dadd
          reader.dadd();
          break;
        case 100: // isub
          reader.isub();
          break;
        case 101: // lsub
          reader.lsub();
          break;
        case 102: // fsub
          reader.fsub();
          break;
        case 103: // dsub
          reader.dsub();
          break;
        case 104: // imul
          reader.imul();
          break;
        case 105: // lmul
          reader.lmul();
          break;
        case 106: // fmul
          reader.fmul();
          break;
        case 107: // dmul
          reader.dmul();
          break;
        case 108: // idiv
          reader.idiv();
          break;
        case 109: // ldiv
          reader.ldiv();
          break;
        case 110: // fdiv
          reader.fdiv();
          break;
        case 111: //ddiv
          reader.ddiv();
          break;
        case 112: // irem
          reader.irem();
          break;
        case 113: // lrem
          reader.lrem();
          break;
        case 114: // frem
          reader.frem();
          break;
        case 115: // drem
          reader.drem();
          break;
        case 116: // ineg
          reader.ineg();
          break;
        case 117: // lneg
          reader.lneg();
          break;
        case 118: // fneg
          reader.fneg();
          break;
        case 119: // dneg
          reader.dneg();
          break;
        case 120: // ishl
          reader.ishl();
          break;
        case 121: // lshl
          reader.lshl();
          break;
        case 122: // ishr
          reader.ishr();
          break;
        case 123: // lshr
          reader.lshr();
          break;
        case 124: // iushr
          reader.iushr();
          break;
        case 125: // lushr
          reader.lushr();
          break;
        case 126: // iand
          reader.iand();
          break;
        case 127: // land
          reader.land();
          break;
        case 128: // ior
          reader.ior();
          break;
        case 129: // lor
          reader.lor();
          break;
        case 130: // ixor
          reader.ixor();
          break;
        case 131: // lxor
          reader.lxor();
          break;
        case 132: // iinc
          if (isWide){
            localVarIndex = readU2();
            constVal = readI2();
          } else {
            localVarIndex = readUByte();
            constVal = readByte();
          }
          reader.iinc(localVarIndex, constVal);
          break;
        case 133: // i2l
          reader.i2l();
          break;
        case 134: // i2f
          reader.i2f();
          break;
        case 135: // i2d
          reader.i2d();
          break;
        case 136: // l2i
          reader.l2i();
          break;
        case 137: // l2f
          reader.l2f();
          break;
        case 138: // l2d
          reader.l2d();
          break;
        case 139: // f2i
          reader.f2i();
          break;
        case 140: // f2l
          reader.f2l();
          break;
        case 141: // f2d
          reader.f2d();
          break;
        case 142: // d2i
          reader.d2i();
          break;
        case 143: // d2l
          reader.d2l();
          break;
        case 144: // d2f
          reader.d2f();
          break;
        case 145: // i2b
          reader.i2b();
          break;
        case 146: // i2c
          reader.i2c();
          break;
        case 147: // i2s
          reader.i2s();
          break;
        case 148: // lcmp
          reader.lcmp();
          break;
        case 149: // fcmpl
          reader.fcmpl();
          break;
        case 150: // fcmpg
          reader.fcmpg();
          break;
        case 151: // dcmpl
          reader.dcmpl();
          break;
        case 152: // dcmpg
          reader.dcmpg();
          break;
        case 153: // ifeq
          offset = readI2();
          reader.ifeq(offset);
          break;
        case 154: // ifne
          offset = readI2();
          reader.ifne(offset);
          break;
        case 155: // iflt
          offset = readI2();
          reader.iflt(offset);
          break;
        case 156: // ifge
          offset = readI2();
          reader.ifge(offset);
          break;
        case 157: // ifgt
          offset = readI2();
          reader.ifgt(offset);
          break;
        case 158: // ifle
          offset = readI2();
          reader.ifle(offset);
          break;
        case 159: // if_icmpeq
          offset = readI2();
          reader.if_icmpeq(offset);
          break;
        case 160: // if_icmpne
          offset = readI2();
          reader.if_icmpne(offset);
          break;
        case 161: // if_icmplt
          offset = readI2();
          reader.if_icmplt(offset);
          break;
        case 162: // if_icmpge
          offset = readI2();
          reader.if_icmpge(offset);
          break;
        case 163: // if_icmpgt
          offset = readI2();
          reader.if_icmpgt(offset);
          break;
        case 164: // if_icmple
          offset = readI2();
          reader.if_icmple(offset);
          break;
        case 165: // if_acmpeq
          offset = readI2();
          reader.if_acmpeq(offset);
          break;
        case 166: // if_acmpne
          offset = readI2();
          reader.if_acmpne(offset);
          break;
        case 167: // goto
          offset = readI2();
          reader.goto_(offset);
          break;
        case 168: // jsr
          offset = readI2();
          reader.jsr(offset);
          break;
        case 169: // ret
          localVarIndex = isWide ? readU2() : readUByte();
          reader.ret(localVarIndex);
          break;
        case 170: // tableswitch
          pos = (((pc+4)>>2)<<2)+startPos; // skip over padding

          defaultOffset = readI4();
          int low = readI4();
          int high = readI4();

          int len = high-low+1;
          nextPos = pos + len*4;
          reader.tableswitch(defaultOffset, low, high);
          pos = nextPos;
          break;
        case 171: // lookupswitch
          pos = (((pc+4)>>2)<<2)+startPos; // skip over padding

          defaultOffset = readI4();
          int nPairs = readI4();

          nextPos = pos + (nPairs*8);
          reader.lookupswitch(defaultOffset, nPairs);
          pos = nextPos;
          break;
        case 172: // ireturn
          reader.ireturn();
          break;
        case 173: // lreturn
          reader.lreturn();
          break;
        case 174: // freturn
          reader.freturn();
          break;
        case 175: // dreturn
          reader.dreturn();
          break;
        case 176: // areturn
          reader.areturn();
          break;
        case 177: // return
          reader.return_();
          break;
        case 178: // getstatic
          cpIdx = readU2(); // CP index of fieldRef
          reader.getstatic(cpIdx);
          break;
        case 179: // putstatic
          cpIdx = readU2(); // CP index of fieldRef
          reader.putstatic(cpIdx);
          break;
        case 180: // getfield
          cpIdx = readU2(); // CP index of fieldRef
          reader.getfield(cpIdx);
          break;
        case 181: // putfield
          cpIdx = readU2(); // CP index of fieldRef
          reader.putfield(cpIdx);
          break;
        case 182: // invokevirtual
          cpIdx = readU2(); // CP index of methodRef
          reader.invokevirtual(cpIdx);
          break;
        case 183: // invokespecial
          cpIdx = readU2(); // CP index of methodRef
          reader.invokespecial(cpIdx);
          break;
        case 184: // invokestatic
          cpIdx = readU2(); // CP index of methodRef
          reader.invokestatic(cpIdx);
          break;
        case 185: // invokeinterface
          cpIdx = readU2(); // CP index of methodRef
          int count = readUByte();
          int zero = readUByte(); // must be 0
          reader.invokeinterface(cpIdx, count, zero);
          break;
        case 186: // invokedynamic
          cpIdx = readU2(); // CP index of bootstrap method
          readUByte();  // 0
          readUByte(); //  0
          reader.invokedynamic(cpIdx);
          break;
        case 187: // new
          cpIdx = readU2();
          reader.new_(cpIdx);
          break;
        case 188: // newarray
          int aType = readUByte();
          reader.newarray(aType);
          break;
        case 189: // anewarray
          cpIdx = readU2(); // CP index of component type
          reader.anewarray(cpIdx);
          break;
        case 190: // arraylength
          reader.arraylength();
          break;
        case 191: // athrow
          reader.athrow();
          break;
        case 192: // checkcast
          cpIdx = readU2(); // cast type cp index
          reader.checkcast(cpIdx);
          break;
        case 193: // instanceof
          cpIdx = readU2(); // check type cp index
          reader.instanceof_(cpIdx);
          break;
        case 194: // monitorenter
          reader.monitorenter();
          break;
        case 195: // monitorexit
          reader.monitorexit();
          break;
        case 196: // wide
          isWide = true;
          // affects immediate operand width if next bytecode is:
          //  iload,fload,aload,lload,dload,
          //  istore,fstore,astore,lstore,dstore
          //  ret
          reader.wide();
          continue;
        case 197: // multianewarray
          cpIdx = readU2();
          int dimensions = readUByte();
          reader.multianewarray(cpIdx, dimensions);
          break;
        case 198: // ifnull
          offset = readI2();
          reader.ifnull(offset);
          break;
        case 199: // ifnonnull
          offset = readI2();
          reader.ifnonnull(offset);
          break;
        case 200: // goto_w
          offset = readI4();
          reader.goto_w(offset);
          break;
        case 201: // jsr_w
          offset = readI4();
          reader.jsr_w(offset);
          break;
          
          
        default:
          reader.unknown(opcode);
      }

      isWide = false; // reset wide modifier
    }

  }

  //--- those can only be called from within a JVMByteCodeReader.tableswitch() notification
  public void parseTableSwitchEntries(JVMByteCodeReader reader, int low, int high){
    for (int val=low; val<=high; val++){
      int offset = readI4();
      reader.tableswitchEntry(val, offset);
    }
  }
  public int getTableSwitchOffset(int low, int high, int defaultOffset, int val){
    if (val < low || val > high){
      return defaultOffset;
    }

    int n = Math.abs(val - low);
    pos += n*4;
    int pcOffset = readI4();

    return pcOffset;
  }

  //--- those can only be called from within a JVMByteCodeReader.lookupswitch() notification
  public void parseLookupSwitchEntries(JVMByteCodeReader reader, int nEntries){
    for (int i=0; i<nEntries; i++){
      int value = readI4();
      int offset = readI4();
      reader.lookupswitchEntry(i, value, offset);
    }
  }
  public int getLookupSwitchOffset(int nEntries, int defaultOffset, int val){
    for (int i=0; i<nEntries; i++){
      int match = readI4();
      if (val > match){
        pos +=4;
      } else if (val == match) {
        int offset = readI4();
        return offset;
      } else {
        break;
      }
    }
    return defaultOffset;
  }

}
