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


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class Misc {
  public static int hashCode(Object o) {
    return o == null ? 0 : o.hashCode();
  }

  public static boolean equals(Object a, Object b) {
    if (a == b) {
      return true;
    } else if (a == null || b == null) {
      // only one could be null
      return false;
    } else {
      return a.equals(b);
    }
  }

  @SuppressWarnings("unchecked")
  public static <E> Iterator<E> emptyIterator() {
    return (Iterator<E>) emptyIterator;
  }

  @SuppressWarnings("unchecked")
  public static <E> Iterable<E> emptyIterable() {
    return (Iterable<E>) emptyIterable;
  }

  public static <E> Iterable<E> iterableFromIterator(Iterator<E> iter) {
    return new Iteratorable<E>(iter);
  }

  public static final Object[] emptyObjectArray = new Object[] {};

  public static final Iterator<?> emptyIterator = new Iterator<Object>() {
    @Override
	public boolean hasNext () { return false; }
    @Override
	public Object next () { throw new NoSuchElementException(); }
    @Override
	public void remove () { throw new NoSuchElementException(); }
  };

  public static final Iterable<?> emptyIterable = new Iterable<Object>() {
    @Override
	@SuppressWarnings("unchecked")
    public Iterator<Object> iterator () {
      return (Iterator<Object>) emptyIterator;
    }
  };

  @SuppressWarnings("unchecked")
  public static <B, E extends B> Iterable<B> asBaseIterable (Collection<E> col){
    Collection<B> base = (Collection)col;
    return base;
  }

  @SuppressWarnings("unchecked")
  public static <B, E extends B> Iterator<B> getBaseIterator (Collection<E> col){
    Collection<B> base = (Collection)col;
    return base.iterator();
  }
  
  public static <E> void addAll(Collection<E> target, Iterable<? extends E> src) {
    for (E e : src) target.add(e);
  }

  public static <T> int indexOf (T[] array, Object elem){
    for (int i=0; i<array.length; i++){
      if (array[i].equals(elem)){
        return i;
      }
    }
    
    return -1;
  } 

  public static <T> boolean contains (T[] array, Object elem){
    for (int i=0; i<array.length; i++){
      if (array[i].equals(elem)){
        return true;
      }
    }
    
    return false;
  } 
  
  @SuppressWarnings("unchecked")
  public static <T> T[] stripNullElements(T[] oldArray){
    int count = 0;
    for (int i=0; i<oldArray.length; i++){
      if (oldArray[i] != null){
        count++;
      }
    }
    
    if (count == oldArray.length){ // nothing to strip
      return oldArray;
      
    } else {
      Class<?> ct = oldArray.getClass().getComponentType();
      T[] newArray = (T[]) Array.newInstance(ct, count);

      int j=0;
      for (int i=0; i<oldArray.length; i++){
        T e = oldArray[i];
        if (e != null){
          newArray[j++] = e;
          if (j == count){
            break;
          }
        }
      }
      
      return newArray;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T[] getAddedElements (T[] oldArray, T[] newArray) {
    
    if (newArray == null || newArray.length == 0) {
      return oldArray; 
    }
    if (oldArray == null || oldArray.length == 0) {
      return newArray;
    }
    
    T[] na = newArray.clone();
    int n = na.length;
    
    for (int i=0; i<na.length; i++) {
      Object eNew = na[i];
      if (eNew != null) {
        for (int j=0; j<oldArray.length; j++) {
          if (eNew.equals(oldArray[j])) {
            na[i] = null;
            n--;
            break;
          }
        }
      } else {
        n--;
      }
    }
    
    Class<?> ct = newArray.getClass().getComponentType();
    T[] addedElements = (T[]) Array.newInstance(ct, n);
    
    for (int i=0, j=0; i<na.length; i++) {
      if (na[i] != null) {
        addedElements[j++] = na[i];
      }
    }
    
    return addedElements;
  }
  
  public static <T> T[] getRemovedElements (T[] oldArray, T[] newArray) {

    if (newArray == null || newArray.length == 0 || oldArray == null || oldArray.length == 0) {
      return null;
    }
    
    T[] oa = oldArray.clone();
    int n = oa.length;
    
    for (int i=0; i<oa.length; i++) {
      Object eOld = oa[i];
      if (eOld != null) {
        for (int j=0; j<newArray.length; j++) {
          if (eOld.equals(newArray[j])) {
            oa[i] = null;
            n--;
            break;
          }
        }
      } else {
        n--;
      }
    }
    
    Class<?> ct = oldArray.getClass().getComponentType();
    T[] addedElements = (T[]) Array.newInstance(ct, n);
    
    for (int i=0, j=0; i<oa.length; i++) {
      if (oa[i] != null) {
        addedElements[j++] = oa[i];
      }
    }
    
    return addedElements;
  }
  
  
  public static <K,V> ArrayList<String> getSortedKeyStrings (HashMap<K,V> map){
    ArrayList<String> list = new ArrayList<String>();

    nextKey:
    for (K key : map.keySet()){
      String ks = key.toString();

      for (int i=0; i<list.size(); i++){
        String k = list.get(i);
        if (ks.compareTo(k) > 0){
          list.add(i, ks);
          continue nextKey;
        }
      }

      list.add(ks);
    }

    return list;
  }

  public static <K,V> ArrayList<Map.Entry<K,V>> createSortedEntryList (HashMap<K,V> map,
                                                                       Comparator<Map.Entry<K,V>> comparer) {
    ArrayList<Map.Entry<K,V>> list = new ArrayList<Map.Entry<K,V>>();

    nextEntry:
      for (Map.Entry<K,V> e : map.entrySet()){

        for (int i=0; i<list.size(); i++){
          if (comparer.compare(e,list.get(i)) > 0) {
            list.add(i, e);
            continue nextEntry;
          }
        }
        list.add(e);
      }


    return list;
  }

  public static <K,V,E> ArrayList<E> createSortedList (HashMap<K,V> map,
                                                    TwoTypeComparator<Map.Entry<K,V>,E> comparer,
                                                    ElementCreator<Map.Entry<K,V>,E> creator) {
    ArrayList<E> list = new ArrayList<E>();

    nextEntry:
      for (Map.Entry<K,V> e : map.entrySet()){

        for (int i=0; i<list.size(); i++){
          if (comparer.compare(e,list.get(i)) > 0) {
            list.add(i, creator.create(e));
            continue nextEntry;
          }
        }
        list.add(creator.create(e));
      }


    return list;
  }

  public static int compare (Integer i1, Integer i2) {
    return Integer.signum(i1.intValue() - i2.intValue());
  }

  public static <E,T> HashMap<E,Integer> createOccurrenceMap (Collection<T> collection,
                                                            ElementCreator<T,E> creator) {
    HashMap<E,Integer> map = new HashMap<E,Integer>();

    for (T o : collection) {
      E e = creator.create(o);
      Integer nE = map.get(e);
      if (nE == null){
        map.put(e,1);
      } else {
        map.put(e,nE.intValue()+1);
      }
    }

    return map;
  }

  public static <T> T createObject (Class<T> cls, Class<?>[] argTypes, Object[] args){
    if (argTypes.length != args.length){
      return null;
    }

    while (argTypes.length >= 0){
      try {
        Constructor<T> ctor = cls.getConstructor(argTypes);
        return ctor.newInstance(args);

      } catch (NoSuchMethodException nsmx){
        Class<?>[] at = new Class<?>[argTypes.length-1];
        System.arraycopy(argTypes, 1, at,0, at.length);
        argTypes = at;

        Object[] a = new Object[at.length];
        System.arraycopy(args,1, a,0, a.length);
        args = a;

      } catch (Throwable t){
        return null;
      }
    }

    return null;
  }

  public static String toString (Object[] collection) {
    StringBuilder sb = new StringBuilder();
    
    for (Object e : collection) {
      sb.append(e);
    }

    return sb.toString();
  }
  
  public static String toString (String[] collection) {
    StringBuilder sb = new StringBuilder();
    
    for (Object e : collection) {
      sb.append(e);
    }

    return sb.toString();
  }
  
  public static String toString (Iterable<?> collection) {
    StringBuilder sb = new StringBuilder();
    
    for (Object e : collection) {
      sb.append(e);
    }

    return sb.toString();
  }
  
  public static String toString( Iterable<?> collection,
                                 String prefix, String separator, String postfix) {
    StringBuilder sb = new StringBuilder();

    if (prefix != null) {
      sb.append(prefix);
    }

    int i=0;
    for (Object e : collection) {
      if (i++ > 0) {
        sb.append(separator);
      }
      sb.append(e);
    }

    if (postfix != null) {
      sb.append(postfix);
    }

    return sb.toString();
  }

  public static String toString( Object[] array,
                                 String prefix, String separator, String postfix) {
    StringBuilder sb = new StringBuilder();

    if (prefix != null) {
      sb.append(prefix);
    }

    for (int i=0; i<array.length; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(array[i]);
    }

    if (postfix != null) {
      sb.append(postfix);
    }

    return sb.toString();
  }

  public static String toString( int[] array, int start, int end, String prefix, String separator, String postfix){
    StringBuilder sb = new StringBuilder();

    if (prefix != null) {
      sb.append(prefix);
    }

    for (int i=start; i<array.length && i < end; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(array[i]);
    }

    if (postfix != null) {
      sb.append(postfix);
    }

    return sb.toString();    
  }

  public static <T> T[] newArray (T... elements) {
    return elements;
  }

  public static <T> T[] appendArray (T[] base, T... elements) {
    if (base == null || base.length == 0){
      return elements;
    } else if (elements == null || elements.length == 0){
      return base;
    } else {
      Class<?> componentType = base.getClass().getComponentType();
      T[] a = (T[]) Array.newInstance(componentType, base.length + elements.length);
      System.arraycopy(base, 0, a, 0, base.length);
      System.arraycopy(elements, 0, a, base.length, elements.length);
      return a;
    }
  }

  public static <T> T[] prependArray (T[] base, T... elements) {
    if (base == null || base.length == 0){
      return elements;
    } else if (elements == null || elements.length == 0){
      return base;
    } else {
      Class<?> componentType = base.getClass().getComponentType();
      T[] a = (T[]) Array.newInstance(componentType, base.length + elements.length);
      System.arraycopy(base,0, a,elements.length, base.length);
      System.arraycopy(elements,0, a,0, elements.length);
      return a;
    }
  }

  public static String[] prependArray (String[] base, String... elements){
    if (base == null || base.length == 0){
      return elements;
    } else if (elements == null || elements.length == 0){
      return base;
    } else {
      String[] a = new String[base.length + elements.length];
      System.arraycopy(base,0, a,elements.length, base.length);
      System.arraycopy(elements,0, a,0, elements.length);
      return a;
    }
  }


  public static <T> T[] arrayWithoutFirst( T[] base, int nElements){
    if (base == null){
      return null;
    } else if (nElements >= base.length){
      Class<?> componentType = base.getClass().getComponentType();
      T[] a = (T[]) Array.newInstance(componentType, 0);
      return a;
    } else {
      int n = base.length - nElements;
      Class<?> componentType = base.getClass().getComponentType();
      T[] a = (T[]) Array.newInstance(componentType, n);
      System.arraycopy(base, nElements, a, 0, n);
      return a;
    }
  }

  public static <T> T[] appendElement (T[] base, T newElement){
    int len = base.length;

    Class<?> componentType = base.getClass().getComponentType();
    T[] a = (T[]) Array.newInstance(componentType, len + 1);
    System.arraycopy(base, 0, a, 0, len);
    a[len] = newElement;

    return a;
  }

  public static <T> T[] appendUniqueElement (T[] base, T newElement){
    int len = base.length;

    for (int i=0; i<len; i++){
      if (base[i] == newElement){
        return base;
      }
    }

    return appendElement(base, newElement);
  }

  
  public static <T> T[] removeElement (T[] base, T element) {
    int len = base.length;

    for (int i=0; i<len; i++){
      if (base[i] == element){
        Class<?> componentType = base.getClass().getComponentType();
        T[] a = (T[]) Array.newInstance(componentType, len -1);
        System.arraycopy(base, 0, a, 0, i);
        System.arraycopy(base, i+1, a, i, len-i-1);
        return a;
      }
    }

    return base;
  }

  public static <T> boolean hasElementOfType (T[] base, Class<?> elemType){
    int len = base.length;
    for (int i=0; i<len; i++){
      if (elemType.isInstance(base[i])){
        return true;
      }
    }

    return false;
  }

  public static <T,E> E getNextElementOfType (T[] base, Class<E> elemType, T prev){
    boolean prevSeen = (prev == null);
    int len = base.length;
    for (int i=0; i<len; i++){
      if (elemType.isInstance(base[i])){
        if (prevSeen){
          return (E)base[i];
        } else {
          prevSeen = (base[i] == prev);
        }
      }
    }
    
    return null;
  }

  public static String[] arrayWithoutFirst(String[] base, int nElements){
    String[] a = new String[base.length-1];
    if (a.length > 0){
      System.arraycopy(base,1,a,0,a.length);
    }
    return a;
  }

  public static boolean equals (Object[] a1, Object[] a2){
    if (a1 == a2){
      return true;
    }

    if (a1 == null){
      if (a2 != null){
        for (int i=0; i<a2.length; i++){
          if (a2[i] != null){
            return false;
          }
        }
      }
    } else if (a2 == null){
      for (int i=0; i<a1.length; i++){
        if (a1[i] != null){
          return false;
        }
      }
    } else {
      if (a1.length != a2.length){
        return false;
      }

      for (int i = 0; i < a1.length; i++) {
        Object o1 = a1[i];
        Object o2 = a2[i];

        if (o1 != null) {
          if (!o1.equals(o2)) {
            return false;
          }
        } else if (o2 != null) {
          return false;
        }
      }
    }

    return true;

  }

  /**
   * equals first len objects of two reference arrays, which can contain null
   * elements. If any of the reference arrays is null, this is treated as
   * if all elements would be null
   */
  public static boolean compare (int len, Object[] a1, Object[] a2){
    if (a1 == null && a2 == null){
      return true;
    }
    
    if (a1 == null){
      if (a2 != null){
        if (a2.length < len){
          return false;
        }
        for (int i=0; i<len; i++){
          if (a2[i] != null){
            return false;
          }
        }
      }
    } else if (a2 == null){
      if (a1.length < len){
        return false;
      }
      for (int i=0; i<len; i++){
        if (a1[i] != null){
          return false;
        }
      }      
    } else {
      if (a1.length < len || a2.length < len){
        return false;
      }

      for (int i = 0; i < len; i++) {
        Object o1 = a1[i];
        Object o2 = a2[i];

        if (o1 != null) {
          if (!o1.equals(o2)) {
            return false;
          }
        } else if (o2 != null) {
          return false;
        }
      }
    }
    
    return true;
  }

  public static String stripToLastDot (String s){
    int i = s.lastIndexOf('.');
    if (i>=0){
      return s.substring(i+1);
    } else {
      return s;
    }
  }

  public static int setBit (int val, int idx){
    return (val | (1<<idx));
  }
  
  public static int clearBit (int val, int idx){
    return (val & ~(1<<idx));
  }

  public static String upToFirst( String s, char c){
    int i = s.indexOf(c);
    if (i >= 0){
      return s.substring(0, i);
    } else {
      return s;
    }
  }
  
  public static String fromFirst( String s, char c){
    int i = s.indexOf(c);
    if (i >= 0){
      return s.substring(i);
    } else {
      return s;
    }
  }
  
  
  /*=================== PRIVATE STUFF ===================*/

  private static final class Iteratorable<E> implements Iterable<E> {
    Iterator<E> iter;

    public Iteratorable(Iterator<E> iter) {
      this.iter = iter;
    }

    @Override
	public Iterator<E> iterator () {
      Iterator<E> ret = iter;
      iter = null;
      return ret;
    }
  }
}
