package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.annotation.BitFlip;

/*
 * Use @BitFlip annotation to inject a bit flip to an argument
 */
public class AnnotationSimpleExample {
    public static void foo(@BitFlip(2) byte bar, int zz) {
        System.out.println("" + bar + " " + zz);
    }
    public static void main(String[] args) {
        foo((byte)0, 1);
    }
}
