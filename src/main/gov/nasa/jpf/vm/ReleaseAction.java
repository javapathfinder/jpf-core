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

/**
 * interface for actions to be taken when gc'ing objects that are no longer 
 * reachable. 
 * 
 * This is meant to be used for types that have native companion objects
 * which need to be cleaned up. We can't use Object.finalize() for this because
 * this can be overridden in user code, and actually can make objects live again
 */
public interface ReleaseAction {
  
  /**
   * object is about to be terminally released (no more finalization etc.)
   */
  public void release (ElementInfo ei);
}
