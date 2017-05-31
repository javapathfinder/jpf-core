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

import java.io.Serializable;
import java.util.Arrays;

/**
 * model class for AtomicLongArray
 */
public class AtomicLongArray implements Serializable {
  private static final long serialVersionUID = -2308431214976778248L;

  private final long[] array;

  public AtomicLongArray(int length) {
    array = new long[length];
    // <2do> need a volatile write in order to conform to JMM  // Does this really matter in JPF?
  }

  public AtomicLongArray(long[] array) {
    if (array == null)
      throw new NullPointerException();

    int length = array.length;
    this.array = new long[length];

    for (int i = 0; i < length; ++i)
      this.array[i] = array[i];

    // <2do> need a volatile write in order to conform to JMM  // Does this really matter in JPF?
  }

  public final int length() {
    return(array.length);
  }

  public final long get(int i) {
    checkIndex(i);
    return(getNative(i));
  }

  private final native long getNative(int i);

  public final boolean compareAndSet(int i, long expect, long update) {
    checkIndex(i);
    return(compareAndSetNative(i, expect, update));
  }

  private final native boolean compareAndSetNative(int i, long expect, long update);

  public final boolean weakCompareAndSet(int i, long expect, long update) {
    return(compareAndSet(i, expect, update));
  }

  public final long getAndSet(int i, long newValue) {
    while (true) {
      long current = get(i);
      if (compareAndSet(i, current, newValue))
        return(current);
    }
  }

  public final void set(int i, long newValue) {
    getAndSet(i, newValue);
  }

  public final void lazySet(int i, long newValue) {
    set(i, newValue);
  }

  public final long getAndIncrement(int i) {
    return(getAndAdd(i, 1));
  }

  public final long getAndDecrement(int i) {
    return(getAndAdd(i, -1));
  }

  public final long getAndAdd(int i, long delta) {
    while (true) {
      long current = get(i);
      long next = current + delta;
      if (compareAndSet(i, current, next))
        return(current);
    }
  }

  public final long incrementAndGet(int i) {
    return(getAndIncrement(i) + 1);
  }

  public final long decrementAndGet(int i) {
    return(getAndDecrement(i) - 1);
  }

  public final long addAndGet(int i, long delta) {
    return(getAndAdd(i, delta) + delta);
  }

  @Override
  public String toString() {
    // <2do> need a volatile read in order to conform to JMM  // Does this really matter in JPF?
    return(Arrays.toString(array));
  }

  private void checkIndex(int i) {
    if (i < 0 || i >= array.length)
      throw new IndexOutOfBoundsException("index " + i);
  }
}
