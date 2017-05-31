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
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.JPFListenerException;
import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.vm.FinalizerThreadInfo;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.IntTable;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.Predicate;

import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class represents the virtual machine. The virtual machine is able to
 * move backward and forward one transition at a time.
 */
public abstract class VM {

  /**
   * this is a debugging aid to control compilation of expensive consistency checks
   * (we don't control these with class-wise assertion enabling since we do use
   * unconditional assertions for mandatory consistency checks)
   */
  public static final boolean CHECK_CONSISTENCY = false;
  
  protected static final String[] EMPTY_ARGS = new String[0];
  
  protected static JPFLogger log = JPF.getLogger("vm");

  /**
   * our execution context
   */
  protected JPF jpf;

  /**
   * The number of errors saved so far.
   * Used to generate the name of the error trail file.
   */
  protected static int error_id;

  /**
   * <2do> - this is a hack to be removed once there are no static references
   * anymore
   */
  protected static VM vm;

  static {
    initStaticFields();
  }

  protected SystemState ss;
  
  protected FunctionObjectFactory funcObjFactory = new FunctionObjectFactory();

  // <2do> - if you are confused about the various pieces of state and its
  // storage/backtrack structures, I'm with you. It's mainly an attempt to
  // separate non-policy VM state (objects), policy VM state (Scheduler)
  // and general JPF execution state, with special support for stack oriented
  // state restoration (backtracking).
  // this needs to be cleaned up and the principle reinstated


  protected Path path;  /** execution path to current state */
  protected StringBuilder out;  /** buffer to store output along path execution */

  /**
   * various caches for VMListener state acquisition. NOTE - these are only
   * valid during notification
   *
   * <2do> get rid of the 'lasts' in favor of queries on the insn, the executing
   * thread, and the VM. This is superfluous work to for every notification
   * (even if there are no listeners using it) that can easily lead to inconsistencies
   */
  protected Transition      lastTrailInfo;

  protected boolean isTraceReplay; // can be set by listeners to indicate this is a replay

  /** the repository we use to find out if we already have seen a state */
  protected StateSet stateSet;

  /** this was the last stateId - note this is also used for stateless model checking */
  protected int newStateId;

  /** the structure responsible for storing and restoring backtrack info */
  protected Backtracker backtracker;

  /** optional serializer/restorer to support backtracker */
  protected StateRestorer<?> restorer;

  /** optional serializer to support stateSet */
  protected StateSerializer serializer;

  /** potential execution listeners. We keep them in a simple array to avoid
   creating objects on each notification */
  protected VMListener[] listeners = new VMListener[0];

  /** did we get a new transition */
  protected boolean transitionOccurred;

  /** how we model execution time */
  protected TimeModel timeModel;
  
  /** ThreadChoiceGenerator management related to data races and shared objects */
  protected Scheduler scheduler;
  
  
  protected Config config; // that's for the options we use only once

  // VM options we use frequently
  protected boolean runGc;
  protected boolean treeOutput;
  protected boolean pathOutput;
  protected boolean indentOutput;
  protected boolean processFinalizers;
  
  // <2do> there are probably many places where this should be used
  protected boolean isBigEndian;

  protected boolean initialized;

  //thread filters
  protected Predicate<ThreadInfo> userliveNonDaemonPredicate;
  protected Predicate<ThreadInfo> timedoutRunnablePredicate;
  protected Predicate<ThreadInfo> alivePredicate;
  protected Predicate<ThreadInfo> userTimedoutRunnablePredicate;

  // a list of actions to be run post GC. This is a bit redundant to VMListener,
  // but in addition to avoid the per-instruction execution overhead of a VMListener
  // we want a (internal) mechanism that is on-demand only, i.e. processed
  // actions are removed from the list
  protected ArrayList<Runnable> postGcActions = new ArrayList<Runnable>();
  
  /**
   * be prepared this might throw JPFConfigExceptions
   */
  public VM (JPF jpf, Config conf) {
    this.jpf = jpf; // so that we know who instantiated us

    // <2do> that's really a bad hack and should be removed once we
    // have cleaned up the reference chains
    vm = this;

    config = conf;

    runGc = config.getBoolean("vm.gc", true);

    treeOutput = config.getBoolean("vm.tree_output", true);
    // we have to defer setting pathOutput until we have a reporter registered
    indentOutput = config.getBoolean("vm.indent_output",false);

    processFinalizers = config.getBoolean("vm.process_finalizers", false);
    
    isBigEndian = getPlatformEndianness(config);
    initialized = false;
    
    initTimeModel(config);

    initSubsystems(config);
    initFields(config);
    
    // set predicates used to query from threadlist
    userliveNonDaemonPredicate = new Predicate<ThreadInfo>() {
      @Override
	public boolean isTrue (ThreadInfo ti) {
        return (!ti.isDaemon() && !ti.isTerminated() && !ti.isSystemThread());
      }
    };

    timedoutRunnablePredicate = new Predicate<ThreadInfo>() {
      @Override
	public boolean isTrue (ThreadInfo ti) {
        return (ti.isTimeoutRunnable());
      }
    };
    
    userTimedoutRunnablePredicate = new Predicate<ThreadInfo>() {
      @Override
	public boolean isTrue (ThreadInfo ti) {
        return (ti.isTimeoutRunnable() && !ti.isSystemThread());
      }
    };
    
    alivePredicate = new Predicate<ThreadInfo>() {
      @Override
	public boolean isTrue (ThreadInfo ti) {
        return (ti.isAlive());
      }
    };
  }

  /**
   * just here for unit test mockups, don't use as implicit base ctor in
   * VM derived classes
   */
  protected VM (){}

  public JPF getJPF() {
    return jpf;
  }

  public void initFields (Config config) {
    path = new Path("fix-this!");
    out = null;

    ss = new SystemState(config, this);

    stateSet = config.getInstance("vm.storage.class", StateSet.class);
    if (stateSet != null) stateSet.attach(this);
    backtracker = config.getEssentialInstance("vm.backtracker.class", Backtracker.class);
    backtracker.attach(this);

    scheduler = config.getEssentialInstance("vm.scheduler.class", Scheduler.class);
    
    newStateId = -1;
  }

  protected void initSubsystems (Config config) {
    ClassLoaderInfo.init(config);
    ClassInfo.init(config);
    ThreadInfo.init(config);
    ElementInfo.init(config);
    MethodInfo.init(config);
    NativePeer.init(config);
    ChoiceGeneratorBase.init(config);
    
    // peer classes get initialized upon NativePeer creation
  }

  protected void initTimeModel (Config config){
    Class<?>[] argTypes = { VM.class, Config.class };
    Object[] args = { this, config };
    timeModel = config.getEssentialInstance("vm.time.class", TimeModel.class, argTypes, args);
  }
  
  /**
   * called after the JPF run is finished. Shouldn't be public, but is called by JPF
   */
  public void cleanUp(){
    // nothing yet
  }
  
  protected boolean getPlatformEndianness (Config config){
    String endianness = config.getString("vm.endian");
    if (endianness == null) {
      return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    } else if (endianness.equalsIgnoreCase("big")) {
      return true;
    } else if (endianness.equalsIgnoreCase("little")) {
      return false;
    } else {
      config.exception("illegal vm.endian value: " + endianness);
      return false; // doesn't matter
    }
  }
  
  public boolean isBigEndianPlatform(){
    return isBigEndian;
  }

  public boolean finalizersEnabled() {
    return processFinalizers;
  }
  
  public boolean isInitialized() {
    return initialized;
  }
  
  public boolean isSingleProcess() {
    return true;
  }

  /**
   * do we see our model classes? Some of them cannot be used from the standard CLASSPATH, because they
   * are tightly coupled with the JPF core (e.g. java.lang.Class, java.lang.Thread,
   * java.lang.StackTraceElement etc.)
   * Our strategy here is kind of lame - we just look into java.lang.Class if we find the 'uniqueId' field
   * (that's a true '42')
   */
  static boolean checkSystemClassCompatibility (SystemClassLoaderInfo systemLoader) {
    ClassInfo ci = systemLoader.getClassClassInfo();
    return ci.checkIfValidClassClassInfo();
  }

  static boolean isValidClassName (String clsName) {
    if ( !clsName.matches("[a-zA-Z_$][a-zA-Z_$0-9.]*")) {
      return false;
    }

    // well, those two could be part of valid class names, but
    // in all likeliness somebody specified a filename instead of
    // a classname
    if (clsName.endsWith(".java")) {
      return false;
    }
    if (clsName.endsWith(".class")) {
      return false;
    }

    return true;
  }

  //--- ThreadInfo factory methods
  protected ThreadInfo createMainThreadInfo (int id, ApplicationContext appCtx){
    ThreadInfo tiMain = new ThreadInfo( this, id, appCtx);
    ThreadInfo.currentThread = tiMain; // we still need this for listeners that process startup class loading events
    registerThread(tiMain);
    
    return tiMain;
  }
  
  protected ThreadInfo createThreadInfo (int objRef, int groupRef, int runnableRef, int nameRef){
    ThreadInfo tiCurrent = ThreadInfo.getCurrentThread();
    ThreadInfo tiNew = new ThreadInfo( this, objRef, groupRef, runnableRef, nameRef, tiCurrent);

    // note that we have to register here so that subsequent native peer calls can use the objRef
    // to lookup the ThreadInfo. This is a bit premature since the thread is not runnable yet,
    // but chances are it will be started soon, so we don't waste another data structure to do the mapping
    registerThread( tiNew);
    
    return tiNew;
  }

  // created if the option "vm.process_finalizers" is set to true
  protected ThreadInfo createFinalizerThreadInfo (int id, ApplicationContext appCtx){
    FinalizerThreadInfo finalizerTi = new FinalizerThreadInfo( this, appCtx, id);
    registerThread(finalizerTi);
    
    return finalizerTi;
  }
  
  /**
   * the minimal set of system classes we need for initialization
   */
  protected List<String> getStartupSystemClassNames() {
    ArrayList<String> startupClasses = new ArrayList<String>(64);

    // bare essentials
    startupClasses.add("java.lang.Object");
    startupClasses.add("java.lang.Class");
    startupClasses.add("java.lang.ClassLoader");

    // the builtin types (and their arrays)
    startupClasses.add("boolean");
    startupClasses.add("[Z");
    startupClasses.add("byte");
    startupClasses.add("[B");
    startupClasses.add("char");
    startupClasses.add("[C");
    startupClasses.add("short");
    startupClasses.add("[S");
    startupClasses.add("int");
    startupClasses.add("[I");
    startupClasses.add("long");
    startupClasses.add("[J");
    startupClasses.add("float");
    startupClasses.add("[F");
    startupClasses.add("double");
    startupClasses.add("[D");
    startupClasses.add("void");

    // the box types
    startupClasses.add("java.lang.Boolean");
    startupClasses.add("java.lang.Character");
    startupClasses.add("java.lang.Short");
    startupClasses.add("java.lang.Integer");
    startupClasses.add("java.lang.Long");
    startupClasses.add("java.lang.Float");
    startupClasses.add("java.lang.Double");
    startupClasses.add("java.lang.Byte");

    // the cache for box types
    startupClasses.add("gov.nasa.jpf.BoxObjectCaches");

    // standard system classes
    startupClasses.add("java.lang.String");
    startupClasses.add("java.lang.Thread");
    startupClasses.add("java.lang.ThreadGroup");
    startupClasses.add("java.lang.Thread$State");
    startupClasses.add("java.lang.Thread$Permit");
    startupClasses.add("java.io.PrintStream");
    startupClasses.add("java.io.InputStream");
    startupClasses.add("java.lang.System");
    startupClasses.add("java.lang.ref.Reference");
    startupClasses.add("java.lang.ref.WeakReference");
    startupClasses.add("java.lang.Enum");
    startupClasses.add("gov.nasa.jpf.FinalizerThread");

    // we could be more fancy and use wildcard patterns and the current classpath
    // to specify extra classes, but this could be VERY expensive. Projected use
    // is mostly to avoid static init of single classes during the search
    String[] extraStartupClasses = config.getStringArray("vm.extra_startup_classes");
    if (extraStartupClasses != null) {      
      for (String extraCls : extraStartupClasses) {
        startupClasses.add(extraCls);
      }
    }

    // the main class has to be handled separately since it might be VM specific

    return startupClasses;
  }

  /**
   * return a list of ClassInfos for essential system types
   * 
   * If system classes are not found, or are not valid JPF model classes, we throw
   * a JPFConfigException and exit
   * 
   * returned ClassInfos are not yet registered in Statics and don't have class objects
   */
  protected List<ClassInfo> getStartupSystemClassInfos (SystemClassLoaderInfo sysCl, ThreadInfo tiMain){
    LinkedList<ClassInfo> list = new LinkedList<ClassInfo>();
    
    try {
      for (String clsName : getStartupSystemClassNames()) {
        ClassInfo ci = sysCl.getResolvedClassInfo(clsName);
        ci.registerStartupClass( tiMain, list); // takes care of superclasses and interfaces
      }
    } catch (ClassInfoException e){
      e.printStackTrace();
      throw new JPFConfigException("cannot load system class " + e.getFailedClass());
    } 
    
    return list;
  }
  
  /**
   * this adds the application main class and its supers to the list of startup classes 
   */
  protected ClassInfo getMainClassInfo (SystemClassLoaderInfo sysCl, String mainClassName, ThreadInfo tiMain, List<ClassInfo> list){
    try {
      ClassInfo ciMain = sysCl.getResolvedClassInfo(mainClassName);
      ciMain.registerStartupClass(tiMain, list); // this might add a couple more
      
      return ciMain;
      
    } catch (ClassInfoException e){
      throw new JPFConfigException("cannot load application class " + e.getFailedClass());
    }
  }
  
  /*
   * these are called when creating ApplicationContexts and can be overridden by concrete VM types 
   */
  protected SystemClassLoaderInfo createSystemClassLoaderInfo (int appId) {
    Class<?>[] argTypes = { VM.class, int.class };
   
    Object[] args = { this, Integer.valueOf(appId)};
    SystemClassLoaderInfo sysCli = config.getEssentialInstance("vm.classloader.class", SystemClassLoaderInfo.class, argTypes, args);
    return sysCli;
  }
  
  protected void createSystemClassLoaderObject (SystemClassLoaderInfo sysCl, ThreadInfo tiMain) {
    Heap heap = getHeap();

    // create ClassLoader object for the ClassLoader type defined by this SystemClassLoaderInfo
    // NOTE - this requires the SystemClassLoaderInfo cache to be initialized
    ClassInfo ciCl = sysCl.getClassLoaderClassInfo();
    ElementInfo ei = heap.newObject( ciCl, tiMain);
    //ei.setReferenceField("parent", MJIEnv.NULL);
    heap.registerPinDown(ei.getObjectRef());

    sysCl.setClassLoaderObject(ei);
  }  
  
  protected void pushMainEntryArgs (MethodInfo miMain, String[] args, ThreadInfo tiMain, DirectCallStackFrame frame){
    String sig = miMain.getSignature();
    Heap heap = getHeap();
    
    if (sig.contains("([Ljava/lang/String;)")){
      ElementInfo eiArgs = heap.newArray("Ljava/lang/String;", args.length, tiMain);
      for (int i = 0; i < args.length; i++) {
        ElementInfo eiArg = heap.newString(args[i], tiMain);
        eiArgs.setReferenceElement(i, eiArg.getObjectRef());
      }
      frame.setReferenceArgument( 0, eiArgs.getObjectRef(), null);

    } else if (sig.contains("(Ljava/lang/String;)")){
      if (args.length > 1){
        ElementInfo eiArg = heap.newString(args[0], tiMain);
        frame.setReferenceArgument( 0, eiArg.getObjectRef(), null);
      } else {
        frame.setReferenceArgument( 0, MJIEnv.NULL, null);
      }
      
    } else if (!sig.contains("()")){
      throw new JPFException("unsupported main entry signature: " + miMain.getFullName());
    }
  }
  
  protected void pushMainEntry (MethodInfo miMain, String[] args, ThreadInfo tiMain) {
    DirectCallStackFrame frame = miMain.createDirectCallStackFrame(tiMain, 0);
    pushMainEntryArgs( miMain, args, tiMain, frame);    
    tiMain.pushFrame(frame);
  }

  protected MethodInfo getMainEntryMethodInfo (String mthName, ClassInfo ciMain) {
    MethodInfo miMain = ciMain.getMethod(mthName, true);

    //--- do some sanity checks if this is a valid entry method
    if (miMain == null || !miMain.isStatic()) {
      throw new JPFConfigException("no static entry method: " + ciMain.getName() + '.' + mthName);
    }
    
    return miMain;
  }
  
  protected void pushClinits (List<ClassInfo> startupClasses, ThreadInfo tiMain) {
    for (ClassInfo ci : startupClasses){
      MethodInfo mi = ci.getMethod("<clinit>()V", false);
      if (mi != null) {
        DirectCallStackFrame frame = mi.createDirectCallStackFrame(tiMain, 0);
        tiMain.pushFrame(frame);
      } else {
        ci.setInitialized();
      }      
    }
  }
  
  /**
   * this is the main initialization point that sets up startup objects threads and callstacks.
   * If this returns false VM initialization cannot proceed and JPF will terminate
   */
  public abstract boolean initialize ();
  
  /**
   * create and initialize the main thread for the given ApplicationContext.
   * This is called from VM.initialize() implementations, the caller has to handle exceptions that should be reported
   * differently (JPFConfigException, ClassInfoException)
   */
  protected ThreadInfo initializeMainThread (ApplicationContext appCtx, int tid){
    SystemClassLoaderInfo sysCl = appCtx.sysCl;
    
    ThreadInfo tiMain = createMainThreadInfo(tid, appCtx);
    List<ClassInfo> startupClasses = getStartupSystemClassInfos(sysCl, tiMain);
    ClassInfo ciMain = getMainClassInfo(sysCl, appCtx.mainClassName, tiMain, startupClasses);

    if (!checkSystemClassCompatibility( sysCl)){
      throw new JPFConfigException("non-JPF system classes, check classpath");
    }
    
    // create essential objects (we can't call ctors yet)
    createSystemClassLoaderObject(sysCl, tiMain);
    for (ClassInfo ci : startupClasses) {
      ci.createAndLinkStartupClassObject(tiMain);
    }
    tiMain.createMainThreadObject(sysCl);
    registerThread(tiMain);
    
    // note that StackFrames have to be pushed in reverse order
    MethodInfo miMain = getMainEntryMethodInfo(appCtx.mainEntry, ciMain);
    appCtx.setEntryMethod(miMain);
    pushMainEntry(miMain, appCtx.args, tiMain);
    Collections.reverse(startupClasses);
    pushClinits(startupClasses, tiMain);

    registerThreadListCleanup(sysCl.getThreadClassInfo());

    return tiMain;
  }
  
  protected void initializeFinalizerThread (ApplicationContext appCtx, int tid) {
    if(processFinalizers) {
      ApplicationContext app = getCurrentApplicationContext();
      FinalizerThreadInfo finalizerTi = app.getFinalizerThread();
    
      finalizerTi = (FinalizerThreadInfo) createFinalizerThreadInfo(tid, app);
      finalizerTi.createFinalizerThreadObject(app.getSystemClassLoader());
    
      appCtx.setFinalizerThread(finalizerTi);
    }
  }
  
  protected void registerThreadListCleanup (ClassInfo ciThread){
    assert ciThread != null : "java.lang.Thread not loaded yet";
    
    ciThread.addReleaseAction( new ReleaseAction(){
      @Override
	public void release (ElementInfo ei) {
        ThreadList tl = getThreadList();
        int objRef = ei.getObjectRef();
        ThreadInfo ti = tl.getThreadInfoForObjRef(objRef);
        if (tl.remove(ti)){        
          vm.getKernelState().changed();    
        }
      }
    });
  }
  

  /**
   * override this if the concrete VM needs a special root CG
   */
  protected void setRootCG(){
    scheduler.setRootCG();
  }
  
  protected void initSystemState (ThreadInfo mainThread){
    ss.setStartThread(mainThread);

    ss.recordSteps(hasToRecordSteps());

    if (!pathOutput) { // don't override if explicitly requested
      pathOutput = hasToRecordPathOutput();
    }

    setRootCG(); // this has to be guaranteed to register a CG
    if (!hasNextChoiceGenerator()){
      throw new JPFException("scheduler failed to set ROOT choice generator: " + scheduler);
    }
    
    transitionOccurred = true;
  }
  
  public void addPostGcAction (Runnable r){
    postGcActions.add(r);
  }
  
  /**
   * to be called from the Heap after GC is completed (i.e. only live objects remain)
   */
  public void processPostGcActions(){
    if (!postGcActions.isEmpty()){
      for (Runnable r : postGcActions){
        r.run();
      }
      
      postGcActions.clear();
    }
  }
  
  public void addListener (VMListener newListener) {
    log.info("VMListener added: ", newListener);
    listeners = Misc.appendElement(listeners, newListener);
  }

  public boolean hasListenerOfType (Class<?> listenerCls) {
    return Misc.hasElementOfType(listeners, listenerCls);
  }

  public <T> T getNextListenerOfType(Class<T> type, T prev){
    return Misc.getNextElementOfType(listeners, type, prev);
  }
  
  public void removeListener (VMListener removeListener) {
    listeners = Misc.removeElement(listeners, removeListener);
  }

  public void setTraceReplay (boolean isReplay) {
    isTraceReplay = isReplay;
  }

  public boolean isTraceReplay() {
    return isTraceReplay;
  }

  public boolean hasToRecordSteps() {
    // we have to record if there either is a reporter that has
    // a 'trace' topic, or there is an explicit request
    return jpf.getReporter().hasToReportTrace()
             || config.getBoolean("vm.store_steps");
  }

  public void recordSteps( boolean cond) {
    // <2do> not ideal - it might be already too late when this is called

    config.setProperty("vm.store_steps", cond ? "true" : "false");

    if (ss != null){
      ss.recordSteps(cond);
    }
  }

  public boolean hasToRecordPathOutput() {
    if (config.getBoolean("vm.path_output")){ // explicitly requested
      return true;
    } else {
      return jpf.getReporter().hasToReportOutput(); // implicilty required
    }
  }
  
  //--- VM listener notifications
  
  /*
   * while some of these can be called from various places, the calls that happen from within Instruction.execute() should
   * happen right before return since listeners might do things such as ThreadInfo.createAndThrowException(..), i.e. cause
   * side effects that would violate consistency requirements of successive operations (e.g. by assuming we are still executing
   * in the same StackFrame - after throwing an exception)
   */
  
  protected void notifyVMInitialized () {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].vmInitialized(this);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during vmInitialized() notification", t);
    }    
  }
  
  protected void notifyChoiceGeneratorRegistered (ChoiceGenerator<?>cg, ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].choiceGeneratorRegistered(this, cg, ti, ti.getPC());
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during choiceGeneratorRegistered() notification", t);
    }
  }

  protected void notifyChoiceGeneratorSet (ChoiceGenerator<?>cg) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].choiceGeneratorSet(this, cg);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during choiceGeneratorSet() notification", t);
    }
  }

  protected void notifyChoiceGeneratorAdvanced (ChoiceGenerator<?>cg) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].choiceGeneratorAdvanced(this, cg);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during choiceGeneratorAdvanced() notification", t);
    }
  }

  protected void notifyChoiceGeneratorProcessed (ChoiceGenerator<?>cg) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].choiceGeneratorProcessed(this, cg);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during choiceGeneratorProcessed() notification", t);
    }
  }

  protected void notifyExecuteInstruction (ThreadInfo ti, Instruction insn) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].executeInstruction(this, ti, insn);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during executeInstruction() notification", t);
    }
  }

  protected void notifyInstructionExecuted (ThreadInfo ti, Instruction insn, Instruction nextInsn) {
    try {
      //listener.instructionExecuted(this);
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].instructionExecuted(this, ti, nextInsn, insn);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during instructionExecuted() notification", t);
    }
  }

  protected void notifyThreadStarted (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadStarted(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadStarted() notification", t);
    }
  }

  // NOTE: the supplied ThreadInfo does NOT have to be the running thread, as this
  // notification can occur as a result of a lock operation in the current thread
  protected void notifyThreadBlocked (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadBlocked(this, ti, ti.getLockObject());
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadBlocked() notification", t);
    }
  }

  protected void notifyThreadWaiting (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadWaiting(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadWaiting() notification", t);
    }
  }

  protected void notifyThreadNotified (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadNotified(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadNotified() notification", t);
    }
  }

  protected void notifyThreadInterrupted (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadInterrupted(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadInterrupted() notification", t);
    }
  }

  protected void notifyThreadTerminated (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadTerminated(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadTerminated() notification", t);
    }
  }

  protected void notifyThreadScheduled (ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].threadScheduled(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during threadScheduled() notification", t);
    }
  }
  
  protected void notifyLoadClass (ClassFile cf){
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].loadClass(this, cf);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during classLoaded() notification", t);
    }    
  }

  protected void notifyClassLoaded(ClassInfo ci) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].classLoaded(this, ci);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during classLoaded() notification", t);
    }
  }

  protected void notifyObjectCreated(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectCreated(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectCreated() notification", t);
    }
  }

  protected void notifyObjectReleased(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectReleased(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectReleased() notification", t);
    }
  }

  protected void notifyObjectLocked(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectLocked(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectLocked() notification", t);
    }
  }

  protected void notifyObjectUnlocked(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectUnlocked(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectUnlocked() notification", t);
    }
  }

  protected void notifyObjectWait(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectWait(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectWait() notification", t);
    }
  }

   protected void notifyObjectExposed(ThreadInfo ti, ElementInfo eiShared, ElementInfo eiExposed) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectExposed(this, ti, eiShared, eiExposed);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectExposed() notification", t);
    }
  }

   protected void notifyObjectShared(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectShared(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectShared() notification", t);
    }
  }
  
  protected void notifyObjectNotifies(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectNotify(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectNotifies() notification", t);
    }
  }

  protected void notifyObjectNotifiesAll(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].objectNotifyAll(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during objectNotifiesAll() notification", t);
    }
  }

  protected void notifyGCBegin() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].gcBegin(this);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during gcBegin() notification", t);
    }
  }

  protected void notifyGCEnd() {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].gcEnd(this);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during gcEnd() notification", t);
    }
  }

  protected void notifyExceptionThrown(ThreadInfo ti, ElementInfo ei) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].exceptionThrown(this, ti, ei);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during exceptionThrown() notification", t);
    }
  }

  protected void notifyExceptionBailout(ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].exceptionBailout(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during exceptionBailout() notification", t);
    }
  }

  protected void notifyExceptionHandled(ThreadInfo ti) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].exceptionHandled(this, ti);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during exceptionHandled() notification", t);
    }
  }

  protected void notifyMethodEntered(ThreadInfo ti, MethodInfo mi) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].methodEntered(this, ti, mi);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during methodEntered() notification", t);
    }
  }

  protected void notifyMethodExited(ThreadInfo ti, MethodInfo mi) {
    try {
      for (int i = 0; i < listeners.length; i++) {
        listeners[i].methodExited(this, ti, mi);
      }
    } catch (UncaughtException x) {
      throw x;
    } catch (JPF.ExitException x) {
      throw x;
    } catch (Throwable t) {
      throw new JPFListenerException("exception during methodExited() notification", t);
    }
  }

  // VMListener acquisition
  public String getThreadName () {
    ThreadInfo ti = ThreadInfo.getCurrentThread();

    return ti.getName();
  }

  // VMListener acquisition
  public Instruction getInstruction () {
    ThreadInfo ti = ThreadInfo.getCurrentThread();
    return ti.getPC();
  }

  /**
   * note this is gone after backtracking or starting the next exception
   */
  public ExceptionInfo getPendingException () {
    ThreadInfo ti = ThreadInfo.getCurrentThread();

    if (ti != null){
      return ti.getPendingException();
    } else {
      return null;
    }
  }

  public Step getLastStep () {
    Transition trail = ss.getTrail();
    if (trail != null) {
      return trail.getLastStep();
    }

    return null;
  }

  public Transition getLastTransition () {
    if (path.size() == 0) {
      return null;
    }
    return path.get(path.size() - 1);
  }

  public ClassInfo getClassInfo (int objref) {
    if (objref != MJIEnv.NULL) {
      return getElementInfo(objref).getClassInfo();
    } else {
      return null;
    }
  }

  /**
   * NOTE: only use this locally, since the path is getting modified by the VM
   *
   * The path only contains all states when queried from a stateAdvanced() notification.
   * If this is called from an instructionExecuted() (or other VMListener), and you need
   * the ongoing transition in it, you have to call updatePath() first
   */
  public Path getPath () {
    return path;
  }

  /**
   * this is the ongoing transition. Note that it is not yet stored in the path
   * if this is called from a VMListener notification
   */
  public Transition getCurrentTransition() {
    return ss.getTrail();
  }

  /**
   * use that one if you have to store the path for subsequent use
   *
   * NOTE: without a prior call to updatePath(), this does NOT contain the
   * ongoing transition. See getPath() for usage from a VMListener
   */
  public Path getClonedPath () {
    return path.clone();
  }

  public int getPathLength () {
    return path.size();
  }

  public ThreadList getThreadList () {
    return getKernelState().getThreadList();
  }
  
  public ClassLoaderList getClassLoaderList() {
    return getKernelState().getClassLoaderList();
  }

  
  /**
   * Bundles up the state of the system for export
   */
  public RestorableVMState getRestorableState () {
    return new RestorableVMState(this);
  }

  /**
   * Gets the system state.
   */
  public SystemState getSystemState () {
    return ss;
  }

  public KernelState getKernelState () {
    return ss.getKernelState();
  }

  public void kernelStateChanged(){
    ss.getKernelState().changed();
  }
  
  public Config getConfig() {
    return config;
  }

  public Backtracker getBacktracker() {
    return backtracker;
  }

  @SuppressWarnings("unchecked")
  public <T> StateRestorer<T> getRestorer() {
    if (restorer == null) {
      if (serializer instanceof StateRestorer) {
        restorer = (StateRestorer<?>) serializer;
      } else if (stateSet instanceof StateRestorer) {
        restorer = (StateRestorer<?>) stateSet;
      } else {
        // config read only if serializer is not also a restorer
        restorer = config.getInstance("vm.restorer.class", StateRestorer.class);
      }
      restorer.attach(this);
    }

    return (StateRestorer<T>) restorer;
  }

  public StateSerializer getSerializer() {
    if (serializer == null) {
      serializer = config.getEssentialInstance("vm.serializer.class",
                                      StateSerializer.class);
      serializer.attach(this);
    }
    return serializer;
  }

  public void setSerializer (StateSerializer newSerializer){
    serializer = newSerializer;
    serializer.attach(this);
  }
  
  /**
   * Returns the stateSet if states are being matched.
   */
  public StateSet getStateSet() {
    return stateSet;
  }

  public Scheduler getScheduler(){
    return scheduler;
  }
  
  public FunctionObjectFactory getFunctionObjectFacotry() {
    return funcObjFactory;
  }
  
  /**
   * return the last registered SystemState's ChoiceGenerator object
   * NOTE: there might be more than one ChoiceGenerator associated with the
   * current transition (ChoiceGenerators can be cascaded)
   */
  public ChoiceGenerator<?> getChoiceGenerator () {
    return ss.getChoiceGenerator();
  }

  public ChoiceGenerator<?> getNextChoiceGenerator() {
    return ss.getNextChoiceGenerator();
  }
  
  public boolean hasNextChoiceGenerator(){
    return (ss.getNextChoiceGenerator() != null);
  }

  public boolean setNextChoiceGenerator (ChoiceGenerator<?> cg){
    return ss.setNextChoiceGenerator(cg);
  }
  
  public void setMandatoryNextChoiceGenerator (ChoiceGenerator<?> cg, String failMsg){
    ss.setMandatoryNextChoiceGenerator(cg, failMsg);
  }
  
  /**
   * return the latest registered ChoiceGenerator used in this transition
   * that matches the provided 'id' and is of 'cgType'.
   * 
   * This should be the main getter for clients that are cascade aware
   */
  public <T extends ChoiceGenerator<?>> T getCurrentChoiceGenerator (String id, Class<T> cgType) {
    return ss.getCurrentChoiceGenerator(id,cgType);
  }

  /**
   * returns all ChoiceGenerators in current path
   */
  public ChoiceGenerator<?>[] getChoiceGenerators() {
    return ss.getChoiceGenerators();
  }

  public <T extends ChoiceGenerator<?>> T[] getChoiceGeneratorsOfType (Class<T> cgType) {
    return ss.getChoiceGeneratorsOfType(cgType);
  }

  public <T extends ChoiceGenerator<?>> T getLastChoiceGeneratorOfType (Class<T> cgType){
    return ss.getLastChoiceGeneratorOfType(cgType);
  }

  public ChoiceGenerator<?> getLastChoiceGeneratorInThread (ThreadInfo ti){
    return ss.getLastChoiceGeneratorInThread(ti);
  }
  
  public void print (String s) {
    if (treeOutput) {
      System.out.print(s);
    }

    if (pathOutput) {
      appendOutput(s);
    }
  }

  public void println (String s) {
    if (treeOutput) {
      if (indentOutput){
        StringBuilder indent = new StringBuilder();
        int i;
        for (i = 0;i<=path.size();i++) {
          indent.append('|').append(i);
        }
        indent.append("|").append(s);
        System.out.println(indent);
      }
      else {
        System.out.println(s);
      }
    }

    if (pathOutput) {
      appendOutput(s);
      appendOutput('\n');
    }
  }

  public void print (boolean b) {
    if (treeOutput) {
      System.out.print(b);
    }

    if (pathOutput) {
      appendOutput(Boolean.toString(b));
    }
  }

  public void print (char c) {
    if (treeOutput) {
      System.out.print(c);
    }

    if (pathOutput) {
      appendOutput(c);
    }
  }

  public void print (int i) {
    if (treeOutput) {
      System.out.print(i);
    }

    if (pathOutput) {
      appendOutput(Integer.toString(i));
    }
  }

  public void print (long l) {
    if (treeOutput) {
      System.out.print(l);
    }

    if (pathOutput) {
      appendOutput(Long.toString(l));
    }
  }

  public void print (double d) {
    if (treeOutput) {
      System.out.print(d);
    }

    if (pathOutput) {
      appendOutput(Double.toString(d));
    }
  }

  public void print (float f) {
    if (treeOutput) {
      System.out.print(f);
    }

    if (pathOutput) {
      appendOutput(Float.toString(f));
    }
  }

  public void println () {
    if (treeOutput) {
      System.out.println();
    }

    if (pathOutput) {
      appendOutput('\n');
    }
  }


  void appendOutput (String s) {
    if (out == null) {
      out = new StringBuilder();
    }
    out.append(s);
  }

  void appendOutput (char c) {
    if (out == null) {
      out = new StringBuilder();
    }
    out.append(c);
  }

  /**
   * get the pending output (not yet stored in the path)
   */
  public String getPendingOutput() {
    if (out != null && out.length() > 0){
      return out.toString();
    } else {
      return null;
    }
  }
  
  /**
   * this is here so that we can intercept it in subclassed VMs
   */
  public Instruction handleException (ThreadInfo ti, int xObjRef){
    ti = null;        // Get rid of IDE warning
    xObjRef = 0;
    return null;
  }

  public void storeTrace (String fileName, String comment, boolean verbose) {
    ChoicePoint.storeTrace(fileName, getSUTName(), comment,
                           ss.getChoiceGenerators(), verbose);
  }

  public void storePathOutput () {
    pathOutput = true;
  }

  private void printCG (ChoiceGenerator<?> cg, int n){
    ChoiceGenerator cgPrev = cg.getPreviousChoiceGenerator();
    if (cgPrev != null){
      printCG( cgPrev, --n);
    }
    
    System.out.printf("[%d] ", n);
    System.out.println(cg);
  } 
  
  // for debugging purposes
  public void printChoiceGeneratorStack(){
    ChoiceGenerator<?> cg = getChoiceGenerator();
    if (cg != null){
      int n = cg.getNumberOfParents();
      printCG(cg, n);
    }
  }
  
  public ThreadInfo[] getLiveThreads () {
    return getThreadList().getThreads();
  }
  
  /**
   * print call stacks of all live threads
   * this is also used for debugging purposes, so we can't move it to the Reporter system
   * (it's also using a bit too many internals for that)
   */
  public void printLiveThreadStatus (PrintWriter pw) {
    int nThreads = ss.getThreadCount();
    ThreadInfo[] threads = getThreadList().getThreads();
    int n=0;

    for (int i = 0; i < nThreads; i++) {
      ThreadInfo ti = threads[i];

      if (ti.getStackDepth() > 0){
        n++;
        //pw.print("Thread: ");
        //pw.print(tiMain.getName());
        pw.println(ti.getStateDescription());

        List<ElementInfo> locks = ti.getLockedObjects();
        if (!locks.isEmpty()) {
          pw.print("  owned locks:");
          boolean first = true;
          for (ElementInfo e : locks) {
            if (first) {
              first = false;
            } else {
              pw.print(",");
            }
            pw.print(e);
          }
          pw.println();
        }

        ElementInfo ei = ti.getLockObject();
        if (ei != null) {
          if (ti.getState() == ThreadInfo.State.WAITING) {
            pw.print( "  waiting on: ");
          } else {
            pw.print( "  blocked on: ");
          }
          pw.println(ei);
        }

        pw.println("  call stack:");
        for (StackFrame frame : ti){
          if (!frame.isDirectCallFrame()) {
            pw.print("\tat ");
            pw.println(frame.getStackTraceInfo());
          }
        }

        pw.println();
      }
    }

    if (n==0) {
      pw.println("no live threads");
    }
  }

  // just a debugging aid
  public void dumpThreadStates () {
    java.io.PrintWriter pw = new java.io.PrintWriter(System.out, true);
    printLiveThreadStatus(pw);
    pw.flush();
  }

  /**
   * Moves one step backward. This method and forward() are the main methods
   * used by the search object.
   * Note this is called with the state that caused the backtrack still being on
   * the stack, so we have to remove that one first (i.e. popping two states
   * and restoring the second one)
   */
  public boolean backtrack () {
    transitionOccurred = false;

    boolean success = backtracker.backtrack();
    if (success) {
      if (CHECK_CONSISTENCY) checkConsistency(false);
      
      // restore the path
      path.removeLast();
      lastTrailInfo = path.getLast();

      return true;
      
    } else {
      return false;
    }
  }

  /**
   * store the current SystemState's Trail in our path, after updating it
   * with whatever annotations the VM wants to add.
   * This is supposed to be called after each transition we want to keep
   */
  public void updatePath () {
    Transition t = ss.getTrail();
    Transition tLast = path.getLast();

    // NOTE: don't add the transition twice, this is public and might get called
    // from listeners, so the transition object might get changed

    if (tLast != t) {
      // <2do> we should probably store the output directly in the TrailInfo,
      // but this might not be our only annotation in the future

      // did we have output during the last transition? If yes, add it
      if ((out != null) && (out.length() > 0)) {
        t.setOutput( out.toString());
        out.setLength(0);
      }

      path.add(t);
    }
  }

  /**
   * advance the program state
   *
   * forward() and backtrack() are the two primary interfaces towards the Search
   * driver. note that the caller still has to check if there is a next state,
   * and if the executed instruction sequence led into a new or already visited state
   *
   * @return 'true' if there was an un-executed sequence out of the current state,
   * 'false' if it was completely explored
   *
   */
  public boolean forward () {

    // the reason we split up CG initialization and transition execution
    // is that program state storage is not required if the CG initialization
    // does not produce a new choice since we have to backtrack in that case
    // anyways. This can be caused by complete enumeration of CGs and/or by
    // CG listener intervention (i.e. not just after backtracking). For a large
    // number of matched or end states and ignored transitions this can be a
    // huge saving.
    // The downside is that CG notifications are NOT allowed anymore to change the
    // KernelState (modify fields or thread states) since those changes would
    // happen before storing the KernelState, and hence would make backtracking
    // inconsistent. This is advisable anyways since all program state changes
    // should take place during transitions, but the real snag is that this
    // cannot be easily enforced.

    // actually, it hasn't occurred yet, but will
    transitionOccurred = ss.initializeNextTransition(this);
    
    if (transitionOccurred){
      if (CHECK_CONSISTENCY) {
        checkConsistency(true); // don't push an inconsistent state
      }

      backtracker.pushKernelState();

      // cache this before we enter (and increment) the next insn(s)
      lastTrailInfo = path.getLast();

      try {
        ss.executeNextTransition(vm);

      } catch (UncaughtException e) {
        // we don't pass this up since it means there were insns executed and we are
        // in a consistent state
      } // every other exception goes upwards

      backtracker.pushSystemState();
      updatePath();

      if (!isIgnoredState()) {
        // if this is ignored we are going to backtrack anyways
        // matching states out of ignored transitions is also not a good idea
        // because this transition is usually incomplete

        if (runGc && !hasPendingException()) {
          if(ss.gcIfNeeded()) {
            processFinalizers();
          }
        }

        if (stateSet != null) {
          newStateId = stateSet.size();
          int id = stateSet.addCurrent();
          ss.setId(id);

        } else { // this is 'state-less' model checking, i.e. we don't match states
          ss.setId(++newStateId); // but we still should have states numbered in case listeners use the id
        }
      }
      
      return true;

    } else {

      return false;  // no transition occurred
    }
  }

  /**
   * Prints the current stack trace. Just for debugging purposes
   */
  public void printCurrentStackTrace () {
    ThreadInfo th = ThreadInfo.getCurrentThread();

    if (th != null) {
      th.printStackTrace();
    }
  }


  public void restoreState (RestorableVMState state) {
    if (state.path == null) {
      throw new JPFException("tried to restore partial VMState: " + state);
    }
    backtracker.restoreState(state.getBkState());
    path = state.path.clone();
  }

  public void activateGC () {
    ss.activateGC();
  }


  //--- various state attribute getters and setters (mostly forwarding to SystemState)

  public void retainStateAttributes (boolean isRetained){
    ss.retainAttributes(isRetained);
  }

  public void forceState () {
    ss.setForced(true);
  }

  /**
   * override the state matching - ignore this state, no matter if we changed
   * the heap or stacks.
   * use this with care, since it prunes whole search subtrees
   */
  public void ignoreState (boolean cond) {
    ss.setIgnored(cond);
  }

  public void ignoreState(){
    ignoreState(true);
  }

  /**
   * imperatively break the transition to enable state matching
   */
  public void breakTransition (String reason) {
    ThreadInfo ti = ThreadInfo.getCurrentThread();
    ti.breakTransition(reason);
  }

  public boolean transitionOccurred(){
    return transitionOccurred;
  }

  /**
   * answers if the current state already has been visited. This is mainly
   * used by the searches (to control backtracking), but could also be useful
   * for observers to build up search graphs (based on the state ids)
   *
   * this returns true if no state has been produced yet, and false if
   * no transition occurred after a forward call
   */
  public boolean isNewState() {

    if (!transitionOccurred){
      return false;
    }

    if (stateSet != null) {
      if (ss.isForced()){
        return true;
      } else if (ss.isIgnored()){
        return false;
      } else {
        return (newStateId == ss.getId());
      }

    } else { // stateless model checking - each transition leads to a new state
      return true;
    }
  }

  /**
   * We made this to be overriden by Single/MultiprcessesVM implementations,
   * since for MultiprcessesVM one can decide when to terminate (after the
   * the termination of all processes or only one process).
   * todo - that needs to be specified through the properties file
   */
  public abstract boolean isEndState ();

  public boolean isVisitedState(){
    return !isNewState();
  }

  public boolean isIgnoredState(){
    return ss.isIgnored();
  }

  public boolean isInterestingState () {
    return ss.isInteresting();
  }

  public boolean isBoringState () {
    return ss.isBoring();
  }

  public boolean hasPendingException () {
    return (getPendingException() != null);
  }

  public abstract boolean isDeadlocked ();
  
  public Exception getException () {
    return ss.getUncaughtException();
  }



  /**
   * get the numeric id for the current state
   * Note: this can be called several times (by the search and observers) for
   * every forward()/backtrack(), so we want to cache things a bit
   */
  public int getStateId() {
    return ss.getId();
  }

  public int getStateCount() {
    return newStateId;
  }


  /**
   * <2do> this is a band aid to bundle all these legacy reference chains
   * from JPFs past. The goal is to replace them with proper accessors (normally
   * through ThreadInfo, MJIEnv or VM, which all act as facades) wherever possible,
   * and use VM.getVM() where there is no access to such a facade. Once this
   * has been completed, we can start refactoring the users of VM.getVM() to
   * get access to a suitable facade. 
   */
  public static VM getVM () {
    return vm;
  }

  /**
   * not ideal to have this here since it is kind of a backlink, but it's not
   * any better if listeners have to dig this out from JPF
   * Note - this isn't set during initialization, since the VM object is created first
   */
  public Search getSearch() {
    return jpf.getSearch();
  }
  
  /**
   * pushClinit all our static fields. Called from <clinit> and reset
   */
  static void initStaticFields () {
    error_id = 0;
  }

  /**
   *  given an object reference, it returns the ApplicationContext of the process to which
   *  this object belongs
   */
  public abstract ApplicationContext getCurrentApplicationContext();
  public abstract ApplicationContext getApplicationContext(int objRef);
  public abstract ApplicationContext[] getApplicationContexts();
  public abstract String getSUTName();
  public abstract String getSUTDescription();

  public abstract int getNumberOfApplications();
  
  public Heap getHeap() {
    return ss.getHeap();
  }

  public ElementInfo getElementInfo(int objref){
    return ss.getHeap().get(objref);
  }

  public ElementInfo getModifiableElementInfo(int objref){
    return ss.getHeap().getModifiable(objref);
  }

  
  public ThreadInfo getCurrentThread () {
    return ThreadInfo.currentThread;
  }
  
  public void registerClassLoader(ClassLoaderInfo cl) {
    this.getKernelState().addClassLoader(cl);
  }

  public int registerThread (ThreadInfo ti){
    getKernelState().changed();
    return getThreadList().add(ti);    
  }

  /**
   * Returns the ClassLoader with the given globalId
   */
  protected ClassLoaderInfo getClassLoader(int gid) {
    return ss.ks.getClassLoader(gid);
  }

  /**
   * <2do> this is where we will hook in a better time model
   */
  public long currentTimeMillis () {
    return timeModel.currentTimeMillis();
  }

  /**
   * <2do> this is where we will hook in a better time model
   */
  public long nanoTime() {
    return timeModel.nanoTime();
  }

  public void resetNextCG() {
    if (ss.nextCg != null) {
      ss.nextCg.reset();
    }
  }
  
  /**
   * only for debugging, this is expensive
   *
   * If this is a store (forward) this is called before the state is stored.
   *
   * If this is a restore (visited forward or backtrack), this is called after
   * the state got restored
   */
  public void checkConsistency(boolean isStateStore) {
    getThreadList().checkConsistency( isStateStore);
    getHeap().checkConsistency( isStateStore);
  }
  
  public abstract void terminateProcess (ThreadInfo ti);
  
  // this is invoked by the heap (see GenericHeap.newInternString()) upon creating
  // the very first intern string
  public abstract Map<Integer,IntTable<String>> getInitialInternStringsMap();
  
  // ---------- Predicates used to query threads from ThreadList ---------- //
  
  public abstract Predicate<ThreadInfo> getRunnablePredicate();
  
  public abstract Predicate<ThreadInfo> getDaemonRunnablePredicate();
  
  public abstract Predicate<ThreadInfo> getAppTimedoutRunnablePredicate();
  
  public Predicate<ThreadInfo> getUserTimedoutRunnablePredicate () {
    return userTimedoutRunnablePredicate;
  }
  
  public Predicate<ThreadInfo> getUserLiveNonDaemonPredicate() {
    return userliveNonDaemonPredicate;
  }
  
  public Predicate<ThreadInfo> getTimedoutRunnablePredicate () {
    return timedoutRunnablePredicate;
  }
  
  public Predicate<ThreadInfo> getAlivePredicate () {
    return alivePredicate;
  }
  
  
  // ---------- Methods for handling finalizers ---------- //
    
  public FinalizerThreadInfo getFinalizerThread() {
    return getCurrentApplicationContext().getFinalizerThread();
  }
  
  abstract void updateFinalizerQueues();
  
  public void processFinalizers() {
    if(processFinalizers) {
      updateFinalizerQueues();
      ChoiceGenerator<?> cg = getNextChoiceGenerator();
      if(cg==null || (cg.isSchedulingPoint() && !cg.isCascaded())) {
        getFinalizerThread().scheduleFinalizer();
      }
    }
  }
}
