## Error Trace Generator ##

This is a listener to output a lightweight error trace. It prints the instructions at POR boundaries or points where there are multiple choices. An example is shown below.  

~~~~~~~~

====================### Lightweight Error Trace ###=======================


Length of Error Trace: 35
--------------------------------------------------- Thread1
 Event.wait_for_event(oldclassic.java:79)
      wait();
--------------------------------------------------- Thread2
 SecondTask.run(oldclassic.java:129)
      if (count == event2.count) { // <race> ditto
--------------------------------------------------- Thread2
 SecondTask.run(oldclassic.java:127)
      event1.signal_event();       // updates event1.count
--------------------------------------------------- Thread2
 SecondTask.run(oldclassic.java:133)
      count = event2.count;        // <race> ditto
--------------------------------------------------- Thread1
 FirstTask.run(oldclassic.java:103)
        event1.wait_for_event();
--------------------------------------------------- Thread1

~~~~~~~~

Configuration: **+listener=gov.nasa.jpf.listener.ErrorTraceGenerator**

Note the Error trace generator does not have the same memory bottlenecks as **report.console.property_violation=trace** that stores every bytecode instruction executed along the path from the start of the program to the error state. The error trace generator dynamically recreates the counterexample by tracing back to the start from the error state. The head of the error trace (list shown in the example) represents the last instruction in the error trace while the tail represents the first instruction.  
