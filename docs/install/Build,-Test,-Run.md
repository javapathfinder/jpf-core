JPF builds using Gradle on two stable branches `java-8` and `java-11`, which support Java 8 and Java 11 respectively.
In the following, we provide instructions for your:

***
### Important
**Some Java releases (e.g., `jdk1.8.0_101`, `_102`, `_111`) do not include `sun.misc.JavaOISAccess`. This will cause compilation errors during the build execution. Consider building JPF with an updated Java version to avoid such errors.**
***

## Building JPF

### Using the command line

The JPF repository includes a Gradle wrapper that requires nothing except Java to execute. It ensures that all JPF developers and environments use the same builder to avoid any kind of configuration issue.
Note that we assume that `./gradle` is used below, which installs a local copy of version 4. If you use your own version of Gradle, make sure it is version 4 or newer.

**Note:** On Ubuntu, the `command apt-get install gradle` seems to install an older version of gradle (version 2.x) which is incompatible with the project and causes unzipping errors. Hence, it is recommended to visit the [Official Gradle installation guide.](https://gradle.org/install/) for installing the latest version of gradle.

> If you are using Windows, consider the `gradlew.bat` script.

```{bash}
> cd jpf-core
> ./gradlew buildJars

...
BUILD SUCCESSFUL in 13s
16 actionable tasks: 16 executed
```

In the following, there is a summary of the main build tasks.
If you want to have some help about what other tasks are available, check the command `./gradlew tasks --all`.

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

***
**We avoid adding IDE-related files on the repository as many of them are user-dependent and may change over different versions of the same IDE.**
***

## Running JPF ##

### Using the command line ###

~~~~~~~~ {.bash}
> cd jpf-core
> java -jar build/RunJPF.jar src/examples/Racer.jpf
JavaPathfinder v5.0 - (C) 1999-2007 RIACS/NASA Ames Research Center
.....
====================================================== statistics
elapsed time:       0:00:00
states:             new=9, visited=1, backtracked=4, end=2
search:             maxDepth=5, constraints=0
choice generators:  thread=8, data=0
heap:               gc=8, new=291, free=32
instructions:       3112
max memory:         79MB
loaded code:        classes=73, methods=1010

====================================================== search finished: 1/12/10 2:30 PM
~~~~~~~~
