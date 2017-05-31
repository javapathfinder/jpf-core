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

import gov.nasa.jpf.util.script.ScriptElementContainer.SECIterator;

import java.io.StringReader;

/**
 * an interpreter that walks a ScriptElementContainer hierarchy, returning
 * Events and Alternatives while expanding loops
 */
public class SequenceInterpreter implements Cloneable {

  ScriptElementContainer.SECIterator top;

  public SequenceInterpreter (ScriptElementContainer seq) {
    top = seq.iterator();
  }

  void push (SECIterator it) {
    it.prev = top;
    top = it;
  }

  SECIterator pop () {
    if (top != null) {
      top = top.getPrev();
    }
    return top;
  }

  public ScriptElement getNext() {
    if (top != null) {
      ScriptElement e = top.next();
      if (e != null) {
        if ((e instanceof ScriptElementContainer) && !(e instanceof Alternative) ) {
          push( ((ScriptElementContainer)e).iterator());
          return getNext();
        } else {
          return e;
        }
      } else {
        pop();
        return (top != null) ? getNext() : null;
      }
    } else {
      return null;
    }
  }

  @Override
  public Object clone() {
    // has to deep copy all iterators

    try {
      SequenceInterpreter si = (SequenceInterpreter) super.clone();
      if (top != null) {
        si.top = (SECIterator)top.clone();
      }
      return si;
    } catch (CloneNotSupportedException nonsense) {
      return null; // we are a Cloneable, so we don't get here
    }
  }

  public boolean isDone() {
    return (top == null);
  }

  //---- test driver
  public static void main (String[] args) {
    //String s = "";
    //String s = "a; b; c ";
    //String s = "REPEAT 2 { e1, REPEAT 1 { r1, r2 }, e2 }";
    //String s = "x, ANY {a1,a2}, y";
    String s = "REPEAT 2 { start, ANY {a1,a2}, REPEAT 2 {r}, end }";


    StringReader r = new StringReader(s);

    try {
      ESParser parser = new ESParser("test", r);
      Script script = parser.parse();

      SequenceInterpreter si = new SequenceInterpreter(script);

      for (ScriptElement e = si.getNext(); e != null; e = si.getNext()) {
        System.out.println(e);
      }

    } catch (Throwable t){
      t.printStackTrace();
    }
  }
}
