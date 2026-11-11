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
 * MJI model class for java.util.concurrent.atomic.AtomicLong
 *
 * The atomic operations (compareAndSet, getAndIncrement, addAndGet, ...) are implemented by the
 * native peer JPF_java_util_concurrent_atomic_AtomicLong, which accesses the field "value". They
 * have to be declared here so that the peer methods are bound, otherwise calling them fails with
 * NoSuchMethodError. The Java bodies of these methods are not executed while the peer is present.
 */
public class AtomicLong extends Number implements java.io.Serializable {
  private static final long serialVersionUID = 1927816293512124184L;

  long value;

  public AtomicLong () {
  }

  public AtomicLong (long initialValue) {
    value = initialValue;
  }

  public static AtomicLong newAtomicLong (long val) {
    return new AtomicLong(val);
  }

  public final long get () {
    return value;
  }

  public final void set (long newValue) {
    value = newValue;
  }

  public final void lazySet (long newValue) {
    value = newValue;
  }

  public final long getAndSet (long newValue) {
    long old = value;
    value = newValue;
    return old;
  }

  public final boolean compareAndSet (long expect, long update) {
    if (value == expect) {
      value = update;
      return true;
    }
    return false;
  }

  public final boolean weakCompareAndSet (long expect, long update) {
    return compareAndSet(expect, update);
  }

  public final long getAndIncrement () {
    return value++;
  }

  public final long getAndDecrement () {
    return value--;
  }

  public final long getAndAdd (long delta) {
    long old = value;
    value += delta;
    return old;
  }

  public final long incrementAndGet () {
    return ++value;
  }

  public final long decrementAndGet () {
    return --value;
  }

  public final long addAndGet (long delta) {
    value += delta;
    return value;
  }

  @Override
  public int intValue () {
    return (int) get();
  }

  @Override
  public long longValue () {
    return get();
  }

  @Override
  public float floatValue () {
    return (float) get();
  }

  @Override
  public double doubleValue () {
    return (double) get();
  }

  @Override
  public String toString () {
    return Long.toString(get());
  }

  public boolean attemptAdd (long l) {
    value += l;

    return true;
  }

  public boolean attemptIncrememt () {
    value++;

    return true;
  }

  public boolean attemptSet (long val) {
    value = val;

    return true;
  }

  public boolean attemptUpdate (long fallback, long newval) {
    value = newval;

    return true;
  }

  @SuppressWarnings("unused")
  private static boolean VMSupportsCS8 () {
    // whatever it means
    return false;
  }
}
