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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.AbstractTypeAnnotationInfo;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.BootstrapMethodInfo;
import gov.nasa.jpf.vm.BytecodeAnnotationInfo;
import gov.nasa.jpf.vm.BytecodeTypeParameterAnnotationInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.DirectCallStackFrame;
import gov.nasa.jpf.vm.ExceptionHandler;
import gov.nasa.jpf.vm.ExceptionParameterAnnotationInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.FormalParameterAnnotationInfo;
import gov.nasa.jpf.vm.GenericSignatureHolder;
import gov.nasa.jpf.vm.InfoObject;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SuperTypeAnnotationInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThrowsAnnotationInfo;
import gov.nasa.jpf.vm.TypeAnnotationInfo;
import gov.nasa.jpf.vm.TypeParameterAnnotationInfo;
import gov.nasa.jpf.vm.TypeParameterBoundAnnotationInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VariableAnnotationInfo;

/**
 * a ClassInfo that was created from a Java classfile
 */
public class JVMClassInfo extends ClassInfo {

  /**
   * this is the inner class that does the actual ClassInfo initialization from ClassFile. It is an inner class so that
   * (a) it can set ClassInfo fields, (b) it can extend ClassFileReaderAdapter, and (c) we don't clutter JVMClassInfo with
   * fields that are only temporarily used during parsing
   */
  class Initializer extends ClassFileReaderAdapter {
    protected ClassFile cf;
    protected JVMCodeBuilder cb;

    public Initializer (ClassFile cf, JVMCodeBuilder cb) throws ClassParseException {
      this.cf = cf;
      this.cb = cb;
      
      cf.parse(this);
    }

    @Override
    public void setClass (ClassFile cf, String clsName, String superClsName, int flags, int cpCount) throws ClassParseException {
      JVMClassInfo.this.setClass(clsName, superClsName, flags, cpCount);
    }

    @Override
    public void setClassAttribute (ClassFile cf, int attrIndex, String name, int attrLength) {
      if (name == ClassFile.SOURCE_FILE_ATTR) {
        cf.parseSourceFileAttr(this, null);

      } else if (name == ClassFile.SIGNATURE_ATTR) {
        cf.parseSignatureAttr(this, JVMClassInfo.this);

      } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR) {
        cf.parseAnnotationsAttr(this, JVMClassInfo.this);

      } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR) {
        //cf.parseAnnotationsAttr(this, ClassInfo.this);
        
      } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR) {
        cf.parseTypeAnnotationsAttr(this, JVMClassInfo.this);
        
      } else if (name == ClassFile.INNER_CLASSES_ATTR) {
        cf.parseInnerClassesAttr(this, JVMClassInfo.this);

      } else if (name == ClassFile.ENCLOSING_METHOD_ATTR) {
        cf.parseEnclosingMethodAttr(this, JVMClassInfo.this);
        
      } else if (name == ClassFile.BOOTSTRAP_METHOD_ATTR) {
        cf.parseBootstrapMethodAttr(this, JVMClassInfo.this);
        
      }
    }
    
    @Override
    public void setBootstrapMethodCount (ClassFile cf, Object tag, int count) {
      bootstrapMethods = new BootstrapMethodInfo[count];
    }
    
    @Override
    public void setBootstrapMethod (ClassFile cf, Object tag, int idx, int refKind, String cls, String mth, String descriptor, int[] cpArgs) {    
   
      int lambdaRefKind = cf.mhRefTypeAt(cpArgs[1]);
      
      int mrefIdx = cf.mhMethodRefIndexAt(cpArgs[1]);
      String clsName = cf.methodClassNameAt(mrefIdx).replace('/', '.');
      ClassInfo eclosingLambdaCls;
      
      if(!clsName.equals(JVMClassInfo.this.getName())) {
        eclosingLambdaCls = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
      } else {
        eclosingLambdaCls = JVMClassInfo.this;
      }
      
      assert (eclosingLambdaCls!=null);
      
      String mthName = cf.methodNameAt(mrefIdx);
      String signature = cf.methodDescriptorAt(mrefIdx);
      
      MethodInfo lambdaBody = eclosingLambdaCls.getMethod(mthName + signature, false);
      
      String samDescriptor = cf.methodTypeDescriptorAt(cpArgs[2]);
            
      if(lambdaBody!=null) {
        bootstrapMethods[idx] = new BootstrapMethodInfo(lambdaRefKind, JVMClassInfo.this, lambdaBody, samDescriptor);
      }
    }
    
   //--- inner/enclosing classes 
    @Override
    public void setInnerClassCount (ClassFile cf, Object tag, int classCount) {
      innerClassNames = new String[classCount];
    }

    @Override
    public void setInnerClass (ClassFile cf, Object tag, int innerClsIndex,
            String outerName, String innerName, String innerSimpleName, int accessFlags) {
      // Ok, this is a total mess - some names are in dot notation, others use '/'
      // and to make it even more confusing, some InnerClass attributes refer NOT
      // to the currently parsed class, so we have to check if we are the outerName,
      // but then 'outerName' can also be null instead of our own name.
      // Oh, and there are also InnerClass attributes that have their own name as inner names
      // (see java/lang/String$CaseInsensitiveComparator or ...System and java/lang/System$1 for instance)
      if (outerName != null) {
        outerName = Types.getClassNameFromTypeName(outerName);
      }

      innerName = Types.getClassNameFromTypeName(innerName);
      if (!innerName.equals(name)) {
        innerClassNames[innerClsIndex] = innerName;

      } else {
        // this refers to ourself, and can be a force fight with setEnclosingMethod
        if (outerName != null) { // only set if this is a direct member, otherwise taken from setEnclosingMethod
          setEnclosingClass(outerName);
        }
      }
    }

    @Override
    public void setEnclosingMethod (ClassFile cf, Object tag, String enclosingClassName, String enclosingMethodName, String descriptor) {
      setEnclosingClass(enclosingClassName);

      if (enclosingMethodName != null) {
        JVMClassInfo.this.setEnclosingMethod(enclosingMethodName + descriptor);
      }
    }

    @Override
    public void setInnerClassesDone (ClassFile cf, Object tag) {
      // we have to check if we allocated too many - see the mess above
      for (int i = 0; i < innerClassNames.length; i++) {
        innerClassNames = Misc.stripNullElements(innerClassNames);
      }
    }

    //--- source file
    @Override
    public void setSourceFile (ClassFile cf, Object tag, String fileName) {
      JVMClassInfo.this.setSourceFile(fileName);
    }
    
    //--- interfaces
    @Override
    public void setInterfaceCount (ClassFile cf, int ifcCount) {
      interfaceNames = new String[ifcCount];
    }

    @Override
    public void setInterface (ClassFile cf, int ifcIndex, String ifcName) {
      interfaceNames[ifcIndex] = Types.getClassNameFromTypeName(ifcName);
    }

    //--- fields
    // unfortunately they are stored together in the ClassFile, i.e. we 
    // have to split them up once we are done
    
    protected FieldInfo[] fields;
    protected FieldInfo curFi; // need to cache for attributes

    @Override
    public void setFieldCount (ClassFile cf, int fieldCount) {
      if (fieldCount > 0){
        fields = new FieldInfo[fieldCount];
      } else {
        fields = null;
      }
    }

    @Override
    public void setField (ClassFile cf, int fieldIndex, int accessFlags, String name, String descriptor) {
      FieldInfo fi = FieldInfo.create(name, descriptor, accessFlags);
      fields[fieldIndex] = fi;
      curFi = fi; // for attributes
    }

    @Override
    public void setFieldAttribute (ClassFile cf, int fieldIndex, int attrIndex, String name, int attrLength) {
      if (name == ClassFile.SIGNATURE_ATTR) {
        cf.parseSignatureAttr(this, curFi);

      } else if (name == ClassFile.CONST_VALUE_ATTR) {
        cf.parseConstValueAttr(this, curFi);

      } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR) {
        cf.parseAnnotationsAttr(this, curFi);

      } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR) {
        cf.parseTypeAnnotationsAttr(this, curFi);
        
      } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR) {
        //cf.parseAnnotationsAttr(this, curFi);
      }
    }

    @Override
    public void setConstantValue (ClassFile cf, Object tag, Object constVal) {
      curFi.setConstantValue(constVal);
    }

    @Override
    public void setFieldsDone (ClassFile cf) {
      setFields(fields);
    }
 
  //--- declaredMethods
    protected MethodInfo curMi;

    @Override
    public void setMethodCount (ClassFile cf, int methodCount) {
      methods = new LinkedHashMap<String, MethodInfo>();
    }

    @Override
    public void setMethod (ClassFile cf, int methodIndex, int accessFlags, String name, String signature) {
      MethodInfo mi = MethodInfo.create(name, signature, accessFlags);
      curMi = mi;
    }
    
    @Override
    public void setMethodDone (ClassFile cf, int methodIndex){
      curMi.setLocalVarAnnotations();

      JVMClassInfo.this.setMethod(curMi);
    }

    @Override
    public void setMethodAttribute (ClassFile cf, int methodIndex, int attrIndex, String name, int attrLength) {
      if (name == ClassFile.CODE_ATTR) {
        cf.parseCodeAttr(this, curMi);

      } else if (name == ClassFile.SIGNATURE_ATTR) {
        cf.parseSignatureAttr(this, curMi);

      } else if (name == ClassFile.EXCEPTIONS_ATTR) {
        cf.parseExceptionAttr(this, curMi);

      } else if (name == ClassFile.RUNTIME_VISIBLE_ANNOTATIONS_ATTR) {
        cf.parseAnnotationsAttr(this, curMi);

      } else if (name == ClassFile.RUNTIME_INVISIBLE_ANNOTATIONS_ATTR) {
        //cf.parseAnnotationsAttr(this, curMi);
      } else if (name == ClassFile.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS_ATTR) {
        cf.parseParameterAnnotationsAttr(this, curMi);

      } else if (name == ClassFile.RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS_ATTR) {
        //cf.parseParameterAnnotationsAttr(this, curMi);
        
      } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR) {
        cf.parseTypeAnnotationsAttr(this, curMi);
      }      
      
    }

    //--- current methods throws list
    protected String[] exceptions;

    @Override
    public void setExceptionCount (ClassFile cf, Object tag, int exceptionCount) {
      exceptions = new String[exceptionCount];
    }

    @Override
    public void setException (ClassFile cf, Object tag, int exceptionIndex, String exceptionType) {
      exceptions[exceptionIndex] = Types.getClassNameFromTypeName(exceptionType);
    }

    @Override
    public void setExceptionsDone (ClassFile cf, Object tag) {
      curMi.setThrownExceptions(exceptions);
    }

    //--- current method exception handlers
    protected ExceptionHandler[] handlers;

    @Override
    public void setExceptionHandlerTableCount (ClassFile cf, Object tag, int exceptionTableCount) {
      handlers = new ExceptionHandler[exceptionTableCount];
    }

    @Override
    public void setExceptionHandler (ClassFile cf, Object tag, int handlerIndex,
            int startPc, int endPc, int handlerPc, String catchType) {
      ExceptionHandler xh = new ExceptionHandler(catchType, startPc, endPc, handlerPc);
      handlers[handlerIndex] = xh;
    }

    @Override
    public void setExceptionHandlerTableDone (ClassFile cf, Object tag) {
      curMi.setExceptionHandlers(handlers);
    }

    //--- current method code  
    @Override
    public void setCode (ClassFile cf, Object tag, int maxStack, int maxLocals, int codeLength) {
      curMi.setMaxLocals(maxLocals);
      curMi.setMaxStack(maxStack);

      cb.reset(cf, curMi);

      cf.parseBytecode(cb, tag, codeLength);
      cb.installCode();
    }

    @Override
    public void setCodeAttribute (ClassFile cf, Object tag, int attrIndex, String name, int attrLength) {
      if (name == ClassFile.LINE_NUMBER_TABLE_ATTR) {
        cf.parseLineNumberTableAttr(this, tag);

      } else if (name == ClassFile.LOCAL_VAR_TABLE_ATTR) {
        cf.parseLocalVarTableAttr(this, tag);
        
      } else if (name == ClassFile.RUNTIME_VISIBLE_TYPE_ANNOTATIONS_ATTR){
        cf.parseTypeAnnotationsAttr(this, tag);
      }
    }

    //--- current method line numbers
    protected int[] lines, startPcs;

    @Override
    public void setLineNumberTableCount (ClassFile cf, Object tag, int lineNumberCount) {
      lines = new int[lineNumberCount];
      startPcs = new int[lineNumberCount];
    }

    @Override
    public void setLineNumber (ClassFile cf, Object tag, int lineIndex, int lineNumber, int startPc) {
      lines[lineIndex] = lineNumber;
      startPcs[lineIndex] = startPc;
    }

    @Override
    public void setLineNumberTableDone (ClassFile cf, Object tag) {
      curMi.setLineNumbers(lines, startPcs);
    }
    
    //--- current method local variables
    protected LocalVarInfo[] localVars;

    @Override
    public void setLocalVarTableCount (ClassFile cf, Object tag, int localVarCount) {
      localVars = new LocalVarInfo[localVarCount];
    }

    @Override
    public void setLocalVar (ClassFile cf, Object tag, int localVarIndex,
            String varName, String descriptor, int scopeStartPc, int scopeEndPc, int slotIndex) {
      LocalVarInfo lvi = new LocalVarInfo(varName, descriptor, "", scopeStartPc, scopeEndPc, slotIndex);
      localVars[localVarIndex] = lvi;
    }

    @Override
    public void setLocalVarTableDone (ClassFile cf, Object tag) {
      curMi.setLocalVarTable(localVars);
    }
    
    //--- annotations
    protected AnnotationInfo[] annotations;
    protected AnnotationInfo curAi;
    protected LinkedList<AnnotationInfo> annotationStack;
    protected LinkedList<Object[]> valuesStack;
    protected AnnotationInfo[][] parameterAnnotations;
    protected Object[] values;
    // true if we need to filter null annotations
    private boolean compactAnnotationArray = false;

    //--- declaration annotations
    
    @Override
    public void setAnnotationCount (ClassFile cf, Object tag, int annotationCount) {
      annotations = new AnnotationInfo[annotationCount];
    }

    @Override
    public void setAnnotation (ClassFile cf, Object tag, int annotationIndex, String annotationType) {
      if (tag instanceof InfoObject) {
        if(annotationIndex == -1) {
          if(annotationStack == null) {
            assert valuesStack == null;
            valuesStack = new LinkedList<>();
            annotationStack = new LinkedList<>();
          }
          annotationStack.addFirst(curAi);
          valuesStack.addFirst(values);
        }
        try { 
          curAi = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
          if(annotationIndex != -1) {
            annotations[annotationIndex] = curAi;
          }
        } catch(ClassInfoException cie) {
          // if we can't parse a field, we're sunk, throw and tank the reflective call
          if(annotationIndex == -1) {
            throw cie;
          }
          compactAnnotationArray = true;
          annotations[annotationIndex] = null;
          // skip this annotation
          throw new SkipAnnotation();
        }
      }
    }
    
    @Override
    public void setAnnotationsDone (ClassFile cf, Object tag) {
      if (tag instanceof InfoObject) {
        AnnotationInfo[] toSet;
        if(compactAnnotationArray) {
          int nAnnot = 0;
          for(AnnotationInfo ai : annotations) {
            if(ai != null) {
              nAnnot++;
            }
          }
          toSet = new AnnotationInfo[nAnnot];
          int idx = 0;
          for(AnnotationInfo ai : annotations) {
            if(ai != null) {
              toSet[idx++] = ai;
            }
          }
        } else {
          toSet = annotations;
        }
        ((InfoObject) tag).addAnnotations(toSet);
      }
      compactAnnotationArray = false;
    }

    @Override
    public void setParameterCount (ClassFile cf, Object tag, int parameterCount) {
      parameterAnnotations = new AnnotationInfo[parameterCount][];
    }

    @Override
    public void setParameterAnnotationCount (ClassFile cf, Object tag, int paramIndex, int annotationCount) {
      annotations = new AnnotationInfo[annotationCount];
      parameterAnnotations[paramIndex] = annotations;
    }

    @Override
    public void setParameterAnnotation (ClassFile cf, Object tag, int annotationIndex, String annotationType) {
      try {
        curAi = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
        annotations[annotationIndex] = curAi;
      } catch(ClassInfoException cie) {
        compactAnnotationArray = true;
        annotations[annotationIndex] = null;
        throw new SkipAnnotation();
      }
    }

    @Override
    public void setParametersDone (ClassFile cf, Object tag) {
      curMi.setParameterAnnotations(parameterAnnotations);
    }
    
    //--- Java 8 type annotations    
    
    @Override
    public void setTypeAnnotationCount(ClassFile cf, Object tag, int annotationCount){
      annotations = new AnnotationInfo[annotationCount];
    }

    @Override
    public void setTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                           int typeIndex, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new TypeParameterAnnotationInfo(base, targetType, typePath, typeIndex);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setSuperTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                       int superTypeIdx, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new SuperTypeAnnotationInfo(base, targetType, typePath, superTypeIdx);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setTypeParameterBoundAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType,
                                       int typeIndex, int boundIndex, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new TypeParameterBoundAnnotationInfo(base, targetType, typePath, typeIndex, boundIndex);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setTypeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType,
                                  short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new TypeAnnotationInfo(base, targetType, typePath);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setFormalParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                             int paramIndex, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new FormalParameterAnnotationInfo(base, targetType, typePath, paramIndex);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setThrowsAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                    int throwsTypeIdx, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new ThrowsAnnotationInfo(base, targetType, typePath, throwsTypeIdx);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setVariableAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                      long[] scopeEntries, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      VariableAnnotationInfo vai = new VariableAnnotationInfo(base, targetType, typePath, scopeEntries);
      curAi = vai;
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setExceptionParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                                int exceptionIndex, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi= new ExceptionParameterAnnotationInfo(base, targetType, typePath, exceptionIndex);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setBytecodeAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                      int offset, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new BytecodeAnnotationInfo(base, targetType, typePath, offset);
      annotations[annotationIndex] = curAi;
    }
    @Override
    public void setBytecodeTypeParameterAnnotation(ClassFile cf, Object tag, int annotationIndex, int targetType, 
                                             int offset, int typeArgIdx, short[] typePath, String annotationType){
      AnnotationInfo base = getResolvedAnnotationInfo(Types.getClassNameFromTypeName(annotationType));
      curAi = new BytecodeTypeParameterAnnotationInfo(base, targetType, typePath, offset, typeArgIdx);
      annotations[annotationIndex] = curAi;
    }

    @Override
    public void setTypeAnnotationsDone(ClassFile cf, Object tag) {
      if (tag instanceof InfoObject) {
        int len = annotations.length;
        AbstractTypeAnnotationInfo[] tais = new AbstractTypeAnnotationInfo[annotations.length];
        for (int i=0; i<len; i++){
          tais[i] = (AbstractTypeAnnotationInfo)annotations[i];
        }
        
        // we can get them in batches (e.g. VariableTypeAnnos from code attrs and ReturnTypeAnnos from method attrs
        ((InfoObject) tag).addTypeAnnotations( tais);
      }
    }

    //--- AnnotationInfo values entries
    @Override
    public void setAnnotationValueCount (ClassFile cf, Object tag, int annotationIndex, int nValuePairs) {
      // if we have values, we need to clone the defined annotation so that we can overwrite entries
      curAi = curAi.cloneForOverriddenValues();
      if(annotationIndex != -1) {
        annotations[annotationIndex] = curAi;
      }
    }
    
    @Override
    public void setPrimitiveAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
            String elementName, int arrayIndex, Object val) {
      if (arrayIndex >= 0) {
        values[arrayIndex] = val;
      } else {
        curAi.setClonedEntryValue(elementName, val);
      }
    }
    
    @Override
    public void setAnnotationFieldValue(ClassFile cf, Object tag, int annotationIndex, int valueIndex, String elementName, int arrayIndex) {
      assert annotationStack.size() > 0;
      AnnotationInfo ai = curAi;
      values = valuesStack.pop();
      curAi = annotationStack.pop();
      if(arrayIndex >= 0) {
        values[arrayIndex] = ai;
      } else {
        curAi.setClonedEntryValue(elementName, ai);
      }
    }

    @Override
    public void setStringAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
            String elementName, int arrayIndex, String val) {
      if (arrayIndex >= 0) {
        values[arrayIndex] = val;
      } else {
        curAi.setClonedEntryValue(elementName, val);
      }
    }

    @Override
    public void setClassAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex, String elementName,
            int arrayIndex, String typeName) {
      Object val = AnnotationInfo.getClassValue(typeName);
      if (arrayIndex >= 0) {
        values[arrayIndex] = val;
      } else {
        curAi.setClonedEntryValue(elementName, val);
      }
    }

    @Override
    public void setEnumAnnotationValue (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
            String elementName, int arrayIndex, String enumType, String enumValue) {
      Object val = AnnotationInfo.getEnumValue(enumType, enumValue);
      if (arrayIndex >= 0) {
        values[arrayIndex] = val;
      } else {
        curAi.setClonedEntryValue(elementName, val);
      }
    }

    @Override
    public void setAnnotationValueElementCount (ClassFile cf, Object tag, int annotationIndex, int valueIndex,
            String elementName, int elementCount) {
      values = new Object[elementCount];
    }

    @Override
    public void setAnnotationValueElementsDone (ClassFile cf, Object tag, int annotationIndex, int valueIndex, String elementName) {
      curAi.setClonedEntryValue(elementName, values);
    }

    //--- common attrs
    @Override
    public void setSignature (ClassFile cf, Object tag, String signature) {
      if (tag instanceof GenericSignatureHolder) {
        ((GenericSignatureHolder) tag).setGenericSignature(signature);
      }
    }
  }

  // since nested class init locking can explode the state space, we make it optional and controllable
  protected static boolean nestedInit;
  protected static StringSetMatcher includeNestedInit;
  protected static StringSetMatcher excludeNestedInit;

  protected static boolean init (Config config){
    nestedInit = config.getBoolean("jvm.nested_init", false);
    if (nestedInit){
      includeNestedInit =  StringSetMatcher.getNonEmpty(config.getStringArray("jvm.nested_init.include"));
      excludeNestedInit = StringSetMatcher.getNonEmpty(config.getStringArray("jvm.nested_init.exclude"));
    }

    return true;
  }

  JVMClassInfo (String name, ClassLoaderInfo cli, ClassFile cf, String srcUrl, JVMCodeBuilder cb) throws ClassParseException {
    super( name, cli, srcUrl);
    
    new Initializer( cf, cb); // we just need the ctor
    
    resolveAndLink();
  }
  
  
  //--- for annotation classinfos
  
  // called on the annotation classinfo
  @Override
  protected ClassInfo createAnnotationProxy (String proxyName){
    return new JVMClassInfo (this, proxyName, classLoader, null);
  }
  
  // concrete proxy ctor
  protected JVMClassInfo (ClassInfo ciAnnotation, String proxyName, ClassLoaderInfo cli, String url) {
    super( ciAnnotation, proxyName, cli, url);
  }

  /**
   * This is called on the functional interface type. It creates a synthetic type which 
   * implements the functional interface and contains a method capturing the behavior 
   * of the lambda expression.
   */
  @Override
  protected ClassInfo createFuncObjClassInfo (BootstrapMethodInfo bootstrapMethod, String name, String samUniqueName, String[] fieldTypesName) {
    return new JVMClassInfo(this, bootstrapMethod, name, samUniqueName, fieldTypesName);
  }
  
  protected JVMClassInfo (ClassInfo funcInterface, BootstrapMethodInfo bootstrapMethod, String name, String samUniqueName, String[] fieldTypesName) {
    super(funcInterface, bootstrapMethod, name, fieldTypesName);
    
    // creating a method corresponding to the single abstract method of the functional interface
    methods = new HashMap<String, MethodInfo>();
    
    MethodInfo fiMethod = funcInterface.getInterfaceAbstractMethod();
    int modifiers = fiMethod.getModifiers() & (~Modifier.ABSTRACT);
    int nLocals = fiMethod.getArgumentsSize();
    int nOperands = this.nInstanceFields + nLocals;

    MethodInfo mi = new MethodInfo(fiMethod.getName(), fiMethod.getSignature(), modifiers, nLocals, nOperands);
    mi.linkToClass(this);
    
    methods.put(mi.getUniqueName(), mi);
    
    setLambdaDirectCallCode(mi, bootstrapMethod);
    
    try {
      resolveAndLink();
    } catch (ClassParseException e) {
      // we do not even get here - this a synthetic class, and at this point
      // the interfaces are already loaded.
    }
  }

  /**
   * perform initialization of this class and its not-yet-initialized superclasses (top down),
   * which includes calling clinit() methods
   *
   * This is overridden here to model a questionable yet consequential behavior of hotspot, which
   * is holding derived class locks when initializing base classes. The generic implementation in
   * ClassInfo uses non-nested locks (i.e. A.clinit() only synchronizes on A.class) and hence cannot
   * produce the same static init deadlocks as hotspot. In order to catch such defects we implement
   * nested locking here.
   *
   * The main difference is that the generic implementation only pushes DCSFs for required clinits
   * and otherwise doesn't lock anything. Here, we create one static init specific DCSF which wraps
   * all clinits in nested monitorenter/exits. We create this even if there is no clinit so that we
   * mimic hotspot locking.
   *
   * Note this scheme also enables us to get rid of the automatic clinit sync (they don't have
   * a 0x20 sync modifier in classfiles)
   *
   * @return true if client needs to re-execute because we pushed DirectCallStackFrames
   */
  @Override
  public boolean initializeClass(ThreadInfo ti) {
    if (needsInitialization(ti)) {
      if (nestedInit && StringSetMatcher.isMatch(name, includeNestedInit, excludeNestedInit)) {
        registerClass(ti); // this is recursively upwards
        int nOps = 2 * (getNumberOfSuperClasses() + 1); // this is just an upper bound for the number of operands we need

        MethodInfo miInitialize = new MethodInfo("[initializeClass]", "()V", Modifier.STATIC, 0, nOps);
        JVMDirectCallStackFrame frame = new JVMDirectCallStackFrame(miInitialize, null);
        JVMCodeBuilder cb = getSystemCodeBuilder(null, miInitialize);

        addClassInit(ti, frame, cb); // this is recursively upwards until we hit a initialized superclass
        cb.directcallreturn();
        cb.installCode();

        // this is normally initialized in the ctor, but at that point we don't have the code yet
        frame.setPC(miInitialize.getFirstInsn());

        ti.pushFrame(frame);
        return true; // client has to re-execute, we pushed a stackframe


      } else { // use generic initialization without nested locks (directly calling clinits)
        return super.initializeClass(ti);
      }

    } else {
      return false; // nothing to do
    }
  }

  protected void addClassInit (ThreadInfo ti, JVMDirectCallStackFrame frame, JVMCodeBuilder cb){
    int clsObjRef = getClassObjectRef();

    frame.pushRef(clsObjRef);
    cb.monitorenter();

    if (superClass != null && superClass.needsInitialization(ti)) {
      ((JVMClassInfo) superClass).addClassInit(ti, frame, cb);      // go recursive
    }

    if (getMethod("<clinit>()V", false) != null) { // do we have a clinit
      cb.invokeclinit(this);
    } else {
      cb.finishclinit(this);
      // we can't just do call ci.setInitialized() since that has to be deferred
    }

    frame.pushRef(clsObjRef);
    cb.monitorexit();
  }

  //--- call processing
  
  protected JVMCodeBuilder getSystemCodeBuilder (ClassFile cf, MethodInfo mi){
    JVMSystemClassLoaderInfo sysCl = (JVMSystemClassLoaderInfo) ClassLoaderInfo.getCurrentSystemClassLoader();
    JVMCodeBuilder cb = sysCl.getSystemCodeBuilder(cf, mi);
    
    return cb;
  }
  
  /**
   * to be called from super proxy ctor
   * this needs to be in the VM specific ClassInfo because we need to create code
   */
  @Override
  protected void setAnnotationValueGetterCode (MethodInfo pmi, FieldInfo fi){
    JVMCodeBuilder cb = getSystemCodeBuilder(null, pmi);

    cb.aload(0);
    cb.getfield( pmi.getName(), name, pmi.getReturnType());
    if (fi.isReference()) {
      cb.areturn();
    } else {
      if (fi.getStorageSize() == 1) {
        cb.ireturn();
      } else {
        cb.lreturn();
      }
    }

    cb.installCode();
  }
  
  @Override
  protected void setDirectCallCode (MethodInfo miDirectCall, MethodInfo miCallee){
    JVMCodeBuilder cb = getSystemCodeBuilder(null, miDirectCall);
    
    String calleeName = miCallee.getName();
    String calleeSig = miCallee.getSignature();

    if (miCallee.isStatic()){
      if (miCallee.isClinit()) {
        cb.invokeclinit(this);
      } else {
        cb.invokestatic( name, calleeName, calleeSig);
      }
    } else if (name.equals("<init>") || miCallee.isPrivate()){
      cb.invokespecial( name, calleeName, calleeSig);
    } else {
      cb.invokevirtual( name, calleeName, calleeSig);
    }

    cb.directcallreturn();
    
    cb.installCode();
  }
  
  @Override
  protected void setNativeCallCode (NativeMethodInfo miNative){
    JVMCodeBuilder cb = getSystemCodeBuilder(null, miNative);
    
    cb.executenative(miNative);
    cb.nativereturn();
    
    cb.installCode();
  }
  
  @Override
  protected void setRunStartCode (MethodInfo miStub, MethodInfo miRun){
    JVMCodeBuilder cb = getSystemCodeBuilder(null, miStub);
    
    cb.runStart( miStub);
    cb.invokevirtual( name, miRun.getName(), miRun.getSignature());
    cb.directcallreturn();
    
    cb.installCode();    
  }
  
  /**
   * This method creates the body of the function object method that captures the 
   * lambda behavior.
   */
  @Override
  protected void setLambdaDirectCallCode (MethodInfo miDirectCall, BootstrapMethodInfo bootstrapMethod) {
    
    MethodInfo miCallee = bootstrapMethod.getLambdaBody();
    String samSignature = bootstrapMethod.getSamDescriptor();
    JVMCodeBuilder cb = getSystemCodeBuilder(null, miDirectCall);
    
    String calleeName = miCallee.getName();
    String calleeSig = miCallee.getSignature();
    
    ClassInfo callerCi = miDirectCall.getClassInfo();
    
    // loading free variables, which are used in the body of the lambda 
    // expression and captured by the lexical scope. These variables  
    // are stored by the fields of the synthetic function object class
    int n = callerCi.getNumberOfInstanceFields();
    for(int i=0; i<n; i++) {
      cb.aload(0);
      FieldInfo fi = callerCi.getInstanceField(i);
      
      cb.getfield(fi.getName(), callerCi.getName(), Types.getTypeSignature(fi.getSignature(), false));
    }

    // adding bytecode instructions to load input parameters of the lambda expression
    n = miDirectCall.getArgumentsSize();
    for(int i=1; i<n; i++) {
      cb.aload(i);
    }
    
    String calleeClass = miCallee.getClassName(); 
    
    // adding the bytecode instruction to invoke lambda method
    switch (bootstrapMethod.getLambdaRefKind()) {
    case ClassFile.REF_INVOKESTATIC:
      cb.invokestatic(calleeClass, calleeName, calleeSig);
      break;
    case ClassFile.REF_INVOKEINTERFACE:
      cb.invokeinterface(calleeClass, calleeName, calleeSig);
      break;
    case ClassFile.REF_INVOKEVIRTUAL:
      cb.invokevirtual(calleeClass, calleeName, calleeSig);
      break;
    case ClassFile.REF_NEW_INVOKESPECIAL:
      cb.new_(calleeClass);
      cb.invokespecial(calleeClass, calleeName, calleeSig);
      break;
    case ClassFile.REF_INVOKESPECIAL:
      cb.invokespecial(calleeClass, calleeName, calleeSig);
      break;
    }
    
    String returnType = Types.getReturnTypeSignature(samSignature);
    int  len = returnType.length();
    char c = returnType.charAt(0);

    // adding a return statement for function object method
    if (len == 1) {
      switch (c) {
      case 'B':
      case 'I':
      case 'C':
      case 'Z':
      case 'S':
        cb.ireturn();
        break;
      case 'D':
        cb.dreturn();
        break;
      case 'J':
        cb.lreturn();
        break;
      case 'F':
        cb.freturn();
        break;
      case 'V':
        cb.return_();
        break;
      }
    } else {
      cb.areturn();
    }
    
    cb.installCode();
  }
  
  // create a stack frame that has properly initialized arguments
  @Override
  public StackFrame createStackFrame (ThreadInfo ti, MethodInfo callee){
    
    if (callee.isMJI()){
      NativeMethodInfo nativeCallee = (NativeMethodInfo) callee;
      JVMNativeStackFrame calleeFrame = new JVMNativeStackFrame( nativeCallee);
      calleeFrame.setArguments( ti);
      return calleeFrame; 
      
    } else {
      JVMStackFrame calleeFrame = new JVMStackFrame( callee);
      calleeFrame.setCallArguments( ti);
      return calleeFrame;      
    }
  }
  
  @Override
  public DirectCallStackFrame createDirectCallStackFrame (ThreadInfo ti, MethodInfo miCallee, int nLocals){
    int nOperands = miCallee.getNumberOfCallerStackSlots();
    
    MethodInfo miDirect = new MethodInfo(miCallee, nLocals, nOperands);
    setDirectCallCode( miDirect, miCallee);
    
    return new JVMDirectCallStackFrame( miDirect, miCallee);
  }
  
  /**
   * while this is a normal DirectCallStackFrame, it has different code which has to be created here 
   */
  @Override
  public DirectCallStackFrame createRunStartStackFrame (ThreadInfo ti, MethodInfo miRun){
    MethodInfo miDirect = new MethodInfo( miRun, 0, 1);
    setRunStartCode( miDirect, miRun);
    
    return new JVMDirectCallStackFrame( miDirect, miRun);
  }
  
}
