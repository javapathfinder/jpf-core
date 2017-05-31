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

package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.util.test.TestJPF;

import java.util.ArrayList;

import org.junit.Test;

public class OutOfMemoryErrorTest extends TestJPF
{
   @Test
   public void outOfMemoryErrorFails()
   {
      ArrayList<byte[]> hold;
      byte hog[];
      
      if (verifyPropertyViolation(new TypeRef("gov.nasa.jpf.vm.NoOutOfMemoryErrorProperty")))
      {
         hold = new ArrayList<byte[]>();
         
         while (true)
         {
            hog = new byte[1024 * 1024 * 1024];
            
            hold.add(hog);
         }
      }
   }
}
