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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.ClassInfo;

/**
 * common base class for MethodSpec and FieldSpec
 */
public abstract class FeatureSpec {

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.util");

  static class ParseData {
    boolean matchInverted;
    boolean matchSuperTypes;
    String typeSpec;
    String nameSpec;
  }

  protected static final char SUB = '+';
  protected static final char INVERTED = '!';


  protected String src;

  // those can be wildcard expressions
  protected StringMatcher  clsSpec;
  protected StringMatcher  nameSpec;

  protected boolean matchInverted;   // matches everything that does NOT conform to the specs
  protected boolean matchSuperTypes; // matches supertypes of the specified one


  protected static String parseInversion (String s, ParseData d){
    if (s.length() > 0){
      if (s.charAt(0) == INVERTED){
        d.matchInverted = true;
        s = s.substring(1).trim();
      }
    }
    return s;
  }

  protected static String parseType (String s, ParseData d){
    d.typeSpec = s;
    return s;
  }
  
  protected static String parseTypeAndName (String s, ParseData d){
    int i = s.lastIndexOf('.'); // beginning of name
    if (i >= 0){
      if (i==0){
        d.typeSpec = "*";
      } else {
        d.typeSpec = s.substring(0, i);
      }

      d.nameSpec = s.substring(i+1);
      if (d.nameSpec.length() == 0){
        d.nameSpec = "*";
      }

    } else { // no name, all fields
      if (s.length() == 0){
        d.typeSpec = "*";
      } else {
        d.typeSpec = s;
      }
      d.nameSpec = "*";
    }

    return s;
  }

  protected FeatureSpec (String rawSpec, String cls, String name, boolean inverted){
    src = rawSpec;
    matchInverted = inverted;

    int l = cls.length()-1;
    if (cls.charAt(l) == SUB){
      cls = cls.substring(0, l);
      matchSuperTypes = true;
    }

    clsSpec = new StringMatcher(cls);
    
    if (name != null){
      nameSpec = new StringMatcher(name);
    }
  }

  public String getSource() {
    return src;
  }

  public StringMatcher getClassSpec() {
    return clsSpec;
  }

  public StringMatcher getNameSpec() {
    return nameSpec;
  }

  public boolean matchSuperTypes() {
    return matchSuperTypes;
  }

  public boolean isMatchingType (Class cls){
    if (clsSpec.matches(cls.getName())){
      return true;
    }
    
    if (matchSuperTypes){
      for (Class c = cls.getSuperclass(); c != null; c = c.getSuperclass()){
        if (clsSpec.matches(c.getName())){
          return true;
        }
      }
    }
    
    for (Class ifc : cls.getInterfaces()){
      if (clsSpec.matches(ifc.getName())) {
        return true;
      }      
    }
    
    return false;
  }
  
  public boolean isMatchingType(ClassInfo ci){
    if (clsSpec.matches(ci.getName())){  // also takes care of '*'
      return true;
    }

    if (matchSuperTypes){
      // check all superclasses
      for (ClassInfo sci = ci.getSuperClass(); sci != null; sci = sci.getSuperClass()){
        if (clsSpec.matches(sci.getName())){
          return true;
        }
      }
    }

    // check interfaces (regardless of 'override' - interfaces make no sense otherwise
    for (ClassInfo ifc : ci.getAllInterfaces()) {
      if (clsSpec.matches(ifc.getName())) {
        return true;
      }
    }

    return false;
  }

  public abstract boolean matches (Object feature);
}
