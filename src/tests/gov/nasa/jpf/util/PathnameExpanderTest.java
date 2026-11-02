
package gov.nasa.jpf.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathnameExpanderTest extends PathnameExpander {

  @Test
  public void testPlusIsTreatedLiterally() {
    assertTrue(getPattern("A+B*.java")
        .matcher("A+BTest.java").matches());

    assertFalse(getPattern("A+B*.java")
        .matcher("AAABTest.java").matches());
  }

  @Test
  public void testWildcardStillMatches() {
    assertTrue(getPattern("Foo*.java")
        .matcher("Foobar.java").matches());

    assertTrue(getPattern("Foo*.java")
        .matcher("Foo.java").matches());
  }

  @Test
  public void testRegexMetacharactersAreLiteral() {
    String filename = "File(1)[test]+$.java";

    assertTrue(getPattern(filename)
        .matcher(filename).matches());

    assertFalse(getPattern(filename)
        .matcher("File1testtest$.java").matches());
  }
}
