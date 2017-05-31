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
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * MJI NativePeer class for java.lang.Math library abstraction
 */
public class JPF_java_lang_Math extends NativePeer {
  
  // <2do> those are here to hide their implementation from traces, not to
  // increase performance. If we want to do that, we should probably inline
  // their real implementation here, instead of delegating (just a compromise)
  @MJI
  public double abs__D__D (MJIEnv env, int clsObjRef, double a) {
    // return Math.abs(a);
    
    return (a <= .0) ? (.0 - a) : a;
  }

  @MJI
  public float abs__F__F (MJIEnv env, int clsObjRef, float a) {
    return Math.abs(a);
  }

  @MJI
  public int abs__I__I (MJIEnv env, int clsObjRef, int a) {
    //return Math.abs(a);
    return (a < 0) ? -a : a; // that's probably slightly faster
  }

  @MJI
  public long abs__J__J (MJIEnv env, int clsObjRef, long a) {
    //return Math.abs(a);
    
    return (a < 0) ? -a : a;
  }

  @MJI
  public double max__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
    // that one has to handle inexact numbers, so it's probably not worth the hassle
    // to inline it
    return Math.max(a, b);
  }

  @MJI
  public float max__FF__F (MJIEnv env, int clsObjRef, float a, float b) {
    return Math.max(a, b);
  }

  @MJI
  public int max__II__I (MJIEnv env, int clsObjRef, int a, int b) {
    //return Math.max(a, b);
    
    return (a >= b) ? a : b;
  }

  @MJI
  public long max__JJ__J (MJIEnv env, int clsObjRef, long a, long b) {
    //return Math.max(a, b);
    return (a >= b) ? a : b;
  }

  @MJI
  public double min__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
    return Math.min(a, b);
  }

  @MJI
  public float min__FF__F (MJIEnv env, int clsObjRef, float a, float b) {
    return Math.min(a, b);
  }

  @MJI
  public int min__II__I (MJIEnv env, int clsObjRef, int a, int b) {
    return Math.min(a, b);
  }

  @MJI
  public long min__JJ__J (MJIEnv env, int clsObjRef, long a, long b) {
    return Math.min(a, b);
  }

  @MJI
  public double pow__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
    return Math.pow(a, b);
  }

  @MJI
  public double sqrt__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.sqrt(a);
  }
  
  @MJI
  public double random____D (MJIEnv env, int clsObjRef) {
    return Math.random();
  }
  
  @MJI
  public long round__D__J (MJIEnv env, int clsObjRef, double a){
    return Math.round(a);
  }
  
  @MJI
  public double exp__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.exp(a);
  }
  
  @MJI
  public double asin__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.asin(a);
  }

  @MJI
  public double acos__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.acos(a);
  }
  
  @MJI
  public double atan__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.atan(a);
  }
  
  @MJI
  public double atan2__DD__D (MJIEnv env, int clsObjRef, double a, double b) {
    return Math.atan2(a,b);
  }
  
  @MJI
  public double ceil__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.ceil(a);
  }
  
  @MJI
  public double cos__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.cos(a);
  }
  
  @MJI
  public double floor__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.floor(a);
  }
  
  @MJI
  public double log10__D__D (MJIEnv env, int clsObjRef, double a) {
	return Math.log10(a);
  }  
  
  @MJI
  public double log__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.log(a);
  }
  
  @MJI
  public double rint__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.rint(a);
  }
  
  @MJI
  public double sin__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.sin(a);
  }
  
  @MJI
  public double tan__D__D (MJIEnv env, int clsObjRef, double a) {
    return Math.tan(a);
  }
}
