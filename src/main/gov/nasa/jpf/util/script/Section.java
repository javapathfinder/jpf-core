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

import gov.nasa.jpf.util.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * this script element is just a way to do logical partitioning of scripts
 * and doesn't bear any additional info than just an id. It is an optional element
 */
public class Section extends ScriptElementContainer {

  ArrayList<String> ids = new ArrayList<String>();

  public Section (ScriptElement parent, String id) {
    super(parent,0);
    this.ids.add(id);
  }

  public Section (ScriptElement parent, List<String> ids, int line) {
    super(parent, line);
    this.ids.addAll(ids);
  }

  public List<String> getIds() {
    return ids;
  }

  public boolean containsId (String id) {
    return ids.contains(id);
  }

  @Override
  public String toString() {
    return super.toString( Misc.toString(ids, "SECTION ", ",", null));
  }

  @Override
  public void process (ElementProcessor proc) {
    proc.process(this);
  }

}
