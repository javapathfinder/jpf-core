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

public class FileOutputStream extends OutputStream {

  FileDescriptor fd;
  private FileChannel fc = null;
  
  public FileOutputStream (String fname) throws FileNotFoundException {
    try {
      fd = new FileDescriptor(fname, FileDescriptor.FD_WRITE);
    } catch (IOException iox){
      throw new FileNotFoundException(fname);
    }
  }
  
  public FileOutputStream (File file) throws FileNotFoundException {
    this( file.getAbsolutePath());
  }
  
  public FileOutputStream (FileDescriptor fd) {
    this.fd = fd;
  }
  
  public FileChannel getChannel() {
    if(this.fc ==null){
      this.fc = new FileChannel(fd);
    }
    return this.fc;
  }
  
  public FileDescriptor getFD() {
    return fd;
  }
  
  //--- our native peer methods
  
  boolean open (String fname) {
    // this sets the FileDescriptor from the peer side
    return false;
  }
  
  @Override
  public void write (int b) throws IOException {
    fd.write(b);
  }

  @Override
  public void write (byte[] buf, int off, int len) throws IOException {
    fd.write(buf, off, len);
  }
  
  @Override
  public void close () throws IOException {
    fd.close();
  }

  @Override
  public void flush () throws IOException {
    fd.sync();
  }
}
