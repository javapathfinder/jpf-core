import java.util.stream.IntStream;

class Stream {
    public static void main(String[] args) {

    }
    public static int testing() {
        IntStream stream1 = IntStream.of(2);
        IntStream stream2 = IntStream.of(1);
        int str;

        // concatenating both the Streams
        // with IntStream.concat() function
        str = IntStream.concat(stream1, stream2)
                .sum();
        return str;
    }
}
