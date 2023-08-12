package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.annotation.BitFlip;

/*
 * Use @BitFlip annotation to inject a bit flip to an argument
 */
public class AnnotationSimpleExample {
    public static void foo(@BitFlip byte bar) {
        System.out.println(bar);
    }
    public static void main(String[] args) {
        foo((byte)0);
    }
}
