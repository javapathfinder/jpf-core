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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.util.ObjectList;

import java.util.Comparator;

/**
 * generic interface for configurable choice generators
 */
public interface ChoiceGenerator<T> extends Cloneable {

  //--- the basic ChoiceGenerator interface, mostly used by SystemState

  T getNextChoice();

  Class<T> getChoiceType();

  boolean hasMoreChoices();

  /**
   * to be called before the first advance(). Can be used in implementors to
   * initialize choices from context (similar to what listeners can do from
   * choiceGeneratorSet() notifications)
   */
  void setCurrent();
  
  /**
   * advance to the next choice. This is the only method that really
   * advances our enumeration
   */
  void advance();

  void advance(int nChoices);

  void select(int nChoice);

  boolean isDone();

  void setDone();

  boolean isProcessed();

  /**
   *  this has to reset the CG to its initial state, which includes resetting
   * 'isDone'
   */
  void reset();

  int getTotalNumberOfChoices();

  int getProcessedNumberOfChoices();

  
  // choice getters. Note that not all CGs need to support them since
  // there is no requirement that CGs compute finite choice sets upon creation
  
  T getChoice(int i);
  T[] getAllChoices();
  T[] getProcessedChoices();
  T[] getUnprocessedChoices();
  
  ChoiceGenerator<?> getPreviousChoiceGenerator();

  int getNumberOfParents();
  
  /**
   * turn the order of choices random (if it isn't already). Only
   * drawback of this generic method (which might be a decorator
   * factory) is that our type layer (e.g. IntChoiceGenerator)
   * has to guarantee type safety. But hey - this is the first case where
   * we can use covariant return types!
   *
   * NOTES:
   * - this method may alter this ChoiceGenerator and return that or return
   * a new "decorated" version.
   * - random data can be read from the "Random random" field in this class.
   */
  ChoiceGenerator<T> randomize();

  ChoiceGenerator<?> clone() throws CloneNotSupportedException;

  ChoiceGenerator<?> deepClone() throws CloneNotSupportedException; 
  
  String getId();

  int getIdRef();

  void setIdRef(int idRef);

  void setId(String id);

  boolean isSchedulingPoint();

  //--- the getters and setters for the CG creation info
  void setThreadInfo(ThreadInfo ti);

  ThreadInfo getThreadInfo();

  void setInsn(Instruction insn);

  Instruction getInsn();

  void setContext(ThreadInfo tiCreator);

  void setStateId (int stateId);
  
  int getStateId ();
  
  String getSourceLocation();

  boolean supportsReordering();
  
  /**
   * reorder according to a user provided comparator
   * @returns instance to reordered CG of same choice type, 
   * null if not supported by particular CG subclass
   * 
   * Note: this should only be called before the first advance, since it
   * can reset the CG enumeration status
   */
  ChoiceGenerator<T> reorder (Comparator<T> comparator);
  
  void setPreviousChoiceGenerator(ChoiceGenerator<?> cg);

  
  void setCascaded();

  boolean isCascaded();

  <T extends ChoiceGenerator<?>> T getPreviousChoiceGeneratorOfType(Class<T> cls);

  /**
   * returns the prev CG if it was registered for the same insn
   */
  ChoiceGenerator<?> getCascadedParent();

  /**
   * return array with all cascaded parents and this CG, in registration order
   */
  ChoiceGenerator<?>[] getCascade();

  /**
   * return array with all parents and this CG, in registration order
   */
  ChoiceGenerator<?>[] getAll();

  /**
   * return array with all CGs (including this one) of given 'cgType', in registration order
   */
  <C extends ChoiceGenerator<?>> C[] getAllOfType(Class<C> cgType);


  //--- the generic attribute API
  boolean hasAttr();

  boolean hasAttr(Class<?> attrType);

  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */
  Object getAttr();

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  void setAttr(Object a);

  void addAttr(Object a);

  void removeAttr(Object a);

  void replaceAttr(Object oldAttr, Object newAttr);

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  <A> A getAttr(Class<A> attrType);

  <A> A getNextAttr(Class<A> attrType, Object prev);

  ObjectList.Iterator attrIterator();

  <A> ObjectList.TypedIterator<A> attrIterator(Class<A> attrType);

}
