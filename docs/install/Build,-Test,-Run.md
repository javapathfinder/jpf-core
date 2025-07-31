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

### Within Eclipse

Eclipse comes with Gradle support by default since the Neon release. If you use an older version for some reason, consider installing the [Buildship Plugin.](https://github.com/eclipse/buildship/blob/master/docs/user/Installation.html) for Gradle support.

To import the project into Eclipse, proceed with the following steps:

1. Start by generating Eclipse configuration files:

```{bash}
> ./gradlew eclipse

BUILD SUCCESSFUL in 0s
3 actionable tasks: 3 executed
```
**Note:** Some versions of Ubuntu provide an outdated version of gradle (see above); visit the [Official Gradle installation guide.](https://gradle.org/install/) for the latest version.

2. Select **File > Import** on the drop-down menu
3. Select **Existing Gradle Project**
4. Choose the root project directory and click **Next**
5. Check the checkbox **Override workspace settings**, then check the option **Gradle wrapper** and click **Finish**.
#### Handling "Access Restriction" errors in the workspace

After importing, you may face some *Access Restriction* errors. To get rid of them, proceed with the following steps:

1. In the **Package Explorer**, right-click on the project name and select **Properties** on the drop-down menu
2. Navigate to **Java Compiler > Errors/Warning** and expand **Deprecated and restricted API**
3. On **Forbidden Reference (access rules)**, select **Ignore** from the drop-down menu and click **Apply and Close**.
4. A pop-up may appear. Click ok to perform a full rebuild.

***
**We avoid adding IDE-related files on the repository as many of them are user-dependent and may change over different versions of the same IDE.**
***

### Within IntelliJ Idea

Importing `jpf-core` on IntelliJ should be straightforward due to its Gradle support.

1. Launch the **New Project** wizard. If no project is currently opened in IntelliJ IDEA, click **Import Project** on the welcome screen. Otherwise, select **File > New > Project** from **Existing Sources** from the main menu.
2. Choose the project root directory containing the build.gradle file. Click OK.
3. On the first page of the Import Project wizard, **in Import Project from External model, select Gradle** and click Next.
4. On the next page of the Import Project wizard, specify Gradle project settings:
  4.1. Check **Use auto-import**
  4.2. Check **Create separate module per source set
  4.3. Make sure that **Use default gradle wrapper (recommended)** is checked
5. Click Finish.

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
