package gov.nasa.jpf.vm;

/**
 * Class representing a Record component in a Java 17+ record class
 */

public class RecordComponentInfo {
    private final String name;
    private final String componentType;
    private final String descriptor;
    private final String signature;
    private final AnnotationInfo[] annotations;
    private final TypeAnnotationInfo[] typeAnnotations;

    public RecordComponentInfo(String name, String componentType, String descriptor, String signature,
                           AnnotationInfo[] annotations, TypeAnnotationInfo[] typeAnnotations) {
        this.name = name;
        this.componentType = componentType;
        this.descriptor = descriptor;
        this.signature = signature;
        this.annotations = annotations !=null ? annotations : new AnnotationInfo[0];
        this.typeAnnotations = typeAnnotations !=null ? typeAnnotations : new TypeAnnotationInfo[0];
    }

    public String getName() {
        return name;
    }

    public String getComponentType() {
        return componentType;
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
    @Override
    public String toString() {
        return "RecordComponentInfo[name=" +name+ ", type=" +componentType+ "]";
    }
    @Override
    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(!(obj instanceof RecordComponentInfo)) return false;

        RecordComponentInfo other=(RecordComponentInfo) obj;

        return name.equals(other.name) && componentType.equals(other.componentType);
    }
    @Override
    public int hashCode() {
        return 31*name.hashCode()+componentType.hashCode();
    }
}