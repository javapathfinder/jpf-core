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

package gov.nasa.jpf.vm.choice;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;

public class DoubleSpec {

  /**
   * return double from String spec, which can be either a literal
   * or a local variable name, or a field name
   */
  public static double eval (String spec) {
    double ret;
    
    char c = spec.charAt(0);
    if (Character.isDigit(c) || (c == '+') || (c == '-') || (c == '.')) {
      try {
        ret = Double.parseDouble(spec); 
      } 
      catch (NumberFormatException nfx) {
        throw new JPFException("illegal double spec: " + spec);
      }
    } else {
      ret = resolveVar(spec);      
    }
    return ret;
  }

  public static double resolveVar(String spec){
    VM vm = VM.getVM();
    String[] varId = spec.split("[.]+");

    double ret;
    switch (varId.length){
    case 1: { // variable name
      ThreadInfo ti = ThreadInfo.getCurrentThread();
      try {
        StackFrame frame = ti.getTopFrame();

        ret = frame.getDoubleLocalVariable(varId[0]);
        // that throws an exception (a few calls down) if  
        // the name is not found...
      }
      catch (JPFException e){ //not local? try a field!
        int id = ti.getThis();
        if(id>=0){  // in a normal (non-static) method
          ElementInfo ei = vm.getElementInfo(id);
          ret = ei.getDoubleField(varId[0]);
        }
        else { // static method (no this)- must be static var
          ClassInfo ci = ti.getTopFrameMethodInfo().getClassInfo();
          ElementInfo ei = ci.getStaticElementInfo();
          ret = ei.getDoubleField(varId[0]);
        }
      }
      break;
    }
    case 2: { // static variable name TODO other cases here...
      ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo( varId[0]);
      ElementInfo ei = ci.getStaticElementInfo();
      ret = ei.getDoubleField(varId[1]);
      break;
    }
    default: 
      throw new JPFException("Choice value format error parsing \"" + spec +"\"");
    }
    return ret;
  }

  
}
