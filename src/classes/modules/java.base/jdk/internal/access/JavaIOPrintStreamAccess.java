package jdk.internal.access;

import java.io.PrintStream;

public interface JavaIOPrintStreamAccess {
    Object lock(PrintStream ps);
}