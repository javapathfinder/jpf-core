package gov.nasa.jpf;

public class D {

    // Make sure this method is non-static
    public void m() {
        System.out.println("m() method called");
        throw new IncompatibleClassChangeError("This should throw an IncompatibleClassChangeError.");
    }
    
}
