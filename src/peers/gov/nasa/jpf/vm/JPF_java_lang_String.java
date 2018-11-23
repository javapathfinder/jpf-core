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
import gov.nasa.jpf.vm.CharArrayFields;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * MJI NativePeer class for java.lang.String library abstraction
 */
public class JPF_java_lang_String extends NativePeer {
  final static String sioobe = "java.lang.StringIndexOutOfBoundsException";
  final static String sioor = "String index out of range: ";
  
  @MJI
  public int init___3CII__Ljava_lang_String_2 (MJIEnv env, int objRef, int valueRef, int offset, int count) {
    char[] value = env.getCharArrayObject(valueRef);
    String result = new String(value, offset, count);
    return env.newString(result);
  }

  @MJI
  public int init___3III__Ljava_lang_String_2 (MJIEnv env, int objRef, int codePointsRef, int offset, int count) {
    int[] codePoints = env.getIntArrayObject(codePointsRef);
    String result = new String(codePoints, offset, count);
    return env.newString(result);
  }

  @SuppressWarnings("deprecation")
  @MJI
  public int init___3BIII__Ljava_lang_String_2 (MJIEnv env, int objRef, int asciiRef, int hibyte, int offset, int count) {
    byte[] ascii = env.getByteArrayObject(asciiRef);
    String result = new String(ascii, hibyte, offset, count);
    return env.newString(result);
  }

  @MJI
  public int init___3BIILjava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int bytesRef, int offset, int length, int charsetNameRef) throws UnsupportedEncodingException {
    byte[] bytes = env.getByteArrayObject(bytesRef);
    String charsetName = env.getStringObject(charsetNameRef);
    String result = new String(bytes, offset, length, charsetName);
    return env.newString(result);
  }

  @MJI
  public int init___3BII__Ljava_lang_String_2 (MJIEnv env, int objRef, int bytesRef, int offset, int length) {
    byte[] bytes = env.getByteArrayObject(bytesRef);
    String result = new String(bytes, offset, length);
    return env.newString(result);
  }

  @MJI
  public int codePointAt__I__I (MJIEnv env, int objRef, int index) {
    String obj = env.getStringObject(objRef);
    return obj.codePointAt(index);
  }

  @MJI
  public int codePointBefore__I__I (MJIEnv env, int objRef, int index) {
    String obj = env.getStringObject(objRef);
    return obj.codePointBefore(index);
  }

  @MJI
  public int codePointCount__II__I (MJIEnv env, int objRef, int beginIndex, int endIndex) {
    String obj = env.getStringObject(objRef);
    return obj.codePointCount(beginIndex, endIndex);
  }

  @MJI
  public int offsetByCodePoints__II__I (MJIEnv env, int objRef, int index, int codePointOffset) {
    String obj = env.getStringObject(objRef);
    return obj.offsetByCodePoints(index, codePointOffset);
  }

  @MJI
  public void getChars__II_3CI__V (MJIEnv env, int objRef, int srcBegin, int srcEnd, int dstRef, int dstBegin) {
    String obj = env.getStringObject(objRef);
    char[] dst = env.getCharArrayObject(dstRef);
    obj.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  @MJI
  public void getChars___3CI__V(MJIEnv env, int objRef, int dstRef, int dstBegin) {
    String obj = env.getStringObject(objRef);
    char[] dst = env.getCharArrayObject(dstRef);
    obj.getChars(0, obj.length(), dst, dstBegin);
  }
  
  @SuppressWarnings("deprecation")
  @MJI
  public void getBytes__II_3BI__V (MJIEnv env, int objRef, int srcBegin, int srcEnd, int dstRef, int dstBegin) {
    String obj = env.getStringObject(objRef);
    byte[] dst = env.getByteArrayObject(dstRef);
    obj.getBytes(srcBegin, srcEnd, dst, dstBegin);
  }

  @MJI
  public int getBytes__Ljava_lang_String_2___3B (MJIEnv env, int objRef, int charSetRef) {
    String string = env.getStringObject(objRef);
    String charset = env.getStringObject(charSetRef);

    try {
      byte[] b = string.getBytes(charset);
      return env.newByteArray(b);

    } catch (UnsupportedEncodingException uex) {
      env.throwException(uex.getClass().getName(), uex.getMessage());
      return MJIEnv.NULL;
    }
  }

  @MJI
  public int getBytes_____3B (MJIEnv env, int objRef) {
    String obj = env.getStringObject(objRef);
    byte[] bytes = obj.getBytes();
    return env.newByteArray(bytes);
  }

  @MJI
  public char charAt__I__C (MJIEnv env, int objRef, int index){
    char[] data = env.getStringChars(objRef);
    if (index >= 0 && index < data.length) {
      return data[index];
    }
    env.throwException(sioobe, sioor + index);
    return '\0';
  }

  
  @MJI
  public boolean equals0___3C_3CI__Z (MJIEnv env, int clsObjRef, int charsRef1, int charsRef2, int len) {

    if ((charsRef1 == MJIEnv.NULL) || (charsRef2 == MJIEnv.NULL)) { return false; }

    char[] a = env.getCharArrayObject(charsRef1);
    char[] b = env.getCharArrayObject(charsRef2);

    if (a.length < len || b.length < len) { return false; }

    for (int i = 0; i < len; i++) {
      if (a[i] != b[i]) { return false; }
    }

    return true;
  }

  @MJI
  public boolean equals__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int argRef) {
    if (argRef == MJIEnv.NULL) { 
      return false; 
    }

    Heap heap = env.getHeap();
    ElementInfo s1 = heap.get(objRef);
    ClassInfo ci1 = s1.getClassInfo();
    
    ElementInfo s2 = heap.get(argRef);
    ClassInfo ci2 = s2.getClassInfo();
   
    if (!ci2.isInstanceOf(ci1)) { 
      return false;
    }

    Fields f1 = heap.get(s1.getReferenceField("value")).getFields();
    Fields f2 = heap.get(s2.getReferenceField("value")).getFields();

    char[] c1 = ((CharArrayFields) f1).asCharArray();
    char[] c2 = ((CharArrayFields) f2).asCharArray();

    if (c1.length != c2.length) { 
      return false; 
    }

    for (int i = 0; i < c1.length; i++) {
      if (c1[i] != c2[i]) { 
        return false; 
      }
    }

    return true;
  }

  @MJI
  public boolean equalsIgnoreCase__Ljava_lang_String_2__Z (MJIEnv env, int objref, int anotherString) {
    String thisString = env.getStringObject(objref);
    if (anotherString != MJIEnv.NULL) {
      return thisString.equalsIgnoreCase(env.getStringObject(anotherString));
    } else {
      return false;
    }
  }

  @MJI
  public int compareTo__Ljava_lang_String_2__I (MJIEnv env, int objRef, int anotherStringRef) {
    String obj = env.getStringObject(objRef);
    String anotherString = env.getStringObject(anotherStringRef);
    return obj.compareTo(anotherString);
  }

  @MJI
  public int MJIcompare__Ljava_lang_String_2Ljava_lang_String_2__I (MJIEnv env, int clsRef, int s1Ref, int s2Ref) {
    // Is there a way to reflect?
    String a = env.getStringObject(s1Ref);
    String s2 = env.getStringObject(s2Ref);
    int n1 = a.length();
    int n2 = s2.length();
    int min = Math.min(n1, n2);
    for (int i = 0; i < min; i++) {
      char x = a.charAt(i);
      char y = s2.charAt(i);
      if (x != y) {
        x = Character.toUpperCase(x);
        y = Character.toUpperCase(y);
        if (x != y) {
          x = Character.toLowerCase(x);
          y = Character.toLowerCase(y);
          if (x != y) { return x - y; }
        }
      }
    }
    return n1 - n2;
  }

  @MJI
  public boolean regionMatches__ILjava_lang_String_2II__Z (MJIEnv env, int objRef, int toffset, int otherRef, int ooffset, int len) {
    String obj = env.getStringObject(objRef);
    String other = env.getStringObject(otherRef);
    return obj.regionMatches(toffset, other, ooffset, len);

  }

  @MJI
  public boolean regionMatches__ZILjava_lang_String_2II__Z (MJIEnv env, int objRef, boolean ignoreCase, int toffset, int otherRef, int ooffset, int len) {
    String obj = env.getStringObject(objRef);
    String other = env.getStringObject(otherRef);
    return obj.regionMatches(ignoreCase, toffset, other, ooffset, len);

  }

  @MJI
  public boolean startsWith__Ljava_lang_String_2I__Z (MJIEnv env, int objRef, int prefixRef, int toffset) {
    String thisStr = env.getStringObject(objRef);
    String prefix = env.getStringObject(prefixRef);
    return thisStr.startsWith(prefix, toffset);
  }

  @MJI
  public boolean startsWith__Ljava_lang_String_2__Z (MJIEnv env, int objRef, int prefixRef) {
    String thisStr = env.getStringObject(objRef);
    String prefix = env.getStringObject(prefixRef);
    return thisStr.startsWith(prefix);
  }

  @MJI
  public int hashCode____I (MJIEnv env, int objref) {
    return computeStringHashCode(env, objref);
  }

  public static int computeStringHashCode(MJIEnv env, int objref) {
    ElementInfo ei = env.getElementInfo(objref);
    int h = ei.getIntField("hash");

    if (h == 0) {
      int vref = env.getReferenceField(objref, "value");

      // now get the char array data, but be aware they are stored as ints
      ElementInfo eiVal = env.getElementInfo(vref);
      char[] values = eiVal.asCharArray();

      for (int i = 0; i < values.length; i++) {
        h = 31 * h + values[i];
      }

      ei = ei.getModifiableInstance();
      ei.setIntField("hash", h);
    }

    return h;
  }

  @MJI
  public int indexOf__I__I (MJIEnv env, int objref, int c) {
    return indexOf__II__I(env, objref, c, 0);
  }

  @MJI
  public int indexOf__II__I (MJIEnv env, int objref, int c, int fromIndex) {
    int vref = env.getReferenceField(objref, "value");
    ElementInfo ei = env.getElementInfo(vref);
    char[] values = ((CharArrayFields) ei.getFields()).asCharArray();

    int len = values.length;

    if (fromIndex >= len) { return -1; }
    if (fromIndex < 0) {
      fromIndex = 0;
    }

    for (int i = fromIndex; i < len; i++) {
      if (values[i] == c) { return i; }
    }

    return -1;
  }

  @MJI
  public int lastIndexOf__I__I (MJIEnv env, int objref, int c) {
    return lastIndexOf__II__I(env, objref, c, Integer.MAX_VALUE);
  }

  @MJI
  public int lastIndexOf__II__I (MJIEnv env, int objref, int c, int fromIndex) {
    int vref = env.getReferenceField(objref, "value");
    ElementInfo ei = env.getElementInfo(vref);
    char[] values = ((CharArrayFields) ei.getFields()).asCharArray();

    int len = values.length;

    if (fromIndex < 0) { return -1; }
    if (fromIndex > len - 1) {
      fromIndex = len - 1;
    }

    for (int i = fromIndex; i > 0; i--) {
      if (values[i] == c) { return i; }
    }

    return -1;
  }

  @MJI
  public int indexOf__Ljava_lang_String_2__I (MJIEnv env, int objref, int str) {
    String thisStr = env.getStringObject(objref);
    String indexStr = env.getStringObject(str);

    return thisStr.indexOf(indexStr);
  }

  @MJI
  public int indexOf__Ljava_lang_String_2I__I (MJIEnv env, int objref, int str, int fromIndex) {
    String thisStr = env.getStringObject(objref);
    String indexStr = env.getStringObject(str);

    return thisStr.indexOf(indexStr, fromIndex);
  }

  @MJI
  public int lastIndexOf__Ljava_lang_String_2I__I (MJIEnv env, int objref, int str, int fromIndex) {
    String thisStr = env.getStringObject(objref);
    String indexStr = env.getStringObject(str);

    return thisStr.lastIndexOf(indexStr, fromIndex);
  }

  @MJI
  public int substring__I__Ljava_lang_String_2 (MJIEnv env, int objRef, int beginIndex) {
    String obj = env.getStringObject(objRef);
    return substring__II__Ljava_lang_String_2(env, objRef, beginIndex, obj.length());
  }

  @MJI
  public int substring__II__Ljava_lang_String_2 (MJIEnv env, int objRef, int beginIndex, int endIndex) {
    if (beginIndex > endIndex) {
      env.throwException(sioobe, sioor + (endIndex - beginIndex));
      return 0;
    }
    if (beginIndex < 0) {
      env.throwException(sioobe, sioor + beginIndex);
      return 0;
    }
    String obj = env.getStringObject(objRef);
    if (endIndex > obj.length()) {
      env.throwException(sioobe, sioor + endIndex);
      return 0;
    }
    String result = obj.substring(beginIndex, endIndex);
    return env.newString(result);

  }

  @MJI
  public int concat__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int strRef) {
    Heap heap = env.getHeap();

    ElementInfo thisStr = heap.get(objRef);
    CharArrayFields thisFields = (CharArrayFields) heap.get(thisStr.getReferenceField("value")).getFields();
    char[] thisChars = thisFields.asCharArray();
    int thisLength = thisChars.length;

    ElementInfo otherStr = heap.get(strRef);
    CharArrayFields otherFields = (CharArrayFields) heap.get(otherStr.getReferenceField("value")).getFields();
    char[] otherChars = otherFields.asCharArray();
    int otherLength = otherChars.length;

    if (otherLength == 0) { return objRef; }

    char resultChars[] = new char[thisLength + otherLength];
    System.arraycopy(thisChars, 0, resultChars, 0, thisLength);
    System.arraycopy(otherChars, 0, resultChars, thisLength, otherLength);

    return env.newString(new String(resultChars));
  }

  // --- the various replaces

  @MJI
  public int replace__CC__Ljava_lang_String_2 (MJIEnv env, int objRef, char oldChar, char newChar) {

    if (oldChar == newChar) { // nothing to replace
      return objRef;
    }

    int vref = env.getReferenceField(objRef, "value");
    ElementInfo ei = env.getModifiableElementInfo(vref);
    char[] values = ((CharArrayFields) ei.getFields()).asCharArray();
    int len = values.length;

    char[] newValues = null;

    for (int i = 0, j = 0; j < len; i++, j++) {
      char c = values[i];
      if (c == oldChar) {
        if (newValues == null) {
          newValues = new char[len];
          if (j > 0) {
            System.arraycopy(values, 0, newValues, 0, j);
          }
        }
        newValues[j] = newChar;
      } else {
        if (newValues != null) {
          newValues[j] = c;
        }
      }
    }

    if (newValues != null) {
      String s = new String(newValues);
      return env.newString(s);

    } else { // oldChar not found, return the original string
      return objRef;
    }
  }

  @MJI
  public boolean matches__Ljava_lang_String_2__Z (MJIEnv env, int objRef, int regexRef) {
    String s = env.getStringObject(objRef);
    String r = env.getStringObject(regexRef);

    return s.matches(r);
  }

  @MJI
  public int replaceFirst__Ljava_lang_String_2Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int regexRef, int replacementRef) {
    String thisStr = env.getStringObject(objRef);
    String regexStr = env.getStringObject(regexRef);
    String replacementStr = env.getStringObject(replacementRef);

    String result = thisStr.replaceFirst(regexStr, replacementStr);
    return (result != thisStr) ? env.newString(result) : objRef;
  }

  @MJI
  public int replaceAll__Ljava_lang_String_2Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int regexRef, int replacementRef) {
    String thisStr = env.getStringObject(objRef);
    String regexStr = env.getStringObject(regexRef);
    String replacementStr = env.getStringObject(replacementRef);

    String result = thisStr.replaceAll(regexStr, replacementStr);
    return (result != thisStr) ? env.newString(result) : objRef;
  }

  @MJI
  public int split__Ljava_lang_String_2I___3Ljava_lang_String_2 (MJIEnv env, int clsObjRef, int strRef, int limit) {
    String s = env.getStringObject(strRef);
    String obj = env.getStringObject(clsObjRef);

    String[] result = obj.split(s, limit);

    return env.newStringArray(result);
  }

  @MJI
  public int split__Ljava_lang_String_2___3Ljava_lang_String_2 (MJIEnv env, int clsObjRef, int strRef) {
    String s = env.getStringObject(strRef);
    String obj = env.getStringObject(clsObjRef);

    String[] result = obj.split(s);

    return env.newStringArray(result);
  }

  @MJI
  public int toLowerCase__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int locRef) {
    String s = env.getStringObject(objRef);
    Locale loc = JPF_java_util_Locale.getLocale(env, locRef);

    String lower = s.toLowerCase(loc);

    return (s == lower) ? objRef : env.newString(lower);
  }

  @MJI
  public int toLowerCase____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    String s = env.getStringObject(objRef);
    String lower = s.toLowerCase();

    return (s == lower) ? objRef : env.newString(lower);
  }

  @MJI
  public int toUpperCase__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int locRef) {
    String s = env.getStringObject(objRef);
    Locale loc = JPF_java_util_Locale.getLocale(env, locRef);

    String upper = s.toUpperCase(loc);

    return (s == upper) ? objRef : env.newString(upper);
  }

  @MJI
  public int toUpperCase____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    String s = env.getStringObject(objRef);
    String upper = s.toUpperCase();

    return (s == upper) ? objRef : env.newString(upper);
  }

  @MJI
  public int trim____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    Heap heap = env.getHeap();
    ElementInfo thisStr = heap.get(objRef);

    CharArrayFields thisFields = (CharArrayFields) heap.get(thisStr.getReferenceField("value")).getFields();
    char[] thisChars = thisFields.asCharArray();
    int thisLength = thisChars.length;

    int start = 0;
    int end = thisLength;

    while ((start < end) && (thisChars[start] <= ' ')) {
      start++;
    }

    while ((start < end) && (thisChars[end - 1] <= ' ')) {
      end--;
    }

    if (start == 0 && end == thisLength) {
      // if there was no white space, return the string itself
      return objRef;
    }

    String result = new String(thisChars, start, end - start);
    return env.newString(result);
  }

  @MJI
  public int toCharArray_____3C (MJIEnv env, int objref) {
    int vref = env.getReferenceField(objref, "value");
    char[] v = env.getCharArrayObject(vref);

    int cref = env.newCharArray(v);

    return cref;
  }

  @MJI
  public int format__Ljava_lang_String_2_3Ljava_lang_Object_2__Ljava_lang_String_2 (MJIEnv env, int clsObjRef, int fmtRef, int argRef) {
    return env.newString(env.format(fmtRef, argRef));
  }

  @MJI
  public int format__Ljava_util_Locale_2Ljava_lang_String_2_3Ljava_lang_Object_2__Ljava_lang_String_2 (MJIEnv env, int clsObjRef, int locRef, int fmtRef, int argRef) {
    Locale loc = JPF_java_util_Locale.getLocale(env, locRef);
    return env.newString(env.format(loc, fmtRef, argRef));
  }

  @MJI
  public int intern____Ljava_lang_String_2 (MJIEnv env, int robj) {
    // <2do> Replace this with a JPF space HashSet once we have a String model
    Heap heap = env.getHeap();

    String s = env.getStringObject(robj);
    ElementInfo ei = heap.newInternString(s, env.getThreadInfo());

    return ei.getObjectRef();
  }

  @MJI
  public int valueOf__I__Ljava_lang_String_2 (MJIEnv env, int clsref, int i) {
    String result = String.valueOf(i);
    return env.newString(result);
  }

  @MJI
  public int valueOf__J__Ljava_lang_String_2 (MJIEnv env, int clsref, long l) {
    String result = String.valueOf(l);
    return env.newString(result);
  }

  @MJI
  public int valueOf__F__Ljava_lang_String_2 (MJIEnv env, int clsref, float f) {
    String result = String.valueOf(f);
    return env.newString(result);
  }

  @MJI
  public int valueOf__D__Ljava_lang_String_2 (MJIEnv env, int clsref, double d) {
    String result = String.valueOf(d);
    return env.newString(result);
  }
}
