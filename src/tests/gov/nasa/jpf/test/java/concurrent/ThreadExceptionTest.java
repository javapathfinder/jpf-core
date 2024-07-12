package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class ThreadExceptionTest extends TestJPF {
    @Test
    public void testResourceAcquisition() {
        if (verifyUnhandledException("java.lang.RuntimeException",
                "+search.class=gov.nasa.jpf.search.RandomSearch",
                "+cg.randomize_choices=FIXED_SEED", "+search.RandomSearch.path_limit=10", "+search.multiple_errors=true")) {
            Thread t = new Thread(() -> {
                throw new RuntimeException("Exception from thread");
            });
            t.start();
        }
    }
}
