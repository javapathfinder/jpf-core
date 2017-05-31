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

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

// NOTE - this only works because DecimalFormat is a Format subclass, i.e.
// the java.text.Format native peer will be initialized first
// (otherwise we shouldn't depend on static data of other native peers)

public class JPF_java_text_DecimalFormat extends NativePeer {

  static final int INTEGER_STYLE=0;
  static final int NUMBER_STYLE=1;

  NumberFormat getInstance (MJIEnv env, int objref) {
    Format fmt = JPF_java_text_Format.getInstance(env,objref);
    assert fmt instanceof NumberFormat;
    
    return (NumberFormat)fmt;
  }
  
  /*
   * NOTE: if we would directly intercept the ctors, we would have to
   * explicitly call the superclass ctors here (the 'id' handle gets
   * initialized in the java.text.Format ctor) 
   */
  @MJI
  public void init0____V (MJIEnv env, int objref) {
    DecimalFormat fmt = new DecimalFormat();
    JPF_java_text_Format.putInstance(env,objref,fmt);    
  }
  
  @MJI
  public void init0__Ljava_lang_String_2__V (MJIEnv env, int objref, int patternref) {
    String pattern = env.getStringObject(patternref);
    
    DecimalFormat fmt = new DecimalFormat(pattern);
    JPF_java_text_Format.putInstance(env,objref,fmt);    
  }
  
  @MJI
  public void init0__I__V (MJIEnv env, int objref, int style) {
    NumberFormat fmt = null;
    if (style == INTEGER_STYLE) {
      fmt = NumberFormat.getIntegerInstance();
    } else if (style == NUMBER_STYLE) {
      fmt = NumberFormat.getNumberInstance();
    } else {
      // unknown style
      fmt = new DecimalFormat();
    }
    
    JPF_java_text_Format.putInstance(env,objref,fmt);    
  }
  
  @MJI
  public void setMaximumFractionDigits__I__V (MJIEnv env, int objref, int newValue){
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setMaximumFractionDigits(newValue);
    }
  }

  @MJI
  public void setMaximumIntegerDigits__I__V (MJIEnv env, int objref, int newValue){
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setMaximumIntegerDigits(newValue);
    }
  }

  @MJI
  public void setMinimumFractionDigits__I__V (MJIEnv env, int objref, int newValue){
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setMinimumFractionDigits(newValue);
    }
  }

  @MJI
  public void setMinimumIntegerDigits__I__V (MJIEnv env, int objref, int newValue){
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setMinimumIntegerDigits(newValue);
    }
  }
  
  @MJI
  public int format__J__Ljava_lang_String_2 (MJIEnv env, int objref, long number) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      String s = fmt.format(number);
      int sref = env.newString(s);
      return sref;
    }
    
    return MJIEnv.NULL;
  }
  
  @MJI
  public int format__D__Ljava_lang_String_2 (MJIEnv env, int objref, double number) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      String s = fmt.format(number);
      int sref = env.newString(s);
      return sref;
    }
    
    return MJIEnv.NULL;
  }

  @MJI
  public void setParseIntegerOnly__Z__V(MJIEnv env, int objref, boolean value) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setParseIntegerOnly(value);
    }
  }

  @MJI
  public boolean isParseIntegerOnly____Z(MJIEnv env, int objref) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      return fmt.isParseIntegerOnly();
    }
    return false;
  }

  @MJI
  public void setGroupingUsed__Z__V(MJIEnv env, int objref, boolean newValue) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      fmt.setGroupingUsed(newValue);
    }
  }

  @MJI
  public boolean isGroupingUsed____Z(MJIEnv env, int objref) {
    NumberFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      return fmt.isGroupingUsed();
    }
    return false;
  }

  @MJI
  public int parse__Ljava_lang_String_2Ljava_text_ParsePosition_2__Ljava_lang_Number_2(MJIEnv env, int objref,int sourceRef,int parsePositionRef) {
    String source = env.getStringObject(sourceRef);
    ParsePosition parsePosition = createParsePositionFromRef(env,parsePositionRef);
    NumberFormat fmt = getInstance(env,objref);
    Number number = null;
    if (fmt != null) {
      number = fmt.parse(source,parsePosition);
    }
    updateParsePositionRef(env,parsePositionRef, parsePosition);
    return createNumberRefFromNumber(env,number);
  }

  private static ParsePosition createParsePositionFromRef(MJIEnv env,int parsePositionRef) {
    int index = env.getIntField(parsePositionRef, "index");
    int errorIndex = env.getIntField(parsePositionRef, "errorIndex");
    ParsePosition ps = new ParsePosition(index);
    ps.setErrorIndex(errorIndex);
    return ps;
  }

  private static void updateParsePositionRef(MJIEnv env,int parsePositionRef, ParsePosition parsePosition) {
    env.setIntField(parsePositionRef, "index", parsePosition.getIndex());
    env.setIntField(parsePositionRef, "errorIndex", parsePosition.getErrorIndex());
  }

  private static int createNumberRefFromNumber(MJIEnv env,Number number) {
    int numberRef = MJIEnv.NULL;
    if(number instanceof Double) {
      numberRef = env.newObject("java.lang.Double");
      env.setDoubleField(numberRef, "value", number.doubleValue());
    } else if(number instanceof Long) {
      numberRef = env.newObject("java.lang.Long");
      env.setLongField(numberRef, "value", number.longValue());
    }
    return numberRef;
  }

}
