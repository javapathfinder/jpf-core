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

import java.io.File;

/**
 * MJI model class for java.lang.StackTraceElement
 */
public class StackTraceElement {
  
  String clsName;
  String fileName;
  String mthName;
  int    line;
  
  public StackTraceElement() {
     // nothing to do
  }

  public StackTraceElement (String clsName, String mthName, String fileName, int line) {
    if (clsName == null) {
      throw new NullPointerException("Declaring class is null");
    } 

    if (mthName == null) {
      throw new NullPointerException("Method name is null");
    }

    this.clsName = clsName;
    this.mthName = mthName;
    this.fileName = fileName;
    this.line = line;
  }

  public String getClassName () {
    return clsName;
  }

  public String getFileName () {
    return fileName;
  }

  public int getLineNumber () {
    return line;
  }

  public String getMethodName () {
    return mthName;
  }

  public boolean isNativeMethod () {
    return false;
  }

  /**
  public int hashCode () {
    return 0;
  }
  **/

  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder();
    sb.append(clsName);
    sb.append('.');
    sb.append(mthName);
    sb.append("(");

    sb.append(new File(fileName).getName());
    
    if (line >= 0){
      sb.append(':');
      sb.append(line);
    }
    sb.append(')');
    return sb.toString();
  }
}
