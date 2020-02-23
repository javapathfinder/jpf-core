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
import gov.nasa.jpf.util.PrintUtils;

import java.io.PrintStream;

/**
 * element values for reference array objects
 * (references are stored as int's)
 */
public class ReferenceArrayFields extends ArrayFields {

  int[] values; // the references

  public ReferenceArrayFields (int length) {
    values = new int[length];

    /** not required for MJIEnv.NULL = 0
    for (int i=0; i<length; i++) {
      values[i] = MJIEnv.NULL;
    }
    **/
  }

  @Override
  public int[] asReferenceArray() {
    return values;
  }
  
  @Override
  public void copyElements (ArrayFields src, int srcPos, int dstPos, int len){
    ReferenceArrayFields a = (ReferenceArrayFields) src;
    System.arraycopy(a.values, srcPos, values, dstPos, len);
  }

  @Override
  protected void printValue(PrintStream ps, int idx){
    PrintUtils.printReference(ps, values[idx]);
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
  public boolean isReferenceArray() {
    return true;
  }

  @Override
  public int getHeapSize() {
    return values.length * 4;
  }

  @Override
  public void appendTo (IntVector v) {
    v.append(values);
  }

  @Override
  public ReferenceArrayFields clone(){
    ReferenceArrayFields f = (ReferenceArrayFields)cloneFields();
    f.values = values.clone();
    return f;
  }


  @Override
  public boolean equals(Object o) {
    if (o instanceof ReferenceArrayFields) {
      ReferenceArrayFields other = (ReferenceArrayFields)o;

      int[] v = values;
      int[] vOther = other.values;
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
  public void setReferenceValue(int pos, int newValue) {
    values[pos] = newValue;
  }

  @Override
  public int getReferenceValue(int pos) {
    return values[pos];
  }

  @Override
  public void hash (HashData hd) {
    int[] v = values;
    for (int i=0; i < v.length; i++) {
      hd.add(v[i]);
    }
  }


}
