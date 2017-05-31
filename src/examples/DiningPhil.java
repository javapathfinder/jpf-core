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

public class DiningPhil {

  static class Fork {
  }

  static class Philosopher extends Thread {

    Fork left;
    Fork right;

    public Philosopher(Fork left, Fork right) {
      this.left = left;
      this.right = right;
      //start();
    }

    @Override
	public void run() {
      // think!
      synchronized (left) {
        synchronized (right) {
          // eat!
        }
      }
    }
  }
  
  static int nPhilosophers = 6;

  public static void main(String[] args) {
    if (args.length > 0){
      nPhilosophers = Integer.parseInt(args[0]);
    }
    
    //Verify.beginAtomic();
    Fork[] forks = new Fork[nPhilosophers];
    for (int i = 0; i < nPhilosophers; i++) {
      forks[i] = new Fork();
    }
    for (int i = 0; i < nPhilosophers; i++) {
      Philosopher p = new Philosopher(forks[i], forks[(i + 1) % nPhilosophers]);
      p.start();
    }
    //Verify.endAtomic();
  }
}