public class VMPropertyTest {
  public static void main(String[] args) {
    System.out.println(jdk.internal.misc.VM.getSavedProperty("os.name"));
  }
}