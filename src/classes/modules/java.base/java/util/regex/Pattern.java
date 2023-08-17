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
package java.util.regex;

/**
 * simplified model of java.util.refex.Pattern, which otherwise
 * is very expensive in terms of state memory and execution costs
 */
public class Pattern {

  String regex;
  int flags;
  
  public static Pattern compile (String regex) {
    return new Pattern(regex, 0);
  }
  
  public static Pattern compile (String regex, int flags){
    return new Pattern(regex, flags);
  }
  
  private Pattern (String regex, int flags){
    this.regex = regex;
    this.flags = flags;
  }
  
  public Matcher matcher (CharSequence input){
    return new Matcher(this, input);
  }
  
  public String pattern() {
    return regex;
  }
  
  public String[] split (CharSequence input){
    return split(input,0);
  }

  public String[] split (CharSequence input, int limit){
    return split0(input.toString(), limit); // just to avoid the CharSequence charAt() hassle on the native side
  }

  private native String[] split0(String input, int limit);
  
  @Override
  public String toString() {
    return regex;
  }

  public static String quote(String s) {
    int slashEIndex = s.indexOf("\\E");
    if (slashEIndex == -1)
      return "\\Q" + s + "\\E";

    StringBuilder sb = new StringBuilder(s.length() * 2);
    sb.append("\\Q");
    slashEIndex = 0;
    int current = 0;
    while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
      sb.append(s.substring(current, slashEIndex));
      current = slashEIndex + 2;
      sb.append("\\E\\\\E\\Q");
    }
    sb.append(s.substring(current, s.length()));
    sb.append("\\E");
    return sb.toString();
  }
}
