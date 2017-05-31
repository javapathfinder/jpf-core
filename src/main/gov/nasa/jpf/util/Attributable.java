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

package gov.nasa.jpf.util;

/**
 * interface for types that support attributes
 */
public interface Attributable {

  boolean hasAttr ();
  boolean hasAttr (Class<?> attrType);
  Object getAttr();
  void setAttr (Object a);
  void addAttr (Object a);
  void removeAttr (Object a);
  void replaceAttr (Object oldAttr, Object newAttr);
  <T> T getAttr (Class<T> attrType);
  <T> T getNextAttr (Class<T> attrType, Object prev);
  ObjectList.Iterator attrIterator();
  <T> ObjectList.TypedIterator<T> attrIterator(Class<T> attrType);
  
}
