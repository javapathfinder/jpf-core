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

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.NEW;
import gov.nasa.jpf.util.LocationSpec;
import gov.nasa.jpf.util.TypeSpec;
import gov.nasa.jpf.vm.bytecode.NewInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * simulator for OutOfMemoryErrors. This can be configured to either
 * fire for a specified location range (file:line) or specified types.
 * Ranges are transitive, i.e. everything called from within it should also
 * trigger.  
 * 
 * Since our only action is to inject OutOfMemoryErrors, we don't need
 * to implement a Property interface
 */
public class OOMEInjector extends ListenerAdapter {

  static class OOME {}
  static OOME throwOOME = new OOME(); // we can reuse the same object as an attribute
  
  List<LocationSpec> locations = new ArrayList<LocationSpec>();
  List<TypeSpec> types = new ArrayList<TypeSpec>();
  
  public OOMEInjector (Config config, JPF jpf){
    String[] spec = config.getStringArray("oome.locations");
    if (spec != null){
      for (String s : spec){
        LocationSpec locSpec = LocationSpec.createLocationSpec(s);
        if (locSpec != null){
          locations.add(locSpec);
        }
      }
    }
    
    spec = config.getStringArray("oome.types");
    if (spec != null){
      for (String s : spec){
        TypeSpec typeSpec = TypeSpec.createTypeSpec(s);
        if (typeSpec != null){
          types.add(typeSpec);
        }
      }      
    }
  }
  
  protected void markMatchingInstructions (MethodInfo mi, LocationSpec locSpec){
    int first = locSpec.getFromLine();
    int[] lineNumbers = mi.getLineNumbers();
              
    if (lineNumbers != null && first >= lineNumbers[0]){
      int last = locSpec.getToLine();
      for (int i=0; i<lineNumbers.length; i++){
        int l = lineNumbers[i];
        if (last < lineNumbers[i]){
          return;
        } else {
          Instruction insn = mi.getInstruction(i);
          insn.addAttr(throwOOME);                
        }
      }
    }    
  }
  
  @Override
  public void classLoaded (VM vm, ClassInfo loadedClass){
    String fname = loadedClass.getSourceFileName();
    
    for (TypeSpec typeSpec : types){
      if (typeSpec.matches(loadedClass)){
        loadedClass.addAttr(throwOOME);
      }
    }

    // if we have a matching typespec this could be skipped, but maybe
    // we also want to cover statis methods of this class
    for (LocationSpec locSpec : locations){
      if (locSpec.matchesFile(fname)){
        for (MethodInfo mi : loadedClass.getDeclaredMethodInfos()){
          markMatchingInstructions(mi, locSpec);
        }
      }
    }
  }
  
  protected boolean checkCallerForOOM (StackFrame frame, Instruction insn){
    // these refer to the calling code
    return (insn.hasAttr(OOME.class) || frame.hasFrameAttr(OOME.class));
  }
  
  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute){
    if (insnToExecute instanceof NewInstruction){
      if (checkCallerForOOM(ti.getTopFrame(), insnToExecute)){
        // we could use Heap.setOutOfMemory(true), but then we would have to reset
        // if the app handles it so that it doesn't throw outside the specified locations.
        // This would require more effort than throwing explicitly
        Instruction nextInsn = ti.createAndThrowException("java.lang.OutOfMemoryError");
        ti.skipInstruction(nextInsn);
      }
    }
  }
  
  @Override
  public void instructionExecuted (VM vm, ThreadInfo ti, Instruction insn, Instruction executedInsn){
    
    if (executedInsn instanceof JVMInvokeInstruction){
      StackFrame frame = ti.getTopFrame();
      
      if (frame.getPC() != executedInsn){ // means the call did succeed
        if (checkCallerForOOM(frame.getPrevious(), executedInsn)){
          frame.addFrameAttr(throwOOME); // propagate caller OOME context
        }
      }
      
    } else if (executedInsn instanceof NEW){
      if (!types.isEmpty()){
        int objRef = ((NEW) executedInsn).getNewObjectRef();
        if (objRef != MJIEnv.NULL) {
          ClassInfo ci = vm.getClassInfo(objRef);
          if (ci.hasAttr(OOME.class)) {
            Instruction nextInsn = ti.createAndThrowException("java.lang.OutOfMemoryError");
            ti.setNextPC(nextInsn);
          }
        }
      }
    }
  }
}
