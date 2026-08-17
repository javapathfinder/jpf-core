package gov.nasa.jpf.vm.multiProcess;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestMultiProcessJPF;

import org.junit.Test;

/**
 * tests for the vm.end_state.first_process property, which terminates the
 * search as soon as the first process reaches an end state, instead of
 * waiting for all processes to terminate
 */
public class EndStateTest extends TestMultiProcessJPF {

  public static class EndStateCounter extends ListenerAdapter {
    static int endStateCount;

    public EndStateCounter() {
      endStateCount = 0;
    }

    @Override
    public void stateAdvanced(Search search) {
      if (search.isEndState()) {
        endStateCount++;
      }
    }
  }

  // runs a non-terminating loop, so that this process stays alive forever
  private void runNonTerminating() {
    while (true) { // keep this process alive forever
    }
  }

  // with vm.end_state.first_process the search terminates once the first
  // process (process 1) is done, even though process 0 is still alive
  @Test
  public void testEndStateOnFirstProcessTermination() {
    if (mpVerifyNoPropertyViolation(2, "+vm.end_state.first_process=true",
                                       "+listener=gov.nasa.jpf.vm.multiProcess.EndStateTest$EndStateCounter",
                                       "+search.depth_limit=100")) {
      if (getProcessId() == 0) {
        runNonTerminating();
      } else {
        // just terminate
      }
    }
    if (!isJPFRun()) {
      assertTrue("no end state reached although the first process terminated",
                 EndStateCounter.endStateCount > 0);
    }
  }

  // without the property, no end state is reached as long as any process
  // still has a runnable thread (default behavior must be preserved)
  @Test
  public void testNoEndStateWhileAnyProcessIsAlive() {
    if (mpVerifyNoPropertyViolation(2, "+listener=gov.nasa.jpf.vm.multiProcess.EndStateTest$EndStateCounter",
                                       "+search.depth_limit=100")) {
      runNonTerminating();
    }
    if (!isJPFRun()) {
      assertTrue("unexpected end state while a process is still alive",
                 EndStateCounter.endStateCount == 0);
    }
  }
}