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

import java.io.OutputStream;

import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.vm.DebugStateSerializer;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.JPFOutputStream;
import gov.nasa.jpf.vm.NativeStateHolder;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * a CFSerializer that stores the serialized program state in a 
 * readable/diffable format.
 * 
 * Automatically used by Debug..StateSet if the configured vm.storage.class is .vm.DebugJenkinsStateSet 
 */
public class DebugCFSerializer extends CFSerializer implements DebugStateSerializer {

  JPFOutputStream os;
  
  // this is for debugging purposes only
  public DebugCFSerializer() {
    os = new JPFOutputStream(System.out);
  }
  
  @Override
  public void setOutputStream (OutputStream s){
    os = new JPFOutputStream(s);
  }
  
  @Override
  protected int[] computeStoringData() {    
    os.printCommentLine("------------------------ serialized state");
    return super.computeStoringData();
  }
  
  @Override
  protected void processReferenceQueue(){
    os.println();
    os.printCommentLine("--- heap");
    os.println();
    super.processReferenceQueue();
  }
  
  @Override
  public void process (ElementInfo ei) {
    super.process( ei);
    
    FinalBitSet filtered = !ei.isArray() ? getInstanceFilterMask(ei.getClassInfo()) : null;
    os.print(ei, filtered);
    os.println();
  }
  
  @Override
  protected void serializeClassLoaders(){
    os.println();
    os.printCommentLine("--- classes");
    os.println();
    super.serializeClassLoaders();
  }
  
  @Override
  protected void serializeClass (StaticElementInfo sei){
    super.serializeClass(sei);
    
    FinalBitSet filtered = getStaticFilterMask(sei.getClassInfo());
    os.print(sei, filtered);
    os.println();    
  }
  
  @Override
  protected void serializeStackFrames(){
    os.println();
    os.printCommentLine("--- threads");
    os.println();
    super.serializeStackFrames();
  }
  
  @Override
  protected void serializeStackFrames(ThreadInfo ti){
    os.println();
    os.print(ti);
    os.println();
    super.serializeStackFrames(ti);
  }
  
  @Override
  protected void serializeFrame(StackFrame frame){
    os.print(frame);
    os.println();
    super.serializeFrame(frame);
  }
  
  @Override
  protected void serializeNativeStateHolders(){
    os.println();
    os.printCommentLine("--- native state holders");
    os.println();
    super.serializeNativeStateHolders();
  }
  
  @Override
  protected void serializeNativeStateHolder (NativeStateHolder nsh){
    os.print(nsh);
    os.println();
    super.serializeNativeStateHolder(nsh);
  }
  
}
