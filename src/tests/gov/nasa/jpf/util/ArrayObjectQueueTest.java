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

import gov.nasa.jpf.util.test.TestJPF;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

/**
 *
 */
public class ArrayObjectQueueTest extends TestJPF {
  
  <E> void printLogicalOrder (ArrayObjectQueue<E> q){
    int n = 0;
    System.out.print('{');
    for (E e : q){
      if (n > 0){
        System.out.print(',');
      }
      System.out.print(e);
      n++;
    }
    System.out.println('}');
  }

  <E> void printPhysicalOrder (ArrayObjectQueue<E> q){
    int n = 0;
    System.out.print('{');
    for (Iterator<E> it = q.storageIterator(); it.hasNext(); ){
      E e = it.next();
      if (n > 0){
        System.out.print(',');
      }
      System.out.print(e);
      n++;
    }
    System.out.println('}');
  }

  
  @Test
  public void testBasic(){
    ArrayObjectQueue<String> q = new ArrayObjectQueue<String>(4);
    
    assertTrue( q.isEmpty());
    assertTrue( q.size() == 0);
    
    q.add("1");
    assertFalse( q.isEmpty());
    assertTrue( q.size() == 1);
    
    printLogicalOrder(q);
    
    q.add("2");
    q.add("3");
    q.add("4");
    
    printLogicalOrder(q);
        
    // now we need to grow
    q.add("5");
    
    printLogicalOrder(q);
    
    assertTrue( "1".equals( q.remove()));
    assertTrue(q.size() == 4);
    printLogicalOrder(q);
    
    assertTrue( "2".equals( q.remove()));
    assertTrue( "3".equals( q.remove()));
    assertTrue( "4".equals( q.remove()));
    assertTrue( "5".equals( q.remove()));
    
    printLogicalOrder(q);
    assertTrue(q.isEmpty());
    
    try {
      q.remove();
      fail("should never get here");
    } catch (NoSuchElementException x){
      // good exception
    }
  }
  
  @Test
  public void testTailChasing(){
    ArrayObjectQueue<String> q = new ArrayObjectQueue<String>(4);
    q.add("1");
    q.add("2");
    q.add("3");
    q.add("4");
    
    q.remove(); // should remove "1"
    q.add("5"); // should make use of the free space
    
    printLogicalOrder(q);
    assertTrue( q.getCurrentCapacity() == 4);
    
    q.remove(); // should remove "2"
    q.remove(); // should remove "3"
    q.add("6");
    
    printLogicalOrder(q);
    printPhysicalOrder(q);
    
    String s = q.remove();
    assertTrue( "4".equals(s));
    // next remove should wrap around
    assertTrue( "5".equals(q.remove()));
    
    printLogicalOrder(q);
    printPhysicalOrder(q);
  }

  
  @Test
  public void testMidGrow(){
    ArrayObjectQueue<String> q = new ArrayObjectQueue<String>(4);
    q.add("1");
    q.add("2");
    q.add("3");
    q.add("4");
  
    q.remove(); // 1
    q.add("5"); // that shouldn't grow yet
    int len = q.getCurrentCapacity();
    assertTrue( len == 4);
    printPhysicalOrder(q);
    
    q.add("6"); // that should grow
    assertTrue(q.getCurrentCapacity() > len);
    
    printLogicalOrder(q);
    printPhysicalOrder(q);    
  }
  
  //--- queue processing
  
  static class E {
    String name;
    E left, right;
    boolean visited;
    
    E (String name){ this.name = name; }
  }

  static class EProcessor implements Processor<E> {
    ArrayObjectQueue queue;
    int processed = 0;
    
    EProcessor (ArrayObjectQueue<E> queue){
      this.queue = queue;
    }
    
    @Override
	public void process(E e){
      e.visited = true;
      processed++;
      System.out.println("processed: " + e.name);

      E ref = e.left;
      if (ref != null && !ref.visited) {
        ref.visited = true;
        queue.add(ref);
      }

      ref = e.right;
      if (ref != null && !ref.visited) {
        ref.visited = true;
        queue.add(ref);
      }
    }
  }
  
  @Test
  public void testProcessing(){
    E a = new E("a");
    E b = new E("b");
    E c = new E("c");
    E d = new E("d");
    E e = new E("e");
    E f = new E("f");
    E g = new E("g");
    
    a.left  = b;
    b.right = c;
    c.left  = d;
    d.left  = a;  d.right = e;
    e.left  = f;  e.right = g;
    g.left  = a;
    
    ArrayObjectQueue<E> q = new ArrayObjectQueue<E>();
    q.add(a);
    q.add(g);
    
    EProcessor proc = new EProcessor(q);
    q.process(proc);
    
    assertTrue(a.visited);
    assertTrue(b.visited);
    assertTrue(c.visited);
    assertTrue(d.visited);
    assertTrue(e.visited);
    assertTrue(f.visited);
    assertTrue(g.visited);
    
    assertTrue( proc.processed == 7);
  }
}
