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

package gov.nasa.jpf;

/**
 * if this class can be loaded, the JPF core is in the CLASSPATH.
 * 
 * The reason to use an empty, non-instantiable tag class for this is to make
 * sure we don't get conflicts between CLs - this class should not be used
 * for anything but to check if it can be found
 *
 * NOTE - this class has to be reachable through the same CP entry like
 * gov.nasa.jpf.JPF and gov.nasa.jpf.Config
 */
public final class $coreTag {
  private $coreTag() {
    // nobody can call this
  }
}
