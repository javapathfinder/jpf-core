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

/**
 * a generic scheduler with configured SyncPolicy and SharednesPolicy objects it delegates to
 */
public class DelegatingScheduler implements Scheduler {
  
  protected SyncPolicy syncPolicy;
  protected SharednessPolicy sharednessPolicy;
  
  
  public DelegatingScheduler (Config config){
    syncPolicy = config.getEssentialInstance("vm.scheduler.sync.class", SyncPolicy.class);
    sharednessPolicy = config.getEssentialInstance("vm.scheduler.sharedness.class", SharednessPolicy.class);
  }
  
  //--- Scheduler interface

  @Override
  public void initialize (VM vm, ApplicationContext appCtx) {
    syncPolicy.initializeSyncPolicy(vm, appCtx);
    sharednessPolicy.initializeSharednessPolicy(vm, appCtx);
  }

  
  //--- SyncPolicy interface
  
  @Override
  public void initializeSyncPolicy (VM vm, ApplicationContext appCtx) {
    syncPolicy.initializeSyncPolicy(vm, appCtx);
  }

  @Override
  public void initializeThreadSync (ThreadInfo tiCurrent, ThreadInfo tiNew) {
    syncPolicy.initializeThreadSync(tiCurrent, tiNew);
  }

  @Override
  public void setRootCG (){
    syncPolicy.setRootCG();
  }
  
  @Override
  public boolean setsBlockedThreadCG (ThreadInfo ti, ElementInfo ei) {
    return syncPolicy.setsBlockedThreadCG(ti, ei);
  }

  @Override
  public boolean setsLockAcquisitionCG (ThreadInfo ti, ElementInfo ei) {
    return syncPolicy.setsLockAcquisitionCG(ti, ei);
  }

  @Override
  public boolean setsLockReleaseCG (ThreadInfo ti, ElementInfo ei, boolean didUnblock) {
    return syncPolicy.setsLockReleaseCG(ti, ei, didUnblock);
  }

  @Override
  public boolean setsTerminationCG (ThreadInfo ti) {
    return syncPolicy.setsTerminationCG(ti);
  }

  @Override
  public boolean setsWaitCG (ThreadInfo ti, long timeout) {
    return syncPolicy.setsWaitCG(ti, timeout);
  }

  @Override
  public boolean setsNotifyCG (ThreadInfo ti, boolean didNotify) {
    return syncPolicy.setsNotifyCG(ti, didNotify);
  }

  @Override
  public boolean setsNotifyAllCG (ThreadInfo ti, boolean didNotify) {
    return syncPolicy.setsNotifyAllCG(ti, didNotify);
  }

  @Override
  public boolean setsStartCG (ThreadInfo tiCurrent, ThreadInfo tiStarted) {
    return syncPolicy.setsStartCG(tiCurrent, tiStarted);
  }

  @Override
  public boolean setsYieldCG (ThreadInfo ti) {
    return syncPolicy.setsYieldCG(ti);
  }

  @Override
  public boolean setsPriorityCG (ThreadInfo ti) {
    return syncPolicy.setsPriorityCG(ti);
  }

  
  @Override
  public boolean setsSleepCG (ThreadInfo ti, long millis, int nanos) {
    return syncPolicy.setsSleepCG(ti, millis, nanos);
  }

  @Override
  public boolean setsSuspendCG (ThreadInfo tiCurrent, ThreadInfo tiSuspended) {
    return syncPolicy.setsSuspendCG(tiCurrent, tiSuspended);
  }

  @Override
  public boolean setsResumeCG (ThreadInfo tiCurrent, ThreadInfo tiResumed) {
    return syncPolicy.setsResumeCG(tiCurrent, tiResumed);
  }

  @Override
  public boolean setsJoinCG (ThreadInfo tiCurrent, ThreadInfo tiJoin, long timeout) {
    return syncPolicy.setsJoinCG(tiCurrent, tiJoin, timeout);
  }

  @Override
  public boolean setsStopCG (ThreadInfo tiCurrent, ThreadInfo tiStopped) {
    return syncPolicy.setsStopCG(tiCurrent, tiStopped);
  }

  @Override
  public boolean setsInterruptCG (ThreadInfo tiCurrent, ThreadInfo tiInterrupted) {
    return syncPolicy.setsInterruptCG(tiCurrent, tiInterrupted);
  }

  @Override
  public boolean setsParkCG (ThreadInfo ti, boolean isAbsTime, long timeout) {
    return syncPolicy.setsParkCG(ti, isAbsTime, timeout);
  }

  @Override
  public boolean setsUnparkCG (ThreadInfo tiCurrent, ThreadInfo tiUnparked) {
    return syncPolicy.setsUnparkCG(tiCurrent, tiUnparked);
  }

  @Override
  public boolean setsBeginAtomicCG (ThreadInfo ti) {
    return syncPolicy.setsBeginAtomicCG(ti);
  }

  @Override
  public boolean setsEndAtomicCG (ThreadInfo ti) {
    return syncPolicy.setsEndAtomicCG(ti);
  }

  @Override
  public boolean setsRescheduleCG (ThreadInfo ti, String reason) {
    return syncPolicy.setsRescheduleCG(ti, reason);
  }

  @Override
  public boolean setsPostFinalizeCG (ThreadInfo tiFinalizer) {
    return syncPolicy.setsPostFinalizeCG(tiFinalizer);
  }

  
  //--- SharednessPolicy interface
  
  @Override
  public void initializeSharednessPolicy (VM vm, ApplicationContext appCtx) {
    sharednessPolicy.initializeSharednessPolicy( vm, appCtx);
  }

  @Override
  public void initializeObjectSharedness (ThreadInfo allocThread, DynamicElementInfo ei) {
    sharednessPolicy.initializeObjectSharedness(allocThread, ei);
  }

  @Override
  public void initializeClassSharedness (ThreadInfo allocThread, StaticElementInfo ei) {
    sharednessPolicy.initializeClassSharedness(allocThread, ei);
  }

  @Override
  public boolean canHaveSharedObjectCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
    return sharednessPolicy.canHaveSharedObjectCG(ti, insn, eiFieldOwner, fi);
  }

  @Override
  public ElementInfo updateObjectSharedness (ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
    return sharednessPolicy.updateObjectSharedness(ti, ei, fi);
  }

  @Override
  public boolean setsSharedObjectCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
    return sharednessPolicy.setsSharedObjectCG(ti, insn, eiFieldOwner, fi);
  }

  @Override
  public boolean canHaveSharedClassCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
    return sharednessPolicy.canHaveSharedClassCG(ti,insn,eiFieldOwner, fi);
  }

  @Override
  public ElementInfo updateClassSharedness (ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
    return sharednessPolicy.updateClassSharedness(ti, ei, fi);
  }

  @Override
  public boolean setsSharedClassCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi) {
    return sharednessPolicy.setsSharedClassCG(ti, insn, eiFieldOwner, fi);
  }

  @Override
  public boolean canHaveSharedArrayCG (ThreadInfo ti, Instruction insn, ElementInfo eiArray, int idx) {
    return sharednessPolicy.canHaveSharedArrayCG(ti, insn, eiArray, idx);
  }

  @Override
  public ElementInfo updateArraySharedness (ThreadInfo ti, ElementInfo eiArray, int idx) {
    return sharednessPolicy.updateArraySharedness(ti,eiArray,idx);
  }

  @Override
  public boolean setsSharedArrayCG (ThreadInfo ti, Instruction insn, ElementInfo eiArray, int idx) {
    return sharednessPolicy.setsSharedArrayCG(ti, insn, eiArray, idx);
  }

  @Override
  public boolean setsSharedObjectExposureCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed) {
    return sharednessPolicy.setsSharedObjectExposureCG(ti,insn,eiFieldOwner,fi,eiExposed);
  }

  @Override
  public boolean setsSharedClassExposureCG (ThreadInfo ti, Instruction insn, ElementInfo eiFieldOwner, FieldInfo fi, ElementInfo eiExposed) {
    return sharednessPolicy.setsSharedClassExposureCG(ti, insn, eiFieldOwner, fi, eiExposed);
  }

  @Override
  public void cleanupThreadTermination (ThreadInfo ti) {
    sharednessPolicy.cleanupThreadTermination(ti);
  }
}
