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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JPF_java_security_MessageDigest extends NativePeer {
  
  MessageDigest[] digests;
  
  public JPF_java_security_MessageDigest (Config conf){
    digests = new MessageDigest[32];
  }
  
  int getNewIndex() {
    int n = digests.length;
    for (int i=0; i<n; i++){
      if (digests[i] == null){
        return i;
      }
    }
    
    MessageDigest[] newd = new MessageDigest[n + 32];
    System.arraycopy(digests,0,newd,0,digests.length);
    digests = newd;
    return n;
  }
  
  MessageDigest getDigest (MJIEnv env, int objRef){
    int id = env.getIntField(objRef, "id");
    return digests[id];
  }
  
  @MJI
  public int init0__Ljava_lang_String_2__I (MJIEnv env, int objRef, int algRef) {
    String algorithm = env.getStringObject(algRef);
    
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
    
      int id = getNewIndex();
      digests[id] = md;
    
      return id;
    } catch (NoSuchAlgorithmException x){
      env.throwException("java.security.NoSuchAlgorithmException", algorithm);
      return -1;
    }
  }
  
  @MJI
  public int digest___3B___3B (MJIEnv env, int objRef, int inputRef){
    MessageDigest md = getDigest(env, objRef);
    byte[] input = env.getByteArrayObject(inputRef);
    
    byte[] res = md.digest(input);
    return env.newByteArray(res);
  }

  @MJI
  public int digest_____3B (MJIEnv env, int objRef){
    MessageDigest md = getDigest(env, objRef);    
    byte[] res = md.digest();
    return env.newByteArray(res);
  }
  
  @MJI
  public void finalize____ (MJIEnv env, int objRef){
    int id = env.getIntField(objRef, "id");
    digests[id] = null;
  }

  @MJI
  public void update___3B__V (MJIEnv env, int objRef, int inputRef){
    MessageDigest md = getDigest(env, objRef);
    byte[] input = env.getByteArrayObject(inputRef);
    md.update(input);
  }
}
