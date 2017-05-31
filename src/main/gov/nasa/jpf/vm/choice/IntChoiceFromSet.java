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
package gov.nasa.jpf.vm.choice;

import gov.nasa.jpf.Config;
/**
 * @author cartho
 *
 * choose from a set of values provided in configuration as
 * xxx.class = IntChoiceFromSet
 * xxx.values = {1, 2, 3, 400}
 * where "xxx" is the choice id.
 * 
 * choices can then made using: getInt("xxx");
 */
public class IntChoiceFromSet extends IntChoiceFromList {

	/**
	 * @param conf JPF configuration object
	 * @param id name used in choice config
	 */
	public IntChoiceFromSet(Config conf, String id) {
		super(conf, id);
		removeDuplicates();
	}

	/* Remove duplicate values; currently implemented as iteration
	 * on array to avoid heavyweight TreeSet */
	private void removeDuplicates() {
		int len = values.length;
		for (int i = 0; i < len; i++) {
			int j = i + 1;
			while (j < len) {
				if (values[i] - values[j] == 0) { // strange comparison to avoid unboxing
					values[j] = values[len - 1];
					len--;
					// don't increment j as new element has been placed there and needs to be re-tested
				} else {
					j++;
				}
			}
		}
		if (len < values.length) {
			Integer[] uniqVals = new Integer[len];
			System.arraycopy(values, 0, uniqVals, 0, len);
			values = uniqVals;
		}
	}

  public IntChoiceFromSet(String id, int... val){
    super(id, val);
    removeDuplicates();
    count = -1;
  }

	/** super constructor for subclasses that want to configure themselves
	 * 
	 * @param id name used in choice config
	 */
	protected IntChoiceFromSet(String id){
		super(id);
	}
}
