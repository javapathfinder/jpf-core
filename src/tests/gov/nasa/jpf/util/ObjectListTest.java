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
 * regression unit test for ObjectList API
 */
public class ObjectListTest extends TestJPF {
  
  Object attr;
  
  @Test
  public void testAdd(){
    assertTrue( ObjectList.isEmpty(attr));
    
    attr = ObjectList.add(attr, "one");
    assertTrue( ObjectList.size(attr) == 1);
    assertTrue( attr != null && attr.equals("one"));
    
    attr = ObjectList.add(attr, new Integer(2));
    assertTrue( ObjectList.size(attr) == 2);
    assertTrue( attr != null && !(attr instanceof Integer));
  }
  
  
  @Test
  public void testListIteration() {
    Object[] v = { new Integer(2), "one" };
    
    for (Object a: ObjectList.iterator(attr)){
      fail("list should be empty");
    }
    
    for (int i=v.length-1; i>=0; i--){
      attr = ObjectList.add(attr, v[i]);
    }
    
    int len = ObjectList.size(attr);
    for (int i=0; i<len; i++){
      Object a = ObjectList.get(attr, i);
      System.out.printf("[%d] = %s\n", i, a.toString());
      assertEquals( a, v[i]);
    }

    attr = ObjectList.add(attr, "three");
    attr = ObjectList.add(attr, new Integer(4));
    
    int i=0;
    for (Integer a = ObjectList.getFirst(attr, Integer.class); a!=null; a = ObjectList.getNext(attr, Integer.class, a)){
      System.out.println(a);
      i++;
    }
    assertTrue( i == 2);

    System.out.println("-- untyped iterator");
    i=0;
    for (Object a: ObjectList.iterator(attr)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }
    assertTrue( i == 4);
    
    System.out.println("-- typed iterator (all Strings)");
    i=0;
    for (String a: ObjectList.typedIterator(attr, String.class)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }
    assertTrue(i == 2);
    
    System.out.println("-- typed iterator (all Floats) => none");
    i=0;
    for (float f: ObjectList.typedIterator(attr, Float.class)){
      System.out.printf("[%d] = %f\n", i++, f);
    }
    assertTrue(i == 0);
    
  }
  
  @Test
  public void testSingleValueIteration(){
    attr = ObjectList.add(attr, "one");
    
    System.out.println("-- untyped iterator");
    int i=0;
    for (Object a: ObjectList.iterator(attr)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }
    assertTrue( i == 1);
    
    System.out.println("-- typed iterator (all Strings)");
    i=0;
    for (String a: ObjectList.typedIterator(attr, String.class)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }
    assertTrue(i == 1);
    
    System.out.println("-- typed iterator (all Floats) => none");
    i=0;
    for (float f: ObjectList.typedIterator(attr, Float.class)){
      System.out.printf("[%d] = %f\n", i++, f);
    }
    assertTrue(i == 0);
  }
  
  
  @Test
  public void testRemove(){
    attr = ObjectList.add(attr, "one");
    
    attr = ObjectList.remove(attr, attr);
    assertTrue(attr == null);
    
    String one = "one";
    String two = "two";
    attr = ObjectList.add(attr, one);
    attr = ObjectList.add(attr, two);
    attr = ObjectList.remove( attr, one);
    assertTrue(attr != null && attr == two);
    
    attr = one;
    attr = ObjectList.add(attr, two);
    attr = ObjectList.remove( attr, two);
    assertTrue(attr != null && attr == one);
   
    
    attr = one;
    attr = ObjectList.add(attr, two);
    attr = ObjectList.add(attr, "three");
    attr = ObjectList.remove( attr, two);
    int i=0;
    for (Object a: ObjectList.iterator(attr)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }    
    assertTrue( ObjectList.size(attr) == 2);
  }
  
  @Test
  public void testReplace(){
    String one = "one";
    attr = ObjectList.add(attr, one);
    
    Integer i1 = new Integer(1);
    attr = ObjectList.replace(attr, one, i1);
    assertTrue(attr == i1);
    
    String two = "two";
    Integer i2 = new Integer(2);
    attr = ObjectList.add(attr, two);
    attr = ObjectList.replace(attr, two, i2);
    Integer o = ObjectList.getFirst(attr, Integer.class);
    assertTrue( o == i2);
    
    int i=0;
    for (Object a: ObjectList.iterator(attr)){
      System.out.printf("[%d] = %s\n", i++, a.toString());
    }    
    assertTrue( ObjectList.size(attr) == 2);
  }
  
  
  Object a1, a2;
  
  @Test
  public void testEquals(){
    assertTrue( ObjectList.equals(a1, a2));
    
    a1 = null;
    a2 = "one";
    assertFalse( ObjectList.equals(a1, a2));
    
    a1 = "one";
    a2 = null;
    assertFalse( ObjectList.equals(a1, a2));

    a1 = ObjectList.createList("one", "two");
    a2 = null;
    assertFalse( ObjectList.equals(a1, a2));

    a1 = "one";
    a2 = "one";
    assertTrue( ObjectList.equals(a1, a2));
    
    a1 = ObjectList.createList("one", "two");
    a2 = "one";
    assertFalse( ObjectList.equals(a1, a2));

    a1 = "one";
    a2 = ObjectList.createList("one", "two");
    assertFalse( ObjectList.equals(a1, a2));
    
    a1 = ObjectList.createList("one", "two");
    a2 = ObjectList.createList("one", "two");
    assertTrue( ObjectList.equals(a1, a2));
    
    a1 = ObjectList.createList("one", "two", "three");
    a2 = ObjectList.createList("one", "two");
    assertFalse( ObjectList.equals(a1, a2));

    a1 = ObjectList.createList("one", "two");
    a2 = ObjectList.createList("one", "two", "three");
    assertFalse( ObjectList.equals(a1, a2));
  }
  
  
  static class A implements Cloneable {
    String id;
    
    A (String id){
      this.id = id;
    }
    
    @Override
	public int hashCode(){
      return id.hashCode();
    }
    
    @Override
	public boolean equals (Object o){
      if (o instanceof A){
        return id.equals(((A)o).id);
      } else {
        return false;
      }
    }
    
    @Override
	public Object clone(){
      try {
        A a = (A)super.clone();
        a.id += "_clone";
        return a;
        
      } catch (CloneNotSupportedException ex) {
        return null;
      }
    }
    
    @Override
	public String toString(){
      return id;
    }
  }
  
  static class B extends A implements CloneableObject {
    B (String id){
      super(id);
    }
  }
  
  @Test
  public void testClone(){
    Object l1, l1c, l2;
    
    // lists with generic data clone()
    l1 = ObjectList.createList(new A("one"), new A("two"));
    l2 = ObjectList.createList(new A("one_clone"), new A("two_clone"));    
    try {
      l1c = ObjectList.clone(l1);
      assertTrue( ObjectList.equals(l1c, l2));
    } catch (CloneNotSupportedException cnsx){
      fail("A.clone() did throw CloneNotSupportedException");
    }
    
    // clone() attempt of non-Cloneables
    l1 = "one";
    l2 = "one_clone";
    try {
      l1c = ObjectList.clone(l1);
      fail("Strings are not cloneable");
    } catch (CloneNotSupportedException cnsx){
      // this is Ok
    }

    // single object clone
    l1 = new A("one");
    try {
      l1c = ObjectList.clone(l1);
      assertTrue( l1.equals( new A("one")));
    } catch (CloneNotSupportedException cnsx){
      fail("object clone failed");
    }

    // null clone
    l1 = null;
    try {
      l1c = ObjectList.clone(l1);
      assertTrue( l1c == null);
    } catch (CloneNotSupportedException cnsx){
      fail("empty list clone failed");
    }
    
    // single object clone of CloneableObject instance
    l1 = new B("one");
    try {
      l1c = ObjectList.clone(l1);
      assertTrue( l1.equals( new B("one")));
    } catch (CloneNotSupportedException cnsx){
      fail("object clone failed");
    }
    
    // list clone with CloneableObject instances
    l1 = ObjectList.createList(new B("one"), new B("two"));
    l2 = ObjectList.createList(new A("one_clone"), new A("two_clone"));
    try {
      l1c = ObjectList.clone(l1);
      assertTrue( ObjectList.equals(l1c, l2));
    } catch (CloneNotSupportedException cnsx){
      fail("B.clone() did throw CloneNotSupportedException");
    }
  }
    
  @Test
  public void testHash() {
    // test single object and list hash
    // test if list nodes are hash transparent
    
    // test equals
    Object l = null;
    HashData hd = new HashData();
    ObjectList.hash(l, hd);
    HashData hd1 = new HashData();
    assertTrue( hd.getValue() == hd1.getValue());
        
    l = "one";
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("one");    
    assertTrue( hd.getValue() == hd1.getValue());
    
    l = ObjectList.createList(new A("one"), new A("two"));
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("one");
    hd1.add("two");
    assertTrue( hd.getValue() == hd1.getValue());

    
    // test non-equals
    l = null;
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("one");
    assertTrue( hd.getValue() != hd1.getValue());

    l = "one";
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    assertTrue( hd.getValue() != hd1.getValue());
    
    l = "one";
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("two");    
    assertTrue( hd.getValue() != hd1.getValue());
    
    l = ObjectList.createList(new A("one"), new A("two"));
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("one");
    assertTrue( hd.getValue() != hd1.getValue());
    
    l = ObjectList.createList(new A("one"), new A("two"));
    hd = new HashData();
    ObjectList.hash(l, hd);
    hd1 = new HashData();
    hd1.add("two");
    hd1.add("one");
    assertTrue( hd.getValue() != hd1.getValue());

  }
}
