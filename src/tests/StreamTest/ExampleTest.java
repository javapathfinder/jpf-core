import org.junit.Test;

public class ExampleTest {
    @Test
    public static void Testing() {
    Example ex = new Example();
    int x = ex.doSomthing();
    assertEquals(x, 0);
    }
}
