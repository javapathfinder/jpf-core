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

import gov.nasa.jpf.JPFException;

import java.util.Iterator;

/**
 * A hash map that holds int values associated with generic key objects.
 * This is a straight forward linked list hashmap.
 *
 * Key objects have to be invariant, lookup uses equality but checks for
 * identity as an optimization.
 *
 * note: this does deep copy clones, which can be quite expensive
 */
public final class IntTable<E> implements Iterable<IntTable.Entry<E>>, Cloneable{
  static final int INIT_TBL_POW = 7;
  static final double MAX_LOAD = 0.80;
  
  //--- inner types
  
  /**
   * encapsulates an Entry in the table.  changes to val will be reflected
   * in the table.
   */  
  public static class Entry<E> implements Cloneable {
    public final E key;
    public final int val;
    protected Entry<E> next;
    
    protected Entry(E k, int v) {
      key = k;
      val = v;
      next = null;
      }
    protected Entry(E k, int v, Entry<E> n) {
      key = k; 
      val = v;
      next = n; 
    }

    @Override
	@SuppressWarnings("unchecked")
    public Entry<E> clone() {
      try {
        return (Entry<E>)super.clone();
      } catch (CloneNotSupportedException x){
        throw new JPFException("clone failed");
      }
    }

    @Override
	public String toString() {
      return key.toString() + " => " + val;
    }
    
    //--- methods required to use IntTable entries itself as HashMap keys
    // but beware - val can be modified since we expose it (never modify
    // key objects of HashMaps)

    @Override
	public int hashCode (){
      return OATHash.hash(key.hashCode(), val);
    }
    
    @Override
	public boolean equals (Object o){
      if (o instanceof Entry){
        @SuppressWarnings("unchecked")
        Entry<E> other = (Entry<E>)o;
        if (val == other.val){
          E k = other.key;
          if (key == k || key.equals(k)){
            return true;
          }
        }
      }
      
      return false;
    }
  }
  
  /**
   * helper class to store a compact, invariant representation of this table
   */
  public static class Snapshot<E> {
    protected final int tblSize;
    protected final int tblPow;
    
    protected final int[] indices;
    protected final E[] keys;
    protected final int[] vals;
    
    @SuppressWarnings("unchecked")
    protected Snapshot (IntTable<E> t){
      Entry<E>[] tbl = t.table;
      int nEntries = t.size;
            
      tblSize = tbl.length;
      tblPow = t.tblPow;
      
      indices = new int[nEntries];
      keys = (E[]) new Object[nEntries];
      vals = new int[nEntries];
      
      int j = 0;
      for (int i=0; i<tbl.length && j<nEntries; i++){
        Entry<E> e = tbl[i];
        if (e != null){
          if (e.next == null){ // just one entry under this head
            
            indices[j] = i;
            keys[j] = e.key;
            vals[j] = e.val;
            j++;
            
          } else {
            // we have to store in reverse order so that restore preserves it
            // we do the revert here because storing happens once, whereas restore can happen many times
            int n = 1;
            for (Entry<E> ee = e.next; ee != null; ee = ee.next){
              n++;
            }

            int k = j+n-1;
            j += n;
            for (; e != null; e = e.next){  
              indices[k] = i;
              keys[k] = e.key;
              vals[k] = e.val;
              k--;
            }
          }
        }
      }
    }
  }
  
  //--- instance fields
  
  protected Entry<E>[] table;  // array of entry heads
  protected int tblPow;        // = log_2(table.length)
  protected int mask;          // = table.length - 1
  protected int nextRehash;    // = ceil(MAX_LOAD * table.length);
  protected int size;          // number of Entry<E> objects reachable from table
  
  protected Entry<E> nullEntry = null;
  
  Snapshot<E> lastSnapshot;   // cache for the last snapshot (nulled once the IntTable is changed)
  
  public IntTable() {
    this(INIT_TBL_POW);
  }
  
  public IntTable(int pow) {
    newTable(pow);
    size = 0;
  }
  
  public Snapshot<E> getSnapshot(){
    if (lastSnapshot == null) {
      lastSnapshot = new Snapshot<E>(this);
    }
    
    return lastSnapshot;
  }
  
  @SuppressWarnings("unchecked")
  public void restore (Snapshot<E> snapshot){
    Entry<E>[] tbl = new Entry[snapshot.tblSize];
    
    int[] indices = snapshot.indices;
    E[] keys = snapshot.keys;
    int[] vals = snapshot.vals; 
    int nEntries = vals.length;
    
    for (int i=0; i<nEntries; i++){
      int idx = indices[i];
      tbl[idx] = new Entry<E>( keys[i], vals[i], tbl[idx]);
    }
    
    table = tbl;
    size = nEntries;
    mask = table.length -1;
    nextRehash = (int) Math.ceil(MAX_LOAD * table.length);
    tblPow = snapshot.tblPow;
    
    lastSnapshot = snapshot;
  }

  // this is a deep copy (needs to be because entries are reused when growing the table)
  @Override
  public IntTable<E> clone() {
    try {
      @SuppressWarnings("unchecked")
      IntTable<E> t = (IntTable<E>)super.clone();
      Entry<E>[] tbl = table.clone();
      t.table = tbl;

      // clone entries
      int len = table.length;
      for (int i=0; i<len; i++){
        Entry<E> eFirst = tbl[i];
        if (eFirst != null){
          eFirst = eFirst.clone();
          Entry<E> ePrev = eFirst;
          for (Entry<E> e = eFirst.next; e != null; e = e.next){
            e = e.clone();
            ePrev.next = e;
            ePrev = e;
          }
          tbl[i] = eFirst;
        }
      }

      return t;

    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("clone failed");
    }
  }

  @SuppressWarnings("unchecked")
  protected void newTable(int pow) {
    tblPow = pow;
    table = new Entry[1 << tblPow];
    mask = table.length - 1;
    nextRehash = (int) Math.ceil(MAX_LOAD * table.length);
  }
  
  protected int getTableIndex(E key) {
    int hc = key.hashCode();
    int ret = hc ^ 786668707;
    ret += (hc >>> tblPow);
    return (ret ^ 1558394450) & mask;
  }

  protected boolean maybeRehash() {
    if (size < nextRehash){
      return false;
      
    } else {
      lastSnapshot = null;
      size = 0;
      Entry<E>[] old = table;
      int oldTblLength = old.length;
      newTable(tblPow + 1);
      int len = oldTblLength;
      for (int i = 0; i < len; i++) {
        addList(old[i]);
      }

      return true;
    }
  }
  
  private void addList(Entry<E> e) {
    Entry<E> cur = e;
    while (cur != null) {
      Entry<E> tmp = cur;
      cur = cur.next;
      addEntry(tmp, getTableIndex(tmp.key));
    }
  }
  
  //--- the methods traversing the entry lists
  
  // helper for adding
  protected void addEntry(Entry<E> e, int idx) {
    e.next = table[idx];
    table[idx] = e;
    
    size++;
    lastSnapshot = null;
  }
  
  // helper for searching
  protected Entry<E> getEntry(E key, int idx) {
    Entry<E> cur = table[idx];
    while (cur != null) {
      E k = cur.key;
      
      // note - this assumes invariant keys !!
      if (k == key || (k.equals(key))){
        return cur;
      }
      cur = cur.next;
    }
    return null; // not found
  }

  // helper for value update
  protected void replaceEntryValue( int idx, Entry<E> oldEntry, int newValue) {
    Entry<E> last = null;
    
    for (Entry<E> e = table[idx]; e != null; e = e.next, last = e) {
      if (e == oldEntry) {
        Entry<E> newEntry = new Entry<E>(oldEntry.key, newValue);
        newEntry.next = e.next;
        lastSnapshot = null;
        
        if (last == null) {
          table[idx] = newEntry;
        } else {
          last.next = newEntry;
        }
      }
    }    
  }
  
  //--- public methods
  
  /** returns number of bindings in the table. */
  public int size() {
    return size;
  }
  
  /** ONLY USE IF YOU ARE SURE NO PREVIOUS BINDING FOR key EXISTS. */
  public Entry<E> add (E key, int val) {
    Entry<E> e = new Entry<E>(key,val);
    if (key == null) {
      nullEntry = e;
    } else {
      maybeRehash();
      addEntry(e, getTableIndex(key));
    }
    
    return e;
  }
  
  /** lookup, returning null if no binding. */
  public Entry<E> get (E key) {
    return getEntry(key, getTableIndex(key));
  }
  
  /**
   * a little optimization to speed up counter increments
   */
  public Entry<E> getInc (E key){
    int idx = getTableIndex(key);
    
    Entry<E> last = null;
    for (Entry<E> e = table[idx]; e != null; e = e.next) {
      if (e.key == key || e.key.equals(key)) { // found it, replace entry
        Entry<E> newEntry = new Entry<E>(key, e.val+1, e.next);
        lastSnapshot = null;
        
        if (last == null) {
          table[idx] = newEntry;
        } else {
          last.next = newEntry;
        }
        
        return newEntry;
        
      } else {
        last = e;
      }
    }
    
    // it wasn't there, add a new entry with value 1
    Entry<E> newEntry = new Entry<E>( key, 1);
    if (maybeRehash()) {
      idx = getTableIndex(key);
    }
    addEntry( newEntry, idx);

    return newEntry;
  }
  
  /** just like HashMap put. */
  public void put(E key, int val) {    
    if (key == null) {
      if (nullEntry == null) {
        nullEntry = new Entry<E>(null,val);
        size++;
      } else {
        nullEntry = new Entry<E>(null, val);
      }
      return;
    }
    
    int idx = getTableIndex(key);
    Entry<E> e = getEntry(key, idx);
    if (e == null) { // wasn't there
      if (maybeRehash()){
        idx = getTableIndex(key);
      }
      addEntry(new Entry<E>(key,val), idx);
      
    } else {
      replaceEntryValue( idx, e, val);
      lastSnapshot = null;
    }
  }

  /** removes a binding/entry from the table. */
  public Entry<E> remove(E key) {
    int idx = getTableIndex(key);
    Entry<E> prev = null;
    Entry<E> cur = table[idx];
    while (cur != null) {
      E k = cur.key;
      if (k == key || k.equals(key)) {
        if (prev == null) {
          table[idx] = cur.next;
        } else {
          prev.next = cur.next;
        }
        cur.next = null;
        size--;
        lastSnapshot = null;
    
        return cur;
      }
      prev = cur;
      cur = cur.next;
    }
    
    return null; // not found
  }
  
  
  /** empties the table, leaving it capacity the same. */
  public void clear() {
    table = new Entry[table.length];
    nullEntry = null;
    size = 0;
    lastSnapshot = null;
  }
  
  /** returns the next val to be assigned by a call to pool() on a fresh key. */
  public int nextPoolVal() {
    return size;
  }
  
  /** gets the Entry associated with key, adding previous `size' if not yet bound. */
  public Entry<E> pool(E key) {
    if (key == null) {
      if (nullEntry == null) {
        nullEntry = new Entry<E>(null,size);
        size++;
      }
      return nullEntry;
    }
    
    int idx = getTableIndex(key);
    Entry<E> e = getEntry(key, idx);
    if (e == null) {
      if (maybeRehash()) {
        idx = getTableIndex(key);
      }
      e = new Entry<E>(key,size);
      addEntry(e, idx);
    }
    return e;
  }
  
  /** shorthand for <code>pool(key).val</code>. */
  public int poolIndex(E key) {
    return pool(key).val;
  }
  
  /** shorthand for <code>pool(key).key</code>. */
  public E poolKey(E key) {
    return pool(key).key;
  }
  
  /** shorthand for <code>get(key) != null</code>. */
  public boolean hasEntry(E key) {
    return get(key) != null;
  }
  


  /**
   * returns an iterator over the entries.  unpredictable behavior could result if
   * using iterator after table is altered.
   */
  @Override
  public Iterator<Entry<E>> iterator () {
    return new TblIterator();
  }

  protected class TblIterator implements Iterator<Entry<E>> {
    int idx;
    Entry<E> cur;

    public TblIterator() {
      idx = -1; cur = null;
      advance();
    }
    
    void advance() {
      if (cur != null) {
        cur = cur.next;
      }
      int len = table.length;
      while (idx < len && cur == null) {
        idx++;
        if (idx < len) {
          cur = table[idx];
        }
      }
    }
    
    @Override
	public boolean hasNext () {
      return idx < table.length;
    }

    @Override
	public Entry<E> next () {
      Entry<E> e = cur;
      advance();
      return e;
    }

    @Override
	public void remove () { 
      throw new UnsupportedOperationException();
    }
  }

  /**
   * for debugging purposes
   */
  public void dump(){
    System.out.print('{');
    int n=0;
    for (int i=0; i<table.length; i++){
      for (Entry<E> e = table[i]; e != null; e = e.next){
        if (n++>0){
          System.out.print(',');
        }
        System.out.print('(');
        System.out.print(e.key);
        System.out.print("=>");
        System.out.print(e.val);
        System.out.print(')');
      }
    }
    System.out.println('}');
  }
  
  public int computeSize() {
    int n=0;
    for (int i=0; i<table.length; i++){
      for (Entry<E> e = table[i]; e != null; e = e.next){
        n++;
      }
    }
    
    return n;
  }
}
