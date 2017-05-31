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

import java.lang.annotation.Annotation;

public class AccessibleObject {
  boolean isAccessible = true;

  public void setAccessible (boolean flag)  throws SecurityException {
    isAccessible = flag;
  }
  
  public static void setAccessible (AccessibleObject[] accessibles, boolean flag) throws SecurityException {
    for (int i=0; i<accessibles.length; i++) {
      accessibles[i].isAccessible = flag;
    }
  }
  
  public boolean isAccessible() {
    return isAccessible;
  }
    
  public native <T extends Annotation> T getAnnotation (Class<T> cls); // <2do> Implement in JPF_java_lang_reflect_Constructor

  public boolean isAnnotationPresent (Class<? extends Annotation> cls) {
    return getAnnotation(cls) != null;
  }

  public native Annotation[] getAnnotations(); // <2do> Implement in JPF_java_lang_reflect_Constructor

  public native Annotation[] getDeclaredAnnotations(); // <2do> Implement in JPF_java_lang_reflect_Method, JPF_java_lang_reflect_Class and JPF_java_lang_reflect_Constructor
}
