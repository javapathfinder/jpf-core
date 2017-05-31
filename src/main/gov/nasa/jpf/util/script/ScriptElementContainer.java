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

import java.io.PrintWriter;
import java.util.Iterator;

public abstract class ScriptElementContainer extends ScriptElement implements Iterable <ScriptElement> {

  protected class SECIterator implements Iterator<ScriptElement>, Cloneable {

    SECIterator prev; // to build ad hoc stacks
    ScriptElement cur;

    SECIterator () {
      cur = firstChild;
    }

    @Override
	public boolean hasNext() {
      return (cur != null);
    }

    @Override
	public ScriptElement next() {
      if (cur != null) {
        ScriptElement ret = cur;
        cur = cur.nextSibling;
        return ret;
      } else {
        return null;
      }
    }

    @Override
	public void remove() {
      throw new UnsupportedOperationException("no ScriptElement removal supported");
    }

    public SECIterator getPrev() {
      return prev;
    }

    public void setPrev (SECIterator it) {
      prev = it;
    }

    @Override
	public Object clone() {
      try {
        // need to deep copy iterators
        SECIterator it = (SECIterator)super.clone();
        if (prev != null) {
          it.prev = (SECIterator) prev.clone();
        }
        return it;
      } catch (CloneNotSupportedException cnsx) {
        return null; // can't happen, just compiler pleasing
      }
    }
  }


  ScriptElement firstChild;

  ScriptElementContainer (ScriptElement parent, int line) {
    super(parent, line);
  }

  public ScriptElement getFirstChild () {
    return firstChild;
  }

  /**
   * beware, this sets the nextSibling
   */
  public void add (ScriptElement e) {
    e.nextSibling = null;

    if (firstChild == null) {
      firstChild = e;
    } else {
      ScriptElement p=firstChild;
      while (p.nextSibling != null) p=p.nextSibling;
      p.nextSibling = e;
    }
  }

  public int getNumberOfChildren() {
    int n=0;
    ScriptElement e = firstChild;
    while (e != null) {
      n++;
      e = e.getNextSibling();
    }
    return n;
  }

  @Override
  public SECIterator iterator () {
    return new SECIterator();
  }

  void dump (PrintWriter w, int level, ScriptElement elem) {

    try {
      while (elem != null) {
        for (int i=0; i<level; i++) {
          w.print("  ");
        }
        w.print(elem);

        if (elem instanceof ScriptElementContainer) {
          ScriptElementContainer c = (ScriptElementContainer) elem;
          w.println(" {");
          dump(w, level+1, c.getFirstChild());
          for (int i=0; i<level; i++) {
            w.print("  ");
          }
          w.println("}");
        } else {
          w.println();
        }

        elem = elem.getNextSibling();
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }

  protected String toString (String type) {
    StringBuilder sb = new StringBuilder();

    if (type != null) {
      sb.append(type);
      sb.append(' ');
    }

    sb.append("{");
    int i=0;
    for (ScriptElement e = firstChild; e != null; e = e.nextSibling) {
      if (i++ > 0) {
        sb.append(',');
      }
      sb.append(e);
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String toString() {
    return toString(null);
  }

  public void dump (PrintWriter w) {
    dump(w, 0, firstChild);
  }

  public void processChildren(ElementProcessor p) {
    for (ScriptElement e = firstChild; e != null; e = e.nextSibling) {
      e.process(p);
    }
  }
}