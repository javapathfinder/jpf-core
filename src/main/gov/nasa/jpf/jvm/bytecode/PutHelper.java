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

package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * helper class to factor out common PUT code
 * 
 * <2do> This is going to be moved into a Java 8 interface with default methods
 */
public class PutHelper {

  protected static boolean hasNewValue (ThreadInfo ti, StackFrame frame, ElementInfo eiFieldOwner, FieldInfo fi){
    Object valAttr = null;
    int fieldSize = fi.getStorageSize();
    
    if (fieldSize == 1){
      valAttr = frame.getOperandAttr();
      int val = frame.peek();
      if (eiFieldOwner.get1SlotField(fi) != val){
        return true;
      }
      
    } else {
      valAttr = frame.getLongOperandAttr();
      long val = frame.peekLong();
      if (eiFieldOwner.get2SlotField(fi) != val){
        return true;
      }
    }
    
    if (eiFieldOwner.getFieldAttr(fi) != valAttr){
      return true;
    }
    
    return false;
  }
  
  protected static int setReferenceField (ThreadInfo ti, StackFrame frame, ElementInfo eiFieldOwner, FieldInfo fi){
    Object valAttr = frame.getOperandAttr();
    int val = frame.peek();
    eiFieldOwner.set1SlotField(fi, val);
    eiFieldOwner.setFieldAttr(fi, valAttr);
    return val;
  }
  
  protected static long setField (ThreadInfo ti, StackFrame frame, ElementInfo eiFieldOwner, FieldInfo fi){
    int fieldSize = fi.getStorageSize();
    
    if (fieldSize == 1){
      Object valAttr = frame.getOperandAttr();
      int val = frame.peek();
      eiFieldOwner.set1SlotField(fi, val);
      eiFieldOwner.setFieldAttr(fi, valAttr);
      return val;
      
    } else {
      Object valAttr = frame.getLongOperandAttr();
      long val = frame.peekLong();
      eiFieldOwner.set2SlotField(fi, val);
      eiFieldOwner.setFieldAttr(fi, valAttr);
      return val;
    }
  }
}
