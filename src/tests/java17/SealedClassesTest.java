package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.lang.annotation.*;

public class SealedClassesTest extends TestJPF {

    @Test
    public void testBasicSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Notification email = new Email("Subject: Meeting");
            Notification sms = new SMS("555-1234");
            assertTrue(email instanceof Email);
            assertTrue(sms instanceof SMS);
            assertEquals("Subject: Meeting", email.getContent());
            assertEquals("555-1234", sms.getContent());
        }
    }

    @Test
    public void testBasicSealedClass() {
        if (verifyNoPropertyViolation()) {
            Payment card = new CreditCard(150.0);
            Payment paypal = new PayPal("user@example.com");
            assertEquals(150.0, card.amount(), 0.001);
            assertEquals("user@example.com", paypal.account());
        }
    }

    @Test
    public void testSealedClassWithGenerics() {
        if (verifyNoPropertyViolation()) {
            Response<Integer> ok = new Success<>(200);
            Response<String> fail = new Failure<>("Error");
            assertEquals(Integer.valueOf(200), ok.payload());
            assertEquals("Error", fail.payload());
            assertTrue(ok instanceof Success);
            assertTrue(fail instanceof Failure);
        }
    }

    @Test
    public void testGenericSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Wrapper<Double> wrap = new DoubleWrapper(3.14);
            assertEquals(3.14, wrap.value(), 0.001);
        }
    }

    @Test
    public void testAnnotatedSealedClass() {
        if (verifyNoPropertyViolation()) {
            LabeledProcess process = new LabeledTask();
            assertTrue(process.getClass().isAnnotationPresent(ProcessLabel.class));
            ProcessLabel label = process.getClass().getAnnotation(ProcessLabel.class);
            assertEquals("Task", label.value());
        }
    }

    @Test
    public void testSealedClassWithRecord() {
        if (verifyNoPropertyViolation()) {
            Transport bike = new Bicycle(2);
            Transport bus = new Bus(40);
            assertEquals("Bicycle", bike.type());
            assertEquals("Bus", bus.type());
        }
    }

    @Test
    public void testRecordImplementingSealedInterface() {
        if (verifyNoPropertyViolation()) {
            Command start = new Start("Init");
            assertEquals("Init", start.name());
        }
    }

    @Test
    public void testRecordDeconstructionWithSealedClasses() {
        if (verifyNoPropertyViolation()) {
            Stop stop = new Stop("Shutdown", 10);
            var name = stop.name();
            var code = stop.code();
            assertEquals("Shutdown", name);
            assertEquals(10, code);
        }
    }

    @Test
    public void testSealedFunctionalInterfaceWithLambda() {
        if (verifyNoPropertyViolation()) {
            Calculator sum = new Sum();
            Calculator prod = new Product();
            assertEquals(9, sum.compute(4, 5));
            assertEquals(20, prod.compute(4, 5));
            assertTrue(sum instanceof Calculator);
            assertTrue(prod instanceof Calculator);
        }
    }

    @Test
    public void testPatternMatchingWithSealedClasses() {
        if (verifyNoPropertyViolation()) {
            Command cmd = new Stop("End", 0);
            if (cmd instanceof Start s) {
                fail("Should be Stop, not Start");
            } else if (cmd instanceof Stop st) {
                assertEquals("End", st.name());
                assertEquals(0, st.code());
            } else {
                fail("Unexpected Command type");
            }
        }
    }

    @Test
    public void testTypeSpecificBehavior() {
        if (verifyNoPropertyViolation()) {
            Notification[] notes = {new Email("Info"), new SMS("999-8888")};
            StringBuilder sb = new StringBuilder();
            for (Notification note : notes) {
                if (note instanceof Email e) {
                    sb.append("Email: ").append(e.getContent());
                } else if (note instanceof SMS s) {
                    sb.append("SMS: ").append(s.getContent());
                }
            }
            assertEquals("Email: InfoSMS: 999-8888", sb.toString());
        }
    }

    @Test
    public void testMultiLevelSealedHierarchy() {
        if (verifyNoPropertyViolation()) {
            LevelOne l1 = new LevelThreeImpl();
            assertTrue(l1 instanceof LevelThreeImpl);
        }
    }

    @Test
    public void testNestedSealedClasses() {
        if (verifyNoPropertyViolation()) {
            ContainerClass container = new ContainerClass();
            ContainerClass.NestedAction action = container.new NestedActionImpl();
            assertTrue(action instanceof ContainerClass.NestedAction);
            assertTrue(action instanceof ContainerClass.NestedActionImpl);
        }
    }

    @Test
    public void testNullHandlingWithSealedTypes() {
        if (verifyNoPropertyViolation()) {
            NullableNotification note = null;
            if (note instanceof NullableEmail e) {
                fail("Null should not match NullableEmail");
            } else {
                assertTrue(true); // Null case handled correctly
            }
        }
    }

    @Test
    public void testAbstractPermittedSubclass() {
        if (verifyNoPropertyViolation()) {
            AbstractProcess proc = new ConcreteProcess();
            assertTrue(proc instanceof ConcreteProcess);
        }
    }

    @Test
    public void testSealedClassWithPrivateConstructor() {
        if (verifyNoPropertyViolation()) {
            PrivateSealed ps = new PrivateSealedImpl();
            assertTrue(ps instanceof PrivateSealedImpl);
        }
    }

    @Test
    public void testSealedInterfaceWithDefaultMethods() {
        if (verifyNoPropertyViolation()) {
            SealedWithDefaultMethod impl = new DefaultMethodImpl();
            assertEquals("DefaultValue", impl.defaultValue());
            assertEquals("Special", impl.specialValue());
        }
    }

    // --- Sealed Types Definitions ---

    // Basic Sealed Interface
    sealed interface Notification permits Email, SMS {
        String getContent();
    }
    final class Email implements Notification {
        private final String subject;
        Email(String subject) { this.subject = subject; }
        @Override public String getContent() { return subject; }
    }
    final class SMS implements Notification {
        private final String number;
        SMS(String number) { this.number = number; }
        @Override public String getContent() { return number; }
    }

    // Basic Sealed Class
    sealed abstract class Payment permits CreditCard, PayPal {
        public abstract double amount();
        public abstract String account();
    }
    final class CreditCard extends Payment {
        private final double amt;
        CreditCard(double amt) { this.amt = amt; }
        @Override public double amount() { return amt; }
        @Override public String account() { return null; }
    }
    final class PayPal extends Payment {
        private final String email;
        PayPal(String email) { this.email = email; }
        @Override public double amount() { return 0; }
        @Override public String account() { return email; }
    }

    // Generics
    sealed interface Response<T> permits Success, Failure {
        T payload();
    }
    final class Success<T> implements Response<T> {
        private final T value;
        Success(T value) { this.value = value; }
        @Override public T payload() { return value; }
    }
    final class Failure<T> implements Response<T> {
        private final T error;
        Failure(T error) { this.error = error; }
        @Override public T payload() { return error; }
    }
    sealed interface Wrapper<T> permits DoubleWrapper {
        T value();
    }
    final class DoubleWrapper implements Wrapper<Double> {
        private final Double val;
        DoubleWrapper(Double val) { this.val = val; }
        @Override public Double value() { return val; }
    }

    // Annotations
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface ProcessLabel {
        String value();
    }
    @ProcessLabel("Labeled")
    sealed class LabeledProcess permits LabeledTask {}
    @ProcessLabel("Task")
    final class LabeledTask extends LabeledProcess {}

    // Records
    public sealed interface Transport permits Bicycle, Bus {
        String type();
    }
    public record Bicycle(int wheels) implements Transport {
        @Override
        public String type() { return "Bicycle"; }
    }
    public static final class Bus implements Transport {
        private final int seats;
        public Bus(int seats) { this.seats = seats; }
        @Override
        public String type() { return "Bus"; }
    }
    sealed interface Command permits Start, Stop {
        String name();
    }
    record Start(String name) implements Command {}
    record Stop(String name, int code) implements Command {}

    // Lambdas and Functional Interfaces
    sealed interface Calculator permits Sum, Product {
        int compute(int a, int b);
    }
    final class Sum implements Calculator {
        @Override public int compute(int a, int b) { return a + b; }
    }
    final class Product implements Calculator {
        @Override public int compute(int a, int b) { return a * b; }
    }

    // Inheritance and Hierarchies
    sealed interface LevelOne permits LevelTwo {}
    sealed interface LevelTwo extends LevelOne permits LevelThreeImpl {}
    final class LevelThreeImpl implements LevelTwo {}

    // Nested Classes
    class ContainerClass {
        sealed interface NestedAction permits NestedActionImpl {}
        final class NestedActionImpl implements NestedAction {}
    }

    // Null Handling
    sealed interface NullableNotification permits NullableEmail {}
    final class NullableEmail implements NullableNotification {
        private final String msg;
        NullableEmail(String msg) { this.msg = msg; }
    }

    abstract sealed class AbstractProcess permits ConcreteProcess {}
    final class ConcreteProcess extends AbstractProcess {}
    sealed class PrivateSealed permits PrivateSealedImpl {
        private PrivateSealed() {}
    }
    final class PrivateSealedImpl extends PrivateSealed {
        public PrivateSealedImpl() { super(); }
    }

    sealed interface SealedWithDefaultMethod permits DefaultMethodImpl {
        default String defaultValue() {
            return "DefaultValue";
        }
        String specialValue();
    }
    final class DefaultMethodImpl implements SealedWithDefaultMethod {
        @Override
        public String specialValue() {
            return "Special";
        }
    }
}
