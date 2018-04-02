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

package java.util;

public class Random {

  // the state of this object
  private long seed;
  
  public Random(){
    // intercepted by native peer to control seed initialization based on JPF configuration
  }
  
  public Random(long seed) {
    // intercepted by native peer to control seed initialization based on JPF configuration
  }
  
  public synchronized void setSeed(long seed){
    // intercepted by native peer
  }
  
  protected int next(int bits){
    return 42; // intercepted by peer
  }
  
  public void nextBytes(byte[] data){
    // intercepted by peer
  }
  
  public int nextInt(){
    return 42; // intercepted by peer    
  }
  
  public int nextInt(int n) {
    return 42; // intercepted by peer
  }
  
  public long nextLong() {
    return 42; // intercepted by peer
  }
  
  public boolean nextBoolean() {
    return true; // intercepted by peer
  }
  
  public float nextFloat() {
    return 42f; // intercepted by peer
  }
  
  public double nextDouble() {
    return 42.0; // intercepted by peer
  }
  
  public synchronized double nextGaussian() {
    return 42.0; // intercepted by peer    
  }
}
