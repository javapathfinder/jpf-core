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

import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.IntVector;

import java.io.PrintStream;

/**
 * element values for double[] objects
 */
public class DoubleArrayFields extends ArrayFields {

  double[] values;

  public DoubleArrayFields (int length) {
    values = new double[length];
  }

  @Override
  public double[] asDoubleArray() {
    return values;
  }

  @Override
  public void copyElements (ArrayFields src, int srcPos, int dstPos, int len){
    DoubleArrayFields a = (DoubleArrayFields) src;
    System.arraycopy(a.values, srcPos, values, dstPos, len);
  }

  @Override
  protected void printValue(PrintStream ps, int idx){
    ps.print(values[idx]);
  }
  
  @Override
  public Object getValues(){
    return values;
  }

  @Override
  public int arrayLength() {
    return values.length;
  }

  @Override
  public int getHeapSize() {  // in bytes
    return values.length * 8;
  }

  @Override
  public void appendTo (IntVector v) {
    v.appendRawBits(values);
  }

  @Override
  public DoubleArrayFields clone(){
    DoubleArrayFields f = (DoubleArrayFields)cloneFields();
    f.values = values.clone();
    return f;
  }

  @Override
  public boolean equals (Object o) {
    if (o instanceof DoubleArrayFields) {
      DoubleArrayFields other = (DoubleArrayFields)o;

      double[] v = values;
      double[] vOther = other.values;
      if (v.length != vOther.length) {
        return false;
      }

      for (int i=0; i<v.length; i++) {
        if (v[i] != vOther[i]) {
          return false;
        }
      }

      return compareAttrs(other);

    } else {
      return false;
    }
  }

  @Override
  public void setDoubleValue (int pos, double newValue) {
    values[pos] = newValue;
  }

  @Override
  public double getDoubleValue (int pos) {
    return values[pos];
  }


  @Override
  public void hash(HashData hd) {
    double[] v = values;
    for (int i=0; i < v.length; i++) {
      hd.add(v[i]);
    }
  }

}
