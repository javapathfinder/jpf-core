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

import gov.nasa.jpf.util.HashData;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Represents the variable, hash-collapsed pooled data associated with an object
 * that is not related to the object values (->Fields), but to the use of the
 * object for synchronization purposes (locks and signals).
 * 
 */
public class Monitor implements Cloneable {
  
  static ThreadInfo[] emptySet = new ThreadInfo[0];
  
  /** the thread owning the lock */
  private ThreadInfo lockingThread;

  /** the nesting level for recursive lock acquisition */
  private int lockCount;
  
  /** 
   * the list of threads that try to acquire the lock (can be in blocked, waiting,
   * interrupted or running state).
   */
  ThreadInfo[] lockedThreads;

  /**
   * Creates a new empty monitor.
   */
  public Monitor () {
    lockingThread = null;
    lockCount = 0;    
    lockedThreads = emptySet;
  }

  private Monitor (ThreadInfo locking, int count, ThreadInfo[] locked) {
    lockingThread = locking;
    lockCount = count;
    lockedThreads = locked.clone();
    Arrays.sort(lockedThreads);
  }
  
  public void printFields (PrintWriter pw) {
    int i;

    pw.print(this);
    pw.print(" [");
    if (lockingThread != null) {
      pw.print( "locked by: ");
      pw.print( lockingThread.getName());
    } else {
      pw.print( "unlocked");
    }
    
    pw.print(", lockCount: ");
    pw.print( lockCount);
    
    pw.print(", locked: {");
    for (i=0; i<lockedThreads.length; i++) {
      if (i > 0) pw.print(',');
      pw.print(lockedThreads[i].getName());
      pw.print(':');
      pw.print(lockedThreads[i].getStateName());
    }
    pw.println("}]");
  }
  
  // for debugging purposes
  public void dump() {
    PrintWriter pw = new PrintWriter(System.out);
    printFields(pw);
    pw.flush();
  }
  
  Monitor cloneWithLocked (ThreadInfo ti) {
    return new Monitor(lockingThread, lockCount, add(lockedThreads, ti));
  }

  Monitor cloneWithoutLocked (ThreadInfo ti) {
    return new Monitor(lockingThread, lockCount, remove(lockedThreads, ti));
  }

  @Override
  public Monitor clone () {
    try {
      // no need to clone the empty set (which should be the majority of cases)
      Monitor m = (Monitor) super.clone();
      if (lockedThreads != emptySet) {
        m.lockedThreads = lockedThreads.clone();
      }
      return m;
      
    } catch (CloneNotSupportedException cnsx) {
      throw new InternalError("should not happen");
    }
  }
  
  
  /**
   * Compares to another object.
   */
  @Override
  public boolean equals (Object o) {
    if (o == null) {
      return false;
    }

    if (!(o instanceof Monitor)) {
      return false;
    }

    Monitor m = (Monitor) o;

    if (lockingThread != m.getLockingThread()) {
      return false;
    }

    if (lockCount != m.getLockCount()) {
      return false;
    }

    ThreadInfo[] list = m.lockedThreads;
    if (lockedThreads.length != list.length) {
      return false;
    }

    for (int i = 0; i < lockedThreads.length; i++) {
      if (lockedThreads[i] != list[i]) {
        return false;
      }
    }

    return true;
  }
  

  public void hash (HashData hd) {
    if (lockingThread != null) {
      hd.add(lockingThread.getId());
    }
    
    hd.add(lockCount);
    
    for (int i = 0; i < lockedThreads.length; i++) {
      hd.add(lockedThreads[i].getId());
    }    
  }

  
  @Override
  public int hashCode () {
    HashData hd = new HashData();
    hash(hd);
    return hd.getValue();
  }
  

  /**
   * Returns the number of nested locks acquired.
   */
  public int getLockCount () {
    return lockCount;
  }


  /**
   * Returns the identifier of the thread holding the lock.
   */
  public ThreadInfo getLockingThread () {
    return lockingThread;
  }


  /**
   * Returns the list of locked threads
   */ 
  public ThreadInfo[] getLockedThreads() {
    return lockedThreads;
  }
  

  public boolean hasLockedThreads () {
    return (lockedThreads.length > 0);
  }
  
  public boolean hasWaitingThreads () {
    for (int i=0; i<lockedThreads.length; i++) {
      if (lockedThreads[i].isWaiting()) {
        return true;
      }
    }

    return false;
  }

  public int getNumberOfWaitingThreads() {
    int n=0;

    for (ThreadInfo ti : lockedThreads){
      if (ti.isWaiting()){
        n++;
      }
    }

    return n;
  }


  public ThreadInfo[] getWaitingThreads() {
    int n = getNumberOfWaitingThreads();

    if (n > 0){
      ThreadInfo[] list = new ThreadInfo[n];
      int i=0;
      for (int j=0; j<lockedThreads.length && i<n; j++){
        ThreadInfo ti = lockedThreads[j];
        if (ti.isWaiting()){
          list[i++] = ti;
        }
      }

      return list;

    } else {
      return emptySet;
    }
  }

  public int getNumberOfBlockedThreads() {
    int n=0;

    for (ThreadInfo ti : lockedThreads){
      if (ti.isBlocked()){
        n++;
      }
    }

    return n;
  }


  public ThreadInfo[] getBlockedThreads() {
    int n = getNumberOfBlockedThreads();

    if (n > 0){
      ThreadInfo[] list = new ThreadInfo[n];
      int i=0;
      for (int j=0; j<lockedThreads.length && i<n; j++){
        ThreadInfo ti = lockedThreads[j];
        if (ti.isBlocked()){
          list[i++] = ti;
        }
      }

      return list;

    } else {
      return emptySet;
    }
  }


  public int getNumberOfBlockedOrWaitingThreads() {
    int n=0;

    for (ThreadInfo ti : lockedThreads){
      if (ti.isBlocked() || ti.isWaiting()){
        n++;
      }
    }

    return n;
  }


  public ThreadInfo[] getBlockedOrWaitingThreads() {
    int n = getNumberOfBlockedThreads();

    if (n > 0){
      ThreadInfo[] list = new ThreadInfo[n];
      int i=0;
      for (int j=0; j<lockedThreads.length && i<n; j++){
        ThreadInfo ti = lockedThreads[j];
        if (ti.isBlocked() || ti.isWaiting()){
          list[i++] = ti;
        }
      }

      return list;

    } else {
      return emptySet;
    }
  }

  
  /**
   * Returns true if it is possible to lock the monitor.
   */
  public boolean canLock (ThreadInfo th) {
    if (lockingThread == null) {
      return true;
    }

    return (lockingThread == th);
  }


  void setLockingThread (ThreadInfo ti) {
    lockingThread = ti;
  }
  

  void incLockCount () {
    lockCount++;
  }
  
  
  void decLockCount () {
    assert lockCount > 0 : "negative lockCount";
    lockCount--;
  }
  
  
  void setLockCount (int lc) {
    assert lc >= 0 : "attempt to set negative lockCount";
    lockCount = lc;
  }
  
  public int objectHashCode () {
    return super.hashCode();
  }

  void resetLockedThreads () {
    lockedThreads = emptySet;
  }

  public boolean isLocking(ThreadInfo ti){
    if (lockedThreads != null){
      for (ThreadInfo lti : lockedThreads){
        if (lti == ti){
          return true;
        }
      }
    }
    
    return false;
  }

  
  static boolean containsLocked(ThreadInfo[] list, ThreadInfo ti){
    int len = list.length;
 
    for (int i=0; i<len; i++){
      if (list[i] == ti){
        return true;
      }
    }
    
    return false;
  }
  
  static ThreadInfo[] add (ThreadInfo[] list, ThreadInfo ti) {
    int len = list.length;
    
    //--- first, check if its already there
    if (containsLocked(list, ti)){
      // this is required because interrupted parks/joins can try to
      // re-park/join from their respective handlers (they don't hold locks) 
      return list;
    }
        
    ThreadInfo[] newList = new ThreadInfo[len+1];

    int pos = 0;
    for (; pos < len && ti.compareTo(list[pos]) > 0; pos++) {
      newList[pos] = list[pos];
    }
    
    newList[pos] = ti;
    for (; pos < len; pos++) {
      newList[pos+1] = list[pos];
    }

    return newList;
  }
  
  void addLocked (ThreadInfo ti) {
    lockedThreads = add(lockedThreads, ti);
  }
  
  static ThreadInfo[] remove (ThreadInfo[] list, ThreadInfo ti) {
    int len = list.length;

    if (len == 0) { // nothing to remove from
      return list;
      
    } else if (len == 1) {  // one element list optimization
      if (list[0] == ti) {
        return emptySet;
      } else {
        return list;
      }
    } else {
      
      //--- first, check if its already there
      if (!containsLocked(list, ti)) {
        // no known case yet, but we keep it symmetric
        // <2do> maybe worth a warning
        return list;
      }
      
      for (int i=0; i<len; i++) {
        if (list[i] == ti) {
          int newLen = len-1;
          ThreadInfo[] newList = new ThreadInfo[newLen];
          if (i > 0) {
            System.arraycopy(list, 0, newList, 0, i);
          }
          if (i < newLen) {
            System.arraycopy(list, i+1, newList, i, newLen-i);
          }
          return newList;
        }
      }
      // else, not in list:
      return list;
    }
  }
  
  void removeLocked (ThreadInfo ti) {
    lockedThreads = remove(lockedThreads, ti);
  }
}
