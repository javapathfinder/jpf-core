/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.base.java.lang.reflect;

/*
 * JPF Standard Library Mock for Java 17 Records
 */
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * MJI model class for java.lang.reflect.RecordComponent library abstraction.
 * This class is necessary for JPF to support Java 14+ Record types.
 */
public final class RecordComponent implements AnnotatedElement {

    // The declaring record class
    private Class<?> clazz;

    // The name of the component
    private String name;

    // The type of the component
    private Class<?> type;

    // The accessor method for this component
    private Method accessor;

    // The generic signature (internal JVM use)
    private String signature;

    // Annotations (internal JVM use)
    private byte[] annotations;
    private byte[] typeAnnotations;

    /**
     * Private constructor - instances are created by the JVM (Native Peers)
     */
    private RecordComponent() {}

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getGenericSignature() {
        return signature;
    }

    public Method getAccessor() {
        return accessor;
    }

    public Class<?> getDeclaringRecord() {
        return clazz;
    }

    @Override
    public String toString() {
        return (type != null ? type.getTypeName() : "null") + " " + name;
    }

    // --- AnnotatedElement interface stubs (To be implemented fully later)
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }
}