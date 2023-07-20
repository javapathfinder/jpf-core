package gov.nasa.jpf.vm;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class AllocationTest extends TestJPF {
    @Test
    public void testEqualsHashCollisionSameContext() {
        AllocationContext ctx = new HashedAllocationContext(-987565178);
        // Two Allocations with the same context, different counts, but equal hash.
        Allocation alloc1 = new Allocation(ctx, 40933);
        Allocation alloc2 = new Allocation(ctx, 64242);
        assertEquals("the two Allocations should have equal hash", alloc1.hash, alloc2.hash);

        assertFalse(alloc1.equals(alloc2));
    }
}
