<div id="footerContent" style="text-align: center;"><pdf:pagenumber></pdf:pagenumber></div>

# JPF and Google Summer of Code 2012

Java Pathfinder (JPF) is here again in 2012\. We are planning to participate with a number of exciting projects as part of Google Summer of Code (GSoC) 2012\. If you are new to the GSoC program, this is an annual event where Google sponsors students to work on selected open source projects, each student being supported by an experienced mentor. Projects have about 3 month scope, carry a relatively low administrative overhead, can be done remotely, and are generally fun. You also get a cool t-shirt, so what's not to like.

### Interested Students - Contact Us

If you have any questions or suggestions regarding JPF and GSoC, email us at <**jpf.gsoc [at] gmail.com**>. Please be sure to describe your interests and background. The more we know about you, the better we will be able to answer any questions you may have about JPF and/or its potential projects. If you are interested in a project that is not listed here but is relevant to JPF, we would love to hear about it. Join our IRC channel #jpf on freenode to engage in a discussion about all things JPF.

### Timeline

*   03/16 : Java Pathfinder accepted as a mentoring org.
*   03/26 - 04/06: student applications
*   04/20: mentoring org student selection deadline
*   04/23: announcement of accepted students
*   05/21 - 08/20: project work

### Required Skills

JPF is written in Java, and it analyzes Java bytecode. The minimum skill required is to be familiar with Java and having some development experience with Java--class projects or industry experience. At a minimum you should know there is more to it than just the language - it's the language, the libraries and the virtual machine/bytecodes. Not all projects require all levels though, please look at the project descriptions to find out which parts are more important.

JPF is a software verification tool. It is a customizable virtual machine that enables the development of various verification algorithms. It will be to your advantage if you are familiar with formal methods, software testing, or model checking. However, JPF is where research meets development, so for many projects it is not a show stopper. We are looking for students who are motivated, bright, willing to learn, and love to code.

JPF is a fairly complex system. The first step to start is to get JPF [running](https://172.29.0.40/trac/jpf/wiki/user/run) and [configured](https://172.29.0.40/trac/jpf/wiki/user/config). This in itself can be a steep learning curve. It also helps if you already know what [listeners](https://172.29.0.40/trac/jpf/wiki/devel/listener), [bytecode factories](https://172.29.0.40/trac/jpf/wiki/devel/bytecode_factory) and [native peers](https://172.29.0.40/trac/jpf/wiki/devel/mji) are, but now worries - the mentors will help you there. One thing you have to look at, but what is now surprisingly simple is [how to set up JPF projects](https://172.29.0.40/trac/jpf/wiki/devel/create_project).

### Application for Students

You will need to submit a proposal to Google once they open the student application phase (03/26 - 04/06). Please check the [<span class="icon">​</span>details](http://socghop.appspot.com/document/show/gsoc_program/google/gsoc2012/faqs) about the process to see how the whole procedure works.

### Beginners Projects

Due to popularity of beginner projects last year, we've selected several topics that are suitable for JPF novices:

(A) **Interactive JPF tutorial and help system** - prepare a "beginner's guide" to JPF. Select/write simple input programs for demonstrating different extensions of JPF. Reproducible documentation of each step of the process of applying JPF / extensions. Develop a help function as an extension of [JPF shell](https://172.29.0.40/trac/jpf/wiki/projects/jpf-shell).

(B) **Generate JPF Documentation** - Last summer, we offered two beginner projects related to this topic: [jpf-autodoc-options](https://172.29.0.40/trac/jpf/wiki/summer-projects/2011-autodoc-options) and [jpf-autodoc-types](https://172.29.0.40/trac/jpf/wiki/summer-projects/2011-autodoc-types). Both projects were a success and laid a foundation for extracting information from code. We would like to take this work to the next level by unifying the two projects and generating documentation for existing jpf projects.

(C) **Write a Conformance Checker** - In some cases, JPF uses model classes as an alternative for actual Java classes from the standard library. It is important that these classes exhibit the same behavior as the actual classes. One example of a bug would be declaring a public method m() in the model class while there is not a corresponding one in the class from the standard library. This project includes implementing such an investigator, based on the Java reflection, which compares model classes with the standard ones.

(D) **State Diff Viewer** - JPF can be configured to save program state details in a readable text format, which is a suitable basis for comparing the differences between two states. This project would add a specialized diff viewer to the [JPF shell](https://172.29.0.40/trac/jpf/wiki/projects/jpf-shell) that allows selection of the compared states, and presents differences broken down into static fields, heap (objects), stack frames and thread status.

### Advanced Project Ideas

**This is not an exclusive list**! If you have variations, or other project ideas altogether, let us know on <jpf.gsoc [at] gmail.com> or the [<span class="icon">​</span>JPF Google Group](http://groups.google.com/group/java-pathfinder). The sooner, the better.

1.  [Information Flow/Security Analysis](https://172.29.0.40/trac/jpf/wiki/events/soc2012#taint) - implement a data flow analysis using JPF's Attributes system
2.  [Model Checking Android Applications](https://172.29.0.40/trac/jpf/wiki/events/soc2012#android) - use JPF to verify Android components
3.  [Modeling java.net/io Libraries](https://172.29.0.40/trac/jpf/wiki/events/soc2012#net) - scripted java.net model checking
4.  [Scripted File Content](https://172.29.0.40/trac/jpf/wiki/events/soc2012#filecontent) - model file content variations
5.  [Swing UI Model Checking](https://172.29.0.40/trac/jpf/wiki/events/soc2012#swing) - extend the UI model checking script language
6.  [Trace Server](https://172.29.0.40/trac/jpf/wiki/events/soc2012#trace) - Store and post-mortem analyze program traces outside JPF
7.  [Net-iocache](https://172.29.0.40/trac/jpf/wiki/events/soc2012#netiocache) - Analysis of networked software
8.  [Centralization](https://172.29.0.40/trac/jpf/wiki/events/soc2012#centralization) - Conversion of multi-process applications
9.  [Dynamic Load Balancing](https://172.29.0.40/trac/jpf/wiki/events/soc2012#loader) - Support for applying the dynamic load balancing technique to instances of JPF
10.  [Concurrent Trace Visualization](https://172.29.0.40/trac/jpf/wiki/events/soc2012#viz) - Support for visualizing traces that involve several threads of execution
11.  [Automated Test Case Generation for Android apps](https://172.29.0.40/trac/jpf/wiki/events/soc2012#androidsymexe) - Generate test cases using symbolic execution (Symbolic PathFinder)
12.  [Abstract Model Checking](https://172.29.0.40/trac/jpf/wiki/events/soc2012#abstraction) - Reduce large program data domains to small domains, to make the program amenable for verification, via abstract interpretation.
13.  [Reliability Analysis](https://172.29.0.40/trac/jpf/wiki/events/soc2012#reliability) - Implement reliability analysis in Symbolic PathFinder.
14.  [State Comparison](https://172.29.0.40/trac/jpf/wiki/events/soc2012#statediff) - Compare differences between program states.
15.  [Symbolic values in JPF-Inspector](https://172.29.0.40/trac/jpf/wiki/events/soc2012#symbspector) - Extension of JPF-Inspector with basic support for jpf-symbc.
16.  [Input File Variation](https://172.29.0.40/trac/jpf/wiki/events/soc2012#filecontent) - Use scripts to specify file content variations
17.  [Binary Decision Diagrams in JPF](https://172.29.0.40/trac/jpf/wiki/events/soc2012#jpfbdd) - Employ Binary Decision Diagrams to represent part of the state space symbolically, in particular Boolean variables. This is an extension of [<span class="icon">​</span>jpf-bdd](https://bitbucket.org/rhein/jpf-bdd)
18.  [JPF for Multi-Agent Systems](https://172.29.0.40/trac/jpf/wiki/events/soc2012#jpfmas) - Implement multi-agent specific verification tasks in JPF.
19.  [Concolic execution: reporting, visualization and heuristics](https://172.29.0.40/trac/jpf/wiki/events/soc2012#jdartVis) - Targeted reporting and visualization for concolic execution in JPF.
20.  [Application of DiSE](https://172.29.0.40/trac/jpf/wiki/events/soc2012#diseapp) - Implementation of software maintenance application using the output of DiSE
21.  [Dimensional Analysis](https://172.29.0.40/trac/jpf/wiki/events/soc2012#dimension) - specify and check physical units for integers and floating points
22.  [Interval Analysis for Floating Point Arithmetic](https://172.29.0.40/trac/jpf/wiki/events/soc2012#fp) - implement interval arithmetic for floating point operations
23.  [Property patterns for human automation interactions](https://172.29.0.40/trac/jpf/wiki/events/soc2012#jpfhmi) - Introduce property patterns and verification of corresponding properties for human automation interaction systems in jpf-hmi.
24.  [Memoized Symbolic Execution](https://172.29.0.40/trac/jpf/wiki/events/soc2012#memoise) - Implement symbolic execution with memoisation of symbolic execution trees across multiple runs, to facilitate re-use of results (jpf-symbc).
25.  [Verifying Scala Applications](https://172.29.0.40/trac/jpf/wiki/events/soc2012#scala) - make JPF Scala aware
26.  [Running Experiments with JPF](https://172.29.0.40/trac/jpf/wiki/events/soc2012#experiments) - gather and analyze data about JPF runs
27.  [New Solver Architecture for Symbolic PathFinder](https://172.29.0.40/trac/jpf/wiki/events/soc2012#solver) - Implement a stand-alone project for interfacing with decision procedures and constraint solvers
28.  [Jive](https://172.29.0.40/trac/jpf/wiki/events/soc2012#jive) - adding a Jive interface to JPF to utilize Jive's execution visualization

#### Information Flow/Security Analysis

Many software vulnerabilities can be formulated as a tainted object propagation problem (e.g. SQL injection in web applications). In such cases, some input (e.g. a user input) is marked as tainted and the analysis tracks its flow through the program. If the tainted object reaches a vulnerable region of the program (e.g. SQL execute statement), then some malicious attack may happen. Similar analysis can be applied to track other unchecked inputs or unvalidated outputs that may lead to information leakage. JPF implements [Attributes](https://172.29.0.40/trac/jpf/wiki/devel/attributes) that can be used to implement various flavors of taint/information flow analyses, applicable to various domains of Java applications.

* * *

#### Model Checking Android Applications

We haven't been too successful with this during the past years during GSoC, but the 3rd time is a charm. This year, we focus on Android framework modeling ala [jpf-awt](https://172.29.0.40/trac/jpf/wiki/projects/jpf-awt), using the script infrastructure of JPF extended by Android's app lifetime events. You need to have a solid understanding of [MJI and NativePeers](https://172.29.0.40/trac/jpf/wiki/devel/mji) for this topic, and should know your way around the Android framework libraries and esp. the <tt>Activity</tt> model.

* * *

#### Modeling java.net/io Libraries

Last year we covered the backtrackable filesystem model (jpf-bfs). This year, we want to extend this towards java.net and <tt>Sockets</tt>. Again, this should build upon/extend the JPF scripting mechanism to implement lightweight request/response models that can be used to verify networked applications. This is not meant to be a replacement for the more heavy weight [jpf-net-iocache](https://172.29.0.40/trac/jpf/wiki/projects/net-iocache), it aims at using scripts instead of real external processes to drive the model checker.

* * *

#### Scripted file content

This would be an extension of last years jpf-bfs project that models file content variations. Assume your system under test (SUT) reads in some configuration file and you want to verify for a number of different setting. One option is to use data ChoiceGenerators where you assign the configuration values to respective fields or local variables, but this requires having access to and analyzing the SUT sources. Another approach would be to have a set of different configuration files and tell JPF that for a given pathname it should try all of them. This could be even further extended by providing a scripting mechanism that is used to automatically generate these different file variants.

* * *

#### Swing UI Model Checking

The [jpf-awt extension](https://172.29.0.40/trac/jpf/wiki/projects/jpf-awt) allows model checking of Swing applications, using a scripting language to specify user input sequences to explore, which looks like

<pre class="wiki">$MyTextField:input.setText("whatever")
ANY { $Option1.doClick(), $Option2.doClick() }
...
</pre>

Apart from only supporting a small number of Swing components, <tt>jpf-awt</tt> lacks in terms of expressiveness of this scripting language (which happens to be the same syntax like the statemachine scripts). This project would add support for more components like JTree or JTable, and extend the scripting language so that especially choices become more convenient, like <tt>ANY_CHECKBOX_COMBINATION <group-name></tt> or <tt>ANY_LIST_ITEM <list-name></tt>. The scripting could also be extended to include properties like <tt>?TEXT_EQUALS <textfield-name> <expected value></tt>, so that users could completely script verification runs without also having to provide listeners checking such functional properties.

* * *

#### Trace Server; project page: [summer-projects/2010-trace-server](https://172.29.0.40/trac/jpf/wiki/summer-projects/2010-trace-server)

Traces are memory hogs. If you have some production code SUT, it is quite normal that you end up with traces that contain millions of steps (<tt>Instruction</tt> objects). Not good to store millions of objects while you explore the state space and you don't even know yet if you are ever going to need the trace. The normal mitigation is to first run JPF without the "trace" topic in the reports, store the <tt>ChoiceGenerator</tt> path if you hit a defect or otherwise need a trace, and then replay this path with traces turned on if you need more information. This is a bit complicated. This is why the trace server was created in 2010\. With such a database, you can use post mortem analyzers to find out about defects. Post mortem analyzers would not only speed up JPF in the first place, but also avoid having to re-run JPF on a large system under test if you need to try several trace analyzers. Work for 2011 includes creating more analyzers, improvements in the database, and the performance of the entire framework.

* * *

#### Analysis of networked software

[<span class="icon">​</span>Net-iocache](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/net-iocache) is a JPF extension that allows JPF to communicate with other programs over the network, even if backtracking is involved. It has various features such as support for the java.net API, checkpointing tools to run programs in a virtualization environment, and more. Yet, various tasks are still open: (1) We currently have no good notion of the coverage of our test suite, which is becoming an important issue for supporting non-blocking I/O; (2) certain features are hard-coded and should be made configurable; (3) UDP (datagrams) is not supported yet. The first two features are good "warm-up" tasks for getting used to the code, while the last one is harder. In a first step, basic UDP could be supported by making datagram delivery non-deterministic. In a second step, the un-ordered delivery of datagrams could be simulated using choice generators.

* * *

#### Centralization

The goal of this project is to build a Java code transformation tool with the ASM bytecode manipulation library, to automatically transform multiple processes into a single process. This kind of transformation is called Centralization. Applications are numerous, and it would first be used to allow the verification of distributed systems with JPF at NASA. The resulting tool could transform any standard Java program and have a potentially widespread usage beyond the project.

Manipulation of Java bytecode requires interest in, and knowledge of, bytecode or similar machine code, and how program executables are structured into executable code and data. A [<span class="icon">​</span>previous version](http://staff.aist.go.jp/c.artho/papers/centralizer.pdf) of the tool exists, supporting only older Java versions, and it can be used as a reference.

* * *

#### Dynamic Load Balancing

Providing a framework for applying the dynamic load balancing technique on instances of JPF. This can allow for parallel exploration of the state space. This work is mainly based on the two existing listeners in jpf-core, which are TraceStorer and ChoiceSelector. It also includes implementation of a system that could remotely orchestrate the invocation of the different JPF instances.

* * *

#### Concurrent Trace Visualization

Making sense of error traces that involve concurrent threads of execution is difficult because typically only a few threads and context switch locations actually matter in generating the error yet the trace often includes all threads and context switches even though many are not relevant. Currently, the **jpf-guided-test** module provides a search to generate simpler error traces that only includes essential threads and context switch locations deemed necessary to generate an assertion violation. The module further provides a visualization tool to colorize the error trace and enable the user to exam it at various levels of detail to aid in debugging. The work in this project is to extend the visualization tool to use static analysis techniques to post-process error traces from other JPF search strategies (including DFS) to remove non-important threads and context switches from the traces and then visualize those traces to the user for debug in the **jpf-shell**. The work also seeks to improve the trace inspection tools in the visualization toolkit to include better colorization, better replay interactivity, better control over source code viewing, and an extensive heap inspection capability. One possible direction is to integrate the trace replay with jpf-inspector.

* * *

#### Automated Test Case Generation for Android Apps

Use symbolic execution to automatically collect numeric constraints through the Android code; use decision procedures/constraint solvers to automatically find solutions to these constraints; solutions will be used to generate test inputs/test cases that guarantee a certain structural coverage of the code (e.g. statement, branch, path coverage). Extend Symbolic Pathfinder project (jpf-symbc).

* * *

#### Abstract Model Checking

Replace large data domains (e.g. int) with small abstract data domains (e.g. {ZERO, POS, NEG}) and change the bytecode interpretation to perform a non-standard execution of the program in terms of the abstract domains (via abstract interpretation). E.g. ZERO+POS=POS but NEG+POS={ZERO, POS,NEG} since adding a negative and a positive value can be either negative, zero, or positive. Builds off Symbolic Pathfinder (jpf-symbc). Initial implementation available in jpf-abstraction.

* * *

#### Reliability Analysis

Implement analysis that will not only detect errors (as it is currently) but will also give the probability of reaching an error (or conversely will give the probability that the program behaves correctly). Involves extending Symbolic Pathfinder (jpf-symbc).

* * *

#### State Comparison

A well known limitation of the systematic state space traversal as done by JPF is state explosion. This typically occurs because of a high number of possible thread interleavings or many data choices in the SUT. However, state explosion may be caused also by unexpected differences between states of the SUT (for example, different values of counter variables that are not relevant for the checked property). The aim of this project is to extend JPF-Inspector so that users will be able to compare specific states and explore their differences. This way it will help users to find out the causes of state explosion. A new JPF-Shell panel should be implemented for presentation and highlighting of differences between the states.

* * *

#### Symbolic values in JPF-Inspector

Extend JPF-Inspector with support for symbolic execution (Symbolic JPF). This includes printing of symbolic values and path conditions, assigning new symbolic values to program variables, and breakpoints over symbolic variables.

* * *

#### Binary Decision Diagrams in JPF

The project [<span class="icon">​</span>jpf-bdd](https://bitbucket.org/rhein/jpf-bdd) has shown that, in a number of circumstances, representing part of the state space using Binary Decision Diagrams can result in substantial performance improvements. This project aims at extending the class of systems that can benefit from the use of Binary Decision Diagrams.

* * *

#### JPF for Multi-Agent Systems

Multi-Agent systems (MAS) are successfully employed to model a range of applications. This project aims at leveraging the verification capabilities of JPF to support the verification of typical MAS features, such as reasoning about belief states and strategies. A key part of the project consists in defining formally the correspondence between semantics for MAS and JPF execution traces.

* * *

#### Concolic execution: reporting, visualization and heuristics

The aim of this project is to support customized / targeted reporting for jpf-jdart, the concolic execution project within JPF. It will also include better visualization of the computations performed during concolic execution. Finally, if time permits, the project will involve introducing several heuristics for lighter weight concolic execution of complex applications.

* * *

#### Application of DiSE

Directed incremental symbolic execution generates a set of affected program behaviors based on the changes between two related versions. The set of affected program behaviors are to be used to improve one of the software maintenance tasks beyond regression testing.

#### Dimensional Analysis

Ok, everybody loves to make jokes about NASA's trouble with imperial and metric unit conversion (or the lack thereof). Everybody except of the Mars Climate Observer guys - this is a serious problem for verification of technical software. There is JSR-275, but it requires using a fairly involved API, hence is expensive to add after the fact for existing software. Enter the [JPF attribute system](https://172.29.0.40/trac/jpf/wiki/devel/attributes), which allows you to attach your own objects to local vars, operands, fields and even objects themselves. Such attributes automatically travel with the associated values (e.g. when a local var gets pushed), so it is convenient to use them to keep track of units. Specifying them in the first place can be done by annotations (e.g. for fields), or through an initialization API, like <tt>double velocity = Unit.initialize(42.0, Unit.meter_per_second)</tt>, but subsequently you just use the basic types (like <tt>double</tt>) for your computations and JPF does the rest. This project is about implementing such an annotation/attribute system that is easy to use with legacy applications.

#### Interval Analysis for Floating Point Arithmetic

Floating point arithmetic has well known problems with round off and cancellation. JPF already has a small project (jpf-numeric) that implements alternative bytecode semantics to track floating point problems like cancellation. This project would add another approach via new bytecode semantics that implemented interval arithmetic for floating point operations.

* * *

#### Property patterns for human automation interactions

Introduce property patterns for human automation interaction (HAI) systems in jpf-hmi. There will be support for pattern instantiation, as well as verification of corresponding properties. Some case studies will also be performed. In collaboration with General Motors for automobile-related HAI systems.

* * *

#### Memoized Symbolic Execution

Implement and extend symbolic execution with memoization of symbolic execution tree to facilitate re-use of results computed by symbolic execution across multiple runs. Use a DB for storing and retrieving of results. Extends jpf-symbc and jpf-memoise from last year.

* * *

#### Verifying Scala Applications

Scala is cool, Scala compiles into the JVM. Used the right way, it could actually avoid problems that otherwise need JPF for detection. If you stick to the paradigm, there are no shared memory races when using Scala Actors. But there still might be other defects (like deadlocks) you can do, for which JPF could be very helpful. Only that Scala's implementation is done on top of all the "nastiness" of low level Java constructs, which would show in traces and properties. The goal of this project is to make JPF Scala aware, so that for instance deadlocks would be reported at the level of <tt>receive</tt> patterns, i.e. the code a user can see - not the (invisible) code of Scala implementation itself. This is not restricted to actors and concurrency, i.e. could also include Scala specific properties to check for. One should look into Basset (aka as [jpf-actor extension](https://172.29.0.40/trac/jpf/wiki/projects/jpf-actor)) for an example on connecting Scala and JPF. The first thing would be to update Basset to work with the latest versions of JPF and Scala.

* * *

#### Running Experiments with JPF

One of the most tedious tasks while writing a research paper is collecting and processing data from experiments. Aim of this project is to automatize this process. During the first phase of the project student will work with JPF community to determine the most common experiment scenarios. Secondly, implement a standalone application that will use JPF reporting mechanism to gather time/memory/state space statistics from a set of experiments and provide several output formats CVS/Latex/Chart.

* * *

#### New Solver Architecture for Symbolic PathFinder

We want to replace the existing approach to interfacing with decision procedures and constraint solvers with a much more flexible stand-alone project. A prototype already exists that can interface with CVC3 and Choco. This new approach will allow other projects (even outside of JPF) to also reuse the Solver component. Essentially all existing decision procedures and solvers that SPF support must be added to the list, but in addition we would like new ones, such as Z3 to be supported. A strong Java programming background is required here and also a firm understanding of (J)Unit testing, since making sure this system is correct will be paramount.

* * *

#### Jive Interface

This project is about interfacing JPF with Jive, an interactive visualization engine for Java: www.cse.buffalo.edu/jive. We propose to augment JPF's model-checking and state exploration capabilities with Jive's rich visualizations, especially of object states and execution paths. While JPF can help find the path to an error, Jive can help visualize this execution path. Jive currently uses JDI (Java Debugger Interface) to receive its data for debugging, hence we would need to adapt the output produced by JPF Listeners so that it can be used by Jive. JPF Listeners can be used to obtain notification of events pertaining to threads, objects, methods, variables, etc.