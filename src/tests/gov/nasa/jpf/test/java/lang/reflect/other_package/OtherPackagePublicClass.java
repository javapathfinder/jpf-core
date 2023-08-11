package gov.nasa.jpf.test.java.lang.reflect.other_package;

/**
 * Used in gov.nasa.jpf.test.java.lang.reflect.FieldTest.
 */
public class OtherPackagePublicClass {
    public static final int publicStaticFinalField = 10;
    public final int publicFinalField = 20;
    public int publicField;

    protected int protectedField = 30;
    int packagePrivateField = 40;

    public OtherPackagePublicClass(int v) {
        this.publicField = v;
    }

    public static OtherPackageInternalClass getPackagePrivateObject() {
        return new OtherPackageInternalClass();
    }
}

class OtherPackageInternalClass {
    public static int publicStaticField = 88;
}
