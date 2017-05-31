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

import java.util.NoSuchElementException;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public abstract class ArrayIntSetTestBase extends TestJPF {

  protected abstract ArrayIntSet createArrayIntSet();
  protected abstract ArrayIntSet createArrayIntSet(int n);
  
  @Test
  public void testInsert(){
    ArrayIntSet s = createArrayIntSet();
    s.add(42);
    s.add(43);
    s.add(41);
    s.add(42);
    s.add(0);
    
    System.out.println(s);
    
    assertTrue(s.size() == 4);
    assertTrue(s.contains(0));
    assertTrue(s.contains(41));
    assertTrue(s.contains(42));
    assertTrue(s.contains(43));
  }
  
  @Test
  public void testRemove(){
    ArrayIntSet s = createArrayIntSet();
    s.add(42);
    assertTrue(s.size() == 1);
    assertTrue(s.contains(42));
    
    s.remove(42);
    assertFalse(s.contains(42));
    assertTrue(s.size() == 0);
    
    s.add(42);
    s.add(42000);
    s.add(0);
    assertTrue(s.size() == 3);
    s.remove(42000);
    assertTrue(s.size() == 2);
    assertFalse(s.contains(42000));
    s.remove(0);
    assertFalse(s.contains(0));
    assertTrue(s.size() == 1);
  }
  
  @Test
  public void testRemoveLast(){
    ArrayIntSet s = createArrayIntSet(2);
    s.add(1);
    s.add(2);
    
    s.remove(2);
    assertTrue( s.size() == 1);
    assertTrue( s.contains(1));
    
    s.remove(1);
    assertTrue( s.isEmpty());
  }
  
  @Test
  public void testRemoveFirst(){
    ArrayIntSet s = createArrayIntSet(2);
    s.add(1);
    s.add(2);
    
    s.remove(1);
    assertTrue( s.size() == 1);
    assertTrue( s.contains(2));
    
    s.remove(2);
    assertTrue( s.isEmpty());
  }
  
  
  @Test
  public void testIterator(){
    ArrayIntSet s = createArrayIntSet();
    s.add(1);
    s.add(2);
    s.add(3);
    
    int i=0;
    IntIterator it = s.intIterator();
    while (it.hasNext()){
      System.out.print(it.next());
      i++;
    }
    System.out.println();
    assertTrue(i == 3);
    
    assertTrue( !it.hasNext());
    try {
      it.next();
      fail("iterator failed to throw NoSuchElementException");
    } catch (NoSuchElementException nsex){
      // that's expected
    }
    
    it = s.intIterator(); // fresh one
    while (it.hasNext()){
      if (it.next() == 2){
        it.remove();
        assertTrue( s.size() == 2);
        break;
      }
    }
    i = it.next();
    assertTrue(i == 3);
    it.remove();
    assertTrue(s.size() == 1);
    assertTrue( !it.hasNext());
    
    s.add(42);
    it = s.intIterator();
    assertTrue(it.next() == 1);
    it.remove();
    assertTrue( it.next() == 42);
    it.remove();
    assertTrue( s.isEmpty());
  }
  
  @Test
  public void testComparison(){
    int[][] a = {
        {42, 0, 41},
        {42, 41, 0},
        {0, 42, 41},
        {0, 41, 42},
        {41, 0, 42},
        {41, 42, 0}
    };
    
    ArrayIntSet[] set = new ArrayIntSet[a.length];
    
    for (int i=0; i< a.length; i++) {
      set[i]= createArrayIntSet();
      int[] v = a[i];
      for (int j=0; j<v.length; j++) {
        set[i].add(v[j]);
      }
    }
    
    ArrayIntSet s1 = null, s2 = null;
    for (int i=1; i< a.length; i++) {
      s1 = set[i-1];
      s2 = set[i];
      System.out.println("comparing " + s1 + " with " + s2);
      
      assertTrue(s1.hashCode() == s2.hashCode());
      assertTrue(s1.equals(s2));
    }
        
    // inverse test
    s2.remove(41);
    assertFalse( s1.hashCode() == s2.hashCode());
    assertFalse( s1.equals(s2));
  }
}
