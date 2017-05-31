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

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.heuristic.HeuristicSearch;

import java.io.PrintStream;

/**
 * An alternative to SearchMonitor that just reports statistics at the end.
 */
public class SearchStats extends ListenerAdapter {
  PrintStream out = System.out;
  
  long time;
  long startTime;
  long startFreeMemory;
  
  int searchLevel=0;
  int maxSearchLevel=0;
  
  int newStates;
  int endStates;
  int backtracks;
  int revisitedStates;
  int processedStates;
  int restoredStates;
  
  int steps;

  long maxMemory;
  long totalMemory;
  long freeMemory;
    
  boolean isHeuristic = false;
  int queueSize = 0;
  
  int currentHeapCount = 0;
  int maxHeapCount = 0;
  
  /*
   * SearchListener interface
   */  
  @Override
  public void stateAdvanced(Search search) {
    steps += search.getTransition().getStepCount();
   
    if (isHeuristic)
    	queueSize = ((HeuristicSearch)(search)).getQueueSize();
    
    if (search.isNewState()) {
      searchLevel = search.getDepth();
      if (searchLevel > maxSearchLevel)
      	maxSearchLevel = searchLevel;
      
      newStates++; 
      
      currentHeapCount = search.getVM().getHeap().size();
      
      if (currentHeapCount > maxHeapCount)
        maxHeapCount = currentHeapCount;
      
      if (search.isEndState()) {
        endStates++;
      }
    } else {
      revisitedStates++;
    }
  }

  @Override
  public void stateProcessed(Search search) {
    processedStates++;
  }

  @Override
  public void stateBacktracked(Search search) {
    searchLevel = search.getDepth();
    backtracks++;
  }

  @Override
  public void stateRestored(Search search) {
    searchLevel = search.getDepth();
    restoredStates++;
  }

  @Override
  public void propertyViolated(Search search) {
  }

  @Override
  public void searchStarted(Search search) {
    if (search instanceof HeuristicSearch) {
      isHeuristic = true;
    }
    
    startTime = System.currentTimeMillis();
    
    Runtime rt = Runtime.getRuntime();
    startFreeMemory = rt.freeMemory();
    totalMemory = rt.totalMemory();
    maxMemory = rt.maxMemory();
  }

  @Override
  public void searchConstraintHit(Search search) {
  }

  void reportRuntime () {
    long td = time - startTime;
    
    int h = (int) (td / 3600000);
    int m = (int) (td / 60000) % 60;
    int s = (int) (td / 1000) % 60;
    
    out.print("  abs time:          ");
    if (h < 10) out.print('0');
    out.print( h);
    out.print(':');
    if (m < 10) out.print('0');
    out.print( m);
    out.print(':');
    if (s < 10) out.print('0');
    out.print( s);
    
    out.print( "  (");
    out.print(td);
    out.println(" ms)");
  }
  
  @Override
  public void searchFinished(Search search) {
    report("------ Search statistics: ------");
  }

  void report (String header) {
    time = System.currentTimeMillis();

    out.println(header);

    reportRuntime();
        
    out.println();
    out.print("  search depth:      ");
    out.print(searchLevel);
    out.print(" (max: ");
    out.print(maxSearchLevel);
    out.println(")");
    
    out.print("  new states:        ");
    out.println(newStates);
    
    out.print("  revisited states:  ");
    out.println(revisitedStates);
        
    out.print("  end states:        ");
    out.println(endStates);

    out.print("  backtracks:        ");
    out.println(backtracks);

    out.print("  processed states:  ");
    out.print( processedStates);
    out.print(" (");
    // a little ad-hoc rounding
    double d = (double) backtracks / (double)processedStates;
    int n = (int) d;
    int m = (int) ((d - /*(double)*/ n) * 10.0);
    out.print( n);
    out.print('.');
    out.print(m);
    out.println( " bt/proc state)");
    
    out.print("  restored states:   ");
    out.println(restoredStates);

    if (isHeuristic) {
    	out.print("  queue size:        ");
    	out.println(queueSize);
    }
    
    out.println();
    out.print("  total memory [kB]: ");
    out.print(totalMemory / 1024);
    out.print(" (max: ");
    out.print(maxMemory / 1024);
    out.println(")");
    
    out.print("  free memory [kB]:  ");
    out.println(freeMemory / 1024);
    
    out.print("  max heap objects:  ");
    out.print(maxHeapCount);

    out.println();
  }
}
