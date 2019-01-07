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

package java.text;

import java.util.Locale;

public abstract class NumberFormat extends Format {

  static final int INTEGER_STYLE=0;
  static final int NUMBER_STYLE=1;
    
  public static NumberFormat getIntegerInstance() {
    return new DecimalFormat(INTEGER_STYLE);
  }
  
  public static NumberFormat getNumberInstance() {
    return new DecimalFormat(NUMBER_STYLE);
  }

  public static NumberFormat getNumberInstance(Locale locale) {
    return new DecimalFormat(NUMBER_STYLE);
  }

  public static NumberFormat getInstance() {
    return new DecimalFormat(NUMBER_STYLE);
  }
  
  public void setMaximumFractionDigits (int newValue){
    // intercepted by native peer
  }
  public void setMaximumIntegerDigits (int newValue){
    // intercepted by native peer
  }
  public void setMinimumFractionDigits(int newValue){
    // intercepted by native peer
  }
  public void setMinimumIntegerDigits(int newValue){
    // intercepted by native peer
  }
  
  public String format (long number) {
    // intercepted by native peer
    return null;
  }
  
  public String format (double d) {
    // intercepted by native peer
    return null;
  }

  @Override
  public final Object parseObject (String source, ParsePosition pos) {
    return parse(source,pos);
  }

  public void setParseIntegerOnly(boolean value) {
      // intercepted by native peer
  }

  public boolean isParseIntegerOnly() {
      // intercepted by native peer
      return false;
  }

  public boolean isGroupingUsed() {
      return false;
  }

  public void setGroupingUsed(boolean newValue) {
    // intercepted by native peer
  }

  public abstract Number parse(String source,ParsePosition pos);

  // ..and probably a lot missing
  
}
