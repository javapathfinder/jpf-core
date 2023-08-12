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

public class JPF_java_lang_invoke_JPFFieldVarHandle extends NativePeer {

  @MJI
  public int get__Ljava_lang_Object_2__I(MJIEnv env, int objRef, int ownerRef) {
    int fieldIndex = env.getIntField(objRef, "fieldRef");
    ElementInfo ownerObj = env.getModifiableElementInfo(ownerRef);
    FieldInfo targetField = ownerObj.getFieldInfo(fieldIndex);
    return ownerObj.getIntField(targetField);
  }

  @MJI
  public boolean compareAndSet__Ljava_lang_Object_2II__Z(
      MJIEnv env, int objRef, int absRef, int expected, int update) {
    int filedRef = env.getIntField(objRef, "fieldRef");
    ElementInfo absEi = env.getModifiableElementInfo(absRef);
    FieldInfo targetField = absEi.getFieldInfo(filedRef);
    int value = absEi.getIntField(targetField);

    if (value == expected) {
      absEi.setIntField(targetField, update);
      return true;
    } else {
      return false;
    }
  }

  @MJI
  public boolean compareAndSet__Ljava_lang_Object_2JJ__Z(
      MJIEnv env, int objRef, int ownerRef, long expected, long update) {
    int fieldIndex = env.getIntField(objRef, "fieldRef");
    ElementInfo ownerObj = env.getModifiableElementInfo(ownerRef);
    FieldInfo targetField = ownerObj.getFieldInfo(fieldIndex);
    long value = ownerObj.getLongField(targetField);

    if (value == expected) {
      ownerObj.setLongField(targetField, update);
      return true;
    } else {
      return false;
    }
  }

  @MJI
  public boolean compareAndSet__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_Object_2__Z(
      MJIEnv env, int objRef, int absRef, int expected, int update) {
    int filedRef = env.getIntField(objRef, "fieldRef");
    ElementInfo absEi = env.getModifiableElementInfo(absRef);
    FieldInfo targetField = absEi.getFieldInfo(filedRef);
    int value = absEi.getReferenceField(targetField);

    if (value == expected) {
      absEi.setReferenceField(targetField, update);
      return true;
    } else {
      return false;
    }
  }


  @MJI
  public void set__Ljava_lang_Object_2I__V(MJIEnv env,
      int objRef, int fieldOwnerRef, int value) {
    int filedRef = env.getIntField(objRef, "fieldRef");
    ElementInfo absEi = env.getModifiableElementInfo(fieldOwnerRef);
    FieldInfo targetField = absEi.getFieldInfo(filedRef);
    absEi.setIntField(targetField, value);
  }

  @MJI
  public void set__Ljava_lang_Object_2Ljava_lang_Object_2__V(MJIEnv env,
      int objRef, int fieldOwnerRef, int valueRef) {
    int filedRef = env.getIntField(objRef, "fieldRef");
    ElementInfo absEi = env.getModifiableElementInfo(fieldOwnerRef);
    FieldInfo targetField = absEi.getFieldInfo(filedRef);
    absEi.setReferenceField(targetField, valueRef);
  }
}
