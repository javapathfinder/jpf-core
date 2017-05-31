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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utility to perform pathname expansion
 * the following patterns are supported so far:
 *
 * (1) brace expansion ala bash: foo{Boo,Shoo} => fooBoo, fooShoo
 *     (this doesn't check for existence, its simply lexical)
 *
 * (2) '*' wildcard pathname expansion ala bash: "*.java" | "*\Main*.java"
 *     (supports wildcards in mutiple path elements and within file/dir name)
 *
 * (3) recursive dir expansion ala Ant: "**\*.jar"
 *
 */
public class PathnameExpander {

  public String[] expandPath (String s) {
    if (s == null || s.length() == 0) {
      return null;
    }

    boolean hasWildcards = (s.indexOf('*') >= 0);

    int i = s.indexOf('{');
    if (i >= 0){
      ArrayList<String> list = new ArrayList<String>();

      int j=0, jLast = s.length();
      for (; (i = s.indexOf('{', j)) >= 0;) {
        if ((j = s.indexOf('}', i)) > 0) {
          String[] choices = s.substring(i + 1, j).split(",");

          if (list.isEmpty()) {
            String prefix = s.substring(0, i);
            for (String c : choices) {
              list.add(prefix + c);
            }
          } else {
            String prefix = s.substring(jLast, i);
            ArrayList<String> newList = new ArrayList<String>();
            for (String e : list) {
              for (String c : choices) {
                newList.add(e + prefix + c);
              }
            }
            list = newList;
          }
          jLast = j+1;
        } else {
          throw new IllegalArgumentException("illegal path spec (missing '}'): " + s);
        }
      }

      if (jLast < s.length()) {
        String postfix = s.substring(jLast);
        ArrayList<String> newList = new ArrayList<String>();
        for (String e : list) {
          newList.add(e + postfix);
        }
        list = newList;
      }

      if (hasWildcards){
        ArrayList<String> newList = new ArrayList<String>();
        for (String p : list) {
          for (String c : expandWildcards(p)) {
            newList.add(c);
          }
        }
        list = newList;
      }

      return list.toArray(new String[list.size()]);

    } else {  // no bracket expansion required

      if (hasWildcards){
        return expandWildcards(s);

      } else { // nothing to expand at all
        return (new String[] {s});
      }
    }
  }

  protected String[] expandWildcards (String s){
    int i = s.indexOf('*');

    if (i >= 0){ // Ok, we have at least one wildcard
      String[] a = s.split("\\/");
      ArrayList<File> list = new ArrayList<File>();

      int j= initializeMatchList(list, a[0]);
      for (; j<a.length; j++){
        ArrayList<File> newList = new ArrayList<File>();

        String e = a[j];
        if (e.indexOf('*') >= 0){

          if (e.equals("**")){ // matches all subdirs recursively
            collectDirs(list, newList);

          } else { // file/dir name match
            collectMatchingNames(list, newList, getPattern(e));
          }

        } else { // no wildcard
          collectExistingFile(list, newList, e);
        }

        if (newList.isEmpty()){  // shortcut, nothing more to match
          return new String[0];
        }
        list = newList;
      }

      return getPaths(list);

    } else { // no wildcards, nothing to expand
      return new String[] {s};
    }
  }

  private int initializeMatchList (ArrayList<File> list, String path){
    if (path.isEmpty()){ // absolute pathname (ignoring drive letters for now)
      list.add(new File(File.separator));
      return 1;
    } else if (path.equals("..") || path.equals(".")){
      list.add(new File(path));
      return 1;
    } else {
      list.add(new File("."));
      return 0;
    }
  }

  private void collectMatchingNames(ArrayList<File> list, ArrayList<File> newList, Pattern pattern){
    for (File dir : list) {
      if (dir.isDirectory()){
        for (String c : dir.list()){
          Matcher m = pattern.matcher(c);
          if (m.matches()){
            newList.add(new File(dir,c));
          }
        }
      }
    }
  }

  private void collectExistingFile(ArrayList<File> list, ArrayList<File> newList, String fname) {
    for (File dir : list) {
      if (dir.isDirectory()){
        File nf = new File(dir, fname);
        if (nf.exists()) {
          newList.add(nf);
        }
      }
    }
  }

  private void collectDirs(ArrayList<File> list, ArrayList<File> newList){
    for (File dir : list) {
      if (dir.isDirectory()){
        newList.add(dir); // this includes the dir itself!
        collectSubdirs(newList,dir);
      }
    }
  }
  private void collectSubdirs(ArrayList<File> newList, File dir) {
    for (File f : dir.listFiles()){
      if (f.isDirectory()){
        newList.add(f);
        collectSubdirs(newList, f);
      }
    }
  }

  protected String[] getPaths(ArrayList<File> list) {
    String[] result = new String[list.size()];
    int k=0;
    for (File f : list){
      String p = f.getPath();
      if ((p.length() > 1) && (p.charAt(0) == '.')){ // remove leading "./"
        char c = p.charAt(1);
        if (c == '\\' || c == '/'){
          p = p.substring(2);
        }
      }
      result[k++] = p;
    }
    return result;
  }

  protected Pattern getPattern(String s){
    Pattern p;

    StringBuilder sb = new StringBuilder();

    int len = s.length();
    for (int j=0; j<len; j++){
      char c = s.charAt(j);
      switch (c){
      case '.' : sb.append("\\."); break;
      case '$' : sb.append("\\$"); break;
      case '[' : sb.append("\\["); break;
      case ']' : sb.append("\\]"); break;
      case '*' : sb.append(".*"); break;
      // <2do> and probably more..
      default:   sb.append(c);
      }
    }

    p = Pattern.compile(sb.toString());
    return p;
  }
}
