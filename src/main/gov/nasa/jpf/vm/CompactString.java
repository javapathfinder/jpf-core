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

import java.nio.ByteOrder;

/**
 * Converts between host Strings and the value/coder pair of the java.lang.String
 * model (JEP 254 compact strings): Latin-1 bytes when every char fits in a byte,
 * otherwise UTF-16 in the byte order StringUTF16 uses (see JPF_java_lang_StringUTF16).
 * Using the platform default charset instead would turn chars it cannot encode into '?'.
 */
public final class CompactString {

  public static final byte LATIN1 = 0;
  public static final byte UTF16 = 1;

  private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

  private CompactString() {}

  public static byte coder (String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) > 0xFF) {
        return UTF16;
      }
    }
    return LATIN1;
  }

  public static byte[] encode (String s, byte coder) {
    int n = s.length();
    if (coder == LATIN1) {
      byte[] value = new byte[n];
      for (int i = 0; i < n; i++) {
        value[i] = (byte) s.charAt(i);
      }
      return value;
    }

    byte[] value = new byte[n << 1];
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      byte hi = (byte) (c >> 8);
      byte lo = (byte) c;
      value[2 * i] = BIG_ENDIAN ? hi : lo;
      value[2 * i + 1] = BIG_ENDIAN ? lo : hi;
    }
    return value;
  }

  public static String decode (byte[] value, byte coder) {
    if (coder == LATIN1) {
      char[] chars = new char[value.length];
      for (int i = 0; i < value.length; i++) {
        chars[i] = (char) (value[i] & 0xFF);
      }
      return new String(chars);
    }

    char[] chars = new char[value.length >> 1];
    for (int i = 0; i < chars.length; i++) {
      int b0 = value[2 * i] & 0xFF;
      int b1 = value[2 * i + 1] & 0xFF;
      chars[i] = (char) (BIG_ENDIAN ? (b0 << 8 | b1) : (b1 << 8 | b0));
    }
    return new String(chars);
  }
}
