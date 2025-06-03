package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.lang.annotation.*;


public class SealedClassesTest extends TestJPF {

    // Section 1: Basic Sealed Classes and Interfaces
    @Test
    public void testBasicSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Shape circle = new Circle(5);
            Shape rectangle = new Rectangle(3, 4);
            assertTrue(circle instanceof Circle);
            assertTrue(rectangle instanceof Rectangle);
            assertEquals(5, circle.value());
            assertEquals(12, rectangle.value());
        }
    }

    @Test
    public void testBasicSealedClass() {
        if (verifyNoPropertyViolation()) {
            Animal dog = new Dog("Woof");
            Animal cat = new Cat("Meow");
            assertEquals("Woof", dog.sound());
            assertEquals("Meow", cat.sound());
        }
    }

    // Section 2: Generics
    @Test
    public void testSealedClassWithGenerics() {
        if (verifyNoPropertyViolation()) {
            Box<Integer> intBox = new IntBox(10);
            Box<String> stringBox = new StringBox("Hello");
            assertEquals(Integer.valueOf(10), intBox.getValue());
            assertEquals("Hello", stringBox.getValue());
            assertTrue(intBox instanceof IntBox);
            assertTrue(stringBox instanceof StringBox);
        }
    }

    @Test
    public void testGenericSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Container<String> container = new StringContainer("Test");
            assertEquals("Test", container.content());
        }
    }

    // Section 3: Annotations
    @Test
    public void testAnnotatedSealedClass() {
        if (verifyNoPropertyViolation()) {
            AnnotatedSealedClass obj = new AnnotatedSubclass();
            assertTrue(obj.getClass().isAnnotationPresent(CustomAnnotation.class));
            CustomAnnotation annotation = obj.getClass().getAnnotation(CustomAnnotation.class);
            assertEquals("Subclass", annotation.value());
        }
    }

    // Section 4: Records
    @Test
    public void testSealedClassWithRecord() {
        if (verifyNoPropertyViolation()) {
            Vehicle car = new Car(4);
            Vehicle truck = new Truck();
            assertEquals("Car", car.getType());
            assertEquals("Truck", truck.getType());
        }
    }

    @Test
    public void testRecordImplementingSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Expr expr = new Constant(5);
            assertEquals(5, expr.value());
        }
    }

    // ADDED: Test for record deconstruction with sealed classes
    @Test
    public void testRecordDeconstructionWithSealedClasses() {
        if (verifyNoPropertyViolation()) {
            Plus p = new Plus(new Constant(5), new Constant(7));
            var left = p.left();
            var right = p.right();
            assertEquals(5, left.value());
            assertEquals(7, right.value());
        }
    }

    // Section 5: Lambdas and Functional Interfaces
    @Test
    public void testSealedFunctionalInterfaceWithLambda() {
        if (verifyNoPropertyViolation()) {
            Operation add = new AddOperation();
            Operation multiply = new MultiplyOperation();
            assertEquals(5, add.apply(2, 3));
            assertEquals(6, multiply.apply(2, 3));
            assertTrue(add instanceof Operation);
            assertTrue(multiply instanceof Operation);
        }
    }

    // Section 6: Pattern Matching
    @Test
    public void testPatternMatchingWithSealedClasses() {
        if (verifyNoPropertyViolation()) {
            Expr expr = new Plus(new Constant(3), new Constant(4));
            if (expr instanceof Constant c) {
                fail("Should be Plus, not Constant");
            } else if (expr instanceof Plus p) {
                assertEquals(3, p.left().value());
                assertEquals(4, p.right().value());
            } else {
                fail("Unexpected Expr type");
            }
        }
    }



    // ADDED: Test for type-specific behavior
    @Test
    public void testTypeSpecificBehavior() {
        if (verifyNoPropertyViolation()) {
            Shape[] shapes = {new Circle(5), new Rectangle(4, 6)};
            StringBuilder result = new StringBuilder();

            for (Shape shape : shapes) {
                if (shape instanceof Circle c) {
                    result.append("Circle: ").append(c.value());
                } else if (shape instanceof Rectangle r) {
                    result.append("Rectangle: ").append(r.value());
                }
            }

            assertEquals("Circle: 5Rectangle: 24", result.toString());
        }
    }

    // Section 7: Inheritance and Hierarchies
    @Test
    public void testMultiLevelSealedHierarchy() {
        if (verifyNoPropertyViolation()) {
            TopLevel top = new MiddleLevelImpl();
            assertTrue(top instanceof MiddleLevelImpl);
        }
    }

    // Section 8: Nested and Inner Classes
    @Test
    public void testNestedSealedClasses() {
        if (verifyNoPropertyViolation()) {
            Outer outer = new Outer();
            Outer.NestedSealed shape = outer.new NestedImpl();
            assertTrue(shape instanceof Outer.NestedSealed);
            assertTrue(shape instanceof Outer.NestedImpl);
        }
    }


    // Section 10: Null Handling
    @Test
    public void testNullHandlingWithSealedTypes() {
        if (verifyNoPropertyViolation()) {
            NullableShape shape = null;
            if (shape instanceof NullableCircle c) {
                fail("Null should not match NullableCircle");
            } else {
                assertTrue(true); // Null case handled correctly
            }
        }
    }

    @Test
    public void testAbstractPermittedSubclass() {
        if (verifyNoPropertyViolation()) {
            AbstractSealed obj = new ConcreteSubclass();
            assertTrue(obj instanceof ConcreteSubclass);
        }
    }

    @Test
    public void testSealedClassWithPrivateConstructor() {
        if (verifyNoPropertyViolation()) {
            PrivateConstructorSealed obj = new PrivateConstructorSubclass();
            assertTrue(obj instanceof PrivateConstructorSubclass);
        }
    }

    @Test
    public void testSealedInterfaceWithDefaultMethods() {
        if (verifyNoPropertyViolation()) {
            SealedWithDefault impl = new DefaultImpl();
            assertEquals("Default", impl.defaultMethod());
            assertEquals("Custom", impl.customMethod());
        }
    }



    // Basic Sealed Interface
    sealed interface Shape permits Circle, Rectangle {
        int value();
    }
    final class Circle implements Shape {
        private final int radius;
        Circle(int radius) { this.radius = radius; }
        @Override public int value() { return radius; }
    }
    final class Rectangle implements Shape {
        private final int width, height;
        Rectangle(int width, int height) { this.width = width; this.height = height; }
        @Override public int value() { return width * height; }
    }

    // Basic Sealed Class
    sealed abstract class Animal permits Dog, Cat {
        public abstract String sound();
    }
    final class Dog extends Animal {
        private final String sound;
        Dog(String sound) { this.sound = sound; }
        @Override public String sound() { return sound; }
    }
    final class Cat extends Animal {
        private final String sound;
        Cat(String sound) { this.sound = sound; }
        @Override public String sound() { return sound; }
    }

    // Generics
    sealed interface Box<T> permits IntBox, StringBox {
        T getValue();
    }
    final class IntBox implements Box<Integer> {
        private final Integer value;
        IntBox(Integer value) { this.value = value; }
        @Override public Integer getValue() { return value; }
    }
    final class StringBox implements Box<String> {
        private final String value;
        StringBox(String value) { this.value = value; }
        @Override public String getValue() { return value; }
    }
    sealed interface Container<T> permits StringContainer {
        T content();
    }
    final class StringContainer implements Container<String> {
        private final String content;
        StringContainer(String content) { this.content = content; }
        @Override public String content() { return content; }
    }

    // Annotations
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface CustomAnnotation {
        String value();
    }
    @CustomAnnotation("SealedClass")
    sealed class AnnotatedSealedClass permits AnnotatedSubclass {}
    @CustomAnnotation("Subclass")
    final class AnnotatedSubclass extends AnnotatedSealedClass {}

    // Records
    public sealed interface Vehicle permits Car, Truck {
        String getType();
    }
    public record Car(int wheels) implements Vehicle {
        @Override
        public String getType() { return "Car"; }
    }
    public static final class Truck implements Vehicle {
        @Override
        public String getType() { return "Truck"; }
    }
    sealed interface Expr permits Constant, Plus {
        int value();
    }
    record Constant(int value) implements Expr {}
    record Plus(Expr left, Expr right) implements Expr {
        @Override public int value() { return left.value() + right.value(); }
    }

    // Lambdas and Functional Interfaces
    sealed interface Operation permits AddOperation, MultiplyOperation {
        int apply(int a, int b);
    }
    final class AddOperation implements Operation {
        @Override public int apply(int a, int b) { return a + b; }
    }
    final class MultiplyOperation implements Operation {
        @Override public int apply(int a, int b) { return a * b; }
    }

    // Inheritance and Hierarchies
    sealed interface TopLevel permits MiddleLevel {}
    sealed interface MiddleLevel extends TopLevel permits MiddleLevelImpl {}
    final class MiddleLevelImpl implements MiddleLevel {}

    // Nested Classes
    class Outer {
        sealed interface NestedSealed permits NestedImpl {}
        final class NestedImpl implements NestedSealed {}
    }


    // Null Handling
    sealed interface NullableShape permits NullableCircle {}
    final class NullableCircle implements NullableShape {
        private final int radius;
        NullableCircle(int radius) { this.radius = radius; }
    }

    abstract sealed class AbstractSealed permits ConcreteSubclass {}
    final class ConcreteSubclass extends AbstractSealed {}
    sealed class PrivateConstructorSealed permits PrivateConstructorSubclass {
        private PrivateConstructorSealed() {}
    }
    final class PrivateConstructorSubclass extends PrivateConstructorSealed {
        public PrivateConstructorSubclass() { super(); }
    }

    sealed interface SealedWithDefault permits DefaultImpl {
        default String defaultMethod() {
            return "Default";
        }

        String customMethod();
    }

    final class DefaultImpl implements SealedWithDefault {
        @Override
        public String customMethod() {
            return "Custom";
        }
    }

}