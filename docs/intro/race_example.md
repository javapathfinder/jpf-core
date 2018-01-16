# Example: Data Race #

That's nice, but of course we also could have provoked the error in our random value example by using explicit loops instead of the `Random.nextInt()` calls, i.e. by explicitly enumerating all possible `a` and `b` values in our program. This would be typically done in a program that is a dedicated test driver, in a process which is called *systematic testing*. However, the program we want to verify might not be a test driver, and we might not even have the sources so that we could modify it accordingly.

But the real show stopper for systematic testing lies within the instructions representing choices: at the application level, we might neither be aware of that there are choices, what the choice values are, nor be able to explicitly pick them.

To demonstrate this point, let us look at a little concurrency example using two threads of execution. Quite obviously, the program produces different results depending on if line (2) or (4) gets executed first. But assuming we can't control what happens in (1) and (2), this time we cannot explicitly enumerate the choices - they are made by the system scheduler, i.e. outside of our application.

~~~~~~~~ {.java}
public class Racer implements Runnable {
 
     int d = 42;
 
     public void run () {
          doSomething(1000);                   // (1)
          d = 0;                               // (2)
     }
 
     public static void main (String[] args){
          Racer racer = new Racer();
          Thread t = new Thread(racer);
          t.start();
 
          doSomething(1000);                   // (3)
          int c = 420 / racer.d;               // (4)
          System.out.println(c);
     }
 
     static void doSomething (int n) {
          // not very interesting..
          try { Thread.sleep(n); } catch (InterruptedException ix) {}
     }
}
~~~~~~~~

Chances are, we don't encounter this defect at all during normal testing:

~~~~~~~~ {.bash}
> java Racer
10
> 
~~~~~~~~

Not so with JPF. Being a real virtual machine, there is nothing we can't control. And being a different kind of a Java virtual machine, JPF recognizes that 'racer' is an object that is shared between two threads, and hence executes all possible statement sequences / scheduling combinations in which this object can be accessed. This time, we give the complete output, which also shows the trace (the execution history) that lead to the defect found by JPF:

~~~~~~~~ {.bash}
> bin/jpf Racer
JavaPathfinder v4.1 - (C) 1999-2007 RIACS/NASA Ames Research Center
====================================================== system under test
application: /Users/pcmehlitz/tmp/Racer.java

====================================================== search started: 5/24/07 12:32 AM
10
10

====================================================== error #1
gov.nasa.jpf.jvm.NoUncaughtExceptionsProperty
java.lang.ArithmeticException: division by zero
        at Racer.main(Racer.java:20)

====================================================== trace #1
------------------------------------------------------ transition #0 thread: 0
gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet {>main}
      [insn w/o sources](282)
  Racer.java:15                  : Racer racer = new Racer();
  Racer.java:1                   : public class Racer implements Runnable {
      [insn w/o sources](1)
  Racer.java:3                   : int d = 42;
  Racer.java:15                  : Racer racer = new Racer();
  Racer.java:16                  : Thread t = new Thread(racer);
      [insn w/o sources](51)
  Racer.java:16                  : Thread t = new Thread(racer);
  Racer.java:17                  : t.start();
------------------------------------------------------ transition #1 thread: 0
gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet {>main,Thread-0}
  Racer.java:17                  : t.start();
  Racer.java:19                  : doSomething(1000);                   // (3)
  Racer.java:6                   : try { Thread.sleep(n); } catch (InterruptedException ix) {}
      [insn w/o sources](2)
  Racer.java:6                   : try { Thread.sleep(n); } catch (InterruptedException ix) {}
  Racer.java:7                   : }
  Racer.java:20                  : int c = 420 / racer.d;               // (4)
------------------------------------------------------ transition #2 thread: 1
gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet {main,>Thread-0}
  Racer.java:10                  : doSomething(1000);                   // (1)
  Racer.java:6                   : try { Thread.sleep(n); } catch (InterruptedException ix) {}
      [insn w/o sources](2)
  Racer.java:6                   : try { Thread.sleep(n); } catch (InterruptedException ix) {}
  Racer.java:7                   : }
  Racer.java:11                  : d = 0;                               // (2)
------------------------------------------------------ transition #3 thread: 1
gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet {main,>Thread-0}
  Racer.java:11                  : d = 0;                               // (2)
  Racer.java:12                  : }
------------------------------------------------------ transition #4 thread: 0
gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet {>main}
  Racer.java:20                  : int c = 420 / racer.d;               // (4)

====================================================== search finished: 5/24/07 12:32 AM
>
~~~~~~~~

Looking at the output created by our test program, we see the result `"10"` printed twice, but that doesn't confuse us anymore. From our first example, we know this simply means that JPF first tried two scheduling sequences that normally terminated the program without provoking the defect, before finally picking the one that causes the error.

It still might be a bit confusing that the printed trace contains some source lines twice. Ignoring the details of its choice generation mechanism, this is caused by JPF executing bytecode instructions, not source lines, and a single source line can easily get translated into a number of bytecode instructions. This would go away if we configure JPF so that it reports the executed bytecode, but at the cost of much larger trace that is harder to read. What is more interesting is that JPF tells us about the thread choices it made in each transition (the lines starting with `gov.nasa.jpf.jvm.ThreadChoice..`).
