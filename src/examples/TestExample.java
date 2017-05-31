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

class T1 {
  int d = 42;

  public int func1(int a, int b) {
    if (a > b) {
      return 1;
    } else if (a == b) {
      return 0;
    } else {
      return -1;
    }
  }

  public boolean func2(boolean cond) {
    if (cond && (d > 40)) {
      d--;
    } else {
      d++;
    }
    return cond;
  }
}

class T2 {

  public int computeSomething (int a, int b){
    try {
      return a / b;
    } catch (ArithmeticException ax){
      return -1; // pretty lame error handling
    }
  }

  public void doSomething() {
    System.out.println("something");
  }
}

public class TestExample {

  public static void main(String[] args) {
    T1 t1 = new T1();

    assert t1.func1(1, 0) > 0;
    assert t1.func1(0, 1) < 0;

    assert t1.func2(true) == true;
    assert t1.func2(false) == false;


    T2 t2 = new T2();

    assert t2.computeSomething(42, 42) == 1.0;
  }
}

