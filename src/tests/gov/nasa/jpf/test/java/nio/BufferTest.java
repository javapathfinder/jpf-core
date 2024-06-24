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
package gov.nasa.jpf.test.java.nio;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class BufferTest extends TestJPF {

  /**
   * This test case checks to see if the missing
   * class java.nio.Buffer.<init>(IIII)V issue
   * is resolved and fails otherwise
   */
  @Test
  public void testByteBufferConstructor() {
    if (verifyNoPropertyViolation()) {
      byte[] bytes1 = "Buffer".getBytes(StandardCharsets.UTF_8);
      byte[] bytes2 = "testBuffer".getBytes(StandardCharsets.UTF_8);

      ByteBuffer buffer1 = ByteBuffer.wrap(bytes1);
      ByteBuffer buffer2 = ByteBuffer.wrap(bytes2);
      buffer2.position(4);

      assertTrue(buffer1.equals(buffer2));
    }
  }

  @Test
  public void testCharBufferConstructorJapanese(){
    if(verifyNoPropertyViolation()){
    String utf8String = "Hello, 世界";
    byte[] utf8Bytes = utf8String.getBytes(StandardCharsets.UTF_8);

    String decodedUtf8FromUtf8 = decodeWithScanner(utf8Bytes, StandardCharsets.UTF_8);
    String decodedLatin1FromUtf8 = decodeWithScanner(utf8Bytes, StandardCharsets.ISO_8859_1);

    byte[] utf8FromUtf8 = decodedUtf8FromUtf8.getBytes(StandardCharsets.UTF_8);
    byte[] latin1FromUtf8 = decodedLatin1FromUtf8.getBytes(StandardCharsets.ISO_8859_1);

    assertTrue(("Assertion failed. Original: " + utf8String + ", Decoded: " + decodedUtf8FromUtf8),utf8FromUtf8.equals(utf8Bytes));
    assertFalse(("Assertion failed. Original: " + utf8String + ", Decoded: " + decodedLatin1FromUtf8),latin1FromUtf8.equals(utf8Bytes));
    }
  }

  @Test
  public void testCharBufferConstructorSwedishEncodingWithLATIN1() {
    if (verifyNoPropertyViolation()) {
      String latin1String = "Hello, Åland";

      byte[] latin1BytesFromLatin1String = latin1String.getBytes(StandardCharsets.ISO_8859_1);

      String decodedUtf8FromLatin1 = decodeWithScanner(latin1BytesFromLatin1String, StandardCharsets.UTF_8);
      String decodedLatin1FromLatin1 = decodeWithScanner(latin1BytesFromLatin1String, StandardCharsets.ISO_8859_1);

      byte[] utf8FromLatin1 = decodedUtf8FromLatin1.getBytes(StandardCharsets.UTF_8);
      byte[] latin1FromLatin1 = decodedLatin1FromLatin1.getBytes(StandardCharsets.ISO_8859_1);

      assertTrue(("Assertion failed. Original: " + latin1String + ", Decoded: " + decodedUtf8FromLatin1),utf8FromLatin1.equals(latin1BytesFromLatin1String));
      assertFalse(("Assertion failed. Original: " + latin1String + ", Decoded: " + decodedLatin1FromLatin1),latin1FromLatin1.equals(latin1BytesFromLatin1String));

    }
  }

  @Test
  public void testCharBufferConstructorSwedishEncodingWithUTF(){
    if(verifyNoPropertyViolation()){
      String latin1String = "Hello, Åland";

      byte[] utf8BytesFromLatin1String = latin1String.getBytes(StandardCharsets.UTF_8);

      String decodedUtf8FromUtf8 = decodeWithScanner(utf8BytesFromLatin1String, StandardCharsets.UTF_8);
      String decodedLatin1FromUtf8 = decodeWithScanner(utf8BytesFromLatin1String, StandardCharsets.ISO_8859_1);

      byte[] utf8FromUtf8 = decodedUtf8FromUtf8.getBytes(StandardCharsets.UTF_8);
      byte[] latin1FromUtf8 = decodedLatin1FromUtf8.getBytes(StandardCharsets.UTF_8);

      assertTrue(("Assertion failed. Original: " + latin1String + ", Decoded: " + decodedUtf8FromUtf8),utf8FromUtf8.equals(utf8BytesFromLatin1String));
      assertFalse(("Assertion failed. Original: " + latin1String + ", Decoded: " + decodedLatin1FromUtf8),latin1FromUtf8.equals(utf8BytesFromLatin1String));
    }
  }

  private static String decodeWithScanner(byte[] bytes, Charset charset) {
    Scanner scanner = new Scanner(new String(bytes, charset));
    StringBuilder result = new StringBuilder();
    while (scanner.hasNextLine()) {
      result.append(scanner.nextLine());
    }
    return result.toString();
  }

}
