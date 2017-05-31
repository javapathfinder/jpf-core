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
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * simple tools to gather statistics about instructions executed by JPF.
 * InsnCounter is mostly a VMListener that observes 'instructionExecuted'
 */
public class InsnCounter extends ListenerAdapter {

  String[] opCodes = new String[500];
  int[] counts = new int[500];
  int   total;
  
  //----------------------------------------- SearchKistener interface
  @Override
  public void searchFinished(Search search) {
    reportStatistics();
  }
    
  //----------------------------------------------------- VMListener interface
  @Override
  public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
    int bc = executedInsn.getByteCode();
    
    if (opCodes[bc] == null) {
      opCodes[bc] = executedInsn.getMnemonic();
    }
    counts[bc]++;
    total++;
  }

  
  //----------------------------------------------------- internal stuff
  void reportStatistics () {
    int[] sorted = getSortedCounts();
    int i;
    
    int total = 0;
    
    for (i=0; i<sorted.length; i++) {
      int idx = sorted[i];
      String opc = opCodes[idx];
            
      if (counts[idx] > 0) {
        System.out.print( i);
        System.out.print( "  ");
        System.out.print( opc);
        System.out.print( " : ");
        System.out.println( counts[idx]);
        
        total += counts[idx];
      } else {
        break;
      }
    }
    
    System.out.println();
    System.out.println("total number of executed instructions: " + total);
  }
  
  int[] getSortedCounts () {
    int[] sorted = new int[256];
    int last = -1;
    int i, j;
    
    for (i=0; i<256; i++) {
      int c = counts[i];
      if (c > 0) {
        for (j=0; j<last; j++) {
          if (counts[sorted[j]] < c) {
            System.arraycopy(sorted, j, sorted, j+1, (last-j));
            break;
          }
        }
        sorted[j] = i;
        last++;
      }
    }
    
    return sorted;
  }
  
  void filterArgs (String[] args) {
    // we don't have any yet
  }
}

