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

package gov.nasa.jpf.util;

import gov.nasa.jpf.vm.MJIEnv;

import java.io.PrintStream;

public class PrintUtils {

  public static void printChar (PrintStream ps, char c){
    switch (c) {
    case '\n': ps.print("\\n"); break;
    case '\r': ps.print("\\r"); break;
    case '\t': ps.print("\\t"); break;
    case '\b': ps.print("\\b"); break;
    case '\f': ps.print("\\f"); break;
    case '\'': ps.print("\\'"); break;
    case '\"': ps.print("\\\""); break;
    case '\\': ps.print("\\"); break;
    default: ps.print(c);
    }
  }
  
  public static void printCharLiteral (PrintStream ps, char c){
    ps.print('\'');
    printChar(ps, c);
    ps.print('\'');
  }
  
  public static void printStringLiteral (PrintStream ps, char[] data, int max){
    int i;
    if (max < 0){
      max = data.length;
    }
    
    ps.print('"');
    for (i=0; i<max; i++){
      printChar(ps, data[i]);
    }
    
    if (i< data.length){
      ps.print("...");
    }
    
    ps.print('"');
  }
  
  // this is mostly here so that we use the same convention
  public static void printReference (PrintStream ps, int ref){
    if (ref == MJIEnv.NULL){
      ps.print("null");
    } else {
      ps.print('@');
      ps.printf("%x", ref);
    }
  }
}
