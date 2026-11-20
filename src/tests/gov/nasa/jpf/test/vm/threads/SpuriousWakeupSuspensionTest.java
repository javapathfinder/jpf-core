package gov.nasa.jpf.test.vm.threads;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * Asserts that suspension excludes a waiter from scheduling even when its
 * monitor is available for a spurious wakeup (In JPF, suspension is tracked
 * separately from the thread’s execution state, so a thread can be both WAITING
 * and suspended). Suspension prevents execution until resume; a spurious wakeup
 * must not bypass that restriction. An untimed waiter is suspended before its
 * monitor becomes available, then {@link SuspendedWaiterListener} checks its
 * exclusion from scheduling choices before selection in
 * {@link #testSpuriousWakeupExcludesSuspendedWaiter()}.
 * {@link #testDefaultPolicyExcludesSuspendedWaiter()} provides the
 * default-policy control; both tests require at least one check of this
 * scenario.
 */
public class SpuriousWakeupSuspensionTest extends TestJPF {
  private final Object monitor = new Object();

  //------------------- Introspection -------------------//

  /** Inspect suspension eligibility before advance() can modify thread state. */
  public static class SuspendedWaiterListener extends ListenerAdapter {
    static int checkedChoices;

    @Override
    public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> cg) {
      if (!(cg instanceof ThreadChoiceGenerator)
          || !((ThreadChoiceGenerator) cg).isSchedulingPoint()) {
        return;
      }
      for (ThreadInfo ti : vm.getThreadList().getThreads()) {
        if (ti.getName().equals("suspended-waiter") && ti.isSuspended()
            && ti.getState() == ThreadInfo.State.WAITING && ti.getLockCount() > 0
            && ti.getLockObject() != null && ti.getLockObject().canLock(ti)) {
          checkedChoices++;
          assertFalse("suspended WAITING thread must not be a scheduling choice at " + cg.getId(),
              ((ThreadChoiceGenerator) cg).contains(ti));
        }
      }
    }
  }

  //------------------- Workload -------------------//

  @SuppressWarnings("deprecation")
  private void runSuspendedWaiter() {
    Thread waiter = new Thread(() -> {
      synchronized (monitor) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          throw new AssertionError(e);
        }
      }
    }, "suspended-waiter");
    waiter.setDaemon(true);
    waiter.start();

    synchronized (monitor) {
      // Keep schedules where the worker has entered wait(). Owning its monitor
      // prevents a spurious wakeup during the suspension itself. Backtrack/Prune
      // JPF's search if the waiter isn't waiting. This is an optimization.
      // https://github.com/javapathfinder/jpf-core/wiki/Verify-API-of-JPF
      Verify.ignoreIf(waiter.getState() != Thread.State.WAITING);
      waiter.suspend();
    }
    // The monitor is now available, but the waiter is still suspended.
    Thread.yield();
  }

  //------------------- Tests -------------------//

  @Test
  public void testDefaultPolicyExcludesSuspendedWaiter() {
    if (!isJPFRun()) {
      SuspendedWaiterListener.checkedChoices = 0;
    }
    if (verifyNoPropertyViolation(
        "+vm.scheduler.sync.class=gov.nasa.jpf.vm.AllRunnablesSyncPolicy",
        "+cg.break_single_choice=true",
        "+listener=gov.nasa.jpf.test.vm.threads.SpuriousWakeupSuspensionTest$SuspendedWaiterListener")) {
      runSuspendedWaiter();
    }
    if (!isJPFRun()) {
      assertTrue("must inspect choices with a suspended waiter and an available monitor",
          SuspendedWaiterListener.checkedChoices > 0);
    }
  }

  /** Spurious wakeups must respect suspension. */
  @Test
  public void testSpuriousWakeupExcludesSuspendedWaiter() {
    if (!isJPFRun()) {
      SuspendedWaiterListener.checkedChoices = 0;
    }
    if (verifyNoPropertyViolation(
        "+vm.scheduler.sync.class=gov.nasa.jpf.vm.SpuriousWakeupSyncPolicy",
        "+cg.break_single_choice=true",
        "+listener=gov.nasa.jpf.test.vm.threads.SpuriousWakeupSuspensionTest$SuspendedWaiterListener")) {
      runSuspendedWaiter();
    }
    if (!isJPFRun()) {
      assertTrue("must inspect choices with a suspended waiter and an available monitor",
          SuspendedWaiterListener.checkedChoices > 0);
    }
  }
}
