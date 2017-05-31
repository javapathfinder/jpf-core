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

import gov.nasa.jpf.util.HashPool;
import gov.nasa.jpf.util.IntTable.Entry;
import gov.nasa.jpf.util.WeakPool;

abstract class CollapsePools {
  static class AllWeak {
    /** Pool used to store the stack frames.*/
    private WeakPool<StackFrame> stackFramePool = new WeakPool<StackFrame>(11);

    /** Pool used to store collections of field values.*/
    private WeakPool<Fields>     fieldsPool     = new WeakPool<Fields>    (11);

    /** Pool used to store the thread data.*/
    private WeakPool<ThreadData> threadDataPool = new WeakPool<ThreadData>(8);
    
    /** Pool used to store monitor states.*/
    private WeakPool<Monitor>    monitorPool    = new WeakPool<Monitor>   (8);

    public StackFrame poolStackFrame(StackFrame o) {
      StackFrame p = stackFramePool.pool(o);
      if (VM.CHECK_CONSISTENCY) assert p.equals(o);
      return p;
    }

    public Fields poolFields(Fields o) {
      Fields p = fieldsPool.pool(o);
      if (VM.CHECK_CONSISTENCY) assert p.equals(o);
      return p;
    }

    public ThreadData poolThreadData(ThreadData o) {
      ThreadData p = threadDataPool.pool(o);
      if (VM.CHECK_CONSISTENCY) assert p.equals(o);
      return p;
    }

    public Monitor poolMonitor(Monitor o) {
      Monitor p = monitorPool.pool(o);
      if (VM.CHECK_CONSISTENCY) assert p.equals(o);
      return p;
    }
  }
  
  static class AllIndexed {
    /** Pool used to store the thread data.*/
    private HashPool<ThreadData> threadDataPool = new HashPool<ThreadData>(8).addNull();

    /** Pool used to store monitor states.*/
    private HashPool<Monitor>    monitorPool    = new HashPool<Monitor>   (8).addNull();

    /** Pool used to store the stack frames.*/
    private HashPool<StackFrame> stackFramePool = new HashPool<StackFrame>(11).addNull();

    /** Pool used to store collections of field values.*/
    private HashPool<Fields>     fieldsPool     = new HashPool<Fields>    (11).addNull();
    
    public StackFrame poolStackFrame(StackFrame o) {
      StackFrame p = stackFramePool.get(o);
      if (VM.CHECK_CONSISTENCY) assert p.equals(o);
      return p;
    }

    public Fields poolFields(Fields o) {
      return fieldsPool.get(o);
    }

    public ThreadData poolThreadData(ThreadData o) {
      return threadDataPool.get(o);
    }

    public Monitor poolMonitor(Monitor o) {
      return monitorPool.get(o);
    }

    public int getFieldsIndex(ElementInfo ei) {
      Entry<Fields> entry = fieldsPool.getEntry(ei.getFields());
      ei.restoreFields(entry.key);
      return entry.val;
    }

    public int getStackFrameIndex(StackFrame sf) {
      return stackFramePool.getIndex(sf);
    }

    public int getThreadDataIndex(ThreadInfo ti) {
      Entry<ThreadData> e = threadDataPool.getEntry(ti.threadData);
      ti.threadData = e.key;
      return e.val;
    }
    
    public int getMonitorIndex(ElementInfo ei) {
      Entry<Monitor> entry = monitorPool.getEntry(ei.getMonitor());
      ei.restoreMonitor(entry.key);
      return entry.val;
    }
    
    public Fields getFieldsAt(int idx) {
      return fieldsPool.getObject(idx);
    }

    public StackFrame getStackFrameAt(int idx) {
      return stackFramePool.getObject(idx);
    }

    public ThreadData getThreadDataAt(int idx) {
      return threadDataPool.getObject(idx);
    }

    public Monitor getMonitorAt(int idx) {
      return monitorPool.getObject(idx);
    }
  }
}
