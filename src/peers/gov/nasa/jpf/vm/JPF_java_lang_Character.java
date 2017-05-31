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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * MJI NativePeer class for java.lang.Character library abstraction
 * Whoever is using this seriously is definitely screwed, performance-wise
 */
public class JPF_java_lang_Character extends NativePeer {
  // <2do> at this point we deliberately do not override clinit

  @MJI
  public boolean isDefined__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isDefined(c);
  }

  @MJI
  public boolean isDigit__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isDigit(c);
  }

  @MJI
  public boolean isISOControl__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isISOControl(c);
  }

  @MJI
  public boolean isIdentifierIgnorable__C__Z (MJIEnv env, int clsObjRef, 
                                                  char c) {
    return Character.isIdentifierIgnorable(c);
  }

  @MJI
  public boolean isJavaIdentifierPart__C__Z (MJIEnv env, int clsObjRef, 
                                                 char c) {
    return Character.isJavaIdentifierPart(c);
  }

  @MJI
  public boolean isJavaIdentifierStart__C__Z (MJIEnv env, int clsObjRef, 
                                                  char c) {
    return Character.isJavaIdentifierStart(c);
  }

  @MJI
  public boolean isJavaLetterOrDigit__C__Z (MJIEnv env, int clsObjRef, 
                                                char c) {
    return Character.isJavaIdentifierPart(c);
  }

  @MJI
  public boolean isJavaLetter__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isJavaIdentifierStart(c);
  }

  @MJI
  public boolean isLetterOrDigit__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isLetterOrDigit(c);
  }

  @MJI
  public boolean isLetter__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isLetter(c);
  }

  @MJI
  public boolean isLowerCase__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isLowerCase(c);
  }

  @MJI
  public int getNumericValue__C__I (MJIEnv env, int clsObjRef, char c) {
    return Character.getNumericValue(c);
  }

  @MJI
  public boolean isSpaceChar__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isSpaceChar(c);
  }

  @MJI
  public boolean isSpace__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isWhitespace(c);
  }

  @MJI
  public boolean isTitleCase__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isTitleCase(c);
  }

  @MJI
  public int getType__C__I (MJIEnv env, int clsObjRef, char c) {
    return Character.getType(c);
  }

  @MJI
  public boolean isUnicodeIdentifierPart__C__Z (MJIEnv env, int clsObjRef, 
                                                    char c) {
    return Character.isUnicodeIdentifierPart(c);
  }

  @MJI
  public boolean isUnicodeIdentifierStart__C__Z (MJIEnv env, int clsObjRef, 
                                                     char c) {
    return Character.isUnicodeIdentifierStart(c);
  }

  @MJI
  public boolean isUpperCase__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isUpperCase(c);
  }

  @MJI
  public boolean isWhitespace__C__Z (MJIEnv env, int clsObjRef, char c) {
    return Character.isWhitespace(c);
  }

  // pcm - we keep this in here to avoid the potentially expensive
  // real clinit. This has changed a lot in Java 1.4.2 (deferred init, i.e.
  // we could actually use it now), but in <= 1.4.1 it executes some
  // 200,000 insns, and some people who didn't grew up with Java might
  // deduce that JPF is hanging. It's not, it just shows why a real VM has to
  // be fast.
  // It is actually Ok to bypass the real clinit if we turn all the
  // important methods into native ones, i.e. delegate to the real thing.
  @MJI
  public void $clinit____V (MJIEnv env, int clsObjRef) {
    env.setStaticByteField("java.lang.Character", "UNASSIGNED", (byte) 0);
    env.setStaticByteField("java.lang.Character", "UPPERCASE_LETTER", (byte) 1);
    env.setStaticByteField("java.lang.Character", "LOWERCASE_LETTER", (byte) 2);
    env.setStaticByteField("java.lang.Character", "TITLECASE_LETTER", (byte) 3);
    env.setStaticByteField("java.lang.Character", "MODIFIER_LETTER", (byte) 4);
    env.setStaticByteField("java.lang.Character", "OTHER_LETTER", (byte) 5);
    env.setStaticByteField("java.lang.Character", "NON_SPACING_MARK", (byte) 6);
    env.setStaticByteField("java.lang.Character", "ENCLOSING_MARK", (byte) 7);
    env.setStaticByteField("java.lang.Character", "COMBINING_SPACING_MARK", (byte) 8);
    env.setStaticByteField("java.lang.Character", "DECIMAL_DIGIT_NUMBER", (byte) 9);
    env.setStaticByteField("java.lang.Character", "LETTER_NUMBER", (byte) 10);
    env.setStaticByteField("java.lang.Character", "OTHER_NUMBER", (byte) 11);
    env.setStaticByteField("java.lang.Character", "SPACE_SEPARATOR", (byte) 12);
    env.setStaticByteField("java.lang.Character", "LINE_SEPARATOR", (byte) 13);
    env.setStaticByteField("java.lang.Character", "PARAGRAPH_SEPARATOR", (byte) 14);
    env.setStaticByteField("java.lang.Character", "CONTROL", (byte) 15);
    env.setStaticByteField("java.lang.Character", "FORMAT", (byte) 16);
    env.setStaticByteField("java.lang.Character", "PRIVATE_USE", (byte) 18);
    env.setStaticByteField("java.lang.Character", "SURROGATE", (byte) 19);
    env.setStaticByteField("java.lang.Character", "DASH_PUNCTUATION", (byte) 20);
    env.setStaticByteField("java.lang.Character", "START_PUNCTUATION", (byte) 21);
    env.setStaticByteField("java.lang.Character", "END_PUNCTUATION", (byte) 22);
    env.setStaticByteField("java.lang.Character", "CONNECTOR_PUNCTUATION", (byte) 23);
    env.setStaticByteField("java.lang.Character", "OTHER_PUNCTUATION", (byte) 24);
    env.setStaticByteField("java.lang.Character", "MATH_SYMBOL", (byte) 25);
    env.setStaticByteField("java.lang.Character", "CURRENCY_SYMBOL", (byte) 26);
    env.setStaticByteField("java.lang.Character", "MODIFIER_SYMBOL", (byte) 27);
    env.setStaticByteField("java.lang.Character", "OTHER_SYMBOL", (byte) 28);
    env.setStaticIntField("java.lang.Character", "MIN_RADIX", 2);
    env.setStaticIntField("java.lang.Character", "MAX_RADIX", 36);
    env.setStaticCharField("java.lang.Character", "MIN_VALUE", '\u0000');
    env.setStaticCharField("java.lang.Character", "MAX_VALUE", '\uffff');

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("char");
    env.setStaticReferenceField("java.lang.Character", "TYPE", 
                             ci.getClassObjectRef());
  }

  @MJI
  public int digit__CI__I (MJIEnv env, int clsObjRef, char c, int radix) {
    return Character.digit(c, radix);
  }

  @MJI
  public char forDigit__II__C (MJIEnv env, int clsObjRef, int digit, 
                                   int radix) {
    return Character.forDigit(digit, radix);
  }

  @MJI
  public char toLowerCase__C__C (MJIEnv env, int clsObjRef, char c) {
    return Character.toLowerCase(c);
  }

  @MJI
  public char toTitleCase__C__C (MJIEnv env, int clsObjRef, char c) {
    return Character.toTitleCase(c);
  }

  @MJI
  public char toUpperCase__C__C (MJIEnv env, int clsObjRef, char c) {
    return Character.toUpperCase(c);
  }

  @MJI
  public int valueOf__C__Ljava_lang_Character_2 (MJIEnv env, int clsRef, char val) {
    return env.valueOfCharacter(val);
  }
}
