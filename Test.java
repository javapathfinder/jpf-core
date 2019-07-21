

public class Test {
  public final static void foo(String s) {
    s = "Hello,"+s;
    System.out.println(s);
  }
  public final static void main(String[] args) {
    foo("Hello");
  }
}
