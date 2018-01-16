# How to Obtain and Install JPF #

The JPF core and most of its extensions are pure Java applications, so they are not many platform requirements other than sufficient memory and a reasonably fast machine. Use of IDEs is optional, but most JPF modules include out-of-the-box configuration files for both Eclipse and Netbeans.

You can obtain JPF sources from the [Mercurial](http://mercurial.selenic.com/wiki/) repositories, but it is not recommended to clone this directory itself (you most likely would get old sub-repository revisions). You need at least the core of JPF, [jpf-core](../jpf-core/index) which can be built with [Ant](http://ant.apache.org) from the command line, or directly opened as a [NetBeans](http://www.netbeans.org) or [Eclipse](http://www.eclipse.org) project.

The JPF core project already come with its configuration file, but you have to create a per-site [site.properties](site-properties) file.

If you use the JPF shells (graphical JPF front-ends), you might also want to install the corresponding NetBeans or Eclipse adapter plugins, although shells are standalone Java (swing) applications that can also be used without an IDE.

Here are the details:

  - [System requirements](requirements)
  - [Downloading binary snapshots](snapshot)
  - [Downloading sources from the Mercurial repositories](repositories)
  - [Creating a site properties file](site-properties)
  - [Building, testing, and running](build)
  - [Installing the Eclipse plugin](eclipse-plugin)
  - [Installing the NetBeans plugin](netbeans-plugin)