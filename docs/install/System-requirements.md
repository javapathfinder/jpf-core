## Java ##
Most of the JPF components, including the [jpf-core.](JPF-core), are pure Java applications. The oldest version supported is Java SE 8 (in `java-8` branch) and the latest Java version supported is Java SE 11 (in `java-11` branch). The other branches (except `java-8` and `java-11`) are obsolete and will no longer be maintained.

> If you have to use JDK 1.7 or JDK 1.6, please, get in touch with us through our mailing list (java-pathfinder@googlegroups.com).
> Keep in mind that JDK 1.7 and JDK 1.6 have reached their end-of-life cycle and Oracle does not support them anymore.


You can find out about your java by running the following statement from the command line.

~~~~~~~~ {.bash}
> java -version
java version "1.8.0_20"
...
~~~~~~~~

JPF is a resource hungry application. We recommend at least 2Gb of memory, and generally, use a `-Xmx1024m` setting when launching Java. The disk footprint for most JPF projects is fairly small, the jpf-core takes about 40M when fully expanded with sources, compiled classes and jar files. The binary distribution (jar files) takes less than 1.5M.

Some JPF projects do require 3rd party native executables (like DLLs) that are platform specific. Please refer to the specific project pages for details.

### Java specifics for Windows ###
Make sure you have the JDK installed, otherwise there is no javac compiler available.

In order to build JPF from a Windows Command Prompt, you have to set the `JAVA_HOME` environment variable. 

### Java specifics for OS X ###
On Mac OS X 10.10, Java 1.7 is default, but `/Applications/Utilities/Java Preferences.app` can change the setting. In some cases, it may be necessary to manually change the symlink that determines which version is default:

~~~~~~~~ {.bash}
sudo rm /System/Library/Frameworks/JavaVM.framework/Versions/Current
sudo ln -s 1.8 /System/Library/Frameworks/JavaVM.framework/Versions/Current
~~~~~~~~

## Git (Version Control System) ##

If you want to download the JPF source repositories, you need to install the [Git.](https://git-scm.com/downloads) distributed version control system on your machine. Most Unix platforms come with Git installed. You can check if you have Git on your machine with the following command:

```{bash}
> git --version
```

If you are new to Git, check the [official website.](https://git-scm.com/) to learn the basics. You can also find some GUI Clients for different platforms.
Note that all major IDEs (e.g., Netbeans, Eclipse, IntelliJ) comes with Git support by default.

For more information about Git and how to use it to clone the repository, refer to the [Downloading Sources.](https://github.com/javapathfinder/jpf-core/wiki/Downloading-sources) page.

***

That's all you need! We build JPF with the Gradle Build System, and we provide a wrapper in our repository that requires you nothing but Java. Check the [Build, Test, and Run.](https://github.com/javapathfinder/jpf-core/wiki/Build,-Test,-Run) page for more information.

## Apache Ant ##

JPF doesn't use Ant build system anymore.

## JPF IDE plugins ##

JPF components come with project configurations for both [NetBeans.](http://www.netbeans.org) and [Eclipse.](http://www.eclipse.org), so you might want to use your favorite IDE. Since the JPF build process is [Ant.](http://ant.apache.org)-based, NetBeans is generally a better fit because it is Ant-based and can make direct use of your JPF site configuration.

If you want to install the [Eclipse plugin.](./eclipse-jpf), you need an Eclipse version >= 3.5 (Galileo) **running under JavaSE-1.8**. Please see the [Installing the Eclipse JPF plugin.](./eclipse-plugin) page for details.

If you want to go with Eclipse and have to rebuild the JPF [Eclipse plugin.](./eclipse-jpf), make sure you install the Eclipse Plugin Development Environment (PDE) from the respective Eclipse server.

If you want to use the [NetBeans plugin.](./netbeans-jpf), the minimal NetBeans version is 6.5.

{% include navigation.html %}
