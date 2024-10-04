package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class RecordFeatureTest extends TestJPF {
    record Point(int x, int y){}
    // Official documents suggest that fields of "record" are "private" and "final".
    // So we are testing by direct access. It should fail here at compile time, but do not know why it works.
    @Test
    public void testRecordFieldsDirectly(){
        if (verifyNoPropertyViolation()){
            Point point = new Point(4, 5);
            assertEquals("",4,point.x);
            assertEquals("",5,point.y);
        }
    }
    @Test
    public void testRecordFields(){
        if (verifyNoPropertyViolation()){
            Point point = new Point(4, 5);
            assertEquals("",4,point.x());
            assertEquals("",5,point.y());
        }
    }
    @Test
    public void testRecordEquality(){
        if (verifyNoPropertyViolation()){
            Point point1 = new Point(4,5);
            Point point2 = new Point(4,5);
            Point point3 = new Point(3,5);
            assertEquals("",point1, point2);
            assertNotEquals("",point1, point3);
        }
    }
    @Test
    public void testRecordHashCode() {
        if (verifyNoPropertyViolation()){
            Point point1 = new Point(4, 5);
            Point point2 = new Point(4, 5);
            Point point3 = new Point(3,5);
            assertEquals("", point1.hashCode(), point2.hashCode());
            assertNotEquals("",point1.hashCode(),point3.hashCode());
        }
    }
    @Test
    public void testRecordToString() {
        if (verifyNoPropertyViolation()){
            Point point = new Point(4, 5);
            assertEquals("Point[x=4, y=5]", point.toString());
        }
    }
}
