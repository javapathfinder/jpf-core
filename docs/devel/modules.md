# JPF Runtime Modules #

JPF is partitioned into separate projects that all follow the same directory layout and build process. Modules can be distributed as source or binary distributions. Binary distributions are just slices through the directory tree of a source distribution that preserve the permanent build artifact, i.e. both distribution forms are runtime-compatible.

![Figure: JPF Modules]({{ site.baseurl }}/graphics/jpf-project.svg)
 
The main artifacts are the *.jar files created and stored in the `build` directory. We can divide this into classes that are executed by the host VM (i.e. have to be in JPF's `native_classpath` setting), and classes that are executed by JPF itself (i.e. have to be in JPF's `classpath` setting). The first category includes [listeners](Listeners) and [native peers](Model-Java-Interface), the second one model classes (compiled from `src/classes`) and annotations, i.e. the system under test code.

The build process is [Gradle](https://gradle.org/) based, which means every source distribution comes with a build.gradle script and Gradle wrapper that implements the basic build tasks `clean`, `build` and `test`.

We do not include required 3rd party runtime libraries in the project distributions. Gradle automatically manages build dependencies. The `build` task uses the standard `javac` command which requires a full JDK installation. `test` generally executes a JUnit based regression test suite.

The `lib` directory contains 3rd party libraries that are required at runtime of the project (like bcel.jar in jpf-core).

For convenience reasons, JPF modules come with corresponding NetBeans configurations, i.e. can be directly opened as projects within NetBeans.
