package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.annotation.BitFlip;

/*
 * Use @BitFlip annotation to inject a bit flip to an argument
 */
public class AnnotationSimpleExample {
    public static byte foo(@BitFlip(1) byte bar, int zz) {
        return bar;
    }
    public byte bar(@BitFlip byte foo) {
        return foo;
    }
    public static void main(String[] args) {
        System.out.println("" + foo((byte)0, 1) + " " + new AnnotationSimpleExample().bar((byte)0));
    }
}
