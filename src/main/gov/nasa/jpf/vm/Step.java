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

import gov.nasa.jpf.util.Source;

import java.util.WeakHashMap;

/**
 * this corresponds to an executed instruction. Note that we can have a
 * potentially huge number of Steps, hence we want to save objects here
 * (e.g. Collection overhead)
 */
public class Step {

  private static WeakHashMap<Step, String> s_comments = new WeakHashMap<Step, String>();  // Not every Step gets a comment.  So save memory and put comments in a global comment HashMap.  Make this a WeakHashMap so that old Step objects can be GCed.

  private final Instruction insn;
  Step next;

  public Step (Instruction insn) {
    if (insn == null)
      throw new IllegalArgumentException("insn == null");

    this.insn = insn;
  }

  public Step getNext() {
    return next;
  }

  public Instruction getInstruction() {
    return insn;
  }

  public void setComment (String s) {
    s_comments.put(this, s);
  }

  public String getComment () {
    return s_comments.get(this);
  }

  public String getLineString () {
    MethodInfo mi = insn.getMethodInfo();
    if (mi != null) {
      Source source = Source.getSource(mi.getSourceFileName());
      if (source != null) {
        int line = mi.getLineNumber(insn);
        if (line > 0) {
          return source.getLine(line);
        }
      }
    }

    return null;
  }

  public boolean sameSourceLocation (Step other){
    
    if (other != null){
      MethodInfo mi = insn.getMethodInfo();
      MethodInfo miOther = other.insn.getMethodInfo();
      if (mi == miOther){
        return (mi.getLineNumber(insn) == miOther.getLineNumber(other.insn));
      }
    }
    
    return false;
  }
  
  public String getLocationString() {
    MethodInfo mi = insn.getMethodInfo();
    if (mi != null) {
      return mi.getSourceFileName() + ':' + mi.getLineNumber(insn);
    }

    return "?:?";
  }

  @Override
  public String toString() {
    return getLocationString();
  }
}
