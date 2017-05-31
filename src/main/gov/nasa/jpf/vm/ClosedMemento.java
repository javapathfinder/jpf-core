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
package gov.nasa.jpf.vm;

/**
 * this is a self-contained memento that remembers which object to restore,
 * and where to restore it into (e.g. a container).
 * 
 * This differs from a normal Memento in that it requires nothing from the
 * caller except of being called at the right times.
 * 
 * Normally, implementors keep a reference to the original object, and copy
 * the field values which have to be restored.
 */
public interface ClosedMemento {

  void restore();
}
