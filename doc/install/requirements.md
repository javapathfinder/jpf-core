# System Requirements #

## Java ##
Most of the JPF components, including the [jpf-core](../jpf-core/index), are pure Java applications. The minimal version is Java SE 8 (if you have to use JDK 1.7 or JDK 1.6 you have to check out the 'java-7' and 'java-1.6' branches of our repository, respectively), we generally advise to use the latest stable Java version that is available for your platform. You can find out about your java by running the following statement from the command line.

~~~~~~~~ {.bash}
> java -version
java version "1.8.0_20"
...
~~~~~~~~

JPF is a resource hungry application. We recommend at least 2Gb of memory, and generally use a `-Xmx1024m` setting when launching Java. The disk footprint for most JPF projects is fairly small, the jpf-core takes about 40M when fully expanded with sources, compiled classes and jar files. The binary distribution (jar files) takes less than 1.5M.

Some JPF projects do require 3rd party native executables (like DLLs) that are platform specific. Please refer to the specific project pages for details.

### Java specifics for Windows ###
Make sure you have the JDK installed, otherwise there is no javac compiler available.

In order to build JPF from a Windows Command Prompt (executing `ant.bat` from inside the respective JPF project directory), you have to set the `JAVA_HOME` environment variable. 

### Java specifics for OS X ###
On Mac OS X 10.10, Java 1.7 is default, but `/Applications/Utilities/Java Preferences.app` can change the setting. In some cases, it may be necessary to manually change the symlink that determines which version is default:

~~~~~~~~ {.bash}
sudo rm /System/Library/Frameworks/JavaVM.framework/Versions/Current
sudo ln -s 1.8 /System/Library/Frameworks/JavaVM.framework/Versions/Current
~~~~~~~~

## Mercurial (Version Control System) ##
If you want to download the JPF source repositories, you need to install the [Mercurial](http://mercurial.selenic.com/wiki/) distributed version control system on your machine, which requires [Python](http://www.python.org). If you are using a Windows machine, you can install [TortoiseHg](http://tortoisehg.bitbucket.org/), which provides a Windows Explorer extension and includes Python.

On some Mac OS X systems, it may be necessary to set the `LC_ALL` and `LANG` environment variables for Mercurial to work correctly.

in `~/.bashrc`:

~~~~~~~~ {.bash}
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8 
~~~~~~~~

in `~/.cshrc`:

~~~~~~~~ {.bash}
setenv LC_ALL en_US.UTF-8
setenv LANG en_US.UTF-8 
~~~~~~~~


If you already have Eclipse installed, and want to download the source repositories from within the IDE, you need the [MercurialEclipse](http://javaforge.com/project/HGE) plugin, which you can install from this update-site: http://cbes.javaforge.com/update

Note that NetBeans comes with Mercurial support by default.


## Apache Ant ##

Although you can also build from Eclipse, we use [Apache Ant](http://ant.apache.org) as our primary build system. **Ant is no longer included in the jpf-core distribution** so you have to install it separately. Currently (as of Ant 1.9.3), this involves

 * getting Ant binaries e.g. from http://www.apache.org/dist/ant/binaries/
 * setting the `ANT_HOME` environment variable to the directory where you unpacked the binaries
 * adding `ANT_HOME/bin` to your `PATH` environment variable


## JUnit ##

Our Ant script (build.xml) includes a `test` target which uses [JUnit](http://junit.org) to run regression tests. **JUnit is no longer included in the jpf-core distribution**. For JUnit-4.11 installation involves the following steps

 * get junit-<version>.jar and hamcrest-core-<version>.jar, e.g. from the links on https://github.com/junit-team/junit/wiki/Download-and-Install
 * add both jars to your `CLASSPATH` environment variable 


## JPF IDE plugins ##

JPF components come with project configurations for both [NetBeans](http://www.netbeans.org) and [Eclipse](http://www.eclipse.org), so you might want to use your favorite IDE. Since the JPF build process is [Ant](http://ant.apache.org)-based, NetBeans is generally a better fit because it is Ant-based and can make direct use of your JPF site configuration.

If you want to install the [Eclipse plugin](./eclipse-jpf), you need an Eclipse version >= 3.5 (Galileo) **running under JavaSE-1.8**. Please see the [Installing the Eclipse JPF plugin](./eclipse-plugin) page for details.

If you want to go with Eclipse and have to rebuild the JPF [Eclipse plugin](./eclipse-jpf), make sure you install the Eclipse Plugin Development Environment (PDE) from the respective Eclipse server.

If you want to use the [NetBeans plugin](./netbeans-jpf), the minimal NetBeans version is 6.5.