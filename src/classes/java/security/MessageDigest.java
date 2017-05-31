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

package java.security;

/**
 * forwarding implementation of MessageDigest.
 * <2do> only partially implemented (as required by serialization)
 */
public class MessageDigest extends MessageDigestSpi {

  String algorithm;
  int id; // set by native peer
  
  private native int init0 (String algorithm);
  
  protected MessageDigest (String algorithm) throws NoSuchAlgorithmException {
    if (!algorithm.equalsIgnoreCase("SHA") && !algorithm.equalsIgnoreCase("MD5")){
      throw new NoSuchAlgorithmException("unknown algorithm: " + algorithm);
    }
    
    this.algorithm = algorithm;
    id = init0(algorithm);
  }
  
  public static MessageDigest getInstance (String algorithm) throws NoSuchAlgorithmException {
    return new MessageDigest(algorithm); // keep it simple
  }
  
  public native byte[] digest (byte[] input);
  
  public native byte[] digest ();

  public native void update(byte[] input);

  @Override
  protected native void finalize(); // to clean up
  
  // those are required by the compiler, but never used since we forward
  // all public methods
  @Override
  protected native byte[] engineDigest ();

  @Override
  protected native void engineReset ();

  @Override
  protected native void engineUpdate (byte input);

  @Override
  protected native void engineUpdate (byte[] input, int offset, int len);

}
