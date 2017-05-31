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

public class ExpGrowth implements Growth {
  final float factor;
  final float sqFactor;
  final int plus;
  
  public ExpGrowth(float factor, int plus) {
    if (factor < 1.001F || factor > 100.F) {
      throw new IllegalArgumentException();
    }
    this.factor = factor;
    this.sqFactor = (float) Math.sqrt(factor);
    this.plus = plus;
  }
  
  public ExpGrowth(float factor) {
    this(factor,7);
  }
  
  @Override
  public int grow(int oldSize, int minNewSize) {
    int newSize = (int)(factor * oldSize) + plus;
    if (newSize < minNewSize) {
      newSize = (int)(sqFactor * minNewSize) + plus;
    }
    return newSize;
  }
}
