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

import gov.nasa.jpf.EventProducer;
import gov.nasa.jpf.util.event.EventContext;
import gov.nasa.jpf.util.event.Event;
import gov.nasa.jpf.util.event.NoEvent;
import gov.nasa.jpf.util.event.TestEventTree;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

/**
 * regression test for EventGenerator based test drivers
 */
public class EventGeneratorTest extends TestJPF {

   
  //---------------------------------------------------------------------------------------
  public static class SimpleTree extends TestEventTree {
    public SimpleTree (){
      expected = new String[] {
        "a1b",
        "axxb"
      };
    }
    
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
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation("+event.tree.class=.test.mc.data.EventGeneratorTest$SimpleTree", "+log.info=event")){
      EventProducer producer = new EventProducer();
      StringBuilder sb = new StringBuilder();
      
      while (producer.processNextEvent()){
        sb.append(producer.getEventName());
      }
      
      String trace = sb.toString();
      System.out.print("got trace: ");
      System.out.println(trace);
      
      if (!producer.checkPath()){
        fail("unexpected trace failure");        
      }
      
      if (producer.isCompletelyCovered()){
        Verify.setCounter(0, 1);
      }
    }
    
    if (!isJPFRun()){
      if (Verify.getCounter(0) != 1){
        fail("unaccounted trace failure");
      }
    }
  }
    
  //-------------------------------------------------------------------------------------
  public static class CombinationTree extends TestEventTree {
    public CombinationTree (){
      printTree();
      printPaths();
    }
    
    @Override
    public Event createRoot() {
       Event[] options = { event("A"), event("B"), event("C") };

       return anyCombination(options);
     }
  }
  
  @Test
  public void testAnyCombination (){
    if (verifyNoPropertyViolation("+event.tree.class=.test.mc.data.EventGeneratorTest$CombinationTree", "+log.info=event")){
      EventProducer producer = new EventProducer();
      StringBuilder sb = new StringBuilder();
      
      while (producer.processNextEvent()){
        sb.append(producer.getEventName());
      }
    }
  }
  
  
  //------------------------------------------------------------------------------------
  public static class ExpandTree extends TestEventTree {
    public ExpandTree (){
      printTree();
    }
    
    @Override
    public Event createRoot(){
      return
              sequence(
                event("a"),
                event("*"),
                event("<opt>"),
                event("b"));
    }    
  }

  public static class MyEventContext implements EventContext {
    @Override
    public Event map (Event e){
        String eventName = e.getName();
      
        if (eventName.equals("*")){
          System.out.println("  expanding " + eventName + " to [X,Y]");
          List<Event> list = new ArrayList<Event>();
          list.add( new Event("X"));
          list.add( new Event("Y"));
          return e.replaceWithAlternativesFrom(list);
          
        } else if (eventName.equals("<opt>")){
          System.out.println("  skipping " + eventName);
          // that's effectively event removal without changing the structure of the tree
          return e.replaceWith(new NoEvent()); 
        }

        return e;
    }
  }

  @Test
  public void testEventExpansion (){
    if (verifyNoPropertyViolation("+event.tree.class=.test.mc.data.EventGeneratorTest$ExpandTree",
                                  "+event.context.class=.test.mc.data.EventGeneratorTest$MyEventContext",
                                  "+log.info=event")){
      EventProducer producer = new EventProducer();
      StringBuilder sb = new StringBuilder();
      
      while (producer.processNextEvent()){
        String eventName = producer.getEventName();
        if (eventName != null){
          sb.append(eventName);
        }
      }
      
      String trace = sb.toString();
      System.out.print("--- got trace: ");
      System.out.println(trace);
    }
  }
  

  
}
