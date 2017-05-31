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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.JPFException;
import java.util.HashMap;

/**
 * the JPF internal representation for Java Annotations
 * 
 * AnnotationInfos represent a separate type system. While we could have used normal ClassInfos
 * (Java annotations are just restricted interfaces with some syntactic sugar), we keep this separate because
 * ClassInfos would be overkill. Besides, our runtime behavior differs in that we synchronously load
 * annotation class files when we encounter them during normal ClassInfo construction (i.e. we parse recursively),
 * whereas a normal JVM only loads them once they are referenced. The reason why we deviate is that
 * annotations are used more often in tools than via reflection within the SUT, i.e. they most likely will
 * be read either by JPF or by listeners, so we want them as soon as possible to avoid additional class state.
 * This also means we do not faithfully model ClassNotFoundExceptions on annotations due to reflection
 * calls within the SUT, but that seems less important than having them available during ClassInfo construction.
 * This mostly matters because of default values and inherited class annotations.
 * 
 * AnnotationInfo serves as the concrete type of declaration annotations, and as the base for
 * type annotations, holding all the info that comes from the annotation class file. In the first
 * case, AnnotationInfo instances can be shared if there are no explicit values. Sharing does not
 * make sense for type annotations which need to store site specific target info (from the classfile).
 * 
 * Note - AnnotationInfos loaded by the same ClassLoader that do not have explicitly set values are shared
 * between annotated objects
 * 
 */
public class AnnotationInfo implements Cloneable {

  // NOTE - never modify an Entry object since it might be shared between
  // different instances of the same annotation type
  public static class Entry implements Cloneable {
    String key;
    Object value;
    
    public String getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }
    
    public Entry (String key, Object value){
      this.key = key;
      this.value = value;
    }
    
    @Override
	public Entry clone(){
      try {
        return (Entry) super.clone();
      } catch (CloneNotSupportedException cnsx){
        throw new JPFException("AnnotationInfo.Entry clone() failed");
      }
    }
  }
  
  public static class EnumValue {
    String eClassName;
    String eConst;
    
    EnumValue (String clsName, String constName){
      eClassName = clsName;
      eConst = constName;
    }
    public String getEnumClassName(){
      return eClassName;
    }
    public String getEnumConstName(){
      return eConst;
    }
    @Override
	public String toString(){
      return eClassName + '.' + eConst;
    }
  }

  public static class ClassValue {
    String name;

    ClassValue (String cn){
      name = cn;
    }

    public String getName(){
      return name;
    }
    @Override
	public String toString(){
      return name;
    }
  }

  static final Entry[] NONE = new Entry[0];
  
  // we have to jump through a lot of hoops to handle default annotation parameter values
  // this is not ideal, since it causes the classfile to be re-read if the SUT
  // uses annotation reflection (which creates a ClassInfo), but this is rather
  // exotic, so we save some time by not creating a ClassInfo (which would hold
  // the default vals as method annotations) and directly store the default values here

  static HashMap<String, AnnotationAttribute> annotationAttributes = new HashMap<String, AnnotationAttribute>();

  public static class AnnotationAttribute {
    Entry[] defaultEntries;
    boolean isInherited;

    AnnotationAttribute (Entry[] defaultEntries, boolean isInherited) {
      this.defaultEntries = defaultEntries;
      this.isInherited = isInherited;
    }
  }
  
  public static Object getEnumValue(String eType, String eConst){
    return new EnumValue( Types.getClassNameFromTypeName(eType), eConst);
  }

  public static Object getClassValue(String type){
    return new ClassValue( Types.getClassNameFromTypeName(type));
  }  
  
  protected String name;
  protected Entry[] entries;
  protected boolean isInherited = false;
    
  /**
   * this records if the associated class file has been loaded. If it isn't resolved yet,
   * we don't know about default values, hence we need to check before retrieving field values
   * that have not been explicitly set. Note this is search global and hence does not need to
   * be state managed since we only check for default values, i.e. there are no side effects.
   * Loading has to happen with the right ClassLoader though
   */
  protected ClassLoaderInfo classLoader; // set once it is resolved (i.e. the corresponding classfile is read)
  
  
  public AnnotationInfo (String name, ClassLoaderInfo classLoader, AnnotationParser parser) throws ClassParseException {
    this.name = name;
    this.classLoader = classLoader;
    
    parser.parse(this);
  }
  
  /**
   * this is the base ctor for AbstractTypeAnnotationInfos, which add additional
   * target information from the classfile
   */
  protected AnnotationInfo (AnnotationInfo exemplar){
    this.name = exemplar.name;
    this.classLoader = exemplar.classLoader;
    this.entries = exemplar.entries;
    this.isInherited = exemplar.isInherited;
  }
  
  //--- the init API used by AnnotationParsers
  public void setName (String name) throws ClassParseException {
    if (!this.name.equals(name)){
      throw new ClassParseException("wrong annotation name in classfile, expected " + this.name + ", found " + name);
    }
  }

  public void setEntries (Entry[] entries){
    this.entries = entries;
  }
  
  public void setInherited (boolean isInherited){
    this.isInherited = isInherited;
  }
  
  
  public AnnotationInfo (String name, Entry[] entries, boolean isInherited){
    this.name = name;
    this.entries = entries;
    this.isInherited = isInherited;
  }


  public boolean isInherited (){
    return this.isInherited;
  }
  
  public ClassLoaderInfo getClassLoaderInfo(){
    return classLoader;
  }

  public String getName() {
    return name;
  }
  
  protected AnnotationInfo cloneFor (ClassLoaderInfo cl){
    try {
      AnnotationInfo ai = (AnnotationInfo) clone();
      
      // <2do> once we support class/enum values we have to clone these too
      
      ai.classLoader = cl;
      
      return ai;
      
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("AnnotationInfo cloneFor() failed");
    }
  }
  
  /**
   * this returns a clone that can be used to explicitly set values.
   * NOTE - Entry instances are still shared, i.e. to change values we have to create and set
   * new Entry instances
   */
  public AnnotationInfo cloneForOverriddenValues(){
    try {
      AnnotationInfo ai = (AnnotationInfo) clone();
      ai.entries = entries.clone();
      return ai;
      
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("AnnotationInfo cloneFor() failed");
    }    
  }
  
  public void setClonedEntryValue (String key, Object newValue){
    for (int i=0; i<entries.length; i++){
      if (entries[i].getKey().equals(key)){
        entries[i] = new Entry( key, newValue);
        return;
      }
    }    
  }
  
  public Entry[] getEntries() {
    return entries;
  }
  
  /**
   * this is the common getter that should trigger parsing the corresponding class file 
   */
  public Object getValue (String key){    
    for (int i=0; i<entries.length; i++){
      if (entries[i].getKey().equals(key)){
        return entries[i].getValue();
      }
    }
    return null;
  }

  
  // convenience method for single-attribute annotations
  public Object value() {
    return getValue("value");
  }
  
  public String valueAsString(){
    Object v = value();
    return (v != null) ? v.toString() : null;
  }
  
  public String getValueAsString (String key){
    Object v = getValue(key);
    return (v != null) ? v.toString() : null;
  }
  
  public String[] getValueAsStringArray() {
    String a[] = null; 
    Object v = value();
    if (v != null && v instanceof Object[]) {
      Object[] va = (Object[])v;
      a = new String[va.length];
      for (int i=0; i<a.length; i++) {
        if (va[i] != null) {
          a[i] = va[i].toString();
        }
      }
    }
    
    return a;    
  }
  
  public String[] getValueAsStringArray (String key) {
    // <2do> not very efficient
    String a[] = null; 
    Object v = getValue(key);
    if (v != null && v instanceof Object[]) {
      Object[] va = (Object[])v;
      a = new String[va.length];
      for (int i=0; i<a.length; i++) {
        if (va[i] != null) {
          a[i] = va[i].toString();
        }
      }
    }
    
    return a;
  }
  
  public <T> T getValue (String key, Class<T> type){
    Object v = getValue(key);
    if (type.isInstance(v)){
      return (T)v;
    } else {
      return null;
    }
  }
  
  public boolean getValueAsBoolean (String key){
    Object v = getValue(key);
    if (v instanceof Boolean){
      return ((Boolean)v).booleanValue();
    } else {
      throw new JPFException("annotation element @" + name + '.' + key + "() not a boolean: " + v);
    }
  }
  
  public int getValueAsInt (String key){
    Object v = getValue(key);
    if (v instanceof Integer){
      return ((Integer)v).intValue();
    } else {
      throw new JPFException("annotation element @" + name + '.' + key + "() not an int: " + v);
    }
  }

  public long getValueAsLong (String key){
    Object v = getValue(key);
    if (v instanceof Long){
      return ((Long)v).longValue();
    } else {
      throw new JPFException("annotation element @" + name + '.' + key + "() not a long: " + v);
    }
  }

  public float getValueAsFloat (String key){
    Object v = getValue(key);
    if (v instanceof Float){
      return ((Float)v).floatValue();
    } else {
      throw new JPFException("annotation element @" + name + '.' + key + "() not a float: " + v);
    }
  }
  
  public double getValueAsDouble (String key){
    Object v = getValue(key);
    if (v instanceof Double){
      return ((Double)v).doubleValue();
    } else {
      throw new JPFException("annotation element @" + name + '.' + key + "() not a double: " + v);
    }
  }
  
  public String asString() {
    StringBuilder sb = new StringBuilder();
    sb.append('@');
    sb.append(name);
    sb.append('[');
    for (int i=0; i<entries.length; i++){
      if (i > 0){
        sb.append(',');
      }
      sb.append(entries[i].getKey());
      sb.append('=');
      sb.append(entries[i].getValue());
    }
    sb.append(']');
    
    return sb.toString();
  }

}
