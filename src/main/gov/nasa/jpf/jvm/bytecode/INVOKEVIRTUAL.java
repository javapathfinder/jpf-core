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

import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.RecordComponentInfo;
import gov.nasa.jpf.JPFException;



/**
 * Invoke instance method; dispatch based on class
 * ..., objectref, [arg1, [arg2 ...]] => ...
 */
public class INVOKEVIRTUAL extends VirtualInvocation {
  public INVOKEVIRTUAL () {}

  protected INVOKEVIRTUAL (String clsDescriptor, String methodName, String signature){
    super(clsDescriptor, methodName, signature);
  }
  @Override
  public MethodInfo getInvokedMethod(ThreadInfo ti) {
    ClassInfo ci=ti.resolveReferencedClass(cname);
    if(ci!=null) {
      return ci.getMethod(mname,true);
    }
    return null;
  }

  @Override
  public int getByteCode () {
    return 0xB6;
  }
  
  @Override
  public String toString() {
    // methodInfo not set outside real call context (requires target object)
    return "invokevirtual " + cname + '.' + mname;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction execute(ThreadInfo ti) {
    MethodInfo mi=getInvokedMethod(ti);
    if(mi==null) {
      throw new JPFException("Method not found:"+cname+'.'+mname);
    }
    if(mi.isRecordAccessor()) {
      Object target =ti.getTopFrame().getThis();
      if(target instanceof Record) {
        try {
          RecordComponentInfo[] components =((ClassInfo)mi.getClassInfo()).getRecordComponents();
          for(RecordComponentInfo component: components) {
            if(mi.getName().equals(component.getAccessor().invoke(target))) {
              Object result =component.getAccessor().invoke(target);
              ti.getTopFrame().setReturnValue(result);
              return ti.getPC().getNext();
            }

          }
        }
        catch(Exception e) {
          throw new JPFException("Failed to invoke accessor for record:"+mi.getName());
        }
      }
    }
    return super.execute(ti);
  }
}
