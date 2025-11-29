package jdk.internal.vm;

import java.util.stream.Stream;

public abstract class ThreadContainer {

    public ThreadContainer(boolean shared) {
        // No-op for JPF
    }

    public Thread owner() {
        // Return null to indicate no specific owner/tracking for JPF simulation
        return null;
    }

    public abstract Stream<Thread> threads();
}
