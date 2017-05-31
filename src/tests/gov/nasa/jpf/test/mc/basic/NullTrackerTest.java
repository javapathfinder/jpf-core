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

package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.util.test.TestJPF;
import java.util.HashMap;
import org.junit.Test;

/**
 * regression test for NullTracker.
 * 
 * Well, not really a regression test since NullTracker only prints out reports, but at least
 * we can see if it has errors while running
 */
public class NullTrackerTest extends TestJPF {
  
  static class TestObject {
    String d;
  
    TestObject(){
      // nothing, we forget to init d;
    }
    
    TestObject (String d){
      this.d = d;
    }
    
    int getDLength(){
      return d.length();
    }
    
    void foo(){
      // nothing
    }
  }

  TestObject o;
  
  TestObject getTestObject (){
    return null;
  }
  
  void accessReturnedObject (){
    TestObject o = getTestObject();
    System.out.println("now accessing testObject");
    String d = o.d; // that will NPE
  }
  
  void accessObject (TestObject o){
    System.out.println("now accessing testObject");
    String d = o.d; // that will NPE    
  }
  
  void createAndAccessObject(){
    TestObject o = getTestObject();
    accessObject(o);
  }
  
  
  @Test
  public void testGetAfterIntraMethodReturn (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      accessReturnedObject();
    }
  }
  
  @Test
  public void testGetAfterInterMethodReturn (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      createAndAccessObject();
    }
  }

  @Test
  public void testGetAfterIntraPut (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      o = null; // the null source
      
      String d = o.d; // causes the NPE
    }    
  }
  
  @Test
  public void testCallAfterIntraPut (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      o = null; // the null source
      
      o.foo(); // causes the NPE
    }    
  }

  @Test
  public void testGetAfterASTORE (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      TestObject myObj = null; // the null source
      
      myObj.foo(); // causes the NPE
    }    
  }

  
  HashMap<String,TestObject> map = new HashMap<String,TestObject>();
  
  TestObject lookupTestObject (String name){
    return map.get(name);
  }
  
  @Test
  public void testHashMapGet (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      TestObject o = lookupTestObject("FooBar");
      o.foo();
    }
  }
  
  //------------------------------------------------------------------
    
  TestObject createTestObject (){
    return new TestObject();
  }
  
  
  TestObject createTestObject (String d){
    return new TestObject(d);
  }
  
  @Test
  public void testMissingCtorInit (){
    if (verifyUnhandledException("java.lang.NullPointerException", "+listener=.listener.NullTracker")){
      TestObject o = createTestObject("blah");
      int len = o.getDLength(); // that should be fine
      
      o = createTestObject();
      len = o.getDLength(); // that should NPE and report the default ctor as culprit
    }    
  }
}
