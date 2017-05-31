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
package gov.nasa.jpf.test.basic;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.DirectCallStackFrame;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.UncaughtException;

/**
 * native peer class for unit testing MJI
 */
public class JPF_gov_nasa_jpf_test_basic_MJITest extends NativePeer {

  // intercept <clinit>
  @MJI
  public void $clinit (MJIEnv env, int rcls) {
    System.out.println("# entering native <clinit>");
    env.setStaticIntField(rcls, "sdata", 42);
  }

  // intercept MJITest(int i) ctor
  @MJI
  public void $init__I__V (MJIEnv env, int robj, int i) {
    // NOTE : if we directly intercept the ctor, then we also have
    // to take care of calling the proper superclass ctors
    // better approach is to refactor this into a separate native method
    // (say init0(..))
    System.out.println("# entering native <init>(I)");
    env.setIntField(robj, "idata", i);
  }

  @MJI
  public int nativeCreate2DimIntArray__II___3_3I (MJIEnv env, int robj, int size1,
                                              int size2) {
    System.out.println("# entering nativeCreate2DimIntArray()");
    int ar = env.newObjectArray("[I", size1);

    for (int i = 0; i < size1; i++) {
      int ea = env.newIntArray(size2);

      if (i == 1) {
        env.setIntArrayElement(ea, 1, 42);
      }

      env.setReferenceArrayElement(ar, i, ea);
    }

    return ar;
  }

  // check if the non-mangled name lookup works
  @MJI
  public int nativeCreateIntArray (MJIEnv env, int robj, int size) {
    System.out.println("# entering nativeCreateIntArray()");

    int ar = env.newIntArray(size);

    env.setIntArrayElement(ar, 1, 1);

    return ar;
  }

  @MJI
  public int nativeCreateStringArray (MJIEnv env, int robj, int size) {
    System.out.println("# entering nativeCreateStringArray()");

    int ar = env.newObjectArray("Ljava/lang/String;", size);
    env.setReferenceArrayElement(ar, 1, env.newString("one"));

    return ar;
  }

  @MJI
  public void nativeException____V (MJIEnv env, int robj) {
    System.out.println("# entering nativeException()");
    env.throwException("java.lang.UnsupportedOperationException", "caught me");
  }

  @SuppressWarnings("null")
  @MJI
  public int nativeCrash (MJIEnv env, int robj) {
    System.out.println("# entering nativeCrash()");
    String s = null;
    return s.length();
  }

  @MJI
  public int nativeInstanceMethod (MJIEnv env, int robj, double d,
                                          char c, boolean b, int i) {
    System.out.println("# entering nativeInstanceMethod() d=" + d +
            ", c=" + c + ", b=" + b + ", i=" + i);

    if ((d == 2.0) && (c == '?') && b) {
      return i + 2;
    }

    return 0;
  }

  @MJI
  public long nativeStaticMethod__JLjava_lang_String_2__J (MJIEnv env, int rcls, long l,
                                                                  int stringRef) {
    System.out.println("# entering nativeStaticMethod()");

    String s = env.getStringObject(stringRef);

    if ("Blah".equals(s)) {
      return l + 2;
    }

    return 0;
  }


  /*
   * nativeRoundtripLoop shows how to
   *
   *  (1) round trip into JPF executed code from within native methods
   *
   *  (2) loop inside of native methods that do round trips (using the
   *      DirectCallStackFrame's local slots)
   *
   * the call chain is:
   *
   *   JPF: testRoundtripLoop
   *     VM: nativeRoundTripLoop  x 3
   *       JPF: roundtrip
   *         VM: nativeInnerRoundtrip
   */

  @MJI
  public int nativeInnerRoundtrip__I__I (MJIEnv env, int robj, int a){
    System.out.println("# entering nativeInnerRoundtrip()");

    return a+2;
  }

  @MJI
  public int nativeRoundtripLoop__I__I (MJIEnv env, int robj, int a) {
    System.out.println("# entering nativeRoundtripLoop(): " + a);

    MethodInfo mi = env.getClassInfo(robj).getMethod("roundtrip(I)I",false);
    ThreadInfo ti = env.getThreadInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();

    if (frame == null){ // first time
      frame = mi.createDirectCallStackFrame(ti, 1);
      frame.setLocalVariable( 0, 0);
      
      int argOffset = frame.setReferenceArgument(0, robj, null);
      frame.setArgument( argOffset, a+1, null);
      
      ti.pushFrame(frame);

      return 42; // whatever, we come back

    } else { // direct call returned

      // this method can't be executed unless the class is already initialized,
      // i.e. we don't have to check for overlayed clinit calls and the frame
      // has to be the one we pushed
      assert frame.getCallee() == mi;
      
      // this shows how to get information back from the JPF roundtrip into
      // the native method
      int r = frame.getResult(); // the return value of the direct call above
      int i = frame.getLocalVariable(0);

      if (i < 3) { // repeat the round trip
        // we have to reset so that the PC is re-initialized
        frame.reset();
        frame.setLocalVariable(0, i + 1);
        
        int argOffset = frame.setReferenceArgument( 0, robj, null);
        frame.setArgument( argOffset, r+1, null);
        
        ti.pushFrame(frame);
        return 42;

      } else { // done, return the final value
        return r;
      }
    }
  }

  /**
   * this shows how to synchronously JPF-enter a method from native peer or
   * listener code
   */
  @MJI
  public int nativeHiddenRoundtrip__I__I (MJIEnv env, int robj, int a){
    ThreadInfo ti = env.getThreadInfo();
    
    System.out.println("# entering nativeHiddenRoundtrip: " + a);
    MethodInfo mi = env.getClassInfo(robj).getMethod("atomicStuff(I)I",false);

    DirectCallStackFrame frame = mi.createDirectCallStackFrame(ti, 0);
    
    int argOffset = frame.setReferenceArgument( 0, robj, null);
    frame.setArgument( argOffset, a, null);
    frame.setFireWall();

    try {
      ti.executeMethodHidden(frame);
      //ti.advancePC();

    } catch (UncaughtException ux) {  // frame's method is firewalled
      System.out.println("# hidden method execution failed, leaving nativeHiddenRoundtrip: " + ux);
      ti.clearPendingException();
      ti.popFrame(); // this is still the DirectCallStackFrame, and we want to continue execution
      return -1;
    }

    // get the return value from the (already popped) frame
    int res = frame.getResult();

    System.out.println("# exit nativeHiddenRoundtrip: " + res);
    return res;
  }

}
