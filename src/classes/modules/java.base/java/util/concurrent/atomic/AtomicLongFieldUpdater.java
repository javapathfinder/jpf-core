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
 * model class for the AtomicLongFieldUpdater
 * in reality it's an abstract class, but this here is merely a stub anyways
 */
public class AtomicLongFieldUpdater<T> {

  int fieldId;

  public static <O> AtomicLongFieldUpdater<O> newUpdater (Class<O> objClass, String fieldName) {
    return new AtomicLongFieldUpdater<O>(objClass, fieldName);
  }

  protected AtomicLongFieldUpdater(Class<T> objClass, String fieldName){
    // direct Object subclass, so we can directly intercept the ctor
    // w/o having to call a parent ctor
  }

  public native boolean compareAndSet(T obj, long expect, long update);
  public native long    get(T obj);
  public native long    getAndAdd(T obj, long delta);
  public native long    getAndSet(T obj, long newValue);
  public native void    lazySet(T obj, long newValue);
  public native void    set(T obj, long newValue);
  public native boolean weakCompareAndSet(T obj, long expect, long update);

  public        long    addAndGet(T obj, long delta) {return(getAndAdd(obj, delta) + delta);}
  public        long    decrementAndGet(T obj)       {return(addAndGet(obj, -1));}
  public        long    getAndDecrement(T obj)       {return(getAndAdd(obj, -1));}
  public        long    getAndIncrement(T obj)       {return(getAndAdd(obj, 1));}
  public        long    incrementAndGet(T obj)       {return(addAndGet(obj, 1));}
}
