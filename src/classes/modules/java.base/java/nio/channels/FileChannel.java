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
package java.nio.channels;

import java.io.IOException;
import java.io.FileDescriptor;

import java.nio.ByteBuffer;

//This class uses the methods from FileDescriptor in order to access files
public class FileChannel {

	public int read(ByteBuffer dst) throws IOException{
		return fd.read(dst.array(),0,dst.array().length);
	}

	public int write(ByteBuffer src) throws IOException{
		fd.write(src.array(),0,src.array().length);
		return src.array().length;
	}

	public void close() throws IOException{
		fd.close();
	}

	public FileChannel(FileDescriptor fd){
		this.fd = fd;
	}

	private FileDescriptor fd = null;

	public long position() { return 0; } // Stub for Eclipse

	public FileChannel position(long p) { return null; } // Stub for Eclipse
}
