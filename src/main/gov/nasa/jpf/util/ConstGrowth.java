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
/**
 * 
 */
package gov.nasa.jpf.util;

public class ConstGrowth implements Growth {
  final int v;
  public ConstGrowth(int v) {
    if (v < 1 || v > 1000000000) {
      throw new IllegalArgumentException();
    }
    this.v = v;
  }
  
  @Override
  public int grow(int oldSize, int minNewSize) {
    int newSize = oldSize + v;
    if (newSize < minNewSize) {
      newSize = minNewSize + (v >> 1);
    }
    return newSize;
  }
}
