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

package java.util.regex;

/**
 * model of a regular expression matcher, to save memory and execution time
 */
public class Matcher {

  // this is the same trick like java.text.Format - avoiding a native
  // memory leak by means of overwriting a JPF state tracked index value
  // (well, it's still a leak since it never gets recycled unless we add a
  // finalizer, but it should be much less serious)
  static int nInstances;
  private int id = nInstances++; // just for peer implementation purposes 
  
  Pattern pattern;
  String input;    // that's an approximation (don't use CharSequence on the native side)
  
  Matcher() {
  }
  
  Matcher (Pattern pattern, CharSequence inp){
    this.pattern = pattern;
    this.input = inp.toString();
    
    register();
  }
  
  public Pattern pattern() {
    return pattern;
  }
  
  native void register();
  
  public native Matcher reset();
  
  public String group() {
    return group(0);
  }
  
  public native String group(int group);
  
  public native int groupCount();

  public Matcher reset(CharSequence inp) {
    this.input = inp.toString();
    return reset();
  }

  public native boolean matches();
  
  public native boolean find();

  public native boolean find(int start);
  
  public native boolean lookingAt();
  
  public int start() {
    return start(0);
  }
  
  public native int start(int group);
  
  public int end() {
    return end(0);
  }
  
  public native int end(int group);

  public native boolean hasTransparentBounds();

  public native Matcher useTransparentBounds(boolean b);

  public native boolean hasAnchoringBounds();

  public native Matcher useAnchoringBounds(boolean b);

  public Matcher usePattern(Pattern newPattern){
    this.pattern = newPattern;
    return updatePattern();
  }

  public native Matcher updatePattern();

  public native int regionStart();

  public native int regionEnd();

  public native Matcher region(int start, int end);

  public static native String quoteReplacement(String abc);

  public native String replaceAll(String replacement);

  public native String replaceFirst(String replacement);

  @Override
  public native String toString();

  public native boolean hitEnd();

  public native boolean requireEnd();

  // TODO public native MatchResult toMatchResult();
  // TODO public native StringBuffer appendTail(StringBuffer sb);
  // TODO public native Matcher appendReplacement(StringBuffer sb, String replacement);
}
