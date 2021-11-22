import java.io.IOException;
import java.util.stream.*;

public class Example {
    public static void main(String[] args) throws IOException {
        //Integer stream
        IntStream
        .range(0, 10)
        .forEach(System.out::print);
    }
}
