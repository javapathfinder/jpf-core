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

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * native peer for the MethodTester tool
 */
public class JPF_gov_nasa_jpf_tools_MethodTester extends NativePeer {

  @MJI
  public void log__Ljava_lang_String_2__V (MJIEnv env, int objRef, int msgRef){
    String msg = env.getStringObject(msgRef);
    System.out.println("@ " + msg);
  }
  
  @MJI
  public void error__Ljava_lang_String_2__V (MJIEnv env, int objRef, int msgRef){
    String msg = env.getStringObject(msgRef);
    System.err.println(msg);    
  }

}
