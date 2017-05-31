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


public class Repetition extends ScriptElementContainer {
  int repeatCount;

  class RepetitionIterator extends ScriptElementContainer.SECIterator {

    int count;

    RepetitionIterator () {
      count = 0;
      cur = firstChild;
    }

    @Override
	public boolean hasNext() {
      return ((cur != null) || (count<repeatCount) || (repeatCount < 0));
    }

    @Override
	public ScriptElement next() {
      if (cur != null) {
        ScriptElement ret = cur;
        cur = cur.nextSibling;
        return ret;
      } else {
        if ((++count < repeatCount) || (repeatCount < 0) ) {
          ScriptElement ret = firstChild;
          cur = ret.nextSibling;
          return ret;
        } else {
          return null;
        }
      }
    }

    @Override
	public void remove() {
      throw new UnsupportedOperationException("no ScriptElement removal supported");
    }
  }


  public Repetition (ScriptElement parent, int n, int line) {
    super(parent, line);
    repeatCount = n;
  }

  @Override
  public SECIterator iterator() {
    return new RepetitionIterator();
  }

  @Override
  public String toString() {
    return toString("REPEAT " + repeatCount );
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  @Override
  public void process (ElementProcessor p) {
    p.process(this);
  }
}
