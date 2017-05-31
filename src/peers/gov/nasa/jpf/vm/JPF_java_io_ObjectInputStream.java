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

public class JPF_java_io_ObjectInputStream extends NativePeer {

  @MJI
  public int latestUserDefinedLoader____Ljava_lang_ClassLoader_2 (MJIEnv env, int clsRef){
    // class loaders are not yet supported
    return MJIEnv.NULL;
  }
  
  @MJI
  public void bytesToDoubles___3BI_3DII__ (MJIEnv env, int clsRef,
                                                  int baRef, int bOff,
                                                  int daRef, int dOff,
                                                  int nDoubles){
    int imax = dOff + nDoubles;
    int j=bOff;
    
    for (int i=dOff; i<imax; i++){
      byte b0 = env.getByteArrayElement(baRef, j++);
      byte b1 = env.getByteArrayElement(baRef, j++);
      byte b2 = env.getByteArrayElement(baRef, j++);
      byte b3 = env.getByteArrayElement(baRef, j++);
      byte b4 = env.getByteArrayElement(baRef, j++);
      byte b5 = env.getByteArrayElement(baRef, j++);
      byte b6 = env.getByteArrayElement(baRef, j++);
      byte b7 = env.getByteArrayElement(baRef, j++);
      
      long bits = 0x00000000000000ff & b7;
      bits <<= 8;
      bits |= 0x00000000000000ff & b6;
      bits <<= 8;
      bits |= 0x00000000000000ff & b5;
      bits <<= 8;
      bits |= 0x00000000000000ff & b4;
      bits <<= 8;
      bits |= 0x00000000000000ff & b3;
      bits <<= 8;
      bits |= 0x00000000000000ff & b2;
      bits <<= 8;
      bits |= 0x00000000000000ff & b1;
      bits <<= 8;
      bits |= 0x00000000000000ff & b0;
      
      double d = Double.longBitsToDouble(bits);
      env.setDoubleArrayElement(daRef, i, d);
    }
  }

  @MJI
  public void bytesToFloats___3BI_3FII__ (MJIEnv env, int clsRef,
                                                 int baRef, int bOff,
                                                 int faRef, int fOff,
                                                 int nFloats){
    int imax = fOff + nFloats;
    int j=bOff;

    for (int i=fOff; i<imax; i++){
      byte b0 = env.getByteArrayElement(baRef, j++);
      byte b1 = env.getByteArrayElement(baRef, j++);
      byte b2 = env.getByteArrayElement(baRef, j++);
      byte b3 = env.getByteArrayElement(baRef, j++);

      int bits = 0x000000ff & b3;
      bits <<= 8;
      bits |= 0x000000ff & b2;
      bits <<= 8;
      bits |= 0x000000ff & b1;
      bits <<= 8;
      bits |= 0x000000ff & b0;

      float f = Float.intBitsToFloat(bits);
      env.setFloatArrayElement(faRef, i, f);
    }
  }
}
