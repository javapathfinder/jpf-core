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
package gov.nasa.jpf.util.json;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.StringReader;

import org.junit.Test;


/**
 *
 * @author Ivan Mushketik
 */
public class JSONLexerTest extends TestJPF {
  @Test
  public void testEmptyObject() {
    String json = "{}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)};

    assertTokenSequence(expectedTokens, lexer);
  }

  private void assertTokenSequence(Token[] expectedTokens, JSONLexer lexer) {
    int i = 0;
    for (; i < expectedTokens.length; i++) {
      Token t = lexer.getNextToken();
      assert t.equals(expectedTokens[i]);
    }

    assert i == expectedTokens.length;
    assert lexer.hasMore() == false;
  }

  @Test
  public void testKeyIdObject() {
    String json = "{ "
            + "\"key1\" : true"
            + "\"key2\" : false"
            + "}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.String, "key1"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Identificator, "true"),
      new Token(Token.Type.String, "key2"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Identificator, "false"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)};

    assertTokenSequence(expectedTokens, lexer);
  }

  @Test
  public void testKeyIdObjectWithEscapes() {
    String json = "{ "
            + "\"key\\t\\n\\\\ \" : true"
            + "\"key\\u1234\" : false"
            + "}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.String, "key\t\n\\ "),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Identificator, "true"),
      new Token(Token.Type.String, "key\u1234"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Identificator, "false"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)};

    assertTokenSequence(expectedTokens, lexer);
  }

  @Test
  public void testKeyIdObjectWithNumbers() {
    String json = "{ "
            + "\"key1\" : -123.234e3"
            + "\"key2\" : 1.4"
            + "\"key3\" : 132"
            + "}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.String, "key1"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Number, "-123.234e3"),
      new Token(Token.Type.String, "key2"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Number, "1.4"),
      new Token(Token.Type.String, "key3"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Number, "132"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)};

    assertTokenSequence(expectedTokens, lexer);
  }

  @Test
  public void testArrayTokens() {
    String json = "{ "
            + "[ ]"
            + "}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.ArrayStart, "["),
      new Token(Token.Type.ArrayEnd, "]"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)
    };

    assertTokenSequence(expectedTokens, lexer);
  }

  @Test
  public void testCGCallTokens() {
    String json = "{ "
            + "\"i\" : MyCG(1, \"str\")"
            + "}";
    JSONLexer lexer = new JSONLexer(new StringReader(json));

    Token expectedTokens[] = {
      new Token(Token.Type.ObjectStart, "{"),
      new Token(Token.Type.String, "i"),
      new Token(Token.Type.KeyValueSeparator, ":"),
      new Token(Token.Type.Identificator, "MyCG"),
      new Token(Token.Type.CGCallParamsStart, "("),
      new Token(Token.Type.Number, "1"),
      new Token(Token.Type.Comma, ","),
      new Token(Token.Type.String, "str"),
      new Token(Token.Type.CGCallParamsEnd, ")"),
      new Token(Token.Type.ObjectEnd, "}"),
      new Token(Token.Type.DocumentEnd, null)
    };

    assertTokenSequence(expectedTokens, lexer);
  }
}
