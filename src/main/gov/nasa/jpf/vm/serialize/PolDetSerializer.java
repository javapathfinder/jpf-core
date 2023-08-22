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
package gov.nasa.jpf.vm.serialize;


import gov.nasa.jpf.JPFErrorException;
import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.Statics;
import gov.nasa.jpf.vm.ThreadInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * The serializer used in PolDet, customized to ignore irrelevant parts of the state
 *
 * @author Pu Yi
 */
public class PolDetSerializer extends FilteringSerializer {

  public enum PolDetPhase {
    // capture the state before test run, collecting loaded classes along the way
    PRESTATE,
    // capture the state after the test run, serializing only from previously loaded classes
    POSTSTATE
  }
  public PolDetPhase phaseOfPolDet;

  public Set<String> loadedClasses = new HashSet<>();
  public Set<String> ignoredFields = new HashSet<>(Arrays.asList("emptyAnnotations", "nInstances"));
  public Set<String> ignoredMethods = new HashSet<>(Arrays.asList("main", "testStarted", "testFinished", "compareStates", "capturePreState"));

  protected boolean isFrameIgnored (StackFrame frame) {
    return frame.getClassName().startsWith("org.junit") || ignoredMethods.contains(frame.getMethodName());
  }

  @Override
  protected void serializeStackFrames (ThreadInfo ti) {
    processReference(ti.getThreadObjectRef());

    for (StackFrame frame = ti.getTopFrame(); frame != null; frame = frame.getPrevious()) {
      if (!isFrameIgnored(frame)) {
        serializeFrame(frame);
      }
    }
  }

  protected boolean isStaticsIgnored (StaticElementInfo sei) {
    String className = sei.toString();
    return className.startsWith("org.junit") || className.toLowerCase().contains("cache");
  }

  @Override
  protected void serializeStatics (Statics statics) {
    int classCount = 0;
    for (StaticElementInfo sei : statics.liveStatics()) {
      if (phaseOfPolDet == PolDetPhase.PRESTATE) {
        if (!isStaticsIgnored(sei)) {
          classCount++;
        }
      } else {
        if (loadedClasses.contains(sei.toString())) {
          classCount++;
        }
      }
    }
    buf.add(classCount);
    for (StaticElementInfo sei : statics.liveStatics()) {
      if (phaseOfPolDet == PolDetPhase.PRESTATE) {
        if (!isStaticsIgnored(sei)) {
          loadedClasses.add(sei.toString());
          serializeClass(sei);
        }
      } else {
        if (loadedClasses.contains(sei.toString())) {
          serializeClass(sei);
        }
      }
    }
  }

  protected String getFieldName (FieldInfo[] fields, int offset) {
    for (FieldInfo fi : fields) {
      if (fi.getStorageOffset() <= offset && offset < fi.getStorageOffset() + fi.getStorageSize()) {
        return fi.getName();
      }
    }
    throw new JPFErrorException("Field not found for the given offset!");
  }

  protected String getStaticFieldName (ClassInfo ci, int offset) {
    return getFieldName(ci.getDeclaredStaticFields(), offset);
  }

  protected String getInstanceFieldName (ClassInfo ci, int offset) {
    return getFieldName(ci.getDeclaredInstanceFields(), offset);
  }

  protected boolean isFieldIgnored (String fn) {
    return ignoredFields.contains(fn) || fn.toLowerCase().contains("cache");
  }

  @Override
  protected void serializeClass (StaticElementInfo sei) {
    buf.add(sei.getStatus());

    Fields fields = sei.getFields();
    ClassInfo ci = sei.getClassInfo();
    FinalBitSet filtered = getStaticFilterMask(ci);
    FinalBitSet refs = getStaticRefMask(ci);

    int max = ci.getStaticDataSize();

    for (int i = 0; i < max; i++) {
      if (!filtered.get(i)) {
        int v = fields.getIntValue(i);
        String fn = getStaticFieldName(ci, i);
        if (refs.get(i)) {
          if (!isFieldIgnored(fn)) {
            processReference(v);
          }
        } else {
          if (!isFieldIgnored(fn)) {
            buf.add(v);
          }
        }
      }
    }
  }

  public int[] getState (PolDetPhase phase) {
    phaseOfPolDet = phase;
    return computeStoringData();
  }
}
