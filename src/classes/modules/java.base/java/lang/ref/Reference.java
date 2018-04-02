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
package java.lang.ref;

/**
 * MJI model class for java.lang.ref.Reference library abstraction
 * we model this so that we can rely on our WeakRefence implementation
 */
public abstract class Reference<T> {
  
  /**
   * the object we reference
   * NOTE: this has to be the *first* field, or we break WeakReference handling in
   * the garbage collection!!
   */
  T ref;

  /** the optional queue for us */
  ReferenceQueue<? super T> queue;

  /** link to enqueue w/o additional memory requirements */
  Reference<T> next;

  Reference (T r) {
    ref = r;
  }
  
  Reference (T r, ReferenceQueue<? super T> q) {
    ref = r;
    queue = q;
  }
  
  /** is the referenced object enqueued */
  public boolean isEnqueued () {
    // <2do>
    return false;
  }

  /** clear, but do not enqueue the referenced object */
  public void clear () {
    ref = null;
  }

  /** add the referenced object to its queue */
  public void enqueue () {
  }

  /** return the referenced object */
  public T get () {
    return ref;
  }
}
