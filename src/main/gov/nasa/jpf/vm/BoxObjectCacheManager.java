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

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 *         Cache management implementation for the types Boolean, Byte,
 *         Character, Short, Integer, Long. The references to the caches are in
 *         the class classes/gov/nasa/jpf/BoxObjectCaches.
 * 
 *         All the caches, except Boolean, are initialized on the first
 *         invocation of valueOf(), and they all exempt from garbage collection.
 * 
 *         NOTE: All classes obtained from getResolvedClassInfo in
 *         BoxObjectCacheManager are safe, and there is no need to check if they
 *         are initialized. The wrappers and BoxObjectCaches are initialized in
 *         VM.intialize(), and there are no clinit for array classes.
 *         
 *         NOTE: the initXCache allocations are system allocations, whereas the
 *         valueOfX() allocations are rooted in SUT code
 */
public class BoxObjectCacheManager {
  private static final String MODEL_CLASS = "gov.nasa.jpf.BoxObjectCaches";
  private static final int ANCHOR = BoxObjectCacheManager.class.getName().hashCode();  
  
  // cache default bounds
  private static int defLow = -128;
  private static int defHigh = 127;

  public static int valueOfBoolean (ThreadInfo ti, boolean b) {
    ClassInfo cls = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Boolean");

    int boolObj;
    if (b) {
      boolObj = cls.getStaticElementInfo().getReferenceField("TRUE");
    } else {
      boolObj = cls.getStaticElementInfo().getReferenceField("FALSE");
    }

    return boolObj;
  }

  // Byte cache bounds
  private static byte byteLow;
  private static byte byteHigh;

  public static int initByteCache (ThreadInfo ti) {
    byteLow = (byte) ti.getVM().getConfig().getInt("vm.cache.low_byte", defLow);
    byteHigh = (byte) ti.getVM().getConfig().getInt("vm.cache.high_byte", defHigh);
    int n = (byteHigh - byteLow) + 1;
    
    Heap heap = ti.getHeap();
    ElementInfo eiArray = heap.newSystemArray("Ljava/lang/Byte", n, ti, ANCHOR);
    int arrayRef = eiArray.getObjectRef();

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Byte");
    byte val = byteLow;
    for (int i = 0; i < n; i++) {
      ElementInfo eiByte = heap.newSystemObject(ci, ti, ANCHOR);
      eiByte.setByteField("value", val++);
      eiArray.setReferenceElement(i, eiByte.getObjectRef());
    }

    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    cacheClass.getModifiableStaticElementInfo().setReferenceField("byteCache", arrayRef);
    return arrayRef;
  }

  public static int valueOfByte (ThreadInfo ti, byte b) {
    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    int byteCache = cacheClass.getStaticElementInfo().getReferenceField("byteCache");

    if (byteCache == MJIEnv.NULL) { // initializing the cache on demand
      byteCache = initByteCache(ti);
    }

    if (b >= byteLow && b <= byteHigh) { return ti.getElementInfo(byteCache).getReferenceElement(b - byteLow); }

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Byte");
    ElementInfo eiByte = ti.getHeap().newObject(ci, ti);
    eiByte.setByteField("value", b);
    return eiByte.getObjectRef();
  }

  // Character cache bound
  private static int charHigh;

  public static int initCharCache (ThreadInfo ti) {
    charHigh = ti.getVM().getConfig().getInt("vm.cache.high_char", defHigh);
    int n = charHigh + 1;
    
    Heap heap = ti.getHeap();    
    ElementInfo eiArray = heap.newSystemArray("Ljava/lang/Character", n, ti, ANCHOR);
    int arrayRef = eiArray.getObjectRef();

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Character");
    for (int i = 0; i < n; i++) {
      ElementInfo eiChar = heap.newSystemObject(ci, ti, ANCHOR);
      eiChar.setCharField("value", (char) i);
      eiArray.setReferenceElement(i, eiChar.getObjectRef());
    }

    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    cacheClass.getModifiableStaticElementInfo().setReferenceField("charCache", arrayRef);
    return arrayRef;
  }

  public static int valueOfCharacter (ThreadInfo ti, char c) {
    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    int charCache = cacheClass.getStaticElementInfo().getReferenceField("charCache");

    if (charCache == MJIEnv.NULL) { // initializing the cache on demand
      charCache = initCharCache(ti);
    }

    if (c >= 0 && c <= charHigh) { return ti.getElementInfo(charCache).getReferenceElement(c); }

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Character");
    ElementInfo eiChar = ti.getHeap().newObject(ci, ti);
    eiChar.setCharField("value", c);
    return eiChar.getObjectRef();
  }

  // Short cache bounds
  private static short shortLow;

  private static short shortHigh;

  public static int initShortCache (ThreadInfo ti) {
    shortLow = (short) ti.getVM().getConfig().getInt("vm.cache.low_short", defLow);
    shortHigh = (short) ti.getVM().getConfig().getInt("vm.cache.high_short", defHigh);
    int n = (shortHigh - shortLow) + 1;
    
    Heap heap = ti.getHeap();    
    ElementInfo eiArray = heap.newSystemArray("Ljava/lang/Short", n, ti, ANCHOR);
    int arrayRef = eiArray.getObjectRef();

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Short");
    short val = shortLow;
    for (int i = 0; i < n; i++) {
      ElementInfo eiShort = heap.newSystemObject(ci, ti, ANCHOR);
      eiShort.setShortField("value", val++);
      eiArray.setReferenceElement(i, eiShort.getObjectRef());
    }

    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    cacheClass.getModifiableStaticElementInfo().setReferenceField("shortCache", arrayRef);
    return arrayRef;
  }

  public static int valueOfShort (ThreadInfo ti, short s) {
    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    int shortCache = cacheClass.getStaticElementInfo().getReferenceField("shortCache");

    if (shortCache == MJIEnv.NULL) { // initializing the cache on demand
      shortCache = initShortCache(ti);
    }

    if (s >= shortLow && s <= shortHigh) { return ti.getElementInfo(shortCache).getReferenceElement(s - shortLow); }

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Short");
    ElementInfo eiShort = ti.getHeap().newObject(ci, ti);
    eiShort.setShortField("value", s);
    return eiShort.getObjectRef();
  }

  // Integer cache bounds
  private static int intLow;
  private static int intHigh;

  public static int initIntCache (ThreadInfo ti) {
    intLow = ti.getVM().getConfig().getInt("vm.cache.low_int", defLow);
    intHigh = ti.getVM().getConfig().getInt("vm.cache.high_int", defHigh);
    int n = (intHigh - intLow) + 1;
    
    Heap heap = ti.getHeap();    
    ElementInfo eiArray = heap.newSystemArray("Ljava/lang/Integer", n, ti, ANCHOR);
    int arrayRef = eiArray.getObjectRef();

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Integer");
    for (int i = 0; i < n; i++) {
      ElementInfo eiInteger = heap.newSystemObject(ci, ti, ANCHOR);
      eiInteger.setIntField("value", i + intLow);
      eiArray.setReferenceElement(i, eiInteger.getObjectRef());
    }

    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    cacheClass.getModifiableStaticElementInfo().setReferenceField("intCache", arrayRef);
    return arrayRef;
  }

  public static int valueOfInteger (ThreadInfo ti, int i) {
    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    int intCache = cacheClass.getStaticElementInfo().getReferenceField("intCache");

    if (intCache == MJIEnv.NULL) { // initializing the cache on demand
      intCache = initIntCache(ti);
    }

    if (i >= intLow && i <= intHigh) { return ti.getElementInfo(intCache).getReferenceElement(i - intLow); }

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Integer");
    ElementInfo eiInteger = ti.getHeap().newObject(ci, ti);
    eiInteger.setIntField("value", i);
    return eiInteger.getObjectRef();
  }

  // Long cache bounds
  private static int longLow;
  private static int longHigh;

  public static int initLongCache (ThreadInfo ti) {
    longLow = ti.getVM().getConfig().getInt("vm.cache.low_long", defLow);
    longHigh = ti.getVM().getConfig().getInt("vm.cache.high_long", defHigh);
    int n = (longHigh - longLow) + 1;
    
    Heap heap = ti.getHeap();    
    ElementInfo eiArray = heap.newSystemArray("Ljava/lang/Long", n, ti, ANCHOR);
    int arrayRef = eiArray.getObjectRef();

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Long");
    for (int i = 0; i < n; i++) {
      ElementInfo eiLong = heap.newSystemObject(ci, ti, ANCHOR);
      eiLong.setLongField("value", i + longLow);
      eiArray.setReferenceElement(i, eiLong.getObjectRef());
    }

    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    cacheClass.getModifiableStaticElementInfo().setReferenceField("longCache", arrayRef);
    return arrayRef;
  }

  public static int valueOfLong (ThreadInfo ti, long l) {
    ClassInfo cacheClass = ClassLoaderInfo.getSystemResolvedClassInfo(MODEL_CLASS);
    int longCache = cacheClass.getStaticElementInfo().getReferenceField("longCache");

    if (longCache == MJIEnv.NULL) { // initializing the cache on demand
      longCache = initLongCache(ti);
    }

    if (l >= longLow && l <= longHigh) { return ti.getElementInfo(longCache).getReferenceElement((int) l - longLow); }

    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Long");
    ElementInfo eiLong = ti.getHeap().newObject(ci, ti);
    eiLong.setLongField("value", l);
    return eiLong.getObjectRef();
  }
}
