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

import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.jvm.DirClassFileContainer;
import gov.nasa.jpf.jvm.JVMAnnotationParser;
import gov.nasa.jpf.jvm.JVMClassFileContainer;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;

import org.junit.Test;

/**
 * unit test for AnnotationInfo creation
 */
public class AnnotationInfoTest extends TestJPF {

  @interface X {
    String value() default "nothing";
  }

  @interface Y {
    String name();
    int[] someArray() default { 1, 2, 3 };
  }

  protected AnnotationInfo createAnnotationInfo (String annotationName) throws ClassParseException {
    DirClassFileContainer dfc = new DirClassFileContainer( new File("build/tests"));
    ClassPath cp = new ClassPath();
    cp.addClassFileContainer(dfc);
    
    JVMClassFileContainer.JVMClassFileMatch match = (JVMClassFileContainer.JVMClassFileMatch)cp.findMatch( annotationName);
    byte[] data = match.getData();
    
    ClassFile cf = new ClassFile( data);
    JVMAnnotationParser parser = new JVMAnnotationParser(cf);
    
    return new AnnotationInfo( annotationName, null, parser);
  }
  
  @Test
  public void testStringDefaultValue() {
    try {
      String annotationName = "gov.nasa.jpf.vm.AnnotationInfoTest$X";
      
      AnnotationInfo ai = createAnnotationInfo(annotationName);
      AnnotationInfo.Entry[] entries = ai.getEntries();
      
      assertTrue(entries.length == 1);
      assertTrue(entries[0].getKey().equals("value"));
      assertTrue(entries[0].getValue().equals("nothing"));
    
    } catch (Throwable t){
      t.printStackTrace();
      fail("unexpected exception: " + t);
    }
  }

  @Test
  public void testIntArrayDefaultValue() {
    try {
      String annotationName = "gov.nasa.jpf.vm.AnnotationInfoTest$Y";
      
      AnnotationInfo ai = createAnnotationInfo(annotationName);
      AnnotationInfo.Entry[] entries = ai.getEntries();

      assertTrue(entries.length == 2);
      assertTrue(entries[1].getKey().equals("someArray"));

      Object[] a = (Object[]) entries[1].getValue();
      assertTrue(a.length == 3);
      assertTrue((Integer)a[0] == 1 && (Integer)a[1] == 2 && (Integer)a[2] == 3);
      
    } catch (Throwable t){
      t.printStackTrace();
      fail("unexpected exception: " + t);
    }
  }
}
