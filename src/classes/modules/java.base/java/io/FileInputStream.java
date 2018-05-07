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

import java.nio.channels.FileChannel;

/**
 * a simple model to read data w/o dragging the file system content into
 * the JPF memory
 */
public class FileInputStream extends InputStream {

  FileDescriptor fd;
  private FileChannel fc = null;

  public FileInputStream (String fname) throws FileNotFoundException {
    try {
      fd = new FileDescriptor(fname, FileDescriptor.FD_READ);
    } catch (IOException iox){
      throw new FileNotFoundException(fname);
    }
  }
  
  public FileInputStream (File file) throws FileNotFoundException {
    this( file.getAbsolutePath());
  }
  
  public FileInputStream (FileDescriptor fd) {
    this.fd = fd;
  }
  
  @Override
  public int read(byte b[]) throws IOException {
    return read(b,0,b.length);
  }

  public FileChannel getChannel() {
    if(this.fc ==null){
      this.fc = new FileChannel(fd);
    }
    return this.fc;
  }
  
  //--- our native peer methods
  
  boolean open (String fname) {
    // this sets the FileDescriptor from the peer side
    return false;
  }
  
  @Override
  public int read() throws IOException {
    return fd.read();
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    return fd.read(b,off,len);
  }
  
  @Override
  public long skip(long n) throws IOException {
    return fd.skip(n);
  }

  @Override
  public int available () throws IOException {
    return fd.available();
  }
  
  @Override
  public void close () throws IOException {
    fd.close();
  }
  
  
}
