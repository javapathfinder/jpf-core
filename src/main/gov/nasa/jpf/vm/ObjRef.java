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
 * helper class to store object references in a context where Integer is used for boxed 'int' values
 */
public class ObjRef {
  public static final ObjRef NULL = new ObjRef(MJIEnv.NULL);
  
  int reference;

  protected ObjRef (int ref) {
    reference = ref;
  }

  public boolean isNull () {
    return reference == MJIEnv.NULL;
  }

  public int getReference () {
    return reference;
  }

  @Override
  public boolean equals (Object o) {
    if (o.getClass() == ObjRef.class) {
      ObjRef other = (ObjRef)o;
      return reference == other.reference;
    }
    return false;
  }

  @Override
  public int hashCode () {
    return reference;
  }

  @Override
  public String toString () {
    return "ObjRef(" + reference + ')';
  }
}