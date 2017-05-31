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

package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.SystemState;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * generic choice tracker tool, to produce a list of choice values that
 * can be used to create readable replay scripts etc.
 */
public class ChoiceTracker extends ListenerAdapter implements PublisherExtension {

  enum Format { CG, CHOICE };

  Config config;
  VM vm;
  Search search;
  
  protected PrintWriter pw;
  Class<?>[] cgClasses;
  boolean isReportExtension;

  boolean showLocation;
  Format format = Format.CHOICE;
  String[] excludes;

  // <2do> hardwired type specific tracker for use with some shells - check if
  // we can get rid of it
  public ChoiceTracker (JPF jpf, String traceFileName, Class<?> cgClass){
    config = jpf.getConfig();
    vm = jpf.getVM();
    search = jpf.getSearch();
    
    cgClasses = new Class<?>[1];
    cgClasses[0] = cgClass;
    
    try {
      pw = new PrintWriter(traceFileName);
    } catch (FileNotFoundException fnfx) {
      System.err.println("cannot write choice trace to file: " + traceFileName);
      pw = new PrintWriter(System.out);
    }
  }

  public ChoiceTracker (Config config, JPF jpf) {
    this.config = config;
    vm = jpf.getVM();
    search = jpf.getSearch();
    
    String fname = config.getString("choice.trace");
    if (fname == null) {
      isReportExtension = true;
      jpf.addPublisherExtension(ConsolePublisher.class, this);
      // pw is going to be set later
    } else {
      try {
        pw = new PrintWriter(fname);
      } catch (FileNotFoundException fnfx) {
        System.err.println("cannot write choice trace to file: " + fname);
        pw = new PrintWriter(System.out);
      }
    }
    
    excludes = config.getStringArray("choice.exclude");
    cgClasses = config.getClasses("choice.class");

    format = config.getEnum("choice.format", Format.values(), Format.CG);
    showLocation = config.getBoolean("choice.show_location", true);
  }

  public void setExcludes (String... ex) {
    excludes=ex;
  }
  
  boolean isRelevantCG (ChoiceGenerator cg){
    if (cgClasses == null){
      return true;
    } else {
      for (Class<?> cls : cgClasses){
        if (cls.isAssignableFrom(cg.getClass())){
          return true;
        }
      }
      
      return false;
    }
  }

  @Override
  public void propertyViolated (Search search) {
        
    if (!isReportExtension) {

      pw.print("// application: ");
      pw.println( search.getVM().getSUTDescription());

      if (cgClasses == null) {
        pw.println("// trace over all CG classes");
      } else {
        pw.print("// trace over CG types: ");
        for (Class<?> cls : cgClasses){
          pw.print(cls.getName());
          pw.print(' ');
        }
        pw.println();
      }

      pw.println("//------------------------- choice trace");
      printChoices();
      
      pw.println("//------------------------- end choice trace");
      pw.flush();
    }
  }

  void printChoices () {
    int i = 0;
    SystemState ss = vm.getSystemState();
    ChoiceGenerator<?>[] cgStack = ss.getChoiceGenerators();

    nextChoice:
    for (ChoiceGenerator<?> cg : cgStack) {
      if (isRelevantCG(cg) && !cg.isDone()){

        Object choice = cg.getNextChoice();
        if (choice == null) {
          continue;
        } else {
          if (excludes != null) {
            for (String e : excludes) {
              if (choice.toString().startsWith(e)) {
                continue nextChoice;
              }
            }
          }
        }

        String line = null;

        switch (format){
          case CHOICE:
            line = choice.toString();
            if (line.startsWith("gov.nasa.jpf.vm.")){
              line = line.substring(17);
            }
            break;
          case CG:
            line = cg.toString();
            if (line.startsWith("gov.nasa.jpf.vm.choice.")){
              line = line.substring(24);
            }
            break;
        }

        if (line != null){
          pw.print(String.format("%4d: ", i++));

          pw.print(line);

          if (showLocation) {
            String loc = cg.getSourceLocation();
            if (loc != null) {
              pw.println();
              pw.print(" \tat ");
              pw.print(loc);
            }
          }
          pw.println();
        }
      }
    }
  }

  //--- the PublisherExtension interface

  @Override
  public void publishPropertyViolation (Publisher publisher) {
    pw = publisher.getOut();
    publisher.publishTopicStart("choice trace " + publisher.getLastErrorId());
    printChoices();
  }

}
