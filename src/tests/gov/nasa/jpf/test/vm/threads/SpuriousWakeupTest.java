package gov.nasa.jpf.test.vm.threads;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * Asserts that the policy explores a spurious return from an untimed monitor
 * wait. A waiter has no notifier, interruption, or timeout, so any return must
 * be spurious. Another thread stays runnable to allow scheduling alternatives,
 * and the counter in {@link #testUntimedWaitCanWakeSpuriously()} proves that at
 * least one return occurs across explored paths.
 */
public class SpuriousWakeupTest extends TestJPF {

  @Test
  public void testUntimedWaitCanWakeSpuriously() {
    if (!isJPFRun()) {
      Verify.resetCounter(0);
    }
    if (verifyNoPropertyViolation("+vm.scheduler.sync.class=gov.nasa.jpf.vm.SpuriousWakeupSyncPolicy")) {
      Object monitor = new Object();
      Thread waiter = new Thread(() -> {
        synchronized (monitor) {
          try {
            monitor.wait(); // No notifier, interrupt, or timeout exists.
            Verify.incrementCounter(0);
          } catch (InterruptedException e) {
            throw new AssertionError(e);
          }
        }
      });
      // Keep main runnable while the waiter can be selected to wake spuriously.
      waiter.setDaemon(true);
      waiter.start();
      Thread.yield();
    }
    if (!isJPFRun()) {
      assertTrue("the spurious wakeup must be explored", Verify.getCounter(0) > 0);
    }
  }
}
