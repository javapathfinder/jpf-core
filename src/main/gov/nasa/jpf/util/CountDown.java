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

final class CountDown extends RuntimeException {
  private int remaining;
  
  CountDown (int remaining){
    this.remaining = remaining;
  }
  
  public final int dec(){
    if (remaining <= 0){
      throw this;
    }
    remaining--;
    return remaining;
  }
  
  public final void expire (){
    remaining = 0;
  }
  
  public final void set(int remaining){
    this.remaining = remaining;
  }
}