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
package gov.nasa.jpf.util;

/**
 * an object that holds a mutable int. Unfortunately, java.lang.Integer is
 * final, but we can at least be a Number
 */
public class MutableInteger extends Number {

  private int value;
  
  public MutableInteger (int val){
    value = val;
  }
  
  public void set (int val){
    value = val;
  }
  
  //--- arithmetic operations
  public MutableInteger inc() {
    value++;
    return this;
  }
  
  public MutableInteger dec() {
    value--;
    return this;
  }
  
  public MutableInteger add (int n){
    value += n;
    return this;
  }
  
  public MutableInteger subtract (int n){
    value -= n;
    return this;
  }
  
  public MutableInteger multiply (int n){
    value *= n;
    return this;
  }
  
  public MutableInteger divide (int n){
    value /= n;
    return this;
  }
  
  //-- Hmm, we probably want to round correctly for these
  public MutableInteger add (Number n){
    value += n.intValue();
    return this;
  }
  
  public MutableInteger subtract (Number n){
    value -= n.intValue();
    return this;
  }
  
  public MutableInteger multiply (Number n){
    value *= n.intValue();
    return this;
  }
  
  public MutableInteger divide (Number n){
    value /= n.intValue();
    return this;
  }
  
  //--- value accessors
  
  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public float floatValue() {
    return value;
  }

  @Override
  public int intValue() {
    return value;
  }

  @Override
  public long longValue() {
    return value;
  }
}
