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

/**
 * concurrency example with deadlock
 * adapted from "Concurrency: State Models & Java Programs", Jeff Magee & Jeff Kramer
 * 
 * Note there need to be at least 2x buffer_size instances of either producers or
 * consumers in order to produce the deadlock, which basically depends on 
 * a notification choice between a consumer and a producer in a context where
 * only threads of the notifier type are still runnable
 * 
 * This is a good benchmark for state management/matching in a small heap context.
 * Higher numbers of buffer size and producers/consumers result in a nice
 * state explosion
 */
public class BoundedBuffer {
  
  static int BUFFER_SIZE = 1;
  static int N_PRODUCERS = 4;
  static int N_CONSUMERS = 4;
  
  static Object DATA = "fortytwo";
      
  //--- the bounded buffer implementation
  protected Object[] buf;
  protected int in = 0;
  protected int out= 0;
  protected int count= 0;
  protected int size;
  
  public BoundedBuffer(int size) {
    this.size = size;
    buf = new Object[size];
  }

  public synchronized void put(Object o) throws InterruptedException {
    while (count == size) {
      wait();
    }
    buf[in] = o;
    //System.out.println("PUT from " + Thread.currentThread().getName());
    ++count;
    in = (in + 1) % size;
    notify(); // if this is not a notifyAll() we might notify the wrong waiter
  }

  public synchronized Object get() throws InterruptedException {
    while (count == 0) {
      wait();
    }
    Object o = buf[out];
    buf[out] = null;
    //System.out.println("GET from " + Thread.currentThread().getName());
    --count;
    out = (out + 1) % size;
    notify(); // if this is not a notifyAll() we might notify the wrong waiter
    return (o);
  }
  
  //--- the producer
  static class Producer extends Thread {
    static int nProducers = 1;
    BoundedBuffer buf;

    Producer(BoundedBuffer b) {
      buf = b;
      setName("P" + nProducers++);
    }

    @Override
	public void run() {
      try {
        while(true) {
          // to ease state matching, we don't put different objects in the buffer
          buf.put(DATA);
        }
      } catch (InterruptedException e){}
    }
  }
  
  //--- the consumer
  static class Consumer extends Thread {
    static int nConsumers = 1;
    BoundedBuffer buf;

    Consumer(BoundedBuffer b) {
      buf = b;
      setName( "C" + nConsumers++);
    }

    @Override
	public void run() {
      try {
        while(true) {
            Object tmp = buf.get();
        }
      } catch(InterruptedException e ){}
    }
  }

  //--- the test driver
  public static void main(String [] args) {
    readArguments( args);
    //System.out.printf("running BoundedBuffer with buffer-size %d, %d producers and %d consumers\n", BUFFER_SIZE, N_PRODUCERS, N_CONSUMERS);

    BoundedBuffer buf = new BoundedBuffer(BUFFER_SIZE);
   
    for (int i=0; i<N_PRODUCERS; i++) {
      new Producer(buf).start();
    }
    for (int i=0; i<N_CONSUMERS; i++) {
      new Consumer(buf).start();
    }
  }
  
  static void readArguments (String[] args){
    if (args.length > 0){
      BUFFER_SIZE = Integer.parseInt(args[0]);
    }
    if (args.length > 1){
      N_PRODUCERS = Integer.parseInt(args[1]);      
    }
    if (args.length > 2){
      N_CONSUMERS = Integer.parseInt(args[2]);      
    }
  }
}
