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

/**
 * a AllRunnablesSyncPolicy that only considers the runnables with the highest
 * priority as choices
 * 
 * 2do - it doesn't make much sense to compare priorities across process
 * boundaries (unless we model distributed apps running on the same machine)
 */
public class PriorityRunnablesSyncPolicy extends AllRunnablesSyncPolicy {

  public PriorityRunnablesSyncPolicy(Config config) {
    super(config);
  }
  
  @Override
  protected ThreadInfo[] getTimeoutRunnables (ApplicationContext appCtx){
    ThreadInfo[] allRunnables = super.getTimeoutRunnables(appCtx);
    
    int maxPrio = Integer.MIN_VALUE;
    int n=0;
    for (int i=0; i<allRunnables.length; i++){
      ThreadInfo ti = allRunnables[i];
      if (ti != null){
        int prio = ti.getPriority();
        
        if (prio > maxPrio){
          maxPrio = prio;
          for (int j=0; j<i; j++){
            allRunnables[j]= null;
          }
          n = 1;
          
        } else if (prio < maxPrio){
          allRunnables[i] = null;
          
        } else { // prio == maxPrio
          n++;
        }
      }
    }
    
    if (n < allRunnables.length){
      ThreadInfo[] prioRunnables = new ThreadInfo[n];
      for (int i=0, j=0; j<n; j++, i++){
        if (allRunnables[i] != null){
          prioRunnables[j++] = allRunnables[i];
        }
      }

      return prioRunnables;
      
    } else { // all runnables had the same prio
      return allRunnables;
    }
  }
}
