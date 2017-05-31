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
package java.lang;

import gov.nasa.jpf.annotation.NeverBreak;
import sun.nio.ch.Interruptible;

/**
 * MJI model class for java.lang.Thread library abstraction
 * 
 * <2do> this should not require the JPF ThreadList to retrieve corresponding ThreadInfos
 * (the ThreadList might not store terminated threads)
 */
public class Thread implements Runnable {

  public interface UncaughtExceptionHandler {
    // note this doesn't stop the thread from being terminated
    void uncaughtException (Thread t, Throwable x);
  }
  
  static int nameThreadNum; // to construct the default thread name  

  public static final int MIN_PRIORITY = 1;
  public static final int NORM_PRIORITY = 5;
  public static final int MAX_PRIORITY = 10;

  // don't rename this - it's used by ThreadGoup.uncaughtException()
  private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler; // null by default
  
  // initialized in init(), except of the main thread (which gets explicitly initialized by the VM)
  ThreadGroup group;
  Runnable target;
  String name;
  int priority;
  boolean isDaemon;
  
  // this is an explicit thread state that gets set on a call of interrupt(), but
  // only if the thread is not blocked. If it is, we only change the status.
  // this gets cleared by calling interrupted()
  boolean             interrupted;
  
  // those are only accessed from peers since thread obects are per se shared
  @NeverBreak
  ThreadLocal.Entry<?>[] threadLocals;
  
  // this is what we use for sun.misc.Unsafe.park()/unpark()
  // this is accessed from the native peer, VM.createMainThread() and sun.misc.Unsafe
  static class Permit {
    boolean blockPark = true; // this is used to remember unpark() calls before park() (they don't accumulate)
  }
  Permit permit; // the object is used for wait/notify

  // referenced by java.util.concurrent.locks.LockSupport via sun.misc.Unsafe
  // DON'T CHANGE THIS NAME
  volatile Object parkBlocker;

  // used to store Thread.stop() exceptions
  Throwable stopException;
  
  private volatile UncaughtExceptionHandler uncaughtExceptionHandler; // null by default

  
  public enum State { BLOCKED, NEW, RUNNABLE, TERMINATED, TIMED_WAITING, WAITING }

  
  public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler xh) {
    defaultUncaughtExceptionHandler = xh;
  }
  
  public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler(){
    return defaultUncaughtExceptionHandler;
  }
  
  
  public Thread () {
    this(null, null, null, 0L);
  }

  public Thread (Runnable target) {
    this(null, target, null, 0L);
  }

  public Thread (Runnable target, String name) {
    this(null, target, name, 0L);
  }

  public Thread (String name) {
    this(null, null, name, 0L);
  }

  public Thread (ThreadGroup group, String name) {
    this(group, null, name, 0L);
  }
  
  public Thread (ThreadGroup group, Runnable target) {
    this(group, target, null, 0L);
  }

  public Thread (ThreadGroup group, Runnable target, String name) {
    this(group, target, name, 0L);
  }

  public Thread (ThreadGroup group, Runnable target, String name, long stackSize) {
    Thread cur = currentThread();

    if (group == null) {
      this.group = cur.getThreadGroup();
    } else {
      this.group = group;
    }

    this.group.add(this);

    if (name == null) {
      this.name = "Thread-" + ++nameThreadNum;
    } else {
      this.name = name;
    }

    this.permit = new Permit();

    // those are always inherited from the current thread
    this.priority = cur.getPriority();
    this.isDaemon = cur.isDaemon();

    this.target = target;

    // do our associated native init
    init0(this.group, target, this.name, stackSize);
    
    initThreadLocals(cur);
  }

  // this takes care of ThreadInfo initialization
  native void init0 (ThreadGroup group, Runnable target, String name, long stackSize);
  
  // this is here since InheritableThreadLocals would require childValue(parentVal) roundtrips.
  // Unfortunately we can't defer this until the ThreadLocal is actually accessed since
  // we have to capture the value at the point of child creation
  // Note this executes in the parent thread
  private void initThreadLocals (Thread parent){
    ThreadLocal.Entry<?>[] tl = parent.threadLocals;
    if (tl != null){
      int len = tl.length;
      ThreadLocal.Entry<?>[] inherited = null;
      int j=0;
      
      for (int i=0; i<len; i++){
        ThreadLocal.Entry<?> e = tl[i];
        ThreadLocal.Entry<?> ec = e.getChildEntry();
        if (ec != null){
          if (inherited == null){
            inherited = new ThreadLocal.Entry<?>[len];
          }
          inherited[j++] = ec;
        }
      }
      
      if (inherited != null){
        ThreadLocal.Entry<?>[] a = new ThreadLocal.Entry<?>[j];
        System.arraycopy(inherited,0,a,0,j);
        threadLocals = a;
      }
    }
  }
  
  public static int activeCount () {
    return 0;
  }

  public void setUncaughtExceptionHandler(UncaughtExceptionHandler xh) {
    uncaughtExceptionHandler = xh;
  }
  
  public UncaughtExceptionHandler getUncaughtExceptionHandler(){
    if (uncaughtExceptionHandler != null){
      return uncaughtExceptionHandler;
    } else {
      return group;
    }
  }
  
  public void setContextClassLoader (ClassLoader cl) {
  }

  public ClassLoader getContextClassLoader () {
    // <NSY>
    return null;
  }

  public synchronized void setDaemon (boolean isDaemon) {
    this.isDaemon = isDaemon;
    setDaemon0(isDaemon);
  }

  public boolean isDaemon () {
    return isDaemon;
  }

  public native long getId();

  public StackTraceElement[] getStackTrace() {
    return null; // not yet implemented
  }

  public native int getState0();

  public Thread.State getState() {
    int i = getState0();
    switch (i) {
    case 0: return State.BLOCKED;
    case 1: return State.NEW;
    case 2: return State.RUNNABLE;
    case 3: return State.TERMINATED;
    case 4: return State.TIMED_WAITING;
    case 5: return State.WAITING;
    }

    return null; // shoudl be intercepted by a getState0 assertion
  }

  public synchronized void setName (String name) {
    if (name == null) {
      throw new IllegalArgumentException("thread name can't be null");
    }

    this.name = name;
    setName0(name);
  }

  public String getName () {
    return name;
  }

  public void setPriority (int priority) {
    if ((priority < MIN_PRIORITY) || (priority > MAX_PRIORITY)) {
      throw new IllegalArgumentException("thread priority out of range");
    }

    this.priority = priority;
    setPriority0(priority);
  }

  public int getPriority () {
    return priority;
  }

  public ThreadGroup getThreadGroup () {
    return group;
  }

  public void checkAccess () {
    // <NSY>
  }

  public native int countStackFrames ();

  public static native Thread currentThread ();

  public void destroy () {
  }

  public static void dumpStack () {
  }

  public static int enumerate (Thread[] tarray) {
    Thread cur = currentThread();

    return cur.group.enumerate(tarray);
  }

  public static native boolean holdsLock (Object obj);

  // this one needs to be native because it might change the thread status
  public native void interrupt ();

  // those don't have to be native, but we keep it symmetric
  public static native boolean interrupted ();
  public native boolean isInterrupted ();

  public native boolean isAlive ();


  /**
   * note these are not synchronized anymore since they are intercepted by the
   * native peer. The reason is that we don't want two CGs per join call (one for the
   * sync call, and one for the wait) because this can cause serious
   * performance degradation
   */
  public void join () throws InterruptedException {
    synchronized(this){

      if (interrupted()) {
        throw new InterruptedException();
      }

      while (isAlive()) {
        // apparently, the JDK doesn't throw InterruptedExceptions if
        // we get interrupted after waiting in the join
        wait();
      }
    }
  }

  public void join (long millis) throws InterruptedException {
    join(millis, 0);
  }

  public void join (long millis, int nanos) throws InterruptedException {

    if (millis < 0){
      throw new java.lang.IllegalArgumentException("timeout value is negative");

    } else if (millis == 0){
      join();

    } else {
      synchronized(this){
        if (interrupted()){
          throw new InterruptedException();
        }

        wait(millis);
      }
    }
  }

    

  @Override
  public void run () {
    if (target != null) {
      target.run();
    }
  }

  public static void sleep (long millis) throws InterruptedException {
    sleep(millis, 0);
  }

  public static native void sleep (long millis, int nanos)
                            throws InterruptedException;

  public native void start();
  public native void stop();
  public native void stop(Throwable obj);

  public native void suspend();
  public native void resume();


  @Override
  public String toString () {
    return ("Thread[" + name + ',' + priority + ',' + (group == null ? "" : group.getName()) + ']');
  }

  public static native void yield ();

  native void setDaemon0 (boolean on);

  native void setName0 (String name);

  native void setPriority0 (int priority);



  /**
   * automatically called by system upon thread termination to clean up
   * references.
   * 
   * NOTE - we clean up atomically during ThreadInfo.finish(), to avoid any
   * additional states. This is important since group per se is a shared object
   * We only include this method here as a specification for ThreadInfo
   */
  private void exit () {
    if (group != null){
      group.threadTerminated(this);
      group = null;
    }
    
    threadLocals = null;    
    parkBlocker = null;
    uncaughtExceptionHandler = null;
  }

  // some Java 6 mojo
  // <2do> not implemented yet
  native void blockedOn (Interruptible b);

  
  // we probably will remove these fields once we modeled java.util.concurrent.ThreadLocalRandom 
  // to make it deterministic
  long threadLocalRandomSeed;
  int threadLocalRandomProbe;
  int threadLocalRandomSecondarySeed;
}
