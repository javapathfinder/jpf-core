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
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LocationSpec;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * information associated with a method. Each method in JPF
 * is represented by a MethodInfo object
 */
public class MethodInfo extends InfoObject implements GenericSignatureHolder  {

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.vm.MethodInfo");
  
  static final int INIT_MTH_SIZE = 4096;
  protected static final ArrayList<MethodInfo> mthTable = new ArrayList<MethodInfo>(INIT_MTH_SIZE);
  
  // special globalIds
  static final int DIRECT_CALL = -1;

  static final LocalVarInfo[] EMPTY = new LocalVarInfo[0];
  
  static final int[] EMPTY_INT = new int[0];
  
  /**
   * Used to warn about local variable information.
   */
  protected static boolean warnedLocalInfo = false;
  
  //--- various JPF method attributes
  static final int  EXEC_ATOMIC = 0x10000; // method executed atomically
  static final int  EXEC_HIDDEN = 0x20000; // method hidden from path
  static final int  FIREWALL    = 0x40000; // firewall any unhandled exceptionHandlers
                                           // (turn into UnhandledException throws)
  static final int  IS_CLINIT   = 0x80000;
  static final int  IS_INIT     = 0x100000;
  
  static final int  IS_REFLECTION = 0x200000; // this is a reflection direct call
  static final int  IS_DIRECT_CALL = 0x400000;
  
  /** a unique int assigned to this method */
  protected int globalId = -1;

  /**
   * this is a lazy evaluated mangled name consisting of the name and
   * arg type signature
   */
  protected String uniqueName;

  /** Name of the method */
  protected String name;

  /** Signature of the method */
  protected String signature;

  /** Generic signature of the method */
  protected String genericSignature;

  /** Class the method belongs to */
  protected ClassInfo ci;

  /** Instructions associated with the method */
  protected Instruction[] code;

  /** JPFConfigException handlers */
  protected ExceptionHandler[] exceptionHandlers;

  /** classnames of checked exception thrown by the method */
  protected String[] thrownExceptionClassNames;

  /** Table used for line numbers 
   * this assigns a line number to every instruction index, instead of 
   * using an array of ranges. Assuming we have 2-3 insns per line on average,
   * this should still require less memory than a reference array with associated
   * range objects, and allows faster access of instruction line numbers, which
   * we might need for location specs
   */
  protected int[] lineNumbers;
  
  /** Local variable information */
  protected LocalVarInfo localVars[] = null;

  /** Maximum number of local variables */
  protected int maxLocals;

  /** Maximum number of elements on the stack */
  protected int maxStack;

  /** null if we don't have any */
  AnnotationInfo[][] parameterAnnotations;

  //--- a batch of attributes
  
  /** the standard Java modifier attributes */
  protected int modifiers;
   
  /** a batch of execution related JPF attributes */
  protected int attributes;
      

  //--- all the stuff we need for native methods
  // <2do> pcm - turn this into a derived class

  /**  the number of stack slots for the arguments (incl. 'this'), lazy eval */
  protected int argSize = -1;

  /** number of arguments (excl. 'this'), lazy eval */
  protected int nArgs = -1;

  /** what return type do we have (again, lazy evaluated) */
  protected byte returnType = -1;

  /** number of stack slots for return value */
  protected int retSize = -1;

  /** used for native method parameter conversion (lazy evaluated) */
  protected byte[] argTypes = null;
  
  static boolean init (Config config) {
    mthTable.clear();    
    return true;
  }

  public static MethodInfo getMethodInfo (int globalId){
    if (globalId >=0 && globalId <mthTable.size()){
      return mthTable.get(globalId);
    } else {
      return null;
    }
  }
  
  public static MethodInfo create (String name, String signature, int modifiers){
    return new MethodInfo( name, signature, modifiers);
  }
  
  public static MethodInfo create (ClassInfo ci, String name, String signature, int modifiers){
    return new MethodInfo( ci, name, signature, modifiers);
  }
  
  static MethodInfo create (ClassInfo ci, String name, String signature, int modifiers, int maxLocals, int maxStack){
    return new MethodInfo( ci, name, signature, modifiers, maxLocals, maxStack);
  }

  /**
   * for direct call construction
   * Note: this is only a partial initialization, the code still has to be created/installed by the caller
   */
  public MethodInfo (MethodInfo callee, int nLocals, int nOperands) {
    globalId = DIRECT_CALL;
    // we don't want direct call methods in the mthTable (would be a memory leak) so don't register
    
    ci = callee.ci;
    name = "[" + callee.name + ']'; // it doesn't allocate anything, so we don't have to be unique
    signature = "()V";
    genericSignature = "";
    maxLocals = nLocals;
    maxStack = nOperands;  // <2do> cache for optimization
    localVars = EMPTY;
    lineNumbers = null;
    exceptionHandlers = null;
    thrownExceptionClassNames = null;
    uniqueName = name;
    
    // we need to preserve the ClassInfo so that class resolution for static method calls works
    ci = callee.ci;
    
    attributes |= IS_DIRECT_CALL;
    modifiers = Modifier.STATIC;   // always treated as static
    
    // code still has to be installed by caller
  }
  
  /**
   * This is used to create synthetic methods of function object types
   */
  public MethodInfo(String name, String signature, int modifiers, int nLocals, int nOperands) {
    this( name, signature, modifiers);
    maxLocals = nLocals;
    maxStack = nOperands;
    localVars = EMPTY;
  }
  
  /**
   * for NativeMethodInfo creation 
   */
  public MethodInfo (MethodInfo mi) {
    globalId = mi.globalId;
    uniqueName = mi.uniqueName;
    name = mi.name;
    signature = mi.signature;
    genericSignature = mi.genericSignature;
    ci = mi.ci;
    modifiers = mi.modifiers;
    attributes = mi.attributes;
    thrownExceptionClassNames = mi.thrownExceptionClassNames;
    parameterAnnotations = mi.parameterAnnotations;

    annotations = mi.annotations;
    
    localVars = null; // there are no StackFrame localVarInfos, this is native
    // code still has to be installed by caller
  }
  
  // <2do> this is going away
  public MethodInfo (ClassInfo ci, String name, String signature, int modifiers, int maxLocals, int maxStack){
    this.ci = ci;
    this.name = name;
    this.signature = signature;
    this.uniqueName = getUniqueName(name, signature);
    this.genericSignature = "";
    this.maxLocals = maxLocals;
    this.maxStack = maxStack;
    this.modifiers = modifiers;

    this.lineNumbers = null;
    this.exceptionHandlers = null;
    this.thrownExceptionClassNames = null;

    // set attributes we can deduce from the name and the ClassInfo
    if (ci != null){
      if (name.equals("<init>")) {
        attributes |= IS_INIT;
      } else if (name.equals("<clinit>")) {
        this.modifiers |= Modifier.SYNCHRONIZED;
        attributes |= IS_CLINIT | FIREWALL;
      }
      if (ci.isInterface()) { // all interface methods are public
        this.modifiers |= Modifier.PUBLIC;
      }
    }

    this.globalId = mthTable.size();
    mthTable.add(this);
  }

  
  public MethodInfo (String name, String signature, int modifiers){
    this.name = name;
    this.signature = signature;
    this.modifiers = modifiers;
    this.uniqueName = getUniqueName(name, signature);
    this.genericSignature = "";

    if (name.equals("<init>")) {
      attributes |= IS_INIT;
    } else if (name.equals("<clinit>")) {
      // for some reason clinits don't have the synchronized modifier, but they are synchronized
      // we keep it consistent so that we don't have to implement special lock acquisition/release for clinits
      this.modifiers |= Modifier.SYNCHRONIZED;
      attributes |= IS_CLINIT | FIREWALL;
    }
    
    this.globalId = mthTable.size();
    mthTable.add(this);    
  }

  public MethodInfo (ClassInfo ci, String name, String signature, int modifiers){
    this(name, signature, modifiers);
    
    this.ci = ci;
  }
  
  //--- setters used during construction
  
  public void linkToClass (ClassInfo ci){
    this.ci = ci;
    
    if (ci.isInterface()) { // all interface methods are public
      this.modifiers |= Modifier.PUBLIC;
    }
  }
  
  public void setMaxLocals(int maxLocals){
    this.maxLocals = maxLocals;
  }

  public void setMaxStack(int maxStack){
    this.maxStack = maxStack;
  }
  
  public void setCode (Instruction[] code){
    for (int i=0; i<code.length; i++){
      code[i].setMethodInfo(this);
    }
    this.code = code;
  }
  
  
  public boolean hasParameterAnnotations() {
    return (parameterAnnotations != null);
  }

  // since some listeners might call this on every method invocation, we should do a little optimization
  static AnnotationInfo[][] NO_PARAMETER_ANNOTATIONS_0 = new AnnotationInfo[0][];
  static AnnotationInfo[][] NO_PARAMETER_ANNOTATIONS_1 = { new AnnotationInfo[0] };
  static AnnotationInfo[][] NO_PARAMETER_ANNOTATIONS_2 = { new AnnotationInfo[0], new AnnotationInfo[0] };
  static AnnotationInfo[][] NO_PARAMETER_ANNOTATIONS_3 = { new AnnotationInfo[0], new AnnotationInfo[0], new AnnotationInfo[0] };  
  
  public AnnotationInfo[][] getParameterAnnotations() {
    if (parameterAnnotations == null){ // keep this similar to getAnnotations()
      int n = getNumberOfArguments();
      switch (n){
      case 0: return NO_PARAMETER_ANNOTATIONS_0;
      case 1: return NO_PARAMETER_ANNOTATIONS_1;
      case 2: return NO_PARAMETER_ANNOTATIONS_2;
      case 3: return NO_PARAMETER_ANNOTATIONS_3;
      default:
        AnnotationInfo[][] pai = new AnnotationInfo[n][];
        for (int i=0; i<n; i++){
          pai[i] = new AnnotationInfo[0];
        }
        return pai;
      }
      
    } else {
      return parameterAnnotations;
    }
  }

  /**
   * return annotations for parameterIndex
   */
  public AnnotationInfo[] getParameterAnnotations(int parameterIndex){
    if (parameterAnnotations == null){
      return null;
    } else {
      if (parameterIndex >= getNumberOfArguments()){
        return null;
      } else {
        return parameterAnnotations[parameterIndex];
      }
    }
  }


  
  public static int getNumberOfLoadedMethods () {
    return mthTable.size();
  }

  void setAtomic (boolean isAtomic) {
    if (isAtomic) {
      attributes |= EXEC_ATOMIC;
    } else {
      attributes &= ~EXEC_ATOMIC;
    }
  }
  public boolean isAtomic () {
    return ((attributes & EXEC_ATOMIC) != 0);
  }
  
  void setHidden (boolean isHidden) {
    if (isHidden) {
      attributes |= EXEC_HIDDEN;
    } else {
      attributes &= ~EXEC_HIDDEN;
    }
  }
  public boolean isHidden () {
    return ((attributes & EXEC_HIDDEN) != 0);    
  }
  
  /**
   * turn unhandled exceptionHandlers at the JPF execution level
   * into UnhandledException throws at the host VM level
   * this is useful to implement firewalls for direct calls
   * which should not let exceptionHandlers permeate into bytecode/
   * application code
   */
  public void setFirewall (boolean isFirewalled) {
    if (isFirewalled) {
      attributes |= FIREWALL;
    } else {
      attributes &= ~FIREWALL;
    }
  }
  public boolean isFirewall () {
    return ((attributes & FIREWALL) != 0);    
  }
  
  
  
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException cnx) {
      return null;
    }
  }
  
  public int getGlobalId() {
    return globalId;
  }

  public DirectCallStackFrame createRunStartStackFrame (ThreadInfo ti){
    return ci.createRunStartStackFrame( ti, this);
  }

  public DirectCallStackFrame createDirectCallStackFrame (ThreadInfo ti, int nLocals){
    return ci.createDirectCallStackFrame(ti, this, nLocals);
  }

  public boolean isSyncRelevant () {
    return (name.charAt(0) != '<');
  }
  
  public boolean isInitOrClinit (){
    return ((attributes & (IS_CLINIT | IS_INIT)) != 0);
  }
  
  public boolean isClinit () {
    return ((attributes & IS_CLINIT) != 0);
  }

  public boolean isClinit (ClassInfo ci) {
    return (((attributes & IS_CLINIT) != 0) && (this.ci == ci));
  }

  public boolean isInit() {
    return ((attributes & IS_INIT) != 0);
  }
  
  public boolean isDirectCallStub(){
    return ((attributes & IS_DIRECT_CALL) != 0);    
  }
  
  /**
   * yet another name - this time with a non-mangled, but abbreviated signature
   * and without return type (e.g. like "main(String[])"
   */
  public String getLongName () {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    
    sb.append('(');
    String[] argTypeNames = getArgumentTypeNames();
    for (int i=0; i<argTypeNames.length; i++) {
      String a = argTypeNames[i];
      int idx = a.lastIndexOf('.');
      if (idx > 0) {
        a = a.substring(idx+1);
      }
      if (i>0) {
        sb.append(',');
      }
      sb.append(a);
    }
    sb.append(')');
    
    return sb.toString();
  }
  
  /**
   * return the minimal name that has to be unique for overloading
   * used as a lookup key
   * NOTE: with the silent introduction of covariant return types
   * in Java 5.0, we have to use the full signature to be unique
   */
  public static String getUniqueName (String mname, String signature) {
    return (mname + signature);
  }

  public String getStackTraceSource() {
    return getSourceFileName();
  }

  public byte[] getArgumentTypes () {
    if (argTypes == null) {
      argTypes = Types.getArgumentTypes(signature);
      nArgs = argTypes.length;
    }

    return argTypes;
  }

  public String[] getArgumentTypeNames () {
    return Types.getArgumentTypeNames(signature);
  }
  
  public int getArgumentsSize () {
    if (argSize < 0) {
      argSize = Types.getArgumentsSize(signature);

      if (!isStatic()) {
        argSize++;
      }
    }

    return argSize;
  }
  
  /**
   * return only the LocalVarInfos for arguments, in order of definition
   * or null if there are no localVarInfos.
   * throw a JPFException if there are more immediately in scope vars than args
   * 
   * NOTE - it is perfectly legal for a method to have arguments but no LocalVarInfos,
   * which are code attributes, clients have to check for a non-null return value
   * even if the method has arguments.
   * Note also that abstract / interface methods don't have code and hence no
   * LocalVarInfos
   */
  public LocalVarInfo[] getArgumentLocalVars(){
    if (localVars == null){ // shortcut in case we don't have args or localVars;
      return null;
    }
    
    int nArgs = getNumberOfStackArguments(); // we want 'this'
    if (nArgs == 0){
      return new LocalVarInfo[0]; // rare enough so that we don't use a static
    }

    LocalVarInfo[] argLvis = new LocalVarInfo[nArgs];
    int n = 0; // how many args we've got so far
    
    for (LocalVarInfo lvi : localVars){
      // arguments are the only ones that are immediately in scope
      if (lvi.getStartPC() == 0){
        if (n == nArgs){ // ARGH - more in-scope vars than args
          throw new JPFException("inconsistent localVar table for method " + getFullName());
        }
        
        // order with respect to slot index - since this might get called
        // frequently, we don't use java.util.Arrays.sort() but sort in
        // on-the-fly. Note that we can have several localVar entries for the
        // same name, but only one can be immediately in scope
        int slotIdx = lvi.getSlotIndex();

        int i;
        for (i = 0; i < n; i++) {
          if (slotIdx < argLvis[i].getSlotIndex()) {
            for (int j=n; j>i; j--){
              argLvis[j] = argLvis[j-1];
            }
            argLvis[i] = lvi;
            n++;
            break;
          }
        }
        if (i == n) { // append
          argLvis[n++] = lvi;
        }
      }
    }
    
    return argLvis;
  }
  
  public String getReturnType () {
    return Types.getReturnTypeSignature(signature);
  }

  public String getReturnTypeName () {
    return Types.getReturnTypeName(signature);
  }
  
  public String getSourceFileName () {
    if (ci != null) {
      return ci.getSourceFileName();
    } else {
      return "[VM]";
    }
  }

  public String getClassName () {
    if (ci != null) {
      return ci.getName();
    } else {
      return "[VM]";
    }
  }
  
  /**
   * Returns the class the method belongs to.
   */
  public ClassInfo getClassInfo () {
    return ci;
  }

  /**
   * @deprecated - use getFullName
   */
  @Deprecated
public String getCompleteName () {
    return getFullName();
  }

  /**
   * return classname.name (but w/o signature)
   */
  public String getBaseName() {
    return getClassName() + '.' + name;
  }
    
  public boolean isCtor () {
    return (name.equals("<init>"));
  }
  
  public boolean isInternalMethod () {
    // <2do> pcm - should turn this into an attribute for efficiency reasons
    return (name.equals("<clinit>") || uniqueName.equals("finalize()V"));
  }
  
  public boolean isThreadEntry (ThreadInfo ti) {
    return (uniqueName.equals("run()V") && (ti.countStackFrames() == 1));
  }
  
  /**
   * Returns the full classname (if any) + name + signature.
   */
  public String getFullName () {
    if (ci != null) {
      return ci.getName() + '.' + getUniqueName();
    } else {
      return getUniqueName();
    }
  }

  /**
   * returns stack trace name: classname (if any) + name
   */
  public String getStackTraceName(){
    if (ci != null) {
      return ci.getName() + '.' + name;
    } else {
      return name;
    }
  }
  
  /**
   * return number of instructions
   */
  public int getNumberOfInstructions() {
    if (code == null){
      return 0;
    }
    
    return code.length;
  }
  
  /**
   * Returns a specific instruction.
   */
  public Instruction getInstruction (int i) {
    if (code == null) {
      return null;
    }

    if ((i < 0) || (i >= code.length)) {
      return null;
    }

    return code[i];
  }

  /**
   * Returns the instruction at a certain position.
   */
  public Instruction getInstructionAt (int position) {
    if (code == null) {
      return null;
    }

    for (int i = 0, l = code.length; i < l; i++) {
      if ((code[i] != null) && (code[i].getPosition() == position)) {
        return code[i];
      }
    }

    throw new JPFException("instruction not found");
  }

  /**
   * Returns the instructions of the method.
   */
  public Instruction[] getInstructions () {
    return code;
  }
  
  public boolean includesLine (int line){
    int len = code.length;
    return (code[0].getLineNumber() <= line) && (code[len].getLineNumber() >= line);
  }

  public Instruction[] getInstructionsForLine (int line){
    return getInstructionsForLineInterval(line,line);
  }

  public Instruction[] getInstructionsForLineInterval (int l1, int l2){
    Instruction[] c = code;
       
    // instruction line numbers don't have to be monotonic (they can decrease for loops)
    // hence we cannot easily check for overlapping ranges
    
    if (c != null){
       ArrayList<Instruction> matchingInsns = null;
       
       for (int i = 0; i < c.length; i++) {
        Instruction insn = c[i];
        int line = insn.getLineNumber();
        if (line == l1 || line == l2 || (line > l1 && line < l2)) {
          if (matchingInsns == null) {
            matchingInsns = new ArrayList<Instruction>();
          }
          matchingInsns.add(insn);
        }
      }
      
      if (matchingInsns == null) {
        return null;
      } else {
        return matchingInsns.toArray(new Instruction[matchingInsns.size()]);
      }
            
    } else {
      return null;
    }
  }

  public Instruction[] getMatchingInstructions (LocationSpec lspec){
    return getInstructionsForLineInterval(lspec.getFromLine(), lspec.getToLine());
  }


  /**
   * Returns the line number for a given position.
   */
  public int getLineNumber (Instruction pc) {
    if (lineNumbers == null) {
      if (pc == null)
        return -1;
      else
        return pc.getPosition();
    }

    if (pc != null) {
      int idx = pc.getInstructionIndex();
      if (idx < 0) idx = 0;
      return lineNumbers[idx];
    } else {
      return -1;
    }
  }

  /**
   * Returns a table to translate positions into line numbers.
   */
  public int[] getLineNumbers () {
    return lineNumbers;
  }

  public boolean containsLineNumber (int n){
    if (lineNumbers != null){
      return (lineNumbers[0] <= n) && (lineNumbers[lineNumbers.length-1] <= n);
    }
    
    return false;
  }
  
  public boolean intersectsLineNumbers( int first, int last){
    if (lineNumbers != null){
      if ((last < lineNumbers[0]) || (first > lineNumbers[lineNumbers.length-1])){
        return false;
      }
      return true;
    }
    
    return false;
  }
  
  public ExceptionHandler getHandlerFor (ClassInfo ciException, Instruction insn){
    if (exceptionHandlers != null){
      int position = insn.getPosition();
      for (int i=0; i<exceptionHandlers.length; i++){
        ExceptionHandler handler = exceptionHandlers[i];
        if ((position >= handler.getBegin()) && (position < handler.getEnd())) {
          // checks if this type of exception is caught here (null means 'any')
          String handledType = handler.getName();
          if ((handledType == null)   // a catch-all handler
                  || ciException.isInstanceOf(handledType)) {
            return handler;
          }
        }          
      }      
    }
    
    return null;
  }
  
  public boolean isMJI () {
    return false;
  }

  public int getMaxLocals () {
    return maxLocals;
  }

  public int getMaxStack () {
    return maxStack;
  }

  public ExceptionHandler[] getExceptions () {
    return exceptionHandlers;
  }

  public String[] getThrownExceptionClassNames () {
    return thrownExceptionClassNames;
  }


  public LocalVarInfo getLocalVar(String name, int pc){
    LocalVarInfo[] vars = localVars;
    if (vars != null){
      for (int i = 0; i < vars.length; i++) {
        LocalVarInfo lv = vars[i];
        if (lv.matches(name, pc)) {
          return lv;
        }
      }
    }

    return null;

  }

  public LocalVarInfo getLocalVar (int slotIdx, int pc){
    LocalVarInfo[] vars = localVars;

    if (vars != null){
      for (int i = 0; i < vars.length; i++) {
        LocalVarInfo lv = vars[i];
        if (lv.matches(slotIdx, pc)) {
          return lv;
        }
      }
    }

    return null;
  }

  public LocalVarInfo[] getLocalVars() {
    return localVars; 
  }


  /**
   * note that this might contain duplicates for variables with multiple
   * scope entries
   */
  public String[] getLocalVariableNames() {
    String[] names = new String[localVars.length];

    for (int i=0; i<localVars.length; i++){
      names[i] = localVars[i].getName();
    }

    return names;
  }


  public MethodInfo getOverriddenMethodInfo(){
    MethodInfo smi = null;
    
    if (ci != null) {
      ClassInfo sci = ci.getSuperClass();
      if (sci != null){
        smi = sci.getMethod(getUniqueName(), true);
      }
    }
    
    return smi;
  }
  
  /**
   * Returns the name of the method.
   */
  public String getName () {
    return name;
  }

  public String getJNIName () {
    return Types.getJNIMangledMethodName(null, name, signature);
  }
  
  public int getModifiers () {
    return modifiers;
  }
  
  /**
   * Returns true if the method is native
   */
  public boolean isNative () {
    return ((modifiers & Modifier.NATIVE) != 0);
  }

  public boolean isAbstract () {
    return ((modifiers & Modifier.ABSTRACT) != 0);
  }
  
  // overridden by NativeMethodInfo
  public boolean isUnresolvedNativeMethod(){
    return ((modifiers & Modifier.NATIVE) != 0);
  }

  // overridden by NativeMethodInfo
  public boolean isJPFExecutable (){
    return !hasAttr(NoJPFExec.class);
  }

  public int getNumberOfArguments () {
    if (nArgs < 0) {
      nArgs = Types.getNumberOfArguments(signature);
    }

    return nArgs;
  }

  /**
   * Returns the size of the arguments.
   * This returns the number of parameters passed on the stack, incl. 'this'
   */
  public int getNumberOfStackArguments () {
    int n = getNumberOfArguments();

    return isStatic() ? n : n + 1;
  }

  public int getNumberOfCallerStackSlots () {
    return Types.getNumberOfStackSlots(signature, isStatic()); // includes return type
  }

  public Instruction getFirstInsn(){
    if (code != null){
      return code[0];
    }
    return null;    
  }
  
  public Instruction getLastInsn() {
    if (code != null){
      return code[code.length-1];
    }
    return null;
  }

  /**
   * do we return Object references?
   */
  public boolean isReferenceReturnType () {
    int r = getReturnTypeCode();

    return ((r == Types.T_REFERENCE) || (r == Types.T_ARRAY));
  }

  public byte getReturnTypeCode () {
    if (returnType < 0) {
      returnType = Types.getReturnBuiltinType(signature);
    }

    return returnType;
  }

  /**
   * what is the slot size of the return value
   */
  public int getReturnSize() {
    if (retSize == -1){
      switch (getReturnTypeCode()) {
        case Types.T_VOID:
          retSize = 0;
          break;

        case Types.T_LONG:
        case Types.T_DOUBLE:
          retSize = 2;
          break;

        default:
          retSize = 1;
          break;
      }
    }

    return retSize;
  }

  public Class<? extends ChoiceGenerator<?>> getReturnChoiceGeneratorType (){
    switch (getReturnTypeCode()){
      case Types.T_BOOLEAN:
        return BooleanChoiceGenerator.class;

      case Types.T_BYTE:
      case Types.T_CHAR:
      case Types.T_SHORT:
      case Types.T_INT:
        return IntChoiceGenerator.class;

      case Types.T_LONG:
        return LongChoiceGenerator.class;

      case Types.T_FLOAT:
        return FloatChoiceGenerator.class;

      case Types.T_DOUBLE:
        return DoubleChoiceGenerator.class;

      case Types.T_ARRAY:
      case Types.T_REFERENCE:
      case Types.T_VOID:
        return ReferenceChoiceGenerator.class;
    }

    return null;
  }

  /**
   * Returns the signature of the method.
   */
  public String getSignature () {
    return signature;
  }

  @Override
  public String getGenericSignature() {
    return genericSignature;
  }

  @Override
  public void setGenericSignature(String sig){
    genericSignature = sig;
  }

  /**
   * Returns true if the method is static.
   */
  public boolean isStatic () {
    return ((modifiers & Modifier.STATIC) != 0);
  }

  /**
   * is this a public method
   */
  public boolean isPublic() {
    return ((modifiers & Modifier.PUBLIC) != 0);
  }
  
  public boolean isPrivate() {
    return ((modifiers & Modifier.PRIVATE) != 0);
  }
  
  public boolean isProtected() {
    return ((modifiers & Modifier.PROTECTED) != 0);
  }

  /**
   * Returns true if the method is synchronized.
   */
  public boolean isSynchronized () {
    return ((modifiers & Modifier.SYNCHRONIZED) != 0);
  }

  // <2do> these modifiers are still java.lang.reflect internal and not
  // supported by public Modifier methods, but since we want to keep this 
  // similar to the Method reflection and we get the modifiers from the
  // classfile we implement this with explicit values
  
  public boolean isSynthetic(){
    return ((modifiers & 0x00001000) != 0);    
  } 
  public boolean isVarargs(){
    return ((modifiers & 0x00000080) != 0);        
  }
  
  /*
   * is this from a classfile or was it created by JPF (and hence should not
   * be visible in stacktraces etc)
   */
  public boolean isJPFInternal(){
    // note this has a different meaning than Method.isSynthetic(), which
    // is defined in VM spec 4.7.8. What we mean here is that this MethodInfo
    // is not associated with any class (such as direct call MethodInfos), but
    // there might be more in the future
    return (ci == null);
  }
  
  public String getUniqueName () {
    return uniqueName;
  }
  
  public boolean hasCode(){
    return (code != null);
  }
  
  public boolean hasEmptyBody (){
    // only instruction is a return
    return (code.length == 1 && (code[0] instanceof ReturnInstruction));
  }


  //--- parameter annotations
  //<2do> these are going away
  protected void startParameterAnnotations(int annotationCount){
    parameterAnnotations = new AnnotationInfo[annotationCount][];
  }
  protected void setParameterAnnotations(int index, AnnotationInfo[] ai){
    parameterAnnotations[index] = ai;
  }
  protected void finishParameterAnnotations(){
    // nothing
  }

  public void setParameterAnnotations (AnnotationInfo[][] parameterAnnotations){
    this.parameterAnnotations = parameterAnnotations;
  }
  
  //--- thrown exceptions
  //<2do> these are going away
  protected void startTrownExceptions (int exceptionCount){
    thrownExceptionClassNames = new String[exceptionCount];
  }
  protected void setException (int index, String exceptionType){
    thrownExceptionClassNames[index] = Types.getClassNameFromTypeName(exceptionType);
  }
  protected void finishThrownExceptions(){
    // nothing
  }

  public void setThrownExceptions (String[] exceptions){
    thrownExceptionClassNames = exceptions;
  }
  

  //--- exception handler table initialization
  //<2do> these are going away
  protected void startExceptionHandlerTable (int handlerCount){
    exceptionHandlers = new ExceptionHandler[handlerCount];
  }
  protected void setExceptionHandler (int index, int startPc, int endPc, int handlerPc, String catchType){
    exceptionHandlers[index] = new ExceptionHandler(catchType, startPc, endPc, handlerPc);
  }
  protected void finishExceptionHandlerTable(){
    // nothing
  }

  public void setExceptionHandlers (ExceptionHandler[] handlers){
    exceptionHandlers = handlers;
  }
  
  //--- local var table initialization
  // <2do> these are going away
  protected void startLocalVarTable (int localVarCount){
    localVars = new LocalVarInfo[localVarCount];
  }
  protected void setLocalVar(int index, String varName, String descriptor, int scopeStartPc, int scopeEndPc, int slotIndex){
    localVars[index] = new LocalVarInfo(varName, descriptor, "", scopeStartPc, scopeEndPc, slotIndex);
  }
  protected void finishLocalVarTable(){
    // nothing to do
  }

  public void setLocalVarTable (LocalVarInfo[] locals){
    localVars = locals;
  }
  
  public void setLocalVarAnnotations (){
    if (localVars != null){
      for (VariableAnnotationInfo ai : getTargetTypeAnnotations(VariableAnnotationInfo.class)){
        for (int i = 0; i < ai.getNumberOfScopeEntries(); i++) {
          for (LocalVarInfo lv : localVars) {
            if (lv.getStartPC() == ai.getStartPC(i) && lv.getSlotIndex() == ai.getSlotIndex(i)) {
              lv.addTypeAnnotation(ai);
            }
          }
        }
      }
    }
  }
  
  public boolean hasTypeAnnotatedLocalVars (){
    if (localVars != null){
      for (LocalVarInfo lv : localVars){
        if (lv.hasTypeAnnotations()){
          return true;
        }
      }
    }
    
    return false;
  }
  
  public List<LocalVarInfo> getTypeAnnotatedLocalVars (){
    List<LocalVarInfo> list = null;
    
    if (localVars != null){
      for (LocalVarInfo lv : localVars){
        if (lv.hasTypeAnnotations()){
          if (list == null){
            list = new ArrayList<LocalVarInfo>();
          }
          list.add(lv);
        }
      }
    }
    
    if (list == null){
      list = Collections.emptyList();
    }
    
    return list;
  }
  
  public List<LocalVarInfo> getTypeAnnotatedLocalVars (String annotationClsName){
    List<LocalVarInfo> list = null;
    
    if (localVars != null){
      for (LocalVarInfo lv : localVars){
        AbstractTypeAnnotationInfo tai = lv.getTypeAnnotation(annotationClsName);
        if (tai != null){
          if (list == null){
            list = new ArrayList<LocalVarInfo>();
          }
          list.add(lv);
        }
      }
    }
    
    if (list == null){
      list = Collections.emptyList();
    }
    
    return list;
  }
  
  
  //--- line number table initialization
  // <2do> these are going away
  protected void startLineNumberTable(int lineNumberCount){
    int len = code.length;
    int[] ln = new int[len];

    lineNumbers = ln;
  }
  protected void setLineNumber(int index, int lineNumber, int startPc){
    int len = code.length;
    int[] ln = lineNumbers;

    for (int i=0; i<len; i++){
      Instruction insn = code[i];
      int pc = insn.getPosition();

      if (pc == startPc){ // this is the first insn with this line number
        ln[i] = lineNumber;
        return;
      }
    }
  }
  protected void finishLineNumberTable (){
    int len = code.length;
    int[] ln = lineNumbers;
    int lastLine = ln[0];

    for (int i=1; i<len; i++){
      if (ln[i] == 0){
        ln[i] = lastLine;
      } else {
        lastLine = ln[i];
      }
    }
  }

  /**
   * note - this depends on that we already have a code array
   * and that the lines/startPcs are sorted (monotonic increasing)
   */
  public void setLineNumbers (int[] lines, int[] startPcs){
    int j=0;
    int lastLine = -1;
    
    int len = code.length;
    int[] ln = new int[len];

    for (int i=0; i<len; i++){
      Instruction insn = code[i];
      int pc = insn.getPosition();
      
      if ((j < startPcs.length) && pc == startPcs[j]){
        lastLine = lines[j];
        j++;
      }
      
      ln[i] = lastLine;
    }
    
    lineNumbers = ln;
  }

  /**
   * this version takes an already expanded line number array which has to be of
   * the same size as the code array
   */
  public void setLineNumbers (int[] lines){
    if (lines.length != code.length){
      throw new JPFException("inconsitent code/line number size");
    }
    lineNumbers = lines;
  }
  
  @Override
  public String toString() {
    return "MethodInfo[" + getFullName() + ']';
  }
  
  // for debugging purposes
  public void dump(){
    System.out.println("--- " + this);
    for (int i = 0; i < code.length; i++) {
      System.out.printf("%2d [%d]: %s\n", i, code[i].getPosition(), code[i].toString());
    }
  }

  /**
   * Creates a method for a given class, by cloning this MethodInfo
   * and all the instructions belong to the method
   */
  public MethodInfo getInstanceFor(ClassInfo ci) {
    MethodInfo clone;

    try {
      clone = (MethodInfo)super.clone();
      clone.ci = ci;

      clone.globalId = mthTable.size();
      mthTable.add(this);

      if(code == null) {
        clone.code = null;
      } else {
        clone.code = new Instruction[code.length];

        for(int i=0; i<code.length; i++) {
          clone.code[i] = code[i].typeSafeClone(clone);
        }
      }

    } catch (CloneNotSupportedException cnsx){
      cnsx.printStackTrace();
      return null;
    }

    return clone;
  }
}
