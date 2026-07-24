/*
 * Copyright (C) 2026, Darshan R
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.lang.reflect;

import java.lang.annotation.Annotation;

public final class RecordComponent {
  int regIdx;        // link to the corresponding RecordComponentInfo
  Class<?> clazz;    // the record class that declared this component
  String name;       // deferred set by the NativePeer

  public native String getName();
  public native Class<?> getType();
  public native String getGenericSignature();
  public native java.lang.reflect.Method getAccessor();
  public native Annotation[] getAnnotations();
  public native Annotation[] getDeclaredAnnotations();
}
