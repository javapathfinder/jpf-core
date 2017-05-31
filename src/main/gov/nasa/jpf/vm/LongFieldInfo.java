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
 *
 */
public class LongFieldInfo extends DoubleSlotFieldInfo {
  long init;

  public LongFieldInfo (String name, int modifiers) {
    super(name, "J", modifiers);
  }

  @Override
  public void setConstantValue(Object constValue){
    if (constValue instanceof Long){
      cv = constValue;
      init = (Long)constValue;

    } else {
      throw new JPFException("illegal long ConstValue=" + constValue);
    }
  }

  @Override
  public void initialize (ElementInfo ei, ThreadInfo ti) {
    ei.getFields().setLongValue( storageOffset, init);
  }

  @Override
  public int getStorageSize() {
    return 2;
  }

  @Override
  public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType() {
    return LongChoiceGenerator.class;
  }

  @Override
  public String valueToString (Fields f) {
    long v = f.getLongValue(storageOffset);
    return Long.toString(v);
  }

  @Override
  public Object getValueObject (Fields f){
    long v = f.getLongValue(storageOffset);
    return new Long(v);
  }

  @Override
  public boolean isLongField(){
    return true;
  }

  @Override
  public boolean isNumericField(){
    return true;
  }
}

