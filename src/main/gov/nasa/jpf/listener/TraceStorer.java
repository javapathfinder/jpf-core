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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * tool to save traces upon various conditions like
 *  - property violation
 *  - call of a certain method
 *  - reaching a certain search depth
 *  - creating a certain thread
 */
public class TraceStorer extends ListenerAdapter {

  int nTrace = 1; 

  String traceFileName;
  
  // do we store to the same file? (i.e. overwrite previously stored traces)
  // if set to 'true', store all traces (in <traceFileName>.n)
  boolean storeMultiple;
  
  // do we want to terminate after first store, even if it's triggered by a
  // property violation?
  boolean terminateOnStore;
  
  boolean storeOnConstraintHit;
  
  // search depth at what we store the tace 
  int storeDepth;
  
  // calls that should trigger a store
  StringSetMatcher storeCalls;
  
  // thread starts that should trigger a store
  StringSetMatcher storeThreads;
  
  // do we want verbose output
  boolean verbose;
  
  Search search;
  VM vm;
  
  public TraceStorer (Config config, JPF jpf){
    
    traceFileName = config.getString("trace.file", "trace");
    storeMultiple = config.getBoolean("trace.multiple", false);    
    storeDepth = config.getInt("trace.depth", Integer.MAX_VALUE);
    verbose = config.getBoolean("trace.verbose", false);
    
    terminateOnStore = config.getBoolean("trace.terminate", false);
    storeOnConstraintHit = config.getBoolean("trace.store_constraint", false);
    
    storeCalls = StringSetMatcher.getNonEmpty(config.getStringArray("trace.store_calls"));
    storeThreads = StringSetMatcher.getNonEmpty(config.getStringArray("trace.store_threads"));
    
    vm = jpf.getVM();
    search = jpf.getSearch();
  }
  
  void storeTrace(String reason) {
    String fname = traceFileName;
    
    if (storeMultiple){
      fname = fname  + '.' + nTrace++;
    }
    
    vm.storeTrace(fname, reason, verbose); // <2do> maybe some comment would be in order
  }
  
  @Override
  public void propertyViolated (Search search){
    // Ok, this is unconditional
    storeTrace("violated property: " + search.getLastError().getDetails());
    
    // no need to terminate (and we don't want to interfere with search.multiple_errors)
  }
 
  @Override
  public void stateAdvanced (Search search){
    if (search.getDepth() == storeDepth){
      storeTrace("search depth reached: " + storeDepth);
      checkSearchTermination();
    }
  }
  
  @Override
  public void searchConstraintHit (Search search){
    if (storeOnConstraintHit){
      storeTrace("search constraint hit: " + search.getLastSearchConstraint());      
    }
  }
  
  @Override
  public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
    if (storeCalls != null){
      if (executedInsn instanceof JVMInvokeInstruction) {
        JVMInvokeInstruction iinsn = (JVMInvokeInstruction)executedInsn;
        String clsName = iinsn.getInvokedMethodClassName();
        String mthName = iinsn.getInvokedMethodName();
        String mn = clsName + '.' + mthName;
        
        if (storeCalls.matchesAny(mn)){
          storeTrace("call: " + mn);
          checkVMTermination(ti);
        }
      }
    }
  }
  
  @Override
  public void threadStarted(VM vm, ThreadInfo ti) {
    if (storeThreads != null){
      String tname = ti.getName();
      if (storeThreads.matchesAny( tname)){
        storeTrace("thread started: " + tname);
        checkVMTermination(ti);
      }
    } 
  }

  boolean checkVMTermination(ThreadInfo ti) {
    if (terminateOnStore){
      ti.breakTransition("storeTraceTermination");
      search.terminate();
      return true;
    }
    
    return false;
  }
  
  boolean checkSearchTermination() {
    if (terminateOnStore){
      search.terminate();
      return true;
    }
    
    return false;
  }
}
