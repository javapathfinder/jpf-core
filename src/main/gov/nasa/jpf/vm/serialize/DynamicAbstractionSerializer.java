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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.util.FieldSpec;
import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ReferenceArrayFields;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.Statics;

import java.util.LinkedList;
import java.util.List;

/**
 * a serializer that uses Abstraction objects stored as field attributes to
 * obtain the values to hash. 
 */
public class DynamicAbstractionSerializer extends FilteringSerializer {

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.vm.serialize.DynamicAbstractionSerializer");

  static class FieldAbstraction {
    FieldSpec fspec;
    Abstraction abstraction;

    FieldAbstraction(FieldSpec f, Abstraction a) {
      fspec = f;
      abstraction = a;
    }
  }

  public class Attributor extends ListenerAdapter {

    @Override
    public void classLoaded(VM vm, ClassInfo loadedClass) {

      if (!loadedClass.isArray() && !loadedClass.isPrimitive()) {

        if (!fieldAbstractions.isEmpty()) {
          for (FieldInfo fi : loadedClass.getDeclaredInstanceFields()) {
            for (FieldAbstraction fabs : fieldAbstractions) {
              if (fabs.fspec.matches(fi)) {
                logger.info("setting instance field abstraction ", fabs.abstraction.getClass().getName(),
                        " for field ", fi.getFullName());
                fi.addAttr(fabs.abstraction);
              }
            }
          }

          for (FieldInfo fi : loadedClass.getDeclaredStaticFields()) {
            for (FieldAbstraction fabs : fieldAbstractions) {
              if (fabs.fspec.matches(fi)) {
                logger.info("setting static field abstraction ", fabs.abstraction.getClass().getName(),
                        " for field ", fi.getFullName());
                fi.addAttr(fabs.abstraction);
              }
            }
          }
        }
      }
    }
  }
  
  protected StringSetMatcher includeClasses = null; //  means all
  protected StringSetMatcher excludeClasses = null; //  means none
  protected StringSetMatcher includeMethods = null;
  protected StringSetMatcher excludeMethods = null;

  List<FieldAbstraction> fieldAbstractions;

  protected boolean processAllObjects;
  protected boolean declaredFieldsOnly;

  
  public DynamicAbstractionSerializer(Config conf) {
    processAllObjects = conf.getBoolean("das.all_objects", true);
    declaredFieldsOnly = conf.getBoolean("das.declared_fields", false);

    includeClasses = StringSetMatcher.getNonEmpty(conf.getStringArray("das.classes.include"));
    excludeClasses = StringSetMatcher.getNonEmpty(conf.getStringArray("das.classes.exclude"));

    includeMethods = StringSetMatcher.getNonEmpty(conf.getStringArray("das.methods.include"));
    excludeMethods = StringSetMatcher.getNonEmpty(conf.getStringArray("das.methods.exclude"));

    fieldAbstractions = getFieldAbstractions(conf);
  }

  
  protected List<FieldAbstraction> getFieldAbstractions(Config conf){
    List<FieldAbstraction> list = null;
    
    String[] fids = conf.getCompactTrimmedStringArray("das.fields");
    for (String id : fids) {
      String keyPrefix = "das." + id;
      String fs = conf.getString(keyPrefix + ".field");
      if (fs != null) {
        FieldSpec fspec = FieldSpec.createFieldSpec(fs);
        if (fspec != null) {
          String aKey = keyPrefix + ".abstraction";
          Abstraction abstraction = conf.getInstance(aKey, Abstraction.class);

          logger.info("found field abstraction for ", fspec, " = ", abstraction.getClass().getName());
          
          if (list == null){
            list = new LinkedList<FieldAbstraction>();
          }
          
          list.add(new FieldAbstraction(fspec, abstraction));
        }
      } else {
        logger.warning("no field spec for id: " + id);
      }
    }
    
    return list;
  }
  
  @Override
  public void attach (VM vm){
    super.attach(vm);
    
    if (fieldAbstractions != null){
      Attributor attributor = new Attributor();
      vm.addListener(attributor);
    }
  }
  
  
  // note that we don't add the reference value here
  @Override
  public void processReference(int objref) {
    if (objref != MJIEnv.NULL) {
      ElementInfo ei = heap.get(objref);
      if (!ei.isMarked()) { // only add objects once
        ei.setMarked();
        refQueue.add(ei);
      }
    }
    
    // we DON'T add the reference value to the buffer here
  }

  protected void processField(Fields fields, int[] slotValues, FieldInfo fi, FinalBitSet filtered) {
    int off = fi.getStorageOffset();
    if (!filtered.get(off)) {
      Abstraction a = fi.getAttr(Abstraction.class);
      if (a != null) {
        if (fi.is1SlotField()) {
          if (fi.isReference()) {
            int ref = fields.getReferenceValue(off);
            buf.add(a.getAbstractObject(ref));

            if (a.traverseObject(ref)) {
              processReference(ref);
            }

          } else if (fi.isFloatField()) {
            buf.add(a.getAbstractValue(fields.getFloatValue(off)));
          } else {
            buf.add(a.getAbstractValue(fields.getIntValue(off)));
          }
        } else { // double or long
          if (fi.isLongField()) {
            buf.add(a.getAbstractValue(fields.getLongValue(off)));
          } else { // got to be double
            buf.add(a.getAbstractValue(fields.getDoubleValue(off)));
          }
        }

      } else { // no abstraction, fall back to concrete values
        if (fi.is1SlotField()) {
          if (fi.isReference()) {
            int ref = slotValues[off];
            buf.add(ref);
            processReference(ref);

          } else {
            buf.add(slotValues[off]);
          }

        } else { // double or long
          buf.add(slotValues[off]);
          buf.add(slotValues[off + 1]);
        }
      }
    }
  }

  // non-ignored class
  @Override
  protected void processArrayFields(ArrayFields fields) {
    buf.add(fields.arrayLength());

    if (fields.isReferenceArray()) {
      int[] values = fields.asReferenceArray();
      for (int i = 0; i < values.length; i++) {
        processReference(values[i]);
        buf.add(values[i]);
      }
    } else {
      fields.appendTo(buf);
    }
  }

  // for ignored class, to get all live objects
  protected void processNamedInstanceReferenceFields(ClassInfo ci, Fields fields) {
    FinalBitSet filtered = getInstanceFilterMask(ci);
    FinalBitSet refs = getInstanceRefMask(ci);
    int[] slotValues = fields.asFieldSlots(); // for non-attributed fields

    for (int i = 0; i < slotValues.length; i++) {
      if (!filtered.get(i)) {
        if (refs.get(i)) {
          processReference(slotValues[i]);
        }
      }
    }
  }

  // for ignored class, to get all live objects
  protected void processNamedStaticReferenceFields(ClassInfo ci, Fields fields) {
    FinalBitSet filtered = getStaticFilterMask(ci);
    FinalBitSet refs = getStaticRefMask(ci);
    int[] slotValues = fields.asFieldSlots(); // for non-attributed fields

    for (int i = 0; i < slotValues.length; i++) {
      if (!filtered.get(i)) {
        if (refs.get(i)) {
          processReference(slotValues[i]);
        }
      }
    }
  }

  // for ignored class, to get all live objects
  protected void processReferenceArray(ReferenceArrayFields fields) {
    int[] slotValues = fields.asReferenceArray();
    for (int i = 0; i < slotValues.length; i++) {
      processReference(slotValues[i]);
    }
  }

  // non-ignored class
  @Override
  protected void processNamedFields(ClassInfo ci, Fields fields) {
    FinalBitSet filtered = getInstanceFilterMask(ci);
    int nFields = ci.getNumberOfInstanceFields();
    int[] slotValues = fields.asFieldSlots(); // for non-attributed fields

    for (int i = 0; i < nFields; i++) {
      FieldInfo fi = ci.getInstanceField(i);

      if (declaredFieldsOnly && fi.getClassInfo() != ci) {
        continue;
      }

      processField(fields, slotValues, fi, filtered);
    }
  }

  // <2do> this should also allow abstraction of whole objects, so that
  // we can hash combinations/relations of field values
  @Override
  public void process (ElementInfo ei) {
    Fields fields = ei.getFields();
    ClassInfo ci = ei.getClassInfo();

    if (StringSetMatcher.isMatch(ci.getName(), includeClasses, excludeClasses)) {
      buf.add(ci.getUniqueId());

      if (fields instanceof ArrayFields) { // not filtered
        processArrayFields((ArrayFields) fields);
      } else { // named fields, potentially filtered & abstracted via attributes
        processNamedFields(ci, fields);
      }

    } else { // ignored class
      // we check for live non-ignored objects along all stack frames, so we should do the same for all objects
      if (fields instanceof ArrayFields) {
        if (fields instanceof ReferenceArrayFields) {
          processReferenceArray((ReferenceArrayFields) fields);
        }
      } else {
        processNamedInstanceReferenceFields(ci, fields);
      }
    }
  }

  @Override
  protected void serializeFrame (StackFrame frame) {
    MethodInfo mi = frame.getMethodInfo();
    
    if (StringSetMatcher.isMatch(mi.getFullName(), includeMethods, excludeMethods)){
      // <2do> should do frame abstraction here
      super.serializeFrame(frame);

    } else {
      if (processAllObjects) {
        frame.visitReferenceSlots(this);
      }
    }
  }

  @Override
  protected void serializeClass (StaticElementInfo sei) {
    ClassInfo ci = sei.getClassInfo();
    Fields fields = sei.getFields();

    if (StringSetMatcher.isMatch(ci.getName(), includeClasses, excludeClasses)) {
      buf.add(sei.getStatus());

      FinalBitSet filtered = getStaticFilterMask(ci);
      int[] slotValues = fields.asFieldSlots();

      for (FieldInfo fi : ci.getDeclaredStaticFields()) {
        processField(fields, slotValues, fi, filtered);
      }

    } else {
      // ignored class, but still process references to extract live objects
      processNamedStaticReferenceFields(ci, fields);
    }
  }

  @Override
  protected void serializeClassLoaders(){
    // we don't care about the number of classloaders
    
    for (ClassLoaderInfo cl : ks.classLoaders) {
      if(cl.isAlive()) {
        serializeStatics( cl.getStatics());
      }
    }
  }

  @Override
  protected void serializeStatics(Statics statics){
    // we don't care about the number of statics entries

    for (StaticElementInfo sei : statics.liveStatics()) {
      serializeClass(sei);
    }
  }
}
