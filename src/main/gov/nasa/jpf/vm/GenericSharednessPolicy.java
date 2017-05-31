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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.SystemAttribute;
import gov.nasa.jpf.util.FieldSpecMatcher;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.MethodSpecMatcher;
import gov.nasa.jpf.util.TypeSpecMatcher;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

/**
 * an abstract SharednessPolicy implementation that makes use of both
 * shared field access CGs and exposure CGs.
 * 
 * This class is highly configurable, both in terms of using exposure CGs and filters.
 * The *never_break filters should be used with care to avoid missing defects, especially
 * the (transitive) method filters.
 * NOTE - the default settings from jpf-core/jpf.properties include several
 * java.util.concurrent* and java.lang.* fields/methods that can in fact contribute to
 * concurrency defects, esp. in SUTs that explicitly use Thread/ThreadGroup objects, in
 * which case they should be removed.
 * 
 * The *always_break field filter should only be used for white box SUT analysis if JPF
 * fails to detect sharedness (e.g. because no exposure is used). This should only
 * go into application property files
 */
public abstract class GenericSharednessPolicy implements SharednessPolicy, Attributor {
  
  //--- auxiliary types to configure filters
  static class NeverBreakIn implements SystemAttribute {
    static NeverBreakIn singleton = new NeverBreakIn();
  } 
  static class NeverBreakOn implements SystemAttribute {
    static NeverBreakOn singleton = new NeverBreakOn();
  } 
  static class AlwaysBreakOn implements SystemAttribute {
    static AlwaysBreakOn singleton = new AlwaysBreakOn();
  } 
  
  protected static JPFLogger logger = JPF.getLogger("shared");
  
  
  //--- options used for concurrent field access detection
  
  protected TypeSpecMatcher neverBreakOnTypes;
  
  protected TypeSpecMatcher alwaysBreakOnTypes;
  
  /**
   * never break or expose if a matching method is on the stack
   */
  protected MethodSpecMatcher neverBreakInMethods;
  
  /**
   * never break on matching fields 
   */  
  protected FieldSpecMatcher neverBreakOnFields;
    
  /**
   * always break matching fields, no matter if object is already shared or not
   */  
  protected FieldSpecMatcher alwaysBreakOnFields;
  

  /**
   * do we break on final field access 
   */
  protected boolean skipFinals;
  protected boolean skipConstructedFinals;
  protected boolean skipStaticFinals;
  
  /**
   * do we break inside of constructors
   * (note that 'this' references could leak from ctors, but
   * this is rather unusual)
   */
  protected boolean skipInits;

  /**
   * do we add CGs for objects that could become shared, e.g. when storing
   * a reference to a non-shared object in a shared object field.
   * NOTE: this is a conservative measure since we don't know yet at the
   * point of exposure if the object will ever be shared, which means it
   * can cause state explosion.
   */
  protected boolean breakOnExposure;
  
  /**
   * options to filter out lock protected field access, which is not
   * supposed to cause shared CGs
   * (this has no effect on exposure though)
   */
  protected boolean useSyncDetection;
  protected int lockThreshold;  
  
  protected VM vm;
  
  
  protected GenericSharednessPolicy (Config config){
    neverBreakInMethods = MethodSpecMatcher.create( config.getStringArray("vm.shared.never_break_methods"));
    
    neverBreakOnTypes = TypeSpecMatcher.create(config.getStringArray("vm.shared.never_break_types"));
    alwaysBreakOnTypes = TypeSpecMatcher.create(config.getStringArray("vm.shared.always_break_types"));
    
    neverBreakOnFields = FieldSpecMatcher.create( config.getStringArray("vm.shared.never_break_fields"));
    alwaysBreakOnFields = FieldSpecMatcher.create( config.getStringArray("vm.shared.always_break_fields"));
    
    skipFinals = config.getBoolean("vm.shared.skip_finals", true);
    skipConstructedFinals = config.getBoolean("vm.shared.skip_constructed_finals", false);
    skipStaticFinals = config.getBoolean("vm.shared.skip_static_finals", true);
    skipInits = config.getBoolean("vm.shared.skip_inits", true);
    
    breakOnExposure = config.getBoolean("vm.shared.break_on_exposure", true);
    
    useSyncDetection = config.getBoolean("vm.shared.sync_detection", true);
    lockThreshold = config.getInt("vm.shared.lockthreshold", 5);  
  }
  
  //--- internal methods (potentially overridden by subclass)
  
  
  //--- attribute management

  protected void setTypeAttributes (TypeSpecMatcher neverMatcher, TypeSpecMatcher alwaysMatcher, ClassInfo ciLoaded){
    // we flatten this for performance reasons
    for (ClassInfo ci = ciLoaded; ci!= null; ci = ci.getSuperClass()){
      if (alwaysMatcher != null && alwaysMatcher.matches(ci)){
        ciLoaded.addAttr(AlwaysBreakOn.singleton);
        return;
      }
      if (neverMatcher != null && neverMatcher.matches(ci)){
        ciLoaded.addAttr( NeverBreakOn.singleton);
        return;
      }
    }
  }
  
  protected void setFieldAttributes (FieldSpecMatcher neverMatcher, FieldSpecMatcher alwaysMatcher, ClassInfo ci){
    for (FieldInfo fi : ci.getDeclaredInstanceFields()) {
      // invisible fields (created by compiler)
      if (fi.getName().startsWith("this$")) {
        fi.addAttr( NeverBreakOn.singleton);
        continue;
      }        

      // configuration
      if (neverMatcher != null && neverMatcher.matches(fi)) {
        fi.addAttr( NeverBreakOn.singleton);
      }
      if (alwaysMatcher != null && alwaysMatcher.matches(fi)) {
        fi.addAttr( AlwaysBreakOn.singleton);
      }
      
      // annotation
      if (fi.hasAnnotation("gov.nasa.jpf.annotation.NeverBreak")){
        fi.addAttr( NeverBreakOn.singleton);        
      }
    }

    for (FieldInfo fi : ci.getDeclaredStaticFields()) {
      // invisible fields (created by compiler)
      if ("$assertionsDisabled".equals(fi.getName())) {
        fi.addAttr( NeverBreakOn.singleton);
        continue;
      }

      // configuration
      if (neverMatcher != null && neverMatcher.matches(fi)) {
        fi.addAttr( NeverBreakOn.singleton);
      }
      if (alwaysMatcher != null && alwaysMatcher.matches(fi)) {
        fi.addAttr( AlwaysBreakOn.singleton);
      }
      
      // annotation
      if (fi.hasAnnotation("gov.nasa.jpf.annotation.NeverBreak")){
        fi.addAttr( NeverBreakOn.singleton);        
      }
    }
  }
  
  protected boolean isInNeverBreakMethod (ThreadInfo ti){
    for (StackFrame frame = ti.getTopFrame(); frame != null; frame=frame.getPrevious()){
      MethodInfo mi = frame.getMethodInfo();
      if (mi.hasAttr( NeverBreakIn.class)){
        return true;
      }
    }

    return false;
  }
  
  protected abstract boolean checkOtherRunnables (ThreadInfo ti);
  
  // this needs a three-way return value, hence Boolean
  protected Boolean canHaveSharednessCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi){
    //--- thread
    if (ti.isFirstStepInsn()){ // no empty transitions
      return Boolean.FALSE;
    }
    
    if (!checkOtherRunnables(ti)){ // nothing to reschedule
      return Boolean.FALSE;
    }
    
    if (ti.hasAttr( NeverBreakIn.class)){
      return Boolean.FALSE;
    }
    
    //--- method
    if (isInNeverBreakMethod(ti)){
      return false;
    }
    
    //--- type
    ClassInfo ciFieldOwner = eiFieldOwner.getClassInfo();
    if (ciFieldOwner.hasAttr(NeverBreakOn.class)){
      return Boolean.FALSE;
    }
    if (ciFieldOwner.hasAttr(AlwaysBreakOn.class)){
      return Boolean.TRUE;
    }
    
    //--- field
    if (fi != null){
      if (fi.hasAttr(AlwaysBreakOn.class)) {
        return Boolean.TRUE;
      }
      if (fi.hasAttr(NeverBreakOn.class)) {
        return Boolean.FALSE;
      }
    }
    
    return null;    
  }

  //--- FieldLockInfo management
  
  /**
   * static attribute filters that determine if the check..Access() and check..Exposure() methods should be called.
   * This is only called once per instruction execution since it filters all cases that would set a CG.
   * Filter conditions have to apply to both field access and object exposure.
   */
  protected abstract FieldLockInfo createFieldLockInfo (ThreadInfo ti, ElementInfo ei, FieldInfo fi);

  
  /**
   * generic version of FieldLockInfo update, which relies on FieldLockInfo implementation to determine
   * if ElementInfo needs to be cloned
   */  
  protected ElementInfo updateFieldLockInfo (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    FieldLockInfo fli = ei.getFieldLockInfo(fi);
    if (fli == null){
      fli = createFieldLockInfo(ti, ei, fi);
      ei = ei.getModifiableInstance();
      ei.setFieldLockInfo(fi, fli);
      
    } else {
      FieldLockInfo newFli = fli.checkProtection(ti, ei, fi);
      if (newFli != fli) {
        ei = ei.getModifiableInstance();
        ei.setFieldLockInfo(fi,newFli);
      }
    }
    
    return ei;
  }
  
  
  //--- runnable computation & CG creation

  // NOTE - we don't schedule threads outside this process since field access if process local
  
  protected ThreadInfo[] getRunnables (ApplicationContext appCtx){
    return vm.getThreadList().getProcessTimeoutRunnables(appCtx);
  }
  
  protected ChoiceGenerator<ThreadInfo> getRunnableCG (String id, ThreadInfo tiCurrent){
    if (vm.getSystemState().isAtomic()){ // no CG if we are in a atomic section
      return null;
    }
    
    ThreadInfo[] choices = getRunnables(tiCurrent.getApplicationContext());
    if (choices.length <= 1){ // field access doesn't block, i.e. the current thread is always runnable
      return null;
    }
    
    return new ThreadChoiceFromSet( id, choices, true);
  }
  
  protected boolean setNextChoiceGenerator (ChoiceGenerator<ThreadInfo> cg){
    if (cg != null){
      return vm.getSystemState().setNextChoiceGenerator(cg); // listeners could still remove CGs
    }
    
    return false;
  }
  
  
  //--- internal policy methods that can be overridden by subclasses
  
  protected ElementInfo updateSharedness (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    ThreadInfoSet tis = ei.getReferencingThreads();
    ThreadInfoSet newTis = tis.add(ti);
    
    if (tis != newTis){
      ei = ei.getModifiableInstance();
      ei.setReferencingThreads(newTis);
    }
      
    // we only change from non-shared to shared
    if (newTis.isShared(ti, ei) && !ei.isShared() && !ei.isSharednessFrozen()) {
      ei = ei.getModifiableInstance();
      ei.setShared(ti, true);
    }

    if (ei.isShared() && fi != null){
      ei = updateFieldLockInfo(ti,ei,fi);
    }
    
    return ei;
  }

  protected boolean setsExposureCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed){
    if (breakOnExposure){
      ClassInfo ciExposed = eiExposed.getClassInfo();
      
      //--- exposed type
      if (ciExposed.hasAttr(NeverBreakOn.class)){
        return false;
      }      
      if (ciExposed.hasAttr(AlwaysBreakOn.class)){
        logger.info("type exposure CG setting field ", fi, " to ", eiExposed);
        return setNextChoiceGenerator(getRunnableCG("EXPOSE", ti));
      }        
        
      // we can't filter on immutability since the race subject could be a reference
      // that is exposed through the exposed object
      
      if (isInNeverBreakMethod(ti)){
        return false;
      }
      
      if (eiFieldOwner.isExposedOrShared() && isFirstExposure(eiFieldOwner, eiExposed)){        
        // don't check against the 'old' field value because this might get called after the field was already updated
        // we should solely depend on different object sharedness
        eiExposed = eiExposed.getExposedInstance(ti, eiFieldOwner);
        logger.info("exposure CG setting field ", fi, " to ", eiExposed);
        return setNextChoiceGenerator(getRunnableCG("EXPOSE", ti));
      }
    }

    return false;
  }

  protected boolean isFirstExposure (ElementInfo eiFieldOwner, ElementInfo eiExposed){
    if (!eiExposed.isImmutable()){
      if (!eiExposed.isExposedOrShared()) {
         return (eiFieldOwner.isExposedOrShared());
      }
    }
        
    return false;
  }

  
  //------------------------------------------------ Attributor interface
    
  /**
   * this can be used to initializeSharednessPolicy per-application mechanisms such as ClassInfo attribution
   */
  @Override
  public void initializeSharednessPolicy (VM vm, ApplicationContext appCtx){
    this.vm = vm;
    
    SystemClassLoaderInfo sysCl = appCtx.getSystemClassLoader();
    sysCl.addAttributor(this);
  }
  
  
  @Override
  public void setAttributes (ClassInfo ci){
    setTypeAttributes( neverBreakOnTypes, alwaysBreakOnTypes, ci);
    
    setFieldAttributes( neverBreakOnFields, alwaysBreakOnFields, ci);
    
    // this one is more expensive to iterate over and should be avoided
    if (neverBreakInMethods != null){
      for (MethodInfo mi : ci.getDeclaredMethods().values()){
        if (neverBreakInMethods.matches(mi)){
          mi.setAttr( NeverBreakIn.singleton);
        }
      }
    }
    
  }
    
  //------------------------------------------------ SharednessPolicy interface
  
  @Override
  public ElementInfo updateObjectSharedness (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    return updateSharedness(ti, ei, fi);
  }
  @Override
  public ElementInfo updateClassSharedness (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    return updateSharedness(ti, ei, fi);
  }
  @Override
  public ElementInfo updateArraySharedness (ThreadInfo ti, ElementInfo ei, int idx){
    // NOTE - we don't support per-element FieldLockInfos (yet)
    return updateSharedness(ti, ei, null);
  }

  
  /**
   * check to determine if call site, object/class attributes and thread execution state
   * could cause CGs. This is called before sharedness is updated, i.e. can be used to
   * filter objects/classes that should not be sharedness tracked
   */
  @Override
  public boolean canHaveSharedObjectCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi){
    Boolean ret = canHaveSharednessCG( ti, insn, eiFieldOwner, fi);
    if (ret != null){
      return ret;
    }
    
    if  (eiFieldOwner.isImmutable()){
      return false;
    }
    
    if (skipFinals && fi.isFinal()){
      return false;
    }
        
    //--- mixed (dynamic) attributes
    if (skipConstructedFinals && fi.isFinal() && eiFieldOwner.isConstructed()){
      return false;
    }
    
    if (skipInits && insn.getMethodInfo().isInit()){
      return false;
    }
    
    return true;
  }
  
  @Override
  public boolean canHaveSharedClassCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi){
    Boolean ret = canHaveSharednessCG( ti, insn, eiFieldOwner, fi);
    if (ret != null){
      return ret;
    }

    if  (eiFieldOwner.isImmutable()){
      return false;
    }
    
    if (skipStaticFinals && fi.isFinal()){
      return false;
    }

    // call site. This could be transitive, in which case it has to be dynamic and can't be moved to isRelevant..()
    MethodInfo mi = insn.getMethodInfo();
    if (mi.isClinit() && (fi.getClassInfo() == mi.getClassInfo())) {
      // clinits are all synchronized, so they are lock protected per se
      return false;
    }
    
    return true;
  }
  
  @Override
  public boolean canHaveSharedArrayCG (ThreadInfo ti, Instruction insn, ElementInfo eiArray, int idx){
    Boolean ret = canHaveSharednessCG( ti, insn, eiArray, null);
    if (ret != null){
      return ret;
    }

    // more array specific checks here
    
    return true;
  }
  
  
  /**
   * <2do> explain why not transitive
   * 
   * these are the public interfaces towards FieldInstructions. Callers have to be aware this will 
   * change the /referenced/ ElementInfo in case the respective object becomes exposed
   */
  @Override
  public boolean setsSharedObjectCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi){
    if (eiFieldOwner.getClassInfo().hasAttr(AlwaysBreakOn.class) ||
            (eiFieldOwner.isShared() && !eiFieldOwner.isLockProtected(fi))) {
      logger.info("CG accessing shared instance field ", fi);
      return setNextChoiceGenerator( getRunnableCG("SHARED_OBJECT", ti));
    }
    
    return false;
  }

  @Override
  public boolean setsSharedClassCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi){
    if (eiFieldOwner.getClassInfo().hasAttr(AlwaysBreakOn.class) ||
            (eiFieldOwner.isShared() && !eiFieldOwner.isLockProtected(fi))) {
      logger.info("CG accessing shared static field ", fi);
      return setNextChoiceGenerator( getRunnableCG("SHARED_CLASS", ti));
    }
    
    return false;
  }
  
  @Override
  public boolean setsSharedArrayCG (ThreadInfo ti, Instruction insn, ElementInfo eiArray, int index){
    if (eiArray.isShared()){
      // <2do> we should check lock protection for the whole array here
      logger.info("CG accessing shared array ", eiArray);
      return setNextChoiceGenerator( getRunnableCG("SHARED_ARRAY", ti));
    }
    
    return false;
  }

  
  //--- internal policy methods that can be overridden by subclasses
    
  protected boolean isRelevantStaticFieldAccess (ThreadInfo ti, Instruction insn, ElementInfo ei, FieldInfo fi){
    if (!ei.isShared()){
      return false;
    }
    
    if  (ei.isImmutable()){
      return false;
    }
    
    if (skipStaticFinals && fi.isFinal()){
      return false;
    }    
    
    if (!ti.hasOtherRunnables()){ // nothing to break for
      return false;
    }

    // call site. This could be transitive, in which case it has to be dynamic and can't be moved to isRelevant..()
    MethodInfo mi = insn.getMethodInfo();
    if (mi.isClinit() && (fi.getClassInfo() == mi.getClassInfo())) {
      // clinits are all synchronized, so they are lock protected per se
      return false;
    }
    
    return true;
  }

  
  protected boolean isRelevantArrayAccess (ThreadInfo ti, Instruction insn, ElementInfo ei, int index){
    // <2do> this is too simplistic, we should support filters for array objects
    
    if (!ti.hasOtherRunnables()){
      return false;
    }
    
    if (!ei.isShared()){
      return false;
    }
    
    if (ti.isFirstStepInsn()){ // we already did break
      return false;
    }

    return true;
  }
  
  //--- object exposure 

  // <2do> explain why not transitive
  
  @Override
  public boolean setsSharedObjectExposureCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed){
    return setsExposureCG(ti,insn,eiFieldOwner,fi,eiExposed);
  }

  @Override
  public boolean setsSharedClassExposureCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed){
    return setsExposureCG(ti,insn,eiFieldOwner,fi,eiExposed);
  }  

  // since exposure is about the object being exposed (the element), there is no separate setsSharedArrayExposureCG
  
  
  @Override
  public void cleanupThreadTermination(ThreadInfo ti) {
    // default action is to do nothing
  }

}
