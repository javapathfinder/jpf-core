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
package gov.nasa.jpf.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.ConfigChangeListener;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.State;
import gov.nasa.jpf.report.Reporter;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.ThreadList;
import gov.nasa.jpf.vm.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code Search} abstract class is at the heart of all search classes. Even when it is not extended by child classes, it will
 * be embedded in the logic as an input for some of the methods. 
 * 
 * <p>The main purpose of the {@code Search} class is to track general search information such as depth, configured properties, 
 * errors, etc. as well as to define how the search algorithm functions. In its simplest form, a search algorithm can be defined 
 * using the abstract method {@code search} with a series of {@code forward} and {@code backtrack}. More complex search 
 * algorithms can also make use of the state storing functionality of the {@code Search} class, as well as ignoring states and
 * removing states from the search tree.
 * 
 * <p>Another very important aspect of the {@code Search} class is not only to implement search algorithms, but also to define
 * the information that describes the current search loop. This is crucial in order to enable the {@code SearchListener} and 
 * {@code SearchListenerAdapter} classes responsible for extracting crucial data from the search loop during its verification
 * efforts.
 */
public abstract class Search {
  
  /** The {@code JPFLogger} object assigned to the {@code gov.nasa.jpf.search} subsystem 
   * @see gov.nasa.jpf.util.JPFLogger
   */
  protected static JPFLogger log = JPF.getLogger("gov.nasa.jpf.search");
  
  /** The value of {@code currentError} will either be the error encountered during last
   *  transition or null if no error was encountered */
  protected Error currentError = null;
  
  /** A running list of all errors encountered during verification. The list will always 
   * contain the current and past values of {@code currentError}. Errors are set added
   * during verification in the {@code error}
   * method.
   * 
   * <p>{@code errors} will only hold one error before verification stops unless {@code getAllErrors}
   * is set to true
   * 
   * @see #error(Property, Path, ThreadList)
   * @see #getAllErrors
   */
  protected ArrayList<Error> errors = new ArrayList<Error>();

  /**  {@code depth} represents the current depth of the search tree. */
  protected int depth = 0;
  /** {@code vm} represents the virtual machine that the search algorithm will be traversing*/
  protected VM vm;

  /** A list of properties provided from the search configuration at the start of verification. Every property has a {@code check}
   * method that will be used during verification to tell if any of the properties have been violated.*/
  protected ArrayList<Property> properties;

  /** A property set by the user before verification. If {@code matchDepth} is true it will change the behavior of 
   * {@code isNewState()}.
   * @see #isNewState() */
  protected boolean matchDepth;
  /** A property set by the user to define what the minimum memory required to keep verifying is. The reason this is a non-zero
   * value is to ensure some memory is left over to output a legible message before ending execution instead of simply throwing
   * an {@code OutOfMemoryError}.
   * 
   * <p> The default value for {@code minFreeMemory} is 1024 << 10 bytes. */
  protected long    minFreeMemory;
  /** A property set by the user to define what the maximum depth allowed during verification is. 
   * 
   * <p>By default the value is set to {@code Integer.MAX_VALUE}*/
  protected int     depthLimit;
  
  /** A property set by the user to define whether to halt execution after encountering the first error during verification.
   * If {@code getAllErrors} is set to true, verification will not stop after the first error, and every possible error
   * during verification will be logged as a result. */
  protected boolean getAllErrors;

  /** A {@code String} set to contain details on what the constraint that caused verification to halt was. Will generally be 
   * used by the {@code searchContraintHit} methods in {@code SearchListeners}.
   * 
   * @see gov.nasa.jpf.search.SearchListener#searchConstraintHit(Search) */
  protected String lastSearchConstraint;

  /** {@code done} is a flag set during verification to notify the search loop when verification has finished. */
  protected boolean done = false;
  /** {@code doBacktrack} is a flag set during verification in order to request that the search loop backtracks
   * one state.*/
  protected boolean doBacktrack = false;

 /** {@code notifyProbeListeners} is a flag set during verification stating whether or not a probe has been 
  * requested. When a probe is requested, relevant {@code SearchListeners} will be notified via {@code checkAndResetProbeRequest}*/
  protected AtomicBoolean notifyProbeListeners = new AtomicBoolean(false);

  /** {@code listeners} is an array used to hold {@code SearchListener} objects. We keep them in a simple array to avoid
   creating objects on each notification */
  protected SearchListener[] listeners = new SearchListener[0];

  /** {@code reporter} is a special listener that is always notified last in order to ensure all other listeners have been notified
   * beforehand. {@code reporter} is used to report information about the search loop to whichever output method the user has 
   * specified.
   * 
   *  <p>By default, the output method is to console.*/
  protected Reporter reporter;

  /** {@code config} is an object passed in during instantiation of the {@code Search} object and it specifies the properties and 
   * configurations the search loop should run under. The value behind {@code config} is only ever used once and stored within this
   * object.*/
  protected final Config config;

  /**
   * {@code ConfigListener} is an implementation of the interface {@code ConfigChangeListener} and is used to subscribe to changes 
   * that occur to the configuration of JPF in order to update the {@code config} object. While highly useful, one should always
   * remember to unregister the {@code config} object from each {@code ConfigListener} once it is no longer needed. Failure to
   * do this will cause massive memory leaks to begin piling up as a result of the same {@code Config} object being used across
   * several JPF executions. 
   *
   */
  class ConfigListener implements ConfigChangeListener {

    @Override
    public void propertyChanged(Config config, String key, String oldValue, String newValue) {
      // Different Config instance
      if (!config.equals(Search.this.config)) {
        return;
      }

      // Check if Search configuration changed
      if (key.startsWith("search.")){
        String k = key.substring(7);
        if ("match_depth".equals(k) ||
            "min_free".equals(k) ||
            "multiple_errors".equals(k)){
          initialize(config);
        }
      }
    }
    
    @Override
    public void jpfRunTerminated (Config config){
      config.removeChangeListener(this);
    }
  }
  
  /** {@code stateDepth} is an int vector storage system (akin to a map) that is responsible for associating states with their 
   * corresponding depths. 
   * 
   * <p>{@code stateDepth} maps the state id of every state to the corresponding depth, in a one to one mapping.*/
  protected final IntVector stateDepth = new IntVector();

  /** 
   * Constructs a {@code Search} object using the configuration specified by the user in {@code config} and a virtual machine
   * that will be used for verification.
   * 
   *  @param config The configuration and properties that the search loop will be required to commence verification under
   *  @param vm The virtual machine that the search loop will traverse and verify states from
   */
  protected Search (Config config, VM vm) {
    this.vm = vm;
    this.config = config;

    initialize( config);

    properties = getProperties(config);
    if (properties.isEmpty()) {
      log.severe("no property");
    }
    
    config.addChangeListener( new ConfigListener());
  }

  /**
   * Initializes the properties that the search loop will need to run under in order to make sure that each iteration of the search
   * loop is in compliance with the properties set by JPF and the user.
   * 
   * @param conf The configuration object ({@code config}) that contains the necessary information to initialize the property values
   */
  protected void initialize( Config conf){
    depthLimit = conf.getInt("search.depth_limit", Integer.MAX_VALUE);
    matchDepth = conf.getBoolean("search.match_depth");
    minFreeMemory = conf.getMemorySize("search.min_free", 1024<<10);    
    getAllErrors = conf.getBoolean("search.multiple_errors");
  }
  
  /**
   * Called after the JPF run is finished. Should not be public, but is called by JPF
   */
  public void cleanUp(){
    // nothing here, the ConfigListener removes itself
  }
  
  /** 
   * Returns the {@code config} object that is used by the {@code Search} class.
   * 
   * @return The configuration object that is used for verification
   */
  public Config getConfig() {
    return config;
  }
  
  /**
   * One of the most important methods in the {@code Search} class. {@code search} is used to specify what the algorithm and behavior 
   * for verification are. When the term "search loop" is brought up in other documentation, it generally means the loop that is used
   * running in the {@code search} method.
   */
  public abstract void search ();

  /**
   * Sets the {@code reporter} object used during verification to the specified {@code reporter}.
   * 
   * @param reporter The reporter used to replace the current reporter object in the {@code Search} class
   */
  public void setReporter(Reporter reporter){
    this.reporter = reporter;
  }

  /**
   * Appends a {@code SearchListener} object to the {@code listener} array. In addition to appending the search listener, the action is also logged
   * to the JPFLog {@code log} used in the {@code Search} class.
   * 
   * @param newListener A new {@code SearchListener} to append to the {@code listener} array
   */
  public void addListener (SearchListener newListener) {
    log.info("SearchListener added: ", newListener);
    listeners = Misc.appendElement(listeners, newListener);
  }

  /**
   * Checks whether a listener of the same type as {@code listenerCls} exists in {@code listeners} already.
   * 
   * @param listenerCls The object type to check {@code listeners} against
   * @return true if an element of the same type as the input parameter exists in {@code listeners}. False otherwise
   */
  public boolean hasListenerOfType (Class<?> listenerCls) {
    return Misc.hasElementOfType(listeners, listenerCls);
  }
  
  /**
   * Returns the next element in {@code listeners} after the element matching {@code prev} that is of the same type as {@code type}.
   * 
   * @param type The type of element to search for and return
   * @param prev The previous element to begin the search from
   * @return An element of the same class as {@code type} and coming after {@code prev} in the {@code listeners} array
   */
  public <T> T getNextListenerOfType(Class<T> type, T prev){
    return Misc.getNextElementOfType(listeners, type, prev);
  }

  /**
   * Remove the specified {@code SearchListener} from the {@code listeners} array.
   * 
   * @param removeListener The element to remove from the {@code listeners} array
   */
  public void removeListener (SearchListener removeListener) {
    listeners = Misc.removeElement(listeners, removeListener);
  }

  /**
   * Add a new property to {@code properties}
   * 
   * @param newProperty The property to add to {@code properties}
   */
  public void addProperty (Property newProperty) {
    properties.add(newProperty);
  }

  /**
   * Remove the specified property from {@code properties}
   * 
   * @param oldProperty The property to remove from {@code properties}
   */
  public void removeProperty (Property oldProperty) {
     properties.remove(oldProperty);
  }

  /**
   * Returns the list of configured properties.
   * 
   * <p>Note there is a name clash here - JPF 'properties' have nothing to do with Java properties (java.util.Properties)
   * 
   * @param config The config object to retrieve the properties list from
   * @return The list of configured properties
   */
  protected ArrayList<Property> getProperties (Config config) {
    Class<?>[] argTypes = { Config.class, Search.class };
    Object[] args = { config, this };

    ArrayList<Property> list = config.getInstances("search.properties", Property.class,
                                                   argTypes, args);

    return list;
  }

  /**
   * Check for property violations and return true if a property is violated and the search loop has finished running (i.e. {@code done} 
   * is set to true). Returns false otherwise.
   * 
   * @return true if an a property has been violated (i.e. {@code currentError} is not null) and {@code done} is true
   */
  protected boolean hasPropertyTermination () {
    if (currentError != null){
      if (done){
        return true;
      } else { // we search for multiple errors, so we ignore and go on
        doBacktrack = true;
      }
    }

    return false;
  }

  /**
   * Iterates through {@code properties} and checks for property violations. {@code checkPropertyViolation} should only be 
   * called once per transition to avoid it adding the same error every time it is called.
   * 
   * @return true if a property violation is found, false otherwise
   */
  protected boolean checkPropertyViolation () {
    for (Property p : properties) {
      if (!p.check(this, vm)) {
        error(p, vm.getClonedPath(), vm.getThreadList());
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the list of errors encountered during verification
   * 
   * @return The list of errors encountered during verification
   */
  public List<Error> getErrors () {
    return errors;
  }

  /**
   * Returns the number of errors encountered during verification. Will always return 1 if {@code getAllErrors} is false
   * 
   * @return The number of errors encountered during verification
   */
  public int getNumberOfErrors(){
    return errors.size();
  }

  /**
   * Returns the search constraint that was most recently encountered
   * 
   * @return A String detailing the search constraint most recently hit or null if none was encountered
   */
  public String getLastSearchConstraint() {
    return lastSearchConstraint;
  }

  /**
   * Request a probe
   * 
   * <p>This does not perform the actual listener notification, it only stores
   * the request, which is then processed from within JPFs inner execution loop.
   * As a consequence, {@code probeSearch} can be called asynchronously, and {@code searchProbed} listeners
   * don't have to bother with synchronization or inconsistent JPF states (notification 
   * happens from within JPFs main thread after a completed Instruction execution)
   */
  public void probeSearch(){
    notifyProbeListeners.set(true);
  }
  
  /**
   * Performs the actual notification and resets the request, hence this call should only happen from within JPFs main thread
   */
  public void checkAndResetProbeRequest(){
    if (notifyProbeListeners.compareAndSet(true, false)){
      notifySearchProbed();
    }
  }
  
  /**
   * Returns the most recent error encountered during the last transition
   * 
   * @return The error encountered during the last transition or null if none was encountered
   */
  public Error getCurrentError(){
    return currentError;
  }

  /**
   * Returns the most recent error encountered in the past
   * 
   * @return The most recent error encountered in the past or none if no error has been encountered during verification
   */
  public Error getLastError() {
    int i=errors.size()-1;
    if (i >=0) {
      return errors.get(i);
    } else {
      return null;
    }
  }

  /**
   * Returns whether or not the search loop has encountered an error yet
   * 
   * @return true if an error has been encountered, false otherwise
   */
  public boolean hasErrors(){
    return !errors.isEmpty();
  }

  /**
   * Returns the {@code vm} object used by the search loop
   * 
   * @return the {@code vm} object used by {@code Search}
   */
  public VM getVM() {
    return vm;
  }

  /**
   * Returns true if the search loop has transitioned into an end state in the virtual machine
   * 
   * @return true if the current state of the {@code vm} is an end state
   */
  public boolean isEndState () {
    return vm.isEndState();
  }

  /**
   * Returns if an error has been encountered during the transition into the current state
   * 
   * @return true if an error has been encountered during the most recent transition, false otherwise
   */
  public boolean isErrorState(){
    return (currentError != null);
  }

  /**
   * Returns whether of not the current state is an end state in the virtual machine
   * 
   * @return true if this is not an end state of the {@code vm}, false otherwise
   */
  public boolean hasNextState () {
    return !isEndState();
  }

  /**
   * Returns whether a transition has occurred from the previous state or not
   * 
   * @return true if a transition has occurred, false otherwise
   */
  public boolean transitionOccurred(){
    return vm.transitionOccurred();
  }

  /**
   * Returns whether the current state in the search loop is a new state and false otherwise. 
   * 
   * <p>However, if {@code matchDepth} is set to true,
   * then it  will true if the state is new or if the states depth is less than the previously recorded depth. If the state is new, the states
   * depth will also be recorded for future use.
   * 
   * @return true if the current state is a new state and false otherwise (behaviour changes if {@code matchDepth} is true
   */
  public boolean isNewState () {
    boolean isNew = vm.isNewState();

    if (matchDepth) {
      int id = vm.getStateId();

      if (isNew) {
        setStateDepth(id, depth);
      } else {
        return depth < getStateDepth(id);
      }
    }

    return isNew;
  }

  /**
   * Returns whether the current state has been visited yet. The opposite of {@code isNewState}
   * 
   * @return true if the state has been visited, false otherwise (behavior changes if {@code matchDepth} is true)
   * @see #isNewState()
   */
  public boolean isVisitedState () {
    return !isNewState();
  }

  /**
   * Returns whether the current state is an ignored state in the virtual machine
   * 
   * @return true if the current state is an ignored state, false otherwise
   */
  public boolean isIgnoredState(){
    return vm.isIgnoredState();
  }

  /**
   * Returns whether the current state has been fully explored by the search loop.
   * 
   * @return true if the current state has been fully explored and processed, false otherwise
   */
  public boolean isProcessedState(){
    return vm.getChoiceGenerator().isProcessed();
  }

  /**
   * Return whether the search loop has finished verification
   * 
   * @return true if the search loop has finished verification, false otherwise
   */
  public boolean isDone(){
    return done;
  }

  /**
   * Return the current depth of the search loop
   * 
   * @return The current depth inside the search tree.
   */
  public int getDepth () {
    return depth;
  }

  /**
   * Returns the most recent search constraint that was encountered
   * 
   * @return A String detailing the most recent search constraint that was encountered
   */
  public String getSearchConstraint () {
    return lastSearchConstraint;
  }

  /**
   * Returns the most recent transition that has occurred
   * 
   * @return The last transition that occurred during verification, or null if none has occurred
   */
  public Transition getTransition () {
    return vm.getLastTransition();
  }

  /**
   * Returns the state id of the current state
   * 
   * @return The state id of the current state
   */
  public int getStateId () {
    return vm.getStateId();
  }

  /**
   * Returns the purged states id
   * 
   * <p>Note that while it should return the purged states id, it currently only returns -1 as a 
   * result of many searches not utilizing this functionality. If it is required, {@code getPurgedStateId}
   * should be overridden and changed in subsequent child classes.
   * 
   * @return The purged states id (currently only returns -1)
   */
  public int getPurgedStateId () {
    return -1; // a lot of Searches don't purge any states
  }


  /**
   * Requests that the search loop backtracks one step
   * 
   * <p>This is somewhat redundant to {@code SystemState.setIgnored}, but we don't
   * want to mix the case of overriding state matching with backtracking when
   * searching for multiple errors.
   * 
   * @return The value of {@code doBacktrack} after is has been set to true (i.e. always true)
   */
  public boolean requestBacktrack () {
    return doBacktrack = true;
  }

  /**
   * Returns the value of {@code doBacktrack} as well as resetting {@code doBacktrack} to false
   * 
   * @return the value of {@code doBackTrack}
   */
  protected boolean checkAndResetBacktrackRequest() {
    if (doBacktrack){
      doBacktrack = false;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns whether the search algorithm supports backtracking or not
   * 
   * @return true if the search algorithm supports backtracking, false otherwise (by default the return
   * is true unless overridden)
   */
  public boolean supportsBacktrack () {
    return true;
  }

  /**
   * Returns whether the search algorithm supports restoring states that have been stored (a useful method in 
   * BreadthFirstSearch)
   * 
   * @return true if the search algorithm supports restoring states, false otherwise (by default the return
   * is false as the function is unsupported)
   */
  public boolean supportsRestoreState () {
    // not supported by default
    return false;
  }

  /**
   * Returns the depth limit set at the beginning of verification
   * 
   * @return The {@code depthLimit} property
   */
  public int getDepthLimit () {
    return depthLimit;
  }
  
  /**
   * Sets a new value for the {@code depthLimit} property
   * 
   * @param limit The new limit to set {@code depthLimit} to
   */
  public void setDepthLimit(int limit){
    depthLimit = limit;
  }

  /**
   * Returns a {@code SearchState} object with information describing the current state in the search loop
   * 
   * @return A {@code SearchState} object with information about the current state
   */
  protected SearchState getSearchState () {
    return new SearchState(this);
  }

  /**
   * Creates a new error with the corresponding {@code Property} that causes the error. Does not provide a {@code Path} or {@code ThreadList}, and
   * can therefore be used by {@code SearchListeners} to create path-less errors to ensure liveness. 
   * 
   * @param property the property that caused the error
   * @see #error(Property, Path, ThreadList)
   */
  public void error (Property property) {
    error(property, null, null);
  }

  /**
   * Creates an error with the corresponding {@code Property} that causes the error, alongside the {@code Path}, and {@code ThreadList}.
   * 
   * <p>Will set {@code done} to true and halt the search loop  if {@code getAllErrors} is false. If {@code getAllErrors} is true, it will 
   * clone the {@code Property}, {@code Path}, and {@code ThreadList} objects that were passed in (as not cloning them may cause subsequent
   * operations to overwrite information still in use) and add them to the {@code errors} list
   * and continue verification.
   * 
   * <p>The property that caused the error is not reset here as the listeners attached to the search should be notified of the error first.
   * This becomes especially problematic if one of the listeners caused the property violation, as which point it would get  confused
   * if the {@code propertyViolated()} notification happens after the property is reset.
   * 
   * @param property The property that causes the error
   * @param path The path corresponding to the error
   * @param threadList The list of thread information corresponding to the error
   */
  public void error (Property property, Path path, ThreadList threadList) {

    if (getAllErrors) {
       // otherwise we are going to overwrite it if we go on
      try {
        property = property.clone();
        path = path.clone();
        threadList = (ThreadList) threadList.clone(); // this makes it a snapshot (deep) clone
      } catch (CloneNotSupportedException cnsx){
        throw new JPFException("failed to clone error information: " + cnsx);
      }
      done = false;
      
    } else {
      done = true;
    }

    currentError = new Error(errors.size()+1, property, path, threadList);

    errors.add(currentError);

  }

  /**
   * Resets all properties, returning violated properties to their default states.
   */
  public void resetProperties(){
    for (Property p : properties) {
      p.reset();
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has advanced states.
   */
  protected void notifyStateAdvanced () {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].stateAdvanced(this);
      }
      if (reporter != null){
        // reporter always comes last to ensure all listeners have been notified
        reporter.stateAdvanced(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateAdvanced() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has finished processing
   * the current state.
   */
  protected void notifyStateProcessed() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].stateProcessed(this);
      }
      if (reporter != null){
        reporter.stateProcessed(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateProcessed() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has stored the current state.
   */
  protected void notifyStateStored() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].stateStored(this);
      }
      if (reporter != null){
        reporter.stateStored(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateStored() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has restored a currently 
   * stored state.
   */
  protected void notifyStateRestored() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].stateRestored(this);
      }
      if (reporter != null){
        reporter.stateRestored(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateRestored() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has backtracked to a 
   * previous state.
   */
  protected void notifyStateBacktracked() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].stateBacktracked(this);
      }
      if (reporter != null){
        reporter.stateBacktracked(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during stateBacktracked() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has purged the current state.
   */
  protected void notifyStatePurged() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].statePurged(this);
      }
      if (reporter != null){
        reporter.statePurged(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during statePurged() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that a probe request has been triggered during 
   * verification.
   */
  public void notifySearchProbed() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].searchProbed(this);
      }
      if (reporter != null){
        reporter.searchProbed(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during searchProbed() notification", t);
    }
  }

  
  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has encountered a property 
   * violation
   */
  protected void notifyPropertyViolated() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].propertyViolated(this);
      }
      if (reporter != null){
        reporter.propertyViolated(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during propertyViolated() notification", t);
    }

    // reset properties if getAllErrors is set
    if (getAllErrors){
      resetProperties();
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has begun verification.
   */
  protected void notifySearchStarted() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].searchStarted(this);
      }
      if (reporter != null){
        reporter.searchStarted(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during searchStarted() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has encountered a
   * search constraint. The {@code details} String outlines the search constraint that was hit as well as any other
   * relevant information.
   * 
   * @param details Information regarding the recent search constraint that was encountered.
   */
  public void notifySearchConstraintHit(String details) {
    try {
      lastSearchConstraint = details;
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].searchConstraintHit(this);
      }
      if (reporter != null){
        reporter.searchConstraintHit(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during searchConstraintHit() notification", t);
    }
  }

  /**
   * Notifies the {@code SearchListener} objects in the {@code listeners} list that the search loop has finished verification.
   */
  protected void notifySearchFinished() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].searchFinished(this);
      }
      if (reporter != null){
        reporter.searchFinished(this);
      }
    } catch (Throwable t) {
      throw new JPFListenerException("exception during searchFinished() notification", t);
    }
  }

  /**
   * Requests that the virtual machine move to the next unvisited state below the current one in the search tree.
   * {@code forward} along with {@code backtrack} constitute the two methods that are generally used to advance
   * the search algorithm during verification.
   * 
   * @return true if a state exists and the virtual machine can move to it, false if the state does not exist
   * or if it was previously explored.
   * @see #backtrack()
   */
  protected boolean forward () {
    currentError = null;

    boolean ret = vm.forward();

    checkPropertyViolation();
    return ret;
  }


  /**
   * modified forward() to support concurrent search
   * @return
   */
  protected boolean concurrentForward () {
    currentError = null;

    boolean ret = vm.concurrentForwardWithSystemStateClone();

    checkPropertyViolation();
    return ret;
  }
  
  /**
   * Requests that the virtual machine move to the previous state in the search tree. {@code backtrack} along
   * with {@code forward} constitute the two methods that are generally used to advance the search algorithm 
   * during verification.
   * 
   * @return true if the backtrack to the previous state was successful, false otherwise
   */
  protected boolean backtrack () {
    return vm.backtrack();
  }

  /**
   * Requests that the virtual machine sets whether the current state should be ignored in all future iterations
   * of the search loop. This should not be used without cause as it causes the search tree to be pruned whenever
   * it is called, which is expensive, especially in larger trees.
   * 
   * @param cond Whether or not the current state should be ignored
   */
  public void setIgnoredState (boolean cond) {
    vm.ignoreState(cond);
  }

  /**
   * Restores a previously stored virtual machine state.
   * 
   * <p>By default this method is not supported, and therefore requires implementation logic in child classes.
   *
   * @param state The stored state to be restored
   */
  protected void restoreState (State state) {
    // not supported by default
  }

  /** 
   * Sets {@code done} to true in order to terminate the search loop.
   * 
   * <p>Can be used by listeners to terminate the search.
   * 
   */
  public void terminate () {
    done = true;
  }

  /**
   * Sets the depth of the specified state given its state id.
   * 
   * <p>When the depth of the state is set, it is set as depth + 1. This is to differentiate between
   * states that have had their depths set, and states that have not (in which case their default depth
   * will be 0).
   * 
   * @param stateId The state in question
   * @param depth The new depth to set for the state
   */
  protected void setStateDepth (int stateId, int depth) {
    stateDepth.set(stateId, depth + 1);
  }

  
  /**
   * Returns the saved depth of the specified state given its state id.
   * 
   * <p>If the state in question has not had its state depth set previously, then it will by default have
   * a depth that is less than or equal to 0. Otherwise if the state has been visited, the depth will be 
   * returned.
   * 
   * @param stateId The state to return the depth of
   * @return The depth of the specified state
   * @throws JPFException If the state has not been visited in the past
   */
  public int getStateDepth (int stateId) {
    int depthPlusOne = stateDepth.get(stateId);
    if (depthPlusOne <= 0) {
      throw new JPFException("Asked for depth of unvisited state");
    } else {
      return depthPlusOne - 1;
    }
  }

  /**
   * Check if there is the minimum amount of free memory left or more. If not, we would rather stop in time
   * (with a threshold amount left) in order to report something useful, and not just end verification silently
   * with a OutOfMemoryError (which is not handled too gracefully by most VMs)
   * 
   * <p>If the minimum amount of memory has been reached, the method will first try to activate garbage collection
   * and then check again if that made a difference. If the amount of memory available is still less than the 
   * minimum amount of memory, then we return false.
   * 
   * @return true if we have more memory than the minimum free memory, false otherwise.
   */
  public boolean checkStateSpaceLimit () {
    Runtime rt = Runtime.getRuntime();

    long avail = rt.freeMemory();

    // we could also just check for a max number of states, but what really
    // limits us is the memory required to store states

    if (avail < minFreeMemory) {
      // try to collect first
      rt.gc();
      avail = rt.freeMemory();

      if (avail < minFreeMemory) {
        // Ok, we give up, threshold reached
        return false;
      }
    }

    return true;
  }
}

