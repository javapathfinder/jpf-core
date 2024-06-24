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
package java.nio;

public class CharBuffer extends Buffer {

  char[] array;
  int offset;

  CharBuffer(int mark, int pos, int lim, int cap, char[] hb, int offset) {
    super(mark, pos, lim, cap);
    this.array = hb;
    this.offset = offset;
  }

  CharBuffer(int mark, int pos, int lim, int cap) {
    this(mark, pos, lim, cap, null, 0);
  }

  public static CharBuffer allocate(int i) {
    if (i < 0) {
      throw new IllegalArgumentException();
    }
    CharBuffer newBuffer = new CharBuffer(-1, 0, i, i, new char[i], 0);
    return newBuffer;
  }

  public CharBuffer put(char c) {
    if (position >= limit) {
      throw new BufferOverflowException();
    }
    array[position] = c;
    position++;
    return this;
  }

  public CharBuffer put(int i, char c) {
    if ((i < 0) || (i >= limit)) {
      throw new IndexOutOfBoundsException();
    }
    array[i] = c;
    return this;
  }

  public CharBuffer put(String src, int start, int end) {
    if(end - start > remaining()) {
      throw new BufferOverflowException();
    }
    for(int i=start; i<end; i++) {
      return this.put(src.charAt(i));
    }
    return this;
  }

  public final CharBuffer put(String src) {
    return put(src, 0, src.length());
  }

  public char get() {
    if (position >= limit) {
      throw new BufferUnderflowException();
    }
    position++;
    return array[position-1];
  }

  public static CharBuffer wrap(CharSequence outMess) {
    CharBuffer charBuffer = new CharBuffer(-1, 0, outMess.length(), outMess.length(), new char[outMess.length()], 0);
    charBuffer.clear();
    for(int i=0; i< outMess.length(); i++) {
      charBuffer.put(outMess.charAt(i));
    }
    charBuffer.flip();
    return charBuffer;
  }

  @Override
  public char[] array() {
    return array;
  }

  @Override
  public boolean hasArray() {
    return true;
  }

  public final int length() {
    return remaining();
  }

  @Override
  public final CharBuffer limit(int newLimit) {
    super.limit(newLimit);
    return this;
  }

  @Override
  public final CharBuffer flip() {
    super.flip();
    return this;
  }

  @Override
  public CharBuffer position(int newPosition){
    super.position(newPosition);
    return this;
  }

}
