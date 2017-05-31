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

package gov.nasa.jpf.util.event;

import gov.nasa.jpf.util.event.EventTree;
import gov.nasa.jpf.util.event.Event;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * regression test for EventTree
 */
public class EventTreeTest extends TestJPF {
  
  protected boolean checkGeneratedPaths (EventTree m, String[] expected){
    System.out.println("event tree: ");
    m.printTree();
    
    int nMatches = 0;
    for (Event ee : m.visibleEndEvents()){
      String trace = ee.getPathString(null);
      System.out.print("checking path: \"" + trace + '"');
      
      if (!m.checkPath(ee, expected)){
        System.out.println("   UNEXPECTED");
        return false;
      } else {
        System.out.println("   OK");
      }
      
      nMatches++;
    }
    
    if (nMatches != expected.length){
      System.out.println("UNCOVERED PATH: ");
      for (int i=0; i<expected.length; i++){
        if (expected[i] != null){
          System.err.println(expected[i]);
        }
      }
      return false;
    }
    
    return true;
  }
  
  
  //--------------------------------------------------------------------
    
  static class SimpleTree extends EventTree {    
    @Override
    public Event createRoot() {
      return 
        sequence(
          event("a"),
          alternatives(
            event("1"),
            iteration(2,
              event("x")
            )
          ),
          event("b")
        );
    }
  }

  @Test
  public void testSimpleTree(){
    SimpleTree m = new SimpleTree();
    
    String[] expected = {
        "a1b",
        "axxb"     
    };
    
    if (!checkGeneratedPaths(m, expected)){
      fail("failed to match traces");
    }
  }
  
  //--------------------------------------------------------------------
  public static class CombinationTree extends EventTree {    
    @Override
    public Event createRoot() {
      return anyCombination(
               event("a"),
               event("b"),
               event("c"),
               event("d")
             );
    }    
  }
  
  @Test
  public void testCombinationTree(){
    CombinationTree t = new CombinationTree();
    //t.printPaths();

    String[] expected = {
        "",
        "a",
        "b",
        "ab",
        "c",
        "ac",
        "bc",
        "abc",
        "d",
        "ad",
        "bd",
        "abd",
        "cd",
        "acd",
        "bcd",
        "abcd"
    };
    
    if (!checkGeneratedPaths(t, expected)){
      fail("failed to match traces");
    }
  }  

  static class SimpleCombinationTree extends EventTree {
    @Override
    public Event createRoot() {
      return anyCombination(
               event("a"),
               event("b")
             );
    }
  }

  //@Test
  public void testSimpleCombinationTree(){
    SimpleCombinationTree t = new SimpleCombinationTree();
    System.out.println("--- tree:");
    t.printTree();
    System.out.println("--- paths:");
    t.printPaths();
  }

  //--------------------------------------------------------------------
 
  static class EmbeddedCombinationTree extends EventTree {
    @Override
    public Event createRoot() {
      return sequence(
                event("1"),
                anyCombination(
                   event("a"),
                   event("b")),
                event("2"));
    }
  }

  //@Test
  public void testEmbeddedCombinationTree(){
    EventTree t = new EmbeddedCombinationTree();
    System.out.println("--- tree:");
    t.printTree();
    System.out.println("--- paths:");
    t.printPaths();
  }
    
  
  //--------------------------------------------------------------------
  static class DT extends EventTree {    
    @Override
    public Event createRoot() {
      return sequence(
              event("a"),
              alternatives(
                  event("1"),
                  sequence(
                      event("X"),
                      event("Y")
                  ),
                  iteration(3,
                      event("r")
                  )
              ),
              event("b"));
    }    
  }

  
  @Test
  public void testMaxDepth(){
    DT t = new DT();
    t.printTree();
    t.printPaths();
    
    int maxDepth = t.getMaxDepth();
    System.out.println("max depth: " + maxDepth);
    
    assertTrue( maxDepth == 5);
  }

  //--------------------------------------------------------------------
  static class PermutationTree extends EventTree {
    @Override
    public Event createRoot(){
      return anyPermutation(
               event("a"),
               event("b"),
               event("c")
              );
    }
  }

  @Test
  public void testPermutationTree(){
    PermutationTree t = new PermutationTree();
    //t.printPaths();
    
    String[] expected = {
        "abc",
        "acb",
        "bac",
        "bca",
        "cab",
        "cba"
      };
    
    if (!checkGeneratedPaths(t, expected)){
      fail("failed to match traces");
    }
  }
  
  //--------------------------------------------------------------------
  static class AddPathTree extends EventTree {        
    @Override
    public Event createRoot(){
      return sequence(
               event("a"),
               event("b"),
               event("c")
              );
    } 
  }
  
  @Test
  public void testAddPath () {
    AddPathTree t = new AddPathTree();
    t.addPath(
            new Event("a"), 
            new Event("b"), 
            new Event("3"));

    String[] expected = { "abc", "ab3" };
    
    if (!checkGeneratedPaths(t, expected)){
      fail("failed to match traces");
    }
  }
    
  //-------------------------------------------------------------------
  static class MT1 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("a"),
               event("b"),
               event("c")
              );
    }
  }
  
  static class MT2 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("1"),
               event("2"),
               event("3")
              );
    }
  }

  static class MT3 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("X"),
               event("Y")
              );
    }
  }

  
  @Test
  public void testMerge (){
    MT1 t1 = new MT1();
    MT2 t2 = new MT2();
    //MT3 t3 = new MT3();
    
    EventTree t = t1.interleave( t2);
    // t.printPaths();
    
    String[] expected = {
      "a123bc",
      "a12b3c",
      "a12bc3",
      "a1b23c",
      "a1b2c3",
      "a1bc23",
      "ab123c",
      "ab12c3",
      "ab1c23",
      "abc123",
      "123abc",
      "12a3bc",
      "12ab3c",
      "12abc3",
      "1a23bc",
      "1a2b3c",
      "1a2bc3",
      "1ab23c",
      "1ab2c3",
      "1abc23"
    };
    
    if (!checkGeneratedPaths(t, expected)){
      fail("failed to match traces");
    }
  }
  
  //-------------------------------------------------------------------
  static class SMT1 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("a"),
               event("b")
              );
    }
  }
  
  static class SMT2 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("1"),
               event("2")
              );
    }
  }

  //@Test
  public void testSimpleMerge (){
    SMT1 t1 = new SMT1();
    SMT2 t2 = new SMT2();
    //MT3 t3 = new MT3();
    
    EventTree t = t1.interleave( t2);
    System.out.println("--- merged tree:");
    t.printTree();
    System.out.println("--- merged paths:");
    t.printPaths();
  }
    

  //-------------------------------------------------------------------
  static class RT1 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("a"),
               event("b")
              );
    }
  }
  
  static class RT2 extends EventTree {
    @Override
    public Event createRoot(){
      return sequence(
               event("1"),
               event("2")
              );
    }
  }

  
  @Test
  public void testRemove (){
    RT1 t1 = new RT1();
    RT2 t2 = new RT2();
    
    EventTree t = t1.interleave( t2);
    System.out.println("merged tree: ");
    //t.printTree();
    t.printPaths();
    
    t = new EventTree( t.removeSource(t2));
    System.out.println("reduced tree: ");
    //t.printTree();
    //t.printPaths();
    
    String[] expected = { "ab" };
    if (!checkGeneratedPaths(t, expected)){
      fail("failed to match traces");
    }    
  }
}
