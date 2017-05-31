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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.DFSearch;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.heuristic.BFSHeuristic;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A listener that tracks information about the stack depth of when a lock is first acquired.  If 
 *
 * Writing a test for this class is very difficult.  Hence, a lot of asserts are added.
 */
public class LockedStackDepth extends ListenerAdapter
{
   private static final Logger  s_logger    = JPF.getLogger(LockedStackDepth.class.getName());
   private static final Integer EMPTY[]     = new Integer[0];
   private static final int     THREAD_FLAG = 0x80000000;
   
   private final HashMap<Integer, Operation> m_operations = new HashMap<Integer, Operation>();
   private final HashMap<Integer, Integer>   m_state      = new HashMap<Integer, Integer>();
   private final HashMap<Operation, Integer> m_index      = new HashMap<Operation, Integer>();
   private final ArrayList<Operation>        m_apply      = new ArrayList<Operation>();
   private       Operation                   m_current;

   public int getLockedStackDepth(ElementInfo lock)
   {
      Integer result;
      int lockIndex;
      
      lockIndex = lock.getObjectRef();
      result    = m_state.get(makeKey(lock));
      
      if (s_logger.isLoggable(Level.INFO))
         s_logger.info("Depth = " + result + " | Lock Index = " + lockIndex + " | Lock = " + lock);
      
      if (result == null)
         return(-1);
      
      assert result >= 0;
      
      return(result);
   }
   
   public List<ElementInfo> getLockedInTopFrame(ThreadInfo thread)
   {
      ArrayList<ElementInfo> result;
      ElementInfo lock;
      int threadDepth;
      
      threadDepth = thread.getStackDepth();
      result      = new ArrayList<ElementInfo>();
      
      for (Integer key : m_state.keySet())
      {
         if (key < 0)
            continue;
         
         if (threadDepth != m_state.get(key))
            continue;
         
         lock = thread.getElementInfo(key);
         
         if (lock == null)
            continue;
         
         if (!lock.isLockedBy(thread))
            continue;
            
         result.add(lock);
      }
      
      return(result);
   }

   @Override
   public void objectLocked(VM vm, ThreadInfo thread, ElementInfo ei)
   {
      ElementInfo lock;
      Integer depth;

      lock   = ei;

      logStack(thread);

      depth  = new Operation(thread, null).getOldDepth();

      if (depth == null)
         depth = thread.getStackDepth();

      assert thread.getLockCount() == 0;
      assert thread.getLockObject() == null;
      assert lock.isLockedBy(thread);
      
      if (m_state.containsKey(makeKey(lock)))               // So that a breakpoint on the next line will only get hit if the assert will trigger.
         assert !m_state.containsKey(makeKey(lock));
            
      assert !m_state.containsKey(makeKey(thread));
      assert depth >= 0;

      new Operation(lock, depth);
   }

   @Override
   public void objectUnlocked(VM vm, ThreadInfo thread, ElementInfo ei)
   {
      ElementInfo lock;
      Integer depth;

      logStack(thread);

      lock   = ei;
      depth  = new Operation(lock, null).getOldDepth();

      assert !m_state.containsKey(makeKey(lock));
      assert !m_state.containsKey(makeKey(thread));
      assert depth >= 0;

      if (thread.isWaiting())
      {
         assert !lock.isLockedBy(thread);
         assert lock.getLockCount() == 0;
         assert thread.getLockCount() > 0;
         assert thread.getLockObject() == lock;
         new Operation(thread, depth);
      }
      else
      {
         assert lock.isLockedBy(thread);
         assert lock.getLockCount() > 0;
         assert thread.getLockCount() == 0;
         assert thread.getLockObject() == null;
      }
   }

   @Override
   public void searchStarted(Search search)
   {
      m_operations.clear();
      m_state.clear();
      
      m_current = null;
   }

   @Override
   public void stateAdvanced(Search search)
   {
      Integer id;
      
      id = search.getStateId();
      
      if (!m_operations.containsKey(id))       // Don't overwrite the original chain of Operations to get to the same state.  The original chain is more likely to be shorter.
         m_operations.put(id, m_current);

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Advanced: " + id);
      
      logState();
   }

   @Override
   public void stateProcessed(Search search)
   {
      Integer id;

      if (!(search instanceof DFSearch))  // Can't remove from m_operations since Search could go back to the state.
         if (!(search instanceof BFSHeuristic))
            return;

      id = search.getStateId();

      m_operations.remove(id);            // DFSearch won't ever revisit this state.  It is safe to remove and allow for cleanup.

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Processed: " + id);
   }

   @Override
   public void stateBacktracked(Search search)
   {
      switchTo(search);
   }

   @Override
   public void stateRestored(Search search)
   {
      switchTo(search);
   }
   
   private void switchTo(Search search)
   {
      Operation next;
      Integer id;
      
      id   = search.getStateId();
      next = m_operations.get(id);

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Switching: " + id);

      assert (id <= 0) || (m_operations.containsKey(id));
      
      switchTo(next);
      
      m_current = next;

      logState();

      if (s_logger.isLoggable(Level.FINE))
         s_logger.fine("State Switched:  " + id);
   }
   
   private void switchTo(Operation next)
   {
      Operation operation;
      Integer index;
      int i;
      
      for (operation = next; operation != null; operation = operation.getParent())  // Go through all of the operations leading back to the root.
      {
         m_index.put(operation, m_apply.size());  // Keep track of the index into m_apply where operation is found
         m_apply.add(operation);
      }
      
      index = null;
      
      for (operation = m_current; operation != null; operation = operation.getParent())  // Go through all of the operations leading back to the root.
      {
         index = m_index.get(operation);
         
         if (index != null)        // If a common ancestor is found, stop going back.
            break;
         
         operation.revert();       // Revert the operation since it isn't common to both states.
      }
      
      if (index == null)
         index = m_apply.size();   // No common ancestor found.  Must apply all of the operations.
      
      for (i = index; --i >= 0; )  // Apply all of the operations required to get back to the "next" state.
         m_apply.get(i).apply();

      m_index.clear();
      m_apply.clear();
   }
   
   private void logState()
   {
      StringBuilder message;
      String type;
      Integer key, keys[], depth;
      int i;
      
      if (!s_logger.isLoggable(Level.FINER))
         return;

      message = new StringBuilder();
      keys    = m_state.keySet().toArray(EMPTY);
      
      Arrays.sort(keys);
      message.append("State | Size = ");
      message.append(keys.length);
      
      for (i = 0; i < keys.length; i++)
      {
         key   = keys[i];
         depth = m_state.get(key);

         if ((key & THREAD_FLAG) != 0)
            type = "Thread";
         else
            type = "Lock";

         message.append('\n');
         message.append("Depth = ");
         message.append(depth);
         message.append(" | Key = ");
         message.append(key & ~THREAD_FLAG);
         message.append(" | ");
         message.append(type);
      }
      
      s_logger.finer(message.toString());
   }
   
   private void logStack(ThreadInfo thread)
   {
      if (!s_logger.isLoggable(Level.FINEST))
         return;
      
      s_logger.finest(thread.getStackTrace());
   }
   
   private static int makeKey(ElementInfo lock)
   {
      return(lock.getObjectRef());
   }
   
   private static int makeKey(ThreadInfo thread)
   {
      return(thread.getThreadObjectRef() ^ THREAD_FLAG);
   }
   
   private class Operation
   {
      private final Operation m_parent;
      private final Integer   m_key;
      private final Integer   m_oldDepth;
      private final Integer   m_newDepth;
      
      public Operation(ElementInfo lock, Integer newDepth)
      {
         this(makeKey(lock), newDepth);
      }
      
      public Operation(ThreadInfo thread, Integer newDepth)
      {
         this(makeKey(thread), newDepth);
      }
      
      private Operation(Integer key, Integer newDepth)
      {
         m_parent   = m_current;
         m_current  = this;
         m_key      = key;
         m_newDepth = newDepth;
         m_oldDepth = m_state.get(key);
         
         apply();
      }
      
      public Operation getParent()
      {
         return(m_parent);
      }
      
      public Integer getOldDepth()
      {
         return(m_oldDepth);
      }
      
      public Integer getNewDepth()
      {
         return(m_newDepth);
      }
      
      public void apply()
      {
         change(m_newDepth);
         log("Apply ");
      }
      
      public void revert()
      {
         change(m_oldDepth);
         log("Revert");
      }
      
      private void change(Integer depth)
      {
         Integer previous;
         
         if (depth == null)
            m_state.remove(m_key);
         else
         {
            previous = m_state.put(m_key, depth);
            
            assert previous == null;
         }
      }
      
      private void log(String header)
      {
         String message, subheader, depthStr, type;
         Integer depth;
         
         if (!s_logger.isLoggable(Level.FINE))
            return;

         if (m_newDepth != null)
         {
            subheader = "Add   ";
            depth     = m_newDepth;
         }
         else
         {
            subheader = "Remove";
            depth     = m_oldDepth;
         }

         depthStr = String.valueOf(depth);
         
         switch (depthStr.length())
         {
            case 1: depthStr = "   " + depthStr; break;
            case 2: depthStr = "  " + depthStr; break;
            case 3: depthStr = " " + depthStr; break;
            default: break;
         }
         
         if ((m_key & THREAD_FLAG) != 0)
            type = "Thread";
         else
            type = "Lock";
         
         message = header + " " + subheader + " | Depth = " + depthStr + " | Key = " + (m_key & ~THREAD_FLAG) + " | " + type;
            
         s_logger.fine(message);
      }
   }
}
