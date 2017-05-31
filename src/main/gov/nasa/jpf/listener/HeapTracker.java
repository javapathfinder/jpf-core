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
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.DynamicObjectArray;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.SourceRef;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * HeapTracker - property-listener class to check heap utilization along all
 * execution paths (e.g. to verify heap bounds)
 */
public class HeapTracker extends PropertyListenerAdapter {

  static class PathStat implements Cloneable {
    int nNew = 0;
    int nReleased = 0;
    int heapSize = 0;  // in bytes

    @Override
	public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }

  static class TypeStat {
    String typeName;
    int nAlloc;
    int nReleased;

    TypeStat (String typeName){
      this.typeName = typeName;
    }
  }

  PathStat stat = new PathStat();
  Stack<PathStat> pathStats = new Stack<PathStat>();

  DynamicObjectArray<SourceRef> loc = new DynamicObjectArray<SourceRef>();

  HashMap<String,TypeStat> typeStat = new HashMap<String,TypeStat>();

  int maxState;
  int nForward;
  int nBacktrack;

  int nElemTotal;
  int nGcTotal;
  int nSharedTotal;
  int nImmutableTotal;

  int nElemMax = Integer.MIN_VALUE;
  int nElemMin = Integer.MAX_VALUE;
  int nElemAv;

  int pElemSharedMax = Integer.MIN_VALUE;
  int pElemSharedMin = Integer.MAX_VALUE;
  int pElemSharedAv;

  int pElemImmutableMax = Integer.MIN_VALUE;
  int pElemImmutableMin = Integer.MAX_VALUE;
  int pElemImmutableAv;

  int nReleased;
  int nReleasedTotal;
  int nReleasedAv;
  int nReleasedMax = Integer.MIN_VALUE;
  int nReleasedMin = Integer.MAX_VALUE;

  int maxPathHeap = Integer.MIN_VALUE;
  int maxPathNew = Integer.MIN_VALUE;
  int maxPathReleased = Integer.MIN_VALUE;
  int maxPathAlive = Integer.MIN_VALUE;

  int initHeap = 0;
  int initNew = 0;
  int initReleased = 0;
  int initAlive = 0;


  boolean showTypeStats;
  int maxTypesShown;

  // used as a property check
  int maxHeapSizeLimit;
  int maxLiveLimit;
  boolean throwOutOfMemory = false;

  StringSetMatcher includes, excludes;

  void updateMaxPathValues() {
      if (stat.heapSize > maxPathHeap) {
        maxPathHeap = stat.heapSize;
      }

      if (stat.nNew > maxPathNew) {
        maxPathNew = stat.nNew;
      }

      if (stat.nReleased > maxPathReleased) {
        maxPathReleased = stat.nReleased;
      }

      int nAlive = stat.nNew - stat.nReleased;
      if (nAlive > maxPathAlive) {
        maxPathAlive = nAlive;
      }
  }

  void allocTypeStats (ElementInfo ei) {
    String typeName = ei.getClassInfo().getName();
    TypeStat ts = typeStat.get(typeName);
    if (ts == null) {
      ts = new TypeStat(typeName);
      typeStat.put(typeName, ts);
    }
    ts.nAlloc++;
  }

  void releaseTypeStats (ElementInfo ei) {
    String typeName = ei.getClassInfo().getName();
    TypeStat ts = typeStat.get(typeName);
    if (ts != null) {
      ts.nReleased++;
    }
  }


  public HeapTracker (Config config, JPF jpf) {
    maxHeapSizeLimit = config.getInt("heap.size_limit", -1);
    maxLiveLimit = config.getInt("heap.live_limit", -1);
    throwOutOfMemory = config.getBoolean("heap.throw_exception");
    showTypeStats = config.getBoolean("heap.show_types");
    maxTypesShown = config.getInt("heap.max_types", 20);

    includes = StringSetMatcher.getNonEmpty(config.getStringArray("heap.include"));
    excludes = StringSetMatcher.getNonEmpty(config.getStringArray("heap.exclude"));

    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }

  /******************************************* abstract Property *****/

  /**
   * return 'false' if property is violated
   */
  @Override
  public boolean check (Search search, VM vm) {
    if (throwOutOfMemory) {
      // in this case we don't want to stop the program, but see if it
      // behaves gracefully - don't report a property violation
      return true;
    } else {
      if ((maxHeapSizeLimit >= 0) && (stat.heapSize > maxHeapSizeLimit)) {
        return false;
      }
      if ((maxLiveLimit >=0) && ((stat.nNew - stat.nReleased) > maxLiveLimit)) {
        return false;
      }

      return true;
    }
  }

  @Override
  public String getErrorMessage () {
    return "heap limit exceeded: " + stat.heapSize + " > " + maxHeapSizeLimit;
  }

  /******************************************* SearchListener interface *****/
  @Override
  public void searchStarted(Search search) {
    super.searchStarted(search);

    updateMaxPathValues();
    pathStats.push(stat);

    initHeap = stat.heapSize;
    initNew = stat.nNew;
    initReleased = stat.nReleased;
    initAlive = initNew - initReleased;

    stat = (PathStat)stat.clone();
  }

  @Override
  public void stateAdvanced(Search search) {

    if (search.isNewState()) {
      int id = search.getStateId();

      if (id > maxState) maxState = id;

      updateMaxPathValues();
      pathStats.push(stat);
      stat = (PathStat)stat.clone();

      nForward++;
    }
  }

  @Override
  public void stateBacktracked(Search search) {
    nBacktrack++;

    if (!pathStats.isEmpty()){
      stat = pathStats.pop();
    }
  }

  /******************************************* PublisherExtension interface ****/
  @Override
  public void publishFinished (Publisher publisher) {
    PrintWriter pw = publisher.getOut();
    publisher.publishTopicStart("heap statistics");

    pw.println("heap statistics:");
    pw.println("  states:         " + maxState);
    pw.println("  forwards:       " + nForward);
    pw.println("  backtrack:      " + nBacktrack);
    pw.println();
    pw.println("  gc cycles:      " + nGcTotal);
    pw.println();
    pw.println("  max Objects:    " + nElemMax);
    pw.println("  min Objects:    " + nElemMin);
    pw.println("  avg Objects:    " + nElemAv);
    pw.println();
    pw.println("  max% shared:    " + pElemSharedMax);
    pw.println("  min% shared:    " + pElemSharedMin);
    pw.println("  avg% shared:    " + pElemSharedAv);
    pw.println();
    pw.println("  max% immutable: " + pElemImmutableMax);
    pw.println("  min% immutable: " + pElemImmutableMin);
    pw.println("  avg% immutable: " + pElemImmutableAv);
    pw.println();
    pw.println("  max released:   " + nReleasedMax);
    pw.println("  min released:   " + nReleasedMin);
    pw.println("  avg released:   " + nReleasedAv);

    pw.println();
    pw.print(  "  max path heap (B):   " + maxPathHeap);
    pw.println(" / " + (maxPathHeap - initHeap));
    pw.print(  "  max path alive:      " + maxPathAlive);
    pw.println(" / " + (maxPathAlive - initAlive));
    pw.print(  "  max path new:        " + maxPathNew);
    pw.println(" / " + (maxPathNew - initNew));
    pw.print(  "  max path released:   " + maxPathReleased);
    pw.println(" / " + (maxPathReleased - initReleased));

    if (showTypeStats) {
      pw.println();
      pw.println("  type allocation statistics:");

      ArrayList<Map.Entry<String,TypeStat>> list =
        Misc.createSortedEntryList(typeStat, new Comparator<Map.Entry<String,TypeStat>>() {
          @Override
		public int compare (Map.Entry<String,TypeStat> e1,
                              Map.Entry<String,TypeStat> e2) {
          return Integer.signum(e1.getValue().nAlloc - e2.getValue().nAlloc);
        }});

      int i=0;
      for (Map.Entry<String,TypeStat> e : list) {
        TypeStat ts = e.getValue();
        pw.print("  ");
        pw.print(String.format("%1$9d : ", ts.nAlloc));
        pw.println(ts.typeName);

        if (i++ > maxTypesShown) {
          pw.println("  ...");
          break;
        }
      }
    }
  }


  /******************************************* VMListener interface *********/
  @Override
  public void gcBegin(VM vm) {
    /**
     System.out.println();
     System.out.println( "----- gc cycle: " + vm.getDynamicArea().getGcNumber()
     + ", state: " + vm.getStateId());
     **/
  }

  @Override
  public void gcEnd(VM vm) {
    Heap heap = vm.getHeap();

    int n = 0;
    int nShared = 0;
    int nImmutable = 0;

    for (ElementInfo ei : heap.liveObjects()) {
      n++;

      if (ei.isShared()) nShared++;
      if (ei.isImmutable()) nImmutable++;

      //printElementInfo(ei);
    }

    nElemTotal += n;
    nGcTotal++;

    if (n > nElemMax) nElemMax = n;
    if (n < nElemMin) nElemMin = n;

    int pShared = (nShared * 100) / n;
    int pImmutable = (nImmutable * 100) / n;

    if (pShared > pElemSharedMax) pElemSharedMax = pShared;
    if (pShared < pElemSharedMin) pElemSharedMin = pShared;

    nSharedTotal += nShared;
    nImmutableTotal += nImmutable;

    pElemSharedAv = (nSharedTotal * 100) / nElemTotal;
    pElemImmutableAv = (nImmutableTotal * 100) / nElemTotal;

    if (pImmutable > pElemImmutableMax) pElemImmutableMax = pImmutable;
    if (pImmutable < pElemImmutableMin) pElemImmutableMin = pImmutable;

    nElemAv = nElemTotal / nGcTotal;
    nReleasedAv = nReleasedTotal / nGcTotal;

    if (nReleased > nReleasedMax) nReleasedMax = nReleased;
    if (nReleased < nReleasedMin) nReleasedMin = nReleased;

    nReleased = 0;
  }

  boolean isRelevantType (ElementInfo ei) {
    String clsName = ei.getClassInfo().getName();
    return StringSetMatcher.isMatch(clsName, includes, excludes);
  }

  @Override
  public void objectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {
    int idx = ei.getObjectRef();
    int line = ti.getLine();
    MethodInfo mi = ti.getTopFrameMethodInfo();
    SourceRef sr = null;

    if (!isRelevantType(ei)) {
      return;
    }

    if (mi != null) {
      ClassInfo mci = mi.getClassInfo();
      if (mci != null) {
        String file = mci.getSourceFileName();
        if (file != null) {
          sr = new SourceRef(file, line);
        } else {
          sr = new SourceRef(mci.getName(), line);
        }
      }
    }

    // means references with null loc are from synthetic methods
    loc.set(idx, sr);

    stat.nNew++;
    stat.heapSize += ei.getHeapSize();

    // update the type statistics
    if (showTypeStats) {
      allocTypeStats(ei);
    }


    // check if we should simulate an OutOfMemoryError
    if (throwOutOfMemory) {
      if (((maxHeapSizeLimit >=0) && (stat.heapSize > maxHeapSizeLimit)) ||
          ((maxLiveLimit >=0) && ((stat.nNew - stat.nReleased) > maxLiveLimit))){
        vm.getHeap().setOutOfMemory(true);
      }
    }
  }

  @Override
  public void objectReleased(VM vm, ThreadInfo ti, ElementInfo ei) {

    if (!isRelevantType(ei)) {
      return;
    }

    nReleasedTotal++;
    nReleased++;

    if (showTypeStats) {
      releaseTypeStats(ei);
    }

    stat.nReleased++;
    stat.heapSize -= ei.getHeapSize();
  }

  /****************************************** private stuff ******/
  protected void printElementInfo(ElementInfo ei) {
    boolean first = false;

    System.out.print( ei.getObjectRef());
    System.out.print( ": ");
    System.out.print( ei.getClassInfo().getName());
    System.out.print( "  [");

    if (ei.isShared()) {
      System.out.print( "shared");
      first = false;
    }
    if (ei.isImmutable()) {
      if (!first) System.out.print(' ');
      System.out.print( "immutable");
    }
    System.out.print( "] ");

    SourceRef sr = loc.get(ei.getObjectRef());
    if (sr != null) {
      System.out.println(sr);
    } else {
      System.out.println("?");
    }
  }


  static void printUsage () {
    System.out.println("HeapTracker - a JPF listener tool to report and check heap utilization");
    System.out.println("usage: java gov.nasa.jpf.tools.HeapTracker <jpf-options> <heapTracker-options> <class>");
    System.out.println("       +heap.size_limit=<num> : report property violation if heap exceeds <num> bytes");
    System.out.println("       +heap.live_limit=<num> : report property violation if more than <num> live objects");
    System.out.println("       +heap.classes=<regEx> : only report instances of classes matching <regEx>");
    System.out.println("       +heap.throw_exception=<bool>: throw a OutOfMemoryError instead of reporting property violation");
  }
}

