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

package gov.nasa.jpf.vm;

/**
 * abstract class that represents the source of a classfile, such
 * as (root) directories and jars
 */
public abstract class ClassFileContainer {
  protected String name;
  protected String url;

  protected ClassFileContainer(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public String getURL() {
    return url;
  }

  public abstract String getClassURL (String clsName);

  protected static void error(String msg) throws ClassParseException {
    throw new ClassParseException(msg);
  }

  public abstract ClassFileMatch getMatch (String clsName) throws ClassParseException;
}
