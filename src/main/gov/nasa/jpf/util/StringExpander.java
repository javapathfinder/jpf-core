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


import java.util.LinkedList;
import java.util.List;

/**
 * utility class to expand regular expression like strings. We support
 * alternatives "<..|..>" and character categories "[.. X-Z ..]". Alternatives
 * can be nested, char categories can include '-' ranges
 *
 *  e.g. "a<B|X[0-1]Y>z" => aBz, aX0Yz, aX1Yz
 *
 * <2do> this is awfully connected to ESParser - we should really make it more generic
 * (this is the reason why it was moved from the general gov.nasa.jpf.util to
 * this package)
 */
public class StringExpander {

  public static final char META_CHAR = '`';  // starts a symbol

  public static final char ALT_START_CHAR = '<';
  public static final char ALT_END_CHAR   = '>';
  public static final char ALT_CHAR = '|';

  public static final char CAT_START_CHAR = '[';
  public static final char CAT_END_CHAR = ']';
  public static final char CAT_CHAR = '-';

  static class Exception extends RuntimeException {
    Exception(String details){
      super(details);
    }
  }

  static class Token {
    String value;

    Token (String value){
      this.value = value;
    }
    int length() {
      return value.length();
    }
    boolean isSymbol() {
      return false;
    }
    @Override
	public String toString(){
      return value;
    }

  }
  static class Symbol extends Token {
    Symbol (String s){
      super(s);
    }
    @Override
	boolean isSymbol(){
      return true;
    }
  }

  // our symbol tokens
  static final Symbol CAT_START = new Symbol("CAT_START");
  static final Symbol CAT_END = new Symbol("CAT_END");

  static final Symbol ALT_START = new Symbol("ALT_START");
  static final Symbol ALT_END = new Symbol("ALT_END");
  static final Symbol ALT = new Symbol("ALT");
  static final Symbol EOS = new Symbol("END");

  final String src;
  final int len;

  Token token;
  int pos;

/**
    // a quoted symbol char version - doesn't look nice, but is more general
  void nextToken () {

    int i = pos;
    int len = this.len;

    if (i>=len){
      token = EOS;

    } else {
      if (src.charAt(i) == META_CHAR){  // symbol
        char c = src.charAt(++i);
        switch (c){
          case CAT_START_CHAR:       token = CAT_START; break;
          case CAT_END_CHAR:         token = CAT_END; break;

          case ALT_START_CHAR:       token = ALT_START; break;
          case ALT_CHAR:             token = ALT; break;
          case ALT_END_CHAR:         token = ALT_END; break;
          default:
            error("illegal symbol: " + c);
        }
        pos += 2;

      } else { // string literal
        int j = i + 1;
        for (; j < len && src.charAt(j) != META_CHAR; j++);
        pos = j;
        token = new Token(src.substring(i, j));
      }
    }
  }
**/

  private boolean isMetaChar(char c){
    return ((c == CAT_START_CHAR) || (c == CAT_END_CHAR) ||
            (c == ALT_START_CHAR) || (c == ALT_END_CHAR) || (c == ALT_CHAR));

  }

  void nextToken() {
    int i = pos;
    int len = this.len;

    if (i>=len){
      token = EOS;

    } else {
      char c = src.charAt(i);
      switch (c){
        case CAT_START_CHAR:       token = CAT_START;   pos++; break;
        case CAT_END_CHAR:         token = CAT_END;     pos++; break;

        case ALT_START_CHAR:       token = ALT_START;   pos++; break;
        case ALT_CHAR:             token = ALT;         pos++; break;
        case ALT_END_CHAR:         token = ALT_END;     pos++; break;

        default:
          int j = i + 1;
          for (; j < len && !isMetaChar(src.charAt(j)); j++);
          pos = j;
          token = new Token(src.substring(i, j));
      }
    }
  }

  boolean match (Symbol sym){
    if (token == sym){
      nextToken();
      return true;
    } else {
      return false;
    }
  }

  public StringExpander (String src){
    this.src = src;
    this.len = src.length();
    this.pos = 0;
  }

  List<String> addSeq (List<String> list, List<String> seq){
    List<String> result = new LinkedList<String>();

    if (list != null && list.size() > 0){
      result.addAll(list);
    }
    result.addAll(seq);

    return result;
  }

  List<String> addLiteral (List<String> list, String s){
    List<String> result = new LinkedList<String>();

    if (list == null || list.size() == 0){
      result.add(s);

    } else {
      for (String e : list) {
        result.add(e + s);
      }
    }

    return result;
  }

  List<String> addAlt (List<String> list, List<String>alt){
    List<String> result = new LinkedList<String>();

    if (list == null || list.size() == 0){
      result.addAll(alt);

    } else {
      for (String e : list) {
        for (String p : alt) {
          result.add(e + p);
        }
      }
    }

    return result;
  }

  List<String> addCat (List<String> list, char[] cat){
    List<String> result = new LinkedList<String>();

    if (list == null || list.size() == 0){
      for (char c : cat){
        result.add(Character.toString(c));
      }

    } else {
      for (String e : list) {
        for (char c : cat){
          result.add(e + c);
        }
      }
    }

    return result;
  }

  void error (String msg){
    throw new Exception(msg);
  }

  protected char[] createCategory(String cat){
    char[] s = cat.toCharArray();
    int l1 = s.length-1;
    char[] d = s;


    for (int i=0, j=0; i<s.length; i++){
      char c = s[i];
      if ((c == CAT_CHAR) && (i>0) && (i<l1)){
        char c0 = s[i-1];
        char c1 = s[i+1];
        int  n = c1 - c0;

        int len = j + n + (s.length-i-2);
        char[] dNew = new char[len];
        System.arraycopy(d, 0, dNew, 0, j);
        d = dNew;

        for (int k=c0+1; k<=c1; k++){
          d[j++] = (char)k;
        }
        i++;

      } else {
        d[j++] = c;
      }
    }

    return d;
  }

  // seq       :=  {LIT | cat | alt}*
  // cat       :=  `[ LIT `]
  // alt       :=  `< spec {`| spec}* `>

  public List<String> expand() {
    return seq();
  }

  List<String> seq() {
    List<String> result = null;

    for (nextToken(); token != EOS; ){
      if (!token.isSymbol()){
        result = addLiteral( result,token.value);
        nextToken();

      } else if (token == ALT_START){
        result = addAlt( result, alt());

      } else if (token == CAT_START){
        result = addCat( result, cat());

      } else {
        break;
      }
    }

    return result;
  }


  List<String> alt() {
    List<String> result = null;

    assert token == ALT_START;

    do {
      result = addSeq(result, seq());
    } while (token == ALT);

    if (!match(ALT_END)){
      error("unterminated alternative");
    }
    return result;
  }

  char[] cat() {
    char[] set = null;

    assert token == CAT_START;

    nextToken();
    if (!token.isSymbol()){
      set = createCategory(token.value);
      nextToken();
    }

    if (!match(CAT_END)){
      error("unterminated category");
    }
    return set;
  }


  public static void main (String[] args) {
    //String a = "<B[0-3]C>";
    String a = args[0];
    System.out.println(a);
    
    StringExpander ex = new StringExpander(a);

/**
    for (ex.nextToken(); ex.token != EOS; ex.nextToken()){
      System.out.println(ex.token);
    }
**/
/**/
    for (String s : ex.expand()) {
      System.out.println(s);
    }
/**/
/**
    System.out.println(new String(ex.createCategory(args[0])));
**/
  }
}
