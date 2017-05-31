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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.JPFLogger;

import java.io.File;
import java.util.ArrayList;

/**
 * this is a lookup mechanism for class files that is based on an ordered
 * list of directory or jar entries
 */
public class ClassPath implements Restorable<ClassPath>{

  static class CPMemento implements Memento<ClassPath> {
    ClassPath cp;
    ArrayList<ClassFileContainer> pathElements;

    CPMemento (ClassPath cp){
      this.cp = cp;
      this.pathElements = new ArrayList<ClassFileContainer>(cp.pathElements);
    }

    @Override
    public ClassPath restore (ClassPath ignored) {
      cp.pathElements = this.pathElements;
      return cp;
    }
  }

  
  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.jvm.classfile");
  
  protected ArrayList<ClassFileContainer> pathElements;


  public ClassPath(){
    pathElements = new ArrayList<ClassFileContainer>();
  }
  
  @Override
  public Memento<ClassPath> getMemento (MementoFactory factory) {
    return factory.getMemento(this);
  }

  public Memento<ClassPath> getMemento(){
    return new CPMemento(this);
  }

  public void addClassFileContainer (ClassFileContainer pathElement){
    assert pathElement != null;
    pathElements.add(pathElement);
  }


  public String[] getPathNames(){
    String[] pn = new String[pathElements.size()];

    for (int i=0; i<pn.length; i++){
      pn[i] = pathElements.get(i).getName();
    }

    return pn;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    int len = pathElements.size();
    int i=0;

    for (ClassFileContainer e : pathElements){
      sb.append(e.getName());
      if (++i < len){
        sb.append(File.pathSeparator);
      }
    }
    return sb.toString();
  }

  protected static void error(String msg) throws ClassParseException {
    throw new ClassParseException(msg);
  }

  public ClassFileMatch findMatch (String clsName) throws ClassParseException {
    for (ClassFileContainer container : pathElements){
      ClassFileMatch match = container.getMatch(clsName);
      if (match != null){
        logger.fine("found ", clsName, " in ", container.getName());
        return match;
      }
    }

    return null;    
  }

}