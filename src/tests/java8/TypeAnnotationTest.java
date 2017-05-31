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

package java8;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.AbstractTypeAnnotationInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.VM;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;


/**
 * regression test for Java 8 type annotations (JSR 308)
 */
public class TypeAnnotationTest extends TestJPF {

  //--- test type annotations
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE_USE)
  @interface MyTA {}

  //--- test class hierarchy
  
  interface Ifc {}
  static class Base {}
  
  public class Anno8 extends @MyTA Base implements @MyTA Ifc {

    @MyTA int data;

    @MyTA int baz (@MyTA int a, int b){
      @MyTA int x = a + b;
      return x;
    }
  }
  
  //--- listener to check annotations are set
  public static class Listener extends ListenerAdapter {
    
    protected int numberOfTargetTypes (AbstractTypeAnnotationInfo[] annos, int targetType){
      int n = 0;
      for (AbstractTypeAnnotationInfo tai : annos){
        if (tai.getTargetType() == targetType){
          n++;
        }
      }
      return n;
    }
    
    @Override
    public void classLoaded(VM vm, ClassInfo loadedClass) {
      if (loadedClass.getName().equals("java8.TypeAnnotationTest$Anno8")){
        System.out.println("checking loaded class " + loadedClass.getName() + " for type annotations..");
        
        // <2do> - needs more tests..
        
        System.out.println("--- super types");
        AbstractTypeAnnotationInfo[] tais = loadedClass.getTypeAnnotations();
        for (AbstractTypeAnnotationInfo tai : tais){
          System.out.println("  " + tai);
        }
        assertTrue(tais.length == 2);
        assertTrue( numberOfTargetTypes(tais, ClassFile.CLASS_EXTENDS) == 2); // base and interface
        
        System.out.println("--- fields");
        FieldInfo fi = loadedClass.getDeclaredInstanceField("data");
        tais = fi.getTypeAnnotations();
        for (AbstractTypeAnnotationInfo tai : tais){
          System.out.println("  " + tai);
        }
        assertTrue(tais.length == 1);
        assertTrue( numberOfTargetTypes(tais, ClassFile.FIELD) == 1);
        
        System.out.println("--- methods");
        MethodInfo mi = loadedClass.getMethod("baz(II)I", false);
        tais = mi.getTypeAnnotations();
        for (AbstractTypeAnnotationInfo tai : tais){
          System.out.println("  " + tai);
        }
        assertTrue(tais.length == 3);
        assertTrue( numberOfTargetTypes(tais, ClassFile.METHOD_RETURN) == 1);
        assertTrue( numberOfTargetTypes(tais, ClassFile.METHOD_FORMAL_PARAMETER) == 1);
        assertTrue( numberOfTargetTypes(tais, ClassFile.LOCAL_VARIABLE) == 1);
        
        LocalVarInfo lv = mi.getLocalVar("x", 4);
        System.out.println("--- local var " + lv);
        tais = lv.getTypeAnnotations();
        for (AbstractTypeAnnotationInfo tai : tais){
          System.out.println("  " + tai);
        }
        assertTrue(tais.length == 1);
        assertTrue( numberOfTargetTypes(tais, ClassFile.LOCAL_VARIABLE) == 1);
        
      }
    }
  }
  
  @Test
  public void testBasicTypeAnnotations (){
    if (verifyNoPropertyViolation("+listener=java8.TypeAnnotationTest$Listener")){
      Anno8 anno8 = new Anno8();
    }
  }
  
}
