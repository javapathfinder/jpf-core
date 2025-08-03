From the previous two sections, you have learned that JPF has one recurring, major theme: it is not a monolithic system, but rather a configured collection of components that implement different functions like state space search strategies, report generation and much more. Being adaptive is JPF's answer to the scalability problem of software model checking.

This not only makes JPF a suitable system for research, but chances are that if are you serious enough about JPF application, you sooner or later end up extending it. This section includes the following topics which describe the different mechanisms that can be used to extend JPF.

 * [Top-level design](Search-Strategies)
 * Key mechanisms, such as 
     - [ChoiceGenerators](ChoiceGenerators)
     - [Partial order reduction](Partial-Order-Reduction)
     - [Slot and field attributes](Slot-and-field-attributes)
 * Extension mechanisms, such as
     - [Listeners](Listeners)
     - [Search Strategies](Search-Strategies)
     - [Model Java Interface (MJI)](Model-Java-Interface)
     - [Bytecode Factories](Bytecode-Factories)
 * Common utility infrastructures, such as
     - [Logging system](Logging-system)
     - [Reporting system](Reporting-system)
 * [Running JPF from within your application](Running-JPF-from-application)
 * [Writing JPF tests](Writing-JPF-tests)
 * [Coding conventions](Coding-convention)
 * [Hosting an Eclipse plugin update site](Host-Eclipse-plugin-update-site) 

{% include navigation.html %}
