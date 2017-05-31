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


public class JPF_gov_nasa_jpf_SerializationConstructor extends NativePeer {

  /**
   * create a new instance, but only call the ctor of the first
   * non-serializable superclass
   */
  @MJI
  public int newInstance___3Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int mthRef, int argsRef) {
    ThreadInfo ti = env.getThreadInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();
    
    int superCtorRef = env.getReferenceField(mthRef, "firstNonSerializableCtor"); 
    MethodInfo miCtor = JPF_java_lang_reflect_Constructor.getMethodInfo(env,superCtorRef);

    if (frame == null){ // first time
      int clsRef = env.getReferenceField(mthRef, "mdc");
      ClassInfo ci = env.getReferredClassInfo( clsRef);

      if (ci.isAbstract()){
        env.throwException("java.lang.InstantiationException");
        return MJIEnv.NULL;
      }

      int objRef = env.newObjectOfUncheckedClass(ci);
      frame = miCtor.createDirectCallStackFrame(ti, 1); 
      frame.setReferenceArgument( 0, objRef, null);
      frame.setLocalReferenceVariable(0, objRef); // (1) we store the reference as a local var for retrieval during reexec      
      ti.pushFrame(frame);
      
      // check for & push required clinits
      ci.initializeClass(ti);
      env.repeatInvocation();
      return MJIEnv.NULL;
      
    } else { // re-execution, 
      int objRef = frame.getLocalVariable(0); // that's the object ref we stored in (1)
      return objRef;
    }
  }
}
