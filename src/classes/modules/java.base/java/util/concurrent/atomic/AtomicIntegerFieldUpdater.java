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
 * model class for the AtomicIntegerFieldUpdater
 * in reality it's an abstract class, but this here is merely a stub anyways
 */
public class AtomicIntegerFieldUpdater<T> {

  int fieldId;

  public static <O> AtomicIntegerFieldUpdater<O> newUpdater (Class<O> objClass, String fieldName) {
    return new AtomicIntegerFieldUpdater<O>(objClass, fieldName);
  }

  protected AtomicIntegerFieldUpdater(Class<T> objClass, String fieldName){
    // direct Object subclass, so we can directly intercept the ctor
    // w/o having to call a parent ctor
  }

  public native boolean compareAndSet(T obj, int expect, int update);
  public native int     get(T obj);
  public native int     getAndAdd(T obj, int delta);
  public native int     getAndSet(T obj, int newValue);
  public native void    lazySet(T obj, int newValue);
  public native void    set(T obj, int newValue);
  public native boolean weakCompareAndSet(T obj, int expect, int update);

  public        int     addAndGet(T obj, int delta)  {return(getAndAdd(obj, delta) + delta);}
  public        int     decrementAndGet(T obj)       {return(addAndGet(obj, -1));}
  public        int     getAndDecrement(T obj)       {return(getAndAdd(obj, -1));}
  public        int     getAndIncrement(T obj)       {return(getAndAdd(obj, 1));}
  public        int     incrementAndGet(T obj)       {return(addAndGet(obj, 1));}
}
