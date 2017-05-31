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
import gov.nasa.jpf.util.Predicate;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * A native peer for FinalizerThread. This is also an interface between the FinalizerThread
 * object at the SUT level and it corresponding FinalizerThreadInfo object at the JPF
 * level. 
 */
public class JPF_gov_nasa_jpf_FinalizerThread extends NativePeer {
  
  @MJI
  public void runFinalizer__Ljava_lang_Object_2__V (MJIEnv env, int tiRef, int objRef) {
    int queueRef = env.getReferenceField(tiRef, "finalizeQueue");
    int[] elements = env.getReferenceArrayObject(queueRef);

    if(elements.length>0 && elements[0]==objRef) {
      ThreadInfo finalizerTi = env.getThreadInfo();
      ClassInfo objCi = env.getElementInfo(objRef).getClassInfo();
      MethodInfo mi = objCi.getMethod("finalize()V", false);
    
      // create and push a stack frame for finalize()V
      DirectCallStackFrame frame = mi.createDirectCallStackFrame(finalizerTi, 0);
      frame.setReferenceArgument(0, objRef, frame);
      finalizerTi.pushFrame(frame);
    
      removeElement(env, tiRef, objRef);
    }
  }
  
  // removes the very first element in the list, which is the last finalizable objects processed
  void removeElement(MJIEnv env, int tiRef, int objRef) {
    int queueRef = env.getReferenceField(tiRef, "finalizeQueue");
    ThreadInfo ti = env.getThreadInfo();
    int[] oldValues = env.getReferenceArrayObject(queueRef);
    
    assert (objRef == oldValues[0]);
    
    assert (oldValues.length>0);
    
    int len = oldValues.length - 1;
    ElementInfo newQueue = env.getHeap().newArray("Ljava/lang/Object;", len, ti);
    int[] newValues = newQueue.asReferenceArray();
    
    System.arraycopy(oldValues, 1, newValues, 0, len);
    env.getModifiableElementInfo(tiRef).setReferenceField("finalizeQueue", newQueue.getObjectRef());
  }
  
  // a predicate to obtain all alive, non-finalizer threads within the ti process 
  Predicate<ThreadInfo> getAppAliveUserPredicate (final ThreadInfo ti){
    return new Predicate<ThreadInfo>(){
      @Override
	public boolean isTrue (ThreadInfo t){
        return (t.isAlive() && !t.isSystemThread() && t.appCtx == ti.appCtx);
      }
    };
  }
  
  @MJI
  public void manageState____V (MJIEnv env, int objref){
    ApplicationContext appCtx = env.getVM().getApplicationContext(objref);
    FinalizerThreadInfo tiFinalizer = appCtx.getFinalizerThread();
    VM vm = env.getVM();
    
    // check for termination - Note that the finalizer thread has to be the last alive thread
    // of the process
    if(!vm.getThreadList().hasAnyMatching(getAppAliveUserPredicate(tiFinalizer))) {
      shutdown(env, objref);
    }
    // make the thread wait until more objects are added to finalizerQueue
    else {
      tiFinalizer.waitOnSemaphore();
      
      assert tiFinalizer.isWaiting();
      
      // this one has to consult the syncPolicy since there might be scheduling choices
      if (!tiFinalizer.getScheduler().setsPostFinalizeCG(tiFinalizer)){
        throw new JPFException("no transition break after finalization");
      }
    }
  }
  
  protected void shutdown(MJIEnv env, int objRef) {
    env.getModifiableElementInfo(objRef).setBooleanField("done", true);
  }
}
