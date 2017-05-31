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

import gov.nasa.jpf.JPFException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.HashMap;

/**
 * a little helper class that is used to replay previously stored traces
 * (which are little more than just a list of ChoiceGenerator classnames and
 * choiceIndex indexes stored in a previous run)
 */
public class ChoicePoint {
  String cgClassName;
  int choiceIndex;
  ChoicePoint next, prev;

  ChoicePoint (String cgClassName, int choice, ChoicePoint prev) {
    this.cgClassName = cgClassName;
    this.choiceIndex = choice;

    if (prev != null) {
      this.prev = prev;
      prev.next = this;
    }
  }

  public String getCgClassName() {
    return cgClassName;
  }

  public int getChoiceIndex() {
    return choiceIndex;
  }

  public ChoicePoint getNext() {
    return next;
  }

  public ChoicePoint getPrevious() {
    return prev;
  }

  public static void storeTrace (String fileName,
                                 String sutName, String comment,
                                 ChoiceGenerator[] trace, boolean verbose) {
    int i;
    if (fileName != null) {
      try {
        FileWriter fw = new FileWriter(fileName);
        PrintWriter pw = new PrintWriter(fw);

        if (comment != null){ // might be multi-line
          pw.print("/* ");
          pw.print(comment);
          pw.println(" */");
        }

        // store the main app class and args here, so that we can do at least some checking
        pw.print( "application: ");
        pw.println( sutName);

        // keep a String->id map so that we don't have to store thousands of redundant strings
        HashMap<String,Integer> map = new HashMap<String,Integer>();
        int clsId = 0;

        for (i=0; i<trace.length; i++) {
          String cgClsName = trace[i].getClass().getName();

          pw.print('[');
          pw.print(i);
          pw.print("] ");

          Integer ref = map.get(cgClsName);
          if (ref == null) {
            pw.print(cgClsName);
            map.put(cgClsName, clsId++);
          } else { // us the numeric id instead
            pw.print('#');
            pw.print(ref.intValue());
          }

          pw.print(" ");
          pw.print( trace[i].getProcessedNumberOfChoices()-1);
          
          if (verbose){
            pw.print("  // ");
            pw.print(trace[i]);
          }
          
          pw.println();
        }

        pw.close();
        fw.close();
      } catch (Throwable t) {
        throw new JPFException(t);
      }
    }
  }

  static StreamTokenizer createScanner (String fileName) {
    StreamTokenizer scanner = null;

    if (fileName == null) {
      return null;
    }

    File f = new File(fileName);
    if (f.exists()) {
      try {
        FileReader fr = new FileReader(f);
        scanner = new StreamTokenizer(fr);

        scanner.slashSlashComments(true);
        scanner.slashStarComments(true);

        // how silly - there is no better way to turn off parseNumbers()
        scanner.resetSyntax();
        scanner.wordChars('a', 'z');
        scanner.wordChars('A', 'Z');
        scanner.wordChars(128 + 32, 255);
        scanner.whitespaceChars(0, ' ');
        //scanner.commentChar('/');
        scanner.quoteChar('"');
        scanner.quoteChar('\'');

        scanner.wordChars('0','9');
        scanner.wordChars(':', ':');
        scanner.wordChars('.', '.');
        scanner.wordChars('#', '#');

        scanner.nextToken();
      } catch (IOException iox) {
        throw new JPFException("cannot read tracefile: " + fileName);
      }

      return scanner;
    } else {
      return null;
    }
  }

  static void match (StreamTokenizer scanner, String s) throws IOException {
    if ((scanner.ttype == StreamTokenizer.TT_WORD) && (scanner.sval.equals(s))) {
      scanner.nextToken();
    } else {
      throw new JPFException ("tracefile error - expected " + s + ", got: " + scanner.sval);
    }
  }

  static String matchString (StreamTokenizer scanner) throws IOException {
    if (scanner.ttype == StreamTokenizer.TT_WORD) {
      String s = scanner.sval;
      if (s.length() == 0) {
        throw new JPFException ("tracefile error - non-empty string expected");
      }
      scanner.nextToken();
      return s;
    } else {
      throw new JPFException ("tracefile error - word expected, got: " + scanner.sval);
    }
  }

  static void matchChar (StreamTokenizer scanner, char c) throws IOException {
    if (scanner.ttype == c) {
      scanner.nextToken();
    } else {
      throw new JPFException ("tracefile error - char '"
                              + c + "' expected, got: " + scanner.sval);
    }
  }

  static int matchNumber (StreamTokenizer scanner) throws IOException {
    try {
      if (scanner.ttype == StreamTokenizer.TT_WORD) {
        int n = Integer.parseInt(scanner.sval);
        scanner.nextToken();
        return n;
      }
    } catch (NumberFormatException nfx) {}

    throw new JPFException ("tracefile error - number expected, got: " + scanner.sval);
  }

  /**
   * "application:" appName
   *  {arg}
   *  "["searchLevel"]" (choiceGeneratorName | '#'cgID) nChoice
   */
  public static ChoicePoint readTrace (String fileName, String sutName) {
    ChoicePoint firstCp = null, cp = null;
    StreamTokenizer scanner = createScanner(fileName);

    if (scanner == null) {
      return null;
    }

    try {
    match(scanner, "application:");
    match(scanner, sutName);

    HashMap<String,String> map = new HashMap<String,String>();
    int clsId = 0;

    while (scanner.ttype != StreamTokenizer.TT_EOF) {
      matchChar(scanner, '[');
      matchNumber(scanner);
      matchChar(scanner, ']');

      String cpClass  = matchString(scanner);
      if (cpClass.charAt(0) == '#') { // it's a CG class id
        cpClass = map.get(cpClass);
        if (cpClass == null) {
          throw new JPFException("tracefile error - unknown ChoicePoint class id: " + cpClass);
        }
      } else {
        String id = "#" + clsId++;
        map.put(id, cpClass);
      }

      int choiceIndex = matchNumber(scanner);

      cp = new ChoicePoint(cpClass, choiceIndex, cp);
      if (firstCp == null) {
        firstCp = cp;
      }
    }
    } catch (IOException iox) {
      throw new JPFException("tracefile read error: " + iox.getMessage());
    }

    return firstCp;
  }
}
