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
 * model class for AtomicReferenceArray
 */
public class AtomicReferenceArray<E> implements Serializable {
  private static final long serialVersionUID = -6209656149925076980L;

  private final Object[] array;

  public AtomicReferenceArray(int length) {
    array = new Object[length];
    // <2do> need a volatile write in order to conform to JMM  // Does this really matter in JPF?
  }

  public AtomicReferenceArray(E[] array) {
    if (array == null)
      throw new NullPointerException();

    int length = array.length;
    this.array = new Object[length];

    for (int i = 0; i < length; ++i)
      this.array[i] = array[i];

    // <2do> need a volatile write in order to conform to JMM  // Does this really matter in JPF?
  }

  public final int length() {
    return(array.length);
  }

  public final E get(int i) {
    checkIndex(i);
    return(getNative(i));
  }

  private final native E getNative(int i);

  public final boolean compareAndSet(int i, E expect, E update) {
    checkIndex(i);
    return(compareAndSetNative(i, expect, update));
  }

  private final native boolean compareAndSetNative(int i, E expect, E update);

  public final boolean weakCompareAndSet(int i, E expect, E update) {
    return(compareAndSet(i, expect, update));
  }

  public final E getAndSet(int i, E newValue) {
    while (true) {
      E current = get(i);
      if (compareAndSet(i, current, newValue))
        return(current);
    }
  }

  public final void set(int i, E newValue) {
    getAndSet(i, newValue);
  }

  public final void lazySet(int i, E newValue) {
    set(i, newValue);
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
