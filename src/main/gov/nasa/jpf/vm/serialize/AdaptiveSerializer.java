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

package gov.nasa.jpf.vm.serialize;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;

/**
 * a CG type adaptive, canonicalizing & filtering serializer that is an
 * under-approximation mostly aimed at finding data races and deadlocks in programs
 * with a large number of scheduling points (= thread choices)
 *
 * This came to bear by accidentally discovering that JPF often seems to finds
 * concurrency defects by just serializing the thread states, their topmost stack
 * frames and the objects directly referenced from there.
 * For non-scheduling points, we just fall back to serializing statics, all thread
 * stacks and all the data reachable from there
 */
public class AdaptiveSerializer extends CFSerializer {

  boolean traverseObjects;
  boolean isSchedulingPoint;

  @Override
  protected void initReferenceQueue() {
    super.initReferenceQueue();
    traverseObjects = true;

    ChoiceGenerator<?> nextCg = vm.getNextChoiceGenerator();
    isSchedulingPoint = (nextCg != null) && nextCg.isSchedulingPoint();
  }

  @Override
  protected void queueReference(ElementInfo ei){
    if (traverseObjects){
      refQueue.add(ei);
    }
  }

  @Override
  protected void processReferenceQueue() {
    if (isSchedulingPoint){
      traverseObjects = false;
    }
    refQueue.process(this);
  }

  //@Override
  @Override
  protected void serializeClassLoaders(){
    // for thread CGs we skip this - assuming that this is only relevant if there is
    // a class object lock, which is covered by the thread lock info
    if (!isSchedulingPoint){
      // <2do> this seems too conservative - we should only serialize what is
      // used from this thread, which can be collected at class load time
      // by looking at GET/PUTSTATIC targets (and their superclasses)
      super.serializeClassLoaders();
    }
  }
}
