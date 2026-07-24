package gov.nasa.jpf.vm;

/**
 * Class representing a Record component in a Java 17+ record class.
 *
 * Extends InfoObject and implements GenericSignatureHolder so that classfile
 * parse callbacks (setSignature / setAnnotation / setAnnotationsDone) populate
 * this object directly, the same way they do for FieldInfo and MethodInfo.
 * Without this, generic signatures and annotations are silently dropped.
 */
public class RecordComponentInfo extends InfoObject implements GenericSignatureHolder {
    private final String name;
    private final String descriptor;
    private String signature;

    public RecordComponentInfo(String name, String descriptor, String signature,
                               AnnotationInfo[] annotations, TypeAnnotationInfo[] typeAnnotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        if (annotations != null) {
            this.annotations = annotations;
        }
        if (typeAnnotations != null) {
            this.typeAnnotations = typeAnnotations;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public String getGenericSignature() {
        return signature;
    }

    @Override
    public void setGenericSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}