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
 * common stuff used by all Annotation Proxies
 */
public class AnnotationProxyBase {

  public native Class<?> annotationType();
  
  // this is just here to be intercepted by the native peer
  @Override
  public native String toString();
  
  /***
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('@');
    
    Class<?> cls = getClass();
    String clsName = cls.getName();
    int idx = clsName.lastIndexOf('$');
    sb.append(clsName.substring(0, idx));
    
    Field[] fields = cls.getDeclaredFields();  
    if (fields.length > 0){
      sb.append('(');
      for (int i=0; i<fields.length; i++){
        fields[i].setAccessible(true);
        
        if (i>0){
          sb.append(',');
        }
        sb.append(fields[i].getName());
        sb.append('=');
        
        try {
          Object v = fields[i].get(this);
          Class<?> vcls = v.getClass();

          if (vcls.isArray()){
            sb.append('[');
            int n = Array.getLength(v);
            for (int j=0; j<n; j++){
              if (j>0){
                sb.append(',');
              }
              sb.append(Array.get(v,j));
            }            
            sb.append(']');
          } else {
            sb.append(fields[i].get(this));
          }
        } catch (IllegalAccessException iax){}
      }
      sb.append(')');
    }
    
    return sb.toString();
  }
  ***/
}
