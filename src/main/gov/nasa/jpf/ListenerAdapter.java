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
package gov.nasa.jpf;

import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.VMListener;

/**
 * Adapter class that dummy implements both VMListener and SearchListener interfaces
 * Used to ease implementation of listeners that only process a few notifications
 */
public abstract class ListenerAdapter implements VMListener, SearchListener, PublisherExtension {

  //--- the VMListener interface
  @Override
  public void vmInitialized(VM vm) {}
  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {}
  @Override
  public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {}
  @Override
  public void threadStarted(VM vm, ThreadInfo startedThread) {}
  @Override
  public void threadWaiting (VM vm, ThreadInfo waitingThread) {}
  @Override
  public void threadNotified (VM vm, ThreadInfo notifiedThread) {}
  @Override
  public void threadInterrupted (VM vm, ThreadInfo interruptedThread) {}
  @Override
  public void threadScheduled (VM vm, ThreadInfo scheduledThread) {}
  @Override
  public void threadBlocked (VM vm, ThreadInfo blockedThread, ElementInfo lock) {}
  @Override
  public void threadTerminated(VM vm, ThreadInfo terminatedThread) {}
  @Override
  public void loadClass (VM vm, ClassFile cf) {}
  @Override
  public void classLoaded(VM vm, ClassInfo loadedClass) {}
  @Override
  public void objectCreated(VM vm, ThreadInfo currentThread, ElementInfo newObject) {}
  @Override
  public void objectReleased(VM vm, ThreadInfo currentThread, ElementInfo releasedObject) {}
  @Override
  public void objectLocked (VM vm, ThreadInfo currentThread, ElementInfo lockedObject) {}
  @Override
  public void objectUnlocked (VM vm, ThreadInfo currentThread, ElementInfo unlockedObject) {}
  @Override
  public void objectWait (VM vm, ThreadInfo currentThread, ElementInfo waitingObject) {}
  @Override
  public void objectNotify (VM vm, ThreadInfo currentThread, ElementInfo notifyingObject) {}
  @Override
  public void objectNotifyAll (VM vm, ThreadInfo currentThread, ElementInfo notifyingObject) {}
  @Override
  public void objectExposed (VM vm, ThreadInfo currentThread, ElementInfo fieldOwnerObject, ElementInfo exposedObject) {}
  @Override
  public void objectShared (VM vm, ThreadInfo currentThread, ElementInfo sharedObject) {}
  @Override
  public void gcBegin(VM vm) {}
  @Override
  public void gcEnd(VM vm) {}
  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {}
  @Override
  public void exceptionBailout(VM vm, ThreadInfo currentThread) {}
  @Override
  public void exceptionHandled(VM vm, ThreadInfo currentThread) {}
  @Override
  public void choiceGeneratorRegistered (VM vm, ChoiceGenerator<?> nextCG, ThreadInfo currentThread, Instruction executedInstruction) {}
  @Override
  public void choiceGeneratorSet (VM vm, ChoiceGenerator<?> newCG) {}
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {}
  @Override
  public void choiceGeneratorProcessed (VM vm, ChoiceGenerator<?> processedCG) {}
  @Override
  public void methodEntered (VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {}
  @Override
  public void methodExited (VM vm, ThreadInfo currentThread, MethodInfo exitedMethod) {}


  //--- the SearchListener interface
  @Override
  public void stateAdvanced(Search search) {}
  @Override
  public void stateProcessed(Search search) {}
  @Override
  public void stateBacktracked(Search search) {}
  @Override
  public void statePurged(Search search) {}
  @Override
  public void stateStored(Search search) {}
  @Override
  public void stateRestored(Search search) {}
  @Override
  public void searchProbed (Search search){}
  @Override
  public void propertyViolated(Search search) {}
  @Override
  public void searchStarted(Search search) {}
  @Override
  public void searchConstraintHit(Search search) {}
  @Override
  public void searchFinished(Search search) {}


  //--- PublisherExtension interface
  @Override
  public void publishStart (Publisher publisher) {}
  @Override
  public void publishTransition (Publisher publisher) {}
  @Override
  public void publishPropertyViolation (Publisher publisher) {}
  @Override
  public void publishConstraintHit (Publisher publisher) {}
  @Override
  public void publishFinished (Publisher publisher) {}
  @Override
  public void publishProbe (Publisher publisher) {}

}
