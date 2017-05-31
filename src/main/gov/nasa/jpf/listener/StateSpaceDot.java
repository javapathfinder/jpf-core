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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.Transition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Add a state space observer to JPF and build a graph of the state space
 * that is explored by JPF. The graph can be generated in different formats.
 * The current formats that are supported are DOT (visualized by a tool
 * like GraphViz from ATT - http://www.graphviz.org/) and gdf (used by GUESS
 * from HP - http://www.hpl.hp.com/research/idl/projects/graphs/).
 * The graph is stored in a file called "jpf-state-space.<extension>" where
 * extension is ".dot" or ".gdf". By default it generates a DOT graph.
 *
 * Options:
 *   -gdf:                Generate the graph in GDF format. The default is DOT.
 *   -transition-numbers: Include transition numbers in transition labels.
 *   -show-source:        Include source lines in transition labels.
 *   -labelvisible:       Indicates if the label on the transitions is visible (used only with the -gdf option)
 *   -help:               Prints this help information and stops.
 *
 * @date 9/12/03
 * @author Owen O'Malley (Initial version generating the DOT graph)
 *
 * @date 7/17/05
 * @author Masoud Mansouri-Samani (Extended to also generate the gdf graph)
 */
public class StateSpaceDot extends ListenerAdapter {
  // NODE styles constants
  static final int RECTANGLE = 1;
  static final int ELLIPSE   = 2;
  static final int ROUND_RECTANGLE = 3;
  static final int RECTANGLE_WITH_TEXT = 4;
  static final int ELLIPSE_WITH_TEXT = 5;
  static final int ROUND_RECTANGLE_WITH_TEXT = 6;

  private static final String DOT_EXT = "dot";
  private static final String GDF_EXT = "gdf";
  private static final String OUT_FILENAME_NO_EXT = "jpf-state-space";

  // State and transition node styles used
  private static final int state_node_style = ELLIPSE_WITH_TEXT;
  private static final int transition_node_style = RECTANGLE_WITH_TEXT;

  // File formats supported
  private static final int DOT_FORMAT=0;
  private static final int GDF_FORMAT=1;

  private BufferedWriter graph = null;
  private int edge_id = 0;
  private static boolean transition_numbers=false;
  private static boolean show_source=false;
  private static int format=DOT_FORMAT;
  private String out_filename = OUT_FILENAME_NO_EXT+"."+DOT_EXT;
  private static boolean labelvisible=false;
  private static boolean helpRequested=false;


  /* In gdf format all the edges must come after all the nodes of the graph
   * are generated. So we first output the nodes as we come across them but
   * we store the strings for edges in the gdfEdges list and output them when
   * the search ends.
   */
  ArrayList<String> gdfEdges=new ArrayList<String>();

  private StateInformation prev_state = null;

  public StateSpaceDot(Config conf, JPF jpf) {

    VM vm = jpf.getVM();
    vm.recordSteps(true);
  }

  @Override
  public void searchStarted(Search search) {
    try {
      beginGraph();
    } catch (IOException e) {}
  }

  @Override
  public void searchFinished(Search search) {
    try {
      endGraph();
    } catch (IOException e) {}
  }

  @Override
  public void stateAdvanced(Search search) {
    int id = search.getStateId();
    boolean has_next =search.hasNextState();
    boolean is_new = search.isNewState();
    try {
      if (format==DOT_FORMAT) {
        graph.write("/* searchAdvanced(" + id + ", " + makeDotLabel(search, id) +
                    ", " + has_next + ") */");
        graph.newLine();
      }
      if (prev_state != null) {
        addEdge(prev_state.id, id, search);
      } else {
        prev_state = new StateInformation();
      }
      addNode(prev_state);
      prev_state.reset(id, has_next, is_new);
    } catch (IOException e) {}
  }

  @Override
  public void stateRestored (Search search) {
    prev_state.reset(search.getStateId(), false, false);
  }

  @Override
  public void stateProcessed (Search search) {
   // nothing to do
  }

  @Override
  public void stateBacktracked(Search search) {
    try {
      addNode(prev_state);
      prev_state.reset(search.getStateId(), false, false);
      if (format==DOT_FORMAT) {
        graph.write("/* searchBacktracked(" + prev_state + ") */");
        graph.newLine();
      }
    } catch (IOException e) {}
  }

  @Override
  public void searchConstraintHit(Search search) {
    try {
      if (format==DOT_FORMAT) {
        graph.write("/* searchConstraintHit(" + search.getStateId() + ") */");
        graph.newLine();
      }
    } catch (IOException e) {}
  }

  private String getErrorMsg(Search search) {
    List<Error> errs = search.getErrors();
    if (errs.isEmpty()) {
      return null;
    } else {
      return errs.get(0).getDescription();
    }
  }

  @Override
  public void propertyViolated(Search search) {
	try {
	  prev_state.error = getErrorMsg(search);
	  if (format==DOT_FORMAT) {
	    graph.write("/* propertyViolated(" + search.getStateId() + ") */");
	    graph.newLine();
	  }
	} catch (IOException e) {}
  }

  /**
   * Put the header for the graph into the file.
   */
  private void beginGraph() throws IOException {
	graph = new BufferedWriter(new FileWriter(out_filename));
	if (format==GDF_FORMAT) {
	  graph.write("nodedef>name,label,style,color");
	} else { // dot
	  graph.write("digraph jpf_state_space {");
	}
	graph.newLine();
  }

  /**
   * In the case of the DOT graph it is just adding the final "}" at the end.
   * In the case of GPF format we must output edge definition and all the
   * edges that we have found.
   */
  private void endGraph() throws IOException {
    if(prev_state != null)
      addNode(prev_state);
    if (format==GDF_FORMAT) {
      graph.write("edgedef>node1,node2,label,labelvisible,directed,thread INT");
      graph.newLine();

      // Output all the edges that we have accumulated so far
      int size=gdfEdges.size();
  	  for (int i=0; i<size; i++) {
  		graph.write(gdfEdges.get(i));
  	    graph.newLine();
  	  }
    } else {
      graph.write("}");
      graph.newLine();
    }
    graph.close();
  }

  /**
   * Return the string that will be used to label this state for the user.
   */
  private String makeDotLabel(Search state, int my_id) {
    Transition trans = state.getTransition();
    if (trans == null) {
      return "-init-";
    }
    Step last_trans_step = trans.getLastStep();
    if (last_trans_step == null) {
      return "?";
    }

    StringBuilder result = new StringBuilder();

    if (transition_numbers) {
      result.append(my_id);
      result.append("\\n");
    }

    int thread = trans.getThreadIndex();

    result.append("Thd");
    result.append(thread);
    result.append(':');
    result.append(last_trans_step.toString());

    if (show_source) {
      String source_line=last_trans_step.getLineString();
      if ((source_line != null) && !source_line.equals("")) {
        result.append("\\n");

        StringBuilder sb=new StringBuilder(source_line);

        // We need to precede the dot-specific special characters which appear
        // in the Java source line, such as ']' and '"', with the '\' escape
        // characters and also to remove any new lines.

        replaceString(sb, "\n", "");
        replaceString(sb, "]", "\\]");
        replaceString(sb, "\"", "\\\"");
        result.append(sb.toString());
      }
    }

    return result.toString();
  }

  /**
   * Return the string that will be used to label this state in the GDF graph.
   */
  private String makeGdfLabel(Search state, int my_id) {
    Transition trans = state.getTransition();
    if (trans == null) {
  	  return "-init-";
  	}

  	StringBuilder result = new StringBuilder();

  	if (transition_numbers) {
  	  result.append(my_id);
  	  result.append(':');
  	}

  	Step last_trans_step = trans.getLastStep();
  	result.append(last_trans_step.toString());

  	if (show_source) {
  	  String source_line=last_trans_step.getLineString();
  	  if ((source_line != null) && !source_line.equals("")) {

  	    // We need to deal with gdf-specific special characters which couls appear
  	    // in the Java source line, such as '"'.
  	    result.append(source_line);
  	  	convertGdfSpecial(result);
  	  }
    }
  	return result.toString();
  }

  /**
   * Locates and replaces all occurrences of a given string with another given
   * string in an original string buffer. There seems to be a bug in Java
   * String's replaceAll() method which does not allow us to use it to replace
   * some special chars so here we use StringBuilder's replace method to do
   * this.
   * @param original The original string builder.
   * @param from The replaced string.
   * @param to The replacing string.
   * @return The original string builder with the substring replaced
   *         throughout.
   */
  private StringBuilder replaceString(StringBuilder original,
                                      String from,
                                      String to) {
    int indexOfReplaced=0, lastIndexOfReplaced=0;
    while (indexOfReplaced!=-1) {
      indexOfReplaced=original.indexOf(from,lastIndexOfReplaced);
      if (indexOfReplaced!=-1) {
      	original.replace(indexOfReplaced, indexOfReplaced+1, to);
        lastIndexOfReplaced=indexOfReplaced+to.length();
      }
    }
    return original;
  }

  /**
   * Locates and replaces all occurrences of a given string with another given
   * string in an original string buffer.
   * @param original The original string buffer.
   * @param from The replaced string.
   * @param to The replacing string.
   * @return The original string buffer with the sub-string replaced
   *         throughout.
   */
  private String replaceString(String original, String from, String to) {
  	if ((original!=null) && (from!=null) && (to!=null)) {
      return replaceString(new StringBuilder(original), from, to).toString();
  	} else {
      return original;
  	}
  }

  /**
   * Add a new node to the graph with the relevant properties.
   */
  private void addNode(StateInformation state) throws IOException {
    if (state.is_new) {
      if (format==GDF_FORMAT) {
        graph.write("st" + state.id + ",\"" + state.id);
        if (state.error != null) {
          graph.write(":" + state.error);
        }
        graph.write("\","+state_node_style);
        if (state.error != null) {
            graph.write(",red");
          } else if (state.has_next) {
            graph.write(",black");
          } else {
            graph.write(",green");
          }
      } else { // The dot version
        graph.write("  st" + state.id + " [label=\"" + state.id);
        if (state.error != null) {
          graph.write(":" + state.error);
        }
        graph.write("\",shape=");
        if (state.error != null) {
          graph.write("diamond,color=red");
        } else if (state.has_next) {
          graph.write("circle,color=black");
        } else {
          graph.write("egg,color=green");
        }
        graph.write("];");
      }
      graph.newLine();
    }
  }

  private static class StateInformation {
    public StateInformation() {}
    public void reset(int id, boolean has_next, boolean is_new) {
      this.id = id;
      this.has_next = has_next;
      this.error = null;
      this.is_new = is_new;
    }
    int id = -1;
    boolean has_next = true;
    String error = null;
    boolean is_new = false;
  }

  /**
   * Creates an GDF edge string.
   */
  private String makeGdfEdgeString(String from_id,
  		                           String to_id,
								   String label,
								   int thread) {
  	StringBuilder sb=new StringBuilder(from_id);
  	sb.append(',').append(to_id).append(',').append('\"');
  	if ((label!=null) && (!"".equals(label))) {
  		sb.append(label);
  	} else {
  		sb.append('-');
  	}
  	sb.append('\"').append(',').append(labelvisible).append(',').append(true).
	append(',').append(thread);
  	replaceString(sb, "\n", "");
  	return sb.toString();
  }

  /**
   * GUESS cannot deal with '\n' chars, so remove them. Also convert all '"'
   * characters to "''".
   * @param str The string to perform the conversion on.
   * @return The converted string.
   */
  private String convertGdfSpecial(String str) {
  	if ((str==null) || "".equals(str)) return "";

  	StringBuilder sb=new StringBuilder(str);
  	convertGdfSpecial(sb);
  	return sb.toString();
  }

  /**
   * GUESS cannot deal with '\n' chars, so replace them with a space. Also
   * convert all '"' characters to "''".
   * @param sb The string buffer to perform the conversion on.
   */
  private void convertGdfSpecial(StringBuilder sb) {
  	replaceString(sb, "\"", "\'\'");
  	replaceString(sb, "\n", " ");
  }

  /**
   * Create an edge in the graph file from old_id to new_id.
   */
  private void addEdge(int old_id, int new_id, Search state) throws IOException {
    int my_id = edge_id++;
    if (format==GDF_FORMAT) {
      Transition trans=state.getTransition();
      int thread = trans.getThreadIndex();

      // edgedef>node1,node2,label,labelvisible,directed,thread INT

      // Old State -> Transition - labeled with the source info if any.
      gdfEdges.add(
      		makeGdfEdgeString("st"+old_id, "tr"+my_id,
      				          makeGdfLabel(state, my_id),
      				          thread));

      // Transition node: name,label,style,color
      graph.write("tr" + my_id + ",\"" +my_id+"\","+transition_node_style);

      graph.newLine();
      // Transition -> New State - labeled with the last output if any.

      String lastOutputLabel=
      	replaceString(convertGdfSpecial(trans.getOutput()), "\"", "\'\'");
      gdfEdges.add(
      	makeGdfEdgeString("tr"+my_id, "st"+new_id, lastOutputLabel, thread));
    } else { // DOT
      graph.write("  st" + old_id + " -> tr" + my_id + ";");
      graph.newLine();
      graph.write("  tr" + my_id + " [label=\"" + makeDotLabel(state, my_id) +
                  "\",shape=box]");
      graph.newLine();
      graph.write("  tr" + my_id + " -> st" + new_id + ";");
    }
  }

  /**
   * Show the usage message.
   */
  static void showUsage() {
    System.out
        .println("Usage: \"java [<vm-options>] gov.nasa.jpf.tools.StateSpaceDot [<graph-options>] [<jpf-options-and-args>]");
    System.out.println("  <graph-options> : ");
    System.out.println("    -gdf:                Generate the graph in GDF format. The default is DOT.");
    System.out.println("    -transition-numbers: Include transition numbers in transition labels.");
    System.out.println("    -show-source:        Include source lines in transition labels.");
    System.out.println("    -labelvisible:       Indicates if the label on the transitions is visible (used only with the -gdf option)");
    System.out.println("    -help:               Prints this help information and stops.");
    System.out.println("  <jpf-options-and-args>:");
    System.out.println("    Options and command line arguments passed directly to JPF.");
    System.out.println("  Note: With -gdf option transition edges could also include program output ");
    System.out.println("  but in order to enable this JPF's vm.path_output option must be set to ");
    System.out.println("  true.");
  }

  void filterArgs (String[] args) {
    for (int i=0; i<args.length; i++) {
      if (args[i] != null){
        String arg = args[i];
        if ("-transition-numbers".equals(arg)) {
          transition_numbers = true;
          args[i] = null;
        } else if ("-show-source".equals(arg)) {
          show_source = true;
          args[i] = null;
        } else if ("-gdf".equals(arg)) {
          format=GDF_FORMAT;
          out_filename=OUT_FILENAME_NO_EXT+"."+GDF_EXT;
          args[i] = null;
        } else if ("-labelvisible".equals(arg)) {
          labelvisible=true;
          args[i] = null;
        } else if  ("-help".equals(args[i])) {
          showUsage();
          helpRequested=true;
        }
      }
    }
  }

}
