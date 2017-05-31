# IdleFilter #

The `gov.nasa.jpf.listener.IdleFilter` is a listener that can be used to close state spaces with loops. Consider a simple "busy waiting" loop

~~~~~~~~ {.java}
for (long l=0; l<10000000; l++);
~~~~~~~~

While not a good thing to do in general, it is benign if executed in a normal VM. For JPF, it causes trouble because it adds a lot of useless steps to the stored path, and slows down execution considerably.

In addition, people who expect JPF to match states can get surprised by programs like

~~~~~~~~ {.java}
while (true){
  // no transition break in here
}
~~~~~~~~
not being state matched, and hence not terminating (it wouldn't terminate in a normal VM either). 

`IdleFilter` is a little tool to deal with such (bad) loops. It counts the number of back-jumps it encounters within the same thread and stackframe, and if this number exceeds a configured threshold it takes one of the following actions:

 * warn - just prints out a warning that we have a suspicious loop
 * break - breaks the transition at the back-jump `goto` instruction, to allow state matching
 * prune - sets the transition ignored, i.e. prunes the search tree
 * jump - skips the back-jump. This is the most dangerous action since you better make sure the loop does not contain side-effects your program depends on.


### Properties ###
Consequently, there are two options:

 * `idle.max_backjumps = \<number\>` : max number of back-jumps that triggers the configured action (default 500)
 * `idle.action = warn|break|prune|jump` : action to take when number of back-jumps exceeds threshold

### Examples ###

**(1)** The test program

~~~~~~~~ {.java}
...
public void testBreak () {
  int y = 4;
  int x = 0;

  while (x != y) { // JPF should state match on the backjump
    x = x + 1;
    if (x > 3) {
      x = 0;
    }
  }
}
~~~~~~~~

would never terminate under JPF or a host VM. Running it with

~~~~~~~~ {.bash}
> bin/jpf +listener=.listener.IdleFilter +idle.action=break ...
~~~~~~~~

does terminate due to state matching and produces the following report

~~~~~~~~ {.bash}
...
====================================================== search started: 4/8/10 4:14 PM
[WARNING] IdleFilter breaks transition on suspicious loop in thread: main
        at gov.nasa.jpf.test.mc.basic.IdleLoopTest.testBreak(gov/nasa/jpf/test/mc/basic/IdleLoopTest.java:42)
...
====================================================== results
no errors detected
~~~~~~~~

-----
**(2)** The following program would execute a long time under JPF

~~~~~~~~ {.java}
...
public void testJump () {
  for (int i=0; i<1000000; i++){
    //...
  }

  System.out.println("Ok, jumped past loop");
}
~~~~~~~~

If we run it with

~~~~~~~~ {.bash}
> bin/jpf +listener=.listener.IdleFilter +idle.action=jump ...
~~~~~~~~

JPF comes back quickly with the result

~~~~~~~~ {.bash}
====================================================== search started: 4/8/10 4:20 PM
[WARNING] IdleFilter jumped past loop in: main
        at gov.nasa.jpf.test.mc.basic.IdleLoopTest.testJump(gov/nasa/jpf/test/mc/basic/IdleLoopTest.java:74)
Ok, jumped past loop
...
~~~~~~~~
