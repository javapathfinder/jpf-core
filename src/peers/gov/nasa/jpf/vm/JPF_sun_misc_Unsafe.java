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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * we don't want this class! This is a hodgepodge of stuff that shouldn't be in Java, but
 * is handy for some hacks. The reason we have it here - very rudimentary - is that
 * java.util.concurrent makes use of the atomic compare&swap which is in it.
 * The choice was to duplicate a lot of relatively difficult code in the "right" class
 * (java.util.concurrent.locks.AbstractQueuedSynchronizer) or a small amount of straight forward
 * code in the "wrong" class (sun.misc.Unsafe). Knowing a bit about the "library chase" game,
 * we opt for the latter
 *
 * <2do> this might change with better modeling of high level java.util.concurrent constructs
 */
public class JPF_sun_misc_Unsafe extends NativePeer {

  @MJI
  public int getUnsafe____Lsun_misc_Unsafe_2 (MJIEnv env, int clsRef) {
    int objRef = env.getStaticReferenceField("sun.misc.Unsafe", "theUnsafe");
    return objRef;
  }

  @MJI
  public long objectFieldOffset__Ljava_lang_reflect_Field_2__J (MJIEnv env, int unsafeRef, int fieldRef) {
    return fieldOffset__Ljava_lang_reflect_Field_2__I(env, unsafeRef, fieldRef);
  }

  /**
   * we don't really return an offset here, since that would be useless. What we really want is
   * to identify the corresponding FieldInfo, and that's much easier done with the Field
   * registration id
   */
  @MJI
  public int fieldOffset__Ljava_lang_reflect_Field_2__I (MJIEnv env, int unsafeRef, int fieldRef) {
    //FieldInfo fi = JPF_java_lang_reflect_Field.getFieldInfo(env, fieldRef);
    //return fi.getStorageOffset();
    return env.getIntField(fieldRef, "regIdx");
  }

  @MJI
  public boolean compareAndSwapObject__Ljava_lang_Object_2JLjava_lang_Object_2Ljava_lang_Object_2__Z (MJIEnv env, int unsafeRef,
                                                                                                             int objRef, long fieldOffset,
                                                                                                             int expectRef, int updateRef) {
    int actual = getObject__Ljava_lang_Object_2J__Ljava_lang_Object_2(env, unsafeRef, objRef, fieldOffset);
    if (actual == expectRef) {
      putObject__Ljava_lang_Object_2JLjava_lang_Object_2__V(env, unsafeRef, objRef, fieldOffset, updateRef);
      return true;
    }
    return false;
  }

  @MJI
  public boolean compareAndSwapInt__Ljava_lang_Object_2JII__Z (MJIEnv env, int unsafeRef,
                                                                      int objRef, long fieldOffset, int expect, int update) {
    int actual = getInt__Ljava_lang_Object_2J__I(env, unsafeRef, objRef, fieldOffset);
    if (actual == expect) {
      putInt__Ljava_lang_Object_2JI__V(env, unsafeRef, objRef, fieldOffset, update);
      return true;
    }
    return false;
  }

  @MJI
  public boolean compareAndSwapLong__Ljava_lang_Object_2JJJ__Z (MJIEnv env, int unsafeRef,
                                                                       int objRef, long fieldOffset, long expect, long update) {
    long actual = getLong__Ljava_lang_Object_2J__J(env, unsafeRef, objRef, fieldOffset);
    if (actual == expect) {
      putLong__Ljava_lang_Object_2JJ__V(env, unsafeRef, objRef, fieldOffset, update);
      return true;
    }
    return false;
  }


  // this is a specialized, native wait() for the current thread that does not require a lock, and that can
  // be turned off by a preceding unpark() call (which is not accumulative)
  // park can be interrupted, but it doesn't throw an InterruptedException, and it doesn't clear the status
  // it can only be called from the current (parking) thread
  @MJI
  public void park__ZJ__V (MJIEnv env, int unsafeRef, boolean isAbsoluteTime, long timeout) {
    ThreadInfo ti = env.getThreadInfo();
    int objRef = ti.getThreadObjectRef();
    int permitRef = env.getReferenceField( objRef, "permit");
    ElementInfo ei = env.getModifiableElementInfo(permitRef);

    if (ti.isInterrupted(false)) {
      // there is no lock, so we go directly back to running and therefore
      // have to remove ourself from the contender list
      ei.setMonitorWithoutLocked(ti);

      // note that park() does not throw an InterruptedException
      return;
    }
    
    if (!ti.isFirstStepInsn()){
      if (ei.getBooleanField("blockPark")) { // we have to wait, but don't need a lock
        // running -> waiting | timeout_waiting
        ei.wait(ti, timeout, false);
        
      } else {
        ei.setBooleanField("blockPark", true); // re-arm for next park
        return;
      }
    }
    
    // scheduling point
    if (ti.getScheduler().setsParkCG( ti, isAbsoluteTime, timeout)) {
      env.repeatInvocation();
      return;
    }
    
    switch (ti.getState()) {
      case WAITING:
      case TIMEOUT_WAITING:
        throw new JPFException("blocking park() without transition break");   
      
      case NOTIFIED:
      case TIMEDOUT:
      case INTERRUPTED:
        ti.resetLockRef();
        ti.setRunning();
        break;
        
      default:
        // nothing
    } 
  }

  @MJI
  public void unpark__Ljava_lang_Object_2__V (MJIEnv env, int unsafeRef, int objRef) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiParked = env.getThreadInfoForObjRef(objRef);
      
    if (tiParked.isTerminated()){
      return; // nothing to do
    }
    
    if (!tiCurrent.isFirstStepInsn()){
      SystemState ss = env.getSystemState();
      int permitRef = env.getReferenceField( objRef, "permit");
      ElementInfo eiPermit = env.getModifiableElementInfo(permitRef);

      if (tiParked.getLockObject() == eiPermit){
        // note that 'permit' is only used in park/unpark, so there never is more than
        // one waiter, which immediately becomes runnable again because it doesn't hold a lock
        // (park is a lockfree wait). unpark() therefore has to be a right mover
        // and we have to register a ThreadCG here
        eiPermit.notifies(ss, tiCurrent, false);
        
      } else {
        eiPermit.setBooleanField("blockPark", false);
        return;
      }
    }
    
    if (tiCurrent.getScheduler().setsUnparkCG(tiCurrent, tiParked)){
      env.repeatInvocation();
      return;
    }
  }
  
  @MJI
  public void ensureClassInitialized__Ljava_lang_Class_2__V (MJIEnv env, int unsafeRef, int clsObjRef) {
    // <2do> not sure if we have to do anyting here - if we have a class object, the class should already
    // be initialized
  }

  @MJI
  public int getObject__Ljava_lang_Object_2J__Ljava_lang_Object_2 (MJIEnv env, int unsafeRef,
                                                                          int objRef, long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getReferenceField(fi);
    } else {
      return ei.getReferenceElement((int)fieldOffset);
    }
  }
  
  @MJI
  public int getObjectVolatile__Ljava_lang_Object_2J__Ljava_lang_Object_2 (MJIEnv env, int unsafeRef,
      int objRef, long fieldOffset) {
    return getObject__Ljava_lang_Object_2J__Ljava_lang_Object_2( env, unsafeRef, objRef, fieldOffset);
  }  

  @MJI
  public void putObject__Ljava_lang_Object_2JLjava_lang_Object_2__V (MJIEnv env, int unsafeRef,
                                                                            int objRef, long fieldOffset, int valRef) {
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setReferenceField(fi, valRef);
    } else {
      ei.setReferenceElement((int)fieldOffset, valRef);
    }
  }
  
  @MJI
  public void putObjectVolatile__Ljava_lang_Object_2JLjava_lang_Object_2__V (MJIEnv env, int unsafeRef,
      int objRef, long fieldOffset, int valRef) {
    putObject__Ljava_lang_Object_2JLjava_lang_Object_2__V( env, unsafeRef, objRef, fieldOffset, valRef);
  }

  @MJI
  public void putOrderedObject__Ljava_lang_Object_2JLjava_lang_Object_2__V(
                                                                                  MJIEnv env,
                                                                                  int unsafeRef,
                                                                                  int objRef,
                                                                                  long fieldOffset,
                                                                                  int valRef) {
    putObject__Ljava_lang_Object_2JLjava_lang_Object_2__V(env, unsafeRef, objRef, fieldOffset, valRef);
  }
  
  @MJI
  public boolean getBoolean__Ljava_lang_Object_2J__Z(MJIEnv env,
                                                            int unsafeRef,
                                                            int objRef,
                                                            long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getBooleanField(fi);
    } else {
      return ei.getBooleanElement((int)fieldOffset);
    }
  }

  @MJI
  public boolean getBooleanVolatile__Ljava_lang_Object_2J__Z(MJIEnv env, int unsafeRef,int objRef,long fieldOffset) {
    return getBoolean__Ljava_lang_Object_2J__Z( env, unsafeRef, objRef, fieldOffset);
  }
  
  @MJI
  public void putBoolean__Ljava_lang_Object_2JZ__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, boolean val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setBooleanField(fi, val);
    } else {
      ei.setBooleanElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putBooleanVolatile__Ljava_lang_Object_2JZ__V (MJIEnv env, int unsafeRef, int objRef, long fieldOffset, boolean val){
    putBoolean__Ljava_lang_Object_2JZ__V( env, unsafeRef, objRef, fieldOffset, val);
  }

  @MJI
  public byte getByte__Ljava_lang_Object_2J__B(MJIEnv env,
                                                      int unsafeRef,
                                                      int objRef,
                                                      long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getByteField(fi);
    } else {
      return ei.getByteElement((int)fieldOffset);
    }
  }

  @MJI
  public byte getByteVolatile__Ljava_lang_Object_2J__B(MJIEnv env,int unsafeRef,int objRef,long fieldOffset) {
    return getByte__Ljava_lang_Object_2J__B(env, unsafeRef, objRef, fieldOffset);
  }

  @MJI
  public void putByte__Ljava_lang_Object_2JB__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, byte val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setByteField(fi, val);
    } else {
      ei.setByteElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putByteVolatile__Ljava_lang_Object_2JB__V (MJIEnv env, int unsafeRef,int objRef, long fieldOffset, byte val){
    putByte__Ljava_lang_Object_2JB__V( env, unsafeRef, objRef, fieldOffset, val);
  }

  @MJI
  public char getChar__Ljava_lang_Object_2J__C(MJIEnv env,
                                                      int unsafeRef,
                                                      int objRef,
                                                      long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getCharField(fi);
    } else {
      return ei.getCharElement((int)fieldOffset);
    }
  }

  @MJI
  public char getCharVolatile__Ljava_lang_Object_2J__C(MJIEnv env,int unsafeRef,int objRef,long fieldOffset) {
    return getChar__Ljava_lang_Object_2J__C( env, unsafeRef, objRef, fieldOffset);
  }
  
  @MJI
  public void putChar__Ljava_lang_Object_2JC__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, char val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setCharField(fi, val);
    } else {
      ei.setCharElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putCharVolatile__Ljava_lang_Object_2JC__V (MJIEnv env, int unsafeRef,int objRef, long fieldOffset, char val){
    putChar__Ljava_lang_Object_2JC__V( env, unsafeRef, objRef, fieldOffset, val);
  }
  
  @MJI
  public short getShort__Ljava_lang_Object_2J__S(MJIEnv env,
                                                        int unsafeRef,
                                                        int objRef,
                                                        long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getShortField(fi);
    } else {
      return ei.getShortElement((int)fieldOffset);
    }
  }

  @MJI
  public short getShortVolatile__Ljava_lang_Object_2J__S(MJIEnv env,int unsafeRef,int objRef,long fieldOffset) {
    return getShort__Ljava_lang_Object_2J__S( env, unsafeRef, objRef, fieldOffset);
  }
  
  @MJI
  public void putShort__Ljava_lang_Object_2JS__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, short val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setShortField(fi, val);
    } else {
      ei.setShortElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putShortVolatile__Ljava_lang_Object_2JS__V (MJIEnv env, int unsafeRef,int objRef, long fieldOffset, short val){
    putShort__Ljava_lang_Object_2JS__V( env, unsafeRef, objRef, fieldOffset, val);
  }  

  @MJI
  public int getInt__Ljava_lang_Object_2J__I(MJIEnv env, int unsafeRef,
                                                    int objRef, long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getIntField(fi);
    } else {
      return ei.getIntElement((int)fieldOffset);
    }
  }

  @MJI
  public int getIntVolatile__Ljava_lang_Object_2J__I(MJIEnv env, int unsafeRef, int objRef, long fieldOffset) {
    return getInt__Ljava_lang_Object_2J__I( env, unsafeRef, objRef, fieldOffset);
  }
  
  @MJI
  public void putInt__Ljava_lang_Object_2JI__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, int val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setIntField(fi, val);
    } else {
      ei.setIntElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putIntVolatile__Ljava_lang_Object_2JI__V (MJIEnv env, int unsafeRef, int objRef, long fieldOffset, int val){
    putInt__Ljava_lang_Object_2JI__V(env, unsafeRef, objRef, fieldOffset, val);
  }  

  @MJI
  public void putOrderedInt__Ljava_lang_Object_2JI__V(MJIEnv env,
                                                             int unsafeRef,
                                                             int objRef,
                                                             long fieldOffset,
                                                             int val) {
    // volatile?
    putInt__Ljava_lang_Object_2JI__V(env, unsafeRef, objRef, fieldOffset, val);
  }

  @MJI
  public float getFloat__Ljava_lang_Object_2J__F(MJIEnv env,
                                                        int unsafeRef,
                                                        int objRef,
                                                        long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getFloatField(fi);
    } else {
      return ei.getFloatElement((int)fieldOffset);
    }
  }

  @MJI
  public float getFloatVolatile__Ljava_lang_Object_2J__F(MJIEnv env,int unsafeRef,int objRef,long fieldOffset) {
    return getFloat__Ljava_lang_Object_2J__F( env, unsafeRef, objRef, fieldOffset);
  }  

  @MJI
  public void putFloat__Ljava_lang_Object_2JF__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, float val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setFloatField(fi, val);
    } else {
      ei.setFloatElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putFloatVolatile__Ljava_lang_Object_2JF__V (MJIEnv env, int unsafeRef,int objRef, long fieldOffset, float val){
    putFloat__Ljava_lang_Object_2JF__V( env, unsafeRef, objRef, fieldOffset, val);
  }  

  @MJI
  public long getLong__Ljava_lang_Object_2J__J(MJIEnv env,
                                                      int unsafeRef,
                                                      int objRef,
                                                      long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getLongField(fi);
    } else {
      return ei.getLongElement((int)fieldOffset);
    }
  }

  @MJI
  public long getLongVolatile__Ljava_lang_Object_2J__J(MJIEnv env, int unsafeRef, int objRef, long fieldOffset) {
    return getLong__Ljava_lang_Object_2J__J( env, unsafeRef, objRef, fieldOffset);
  }

  @MJI
  public void putLong__Ljava_lang_Object_2JJ__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, long val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setLongField(fi, val);
    } else {
      ei.setLongElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putLongVolatile__Ljava_lang_Object_2JJ__V (MJIEnv env, int unsafeRef, int objRef, long fieldOffset, long val){
    putLong__Ljava_lang_Object_2JJ__V( env, unsafeRef, objRef, fieldOffset, val);
  }  

  @MJI
  public void putOrderedLong__Ljava_lang_Object_2JJ__V (MJIEnv env, int unsafeRef,
                                                        int objRef, long fieldOffset, long val) {
    putLong__Ljava_lang_Object_2JJ__V(env, unsafeRef, objRef, fieldOffset, val);
  }

  @MJI
  public double getDouble__Ljava_lang_Object_2J__D(MJIEnv env,
                                                         int unsafeRef,
                                                         int objRef,
                                                         long fieldOffset) {
    ElementInfo ei = env.getElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      return ei.getDoubleField(fi);
    } else {
      return ei.getDoubleElement((int)fieldOffset);
    }
  }

  @MJI
  public double getDoubleVolatile__Ljava_lang_Object_2J__D(MJIEnv env,int unsafeRef,int objRef,long fieldOffset) {
    return getDouble__Ljava_lang_Object_2J__D( env, unsafeRef, objRef, fieldOffset);
  }
  
  @MJI
  public void putDouble__Ljava_lang_Object_2JD__V (MJIEnv env, int unsafeRef,
                                                       int objRef, long fieldOffset, double val){
    ElementInfo ei = env.getModifiableElementInfo(objRef);
    if (!ei.isArray()) {
      FieldInfo fi = getRegisteredFieldInfo(fieldOffset);
      ei.setDoubleField(fi, val);
    } else {
      ei.setDoubleElement((int)fieldOffset, val);
    }
  }

  @MJI
  public void putDoubleVolatile__Ljava_lang_Object_2JD__V (MJIEnv env, int unsafeRef, int objRef, long fieldOffset, double val){
    putDouble__Ljava_lang_Object_2JD__V( env, unsafeRef, objRef, fieldOffset, val);
  }
  
  @MJI
  public int arrayBaseOffset__Ljava_lang_Class_2__I (MJIEnv env, int unsafeRef, int clazz) {
    return 0;
  }

  @MJI
  public int arrayIndexScale__Ljava_lang_Class_2__I (MJIEnv env, int unsafeRef, int clazz) {
    return 1;
  }

  private static FieldInfo getRegisteredFieldInfo(long fieldOffset) {
    return JPF_java_lang_reflect_Field.getRegisteredFieldInfo((int)fieldOffset);
  }

  
  //--- the explicit memory buffer allocation/free + access methods - evil pointer arithmetic

  /*
   * we shy away from maintaining our own address table by means of knowing that
   * the byte[] object stored in the ArrayFields will not be recycled, and hashCode() will
   * return its address, so the start/endAdr pairs we get from that have to be
   * non-overlapping. Of course that falls apart if  hashCode() would do something
   * different, which is the case for any address that exceeds 32bit
   */
  
  static class Alloc {
    int objRef;
    
    int startAdr;
    int endAdr;
    
    Alloc next;
    
    Alloc (MJIEnv env, int baRef, long length){
      this.objRef = baRef;

      ElementInfo ei = env.getElementInfo(baRef);
      ArrayFields afi = (ArrayFields) ei.getFields();
      byte[] mem = afi.asByteArray();

      startAdr = mem.hashCode();
      endAdr = startAdr + (int)length -1;
    }
    
    @Override
	public String toString(){
      return String.format("Alloc[objRef=%x,startAdr=%x,endAdr=%x]", objRef, startAdr, endAdr);
    }
  }
  
  Alloc firstAlloc;
  
  // for debugging purposes only
  private void dumpAllocs(){
    System.out.println("Unsafe allocated memory blocks:{");
    for (Alloc a = firstAlloc; a != null; a = a.next){
      System.out.print("  ");
      System.out.println(a);
    }
    System.out.println('}');
  }
  
  private void sortInAlloc(Alloc newAlloc){
    int startAdr = newAlloc.startAdr;
    
    if (firstAlloc == null){
      firstAlloc = newAlloc;
      
    } else {
      Alloc prev = null;
      for (Alloc a = firstAlloc; a != null; prev = a, a = a.next){
        if (startAdr < a.startAdr){
          newAlloc.next = a;
          if (prev == null){
            firstAlloc = newAlloc;
          } else {
            prev.next = newAlloc;
          }
        }
      }
    }
  }
  
  private Alloc getAlloc (int address){
    for (Alloc a = firstAlloc; a != null; a = a.next){
      if (address >= a.startAdr && address <= a.endAdr){
        return a;
      }
    }
    
    return null;
  }
  
  private Alloc removeAlloc (int startAddress){
    Alloc prev = null;
    for (Alloc a = firstAlloc; a != null; prev = a, a = a.next) {
      if (a.startAdr == startAddress){
        if (prev == null){
          firstAlloc = a.next;
        } else {
          prev.next = a.next;
        }
        
        return a;
      }
    }
    
    return null;
  }
  
  @MJI
  public long allocateMemory__J__J (MJIEnv env, int unsafeRef, long nBytes) {
    if (nBytes < 0 || nBytes > Integer.MAX_VALUE) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory block size: " + nBytes);
      return 0;
    }
    
    // <2do> we should probably also throw OutOfMemoryErrors on configured thresholds 
    
    int baRef = env.newByteArray((int) nBytes);
    // the corresponding objects have to be freed explicitly
    env.registerPinDown(baRef);
    
    Alloc alloc = new Alloc(env, baRef, nBytes);
    sortInAlloc(alloc);
    
    return alloc.startAdr;
  }
  
  @MJI
  public void freeMemory__J__V (MJIEnv env, int unsafeRef, long startAddress) {
    int addr = (int)startAddress;

    if (startAddress != MJIEnv.NULL){
      Alloc a = removeAlloc(addr);
      if (a == null){
        env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      } else {
        env.releasePinDown(a.objRef);
      }
    }
  }
  
  @MJI
  public byte getByte__J__B (MJIEnv env, int unsafeRef, long address) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return 0;
    }
    
    ElementInfo ei = env.getElementInfo(a.objRef);
    return ei.getByteElement(addr - a.startAdr);
  }

  @MJI
  public void putByte__JB__V (MJIEnv env, int unsafeRef, long address, byte val) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return;
    }
    
    ElementInfo ei = env.getModifiableElementInfo(a.objRef);
    ei.setByteElement(addr - a.startAdr, val);
  }
  
  @MJI
  public char getChar__J__C (MJIEnv env, int unsafeRef, long address) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return 0;
    }
    
    ElementInfo ei = env.getElementInfo(a.objRef);
    byte[] ba = ei.asByteArray();
    
    byte b0 = ba[addr];
    byte b1 = ba[addr+1];
    
    char val;
    if (env.isBigEndianPlatform()){
      val = (char) ((b0 << 8) | b1);
    } else {
      val = (char) ((b1 << 8) | b0);      
    }
    
    return val;
  }

  @MJI
  public void putChar__JC__V (MJIEnv env, int unsafeRef, long address, char val) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return;
    }
        
    byte b1 = (byte)(0xff & val);
    byte b0 = (byte)(0xff & (val >>> 8));
    
    ElementInfo ei = env.getModifiableElementInfo(a.objRef);

    if (env.isBigEndianPlatform()){
      ei.setByteElement(addr,   b0);
      ei.setByteElement(addr+1, b1);
    } else {
      ei.setByteElement(addr,   b1);
      ei.setByteElement(addr+1, b0);      
    }
  }

  @MJI
  public int getInt__J__I (MJIEnv env, int unsafeRef, long address) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return 0;
    }
    
    ElementInfo ei = env.getElementInfo(a.objRef);
    byte[] ba = ei.asByteArray();
    
    byte b0 = ba[addr];
    byte b1 = ba[addr+1];
    byte b2 = ba[addr+2];
    byte b3 = ba[addr+3];
    
    int val;
    if (env.isBigEndianPlatform()){
      val = b0;
      val = (val << 8) | b1;
      val = (val << 8) | b2;
      val = (val << 8) | b3;

    } else {
      val = b3;
      val = (val << 8) | b2;
      val = (val << 8) | b1;
      val = (val << 8) | b0;
    }
    
    return val;
  }

  @MJI
  public void putInt__JI__V (MJIEnv env, int unsafeRef, long address, int val) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return;
    }
        
    byte b3 = (byte)(0xff & val);
    byte b2 = (byte)(0xff & (val >>> 8));
    byte b1 = (byte)(0xff & (val >>> 16));
    byte b0 = (byte)(0xff & (val >>> 24));    
    
    ElementInfo ei = env.getModifiableElementInfo(a.objRef);

    if (env.isBigEndianPlatform()){
      ei.setByteElement(addr,   b0);
      ei.setByteElement(addr+1, b1);
      ei.setByteElement(addr+2, b2);
      ei.setByteElement(addr+3, b3);
    } else {
      ei.setByteElement(addr,   b3);
      ei.setByteElement(addr+1, b2);
      ei.setByteElement(addr+2, b1);
      ei.setByteElement(addr+3, b0);
    }
  }

  @MJI
  public long getLong__J__J (MJIEnv env, int unsafeRef, long address) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return 0;
    }
    
    ElementInfo ei = env.getElementInfo(a.objRef);
    byte[] ba = ei.asByteArray();
    int offset = addr - a.startAdr;
    
    byte b0 = ba[offset];
    byte b1 = ba[offset+1];
    byte b2 = ba[offset+2];
    byte b3 = ba[offset+3];
    byte b4 = ba[offset+4];
    byte b5 = ba[offset+5];
    byte b6 = ba[offset+6];
    byte b7 = ba[offset+7];
    
    int val;
    if (env.isBigEndianPlatform()){
      val = b0;
      val = (val << 8) | b1;
      val = (val << 8) | b2;
      val = (val << 8) | b3;
      val = (val << 8) | b4;
      val = (val << 8) | b5;
      val = (val << 8) | b6;
      val = (val << 8) | b7;

    } else {
      val = b7;
      val = (val << 8) | b6;
      val = (val << 8) | b5;
      val = (val << 8) | b4;
      val = (val << 8) | b3;
      val = (val << 8) | b2;
      val = (val << 8) | b1;
      val = (val << 8) | b0;
    }
    
    return val;
  }

  @MJI
  public void putLong__JJ__V (MJIEnv env, int unsafeRef, long address, long val) {
    int addr = (int)address;
    Alloc a = getAlloc(addr);
    
    if (a == null) {
      env.throwException("java.lang.IllegalArgumentException", "invalid memory address: " + Integer.toHexString(addr));
      return;
    }
        
    byte b7 = (byte)(0xff & val);
    byte b6 = (byte)(0xff & (val >>> 8));
    byte b5 = (byte)(0xff & (val >>> 16));
    byte b4 = (byte)(0xff & (val >>> 24));    
    byte b3 = (byte)(0xff & (val >>> 32));    
    byte b2 = (byte)(0xff & (val >>> 40));    
    byte b1 = (byte)(0xff & (val >>> 48));    
    byte b0 = (byte)(0xff & (val >>> 56));    

    ElementInfo ei = env.getModifiableElementInfo(a.objRef);
    int offset = addr - a.startAdr;
    
    if (env.isBigEndianPlatform()){
      ei.setByteElement(offset,   b0);
      ei.setByteElement(offset+1, b1);
      ei.setByteElement(offset+2, b2);
      ei.setByteElement(offset+3, b3);
      ei.setByteElement(offset+4, b4);
      ei.setByteElement(offset+5, b5);
      ei.setByteElement(offset+6, b6);
      ei.setByteElement(offset+7, b7);
      
    } else {
      ei.setByteElement(offset,   b7);
      ei.setByteElement(offset+1, b6);
      ei.setByteElement(offset+2, b5);
      ei.setByteElement(offset+3, b4);
      ei.setByteElement(offset+4, b3);
      ei.setByteElement(offset+5, b2);
      ei.setByteElement(offset+6, b1);
      ei.setByteElement(offset+7, b0);
    }
  }

}

