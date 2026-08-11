package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Regression tests for GitHub issue #628:
 * Record equals/toString/hashCode with reference and array components.
 */
public class RecordReferenceComponentTest extends TestJPF {

  record ArrayRecord(int[] data) {}
  record StringRecord(String name, int age) {}
  record BoxedRecord(Integer val) {}
  record NullableRecord(String s, Integer i) {}
  record NestedRecord(StringRecord inner, int x) {}

  static class IdentityClass {
    int value;
    IdentityClass(int v) { this.value = v; }
  }
  record IdentityRecord(IdentityClass obj) {}

  @Test
  public void testArraySameReference() {
    if (verifyNoPropertyViolation()) {
      int[] arr = {1, 2, 3};
      ArrayRecord r1 = new ArrayRecord(arr);
      ArrayRecord r2 = new ArrayRecord(arr);
      assertTrue(r1.equals(r2));
    }
  }

  @Test
  public void testArrayDifferentReference() {
    if (verifyNoPropertyViolation()) {
      ArrayRecord r1 = new ArrayRecord(new int[]{1, 2, 3});
      ArrayRecord r2 = new ArrayRecord(new int[]{1, 2, 3});
      assertFalse(r1.equals(r2));
    }
  }

  @Test
  public void testArrayToStringFormat() {
    if (verifyNoPropertyViolation()) {
      ArrayRecord r = new ArrayRecord(new int[]{1, 2, 3});
      String s = r.toString();
      assertTrue("should contain [I@", s.contains("[I@"));
    }
  }

  @Test
  public void testStringToString() {
    if (verifyNoPropertyViolation()) {
      StringRecord p = new StringRecord("alice", 30);
      assertEquals("StringRecord[name=alice, age=30]", p.toString());
    }
  }

  @Test
  public void testStringEquals() {
    if (verifyNoPropertyViolation()) {
      StringRecord p1 = new StringRecord("alice", 30);
      StringRecord p2 = new StringRecord("alice", 30);
      assertTrue(p1.equals(p2));
    }
  }

  @Test
  public void testBoxedIntegerEquals() {
    if (verifyNoPropertyViolation()) {
      BoxedRecord r1 = new BoxedRecord(Integer.valueOf(1000));
      BoxedRecord r2 = new BoxedRecord(Integer.valueOf(1000));
      assertTrue(r1.equals(r2));
    }
  }

  @Test
  public void testBoxedIntegerToString() {
    if (verifyNoPropertyViolation()) {
      BoxedRecord r = new BoxedRecord(42);
      assertEquals("BoxedRecord[val=42]", r.toString());
    }
  }

  @Test
  public void testIdentityClassEquality() {
    if (verifyNoPropertyViolation()) {
      IdentityClass a = new IdentityClass(99);
      IdentityClass b = new IdentityClass(99);
      IdentityRecord r1 = new IdentityRecord(a);
      IdentityRecord r2 = new IdentityRecord(b);
      assertFalse(r1.equals(r2));
      assertTrue(new IdentityRecord(a).equals(new IdentityRecord(a)));
    }
  }

  @Test
  public void testNullComponents() {
    if (verifyNoPropertyViolation()) {
      NullableRecord r1 = new NullableRecord(null, null);
      NullableRecord r2 = new NullableRecord(null, null);
      assertTrue(r1.equals(r2));
    }
  }

  @Test
  public void testNullToString() {
    if (verifyNoPropertyViolation()) {
      NullableRecord r = new NullableRecord(null, null);
      assertEquals("NullableRecord[s=null, i=null]", r.toString());
    }
  }

  @Test
  public void testNestedRecordEquals() {
    if (verifyNoPropertyViolation()) {
      NestedRecord r1 = new NestedRecord(new StringRecord("alice", 30), 1);
      NestedRecord r2 = new NestedRecord(new StringRecord("alice", 30), 1);
      assertTrue(r1.equals(r2));
    }
  }

  @Test
  public void testNestedRecordToString() {
    if (verifyNoPropertyViolation()) {
      NestedRecord r = new NestedRecord(new StringRecord("alice", 30), 1);
      assertEquals("NestedRecord[inner=StringRecord[name=alice, age=30], x=1]", r.toString());
    }
  }

  @Test
  public void testHashCodeConsistencyString() {
    if (verifyNoPropertyViolation()) {
      StringRecord r1 = new StringRecord("alice", 30);
      StringRecord r2 = new StringRecord("alice", 30);
      assertEquals(r1.hashCode(), r2.hashCode());
    }
  }

  @Test
  public void testHashCodeConsistencyBoxed() {
    if (verifyNoPropertyViolation()) {
      BoxedRecord r1 = new BoxedRecord(Integer.valueOf(1000));
      BoxedRecord r2 = new BoxedRecord(Integer.valueOf(1000));
      assertEquals(r1.hashCode(), r2.hashCode());
    }
  }

  @Test
  public void testHashCodeConsistencyArray() {
    if (verifyNoPropertyViolation()) {
      int[] arr = {1, 2, 3};
      ArrayRecord r1 = new ArrayRecord(arr);
      ArrayRecord r2 = new ArrayRecord(arr);
      assertEquals(r1.hashCode(), r2.hashCode());
    }
  }
}
