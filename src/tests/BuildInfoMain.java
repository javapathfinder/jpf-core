import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.report.Reporter;

public class BuildInfoMain {

  public static void main(String[] args) {
    Config config = new Config(new String[] { "+vm.class=.vm.MultiProcessVM", "+target.1=HelloWorld",
        "+target.2=HelloWorld", "+report.show_repository=true" });
    JPF jpf = new JPF(config);
    Reporter reporter = jpf.getReporter();
    System.out.println(reporter.getJPFBanner());
  }
}
