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
 * type, name and attribute information for 'double' fields
 */
public class DoubleFieldInfo extends DoubleSlotFieldInfo {
  double init;

  public DoubleFieldInfo (String name, int modifiers) {
    super(name, "D", modifiers);
  }

  @Override
  public void setConstantValue(Object constValue){
    if (constValue instanceof Double){
      cv = constValue;
      init = ((Double)constValue).doubleValue();

    } else {
      throw new JPFException("illegal boolean ConstValue=" + constValue);
    }
  }


  @Override
  public void initialize (ElementInfo ei, ThreadInfo ti) {
    ei.getFields().setDoubleValue(storageOffset, init);
  }

  @Override
  public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType() {
    return DoubleChoiceGenerator.class;
  }

  @Override
  public int getStorageSize () {
    return 2;
  }

  @Override
  public String valueToString (Fields f) {
    double d = f.getDoubleValue(storageOffset);
    return Double.toString(d);
  }

  @Override
  public Object getValueObject (Fields f){
    double d = f.getDoubleValue(storageOffset);
    return new Double(d);
  }

  @Override
  public boolean isDoubleField(){
    return true;
  }

  @Override
  public boolean isNumericField(){
    return true;
  }

  @Override
  public boolean isFloatingPointField(){
    return true;
  }
}
