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

/**
 * abstract AnnotationInfo base for Java 8 type annotations
 */
public abstract class AbstractTypeAnnotationInfo extends AnnotationInfo {
  
  protected int targetType;   // see section 3.2 of JSR 308 - constants defined in .jvm.ClassFile
  protected short[] typePath; // the type path for compound type annotations as per 3.4 of JSR 308
  
  protected AbstractTypeAnnotationInfo (AnnotationInfo base, int targetType, short[] typePath) {
    super(base);
    
    this.targetType = targetType;
    this.typePath = typePath;
  }
  
  // <2do> add typePath query
  
  public int getTargetType(){
    return targetType;
  }
  
  protected void addArgs(StringBuilder sb){
    // nothing here
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getName());
    sb.append( '{');
    
    sb.append( "targetType:");
    sb.append( Integer.toHexString(targetType));
    
    sb.append( ",name:");
    sb.append( name);
    
    if (typePath != null){
      sb.append( ",path:");
      for (int i=0; i<typePath.length;i++){
        int e = typePath[i];
        sb.append('(');
        sb.append( Integer.toString((e>>8) & 0xff));
        sb.append( Integer.toString(e & 0xff));
        sb.append(')');
      }
    }
    
    addArgs(sb); // overridden by subclasses
    sb.append( '}');    
    
    return sb.toString();
  }
}
