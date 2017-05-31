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

package gov.nasa.jpf.test.java.io;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

/**
 * regression test for object streams
 */
public class ObjectStreamTest extends TestJPF {

  static class X implements Serializable {
    String q = "the ultimate question";
    Y a = new Y(-42);

    @Override
	public String toString() {
      return "X{q=\""+q+"\",a="+a+'}';
    }
  }

  static class Y implements Serializable {
    static final long serialVersionUID = -42;

    boolean z = true;
    byte b = 42;
    char c = '!';
    short s = 42;
    int i;
    long l = 42000;
    float f = 42.0f;
    double d = 4.2e5;

    Y (int answer){
      i = answer;
    }

    @Override
	public String toString() {
      return "Y{z="+z+",b="+b+",c="+c+",s="+s+",i="+i+",l="+l+",f="+f+",d="+d+ '}';
    }
  }

  @Test
  public void testSimpleReadbackOk () {
    String fname = "tmp.ser";

    if (!isJPFRun()){
      try {
        X x = new X();
        FileOutputStream fos = new FileOutputStream(fname);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(x);
        oos.close();
      } catch (Throwable t){
        fail("serialization failed: " + t);
      }
    }

    if (verifyNoPropertyViolation()){
      try {
        FileInputStream fis = new FileInputStream(fname);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o = ois.readObject();
        ois.close();

        System.out.println(o);

        assert o instanceof X : "wrong object type: " + o.getClass().getName();
        X x = (X) o;
        assert x.a.i == -42;
      } catch (Throwable t){
        //t.printStackTrace();
        fail("serialization readback failed: " + t);
      }

      File f = new File(fname);
      f.delete();
    }
  }

}
