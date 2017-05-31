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

import gov.nasa.jpf.SystemAttribute;
import gov.nasa.jpf.util.IntTable;

/**
 * auxiliary class that captures the main entry and classloader context
 * of applications
 */
public class ApplicationContext implements SystemAttribute {

  final int id;
  final String mainClassName;
  final String mainEntry;
  final String[] args;
  final String host;
  
  final SystemClassLoaderInfo sysCl;
  MethodInfo miEntry;
  
  FinalizerThreadInfo finalizerThread;
  IntTable<String> internStrings;
  
  ApplicationContext (int id, String mainClassName, String mainEntry, String[] args, String host, SystemClassLoaderInfo sysCl){
    this.id = id;
    this.mainClassName = mainClassName;
    this.mainEntry = mainEntry;
    this.args = args;
    this.host = host;
    this.sysCl = sysCl;
    this.internStrings = new IntTable<String>(8);
  }
  
  void setEntryMethod (MethodInfo miEntry){
    this.miEntry = miEntry;
  }
  
  MethodInfo getEntryMethod(){
    return miEntry;
  }
  
  public int getId(){
    return id;
  }
  
  public String getMainClassName(){
    return mainClassName;
  }
  
  public String getHost() {
    return host;
  }
  
  public String[] getArgs(){
    return args;
  }
  
  public SystemClassLoaderInfo getSystemClassLoader(){
    return sysCl;
  }
  
  public FinalizerThreadInfo getFinalizerThread() {
    return finalizerThread;
  }
  
  public void setFinalizerThread(ThreadInfo ti) {
    finalizerThread = (FinalizerThreadInfo)ti;
  }
  
  public IntTable<String> getInternStrings() {
    return internStrings;
  }
  
  @Override
  public String toString(){
    StringBuffer sb = new StringBuffer();
    sb.append("ApplicationContext {mainClassName=");
    sb.append(mainClassName);
    sb.append(",mainEntry=");
    sb.append(mainEntry);
    sb.append(",host=");
    sb.append(host);
    
    sb.append(",args=[");
    for (int i=0; i<args.length; i++){
      if (i>0) sb.append(',');
      sb.append(args[i]);
    }
    sb.append("], miMain=");
    if (miEntry != null){
      sb.append(miEntry.getFullName());
    } else {
      sb.append("null");
    }
    sb.append('}');
    
    return sb.toString();
  }
}
