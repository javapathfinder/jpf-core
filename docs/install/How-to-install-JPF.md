The JPF core and most of its extensions are pure Java applications, so there are not many platform requirements other than sufficient memory and a reasonably fast machine. Use of IDEs is optional, but NetBeans is recommended for JPF development.

You can obtain JPF sources from the Git repositories. You need at least the core of JPF, [jpf-core](https://github.com/javapathfinder/jpf-core), which can be built from the command line using Gradle or directly opened as a [NetBeans](http://www.netbeans.org) project.

The JPF core project already comes with its configuration file, but you have to create a per-site [site.properties](Creating-site-properties-file) file.

If you use the JPF shells (graphical JPF front-ends), you might also want to install the NetBeans adapter plugin, although shells are standalone Java (swing) applications that can also be used without an IDE.

Here are the details:

  - [System requirements.](System-requirements)
  - [Downloading binary snapshots.](Downloading-binary-snapshots)
  - [Downloading sources from the GitHub repositories.](Downloading-sources)
  - [Creating a site properties file.](Creating-site-properties-file)
  - [Building, testing, and running.](Build,-Test,-Run)
  - [Installing the NetBeans plugin.](NetBeans-Plugin)
