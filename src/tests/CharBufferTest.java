import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class CharBufferTest extends TestJPF {
    @Test
    public void Test_Wrap() throws CharacterCodingException {
        if(verifyNoPropertyViolation()){
            java.nio.charset.StandardCharsets.UTF_8.newEncoder().encode(java.nio.CharBuffer.wrap("heyo"));
        }
    }
}
