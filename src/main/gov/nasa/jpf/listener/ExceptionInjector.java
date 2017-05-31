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

package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

import java.util.HashMap;

/**
 * listener to inject exceptions according to user specifications. This
 * tool is meant to be used for exception handler verification, esp. if
 * exceptions thrown by 3rd party code would be hard to produce.
 *
 * Exceptions are specified as a list of xSpec'@'location pairs.
 *
 * ExceptionSpec is specified as a class name, with optional details parameter. If no
 * package is specified, either java.lang or default package are assumed
 *
 * Location can be 
 *   - class:line
 *   - fully qualified method (callee that is supposed to throw, which is
 *     NOT executed in this case)
 *   - fully qualified method ':' lineOffset
 *
 * for line/offest based locations, either the first or last insn associated
 * with this line (depending on ei.throwFirst=true|false) is not executed
 * but replaced with throwing the exception.
 *
 * Method body line offsets count from the first statement line in the method body
 *
 * Examples:
 *   IOException@x.Foobar:42
 *   NullPointerException@x.SomeClass.computeSomething(Ljava/lang/String;I)
 *   y.MyException("something went wrong")@x.SomeClass.foo(D):10
 */

public class ExceptionInjector extends ListenerAdapter {

  boolean throwFirst; // for location targets, throw on first insn associated with line

  static class ExceptionEntry {
    Instruction insn;
    ExceptionSpec xSpec;
    Location loc;

    ExceptionEntry next;  // there might be more than one for one class

    ExceptionEntry (ExceptionSpec xSpec, Location loc, ExceptionEntry next){
      this.xSpec = xSpec;
      this.loc = loc;
      this.next = next;
    }

    String getLocationClassName() {
      return loc.className;
    }

    String getMethod() {
      return loc.method;
    }

    int getLine() {
      return loc.line;
    }

    ClassInfo getExceptionClassInfo(ThreadInfo ti) {
      return ClassLoaderInfo.getCurrentResolvedClassInfo(xSpec.xClsName);
    }

    String getExceptionDetails() {
      return xSpec.details;
    }

    @Override
	public String toString() {
      return xSpec.toString() + '@' + loc.toString();
    }
  }

  static class ExceptionSpec {
    String xClsName;
    String details;

    ExceptionSpec (String xClsName, String details){
      this.xClsName = xClsName;
      this.details = details;
    }
    
    @Override
	public String toString() {
      if (details == null){
        return xClsName;
      } else {
        StringBuilder sb = new StringBuilder(xClsName);
        sb.append('(');
        if (!details.isEmpty()){
          sb.append('"');
          sb.append(details);
          sb.append('"');
        }
        sb.append(')');
        return sb.toString();
      }
    }
  }

  static class Location {
    String className;
    String method; // name + signature
    int line;

    Location (String className, String method, int line){
      this.className = className;
      this.method = method;
      this.line = line;
    }

    @Override
	public String toString() {
      StringBuilder sb = new StringBuilder(className);
      if (method != null){
        sb.append('.');
        sb.append(method);
      }
      if (line >= 0){
        sb.append(':');
        sb.append(line);
      }
      return sb.toString();
    }
  }

  // these two are used to process classes at loadtime
  HashMap<String,ExceptionEntry> targetClasses = new HashMap<String,ExceptionEntry>();
  HashMap<String,ExceptionEntry> targetBases = new HashMap<String,ExceptionEntry>();
  
  // methods and instructions to watch for at runtime will have ExceptionEntry attrs


  public ExceptionInjector (Config config, JPF jpf){
    throwFirst = config.getBoolean("ei.throw_first", false);
    String[] xSpecs = config.getStringArray("ei.exception", new char[] {';'});

    if (xSpecs != null){
      for (String xSpec : xSpecs){
        if (!parseException(xSpec)){
          throw new JPFConfigException("invalid exception spec: " + xSpec);
        }
      }
    }

    printEntries();
  }

  boolean parseException (String xSpec){
    int i = xSpec.indexOf('@');
    if (i > 0){
      String typeSpec = xSpec.substring(0, i).trim();
      String locSpec = xSpec.substring(i+1).trim();

      ExceptionSpec type = parseType(typeSpec);
      if (type != null){
        Location loc = parseLocation(locSpec);
        if (loc != null){
          String cls = loc.className;
          int line = loc.line;
          if (line >= 0){
            targetClasses.put(cls, new ExceptionEntry(type,loc,targetClasses.get(cls)));
          } else {
            targetBases.put(cls, new ExceptionEntry(type,loc,targetBases.get(cls)));
          }
          return true;
        }
      }
    }

    return false;
  }

  ExceptionSpec parseType (String spec){
    String cls = null;
    String details = null;

    int i = spec.indexOf('(');
    if (i > 0){
      cls = spec.substring(0, i);

      int j = spec.lastIndexOf(')');
      if (spec.charAt(i+1) == '"'){
        i++;
      }
      if (spec.charAt(j-1) == '"'){
        j--;
      }
      details = spec.substring(i+1, j);
      if (details.isEmpty()){
        details = null;
      }

    } else if (i < 0) {  // no details
      cls = spec;
    }

    if (cls != null){
      return new ExceptionSpec( cls,details);
    }

    return null;
  }

  Location parseLocation (String spec){
    int i = spec.indexOf('(');
    if (i > 0){  // we have a method name
      int j = spec.lastIndexOf('.', i);  // get class part
      if (j > 0){
        String cls = spec.substring(0, j).trim();
        i = spec.indexOf(':');
        if (i > 0){

          String mth = Types.getSignatureName(spec.substring(j+1, i));

          try {
            int line = Integer.parseInt(spec.substring(i + 1));
            if (!cls.isEmpty() && !mth.isEmpty() && line >= 0){
              return new Location(cls, mth, line);
            }
          } catch (NumberFormatException nfx) {
            return null;
          }
        } else {
          String mth = Types.getSignatureName(spec.substring(j+1));
          return new Location(cls,mth, -1);
        }
      }

    } else { // no method
      i = spec.indexOf(':');  // but we need a line number
      if (i > 0){
        String cls = spec.substring(0, i).trim();
        try {
          int line = Integer.parseInt(spec.substring(i+1));
          if (!cls.isEmpty() && line >= 0){
            return new Location (cls, null, line);
          }
        } catch (NumberFormatException nfx){
          return null;
        }
      }
    }

    return null;
  }

  boolean checkTargetInsn (ExceptionEntry e, MethodInfo mi, int[] ln, int line){
    if ((ln[0] <= line) && (ln[ln.length - 1] >= line)) {
      for (int i = 0; i < ln.length; i++) {
        if (ln[i] == line) {
          if (!throwFirst) {
            while ((i++ < ln.length) && (ln[i] == line));
            i--;
          }

          mi.getInstruction(i).addAttr(e);
          return true;
        }
      }
    }

    return false;
  }

  /**
   * get the target insns/methods
   */
  @Override
  public void classLoaded (VM vm, ClassInfo loadedClass){

    nextClassEntry:
    for (ExceptionEntry e = targetClasses.get(loadedClass.getName()); e != null; e = e.next){
      String method = e.getMethod();
      int line = e.getLine();

      if (method != null){  // method or method/line-offset
        for (MethodInfo mi : loadedClass.getDeclaredMethodInfos()){
          if (mi.getUniqueName().startsWith(method)){
            if (line >= 0){ // line offset
              int[] ln = mi.getLineNumbers();
              line += ln[0];
              if (checkTargetInsn(e,mi,ln,line)){
                continue nextClassEntry;
              }
            }
          }
        }

      } else { // absolute line number
        if (line >= 0){
          for (MethodInfo mi : loadedClass.getDeclaredMethodInfos()) {
            int[] ln = mi.getLineNumbers();
            if (checkTargetInsn(e, mi, ln, line)) {
              continue nextClassEntry;
            }
          }
        }
      }
    }

    if (targetBases != null){
      for (; loadedClass != null; loadedClass = loadedClass.getSuperClass()) {
        nextBaseEntry:
        for (ExceptionEntry e = targetBases.get(loadedClass.getName()); e != null; e = e.next){
          String method = e.getMethod();
          for (MethodInfo mi : loadedClass.getDeclaredMethodInfos()){
            if (mi.getUniqueName().startsWith(method)){
              mi.addAttr(e);
              continue nextBaseEntry;
            }
          }
        }
      }
    }
  }

  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute){

    ExceptionEntry e = insnToExecute.getAttr(ExceptionEntry.class);
    if ((e == null) && insnToExecute instanceof JVMInvokeInstruction){
      MethodInfo mi = ((JVMInvokeInstruction) insnToExecute).getInvokedMethod();
      e = mi.getAttr(ExceptionEntry.class);
    }

    if (e != null){
      Instruction nextInsn = ti.createAndThrowException(e.getExceptionClassInfo(ti), e.getExceptionDetails());
      ti.skipInstruction(nextInsn);
      return;
    }
  }
  
  // for debugging purposes
  void printEntries () {
    for (ExceptionEntry e : targetClasses.values()){
      System.out.println(e);
    }
    for (ExceptionEntry e : targetBases.values()){
      System.out.println(e);
    }
  }

  /**
  public static void main (String[] args){
    Config conf = JPF.createConfig(args);
    ExceptionInjector ei = new ExceptionInjector(conf,null);

    ei.parseException("x.y.Zang(\"bang\")@z.Foo.doit(Ljava/lang/Object;I)");
    ei.printEntries();
  }
  **/
}
