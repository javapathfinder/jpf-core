package gov.nasa.jpf.vm;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.jpf.annotation.MJI;

/**
 * NativePeer for {@link StackStreamFactory}
 */
public class JPF_java_lang_StackStreamFactory extends NativePeer {

  /**
   * NativePeer method for {@link StackStreamFactory#checkStackWalkModes()}
   */
  @MJI
  public static boolean checkStackWalkModes____Z(MJIEnv env, int cref) {
    // supposed to return false, if StackWalker mode values do not match with JVM
    return true;
  }

  // java.lang.StackWalker traverses stack from top to bottom,
  // and it works like this:
  // 1. StackWalker.walk() receives a stream function.
  // 2. walk() indirectly calls native method callStackWalk() to
  //    fetch stack frames from JVM and apply the stream function.
  // 3. callStackFrame() does this by fetch first batch of frames
  //    and call java method doStackWalk() to apply the stream function.
  // 4. Depending on the number of stack frames and the need of the
  //    stream function, doStackWalk() may call the native method
  //    fetchStackFrames() zero or more times to fetch more stack frames
  //    to apply the stream function.
  public static class AbstractStackWalker extends NativePeer {

    // There may be multiple StackWalkers traversing stacks, and the stack
    // frames are not fetched only once (fetchStackFrames() may be called multiple
    // times to fetch frames incrementally). This HashMap is used to record
    // where to continue fetching frames (we use hash code of the next frame
    // to fetch after we fetch the first batch in callStackWalk() as the key
    // and the `StackFrame` to start fetching next time as the value).
    //
    // Generation of entry:
    //     The key is generated at the start of traversal, i.e., when
    //     callStackWalk() is first invoked indirectly by StackWalker.walk().
    //     And the info is also stored at this time.
    // Usage and update:
    //     The key is passed to fetchStackFrames() as its argument. And the
    //     stored info is updated each time it fetches more frames.
    // Deletion:
    //     The stored info is deleted at the end of traversal, i.e., at return
    //     of callStackWalk().
    Map<Integer, StackFrame> nextStartFrames = new HashMap<>();

    private StackFrame getFirstNonStackWalkerFrame(ThreadInfo ti) {
      StackFrame curFrame = ti.getTopFrame();
      while (true) {
        ClassInfo ci = curFrame.getClassInfo();
        boolean isStackWalker =
            null != ci.getSuperClass("java.lang.StackWalker");
        boolean isAbstractStackWalker =
            null != ci.getSuperClass("java.lang.StackStreamFactory$AbstractStackWalker");
        if (!isStackWalker && !isAbstractStackWalker) {
          break;
        }
        curFrame = curFrame.getPrevious();
      }
      return curFrame;
    }

    private boolean isBottomFrame(StackFrame frame) {
      return frame == null;
    }

    private ElementInfo buildFrameInfoObj(MJIEnv env, StackFrame curFrame) {
      // Prepare info needed by java.lang.StackFrameInfo
      int declaringClsObjRef = curFrame.getMethodInfo().getClassInfo().getClassObjectRef();
      int methodNameRef = env.newString(curFrame.getMethodName());
      int descriptorRef = env.newString(curFrame.getMethodInfo().getSignature());
      int bci = curFrame.getPC().getPosition();
      int fileNameRef = env.newString(curFrame.getSourceFile());
      int lineNumber = curFrame.getLine();
      boolean isNative = curFrame.isNative();

      ClassInfo frameInfoCls = env.getSystemClassLoaderInfo().getResolvedClassInfo("java.lang.StackFrameInfo");
      ElementInfo frameInfoObj = env.getHeap().newObject(frameInfoCls, env.getThreadInfo());
      frameInfoObj.setReferenceField("declaringClass", declaringClsObjRef);
      frameInfoObj.setReferenceField("methodName", methodNameRef);
      // TODO: set methodType
      frameInfoObj.setReferenceField("descriptor", descriptorRef);
      frameInfoObj.setIntField("bci", bci);
      frameInfoObj.setReferenceField("fileName", fileNameRef);
      frameInfoObj.setIntField("lineNumber", lineNumber);
      frameInfoObj.setBooleanField("isNative", isNative);
      return frameInfoObj;
    }

    @MJI
    public int callStackWalk__JIII_3Ljava_lang_Object_2__Ljava_lang_Object_2(MJIEnv env,
                                                                             int objRef,
                                                                             long mode,
                                                                             int skipframes,
                                                                             int batchSize,
                                                                             int startIndex,
                                                                             int frameArrayRef) {
      ThreadInfo ti = env.getThreadInfo();
      DirectCallStackFrame ret = ti.getReturnedDirectCall();
      if (ret != null) {
        // Stack traversal is finished, we do two things:
        //   1. Clear the mark of next stack frame for this traversal
        //   2. Return the stack traversal result
        int nextStartFrameIdToRemove = (Integer) ret.getFrameAttr();
        nextStartFrames.remove(nextStartFrameIdToRemove);
        return ret.getReferenceResult();
      }

      ElementInfo frameBufObj = env.getElementInfo(frameArrayRef);
      int[] frameRefArray = frameBufObj.asReferenceArray();
      int idx = 0;
      StackFrame curFrame = getFirstNonStackWalkerFrame(ti);
      for (; !isBottomFrame(curFrame); curFrame = curFrame.getPrevious()) {
        // Since direct call frames are JPF's implementation details
        // and have not java level correspondence, and we mainly use
        // JDK library to implement StackWalker, which don't expect
        // to see them, we should hide these frames.
        if (curFrame.isDirectCallFrame()) {
          continue;
        }
        ElementInfo frameInfoObj = buildFrameInfoObj(env, curFrame);
        frameRefArray[startIndex + idx] = frameInfoObj.getObjectRef();
        idx++;
        if (idx >= batchSize) {
          break;
        }
      }

      int nextStartFrameId = 1;
      if (!isBottomFrame(curFrame)) {
        StackFrame nextStartFrame = curFrame.getPrevious();
        // Use hash code of the first frame of the remaining frame
        // batches as the key, since we may not need this key-value
        // pair if we don't have remaining batches.
        nextStartFrameId = nextStartFrame.hashCode();
        nextStartFrames.put(nextStartFrameId, nextStartFrame);
      }

      // Similar to OpenJDK's implementation, we have to invoke the java method
      // doStackWalk() from our native method. What doStackWalk() mainly does is
      // to apply the stream function (the argument of StackWalker.walk()) to the
      // batch of frames. It may or may not invoke fetchStackFrames() to fetch
      // more frames from JVM, which depends on the number of stack frames
      // and the need of the stream function
      MethodInfo doStackWalkMtd = env.getSystemClassLoaderInfo()
        .getResolvedClassInfo("java.lang.StackStreamFactory$AbstractStackWalker")
        .getMethod("doStackWalk(JIIII)Ljava/lang/Object;", false);
      DirectCallStackFrame doWalkFrame = doStackWalkMtd.createDirectCallStackFrame(ti, 7);
      doWalkFrame.setArgument(0, objRef, null);
      doWalkFrame.setLongArgument(1, nextStartFrameId, null);
      doWalkFrame.setArgument(3, skipframes, null);
      doWalkFrame.setArgument(4, batchSize, null);
      doWalkFrame.setArgument(5, startIndex, null);
      doWalkFrame.setArgument(6, startIndex + idx, null);
      doWalkFrame.setFrameAttr(nextStartFrameId);
      ti.pushFrame(doWalkFrame);

      // Dummy value. After this native call returns, execution will
      // continue from doStackWalk() method. The real return value
      // is the one returned by doStackWalk() (refer to the call to
      // getReturnedDirectCall() at this method start).
      return MJIEnv.NULL;
    }

    // Used to fetch more frames from JVM. In order to fetch frames just following
    // what is fetched last time, this method needs a state. In JDK's implementation,
    // this state is stored in the JVM and is associated with a "key", which is the
    // `anchor` argument here. In our implementation, we also use `anchor` as a key.
    // More specifically, for each StackWalker.walk(), we give it a key and store a
    // map from this key to the frame the traversal should start from next time.
    // Every time we need to fetch more frames (in fetchStackFrames()), we fetch the
    // start frame from this map use the key (`anchor`). At the end of traversal,
    // we clear the stored mapping for this key (in callStackWalk()).
    @MJI
    public int fetchStackFrames__JJII_3Ljava_lang_Object_2__I(MJIEnv env,
                                                              int objRef,
                                                              long mode,
                                                              long anchor,
                                                              int batchSize,
                                                              int startIndex,
                                                              int frameArrayRef) {
      if (batchSize <= 0) {
        return startIndex;
      }

      int nextStartFrameId = (int) anchor;
      ElementInfo frameBufObj = env.getElementInfo(frameArrayRef);
      int[] frameRefArray = frameBufObj.asReferenceArray();
      int idx = 0;
      StackFrame curFrame = nextStartFrames.get(nextStartFrameId);
      for (; !isBottomFrame(curFrame); curFrame = curFrame.getPrevious()) {
        // Hide JPF's implementation details from JDK library
        if (curFrame.isDirectCallFrame()) {
          continue;
        }
        ElementInfo frameInfoObj = buildFrameInfoObj(env, curFrame);
        frameRefArray[startIndex + idx] = frameInfoObj.getObjectRef();
        idx++;
        if (idx >= batchSize) {
          break;
        }
      }

      if (!isBottomFrame(curFrame)) {
        StackFrame nextStartFrame = curFrame.getPrevious();
        nextStartFrames.put(nextStartFrameId, nextStartFrame);
      }
      return startIndex + idx;
    }
  }
}
