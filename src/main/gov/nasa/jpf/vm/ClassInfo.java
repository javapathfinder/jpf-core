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
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.util.ImmutableList;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LocationSpec;
import gov.nasa.jpf.util.MethodSpec;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.OATHash;
import gov.nasa.jpf.util.Source;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;


/**
 * Describes the VM's view of a java class.  Contains descriptions of the
 * static and dynamic fields, declaredMethods, and information relevant to the
 * class.
 * 
 * Note that ClassInfos / classes have three different construction/initialization steps:
 * (1) construction : recursively via ClassLoaderInfo.getResolvedClassInfo -> ClassFileContainer.createClassInfo
 *     -> ClassInfo ctor -> resolveClass
 *     this only creates the ClassInfo object, but it is not visible/usable from SUT code yet and hence not
 *     observable from classLoaded() listeners
 * (2) registration : create StaticElementInfo and add it to the respective ClassLoaderInfo statics, then create
 *     the java.lang.Class companion object in the SUT
 *     this makes the ClassInfo usable from SUT code
 * (3) initialization : execute clinit (if the class has one)
 * 
 * Note that id/uniqueId are NOT set before registration takes place, and registration is not automatically performed since
 * listeners/peers might create ClassInfos internally (e.g. for inspection), which should not be visible from the SUT or observable
 * by other listeners.
 * 
 * Automatic registration from the ClassInfo ctors would require to pass a ThreadInfo context throughout the whole ClassLoaderInfo/
 * ClassFileContainer/ClassInfo chain and could lead to false positives for sharedness based POR, which would record this
 * thread as referencing even if this is a listener/peer internal request
 */
public class ClassInfo extends InfoObject implements Iterable<MethodInfo>, GenericSignatureHolder {

  //--- ClassInfo states, in chronological order
  // note the somewhat strange, decreasing values - >= 0 (=thread-id) means 
  // we are in clinit
  // ideally, we would have a separate RESOLVED state, but (a) this is somewhat
  // orthogonal to REGISTERED, and - more importantly - (b) we need the
  // superClass instance when initializing our Fields (instance field offsets).
  // Doing the field initialization during resolveReferencedClass() seems awkward and
  // error prone (there is not much you can do with an unresolved class then)
  
  // not registered or clinit'ed (but cached in loadedClasses)
  public static final int UNINITIALIZED = -1;
  // 'REGISTERED' simply means 'sei' is set (we have a StaticElementInfo)
  // 'INITIALIZING' is any number >=0, which is the thread objRef that executes the clinit
  public static final int INITIALIZED = -2;

  protected static final String ID_FIELD = "nativeId"; 

  protected static JPFLogger logger = JPF.getLogger("class");

  protected static int nClassInfos; // for statistics
  
  protected static Config config;

  /**
   * ClassLoader that loaded this class.
   */
  protected static final ClassLoader thisClassLoader = ClassInfo.class.getClassLoader();  
  
  /**
   * our abstract factory to createAndInitialize object and class fields
   */
  protected static FieldsFactory fieldsFactory;

  
  protected static final FieldInfo[] EMPTY_FIELDINFO_ARRAY = new FieldInfo[0];
  protected static final String[] EMPTY_STRING_ARRAY = new String[0];
  protected static final String UNINITIALIZED_STRING = "UNINITIALIZED"; 
  protected static final Map<String,MethodInfo> NO_METHODS = Collections.emptyMap();
  protected static final Set<ClassInfo> NO_INTERFACES = new HashSet<ClassInfo>();
  
  /**
   * support to auto-load listeners from annotations
   */
  protected static HashSet<String> autoloadAnnotations;
  protected static HashSet<String> autoloaded;

  /**
   * Name of the class. e.g. "java.lang.String"
   * NOTE - this is the expanded name for builtin types, e.g. "int", but NOT
   * for arrays, which are for some reason in Ldot notation, e.g. "[Ljava.lang.String;" or "[I"
   */
  protected String name;
  
  /** type erased signature of the class. e.g. "Ljava/lang/String;" */
  protected String signature;

  /** Generic type signatures of the class as per para. 4.4.4 of the revised VM spec */
  protected String genericSignature;

  /** The classloader that defined (directly loaded) this class */
  protected ClassLoaderInfo classLoader;
  
  // various class attributes
  protected boolean      isClass = true;
  protected boolean      isWeakReference = false;
  protected boolean      isObjectClassInfo = false;
  protected boolean      isStringClassInfo = false;
  protected boolean      isThreadClassInfo = false;
  protected boolean      isRefClassInfo = false;
  protected boolean      isArray = false;
  protected boolean      isEnum = false;
  protected boolean      isReferenceArray = false;
  protected boolean      isAbstract = false;
  protected boolean      isBuiltin = false;

  // that's ultimately where we keep the attributes
  // <2do> this is currently quite redundant, but these are used in reflection
  protected int modifiers;

  protected MethodInfo   finalizer = null;

  /** type based object attributes (for GC, partial order reduction and
   * property checks)
   */
  protected int elementInfoAttrs = 0;

  /**
   * all our declared declaredMethods (we don't flatten, this is not
   * a high-performance VM)
   */
  protected Map<String, MethodInfo> methods;

  /**
   * our instance fields.
   * Note these are NOT flattened, idx.e. only contain the declared ones
   */
  protected FieldInfo[] iFields;

  /** the storage size of instances of this class (stored as an int[]) */
  protected int instanceDataSize;

  /** where in the instance data array (int[]) do our declared fields start */
  protected int instanceDataOffset;

  /** total number of instance fields (flattened, not only declared ones) */
  protected int nInstanceFields;

  /**
   * our static fields. Again, not flattened
   */
  protected FieldInfo[] sFields;

  /** the storage size of static fields of this class (stored as an int[]) */
  protected int staticDataSize;

  /**
   * we only set the superClassName upon creation, it is instantiated into
   * a ClassInfo by resolveReferencedClass(), which is required to be called before
   * we can createAndInitialize objects of this type
   */
  protected ClassInfo  superClass;
  protected String superClassName;

  protected String enclosingClassName;
  protected String enclosingMethodName;

  protected String[] innerClassNames = EMPTY_STRING_ARRAY;
  protected BootstrapMethodInfo[] bootstrapMethods;
    
  /** direct ifcs implemented by this class */
  protected String[] interfaceNames;

  protected Set<ClassInfo> interfaces = new HashSet<ClassInfo>();
  
  /** cache of all interfaceNames (parent interfaceNames and interface parents) - lazy eval */
  protected Set<ClassInfo> allInterfaces;
  
  /** Name of the package. */
  protected String packageName;

  /** this is only set if the classfile has a SourceFile class attribute */
  protected String sourceFileName;

  /** 
   * Uniform resource locater for the class file. NOTE: since for builtin classes
   * there is no class file assigned is set to the typeName 
   */ 
  protected String classFileUrl;

  /** from where the corresponding classfile was loaded (if this is not a builtin) */
  protected gov.nasa.jpf.vm.ClassFileContainer container;

  
  /**
   *  a search global numeric id that is only unique within this ClassLoader namespace. Ids are
   *  computed by the ClassLoaderInfo/Statics implementation during ClassInfo registration
   */
  protected int  id = -1;

  /**
   * A search global unique id associate with this class, which is comprised of the classLoader id
   * and the (loader-specific) ClassInfo id. This is just a quick way to do search global checks for equality
   * 
   * NOTE - since this is based on the classloader-specific id, it can't be used before the ClassInfo is registered
   */
  protected long uniqueId = -1;

  /**
   * this is the object we use to enter declaredMethods in the underlying VM
   * (it replaces Reflection)
   */
  protected NativePeer nativePeer;

  /** Source file associated with the class.*/
  protected Source source;

  protected boolean enableAssertions;

  /** actions to be taken when an object of this type is gc'ed */
  protected ImmutableList<ReleaseAction> releaseActions; 
          
  
  static boolean init (Config config) {

    ClassInfo.config = config;
    
    setSourceRoots(config);
    //buildBCELModelClassPath(config);

    fieldsFactory = config.getEssentialInstance("vm.fields_factory.class",
                                                FieldsFactory.class);

    autoloadAnnotations = config.getNonEmptyStringSet("listener.autoload");
    if (autoloadAnnotations != null) {
      autoloaded = new HashSet<String>();

      if (logger.isLoggable(Level.INFO)) {
        for (String s : autoloadAnnotations){
          logger.info("watching for autoload annotation @" + s);
        }
      }
    }

    return true;
  }

  public static boolean isObjectClassInfo (ClassInfo ci){
    return ci.isObjectClassInfo();
  }

  public static boolean isStringClassInfo (ClassInfo ci){
    return ci.isStringClassInfo();
  }

  
   //--- initialization interface towards parsers (which might reside in other packages)
    
  protected void setClass(String clsName, String superClsName, int flags, int cpCount) throws ClassParseException {
    String parsedName = Types.getClassNameFromTypeName(clsName);

    if (name != null && !name.equals(parsedName)){
      throw new ClassParseException("wrong class name (expected: " + name + ", found: " + parsedName + ')');
    }
    name = parsedName;
    
    // the enclosingClassName is set on demand since it requires loading enclosing class candidates
    // to verify their innerClass attributes

    int i = name.lastIndexOf('.');
    packageName = (i > 0) ? name.substring(0, i) : "";

    modifiers = flags;
    
    // annotations are interfaces too (not exposed by Modifier)
    isClass = ((flags & Modifier.INTERFACE) == 0);

    superClassName = superClsName;
  }

  public void setInnerClassNames(String[] clsNames) {
    innerClassNames = clsNames;
  }

  public void setEnclosingClass (String clsName) {
    enclosingClassName = clsName;
  }
  
  public void setEnclosingMethod (String mthName){
    enclosingMethodName = mthName;    
  }

  public void setInterfaceNames(String[] ifcNames) {
    interfaceNames = ifcNames;
  }
  
  public void setSourceFile (String fileName){
    // prepend if we already know the package
    if (packageName.length() > 0) {
      // Source will take care of proper separator chars later
      sourceFileName = packageName.replace('.', '/') + '/' + fileName;
    } else {
      sourceFileName = fileName;
    }
  }

  public void setFields(FieldInfo[] fields) {
    if (fields == null){
      iFields = EMPTY_FIELDINFO_ARRAY;
      sFields = EMPTY_FIELDINFO_ARRAY;
      
    } else { // there are fields, we have to tell them apart
      int nInstance = 0, nStatic = 0;
      for (int i = 0; i < fields.length; i++) {
        if (fields[i].isStatic()) {
          nStatic++;
        } else {
          nInstance++;
        }
      }

      FieldInfo[] instanceFields = (nInstance > 0) ? new FieldInfo[nInstance] : EMPTY_FIELDINFO_ARRAY;
      FieldInfo[] staticFields = (nStatic > 0) ? new FieldInfo[nStatic] : EMPTY_FIELDINFO_ARRAY;

      int iInstance = 0;
      int iStatic = 0;
      for (int i = 0; i < fields.length; i++) {
        FieldInfo fi = fields[i];

        if (fi.isStatic()) {
          staticFields[iStatic++] = fi;
        } else {
          instanceFields[iInstance++] = fi;
        }
        
        processJPFAnnotations(fi);
      }

      iFields = instanceFields;
      sFields = staticFields;

      // we can't link the fields yet because we need the superclasses to be resolved
    }
  }

  protected void setMethod (MethodInfo mi){
    mi.linkToClass(this);
    methods.put( mi.getUniqueName(), mi);
    processJPFAnnotations(mi);
  }
  
  public void setMethods (MethodInfo[] newMethods) {
    if (newMethods != null && newMethods.length > 0) {
      methods = new LinkedHashMap<String, MethodInfo>();

      for (int i = 0; i < newMethods.length; i++) {
        setMethod( newMethods[i]);
      }
    }
  }
 
  protected void processJPFAttrAnnotation(InfoObject infoObj){
    AnnotationInfo ai = infoObj.getAnnotation("gov.nasa.jpf.annotation.JPFAttribute");
    if (ai != null){
      String[] attrTypes = ai.getValueAsStringArray();
      if (attrTypes != null){
        ClassLoader loader = config.getClassLoader();

        for (String clsName : attrTypes){
          try {
            Class<?> attrCls = loader.loadClass(clsName);
            Object attr = attrCls.newInstance(); // needs to have a default ctor
            infoObj.addAttr(attr);
            
          } catch (ClassNotFoundException cnfx){
            logger.warning("attribute class not found: " + clsName);
            
          } catch (IllegalAccessException iax){
            logger.warning("attribute class has no public default ctor: " + clsName);            
            
          } catch (InstantiationException ix){
            logger.warning("attribute class has no default ctor: " + clsName);            
          }
        }
      }
    }    
  }

  protected void processNoJPFExecutionAnnotation(InfoObject infoObj) {
    AnnotationInfo ai = infoObj.getAnnotation("gov.nasa.jpf.annotation.NoJPFExecution");
    if (ai != null) {
      infoObj.addAttr(NoJPFExec.SINGLETON);
    }
  }

  protected void processJPFAnnotations(InfoObject infoObj) {
    processJPFAttrAnnotation(infoObj);
    processNoJPFExecutionAnnotation(infoObj);
  }

    public AnnotationInfo getResolvedAnnotationInfo (String typeName){
    return classLoader.getResolvedAnnotationInfo( typeName);
  }
  
  @Override
  public void setAnnotations(AnnotationInfo[] annotations) {
    this.annotations = annotations;
  }
  
  //--- end initialization interface
 
  //--- the overridden annotation accessors (we need these because of inherited superclass annotations)
  // note that we don't flatten annotations anymore, assuming the prevalent query will be getAnnotation(name)
  
  @Override
  public boolean hasAnnotations(){
    if (annotations.length > 0){
      return true;
    }
    
    for (ClassInfo ci = superClass; ci != null; ci = ci.superClass){
      AnnotationInfo[] a = ci.annotations;
      for (int j=0; j<a.length; j++){
        if (a[j].isInherited()){
          return true;
        }
      }
    }
    
    return false;
  }
  
  /**
   * return all annotations, which includes the ones inherited from our superclasses
   * NOTE - this is not very efficient
   */
  @Override
  public AnnotationInfo[] getAnnotations() {
    int nAnnotations = annotations.length;
    for (ClassInfo ci = superClass; ci != null; ci = ci.superClass){
      AnnotationInfo[] a = ci.annotations;
      for (int i=0; i<a.length; i++){
        if (a[i].isInherited()){
          nAnnotations++;
        }
      }
    }
    
    AnnotationInfo[] allAnnotations = new AnnotationInfo[nAnnotations];
    System.arraycopy(annotations, 0, allAnnotations, 0, annotations.length);
    int idx=annotations.length;
    for (ClassInfo ci = superClass; ci != null; ci = ci.superClass){
      AnnotationInfo[] a = ci.annotations;
      for (int i=0; i<a.length; i++){
        if (a[i].isInherited()){
          allAnnotations[idx++] = a[i];
        }
      }
    }
    
    return allAnnotations;
  }
    
  @Override
  public AnnotationInfo getAnnotation (String annotationName){
    AnnotationInfo[] a = annotations;
    for (int i=0; i<a.length; i++){
      if (a[i].getName().equals(annotationName)){
        return a[i];
      }
    }
    
    for (ClassInfo ci = superClass; ci != null; ci = ci.superClass){
      a = ci.annotations;
      for (int i=0; i<a.length; i++){
        AnnotationInfo ai = a[i];
        if (ai.getName().equals(annotationName) && ai.isInherited()){
          return ai;
        }
      }
    }
    
    return null;
  }
  
  protected ClassInfo (String name, ClassLoaderInfo cli, String classFileUrl){
    nClassInfos++;
    
    this.name = name;
    this.classLoader = cli;
    this.classFileUrl = classFileUrl;
    
    this.methods = NO_METHODS;  // yet

    // rest has to be initialized by concrete ctor, which should call resolveAndLink(parser)
  }
  
  /**
   * the initialization part that has to happen once we have super, fields, methods and annotations
   * NOTE - this has to be called by concrete ctors after parsing class files
   */
  protected void resolveAndLink () throws ClassParseException {
    
    //--- these might get streamlined
    isStringClassInfo = isStringClassInfo0();
    isThreadClassInfo = isThreadClassInfo0();
    isObjectClassInfo = isObjectClassInfo0();
    isRefClassInfo = isRefClassInfo0();
   // isWeakReference = isWeakReference0();
    isAbstract = (modifiers & Modifier.ABSTRACT) != 0;
   // isEnum = isEnum0();
    
    finalizer = getFinalizer0();

    resolveClass(); // takes care of super classes and interfaces

    // Used to enter native methods (in the host VM).
    // This needs to be initialized AFTER we get our  MethodInfos, since it does a reverse lookup to determine which
    // ones are handled by the peer (by means of setting MethodInfo attributes)
    nativePeer = loadNativePeer();
    checkUnresolvedNativeMethods();

    linkFields(); // computes field offsets
    
    setAssertionStatus();
    processJPFConfigAnnotation();
    processJPFAnnotations(this);
    loadAnnotationListeners();    
  }
  
  protected ClassInfo(){
    nClassInfos++;
    
    // for explicit subclass initialization
  }
  
  /**
   * ClassInfo ctor used for builtin types (arrays and primitive types)
   * idx.e. classes we don't have class files for
   */
  protected ClassInfo (String builtinClassName, ClassLoaderInfo classLoader) {
    nClassInfos++;

    this.classLoader = classLoader;

    isArray = (builtinClassName.charAt(0) == '[');
    isReferenceArray = isArray && (builtinClassName.endsWith(";") || builtinClassName.charAt(1) == '[');
    isBuiltin = true;

    name = builtinClassName;

    logger.log(Level.FINE, "generating builtin class: %1$s", name);

    packageName = ""; // builtin classes don't reside in java.lang !
    sourceFileName = null;
    source = null;
    genericSignature = "";

    // no fields
    iFields = EMPTY_FIELDINFO_ARRAY;
    sFields = EMPTY_FIELDINFO_ARRAY;

    if (isArray) {
      if(classLoader.isSystemClassLoader()) {
        superClass = ((SystemClassLoaderInfo)classLoader).getObjectClassInfo();
      } else {
        superClass = ClassLoaderInfo.getCurrentSystemClassLoader().getObjectClassInfo();
      }
      interfaceNames = loadArrayInterfaces();
      methods = loadArrayMethods();
    } else {
      superClass = null; // strange, but true, a 'no object' class
      interfaceNames = loadBuiltinInterfaces(name);
      methods = loadBuiltinMethods(name);
    }

    enableAssertions = true; // doesn't really matter - no code associated

    classFileUrl = name;
    
    // no fields or declaredMethods, so we don't have to link/resolve anything
  }
  
  public static int getNumberOfLoadedClasses(){
    return nClassInfos;
  }
  
  //--- the VM type specific methods
  // <2do> those should be abstract
  
  protected void setAnnotationValueGetterCode (MethodInfo pmi, FieldInfo fi){
    // to be overridden by VM specific class
  }
  
  protected void setDirectCallCode (MethodInfo miCallee, MethodInfo miStub){
    // to be overridden by VM specific class
  }
  
  protected void setLambdaDirectCallCode (MethodInfo miDirectCall, BootstrapMethodInfo bootstrapMethod){
    // to be overridden by VM specific class
  }
  
  protected void setNativeCallCode (NativeMethodInfo miNative){
    // to be overridden by VM specific class
  }
  
  protected void setRunStartCode (MethodInfo miStub, MethodInfo miRun){
    // to be overridden by VM specific class
  }
  
  /**
   * createAndInitialize a fully synthetic implementation of an Annotation proxy
   */
  protected ClassInfo (ClassInfo annotationCls, String name, ClassLoaderInfo classLoader, String url) {
    this.classLoader = classLoader;
    
    this.name = name;
    isClass = true;

    //superClass = objectClassInfo;
    superClass = ClassLoaderInfo.getSystemResolvedClassInfo("gov.nasa.jpf.AnnotationProxyBase");

    interfaceNames = new String[]{ annotationCls.name };    
    packageName = annotationCls.packageName;
    sourceFileName = annotationCls.sourceFileName;
    genericSignature = annotationCls.genericSignature;

    sFields = new FieldInfo[0]; // none
    staticDataSize = 0;

    methods = new HashMap<String, MethodInfo>();
    iFields = new FieldInfo[annotationCls.methods.size()];
    nInstanceFields = iFields.length;

    // all accessor declaredMethods of ours make it into iField/method combinations
    int idx = 0;
    int off = 0;  // no super class
    for (MethodInfo mi : annotationCls.getDeclaredMethodInfos()) {
      String mname = mi.getName();
      String mtype = mi.getReturnType();
      String genericSignature = mi.getGenericSignature();

      // create and initialize an instance field for it
      FieldInfo fi = FieldInfo.create(mname, mtype, 0);
      fi.linkToClass(this, idx, off);
      fi.setGenericSignature(genericSignature);
      iFields[idx++] = fi;
      off += fi.getStorageSize();

      MethodInfo pmi = new MethodInfo(this, mname, mi.getSignature(), Modifier.PUBLIC, 1, 2);
      pmi.setGenericSignature(genericSignature);
      
      setAnnotationValueGetterCode( pmi, fi);
      methods.put(pmi.getUniqueName(), pmi);
    }

    instanceDataSize = computeInstanceDataSize();
    instanceDataOffset = 0;

    classFileUrl = url;
    linkFields();
  }
  
  
  //used to create synthetic classes that implement functional interfaces
  protected ClassInfo createFuncObjClassInfo (BootstrapMethodInfo bootstrapMethod, String name, String samUniqueName, String[] fieldTypesName) {
   return null;
 }
 
 protected ClassInfo (ClassInfo funcInterface, BootstrapMethodInfo bootstrapMethod, String name, String[] fieldTypesName) {
   ClassInfo enclosingClass = bootstrapMethod.enclosingClass;
   this.classLoader = enclosingClass.classLoader;

   this.name = name;
   isClass = true;

   superClassName = "java.lang.Object";

   interfaceNames = new String[]{ funcInterface.name };    
   packageName = enclosingClass.getPackageName();

   // creating fields used to capture free variables
   int n = fieldTypesName.length;
   
   iFields = new FieldInfo[n];
   nInstanceFields = n;
   
   sFields = new FieldInfo[0];
   staticDataSize = 0;
   
   int idx = 0;
   int off = 0;  // no super class
   
   int i = 0;
   for(String type: fieldTypesName) {
     FieldInfo fi = FieldInfo.create("arg" + i++, type, 0);
     fi.linkToClass(this, idx, off);
     iFields[idx++] = fi;
     off += fi.getStorageSize();
   }
   
   linkFields();
 }
  
  // since id and hence uniqueId are not set before this class is registered, we can't use them
  
  @Override
  public int hashCode() {
    return OATHash.hash(name.hashCode(), classLoader.hashCode());
  }
  
  @Override
  public boolean equals (Object o) {
    if (o instanceof ClassInfo) {
      ClassInfo other = (ClassInfo)o;
      if (classLoader == other.classLoader) {
        // beware of ClassInfos that are not registered yet - in this case we have to equals names
        if (name.equals(other.name)) {
          return true;
        }
      }
    }
    
    return false;
  }

  protected String computeSourceFileName(){
    return name.replace('.', '/') + ".java";
  }

  protected void checkUnresolvedNativeMethods(){
    for (MethodInfo mi : methods.values()){
      if (mi.isUnresolvedNativeMethod()){
        NativeMethodInfo nmi = new NativeMethodInfo(mi, null, nativePeer);
        nmi.replace(mi);
      }
    }
  }

  protected void processJPFConfigAnnotation() {
    AnnotationInfo ai = getAnnotation("gov.nasa.jpf.annotation.JPFConfig");
    if (ai != null) {
      for (String s : ai.getValueAsStringArray()) {
        config.parse(s);
      }
    }
  }

  protected void loadAnnotationListeners () {
    if (autoloadAnnotations != null) {
      autoloadListeners(annotations); // class annotations

      for (int i=0; i<sFields.length; i++) {
        autoloadListeners(sFields[i].getAnnotations());
      }

      for (int i=0; i<iFields.length; i++) {
        autoloadListeners(iFields[i].getAnnotations());
      }

      // method annotations are checked during method loading
      // (to avoid extra iteration)
    }
  }

  void autoloadListeners(AnnotationInfo[] annos) {
    if ((annos != null) && (autoloadAnnotations != null)) {
      for (AnnotationInfo ai : annos) {
        String aName = ai.getName();
        if (autoloadAnnotations.contains(aName)) {
          if (!autoloaded.contains(aName)) {
            autoloaded.add(aName);
            String key = "listener." + aName;
            String defClsName = aName + "Checker";
            try {
              JPFListener listener = config.getInstance(key, JPFListener.class, defClsName);
              
              JPF jpf = VM.getVM().getJPF(); // <2do> that's a BAD access path
              jpf.addUniqueTypeListener(listener);

              if (logger.isLoggable(Level.INFO)){
                logger.info("autoload annotation listener: @", aName, " => ", listener.getClass().getName());
              }

            } catch (JPFConfigException cx) {
              logger.warning("no autoload listener class for annotation " + aName +
                             " : " + cx.getMessage());
              autoloadAnnotations.remove(aName);
            }
          }
        }
      }

      if (autoloadAnnotations.isEmpty()) {
        autoloadAnnotations = null;
      }
    }
  }

  protected NativePeer loadNativePeer(){
    return NativePeer.getNativePeer(this);
  }
  
  /**
   * Returns the class loader that 
   */
  public ClassLoaderInfo getClassLoaderInfo() {
    return classLoader;
  }

  /**
   * the container this is stored in
   */
  public Statics getStatics() {
    return classLoader.getStatics();
  }
  
  /**
   * required by InfoObject interface
   */
  public ClassInfo getClassInfo() {
    return this;
  }

  protected void setAssertionStatus() {
    if(isInitialized()) {
      return;
    } else {
      enableAssertions = classLoader.desiredAssertionStatus(name);
    }
  }

  boolean getAssertionStatus () {
    return enableAssertions;
  }

  public boolean desiredAssertionStatus() {
    return classLoader.desiredAssertionStatus(name);
  }

  @Override
  public String getGenericSignature() {
    return genericSignature;
  }

  @Override
  public void setGenericSignature(String sig){
    genericSignature = sig;
  }
  
  public boolean isArray () {
    return isArray;
  }

  public boolean isEnum () {
    return isEnum;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public boolean isBuiltin(){
    return isBuiltin;
  }
  
  public boolean isInterface() {
    return ((modifiers & Modifier.INTERFACE) != 0);
  }

  public boolean isReferenceArray () {
    return isReferenceArray;
  }

  public boolean isObjectClassInfo() {
    return isObjectClassInfo;
  }

  public boolean isStringClassInfo() {
    return isStringClassInfo;
  }

  public boolean isThreadClassInfo() {
    return isThreadClassInfo;
  }

  protected void checkNoClinitInitialization(){
    if (!isInitialized()){
      ThreadInfo ti = ThreadInfo.getCurrentThread();
      registerClass(ti);
      setInitialized(); // we might want to check if there is a clinit
    }
  }
  
  protected ClassInfo createAnnotationProxy (String proxyName){
    // to be overridden by VM specific ClassInfos
    return null;
  }
  
  public ClassInfo getAnnotationProxy (){
    // <2do> test if this is a annotation ClassInfo
    
    checkNoClinitInitialization(); // annotation classes don't have clinits
    
    ClassInfo ciProxy = classLoader.getResolvedAnnotationProxy(this);
    ciProxy.checkNoClinitInitialization();
    
    return ciProxy;
  }
  
/**
  public static ClassInfo getAnnotationProxy (ClassInfo ciAnnotation){
    ThreadInfo ti = ThreadInfo.getCurrentThread();

    // make sure the annotationCls is initialized (no code there)
    if (!ciAnnotation.isInitialized()) {
      ciAnnotation.registerClass(ti);
      ciAnnotation.setInitialized(); // no clinit
    }

    String url = computeProxyUrl(ciAnnotation);
    ClassInfo ci = null; // getOriginalClassInfo(url);

    if (ci == null){
      String cname = ciAnnotation.getName() + "$Proxy";
      ci = new ClassInfo(ciAnnotation, cname, ciAnnotation.classLoader, url);
      ciAnnotation.classLoader.addResolvedClass(ci);
      if (!ci.isInitialized()){
        ci.registerClass(ti);
        ci.setInitialized();
      }
    }

    return ci;
  }
**/

  public boolean areAssertionsEnabled() {
    return enableAssertions;
  }

  public boolean hasInstanceFields () {
    return (instanceDataSize > 0);
  }

  public ElementInfo getClassObject(){
    StaticElementInfo sei = getStaticElementInfo();
    
    if (sei != null){
      int objref = sei.getClassObjectRef();
      return VM.getVM().getElementInfo(objref);
    }

    return null;
  }
  
  public ElementInfo getModifiableClassObject(){
    StaticElementInfo sei = getStaticElementInfo();
    
    if (sei != null){
      int objref = sei.getClassObjectRef();
      return VM.getVM().getModifiableElementInfo(objref);
    }

    return null;
  }
  

  public int getClassObjectRef () {
    StaticElementInfo sei = getStaticElementInfo();    
    return (sei != null) ? sei.getClassObjectRef() : MJIEnv.NULL;
  }

  public gov.nasa.jpf.vm.ClassFileContainer getContainer(){
    return container;
  }
  
  public String getClassFileUrl (){
    return classFileUrl;
  }

  //--- type based object release actions
  
  public boolean hasReleaseAction (ReleaseAction action){
    return (releaseActions != null) && releaseActions.contains(action);
  }
  
  /**
   * NOTE - this can only be set *before* subclasses are loaded (e.g. from classLoaded() notification) 
   */
  public void addReleaseAction (ReleaseAction action){
    // flattened in ctor to super releaseActions
    releaseActions = new ImmutableList<ReleaseAction>( action, releaseActions);
  }
  
  /**
   * recursively process release actions registered for this type or any of
   * its super types (only classes). The releaseAction list is flattened during
   * ClassInfo initialization, to reduce runtime overhead during GC sweep
   */
  public void processReleaseActions (ElementInfo ei){
    if (superClass != null){
      superClass.processReleaseActions(ei);
    }
    
    if (releaseActions != null) {
      for (ReleaseAction action : releaseActions) {
        action.release(ei);
      }
    }
  }
  
  public int getModifiers() {
    return modifiers;
  }

  /**
   * Note that 'uniqueName' is the name plus the argument type part of the
   * signature, idx.e. everything that's relevant for overloading
   * (besides saving some const space, we also ease reverse lookup
   * of natives that way).
   * Note also that we don't have to make any difference between
   * class and instance declaredMethods, because that just matters in the
   * INVOKExx instruction, when looking up the relevant ClassInfo to start
   * searching in (either by means of the object type, or by means of the
   * constpool classname entry).
   */
  public MethodInfo getMethod (String uniqueName, boolean isRecursiveLookup) {
    MethodInfo mi = methods.get(uniqueName);

    if ((mi == null) && isRecursiveLookup && (superClass != null)) {
      mi = superClass.getMethod(uniqueName, true);
    }

    return mi;
  }

  /**
   * if we don't know the return type
   * signature is in paren/dot notation
   */
  public MethodInfo getMethod (String name, String signature, boolean isRecursiveLookup) {
    MethodInfo mi = null;
    String matchName = name + signature;

    for (Map.Entry<String, MethodInfo>e : methods.entrySet()) {
      if (e.getKey().startsWith(matchName)){
        mi = e.getValue();
        break;
      }
    }

    if ((mi == null) && isRecursiveLookup && (superClass != null)) {
      mi = superClass.getMethod(name, signature, true);
    }

    return mi;
  }

  
  public MethodInfo getDefaultMethod (String uniqueName) {
    MethodInfo mi = null;
    
    for (ClassInfo ci = this; ci != null; ci = ci.superClass){
      for (ClassInfo ciIfc : ci.interfaces){
        MethodInfo miIfc = ciIfc.getMethod(uniqueName, true);
        if (miIfc != null && !miIfc.isAbstract()){
          if (mi != null && !mi.equals(miIfc)){
            // this has to throw a IncompatibleClassChangeError in the client since Java prohibits ambiguous default methods
            String msg = "Conflicting default methods: " + mi.getFullName() + ", " + miIfc.getFullName();
            throw new ClassChangeException(msg);
          } else {
            mi = miIfc;
          }
        }
      }
    }
    
    return mi;
  }
  
  /**
   * This retrieves the SAM from this functional interface. Note that this is only
   * called on functional interface expecting to have a SAM. This shouldn't expect 
   * this interface to have only one method which is abstract, since:
   *    1. functional interface can declare the abstract methods from the java.lang.Object 
   *       class.
   *    2. functional interface can extend another interface which is functional, but it 
   *       should not declare any new abstract methods.
   *    3. functional interface can have one abstract method and any number of default
   *       methods.
   * 
   * To retrieve the SAM, this method iterates over the methods of this interface and its 
   * superinterfaces, and it returns the first method which is abstract and it does not 
   * declare a method in java.lang.Object.
   */
  public MethodInfo getInterfaceAbstractMethod () {
    ClassInfo objCi = ClassLoaderInfo.getCurrentResolvedClassInfo("java.lang.Object");
    
    for(MethodInfo mi: this.methods.values()) {
      if(mi.isAbstract() && objCi.getMethod(mi.getUniqueName(), false)==null) {
        return mi;
      }
    }
    
    for (ClassInfo ifc : this.interfaces){
      MethodInfo mi = ifc.getInterfaceAbstractMethod();
      if(mi!=null) {
        return mi;
      }
    }
    
    return null;
  }

  /**
   * method lookup for use by reflection methods (java.lang.Class.getXMethod)
   * 
   * note this doesn't specify the return type, which means covariant return 
   * types are not allowed in reflection lookup.
   * 
   * note also this includes interface methods, but only after the inheritance
   * hierarchy has been searched
   */
  public MethodInfo getReflectionMethod (String fullName, boolean isRecursiveLookup) {
        
    // first look for methods within the class hierarchy
    for (ClassInfo ci = this; ci != null; ci = ci.superClass){
      for (Map.Entry<String, MethodInfo>e : ci.methods.entrySet()) {
        String name = e.getKey();
        if (name.startsWith(fullName)) {
          return e.getValue();
        }
      }
      if (!isRecursiveLookup){
        return null;
      }
    }

    // this is the recursive case - if none found, look for interface methods
    for (ClassInfo ci : getAllInterfaces() ){
      for (Map.Entry<String, MethodInfo>e : ci.methods.entrySet()) {
        String name = e.getKey();
        if (name.startsWith(fullName)) {
          return e.getValue();
        }
      }      
    }    

    return null;
  }
  
  /**
   * iterate over all declaredMethods of this class (and it's superclasses), until
   * the provided MethodLocator tells us it's done
   */
  public void matchMethods (MethodLocator loc) {
    for (MethodInfo mi : methods.values()) {
      if (loc.match(mi)) {
        return;
      }
    }
    if (superClass != null) {
      superClass.matchMethods(loc);
    }
  }

  /**
   * iterate over all declaredMethods declared in this class, until the provided
   * MethodLocator tells us it's done
   */
  public void matchDeclaredMethods (MethodLocator loc) {
    for (MethodInfo mi : methods.values()) {
      if (loc.match(mi)) {
        return;
      }
    }
  }

  @Override
  public Iterator<MethodInfo> iterator() {
    return new Iterator<MethodInfo>() {
      ClassInfo ci = ClassInfo.this;
      Iterator<MethodInfo> it = ci.methods.values().iterator();

      @Override
	public boolean hasNext() {
        if (it.hasNext()) {
          return true;
        } else {
          if (ci.superClass != null) {
            ci = ci.superClass;
            it = ci.methods.values().iterator();
            return it.hasNext();
          } else {
            return false;
          }
        }
      }

      @Override
	public MethodInfo next() {
        if (hasNext()) {
          return it.next();
        } else {
          throw new NoSuchElementException();
        }
      }

      @Override
	public void remove() {
        // not supported
        throw new UnsupportedOperationException("can't remove methods");
      }
    };
  }
  
  public Iterator<MethodInfo> declaredMethodIterator() {
    return methods.values().iterator();
  }

  /**
   * Search up the class hierarchy to find a static field
   * @param fName name of field
   * @return null if field name not found (not declared)
   */
  public FieldInfo getStaticField (String fName) {
    FieldInfo fi;
    ClassInfo c = this;

    while (c != null) {
      fi = c.getDeclaredStaticField(fName);
      if (fi != null) {
        return fi;
      }
      c = c.superClass;
    }

    //interfaceNames can have static fields too
    // <2do> why would that not be already resolved here ?
    for (ClassInfo ci : getAllInterfaces()) {
      fi = ci.getDeclaredStaticField(fName);
      if (fi != null) {
        return fi;
      }
    }

    return null;
  }

  public Object getStaticFieldValueObject (String id){
    ClassInfo c = this;
    Object v;

    while (c != null){
      ElementInfo sei = c.getStaticElementInfo();
      v = sei.getFieldValueObject(id);
      if (v != null){
        return v;
      }
      c = c.getSuperClass();
    }

    return null;
  }

  public FieldInfo[] getDeclaredStaticFields() {
    return sFields;
  }

  public FieldInfo[] getDeclaredInstanceFields() {
    return iFields;
  }

  /**
   * FieldInfo lookup in the static fields that are declared in this class
   * <2do> pcm - should employ a map at some point, but it's usually not that
   * important since we can cash the returned FieldInfo in the PUT/GET_STATIC insns
   */
  public FieldInfo getDeclaredStaticField (String fName) {
    for (int i=0; i<sFields.length; i++) {
      if (sFields[i].getName().equals(fName)) return sFields[i];
    }

    return null;
  }

  /**
   * base relative FieldInfo lookup - the workhorse
   * <2do> again, should eventually use Maps
   * @param fName the field name
   */
  public FieldInfo getInstanceField (String fName) {
    FieldInfo fi;
    ClassInfo c = this;

    while (c != null) {
      fi = c.getDeclaredInstanceField(fName);
      if (fi != null) return fi;
      c = c.superClass;
    }

    return null;
  }

  /**
   * FieldInfo lookup in the fields that are declared in this class
   */
  public FieldInfo getDeclaredInstanceField (String fName) {
    for (int i=0; i<iFields.length; i++) {
      if (iFields[i].getName().equals(fName)) return iFields[i];
    }

    return null;
  }
  
  public String getSignature() {
    if (signature == null) {
      signature = Types.getTypeSignature(name, false);
    }
    
    return signature;     
  }

  /**
   * Returns the name of the class.  e.g. "java.lang.String".  similar to
   * java.lang.Class.getName().
   */
  public String getName () {
    return name;
  }

  public String getSimpleName () {
    int i;
    String enclosingClassName = getEnclosingClassName();
    
    if(enclosingClassName!=null){
      i = enclosingClassName.length();      
    } else{
      i = name.lastIndexOf('.');
    }
    
    return name.substring(i+1);
  }

  public String getPackageName () {
    return packageName;
  }

  public int getId() {
    return id;
  }

  public long getUniqueId() {
    return uniqueId;
  }

  public int getFieldAttrs (int fieldIndex) {
    fieldIndex = 0; // Get rid of IDE warning
     
    return 0;
  }

  public void setElementInfoAttrs (int attrs){
    elementInfoAttrs = attrs;
  }

  public void addElementInfoAttr (int attr){
    elementInfoAttrs |= attr;
  }

  public int getElementInfoAttrs () {
    return elementInfoAttrs;
  }

  public Source getSource () {
    if (source == null) {
      source = loadSource();
    }

    return source;
  }

  public String getSourceFileName () {
    return sourceFileName;
  }

  /**
   * Returns the information about a static field.
   */
  public FieldInfo getStaticField (int index) {
    return sFields[index];
  }

  /**
   * Returns the name of a static field.
   */
  public String getStaticFieldName (int index) {
    return getStaticField(index).getName();
  }

  /**
   * Checks if a static method call is deterministic, but only for
   * abtraction based determinism, due to Bandera.choose() calls
   */
  public boolean isStaticMethodAbstractionDeterministic (ThreadInfo th,
                                                         MethodInfo mi) {
    //    Reflection r = reflection.instantiate();
    //    return r.isStaticMethodAbstractionDeterministic(th, mi);
    // <2do> - still has to be implemented
     
    th = null;  // Get rid of IDE warning
    mi = null;
     
    return true;
  }

  public String getSuperClassName() {
    return superClassName;
  }

  /**
   * Return the super class.
   */
  public ClassInfo getSuperClass () {
    return superClass;
  }

  /**
   * return the ClassInfo for the provided superclass name. If this is equals
   * to ourself, return this (a little bit strange if we hit it in the first place)
   */
  public ClassInfo getSuperClass (String clsName) {
    if (clsName.equals(name)) return this;

    if (superClass != null) {
      return superClass.getSuperClass(clsName);
    } else {
      return null;
    }
  }

  public int getNumberOfSuperClasses(){
    int n = 0;
    for (ClassInfo ci = superClass; ci != null; ci = ci.superClass){
      n++;
    }
    return n;
  }
  
  /**
   * beware - this loads (but not yet registers) the enclosing class
   */
  public String getEnclosingClassName(){
    return enclosingClassName;
  }
  
  /**
   * beware - this loads (but not yet registers) the enclosing class
   */
  public ClassInfo getEnclosingClassInfo() {
    String enclName = getEnclosingClassName();
    return (enclName == null ? null : classLoader.getResolvedClassInfo(enclName)); // ? is this supposed to use the same classloader
  }

  public String getEnclosingMethodName(){
    return enclosingMethodName;
  }

  /**
   * same restriction as getEnclosingClassInfo() - might not be registered/initialized
   */
  public MethodInfo getEnclosingMethodInfo(){
    MethodInfo miEncl = null;
    
    if (enclosingMethodName != null){
      ClassInfo ciIncl = getEnclosingClassInfo();
      miEncl = ciIncl.getMethod( enclosingMethodName, false);
    }
    
    return miEncl;
  }
  
  /**
   * Returns true if the class is a system class.
   */
  public boolean isSystemClass () {
    return name.startsWith("java.") || name.startsWith("javax.");
  }

  /**
   * <2do> that's stupid - we should use subclasses for builtin and box types
   */
  public boolean isBoxClass () {
    if (name.startsWith("java.lang.")) {
      String rawType = name.substring(10);
      if (rawType.startsWith("Boolean") ||
          rawType.startsWith("Byte") ||
          rawType.startsWith("Character") ||
          rawType.startsWith("Integer") ||
          rawType.startsWith("Float") ||
          rawType.startsWith("Long") ||
          rawType.startsWith("Double")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the type of a class.
   */
  public String getType () {
    if (!isArray) {
      return "L" + name.replace('.', '/') + ";";
    } else {
      return name;
    }
  }

  /**
   * is this a (subclass of) WeakReference? this must be efficient, since it's
   * called in the mark phase on all live objects
   */
  public boolean isWeakReference () {
    return isWeakReference;
  }

  /**
   * note this only returns true is this is really the java.lang.ref.Reference classInfo
   */
  public boolean isReferenceClassInfo () {
    return isRefClassInfo;
  }

  /**
   * whether this refers to a primitive type.
   */
  public boolean isPrimitive() {
    return superClass == null && !isObjectClassInfo();
  }


  boolean hasRefField (int ref, Fields fv) {
    ClassInfo c = this;

    do {
      FieldInfo[] fia = c.iFields;
      for (int i=0; i<fia.length; i++) {
        FieldInfo fi = c.iFields[i];
        if (fi.isReference() && (fv.getIntValue( fi.getStorageOffset()) == ref)) return true;
      }
      c = c.superClass;
    } while (c != null);

    return false;
  }

  boolean hasImmutableInstances () {
    return ((elementInfoAttrs & ElementInfo.ATTR_IMMUTABLE) != 0);
  }

  public boolean hasInstanceFieldInfoAttr (Class<?> type){
    for (int i=0; i<nInstanceFields; i++){
      if (getInstanceField(i).hasAttr(type)){
        return true;
      }
    }
    
    return false;
  }
  
  public NativePeer getNativePeer () {
    return nativePeer;
  }
  
  /**
   * Returns true if the given class is an instance of the class
   * or interface specified.
   */
  public boolean isInstanceOf (String cname) {
    if (isPrimitive()) {
      return Types.getJNITypeCode(name).equals(cname);

    } else {
      cname = Types.getClassNameFromTypeName(cname);
      ClassInfo ci = this.classLoader.getResolvedClassInfo(cname);
      return isInstanceOf(ci);
    }
  }

  /**
   * Returns true if the given class is an instance of the class
   * or interface specified.
   */
  public boolean isInstanceOf (ClassInfo ci) {
    if (isPrimitive()) { // no inheritance for builtin types
      return (this==ci);
    } else {
      for (ClassInfo c = this; c != null; c = c.superClass) {
        if (c==ci) {
          return true;
        }
      }

      return getAllInterfaces().contains(ci);
    }
  }

  public boolean isInnerClassOf (String enclosingName){
    // don't register or initialize yet
    ClassInfo ciEncl = classLoader.tryGetResolvedClassInfo( enclosingName);
    if (ciEncl != null){
      return ciEncl.hasInnerClass(name);
    } else {
      return false;
    }
  }
  
  public boolean hasInnerClass (String innerName){
    for (int i=0; i<innerClassNames.length; i++){
      if (innerClassNames[i].equals(innerName)){
        return true;
      }
    }
    
    return false;
  }


  public static String makeModelClassPath (Config config) {
    StringBuilder buf = new StringBuilder(256);
    String ps = File.pathSeparator;
    String v;

    for (File f : config.getPathArray("boot_classpath")){
      buf.append(f.getAbsolutePath());
      buf.append(ps);
    }

    for (File f : config.getPathArray("classpath")){
      buf.append(f.getAbsolutePath());
      buf.append(ps);
    }

    // finally, we load from the standard Java libraries
    v = System.getProperty("sun.boot.class.path");
    if (v != null) {
      buf.append(v);
    }
    
    return buf.toString();
  }
  
  protected static String[] loadArrayInterfaces () {
    return new String[] {"java.lang.Cloneable", "java.io.Serializable"};
  }

  protected static String[] loadBuiltinInterfaces (String type) {
    return EMPTY_STRING_ARRAY;
  }


  /**
   * Loads the ClassInfo for named class.
   */
  void loadInterfaceRec (Set<ClassInfo> set, String[] interfaces) throws ClassInfoException {
    if (interfaces != null) {
      for (String iname : interfaces) {

        ClassInfo ci = classLoader.getResolvedClassInfo(iname);

        if (set != null){
          set.add(ci);
        }

        loadInterfaceRec(set, ci.interfaceNames);
      }
    }
  }

  int computeInstanceDataOffset () {
    if (superClass == null) {
      return 0;
    } else {
      return superClass.getInstanceDataSize();
    }
  }

  int getInstanceDataOffset () {
    return instanceDataOffset;
  }

  ClassInfo getClassBase (String clsBase) {
    if ((clsBase == null) || (name.equals(clsBase))) return this;

    if (superClass != null) {
      return superClass.getClassBase(clsBase);
    }

    return null; // Eeek - somebody asked for a class that isn't in the base list
  }

  int computeInstanceDataSize () {
    int n = getDataSize( iFields);

    for (ClassInfo c=superClass; c!= null; c=c.superClass) {
      n += c.getDataSize(c.iFields);
    }

    return n;
  }

  public int getInstanceDataSize () {
    return instanceDataSize;
  }

  int getDataSize (FieldInfo[] fields) {
    int n=0;
    for (int i=0; i<fields.length; i++) {
      n += fields[i].getStorageSize();
    }

    return n;
  }

  public int getNumberOfDeclaredInstanceFields () {
    return iFields.length;
  }

  public FieldInfo getDeclaredInstanceField (int i) {
    return iFields[i];
  }

  public int getNumberOfInstanceFields () {
    return nInstanceFields;
  }

  public FieldInfo getInstanceField (int i) {
    int idx = i - (nInstanceFields - iFields.length);
    if (idx >= 0) {
      return ((idx < iFields.length) ? iFields[idx] : null);
    } else {
      return ((superClass != null) ? superClass.getInstanceField(i) : null);
    }
  }

  public FieldInfo[] getInstanceFields(){
    FieldInfo[] fields = new FieldInfo[nInstanceFields];
    
    for (int i=0; i<fields.length; i++){
      fields[i] = getInstanceField(i);
    }
    
    return fields;
  }

  public int getStaticDataSize () {
    return staticDataSize;
  }

  int computeStaticDataSize () {
    return getDataSize(sFields);
  }

  public int getNumberOfStaticFields () {
    return sFields.length;
  }

  protected Source loadSource () {
    return Source.getSource(sourceFileName);
  }

  public static boolean isBuiltinClass (String cname) {
    char c = cname.charAt(0);

    // array class
    if ((c == '[') || cname.endsWith("[]")) {
      return true;
    }

    // primitive type class
    if (Character.isLowerCase(c)) {
      if ("int".equals(cname) || "byte".equals(cname) ||
          "boolean".equals(cname) || "double".equals(cname) ||
          "long".equals(cname) || "char".equals(cname) ||
          "short".equals(cname) || "float".equals(cname) || "void".equals(cname)) {
        return true;
      }
    }

    return false;
  }

  /**
   * set the locations where we look up sources
   */
  static void setSourceRoots (Config config) {
    Source.init(config);
  }

  /**
   * get names of all interfaceNames (transitive, idx.e. incl. bases and super-interfaceNames)
   * @return a Set of String interface names
   */
  public Set<ClassInfo> getAllInterfaces () {
    if (allInterfaces == null) {
      HashSet<ClassInfo> set = new HashSet<ClassInfo>();

      for (ClassInfo ci=this; ci != null; ci=ci.superClass) {
        loadInterfaceRec(set, ci.interfaceNames);
      }

      allInterfaces = Collections.unmodifiableSet(set);
    }

    return allInterfaces;
  }

  /**
   * get names of directly implemented interfaceNames
   */
  public String[] getDirectInterfaceNames () {
    return interfaceNames;
  }

  public Set<ClassInfo> getInterfaceClassInfos() {
    return interfaces;
  }

  public Set<ClassInfo> getAllInterfaceClassInfos() {
    return getAllInterfaces();
  }

  
  /**
   * get names of direct inner classes
   */
  public String[] getInnerClasses(){
    return innerClassNames;
  }
  
  public ClassInfo[] getInnerClassInfos(){
    ClassInfo[] innerClassInfos = new ClassInfo[innerClassNames.length];
    
    for (int i=0; i< innerClassNames.length; i++){
      innerClassInfos[i] = classLoader.getResolvedClassInfo(innerClassNames[i]); // ? is this supposed to use the same classloader
    }
    
    return innerClassInfos;
  }
  
  public BootstrapMethodInfo getBootstrapMethodInfo(int index) {
    return bootstrapMethods[index];
  }

  public ClassInfo getComponentClassInfo () {
    if (isArray()) {
      String cn = name.substring(1);

      if (cn.charAt(0) != '[') {
        cn = Types.getTypeName(cn);
      }

      ClassInfo cci = classLoader.getResolvedClassInfo(cn);

      return cci;
    }

    return null;
  }

  /**
   * most definitely not a public method, but handy for the NativePeer
   */
  protected Map<String, MethodInfo> getDeclaredMethods () {
    return methods;
  }

  /**
   * be careful, this replaces or adds MethodInfos dynamically
   */
  public MethodInfo putDeclaredMethod (MethodInfo mi){
    return methods.put(mi.getUniqueName(), mi);
  }

  public MethodInfo[] getDeclaredMethodInfos() {
    MethodInfo[] a = new MethodInfo[methods.size()];
    methods.values().toArray(a);
    return a;
  }

  public Instruction[] getMatchingInstructions (LocationSpec lspec){
    Instruction[] insns = null;

    if (lspec.matchesFile(sourceFileName)){
      for (MethodInfo mi : methods.values()) {
        Instruction[] a = mi.getMatchingInstructions(lspec);
        if (a != null){
          if (insns != null) {
            // not very efficient but probably rare
            insns = Misc.appendArray(insns, a);
          } else {
            insns = a;
          }

          // little optimization
          if (!lspec.isLineInterval()) {
            break;
          }
        }
      }
    }

    return insns;
  }

  public List<MethodInfo> getMatchingMethodInfos (MethodSpec mspec){
    ArrayList<MethodInfo> list = null;
    if (mspec.matchesClass(name)) {
      for (MethodInfo mi : methods.values()) {
        if (mspec.matches(mi)) {
          if (list == null) {
            list = new ArrayList<MethodInfo>();
          }
          list.add(mi);
        }
      }
    }
    return list;
  }

  public MethodInfo getFinalizer () {
    return finalizer;
  }

  public MethodInfo getClinit() {
    // <2do> braindead - cache
    for (MethodInfo mi : methods.values()) {
      if ("<clinit>".equals(mi.getName())) {
        return mi;
      }
    }
    return null;
  }

  public boolean hasCtors() {
    // <2do> braindead - cache
    for (MethodInfo mi : methods.values()) {
      if ("<init>".equals(mi.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * see getInitializedClassInfo() for restrictions.
   */
  public static ClassInfo getInitializedSystemClassInfo (String clsName, ThreadInfo ti){
    ClassLoaderInfo systemLoader = ClassLoaderInfo.getCurrentSystemClassLoader();
    ClassInfo ci = systemLoader.getResolvedClassInfo(clsName);
    ci.initializeClassAtomic(ti);

    return ci;
  }

  /**
   * this one is for clients that need to synchronously get an initialized classinfo.
   * NOTE: we don't handle clinits here. If there is one, this will throw
   * an exception. NO STATIC BLOCKS / FIELDS ALLOWED
   */
  public static ClassInfo getInitializedClassInfo (String clsName, ThreadInfo ti){
    ClassLoaderInfo cl = ClassLoaderInfo.getCurrentClassLoader();
    ClassInfo ci = cl.getResolvedClassInfo(clsName);
    ci.initializeClassAtomic(ti);

    return ci;
  }

  public boolean isRegistered () {
    //return (id != -1);
    return getStaticElementInfo() != null;
  }
  
  /**
   * this registers a ClassInfo in the corresponding ClassLoader statics so that we can cross-link from
   * SUT code and access static fields.
   */
  public StaticElementInfo registerClass (ThreadInfo ti){
    StaticElementInfo sei = getStaticElementInfo();
    
    if (sei == null) {
      // do this recursively for superclasses and interfaceNames
      // respective classes might be defined by another classloader, so we have to call their ClassInfo.registerClass()
      
      if (superClass != null) {
        superClass.registerClass(ti);
      }

      for (ClassInfo ifc : interfaces) {
        ifc.registerClass(ti);
      }
      
      ClassInfo.logger.finer("registering class: ", name);
      
      ElementInfo ei = createClassObject( ti);
      sei = createAndLinkStaticElementInfo( ti, ei);
      
      // SUT class is fully resolved and registered (but not necessarily initialized), notify listeners
      ti.getVM().notifyClassLoaded(this);
    }
    
    return sei;
  }

  ElementInfo createClassObject (ThreadInfo ti){
    Heap heap = VM.getVM().getHeap(); // ti can be null (during main thread initialization)

    int anchor = name.hashCode(); // 2do - this should also take the ClassLoader ref into account

    SystemClassLoaderInfo systemClassLoader = ti.getSystemClassLoaderInfo();

    ClassInfo classClassInfo = systemClassLoader.getClassClassInfo();    
    ElementInfo ei = heap.newSystemObject(classClassInfo, ti, anchor);
    int clsObjRef = ei.getObjectRef();
    
    ElementInfo eiClsName = heap.newSystemString(name, ti, clsObjRef);
    ei.setReferenceField("name", eiClsName.getObjectRef());

    ei.setBooleanField("isPrimitive", isPrimitive());
    
    // setting the ID_FIELD is done in registerClass once we have a StaticElementInfo

    // link the SUT class object to the classloader 
    ei.setReferenceField("classLoader", classLoader.getClassLoaderObjectRef());
    
    return ei;
  }
  
  StaticElementInfo createAndLinkStaticElementInfo (ThreadInfo ti, ElementInfo eiClsObj) {
    Statics statics = classLoader.getStatics();
    StaticElementInfo sei = statics.newClass(this, ti, eiClsObj);
    
    id = sei.getObjectRef();  // kind of a misnomer, it's really an id    
    uniqueId = ((long)classLoader.getId() << 32) | id;
    
    eiClsObj.setIntField( ID_FIELD, id);      
    
    return sei;
  }

  
  // for startup classes, the order of initialization is reversed since we can't create
  // heap objects before we have a minimal set of registered classes
  
  void registerStartupClass(ThreadInfo ti, List<ClassInfo> list) {
    if (!isRegistered()) {
      // do this recursively for superclasses and interfaceNames
      // respective classes might be defined by another classloader, so we have
      // to call their ClassInfo.registerClass()

      if (superClass != null) {
        superClass.registerStartupClass(ti, list);
      }

      for (ClassInfo ifc : interfaces) {
        ifc.registerStartupClass(ti, list);
      }
    }

    if (!list.contains(this)) {
      list.add(this);
      ClassInfo.logger.finer("registering startup class: ", name);
      createStartupStaticElementInfo(ti);
    }
    
      // SUT class is fully resolved and registered (but not necessarily initialized), notify listeners
      ti.getVM().notifyClassLoaded(this);
  }
  
  StaticElementInfo createStartupStaticElementInfo (ThreadInfo ti) {
    Statics statics = classLoader.getStatics();
    StaticElementInfo sei = statics.newStartupClass(this, ti);
    
    id = sei.getObjectRef();  // kind of a misnomer, it's really an id    
    uniqueId = ((long)classLoader.getId() << 32) | id;
    
    return sei;
  }
  
  ElementInfo createAndLinkStartupClassObject (ThreadInfo ti) {
    StaticElementInfo sei = getStaticElementInfo();
    ElementInfo ei = createClassObject(ti);
    
    sei.setClassObjectRef(ei.getObjectRef());
    ei.setIntField( ID_FIELD, id);      
    
    return ei;
  }
  
  boolean checkIfValidClassClassInfo() {
    return getDeclaredInstanceField( ID_FIELD) != null;
  }
  
  public boolean isInitializing () {
    StaticElementInfo sei = getStaticElementInfo();
    return ((sei != null) && (sei.getStatus() >= 0));
  }

  /**
   * note - this works recursively upwards since there might
   * be a superclass with a clinit that is still executing
   */
  public boolean isInitialized () {
    for (ClassInfo ci = this; ci != null; ci = ci.superClass){
      StaticElementInfo sei = ci.getStaticElementInfo();
      if (sei == null || sei.getStatus() != INITIALIZED){
        return false;
      }
    }
    
    return true;
  }

  public boolean isResolved () {
    return (!isObjectClassInfo() && superClass != null);
  }

  public boolean needsInitialization (ThreadInfo ti){
    StaticElementInfo sei = getStaticElementInfo();
    if (sei != null){
      int status = sei.getStatus();
      if (status == INITIALIZED || status == ti.getId()){
        return false;
      }
    }

    return true;
  }

  public void setInitializing(ThreadInfo ti) {
    StaticElementInfo sei = getModifiableStaticElementInfo();
    sei.setStatus(ti.getId());
  }
  
  /**
   * initialize this class and its superclasses (but not interfaces)
   * this will cause execution of clinits of not-yet-initialized classes in this hierarchy
   *
   * note - we don't treat registration/initialization of a class as
   * a sharedness-changing operation since it is done automatically by
   * the VM and the triggering action in the SUT (e.g. static field access or method call)
   * is the one that should update sharedness and/or break the transition accordingly
   *
   * @return true - if initialization pushed DirectCallStackFrames and caller has to re-execute
   */
  public boolean initializeClass(ThreadInfo ti){
    int pushedFrames = 0;

    // push clinits of class hierarchy (upwards, since call stack is LIFO)
    for (ClassInfo ci = this; ci != null; ci = ci.getSuperClass()) {
      StaticElementInfo sei = ci.getStaticElementInfo();
      if (sei == null){
        sei = ci.registerClass(ti);
      }

      int status = sei.getStatus();
      if (status != INITIALIZED){
        // we can't do setInitializing() yet because there is no global lock that
        // covers the whole clinit chain, and we might have a context switch before executing
        // a already pushed subclass clinit - there can be races as to which thread
        // does the static init first. Note this case is checked in INVOKECLINIT
        // (which is one of the reasons why we have it).

        if (status != ti.getId()) {
          // even if it is already initializing - if it does not happen in the current thread
          // we have to sync, which we do by calling clinit
          MethodInfo mi = ci.getMethod("<clinit>()V", false);
          if (mi != null) {
            DirectCallStackFrame frame = ci.createDirectCallStackFrame(ti, mi, 0);
            ti.pushFrame( frame);
            pushedFrames++;

          } else {
            // it has no clinit, we can set it initialized
            ci.setInitialized();
          }
        } else {
          // ignore if it's already being initialized  by our own thread (recursive request)
        }
      } else {
        break; // if this class is initialized, so are its superclasses
      }
    }

    return (pushedFrames > 0);
  }

  /**
   * use this with care since it will throw a JPFException if we encounter a choice point
   * during execution of clinits
   * Use this mostly for wrapper exceptions and other system classes that are guaranteed to load
   */
  public void initializeClassAtomic (ThreadInfo ti){
    for (ClassInfo ci = this; ci != null; ci = ci.getSuperClass()) {
      StaticElementInfo sei = ci.getStaticElementInfo();
      if (sei == null){
        sei = ci.registerClass(ti);
      }

      int status = sei.getStatus();
      if (status != INITIALIZED && status != ti.getId()){
          MethodInfo mi = ci.getMethod("<clinit>()V", false);
          if (mi != null) {
            DirectCallStackFrame frame = ci.createDirectCallStackFrame(ti, mi, 0);
            ti.executeMethodAtomic(frame);
          } else {
            ci.setInitialized();
          }
      } else {
        break; // if this class is initialized, so are its superclasses
      }
    }
  }

  public void setInitialized() {
    StaticElementInfo sei = getStaticElementInfo();
    if (sei != null && sei.getStatus() != INITIALIZED){
      sei = getModifiableStaticElementInfo();
      sei.setStatus(INITIALIZED);

      // we don't emit classLoaded() notifications for non-builtin classes
      // here anymore because it would be confusing to get instructionExecuted()
      // notifications from the <clinit> execution before the classLoaded()
    }
  }

  public StaticElementInfo getStaticElementInfo() {
    if (id != -1) {
      return classLoader.getStatics().get( id);
    } else {
      return null;
    }
  }

  public StaticElementInfo getModifiableStaticElementInfo() {
    if (id != -1) {
      return classLoader.getStatics().getModifiable( id);
    } else {
      return null;      
    }
  }

  Fields createArrayFields (String type, int nElements, int typeSize, boolean isReferenceArray) {
    return fieldsFactory.createArrayFields( type, this,
                                            nElements, typeSize, isReferenceArray);
  }

  /**
   * Creates the fields for a class.  This gets called during registration of a ClassInfo
   */
  Fields createStaticFields () {
    return fieldsFactory.createStaticFields(this);
  }

  void initializeStaticData (ElementInfo ei, ThreadInfo ti) {
    for (int i=0; i<sFields.length; i++) {
      FieldInfo fi = sFields[i];
      fi.initialize(ei, ti);
    }
  }

  /**
   * Creates the fields for an object.
   */
  public Fields createInstanceFields () {
    return fieldsFactory.createInstanceFields(this);
  }

  void initializeInstanceData (ElementInfo ei, ThreadInfo ti) {
    // Note this is only used for field inits, and array elements are not fields!
    // Since Java has only limited element init requirements (either 0 or null),
    // we do this ad hoc in the ArrayFields ctor

    // the order of inits should not matter, since this is only
    // for constant inits. In case of a "class X { int a=42; int b=a; ..}"
    // we have a explicit "GETFIELD a, PUTFIELD b" in the ctor, but to play it
    // safely we init top down

    if (superClass != null) { // do superclasses first
      superClass.initializeInstanceData(ei, ti);
    }

    for (int i=0; i<iFields.length; i++) {
      FieldInfo fi = iFields[i];
      fi.initialize(ei, ti);
    }
  }

  Map<String, MethodInfo> loadArrayMethods () {
    return new HashMap<String, MethodInfo>(0);
  }

  Map<String, MethodInfo> loadBuiltinMethods (String type) {
    type = null;  // Get rid of IDE warning 
     
    return new HashMap<String, MethodInfo>(0);
  }

  protected ClassInfo loadSuperClass (String superName) throws ClassInfoException {
    if (isObjectClassInfo()) {
      return null;
    }

    logger.finer("resolving superclass: ", superName, " of ", name);

    // resolve the superclass
    ClassInfo sci = resolveReferencedClass(superName);

    return sci;
  }

  protected Set<ClassInfo> loadInterfaces (String[] ifcNames) throws ClassInfoException {
    if (ifcNames == null || ifcNames.length == 0){
      return NO_INTERFACES;
      
    } else {
      Set<ClassInfo> set = new HashSet<ClassInfo>();

      for (String ifcName : ifcNames) {
        ClassInfo.logger.finer("resolving interface: ", ifcName, " of ", name);
        ClassInfo ifc = resolveReferencedClass(ifcName);
        set.add(ifc);
      }

      return set;
    }
  }
  
  /**
   * loads superclass and direct interfaces, and computes information
   * that depends on them
   */
  protected void resolveClass() {
    if (!isObjectClassInfo){
      superClass = loadSuperClass(superClassName);
      releaseActions = superClass.releaseActions;
    }
    interfaces = loadInterfaces(interfaceNames);

    //computeInheritedAnnotations(superClass);

    isWeakReference = isWeakReference0();
    isEnum = isEnum0();
  }

  /**
   * get a ClassInfo for a referenced type that is resolved with the same classLoader, but make
   * sure we only do this once per path
   * 
   * This method is called by the following bytecode instructions:
   * anewarray, checkcast, getstatic, instanceof, invokespecial, 
   * invokestatic, ldc, ldc_w, multianewarray, new, and putstatic
   * 
   * It loads the class referenced by these instructions and adds it to the 
   * resolvedClasses map of the classLoader
   */
  public ClassInfo resolveReferencedClass(String cname) {
    if(name.equals(cname)) {
      return this;
    }

    // if the class has been already resolved just return it
    ClassInfo ci = classLoader.getAlreadyResolvedClassInfo(cname);
    if(ci != null) {
      return ci;
    }
 
    // The defining class loader of the class initiate the load of referenced classes
    ci = classLoader.loadClass(cname);
    classLoader.addResolvedClass(ci);

    return ci;
  }

  protected int linkFields (FieldInfo[] fields, int idx, int off){
    for (FieldInfo fi: fields) {      
      fi.linkToClass(this, idx, off);
      
      int storageSize = fi.getStorageSize();      
      off += storageSize;
      idx++;
    }
    
    return off;
  }
  
  protected void linkFields() {
    //--- instance fields
    if(superClass != null) {
      int superDataSize = superClass.instanceDataSize;
      instanceDataSize = linkFields( iFields,  superClass.nInstanceFields, superDataSize);
      nInstanceFields = superClass.nInstanceFields + iFields.length;
      instanceDataOffset = superClass.instanceDataSize;
      
    } else {
      instanceDataSize = linkFields( iFields, 0, 0);
      nInstanceFields = iFields.length;
      instanceDataOffset = 0;
    }
    
    //--- static fields
    staticDataSize = linkFields( sFields, 0, 0);
  }

  // this resolves all annotations in this class hierarchy, which sets inherited attributes
  protected void checkInheritedAnnotations (){
    
  }
  
  @Override
  public String toString() {
    return "ClassInfo[name=" + name + "]";
  }

  protected MethodInfo getFinalizer0 () {
    MethodInfo mi = getMethod("finalize()V", true);

    // we are only interested in non-empty method bodies, Object.finalize()
    // is a dummy
    if ((mi != null) && (!mi.getClassInfo().isObjectClassInfo())) {
      return mi;
    }

    return null;
  }

  protected boolean isObjectClassInfo0 () {
	if (name.equals("java.lang.Object")) {
	  return true;
	}
	return false;
  }

  protected boolean isStringClassInfo0 () {
    if(name.equals("java.lang.String")) {
      return true;
    }
    return false;
  }

  protected boolean isRefClassInfo0 () {
    if(name.equals("java.lang.ref.Reference")) {
      return true;
    }
    return false;
  }

  protected boolean isWeakReference0 () {
	if(name.equals("java.lang.ref.WeakReference")) {
      return true;
	}

    for (ClassInfo ci = this; !ci.isObjectClassInfo(); ci = ci.superClass) {
      if (ci.isWeakReference()) {
        return true;
      }
    }

    return false;
  }

  protected boolean isEnum0 () {
	if(name.equals("java.lang.Enum")) {
      return true;
	}

    for (ClassInfo ci = this; !ci.isObjectClassInfo(); ci = ci.superClass) {
      if (ci.isEnum()) {
        return true;
      }
    }

    return false;
  }

  protected boolean isThreadClassInfo0 () {
    if(name.equals("java.lang.Thread")) {
      return true;
    }
    return false;
  }


  /**
   * It creates an instance from a original ClassInfo instance. It doesn't copy sei & 
   * uniqueId.
   * 
   * It is used for the cases where cl tries to load a class that the original version 
   * of which has been loaded by some other classloader.
   */
  public ClassInfo cloneFor (ClassLoaderInfo cl) {
    ClassInfo ci;

    try {
      ci = (ClassInfo)clone();

      ci.classLoader = cl;
      ci.interfaces = new HashSet<ClassInfo>();
      ci.resolveClass();

      ci.id = -1;
      ci.uniqueId = -1;

      if (methods != Collections.EMPTY_MAP){
        ci.methods = (Map<String, MethodInfo>)((HashMap<String, MethodInfo>) methods).clone();
      }

      for(Map.Entry<String, MethodInfo> e: ci.methods.entrySet()) {
        MethodInfo mi = e.getValue();
        e.setValue(mi.getInstanceFor(ci));
      }

      ci.iFields = new FieldInfo[iFields.length];
      for(int i=0; i<iFields.length; i++) {
        ci.iFields[i] = iFields[i].getInstanceFor(ci);
      }

      ci.sFields = new FieldInfo[sFields.length];
      for(int i=0; i<sFields.length; i++) {
        ci.sFields[i] = sFields[i].getInstanceFor(ci);
      }

      if(nativePeer != null) {
        ci.nativePeer = NativePeer.getNativePeer(ci);
      }

      ci.setAssertionStatus();

    } catch (CloneNotSupportedException cnsx){
      cnsx.printStackTrace();
      return null;
    }

    VM.getVM().notifyClassLoaded(ci);
    return ci;
  }
  
  // <2do> should be abstract
  public StackFrame createStackFrame (ThreadInfo ti, MethodInfo callee){
    return null;
  }
  
  public DirectCallStackFrame createDirectCallStackFrame (ThreadInfo ti, MethodInfo callee, int nLocalSlots){
    return null;
  }
  
  public DirectCallStackFrame createRunStartStackFrame (ThreadInfo ti, MethodInfo miRun){
    return null;
  }
}


