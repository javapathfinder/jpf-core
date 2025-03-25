package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.regex.*;

public class MatcherTest extends TestJPF {

    @Test
    public void testToMatchResult() {
        if (verifyNoPropertyViolation()) {
            String regex = "(\\d+)-([a-zA-Z]+)";
            String input = "123-abc";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);

            assertTrue("Matcher should find a match", matcher.find());

            MatchResult matchResult = matcher.toMatchResult();

            // Validate match boundaries
            assertEquals("Start index should match", matcher.start(), matchResult.start());
            assertEquals("End index should match", matcher.end(), matchResult.end());

            // Validate group matches
            assertEquals("Group count should match", matcher.groupCount(), matchResult.groupCount());
            assertEquals("Full match should match", matcher.group(), matchResult.group());
            assertEquals("First capturing group should match", matcher.group(1), matchResult.group(1));
            assertEquals("Second capturing group should match", matcher.group(2), matchResult.group(2));
        }
    }
}
