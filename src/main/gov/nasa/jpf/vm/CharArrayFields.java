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
 * element values for char[] objects
 */
public class CharArrayFields extends ArrayFields {

  char[] values;

  public CharArrayFields (int length) {
    values = new char[length];
  }

  @Override
  public char[] asCharArray(){
    return values;
  }

  @Override
  public void copyElements (ArrayFields src, int srcPos, int dstPos, int len){
    CharArrayFields a = (CharArrayFields) src;
    System.arraycopy(a.values, srcPos, values, dstPos, len);
  }
  
  @Override
  protected void printValue(PrintStream ps, int idx){
    PrintUtils.printCharLiteral(ps, values[idx]);
  }
  
  @Override
  public void printElements( PrintStream ps, int max){
    PrintUtils.printStringLiteral(ps, values, max);
  }  
  
  @Override
  public char[] asCharArray (int offset, int length) {
    char[] result = new char[length];
    System.arraycopy(values, offset, result, 0, length);

    return result;
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
    return values.length * 2;
  }

  @Override
  public void appendTo (IntVector v) {
    v.appendPacked(values);
  }

  @Override
  public CharArrayFields clone(){
    CharArrayFields f = (CharArrayFields)cloneFields();
    f.values = values.clone();
    return f;
  }


  @Override
  public boolean equals (Object o) {
    if (o instanceof CharArrayFields) {
      CharArrayFields other = (CharArrayFields)o;

      char[] v = values;
      char[] vOther = other.values;
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
  public char getCharValue(int pos) {
    return values[pos];
  }

  @Override
  public void setCharValue(int pos, char newValue) {
    values[pos] = newValue;
  }

  public void setCharValues(char[] v){
    System.arraycopy(v,0,values,0,v.length);
  }

  //--- some methods to ease native String operations

  public String asString(int offset, int length) {
    return new String(values, offset, length);
  }

  // a special string compare utility
  public boolean equals (int offset, int length, String s) {
    char[] v = values;

    if (offset+length > v.length) {
      return false;
    }

    for (int i=offset, j=0; j<length; i++, j++) {
      if (v[i] != s.charAt(j)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void hash(HashData hd) {
    char[] v = values;
    for (int i=0; i < v.length; i++) {
      hd.add(v[i]);
    }
  }

}
