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
 * this class wraps the various exceptions we might encounter during
 * initialization and use of Config
 */
public class JPFConfigException extends JPFException {

  public JPFConfigException(String msg) {
    super(msg);
  }

  public JPFConfigException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public JPFConfigException(String key, Class<?> cls, String failure) {
    super("error instantiating class " + cls.getName() + " for entry \"" + key + "\":" + failure);
  }

  public JPFConfigException(String key, Class<?> cls, String failure, Throwable cause) {
    this(key, cls, failure);
    initCause(cause);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("JPF configuration error: ");
    sb.append(getMessage());

    return sb.toString();
  }
}
