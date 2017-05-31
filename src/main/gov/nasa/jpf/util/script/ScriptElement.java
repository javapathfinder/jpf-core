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

public abstract class ScriptElement implements Cloneable {
  protected ScriptElement parent;
  protected ScriptElement nextSibling;
  protected int line;

  ScriptElement (ScriptElement parent, int line){
    this.parent = parent;
    this.line = line;
  }

  public ScriptElement getParent() {
    return parent;
  }

  public int getLine() {
    return line;
  }

  public ScriptElement getNextSibling() {
    return nextSibling;
  }

  void setNextSibling(ScriptElement e) {
    nextSibling = e;
  }

  @Override
  public ScriptElement clone() {
    try {
      return (ScriptElement) super.clone();
    } catch (CloneNotSupportedException cnsx) {
      return null;
    }
  }

  public abstract void process (ElementProcessor proc);
}
