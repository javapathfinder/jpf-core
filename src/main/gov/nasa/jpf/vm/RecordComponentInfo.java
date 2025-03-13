package gov.nasa.jpf.vm;

/**
 * Class representing a Record component in a Java 17+ record class
 */

public class RecordComponentInfo {
    private final String name;
    private final String descriptor;
    private final String signature;
    private final AnnotationInfo[] annotations;
    private final TypeAnnotationInfo[] typeAnnotations;

    public RecordComponentInfo(String name, String descriptor, String signature,
                           AnnotationInfo[] annotations, TypeAnnotationInfo[] typeAnnotations) {
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.annotations = annotations;
        this.typeAnnotations = typeAnnotations;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public AnnotationInfo[] getAnnotations() {
        return annotations;
    }

    public TypeAnnotationInfo[] getTypeAnnotations() {
        return typeAnnotations;
    }
}