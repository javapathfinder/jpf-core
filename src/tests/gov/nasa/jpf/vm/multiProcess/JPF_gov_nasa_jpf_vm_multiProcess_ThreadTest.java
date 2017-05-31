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

package gov.nasa.jpf.vm.multiProcess;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class JPF_gov_nasa_jpf_vm_multiProcess_ThreadTest extends NativePeer {
  private static List<Integer> prcIds = new ArrayList<Integer>();

  protected static void resetPrcIds() {
    prcIds.clear();
  }

  private static List<ThreadInfo> threads =  new ArrayList<ThreadInfo>();

  protected static void resetThreads() {
    threads.clear();
  }

  @MJI
  public void keepThread__Ljava_lang_Thread_2I__V(MJIEnv env, int objRef, int thdRef, int prcId) {
    ThreadInfo ti = env.getThreadInfoForObjRef(thdRef);
    if(!prcIds.contains(prcId)) {
      prcIds.add(prcId);
      threads.add(ti);
    }
  }

  protected static List<ThreadInfo> getThreads() {
    return threads;
  }

  private static List<Integer> threadIds = new ArrayList<Integer>();

  @MJI
  public static void addToThreads__Ljava_lang_Thread_2__V (MJIEnv env, int objRef, int thdRef) {
    ThreadInfo ti = env.getThreadInfoForObjRef(thdRef);

    int id = ti.getId();
    if(!threadIds.contains(id)) {
      threadIds.add(id);
    }
  }

  protected static List<Integer> getThreadIds() {
    return threadIds;
  }
}
