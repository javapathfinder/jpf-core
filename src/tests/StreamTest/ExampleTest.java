import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExampleTest {
    @Test
    public static void Testing() {
    Example ex = new Example();
    int x = ex.doSomthing();
    assertEquals(x, 0);
    }
}
