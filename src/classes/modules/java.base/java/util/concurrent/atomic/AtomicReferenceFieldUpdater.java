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

package java.util.concurrent.atomic;

/**
 * model class for the AtomicReferenceFieldUpdater
 * in reality it's an abstract class, but this here is merely a stub anyways
 */
public class AtomicReferenceFieldUpdater<T,V> {

  int fieldId;

  public static <O,F> AtomicReferenceFieldUpdater<O,F> newUpdater (Class<O> objClass, Class<F> fieldClass,
                                                                   String fieldName) {
    return new AtomicReferenceFieldUpdater<O,F>(objClass, fieldClass, fieldName);
  }

  protected AtomicReferenceFieldUpdater(Class<T> objClass, Class<V> fieldClass, String fieldName){
    // direct Object subclass, so we can directly intercept the ctor
    // w/o having to call a parent ctor
  }

  public native boolean compareAndSet(T obj, V expect, V update);
  public native V       get(T obj);
  public native V       getAndSet(T obj, V newValue);
  public native void    lazySet(T obj, V newValue);
  public native void    set(T obj, V newValue);
  public native boolean weakCompareAndSet(T obj, V expect, V update);
}
