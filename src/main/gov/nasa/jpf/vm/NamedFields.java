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

/**
 * value container for non-array classes
 */
public class NamedFields extends Fields {

  /** this is where we store the instance data. Since field types are
   * heterogenous, we have to map everything into int
   */
  protected int[] values;

  public NamedFields (int dataSize) {
    values = new int[dataSize];
  }

  @Override
  public int[] asFieldSlots() {
    return values;
  }

  /**
   * give an approximation of the heap size in bytes - we assume fields are word
   * aligned, hence the number of values*4 should be good. Note that this is
   * overridden by ArrayFields (arrays would be packed)
   */
  @Override
  public int getHeapSize () {
    return values.length*4;
  }

  // our low level getters and setters
  @Override
  public int getIntValue (int index) {
    return values[index];
  }

  public boolean isEqual(Fields o, int off, int len, int otherOff) {
    if (o instanceof NamedFields) {
      NamedFields other = (NamedFields) o;
      int iEnd = off + len;
      int jEnd = otherOff + len;
      int[] v = other.values;

      if ((iEnd > values.length) || (jEnd > v.length)) {
        return false;
      }

      for (int i = off, j = otherOff; i < iEnd; i++, j++) {
        if (values[i] != v[j]) {
          return false;
        }
      }

      return true;
    } else {
      return false;
    }
  }

  // same as above, just here to make intentions clear
  @Override
  public int getReferenceValue (int index) {
    return values[index];
  }

  @Override
  public long getLongValue (int index) {
    return Types.intsToLong(values[index + 1], values[index]);
  }

  @Override
  public boolean getBooleanValue (int index) {
    return Types.intToBoolean(values[index]);
  }

  @Override
  public byte getByteValue (int index) {
    return (byte) values[index];
  }

  @Override
  public char getCharValue (int index) {
    return (char) values[index];
  }

  @Override
  public short getShortValue (int index) {
    return (short) values[index];
  }

  // <2do> get rid of it!! this is only for internal use, to increase efficiency
  public int[] getValues() {
    return values;
  }

  //--- the field modifier methods (both instance and static)

  @Override
  public void setReferenceValue (int index, int newValue) {
    values[index] = newValue;
  }

  @Override
  public void setBooleanValue (int index, boolean newValue) {
    values[index] = newValue ? 1 : 0;
  }

  @Override
  public void setByteValue (int index, byte newValue) {
    values[index] = newValue;
  }

  @Override
  public void setCharValue (int index, char newValue) {
    values[index] = newValue;
  }

  @Override
  public void setShortValue (int index, short newValue) {
    values[index] = newValue;
  }

  @Override
  public void setFloatValue (int index, float newValue) {
    values[index] = Types.floatToInt(newValue);
  }

  @Override
  public void setIntValue (int index, int newValue) {
    values[index] = newValue;
  }

  @Override
  public void setLongValue (int index, long newValue) {
		values[index++] = Types.hiLong(newValue);
    values[index] = Types.loLong(newValue);
  }

  @Override
  public void setDoubleValue (int index, double newValue) {
    values[index++] = Types.hiDouble(newValue);
    values[index] = Types.loDouble(newValue);
  }


  @Override
  public float getFloatValue (int index) {
    return Types.intToFloat(values[index]);
  }

  @Override
  public double getDoubleValue (int index) {
    return Types.intsToDouble( values[index+1], values[index]);
  }

  /**
   * Creates a clone.
   */
  @Override
  public NamedFields clone () {
    NamedFields f = (NamedFields) cloneFields();
    f.values = values.clone();
    return f;
  }

  /**
   * Checks for equality.
   */
  @Override
  public boolean equals (Object o) {
    if (o instanceof NamedFields) {
      NamedFields other = (NamedFields) o;

      //--- check values
      int[] v1 = values;
      int[] v2 = other.values;
      int l = v1.length;
      if (l != v2.length) {
        return false;
      }
      for (int i = 0; i < l; i++) {
        if (v1[i] != v2[i]) {
          return false;
        }
      }
      
      return super.compareAttrs(other);

    } else {
      return false;
    }
  }

  // serialization interface
  @Override
  public void appendTo(IntVector v) {
    v.append(values);
  }


  /**
   * Adds some data to the computation of an hashcode.
   */
  @Override
  public void hash (HashData hd) {
    int[] v = values;
    for (int i=0, l=v.length; i < l; i++) {
      hd.add(v[i]);
    }
  }

  /**
   * Size of the fields.
   */
  public int size () {
    return values.length;
  }

  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder("NamedFields[");

    sb.append("values=");
    sb.append('[');

    for (int i = 0; i < values.length; i++) {
      if (i != 0) {
        sb.append(',');
      }

      sb.append(values[i]);
    }

    sb.append(']');
    sb.append(',');

    sb.append(']');

    return sb.toString();
  }

  // <2do> replace with copyTo() !!
  public int[] getRawValues() {
    return values;
  }

  public void copyFrom(Fields other) {
    System.arraycopy(((NamedFields)other).values, 0, this.values, 0, values.length);
    super.copyAttrs(other);
  }

}
