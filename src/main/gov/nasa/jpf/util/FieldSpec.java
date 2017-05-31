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
import gov.nasa.jpf.vm.FieldInfo;

/**
 * utility class that can match FieldInfos against specs.
 * if the class or name part are omitted, "*" is assumed
 * a preceeding '!' means the match is inverted
 *
 * spec examples
 *   "x.y.Foo.bar  : field 'bar' in class 'x.y.Foo'
 *   "x.y.Foo+.bar : all 'bar' fields in 'x.y.Foo' and all its supertypes
 *   "x.y.Foo.*"   : all fields of x.y.Foo
 *   "*.myData"    : all fields names 'myData'
 *   "!x.y.*"      : all fields of types outside types in package x.y
 */
public class FieldSpec extends FeatureSpec {

  /**
   * factory method that includes the parser
   */
  public static FieldSpec createFieldSpec (String s){
    ParseData d = new ParseData();

    s = s.trim();
    String src = s;

    s = parseInversion(s,d);
    parseTypeAndName(s,d);

    try {
      return new FieldSpec(src, d.typeSpec, d.nameSpec, d.matchInverted);
    } catch (IllegalArgumentException iax){
      return null;
    }
  }


  public FieldSpec (String rawSpec, String cls, String name, boolean inverted){
    super(rawSpec,cls,name,inverted);
  }

  @Override
  public boolean matches (Object feature){
    if (feature instanceof FieldInfo){
      return matches( (FieldInfo) feature);
    } else {
      return false;
    }
  }

  public boolean matches (FieldInfo fi){

    ClassInfo ci = fi.getClassInfo();
    if (isMatchingType(ci)) {
      if (nameSpec.matches(fi.getName()) != matchInverted) {
        return true;
      }
    }

    return matchInverted;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FieldSpec {");
    if (clsSpec != null){
      sb.append("clsSpec:\"");
      sb.append(clsSpec);
      sb.append('"');
    }
    if (nameSpec != null){
      sb.append(",nameSpec:\"");
      sb.append(nameSpec);
      sb.append('"');
    }
    sb.append('}');
    return sb.toString();
  }
}
