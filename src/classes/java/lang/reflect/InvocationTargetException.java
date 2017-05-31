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

package java.lang.reflect;

/**
 * not really required to model, but the real thing does some funky
 * things to override the cause, just making things a bit more complicated
 * on our VM side (we still init Throwables explicitly from ThreadInfo)
 */
public class InvocationTargetException extends Exception {

  public InvocationTargetException (Throwable cause){
    super(cause);
  }
  
  public Throwable getTargetException() {
    return cause;
  }
}
