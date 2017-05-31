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

import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.report.Reporter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.tool.RunJPF;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LogManager;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.RunRegistry;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.NoOutOfMemoryErrorProperty;
import gov.nasa.jpf.vm.VMListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * main class of the JPF verification framework. This reads the configuration,
 * instantiates the Search and VM objects, and kicks off the Search
 */
public class JPF implements Runnable {
  
  public static String VERSION = "8.0"; // the major version number

  static Logger logger     = null; // initially

  public enum Status { NEW, RUNNING, DONE };

  class ConfigListener implements ConfigChangeListener {
    
    /**
     * check for new listeners that are dynamically configured
     */
    @Override
    public void propertyChanged(Config config, String key, String oldValue, String newValue) {
      if ("listener".equals(key)) {
        if (oldValue == null)
          oldValue = "";
        
        String[] nv = config.asStringArray(newValue);
        String[] ov = config.asStringArray(oldValue);
        String[] newListeners = Misc.getAddedElements(ov, nv);
        Class<?>[] argTypes = { Config.class, JPF.class };          // Many listeners have 2 parameter constructors
        Object[] args = {config, JPF.this };
        
        if (newListeners != null) {
          for (String clsName : newListeners) {
            try {
              JPFListener newListener = config.getInstance("listener", clsName, JPFListener.class, argTypes, args);
              addListener(newListener);
              logger.info("config changed, added listener " + clsName);

            } catch (JPFConfigException cfx) {
              logger.warning("listener change failed: " + cfx.getMessage());
            }
          }
        }
      }
    }
    
    /**
     * clean up to avoid a sublte but serious memory leak when using the
     * same config for multiple JPF objects/runs - this listener is an inner
     * class object that keeps its encapsulating JPF instance alive
     */
    @Override
    public void jpfRunTerminated(Config config){
      config.removeChangeListener(this);
    }
  }
  
  /** this is the backbone of all JPF configuration */
  Config config;
  
  /** The search policy used to explore the state space */
  Search search;

  /** Reference to the virtual machine used by the search */
  VM vm;

  /** the report generator */
  Reporter reporter;

  Status status = Status.NEW;

  /** a list of listeners that get automatically added from VM, Search or Reporter initialization */
  List<VMListener> pendingVMListeners;
  List<SearchListener> pendingSearchListeners;

  
  /** we use this as safety margin, to be released upon OutOfMemoryErrors */
  byte[] memoryReserve;
  
  private static Logger initLogging(Config conf) {
    LogManager.init(conf);
    return getLogger("gov.nasa.jpf");
  }

  /**
   * use this one to get a Logger that is initialized via our Config mechanism. Note that
   * our own Loggers do NOT pass
   */
  public static JPFLogger getLogger (String name) {
    return LogManager.getLogger( name);
  }

  public static void main(String[] args){
    int options = RunJPF.getOptions(args);

    if (args.length == 0 || RunJPF.isOptionEnabled( RunJPF.HELP,options)) {
      RunJPF.showUsage();
      return;
    }
    if (RunJPF.isOptionEnabled( RunJPF.ADD_PROJECT,options)){
      RunJPF.addProject(args);
      return;
    }
    
    if (RunJPF.isOptionEnabled( RunJPF.BUILD_INFO,options)){
      RunJPF.showBuild(RunJPF.class.getClassLoader());
    }

    if (RunJPF.isOptionEnabled( RunJPF.LOG,options)){
      Config.enableLogging(true);
    }

    Config conf = createConfig(args);

    if (RunJPF.isOptionEnabled( RunJPF.SHOW, options)) {
      conf.printEntries();
    }

    start(conf, args);
  }

  public static void start(Config conf, String[] args){
    // this is redundant to jpf.report.<publisher>.start=..config..
    // but nobody can remember this (it's only used to produce complete reports)

    if (logger == null) {
      logger = initLogging(conf);
    }

    if (!checkArgs(args)){
      return;
    }

    setNativeClassPath(conf); // in case we have to find a shell

    // check if there is a shell class specification, in which case we just delegate
    JPFShell shell = conf.getInstance("shell", JPFShell.class);
    if (shell != null) {
      shell.start(args); // responsible for exception handling itself

    } else {
      // no shell, we start JPF directly
      LogManager.printStatus(logger);
      conf.printStatus(logger);

      // this has to be done after we checked&consumed all "-.." arguments
      // this is NOT about checking properties!
      checkUnknownArgs(args);

      try {
        JPF jpf = new JPF(conf);
        jpf.run();

      } catch (ExitException x) {
        logger.severe( "JPF terminated");

        // this is how we get most runtime config exceptions
        if (x.shouldReport()) {
          x.printStackTrace();
        }

        /**
        Throwable cause = x.getCause();
        if ((cause != null) && conf.getBoolean("pass_exceptions", false)) {
          cause.fillInStackTrace();
          throw cause;
        }
        **/

      } catch (JPFException jx) {
        logger.severe( "JPF exception, terminating: " + jx.getMessage());
        if (conf.getBoolean("jpf.print_exception_stack")) {

          Throwable cause = jx.getCause();
          if (cause != null && cause != jx){
            cause.printStackTrace();
          } else {
            jx.printStackTrace();
          }
        }
        // pass this on, caller has to handle
        throw jx;
      }
    }
  }

  
  static void setNativeClassPath(Config config) {
    if (!config.hasSetClassLoader()) {
      config.initClassLoader( JPF.class.getClassLoader());
    }
  }


  protected JPF (){
    // just to support unit test mockups
  }

  /**
   * create new JPF object. Note this is always guaranteed to return, but the
   * Search and/or VM object instantiation might have failed (i.e. the JPF
   * object might not be really usable). If you directly use getSearch() / getVM(),
   * check for null
   */
  public JPF(Config conf) {
    config = conf;

    setNativeClassPath(config); // before we do anything else

    if (logger == null) { // maybe somebody created a JPF object explicitly
      logger = initLogging(config);
    }

    initialize();
  }

  /**
   * convenience method if caller doesn't care about Config
   */
  public JPF (String ... args) {
    this( createConfig(args));
  }
  
  private void initialize() {
    VERSION = config.getString("jpf.version", VERSION);
    memoryReserve = new byte[config.getInt("jpf.memory_reserve", 64 * 1024)]; // in bytes
    
    try {
      
      Class<?>[] vmArgTypes = { JPF.class, Config.class };
      Object[] vmArgs = { this, config };
      vm = config.getEssentialInstance("vm.class", VM.class, vmArgTypes, vmArgs);

      Class<?>[] searchArgTypes = { Config.class, VM.class };
      Object[] searchArgs = { config, vm };
      search = config.getEssentialInstance("search.class", Search.class,
                                                searchArgTypes, searchArgs);

      // although the Reporter will always be notified last, this has to be set
      // first so that it can register utility listeners like Statistics that
      // can be used by configured listeners
      Class<?>[] reporterArgTypes = { Config.class, JPF.class };
      Object[] reporterArgs = { config, this };
      reporter = config.getInstance("report.class", Reporter.class, reporterArgTypes, reporterArgs);
      if (reporter != null){
        search.setReporter(reporter);
      }
      
      addListeners();
      
      config.addChangeListener(new ConfigListener());
      
    } catch (JPFConfigException cx) {
      logger.severe(cx.toString());
      //cx.getCause().printStackTrace();      
      throw new ExitException(false, cx);
    }
  }  

  
  public Status getStatus() {
    return status;
  }
  
  public boolean isRunnable () {
    return ((vm != null) && (search != null));
  }

  public void addPropertyListener (PropertyListenerAdapter pl) {
    if (vm != null) {
      vm.addListener( pl);
    }
    if (search != null) {
      search.addListener( pl);
      search.addProperty(pl);
    }
  }

  public void addSearchListener (SearchListener l) {
    if (search != null) {
      search.addListener(l);
    }
  }

  protected void addPendingVMListener (VMListener l){
    if (pendingVMListeners == null){
      pendingVMListeners = new ArrayList<VMListener>();
    }
    pendingVMListeners.add(l);
  }
  
  protected void addPendingSearchListener (SearchListener l){
    if (pendingSearchListeners == null){
      pendingSearchListeners = new ArrayList<SearchListener>();
    }
    pendingSearchListeners.add(l);
  }
  
  public void addListener (JPFListener l) {    
    // the VM is instantiated first
    if (l instanceof VMListener) {
      if (vm != null) {
        vm.addListener( (VMListener) l);
        
      } else { // no VM yet, we are in VM initialization
        addPendingVMListener((VMListener)l);
      }
    }
    
    if (l instanceof SearchListener) {
      if (search != null) {
        search.addListener( (SearchListener) l);
        
      } else { // no search yet, we are in Search initialization
        addPendingSearchListener((SearchListener)l);
      }
    }
  }

  public <T> T getListenerOfType( Class<T> type){
    if (search != null){
      T listener = search.getNextListenerOfType(type, null);
      if (listener != null){
        return listener;
      }
    }
    
    if (vm != null){
      T listener = vm.getNextListenerOfType(type, null);
      if (listener != null){
        return listener;
      }
    }
    
    return null;
  }
  
  public boolean addUniqueTypeListener (JPFListener l) {
    boolean addedListener = false;
    Class<?> cls = l.getClass();
    
    if (l instanceof VMListener) {
      if (vm != null) {
        if (!vm.hasListenerOfType(cls)) {
          vm.addListener( (VMListener) l);
          addedListener = true;
        }
      }
    }
    if (l instanceof SearchListener) {
      if (search != null) {
        if (!search.hasListenerOfType(cls)) {
          search.addListener( (SearchListener) l);
          addedListener = true;
        }
      }
    }

    return addedListener;
  }
  
  
  public void removeListener (JPFListener l){
    if (l instanceof VMListener) {
      if (vm != null) {
        vm.removeListener( (VMListener) l);
      }
    }
    if (l instanceof SearchListener) {
      if (search != null) {
        search.removeListener( (SearchListener) l);
      }
    }
  }

  public void addVMListener (VMListener l) {
    if (vm != null) {
      vm.addListener(l);
    }
  }

  public void addSearchProperty (Property p) {
    if (search != null) {
      search.addProperty(p);
    }
  }
    
  /**
   * this is called after vm, search and reporter got instantiated
   */
  void addListeners () {
    Class<?>[] argTypes = { Config.class, JPF.class };
    Object[] args = { config, this };

    // first listeners that were automatically added from VM, Search and Reporter initialization
    if (pendingVMListeners != null){
      for (VMListener l : pendingVMListeners) {
       vm.addListener(l);
      }      
      pendingVMListeners = null;
    }
    
    if (pendingSearchListeners != null){
      for (SearchListener l : pendingSearchListeners) {
       search.addListener(l);
      }
      pendingSearchListeners = null;
    }
    
    // and finally everything that's user configured
    List<JPFListener> listeners = config.getInstances("listener", JPFListener.class, argTypes, args);
    if (listeners != null) {
      for (JPFListener l : listeners) {
        addListener(l);
      }
    }
  }

  public Reporter getReporter () {
    return reporter;
  }

  public <T extends Publisher> boolean addPublisherExtension (Class<T> pCls, PublisherExtension e) {
    if (reporter != null) {
      return reporter.addPublisherExtension(pCls, e);
    }
    return false;
  }

  public <T extends Publisher> void setPublisherItems (Class<T> pCls,
                                                        int category, String[] topics) {
    if (reporter != null) {
      reporter.setPublisherItems(pCls, category, topics);
    }
  }


  public Config getConfig() {
    return config;
  }

  /**
   * return the search object. This can be null if the initialization has failed
   */
  public Search getSearch() {
    return search;
  }

  /**
   * return the VM object. This can be null if the initialization has failed
   */
  public VM getVM() {
    return vm;
  }

  public static void exit() {
    // Hmm, exception as non local return. But we might be called from a
    // context we don't want to kill
    throw new ExitException();
  }

  public boolean foundErrors() {
    return !(search.getErrors().isEmpty());
  }

  /**
   * this assumes that we have checked and 'consumed' (nullified) all known
   * options, so we just have to check for any '-' option prior to the
   * target class name
   */
  static void checkUnknownArgs (String[] args) {
    for ( int i=0; i<args.length; i++) {
      if (args[i] != null) {
        if (args[i].charAt(0) == '-') {
          logger.warning("unknown command line option: " + args[i]);
        }
        else {
          // this is supposed to be the target class name - everything that follows
          // is supposed to be processed by the program under test
          break;
        }
      }
    }
  }

  public static void printBanner (Config config) {
    System.out.println("Java Pathfinder core system v" +
                  config.getString("jpf.version", VERSION) +
                  " - (C) 2005-2014 United States Government. All rights reserved.");
  }


  /**
   * find the value of an arg that is either specific as
   * "-key=value" or as "-key value". If not found, the supplied
   * defValue is returned
   */
  static String getArg(String[] args, String pattern, String defValue, boolean consume) {
    String s = defValue;

    if (args != null){
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];

        if (arg != null) {
          if (arg.matches(pattern)) {
            int idx=arg.indexOf('=');
            if (idx > 0) {
              s = arg.substring(idx+1);
              if (consume) {
                args[i]=null;
              }
            } else if (i < args.length-1) {
              s = args[i+1];
              if (consume) {
                args[i] = null;
                args[i+1] = null;
              }
            }
            break;
          }
        }
      }
    }
    
    return s;
  }

  /**
   * what property file to look for
   */
  static String getConfigFileName (String[] args) {
    if (args.length > 0) {
      // check if the last arg is a mode property file
      // if yes, it has to include a 'target' spec
      int idx = args.length-1;
      String lastArg = args[idx];
      if (lastArg.endsWith(".jpf") || lastArg.endsWith(".properties")) {
        if (lastArg.startsWith("-c")) {
          int i = lastArg.indexOf('=');
          if (i > 0) {
            lastArg = lastArg.substring(i+1);
          }
        }
        args[idx] = null;
        return lastArg;
      }
    }
    
    return getArg(args, "-c(onfig)?(=.+)?", "jpf.properties", true);
  }

  /**
   * return a Config object that holds the JPF options. This first
   * loads the properties from a (potentially configured) properties file, and
   * then overlays all command line arguments that are key/value pairs
   */
  public static Config createConfig (String[] args) {
    return new Config(args);
  }
  
  /**
   * runs the verification.
   */
  @Override
  public void run() {
    Runtime rt = Runtime.getRuntime();

    // this might be executed consecutively, so notify everybody
    RunRegistry.getDefaultRegistry().reset();

    if (isRunnable()) {
      try {
        if (vm.initialize()) {
          status = Status.RUNNING;
          search.search();
        }
      } catch (OutOfMemoryError oom) {
        
        // try to get memory back before we do anything that makes it worse
        // (note that we even try to avoid calls here, we are on thin ice)

        // NOTE - we don't try to recover at this point (that is what we do
        // if we fall below search.min_free within search()), we only want to
        // terminate gracefully (incl. report)

        memoryReserve = null; // release something
        long m0 = rt.freeMemory();
        long d = 10000;
        
        // see if we can reclaim some memory before logging or printing statistics
        for (int i=0; i<10; i++) {
          rt.gc();
          long m1 = rt.freeMemory();
          if ((m1 <= m0) || ((m1 - m0) < d)) {
            break;
          }
          m0 = m1;
        }
        
        logger.severe("JPF out of memory");

        // that's questionable, but we might want to see statistics / coverage
        // to know how far we got. We don't inform any other listeners though
        // if it throws an exception we bail - we can't handle it w/o memory
        try {
          search.notifySearchConstraintHit("JPF out of memory");
          search.error(new NoOutOfMemoryErrorProperty());            // JUnit tests will succeed if OOM isn't flagged.
          reporter.searchFinished(search);
        } catch (Throwable t){
          throw new JPFListenerException("exception during out-of-memory termination", t);
        }
        
      // NOTE - this is not an exception firewall anymore

      } finally {
        status = Status.DONE;

        config.jpfRunTerminated();
        cleanUp();        
      }
    }
  }
  
  protected void cleanUp(){
    search.cleanUp();
    vm.cleanUp();
    reporter.cleanUp();
  }
  
  public List<Error> getSearchErrors () {
    if (search != null) {
      return search.getErrors();
    }

    return null;
  }

  public Error getLastError () {
    if (search != null) {
      return search.getLastError();
    }

    return null;
  }
  
  
  // some minimal sanity checks
  static boolean checkArgs (String[] args){
    String lastArg = args[args.length-1];
    if (lastArg != null && lastArg.endsWith(".jpf")){
      if (!new File(lastArg).isFile()){
        logger.severe("application property file not found: " + lastArg);
        return false;
      }
    }

    return true;
  }

  public static void handleException(JPFException e) {
    logger.severe(e.getMessage());
    exit();
  }

  /**
   * private helper class for local termination of JPF (without killing the
   * whole Java process via System.exit).
   * While this is basically a bad non-local goto exception, it seems to be the
   * least of evils given the current JPF structure, and the need to terminate
   * w/o exiting the whole Java process. If we just do a System.exit(), we couldn't
   * use JPF in an embedded context
   */
  @SuppressWarnings("serial")
  public static class ExitException extends RuntimeException {
    boolean report = true;
    
    ExitException() {}
    
    ExitException (boolean report, Throwable cause){
      super(cause);
      
      this.report = report;
    }
    
    ExitException(String msg) {
      super(msg);
    }
    
    public boolean shouldReport() {
      return report;
    }
  }
}
