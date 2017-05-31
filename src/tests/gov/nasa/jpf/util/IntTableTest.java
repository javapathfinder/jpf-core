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
 * unit test for IntTable
 */
public class IntTableTest extends TestJPF {

  @Test
  public void testBasicPut(){
    IntTable<Integer> tbl = new IntTable<Integer>();

    assert tbl.size() == 0;
    final int N = 5000;

    for (int i=0; i<N; i++){
      tbl.put(i, i);

      IntTable.Entry<Integer> e = tbl.get(i);
      assert e.val == i;
    }

    assert tbl.size() == N;
  }

  
  @Test
  public void testStringKeyAdd(){
    IntTable<String> tbl = new IntTable<String>();

    assert tbl.size() == 0;
    final int N = 5000;

    for (int i=0; i<N; i++){
      String key = "averylonginttablekey-" + i;
      tbl.add(key, i);

      IntTable.Entry<String> e = tbl.get(key);
      assert e.val == i;
    }

    assert tbl.size() == N;
  }

  @Test
  public void testClone(){
    IntTable<String> tbl = new IntTable<String>(3); // make it small so that we rehash

    tbl.add("1", 1);
    tbl.add("2", 2);
    tbl.add("3", 3);

    IntTable<String> t1 = tbl.clone();

    for (int i=10; i<20; i++){
      t1.add(Integer.toString(i), i);
    }

    assert tbl.size() == 3;
    System.out.println("-- original table");
    for (IntTable.Entry<String> e : tbl){
      assert Integer.parseInt(e.key) == e.val;
      System.out.println(e);
    }

    assert t1.size() == 13;
    System.out.println("-- cloned+modified table");
    for (IntTable.Entry<String> e : t1){
      assert Integer.parseInt(e.key) == e.val;
      System.out.println(e);
    }
  }
  
  @Test
  public void testSnapshot (){
    IntTable<String> tbl = new IntTable<String>();
    
    tbl.add("1", 1);
    tbl.add("2", 2);
    tbl.add("3", 3);
    tbl.add("12345", 12345);
    tbl.dump();
    
    IntTable.Snapshot<String> snap = tbl.getSnapshot();
    
    tbl.remove("3");
    tbl.remove("1");
    tbl.add("42", 42);
    tbl.dump();
    
    assertTrue(tbl.size() == 3);
    
    tbl.restore(snap);
    tbl.dump();
    
    assertTrue(tbl.size() == 4);
    assertTrue(tbl.hasEntry("1"));
    assertTrue(tbl.hasEntry("2"));
    assertTrue(tbl.hasEntry("3"));
    assertTrue(tbl.hasEntry("12345"));
  }
}
