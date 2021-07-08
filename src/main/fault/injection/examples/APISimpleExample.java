package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;

/*
 * Use getBitFlip API to inject a bit flip to a variable
 */
public class APISimpleExample {
    public static void main(String[] args) {
        System.out.println(Verify.getBitFlip((byte)0, 2));
    }
}
