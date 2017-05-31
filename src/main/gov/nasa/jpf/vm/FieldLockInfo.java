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

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

/**
 * class encapsulating the lock protection status for field access
 * instructions. Used by on-the-fly partial order reduction in FieldInstruction
 * to determine if a GET/PUT_FIELD/STATIC insn has to be treated as a
 * boundary step (terminates a transition). If the field access is always
 * protected by a lock, only the corresponding sync (INVOKExx or MONITORENTER)
 * are boundary steps, thus the number of states can be significantly reduced.
 * 
 * FieldLockInfos are only used if vm.por.sync_detection is set
 * 
 * NOTE this might involve assumptions that can be violated in subsequent
 * paths, and might cause potential races to go undetected
 */
public abstract class FieldLockInfo implements Cloneable  {
  
  static Logger log = JPF.getLogger("gov.nasa.jpf.vm.FieldLockInfo");
  
  static protected final FieldLockInfo empty = new EmptyFieldLockInfo();
    
  protected ThreadInfo tiLastCheck; // the thread this FieldLockInfo was last checked for

  public static FieldLockInfo getEmptyFieldLockInfo(){
    return empty;
  }
  
  public abstract FieldLockInfo checkProtection (ThreadInfo ti, ElementInfo ei, FieldInfo fi);
  public abstract boolean isProtected ();
  
  public abstract FieldLockInfo cleanUp (Heap heap);
  protected abstract int[] getCandidateLockSet();
    
  public boolean isFinal() {
    return isProtected();
  }
  
  public boolean needsPindown (ElementInfo ei) {
    return false;
  }
  
  
  /*
   * we need this for faster instantiation. Make sure it gets overridden in
   * case there is a need for per-instance parameterization
   */
  @Override
  public Object clone () throws CloneNotSupportedException {
    return super.clone();
  }

  void lockAssumptionFailed (ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
    String src = ti.getTopFrameMethodInfo().getClassInfo().getSourceFileName();
    int line = ti.getLine();

    StringBuilder sb = new StringBuilder( "unprotected field access of: ");
    sb.append(ei);
    sb.append('.');
    sb.append(fi.getName());
    sb.append( " in thread: ");
    sb.append( ti.getName());
    sb.append( " (");
    sb.append( src);
    sb.append(':');
    sb.append(line);
    sb.append(")\n[SEVERE].. last lock candidates: ");
    appendLockSet(sb, getCandidateLockSet());
    if (tiLastCheck != null) {
      sb.append(" set by ");
      sb.append(tiLastCheck);
    }
    sb.append( "\n[SEVERE].. current locks: ");
    appendLockSet(sb, ti.getLockedObjectReferences());
    sb.append("\n[SEVERE].. if this is not a race, re-run with 'vm.shared.sync_detection=false' or exclude field from checks");

    log.severe(sb.toString());
  }

  void appendLockSet (StringBuilder sb, int[] lockSet) {
    Heap heap = VM.getVM().getHeap();

    if ((lockSet == null) || (lockSet.length == 0)) {
      sb.append( "{}");
    } else {
      sb.append('{');
      for (int i=0; i<lockSet.length;) {
        int ref = lockSet[i];
        if (ref != MJIEnv.NULL) {
          ElementInfo ei = heap.get(ref);
          if (ei != null) {
            sb.append(ei);
          } else {
            sb.append("?@");
            sb.append(lockSet[i]);
          }
        }
        i++;
        if (i<lockSet.length) sb.append(',');
      }
      sb.append('}');
    }
  }

}

/**
 * FieldLockSet implementation for fields that are terminally considered to be unprotected
 */
class EmptyFieldLockInfo extends FieldLockInfo {
  
  @Override
  public FieldLockInfo checkProtection (ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
    return this;
  }
      
  @Override
  public FieldLockInfo cleanUp (Heap heap) {
    return this;
  }
  
  @Override
  public boolean isProtected () {
    return false;
  }
    
  @Override
  public boolean isFinal() {
    return true;
  }
  
  @Override
  protected int[] getCandidateLockSet() {
    return new int[0];
  }
  
  @Override
  public String toString() {
    return "EmptyFieldLockInfo";
  }
}

