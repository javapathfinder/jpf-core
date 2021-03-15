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
package java.lang.invoke;


// We use this a model class in JPF to model the new VarHandle behavior for field replacing longterm the Unsafe implementation.
// See: http://cr.openjdk.java.net/~psandoz/varhandles/VarHandle-0.1.md for more information on VarHandle.
public class JPFFieldVarHandle extends VarHandle {

  int fieldRef;
  int classRef;

  JPFFieldVarHandle() {
    super(null);
  }

  JPFFieldVarHandle(int fieldRef) {
    super(null);
    this.fieldRef = fieldRef;
  }

  MethodType accessModeTypeUncached(AccessMode accessMode) {
    return null;
  }

  public boolean compareAndSet(Object aqs, int expect, int update) {
    //This is implemented in the corresponding peer
    return false;
  }

  public boolean compareAndSet(Object aqs, Object expect, Object update) {
    //This is implemented in the corresponding peer
    return false;
  }

  public void set(Object called, int value) {
    //This is implemented in the corresponding peer
  }

  public void set(Object called, Object value) {
    //This is implemented in the corresponding peer
  }
}
