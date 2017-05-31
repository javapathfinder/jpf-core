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
 * MJI NativePeer class to intercept all System.out and System.err
 * printing. We handle all of this native, since it's already slow enough
 */
public class JPF_gov_nasa_jpf_ConsoleOutputStream extends NativePeer {
  
  /****************************************************************************
   * these are the native methods we intercept
   */

  @MJI
  public void $init____V (MJIEnv env, int objref) {
    // that's just a dummy because we have no OutputStream, which would cause
    // an exception in the PrintStream ctor
  }
  
  @MJI
  public void print__C__V (MJIEnv env, int objref, char c) {
    env.getVM().print(c);
  }

  @MJI
  public void print__D__V (MJIEnv env, int objref, double d) {
    env.getVM().print(d);
  }

  @MJI
  public void print__F__V (MJIEnv env, int objref, float f) {
    env.getVM().print(f);
  }

  @MJI
  public void print__I__V (MJIEnv env, int objref, int i) {
    env.getVM().print(i);
  }

  @MJI
  public void print__J__V (MJIEnv env, int objref, long j) {
    env.getVM().print(j);
  }

  @MJI
  public void print__Ljava_lang_String_2__V (MJIEnv env, int objRef,
                                                 int strRef) {
    env.getVM().print(env.getStringObject(strRef));
  }

  @MJI
  public void print__Z__V (MJIEnv env, int objref, boolean z) {
    env.getVM().print(z);
  }

  @MJI
  public void println____V (MJIEnv env, int objRef) {
    env.getVM().println();
  }

  @MJI
  public void println__C__V (MJIEnv env, int objref, char c) {
    env.getVM().print(c);
    env.getVM().println();
  }

  @MJI
  public void println__D__V (MJIEnv env, int objref, double d) {
    env.getVM().print(d);
    env.getVM().println();
  }

  @MJI
  public void println__F__V (MJIEnv env, int objref, float f) {
    env.getVM().print(f);
    env.getVM().println();
  }

  @MJI
  public void println__I__V (MJIEnv env, int objref, int i) {
    env.getVM().print(i);
    env.getVM().println();
  }

  @MJI
  public void println__J__V (MJIEnv env, int objref, long j) {
    env.getVM().print(j);
    env.getVM().println();
  }

  @MJI
  public void println__Ljava_lang_String_2__V (MJIEnv env, int objRef,
                                                   int strRef) {
    env.getVM().println(env.getStringObject(strRef));
  }

  @MJI
  public void write__I__V (MJIEnv env, int objRef, int b){
    env.getVM().print((char)(byte)b);
  }
  
  @MJI
  public void write___3BII__V (MJIEnv env, int objRef,
                                      int bufRef, int off, int len){
    
  }

  @MJI
  public void println__Z__V (MJIEnv env, int objref, boolean z) {
    env.getVM().print(z);
    env.getVM().println();
  }

  @MJI
  public int printf__Ljava_lang_String_2_3Ljava_lang_Object_2__Ljava_io_PrintStream_2
                   (MJIEnv env, int objref, int fmtRef, int argRef) {
    env.getVM().print(env.format(fmtRef,argRef));
    return objref;
  }
  
}
