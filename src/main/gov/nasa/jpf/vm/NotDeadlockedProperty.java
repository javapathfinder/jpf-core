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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.GenericProperty;
import gov.nasa.jpf.search.Search;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * property class to check for no-runnable-threads conditions
 */
public class NotDeadlockedProperty extends GenericProperty {
  Search search;
  ThreadInfo tiAtomic;
  
  public NotDeadlockedProperty (Config conf, Search search) {
    this.search = search; 
  }
  
  @Override
  public String getErrorMessage () {
    VM vm = search.getVM();
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    if (tiAtomic != null){
      pw.println("blocked in atomic section:");
    } else {
      pw.println("deadlock encountered:");
    }
    
    ThreadInfo[] liveThreads = vm.getLiveThreads();
    for (ThreadInfo ti : liveThreads) {
      pw.print("  ");
      if (ti == tiAtomic){
        pw.print("ATOMIC ");
      }
      pw.println(ti.getStateDescription());
    }
    
    return sw.toString();
  }

  @Override
  public boolean check (Search search, VM vm) {
    if (vm.isDeadlocked()){
      ThreadInfo ti = vm.getCurrentThread();
      if (ti.isAtomic()){
        tiAtomic = ti;
      }
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void reset() {
    tiAtomic = null;
  }
}
