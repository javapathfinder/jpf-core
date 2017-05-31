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


/**
 * field info for object fields
 */
public class ReferenceFieldInfo extends SingleSlotFieldInfo {
  int init;  //  = MJIEnv.NULL; // not required for MJIEnv.NULL = 0
  
  String sInit; // <2do> pcm - just a temporary quirk to init from string literals
                // check if there are other non-object reference inits

  public ReferenceFieldInfo (String name, String type, int modifiers) {
    super(name, type, modifiers);
  }

  @Override
  public String valueToString (Fields f) {
    int i = f.getIntValue(storageOffset);
    if (i == MJIEnv.NULL) {
      return "null";
    } else {
      return (VM.getVM().getHeap().get(i)).toString();
    }
  }

  @Override
  public boolean isReference () {
    return true;
  }

  @Override
  public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType() {
    return ReferenceChoiceGenerator.class;
  }

  @Override
  public boolean isArrayField () {
    return ci.isArray;
  }

  @Override
  public void setConstantValue (Object constValue){
    // <2do> pcm - check what other constants we might encounter, this is most
    // probably not just used for Strings.
    // Besides the type issue, there is an even bigger problem with identities.
    // For instance, all String refs initialized via the same string literal
    // inside a single classfile are in fact refering to the same object. This
    // means we have to keep a registry (hashtab) with string-literal created
    // String objects per ClassInfo, and use this when we assign or init
    // String references.
    // For the sake of progress, we ignore this for now, but have to come back
    // to it because it violates the VM spec

    if (constValue instanceof String){
      cv = constValue;
      sInit = (String)constValue;
    } else {
      throw new JPFException ("unsupported reference initialization: " + constValue);
    }
  }

  @Override
  public void initialize (ElementInfo ei, ThreadInfo ti) {
    int ref = init;
    if (sInit != null) {
      VM vm = ti.getVM();
      Heap heap = vm.getHeap();
      ref = heap.newString(sInit, ti).getObjectRef();
    }
    ei.getFields().setReferenceValue( storageOffset, ref);
  }

  @Override
  public Object getValueObject (Fields f){
    int i = f.getIntValue(storageOffset);
    if (i == MJIEnv.NULL) {
      return null;
    } else {
      Heap heap = VM.getVM().getHeap();
      return heap.get(i);
    }
  }
}
