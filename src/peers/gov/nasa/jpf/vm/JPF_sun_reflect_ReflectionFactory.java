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
 * <2do> hack around a hack - we need to override this as long as we don't
 * replace ObjectStreamClass 
 */
public class JPF_sun_reflect_ReflectionFactory extends NativePeer {

  @MJI
  public int newConstructorForSerialization__Ljava_lang_Class_2Ljava_lang_reflect_Constructor_2__Ljava_lang_reflect_Constructor_2 (MJIEnv env, int objRef,
                                                                                                                                          int clsRef, int ctorRef){
    // NOPE - unnless I miss my guess, this creates an artificial
    // ctor for the concrete type that explicitly calls the default ctor of the
    // first non-serializable superclass. Oh my!

    // <2do> we really have to model ObjectStreamClass and ObjectStreamField
    ClassInfo ci = ClassInfo.getInitializedClassInfo("gov.nasa.jpf.SerializationConstructor", env.getThreadInfo());
    int sCtorRef = env.newObject(ci);
    
    env.setReferenceField(sCtorRef, "mdc", clsRef);
    env.setReferenceField(sCtorRef, "firstNonSerializableCtor", ctorRef);
    
    return sCtorRef;
  }
}
