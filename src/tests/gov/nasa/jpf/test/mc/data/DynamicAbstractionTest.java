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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.vm.serialize.AbstractionAdapter;

import org.junit.Test;

/**
 * regression test for field value abstractions
 */
public class DynamicAbstractionTest extends TestJPF {
  
  static final String SERIALIZER_ARG = "+vm.serializer.class=.vm.serialize.DynamicAbstractionSerializer";
  
  static class MyClass {
    int data;
    double notAbstracted;
  }
  
  public static class MyClassDataAbstraction extends AbstractionAdapter {
    
    @Override
    public int getAbstractValue (int data){
      int cat = 1;
      if (data > 5) cat = 2;
      if (data > 10) cat = 3;
      
      System.out.println("abstracted value for " + data + " = " + cat);
      return cat;
    }
  }
  
  //---------------------------------------------------------------------------
  
  @Test
  public void testMyClass() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation(SERIALIZER_ARG,
                                  "+das.classes.include=*$MyClass",
                                  "+das.fields=data", 
                                  "+das.data.field=*$MyClass.data",
                                  "+das.data.abstraction=gov.nasa.jpf.test.mc.data.DynamicAbstractionTest$MyClassDataAbstraction")){
      MyClass matchedObject = new MyClass();
      matchedObject.data = Verify.getInt(0, 20);
      
      Verify.breakTransition("testDataAbstraction");
      System.out.println("new state for myClass.data = " + matchedObject.data);
      Verify.incrementCounter(0);
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 3);
    }    
  }

  
  //---------------------------------------------------------------------------
  
  @Test
  public void testMixedFields(){
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation(SERIALIZER_ARG,
                                  "+das.classes.include=*$MyClass",
                                  "+das.fields=data", 
                                  "+das.data.field=*$MyClass.data",
                                  "+das.data.abstraction=gov.nasa.jpf.test.mc.data.DynamicAbstractionTest$MyClassDataAbstraction")){
      MyClass matchedObject = new MyClass();
      matchedObject.data = Verify.getInt(0, 20);
      
      if (matchedObject.data % 4 == 0){
        System.out.println("  notAbstracted=1");
        matchedObject.notAbstracted = 1;
      }
      
      Verify.breakTransition("testDataAbstraction"); // matching point
      System.out.println("new state for myClass.data = " + matchedObject.data + ", " + matchedObject.notAbstracted);
      Verify.incrementCounter(0);
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 6);
    }        
  }

  
  //---------------------------------------------------------------------------
  
  static class SomeIgnoredClass {
    int data;  // note that it is not @FilterField annotated
  }
  
  @Test
  public void testClassFilter() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation(SERIALIZER_ARG,
                                  "+das.classes.include=*$MyClass", // only consider MyClass instance and static data
                                  "+das.methods.exclude=*",  // make sure we don't match this stackframe ('i' changes)
                                  "+vm.max_transition_length=MAX")){
      MyClass matchedObject = new MyClass();
      SomeIgnoredClass ignoredObject = new SomeIgnoredClass();
      
      matchedObject.data = Verify.getInt(0, 2); // (1) 1st CG
      System.out.print(" matchedObject.data=");
      System.out.println( matchedObject.data);
      
      for (int i=0; i<2; i++){
        ignoredObject.data = i;
        System.out.print("    ignoredObject.data=");
        System.out.println( ignoredObject.data);

        Verify.breakTransition("testDataAbstraction"); // (2) matching point for someObject
        
        // if we get here we had a new state (i.e. wasn't matched)
        // NOTE we don't get here for matchedObject.data=0 because that would match with the state before (1)
        System.out.printf("         new state for matched=%d, ignored=%d\n", matchedObject.data, ignoredObject.data);
        Verify.incrementCounter(0); // should be only reached once for matchedObject.data={1,2}
      }
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
    }
  }
  

  //---------------------------------------------------------------------------

  void matchThis() {
    for (int i=0; i<2; i++){
      System.out.printf("  matchThis() i=%d\n", i);
    
      Verify.breakTransition("testDataAbstraction"); // 'i' has changed
      System.out.println("    new state");
      Verify.incrementCounter(0);
    }
  }
  
  @Test
  public void testStackFrameFilter() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation(SERIALIZER_ARG,
                                  "+das.methods.include=*DynamicAbstractionTest.matchThis(*)V")){
      for (int i=0; i<10; i++){ // 'i' changes this frame..
        System.out.printf("loop cycle %d\n", i);
        matchThis(); // ..but not this one
      }
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
    }
  }
}
