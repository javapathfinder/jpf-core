package gov.nasa.jpf;

import org.junit.AfterClass;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaticCallToNonStaticTest extends TestJPF {

    @Test
    public void testStaticCallToNonStatic() {
        
        Assertions.assertThrows(java.lang.IncompatibleClassChangeError.class, () -> {
            D.m();  
        });
    }
}
