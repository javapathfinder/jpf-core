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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Persistent (immutable) associative array that maps integer keys to generic reference values.
 * <p>
 * PSIntMap is implemented as a bitwise trie which processes key bits in msb order
 * (from left to right) and has the same depth along all paths (i.e. values are only kept at the
 * terminal node level, which corresponds to the rightmost bit block in the key).
 * 
 * This particular implementation was chosen to optimize performance for dense key value domains,
 * e.g. keys that are computed from counters. More specifically, PSIntMap was designed to be a
 * suitable basis for JPF Heap implementations with their characteristic usage pattern:
 *   ..
 *   transition{ ..alloc( n),..alloc(n+1),..alloc(n+2), ..}, garbage-collection{ remove(x),remove(y),..}
 *   ..
 * 
 * The 32bit keys are broken up into 5bit blocks that represent the trie levels, each 5bit block
 * (0..31) being the index for the respective child node or value.
 * For instance, a key/value pair of 12345->'x' is stored as
 * <blockquote><pre>
 *   level:     6    5     4     3     2     1     0
 *   key:       00.00000.00000.00000.01100.00001.11001  = 12345
 *   block-val:  0     0     0     0    12     1    25
 * 
 *       Node0 (level 2 : nodes)
 *         ... 
 *         [12] -> Node1 (level 1 : nodes)
 *                   ...
 *                   [1] -> Node2 (level 0 : values)
 *                            ...
 *                           [25] -> 'x'
 *</pre></blockquote>
 * The main benefit of using this representation is that existing maps are never modified (are
 * persistent) and hence a previous state can be restored by simply keeping the reference of
 * the respective map. The main drawback is that not only the changed value has to be stored
 * upon add/remove, but everything from the node that contains this value up to the root node.
 * 
 * This implementation partitions keys from left (msb) to right, which has the major property that
 * consecutive keys are stored in the same node, which in turn allows for efficient caching of
 * the last modified node. Keeping track of this 'stagingNode' avoids copying anything
 * but the affected node until the next staging node miss, at which point the old stagingNode
 * has to be merged. This merge only requires copying of old stagingNode parents up to the
 * level that already has been copied due to the new key insertion that caused the stagingNode miss).
 * 
 * The internal trie representation uses a protected Node type, which uses the bit block values (0..31)
 * as index into an array that stores either child node references (in case this is not a
 * terminal block), or value objects (if this is the terminal level). There are three Node
 * subtypes that get promoted upon population in the following order:
 * <ul>
 *  <li>OneNode - store only a single value/child element. Every node starts as a OneNode
 *  <li>BitmapNode - stores up to 31 elements (compressed)
 *  <li>FullNode - stores 32 elements
 * </ul>
 * Removal of keys leads to symmetric demotion of node types.
 * 
 * The five major public operations for PersistentIntMaps are
 * 
 * <ol>
 *  <li>set(int key, V value) -> PersistentIntMap : return a new map with an additional value 
 *  <li>get(int key) -> V : retrieve value
 *  <li>remove(int key) -> PersistentIntMap : return a new map without the specified key/value
 *  <li>removeAllSatisfying(Predicate<V> predicate) -> PersistentIntMap : return a new map
 *                             without all values satisfying the specified predicate
 *  <li>process(Processor<V> processor) : iterate over all values with specified processor
 * </ol>
 *  
 * Being a persistent data structure, the main property of PersistentIntMaps is that all
 * add/remove operations (set,remove,removeAllSatisfying) have to return new PersistenIntMap
 * instances, no destructive update is allowed. Normal usage patterns therefore look like this:
 * 
 * <blockquote><pre>
 *   PSIntMap<String> map = PSIntMap<String>();
 *   ..
 *   map = map.set(42, "fortytwo"); // returns a new map
 *   ..
 *   map = map.remove(42); // returns a new map
 *   ..
 *   map = map.removeAllSatisfying( new Predicate<String>(){ // returns a new map
 *     public boolean isTrue (String val){ 
 *       return val.endsWith("two");
 *     });
 *     
 *   map.process( new Processor<String>(){
 *     public void process (String val){
 *       System.out.println(val);
 *     });
 * </pre></blockquote>
 * 
 * NOTE: bitwise tries are inherently recursive data structures, which would naturally lend
 * itself to implementations using recursive methods (over nodes). However, the recursion
 * is always bounded (finite number of key bits), and we need to keep track of the terminal
 * (value) node that was modified, which means we would have to return two values from
 * every recursion level (new current level node and new (terminal) stagingNode), thus
 * requiring additional allocation per map operation ( e.g. 'result' object to keep track
 * of transient state, as in "node = node.assoc(..key, value, result)") or per recursive call
 * ( result: {node,stagingNode}, as in "result = node.assoc( ..key, value)"). The first solution
 * would allow to create/store a result object on the caller site, but this could compromise
 * map consistency in case of concurrent map operations. Both solutions are counter-productive
 * in a sense that PSIntMap is optimized to minimize allocation count, which is the crux of
 * persistent data structures.
 * 
 * The approach that is taken here is to manually unroll the recursion by means of explicit
 * operand stacks, which leads to methods with large number of local variables (to avoid
 * array allocation) and large switch statements to set respective fields. The resulting
 * programming style should only be acceptable for critical runtime optimizations.
 */ 

public class PSIntMap <V> implements Iterable<V> {

  //--- auxiliary types
  
  /**
   * Abstract root class for all node types. This type needs to be internal, no instances
   * are allowed to be visible outside the PersistentIntMap class hierarchy in order to guarantee
   * invariant data.
   * 
   * NOTE - since this is an internal type, we forego a lot of argument range checks in
   * the Node subclasses, assuming that all internal use has been tested and bugs will not
   * cause silent corruption of node data but will lead to follow-on exceptions such as
   * ArrayIndexOutOfBounds etc.
   */
  protected abstract static class Node<E> {
    
    abstract E getElementAtLevelIndex (int i);
    
    abstract int getNumberOfElements();
    abstract E getElementAtStorageIndex (int i);
    
    abstract int storageToLevelIndex (int i);
    
    //--- those clone
    abstract Node cloneWithAdded (int idx, E e);
    abstract Node cloneWithReplaced (int idx, E e);
    abstract Node cloneWithRemoved (int idx);
    abstract Node removeAllSatisfying (Predicate<E> pred);
    
    //--- no clone
    abstract void set (int idx, E e);
    abstract void process (int level, Node<E> targetNode, Node<E> stagingNode, Processor<E> p);
    
    boolean isEmptyNode(){
      return false;
    }
    
    //--- debugging
    void printIndentOn (PrintStream ps, int level) {
      for (int i=0; i<level; i++) {
        ps.print("    ");
      }
    }
    
    void printNodeInfoOn (PrintStream ps, Node targetNode, Node stagingNode) {
      String clsName = getClass().getSimpleName();
      int idx = clsName.indexOf('$');
      if (idx > 0) {
        clsName = clsName.substring(idx+1);
      }
      ps.print(clsName);
      
      if (this == targetNode){
        ps.print( " (target)");
      }
    }
    
    abstract void printOn(PrintStream ps, int level, Node targetNode, Node stagingNode);
  }
  
  /**
   * Node that has only one element and hence does not need an array.
   * If a new element is added, this OneNode gets promoted into a BitmapNode
   */
  protected static class OneNode<E> extends Node<E> {
    E e;
    int idx;
    
    OneNode (int idx, E e){
      this.idx = idx;
      this.e = e;
    }

    @Override
    int getNumberOfElements(){
      return 1;
    }
    
    @Override
    E getElementAtStorageIndex (int i){
      assert i == 0;
      return e;
    }
    
    @Override
    E getElementAtLevelIndex(int i) {
      if (i == idx){
        return e;
      } else {
        return null;
      }
    }

    @Override
    int storageToLevelIndex (int i){
      if (i == 0){
        return idx;
      }
      return -1;
    }
    
    /**
     * this assumes the index is not set 
     */
    @Override
    Node cloneWithAdded(int i, E newElement) {
      assert i != idx;
      
      Object[] a = new Object[2];
      
      if (i < idx){
        a[0] = newElement;
        a[1] = e;
      } else {
        a[0] = e;
        a[1] = newElement;
      }
      int bitmap = (1 << idx) | (1 << i);
      
      return new BitmapNode(bitmap, a);
    }

    /**
     * this assumes the index is set 
     */
    @Override
    Node cloneWithReplaced(int i, E e) {
      assert i == idx;
      return new OneNode( i, e);
    }

    /**
     * this assumes the index is set 
     */
    @Override
    Node cloneWithRemoved(int i){
      assert (i == idx);
      return null;
    }
    
    @Override
    Node removeAllSatisfying (Predicate<E> pred){
      if (pred.isTrue(e)){
        return null;
      } else {
        return this;
      }
    }
    
    @Override
    void set (int i, E e){
      assert i == idx;
      this.e = e;
    }
    
    @Override
    boolean isEmptyNode(){
      return idx == 0;
    }
    
    @Override
    void process (int level, Node<E> targetNode, Node<E> stagingNode, Processor<E> p){
      if (level == 0){
        if (this == targetNode){
          stagingNode.process( 0, null, null, p);
        } else {
          p.process(e);
        }
      } else {
        ((Node)e).process( level-1, targetNode, stagingNode, p);
      }
    }
    
    @Override
	public void printOn (PrintStream ps, int depth, Node targetNode, Node stagingNode) {
      printIndentOn(ps, depth);
      ps.printf("%2d: ", idx);

      if (e instanceof Node) {
        Node<E> n = (Node<E>) e;
        n.printNodeInfoOn(ps, targetNode, stagingNode);
        ps.println();
        n.printOn(ps, depth+1, targetNode, stagingNode);
      } else {
        ps.print("value=");
        ps.println(e);
      }
    }

  }
  
  /**
   * A node that holds between 2 and 31 elements.
   * 
   * We use bitmap based element array compaction - the corresponding bit block of the key
   * [0..31] is used as an index into a bitmap. The elements are stored in a dense
   * array at indices corresponding to the number of set bitmap bits to the right of the
   * respective index in the bitmap, e.g. for
   * 
   * <blockquote><pre> 
   *   key = 289 =  0b01001.00001, shift = 5, assuming node already contains key 97 = 0b00011.00001 =>
   *     idx = (key >>> shift) & 0x1f = 0b01001 = 9
   *     bitmap =  1000001000  : bit 9 from key 289 (0b01001.), bit 3 from key 97 (0b00011.)
   *     node element index for key 289 (level index 9) = 1 (one set bit to the right of bit 9)
   * </pre></blockquote>
   * 
   * While storage index computation seems complicated and expensive, there are efficient algorithms to
   * count leading/trailing bits by means of binary operations and minimal branching, which is
   * suitable for JIT compilation (see http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogLookup)
   * 
   * <p>
   * If the bit count of a BitmapNode is 2 and an element is removed, this gets demoted into q OneNode.
   * If the bit count of a BitmapNode is 31 and an element is added, this gets promoted into a FullNode
   */
  protected static class BitmapNode<E> extends Node<E> {
    final E[] elements;
    final int bitmap;
    
    BitmapNode (int idx, E e, E e0){
      bitmap = (1 << idx) | 1;
      
      elements = (E[]) new Object[2];
      elements[0] = e0;
      elements[1] = e;
    }
    
    BitmapNode (int bitmap, E[] elements){
      this.bitmap = bitmap;
      this.elements = elements;
    }
    
    @Override
    int getNumberOfElements(){
      return elements.length;
    }
    
    @Override
    E getElementAtStorageIndex (int i){
      return elements[i];
    }
    
    @Override
    E getElementAtLevelIndex (int i) {
      int bit = 1 << i;
      if ((bitmap & bit) != 0) {
        int idx = Integer.bitCount( bitmap & (bit-1));
        return elements[idx];
      } else {
        return null;
      }
    }

    /**
     * get the position of the (n+1)'th set bit in bitmap
     */
    @Override
    int storageToLevelIndex (int n){
      int v = bitmap;
      /**/
      switch (n){
        case 30: v &= v-1;
        case 29: v &= v-1;
        case 28: v &= v-1;
        case 27: v &= v-1;
        case 26: v &= v-1;
        case 25: v &= v-1;
        case 24: v &= v-1;
        case 23: v &= v-1;
        case 22: v &= v-1;
        case 21: v &= v-1;
        case 20: v &= v-1;
        case 19: v &= v-1;
        case 18: v &= v-1;
        case 17: v &= v-1;
        case 16: v &= v-1;
        case 15: v &= v-1;
        case 14: v &= v-1;
        case 13: v &= v-1;
        case 12: v &= v-1;
        case 11: v &= v-1;
        case 10: v &= v-1;
        case 9: v &= v-1;
        case 8: v &= v-1;
        case 7: v &= v-1;
        case 6: v &= v-1;
        case 5: v &= v-1;
        case 4: v &= v-1;
        case 3: v &= v-1;
        case 2: v &= v-1;
        case 1: v &= v-1;
      }
      /**/
      
      /**
      for (int i=n; i>0; i--){
        v &= v-1; // remove n-1 least significant bits
      }
      **/
      
      v = v & ~(v-1); // reduce to the least significant bit
      return TrailingMultiplyDeBruijnBitPosition[((v & -v) * 0x077CB531) >>> 27];
    }
    
    @Override
    Node cloneWithAdded(int i, E e) {
      int bit = 1 << i;
      int idx = Integer.bitCount( bitmap & (bit -1));
      
      if (elements.length == 31){
        Object[] a = new Object[32];

        if (idx > 0) {
          System.arraycopy(elements, 0, a, 0, idx);
        }
        if (idx < 31) {
          System.arraycopy(elements, idx, a, idx + 1, 31 - idx);
        }
        a[idx] = e;
        return new FullNode(a);
        
      } else {
        int n = elements.length;
        Object[] a = new Object[n + 1];

        if (idx > 0) {
          System.arraycopy(elements, 0, a, 0, idx);
        }

        a[idx] = e;

        if (n > idx) {
          System.arraycopy(elements, idx, a, idx + 1, (n - idx));
        }
      
        return new BitmapNode( bitmap | bit, a);
      }
    }

    @Override
    Node cloneWithReplaced(int i, E e) {
      int idx = Integer.bitCount( bitmap & ((1<<i) -1));
      
      E[] a = elements.clone();
      a[idx] = e;
      
      return new BitmapNode( bitmap, a);
    }
    
    @Override
    Node cloneWithRemoved(int i){
      int bit = (1<<i);
      int idx = Integer.bitCount( bitmap & (bit-1));
      int n = elements.length;
      
      if (n == 2){
        E e = (idx == 0) ? elements[1] : elements[0]; // the remaining value
        int i0 = Integer.numberOfTrailingZeros(bitmap ^ bit);
        return new OneNode( i0, e);
        
      } else {
        Object[] a = new Object[n - 1];
        if (idx > 0) {
          System.arraycopy(elements, 0, a, 0, idx);
        }
        n--;
        if (n > idx) {
          System.arraycopy(elements, idx + 1, a, idx, (n - idx));
        }
        return new BitmapNode(bitmap ^ bit, a);
      }
    }
    
    @Override
    Node removeAllSatisfying (Predicate<E> pred){
      int newBitmap = bitmap;
      int len = elements.length;
      int newLen = len;
      E[] elem = elements;
      int removed = 0;
      
      for (int i=0, bit=1; i<len; i++, bit<<=1){
        while ((newBitmap & bit) == 0){
          bit <<= 1;
        }
        
        if (pred.isTrue(elem[i])){
          newBitmap ^= bit;
          newLen--;
          removed |= (1 << i);
        } 
      }
      
      if (newLen == 0){ // nothing left
        return null;
        
      } else if (newLen == len){ // nothing removed
        return this;
        
      } else if (newLen == 1) { // just one value left - reduce to OneNode
        int i = Integer.bitCount( bitmap & (newBitmap -1));
        int idx = Integer.numberOfTrailingZeros(newBitmap);
        return new OneNode<E>( idx, elem[i]);
        
      } else { // some values removed - reduced BitmapNode
        E[] newElements = (E[]) new Object[newLen];
        for (int i=0, j=0; j<newLen; i++){
          if ((removed & (1<<i)) == 0){
            newElements[j++] = elem[i];
          }
        }
        return new BitmapNode( newBitmap, newElements);
      }
    }

    
    @Override
    void set (int i, E e){
      int idx = Integer.bitCount( bitmap & ((1<<i) -1));
      elements[idx] = e;
    }
    
    @Override
    void process (int level, Node<E> targetNode, Node<E> stagingNode, Processor<E> p){
      if (level == 0){
        if (this == targetNode){
          stagingNode.process(0, null, null, p);
        } else {
          for (int i = 0; i < elements.length; i++) {
            p.process(elements[i]);
          }
        }
      } else {
        for (int i=0; i<elements.length; i++){
          ((Node)elements[i]).process(level-1, targetNode, stagingNode, p);
        }        
      }
    }
    
    @Override
	void printOn (PrintStream ps, int depth, Node targetNode, Node stagingNode) {
      int j=0;
      for (int i=0; i<32; i++) {
        if ((bitmap & (1<<i)) != 0) {
          printIndentOn(ps, depth);
          ps.printf("%2d: ", i);
          
          E e = elements[j++];
          if (e instanceof Node) {
            Node<E> n = (Node<E>)e;
            n.printNodeInfoOn(ps, targetNode, stagingNode);
            ps.println();
            n.printOn(ps, depth+1, targetNode, stagingNode);
          } else {
            ps.print("value=");
            ps.println(e);
          }
        }
      }
    }

  }

  /**
   * newElements node with 32 elements, for which we don't need newElements bitmap.
   * No element can be added since this means we just promote an existing element
   * If an element is removed, this FullNode gets demoted int newElements BitmapNode
   */
  protected static class FullNode<E> extends Node<E> {
    final E[] elements;

    FullNode (E[] elements){
      this.elements = elements;
    }
    
    @Override
    int getNumberOfElements(){
      return 32;
    }
    
    @Override
    E getElementAtStorageIndex (int i){
      return elements[i];
    }
    
    @Override
    E getElementAtLevelIndex (int i) {
      return elements[i];
    }

    @Override
    int storageToLevelIndex (int i){
      return i;
    }

    
    @Override
    Node cloneWithAdded (int idx, E e){
      throw new RuntimeException("can't add a new element to a FullNode");
    }
    
    @Override
    Node cloneWithReplaced (int idx, E e){
      E[] newElements = elements.clone();
      newElements[idx] = e;
      return new FullNode(newElements);
    }
    
    @Override
    Node cloneWithRemoved(int idx){
      Object[] a = new Object[31];
      int bitmap = 0xffffffff ^ (1 << idx);
      
      if (idx > 0){
        System.arraycopy(elements, 0, a, 0, idx);
      }
      if (idx < 31){
        System.arraycopy(elements, idx+1, a, idx, 31-idx);
      }
      
      return new BitmapNode( bitmap, a);
    }
    
    @Override
    Node removeAllSatisfying (Predicate<E> pred){
      int newBitmap = 0xffffffff;
      int newLen = 32;
      E[] elem = elements;
      int removed = 0;
      
      for (int i=0, bit=1; i<32; i++, bit<<=1){
        if (pred.isTrue(elem[i])){
          newBitmap ^= bit;
          newLen--;
          removed |= (1 << i);
        } 
      }
      
      if (newLen == 0){ // nothing left
        return null;
        
      } else if (newLen == 32){ // nothing removed
        return this;
        
      } else if (newLen == 1) { // just one value left - reduce to OneNode
        int idx = Integer.numberOfTrailingZeros(newBitmap);  
        return new OneNode<E>( idx, elem[idx]);
        
      } else { // some values removed - reduced BitmapNode
        E[] newElements = (E[]) new Object[newLen];
        for (int i=0, j=0; j<newLen; i++){
          if ((removed & (1<<i)) == 0){
            newElements[j++] = elem[i];
          }
        }
        return new BitmapNode( newBitmap, newElements);
      }
    }
    
    @Override
    void set (int i, E e){
      elements[i] = e;
    }
    
    @Override
    void process (int level, Node<E> targetNode, Node<E> stagingNode, Processor<E> p){
      if (level == 0){
        if (this == targetNode){
          stagingNode.process(0, null, null, p);
        } else {
          for (int i = 0; i < elements.length; i++) {
            p.process(elements[i]);
          }
        }
      } else {
        for (int i=0; i<elements.length; i++){
          ((Node)elements[i]).process(level-1, targetNode, stagingNode, p);
        }        
      }
    }
    
    @Override
	void printOn (PrintStream ps, int depth, Node targetNode, Node stagingNode) {    
      for (int i=0; i<32; i++) {
        printIndentOn(ps, depth);
        ps.printf("%2d: ", i);

        E e = elements[i];
        if (e instanceof Node) {
          Node<E> n = (Node<E>) e;
          n.printNodeInfoOn(ps, targetNode, stagingNode);
          ps.println();
          n.printOn(ps, depth+1, targetNode, stagingNode);
        } else {
          ps.print("value=");
          ps.println(e);
        }
      }
    }
  }

  @Override
@SuppressWarnings({ "rawtypes", "unchecked" })
  public Iterator<V> iterator(){
    return new ValueIterator();
  }
  
  /**
   * this is less efficient than using map.process(processor), but required to use PSIntMaps in lieu of ordinary containers
   * Since PSIntMaps are bounded recursive data structures, we have to model newElements stack explicitly, but at least we know it is
   * not exceeding newElements depth of 6 (5 bit index blocks)
   * 
   * Note - there are no empty nodes. Each one has at least newElements single child node or value
   */
  protected class ValueIterator implements Iterator<V> {

    Node node;
    int nodeIdx, maxNodeIdx;
    
    Node[] parentNodeStack;
    int[] parentIdxStack;
    int top;
    int nVisited, nTotal;
    
    
    @SuppressWarnings("unchecked")
    public ValueIterator (){
      node = PSIntMap.this.rootNode;
      if (node != null) {
        if (node == PSIntMap.this.targetNode){
          node = PSIntMap.this.stagingNode;
        }
        
        maxNodeIdx = node.getNumberOfElements();
        
        // nodeIdx = 0;
        // nVisited = 0;
        // top = 0;
        
        int depth = PSIntMap.this.rootLevel;
        parentNodeStack = new Node[depth];
        parentIdxStack = new int[depth];
            
        nTotal = PSIntMap.this.size;
      }
    }
    
    @Override
    public boolean hasNext() {
      return nVisited < nTotal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V next() {
      if (nVisited >= nTotal) {
        throw new NoSuchElementException();
      }
      
      int idx = nodeIdx;
      Object nv = node.getElementAtStorageIndex( idx);
      
      //--- descend
      while (top < PSIntMap.this.rootLevel) {
        parentNodeStack[top] = node; // push current node on stack
        parentIdxStack[top] = idx;
        top++;
        
        if (nv == PSIntMap.this.targetNode){
          node = PSIntMap.this.stagingNode;
        } else {        
          node = (Node)nv;
        }
        
        idx = nodeIdx = 0;
        maxNodeIdx = node.getNumberOfElements();
        
        nv = node.getElementAtStorageIndex(0);
      }
      
      //--- newElements value, finally
      nVisited++;
      idx++;

      if (idx == maxNodeIdx) { // done, no more child nodes/values for this node
        while (top > 0) { // go up
          top--;
          node = parentNodeStack[top];
          nodeIdx = parentIdxStack[top] + 1;
          maxNodeIdx = node.getNumberOfElements();
          if (nodeIdx < maxNodeIdx) break;
        }
      } else {
        nodeIdx = idx;
      }

      //assert (nVisited == nTotal) || (nodeIdx < maxNodeIdx);
      return (V) nv;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("PersistentIntMap iterators don't support removal");
    }
    
  }

  
  //--- auxiliary data and functions
  
  static final int BASE_MASK = ~0x1f;
  
  static final int TrailingMultiplyDeBruijnBitPosition[] = {
    0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 
    31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
  };
  
  static int getNumberOfTrailingZeros (int v){
    return TrailingMultiplyDeBruijnBitPosition[((v & -v) * 0x077CB531) >>> 27];
  }
  
  
  // the values are the respective block levels
  static final int LeadingMultiplyDeBruijnBitPosition[] = {
    0, 1, 0, 2, 2, 4, 0, 5, 2, 2, 3, 3, 4, 5, 0, 6,
    1, 2, 4, 5, 3, 3, 4, 1, 3, 5, 4, 1, 5, 1, 0, 6
  };
  
  /**
   * get the start level [0..7] for the highest bit index (bit block). This is essentially counting the number of leading zero bits,
   * which we can derive from http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogLookup
   */
  static int getStartLevel (int v){
    v |= v >>> 1;
    v |= v >>> 2;
    v |= v >>> 4;
    v |= v >>> 8;
    v |= v >>> 16;

    return LeadingMultiplyDeBruijnBitPosition[(v * 0x07C4ACDD) >>> 27];
  }
  
  //--- instance data
  
  final protected int size;       // number of values in this map
  final protected int rootLevel;  // bit block level of the root node (highest non-0 bit block of all keys in map)
  final protected Node rootNode;  // topmost node of trie
  
  /*
   * the following fields are used to cache consecutive key operations with the goal of avoiding
   * path copies from the modified value node all the way up to the root node. As long as the same value
   * node is modified (hence msb key block traversal) we just need to keep track which position in the
   * trie the stagingNode refers to (stagingNodeMask), and only have to create a new stagingNode with the
   * updated values. Once we have a key operation that refers to a different value node position (staging miss),
   * we merge the old stagingNode back into the trie. If we do this after inserting the new key, only
   * nodes from the old stagingNode parent up to the first node that is on the new key path have to be copied,
   * the merge node (on the new stagingNode path) can be safely modified since it has only been created during
   * the ongoing map operation. Example:
   *                                          key    value
   * last mod key/value (old stagingNode) : a.c.e -> Y    => stagingNodeMask = a.c.FF
   * new key/value (new stagingNode)      : a.b.d -> X
   * 
   *                            a
   *                    n0: [...n1...]            root node (level 2)
   *                           /
   *                 b    c   /
   *          n1:  [.n2...n3.]                    
   *                /       \
   *           d   /         \                                                  e
   *    n2:  [.X..]      n3:  [.....]             value nodes (level 0)     [...Y...]
   *      new stagingNode       old targetNode  <-------------------------- old stagingNode
   *    (= new targetNode)
   * 
   * In this case, the sequence of operations is as follows:
   * <ol>
   *   <li> insert new key/value pair (a.b.d)->X into the trie, which is a stagingNode miss since
   *        stagingNodeMasks are different (a.b.FF != a.c.FF). This leads to copied/new nodes n2,n1,n0
   *   <li> check if old stagingNode differs from targetNode (had several consecutive modifications), if
   *        targetNode != stagingNode then merge old stagingNode <em>after</em> n2,n1,n0 creation
   *   <li> since n1 is already a new node that is not shared with any prior version of this map,
   *        its [c] element can be simply set to the old stagingNode, i.e. the merge does not require
   *        any additional allocation. Note that n1 has to contain a [c] element since we always link
   *        new stagingNodes into the trie upon creation. This means the number of elements in n1
   *        (and hence the node type) does not change, i.e. setting the new [c] element involves
   *        just a single AASTORE instruction
   *   <li> set stagingNode = targetNode = n2
   * </ol>
   */
  
  final protected Node<V> stagingNode; // last modified value node (not linked into the trie upon subsequent modification)
  final protected int stagingNodeMask; // key mask for stagingNode (key | 0x1f)
  final protected Node targetNode;     // original stagingNode state that is linked into the trie
  
  /**
   * the only public constructor
   */
  public PSIntMap(){
    this.size = 0;
    this.rootLevel = 0;
    this.rootNode = null;
    this.targetNode = null;
    this.stagingNode = null;
    this.stagingNodeMask = 0;
  }
  
  protected PSIntMap (int size, int rootLevel, Node rootNode, Node<V> stagingNode, Node<V> targetNode, int stagingNodeMask){
    this.size = size;
    this.rootLevel = rootLevel;
    this.rootNode = rootNode;
    this.stagingNode = stagingNode;
    this.targetNode = targetNode;
    this.stagingNodeMask = stagingNodeMask;
  }
  
  //--- public API
  
  public int size(){
    return size;
  }
  
  public V get (int key){
    if (stagingNodeMask == (key | 0x1f)){
      int idx = key & 0x1f;
      return stagingNode.getElementAtLevelIndex(idx);
      
    } else {
      if (rootNode == null) return null;
      
      int l = getStartLevel(key);
      if (l > rootLevel) return null;
      
      Node<Node> n = rootNode;
      
      switch (rootLevel){
        case 6: 
          n = n.getElementAtLevelIndex( key >>> 30);
          if (n == null) return null;
        case 5:
          n = n.getElementAtLevelIndex( (key >>> 25) & 0x1f); 
          if (n == null) return null;
        case 4:
          n = n.getElementAtLevelIndex( (key >>> 20) & 0x1f); 
          if (n == null) return null;
        case 3:
          n = n.getElementAtLevelIndex( (key >>> 15) & 0x1f);
          if (n == null) return null;
        case 2:
          n = n.getElementAtLevelIndex( (key >>> 10) & 0x1f);
          if (n == null) return null;
        case 1:
          n = n.getElementAtLevelIndex( (key >>>  5) & 0x1f);
          if (n == null) return null;
        case 0: 
          return ((Node<V>)n).getElementAtLevelIndex(key & 0x1f);
      }
      
      return null; // can't get here
    }
  }
  
  protected Node mergeStagingNode (){
    Node<Node> n2=null, n3=null, n4=null, n5=null, n6=null;
    int i1, i2=0, i3=0, i4=0, i5=0, i6=0;
    
    int k = stagingNodeMask;
    Node<Node> n = rootNode;
    
    switch (rootLevel){
      case 6: 
        i6 = (k >>> 30);
        n6 = n; 
        n = n.getElementAtLevelIndex(i6);
      case 5:
        i5 = (k >>> 25) & 0x1f;
        n5 = n;
        n = n.getElementAtLevelIndex(i5);
      case 4:
        i4 = (k >>> 20) & 0x1f;
        n4 = n;
        n = n.getElementAtLevelIndex(i4);
      case 3:
        i3 = (k >>> 15) & 0x1f;
        n3 = n; 
        n = n.getElementAtLevelIndex(i3);
      case 2:
        i2 = (k >>> 10) & 0x1f;
        n2 = n; 
        n = n.getElementAtLevelIndex(i2);
      case 1: 
        i1 = (k >>> 5) & 0x1f;
        n = n.cloneWithReplaced(i1, stagingNode);
        if (n2 != null){
          n = n2.cloneWithReplaced(i2, n);
          if (n3 != null){
            n = n3.cloneWithReplaced(i3, n);
            if (n4 != null){
              n = n4.cloneWithReplaced(i4, n);
              if (n5 != null){
                n = n5.cloneWithReplaced(i5, n);
                if (n6 != null){
                  n = n6.cloneWithReplaced(i6, n);
                }
              }
            }
          }
        }
        return n;
        
      case 0:
        // special case - only node in the trie is the targetNode
        return stagingNode;
    }
    
    return null; //  can't get here
  }
  
  /**
   * this relies on that all nodes from the new staging node to the newRootNode have been copied
   * and can be modified without cloning.
   * The modification does not change the node type since the old staging/target node was in the trie
   * The first node where new and old staging indices differ is the mergeNode that needs to be
   * modified (old staging path node replaced). This has to be level 1..6
   * Everything above the mergeNode is not modified (the newRootNode does not have to be copied
   * as it is new)
   * All nodes between the old stagingNode and the mergeNode have to be copied
   * The old stagingNode itself does not need to be cloned.
   */
  protected void mergeStagingNode (int key, int newRootLevel, Node newRootNode){
    int k = stagingNodeMask;
    int mergeLevel = getStartLevel( key ^ k); // block of first differing bit
    Node<Node> mergeNode = newRootNode;
    int shift = newRootLevel*5;
    
    //--- get the mergeNode
    for (int l=newRootLevel; l>mergeLevel; l--){
      int idx = (k >>> shift) & 0x1f;
      mergeNode = mergeNode.getElementAtLevelIndex(idx);
      shift -= 5;
    }
    int mergeIdx = (k >>> shift) & 0x1f;
    
    //--- copy from old staging up to mergeNode
    Node<Node> n5=null, n4=null, n3=null, n2=null, n1=null;
    int i5=0, i4=0, i3=0, i2=0, i1=0;
    Node<Node> n = mergeNode.getElementAtLevelIndex(mergeIdx);
    
    switch (mergeLevel-1){ 
      case 5:        
        i5 = (k >>> 25) & 0x1f;
        n5 = n;
        n = n.getElementAtLevelIndex(i5);
      case 4:
        i4 = (k >>> 20) & 0x1f;
        n4 = n;
        n = n.getElementAtLevelIndex(i4);
      case 3:
        i3 = (k >>> 15) & 0x1f;
        n3 = n;
        n = n.getElementAtLevelIndex(i3);
      case 2:
        i2 = (k >>> 10) & 0x1f;
        n2 = n;
        n = n.getElementAtLevelIndex(i2);
      case 1:
        i1 = (k >>> 5) & 0x1f;
        n1 = n;
      case 0:
        n = (Node)stagingNode;
      
        if (n1 != null){
          n = n1.cloneWithReplaced(i1, n);
          if (n2 != null) {
            n = n2.cloneWithReplaced(i2, n);
            if (n3 != null) {
              n = n3.cloneWithReplaced(i3, n);
              if (n4 != null) {
                n = n4.cloneWithReplaced(i4, n);
                if (n5 != null) {
                  n = n5.cloneWithReplaced(i5, n);
                }
              }
            }
          }          
        }
    }
    
    //--- modify mergeNode
    mergeNode.set(mergeIdx, n);
  }

  PSIntMap<V> remove (int key, boolean isTargetNode){
    Node<Node> n6=null, n5=null, n4=null, n3=null, n2=null, n1=null;
    Node<V> n0;
    int i6=0, i5=0, i4=0, i3=0, i2=0, i1=0, i0;
    
    Node<Node> n = rootNode;
    switch (rootLevel){
      case 6:
        i6 = (key >>> 30);
        n5 = n.getElementAtLevelIndex(i6);
        if (n5 == null){
          return this; // key not in map
        } else {
          n6 = n;
          n = n5;
        }
      case 5:
        i5 = (key >>> 25) & 0x1f;
        n4 = n.getElementAtLevelIndex(i5);
        if (n4 == null){
          return this; // key not in map
        } else {
          n5 = n;
          n = n4;
        }
      case 4:
        i4 = (key >>> 20) & 0x1f;
        n3 = n.getElementAtLevelIndex(i4);
        if (n3 == null){
          return this; // key not in map
        } else {
          n4 = n;
          n = n3;
        }
      case 3:
        i3 = (key >>> 15) & 0x1f;
        n2 = n.getElementAtLevelIndex(i3);
        if (n2 == null){
          return this; // key not in map
        } else {
          n3 = n;
          n = n2;
        }
      case 2:
        i2 = (key >>> 10) & 0x1f;
        n1 = n.getElementAtLevelIndex(i2);
        if (n1 == null){
          return this; // key not in map
        } else {
          n2 = n;
          n = n1;
        }
      case 1:
        i1 = (key >>> 5) & 0x1f;
        n0 = n.getElementAtLevelIndex(i1);
        if (n0 == null){
          return null;
        } else {
          n1 = n;
          n = (Node)n0;
        }
        
      case 0:
        n0 = (Node<V>)n;
        if (isTargetNode){
          n0 = null;
        } else {
          i0 = key & 0x1f;
          if (n0 == null || n0.getElementAtLevelIndex(i0) == null){
            return this; // key not in map
          } else {
            n0 = n0.cloneWithRemoved(i0);
          }
        }
        n = (Node)n0;
        if (n1 != null){
          n = (n == null) ? n1.cloneWithRemoved(i1) : n1.cloneWithReplaced(i1, n);
          if (n2 != null){
            n = (n == null) ? n2.cloneWithRemoved(i2) : n2.cloneWithReplaced(i2, n);
            if (n3 != null){
              n = (n == null) ? n3.cloneWithRemoved(i3) : n3.cloneWithReplaced(i3, n);
              if (n4 != null){
                n = (n == null) ? n4.cloneWithRemoved(i4) : n4.cloneWithReplaced(i4, n);
                if (n5 != null){
                  n = (n == null) ? n5.cloneWithRemoved(i5) : n5.cloneWithReplaced(i5, n);
                  if (n6 != null){
                    n = (n == null) ? n6.cloneWithRemoved(i6) : n6.cloneWithReplaced(i6, n);
                  }
                }
              }
            }
          }
        }
        
        if (n == null){
          return new PSIntMap<V>();
          
        } else {
          int newRootLevel = rootLevel;
          int newSb = (n0 == null) ? 0 : (key | 0x1f);
          
          while ((newRootLevel > 0) && n.isEmptyNode()){
            newRootLevel--;
            n = n.getElementAtLevelIndex(0);
          }
          
          if (!isTargetNode && (stagingNode != targetNode)){
            mergeStagingNode(key, newRootLevel, n);
          }
          
          return new PSIntMap<V>( size-1, newRootLevel, n, n0, n0, newSb);
        }
    }
    
    return null; // can't get here
  }
  
  public PSIntMap<V> remove (int key){
    int newSm = key | 0x1f;

    if (newSm == stagingNodeMask){ // staging node hit - this should be the dominant case
      int i = key & 0x1f;
      Node<V> n = stagingNode;
      if ((n.getElementAtLevelIndex(i)) != null) { // key is in the stagingNode
        n = n.cloneWithRemoved(i);
        if (n == null){ // staging node is empty, remove target node
          return remove(newSm, true);
        } else { // non-empty staging node, just replace it
          return new PSIntMap<V>( size-1, rootLevel, rootNode, n, targetNode, newSm);
        }
        
      } else { // key wasn't in the stagingNode
        return this;
      }
      
    } else { // staging node miss
      return remove( key, false);
    }
  }
  
  /**
   * this either replaces or adds newElements new value 
   */
  public PSIntMap<V> set (int key, V value){
  
    if (value == null){
      // we don't store null values, this is a remove in disguise
      return remove(key);
    }
    
    int newSm = key | 0x1f;
    
    if (newSm == stagingNodeMask){ // staging node hit - this should be the dominant case
      int i = key & 0x1f;
      Node<V> n = stagingNode;
      int newSize = size;
      if ((n.getElementAtLevelIndex(i)) == null) {
        n = n.cloneWithAdded(i, value);
        newSize = size+1;
      } else {
        n = n.cloneWithReplaced(i, value);
      }
      return new PSIntMap<V>( newSize, rootLevel, rootNode, n, targetNode, newSm);
      
    } else { // staging node miss
      int newRootLevel = getStartLevel(key);
      
      if (newRootLevel > rootLevel){ // old trie has to be merged in
        return setInNewRootLevel( newRootLevel, key, value);
        
      } else {     // new value can be added to old trie (stagingNode change)
        return setInCurrentRootLevel( key, value);
      }
    }
  }
  
  protected PSIntMap<V> setInNewRootLevel (int newRootLevel, int key, V value){
    int newSm = key | 0x1f;

    Node<Node> nOld;
    if (stagingNode != targetNode){
      nOld = mergeStagingNode();
    } else {
      nOld = rootNode;
    }
    
    //--- expand old root upwards
    if (nOld != null){
      for (int l = rootLevel + 1; l < newRootLevel; l++) {
        nOld = new OneNode(0, nOld);
      }
    }

    //--- create chain of new value nodes
    int i = key & 0x1f;
    Node nNew = new OneNode(i, value);
    int shift = 5;
    Node newStagingNode = nNew;
    for (int l = 1; l < newRootLevel; l++) {
      i = (key >>> shift) & 0x1f;
      nNew = new OneNode(i, nNew);
      shift += 5;
    }

    //--- create new root
    i = (key >>> shift); // no remainBmp needed, top level
    Node<Node> newRootNode = (nOld == null) ? new OneNode( i, nNew) : new BitmapNode<Node>(i, nNew, nOld);

    return new PSIntMap<V>(size + 1, newRootLevel, newRootNode, newStagingNode, newStagingNode, newSm);
  }  
  
  /**
   * that's ugly, but if we use recursion we need newElements result object to obtain the new stagingNode and
   * the size change, which means there would be an additional allocation per set() or newElements non-persistent,
   * transient object that would need synchronization
   */
  protected PSIntMap<V> setInCurrentRootLevel (int key, V value){
    Node<Node> n6=null, n5=null, n4=null, n3=null, n2=null, n1=null;
    Node<V> n0;
    int i6=0, i5=0, i4=0, i3=0, i2=0, i1=0, i0;
    int newSb = key | 0x1f;
    boolean needsMerge = (targetNode != stagingNode);
    int newSize = size+1;
    
    //--- new stagingNode
    Node<Node> n = rootNode;

    switch(rootLevel){
      case 6:
        i6 = key >>> 30;
        n5 = n.getElementAtLevelIndex(i6);
        if (n5 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n1 = new OneNode( (key >>> 5) & 0x1f, n0);
          n2 = new OneNode( (key >>> 10) & 0x1f, n1);
          n3 = new OneNode( (key >>> 15) & 0x1f, n2);
          n4 = new OneNode( (key >>> 20) & 0x1f, n3);
          n5 = new OneNode( (key >>> 25) & 0x1f, n4);
          n = n.cloneWithAdded( i6, n5);
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);
          
        } else {
          n6 = n;
          n = n5;
        }

      case 5:
        i5 = (key >>> 25) & 0x1f;
        n4 = n.getElementAtLevelIndex(i5);
        if (n4 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n1 = new OneNode( (key >>> 5) & 0x1f, n0);
          n2 = new OneNode( (key >>> 10) & 0x1f, n1);
          n3 = new OneNode( (key >>> 15) & 0x1f, n2);
          n4 = new OneNode( (key >>> 20) & 0x1f, n3);
          n = n.cloneWithAdded( i5, n4);

          if (n6 != null){
            n = n6.cloneWithReplaced( i6, n);
          }
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);

        } else {
          n5 = n;
          n = n4;
        }

      case 4:
        i4 = (key >>> 20) & 0x1f;
        n3 = n.getElementAtLevelIndex(i4);
        if (n3 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n1 = new OneNode( (key >>> 5) & 0x1f, n0);
          n2 = new OneNode( (key >>> 10) & 0x1f, n1);
          n3 = new OneNode( (key >>> 15) & 0x1f, n2);
          n = n.cloneWithAdded( i4, n3);

          if (n5 != null){
            n = n5.cloneWithReplaced( i5, n);
            if (n6 != null){ 
              n = n6.cloneWithReplaced( i6, n);
            }
          }
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);

        } else {
          n4 = n;
          n = n3;
        }
        
      case 3:
        i3 = (key >>> 15) & 0x1f;
        n2 = n.getElementAtLevelIndex(i3);
        if (n2 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n1 = new OneNode( (key >>> 5) & 0x1f, n0);
          n2 = new OneNode( (key >>> 10) & 0x1f, n1);
          n = n.cloneWithAdded( i3, n2);

          if (n4 != null){
            n = n4.cloneWithReplaced( i4, n);
            if (n5 != null){
              n = n5.cloneWithReplaced( i5, n);
              if (n6 != null){ 
                n = n6.cloneWithReplaced( i6, n);
              }
            }
          }
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);

        } else {
          n3 = n;
          n = n2;
        }
        
      case 2:
        i2 = (key >>> 10) & 0x1f;
        n1 = n.getElementAtLevelIndex(i2);
        if (n1 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n1 = new OneNode( (key >>> 5) & 0x1f, n0);
          n = n.cloneWithAdded( i2, n1);

          if (n3 != null){
            n = n3.cloneWithReplaced( i3, n);
            if (n4 != null){
              n = n4.cloneWithReplaced( i4, n);
              if (n5 != null){
                n = n5.cloneWithReplaced( i5, n);
                if (n6 != null){ 
                  n = n6.cloneWithReplaced( i6, n);
                }
              }
            }
          }
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);

        } else {
          n2 = n;
          n = n1;
        }
 
      case 1:
        i1 = (key >>> 5) & 0x1f;
        n0 = n.getElementAtLevelIndex(i1);
        if (n0 == null) {
          n0 = new OneNode( (key & 0x1f), value);
          n = n.cloneWithAdded( i1, n0);

          if (n2 != null){
            n = n2.cloneWithReplaced( i2, n);
            if (n3 != null){
              n = n3.cloneWithReplaced( i3, n);
              if (n4 != null){
                n = n4.cloneWithReplaced( i4, n);
                if (n5 != null){
                  n = n5.cloneWithReplaced( i5, n);
                  if (n6 != null){ 
                    n = n6.cloneWithReplaced( i6, n);
                  }
                }
              }
            }
          }
          if (needsMerge) mergeStagingNode(key, rootLevel, n);
          return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);

        } else {
          n1 = n;
          n = (Node)n0;
        }
 
      case 0: // finally the value level
        i0 = key & 0x1f;
        n0 = (Node<V>)n;
        if (n0 != null){
          if (n0.getElementAtLevelIndex(i0) == null) {
            n0 = n0.cloneWithAdded(i0, value);
          } else {
            n0 = n0.cloneWithReplaced(i0, value);
            newSize = size;
          }
        } else { // first node
          n0 = new OneNode( i0, value);
          newSize = 1;
        }
        
        n = (Node)n0;
        if (n1 != null){
          n = n1.cloneWithReplaced( i1, n);
          if (n2 != null){
            n = n2.cloneWithReplaced( i2, n);
            if (n3 != null){
              n = n3.cloneWithReplaced( i3, n);
              if (n4 != null){
                n = n4.cloneWithReplaced( i4, n);
                if (n5 != null){
                  n = n5.cloneWithReplaced( i5, n);
                  if (n6 != null){
                    n = n6.cloneWithReplaced( i6, n);
                  }
                }
              }
            }
          } 
        }
        if (needsMerge) mergeStagingNode( key, rootLevel, n);
        return new PSIntMap<V>( newSize, rootLevel, n, n0, n0, newSb);
    }
    
    return null; // can't get here
  }

  
  public void process (Processor<V> p){
    if (rootNode != null){
      if (targetNode == stagingNode){
        rootNode.process( rootLevel, null, null, p);
      } else {
        rootNode.process( rootLevel, targetNode, stagingNode, p);
      }
    }
  }
    
  final protected Node removeAllSatisfying (int level, Node node, Predicate<V> pred){
    if (level == 0){ // value level
      return ((Node<V>)node).removeAllSatisfying(pred);
      
    } else { // node level
      // it sucks not having stack arrays but we don't want to allocate for temporary results
      Node n0=null,n1=null,n2=null,n3=null,n4=null,n5=null,n6=null,n7=null,n8=null,n9=null,n10=null,
           n11=null,n12=null,n13=null,n14=null,n15=null,n16=null,n17=null,n18=null,n19=null,n20=null,
           n21=null,n22=null,n23=null,n24=null,n25=null,n26=null,n27=null,n28=null,n29=null,n30=null,n31=null;
      int nRemaining = 0, nChanged = 0;
      int len = node.getNumberOfElements();
      
      //--- collect the remaining nodes
      for (int i=0; i<len; i++){
        Node e = (Node)node.getElementAtStorageIndex(i);
        Node n = removeAllSatisfying( level-1, e, pred);
        if (n != null){
          nRemaining++;
          if (n != e){
            nChanged++;
          }
          switch (i){
            case  0:  n0=n; break;
            case  1:  n1=n; break;
            case  2:  n2=n; break;
            case  3:  n3=n; break;
            case  4:  n4=n; break;
            case  5:  n5=n; break;
            case  6:  n6=n; break;
            case  7:  n7=n; break;
            case  8:  n8=n; break;
            case  9:  n9=n; break;
            case 10: n10=n; break;
            case 11: n11=n; break;
            case 12: n12=n; break;
            case 13: n13=n; break;
            case 14: n14=n; break;
            case 15: n15=n; break;
            case 16: n16=n; break;
            case 17: n17=n; break;
            case 18: n18=n; break;
            case 19: n19=n; break;
            case 20: n20=n; break;
            case 21: n21=n; break;
            case 22: n22=n; break;
            case 23: n23=n; break;
            case 24: n24=n; break;
            case 25: n25=n; break;
            case 26: n26=n; break;
            case 27: n27=n; break;
            case 28: n28=n; break;
            case 29: n29=n; break;
            case 30: n30=n; break;
            case 31: n31=n; break;
          }
        }
      }
      
      //--- construct the returned node
      if (nRemaining == 0){
        return null;
        
      } else if ((nRemaining == len) && (nChanged == 0)){
        return node;
        
      } else {
        if (nRemaining == 1){ // becomes a OneNode
          for (int i=0; i<32; i++){
            switch (i){
              case  0:  if  (n0!=null) return new OneNode( node.storageToLevelIndex(0), n0); break;
              case  1:  if  (n1!=null) return new OneNode( node.storageToLevelIndex(1), n1); break;
              case  2:  if  (n2!=null) return new OneNode( node.storageToLevelIndex(2), n2); break;
              case  3:  if  (n3!=null) return new OneNode( node.storageToLevelIndex(3), n3); break;
              case  4:  if  (n4!=null) return new OneNode( node.storageToLevelIndex(4), n4); break;
              case  5:  if  (n5!=null) return new OneNode( node.storageToLevelIndex(5), n5); break;
              case  6:  if  (n6!=null) return new OneNode( node.storageToLevelIndex(6), n6); break;
              case  7:  if  (n7!=null) return new OneNode( node.storageToLevelIndex(7), n7); break;
              case  8:  if  (n8!=null) return new OneNode( node.storageToLevelIndex(8), n8); break;
              case  9:  if  (n9!=null) return new OneNode( node.storageToLevelIndex(9), n9); break;
              case 10:  if (n10!=null) return new OneNode( node.storageToLevelIndex(10),n10); break;
              case 11:  if (n11!=null) return new OneNode( node.storageToLevelIndex(11),n11); break;
              case 12:  if (n12!=null) return new OneNode( node.storageToLevelIndex(12),n12); break;
              case 13:  if (n13!=null) return new OneNode( node.storageToLevelIndex(13),n13); break;
              case 14:  if (n14!=null) return new OneNode( node.storageToLevelIndex(14),n14); break;
              case 15:  if (n15!=null) return new OneNode( node.storageToLevelIndex(15),n15); break;
              case 16:  if (n16!=null) return new OneNode( node.storageToLevelIndex(16),n16); break;
              case 17:  if (n17!=null) return new OneNode( node.storageToLevelIndex(17),n17); break;
              case 18:  if (n18!=null) return new OneNode( node.storageToLevelIndex(18),n18); break;
              case 19:  if (n19!=null) return new OneNode( node.storageToLevelIndex(19),n19); break;
              case 20:  if (n20!=null) return new OneNode( node.storageToLevelIndex(20),n20); break;
              case 21:  if (n21!=null) return new OneNode( node.storageToLevelIndex(21),n21); break;
              case 22:  if (n22!=null) return new OneNode( node.storageToLevelIndex(22),n22); break;
              case 23:  if (n23!=null) return new OneNode( node.storageToLevelIndex(23),n23); break;
              case 24:  if (n24!=null) return new OneNode( node.storageToLevelIndex(24),n24); break;
              case 25:  if (n25!=null) return new OneNode( node.storageToLevelIndex(25),n25); break;
              case 26:  if (n26!=null) return new OneNode( node.storageToLevelIndex(26),n26); break;
              case 27:  if (n27!=null) return new OneNode( node.storageToLevelIndex(27),n27); break;
              case 28:  if (n28!=null) return new OneNode( node.storageToLevelIndex(28),n28); break;
              case 29:  if (n29!=null) return new OneNode( node.storageToLevelIndex(29),n29); break;
              case 30:  if (n30!=null) return new OneNode( node.storageToLevelIndex(30),n30); break;
              case 31:  if (n31!=null) return new OneNode( node.storageToLevelIndex(31),n31); break;
            }
          }
          
        } else if (nRemaining == 32) { // still a FullNode, but elements might have changed
          Node[] a = {n0,n1,n2,n3,n4,n5,n6,n7,n8,n9,n10,
                      n11,n12,n13,n14,n15,n16,n17,n18,n19,n20,
                      n21,n22,n23,n24,n25,n26,n27,n28,n29,n30,n31};
          
          return new FullNode(a);
          
        } else {
          int bitmap = 0;
          Node[] a = new Node[nRemaining];
          int j=0;

          // <2do> this is bad - there has to be a more efficient way to generate the bitmap
          for (int i=0; j < nRemaining; i++){
            switch (i){
              case  0:  if  (n0!=null) { a[j++] =  n0; bitmap |= (1<<node.storageToLevelIndex(0)); } break;
              case  1:  if  (n1!=null) { a[j++] =  n1; bitmap |= (1<<node.storageToLevelIndex(1)); } break;
              case  2:  if  (n2!=null) { a[j++] =  n2; bitmap |= (1<<node.storageToLevelIndex(2)); } break;
              case  3:  if  (n3!=null) { a[j++] =  n3; bitmap |= (1<<node.storageToLevelIndex(3)); } break;
              case  4:  if  (n4!=null) { a[j++] =  n4; bitmap |= (1<<node.storageToLevelIndex(4)); } break;
              case  5:  if  (n5!=null) { a[j++] =  n5; bitmap |= (1<<node.storageToLevelIndex(5)); } break;
              case  6:  if  (n6!=null) { a[j++] =  n6; bitmap |= (1<<node.storageToLevelIndex(6)); } break;
              case  7:  if  (n7!=null) { a[j++] =  n7; bitmap |= (1<<node.storageToLevelIndex(7)); } break;
              case  8:  if  (n8!=null) { a[j++] =  n8; bitmap |= (1<<node.storageToLevelIndex(8)); } break;
              case  9:  if  (n9!=null) { a[j++] =  n9; bitmap |= (1<<node.storageToLevelIndex(9)); } break;
              case 10:  if (n10!=null) { a[j++] = n10; bitmap |= (1<<node.storageToLevelIndex(10)); } break;
              case 11:  if (n11!=null) { a[j++] = n11; bitmap |= (1<<node.storageToLevelIndex(11)); } break;
              case 12:  if (n12!=null) { a[j++] = n12; bitmap |= (1<<node.storageToLevelIndex(12)); } break;
              case 13:  if (n13!=null) { a[j++] = n13; bitmap |= (1<<node.storageToLevelIndex(13)); } break;
              case 14:  if (n14!=null) { a[j++] = n14; bitmap |= (1<<node.storageToLevelIndex(14)); } break;
              case 15:  if (n15!=null) { a[j++] = n15; bitmap |= (1<<node.storageToLevelIndex(15)); } break;
              case 16:  if (n16!=null) { a[j++] = n16; bitmap |= (1<<node.storageToLevelIndex(16)); } break;
              case 17:  if (n17!=null) { a[j++] = n17; bitmap |= (1<<node.storageToLevelIndex(17)); } break;
              case 18:  if (n18!=null) { a[j++] = n18; bitmap |= (1<<node.storageToLevelIndex(18)); } break;
              case 19:  if (n19!=null) { a[j++] = n19; bitmap |= (1<<node.storageToLevelIndex(19)); } break;
              case 20:  if (n20!=null) { a[j++] = n20; bitmap |= (1<<node.storageToLevelIndex(20)); } break;
              case 21:  if (n21!=null) { a[j++] = n21; bitmap |= (1<<node.storageToLevelIndex(21)); } break;
              case 22:  if (n22!=null) { a[j++] = n22; bitmap |= (1<<node.storageToLevelIndex(22)); } break;
              case 23:  if (n23!=null) { a[j++] = n23; bitmap |= (1<<node.storageToLevelIndex(23)); } break;
              case 24:  if (n24!=null) { a[j++] = n24; bitmap |= (1<<node.storageToLevelIndex(24)); } break;
              case 25:  if (n25!=null) { a[j++] = n25; bitmap |= (1<<node.storageToLevelIndex(25)); } break;
              case 26:  if (n26!=null) { a[j++] = n26; bitmap |= (1<<node.storageToLevelIndex(26)); } break;
              case 27:  if (n27!=null) { a[j++] = n27; bitmap |= (1<<node.storageToLevelIndex(27)); } break;
              case 28:  if (n28!=null) { a[j++] = n28; bitmap |= (1<<node.storageToLevelIndex(28)); } break;
              case 29:  if (n29!=null) { a[j++] = n29; bitmap |= (1<<node.storageToLevelIndex(29)); } break;
              case 30:  if (n30!=null) { a[j++] = n30; bitmap |= (1<<node.storageToLevelIndex(30)); } break;
              case 31:  if (n31!=null) { a[j++] = n31; bitmap |= (1<<node.storageToLevelIndex(31)); } break;
            }
          }
          
          return new BitmapNode( bitmap, a);
        }
      }
    }
    
    throw new RuntimeException("can't get here");
  }
  
  public PSIntMap<V> removeAllSatisfying( Predicate<V> pred){
    Node<Node> node = rootNode;
    
    if (stagingNode != targetNode){
      // we need to merge first since the target node might be gone after bulk removal
      node = mergeStagingNode();
    }
    node = removeAllSatisfying( rootLevel, node, pred);
    
    // reduce depth
    
    int newRootLevel = rootLevel;
    int newSize = countSize( newRootLevel, node);
    
    return new PSIntMap<V>( newSize, newRootLevel, node, null, null, 0);    
  }
  
  protected final int countSize (int level, Node node){
    if (node == null){
      return 0;
      
    } else {
      if (level == 0) {
        return node.getNumberOfElements();

      } else {
        int nValues = 0;
        int len = node.getNumberOfElements();
        for (int i = 0; i < len; i++) {
          nValues += countSize(level - 1, (Node) node.getElementAtStorageIndex(i));
        }
        return nValues;
      }
    }
  }
  
  public V[] values (){
    final Object[] values = new Object[size];
    Processor<V> flattener = new Processor<V>(){
      int i=0;
      @Override
	public void process (V v){
        values[i] = v;
      }
    };
    
    process(flattener);
    
    return (V[])values;
  }
  
  //--- debugging
  
  public void printOn(PrintStream ps) {
    if (rootNode != null) {
      rootNode.printNodeInfoOn(ps, targetNode, stagingNode);
      ps.println();
      rootNode.printOn(ps, rootLevel, targetNode, stagingNode);
    } else {
      ps.println( "empty");
    }

    if (stagingNode != null) {
      ps.println("--------------- staging");
      stagingNode.printNodeInfoOn(ps, targetNode, stagingNode);
      ps.println();
      stagingNode.printOn(ps, 0, targetNode, stagingNode);
    }
  }
  
  public String keyDescription (int key) {
    StringBuilder sb = new StringBuilder();
    int ish = getStartLevel(key);
    
    sb.append(key);
    sb.append(" (0x");
    sb.append(Integer.toHexString(key));
    sb.append(") => ");
    
    for (int shift=ish*5; shift>=0; shift-=5) {
      sb.append((key>>shift) & 0x1f);
      if (shift > 0) {
        sb.append('.');
      }
    }
    
    return sb.toString();
  }
  
}
