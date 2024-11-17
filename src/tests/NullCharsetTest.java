import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.nio.file.Path;

public class NullCharsetTest extends TestJPF
{ 
    @Test
    public void testDirectPathEntry()
    { 
        if (verifyNoPropertyViolation())
        { 
            try { 
                Path.of("/tmp");
            } catch (IllegalArgumentException iae)
            { 
                if ("Null charset name".equals(e.getMessage()))
                { 
                    fail("IllegalArgumentException with 'Null charset name' encountered");
                }
            }
        }
    }
}