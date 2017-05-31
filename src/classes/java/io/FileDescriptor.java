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

package java.io;

import gov.nasa.jpf.annotation.FilterField;

/**
 * a simple abstraction for a file descriptor, which for us is little more
 * than just an id for a native data buffer (we don't want to keep the
 * data itself in the JPF space)
 * 
 * <2do> still needs the standard descriptors
 */
public class FileDescriptor {
  @FilterField int fd; // to be set from the native side
  
  static final int FD_READ = 0;
  static final int FD_WRITE = 1;
  
  static final int FD_NEW = 0;
  static final int FD_OPENED = 1;
  static final int FD_CLOSED = 2;
  
  String fileName;
  int mode; // no use to turn it into an Enum - it's accessed from the native peer
  
  // we can't use the 'fd' field for it, because we need to keep that in
  // case we have to automatically reopen after a backtrack
  int state = FD_NEW;
  
  long off; // we need to keep this on the model side to make it backtrackable
    
  public FileDescriptor () {
    fd = -1;
  }
  
  FileDescriptor (String fname, int mode) throws IOException, FileNotFoundException  {
    fileName = fname;
    this.mode = mode;
    
    fd = open(fname, mode);
    
    if (fd != -1){
      state = FD_OPENED;
    } else {
      throw new FileNotFoundException(fname);
    }
  }
  
  public boolean valid () {
    return (fd != -1);
  }
  
  public void close () throws IOException {
    close0();
    state = FD_CLOSED;
  }
  
  //--- those are the real work horses
  native int open (String fname, int mode) throws IOException;
  public native void sync();  
  native int read () throws IOException;
  public native int read (byte[] buf, int off, int len);
  native long skip(long n) throws IOException;
  native int available () throws IOException;
  native void close0 () throws IOException;
  
  native void write (int b) throws IOException;
  public native void write (byte[] buf, int off, int len);
}
