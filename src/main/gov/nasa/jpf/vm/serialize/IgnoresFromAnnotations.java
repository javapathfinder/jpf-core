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
import gov.nasa.jpf.annotation.FilterField;
import gov.nasa.jpf.annotation.FilterFrame;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.serialize.AmmendableFilterConfiguration.FieldAmmendment;
import gov.nasa.jpf.vm.serialize.AmmendableFilterConfiguration.FrameAmmendment;

public class IgnoresFromAnnotations
implements FieldAmmendment, FrameAmmendment {
  protected Config config;
  
  public IgnoresFromAnnotations(Config config) {
    this.config = config;
  }
  
  @Override
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    AnnotationInfo ann = fi.getAnnotation(FilterField.class.getName());
    if (ann != null){
      String condition = ann.getValueAsString("condition");
      boolean invert = ann.getValueAsBoolean("invert");
      if ((condition == null) || condition.isEmpty() || (config.getBoolean(condition)) == !invert ) {
        return POLICY_IGNORE;
      }
    }
    
    return sofar;
  }

  @Override
  public FramePolicy ammendFramePolicy(MethodInfo mi, FramePolicy sofar) {
    AnnotationInfo ann = mi.getAnnotation(FilterFrame.class.getName());
    if (ann != null) {
      if (ann.getValueAsBoolean("filterData")) {
        sofar.includeLocals = false;
        sofar.includeOps = false;
      }
      if (ann.getValueAsBoolean("filterPC")) {
        sofar.includePC = false;
      }
      if (ann.getValueAsBoolean("filterSubframes")) {
        sofar.recurse = false;
      }
    }
    return sofar;
  }

}
