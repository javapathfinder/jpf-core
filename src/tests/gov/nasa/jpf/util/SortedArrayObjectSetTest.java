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
import org.junit.Test;


/**
 * regression test for SortedArrayObjectSet
 */
public class SortedArrayObjectSetTest extends TestJPF {
  
  static class X implements Comparable<X> {
    String id;
    int x;
    
    X (String id, int x){
      this.id = id;
      this.x = x;
    }
    
    @Override
	public int compareTo (X other){
      return (x - other.x);
    }
    
    @Override
	public String toString(){
      return id;
    }
    
    @Override
	public boolean equals(Object o){
      if (o instanceof X){
        X other = (X)o;
        if (x == other.x){
          if (id.equals(other.id)){
            return true;
          }
        }
      }
      
      return false;
    }
  }
  
  @Test
  public void testBasic(){
    SortedArrayObjectSet<X> s = new SortedArrayObjectSet<X>();
    
    X o1 = new X("1",1);
    X o2 = new X("20",20);
    X o3 = new X("5",5);
    X o4 = new X("7",7);
    
    s.add(o1);
    System.out.println(s);
    s.add(o2);
    System.out.println(s);
    s.add(o3);
    System.out.println(s);
    s.add(o4);
    System.out.println(s);
    s.add(o1);
    System.out.println(s);

    assertTrue(s.size() == 4);
    assertTrue(s.contains(o1));
    assertTrue(s.contains(o2));
    assertTrue(s.contains(o3));
    assertTrue(s.contains(o4));
    
    X o3a = new X("5a", 5);
    s.add(o3a);
    System.out.println(s);
    assertTrue(s.size() == 5);
    assertTrue(s.contains(o3a));
    
    s.remove(o3a);
    System.out.println(s);
    assertTrue(s.size() == 4);
    assertFalse(s.contains(o3a));
    assertTrue(s.contains(o3));
  }
}
