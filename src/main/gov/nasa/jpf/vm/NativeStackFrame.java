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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.NATIVERETURN;
import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.Misc;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * a stack frame for MJI methods
 * 
 * This is a special Stackframe to execute NativeMethodInfos, which are just a wrapper around Java reflection
 * calls. As required by the Java reflection API, they can store argument and return values as object references
 *
 * NOTE: operands and locals can be, but are not automatically used during
 * native method execution.
 */
public abstract class NativeStackFrame extends StackFrame {


  // we don't use the operand stack or locals for arguments and return value
  // because (1) they don't have the right representation (host VM),
  // (2) for performance reasons (no array alloc), and (3) because there is no
  // choice point once we enter a native method, so there is no need to do
  // copy-on-write on the ThreadInfo callstack. Native method execution is
  // atomic (leave alone roundtrips of course)

  // return value registers
  protected Object ret;
  protected Object retAttr;

  // our argument registers
  protected Object[] args;

  public NativeStackFrame (NativeMethodInfo mi){
    super( mi, 0, 0);
  }
  
  public void setArgs (Object[] args){
    this.args = args; 
  }

  @Override
  public StackFrame clone () {
    NativeStackFrame sf = (NativeStackFrame) super.clone();

    if (args != null) {
      sf.args = args.clone();
    }

    return sf;
  }

  @Override
  public boolean isNative() {
    return true;
  }

  @Override
  public boolean isSynthetic() {
    return true;
  }

  @Override
  public boolean modifiesState() {
    // native stackframes don't do anything with their operands or locals per se
    // they are executed atomically, so there is no need to ever restore them
    return false;
  }

  @Override
  public boolean hasAnyRef() {
    return false;
  }

  public void setReturnAttr (Object a){
    retAttr = a;
  }

  public void setReturnValue(Object r){
    ret = r;
  }

  public void clearReturnValue() {
    ret = null;
    retAttr = null;
  }

  public Object getReturnValue() {
    return ret;
  }

  public Object getReturnAttr() {
    return retAttr;
  }

  public Object[] getArguments() {
    return args;
  }

  @Override
  public void markThreadRoots (Heap heap, int tid) {
    // what if some listener creates a CG post-EXECUTENATIVE or pre-NATIVERETURN?
    // and the native method returned an object?
    // on the other hand, we have to make sure we don't mark a return value from
    // a previous transition

    if (pc instanceof NATIVERETURN){
      if (ret != null && ret instanceof Integer && mi.isReferenceReturnType()) {
        int ref = ((Integer) ret).intValue();
        heap.markThreadRoot(ref, tid);
      }
    }
  }

  @Override
  protected void hash (HashData hd) {
    super.hash(hd);

    if (ret != null){
      hd.add(ret);
    }
    if (retAttr != null){
      hd.add(retAttr);
    }

    for (Object a : args){
      hd.add(a);
    }
  }

  @Override
  public boolean equals (Object object) {
    if (object == null || !(object instanceof NativeStackFrame)){
      return false;
    }

    if (!super.equals(object)){
      return false;
    }

    NativeStackFrame o = (NativeStackFrame)object;

    if (ret != o.ret){
      return false;
    }
    if (retAttr != o.retAttr){
      return false;
    }

    if (args.length != o.args.length){
      return false;
    }

    if (!Misc.compare(args.length, args, o.args)){
      return false;
    }

    return true;
  }

  @Override
  public String toString () {
    StringWriter sw = new StringWriter(128);
    PrintWriter pw = new PrintWriter(sw);

    pw.print("NativeStackFrame@");
    pw.print(Integer.toHexString(objectHashCode()));
    pw.print("{ret=");
    pw.print(ret);
    if (retAttr != null){
      pw.print('(');
      pw.print(retAttr);
      pw.print(')');
    }
    pw.print(',');
    printContentsOn(pw);
    pw.print('}');

    return sw.toString();
  }
  
  //--- NativeStackFrames aren't called directly and have special return value processing (in NATIVERETURN.execute())
  @Override
  public void setArgumentLocal (int idx, int value, Object attr){
    throw new JPFException("NativeStackFrames don't support setting argument locals");
  }
  @Override
  public void setLongArgumentLocal (int idx, long value, Object attr){
    throw new JPFException("NativeStackFrames don't support setting argument locals");    
  }
  @Override
  public void setReferenceArgumentLocal (int idx, int ref, Object attr){
    throw new JPFException("NativeStackFrames don't support setting argument locals");
  }
  
  //--- exception refs
  @Override
  public void setExceptionReference (int exRef){
    throw new JPFException("NativeStackFrames don't support exception handlers");    
  }

  @Override
  public int getExceptionReference (){
    throw new JPFException("NativeStackFrames don't support exception handlers");    
  }

  @Override
  public void setExceptionReferenceAttribute (Object attr){
    throw new JPFException("NativeStackFrames don't support exception handlers");    
  }
  
  @Override
  public Object getExceptionReferenceAttribute (){
    throw new JPFException("NativeStackFrames don't support exception handlers");    
  }
  
  @Override
  public int getResult(){
    Object r = ret;
    
    if (r instanceof Number){
      if (r instanceof Double){
        throw new JPFException("result " + ret + " can't be converted into int");    
      } else if (r instanceof Float){
        return Float.floatToIntBits((Float)r);
      } else {
        return ((Number)r).intValue();
      }
    } else if (r instanceof Boolean){
      return (r == Boolean.TRUE) ? 1 : 0;
    } else {
      throw new JPFException("result " + ret + " can't be converted into raw int value");
    }
  }
  
  @Override
  public int getReferenceResult(){
    if (ret instanceof Integer){
      return (Integer)ret; // MJI requires references to be returned as 'int'
    } else {
      throw new JPFException("result " + ret + " can't be converted into JPF refrence value");      
    }
  }
  
  @Override
  public long getLongResult(){
    Object r = ret;
    if (r instanceof Long){
      return (Long)r;
    } else if (r instanceof Double){
      return Double.doubleToLongBits((Double)r);
    } else {
      throw new JPFException("result " + ret + " can't be converted into raw long value");      
    }
  }
  
  @Override
  public Object getResultAttr(){
    return retAttr;
  }
  @Override
  public Object getLongResultAttr(){
    return retAttr;    
  }
}
