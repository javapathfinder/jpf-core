/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package sun.misc;

import sun.nio.ch.Interruptible;
import sun.reflect.ConstantPool;
import sun.reflect.annotation.AnnotationType;

/**
 * this is a placeholder for a Java 6 class, which we only have here to
 * support both Java 1.5 and 6 with the same set of env/ classes
 *
 * see sun.misc.SharedSecrets for details
 *
 * <2do> THIS IS GOING AWAY AS SOON AS WE OFFICIALLY SWITCH TO JAVA 6
 */

public interface JavaLangAccess {

    ConstantPool getConstantPool(Class<?> klass);

    void setAnnotationType(Class<?> klass, AnnotationType annotationType);

    AnnotationType getAnnotationType(Class<?> klass);

    <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> klass);

    void blockedOn(Thread t, Interruptible b);

    void registerShutdownHook(int slot, Runnable r);
    
    int getStackTraceDepth(Throwable t);
    
    StackTraceElement getStackTraceElement(Throwable t, int i);
}
