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

import java.lang.reflect.Constructor;

public class SerializationConstructor<T> extends Constructor<T> {

  // those are set by the the sun.reflect.ReflectionFactory
  Class<T> mdc;
  Constructor<?> firstNonSerializableCtor;
  
  @Override
  public Class<T> getDeclaringClass() {
    return mdc;
  }
  
  // this has to be native because we need to create a new object of the
  // mdc without initializing it
  @Override
  public native T newInstance (Object... args);
  
  @Override
  public boolean isSynthetic () {
    return true;
  }

}
