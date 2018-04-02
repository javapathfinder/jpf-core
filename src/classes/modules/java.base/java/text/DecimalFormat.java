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

/**
 * <2do> this class is a Q&D version to get some support for NumberFormats -
 * something we aren't too interested in yet. The model side has to be extended
 * to include all the delegation, but the real work should happen on the
 * peer side by forwarding to the real classes
 */

public class DecimalFormat extends NumberFormat implements Cloneable{
  private static final long serialVersionUID = 1L;

  /*
   * NOTE: if we would directly intercept the ctors, we would have to
   * explicitly call the superclass ctors from the native peer
   * (the 'id' handle gets initialized in the java.text.Format ctor) 
   */
  private native void init0();
  private native void init0(String pattern);
  private native void init0(int style);
  
  public DecimalFormat () {
    init0();
  }
  
  public DecimalFormat (String pattern) {
    init0(pattern);
  }
  
  public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
    // <2do> that's incomplete - has to delegate to the DecimalFormatSymbols
    // object, but should do so here in the model not the peer
    this(pattern);
  }
  
  DecimalFormat (int style) {
    init0(style);
  }
  // intercepted by native peer
  
  @Override
  public StringBuffer format (Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    // works for use case where pos = new FieldPosition(0)
    toAppendTo.append(obj.toString());
    return toAppendTo;
  }

  @Override
  public Number parse(String source, ParsePosition pos){
    // interceptted by native peer
    return null;
  }

  @Override
  public void setMaximumFractionDigits (int newValue){
    // intercepted by native peer
  }
  @Override
  public void setMaximumIntegerDigits (int newValue){
    // intercepted by native peer
  }
  @Override
  public void setMinimumFractionDigits(int newValue){
    // intercepted by native peer
  }
  @Override
  public void setMinimumIntegerDigits(int newValue){
    // intercepted by native peer
  }
  
  @Override
  public String format (long number) {
    // intercepted by native peer
    return null;
  }
  
  @Override
  public String format (double number) {
    // intercepted by native peer
    return null;
  }

  public DecimalFormatSymbols getDecimalFormatSymbols() {
      return new DecimalFormatSymbols();
  }

  public String getPositivePrefix() {
      return "";
  }

  public String getNegativePrefix() {
      return "-";
  }

  public String getPositiveSuffix() {
      return "";
  }

  public String getNegativeSuffix() {
      return "";
  }

  @Override
  public boolean isGroupingUsed() {
      return false;
  }

  @Override
  public void setGroupingUsed(boolean newValue) {
    // intercepted by native peer
  }

  @Override
  public boolean isParseIntegerOnly() {
      return false;
  }

  @Override
  public void setParseIntegerOnly(boolean value) {
      // intercepted by native peer
  }
  // and probably a lot missing
}
