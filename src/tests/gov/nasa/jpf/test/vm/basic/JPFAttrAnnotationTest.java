/*
 * Copyright (C) 2015, United States Government, as represented by the
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

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.annotation.JPFAttribute;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.VM;
import org.junit.Test;

/**
 * regression test for JPFAttribute annotations
 */
public class JPFAttrAnnotationTest extends TestJPF {

  static final String LISTENER = "gov.nasa.jpf.test.vm.basic.JPFAttrAnnotationTest$LoadListener";
  static final String ATTR_CLS = "gov.nasa.jpf.test.vm.basic.JPFAttrAnnotationTest$MyAttr"; // needs to const
  static final String TGT_CLS = "gov.nasa.jpf.test.vm.basic.JPFAttrAnnotationTest$SomeClass";
  
  public static class MyAttr {}
  
  @JPFAttribute(ATTR_CLS)
  public static class SomeClass {

    Object data1 = 42;
    
    @JPFAttribute(ATTR_CLS)
    Object data2 = "whatever";
    
    public void foo(){}
    
    @JPFAttribute(ATTR_CLS)
    public void bar(){
      System.out.println("SomeClass.bar() executed");
    }
  }
  
  public static class LoadListener extends ListenerAdapter {
    
    @Override
    public void classLoaded (VM vm, ClassInfo ci){
      if (ci.getName().equals(TGT_CLS)){
        System.out.println("#--- checking attribute annotations of " + ci.getName());
        
        assertTrue( ci.hasAttr(MyAttr.class));
        System.out.println("# class attr Ok");
        
        MethodInfo mi = ci.getMethod("bar()V", false);
        assertTrue( mi.hasAttr(MyAttr.class));
        System.out.println("# method bar() attr Ok");
       
        mi = ci.getMethod("foo()V", false);
        assertFalse( mi.hasAttr(MyAttr.class));
        
        FieldInfo fi = ci.getDeclaredInstanceField("data2");
        assertTrue( fi.hasAttr(MyAttr.class));
        System.out.println("# field data2 attr Ok");

        fi = ci.getDeclaredInstanceField("data1");
        assertFalse( fi.hasAttr(MyAttr.class));
      }
    }
  }
  
  @Test
  public void testAttrs(){
    if (verifyNoPropertyViolation("+listener=" + LISTENER)){
      SomeClass o = new SomeClass();
      o.bar();
    }
  }
  
}
