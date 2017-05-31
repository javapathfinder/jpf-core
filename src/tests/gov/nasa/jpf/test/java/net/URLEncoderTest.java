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

package gov.nasa.jpf.test.java.net;

import gov.nasa.jpf.util.test.TestJPF;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Test;


/**
 * regression test for URLEncoder/Decoder
 */
public class URLEncoderTest extends TestJPF {

  /************************** test methods ************************/
  @Test
  public void testEncodeCycle (){
    if (verifyNoPropertyViolation()){
      String s = "< what a mess >";
      String enc = "UTF-8";

      try {
        System.out.println("original: " + s);
        String e = URLEncoder.encode(s, enc);
        System.out.println("encoded:  " + e);
        String d = URLDecoder.decode(e,enc);
        System.out.println("decoded:  " + d);

        assert s.equals(d) : "encode/decode roundtrip failed";

      } catch (Throwable t){
        fail("unexpected exception: " + t);
      }
    }
  }

  @Test
  public void testEncodingException (){
    if (verifyNoPropertyViolation()){
      String s = "< what a mess >";
      String enc = "wrgsGrff";

      try {
        System.out.println("original: " + s);
        String e = URLEncoder.encode(s, enc);
        System.out.println("encoded:  " + e);

        fail("this is really not a known encoding: " + enc);

      } catch (java.io.UnsupportedEncodingException x){
        System.out.println("rightfully throws " + x);
      }
    }
  }
}
