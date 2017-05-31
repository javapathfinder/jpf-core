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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.JPFLogger;


/**
 * MJI NativePeer class for java.lang.Thread library abstraction
 * 
 * NOTE - this implementation depends on all live thread objects being
 * in ThreadList
 */
public class JPF_java_lang_Thread extends NativePeer {

  static JPFLogger log = JPF.getLogger("gov.nasa.jpf.vm.ThreadInfo");
  
  
  /**
   * This method is the common initializer for all Thread ctors, and the only
   * single location where we can init our ThreadInfo, but it is PRIVATE
   */
  @MJI
  public void init0__Ljava_lang_ThreadGroup_2Ljava_lang_Runnable_2Ljava_lang_String_2J__V (MJIEnv env,
                         int objRef, int groupRef, int runnableRef, int nameRef, long stackSize) {
    VM vm = env.getVM();
    
    // we only need to create the ThreadInfo - its initialization will take care
    // of proper linkage to the java.lang.Thread object (objRef)
    vm.createThreadInfo( objRef, groupRef, runnableRef, nameRef);
  }

  @MJI
  public boolean isAlive____Z (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    if (ti != null){
      return ti.isAlive();
    } else {
      return false; // not in ThreadList anymore
    }
  }

  @MJI
  public void setDaemon0__Z__V (MJIEnv env, int objref, boolean isDaemon) {
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    ti.setDaemon(isDaemon);
  }

  @MJI
  public void dumpStack____V (MJIEnv env, int clsObjRef){
    ThreadInfo ti = env.getThreadInfo();
    ti.printStackTrace(); // this is not correct, we should go through VM.print
  }

  @MJI
  public void setName0__Ljava_lang_String_2__V (MJIEnv env, int objref, int nameRef) {
    // it bails if you try to set a null name
    if (nameRef == MJIEnv.NULL) {
      env.throwException("java.lang.IllegalArgumentException");

      return;
    }

    // we have to intercept this to cache the name as a Java object
    // (to be stored in ThreadData)
    // luckily enough, it's copied into the java.lang.Thread object
    // as a char[], i.e. does not have to preserve identity
    // Note the nastiness in here - the java.lang.Thread object is only used
    // to get the initial values into ThreadData, and gets inconsistent
    // if this method is called (just works because the 'name' field is only
    // directly accessed from within the Thread ctors)
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    ti.setName(env.getStringObject(nameRef));
  }

  @MJI
  public void setPriority0__I__V (MJIEnv env, int objref, int prio) {
    // again, we have to cache this in ThreadData for performance reasons
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    
    if (prio != ti.getPriority()){
      ti.setPriority(prio);
    
      // this could cause a context switch in a priority based scheduler
      if (ti.getScheduler().setsPriorityCG(ti)){
        env.repeatInvocation();
        return;
      }
    }
  }

  @MJI
  public int countStackFrames____I (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    return ti.countStackFrames();
  }

  @MJI
  public int currentThread____Ljava_lang_Thread_2 (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    return ti.getThreadObjectRef();
  }

  @MJI
  public boolean holdsLock__Ljava_lang_Object_2__Z (MJIEnv env, int clsObjRef, int objref) {
    ThreadInfo  ti = env.getThreadInfo();
    ElementInfo ei = env.getElementInfo(objref);

    return ei.isLockedBy(ti);
  }

  @MJI
  public void interrupt____V (MJIEnv env, int objref) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiInterrupted = env.getThreadInfoForObjRef(objref);

    if (!tiCurrent.isFirstStepInsn()) {
      tiInterrupted.interrupt();
    }
    
    if (tiCurrent.getScheduler().setsInterruptCG(tiCurrent, tiInterrupted)) {
      env.repeatInvocation();
      return;
    }
  }

  // these could be in the model, but we keep it symmetric, which also saves
  // us the effort of avoiding unwanted shared object field access CGs
  @MJI
  public boolean isInterrupted____Z (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    return ti.isInterrupted(false);
  }

  @MJI
  public boolean interrupted____Z (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    return ti.isInterrupted(true);
  }

  @MJI
  public void start____V (MJIEnv env, int objref) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiStarted = env.getThreadInfoForObjRef(objref);
    VM vm = tiCurrent.getVM();

    //--- actions that are only performed upon first execution
    if (!tiCurrent.isFirstStepInsn()){
      if (tiStarted.isStopped()) {
        // don't do anything but set it terminated - it hasn't acquired any resources yet.
        // note that apparently host VMs don't schedule this thread, so there is no handler invocation
        tiStarted.setTerminated();
        return;
      }

      if (!tiStarted.isNew()) {
        // alredy running, throw a IllegalThreadStateException. If it already terminated, it just gets
        // silently ignored in Java 1.4, but the 1.5 spec explicitly marks this
        // as illegal, so we adopt this by throwing an IllegalThreadState, too
        env.throwException("java.lang.IllegalThreadStateException");
        return;
      }

      int runnableRef = tiStarted.getRunnableRef();
      if (runnableRef == MJIEnv.NULL) {
        // note that we don't set the 'tiSuspended' field, since java.lang.Thread doesn't
        runnableRef = objref;
      }

      ElementInfo eiTarget = env.getElementInfo(runnableRef);
      ClassInfo   ci = eiTarget.getClassInfo();
      MethodInfo  miRun = ci.getMethod("run()V", true);

      // we do direct call run() invocation so that we have a well defined
      // exit point (DIRECTCALLRETURN) in case the thread is stopped or there is
      // a fail-safe UncaughtExceptionHandler set
      DirectCallStackFrame runFrame = miRun.createRunStartStackFrame(tiStarted);
      runFrame.setReferenceArgument(0, runnableRef, null);
            
      tiStarted.pushFrame(runFrame);
      tiStarted.setState(ThreadInfo.State.RUNNING);
      
      vm.notifyThreadStarted(tiStarted);
    }
    
    //--- scheduling point
    if (tiCurrent.getScheduler().setsStartCG(tiCurrent, tiStarted)){
      env.repeatInvocation();
    }
    // everything that would follow would be re-executed
  }

  @MJI
  public void yield____V (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    if (ti.getScheduler().setsYieldCG(ti)){
      env.repeatInvocation();
    }
  }

  @MJI
  public void sleep__JI__V (MJIEnv env, int clsObjRef, long millis, int nanos) {
    ThreadInfo ti = env.getThreadInfo();

    // check scheduling point
    if (ti.getScheduler().setsSleepCG(ti, millis, nanos)){
      ti.setSleeping();
      env.repeatInvocation();
      return;
    }
    
    if (ti.isSleeping()){
      ti.setRunning();
    }
  }

  @MJI
  public void suspend____V (MJIEnv env, int threadObjRef) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiSuspended = env.getThreadInfoForObjRef(threadObjRef);

    if (tiSuspended.isTerminated()) {
      return;  // nothing to do, it's already gone
    }
    
    if (!tiCurrent.isFirstStepInsn()){ // do this just once
      tiSuspended.suspend();
    }
    
    // scheduling point
    if (tiCurrent.getScheduler().setsSuspendCG(tiCurrent, tiSuspended)){
      env.repeatInvocation();      
    }
  }

  @MJI
  public void resume____V (MJIEnv env, int threadObjRef) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiResumed = env.getThreadInfoForObjRef(threadObjRef);

    if (tiCurrent == tiResumed){
      return;  // no self resume prior to suspension
    }

    if (tiResumed.isTerminated()) {
      return;  // nothing to resume
    }

    if (!tiCurrent.isFirstStepInsn()) { // do this just once
      tiResumed.resume();
    }
    
    // check scheduling point
    if (tiCurrent.getScheduler().setsResumeCG(tiCurrent, tiResumed)){
      env.repeatInvocation();
      return;
    }
  }

  /*
   * the join() workhorse. We use lockfree waits instead of a simple wait from a synchronized block
   * to save states
   */
  protected void join0 (MJIEnv env, int joineeRef, long timeout){
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiJoinee = env.getThreadInfoForObjRef(joineeRef);
    ElementInfo eiJoinee = env.getModifiableElementInfo(joineeRef); // the thread object to wait on

    if (timeout < 0) {
      env.throwException("java.lang.IllegalArgumentException", "timeout value is negative");
      return;
    }
      
    if (tiCurrent.isInterrupted(true)){ // interrupt status is set, throw and bail      
      // since we use lock-free waits, we need to remove ourselves from the lock contender list
      eiJoinee.setMonitorWithoutLocked(tiCurrent);
      
      // note that we have to throw even if the thread to join to is not alive anymore
      env.throwInterrupt();
      return;
    }
  
    if (!tiCurrent.isFirstStepInsn()){ // to be executed only once    
      if (tiJoinee.isAlive()) {
        // block in first top half so that following transitions see this thread as not runnable
        eiJoinee.wait( tiCurrent, timeout, false);
      } else {
        return; // nothing to do
      }
    }    

    if (tiCurrent.getScheduler().setsJoinCG(tiCurrent, tiJoinee, timeout)) {
      env.repeatInvocation();
      return;
    }
    
    // unblock in bottom half
    switch (tiCurrent.getState()) {
      case WAITING:
      case TIMEOUT_WAITING:
        throw new JPFException("blocking join without transition break");        
      
      case UNBLOCKED:
        // Thread was owning the lock when it joined - we have to wait until
        // we can reacquire it
        eiJoinee.lockNotified(tiCurrent);
        break;

      case TIMEDOUT:
        eiJoinee.resumeNonlockedWaiter(tiCurrent);
        break;

      case RUNNING:
        if (tiJoinee.isAlive()) { // we still need to wait
          eiJoinee.wait(tiCurrent, timeout, false); // no need for a new CG
          env.repeatInvocation();
        }
        break;
    }
  }

  @MJI
  public void join____V (MJIEnv env, int objref){
    join0(env,objref,0);
  }

  @MJI
  public void join__J__V (MJIEnv env, int objref, long millis) {
    join0(env,objref,millis);

  }

  @MJI
  public void join__JI__V (MJIEnv env, int objref, long millis, int nanos) {
    join0(env,objref,millis); // <2do> we ignore nanos for now
  }

  @MJI
  public int getState0____I (MJIEnv env, int objref) {
    // return the state index with respect to one of the public Thread.States
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);

    switch (ti.getState()) {
      case NEW:
        return 1;
      case RUNNING:
        return 2;
      case BLOCKED:
        return 0;
      case UNBLOCKED:
        return 2;
      case WAITING:
        return 5;
      case TIMEOUT_WAITING:
        return 4;
      case SLEEPING:
        return 4;
      case NOTIFIED:
        return 0;
      case INTERRUPTED:
        return 0;
      case TIMEDOUT:
        return 2;
      case TERMINATED:
        return 3;
      default:
        throw new JPFException("illegal thread state: " + ti.getState());
    }
  }

  @MJI
  public long getId____J (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfoForObjRef(objref);
    return ti.getId();
  }
  
  @MJI
  public void stop____V (MJIEnv env, int threadRef) {
    stop__Ljava_lang_Throwable_2__V(env, threadRef, MJIEnv.NULL);
  }

  @MJI
  public void stop__Ljava_lang_Throwable_2__V (MJIEnv env, int threadRef, int throwableRef) {
    ThreadInfo tiCurrent = env.getThreadInfo();
    ThreadInfo tiStopped = env.getThreadInfoForObjRef(threadRef);

    if (tiStopped.isTerminated() || tiStopped.isStopped()) {
      return; // silently ignored
    }

    if (tiCurrent.getScheduler().setsStopCG(tiCurrent, tiStopped)){
      env.repeatInvocation();
      return;
    }

    // stop thread in bottom half
    tiStopped.setStopped(throwableRef);
  }
}
