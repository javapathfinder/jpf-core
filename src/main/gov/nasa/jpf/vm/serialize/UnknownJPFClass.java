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

package gov.nasa.jpf.vm.serialize;

/**
 * exception to indicate that JPF can't find a (model) class of a given name
 * For native clients directly or indirectly cause class resolution
 *
 * <2do> we might turn this into an handled exception at some point
 */
public class UnknownJPFClass extends RuntimeException {
  protected String clsName;

  public UnknownJPFClass(String clsName){
    this.clsName = clsName;
  }

  String getRequestedClassName() {
    return clsName;
  }

  @Override
  public String getMessage(){
    return clsName;
  }
}
