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

package gov.nasa.jpf.test.xerces;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * regression test for parsing xml files with Apache xerces
 * The data files are shamelessly lifted from Checkstyle 5.3, to use some
 * real input data
 */
public class SAXParserTest extends TestJPF {

  @Test
  public void testSimpleParse() throws ParserConfigurationException, SAXException, IOException  {

    if (verifyNoPropertyViolation(
            "+http.connection=http://*.dtd -- gov.nasa.jpf.CachedROHttpConnection",
            "+http.cache_dir=src/tests/gov/nasa/jpf/test/xerces",
            "+log.info=http")){
      String pathName = "src/tests/gov/nasa/jpf/test/xerces/sun_checks.xml";

      DefaultHandler handler = new DefaultHandler();

      XMLReader mParser;
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(true);
      factory.setNamespaceAware(true);
      mParser = factory.newSAXParser().getXMLReader();
      mParser.setContentHandler(handler);
      mParser.setEntityResolver(handler);
      mParser.setErrorHandler(handler);

      mParser.parse(pathName);
    }
  }
}
