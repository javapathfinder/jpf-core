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

import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class JSONParserTest extends TestJPF {

  @Test
  public void testOneLevelJSON() {
    String json = "{"
            + "\"key1\" : \"str\","
            + "\"key2\" : 123"
            + "}";
    JSONObject o = parseJSON(json);

    Value key1 = o.getValue("key1");
    assert key1.getString().equals("str");
    Value key2 = o.getValue("key2");
    assert key2.getDouble() == 123;
  }

  @Test
  public void testEmptyObject() {
    String json = "{}";
    JSONObject o = parseJSON(json);

    assert o.getValue("noValue") == null;
  }

  @Test
  public void testArrayParse() {
    String json = "{"
            + "\"key\" : ["
            + "{ \"key1\" : 123 },"
            + "{ \"key2\" : \"str\" } ]"
            + "}";
    JSONObject o = parseJSON(json);

    Value objects[] = o.getValue("key").getArray();
    assert objects[0].getObject().getValue("key1").getDouble() == 123;
    assert objects[1].getObject().getValue("key2").getString().equals("str") == true;
  }

  @Test
  public void testEmptyArray() {
    String json = "{ \"emptyArr\" : [] }";
    JSONObject o = parseJSON(json);

    assert o.getValue("noArray") == null;
  }

  @Test
  public void testIdentifacatorsParsing() {
    String json = "{ "
            + "\"Null\" : null,"
            + "\"True\" : true,"
            + "\"False\": false"
            + "}";
    JSONObject o = parseJSON(json);

    assert o.getValue("Null").getString() == null;
    assert o.getValue("True").getBoolean() == true;
    assert o.getValue("False").getBoolean() == false;
  }

  @Test
  public void testCGCallParsing() {
    String json = "{"
            + "\"id\" : CGName(1, \"str\", true, false, null)"
            + "}";

    JSONObject o = parseJSON(json);
    CGCall cgCall = o.getCGCall("id");

    assert cgCall.getName().equals("CGName") == true;
    Value params[] = cgCall.getValues();

    assertEquals(1d, params[0].getDouble(), 0.0001);
    assert params[1].getString().equals("str") == true;
    assert params[2].getBoolean() == true;
    assert params[3].getBoolean() == false;
    assert params[4].getDouble() == null;
  }

  private JSONObject parseJSON(String json) {
    JSONLexer lexer = new JSONLexer(json);
    JSONParser parser = new JSONParser(lexer);
    JSONObject o = parser.parse();
    return o;
  }
}
