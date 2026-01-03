# Project Ideas

Please note that this list is not exclusive. If you have other ideas and topics related to JPF, please let us know on our [Discord server](https://discord.gg/sX4YZUVHK7).
A possible proposal template can be found at the bottom of our GSoC page: [[JPF Google Summer of Code 2025]].

### JPF Infrastructure

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) [Support Java 11/17 for JPF extensions](#support-java-11) <Cyrille>

*  ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) [Support Java 17 for jpf-core](#support-java-17) <Cyrille> 

<!-- ### JPF Application Domains -->

<!-- * [Model Checking Distributed Java Applications](#model-checking-distributed-java-applications) <Cyrille> -->

<!-- * [Verification of Multi Agent Systems](#verification-of-multi-agent-systems) <Franco><Eric><CheckWithNeha> -->

<!--* [Verification of Actor-based Systems](#verification-of-actor-based-systems) <Nastaran> -->

<!--* [Verification of Event-Driven Applications](#verification-of-event-driven-applications) <Oksana>-->

<!-- * [Verification of epistemic properties of Java programs](#verification-of-epistemic-properties-of-java-programs) <Franco><Nikos> -->

<!-- ### Separation Logic

* [Verification of unbounded heap-manipulating programs via learning](#verification-of-unbounded-heap-manipulating-programs-via-learning) <Loc><Sang> -->

<!-- ### Automatic Program Repair -->

<!-- * [Automatic program repair using annotations](#automatic-program-repair-using-annotations) <Bach><Vaibhav><Eric><Corina> -->

### Symbolic Pathfinder (SPF)

<!-- * ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) [Support Java 11+ for SPF](#support-java-11-for-spf) <Yannic><Corina> -->

<!-- * [Support gradle for SPF](#support-gradle-for-spf) <Yannic><Corina> -->

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) [String Constraint Solver Integration in SPF](#improving-string-analysis-in-spf) <Yannic><Corina><Elena><Soha>

* ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) [Support runtime exceptions in SPF](#runtime-exception-in-spf) <Soha>

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) [Support a portfolio of solvers in SPF](#solvers-portfolio-in-spf) <Soha>

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) [Use LLM to generate sound reduction rules in SPF](#llm-reduction-rules-in-spf) <Soha>

<!-- * [Support Bit-Vector Floating Point in SPF](#bvfloating-point-in-spf) <Soha> -->

<!-- * [Refactoring SPF constraint library](#refactoring-spf-constraint-library) <Elena> -->

<!-- * [Handling Native Calls in the Context of Symbolic Execution](#handling-native-calls-in-the-context-of-symbolic-execution) <Corina><Nastaran> -->

<!-- * [Comparison between concolic execution and classical symbolic execution](#comparison-between-concolic-and-classical-symbolic-execution) -->

<!-- * [Generic GREEN](#generic-green) <Willem> -->

<!-- * [Improving Symbolic PathFinder](#improving-symbolic-pathfinder) <Kasper><Corina> -->

<!-- * [Improving Sampling of Symbolic Paths](#improving-sampling-of-symbolic-paths) <Kasper> -->

<!-- * [Hash-consing for SPF](#hash-consing-for-spf) <Vaibhav> -->

<!-- * [Visualizing ChoiceGenerator tree for SPF](#visualizing-choicegenerator-tree-for-spf) <Vaibhav> -->

<!-- * [Combinatorial testing of configuration options for SPF](#combinatorial-testing-of-configuration-options-for-SPF) <Vaibhav> -->

<!-- * [Beneficial path-merging for SPF](#beneficial-path-merging-for-SPF) <Vaibhav> -->

<!--### Hybrid Fuzzing-->

<!-- * [Whitebox Fuzzer and Grammar Learner](#whitebox-fuzzer-and-grammar-learner)  -->

<!-- * [Fuzzing and Symbolic Execution](#fuzzing-and-symbolic-execution) <Corina><Yannic> -->


<!-- ### Smart Contract -->

<!-- * [Smart Contract Analysis](#smart-contract-analysis) <Cyrille> -->



<!-- ### Android -->

<!-- * [Analysis of Android Applications](#analysis-of-android-applications) -->



<!-- ### Concolic Execution -->

<!-- * [JDart maintenance and scalability](#jdart-maintenance-and-scalability) <Falk> -->

<!--
* [New Features for JDart](#new-features-for-jdart) <Kasper>

* [Concolic Execution for Android Apps](#concolic-execution-for-android-apps) <Kasper>

* [Support for parallel or distributed exploration in JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)

* [Regression tests for JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart)-->



### Environment and Test Case Generation

<!-- * [Environment and Test Case Generation for Specific Domains](#environment-and-test-case-generation-for-specific-domains) <Oksana> -->

<!-- * ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) [Model-based Testing with Modbat for JPF](#mbt-modbat) <Cyrille> -->

<!-- * [Minimizing test-cases for branch coverage of Path-Merged Regions](#minimize-testcases-path-merging) <Soha> -->

<!-- * [Method summaries, extended](#method-summaries)<Cyrille><Pavel> -->

<!-- * [Environment and Test Case Generation for Symbolic Execution](#environment-and-test-case-generation-for-symbolic-execution) <Oksana>

<!-- * [Test Case Generation for Evolving Applications](#test-case-generation-for-evolving-applications) <Oksana> -->



<!-- ### JPF Extensions and External Systems Interfacing -->

<!-- * [Evaluating jpf-psyco](#evaluating-jpf-psyco) <Kasper><CheckWithFalk> -->



<!-- ### Symbolic Data-race Detection -->

<!-- * [Symbolic data-race detection for Habanero Java](#symbolic-data-race-detection-for-habanero-java) <Eric> -->



### Project Description

<a name="support-java-11"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Support Java 11/17 for JPF extensions

**Description:**
jpf-core is essentially a JVM that currently fully supports only Java 8 and Java 11 (with limitations on bootstrap methods). Bootstrap methods are currently interpreted, which works for common usage but may not work for advanced cases. The goal of this project is to generate the call site code on the fly so bootstrap methods work as on the host JVM. 

**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Java bytecode  
**Preferred skills:** Knowledge of private Java APIs 
**Possible Mentors:** Cyrille

<a name="support-java-17"></a>
#### ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) Support for Java 17 for jpf-core

Related to the project above, there are also some new features in Java 17 that are not yet supported by JPF. We have work in progress on branch `java-17`. Currently unsupported Java features include language features that are not supported at run time (e.g., records) and Java language features that are not fully analyzed (e.g., sealed classes). In this project, you would identify such unsupported features and extend JPF (jpf-core) to support them.

**Difficulty:** Medium  
**Scope:** 175 hours  
**Required skills:** Knowledge of Java internals  
**Possible Mentors:** Cyrille

<!--
<a name="mbt-modbat"></a>
#### ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) Test Case Generation/Model-based Testing with Modbat for JPF

**Description:**
JPF requires test cases as a starting point to explore a system. It is therefore suitable to use
test case generation to create test cases automatically. [Modbat](https://github.com/cyrille-artho/modbat/) is an open-source tool for test case generation. For testing concurrent software,
an obvious choice would be to combine Modbat (to generate tests) with JPF (to execute tests and
find concurrency problems). This has been done once as a [proof of concept](https://people.kth.se/~artho/papers/ase-2013-preprint.pdf) but is not supported in the current version of Modbat.
The main reason for this is that Modbat's reporting has to read and parse bytecode, which requires
access to some native code that JPF does not support.
The goal is to find all problems where Modbat requires native access, and to use jpf-nhandler
to resolve as many of these cases as possible. Remaining cases can be handled with custom model/peer classes, perhaps not with the full feature set, but at least to avoid JPF aborting due to an unsupported feature.

**Difficulty:** Medium  
**Scope:** 350 hours  
**Required skills:** Knowledge of Java Pathfinder  
**Preferred skills:** Knowledge of test generation  
**Possible Mentors:** Cyrille
-->

<!--
<a name="support-gradle-for-spf"></a>
#### Support for gradle for SPF

**Description:**
The goal of this project is to (1) implement gradle support for Symbolic Pathfinder, (2) to update the extension template, including gradle support and updated documentation.

**Difficulty:** Easy  
**Scope:** 175 hours  
**Required skills:** Knowledge of Java Pathfinder and Gradle build automation  
**Preferred skills:** Knowledge of Symbolic Pathfinder  
**Possible Mentors:** Yannic, Corina
-->

<!-- 
<a name="support-java-11-for-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Support for Java v11 for SPF

**Description:**
The goal of this project is to upgrade SPF to work with Java 11.

**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** Knowledge of Java v11  
**Possible Mentors:** Yannic, Corina

-->

<a name="improving-string-analysis-in-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Robustify String solving for SPF

**Description:**
The goal of this project is to test SPF integration with Z3 string constraint solving; adding support cvc5 is a plus. This project will extend SPF branch `sv-comp`.


**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** Knowledge of String constraint solving.  
**Possible Mentors:** Corina, Elena, Soha


<a name="runtime-exception-in-spf"></a>
#### ![#FFD700](https://placehold.co/15x15/FFD700/FFD700.png) Support runtime exception in SPF

**Description:**
The main goal of this project is to support throwing a runtime exception for some of the summarized functions such as `String.substring`. Also, this project should build on [SPF](https://github.com/SymbolicPathFinder/jpf-symbc) Java 11 Gradle support, which implies fixing existing issues. This project will extend SPF branch `sv-comp`.


**Difficulty:** Medium
**Scope:** 350 hours    
**Required skills:** Knowledge of Symbolic Pathfinder   
**Possible Mentors:** Soha


<a name="solvers-portfolio-in-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Support portfolio of solvers in SPF

**Description:**
The main goal of this project is to enable the simultaneous invocation of multiple solvers, terminating the wait as soon as any solver returns a satisfiable result. This approach is expected to enhance [SPF's](https://github.com/SymbolicPathFinder/jpf-symbc) ability to handle a broader range of constraints. This project will extend SPF branch `sv-comp`. 


**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** Expeirence with various solvers   
**Possible Mentors:** Soha


<a name="llm-reduction-rules-in-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png) Use LLM to generate sound reduction rules in SPF

**Description:**
Solver constraints can become very complex, and very large. In this project, we will use LLM in [SPF](https://github.com/SymbolicPathFinder/jpf-symbc) to identify sound reduction rules that can be applied to the constraints before sending them to the solver, ideally improving its performance. See [this paper](https://link.springer.com/chapter/10.1007/978-3-642-39176-7_19) for reference. This project will extend SPF branch `sv-comp`.

**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder     
**Preferred skills:** LLM  
**Possible Mentors:** Soha



<!--
 
<a name="minimize-testcases-path-merging"></a>
#### Minimizing Test-Cases for Branch Coverage of Path-Merged Regions

**Description:**
When generating test cases for path-merging more branch obligations could be covered with a single test case. In this project, we will focus on ways to minimize the number of test cases generated from path-merging for branch coverage criteria. 

**Difficulty:** Medium  
**Required skills:** Knowledge of Symbolic PathFinder.  
**Preferred skills:** Knowledge of Java Ranger.  

-->
