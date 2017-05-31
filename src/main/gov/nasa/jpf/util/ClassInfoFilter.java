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

package gov.nasa.jpf.util;

import gov.nasa.jpf.vm.ClassInfo;

/**
 * utility class that can be used by InstructionFactory implementations to
 * selectively replace bytecodes for specified class sets.
 *
 * Filtering is based on include/exclude name patterns (e.g. for packages) and/or
 * on inheritance (both down- and upwards)
 */
public class ClassInfoFilter {

    // filter using an explicit set of class names (can be used for one-pass load)
  protected StringSetMatcher includes;  // included classes that should use them
  protected StringSetMatcher excludes;  // excluded classes (that should NOT use them)

  // filter using base/derived class sets (only useful in subsequent pass)
  ClassInfo ciLeaf;
  ClassInfo ciRoot;

  public ClassInfoFilter (String[] includeCls, String[] excludeCls,
                                   ClassInfo rootCls, ClassInfo leafCls) {
    includes = StringSetMatcher.getNonEmpty(includeCls);
    excludes = StringSetMatcher.getNonEmpty(excludeCls);

    ciRoot = rootCls;
    ciLeaf = leafCls;
  }


  public boolean isPassing (ClassInfo ci){
    if (ci == null){

      // <??> not clear what to do in this case, since we have nothing to
      // filter on. Since all reflection calls come in here, it's probably
      // better to instrument by default (until we have a better mechanism)
      return true;

    } else {
      String clsName = ci.getName();

      if (StringSetMatcher.isMatch(clsName, includes, excludes)){
        if (ciLeaf == null || ciLeaf.isInstanceOf(ci)){
          if (ciRoot == null || ci.isInstanceOf(ciRoot)){
            return true;
          }
        }
      }
    }

    return false;
  }

}
