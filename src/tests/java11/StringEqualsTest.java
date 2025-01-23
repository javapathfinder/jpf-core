package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
public class StringEqualsTest extends TestJPF {
    @Test
    public void testStringEqual_differentObj(){
        if(verifyNoPropertyViolation()){
            String word="a";
            boolean actual=word.equals(new Object());
            boolean expected =false;
            assertEquals(expected,actual);
        }
    }
}
