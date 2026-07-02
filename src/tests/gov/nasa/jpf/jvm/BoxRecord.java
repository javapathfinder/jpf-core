package gov.nasa.jpf.jvm;

import java.util.List;

/**
 * Test record with generic components used to verify that
 * record component generic signatures are preserved during
 * class-file parsing.
 */
record BoxRecord<T>(T value, List<String> items) {}
