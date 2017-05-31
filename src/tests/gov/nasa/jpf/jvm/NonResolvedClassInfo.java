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
package gov.nasa.jpf.jvm;

import gov.nasa.jpf.jvm.bytecode.InstructionFactory;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.NativePeer;
import java.io.File;

/**
 * just a helper construct to create ClassInfos that can be used in unit tests
 * (without superclasses, clinit calls and the other bells and whistles)
 */
class NonResolvedClassInfo extends JVMClassInfo {
    
  NonResolvedClassInfo (String clsName, File file) throws ClassParseException {
    super( clsName, null, new ClassFile(file), file.getAbsolutePath(), new JVMCodeBuilder(new InstructionFactory()));
  }

  //--- these are overridden so that we can create instances without the whole JPF ClassInfo environment
  
  @Override
  protected void resolveClass() {
    linkFields();
  }

  @Override
  protected NativePeer loadNativePeer(){
    return null;
  }
  
  @Override
  protected void setAssertionStatus(){
    // nothing
  }
  
}