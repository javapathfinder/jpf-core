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
import gov.nasa.jpf.annotation.MJI;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * MJI NativePeer class for java.lang.System library abstraction
 */
public class JPF_java_lang_System extends NativePeer {
  
  @MJI
  public void arraycopy__Ljava_lang_Object_2ILjava_lang_Object_2II__V (MJIEnv env, int clsObjRef,
                                                                              int srcArrayRef, int srcIdx, 
                                                                              int dstArrayRef, int dstIdx,
                                                                              int length) {
    if ((srcArrayRef == MJIEnv.NULL) || (dstArrayRef == MJIEnv.NULL)) {
      env.throwException("java.lang.NullPointerException");
      return;
    }

    ElementInfo eiSrc = env.getElementInfo(srcArrayRef);
    ElementInfo eiDst = env.getModifiableElementInfo(dstArrayRef);
    
    try {
      eiDst.copyElements( env.getThreadInfo(), eiSrc ,srcIdx, dstIdx, length);
    } catch (IndexOutOfBoundsException iobx){
      env.throwException("java.lang.IndexOutOfBoundsException", iobx.getMessage());
    } catch (ArrayStoreException asx){
      env.throwException("java.lang.ArrayStoreException", asx.getMessage());      
    }
  }

  @MJI
  public int getenv__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int clsObjRef,
                                                                         int keyRef){
    String k = env.getStringObject(keyRef);
    String v = System.getenv(k);
    
    if (v == null){
      return MJIEnv.NULL;
    } else {
      return env.newString(v);
    }
  }

  
  int createPrintStream (MJIEnv env, int clsObjRef){
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    StackFrame frame = ti.getTopFrame();
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo("gov.nasa.jpf.ConsoleOutputStream");

    if (ci.initializeClass(ti)) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    return env.newObject(ci);
  }
  
  @MJI
  public int createSystemOut____Ljava_io_PrintStream_2 (MJIEnv env, int clsObjRef){
    return createPrintStream(env,clsObjRef);
  }
  
  @MJI
  public int createSystemErr____Ljava_io_PrintStream_2 (MJIEnv env, int clsObjRef){
    return createPrintStream(env,clsObjRef);
  }
  
  int getProperties (MJIEnv env, Properties p){
    int n = p.size() * 2;
    int aref = env.newObjectArray("Ljava/lang/String;", n);
    int i=0;
    
    for (Map.Entry<Object,Object> e : p.entrySet() ){
      env.setReferenceArrayElement(aref,i++, 
                                   env.newString(e.getKey().toString()));
      env.setReferenceArrayElement(aref,i++,
                                   env.newString(e.getValue().toString()));
    }
    
    return aref;
  }

  int getSysPropsFromHost (MJIEnv env){
    return getProperties(env, System.getProperties());
  }
  
  int getSysPropsFromFile (MJIEnv env){
    Config conf = env.getConfig();
    
    String cf = conf.getString("vm.sysprop.file", "system.properties");
    if (cf != null){
      try {
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream(cf);
        p.load(fis);
        
        return getProperties(env, p);
        
      } catch (IOException iox){
        return MJIEnv.NULL;
      }
    }
    //.. not yet
    return MJIEnv.NULL;
  }
  
  static String JAVA_CLASS_PATH = "java.class.path";
  
  @MJI
  public String getSUTJavaClassPath(VM vm) {
    ClassInfo system = ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.System");
    
    ThreadInfo ti = vm.getCurrentThread();
    Heap heap = vm.getHeap();
    ElementInfo eiClassPath = heap.newString(JAVA_CLASS_PATH, ti);
    
    MethodInfo miGetProperty = system.getMethod("getProperty(Ljava/lang/String;)Ljava/lang/String;", true);
    DirectCallStackFrame frame = miGetProperty.createDirectCallStackFrame(ti, 0);
    frame.setReferenceArgument( 0, eiClassPath.getObjectRef(), null);
    frame.setFireWall(); // we don't want exceptions to escape into the SUT
    
    
    try {
      ti.executeMethodHidden(frame);
      
    } catch (UncaughtException e) {
       ti.clearPendingException();
       StackFrame caller = ti.popAndGetModifiableTopFrame();
       caller.advancePC();
       return null;
    }
    
    int ref = frame.peek();
    ElementInfo metaResult = heap.get(ref);
    String result = metaResult.asString();
    
    return result;
  }
  
  int getSelectedSysPropsFromHost (MJIEnv env){
    Config conf = env.getConfig();
    String keys[] = conf.getStringArray("vm.sysprop.keys");

    if (keys == null){
      String[] defKeys = {
        "path.separator",
        "line.separator", 
        "file.separator",
        "user.name",
        "user.dir",
        "user.timezone",
        "user.country",
        "java.home",
        "java.version",
        "java.io.tmpdir",
        JAVA_CLASS_PATH
        //... and probably some more
        // <2do> what about -Dkey=value commandline options
      };
      keys = defKeys;
    }
    
    int aref = env.newObjectArray("Ljava/lang/String;", keys.length * 2);
    int i=0;
    
    for (String s : keys) {
      String v;
      
      int idx = s.indexOf('/');
      if (idx >0){ // this one is an explicit key/value pair from vm.system.properties
        v = s.substring(idx+1);
        s = s.substring(0,idx);
        
      } else {
        // the special beasts first (if they weren't overridden with vm.system.properties)
        if (s == JAVA_CLASS_PATH) {
          // maybe we should just use vm.classpath
          // NOTE: the curent classloader at the point it has to be a system classloader.
          ClassPath cp = ClassLoaderInfo.getCurrentClassLoader().getClassPath();
          // <2do> should be consistent with path.separator (this is host VM notation)
          v = cp.toString();
          
        } else { // we bluntly grab it from the host VM properties
          v = System.getProperty(s);
        }
      }
            
      if (v != null){
        env.setReferenceArrayElement(aref,i++, env.newString(s));
        env.setReferenceArrayElement(aref,i++, env.newString(v));
      }
    }
        
    return aref;
  }

  /**
   * policy of how to initialize system properties of the system under test
   */
  static enum SystemPropertyPolicy {
    SELECTED,  // copy host values for keys specified in  
    FILE, 
    HOST
  };

  @MJI
  public int getKeyValuePairs_____3Ljava_lang_String_2 (MJIEnv env, int clsObjRef){
    Config conf = env.getConfig();
    SystemPropertyPolicy sysPropSrc = conf.getEnum( "vm.sysprop.source", SystemPropertyPolicy.values(), SystemPropertyPolicy.SELECTED);

    if (sysPropSrc == SystemPropertyPolicy.FILE){
      return getSysPropsFromFile(env);
    } else if (sysPropSrc == SystemPropertyPolicy.HOST){
      return getSysPropsFromHost(env);
    } else if (sysPropSrc == SystemPropertyPolicy.SELECTED){
      return getSelectedSysPropsFromHost(env);
    }
    
    return 0;
  }
  
  // <2do> - this break every app which uses time delta thresholds
  // (sort of "if ((t2 - t1) > d)"). Ok, we can't deal with
  // real time, but we could at least give some SystemState dependent
  // increment
  @MJI
  public long currentTimeMillis____J (MJIEnv env, int clsObjRef) {
    return env.currentTimeMillis();
  }

  // <2do> - likewise. Java 1.5's way to measure relative time
  @MJI
  public long nanoTime____J (MJIEnv env, int clsObjRef) {
    return env.nanoTime();
  }  
  
  // this works on the assumption that we sure break the transition, and
  // then the search determines that it is an end state (all terminated)
  @MJI
  public void exit__I__V (MJIEnv env, int clsObjRef, int ret) {
    ThreadInfo ti = env.getThreadInfo();
    env.getVM().terminateProcess(ti);
  }

  @MJI
  public void gc____V (MJIEnv env, int clsObjRef) {
    env.getSystemState().activateGC();
  }

  @MJI
  public int identityHashCode__Ljava_lang_Object_2__I (MJIEnv env, int clsObjRef, int objref) {
    return (objref ^ 0xABCD);
  }
  
}
