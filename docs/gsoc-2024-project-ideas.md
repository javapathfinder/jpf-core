# Project Ideas

Please note that this list is not exclusive. If you have other ideas and topics related to JPF, please let us know on the [JPF Google group](https://groups.google.com/forum/#!forum/java-pathfinder.md).
A possible proposal template can be found at the bottom of our GSoC page: [[JPF Google Summer of Code 2024]].

### JPF Infrastructure

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) [Support Java 11 (bootstrap methods and other issues) for jpf-core](#support-java-11.md) <Cyrille>

*  ![#FFD700](https://placehold.co/15x15/ffd700/ffd700.png.md) [Support Java 17 (private API dependencies) for jpf-core](#support-java-17.md) <Cyrille> 

<!-- ### JPF Application Domains -->

<!-- * [Model Checking Distributed Java Applications](#model-checking-distributed-java-applications.md) <Cyrille> -->

<!-- * [Verification of Multi Agent Systems](#verification-of-multi-agent-systems.md) <Franco><Eric><CheckWithNeha> -->

<!--* [Verification of Actor-based Systems](#verification-of-actor-based-systems.md) <Nastaran> -->

<!--* [Verification of Event-Driven Applications](#verification-of-event-driven-applications.md) <Oksana>-->

<!-- * [Verification of epistemic properties of Java programs](#verification-of-epistemic-properties-of-java-programs.md) <Franco><Nikos> -->

<!-- ### Separation Logic

* [Verification of unbounded heap-manipulating programs via learning](#verification-of-unbounded-heap-manipulating-programs-via-learning.md) <Loc><Sang> -->

<!-- ### Automatic Program Repair -->

<!-- * [Automatic program repair using annotations](#automatic-program-repair-using-annotations.md) <Bach><Vaibhav><Eric><Corina> -->

### Symbolic Pathfinder (SPF)

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) [Support Java 11+ for SPF](#support-java-11-for-spf.md) <Yannic><Corina>

<!-- * [Support gradle for SPF](#support-gradle-for-spf.md) <Yannic><Corina> -->

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) [String Constraint Solver Integration in SPF](#improving-string-analysis-in-spf.md) <Yannic><Corina><Elena><Soha>

* ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) [Support the generation of violation witness in GraphML format in SPF](#witness-generation-in-spf.md) <Soha>

<!-- * [Support Bit-Vector Floating Point in SPF](#bvfloating-point-in-spf.md) <Soha> -->

<!-- * [Refactoring SPF constraint library](#refactoring-spf-constraint-library.md) <Elena> -->

<!-- * [Handling Native Calls in the Context of Symbolic Execution](#handling-native-calls-in-the-context-of-symbolic-execution.md) <Corina><Nastaran> -->

<!-- * [Comparison between concolic execution and classical symbolic execution](#comparison-between-concolic-and-classical-symbolic-execution.md) -->

<!-- * [Generic GREEN](#generic-green.md) <Willem> -->

<!-- * [Improving Symbolic PathFinder](#improving-symbolic-pathfinder.md) <Kasper><Corina> -->

<!-- * [Improving Sampling of Symbolic Paths](#improving-sampling-of-symbolic-paths.md) <Kasper> -->

<!-- * [Hash-consing for SPF](#hash-consing-for-spf.md) <Vaibhav> -->

<!-- * [Visualizing ChoiceGenerator tree for SPF](#visualizing-choicegenerator-tree-for-spf.md) <Vaibhav> -->

<!-- * [Combinatorial testing of configuration options for SPF](#combinatorial-testing-of-configuration-options-for-spf.md) <Vaibhav> -->

<!-- * [Beneficial path-merging for SPF](#beneficial-path-merging-for-spf.md) <Vaibhav> -->

<!--### Hybrid Fuzzing-->

<!-- * [Whitebox Fuzzer and Grammar Learner](#whitebox-fuzzer-and-grammar-learner.md)  -->

<!-- * [Fuzzing and Symbolic Execution](#fuzzing-and-symbolic-execution.md) <Corina><Yannic> -->


<!-- ### Smart Contract -->

<!-- * [Smart Contract Analysis](#smart-contract-analysis.md) <Cyrille> -->



<!-- ### Android -->

<!-- * [Analysis of Android Applications](#analysis-of-android-applications.md) -->



<!-- ### Concolic Execution -->

<!-- * [JDart maintenance and scalability](#jdart-maintenance-and-scalability.md) <Falk> -->

<!--
* [New Features for JDart](#new-features-for-jdart.md) <Kasper>

* [Concolic Execution for Android Apps](#concolic-execution-for-android-apps.md) <Kasper>

* [Support for parallel or distributed exploration in JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart.md)

* [Regression tests for JDart](#support-for-parallel-or-distributed-exploration-in-jdart-and-regression-tests-for-jdart.md)-->



### Environment and Test Case Generation

<!-- * [Environment and Test Case Generation for Specific Domains](#environment-and-test-case-generation-for-specific-domains.md) <Oksana> -->

* ![#4CAF50](https://placehold.co/15x15/4caf50/4caf50.png.md) [Model-based Testing with Modbat for JPF](#mbt-modbat.md) <Cyrille>

<!-- * [Minimizing test-cases for branch coverage of Path-Merged Regions](#minimize-testcases-path-merging.md) <Soha> -->

<!-- * [Method summaries, extended](#method-summaries.md)<Cyrille><Pavel> -->

<!-- * [Environment and Test Case Generation for Symbolic Execution](#environment-and-test-case-generation-for-symbolic-execution.md) <Oksana>

<!-- * [Test Case Generation for Evolving Applications](#test-case-generation-for-evolving-applications.md) <Oksana> -->



<!-- ### JPF Extensions and External Systems Interfacing -->

<!-- * [Evaluating jpf-psyco](#evaluating-jpf-psyco.md) <Kasper><CheckWithFalk> -->



<!-- ### Symbolic Data-race Detection -->

<!-- * [Symbolic data-race detection for Habanero Java](#symbolic-data-race-detection-for-habanero-java.md) <Eric> -->



### Project Description

<a name="support-java-11"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) Support Java 11 (bootstrap methods and other issues) for jpf-core

**Description:**
jpf-core is essentially a JVM that currently fully supports only Java 8 and Java 11 (with limitations on bootstrap methods). Bootstrap methods are currently interpreted, which works for common usage but may not work for advanced cases. The goal of this project is to generate the call site code on the fly so bootstrap methods work as on the host JVM. 

*Note:* You can apply to both projects (Java 11 or 17 support); in that case, please indicate that you would like to work on either one, and what your preference would be.

**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Java bytecode  
**Preferred skills:** Knowledge of bootstrap methods in Java bytecode  
**Possible Mentors:** Cyrille

<a name="support-java-17"></a>
#### ![#FFD700](https://placehold.co/15x15/ffd700/ffd700.png.md) Support for Java 17 (private API dependencies) for jpf-core

Related to the project above, there are also some internal APIs from Java 11 that no longer exist in Java 17.
This requires redesigning and reimplementing part of the code, in order to take a different approach that no longer depends on functionality that was removed in Java 17. The code in question is easily found by trying to compile JPF with Java 17.
*Note:* You can apply to both projects (Java 11 or 17 support); in that case, please indicate that you would like to work on either one, and what your preference would be.

**Difficulty:** Medium  
**Scope:** 175 hours  
**Required skills:** Knowledge of Java internals  
**Possible Mentors:** Cyrille

<a name="mbt-modbat"></a>
#### ![#4CAF50](https://placehold.co/15x15/4caf50/4caf50.png.md) Test Case Generation/Model-based Testing with Modbat for JPF

**Description:**
JPF requires test cases as a starting point to explore a system. It is therefore suitable to use
test case generation to create test cases automatically. [Modbat](https://github.com/cyrille-artho/modbat/.md) is an open-source tool for test case generation. For testing concurrent software,
an obvious choice would be to combine Modbat (to generate tests) with JPF (to execute tests and
find concurrency problems). This has been done once as a [proof of concept](https://people.kth.se/~artho/papers/ase-2013-preprint.pdf.md) but is not supported in the current version of Modbat.
The main reason for this is that Modbat's reporting has to read and parse bytecode, which requires
access to some native code that JPF does not support.
The goal is to find all problems where Modbat requires native access, and to use jpf-nhandler
to resolve as many of these cases as possible. Remaining cases can be handled with custom model/peer classes, perhaps not with the full feature set, but at least to avoid JPF aborting due to an unsupported feature.

**Difficulty:** Easy  
**Scope:** 350 hours  
**Required skills:** Knowledge of Java Pathfinder  
**Preferred skills:** Knowledge of test generation  
**Possible Mentors:** Cyrille


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

<a name="support-java-11-for-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) Support for Java v11 for SPF

**Description:**
The goal of this project is to upgrade SPF to work with Java 11.

**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** Knowledge of Java v11  
**Possible Mentors:** Yannic, Corina

<a name="improving-string-analysis-in-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) Robustify String solving for SPF

**Description:**
The goal of this project is to test SPF integration with Z3 string constraint solving; adding support cvc5 is a plus.


**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** Knowledge of String constraint solving.  
**Possible Mentors:** Corina, Elena, Soha


<a name="witness-generation-in-spf"></a>
#### ![#f03c15](https://placehold.co/15x15/f03c15/f03c15.png.md) Support the generation of violation witness in GraphML format in SPF

**Description:**
The goal of this project is to support witness generation for SPF in a state-machine format based on GraphML. This would, in particular, help verify SPF's results in SV-COMP.


**Difficulty:** Hard  
**Scope:** 350 hours  
**Required skills:** Knowledge of Symbolic Pathfinder   
**Preferred skills:** GraphML  
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
