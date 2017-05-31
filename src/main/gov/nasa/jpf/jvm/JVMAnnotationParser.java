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
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.AnnotationParser;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.Types;

/**
 * parser that reads annotation classfiles and extracts default value entries
 * 
 * Java annotations form a different type system. Java annotations are essentially
 * restricted interfaces (no super-interface, no fields other than static finals
 * that are inlined by the compiler)
 * 
 * Since Java annotations use only a small subset of the Java classfile format
 * we only have to parse methods and method attributes
 * 
 * <2do> class and enum values are not yet supported
 */
public class JVMAnnotationParser extends ClassFileReaderAdapter implements AnnotationParser {

  ClassFile cf;
  AnnotationInfo ai;
  
  String key;
  Object value;
  Object[] valElements;

  String annotationName;
  AnnotationInfo.Entry[] entries;
  
  public JVMAnnotationParser (ClassFile cf) {
    this.cf = cf;
  }

  @Override
  public void parse (AnnotationInfo ai) throws ClassParseException {
    this.ai = ai;
    
    cf.parse(this);
  }
    
  //--- the overridden ClassFileReader methods

  @Override
  public void setClass (ClassFile cf, String clsName, String superClsName, int flags, int cpCount) throws ClassParseException {
    entries = null;
    annotationName = Types.getClassNameFromTypeName(clsName);
    
    ai.setName(annotationName);
  }

  @Override
  public void setInterface (ClassFile cf, int ifcIndex, String ifcName) {
    if (!"java/lang/annotation/Annotation".equals(ifcName)) {
      throw new JPFException("illegal annotation interface of: " + annotationName + " is " + ifcName);
    }
  }

  @Override
  public void setMethodCount (ClassFile cf, int methodCount) {
    entries = new AnnotationInfo.Entry[methodCount];
  }

  @Override
  public void setMethod (ClassFile cf, int methodIndex, int accessFlags, String name, String descriptor) {
    key = name;
    value = null;
  }

  @Override
  public void setMethodDone (ClassFile cf, int methodIndex){
    entries[methodIndex] = new AnnotationInfo.Entry(key, value);
  }
  
  @Override
  public void setMethodsDone (ClassFile cf){
    ai.setEntries(entries);
  }
  
  @Override
  public void setMethodAttribute (ClassFile cf, int methodIndex, int attrIndex, String name, int attrLength) {
    if (name == ClassFile.ANNOTATIONDEFAULT_ATTR) {
      cf.parseAnnotationDefaultAttr(this, key);
    }
  }

  @Override
  public void setClassAttribute (ClassFile cf, int attrIndex, String name, int attrLength) {
    if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR) {
      key = null;
      cf.parseAnnotationsAttr(this, null);
    }
  }

  @Override
  public void setAnnotation (ClassFile cf, Object tag, int annotationIndex, String annotationType) {
    if (annotationType.equals("Ljava/lang/annotation/Inherited;")) {
      ai.setInherited( true);
    }
  }

  @Override
  public void setPrimitiveAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, Object val) {
    if (arrayIndex >= 0) {
      valElements[arrayIndex] = val;
    } else {
      if (key != null){
        value = val;
      }
    }
  }

  @Override
  public void setStringAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String val) {
    if (arrayIndex >= 0) {
      valElements[arrayIndex] = val;
    } else {
      if (key != null){
        value = val;
      }
    }
  }

  @Override
  public void setClassAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String typeName) {
    Object val = AnnotationInfo.getClassValue(typeName);
    if (arrayIndex >= 0) {
      valElements[arrayIndex] = val;
    } else {
      if (key != null){
        value = val;
      }
    }
  }

  @Override
  public void setEnumAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int arrayIndex, String enumType, String enumValue) {
    Object val = AnnotationInfo.getEnumValue(enumType, enumValue);
    if (arrayIndex >= 0) {
      valElements[arrayIndex] = val;
    } else {
      if (key != null){
        value = val;
      }
    }
  }

  @Override
  public void setAnnotationValueElementCount (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName, int elementCount) {
    valElements = new Object[elementCount];
  }

  @Override
  public void setAnnotationValueElementsDone (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
          String elementName) {
    if (key != null) {
      value = valElements;
    }
  }
}
