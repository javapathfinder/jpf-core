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

package gov.nasa.jpf.report;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.RepositoryEntry;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Transition;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class XMLPublisher extends Publisher {

  public XMLPublisher(Config conf, Reporter reporter) {
    super(conf, reporter);
  }

  @Override
  public String getName() {
    return "xml";
  }
  
  @Override
  protected void openChannel(){
    if (out == null) {
      String fname = getReportFileName("report.xml.file") + ".xml";
      try {
        out = new PrintWriter(fname);
      } catch (FileNotFoundException fnfx) {
        // log here
      }
    }
  }

  @Override
  protected void closeChannel() {
    if (out != null){
      out.close();
      out = null;
    }
  }
  
  
  @Override
  protected void publishProlog() {
    out.println("<?xml version=\"1.0\" ?>");
    out.println("<jpfreport>");
  }
  
  @Override
  public void publishTopicStart(String topic) {
    out.println("  <" + topic + ">");
  }
  
  @Override
  public void publishTopicEnd(String topic) {
    out.println("  </" + topic + ">");
  }  
  
  @Override
  protected void publishEpilog() {
    out.println("</jpfreport>");
  }
  
  @Override
  protected void publishJPF() {
    out.println("  <jpf-version>" + JPF.VERSION + "</jpf-version>");
  }

  @Override
  protected void publishJPFConfig() {
    TreeMap<Object,Object> map = conf.asOrderedMap();
    Set<Map.Entry<Object,Object>> eSet = map.entrySet();

    out.println("  <jpf-properties>");

    for (Object src : conf.getSources()){
      out.println("    <source value=\"" + conf.getSourceName(src) + "\"/>");
    }    
    
    for (Map.Entry<Object,Object> e : eSet) {
      out.println("    <entry key=\"" + e.getKey() + "\" value=\"" + e.getValue() + "\"/>");
    }      
    out.println("  </jpf-properties>");
    
  }

  @Override
  protected void publishPlatform() {
    out.println("  <platform>");
    out.println("    <hostname>" + reporter.getHostName() + "</hostname>");
    out.println("    <arch>" + reporter.getArch() + "</arch>");
    out.println("    <os>" + reporter.getOS() + "</os>");
    out.println("    <java>" + reporter.getJava() + "</java>");    
    out.println("  </platform>");
  }

  @Override
  protected void publishUser() {
    out.println("  <user>" + reporter.getUser() + "</user>");
  }

  @Override
  protected void publishDTG() {
    out.println("  <started>" + reporter.getStartDate() + "</started>");
  }
  
  @Override
  protected void publishSuT() {
    out.println("  <sut>");
    String mainCls = reporter.getSuT();
    if (mainCls != null) {
      String mainPath = reporter.getSuT();
      if (mainPath != null) {
        out.println("    <source>" + mainPath + "</source>");

        RepositoryEntry rep = RepositoryEntry.getRepositoryEntry(mainPath);
        if (rep != null) {
          out.println("    <repository>" + rep.getRepository() + "</repository>");
          out.println("    <revision>" + rep.getRevision() + "</revision>");
        }
      } else {
        out.println("    <binary>" + mainCls + ".class" + "</binary>");
      }
    } else {
      // no app specified
    }
    out.println("  </sut>");
  }

  @Override
  protected void publishResult() {
    List<Error> errors = reporter.getErrors();
    
    out.print("  <result findings=\"");
    if (errors.isEmpty()){
      out.println("none\"/>");
    } else {
      out.println("errors\">");
      int i=0;
      for (Error e : errors) {
        out.print("    <error id=\"");
        out.print(i++);
        out.println("\">");
        out.print("      <property>");
        out.print(e.getProperty().getClass().getName());
        out.println("</property>");
        out.print("      <details>");
        out.print(e.getDetails());
        out.println("      </details>");
        out.println("    </error>");
      }
      out.println("  </result>");
    }
  }

  // not sure how much effort we want to put into readability here
  @Override
  protected void publishTrace() {
    Path path = reporter.getPath();
    int i=0;

    if (path.size() == 0) {
      return; // nothing to publish
    }
    
    out.println("  <trace>");
    for (Transition t : path) {
      ChoiceGenerator<?> cg = t.getChoiceGenerator();
      out.println("    <transition id=\"" + i++ + "\" thread=\"" + t.getThreadIndex() + "\">");
      out.println("      <cg class=\""+cg.getClass().getName() + "\" choice=\"" +
                  cg.getProcessedNumberOfChoices() + "\"/>");
      for (Step s : t) {
        out.print("      <insn src=\"" + s.getLocationString() + "\">");
        String insn = s.getInstruction().toString();
        if (insn.indexOf('<') >= 0) { // <init> and <clinit> clash with XML
          insn = insn.replaceAll("<", "&lt;");
          insn = insn.replaceAll(">", "&gt;");
        }
        out.print(insn);
        out.println("</insn>");
      }
      out.println("    </transition>");      
    }
    out.println("  </trace>");
  }

  @Override
  protected void publishOutput() {
    Path path = reporter.getPath();

    if (path.size() == 0) {
      return; // nothing to publish
    }
        
    if (path.hasOutput()) {
      out.println("  <output>");
      for (Transition t : path) {
        String s = t.getOutput();
        if (s != null){
          out.print(s);
        }
      }
      out.println("  </output>");
    }
  }
  
  @Override
  protected void publishSnapshot() {
    VM vm = reporter.getVM();
    
    out.println("  <live-threads>");
    for (ThreadInfo ti : vm.getLiveThreads()) {
      out.println("    <thread id=\"" + ti.getId() + "\" name=\"" + ti.getName()
                  + "\" status=\"" + ti.getStateName() + "\">");
      // owned locks
      for (ElementInfo e : ti.getLockedObjects()) {
        out.println("      <lock-owned object=\"" + e + "\"/>");
      }
      // requested locks
      ElementInfo ei = ti.getLockObject();
      if (ei != null) {
        out.println("      <lock-request object=\"" + ei + "\"/>");
      }
      // stack frames
      for (StackFrame frame : ti){
        if (!frame.isDirectCallFrame()){
          out.println("      <frame>" + frame.getStackTraceInfo() + "</frame>");
        }
      }
      out.println("    </thread>");
    }
    out.println("  </live-threads>");
  }

  @Override
  protected void publishStatistics() {
    Statistics stat = reporter.getStatistics();
    out.println("  <statistics>");
    out.println("    <elapsed-time>" + formatHMS(reporter.getElapsedTime()) + "</elapsed-time>");
    out.println("    <new-states>" + stat.newStates + "</new-states>");
    out.println("    <visited-states>" + stat.visitedStates + "</visited-states>");
    out.println("    <backtracked-states>" + stat.backtracked + "</backtracked-states>");
    out.println("    <end-states>" + stat.endStates + "</end-states>");
    out.println("    <max-memory unit=\"MB\">" + (stat.maxUsed >>20) + "</max-memory>");
    out.println("  </statistics>");
  }

}
