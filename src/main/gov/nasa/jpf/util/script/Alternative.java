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

package gov.nasa.jpf.util.script;

/**
 * ScriptElement that represents an alternative between choices. At runtime,
 * this usually gets translated into a ChoiceGenerator instance, to specify
 * an event context that should facilitate state space exploration
 */
public class Alternative extends ScriptElementContainer {

  Alternative (ScriptElement parent, int line) {
    super(parent, line);
  }

  @Override
  public String toString() {
    return toString("ANY");
  }

  @Override
  public void process (ElementProcessor p) {
    p.process(this);
  }

}
