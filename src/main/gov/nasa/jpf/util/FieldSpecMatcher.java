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

import gov.nasa.jpf.vm.FieldInfo;

/**
 * a matcher for a collection of FieldSpecs
 */
public class FieldSpecMatcher {

  protected FieldSpec[] fieldSpecs;
  
  public static FieldSpecMatcher create (String[] specs){
    if (specs != null && specs.length > 0){
      return new FieldSpecMatcher(specs);
    } else {
      return null;
    }
  }
  
  public FieldSpecMatcher(String[] specs){
    int len = specs.length;
    fieldSpecs = new FieldSpec[len];
    for (int i=0; i<len; i++){
      fieldSpecs[i] = FieldSpec.createFieldSpec(specs[i]);
    }
  }
  
  public boolean matches (FieldInfo fi){
    for (int i=0; i<fieldSpecs.length; i++){
      if (fieldSpecs[i].matches(fi)){
        return true;
      }
    }
    
    return false;
  }
}
