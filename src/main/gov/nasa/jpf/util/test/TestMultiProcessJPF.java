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

package gov.nasa.jpf.util.test;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.vm.NotDeadlockedProperty;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * This is a root class for testing multi-processes code. This forces
 * JPF to use MultiProcessVM and DistributedSchedulerFactory
 */
public abstract class TestMultiProcessJPF extends TestJPF {
  int numOfPrc;

  @Override
  protected void setTestTargetKeys(Config conf, StackTraceElement testMethod) {
    conf.put("target.entry", "runTestMethod([Ljava/lang/String;)V");
    conf.put("target.replicate", Integer.toString(numOfPrc));
    conf.put("target", testMethod.getClassName());
    conf.put("target.test_method", testMethod.getMethodName());
    conf.put("vm.class", "gov.nasa.jpf.vm.MultiProcessVM");
    conf.put("vm.scheduler_factory.class", "gov.nasa.jpf.vm.DistributedSchedulerFactory");
  }

  protected native int getProcessId();

  protected boolean mpVerifyAssertionErrorDetails (int prcNum, String details, String... args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      unhandledException( getCaller(), "java.lang.AssertionError", details, args);
      return false;
    }
  }

  protected boolean mpVerifyAssertionError (int prcNum, String... args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      unhandledException( getCaller(), "java.lang.AssertionError", null, args);
      return false;
    }
  }

  protected boolean mpVerifyNoPropertyViolation (int prcNum, String...args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      noPropertyViolation(getCaller(), args);
      return false;
    }
  }

  protected boolean mpVerifyUnhandledExceptionDetails (int prcNum, String xClassName, String details, String... args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      unhandledException( getCaller(), xClassName, details, args);
      return false;
    }
  }

  protected boolean mpVerifyUnhandledException (int prcNum, String xClassName, String... args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      unhandledException( getCaller(), xClassName, null, args);
      return false;
    }
  }

  protected boolean mpVerifyJPFException (int prcNum, TypeRef xClsSpec, String... args){
    if (runDirectly) {
      return true;

    } else {
      numOfPrc = prcNum;
      try {
        Class<? extends Throwable> xCls = xClsSpec.asNativeSubclass(Throwable.class);

        jpfException( getCaller(), xCls, args);

      } catch (ClassCastException ccx){
        fail("not a property type: " + xClsSpec);
      } catch (ClassNotFoundException cnfx){
        fail("property class not found: " + xClsSpec);
      }
      return false;
    }
  }

  protected boolean mpVerifyPropertyViolation (int prcNum, TypeRef propertyClsSpec, String... args){
    if (runDirectly) {
      return true;

    } else {
      numOfPrc = prcNum;
      try {
        Class<? extends Property> propertyCls = propertyClsSpec.asNativeSubclass(Property.class);
        propertyViolation( getCaller(), propertyCls, args);

      } catch (ClassCastException ccx){
        fail("not a property type: " + propertyClsSpec);
      } catch (ClassNotFoundException cnfx){
        fail("property class not found: " + propertyClsSpec);
      }
      return false;
    }
  }

  protected boolean mpVerifyDeadlock (int prcNum, String... args){
    if (runDirectly) {
      return true;
    } else {
      numOfPrc = prcNum;
      propertyViolation( getCaller(), NotDeadlockedProperty.class, args);
      return false;
    }
  }
}
