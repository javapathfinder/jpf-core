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
 * listener for gov.nasa.jpf.Config changes. Implementors
 * can register themselves upon initialization, to react to
 * downstream changes even if they cache or process Config
 * settings for increased performance.
 * 
 * the notification is per-key
 */
public interface ConfigChangeListener {

  /**
   * a JPF property was changed during runtime (e.g. by using the Verify API
   * or encountering annotations)
   */
  void propertyChanged (Config conf, String key, String oldValue, String newValue);
  
  /**
   *  this can be used to let a config listener remove itself, which is
   *  required if the same Config object is used for several JPF runs
   */
  void jpfRunTerminated (Config conf);
}
