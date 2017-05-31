/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.util.event;

import gov.nasa.jpf.vm.MJIEnv;

/**
 * event type that is not supposed to be handled by the SUT
 */
public abstract class SystemEvent extends Event {

  protected SystemEvent (String name, Object... arguments){
    super(name, arguments);
  }

  @Override
  public boolean isSystemEvent(){
    return true;
  }

  public void process (MJIEnv env, int objRef){
    // nothing, optionally overridden by subclass
  }

}
