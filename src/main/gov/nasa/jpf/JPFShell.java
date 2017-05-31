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
 * type that can be used to instantiate and run a JPF object
 * Shell objects can be configured via the JPF 'shell' property, and are
 * typically JPF user interface implementations
 * 
 * Instantiation and start() call are done from the JPF.main() method
 *
 * Usually, JPFShell implementors have a <init>(Config) ctor so that they
 * don't need to obtain a new Config object. This is the reason for
 * using a shell *instance* instead of a static main() method - we cannot
 * pass the Config object that was already created by JPF.main() at this point
 * into the shell main(), which means we would either have to turn Config
 * into a singleton (causing problems for multiple JPF runs), or create a new
 * Config object within the shell. Since initialization of Config objects is
 * an expensive task with our new bootstrapping, it seems better to pass the
 * Config object from JPF.main() (which is done automatically when using
 * Config based initialization)
 */
public interface JPFShell {

  void start(String[] args);
}
