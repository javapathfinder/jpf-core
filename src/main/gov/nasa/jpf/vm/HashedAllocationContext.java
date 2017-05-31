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

// see mixinJPFStack() comments
import sun.misc.SharedSecrets;
import sun.misc.JavaLangAccess;

import gov.nasa.jpf.Config;
import static gov.nasa.jpf.util.OATHash.*;

/**
 * an AllocationContext that uses a hash value for comparison. This is
 * lossy - heap implementations using this class have to check/handle
 * collisions.
 * 
 * However, given that we have very good hash data (search global object
 * references), the probability of collisions is low enough that heap
 * implementations might simply report this as a problem requiring a
 * non-lossy AllocationContext.
 * 
 * Ideally, we would like to hash the host VM thread context too (esp.
 * for system allocations), but host VM stack traces are expensive, and it is
 * arguable if would be too strict (e.g. when using a dedicated allocator
 * method called from alternative branches of the caller) 
 * 
 * note - this is a HashMap key type which has to obey the hashCode/equals contract
 */
public class HashedAllocationContext implements AllocationContext {
    
  static final Throwable throwable = new Throwable(); // to avoid frequent allocations
    
  static int mixinSUTStack (int h, ThreadInfo ti) {
    h = hashMixin( h, ti.getId());

    // we don't want to mixin the stack slots (locals and operands) because this would
    // cause state leaks (different hash) if there are changed slot values that do not
    // relate to the allocation
    
    for (StackFrame frame = ti.getTopFrame(); frame != null; frame = frame.getPrevious() ) {
      if (!(frame instanceof DirectCallStackFrame)) {
        Instruction insn = frame.getPC();
        
        //h = hashMixin(h, insn.hashCode()); // this is the Instruction object system hash - not reproducible between runs
        
        h = hashMixin( h, insn.getMethodInfo().getGlobalId()); // the method
        h = hashMixin( h, insn.getInstructionIndex()); // the position within the method code
        h = hashMixin( h, insn.getByteCode()); // the instruction type
      }
    }
    
    return h;
  }
  
  /*
   * this is an optimization to cut down on host VM StackTrace acquisition, since we just need one
   * element.
   * 
   * NOTE: this is more fragile than Throwable.getStackTrace() and String.equals() since it assumes
   * availability of the sun.misc.JavaLangAccess SharedSecret and invariance of classname strings.
   * 
   * The robust version would be
   *   ..
   *   throwable.fillInStackTrace();
   *   StackTraceElement[] ste = throwable.getStackTrace();
   *   StackTraceElement e = ste[4];
   *   if (e.getClassName().equals("gov.nasa.jpf.vm.MJIEnv") && e.getMethodName().startsWith("new")){ ..
   */ 
  
   static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();
   static final String ENV_CLSNAME = MJIEnv.class.getName();
  
  // <2do> this method is problematic - we should not assume a fixed stack position
  // but we can't just mixin the whole stack since this would cause different class object
  // allocation contexts (registerClass can happen from lots of locations).
  // At the other end of the spectrum, MJIEnv.newXX() is not differentiating enough since
  // those are convenience methods used from a gazillion of places that might share
   // the same SUT state
  static int mixinJPFStack (int h) {
    throwable.fillInStackTrace();
    
    // we know the callstack is at least 4 levels deep:
    //   0: mixinJPFStack
    //   1: getXAllocationContext
    //   2: heap.getXAllocationContext
    //   3: heap.newObject/newArray/newString
    //   4: <allocating method>
    //   ...

    // note that it is not advisable to mixin more than the immediate newX() caller since
    // this would create state leaks for allocations that are triggered by SUT threads and
    // have different native paths (e.g. Class object creation caused by different SUT thread context)
    
    StackTraceElement e = JLA.getStackTraceElement(throwable, 4); // see note below regarding fixed call depth fragility
    // <2do> this sucks - MJIEnv.newObject/newArray/newString are used from a gazillion of places that might not differ in SUT state
    if (e.getClassName() == ENV_CLSNAME && e.getMethodName().startsWith("new")){
      // there is not much use to loop, since we don't have a good end condition
      e = JLA.getStackTraceElement(throwable, 5);
    }
          
    // NOTE - this is fragile since it is implementation dependent and differs
    // between JPF runs
    // the names are interned string from the class object
    // h = hashMixin( h, System.identityHashCode(e.getClassName()));
    // h = hashMixin( h, System.identityHashCode(e.getMethodName()));

    // this should be reproducible, but the string hash is bad
    h = hashMixin(h, e.getClassName().hashCode());
    h = hashMixin(h, e.getMethodName().hashCode());
    h = hashMixin(h, e.getLineNumber());
    
    return h;
  }
  
  /*
   * !! NOTE: these always have to be at a fixed call distance of the respective Heap.newX() call:
   * 
   *  ConcreteHeap.newX()
   *    ConcreteHeap.getXAllocationContext()
   *      ConcreteAllocationContext.getXAllocationContext()
   *      
   * that means the allocation site is at stack depth 4. This is not nice, but there is no
   * good heuristic we could use instead, other than assuming there is a newObject/newArray/newString
   * call on the stack
   */
  
  /**
   * this one is for allocations that should depend on the SUT thread context (such as all
   * explicit NEW executions)
   */
  public static AllocationContext getSUTAllocationContext (ClassInfo ci, ThreadInfo ti) {
    int h = 0;
    
    //--- the type that gets allocated
    h = hashMixin(h, ci.getUniqueId()); // ClassInfo instances can change upon backtrack
    
    //--- the SUT execution context (allocating ThreadInfo and its stack)
    h = mixinSUTStack( h, ti);
    
    //--- the JPF execution context (from where in the JPF code the allocation happens)
    h = mixinJPFStack( h);
    
    h = hashFinalize(h);
    HashedAllocationContext ctx = new HashedAllocationContext(h);

    return ctx;
  }
  
  /**
   * this one is for allocations that should NOT depend on the SUT thread context (such as
   * automatic allocation of java.lang.Class objects by the VM)
   * 
   * @param anchor a value that can be used to provide a context that is heap graph specific (such as
   * a classloader or class object reference)
   */
  public static AllocationContext getSystemAllocationContext (ClassInfo ci, ThreadInfo ti, int anchor) {
    int h = 0;
    
    h = hashMixin(h, ci.getUniqueId()); // ClassInfo instances can change upon backtrack
    
    // in lieu of the SUT stack, add some magic salt and the anchor
    h = hashMixin(h, 0x14040118);
    h = hashMixin(h, anchor);
    
    //--- the JPF execution context (from where in the JPF code the allocation happens)
    h = mixinJPFStack( h);
    
    h = hashFinalize(h);
    HashedAllocationContext ctx = new HashedAllocationContext(h);

    return ctx;
  }

  public static boolean init (Config conf) {
    //pool = new SparseObjVector<HashedAllocationContext>();
    return true;
  }
  
  //--- instance data
  
  // rolled up hash value for all context components
  protected final int id;

  
  //--- instance methods
  
  protected HashedAllocationContext (int id) {
    this.id = id;
  }
  
  @Override
  public boolean equals (Object o) {
    if (o instanceof HashedAllocationContext) {
      HashedAllocationContext other = (HashedAllocationContext)o;
      return id == other.id; 
    }
    
    return false;
  }
  
  /**
   * @pre: must be the same for two objects that result in equals() returning true
   */
  @Override
  public int hashCode() {
    return id;
  }
  
  // for automatic field init allocations
  @Override
  public AllocationContext extend (ClassInfo ci, int anchor) {
    //int h = hash( id, anchor, ci.hashCode());
    int h = hashMixin(id, anchor);
    h = hashMixin(h, ci.getUniqueId());
    h = hashFinalize(h);
    
    return new HashedAllocationContext(h);
  }
}
