package com.eprogrammerz.examples.java8.testing;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Yogen Rai
 */
public class StreamTester {
    Function<List<String>, List<String>> allToUpperCase = words -> words.stream().map(String::toUpperCase).collect(Collectors.toList());

    public List<String> convertAllToUpperCase(List<String> words) {
        return words.stream().map(String::toUpperCase).collect(Collectors.toList());
    }

    @Test
    public void testAllToUpperCase() {
        List<String> expected = Arrays.asList("JAVA8", "STREAMS");
        List<String> result = allToUpperCase.apply(Arrays.asList("java8", "streams"));
        assertEquals(expected, result);
    }

    @Test
    public void testConvertAllToUpperCase() {
        List<String> expected = Arrays.asList("JAVA8", "STREAMS");
        List<String> result = convertAllToUpperCase(Arrays.asList("java8", "streams"));
        assertEquals(expected, result);
    }
}