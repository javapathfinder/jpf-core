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

package gov.nasa.jpf.vm.serialize;


public final class FramePolicy {
  public FramePolicy() {
    includeOps = true;
    includeLocals = true;
    includePC = true;
    recurse = true;
  }
  
  //May be migrated to BitArray or similar in the future.
  public boolean includeLocals;
  
  //May be migrated to BitArray or similar in the future.
  public boolean includeOps;
  
  /**
   * Whether to include instruction offset.
   */
  public boolean includePC;
  
  /**
   * Whether to considered frames "below" this one (called from here).
   */
  public boolean recurse;
  
  
  
  
  public boolean isDefaultPolicy() {
    return includeLocals && includeOps && includePC && recurse;
  }
  
  
  public void ignoreLocals() {
    includeLocals = false;
  }

  public void ignoreOps() {
    includeOps = false;
  }
  
  public void includeLocals() {
    includeLocals = true;
  }

  public void includeOps() {
    includeOps = true;
  }
}