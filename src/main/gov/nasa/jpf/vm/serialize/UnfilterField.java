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


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that a field in the model SHOULD BE considered during
 * state matching, even if other (prior) configuration would filter it.
 * @author peterd
 */
@Target({ElementType.FIELD})
public @interface UnfilterField {
  /**
   * If not the empty string, specifies a property that must be "true" to
   * activate unfiltering--unless <code>invert</code> is set.
   */
  String condition() default "";
  
  /**
   * If set to <code>true</code>, property must be "false" to activate
   * unfiltering. Does nothing if <code>condition</code> is empty string.
   */
  boolean invert() default false;
}
