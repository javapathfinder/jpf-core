package gov.nasa.jpf.test.java.misc;

import gov.nasa.jpf.util.test.TestJPF;
import jdk.internal.misc.VM;
import org.junit.Test;

/**
 * Simple test to verify properties are loaded correctly to VM
 */
public class VMTest extends TestJPF {

  /**
   * Test to verify all the properties of System is loaded to VM
   */
  @Test
  public void getSavedPropertiesTest(){
    if(verifyNoPropertyViolation()){
      assertEquals(System.getProperties().size(),VM.getSavedProperties().size());
    }
  }

  /**
   * Test to verify property "os.name" is loaded in VM
   */
  @Test
  public void getSavedPropertyTest(){
    if(verifyNoPropertyViolation()){
      assertNotNull(VM.getSavedProperty("os.name"));
    }
  }
}
