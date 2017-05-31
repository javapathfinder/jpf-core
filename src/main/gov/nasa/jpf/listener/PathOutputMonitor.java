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
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.Transition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * listener that monitors path output, matching it against specifications
 * supplied as text files. Per default, this uses simple line-by-line
 * regular expression matching, also supporting prefixes by means of
 * special ". . ." ellipsis patterns. Each file can contain a number of
 * path output specs, separated by "~~~~~" lines.
 * 
 *  The major purpose of this listener is to verify JPF state spaces,
 *  but it can also be used as a functional property
 */
public class PathOutputMonitor extends PropertyListenerAdapter {
 
  static final String SEPARATOR = "~~~";
  static final String ELLIPSIS = "...";
  
  static Logger log = JPF.getLogger("gov.nasa.jpf.listener.PathOutputMonitor");

  public interface PathOutputSpec {
    boolean add (String spec);
    boolean matches (String[] output);
    void printOn (PrintWriter pw);
    boolean isEmpty();
  }
  
  static class VerbatimOutputSpec implements PathOutputSpec {
    ArrayList<String> patterns = new ArrayList<String>();

    @Override
	  public boolean add (String spec) {
      patterns.add(spec);
      return true;
    }
    
    @Override
	  public boolean matches (String[] output) {
      if ((output != null) && (output.length > 0)) {
        Iterator<String> it = patterns.iterator();
        for (String line : output) {
          if (it.hasNext()) {
            String p = it.next();
            if (!p.equals(line)){
              return false;
            }
          } else {
            return false;
          }
        }
        
        return !it.hasNext();
        
      } else {
        return patterns.isEmpty();        
      }
    }
    
    // sometimes, duck typing would be nice..
    
    @Override
	  public void printOn (PrintWriter pw) {
      for (String p : patterns) {
        pw.println(p.toString());
      }
    }
    
    @Override
    public boolean isEmpty(){
      return patterns.isEmpty();
    }
  }
  
  // simple regular expression matchers (could be a more sophisticated parser)
  static class RegexOutputSpec implements PathOutputSpec {  
    ArrayList<Pattern> patterns = new ArrayList<Pattern>();
    
    @Override
	  public boolean add (String spec) {
      try {
        Pattern p = Pattern.compile(spec);
        patterns.add(p);
      } catch (PatternSyntaxException psx) {
        return false;
      }
      
      return true;
    }
    
    @Override
	  public boolean matches (String[] output) {
      
      if ((output != null) && (output.length > 0)) {
        Iterator<Pattern> it = patterns.iterator();
        for (String line : output) {
          if (it.hasNext()) {
            Pattern p = it.next();

            if (p.toString().equals(ELLIPSIS)) {
              return true;
            }

            Matcher m = p.matcher(line);
            if (!m.matches()) {
              return false;
            }
          } else {
            return false;
          }
        }

        return (!it.hasNext() || it.next().toString().equals(ELLIPSIS));          
        
      } else { // no output
        return patterns.isEmpty();
      }
    }
    
    @Override
	  public void printOn (PrintWriter pw) {
      for (Pattern p : patterns) {
        pw.println(p.toString());
      }
    }
    
    @Override
    public boolean isEmpty(){
      return patterns.isEmpty();
    }
  }

  //---- our instance data
  VM vm;
  
  //--- this is where we store the outputs (line-wise)
  // <2do> not very space efficient
  List<String[]> pathOutputs = new ArrayList<String[]>();
  
  //--- config options
  Class<? extends PathOutputSpec> psClass; 
  boolean printOutput;
  boolean deferOutput;
  List<PathOutputSpec> anySpecs, allSpecs, noneSpecs;

  //--- keep track of property violations
  String errorMsg;
  List<PathOutputSpec> violatedSpecs;
  String[] offendingOutput;
    
  
  public PathOutputMonitor (Config config, JPF jpf) {
    vm = jpf.getVM();
    vm.storePathOutput();
    
    jpf.addPublisherExtension(ConsolePublisher.class, this);
    
    printOutput = config.getBoolean("pom.print_output", true);
    deferOutput = config.getBoolean("pom.defer_output", true);
  
    psClass = config.getClass("pom.output_spec.class", PathOutputSpec.class);

    if (psClass == null) {
      psClass = RegexOutputSpec.class;
    }
    
    anySpecs = loadSpecs(config, "pom.any");
    allSpecs = loadSpecs(config, "pom.all");
    noneSpecs = loadSpecs(config, "pom.none");
    
    violatedSpecs = new ArrayList<PathOutputSpec>();
  }

  
  List<PathOutputSpec> loadSpecs(Config conf, String key) {
    String spec = conf.getString(key);
    if (spec != null) {
      if (spec.startsWith("\"")){ // spec is in-situ content (convenience method for test classes)
        spec = spec.substring(1, spec.length()-1);
        BufferedReader br = new BufferedReader( new StringReader(spec));
        return readPathPatterns(br);
        
      } else { // spec is pathname of output sepc file
        File file = new File(spec);
        try {
          BufferedReader br = new BufferedReader( new FileReader(file));
          return readPathPatterns(br);
          
        } catch (FileNotFoundException fnfx){
          log.warning("pattern file not found: " + spec);
        }        
      }
    }
    
    return null;
  }

  PathOutputSpec createPathOutputSpec() {
    try {
      return psClass.newInstance();
    } catch (Throwable t) {
      log.severe("cannot instantiate PathoutputSpec class: " + t.getMessage());
      return null;
    }
  }
  
  
  
  List<PathOutputSpec> readPathPatterns (BufferedReader br){  
    ArrayList<PathOutputSpec> results = new ArrayList<PathOutputSpec>();
    
    // prefix pattern goes into file
    
    try {
      PathOutputSpec ps = createPathOutputSpec();
      
      int lineno = 0;
      for (String line=br.readLine(); true; line = br.readLine()) {
        if (line == null) {
          results.add(ps);
          break;
        }
        lineno++;
        
        if (line.startsWith(SEPARATOR)) {
          results.add(ps);
          ps = createPathOutputSpec();
        } else {
          ps.add(line);
        }
      }
            
      br.close();
    } catch (FileNotFoundException fnfx) {
      return null;
    } catch (IOException e) {
      e.printStackTrace();
    }
      
    return results;
  }

  String[] getLines (String output) {
    ArrayList<String> lines = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new StringReader(output));
    try {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        lines.add(line);
      }
    } catch (IOException iox) {
      iox.printStackTrace();
    }
    
    return lines.toArray(new String[lines.size()]);
  }

  boolean matchesAny (List<PathOutputSpec> outputSpecs, String[] lines) {
    for (PathOutputSpec ps : outputSpecs) {
      if (ps.matches(lines)) {
        return true;
      }
    }

    errorMsg = "unmatched output";
    offendingOutput = lines;
    
    return false;
  }
    
  boolean matchesNone (List<PathOutputSpec> outputSpecs, String[] lines) {
    for (PathOutputSpec ps : outputSpecs) {
      if (ps.matches(lines)) {
        errorMsg = "illegal output (matching inverse spec)";
        offendingOutput = lines;
        violatedSpecs.add(ps);
        
        return false;
      }
    }
        
    return true;
  } 
  
  boolean matchesAll (List<PathOutputSpec> outputSpecs, List<String[]> outputs) {
    HashSet<PathOutputSpec> unmatched = new HashSet<PathOutputSpec>();
    unmatched.addAll(outputSpecs);
    
    for (String[] lines : outputs) {
      for (PathOutputSpec ps : outputSpecs) {
        if (ps.matches(lines)) {
           unmatched.remove(ps);
          if (unmatched.isEmpty()) {
            return true;
          }
        }
      }
    }

    errorMsg = "unmatched specs (" + unmatched.size() + ')';
    for (PathOutputSpec ps : unmatched) {
      violatedSpecs.add(ps);
    }
    
    return false;
  }

  
  //----------- the listener interface
  
  @Override
  public boolean check(Search search, VM vm) {
    return (errorMsg == null);
  }

  @Override
  public String getErrorMessage () {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    pw.println(errorMsg);
    
    if (offendingOutput != null) {
      pw.println("offending output:");
      for (String line : offendingOutput) {
        pw.println(line);
      }
    }
    
    if (!violatedSpecs.isEmpty()) {
      pw.println("violated specs:");
      for (PathOutputSpec ps : violatedSpecs) {
        ps.printOn(pw);
        pw.println(SEPARATOR);
      }
      
    }
    
    String s = sw.toString();
    pw.close();
    
    return s;
  }
  
  @Override
  public void reset () {
    errorMsg = null;
    violatedSpecs.clear();
    offendingOutput = null;
  }

  @Override
  public void stateAdvanced(Search search) {
    if (search.isEndState()) {
      
      Path path = vm.getPath();
      if (path.hasOutput()) {
        StringBuilder sb = null;
        
        if (deferOutput || (noneSpecs != null)) {
          sb = new StringBuilder();
          for (Transition t : path) {
            String s = t.getOutput();
            if (s != null){
              sb.append(s);
            }
          }
        }

        String[] lines = getLines(sb.toString());
        
        if (deferOutput) {
          pathOutputs.add(lines);
          
        } else if (printOutput){
          for (Transition t : path) {
            String s = t.getOutput();
            if (s != null){
              System.out.print(s); // <2do> don't use System.out
            }            
          }          
        }
      
        // check safety properties
        if (noneSpecs != null && !matchesNone(noneSpecs, lines)) {
           log.warning("pom.none violated");
        }
        if (anySpecs != null && !matchesAny(anySpecs, lines)) {
          log.warning("pom.any violated");
       }        
        
      }
    }
  }
  
  @Override
  public void searchFinished (Search search) {
    if (allSpecs != null && !matchesAll(allSpecs, pathOutputs)) {
      log.warning("pom.all violated");
      search.error(this);
    }
  }
  
  @Override
  public void publishFinished (Publisher publisher) {
    
    if (printOutput) {
      PrintWriter pw = publisher.getOut();
      publisher.publishTopicStart("path outputs");
      for (String[] output : pathOutputs) {
        for (String line : output) {
          pw.println(line);
        }
        pw.println(SEPARATOR);
      }
    }    
  }
    
}
