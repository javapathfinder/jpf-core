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

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * even more aggressive under-approximation than AdaptiveSerializer. This one
 * only looks at the top frame of each thread, and only serializes objects
 * referenced from there. It ignores static fields and deeper heap objects that
 * are not directly referenced.
 *
 * While this seems too aggressive, it actually finds a lot of concurrency
 * defects in real world applications. This is esp. true if there are
 * a lot of field access CGs, i.e. shared objects. In this case, the
 * TopFrameSerializer can behave an order of magnitude better than CFSerializer
 */
public class TopFrameSerializer extends CFSerializer {

  boolean traverseObjects;

  @Override
  protected void initReferenceQueue() {
    super.initReferenceQueue();

    traverseObjects = true;
  }

  @Override
  protected void serializeStackFrames(ThreadInfo ti){
    // we just look at the top frame
    serializeFrame(ti.getTopFrame());
  }


  @Override
  protected void queueReference(ElementInfo ei){
    if (traverseObjects){
      refQueue.add(ei);
    }
  }

  @Override
  protected void processReferenceQueue() {
    // we only go one level deep
    traverseObjects = false;
    refQueue.process(this);
  }

  @Override
  protected void serializeClassLoaders(){
    // totally ignore statics
  }
}
