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
 * element values for byte[] objects
 */
public class ByteArrayFields extends ArrayFields {

  byte[] values;

  public ByteArrayFields (int length) {
    values = new byte[length];
  }

  @Override
  public byte[] asByteArray() {
    return values;
  }

  @Override
  public void copyElements (ArrayFields src, int srcPos, int dstPos, int len){
    ByteArrayFields a = (ByteArrayFields) src;
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
  public int getHeapSize() {
    return values.length;
  }

  @Override
  public boolean equals (Object o) {
    if (o instanceof ByteArrayFields) {
      ByteArrayFields other = (ByteArrayFields)o;

      byte[] v = values;
      byte[] vOther = other.values;
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
  public ByteArrayFields clone(){
    ByteArrayFields f = (ByteArrayFields)cloneFields();
    f.values = values.clone();
    return f;
  }

  @Override
  public void setByteValue (int pos, byte b) {
    values[pos] = b;
  }

  @Override
  public byte getByteValue (int pos) {
    return values[pos];
  }

  @Override
  public void appendTo (IntVector v) {
    v.appendPacked(values);
  }

  @Override
  public void hash(HashData hd) {
    byte[] v = values;
    for (int i=0; i < v.length; i++) {
      hd.add(v[i]);
    }
  }

}
