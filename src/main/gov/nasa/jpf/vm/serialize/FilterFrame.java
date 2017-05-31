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
 * USE CAREFULLY - Indicates that the stack frame of a method should not,
 * in specified ways, be considered during state matching.
 * 
 * This can easily cause the search to be cut off even though the VM has made
 * progress, so USE WISELY!
 * 
 * @author peterd
 */
@Target({ElementType.METHOD})
public @interface FilterFrame {
  /**
   * True means locals (incl. parameters) and operand stack will be filtered.
   */
  boolean filterData() default true;

  /**
   * True means the location of the next instruction will be filtered.
   */
  boolean filterPC() default true;

  /**
   * True means frames below this one will not appear at all in the abstracted
   * state.
   */
  boolean filterSubframes() default true;
}