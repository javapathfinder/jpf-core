# Project Ideas

Please note that this list is not exclusive. If you have other ideas and topics related to JPF, please let us know on \<jpf.gsoc [at] gmail.com\> or the [JPF Google group](https://groups.google.com/forum/#!forum/java-pathfinder).

### JPF Infrastructure

* [Upgrading the Build System from Ant to sbt](#upgrading-the-build-system-from-ant-to-sbt)

* [Support Java 9 for jpf-core](#support-java-9-for-jpf-core) <quocsangphan>

* [Visualization of Execution Traces v2](#visualization-of-execution-traces-v2)

### JPF Application Domains

* [Model Checking Distributed Java Applications](#model-checking-distributed-java-applications) <Nastaran><Cyrille>

* [Verification of Multi Agent Systems](#verification-of-multi-agent-systems) <Franco><Eric><CheckWithNeha>

<!--* [Verification of Actor-based Systems](#verification-of-actor-based-systems)--> <Nastaran>

* [Verification of Event-Driven Applications](#verification-of-event-driven-applications) <Oksana>

<!-- * [Verification of epistemic properties of Java programs](#verification-of-epistemic-properties-of-java-programs)--> <Franco><Nikos>

### Separation Logic

* [Concolic execution with separation logic](#concolic-execution-with-separation-logic) <Loc><Sang>

* [Synthesis to repair heap-manipulating programs](#synthesis-to-repair-heap-manipulating-programs) <Loc><Sang>

* [Verification of unbounded heap-manipulating programs via learning](#verification-of-unbounded-heap-manipulating-programs-via-learning) <Loc><Sang>

### Symbolic Execution

* [Refactoring SPF constraint library](#refactoring-spf-constraint-library) <Mike>

* ["Higher order" veritesting](#higher-order-veritesting) <Mike>

<!-- * [Handling Native Calls in the Context of Symbolic Execution](#handling-native-calls-in-the-context-of-symbolic-execution)--> <Corina><Nastaran>

* [Comparison between concolic execution and classical symbolic execution](#comparison-between-concolic-and-classical-symbolic-execution) <Willem>

<!-- * [Generic GREEN](#generic-green) <Willem> -->

<!-- * [Improving Symbolic PathFinder](#improving-symbolic-pathfinder) <Kasper><Corina>

* [Improving Sampling of Symbolic Paths](#improving-sampling-of-symbolic-paths) <Kasper>

-->

### Fuzzing

* [Whitebox Fuzzer and Grammar Learner](#whitebox-fuzzer-and-grammar-learner) <Willem>

* [Fuzzing and Symbolic Execution](#fuzzing-and-symbolic-execution) <Willem>

### Smart Contract

* [Smart Contract Analysis](#smart-contract-analysis) <Willem>

### Android

* [Analysis of Android Applications](#analysis-of-android-applications)

### Concolic Execution

* [JDart for Dynamic Taint Analysis](#jdart-for-dynamic-taint-analysis) <Falk>

* [New Features for JDart](#new-features-for-jdart) <Kasper>

* [Concolic Execution for Android Apps](#concolic-execution-for-android-apps) <Kasper>

* [Support for parallel or distributed exploration in JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)

* [Regression tests for JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)

### Environment and Test Case Generation

* [Environment and Test Case Generation for Specific Domains](#environment-and-test-case-generation-for-specific-domains) <Oksana>

<!-- * [Environment and Test Case Generation for Symbolic Execution](#environment-and-test-case-generation-for-symbolic-execution) <Oksana>

* [Test Case Generation for Evolving Applications](#test-case-generation-for-evolving-applications) <Oksana>

### JPF Extensions and External Systems Interfacing

* [Evaluating jpf-psyco](#evaluating-jpf-psyco) <Kasper><CheckWithFalk>
-->



### Projects Descriptions


#### Upgrading the Build System from Ant to sbt
The goal of this project is to improve the JPF build system. Currently, JPF uses Ant, and this project includes changing the JPF build system to [sbt](http://www.scala-sbt.org/). This also includes bringing the configuration mechanism of JPF under sbt. Currently, the configuration mechanism is part of the core of JPF, [jpf-core](https://github.com/javapathfinder/jpf-core). The goal is to make this functionally as part of the build system.

#### Support Java 9 for jpf-core
jpf-core is essentially a JVM that currently supports only Java 8. The goal of this project is to make it up-to-date with new features of Java 9. First, the JPF source itself has to be compatible with Java 9. Second, JPF should support new features of Java 9 bytecode and archives. Among new features of Java 9 are multi-version archives (JAR files) and the ability to link JAR files before they are used by the JVM.

#### Visualization of Execution Traces v2
JPF is able to find notorious concurrency bugs such as deadlocks. Although finding bugs is one of the major strengths of JPF, providing feedback to the programmer is one of its main weaknesses. For example, for a deadlock JPF provides the programmer at which line each thread is stuck. Although this is of some use, what is much more valuable is to report how each thread got to that point.
This project is concerned with extending [jpf-visual, a visual analytics tool from GSoC 2017](https://bitbucket.org/qiyitang71/jpf-visual/overview). The web site has about a dozen possible enhancements listed as [open issues](https://bitbucket.org/qiyitang71/jpf-visual/issues?status=new&status=open) Some of these may be feasible to implement as a GSoC project. Other ideas for enhancements are of course also welcome.


<!--#### Procedure Summaries
Implement construction of procedure summaries and their usage during the state space traversal. An important part of the project will be a survey of existing literature on the topic of generating summaries, and identification of an approach that is suitable in the context of the JPF code base. Key aspects of the solution include 

1. designing representation of the summaries that captures all side effects of the given procedure; and 
2. implementing modifications of the program state. 

The actual summary of a method will be computed during its first execution, and then reused within traversal of other state paths. Another possible feature is the support for externally defined summaries that would be useful for library methods.-->

#### Model Checking Distributed Java Applications
[jpf-nas](http://babelfish.arc.nasa.gov/hg/jpf/jpf-nas) is an extension of JPF that provides support for model checking distributed multithreaded Java applications. It relies on the multiprocess support included in the [JPF core](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core) which provides basic functionality to verify the bytecode of distributed applications. jpf-nas supports interprocess communication via TCP sockets by modeling the Java networking package java.net. This tool can handle simple multi-client server applications. Some examples can be found in the jpf-nas distribution (at jpf-nas/src/examples/). The goal of this project is to extend the functionality of jpf-nas in various ways, such as extending the communication model supported by the tool towards an existing open source Java library/framework, called [QuickServer](http://www.quickserver.org/), increasing the performance of the tool by improving the mechanism used to manage the state of communication objects, extending the tool with the cache-based approach used in [net-iocache](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/net-iocache), etc.

#### Verification of Multi Agent Systems
The goal of this project is to develop techniques that analyze key properties in multi-agent systems. The [jpf-mas](http://dl.acm.org/citation.cfm?id=2485058) extension will initially provide the ability to generate the reachable state space of Brahms models. The reachable state space can then be encoded into input for a variety of model checkers such as SPIN, NuSMV and PRISM, thereby enabling the verification of LTL, CTL and PCTL properties. The project will also need to investigate how to generate the set of reachable states for other kinds of models, such as Jason models, and how to compose reachable states of different modelling languages both at run-time and off-line.

#### Synthesis to repair heap-manipulating programs
This project aims to automatically repair a data structure if it is implemented incorrectly, given its specification in separation logic. This project is based on [Java StarFinder](https://github.com/star-finder/jpf-star), a new symbolic execution engine developed in GSoC 2017. Reference:

  - [Enhancing Symbolic Execution of Heap-based Programs with Separation Logic for Test Input Generation](https://arxiv.org/abs/1712.06025).
  - [Assertion-based repair of complex data structures](https://dl.acm.org/citation.cfm?id=1321643).

#### Verification of unbounded heap-manipulating programs via learning
The goal of this project is to prove (or refute) a Hoare triple *{Pre}P{Post}*, where the program *P* contains unbound loops, and loop invariants are not available. We will use [Java StarFinder](https://github.com/star-finder/jpf-star) with the precondition *{Pre}* to generate test inputs for the program. We then execute the program with these inputs, and synthesize loop invariants from program executions using machine learning techniques. The resulted loop invariants will be validated by verification. If an invariant is invalid, we will obtain a counter-example, which is then used as a new test input, and the process repeats. Reference:

  - [Enhancing Symbolic Execution of Heap-based Programs with Separation Logic for Test Input Generation](https://arxiv.org/abs/1712.06025).
  - [Learning Shape Analysis](https://www.microsoft.com/en-us/research/wp-content/uploads/2017/06/veriml.pdf).
  
#### Concolic execution with separation logic
[Java StarFinder](https://github.com/star-finder/jpf-star) (JSF) currently performs classical symbolic execution on heap-based programs. This project aims to extends JSF with concolic execution. Reference:

  - [Enhancing Symbolic Execution of Heap-based Programs with Separation Logic for Test Input Generation](https://arxiv.org/abs/1712.06025).
  - [JDart: A Dynamic Symbolic Analysis Framework](https://github.com/psycopaths/jdart).

<!--#### Verification of Actor-based Systems
The goal of this project is verifying actor-based systems using the model checking technique. This can be achieved through extending the [jpf-actor](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-actor) extension of JPF. jpf-actor is a framework that can be used to systematically test actor programs that compile to bytecode. It requires making the codebase of jpf-actor up-to-date with the current version of [jpf-core](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core) which is compatible with Java 8. It also includes extending the project towards an actor-based Scala library, called [Akka](http://akka.io).-->

#### Verification of Event-Driven Applications
The goal of this project is to evaluate and/or advance existing state-of-the-art tools for analysis of event-driven applications, for example jpf-awt, jpf-android. Some of the issues that need to be addressed include event sequence generation, search heuristics, and scalability. Other related problems and solutions are welcome.

<!-- #### Verification of epistemic properties of Java programs
Epistemic properties allow to reason about the "knowledge" of an agent in a system. Epistemic properties have been employed for several years in the specification and verification of a range of scenarios, from security to communication protocols to mental states of human agents such as pilots. There is a solid theory for epistemic properties based on the notion of observable states, and this project will build upon this. In particular, this project's aim is to extend JPF so that one can: 

* describe agents' observable state;
* write epistemic specifications, e.g., using annotations;
* algorithmically prove these assertions, or generate counter-examples.

This project may use components such as jpf-symbc, jpf-concolic and jpf-abstraction and external tools such as SMT solvers. -->

#### Refactoring SPF constraint library 
SPF constraints need to be refactored to allow different kinds of constraints to be combined during the construction of a path condition. An example of how it should be after the refactoring is the Abstract Syntax Tree constructed by [GREEN](http://dl.acm.org/citation.cfm?id=2393665).

#### "Higher Order" veritesting 
<!-- Bringing in runtime information to improve the performance of static regions as an improvement to the [veritesting implemented as a Google Summer of Code 2017 project](https://jpf.byu.edu/gsoc17/projects/projects.html#increasing-spf-performance-with-bounded-static-symbolic-execution). -->

It is well known that one of the limiting factors in symbolic execution is the path explosion problem; complex programs have billions of paths that make exhaustive exploration computationally infeasible. Recently, an approach for dramatically reducing the number of paths was proposed by Brumley in the paper: Enhancing Symbolic Execution with Veritesting, which led to dramatic performance improvements for an x86-assembly-based symbolic execution engine. This engine, run against the Debian Linux distribution, found over 11000 crash bugs and 154 privilege escalation bugs in the distribution, and achieved far greater coverage than previous approaches.

Last year, in Google Summer of Code 2017, we implemented initial support for static regions (similar to Veritesting) in JPF, and we have seen 100x - 1000x speedups on some models, such as the TCAS and the WBS benchmarks.  However, on other benchmarks we see little performance improvement because non-local control jumps and object creation limit the applicability of static regions, especially due to the JVM's use of exceptions and the nature of Java programs as opposed to C/C++ programs (e.g., small dynamically-dispatched methods).

In this project, we would dramatically improve the applicability of static regions by examining "partial static regions" that contain most of the paths through a function, but skip paths involving exceptions or object creation.  These would be handled by coordinating with SPF by adding a path constraint to SPF to handle the remaining "exceptional" paths.  In addition, we will support for "higher order" static regions, in which we inject the region for a function into another region.  With these additions, we expect to achieve 100x to 1000x speedups for arbitrary Java programs for symbolic execution.

In addition, we would like to examine the effect of static regions on other aspects of symbolic execution, such as test generation and probabilistic symbolic execution.

<!-- #### Handling Native Calls in the Context of Symbolic Execution
The goal of this project is to handle native calls in the context of symbolic execution by generating native peers and associating them with native methods on-the-fly. For the native peers we need concrete values to be used as input parameters for automatically generated native peers methods. The idea is to first solve the constraints obtained with symbolic execution and use those solutions as input parameters. This can be accomplished by enhancing [jpf-symbc](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc) to use the [jpf-nhandler](https://bitbucket.org/nastaran/jpf-nhandler) extension of JPF. -->

#### Whitebox Fuzzer and Grammar Learner
Build a whitebox fuzzing tool on top of Symbolic PathFinder, that can learn the input grammar of a piece of code in an iterative fashion. The idea would be to first run the code on symbolic input of a fixed length and then learn a possible grammar for this length, at that point extend the length and generalise the grammar. The main research goal behind this project is to see if one can do whitebox fuzzing without a pre-determined seed file (which is the way most whitebox fuzzers work at the moment). 

#### Fuzzing and Symbolic Execution
Develop a fuzzer for Java that can be integrated with SPF (or another Java based symbolic execution engine). The idea would be that when fuzzing gets stuck and makes no progress that the symbolic analysis can create a new seed file to allow analysis to progress.

#### Comparison between concolic and classical symbolic execution
Comparison between concolic execution, e.g. DEEPSEA and JDart, and classical symbolic execution, e.g. SPF.

<!-- #### Generic GREEN
[GREEN](http://dl.acm.org/citation.cfm?id=2393665) is a framework being used to cache constraints with satisfiability and model counting results during analysis. It is used in several jpf-symbc extensions for probabilistic software analysis. Recently there has been a number of suggested improvements to optimise such caching. This project will focus on two ideas: one reusing previous solutions for SAT checking and secondly to store the characterising function for model counting produced by a tool like Barvinok to allow one to reuse model counting solutions even when bounds on variables change. -->

<!-- #### Improving Symbolic PathFinder
This project idea seeks to improve Symbolic PathFinder by adding unit tests and improving the quality of the code base.

#### Improving Sampling of Symbolic Paths
We are working on a number of tools that use jpf-symbc for sampling symbolic paths in order to maximize some reward function. For example, [jpf-reliability](http://dl.acm.org/citation.cfm?id=2643011) is a probabilistic software analysis tool for programs with probabilistic and nondeterministic behavior. It synthesizes schedulers, i.e. resolutions of nondeterminism, that maximizes (or conversely minimizes) the probability of a property being satisfied.

Common to these tools is that sampling is highly parallelizable and a possible direction of this project could be to design and implement a distributed infrastructure for these tools. Other improvements can also be experimented with, e.g., state pruning algorithms, constraints caching, and other algorithms for sampling paths.

#### Verification and Testing Heap-based Programs with Symbolic PathFinder
The goal of this project is to extend Symbolic PathFinder to verify Separation Logic assertions, and to improve test-case generation for heap-based programs. Currently, SPF uses "lazy initialization", which is a brute-force enumeration of all heap objects that can bind to the structured inputs accessed by the program. This explicit enumeration may identify many invalid heap configurations that violate properties of the data structures in the heap, which leads to a huge amount of false alarms. -->

#### Analysis of Android Applications
Various ideas are welcome here. Here are a couple of possible subprojects:

1. The goal of this project is collecting interesting Android applications, and evaluating and applying [JPF-Android](https://heila.bitbucket.io/jpf-android/) to analyze them. JPF-Android is an extension to JPF used to model check Android Applications. Android applications have many dependencies which make them hard to test and verify. They also require events to drive the execution of the applications.  The goal of this project is to identify interesting Android applications to run on ]JPF-Android and then  evaluate the efficiency and effectiveness of the tool  on these apps. You will be using/improving existing approaches to generate stubs and models for applications and then compare the coverage and runtime on JPF-Android to other dynamic analysis tools.


2. This project includes using [JPF-Android](https://heila.bitbucket.io/jpf-android/) to generated test sequences for android applications, and implementing a tool to convert these sequences into tests that can be run on the emulator. JPF-Android verifies Android applications outside of the Android software stack on JPF using a model environment to improve coverage and efficiency. It generates event sequences to drive the execution of the application during exploration. Each sequence also includes the configuration of the environment (device) for which the sequence was executed. This project uses the AndroidViewClient API in Python to run the set of event sequences as detected by JPF-Android on  an emulator to find the number of valid sequences and the code coverage they obtain compared to JPF-Android.

#### Smart Contract Analysis
Develop a mechanism to allow the analysis of Ethereum Virtual Machine (EVM) bytecode by replacing the JVM bytecodes with EVM bytecodes within JPF. The second part of the project would be to extend the bytecodes further to allow symbolic execution as well. 

#### JDart for Dynamic Taint Analysis
JDart is a dynamic symbolic execution engine Java based on Java PathFinder (JPF). The tool executes Java programs with concrete and symbolic values at the same time and records symbolic constraints describing all the decisions along a particular path of the execution. These path constraints are then used to find new paths in the program. Concrete data values for exercising these paths are generated using a constraint solver.

Currently JDart has two mechanisms for marking data values in a program for symbolic analysis. First, JDart analyzes methods during starting symbolic analysis. All parameters of an analyzed method are treated symbolically. Second, special annotations can be used to mark class members that should be analyzed symbolically. This mode of operation is geared towards generating test cases as well as the symbolic summaries of methods.

The goal of this project is to add a new mode of operation to JDart, in which symbolic variables can be introduced dynamically during execution – and also be made purely concrete again. This can be done by introducing symbolic variables for (or removing symbolic annotations from) the return values of specific methods (e.g., Random.nextInt()) – using listeners provided by JPF. This new mode of operation would have two benefits:

1. Existing programs can be analyzed without modifications. And analysis is not confined to individual methods. This will, e.g., enable JDart to analyze the Java programs in the SVCOMP benchmarks ([Injection Flaws](https://www.owasp.org/index.php/Top_10_2007-Injection_Flaws)).
2. It is one step towards enabling using JDart for dynamic taint analysis, e.g., for analyzing potential for injection attacks ([Minepump](https://github.com/sosy-lab/sv-benchmarks/tree/master/java/MinePump)). Web-applications tend to contain code of form: 
```
u = Request.getValueOf(“user”); //Taint u (symbolically) 
query = “SELECT user FROM table WHERE uid=”+u; //query becomes tainted
query = santitize(query); //Un-taint query (symbolically.) 
db.select(query);
```
If the value of u is not properly analyzed and sanitized, an attacker can exploit this code to gain access to arbitrary information in the applications database. Making the return value of Request.getValueOf(“user”) symbolic, is the first step towards tracing taint of this value.

Please note: For a full dynamic taint analysis, JDart would also have to be extended to analyze strings and arrays symbolically --- this is out of the scope of this project.


#### New Features for JDart
[JDart](https://github.com/psycopaths/jdart) is an open-source, dynamic symbolic analysis framework built on Java PathFinder. It has been applied to industrial scale software, including complex NASA systems. 

We have many ideas for improving JDart and welcome additional ideas too:

1. JDart supports various (fixed-size) symbolic data structures, e.g., arrays and HashMap. It would be interesting to improve this, in particular with symbolic array indexes and support for unbounded data structures possibly with Lazy Initialization.
2. Add more exploration strategies to JDart. In particular, new exploration heuristics could be interesting, e.g., for targeted concolic execution. It could also combine other static analyses, such as, program slicing, where the computed slice can be used to constrain the exploration.
3. [JConstraints](https://github.com/psycopaths/jconstraints) is a solver abstraction layer used by JDart to interact transparently with SMT solvers. JConstraints has support for some solvers, e.g., Z3 and SMTinterpol. Often, however, one needs to select a solver that is best suited for the constraints generated (e.g., linear, non-linear). It could be very useful adding additional solvers to JDart based on JConstraints and evaluate them to understand better their applicability. This could also comprise adding a general interface to solvers that support the SMTLib format.

#### Concolic Execution for Android Apps
[JDart](https://github.com/psycopaths/jdart) is an open-source, dynamic symbolic analysis framework built on Java PathFinder. It has been applied to industrial scale software, including complex NASA systems. This project seeks to extend this capability to Android applications by supporting the Dalvik instruction set, e.g., by using [jpf-pathdroid](http://babelfish.arc.nasa.gov/hg/jpf/jpf-pathdroid). This would enable analyses build on JDart to also work for Android, e.g., automated test case generation, finding bugs, and program understanding. 

#### Support for parallel or distributed exploration in JDart and Regression tests for JDart
Here is an incomplete list of ideas and projects for extending and improving JDart. If you would like to work on any of the projects or have your own ideas (or just want to contribute to JDart), let us know and we can talk more.

* Add regression test suite
* Add visualization capabilities to JDart. This could be really useful -- especially for program understanding and debugging. It could be as simple as translating the constraints tree to DOT, but it would be a great feature to have more powerful, interactive visualizations, e.g., browser-based with d3, or using Gephi, yEd, jung, prefuse, or jgraph.
* Finish (or re-implement) [JConstraints](https://github.com/psycopaths/jconstraints) SMTLib interface and experiment with other solvers. dReal is (partly) integrated using this, but it would be interesting to experiment more
* Add more search heuristics and evaluate
* Make support for [PathDroid](http://ti.arc.nasa.gov/opensource/projects/pathdroid/). This would allow JDart to analyze Dalvik bytecode programs
* Work on constraints caching in a similar fashion as [Green](http://www.cs.sun.ac.za/~jaco/PUBS/vgd12.pdf) (or make support for Green)
* Add support for parallel/distributed exploration
* Add support for symbolic data structures. Maybe ala [Lazy Initialization](http://users.ece.utexas.edu/~khurshid/testera/GSE.pdf)
* Improve test suite generation from symbolic analysis. Currently, static target methods are fully supported while instance methods are only partly supported
* General refactoring and code improvement


#### Environment and Test Case Generation for Specific Domains
When model checking applications belonging to specific domains (e.g., Swing, Android), 
JPF users have to provide application environment, consisting of test drivers and models 
for libraries that are too complex for JPF to run. The goal of this project is to evaluate the
existing (or provide new) semi-automated support for generation of test drivers and library 
models/stubs based on the results of domain-specific static analysis, specifications, run-time 
information, or other suitable techniques. Once generated, such drivers and stubs can 
be used to verify applications belonging to specific domains using appropriate jpf 
extensions (e.g., [jpf-awt](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-awt), [jpf-android](https://bitbucket.org/heila/jpf-android)). The project can be implemented on top of 
[OCSEGen](http://ti.arc.nasa.gov/publications/8752/download) or another suitable tool.

<!-- #### Environment and Test Case Generation for Symbolic Execution
When using Symbolic PathFinder (SPF), one needs to supply application environment, consisting of test drivers and models/stubs for libraries that are too complex for SPF to handle. The goal of this project is to evaluate the existing (or provide new) semi-automated support for generation of test drivers and library models/stubs, containing symbolic values, based on the results of domain-specific static analysis, specifications, run-time information, or other suitable techniques. Once generated, such drivers and stubs can be used to verify applications using SPF. The project can be implemented on top of [OCSEGen](http://ti.arc.nasa.gov/publications/8752/download) or another suitable tool.

#### Test Case Generation for Evolving Applications
The goal of this project is to evaluate and/or advance existing state-of-the-art tools for test suite generation and augmentation based on software changes between versions. Tools like jpf-symbc, Randoop, Evosuite can be used to generate unit tests for an application under test. Other related tools and ideas are welcome.


#### Evaluating jpf-psyco
[jpf-psyco](https://github.com/psycopaths/psyco) is an open-source tool built on JPF for generating temporal component interfaces. A temporal interface is expressed as a finite-state automaton over the public methods of the component and captures safe ordering relationships of method invocations. jpf-psyco relies on a combination of symbolic execution and automata learning for generating interfaces. This project seeks to evaluate jpf-psyco with new examples (e.g. reactive systems) and experimenting with other learning algorithms.
-->
