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
package gov.nasa.jpf.vm.serialize;


import gov.nasa.jpf.util.ArrayObjectQueue;
import gov.nasa.jpf.util.BitArray;
import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;
import gov.nasa.jpf.util.ObjectQueue;
import gov.nasa.jpf.util.Processor;
import gov.nasa.jpf.vm.AbstractSerializer;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeStateHolder;
import gov.nasa.jpf.vm.ReferenceProcessor;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.Statics;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;

import java.util.HashMap;
import java.util.List;


/**
 * serializer that can ignore marked fields and stackframes for state matching
 *
 * <2do> rework filter policies
 */
public class FilteringSerializer extends AbstractSerializer implements ReferenceProcessor, Processor<ElementInfo> {

  // indexed by method globalId
  final ObjVector<FramePolicy> methodCache = new ObjVector<FramePolicy>();

  //--- search global bitmask caches
  final HashMap<ClassInfo,FinalBitSet> instanceRefMasks = new HashMap<ClassInfo,FinalBitSet>();
  final HashMap<ClassInfo,FinalBitSet> staticRefMasks   = new HashMap<ClassInfo,FinalBitSet>();

  final HashMap<ClassInfo,FinalBitSet> instanceFilterMasks = new HashMap<ClassInfo,FinalBitSet>();
  final HashMap<ClassInfo,FinalBitSet> staticFilterMasks   = new HashMap<ClassInfo,FinalBitSet>();

  protected FilterConfiguration filter;

  protected transient IntVector buf = new IntVector(4096);

  // the reference queue for heap traversal
  protected ObjectQueue<ElementInfo> refQueue;
  
  Heap heap;


  @Override
  public void attach(VM vm) {
    super.attach(vm);
    
    filter = vm.getConfig().getInstance("filter.class", FilterConfiguration.class);
    if (filter == null) {
      filter = new DefaultFilterConfiguration();
    }
    filter.init(vm.getConfig());
  }

  protected FramePolicy getFramePolicy(MethodInfo mi) {
    FramePolicy p = null;

    int mid = mi.getGlobalId();
    if (mid >= 0){
      p = methodCache.get(mid);
    if (p == null) {
      p = filter.getFramePolicy(mi);
      methodCache.set(mid, p);
    }
    } else {
      p = filter.getFramePolicy(mi);
    }

    return p;
  }

  protected FinalBitSet getInstanceRefMask(ClassInfo ci) {
    FinalBitSet v = instanceRefMasks.get(ci);
    if (v == null) {
      BitArray b = new BitArray(ci.getInstanceDataSize());
      for (FieldInfo fi : filter.getMatchedInstanceFields(ci)) {
        if (fi.isReference()) {
          b.set(fi.getStorageOffset());
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      instanceRefMasks.put(ci, v);
    }
    return v;
  }

  protected FinalBitSet getStaticRefMask(ClassInfo ci) {
    FinalBitSet v = staticRefMasks.get(ci);
    if (v == null) {
      BitArray b = new BitArray(ci.getStaticDataSize());
      for (FieldInfo fi : filter.getMatchedStaticFields(ci)) {
        if (fi.isReference()) {
          b.set(fi.getStorageOffset());
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      staticRefMasks.put(ci, v);
    }
    return v;
  }

  protected FinalBitSet getInstanceFilterMask(ClassInfo ci) {
    FinalBitSet v = instanceFilterMasks.get(ci);
    if (v == null) {
      BitArray b = new BitArray(ci.getInstanceDataSize());
      b.setAll();
      for (FieldInfo fi : filter.getMatchedInstanceFields(ci)) {
        int start = fi.getStorageOffset();
        int end = start + fi.getStorageSize();
        for (int i = start; i < end; i++) {
          b.clear(i);
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      instanceFilterMasks.put(ci, v);
    }
    return v;
  }

  protected FinalBitSet getStaticFilterMask(ClassInfo ci) {
    FinalBitSet v = staticFilterMasks.get(ci);
    if (v == null) {
      BitArray b = new BitArray(ci.getStaticDataSize());
      b.setAll();
      for (FieldInfo fi : filter.getMatchedStaticFields(ci)) {
        int start = fi.getStorageOffset();
        int end = start + fi.getStorageSize();
        for (int i = start; i < end; i++) {
          b.clear(i);
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      staticFilterMasks.put(ci, v);
    }
    return v;
  }

  protected void initReferenceQueue() {
    // note - this assumes all heap objects are in an unmarked state, but this
    // is true if we enter outside the gc

    if (refQueue == null){
      refQueue = new ArrayObjectQueue<ElementInfo>();
    } else {
      refQueue.clear();
    }
  }


  //--- those are the methods that can be overridden by subclasses to implement abstractions

  // needs to be public because of ReferenceProcessor interface
  @Override
  public void processReference(int objref) {
    if (objref != MJIEnv.NULL) {
      ElementInfo ei = heap.get(objref);
      if (!ei.isMarked()) { // only add objects once
        ei.setMarked();
        refQueue.add(ei);
      }
    }

    buf.add(objref);
  }

  
  protected void processArrayFields (ArrayFields afields){
    buf.add(afields.arrayLength());

    if (afields.isReferenceArray()) {
      int[] values = afields.asReferenceArray();
      for (int i = 0; i < values.length; i++) {
        processReference(values[i]);
      }
    } else {
      afields.appendTo(buf);
    }
  }
    
  protected void processNamedFields (ClassInfo ci, Fields fields){
    FinalBitSet filtered = getInstanceFilterMask(ci);
    FinalBitSet refs = getInstanceRefMask(ci);

    // using a block operation probably doesn't buy us much here since
    // we would have to blank the filtered slots and then visit the
    // non-filtered reference slots, i.e. do two iterations over
    // the mask bit sets
    int[] values = fields.asFieldSlots();
    for (int i = 0; i < values.length; i++) {
      if (!filtered.get(i)) {
        int v = values[i];
        if (refs.get(i)) {
          processReference(v);
        } else {
          buf.add(v);
        }
      }
    }
  }

  // needs to be public because of ElementInfoProcessor interface
  // NOTE: we don't serialize the monitor state here since this is
  // redundant to the thread locking state (which we will do after the heap).
  // <2do> we don't strictly need the lockCount since this has to show in the
  // stack frames. However, we should probably add monitor serialization to
  // better support specialized subclasses
  @Override
  public void process (ElementInfo ei) {
    Fields fields = ei.getFields();
    ClassInfo ci = ei.getClassInfo();
    buf.add(ci.getUniqueId());

    if (fields instanceof ArrayFields) { // not filtered
      processArrayFields((ArrayFields)fields);

    } else { // named fields, filtered
      processNamedFields(ci, fields);
    }
  }
  
  protected void processReferenceQueue () {
    refQueue.process(this);
    
    // this sucks, but we can't do the 'isMarkedOrLive' trick used in gc here
    // because gc depends on live bit integrity, and we only mark non-filtered live
    // objects here, i.e. we can't just set the Heap liveBitValue subsequently.
    heap.unmarkAll();
  }

  protected void serializeStackFrames() {
    ThreadList tl = ks.getThreadList();

    for (ThreadInfo ti : tl) {
      if (ti.isAlive()) {
        serializeStackFrames(ti);
      }
    }
  }

  protected void serializeStackFrames(ThreadInfo ti){
    // we need to add the thread object itself as a root
    processReference( ti.getThreadObjectRef());
    
    for (StackFrame frame = ti.getTopFrame(); frame != null; frame = frame.getPrevious()){
      serializeFrame(frame);
    }
  }

  /** more generic, but less efficient because it can't use block operations
  protected void _serializeFrame(StackFrame frame){
    buf.add(frame.getMethodInfo().getGlobalId());
    buf.add(frame.getPC().getInstructionIndex());

    int len = frame.getTopPos()+1;
    buf.add(len);

    // this looks like something we can push into the frame
    int[] slots = frame.getSlots();
    for (int i = 0; i < len; i++) {
      if (frame.isReferenceSlot(i)) {
        processReference(slots[i]);
      } else {
        buf.add(slots[i]);
      }
    }
  }
  **/

  protected void serializeFrame(StackFrame frame){
    buf.add(frame.getMethodInfo().getGlobalId());

    // there can be (rare) cases where a listener sets a null nextPc in
    // a frame that is still on the stack
    Instruction pc = frame.getPC();
    if (pc != null){
      buf.add(pc.getInstructionIndex());
    } else {
      buf.add(-1);
    }

    int len = frame.getTopPos()+1;
    buf.add(len);

    int[] slots = frame.getSlots();
    buf.append(slots,0,len);

    frame.visitReferenceSlots(this);
  }

  // this is called after the heap got serialized, i.e. we should not use
  // processReference() anymore. 
  protected void serializeThreadState (ThreadInfo ti){
    
    buf.add( ti.getId());
    buf.add( ti.getState().ordinal());
    buf.add( ti.getStackDepth());
    
    //--- the lock state
    // NOTE: both lockRef and lockedObjects can only refer to live objects
    // which are already heap-processed at this point (i.e. have a valid 'sid'
    // in case we don't want to directly serialize the reference values)
    
    // the object we are waiting for 
    ElementInfo eiLock = ti.getLockObject();
    if (eiLock != null){
      buf.add(getSerializedReferenceValue( eiLock));
    }
    
    // the objects we hold locks for
    // NOTE: this should be independent of lockedObjects order, hence we
    // have to factor this out
    serializeLockedObjects( ti.getLockedObjects());
  }

  // NOTE: this should not be called before all live references have been processed
  protected int getSerializedReferenceValue (ElementInfo ei){
    return ei.getObjectRef();
  }
  
  protected void serializeLockedObjects(List<ElementInfo> lockedObjects){
    // lockedObjects are already a set since we don't have multiple entries
    // (that would just increase the lock count), but our serialization should
    // NOT produce different values depending on order of entry. We could achieve this by using
    // a canonical order (based on reference or sid values), but this would require
    // System.arraycopys and object allocation, which is too much overhead
    // given that the number of lockedObjects is small for all but the most
    // pathological systems under test. 
    // We could spend all day to compute the perfect order-independent hash function,
    // but since our StateSet isn't guaranteed to be collision free anyway, we
    // rather shoot for something that can be nicely JITed
    int n = lockedObjects.size();
    buf.add(n);
    
    if (n > 0){
      if (n == 1){ // no order involved
        buf.add( getSerializedReferenceValue( lockedObjects.get(0)));
        
      } else {
        // don't burn an iterator on this, 'n' is supposed to be small
        int h = (n << 16) + (n % 3);
        for (int i=0; i<n; i++){
          int rot = (getSerializedReferenceValue( lockedObjects.get(i))) % 31;
          h ^= (h << rot) | (h >>> (32 - rot)); // rotate left
        }        
        buf.add( h);
      }
    }
  }
  
  protected void serializeThreadStates (){
    ThreadList tl = ks.getThreadList();

    for (ThreadInfo ti : tl) {
      if (ti.isAlive()) {
        serializeThreadState(ti);
      }
    }    
  }
  
  protected void serializeClassLoaders(){
    buf.add(ks.classLoaders.size());

    for (ClassLoaderInfo cl : ks.classLoaders) {
      if(cl.isAlive()) {
        serializeStatics(cl.getStatics());
      }
    }
  }

  protected void serializeStatics(Statics statics){
    buf.add(statics.size());

    for (StaticElementInfo sei : statics.liveStatics()) {
      serializeClass(sei);
    }
  }

  protected void serializeClass (StaticElementInfo sei){
    buf.add(sei.getStatus());

    Fields fields = sei.getFields();
    ClassInfo ci = sei.getClassInfo();
    FinalBitSet filtered = getStaticFilterMask(ci);
    FinalBitSet refs = getStaticRefMask(ci);
    int max = ci.getStaticDataSize();
    for (int i = 0; i < max; i++) {
      if (!filtered.get(i)) {
        int v = fields.getIntValue(i);
        if (refs.get(i)) {
          processReference(v);
        } else {
          buf.add(v);
        }
      }
    }
  }
  
  protected void serializeNativeStateHolders(){
    for (NativeStateHolder nsh : nativeStateHolders){
      serializeNativeStateHolder(nsh);
    }
  }
  
  protected void serializeNativeStateHolder (NativeStateHolder nsh){
    buf.add(nsh.getHash());
  }
  
  //--- our main purpose in life

  @Override
  protected int[] computeStoringData() {

    buf.clear();
    heap = ks.getHeap();
    initReferenceQueue();

    //--- serialize all live objects and loaded classes
    serializeStackFrames();
    serializeClassLoaders();
    processReferenceQueue();
    
    //--- now serialize the thread states (which might refer to live objects)
    // we do this last because threads contain some internal references
    // (locked objects etc) that should NOT set the canonical reference serialization
    // values (if they are encountered before their first explicit heap reference)
    serializeThreadStates();

    //--- last is serialization of native state holders
    serializeNativeStateHolders();
    
    return buf.toArray();
  }

  protected void dumpData() {
    int n = buf.size();
    System.out.print("serialized data: [");
    for (int i=0; i<n; i++) {
      if (i>0) {
        System.out.print(',');
      }
      System.out.print(buf.get(i));
    }
    System.out.println(']');
  }
}