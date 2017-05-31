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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;

/**
 * a JenkinsStateSet that stores program state information in a readable
 * and diffable format.
 * 
 * Storing states as readable text is enabled by setting vm.storage.class to this class
 * 
 * Note: this automatically sets/overrides the serializer to Debug<serializer-class>
 */
public class DebugJenkinsStateSet extends JenkinsStateSet {

  static final String LOGFILE = "state";
  
  File outputDir;
  File outputFile;
  
  public DebugJenkinsStateSet (Config conf){
    String serializerCls = conf.getString("vm.serializer.class");
    if (serializerCls != null){
      int idx = serializerCls.lastIndexOf('.') + 1;
      String cname = serializerCls.substring(idx);
      
      if (!cname.startsWith("Debug")){
        if (idx > 0){
          serializerCls = serializerCls.substring(0,idx) + "Debug" + cname;
        } else {
          serializerCls = "Debug" + cname;
        }
      }
      
      serializer = conf.getInstance(null, serializerCls, DebugStateSerializer.class);
      if (serializer == null){
        throw new JPFConfigException("Debug StateSet cannot instantiate serializer: " + serializerCls);
      }
    }
    
    String path = conf.getString("vm.serializer.output", "tmp");
    outputDir = new File(path);
    if (!outputDir.isDirectory()){
      if (!outputDir.mkdirs()){
        throw new JPFConfigException("Debug StateSet cannot create output dir: " + outputDir.getAbsolutePath());        
      }
    }
    
    outputFile = new File( outputDir, LOGFILE);
  }
  
  @Override
  public void attach(VM vm){
    // we use our own serializer
    vm.setSerializer( serializer);
    
    // <2do> this is a bit hack'ish - why does the VM keep the serializer anyways,
    // if it is only used here
    super.attach(vm);
  }
  
  @Override
  public int addCurrent () {
    int maxId = lastStateId;
    FileOutputStream fos = null;
    
    try {
      fos = new FileOutputStream( outputFile);
    } catch (FileNotFoundException fnfx){
      throw new JPFException("cannot create Debug state set output file: " + outputFile.getAbsolutePath());
    }
    
    ((DebugStateSerializer)serializer).setOutputStream(fos);
    
    int stateId = super.addCurrent();
    
    try {
      fos.flush();
      fos.close();
    } catch (IOException iox){
      throw new JPFException("cannot write Debug state set output file: " + outputFile.getAbsolutePath());      
    }
    
    // if this is a new state, store it under its id, otherwise throw it away
    if (stateId > maxId){
      String fname = "state." + stateId;
      outputFile.renameTo( new File(outputDir, fname));
    } else {
      if (outputFile.isFile()){
        outputFile.delete();
      }
    }
    
    return stateId;
  }
}
