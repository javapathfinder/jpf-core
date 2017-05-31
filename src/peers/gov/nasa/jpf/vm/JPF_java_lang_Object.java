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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * MJI NativePeer class for java.lang.Object library abstraction
 */
public class JPF_java_lang_Object extends NativePeer {
  
  @MJI
  public int getClass____Ljava_lang_Class_2 (MJIEnv env, int objref) {
    ClassInfo oci = env.getClassInfo(objref);

    return oci.getClassObjectRef();
  }

  @MJI
  public int clone____Ljava_lang_Object_2 (MJIEnv env, int objref) {
    Heap heap = env.getHeap();
    ElementInfo ei = heap.get(objref);
    ClassInfo ci = ei.getClassInfo();
    ElementInfo eiClone = null;
    
    if (!ci.isInstanceOf("java.lang.Cloneable")) {
      env.throwException("java.lang.CloneNotSupportedException",
          ci.getName() + " does not implement java.lang.Cloneable.");
      return MJIEnv.NULL;  // meaningless
      
    } else {
      int newref;
      if (ci.isArray()) {
        ClassInfo cci = ci.getComponentClassInfo();
        
        String componentType;
        if (cci.isPrimitive()){
          componentType = Types.getTypeSignature(cci.getName(),false);
        } else {
          componentType = cci.getType();
        }

        eiClone = heap.newArray(componentType, ei.arrayLength(), env.getThreadInfo());
        
      } else {
        eiClone = heap.newObject(ci, env.getThreadInfo());
      }
      
      // Ok, this is nasty but efficient
      eiClone.fields = ei.getFields().clone();

      return eiClone.getObjectRef();
    }
  }

  @MJI
  public int hashCode____I (MJIEnv env, int objref) {
    return (objref ^ 0xABCD);
  }

  protected void wait0 (MJIEnv env, int objref, long timeout) {
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = env.getModifiableElementInfo(objref);
    
    if (!ti.isFirstStepInsn()) {
      if (!ei.isLockedBy(ti)) {
        env.throwException("java.lang.IllegalMonitorStateException", "wait() without holding lock");
        return;
      }

      if (ti.isInterrupted(true)) {
        env.throwException("java.lang.InterruptedException");
      } else {
        ei.wait(ti, timeout); // block
      }
    }
    
    // scheduling point
    if (ti.getScheduler().setsWaitCG(ti, timeout)) {
      env.repeatInvocation();
      return;
    }
    
    // bottom half, unblock
    switch (ti.getState()) {
      case WAITING:
      case TIMEOUT_WAITING:
        throw new JPFException("blocking wait() without transition break");      
      
      // we can get here by direct call from ...Unsafe.park__ZJ__V()
      // which aquires the park lock and waits natively
      case RUNNING:

      // note that we can't get here if we are in NOTIFIED or INTERRUPTED state,
      // since we still have to reacquire the lock
      case UNBLOCKED:
      case TIMEDOUT: // nobody else acquired the lock
        // thread status set by explicit notify() call
        env.lockNotified(objref);

        if (ti.isInterrupted(true)) {
          env.throwException("java.lang.InterruptedException");
        }
        break;

      default:
        throw new JPFException("invalid thread state of: " + ti.getName() + " is " + ti.getStateName()
                + " while waiting on " + ei);
    }
  }
  
  // we intercept them both so that we don't get the java.lang.Object.wait() location
  // as the blocking insn
  @MJI
  public void wait____V (MJIEnv env, int objref){
    wait0(env,objref,0);
  }
  
  @MJI
  public void wait__J__V (MJIEnv env, int objref, long timeout) {
    wait0(env,objref,timeout);
  }

  @MJI
  public void wait__JI__V (MJIEnv env, int objref, long timeout, int nanos) {
    wait0(env,objref,timeout);
  }

  
  @MJI
  public void notify____V (MJIEnv env, int objref) {
    boolean didNotify = false;
    ThreadInfo ti = env.getThreadInfo();
    
    if (!ti.isFirstStepInsn()) {
      ElementInfo ei = env.getModifiableElementInfo(objref);
      if (!ei.isLockedBy(ti)) {
        env.throwException("java.lang.IllegalMonitorStateException", "notify() without holding lock");
        return;
      }
      
      didNotify = env.notify(ei);
    }
    
    if (ti.getScheduler().setsNotifyCG(ti, didNotify)){
      env.repeatInvocation();
      return;
    }
  }

  @MJI
  public void notifyAll____V (MJIEnv env, int objref) {
    boolean didNotify = false;
    ThreadInfo ti = env.getThreadInfo();
    
    if (!ti.isFirstStepInsn()) {
      ElementInfo ei = env.getModifiableElementInfo(objref);
      if (!ei.isLockedBy(ti)) {
        env.throwException("java.lang.IllegalMonitorStateException", "notifyAll() without holding lock");
        return;
      }
      
      didNotify = env.notifyAll(ei);
    }
    
    if (ti.getScheduler().setsNotifyCG(ti, didNotify)){
      env.repeatInvocation();
      return;
    }
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objref) {
    ClassInfo ci = env.getClassInfo(objref);
    int hc = hashCode____I(env,objref);
    
    String s = ci.getName() + '@' + hc;
    int sref = env.newString(s);
    return sref;
  }
}
