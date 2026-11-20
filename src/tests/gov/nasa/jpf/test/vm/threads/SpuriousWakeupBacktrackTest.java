package gov.nasa.jpf.test.vm.threads;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Asserts that exploring a spurious wakeup leaves the predecessor state intact
 * for other scheduling alternatives. Two untimed waiters and a runnable thread
 * provide competing choices. {@link RestoreListener} compares waiter states
 * before choice selection with those restored after backtracking in
 * {@link #testSpuriousWakeupRestoresWaiters()}.
 * {@link #testDefaultPolicyRestoresWaiters()} provides the default-policy
 * control; both tests require at least one restoration check, and the spurious
 * policy test also requires restoration after selecting a spurious wakeup.
 */
public class SpuriousWakeupBacktrackTest extends TestJPF {

  /**
   * Record waiter states in choiceGeneratorSet(), before the first advance(),
   * and compare them with the states restored when stateBacktracked() runs.
   * The listener's predecessor map lives in the host VM and is not backtracked.
   */
  public static class RestoreListener extends ListenerAdapter {
    private final Map<ChoiceGenerator<?>, Map<Integer, ThreadInfo.State>> predecessors = new IdentityHashMap<>();
    private final Map<ChoiceGenerator<?>, Integer> selectedWakeups = new IdentityHashMap<>();
    static int checkedRestores;
    static int checkedWakeupRestores;
    static int checkedSelfWakeupRestores;

    @Override
    public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> cg) {
      Map<Integer, ThreadInfo.State> states = new HashMap<>();
      for (ThreadInfo ti : vm.getThreadList().getThreads()) {
        if (ti.getName().startsWith("waiter-") && ti.getState() == ThreadInfo.State.WAITING) {
          states.put(ti.getId(), ti.getState());
        }
      }
      // Record the predecessor with two untimed waiters and a runnable main.
      if (states.size() == 2) {
        predecessors.put(cg, states);
      }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
      Map<Integer, ThreadInfo.State> expected = predecessors.get(cg);
      if (expected != null) {
        for (Integer id : expected.keySet()) {
          assertEquals("choice advancement must not change waiter state before the snapshot",
              ThreadInfo.State.WAITING, vm.getThreadList().getThreadInfoForId(id).getState());
        }
      }
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo ti, Instruction insn) {
      ChoiceGenerator<?> cg = vm.getSystemState().getCurrentSchedulingPoint();
      Map<Integer, ThreadInfo.State> expected = predecessors.get(cg);
      // The selected waiter is timed out only after the kernel snapshot.
      if (expected != null && expected.containsKey(ti.getId()) && ti.isTimedOut()) {
        selectedWakeups.put(cg, ti.getId());
      }
    }

    @Override
    public void stateBacktracked(Search search) {
      VM vm = search.getVM();
      Map<Integer, ThreadInfo.State> expected = predecessors.get(vm.getChoiceGenerator());
      if (expected != null) {
        for (Map.Entry<Integer, ThreadInfo.State> entry : expected.entrySet()) {
          ThreadInfo ti = vm.getThreadList().getThreadInfoForId(entry.getKey());
          assertNotNull("backtracking must restore waiter with id " + entry.getKey(), ti);
          assertEquals("backtracking must restore " + ti.getName() + " at "
              + vm.getChoiceGenerator().getId() + ": expected " + entry.getValue()
              + ", got " + ti.getState(), entry.getValue(), ti.getState());
        }
        checkedRestores++;
        Integer wokenId = selectedWakeups.remove(vm.getChoiceGenerator());
        if (wokenId != null) {
          checkedWakeupRestores++;
          if (wokenId == vm.getChoiceGenerator().getThreadInfo().getId()) {
            checkedSelfWakeupRestores++;
          }
        }
      }
    }
  }

  private volatile int waiting;
  private final Object monitor = new Object();

  private void runTwoWaiters() {
    for (int i = 0; i < 2; i++) {
      Thread waiter = new Thread(() -> {
        synchronized (monitor) {
          waiting++;
          try {
            monitor.wait(); // No notifier, interrupt, or timeout exists.
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
        }
      }, "waiter-" + i);
      // Main can exit without notifying; no deadlock or join can mask the test.
      waiter.setDaemon(true);
      waiter.start();
    }
    while (waiting != 2) {
      Thread.yield();
    }
    Thread.yield();
  }

  @Test
  public void testDefaultPolicyRestoresWaiters() {
    if (!isJPFRun()) {
      RestoreListener.checkedRestores = 0;
    }
    if (verifyNoPropertyViolation(
        "+vm.scheduler.sync.class=gov.nasa.jpf.vm.AllRunnablesSyncPolicy",
        "+cg.break_single_choice=true",
        "+listener=gov.nasa.jpf.test.vm.threads.SpuriousWakeupBacktrackTest$RestoreListener")) {
      runTwoWaiters();
    }
    if (!isJPFRun()) {
      assertTrue("must backtrack to a state with two waiters", RestoreListener.checkedRestores > 0);
    }
  }

  /**
   * Backtracking must restore each waiter to its predecessor state.
   */
  @Test
  public void testSpuriousWakeupRestoresWaiters() {
    if (!isJPFRun()) {
      RestoreListener.checkedRestores = 0;
      RestoreListener.checkedWakeupRestores = 0;
      RestoreListener.checkedSelfWakeupRestores = 0;
    }
    if (verifyNoPropertyViolation(
        "+vm.scheduler.sync.class=gov.nasa.jpf.vm.SpuriousWakeupSyncPolicy",
        "+cg.break_single_choice=true",
        "+listener=gov.nasa.jpf.test.vm.threads.SpuriousWakeupBacktrackTest$RestoreListener")) {
      runTwoWaiters();
    }
    if (!isJPFRun()) {
      assertTrue("must backtrack to a state with two waiters", RestoreListener.checkedRestores > 0);
      assertTrue("must restore both waiters after selecting a spurious wakeup",
          RestoreListener.checkedWakeupRestores > 0);
      assertTrue("must restore both waiters after the waiting thread selects itself",
          RestoreListener.checkedSelfWakeupRestores > 0);
    }
  }
}
