/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.util;

import java.io.PrintStream;

/**
 * convenience interface to mix in PrintStream interface
 */
public interface PrintStreamable {

  // the primitive method used by the defaults
  PrintStream getPrintStream();

  default void println() {
    getPrintStream().println();
  }

  default void print(boolean a){
    getPrintStream().print(a);
  }
  default void print(int a){
    getPrintStream().print(a);
  }
  default void print(double a){
    getPrintStream().print(a);
  }
  default void print(String s) {
    getPrintStream().print(s);
  }
  default void print(Object o) {
    getPrintStream().print(o.toString());
  }

  default void println(boolean a){
    getPrintStream().println(a);
  }
  default void println(int a){
    getPrintStream().println(a);
  }
  default void println(double a){
    getPrintStream().println(a);
  }
  default void println(String s) {
    getPrintStream().println(s);
  }
  default void println(Object o) {
    getPrintStream().println(o.toString());
  }

  default void printf (String format, Object... args) {
    getPrintStream().printf(format, args);
  }

  //... and many more
}
