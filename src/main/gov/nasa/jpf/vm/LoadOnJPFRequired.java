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
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * This is used where a customized classloader loads a class, and needs
 * to resolve it. To do the resolve, a standard VM invokes the classloader 
 * loadClass() method on the superclass. Therefore to mimic this JPF need
 * to do a round trip back and forth to JPF, to execute the user implemented
 * user code.
 */
public class LoadOnJPFRequired extends RuntimeException {
  String className;

  public LoadOnJPFRequired (String className){
    this.className = className;
  }
}
