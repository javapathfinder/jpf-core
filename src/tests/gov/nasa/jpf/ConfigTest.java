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

package gov.nasa.jpf;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.util.regex.Matcher;

import org.junit.Test;


/**
 * unit test for Config
 */
public class ConfigTest extends TestJPF {

  @Test
  public void testDefaultAppPropertyInit () {

    String dir = "src/tests/gov/nasa/jpf";
    String[] args = {dir + "/configTestApp.jpf"};

    Config conf = new Config(args);

    String val = conf.getString("vm.class");
    assert "gov.nasa.jpf.vm.SingleProcessVM".equals(val);

    val = conf.getString("target"); // from configTest.jpf
    assert "urgh.org.MySystemUnderTest".equals(val);

    // that's testing key expansion and the builtin "config_path"
    val = conf.getString("mySUT.location");
    
    assert val != null;
    
    if (!File.separator.equals("/"))
       dir = dir.replaceAll("/", Matcher.quoteReplacement(File.separator));  // On UNIX Config returns / and on Windows Config returns \\

    assert val.endsWith(dir);
  }

  @Test
  public void testDefaultExplicitTargetInit ()  {
    String[] args = {"urgh.org.MySystemUnderTest"};

    Config conf = new Config( args);
    String[] freeArgs = conf.getFreeArgs();
    assertTrue( freeArgs.length == 1);
    assertTrue( "urgh.org.MySystemUnderTest".equals(freeArgs[0]));
  }

  @Test
  public void testExplicitLocations () {
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = {"+site=" + dir + "configTestSite.properties",
                     "+app=" + dir + "configTestApp.jpf" };

    Config conf = new Config( args);
    conf.printEntries();

    assert "urgh.org.MySystemUnderTest".equals(conf.getString("target"));
  }

  @Test
  public void testTargetArgsOverride () {

    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = { dir + "configTestApp.jpf",
                      "x", "y"};

    Config conf = new Config(args);
    conf.printEntries();

    String[] ta = conf.getStringArray("target.args");
    assert ta != null;
    assert ta.length == 3;
    assert "a".equals(ta[0]);
    assert "b".equals(ta[1]);
    assert "c".equals(ta[2]);
    
    String[] freeArgs = conf.getFreeArgs();
    assert freeArgs != null;
    assert freeArgs.length == 2;
    assert "x".equals(freeArgs[0]);
    assert "y".equals(freeArgs[1]);
  }

  @Test
  public void testClassPaths () {
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = {"+site=" + dir + "configTestSite.properties",
                     "+app=" + dir + "configTestApp.jpf" };

    Config conf = new Config( args);
    conf.printEntries();

    // those properties are very weak!
    String[] bootCpEntries = conf.asStringArray("boot_classpath");
    assert bootCpEntries.length > 0;

    String[] nativeCpEntries = conf.asStringArray("native_classpath");
    assert nativeCpEntries.length > 0;
  }

  @Test
  public void testRequiresOk () {
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = { "+site=" + dir + "configTestSite.properties",
                      dir + "configTestRequires.jpf" };

    Config.enableLogging(true);
    Config conf = new Config( args);
    String v = conf.getString("whoa");
    System.out.println("got whoa = " + v);
    
    assert (v != null) && v.equals("boa");
  }

  @Test
  public void testRequiresFail () {
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = { "+site=" + dir + "configTestSite.properties",
                      dir + "configTestRequiresFail.jpf" };

    Config.enableLogging(true);
    Config conf = new Config( args);
    String v = conf.getString("whoa");
    System.out.println("got whoa = " + v);

    assert (v == null);
  }

  @Test
  public void testIncludes () {
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = { "+site=" + dir + "configTestSite.properties",
                      dir + "configTestIncludes.jpf" };

    Config.enableLogging(true);
    Config conf = new Config( args);
    String v = conf.getString("my.common");
    System.out.println("got my.common = " + v);

    assert (v != null) && v.equals("whatever");
  }

  @Test
  public void testIntArray(){
    String dir = "src/tests/gov/nasa/jpf/";
    String[] args = { "+site=" + dir + "configTestSite.properties",
                      "+arr=-42,0xff,0" };

    Config.enableLogging(true);
    Config conf = new Config( args);
    int[] a = conf.getIntArray("arr");
    
    assertTrue(a != null);
    assertTrue(a.length == 3);
    assertTrue(a[0] == -42 && a[1] == 0xff && a[2] == 0);
    
  }
}
