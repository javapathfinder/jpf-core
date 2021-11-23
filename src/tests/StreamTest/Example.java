import java.io.IOException;
import java.util.stream.*;

public class Example {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 10; i++) {
            doSomthing(i);
        }
        //Integer stream
        IntStream
        .range(0, 10)
        .forEach(Example::doSomthing);
    }
    private static void doSomthing(int i) {
    }
}