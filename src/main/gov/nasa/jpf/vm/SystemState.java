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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.TypeSpecMatcher;
import gov.nasa.jpf.vm.choice.BreakGenerator;

import java.io.PrintWriter;
import java.util.LinkedHashMap;


/**
 * the class that encapsulates not only the current execution state of the VM
 * (the KernelState), but also the part of it's history that is required
 * by VM to backtrack, plus some potential annotations that can be used to
 * control the search (i.e. forward/backtrack calls)
 */
public class SystemState {

  /**
   * instances of this class are used to store the SystemState parts which are
   * subject to backtracking/state resetting. At some point, we might have
   * stripped SystemState down enough to just store the SystemState itself
   * (so far, we don't change it's identity, there is only one)
   * the KernelState is still stored separately (which seems to be another
   * anachronism)
   *
   * NOTE: this gets stored at the end of a transition, i.e. if we need a value
   * to be restored to it's transition entry state (like atomicLevel), we have
   * to do that explicitly. Alternatively we could create the Memento before
   * we start to enter the step, but then we have to update the nextCg in the
   * snapshot, since it's only set at the transition end (required for
   * restore(), i.e.  HeuristicSearches)
   * 
   * NOTE: the plain Memento doesn't deep copy the CGs, which means it can
   * only be used for depth first search, where the parent CG states are always
   * current if we encounter an error. If general state restoration is
   * required (where the parent CGs might have been changed at the time we
   * restore), we have to use a RestorableMemento
   * <2do> this separation is error prone and fragile. It depends on correct
   * ChoiceGenerator deepCopy() implementations and a separate state acquisition
   * for restorable states. Currently, the gate for this is VM.getRestorableState(),
   * but this could be bypassed.
   */
  static class Memento {
    ChoiceGenerator<?> curCg;  // the ChoiceGenerator for the current transition
    ChoiceGenerator<?> nextCg;
    int atomicLevel;
    ChoicePoint trace;
    ThreadInfo execThread;
    int id;              // the state id
    LinkedHashMap<Object,ClosedMemento> restorers;
    
    static protected ChoiceGenerator<?> cloneCG( ChoiceGenerator<?> cg){
      if (cg != null){
        try {
          return cg.deepClone();
        } catch (CloneNotSupportedException cnsx){
          throw new JPFException("clone failed: " + cg);          
        }
      } else {
        return null;
      }
    }
    
    Memento (SystemState ss) {
      nextCg = ss.nextCg;      
      curCg = ss.curCg;
      
      atomicLevel = ss.entryAtomicLevel; // store the value we had when we started the transition
      id = ss.id;
      execThread = ss.execThread;
      
      // we can just copy the reference since it is re-created in each transition
      restorers = ss.restorers;
    }

    /**
     * this one is used to restore to a state which will re-enter with the next choice
     * of the same CG, i.e. nextCG is reset
     */
    void backtrack (SystemState ss) {
      ss.nextCg = null; // this is important - the nextCG will be set by the next Transition      
      ss.curCg = curCg;
      
      ss.atomicLevel = atomicLevel;
      ss.id = id;
      ss.execThread = execThread;
      
      if (restorers != null){
        for (ClosedMemento r : restorers.values()){
          r.restore();
        }
      }
    }

    void restore (SystemState ss) {
      throw new JPFException("can't restore a SystemState.Memento that was created for backtracking");
      
      /**
      ss.nextCg = nextCg;
      ss.curCg = curCg;

      ss.atomicLevel = atomicLevel;
      ss.id = id;
      ss.execThread = execThread;
      **/
    }
  }
  
  /**
   * a Memento that can be restored, not just backtracked to. Be aware this can
   * be a lot more expensive since it has to deep copy CGs so that we have
   * the state of the parent CGs restored properly
   */
  static class RestorableMemento extends Memento {
    RestorableMemento (SystemState ss){
      super(ss);
      
      nextCg = cloneCG(nextCg);
      curCg = cloneCG( curCg);
    }
    
    @Override
	void backtrack (SystemState ss){
      super.backtrack(ss);
      ss.curCg = cloneCG(curCg);
    }
    
    /**
     * this one is used if we restore and then advance, i.e. it might change the CG on
     * the next advance (if nextCg was set)
     */
    @Override
	void restore (SystemState ss) {      
      // if we don't clone them on restore, it means we can only restore this memento once
      ss.nextCg = cloneCG(nextCg);
      ss.curCg = cloneCG(curCg);

      ss.atomicLevel = atomicLevel;
      ss.id = id;
      ss.execThread = execThread;
      
      if (restorers != null){
        for (ClosedMemento r : restorers.values()){
          r.restore();
        }
      }
    }  
  }

  int id;                   /** the state id */

  ChoiceGenerator<?> nextCg;   // the ChoiceGenerator for the next transition
  ChoiceGenerator<?>  curCg;   // the ChoiceGenerator used in the current transition
  ThreadInfo execThread;    // currently executing thread, reset by ThreadChoiceGenerators
  
  // on-demand list of optional restorers that run if we backtrack to this state
  // this is reset before each transition
  LinkedHashMap<Object,ClosedMemento> restorers;
  

  /** current execution state of the VM (stored separately by VM) */
  public KernelState ks;

  public Transition trail;      /** trace information */

  //--- attributes that can be explicitly set for a state

  boolean retainAttributes; // as long as this is set, we don't reset attributes

  //--- ignored and isNewState are imperative
  boolean isIgnored; // treat this as a matched state, i.e. backtrack
  boolean isForced;  // treat this as a new state

  //--- those are hints (e.g. for HeuristicSearches)
  boolean isInteresting;
  boolean isBoring;

  boolean isBlockedInAtomicSection;

  /** uncaught exception in current transition */
  public UncaughtException uncaughtException;

  /** set to true if garbage collection is necessary */
  boolean GCNeeded = false;

  // this is an optimization - long transitions can cause a lot of short-living
  // garbage, which in turn can slow down the system considerably (heap size)
  // by setting 'nAllocGCThreshold', we can do sync. on-the-fly gc when the
  // number of new allocs within a single transition exceeds this value
  int maxAllocGC;
  int nAlloc;

  /** NOTE: this has changed its meaning again. Now it once more is an
   * optimization that can be used by applications calling Verify.begin/endAtomic(),
   * but be aware of that it now reports a deadlock property violation in
   * case of a blocking op inside an atomic section
   * Data CGs however are now allowed to be inside atomic sections
   *
   * BEWARE - It is in the nature of atomic sections that they might loose paths that
   * are relevant. This is esp. true for Thread.start() within AS if the starter
   * runs to completion without further scheduling points (DiningPhil problem).
   */
  int atomicLevel;
  int entryAtomicLevel;

  /** do we want executed insns to be recorded */
  boolean recordSteps;

  /** CG types for which we extend transitions if the CG has only non-rescheduling single choices */
  TypeSpecMatcher extendTransitions;
  
  /**
   * Creates a new system state.
   */
  public SystemState (Config config, VM vm) {
    ks = new KernelState(config);
    id = StateSet.UNKNOWN_ID;

    Class<?>[] argTypes = { Config.class, VM.class, SystemState.class };

    // we can't yet initialize the trail until we have the start thread
    
    maxAllocGC = config.getInt("vm.max_alloc_gc", Integer.MAX_VALUE);
    if (maxAllocGC <= 0){
      maxAllocGC = Integer.MAX_VALUE;
    }

    extendTransitions = TypeSpecMatcher.create(config.getStringArray("vm.extend_transitions"));
    // recordSteps is set later by VM, first we need a reporter (which requires the VM)
  }

  protected SystemState() {
    // just for unit test mockups
  }

  public void setStartThread (ThreadInfo ti) {
    execThread = ti;
    trail = new Transition(nextCg, execThread);
  }

  public int getId () {
    return id;
  }

  public void setId (int newId) {
    id = newId;
    trail.setStateId(newId);
    
    if (nextCg != null){
      nextCg.setStateId(newId);
    }
  }

  public void recordSteps (boolean cond) {
    recordSteps = cond;
  }

  /**
   * use those with extreme care, it overrides scheduling choices
   */
  public void incAtomic () {
    atomicLevel++;
  }

  public void decAtomic () {
    if (atomicLevel > 0) {
      atomicLevel--;
    }
  }
  public void clearAtomic() {
    atomicLevel = 0;
  }

  public boolean isAtomic () {
    return (atomicLevel > 0);
  }

  public boolean isBlockedInAtomicSection() {
    return isBlockedInAtomicSection;
  }

  public void setBlockedInAtomicSection() {
    isBlockedInAtomicSection = true;
  }

  public Transition getTrail() {
    return trail;
  }

  public KernelState getKernelState() {
    return ks;
  }

  public Heap getHeap() {
    return ks.getHeap();
  }

  //--- these are the various choice generator retrievers

  /**
   * answer the ChoiceGenerator that is used in the current transition
   */
  public ChoiceGenerator<?> getChoiceGenerator () {
    return curCg;
  }

  public ChoiceGenerator<?> getChoiceGenerator (String id) {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getPreviousChoiceGenerator()){
      if (id.equals(cg.getId())){
        return cg;
      }
    }

    return null;
  }
  
  /**
   * return the whole stack of CGs of the current path
   */
  public ChoiceGenerator<?>[] getChoiceGenerators () {
    if (curCg != null){
      return curCg.getAll();
    } else {
      return null;
    }
  }

  public ChoiceGenerator<?> getLastChoiceGeneratorInThread (ThreadInfo ti){
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getPreviousChoiceGenerator()){
      if (cg.getThreadInfo() == ti){
        return cg;
      }
    }
    
    return null;
  }
  
  
  public <T extends ChoiceGenerator<?>> T[] getChoiceGeneratorsOfType (Class<T> cgType) {
    if (curCg != null){
      return curCg.getAllOfType(cgType);
    } else {
      return null;
    }
  }


  public <T extends ChoiceGenerator<?>> T getLastChoiceGeneratorOfType (Class<T> cgType) {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getPreviousChoiceGenerator()){
      if (cgType.isAssignableFrom(cg.getClass())) {
        return (T)cg;
      }
    }

    return null;
  }

  public <T> ChoiceGenerator<T> getLastChoiceGeneratorOfChoiceType (String id, Class<T> choiceType){
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getPreviousChoiceGenerator()){
      if ((id == null || id.equals(cg.getId())) && choiceType.isAssignableFrom(cg.getChoiceType())) {
        return (ChoiceGenerator<T>)cg;
      }
    }

    return null;    
  }

  
  public <T extends ChoiceGenerator<?>> T getCurrentChoiceGeneratorOfType (Class<T> cgType) {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getCascadedParent()){
      if (cgType.isAssignableFrom(cg.getClass())){
        return (T)cg;
      }
    }

    return null;
  }

  public <T extends ChoiceGenerator<?>> T getCurrentChoiceGenerator (String id, Class<T> cgType) {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getCascadedParent()){
      if (id.equals(cg.getId()) && cgType.isAssignableFrom(cg.getClass())){
        return (T)cg;
      }
    }

    return null;
  }
  
  public <T> ChoiceGenerator<T> getCurrentChoiceGeneratorForChoiceType (String id, Class<T> choiceType){
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getCascadedParent()){
      if ((id == null || id.equals(cg.getId())) && choiceType.isAssignableFrom(cg.getChoiceType())){
        return (ChoiceGenerator<T>)cg;
      }
    }

    return null;    
  }


  public ChoiceGenerator<?> getCurrentChoiceGenerator (String id) {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getCascadedParent()){
      if (id.equals(cg.getId())){
        return cg;
      }
    }

    return null;
  }

  public ChoiceGenerator<?> getCurrentChoiceGenerator (ChoiceGenerator<?> cgPrev) {
    if (cgPrev == null){
      return curCg;
    } else {
      return cgPrev.getCascadedParent();
    }
  }

  /**
   * this returns the most recently registered ThreadChoiceGenerator that is 
   * also a scheduling point, or 'null' if there is none in the list of current CGs
   */
  public ThreadChoiceGenerator getCurrentSchedulingPoint () {
    for (ChoiceGenerator<?> cg = curCg; cg != null; cg = cg.getCascadedParent()){
      if (cg instanceof ThreadChoiceGenerator){
        ThreadChoiceGenerator tcg = (ThreadChoiceGenerator)cg;
        if (tcg.isSchedulingPoint()){
          return tcg;
        }
      }
    }

    return null;
  }

  public ChoiceGenerator<?>[] getCurrentChoiceGenerators () {
    return curCg.getCascade();
  }

  
  public <T extends ChoiceGenerator<?>> T getInsnChoiceGeneratorOfType (Class<T> cgType, Instruction insn, ChoiceGenerator<?> cgPrev){
    ChoiceGenerator<?> cg = cgPrev != null ? cgPrev.getPreviousChoiceGenerator() : curCg;

    if (cg != null && cg.getInsn() == insn && cgType.isAssignableFrom(cg.getClass())){
      return (T)cg;
    }

    return null;
  }

  public ChoiceGenerator<?> getNextChoiceGenerator () {
    return nextCg;
  }

  /**
   * set the ChoiceGenerator to be used in the next transition
   * @return true if there is a nextCg set after registration and listener notification
   */
  public boolean setNextChoiceGenerator (ChoiceGenerator<?> cg) {
    if (isIgnored){
      // if this transition is already marked as ignored, we are not allowed
      // to set nextCg because 'isIgnored' results in a shortcut backtrack that
      // is not handed back to the Search (its solely in VM forward)
      return false;
    }

    if (cg != null){
      // first, check if we have to randomize it. Note this might change the CG
      // instance since some algorithmic CG types need to be transformed into
      // explicit choice lists
      if (ChoiceGeneratorBase.useRandomization()) {
        cg = cg.randomize();
      }

      // set its context (thread and insn)
      cg.setContext(execThread);

      // do we already have a nextCG, which means this one is a cascaded CG
      if (nextCg != null) {
        cg.setPreviousChoiceGenerator(nextCg);
        nextCg.setCascaded(); // note the last registered CG is NOT set cascaded

      } else {
        cg.setPreviousChoiceGenerator(curCg);
      }

      nextCg = cg;

      execThread.getVM().notifyChoiceGeneratorRegistered(cg, execThread); // <2do> we need a better way to get the vm
    }

    // a choiceGeneratorRegistered listener might have removed this CG
    return (nextCg != null);
  }

  public void setMandatoryNextChoiceGenerator (ChoiceGenerator<?> cg, String failMsg){
    if (!setNextChoiceGenerator(cg)){
      throw new JPFException(failMsg);
    }
  }

  /**
   * remove the current 'nextCg'
   * Note this has to be called in a loop if all cascaded CGs have to be removed 
   */
  public void removeNextChoiceGenerator (){
    if (nextCg != null){
      nextCg = nextCg.getPreviousChoiceGenerator();
    }
  }

  /**
   * remove the whole chain of currently registered nextCGs
   */
  public void removeAllNextChoiceGenerators(){
    while (nextCg != null){
      nextCg = nextCg.getPreviousChoiceGenerator();
    }
  }

  
  public Object getBacktrackData () {
    return new Memento(this);
  }

  public void backtrackTo (Object backtrackData) {
    ((Memento) backtrackData).backtrack( this);
  }

  public Object getRestoreData(){
    return new RestorableMemento(this);
  }
  
  public void restoreTo (Object backtrackData) {
    ((Memento) backtrackData).restore( this);
  }

  public void retainAttributes (boolean b){
    retainAttributes = b;
  }

  public boolean getRetainAttributes() {
    return retainAttributes;
  }

  /**
   * this can be called anywhere from within a transition, to revert it and
   * go on with the next choice. This is mostly used explicitly in the app
   * via Verify.ignoreIf(..)
   *
   * calling setIgnored() also breaks the current transition, i.e. no further
   * instructions are executed within this step
   */
  public void setIgnored (boolean b) {
    isIgnored = b;

    if (b){
      isForced = false; // mutually exclusive
    }
  }

  public boolean isIgnored () {
    return isIgnored;
  }

  public void setForced (boolean b){
    isForced = b;

    if (b){
      isIgnored = false; // mutually exclusive
    }
  }

  public boolean isForced () {
    return isForced;
  }

  public void setInteresting (boolean b) {
    isInteresting = b;

    if (b){
      isBoring = false;
    }
  }

  public boolean isInteresting () {
    return isInteresting;
  }

  public void setBoring (boolean b) {
    isBoring = b;

    if (b){
      isInteresting = false;
    }
  }

  public boolean isBoring () {
    return isBoring;
  }

  public boolean isInitState () {
    return (id == StateSet.UNKNOWN_ID);
  }

  public int getThreadCount () {
    return ks.threads.length();
  }

  public UncaughtException getUncaughtException () {
    return uncaughtException;
  }

  public void activateGC () {
    GCNeeded = true;
  }

  public boolean hasRestorer (Object key){
    if (restorers != null){
      return restorers.containsKey(key);
    }
    
    return false;
  }
  
  public ClosedMemento getRestorer( Object key){
    if (restorers != null){
      return restorers.get(key);
    }
    
    return null;    
  }
  
  /**
   * call the provided restorer each time we get back to this state
   * 
   * @param key usually the object this restorer encapsulates
   * @param restorer the ClosedMemento that restores the state of the object
   * it encapsulates once we backtrack/restore this program state
   * 
   * Note that restorers are called in the order of registration, but in
   * general it is not a good idea to depend on order since restorers can
   * be set from different locations (listeners, peers, instructions)
   */
  public void putRestorer (Object key, ClosedMemento restorer){
    if (restorers == null){
      restorers = new LinkedHashMap<Object,ClosedMemento>();
    }
    
    // we only support one restorer per target for now
    restorers.put(key,restorer);
  }
  
  public boolean gcIfNeeded () {
    boolean needed = false;
    if (GCNeeded) {
      ks.gc();
      GCNeeded = false;
      needed = true;
    }

    nAlloc = 0;
    return needed;
  }

  /**
   * check if number of allocations since last GC exceed the maxAllocGC
   * threshold, perform on-the-fly GC if yes. This is aimed at avoiding a lot
   * of short-living garbage in long transitions, which slows down the heap
   * exponentially
   */
  public void checkGC () {
    if (nAlloc++ > maxAllocGC){
      gcIfNeeded();
    }
  }


  void dumpThreadCG (ThreadChoiceGenerator cg) {
    PrintWriter pw = new PrintWriter(System.out, true);
    cg.printOn(pw);
    pw.flush();
  }

  /**
   * reset the SystemState and initialize the next CG. This gets called
   * *before* the restorer computes the KernelState snapshot, i.e. it is *not*
   * allowed to change anything in the program state. The reason for splitting
   * CG initialization from transition execution is to avoid KernelState storage
   * in case the initialization does not produce a next choice and we have to
   * backtrack.
   *
   * @see VM.forward()
   * 
   * @return 'true' if there is a next choice, i.e. a next transition to enter.
   * 'false' if there is no next choice and the system has to backtrack
   */
  public boolean initializeNextTransition(VM vm) {

    // set this before any choiceGeneratorSet or choiceGeneratorAdvanced
    // notification (which can override it)
    if (!retainAttributes){
      isIgnored = false;
      isForced = false;
      isInteresting = false;
      isBoring = false;
    }

    restorers = null;
    
    // 'nextCg' got set at the end of the previous transition (or a preceding
    // choiceGeneratorSet() notification).
    // Be aware of that 'nextCg' is only the *last* CG that was registered, i.e.
    // there can be any number of CGs between the previous 'curCg' and 'nextCg'
    // that were registered for the same insn.
    while (nextCg != null) {
      curCg = nextCg;
      nextCg = null;

      // these are hooks that can be used to do context specific CG initialization
      curCg.setCurrent();
      notifyChoiceGeneratorSet(vm, curCg);
    }

    assert (curCg != null) : "transition without choice generator";

    return advanceCurCg(vm);
  }

  /**
   * enter all instructions that constitute the next transition.
   *
   * Note this gets called *after* storing the KernelState, i.e. is allowed to
   * modify thread states and fields
   *
   * @see VM.forward()
   */
  public void executeNextTransition (VM vm){
     // do we have a thread context switch? (this sets execThread)
    setExecThread( vm);

    assert execThread.isRunnable() : "next transition thread not runnable: " + execThread.getStateDescription();

    trail = new Transition(curCg, execThread);
    entryAtomicLevel = atomicLevel; // store before we start to enter

    execThread.executeTransition(this);    
  }

  /**
   * check if we can extend the current transition without state storing/matching
   * This is useful for non-cascaded single choice CGs that do not cause
   * rescheduling. Such CGs are never backtracked to anyways (they are processed
   * on their first advance).
   * 
   * NOTE: this is on top of CG type specific optimizations that are controlled
   * by cg.break_single_choice (unset by default). If the respective CG creator
   * is single choice aware it might not create / register a CG in the first
   * place and we never get here. This is only called if somebody did create
   * and register a CG
   * 
   * note also that we don't eliminate BreakGenerators since their only purpose
   * in life is to explicitly cause transition breaks. We don't want to override
   * the override here.
   */
  protected boolean extendTransition (){
    ChoiceGenerator<?> ncg = nextCg;
    if (ncg != null){
      if (CheckExtendTransition.isMarked(ncg) ||
              ((extendTransitions != null) && extendTransitions.matches(ncg.getClass()))){
        if (ncg.getTotalNumberOfChoices() == 1 && !ncg.isCascaded()){
          if (ncg instanceof ThreadChoiceGenerator){
            if ((ncg instanceof BreakGenerator) || !((ThreadChoiceGenerator) ncg).contains(execThread)){
              return false;
            }
          }

          initializeNextTransition(execThread.getVM());
          return true;
        }
      }
    }
    
    return false;
  }
  
  protected void setExecThread( VM vm){
    ThreadChoiceGenerator tcg = getCurrentSchedulingPoint();
    if (tcg != null){
      ThreadInfo tiNext = tcg.getNextChoice();
      if (tiNext != execThread) {
        vm.notifyThreadScheduled(tiNext);
        execThread = tiNext;
      }
    }

    if (execThread.isTimeoutWaiting()) {
      execThread.setTimedOut();
    }
  }


  // the number of advanced choice generators in this step
  protected int nAdvancedCGs;

  protected void advance( VM vm, ChoiceGenerator<?> cg){
    while (true) {
      if (cg.hasMoreChoices()){
        cg.advance();
        isIgnored = false;
        vm.notifyChoiceGeneratorAdvanced(cg);
        
        if (!isIgnored){
          // this seems redundant, but the CG or the listener might actually skip choices,
          // in which case we can't execute the next transition.
          // NOTE - this causes backtracking
          // <2do> it's debatable if we should treat this as a processed CG
          if (cg.getNextChoice() != null){
            nAdvancedCGs++;
          }
          break;
        }
        
      } else {
        vm.notifyChoiceGeneratorProcessed(cg);
        break;
      }
    }
  }

  protected void advanceAllCascadedParents( VM vm, ChoiceGenerator<?> cg){
    ChoiceGenerator<?> parent = cg.getCascadedParent();
    if (parent != null){
      advanceAllCascadedParents(vm, parent);
    }
    advance(vm, cg);
  }

  protected boolean advanceCascadedParent (VM vm, ChoiceGenerator<?> cg){
    if (cg.hasMoreChoices()){
      advance(vm,cg);
      return true;

    } else {
      vm.notifyChoiceGeneratorProcessed(cg);

      ChoiceGenerator<?> parent = cg.getCascadedParent();
      if (parent != null){
        if (advanceCascadedParent(vm,parent)){
          cg.reset();
          advance(vm,cg);
          return true;
        }
      }
      return false;
    }
  }

  protected boolean advanceCurCg (VM vm){
    nAdvancedCGs = 0;

    ChoiceGenerator<?> cg = curCg;
    ChoiceGenerator<?> parent = cg.getCascadedParent();

    if (cg.hasMoreChoices()){
      // check if this is the first time, for which we also have to advance our parents
      if (parent != null && parent.getProcessedNumberOfChoices() == 0){
        advanceAllCascadedParents(vm,parent);
      }
      advance(vm, cg);

    } else { // this one is done, but how about our parents
      vm.notifyChoiceGeneratorProcessed(cg);

      if (parent != null){
        if (advanceCascadedParent(vm,parent)){
          cg.reset();
          advance(vm,cg);
        }
      }
    }

    return (nAdvancedCGs > 0);
  }



  protected void notifyChoiceGeneratorSet (VM vm, ChoiceGenerator<?> cg){
    ChoiceGenerator<?> parent = cg.getCascadedParent();
    if (parent != null) {
      notifyChoiceGeneratorSet(vm, parent);
    }
    vm.notifyChoiceGeneratorSet(cg); // notify top down
  }


  // this is called on every executeInstruction from the running thread
  public boolean breakTransition () {
    return ((nextCg != null) || isIgnored);
  }

  void recordExecutionStep (Instruction pc) {
    // this can require a lot of memory, so we should only store
    // executed insns if we have to
    if (recordSteps) {
      Step step = new Step(pc);
      trail.addStep( step);
    } else {
      trail.incStepCount();
    }
  }

  // the three primitive ops used from within VM.forward()


}

