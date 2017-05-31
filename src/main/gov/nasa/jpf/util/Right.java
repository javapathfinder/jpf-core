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

import java.util.Arrays;

/**
 * right justified output
 * <2do> this is not worth a class! use a general TextFormatter
 */

public class Right {
  public static String format (String value, int spaces, char ch) {
    int vlen = value.length();
    int newLen = Math.max(spaces, vlen);
    char[] result = new char[newLen];
    int pivot = newLen - vlen;
    value.getChars(0, vlen, result, pivot);
    Arrays.fill(result, 0, pivot, ch);
    return new String(result);
  }

  public static String format (String value, int spaces) {
    return format(value, spaces, ' ');
  }

  public static String format (int value, int digits) {
    return format(Integer.toString(value),digits,' ');
  }
}
