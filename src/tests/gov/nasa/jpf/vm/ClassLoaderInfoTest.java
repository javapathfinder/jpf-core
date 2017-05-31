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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.SystemClassLoaderInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * unit test for ClassLoaderInfo
 */
public class ClassLoaderInfoTest extends TestJPF {

  @Test
  public void testSystemClassLoader() {
    //--- Sets up the JPF environment
    String[] args = { "+vm.class=.vm.MultiProcessVM", "+target.1=HelloWorld", "+target.2=HelloWorld" };
    Config config = new Config(args);
    JPF jpf = new JPF(config);
    VM vm = jpf.getVM();
    Heap heap = vm.getHeap();

    vm.initialize(); // this should instantiate two SystemClassLoaders

    ThreadInfo[] threads = vm.getLiveThreads();
    assertTrue( threads.length == 2);

    //--- app 0
    SystemClassLoaderInfo cl0 = threads[0].getSystemClassLoaderInfo();
    assertTrue( cl0 != null);
    assertTrue( cl0.parent == null);

    int cl0ObjRef = cl0.objRef;
    assertTrue( cl0ObjRef != MJIEnv.NULL);
    ElementInfo ei0 = heap.get(cl0ObjRef);
    assertTrue( ei0.getIntField( ClassLoaderInfo.ID_FIELD) == cl0.getId());
    
    //--- app 1
    SystemClassLoaderInfo cl1 = threads[1].getSystemClassLoaderInfo();
    assertTrue( cl1 != null);
    assertTrue( cl0.parent == null);
    
    int cl1ObjRef = cl1.objRef;
    assertTrue( cl1ObjRef != MJIEnv.NULL);
    ElementInfo ei1 = heap.get(cl1ObjRef);
    assertTrue( ei1.getIntField( ClassLoaderInfo.ID_FIELD) == cl1.getId());
    
    //--- compare them
    assertTrue( cl0 != cl1);
    assertTrue( cl0.getId() != cl1.getId());
    assertTrue( cl0.statics != cl1.statics);
    assertTrue( cl0ObjRef != cl1ObjRef);

    //--- compare the loaded classes
    ClassInfo ci0 = cl0.getResolvedClassInfo("java.lang.Class");
    ClassInfo ci1 = cl1.getResolvedClassInfo("java.lang.Class");

    assertTrue( ci0 != ci1);
    assertTrue( ci0.getUniqueId() != ci1.getUniqueId());

    assertTrue( ci0.getName().equals(ci1.getName()));
    assertTrue( ci0.getClassFileUrl().equals(ci1.getClassFileUrl()));
    
    //--- should compare on-demand loaded classes here..
  }
}
