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
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE;
import gov.nasa.jpf.jvm.bytecode.JVMFieldInstruction;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.JVMInstanceFieldInstruction;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.LockInstruction;
import gov.nasa.jpf.jvm.bytecode.PUTFIELD;
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.jvm.bytecode.JVMStaticFieldInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ExceptionInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.NotDeadlockedProperty;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * an alternative  Graphviz dot-file generator for simple,educational state graphs
 * except of creating funny wallpapers, it doesn't help much in real life if the
 * state count gets > 50, but for the small ones it's actually quite readable.
 * Good for papers.
 *
 * normal states are labeled with their numeric ids, end states are double circled.
 * start, end and error states are color filled
 *
 * edges have two labels: the choice value at the beginning and the CG cause
 * at the end. Only the first incoming edge into a state shows the CG cause
 *
 * we only render one backtrack edge per from-state
 *
 * <2do> GraphViz doesn't seem to handle color or fontname for head/tail labels correctly
 */
public class SimpleDot extends ListenerAdapter {

  static final String GRAPH_ATTRS = "pad=0.5";
  static final String GENERIC_NODE_ATTRS = "shape=circle,style=filled,fillcolor=white";
  static final String GENERIC_EDGE_ATTRS = "fontsize=10,fontname=Helvetica,fontcolor=blue,color=cadetblue,style=\"setlinewidth(0.5)\",arrowhead=empty,arrowsize=0.5";
  static final String START_NODE_ATTRS = "fillcolor=green";
  static final String END_NODE_ATTRS = "shape=doublecircle,fillcolor=cyan";
  static final String ERROR_NODE_ATTRS = "color=red,fillcolor=lightcoral";
  static final String BACKTRACK_EDGE_ATTRS = "arrowhead=onormal,color=gray52,style=\"dotted\"";
  static final String RESTORED_EDGE_ATTRS = "arrowhead=onormal,color=red,style=\"dotted\"";
  static final String NEW_EDGE_ATTRS = "arrowhead=normal";
  static final String VISITED_EDGE_ATTRS = "arrowhead=vee";


  //--- configurable Graphviz attributes
  protected String graphAttrs;
  protected String genericNodeAttrs;
  protected String genericEdgeAttrs;
  protected String startNodeAttrs;
  protected String endNodeAttrs;
  protected String errorNodeAttrs;
  protected String newEdgeAttrs;
  protected String visitedEdgeAttrs;
  protected String backtrackEdgeAttrs;
  protected String restoreEdgeAttrs;

  protected boolean showTarget;
  protected boolean printFile;

  protected VM vm;
  protected String app;
  protected File file;
  protected PrintWriter pw;

  protected int lastId = -1;    // where we come from
  protected String lastErrorId;
  protected ElementInfo lastEi;
  protected ThreadInfo lastTi;  // the last started thread

  // helper because GraphViz cannot eliminate duplicate edges
  HashSet<Long> seenEdges;

  public SimpleDot( Config config, JPF jpf){

    graphAttrs = config.getString("dot.graph_attr", GRAPH_ATTRS);
    genericNodeAttrs = config.getString("dot.node_attr", GENERIC_NODE_ATTRS);
    genericEdgeAttrs = config.getString("dot.edge_attr", GENERIC_EDGE_ATTRS);
    newEdgeAttrs = config.getString("dot.new_edge_attr", NEW_EDGE_ATTRS);
    visitedEdgeAttrs = config.getString("dot.visited_edge_attr", VISITED_EDGE_ATTRS);
    startNodeAttrs = config.getString("dot.start_node_attr", START_NODE_ATTRS);
    endNodeAttrs = config.getString("dot.end_node_attr", END_NODE_ATTRS);
    errorNodeAttrs = config.getString("dot.error_node_attr", ERROR_NODE_ATTRS);
    backtrackEdgeAttrs = config.getString("dot.bt_edge_attr", BACKTRACK_EDGE_ATTRS);
    restoreEdgeAttrs = config.getString("dot.restore_edge_attr", RESTORED_EDGE_ATTRS);

    printFile = config.getBoolean("dot.print_file", false);
    showTarget = config.getBoolean("dot.show_target", false);

    // app and filename are not known until the search is started
    
    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }

  void initialize (VM vm){
    Config config = vm.getConfig();
    
    app = vm.getSUTName();
    app = app.replace("+", "__");
    app = app.replace('.', '_');

    String fname = config.getString("dot.file");
    if (fname == null){
      fname = app + ".dot";
    }

    try {
      file = new File(fname);
      FileWriter fw = new FileWriter(file);
      pw = new PrintWriter(fw);
    } catch (IOException iox){
      throw new JPFConfigException("unable to open SimpleDot output file: " + fname);
    }
    
    seenEdges = new HashSet<Long>();
  }
  
  //--- the listener interface

  @Override
  public void searchStarted(Search search){
    vm = search.getVM();
    
    initialize(vm);
    
    printHeader();
    printStartState("S");
  }

  @Override
  public void stateAdvanced(Search search){
    int id = search.getStateId();
    long edgeId = ((long)lastId << 32) | id;

    if (id <0 || seenEdges.contains(edgeId)){
      return; // skip the root state and property violations (reported separately)
    }


    if (search.isErrorState()) {
      String eid = "e" + search.getNumberOfErrors();
      printTransition(getStateId(lastId), eid, getLastChoice(), getError(search));
      printErrorState(eid);
      lastErrorId = eid;

    } else if (search.isNewState()) {

      if (search.isEndState()) {
        printTransition(getStateId(lastId), getStateId(id), getLastChoice(), "return");
        printEndState(getStateId(id));
      } else {
        printTransition(getStateId(lastId), getStateId(id), getLastChoice(), getNextCG());
      }

    } else { // already visited state
      printTransition(getStateId(lastId), getStateId(id), getLastChoice(), null);
    }

    seenEdges.add(edgeId);
    lastId = id;
  }

  @Override
  public void stateBacktracked(Search search){
    int id = search.getStateId();
    long edgeId = ((long)lastId << 32) | id;

    if (!seenEdges.contains(edgeId)) {
      if(lastErrorId!=null) {
        printBacktrack(lastErrorId, getStateId(id));
        lastErrorId = null;
      } else {
        printBacktrack(getStateId(lastId), getStateId(id));
      }
      seenEdges.add(edgeId);
    }
    lastId = id;
  }
  
  @Override
  public void stateRestored(Search search) {
    int id = search.getStateId();
    long edgeId = ((long)lastId << 32) | id;

    if (!seenEdges.contains(edgeId)) {
      printRestored(getStateId(lastId), getStateId(id));
      seenEdges.add(edgeId);
    }
    lastId = id;
  }

  @Override
  public void searchFinished (Search search){
    pw.println("}");
    pw.close();
  }

  @Override
  public void threadStarted (VM vm, ThreadInfo ti){
    lastTi = ti;
  }

  @Override
  public void objectWait (VM vm, ThreadInfo ti, ElementInfo ei){
    lastEi = ei;
    lastTi = ti;
  }

  @Override
  public void publishFinished (Publisher publisher) {
    PrintWriter ppw = publisher.getOut();
    publisher.publishTopicStart("SimpleDot");

    ppw.println("dot file generated: " + file.getPath());

    if (printFile){
      ppw.println();
      FileUtils.printFile(ppw,file);
    }
  }


  //--- data collection

  protected String getStateId (int id){
    return id < 0 ? "S" : Integer.toString(id);
  }

  protected String getLastChoice() {
    ChoiceGenerator<?> cg = vm.getChoiceGenerator();
    Object choice = cg.getNextChoice();

    if (choice instanceof ThreadInfo){
      int idx = ((ThreadInfo)choice).getId();
      return "T"+idx;
    } else {
      return choice.toString(); // we probably want more here
    }
  }

  // this is the only method that's more tricky - we have to find a balance
  // between being conscious enough to not clutter the graph, and expressive
  // enough to understand it.
  // <2do> this doesn't deal well with custom or data CGs yet
  protected String getNextCG(){
    ChoiceGenerator<?> cg = vm.getNextChoiceGenerator(); // that's the next one
    Instruction insn = cg.getInsn();

    if (insn instanceof EXECUTENATIVE) {
      return getNativeExecCG((EXECUTENATIVE)insn);

    } else if (insn instanceof JVMFieldInstruction) { // shared object field access
      return getFieldAccessCG((JVMFieldInstruction)insn);

    } else if (insn instanceof LockInstruction){ // monitor_enter
      return getLockCG((LockInstruction)insn);

    } else if (insn instanceof JVMInvokeInstruction){ // sync method invoke
      return getInvokeCG((JVMInvokeInstruction)insn);
    }

    return insn.getMnemonic(); // our generic fallback
  }

  protected String getNativeExecCG (EXECUTENATIVE insn){
    MethodInfo mi = insn.getExecutedMethod();
    String s = mi.getName();

    if (s.equals("start")) {
      s = "T" + lastTi.getId() + ".start";
    } else if (s.equals("wait")) {
      s = "T" + lastTi.getId() + ".wait";
    }

    return s;
  }

  protected String getFieldAccessCG (JVMFieldInstruction insn){
    String s;

    if (insn instanceof JVMInstanceFieldInstruction) {

      if (insn instanceof PUTFIELD) {
        s = "put";
      } else /* if (insn instanceof GETFIELD) */ {
        s = "get";
      }

      if (showTarget){
        int ref = ((JVMInstanceFieldInstruction) insn).getLastThis();
        s = getInstanceRef(ref) + '.' + s;
      }

    } else /* if (insn instanceof StaticFieldInstruction) */ {
      if (insn instanceof PUTSTATIC) {
        s = "put";
      } else /* if (insn instanceof GETSTATIC) */ {
        s = "get";
      }

      if (showTarget){
        String clsName = ((JVMStaticFieldInstruction) insn).getLastClassName();
        s = Misc.stripToLastDot(clsName) + '.' + s;
      }
    }

    String varId = insn.getFieldName();
    s = s + ' ' + varId;

    return s;
  }

  protected String getLockCG(LockInstruction insn){
    String s = "sync";

    if (showTarget){
      int ref = insn.getLastLockRef();
      s = getInstanceRef(ref) + '.' + s;
    }

    return s;
  }

  protected String getInvokeCG (JVMInvokeInstruction insn){
    MethodInfo mi = insn.getInvokedMethod();
    String s = mi.getName() + "()";

    if (showTarget){
      if (insn instanceof InstanceInvocation) {
        int ref = ((InstanceInvocation) insn).getLastObjRef();
        s = getInstanceRef(ref) + '.' + s;

      } else if (insn instanceof INVOKESTATIC) {
        String clsName = ((INVOKESTATIC) insn).getInvokedClassName();
        s = Misc.stripToLastDot(clsName) + '.' + s;
      }
    }

    return s;
  }

  protected String getError (Search search){
    String e;
    Error error = search.getLastError();
    Property prop = error.getProperty();

    if (prop instanceof NoUncaughtExceptionsProperty){
      ExceptionInfo xi = ((NoUncaughtExceptionsProperty)prop).getUncaughtExceptionInfo();
      return Misc.stripToLastDot(xi.getExceptionClassname());

    } else if (prop instanceof NotDeadlockedProperty){
      return "deadlock";
    }

    // fallback
    return Misc.stripToLastDot(prop.getClass().getName());
  }

  protected static String getInstanceRef (int ref){
    return "@" + Integer.toHexString(ref).toUpperCase();
  }

  protected static String getClassObjectRef (int ref){
    return "#" + Integer.toHexString(ref).toUpperCase();
  }

  //--- dot file stuff

  protected void printHeader(){
    pw.print("digraph ");
    pw.print(app);
    pw.println(" {");

    pw.print("node [");
    pw.print(genericNodeAttrs);
    pw.println(']');

    pw.print("edge [");
    pw.print(genericEdgeAttrs);
    pw.println(']');

    pw.println(graphAttrs);

    pw.println();
    pw.print("label=\"");
    pw.print(app);
    pw.print("\"");
    pw.println();
  }

  protected void printTransition(String fromState, String toState, String choiceVal, String cgCause){
    pw.println();
    pw.print(fromState);
    pw.print(" -> ");
    pw.print( toState);
    pw.print(" [label=\"");
    pw.print(choiceVal);
    pw.print('"');
    if (cgCause != null){
      pw.print(NEW_EDGE_ATTRS);
      pw.print(",headlabel=\"");
      pw.print(cgCause);
      pw.print('"');
    } else {
      pw.print(VISITED_EDGE_ATTRS);
    }
    pw.println(']');
  }

  protected void printBacktrack (String fromState, String toState){
    pw.println();
    pw.print(fromState);
    pw.print(" -> ");
    pw.print( toState);

    pw.print(" [");
    pw.print(backtrackEdgeAttrs);
    pw.print(']');

    pw.println("  // backtrack");
  }

  protected void printRestored (String fromState, String toState){
    pw.println();
    pw.print(fromState);
    pw.print(" -> ");
    pw.print( toState);

    pw.print(" [");
    pw.print(restoreEdgeAttrs);
    pw.print(']');

    pw.println("  // restored");
  }
  
  protected void printStartState(String stateId){
    pw.print(stateId);

    pw.print(" [");
    pw.print(startNodeAttrs);
    pw.print(']');

    pw.println("  // start state");
  }

  protected void printEndState(String stateId){
    pw.print(stateId);

    pw.print(" [");
    pw.print(endNodeAttrs);
    pw.print(']');

    pw.println("  // end state");
  }

  protected void printErrorState(String error){
    pw.print(error);

    pw.print(" [");
    pw.print(errorNodeAttrs);
    pw.print(']');

    pw.println("  // error state");
  }
}
