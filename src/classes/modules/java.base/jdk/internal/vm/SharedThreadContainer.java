package jdk.internal.vm;

import java.util.stream.Stream;

/**
 * Stub matching JDK 21 structure to avoid compilation errors.
 */
public class SharedThreadContainer extends ThreadContainer {

    public SharedThreadContainer(boolean shared) {
        super(shared);
    }

    public static SharedThreadContainer create(ThreadContainer parent) {
        return new SharedThreadContainer(true);
    }

    public static SharedThreadContainer create(String name) {
        return new SharedThreadContainer(true);
    }

    public void close() {
        // No-op
    }

    @Override
    public Stream<Thread> threads() {
        // Return empty stream for JPF simulation
        return Stream.empty();
    }

    public void start(Thread t) {
        t.start();
    }
}