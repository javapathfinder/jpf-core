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

import gov.nasa.jpf.JPFException;

/**
 * our concrete Fields factory (representing the default JPF object model)
 */
public class DefaultFieldsFactory implements FieldsFactory {

  @Override
  public Fields createInstanceFields (ClassInfo ci) {
    return new NamedFields(ci.getInstanceDataSize());
  }

  @Override
  public Fields createStaticFields (ClassInfo ci) {
    return new NamedFields(ci.getStaticDataSize());
  }

  @Override
  public Fields createArrayFields (String type, ClassInfo ci, int nElements, int typeSize, boolean isReferenceArray) {
    char t = type.charAt(1);
    switch (t){
      case 'Z': return new BooleanArrayFields(nElements);
      case 'B': return new ByteArrayFields(nElements);
      case 'C': return new CharArrayFields(nElements);
      case 'S': return new ShortArrayFields(nElements);
      case 'I': return new IntArrayFields(nElements);
      case 'J': return new LongArrayFields(nElements);
      case 'F': return new FloatArrayFields(nElements);
      case 'D': return new DoubleArrayFields(nElements);
      case 'L':
      case '[':
        return new ReferenceArrayFields(nElements);
      default:
        throw new JPFException("unknown array type: " + type);
    }
  }
}
