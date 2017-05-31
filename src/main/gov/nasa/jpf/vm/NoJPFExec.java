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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.SystemAttribute;

/**
 * InfoObject attr that flags a certain construct (field, method, class) is not supposed
 * to be used under JPF. Useful for model classes that have or use features which have to be
 * cut off / abstracted by means of native peers, and we want to catch violations as
 * early as possible
 */
public class NoJPFExec implements SystemAttribute {

  // we only need one
  public static final NoJPFExec SINGLETON = new NoJPFExec();
}
