# Project Ideas

Please note that this list is not exclusive. If you have other ideas and topics related to JPF, please let us know on the [JPF Google group](https://groups.google.com/forum/#!forum/java-pathfinder).
A possible proposal template can be found at the bottom of our GSoC page: [[JPF Google Summer of Code 2020]].

### JPF Infrastructure

* [Support Java 11 (bootstrap methods) for jpf-core](#support-java-11-bootstrap-methods-for-jpf-core) <Cyrille>

* [Support Java 12 (private API dependencies) for jpf-core](#support-for-java-12-private-api-dependencies-for-jpf-core) <Cyrille>

### JPF Application Domains

* [Model Checking Distributed Java Applications](#model-checking-distributed-java-applications) <Cyrille>

<!-- * [Verification of Multi Agent Systems](#verification-of-multi-agent-systems) <Franco><Eric><CheckWithNeha> -->

<!--* [Verification of Actor-based Systems](#verification-of-actor-based-systems) <Nastaran> -->

<!--* [Verification of Event-Driven Applications](#verification-of-event-driven-applications) <Oksana>-->

<!-- * [Verification of epistemic properties of Java programs](#verification-of-epistemic-properties-of-java-programs) <Franco><Nikos> -->

<!-- ### Separation Logic

* [Verification of unbounded heap-manipulating programs via learning](#verification-of-unbounded-heap-manipulating-programs-via-learning) <Loc><Sang> -->

### Automatic Program Repair

* [Automatic program repair using annotations](#automatic-program-repair-using-annotations) <Bach><Vaibhav><Eric><Corina>

### Symbolic Execution

* [Support Java 11+ for SPF](#support-java-11-for-spf) <Yannic><Corina><Elena>

* [Support gradle for SPF](#support-gradle-for-spf) <Yannic><Corina>

* [Improving String analysis in SPF](#improving-string-analysis-in-spf) <Yannic><Corina>

* [Refactoring SPF constraint library](#refactoring-spf-constraint-library) <Elena>

* [Symbolic PathFinder for Neural Network Analysis](#symbolic-pathfinder-for-neural-network-analysis) <Corina><Sarfraz><Yannic>

<!-- * [Handling Native Calls in the Context of Symbolic Execution](#handling-native-calls-in-the-context-of-symbolic-execution) <Corina><Nastaran> -->

<!--
* [Comparison between concolic execution and classical symbolic execution](#comparison-between-concolic-and-classical-symbolic-execution) -->

<!-- * [Generic GREEN](#generic-green) <Willem> -->

<!-- * [Improving Symbolic PathFinder](#improving-symbolic-pathfinder) <Kasper><Corina>

* [Improving Sampling of Symbolic Paths](#improving-sampling-of-symbolic-paths) <Kasper> -->

* [Hash-consing for SPF](#hash-consing-for-spf) <Vaibhav>

* [Visualizing ChoiceGenerator tree for SPF](#visualizing-choicegenerator-tree-for-spf) <Vaibhav>

* [Combinatorial testing of configuration options for SPF](#combinatorial-testing-of-configuration-options-for-SPF) <Vaibhav>

* [Beneficial path-merging for SPF](#beneficial-path-merging-for-SPF) <Vaibhav>

* [Test generation with path-merging](#test-generation-with-path-merging) <Vaibhav>

### Fuzzing

<!--
* [Whitebox Fuzzer and Grammar Learner](#whitebox-fuzzer-and-grammar-learner)  -->

* [Fuzzing and Symbolic Execution](#fuzzing-and-symbolic-execution) <Corina><Yannic>

### Smart Contract

* [Smart Contract Analysis](#smart-contract-analysis) <Cyrille>

<!-- ### Android

* [Analysis of Android Applications](#analysis-of-android-applications) -->

### Concolic Execution

* [JDart maintenance and scalability](#jdart-maintenance-and-scalability) <Falk>

<!--
* [New Features for JDart](#new-features-for-jdart) <Kasper>

* [Concolic Execution for Android Apps](#concolic-execution-for-android-apps) <Kasper>

* [Support for parallel or distributed exploration in JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)

* [Regression tests for JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)-->

### Environment and Test Case Generation

<!-- * [Environment and Test Case Generation for Specific Domains](#environment-and-test-case-generation-for-specific-domains) <Oksana> -->

* [Model-based Testing with Modbat for JPF](#mbt-modbat) <Cyrille>

* [Method summaries, extended](#method-summaries)<Cyrille><Pavel>

<!-- * [Environment and Test Case Generation for Symbolic Execution](#environment-and-test-case-generation-for-symbolic-execution) <Oksana>

* [Test Case Generation for Evolving Applications](#test-case-generation-for-evolving-applications) <Oksana>

### JPF Extensions and External Systems Interfacing

* [Evaluating jpf-psyco](#evaluating-jpf-psyco) <Kasper><CheckWithFalk>
-->

### Symbolic Data-race Detection

* [Symbolic data-race detection for Habanero Java](#symbolic-data-race-detection-for-habanero-java) <Eric>

### Projects Descriptions

#### Support Java 11 (bootstrap methods) for jpf-core

**Description:**
jpf-core is essentially a JVM that currently fully supports only Java 8. The goal of this project is to make it up-to-date with new features of Java 11. The JPF source itself has already been made compatible with Java 11. Now, JPF should support new features of Java 11 bytecode. The key feature of Java 11 that is currently not supported are bootstrap methods that are generated at load time. They are used for things as common as string concatenation ("Hello, " + name). As of now, a few specialized cases are supported, but there are still many programs (and unit tests) that fail with Java 11. It is therefore very important for us that we support the general case of this feature. The current state can be seen by running the unit tests of branch `java-10-gradle` with Java 11.

This is a high-priority project, as support for Java 8 is limited to the near future.
*Note:* You can apply to both projects (Java 11 or 12 support); in that case, please indicate that you would like to work on either one, and what your preference would be.

**Difficulty:** Hard  
**Required skills:** Knowledge of Java bytecode  
**Preferred skills:** Knowledge of bootstrap methods in Java bytecode

#### Support for Java 12 (private API dependencies) for jpf-core

Related to the project above, there are also some internal APIs from Java 11 that no longer exist in Java 12 and later.
This requires redesigning and reimplementing part of the code, in order to take a different approach that no longer depends on functionality that was removed in Java 12. The code in question is easily found by trying to compile branch `java-10-gradle` with Java 12.
*Note:* You can apply to both projects (Java 11 or 12 support); in that case, please indicate that you would like to work on either one, and what your preference would be.

**Difficulty:** Medium  
**Required skills:** Knowledge of Java internals

#### Support Java 11 for SPF

**Description:**
Symbolic PathFinder is essentially a (symbolic) JVM that currently supports only Java 8. The goal of this project is to make it up-to-date with new features of Java 11.
This is a high-priority project, as support for Java 8 is limited to the near future.

#### Support for gradle for SPF

**Description:**
The goal of this project is to (1) implement gradle support for Symbolic Pathfinder, (2) to update the extension template, including gradle support and updated documentation.

#### Improving String Analysis in SPF

**Description:**
Symbolic PathFinder incorporates String constraint solvers (ABC,Z3) to enable analysis of programs that process Strings. The project will involve careful testing and improving the infrastructure for String solving. 

#### Method Summaries, extended

**Description:**
A thesis project implemented [Summaries of methods](https://github.com/lassebe/jpf-summary) when executing in JPF.
It includes a representation of the summaries that captures all side effects of the given procedure; and 
implements modifications of the program state. 

The actual summary of a method will be computed during its first execution, and then reused within traversal of other state paths. Another possible feature is the support for externally defined summaries that would be useful for library methods.

Experiments have shown that without summarizing the effects of constructors, most methods cannot be summarized. This is caused by the construction of new objects or throwing an exception (which also create a new object). Summarizing the effect of constructors would therefore be a huge enhancement to this technique. Other enhancements may also be possible.

**Difficulty:** Medium  
**Required skills:** Knowledge of Java bytecode  
**Preferred skills:** Handling of weak references, garbage collection

#### Model Checking Distributed Java Applications

**Description:**
[jpf-nas](https://github.com/javapathfinder/jpf-nas) is an extension of JPF that provides support for model checking distributed multithreaded Java applications. It relies on the multiprocess support included in the jpf-core which provides basic functionality to verify the bytecode of distributed applications. jpf-nas supports interprocess communication via TCP sockets by modeling the Java networking package java.net. This tool can handle simple multi-client server applications. Some examples can be found in the jpf-nas distribution (at jpf-nas/src/examples/). The goal of this project is to extend the functionality of jpf-nas in various ways, such as extending the communication model supported by the tool towards an existing open source Java library/framework, called [QuickServer](http://www.quickserver.org/), increasing the performance of the tool by improving the mechanism used to manage the state of communication objects, extending the tool with the cache-based approach used in [net-iocache](https://bitbucket.org/cyrille.artho/net-iocache), etc.

**Difficulty:** Medium  
**Required skills:** Knowledge of Java networking  
**Preferred skills:** Knowledge of jpf-nas or net-iocache

<!-- #### Verification of Multi Agent Systems

**Description:**
The goal of this project is to develop techniques that analyze key properties in multi-agent systems. The [jpf-mas](http://dl.acm.org/citation.cfm?id=2485058) extension will initially provide the ability to generate the reachable state space of Brahms models. The reachable state space can then be encoded into input for a variety of model checkers such as SPIN, NuSMV and PRISM, thereby enabling the verification of LTL, CTL and PCTL properties. The project will also need to investigate how to generate the set of reachable states for other kinds of models, such as Jason models, and how to compose reachable states of different modelling languages both at run-time and off-line. -->

<!--
#### Verification of unbounded heap-manipulating programs via learning
The goal of this project is to prove (or refute) a Hoare triple *{Pre}P{Post}*, where the program *P* contains unbound loops, and loop invariants are not available. We will use [Java StarFinder](https://github.com/star-finder/jpf-star) with the precondition *{Pre}* to generate test inputs for the program. We then execute the program with these inputs, and synthesize loop invariants from program executions using machine learning techniques. The resulted loop invariants will be validated by verification. If an invariant is invalid, we will obtain a counter-example, which is then used as a new test input, and the process repeats. Reference:

  - [Enhancing Symbolic Execution of Heap-based Programs with Separation Logic for Test Input Generation](https://arxiv.org/abs/1712.06025).
  - [Learning Shape Analysis](https://www.microsoft.com/en-us/research/wp-content/uploads/2017/06/veriml.pdf).
-->

#### Automatic program repair using annotations

**Description:**
Automated program repair  (APR) techniques often generate overfitting patches due to the reliance on test cases for patch generation and validation. In this project, we propose to overcome the overffiting issue in APR by leveraging developer-provided partial annotations to aid semantic reasoning. Developer annotations can come in different forms, e.g., JPF annotation. The advantage of developer annotations is two-fold. First, in addition to test cases, it helps augment the specifications of the program under analysis and thus provides more complete specifications. These annotations, despite being simple, can help significantly in semantic reasoning, e.g., null pointer analysis. Second, these annotations are not required to be complex so that to reduce the burden of manual effort by developers. For example, to reason about null pointer exception errors, developers are only required to add a few Nullable or Non-Nullable annotations to class fields or method parameters, etc.
We will use JPF and SPF for symbolically reasoning about the semantics of programs under analysis and generating repairs. We will also use JPF-Annotation as a way for developers to provide annotations.
  
<!-- #### Verification of Event-Driven Applications
The goal of this project is to evaluate and/or advance existing state-of-the-art tools for analysis of event-driven applications, for example jpf-awt, jpf-android. Some of the issues that need to be addressed include event sequence generation, search heuristics, and scalability. Other related problems and solutions are welcome.-->

<!-- #### Verification of epistemic properties of Java programs
Epistemic properties allow to reason about the "knowledge" of an agent in a system. Epistemic properties have been employed for several years in the specification and verification of a range of scenarios, from security to communication protocols to mental states of human agents such as pilots. There is a solid theory for epistemic properties based on the notion of observable states, and this project will build upon this. In particular, this project's aim is to extend JPF so that one can: 

* describe agents' observable state;
* write epistemic specifications, e.g., using annotations;
* algorithmically prove these assertions, or generate counter-examples.

This project may use components such as jpf-symbc, jpf-concolic and jpf-abstraction and external tools such as SMT solvers. -->

#### Refactoring SPF constraint library 

**Description:**
SPF constraints need to be refactored to allow different kinds of constraints to be combined during the construction of a path condition. An example of how it should be after the refactoring is the Abstract Syntax Tree constructed by [GREEN](http://dl.acm.org/citation.cfm?id=2393665).

**Difficulty:** Medium  
**Required skills:** Knowledge of object-oriented programming in Java, general knowledge of SMT-solvers  
**Preferred skills:** Priori software development experience using SMT-solvers' APIs  
**Expected outcomes:** More efficient and flexible constraint interface used to express a path constraints

#### Hash-consing for SPF

**Description:**
Hash-consing is a technique that reuses previously constructed expressions to avoid duplication during construction of larger expressions. It is a technique that has been extensively used for creating maximally-shared graphs (see Calysto by Babic et al.), reusing structurally equivalent expressions in KLEE (by Cadar et al.). Variants of this idea are also applied in other binary symbolic executors like FuzzBALL and built in to new binary analysis frameworks (see Jung et al). Symbolic PathFinder currently does not support hash-consing or sharing of subexpressions causing memory usage to blow-up in pathological examples. This project would be about implementing hash-consing or a variant of this idea in SPF. This would have a clear benefit in reducing SPF's memory usage.

**Difficulty:** Medium  
**Required skills:** Knowledge of symbolic execution and what it is useful for  
**Preferred skills:** Prior experience of developing or maintaining a symbolic execution tool  
**Expected outcomes:** Clear reduction in memory usage of SPF when we run benchmarks. The idea of hash-consing is being adapted from the veritesting paper by [Avgerinos et al.](https://dl.acm.org/doi/abs/10.1145/2568225.2568293?casa_token=WgY5X3ESR5AAAAAA:50nH1DsJ_YRg1Pyv4zNgz8a4RLfyj49eETxFH7OhuQmwr_4vjjS9u5h5_aSsJt54fvNIuBEc0mYdu2M)

#### Visualizing ChoiceGenerator tree for SPF

**Description:**
A symbolic executor explores feasible choices through a program. It can often be difficult to understand how a symbolic executor got to a particular program location in a given symbolic state. These difficulties arise from not being able to easily see all the previous choices the symbolic executor made to get to a certain point. To address this limitation, a symbolic executor can be asked to visually report its tree of exploration choices. An example of this is FuzzBALL's "-decision-tree-use-file" that reports FuzzBALL's tree of explored choices into a file that can be visualized. This project would be add a similar feature in SPF. Given a point of symbolic exploration, it would allow SPF to report its tree of explored ChoiceGenerator objects into a file that can be observed visually. 

**Difficulty:** Easy  
**Required skills:** Java programming  
**Preferred skills:** Some knowledge of symbolic execution  
**Expected outcomes:** Visualization of the choice generator tree when exploring a few benchmarks; implementation of a few ideas to improve the usability of such trees when generated from a symbolic execution run with complex software

#### Combinatorial testing of configuration options for SPF

**Description:**
SPF has a large number of diverse configuration options. Enabling some features require combinations of options (such as incremental solving) whereas other options are backed by a broken implementation. For example, during the recently concluded SV-COMP 2020 competition, the Java Ranger authors (which also includes me) turned off symbolic string support, while the SPF team chose to leave this option on. For this project, we would examine all of SPF's options and construct test cases to combinatorially cover all of them. The outcome of this project would be a regression test suite that combinatorially covers all of SPF's options and provides clear documentation on which options don't work. If there is still time available during the summer, we would also attempt to fix SPF's broken symbolic string solving. There have been many recent advances in string solving and it would be valuable to have support for powerful string solvers such as z3str3. This project can also be combined with the [improving string analysis](#improving-string-analysis-in-spf) project above. 

**Difficulty:** Medium  
**Required skills:** Java programming  
**Preferred skills:** Some knowledge of SPF's configuration options  
**Expected outcomes:** A clear list of SPF options that dont play well with each other; a list of test cases that tested the correct interactions between SPF options

#### Beneficial path-merging for SPF

**Description:**
Path-merging has recently been implemented as an extension to the Symbolic PathFinder tool. However, path-merging is not always beneficial because it can contribute to making the contents of the stack and/or the heap symbolic. Later branching on these symbolic contents can cause further branching. This project is about developing a heuristic similar to the one proposed by [Kuznetsov et al.](https://dslab.epfl.ch/pubs/stateMerging.pdf) for symbolic execution of Java bytecode.

**Difficulty:** Hard  
**Required skills:** Knowledge of path-merging in symbolic execution; examples include [dynamic state merging](https://dslab.epfl.ch/pubs/stateMerging.pdf) and/or [veritesting](https://dl.acm.org/doi/abs/10.1145/2568225.2568293?casa_token=WgY5X3ESR5AAAAAA:50nH1DsJ_YRg1Pyv4zNgz8a4RLfyj49eETxFH7OhuQmwr_4vjjS9u5h5_aSsJt54fvNIuBEc0mYdu2M)  
**Preferred skills:** Knowledge of SPF's internals when it comes to its execution of conditional branches; knowledge of [Java Ranger](https://github.com/vaibhavbsharma/java-ranger) would be further beneficial  
**Expected outcomes:** a heuristic that improves SPF's performance when it uses path-merging but avoids path-merging when it doesn't seem beneficial; the targeted heuristic is of the kind proposed in the state merging paper above by Kuznetsov et al.

#### Test generation with path-merging

**Description:**
Path-merging has recently been implemented as an extension to the Symbolic PathFinder tool. However, test generation with a path-merging symbolic executor is different because we would not want test generation to undo all the benefits of path-merging. Test generation should instead be targeted towards achieving coverage of some coverage criterion specified by the user. In this project, we will attempt to do test generation with a path-merging symbolic executor which will be [Java Ranger](https://github.com/vaibhavbsharma/java-ranger). One example of test generation can be found in the test generation performed with [veritesting](https://doi.org/10.1145/2568225.2568293) by Avgerinos et al.

**Difficulty:** Hard  
**Required skills:** Knowledge of path-merging in symbolic execution; examples include [dynamic state merging](https://dslab.epfl.ch/pubs/stateMerging.pdf) and/or [veritesting](https://doi.org/10.1145/2568225.2568293). This project would also involve knowledge of test criteria like MC/DC.
**Preferred skills:** Knowledge of SPF's internals when it comes to its execution of conditional branches; knowledge of [Java Ranger](https://github.com/vaibhavbsharma/java-ranger) would be further beneficial  
**Expected outcomes:** a implementation that generates tests with a path-merging symbolic executor like Java Ranger

<!-- #### Handling Native Calls in the Context of Symbolic Execution
The goal of this project is to handle native calls in the context of symbolic execution by generating native peers and associating them with native methods on-the-fly. For the native peers we need concrete values to be used as input parameters for automatically generated native peers methods. The idea is to first solve the constraints obtained with symbolic execution and use those solutions as input parameters. This can be accomplished by enhancing [jpf-symbc](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-symbc) to use the [jpf-nhandler](https://bitbucket.org/nastaran/jpf-nhandler) extension of JPF. -->

<!--
#### Whitebox Fuzzer and Grammar Learner

**Description:**
Build a whitebox fuzzing tool on top of Symbolic PathFinder, that can learn the input grammar of a piece of code in an iterative fashion. The idea would be to first run the code on symbolic input of a fixed length and then learn a possible grammar for this length, at that point extend the length and generalise the grammar. The main research goal behind this project is to see if one can do whitebox fuzzing without a pre-determined seed file (which is the way most whitebox fuzzers work at the moment).  -->

#### Fuzzing and Symbolic Execution

**Description:**
Develop a fuzzer for Java that can be integrated with SPF (or another Java based symbolic execution engine). The idea would be that when fuzzing gets stuck and makes no progress that the symbolic analysis can create a new seed file to allow analysis to progress.

<!-- 
#### Comparison between concolic and classical symbolic execution

**Description:**
Comparison between concolic execution, e.g. DEEPSEA and JDart, and classical symbolic execution, e.g. SPF. -->

<!-- #### Generic GREEN
[GREEN](http://dl.acm.org/citation.cfm?id=2393665) is a framework being used to cache constraints with satisfiability and model counting results during analysis. It is used in several jpf-symbc extensions for probabilistic software analysis. Recently there has been a number of suggested improvements to optimise such caching. This project will focus on two ideas: one reusing previous solutions for SAT checking and secondly to store the characterising function for model counting produced by a tool like Barvinok to allow one to reuse model counting solutions even when bounds on variables change. -->

#### Symbolic PathFinder for Neural Network Analysis

**Description:**
This project explores the application of symbolic execution and related methods to the domain of neural networks.  The goal of the project is to design and implement a family of integrated analyses that allow testing and debugging of neural networks.  The project will build on recent advances in symbolic analysis of neural networks and utilize the SPF/JPF toolset.

<!-- #### Improving Symbolic PathFinder
This project idea seeks to improve Symbolic PathFinder by adding unit tests and improving the quality of the code base.

#### Improving Sampling of Symbolic Paths

**Description:**
We are working on a number of tools that use jpf-symbc for sampling symbolic paths in order to maximize some reward function. For example, [jpf-reliability](http://dl.acm.org/citation.cfm?id=2643011) is a probabilistic software analysis tool for programs with probabilistic and nondeterministic behavior. It synthesizes schedulers, i.e. resolutions of nondeterminism, that maximizes (or conversely minimizes) the probability of a property being satisfied.

Common to these tools is that sampling is highly parallelizable and a possible direction of this project could be to design and implement a distributed infrastructure for these tools. Other improvements can also be experimented with, e.g., state pruning algorithms, constraints caching, and other algorithms for sampling paths.

#### Verification and Testing Heap-based Programs with Symbolic PathFinder

**Description:**
The goal of this project is to extend Symbolic PathFinder to verify Separation Logic assertions, and to improve test-case generation for heap-based programs. Currently, SPF uses "lazy initialization", which is a brute-force enumeration of all heap objects that can bind to the structured inputs accessed by the program. This explicit enumeration may identify many invalid heap configurations that violate properties of the data structures in the heap, which leads to a huge amount of false alarms. -->

<!-- #### Analysis of Android Applications
Various ideas are welcome here. Here are a couple of possible subprojects:

1. The goal of this project is collecting interesting Android applications, and evaluating and applying [JPF-Android](https://heila.bitbucket.io/jpf-android/) to analyze them. JPF-Android is an extension to JPF used to model check Android Applications. Android applications have many dependencies which make them hard to test and verify. They also require events to drive the execution of the applications.  The goal of this project is to identify interesting Android applications to run on ]JPF-Android and then  evaluate the efficiency and effectiveness of the tool  on these apps. You will be using/improving existing approaches to generate stubs and models for applications and then compare the coverage and runtime on JPF-Android to other dynamic analysis tools.


2. This project includes using [JPF-Android](https://heila.bitbucket.io/jpf-android/) to generated test sequences for android applications, and implementing a tool to convert these sequences into tests that can be run on the emulator. JPF-Android verifies Android applications outside of the Android software stack on JPF using a model environment to improve coverage and efficiency. It generates event sequences to drive the execution of the application during exploration. Each sequence also includes the configuration of the environment (device) for which the sequence was executed. This project uses the AndroidViewClient API in Python to run the set of event sequences as detected by JPF-Android on  an emulator to find the number of valid sequences and the code coverage they obtain compared to JPF-Android.

3. An extension of [jpf-mobile-devices](https://bitbucket.org/matsurago/jpf-mobile-devices) to generate the right initialization sequence for applications running inside JPF on Android. This project is different from the ones above in the sense that JPF is run as an Android application that can use the underlying Android environment, not as a normal application that models the Android environment. Also see the [paper on jpf-mobile-devices](https://people.kth.se/~artho/papers/jpf-mobile.pdf). -->

#### Smart Contract Analysis

**Description:**
Develop a mechanism to allow the analysis of Ethereum Virtual Machine (EVM) bytecode by replacing the JVM bytecodes with EVM bytecodes within JPF. The second part of the project would be to extend the bytecodes further to allow symbolic execution as well. 

#### JDart maintenance and scalability

**Description:**
JDart is a dynamic symbolic execution for Java programs and is based on Java PathFinder (JPF). The tool executes Java programs with concrete and symbolic values at the same time and records symbolic constraints describing all the decisions along a particular path of the execution. These path constraints are then used to find new paths in the program. Concrete data values for exercising these paths are generated using a constraint solver. Recent development on JDart has focused on supporting more language features of Java (e.g., symbolic analysis of String variables) and on implementing taint analysis on top of concolic execution. As a consequence, JDart scored the third place in the Java track of SV-Comp 2020 and was able to beat the OWASP security benchmark. Source code of recent development can be found at [https://github.com/tudo-aqua/jdart](https://github.com/tudo-aqua/jdart).

To further robustness of JDart, the tools needs a small overhaul of its architecture: the build system has to be updated to maven or gradle, dependencies should be handled by the build system, and proper unit tests should be executed by the build system. (A subset of) SV-Comp and OWASP benchmarks should become regression tests. To increase scalability, JDart should be make use of parallelization. Concolic execution lends itself to parallelization as individual executions are completely independent of one another (cf. works of white-box fuzzing). The architecture of JDart should be modularized with clear APIs between components as a basis for making JDart parallel. Google summer of code project could focus on a subset on these goals depending on skill set and interests of students.

**Difficulty:** Medium  
**Required skills:** Java, maven/gradle, unit testing  
**Preferred skills:** APIs, docker, multi-threading, distributed applications  
**Expected outcomes:** An overhauled version of JDart should be made available under the Java PathFinder organization on github as one result of the Google summer of code project. When tackling scalability, results could be submitted to the JPF workshop.  

<!--
#### New Features for JDart
[JDart](https://github.com/psycopaths/jdart) is an open-source, dynamic symbolic analysis framework built on Java PathFinder. It has been applied to industrial scale software, including complex NASA systems. 

We have many ideas for improving JDart and welcome additional ideas too:

1. JDart supports various (fixed-size) symbolic data structures, e.g., arrays and HashMap. It would be interesting to improve this, in particular with symbolic array indexes and support for unbounded data structures possibly with Lazy Initialization.
2. Add more exploration strategies to JDart. In particular, new exploration heuristics could be interesting, e.g., for targeted concolic execution. It could also combine other static analyses, such as, program slicing, where the computed slice can be used to constrain the exploration.
3. [JConstraints](https://github.com/psycopaths/jconstraints) is a solver abstraction layer used by JDart to interact transparently with SMT solvers. JConstraints has support for some solvers, e.g., Z3 and SMTinterpol. Often, however, one needs to select a solver that is best suited for the constraints generated (e.g., linear, non-linear). It could be very useful adding additional solvers to JDart based on JConstraints and evaluate them to understand better their applicability. This could also comprise adding a general interface to solvers that support the SMTLib format.

#### Concolic Execution for Android Apps

**Description:**
[JDart](https://github.com/psycopaths/jdart) is an open-source, dynamic symbolic analysis framework built on Java PathFinder. It has been applied to industrial scale software, including complex NASA systems. This project seeks to extend this capability to Android applications by supporting the Dalvik instruction set, e.g., by using [jpf-pathdroid](http://babelfish.arc.nasa.gov/hg/jpf/jpf-pathdroid). This would enable analyses build on JDart to also work for Android, e.g., automated test case generation, finding bugs, and program understanding. 

#### Support for parallel or distributed exploration in JDart and Regression tests for JDart

**Description:**
Here is an incomplete list of ideas and projects for extending and improving JDart. If you would like to work on any of the projects or have your own ideas (or just want to contribute to JDart), let us know and we can talk more.

* Add regression test suite
* Add visualization capabilities to JDart. This could be really useful - especially for program understanding and debugging. It could be as simple as translating the constraints tree to DOT, but it would be a great feature to have more powerful, interactive visualizations, e.g., browser-based with d3, or using Gephi, yEd, jung, prefuse, or jgraph.
* Finish (or re-implement) [JConstraints](https://github.com/psycopaths/jconstraints) SMTLib interface and experiment with other solvers. dReal is (partly) integrated using this, but it would be interesting to experiment more
* Add more search heuristics and evaluate
* Make support for [PathDroid](http://ti.arc.nasa.gov/opensource/projects/pathdroid/). This would allow JDart to analyze Dalvik bytecode programs
* Work on constraints caching in a similar fashion as [Green](http://www.cs.sun.ac.za/~jaco/PUBS/vgd12.pdf) (or make support for Green)
* Add support for parallel/distributed exploration
* Add support for symbolic data structures. Maybe ala [Lazy Initialization](http://users.ece.utexas.edu/~khurshid/testera/GSE.pdf)
* Improve test suite generation from symbolic analysis. Currently, static target methods are fully supported while instance methods are only partly supported
* General refactoring and code improvement-->


<!-- #### Environment and Test Case Generation for Specific Domains
When model checking applications belonging to specific domains (e.g., Swing, Android), 
JPF users have to provide application environment, consisting of test drivers and models 
for libraries that are too complex for JPF to run. The goal of this project is to evaluate the
existing (or provide new) semi-automated support for generation of test drivers and library 
models/stubs based on the results of domain-specific static analysis, specifications, run-time 
information, or other suitable techniques. Once generated, such drivers and stubs can 
be used to verify applications belonging to specific domains using appropriate jpf 
extensions (e.g., [jpf-awt](http://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-awt), [jpf-android](https://bitbucket.org/heila/jpf-android)). The project can be implemented on top of 
[OCSEGen](http://ti.arc.nasa.gov/publications/8752/download) or another suitable tool.-->

#### Test Case Generation/Model-based Testing with Modbat for JPF

**Description:**
JPF requires test cases as a starting point to explore a system. It is therefore suitable to use
test case generation to create test cases automatically. [Modbat](https://github.com/cyrille-artho/modbat/) is an open-source tool for test case generation. For testing concurrent software,
an obvious choice would be to combine Modbat (to generate tests) with JPF (to execute tests and
find concurrency problems). This has been done once as a [proof of concept](https://people.kth.se/~artho/papers/ase-2013-preprint.pdf) but is not supported in the current version of Modbat.
The main reason for this is that Modbat's reporting has to read and parse bytecode, which requires
access to some native code that JPF does not support.
The goal is to find all problems where Modbat requires native access, and to use jpf-nhandler
to resolve as many of these cases as possible. Remaining cases can be handled with custom model/peer classes.

**Difficulty:** Easy  
**Required skills:** Knowledge of Java Pathfinder  
**Preferred skills:** Knowledge of test generation

#### Symbolic data-race detection for Habanero Java

**Description:**
[Habanero Java](http://faculty.knox.edu/dbunde/teaching/hj/) is a Java implementation of the [Habanero Extreme-scale](http://vsarkar.rice.edu/research/publications/publi-habanero/) programming model for multithreaded applications. The model is based on X10 and supports fork/join semantics as well as futures, isolation, and phasers. The advantage of structured parallelism such as Habanero is that the language itself provides concurrency guarantees such as deadlock freedom and determinacy if and only the program is free of data-race. A data-race occurs when two or more threads of execution access the same memory location and at least one of those accesses is a write. An additional advantage of structured parallelism is that run-times and analysis can be optimized based on the language structure itself. Recent work adds to JPF the ability to model check Habanero Java programs using a verification specific runtime and an algorithm that constructs and analyzes a computation graph representing the happens-before relation of the program execution ([1](https://dl.acm.org/citation.cfm?doid=2693208.2693245), [2](https://link.springer.com/chapter/10.1007%2F978-3-319-77935-5_25). The analysis is predictive because it infers from the single observed schedule the presence or absence of data-race in other non-observed schedules and only needs to enumerate schedules around isolation. Enumeration schedules around isolation though is still expensive and leads to state explosion in JPF. The work in this project is to mitigate this state explosion in enumerating schedules around isolation by building a symbolic computation graph from the program execution that adds constraints on the graph edges indicating under what condition the edge is active, and then using an SMT solver to find a set of edges on which a data-race exists. A first step in the project is to add a dynamic partial order reduction to JPF that is able to inform the symbolic computation graph about dependencies. 

**Difficulty:** Medium  
**Required skills:** Java programming and knowledge of model checking  
**Expected outcomes:** SMT solution to data-race that avoids state exploration

<!-- #### Environment and Test Case Generation for Symbolic Execution
When using Symbolic PathFinder (SPF), one needs to supply application environment, consisting of test drivers and models/stubs for libraries that are too complex for SPF to handle. The goal of this project is to evaluate the existing (or provide new) semi-automated support for generation of test drivers and library models/stubs, containing symbolic values, based on the results of domain-specific static analysis, specifications, run-time information, or other suitable techniques. Once generated, such drivers and stubs can be used to verify applications using SPF. The project can be implemented on top of [OCSEGen](http://ti.arc.nasa.gov/publications/8752/download) or another suitable tool.

#### Test Case Generation for Evolving Applications
The goal of this project is to evaluate and/or advance existing state-of-the-art tools for test suite generation and augmentation based on software changes between versions. Tools like jpf-symbc, Randoop, Evosuite can be used to generate unit tests for an application under test. Other related tools and ideas are welcome.

#### Evaluating jpf-psyco
[jpf-psyco](https://github.com/psycopaths/psyco) is an open-source tool built on JPF for generating temporal component interfaces. A temporal interface is expressed as a finite-state automaton over the public methods of the component and captures safe ordering relationships of method invocations. jpf-psyco relies on a combination of symbolic execution and automata learning for generating interfaces. This project seeks to evaluate jpf-psyco with new examples (e.g. reactive systems) and experimenting with other learning algorithms.
-->
