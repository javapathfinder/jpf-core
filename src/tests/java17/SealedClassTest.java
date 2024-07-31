package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class SealedClassTest extends TestJPF {

    // Shape is a "sealed" class, and it allows only "Circle", "Square" and "Rectangle" to extend.
    sealed class Shape permits Square, Rectangle {

    }


    // Here "Square" class is allowed to extend "Shape". Sealed class "Shape" allows "Square" to extends in "permits" clause.
    // But "Square" is "final class" so it cannot be extended by anyone more.
    final class Square extends Shape {

    }


    // Here "Rectangle" class is allowed to extend "Shape". Sealed class "Shape" allows "Rectangle" to extends in "permits" clause.
    // But "Rectangle" is "non-sealed class" so it can be extended by anyone more.
    non-sealed class Rectangle extends Shape {

    }


    // Here "CustomRectangle" class is allowed to extend "non-sealed class Rectangle".
    class CustomRectangle extends Rectangle {

    }

    @Test
    public void testSquareInstance() {
        if (verifyNoPropertyViolation()){
            Shape shape = new Square();
            assertTrue("", shape instanceof Square);
        }
    }

    @Test
    public void testRectangleInstance() {
        if (verifyNoPropertyViolation()){
            Shape shape = new Rectangle();
            assertTrue("", shape instanceof Rectangle);
        }
    }

    @Test
    public void testCustomRectangleInstance() {
        if (verifyNoPropertyViolation()){
            Shape shape = new CustomRectangle();
            assertTrue("", shape instanceof CustomRectangle);
            assertTrue("", shape instanceof Rectangle);
        }
    }
}
