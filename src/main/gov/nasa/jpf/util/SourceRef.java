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

/**
 * a source reference abstraction wrapping file and line information
 */
public class SourceRef {
  public String fileName;
  public int    line;

  public SourceRef (String f, int l) {
    if (f == null) {
      fileName = "?";
    } else {
      fileName = f;
    }
    
    line = l;
  }

  public SourceRef (String spec){
    int idx = spec.indexOf(':');
    if (idx > 0){
      fileName = spec.substring(0, idx);
      line = Integer.parseInt(spec.substring(idx+1));
    } else {
      fileName = spec;
      line = 0;
    }
  }

  public String getLocationString() {
    return (fileName + ':' + line);
  }
  
  public String getLineString () {
    Source source = Source.getSource(fileName);
    if (source != null) {
      return source.getLine(line);
    } else {
      return null;
    }
  }

  @Override
  public boolean equals (Object o) {
    if (o == null) {
      return false;
    }

    if (!(o instanceof SourceRef)) {
      return false;
    }

    SourceRef that = (SourceRef) o;

    if (this.fileName == null) {
      return false;
    }

    if (this.line == -1) {
      return false;
    }

    if (!this.fileName.equals(that.fileName)) {
      return false;
    }

    if (this.line != that.line) {
      return false;
    }

    return true;
  }

  public boolean equals (String f, int l) {
    if (fileName == null) {
      return false;
    }

    if (line == -1) {
      return false;
    }

    if (!fileName.equals(f)) {
      return false;
    }

    if (line != l) {
      return false;
    }

    return true;
  }

  public boolean equals (String filePos){
    if (filePos.startsWith(fileName)){
      int len = fileName.length();
      if (filePos.charAt(len) == ':'){
        if (Integer.parseInt(filePos.substring(len+1)) == line){
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    assert false : "hashCode not designed";
    return 42; // any arbitrary constant will do
    // thanks, FindBugs!
  }

  public String getFileName () {
    return fileName;
  }

  public void set (SourceRef sr) {
    fileName = sr.fileName;
    line = sr.line;
  }

  @Override
  public String toString () {
    return (fileName + ':' + line);
  }
}
