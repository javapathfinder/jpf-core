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

import gov.nasa.jpf.SystemAttribute;

/**
 * a SystemAttribute to mark global scheduling points.
 * 
 * While we can identify process context switches through the ThreadInfo/AooCtx
 * of respective CG instances, we could not identify process global scheduling
 * choices that include threads of the current process
 */
public class GlobalSchedulingPoint implements SystemAttribute {
  
  static final GlobalSchedulingPoint singleton = new GlobalSchedulingPoint();
  
  public static void setGlobal (ChoiceGenerator<?> cg){
    cg.addAttr(singleton);
  }
  
  public static boolean isGlobal (ChoiceGenerator<?> cg){
    return (cg != null) && cg.hasAttr(GlobalSchedulingPoint.class);
  }
}
