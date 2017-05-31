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

import gov.nasa.jpf.Config;


public class DefaultFilterConfiguration extends AmmendableFilterConfiguration {
  @Override
  public void init(Config config) {
    // these built-in come first
    appendStaticAmmendment(IgnoreConstants.instance);
    appendInstanceAmmendment(IgnoreReflectiveNames.instance);
    appendFieldAmmendment(IgnoreThreadNastiness.instance);
    appendFieldAmmendment(IgnoreUtilSilliness.instance);
    
    // ignores (e.g. NoMatch) from annotations
    IgnoresFromAnnotations ignores = new IgnoresFromAnnotations(config); 
    appendFieldAmmendment(ignores);
    appendFrameAmmendment(ignores);
    
    // configured via properties
    super.init(config);
    
    // includes (e.g. ForceMatch) from annotations
    IncludesFromAnnotations includes = new IncludesFromAnnotations(config); 
    appendFieldAmmendment(includes);
    //appendFrameAmmendment(includes);
  }
}
