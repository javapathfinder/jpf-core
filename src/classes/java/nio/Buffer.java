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

public abstract class Buffer {

	protected int position;
	protected int capacity;
	protected int limit;
	protected int mark = -1;


	// Todo: There exists other missing methods in this class
	// which may throw NoSuchMethodException errors to users
	// For example checkBounds has yet to be implemented
	Buffer(int mark, int pos, int lim, int cap) {
		if (cap < 0)
			throw new IllegalArgumentException("Negative capacity: " + cap);
		this.capacity = cap;
		limit(lim);
		position(pos);
		if (mark >= 0 && mark <= pos){
			this.mark = mark;
		}
	}

	public final int capacity() {
		return capacity;
	}

	public final Buffer position(int newPosition) {
		if ((newPosition<0)||(newPosition>limit)) {
			throw new IllegalArgumentException("Illegal buffer position exception: "+newPosition);
		}
		this.position = newPosition;
		return this;
	}

	public final int position() {
		return position;
	}

	public final int limit() {
		return this.limit;
	}

	public final Buffer limit(int newLimit) {
		if ((newLimit<0)||(newLimit>capacity)) {
			throw new IllegalArgumentException("Illegal buffer limit exception: "+newLimit);
		}
		this.limit = newLimit;
		return this;
	}

	public final Buffer clear(){
		position = 0;
		limit = capacity;
		mark = -1;
		return this;
	}

	public final Buffer flip() {
		limit = position;
		position = 0;
		return this;
	}

	public final Buffer rewind() {
		position = 0;
		return this;
	}

	public final int remaining() {
		return limit-position;
	}

	public final boolean hasRemaining() {
		return remaining()>0;
	}

	public abstract boolean hasArray();

	public abstract Object array();
}
