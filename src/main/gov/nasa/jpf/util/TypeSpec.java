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
 * wildcard supporting type specification to be used for JPF configuration.
 * This supports supertype spec ('+') and inversion ('-')
 * Examples:
 *   "x.y.Foo" : class x.y.Foo
 *   "+x.y.Foo" : everything that is an instance of x.y.Foo
 *   "x.y.*" : every class that starts with "x.y."
 *   "!x.y.*" : everything that does not start with "x.y."
 */
public class TypeSpec extends FeatureSpec {
  
  
  public static TypeSpec createTypeSpec (String s){
    ParseData d = new ParseData();

    s = s.trim();
    String src = s; // keep the original spec around

    s = parseInversion(s,d);
    parseType(s,d);
    
    try {
      return new TypeSpec(src, d.typeSpec, d.matchInverted);
    } catch (IllegalArgumentException iax){
      return null;
    }
  }
  
  protected TypeSpec (String rawSpec, String cls, boolean inverted){
    super(rawSpec,cls,null,inverted);
  }
  
  @Override
  public boolean matches (Object o){
    if (o instanceof ClassInfo){
      return matches( (ClassInfo) o);
    } else if (o instanceof Class){
      return matches( (Class)o);
    } else {
      return false;
    }
  }
  
  public boolean matches (Class<?> cls){
    return isMatchingType(cls);
  }
  
  public boolean matches (ClassInfo ci){
    return isMatchingType(ci);
  }
  
}
