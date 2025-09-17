The JPF project has been using [Ant](https://ant.apache.org/) to automate the build process since it was published. Although Ant is a popular build tool for Java project it has some drawbacks such as the lack of automatic dependency resolution and the large and verbose XML-based script file. Given the necessity to move to a more flexible alternative, we migrated to [Gradle](https://gradle.org/) as the standard builder for JPF. In the following, we summarize what has been achieved and what is remaining.

## Gradle Support to JPF-core

The JPF-core module has been fully migrated to Gradle and it is already merged into master.
A full detailed list of the Gradle tasks can be obtained by running `./gradlew tasks`. Tasks can be identified by their group (e.g., "JPF Jars tasks", "JPF Build Resources tasks", etc).
Here is a description of the main tasks:


***
For more details, refer to the Wiki pages. Updated pages are highlighted later in this page.
***


```
JPF Build tasks
---------------
buildJars - Generates all core JPF jar files.
compile - Compiles all JPF core sources.

JPF Distribution tasks
----------------------
dist - Builds binary distribution.
srcDist - Builds the source distribution.

Verification tasks
------------------
test - Runs core regression tests.
```

### Summary of Changes


In the following, we summarize the changes in reverse chronological order.

|Summary| PR(s) |
|---|---|
|Support to Gradle Build and Repository Cleanup | [#98](https://github.com/javapathfinder/jpf-core/pull/98) |
|Added missing packaging tasks on Gradle build | [#84](https://github.com/javapathfinder/jpf-core/pull/84) |
|Created buildinfo task (#78) | [#79](https://github.com/javapathfinder/jpf-core/pull/79) |
|Added tests to verify if resources exist in classpath | [#76](https://github.com/javapathfinder/jpf-core/pull/76) |
|Added ignored tests (Fixes #56) | [#74](https://github.com/javapathfinder/jpf-core/pull/74) |
|Adds support to Jar tasks on Gradle build | [#70](https://github.com/javapathfinder/jpf-core/pull/70) |
|Copying build.properties to output dir | [#65](https://github.com/javapathfinder/jpf-core/pull/75) |
|Implemented log summary and updated log configurations | [#55](https://github.com/javapathfinder/jpf-core/pull/55) |
|Added Java Plugin to the Gradle build | [#52](https://github.com/javapathfinder/jpf-core/pull/52) |
|Added minimal Gradle support to jpf-core | [#45](https://github.com/javapathfinder/jpf-core/pull/45) |



### Updated Wiki pages

1. [How to install JPF](https://github.com/javapathfinder/jpf-core/wiki/How-to-install-JPF)
2. [System requirements](https://github.com/javapathfinder/jpf-core/wiki/System-requirements)
3. [Downloading sources](https://github.com/javapathfinder/jpf-core/wiki/Downloading-sources)
4. [Build, Test, Run](https://github.com/javapathfinder/jpf-core/wiki/Build,-Test,-Run)

### Pending Tasks

In parallel to this project, there is work in progress related to [Java 10 support](https://github.com/javapathfinder/jpf-core/wiki/Support-Java-10-for-JPF-CORE) in a dedicated project.
Therefore, it is also important to migrate the Java 10 support to use Gradle, as well.
The work to support Gradle in the Java 10 branch is still in the beginning.
The `gradle` branch is in synchronization with the `java-10` branch, and the idea is to merge the `gradle` branch to the `java-10` branch in a later moment. Check the following links for more details on the current state of the Gradle support on Java 10:

* [Bring Gradle to Java 10](https://github.com/javapathfinder/jpf-core/issues/138)
* [Add support to compile tasks on Gradle with Java 10](https://github.com/javapathfinder/jpf-core/issues/139)

## Gradle Support to JPF-nhandler

We have been working to update JPF-nhandler in a forked repository (see below).
First, we fixed some bugs and cleaned the Ant build script to remove unnecessary warnings and make the tests pass.
Once the Ant build has been fixed, it remains to migrate it to Gradle.
The work is in progress and because the build of this JPF extension is simple, must be finished in the following days.
Another concern is running tests in a Continuous Integration (CI) environment. Every JPF extension depends on the presence of JPF-core. It remains to elaborate a way to run JPF extensions in a CI environment. Although it may be a bit out of scope, having a CI running tests is important to the maintenance of the project.

- https://github.com/jeandersonbc/jpf-nhandler
- [Issue 1 - Investigate the cause of failing tests (Closed)](https://github.com/jeandersonbc/jpf-nhandler/issues/1)

## Lessons Learned

#### Have a stable build before proceeding

Migrating to a different build tool can be challenging. The build tool is responsible for compiling, packaging, running tests, and sometimes, managing resources for tests. Misconfigurations may break the build and manifest as compilation and test errors. For this reason, it is extremely important to have a stable build as a reference. Without a reference to what is expected to achieve, it is nontrivial to determine if the build breaks due to misconfigurations or if the project to be built is broken.

#### Add support to the new build tool incrementally

It is easier when you break down the build cycle and identify tasks and their dependencies. Once you have identified tasks and their dependencies, it is recommended to start with the simpler tasks, i.e., no dependency, and continue with the tasks with fewer dependencies, until you have covered all tasks.
Making an analogy to a tree (graph), you should start by the leaves and move forward, layer by layer, until you reach the root node.

In this iterative process, it is important to compare outputs. We created a simple yet useful tool to assist in this task. This tool compares the output of two build tools by running a recursive diff. Visit [builder-diff](https://github.com/jeandersonbc/builder-diff) for more info.