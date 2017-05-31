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
 * generic interface for objects that are used to restore previous states from
 * within a context that holds the references to the objects to restore (e.g. a
 * container), i.e. the caller knows where to restore the objects in question.
 * The caller can provide a cached object the memento can update. However, its
 * up to the memento if it uses this (optional) argument object to restore
 * in-situ, the only guarantee it makes is that it returns a restored object
 */
public interface Memento<T> {

  /**
   * note that there is no guarantee the restored object will be the same that
   * is (optionally) passed in.
   * 
   * Implementations are free to restore in-situ or create a new object if a
   * non-null reference is provided. Callers are responsible for identity
   * integrity if they do provide in-situ objects
   * 
   * The caller does not guarantee the provided in-situ object was the one the
   * Memento was created from
   */
  T restore(T inSitu);
}
