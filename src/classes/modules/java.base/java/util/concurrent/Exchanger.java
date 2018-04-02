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

package java.util.concurrent;

/**
 * model class for java.util.concurrent.Exchanger
 * We model because the original class goes to great lengths implementing
 * memory based synchronization, using execution time and spins
 * 
 * Exchangers are also per se shared objects, so we want to minimize field
 * access from bytecode
 */
public class Exchanger<V> {
  
  // created on native side and pinned down until transaction is complete
  static class Exchange<T> {
    Thread waiterThread;
    boolean waiterTimedOut;
    
    T waiterData;
    T responderData;
  }
  
  //-- only accessed from native methods
  private Exchange<V> exchange;
  
  
  public native V exchange(V value) throws InterruptedException;

  private native V exchange0 (V value, long timeoutMillis) throws InterruptedException, TimeoutException;
  
  // unfortunately we can't directly go native here without duplicating the TimeUnit conversion in the peer
  public V exchange(V value, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    long to = unit.convert(timeout,TimeUnit.MILLISECONDS);
    return exchange0( value, to);
  }
}
