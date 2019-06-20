import gov.nasa.jpf.vm.Verify;

public class VerifyChoiceTest {
	
	static void main(String[] args) {
		int m = Verify.getInt(1, 3);
		System.out.println("m = " + m);
		int n = Verify.getInt(4, 5);
		System.out.println("n = " + n);
		int o = Verify.getInt(7,8);
		System.out.println("o = " + o);
		int p = Verify.getInt(15,16);
		System.out.println("p = " + p);
		//System.out.println("m * n =" + m * n);
		//assert ( (m * n) < 5); // fails (so we get a trace)
   }
	
	
}

