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

import gov.nasa.jpf.annotation.MJI;

import java.util.HashMap;

import gov.nasa.jpf.Config;

/**
 * MJI NativePeer class for java.io.RandomAccessFile library abstraction
 *
 * @author Owen O'Malley
 */
public class JPF_java_io_RandomAccessFile extends NativePeer {

	// need to see whether the file is already in use
	// if so, then we'll update the file data and length in the original file
	// we do update the length in the local object, but not the data
		
	static HashMap<Integer, Integer> File2DataMap;
	
  public static boolean init (Config conf) {
    File2DataMap = new HashMap<Integer, Integer>();
    return (File2DataMap != null);
  } 

	// get the mapped object if one exists
	private static int getMapping(MJIEnv env, int this_ptr) {
		int fn_ptr = env.getReferenceField(this_ptr,"filename");
		Object o = File2DataMap.get(new Integer(fn_ptr));
		if (o == null)
			return this_ptr;
		return ((Integer)o).intValue();
	}
	
	// set the mapping during the constructor call
  @MJI
	public void setDataMap____V (MJIEnv env, int this_ptr) {
		int fn_ptr = env.getReferenceField(this_ptr,"filename");
		if (!File2DataMap.containsKey(new Integer(fn_ptr))) 
			File2DataMap.put(new Integer(fn_ptr),new Integer(this_ptr));
	}
	
  static ClassInfo getDataRepresentationClassInfo (MJIEnv env) {
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo(DataRepresentation);
    if (ci.initializeClass(ti)){
      env.repeatInvocation();
      return null;
    }
    
    return ci;
  }

  @MJI
  public void writeByte__I__V (MJIEnv env, int this_ptr, int data) {
    
    
    long current_posn = env.getLongField(this_ptr, current_position);
    long current_len = env.getLongField(this_ptr, current_length);
    int chunk_size = env.getStaticIntField(RandomAccessFile, CHUNK_SIZE);
    int chunk = findDataChunk(env, this_ptr, current_posn,
                              chunk_size);
    setDataValue(env, chunk, current_posn, (byte) data, chunk_size);
    current_posn += 1;
    env.setLongField(this_ptr, current_position, current_posn);
    if (current_posn >= current_len) {
      env.setLongField(this_ptr, current_length, current_posn + 1);
      // update length in the mapped object if it exists
      env.setLongField(getMapping(env,this_ptr), current_length, current_posn + 1);
    }
  }

  /**
   * This is a bit lame doing it this way, but it is easy.
   */
  @MJI
  public void write___3BII__V (MJIEnv env, int this_ptr, int data_array,
                           int start, int len) {
    byte[] data_values = env.getByteArrayObject(data_array);
    for(int i=start; i < len; ++i) {
      writeByte__I__V(env, this_ptr, data_values[i]);
    }
  }

  @MJI
  public void setLength__J__V(MJIEnv env, int this_ptr, long len) {
    long current_posn = env.getLongField(this_ptr, current_position);
    long current_len = env.getLongField(this_ptr, current_length);
    if (current_posn >= len && len < current_len) {
      env.setLongField(this_ptr, current_position, len);
    }
    env.setLongField(this_ptr, current_length, len);
    // update length in the mapped object if it exists
    env.setLongField(getMapping(env,this_ptr), current_length, current_posn + 1);
  }

  @MJI
  public int read___3BII__I (MJIEnv env, int this_ptr, int data_array,
                         int start, int len) {
    int i = 0;
    long current_posn = env.getLongField(this_ptr, current_position);
    long current_len = env.getLongField(this_ptr, current_length);
    while (i < len && current_posn < current_len) {
      env.setByteArrayElement(data_array, start + i, readByte____B(env, this_ptr));
      i += 1;
      current_posn += 1;
    }
    if (i == 0) {
      return -1;
    }
    return i;
  }

  @MJI
  public byte readByte____B (MJIEnv env, int this_ptr) {
    long current_posn = env.getLongField(this_ptr, current_position);
    long current_len = env.getLongField(this_ptr, current_length);
    int chunk_size = env.getStaticIntField(RandomAccessFile, CHUNK_SIZE);
    if (current_posn >= current_len) {
      env.throwException(EOFException);
    }
    int chunk = findDataChunk(env, this_ptr, current_posn,
                              chunk_size);
    byte result = getDataValue(env, chunk, current_posn, chunk_size);
    env.setLongField(this_ptr, current_position, current_posn + 1);
    return result;
  }

  private static final int INT_SIZE = 4;
  private static final String data_root = "data_root";
  private static final String current_position = "currentPosition";
  private static final String current_length = "currentLength";
  private static final String CHUNK_SIZE = "CHUNK_SIZE";
  private static final String chunk_index = "chunk_index";
  private static final String next = "next";
  private static final String data = "data";
  private static final String EOFException = "java.io.EOFException";
  private static final String RandomAccessFile = "java.io.RandomAccessFile";
  private static final String DataRepresentation =
    RandomAccessFile + "$DataRepresentation";

  private static int findDataChunk(MJIEnv env, int this_ptr, long position,
                                   int chunk_size) {
  	
    ClassInfo dataRep = getDataRepresentationClassInfo(env);
    if (dataRep == null) {
      // will be reexecuted
      return 0;
    }
    
  	//check if the file data is mapped, use mapped this_ptr if it exists
  	this_ptr = getMapping(env,this_ptr);  	
    int prev_obj = MJIEnv.NULL;
    int cur_obj = env.getReferenceField(this_ptr, data_root);
    long chunk_idx = position/chunk_size;
    while (cur_obj != MJIEnv.NULL &&
           env.getLongField(cur_obj, chunk_index) < chunk_idx) {
      prev_obj = cur_obj;
      cur_obj = env.getReferenceField(cur_obj, next);
    }
    if (cur_obj != MJIEnv.NULL &&
        env.getLongField(cur_obj, chunk_index) == chunk_idx) {
      return cur_obj;
    }
    int result = env.newObject(dataRep);
    int int_array = env.newIntArray(chunk_size/INT_SIZE);
    env.setReferenceField(result, data, int_array);
    env.setLongField(result, chunk_index, chunk_idx);
    env.setReferenceField(result, next, cur_obj);
    if (prev_obj == MJIEnv.NULL) {
      env.setReferenceField(this_ptr, data_root, result);
    } else {
      env.setReferenceField(prev_obj, next, result);
    }
    return result;
  }

  private static void setDataValue(MJIEnv env, int chunk_obj, long position,
                                   byte data_value, int chunk_size) {
    int offset = (int) (position % chunk_size);
    int index = offset / INT_SIZE;
    int bit_shift = 8 * (offset % INT_SIZE);
    int int_array = env.getReferenceField(chunk_obj, data);
    int old_value = env.getIntArrayElement(int_array, index);
    env.setIntArrayElement(int_array, index,
                             (old_value & ~(0xff << bit_shift)) |
                             data_value << bit_shift);
  }

  private static byte getDataValue(MJIEnv env, int chunk_obj, long position,
                                   int chunk_size) {
    int offset = (int) (position % chunk_size);
    int index = offset / INT_SIZE;
    int bit_shift = 8 * (offset % INT_SIZE);
    int int_array = env.getReferenceField(chunk_obj, data);
    return (byte) (env.getIntArrayElement(int_array, index) >> bit_shift);

  }
}

