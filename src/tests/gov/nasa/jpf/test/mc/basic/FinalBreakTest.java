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
import gov.nasa.jpf.vm.Verify;
import java.lang.reflect.Field;
import org.junit.Test;

/**
 * test transition breaks on finals
 */
public class FinalBreakTest extends TestJPF {


  //--- breaks on instance finals
  
  static class InstanceFinal {
    static InstanceFinal global;
    
    final int a;
    final int b;
    
    InstanceFinal (){
      Verify.println("T enter InstanceFinal ctor");
      global = this; // leak this reference before construction
      
      a = 1;
      b = 1;
      Verify.println("T exit InstanceFinal ctor");
    }
  }
  
  void startInstanceFinal(){
    Thread t = new Thread(new Runnable() {
      @Override
	public void run () {
        Verify.println("T running");
        InstanceFinal o = new InstanceFinal();
        Thread.yield();
        assertTrue( "constructed object corrupted", o.a == 1 && o.b == 1);
        Verify.println("T terminating");
      }
    });
    t.start();
  }
  
  @Test
  public void testNoFinalBreak(){
   if (verifyNoPropertyViolation("+vm.shared.skip_finals=true")){
     startInstanceFinal();
     InstanceFinal o = InstanceFinal.global;
     if (o != null){
       assertTrue( "break between field inits", o.a == o.b);
     }
   }
  }
  
  @Test
  public void testFinalBreak(){
   if (verifyAssertionError("+vm.shared.skip_finals=false")){
     startInstanceFinal();
     InstanceFinal o = InstanceFinal.global;
     if (o != null){
       assertTrue( "break between field inits", o.a == o.b);
     }
   }
  }

  @Test 
  public void testNoConstructedFinalBreak(){
    if (verifyNoPropertyViolation("+vm.shared.skip_constructed_finals=true")){
      startInstanceFinal();
      InstanceFinal o = InstanceFinal.global;
      if (o != null){
        try {
          Field f = InstanceFinal.class.getField("a");
          f.setAccessible(true);
          
          // those should not break
          Verify.println("main now corrupting object");
          f.setInt(o, 42);
          Verify.println("main now fixing object");
          f.setInt(o, 1);          
          
        } catch (Throwable x){
          fail("unexpected exception: " + x);
        }
      }
    }
  }

  @Test 
  public void testConstructedFinalBreak(){
    if (verifyAssertionError("+vm.shared.skip_finals=false", "+vm.shared.skip_constructed_finals=false")){
      startInstanceFinal();
      InstanceFinal o = InstanceFinal.global;
      if (o != null){
        try {
          Field f = InstanceFinal.class.getField("a");
          f.setAccessible(true);
          
          // those should not break
          Verify.println("main now corrupting object");
          f.setInt(o, 42);
          //Thread.yield();  // not required if Field.set() properly breaks
          Verify.println("main now fixing object");
          f.setInt(o, 1);          
          
        } catch (Throwable x){
          throw new RuntimeException("caught " + x);
        }
      }
    }
  }

  
  //--- breaks on static finals
  
  final static Object o1 = new Object();
  final static Object o2 = new Object();
  
  static class StaticFinal {
    final static Object a = o1;
    final static Object b = o1;
  }

  void startStaticFinal(){
    Thread t = new Thread(new Runnable() {
      @Override
	public void run () {
        Verify.println("T running");
        Thread.yield();
        assertTrue( "static finals corrupted", StaticFinal.a == StaticFinal.b);
        Verify.println("T terminating");
      }
    });
    t.start();
  }

  @Test
  public void testNoStaticFinalBreak(){
    if (verifyNoPropertyViolation("+vm.shared.skip_static_finals=true")){
      startStaticFinal();
      try {
        Field f = StaticFinal.class.getField("a");
        f.setAccessible(true);
        
        // those should not break
        Verify.println("main now corrupting static fields");
        f.set(null, o2);
        Verify.println("main now fixing static fields");
        f.set(null, o1);
        
      } catch (Throwable x){
          fail("unexpected exception: " + x);
      }
    }
  }

  
  @Test
  public void testStaticFinalBreak(){
    if (verifyAssertionError("+vm.shared.skip_static_finals=false")){
      startStaticFinal();
      try {
        Field f = StaticFinal.class.getField("a");
        f.setAccessible(true);
        
        // those should  break
        Verify.println("main now corrupting static fields");
        f.set(null, o2);
        Verify.println("main now fixing static fields");
        f.set(null, o1);          
        
      } catch (Throwable x){
          x.printStackTrace();
          throw new RuntimeException("caught " + x);
      }
    }
  }
  
}
