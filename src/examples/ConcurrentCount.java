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
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentCount {
  static final int COUNT = 30000;
  volatile static int count = COUNT;
  volatile static AtomicBoolean lock = new AtomicBoolean(false);
  static int a = 0;
  static int b = 0;

  public static void main(String args[]) {

    new Thread() {

      @Override
      public void run() {
        while(count > 0) {
          if (lock.get()) continue;
          lock.set(true);
          decreaseCount();
          a++;
          lock.set(false);


        }
      }
    }.start();

    new Thread() {

      @Override
      public void run() {
        while(count > 0) {
          if (lock.get()) continue;
          lock.set(true);
          decreaseCount();
          b++;
          lock.set(false);


        }
      }
    }.start();

    while(count > 0);
    //System.out.println("a = " + a);
    //System.out.println("b = " + b);
    //System.out.println("a + b = " + (a + b));
    //System.out.println("count = " + count);

    //assert a + b == COUNT;
  }

  private static synchronized void decreaseCount() {
    count--;
  }

}

