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
package sun.misc;

/**
 * MJI model class for sun.misc.AtomicLong library abstraction
 */
public class AtomicLong {
  long value;

  public AtomicLong () {
  }

  private AtomicLong (long val) {
    value = val;
  }

  public static AtomicLong newAtomicLong (long val) {
    return new AtomicLong(val);
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

  public long get () {
    return value;
  }

  @SuppressWarnings("unused")
  private static boolean VMSupportsCS8 () {
    // whatever it means
    return false;
  }
}
