package gov.nasa.jpf.listener;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

class Emptymain{
    public static void main(String[] args) {

    }
}
public class VerTrackerTest extends TestJPF {
    @Test
      public void test_Emptymain(){
        if(verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.VarTracker")){
          Emptymain.main(null);
        }
    }
    public static void main(String[] args) {
        runTestsOfThisClass(null);
    }

}
