# Checkout and Build from Shell Prompt (Unix) #

While there is a CVS repository on the JPF Sourceforge site, it is not in use anymore. The current version is kept in the Subversion repository.

<note> We will shift to a distributed version control system (Mercurial or Git) soon</note>


# SVN #

This is not a general introduction of [nor do we cover details of [[https://sourceforge.net/svn/?group_id=136825|Subversion on Sourceforge]((SVN),)(http://subversion.tigris.org|Subversion]]). To obtain the current JPF version, execute the following command from a shell prompt:

~~~~~~~~ {.bash}
>svn checkout https://javapathfinder.svn.sourceforge.net/svnroot/javapathfinder/trunk
~~~~~~~~

To update later-on, enter from within one of the javapathfinder directories


~~~~~~~~ {.bash}
>svn update
~~~~~~~~

To commit (in case you are a project member and have a sourceforge account), use


~~~~~~~~ {.bash}
>svn commit -m "commit message" 
~~~~~~~~

In order to build and test JPF from the commandline, you need Ant and JUnit. If you do not want to use the scripts and versions that are provided with JPF, make sure you have set up your *CLASSPATH* to contain both tools. As of Ant 1.6.5 and JUnit 4.1, this involves the following environment settings:


~~~~~~~~ {.bash}
>export PATH=$PATH:<your-ant-dir>/bin
>export CLASSPATH=<your-ant-dir>/lib/ant.jar:<your-junit-dir>/junit-4.1.jar
~~~~~~~~

~~~~~~~~
<note tip> for your convenience, we have added all required external libraries 
and scripts to the *build-tools* directory, so you do not have to install 
any of the external components.</note>
~~~~~~~~
Now you can proceed as described in section Building JPF from a Command Line. For the impatient reader, this is mainly one command


~~~~~~~~ {.bash}
>cd javapathfinder-trunk
>build-tools/bin/ant run-tests

Buildfile: build.xml

init:
    [mkdir] Created dir: /users/pcmehlitz/projects/javapathfinder-trunk/build
 ...
compile-jpf:
    [javac] Compiling 543 source files to /users/pcmehlitz/projects/javapathfinder-trunk/build/jpf
 ...
run-tests:
     [echo] --- running Junit tests from build/test..
    [junit] Running TestJavaLangObjectJPF
    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.876 sec
 ...
BUILD SUCCESSFUL
Total time: 2 minutes 25 seconds
~~~~~~~~

or (especially for non-Unix folk)


~~~~~~~~ {.bash}
>java RunAnt run-tests
~~~~~~~~

which should compile the whole system and runs the regression test suite with the provided Ant and JUnit versions.