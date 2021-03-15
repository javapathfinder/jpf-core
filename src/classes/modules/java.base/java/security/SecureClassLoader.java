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

package java.security;

/**
 * This is just a dummy implementation of java.security.SecureClassLoader
 *
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class SecureClassLoader extends ClassLoader {

  protected SecureClassLoader(String name, ClassLoader parent) {
    super(parent);
  }

  protected SecureClassLoader(ClassLoader parent) {
    super(parent);
  }

  protected SecureClassLoader() {
    super();
  }
}
