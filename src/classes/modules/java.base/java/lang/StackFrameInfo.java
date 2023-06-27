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
package java.lang;

import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodType;

/**
 * MJI model class for java.lang.StackFrameInfo
 */
public class StackFrameInfo implements StackFrame {

  private Class<?> declaringClass;
  private String methodName;
  private MethodType methodType;
  private String descriptor;
  private int bci;
  private String fileName;
  private int lineNumber;
  private boolean isNative;

  StackFrameInfo(StackWalker walker) {

  }

  Class<?> declaringClass() {
    return declaringClass;
  }

  @Override
  public String getClassName() {
    return declaringClass.getName();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public MethodType getMethodType() {
    return methodType;
  }

  @Override
  public String getDescriptor() {
    return descriptor;
  }

  @Override
  public int getByteCodeIndex() {
    return bci;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public boolean isNativeMethod() {
    return isNative;
  }

  @Override
  public String toString() {
    return declaringClass.getName() + "." + methodName + "(" +
        (isNative ? "Native Method)" :
          (fileName != null && lineNumber >= 0 ?
            fileName + ":" + lineNumber + ")" :
            (fileName != null ?  ""+fileName+")" : "Unknown Source)")));
  }

  @Override
  public StackTraceElement toStackTraceElement() {
    return new StackTraceElement(declaringClass.getName(),
                                 methodName,
                                 fileName,
                                 lineNumber);
  }
}
