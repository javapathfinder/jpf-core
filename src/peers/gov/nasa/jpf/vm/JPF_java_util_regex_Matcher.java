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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.DirectCallStackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MethodInfo;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * native peer for a regex Matcher
 * this is just a delegatee peer
 */
public class JPF_java_util_regex_Matcher extends NativePeer {

  // Track last append position for each matcher
  private HashMap<Integer, Integer> lastAppendPosition = new HashMap<>();
  
  HashMap<Integer, Matcher> matchers;
 
  public JPF_java_util_regex_Matcher (Config conf) {
    matchers = new HashMap<Integer,Matcher>();
  }

  Pattern getPatternFromMatcher(MJIEnv env, int objref) {
    int patRef = env.getReferenceField(objref, "pattern");

    int regexRef = env.getReferenceField(patRef, "regex");
    String regex = env.getStringObject(regexRef);
    int flags = env.getIntField(patRef, "flags");

    return Pattern.compile(regex, flags);
  }

  void putInstance (MJIEnv env, int objref, Matcher matcher) {
    int id = env.getIntField(objref,  "id");
    matchers.put(id, matcher);
  }

  Matcher getInstance (MJIEnv env, int objref) {
    
    int id = env.getIntField(objref,  "id");
    return matchers.get(id);
  }
  
  @MJI
  public void register____V (MJIEnv env, int objref) {
    Pattern pat = getPatternFromMatcher(env, objref);

    int inputRef = env.getReferenceField(objref, "input");
    String input = env.getStringObject(inputRef);
    
    Matcher matcher = pat.matcher(input);
    putInstance(env, objref, matcher);
    
    // Initialize last append position for this matcher
    int id = env.getIntField(objref, "id");
    lastAppendPosition.put(id, 0);
  }
  
  @MJI
  public boolean matches____Z (MJIEnv env, int objref) {
    Matcher matcher = getInstance( env, objref);
    return matcher.matches();
  }
  
  @MJI
  public boolean find__I__Z (MJIEnv env, int objref, int i) {
	Matcher matcher = getInstance( env, objref);
    return matcher.find(i);
  }

  @MJI
  public boolean find____Z (MJIEnv env, int objref) {
	Matcher matcher = getInstance( env, objref);
    return matcher.find();
  }

  @MJI
  public boolean lookingAt____Z(MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.lookingAt();
  }

  @MJI
  public int start__I__I(MJIEnv env, int objref, int group) {
    Matcher matcher = getInstance(env, objref);
    return matcher.start(group);
  }

  @MJI
  public int end__I__I(MJIEnv env, int objref, int group) {
    Matcher matcher = getInstance(env, objref);
    return matcher.end(group);
  }

  @MJI
  public int regionStart____I(MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.regionStart();
  }

  @MJI
  public int regionEnd____I(MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.regionEnd();
  }

  @MJI
  public int region__II__Ljava_util_regex_Matcher_2(MJIEnv env, int objref, int start, int end) {
    Matcher matcher = getInstance(env, objref);
    matcher = matcher.region(start, end);
    putInstance(env, objref, matcher);

    return objref;
  }

  @MJI
  public int reset____Ljava_util_regex_Matcher_2 (MJIEnv env, int objref) {
    Matcher matcher = getInstance( env, objref);

    int inputRef = env.getReferenceField(objref, "input");
    String input = env.getStringObject(inputRef);
    
    matcher = matcher.reset(input);
    putInstance(env, objref, matcher);

    // reset() resets append position to zero per Java spec
    int id = env.getIntField(objref, "id");
    lastAppendPosition.put(id, 0);

    return objref;
  }
  
  @MJI
  public int groupCount____I (MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.groupCount();
  }
  
  @MJI
  public int group__I__Ljava_lang_String_2 (MJIEnv env, int objref, int i) {
    Matcher matcher = getInstance( env, objref);
    String grp = matcher.group(i);
    
    return env.newString(grp);
  }

  @MJI
  public int quoteReplacement__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int clsObjref, int string) {
    String parm = env.getStringObject(string);
    String result = Matcher.quoteReplacement(parm);
    return env.newString(result);
  }

  @MJI
  public int replaceAll__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objref, int string) {
    Matcher matcher = getInstance(env, objref);
    String replacement = env.getStringObject(string);
    String result = matcher.replaceAll(replacement);

    int resultref = env.newString(result);
    return resultref;
  }
  
  @MJI
  public int replaceFirst__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, int objref, int string) {
    Matcher matcher = getInstance(env, objref);
    String replacement = env.getStringObject(string);
    String result = matcher.replaceFirst(replacement);

    int resultref = env.newString(result);
    return resultref;
  }

  @MJI
  public boolean hasTransparentBounds____Z(MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.hasTransparentBounds();
  }

  @MJI
  public int useTransparentBounds__Z__Ljava_util_regex_Matcher_2(MJIEnv env, int objref, boolean b) {
    Matcher matcher = getInstance(env, objref);
    matcher = matcher.useTransparentBounds(b);
    putInstance(env, objref, matcher);
     
    return objref;
  }

  @MJI
  public boolean hasAnchoringBounds____Z(MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    return matcher.hasTransparentBounds();
  }

  @MJI
  public int useAnchoringBounds__Z__Ljava_util_regex_Matcher_2(MJIEnv env, int objref, boolean b) {
    Matcher matcher = getInstance(env, objref);
    matcher = matcher.useAnchoringBounds(b);
    putInstance(env, objref, matcher);

    return objref;
  }

  @MJI
  public int updatePattern____Ljava_util_regex_Matcher_2(MJIEnv env, int objref) {
    //We get the newly updated pattern
    Pattern pat = getPatternFromMatcher(env, objref);

    //We update the matcher with the new pattern
    Matcher matcher = getInstance(env, objref);
    matcher = matcher.usePattern(pat);
    putInstance(env, objref, matcher);

    return objref;
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objref) {
    Matcher matcher = getInstance(env, objref);
    String str = matcher.toString();

    return env.newString(str);
  }

  @MJI
  public boolean hitEnd____Z (MJIEnv env, int objref) {
    Matcher matcher = getInstance( env, objref);
    return matcher.hitEnd();
  }

  @MJI
  public boolean requireEnd____Z (MJIEnv env, int objref) {
    Matcher matcher = getInstance( env, objref);
    return matcher.requireEnd();
  }

  @MJI
  public int appendTail__Ljava_lang_StringBuffer_2__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int sbRef) {
    int id = env.getIntField(objref, "id");

    int inputRef = env.getReferenceField(objref, "input");
    String input = env.getStringObject(inputRef);

    Integer lastPos = lastAppendPosition.get(id);
    if (lastPos == null) {
      lastPos = 0;
    }

    String tailContent = input.substring(lastPos);

    ThreadInfo ti = env.getThreadInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();

    if (frame == null) {
      ClassInfo sbClass = env.getClassInfo(sbRef);
      MethodInfo appendMi = sbClass.getMethod("append(Ljava/lang/String;)Ljava/lang/StringBuffer;", false);

      if (appendMi != null) {
        int tailRef = env.newString(tailContent);
        frame = appendMi.createDirectCallStackFrame(ti, 1);
        int argOffset = frame.setReferenceArgument(0, sbRef, null);
        frame.setReferenceArgument(argOffset, tailRef, null);
        ti.pushFrame(frame);
        // advance append position to end of input so a second call appends nothing
        lastAppendPosition.put(id, input.length());
        env.repeatInvocation();
      }
    }

    return sbRef;
  }

  @MJI
  public int appendReplacement__Ljava_lang_StringBuffer_2Ljava_lang_String_2__Ljava_util_regex_Matcher_2 (MJIEnv env, int objref, int sbRef, int replacementRef) {
    ThreadInfo ti = env.getThreadInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();
    
    if (frame == null) {
      try {
        Matcher matcher = getInstance(env, objref);
        String replacement = env.getStringObject(replacementRef);
        int id = env.getIntField(objref, "id");
        
        int inputRef = env.getReferenceField(objref, "input");
        String input = env.getStringObject(inputRef);
        
        int matchStart = matcher.start();
        int matchEnd = matcher.end();
        
        Integer lastPos = lastAppendPosition.get(id);
        if (lastPos == null) {
          lastPos = 0;
        }
        
        String beforeMatch = input.substring(lastPos, matchStart);
        String processedReplacement = processReplacement(replacement, matcher);
        String content = beforeMatch + processedReplacement;
        
        ClassInfo sbClass = env.getClassInfo(sbRef);
        MethodInfo appendMi = sbClass.getMethod("append(Ljava/lang/String;)Ljava/lang/StringBuffer;", false);

        if (appendMi != null) {
          int contentRef = env.newString(content);
          frame = appendMi.createDirectCallStackFrame(ti, 1);
          int argOffset = frame.setReferenceArgument(0, sbRef, null);
          frame.setReferenceArgument(argOffset, contentRef, null);
          ti.pushFrame(frame);
          // update position only after we know the append will actually happen
          lastAppendPosition.put(id, matchEnd);
          env.repeatInvocation();
        }
      } catch (IllegalStateException e) {
        env.throwException("java.lang.IllegalStateException", "No match available");
      } catch (Exception e) {
        env.throwException("java.lang.RuntimeException", "Error in appendReplacement: " + e.getMessage());
      }
    }
    
    return objref;
  }
  
  private String processReplacement(String replacement, Matcher matcher) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    while (i < replacement.length()) {
      char c = replacement.charAt(i);
      if (c == '$' && i + 1 < replacement.length()) {
        char next = replacement.charAt(i + 1);
        if (next >= '0' && next <= '9') {
          int groupNum = next - '0';
          i += 2;
          // greedily consume more digits as long as the group index stays valid
          while (i < replacement.length()) {
            char digit = replacement.charAt(i);
            if (digit < '0' || digit > '9') break;
            int newGroupNum = (groupNum * 10) + (digit - '0');
            if (newGroupNum > matcher.groupCount()) break;
            groupNum = newGroupNum;
            i++;
          }
          String groupValue = matcher.group(groupNum);
          if (groupValue != null) {
            result.append(groupValue);
          }
          continue;
        } else if (next == '$') {
          result.append('$');
          i += 2;
          continue;
        }
      } else if (c == '\\' && i + 1 < replacement.length()) {
        char next = replacement.charAt(i + 1);
        if (next == '\\') {
          result.append('\\');
          i += 2;
          continue;
        } else if (next == '$') {
          result.append('$');
          i += 2;
          continue;
        }
      }
      result.append(c);
      i++;
    }
    return result.toString();
  }

  @MJI
  public void cleanup____V(MJIEnv env, int objref) {
    int id = env.getIntField(objref, "id");
    matchers.remove(id);
    lastAppendPosition.remove(id);
  }
}
