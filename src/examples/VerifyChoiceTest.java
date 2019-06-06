import gov.nasa.jpf.vm.Verify;

public class VerifyChoiceTest {
	
	static void main(String[] args) {
		int m = Verify.getInt(1, 11);
		System.out.println("m = " + m);
		int n = Verify.getInt(2, 3);
		System.out.println("n = " + n);
		System.out.println("m * n =" + m * n);
		//assert ( (m * n) < 5); // fails (so we get a trace)
   }
	
	
}

