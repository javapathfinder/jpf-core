import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.test.TestJPF;

/**
 * This is a plain JUnit test to check whether required resource files exist on
 * JPF classpath.
 *
 * @author Jeanderson Candido
 *
 */
public class ReporterResourcesTest extends TestJPF {

  private JPF jpf;

  @Before
  public void setup() {
    String[] configArgs = { "+vm.class=.vm.MultiProcessVM", "+target.1=HelloWorld", "+target.2=HelloWorld" };
    this.jpf = new JPF(new Config(configArgs));
  }

  @Test
  public void checkResources() throws IOException {
    assertNotNull("build.properties should exist on classpath", jpf.getClass().getResourceAsStream("build.properties"));
    assertNotNull(".version should exist on classpath", jpf.getClass().getResourceAsStream(".version"));
  }
}
