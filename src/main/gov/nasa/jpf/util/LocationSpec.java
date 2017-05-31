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

import java.io.File;

/**
 * support for specification of source locations
 *
 * This maps sourcefile:line1-range strings into instructions that can be efficiently
 * checked for by listeners. Example formats are:
 *
 *  X.java                (the whole of file X.java)
 *  MyClass.java:42       (file MyClass.java, line1 42)
 *  FooBar.java:42-48     (file FooBar.java, lines 42 to 48)
 *  FooBar.java:42+3      (range of lines from 42 to 45)
 *  x/y/z/whatever:42+    (file with pathname x/y/z/whatever, lines 42 to end)
 * 
 *
 * NOTE path names are given in Unix notation, to avoid the usual Java string
 * quoting problem with backslashes
 */
public class LocationSpec {

  public static final int ANYLINE = -1;

  protected StringMatcher pathSpec;
  protected StringMatcher fileSpec;
  protected int fromLine = ANYLINE;   // inclusive
  protected int toLine = ANYLINE;     // inclusive

  /**
   * factory method that includes the parser
   */
  public static LocationSpec createLocationSpec (String s){
    s = s.replace('\\', '/');

    String pspec = null, fspec;
    int line1 = -1, line2 = -1;

    int idx = s.lastIndexOf(':');
    // check if it is just a dreaded "drive letter"
    if (idx == s.length()-1 || s.charAt(idx + 1) == '/') {
      idx = -1;
    }

    if (idx < 0){ // no line1 spec
      fspec = s.trim();

    } else if (idx > 0){ // line1 spec present
      fspec = s.substring(0, idx).trim();

      // now get the line1 (range)
      s = s.substring(idx+1).trim();
      int len = s.length();

      if (len > 0){
        int i = 0;
        for (; i < len; i++) {
          char c = s.charAt(i);
          if (c == '-' || c == '+') {
            line1 = Integer.parseInt(s.substring(0, i));

            if (i == len - 1) { // open interval
              line2 = Integer.MAX_VALUE;
            } else {
              line2 = Integer.parseInt(s.substring(i + 1));
              if (c == '+') {
                line2 = line1 + line2;
              }
            }
            break;
          }
        }

        if (i == len) { // single line
          line1 = Integer.parseInt(s);
        }
      }

    } else {
      throw new RuntimeException("no filename in LocationSpec: " + s);
    }

    idx = fspec.lastIndexOf('/');
    if (idx > 0){
      pspec = fspec.substring(0, idx);
      fspec = fspec.substring(idx+1);
    } else if (idx == 0){
      pspec = "/";
      fspec = fspec.substring(1);
    } else {
      pspec = null;
    }

    return new LocationSpec(pspec, fspec, line1, line2);
  }

  public LocationSpec (String pspec, String fspec, int line1, int line2){
    if (pspec != null){
      pathSpec = new StringMatcher(pspec);
    }
    fileSpec = new StringMatcher(fspec);

    fromLine = line1;
    toLine = line2;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();

    if (pathSpec != null){
      sb.append(pathSpec);
      sb.append('/');
    }

    sb.append(fileSpec);

    if (fromLine != ANYLINE){
      sb.append(':');
      sb.append(fromLine);
    }
    if (toLine != ANYLINE){
      sb.append('-');
      if (toLine != Integer.MAX_VALUE){
        sb.append(toLine);
      }
    }

    return sb.toString();
  }

  public boolean matchesFile (File f){
    if (fileSpec.matches(f.getName())){
      if (pathSpec != null){
        String pspec = f.getParent();
        pspec = pspec.replace('\\', '/'); // use Unix '/' to avoid quoting issue

        return pathSpec.matches(pspec);

      } else { // no path
        return true;
      }
    }

    return false;
  }

  public boolean isAnyLine(){
    return fromLine == ANYLINE;
  }

  public boolean isLineInterval(){
    return toLine != ANYLINE;
  }

  public int getLine(){ // for specs that are single locations
    return fromLine;
  }

  public int getFromLine() {
    return fromLine;
  }

  // note - this is < 0 if there was only a
  public int getToLine() {
    if (toLine < 0){
      if (fromLine >= 0){
        return fromLine;
      }
    }

    return toLine;
  }

  /**
   * 'pathName' has to use '/' as separators
   */
  public boolean matchesFile (String pathName){
    if (pathName != null){
      pathName = pathName.replace('\\', '/');
      int idx = pathName.lastIndexOf('/');

      if (idx >= 0) {
        String fname = pathName.substring(idx + 1);

        if (fileSpec.matches(fname)) {
          if (pathSpec != null) {
            String pname = idx > 0 ? pathName.substring(0, idx) : "/";
            return pathSpec.matches(pname);
          } else {
            return true;
          }
        }

      } else { // no path
        return fileSpec.matches(pathName);
      }
    }

    return false;
  }
  
  public boolean includesLine (int line){
    if (fromLine == ANYLINE){
      return true;

    } else {
      if (fromLine == line){
        return true;
      } else if (fromLine < line){
        if (toLine == ANYLINE){ // single location
          return false;
        } else {
          return (line <= toLine);
        }
      }
    }

    return false;
  }
}
