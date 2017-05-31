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

package gov.nasa.jpf.util;

import gov.nasa.jpf.vm.ClassInfo;

/**
 * a matcher for collections of TypeSpecs
 */
public class TypeSpecMatcher {

  protected TypeSpec[] mypeSpecs;
  
  public static TypeSpecMatcher create (String[] specs){
    if (specs != null && specs.length > 0){
      return new TypeSpecMatcher(specs);
    } else {
      return null;
    }
  }
  
  public TypeSpecMatcher(String[] specs){
    int len = specs.length;
    mypeSpecs = new TypeSpec[len];
    for (int i=0; i<len; i++){
      mypeSpecs[i] = TypeSpec.createTypeSpec(specs[i]);
    }
  }
  
  public boolean matches (Class<?> cls){
    for (int i=0; i<mypeSpecs.length; i++){
      if (mypeSpecs[i].matches(cls)){
        return true;
      }
    }
    
    return false;
  }
  
  
  public boolean matches (ClassInfo ci){
    for (int i=0; i<mypeSpecs.length; i++){
      if (mypeSpecs[i].matches(ci)){
        return true;
      }
    }
    
    return false;
  }
}
