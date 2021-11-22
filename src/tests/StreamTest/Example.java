import java.io.IOException;
import java.util.stream.*;

public class Example {
    public static void main(String[] args) throws IOException {
        //Integer stream will print the numbers from 0 to 9
        IntStream
        .range(0, 10)
        .forEach(System.out::print);
    }
}