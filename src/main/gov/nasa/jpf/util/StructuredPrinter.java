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

import java.io.PrintWriter;

/**
 * a common base for printers that need to keep track of indentation levels
 */
public abstract class StructuredPrinter {

  protected PrintWriter pw;

  protected int indentLevel = 0;
  protected String indent = "";
  
  protected StructuredPrinter(){
    pw = new PrintWriter(System.out, true);
  }
  
  protected StructuredPrinter (PrintWriter pw){
    this.pw = pw;
  }
  
  protected void incIndent(){
    indentLevel++;
    indent = indent();
  }

  protected void decIndent(){
    if (indentLevel > 0){
      indentLevel--;
      indent = indent();
    }
  }

  protected String indent(){
    switch (indentLevel){
      case 0: return "";
      case 1: return "    ";
      case 2: return "        ";
      case 3: return "            ";
      case 4: return "                ";
      default:
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<indentLevel; i++){
          sb.append("    ");
        }
        return sb.toString();
    }
  }
  
  protected void printSectionHeader(String id){
    pw.println();
    pw.print("--------------------------------------------------- ");
    pw.println(id);
  }

  
}
