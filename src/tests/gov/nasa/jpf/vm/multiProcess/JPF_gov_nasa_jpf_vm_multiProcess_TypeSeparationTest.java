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

package gov.nasa.jpf.vm.multiProcess;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class JPF_gov_nasa_jpf_vm_multiProcess_TypeSeparationTest extends NativePeer {
  private static List<Integer> prcIds = new ArrayList<Integer>();

  protected static void resetPrcIds() {
    prcIds.clear();
  }

  private static List<ClassInfo> annClasses =  new ArrayList<ClassInfo>();

  @MJI
  public void keepAnnotationClass__Ljava_lang_Class_2I__V(MJIEnv env, int objRef, int annoClsRef, int prcId) {
    ClassInfo aci = env.getReferredClassInfo(annoClsRef);
    if(!prcIds.contains(prcId)) {
      prcIds.add(prcId);
      annClasses.add(aci);
    }
  }

  protected static List<ClassInfo> getAnnotationClasses() {
    return annClasses;
  }

  private static List<ClassLoaderInfo> classLoaders =  new ArrayList<ClassLoaderInfo>();

  @MJI
  public void keepClassLoader__Ljava_lang_ClassLoader_2I__V(MJIEnv env, int objRef, int clRef, int prcId) {
    ClassLoaderInfo cl = env.getClassLoaderInfo(clRef);

    if(!prcIds.contains(prcId)) {
      prcIds.add(prcId);
      classLoaders.add(cl);
    }
  }

  protected static List<ClassLoaderInfo> getClassLoaders() {
    return classLoaders;
  }
}
