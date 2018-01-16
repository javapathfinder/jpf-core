# Creating a New JPF Module #

So what do you have to do to create a new JPF module? For a quick shortcut to setting up *most* things,  use the `create_project` script of the [jpf-template module](wiki:projects/jpf-template).  

However, jpf-template cannot do everything for you, so see below for more information on how to finish setting up your new project.

Several steps are involved:

### 1. get familiar with the JPF configuration ###
You need to understand how your project will be looked up and initialized during JPF startup, and the place to learn that is the [JPF configuration](../user/config) page. Once you know what *[site properties](../install/site-properties)* and *project properties* are, you can proceed.

### 2. get familiar with the standard JPF project layout ###
Although this is mostly convention, and you can deviate if you really need to, please try hard not to.

You can get the details from the [JPF Runtime Modules](modules) page, but the essence is that each project has two (possible) major build artifacts:

 * `jpf-<module>.jar` - executed by the host (platform) VM (contains main classes and peers)
 * `jpf-<module>-classes.jar` - executed by JPF (contains modeled classes)

Consequently, your sources are kept in `src/main`, `src/peers`, `src/classes`, `src/annotations`, `src/tests` and `src/examples`. You might only have some of these, but please provide regression tests so that people can check if your project works as expected. 

All 3rd party code that is required at runtime goes into a `lib` directory.

We keep potential annotations separate (and provide additional `jpf-<module>-annotations.jar`) so that external projects (systems under test) can use them without relying on all JPF classes to be in their `classpath`. The idea is that this jar does not contain any code which could alter the system under test behavior if you execute it outside of JPF. 

<The `tools` directory contains 3rd party libraries and tools that are used at build-time. For convenience reasons, we usually copy the small `RunJPF.jar` from the jpf-core in here, so that you can easily run JPF from the command line without the need for platform specific scripts or links, but that is completely optional.>

### 3. create a jpf.properties file ###
Within the root directory of each JPF module a project properties file is needed which is named `jpf.properties`. It contains the path settings the host VM and JPF need to know about at runtime. It looks like this:

~~~~~~~~ {.bash}
# standard header
<module-name> = ${config_path}

# classpath elements for the host VM (java)
<module-name>.native_classpath = build/<module-name>.jar;lib/...

# classpath elements for JPF
<module-name>.classpath = build/<module-name>-classes.jar;...

# sources JPF should know about when creating traces etc.
<module-name>.sourcepath = src/classes;...
~~~~~~~~

You can add other JPF properties, but be aware of that this is always processed during JPF startup if you add your module to the `extensions` list in your [site.properties](../install/site-properties), and might conflict with other JPF modules. For this reason you should only add your module to `extensions` if you know it will always be used.


### 4. create your build.xml ###
Our build process is [Ant](http://ant.apache.org/) based, hence we need a `build.xml` file. The standard targets are

 * `clean`
 * `compile`
 * `build` (the default, creates the jars and hence depends on compile)
 * `test` (run JUnit regression tests, depends on build)
 * `dist` (creates a binary-only distribution) 

If you stick to the general layout, you can use a template like the one attached to this page (of course you need to replace `<your-project-name>`).

Please note how `site.properties` and `jpf.properties` can be used from within the `build.xml` (Ant understands a subset of the JPF property syntax), which means you don't have to explicitly add the jars of other JPF components you depend on (at least jpf-core). You can reference them symbolically like this:

~~~~~~~~ {.xml}
  <property file="${user.home}/.jpf/site.properties"/>
  <property file="${jpf-core}/jpf.properties"/>
  ..
  <!-- generic classpath settings -->
  <path id="lib.path">

    <!-- our own classes and libs come first -->
    <pathelement location="build/main"/>
    ...
    <fileset dir=".">
  	    <include name="lib/*.jar"/>
    </fileset>

    <!-- add in what we need from the core -->
    <pathelement path="${jpf-core.native_classpath}"/>
  </path>
  ...
~~~~~~~~

### 5. add your module to your site.properties ###
This is optional, you only need to do this if you want to be able to run your JPF module outside its own directory. If so, add an entry to your [site properties file](../install/site-properties) that looks like this:

~~~~~~~~ {.bash}
...
<module-name> = <path to your JPF extension module>
...
~~~~~~~~

### 6. publish your repository ###
You can publish this wherever you want ([sourceforge](http://sourceforge.net), [bitbucket](http://bitbucket.org), [google code](http://code.google.com), or [github](http://github.com) are suitable free site supporting Mercurial), or ask us to host it on the JPF server. If you decide to use a 3rd party hosting service, please let us/the JPF community know about it (e.g. by posting to the mailing list at [java-pathfinder@googlegroups.com](https://groups.google.com/forum/#!forum/java-pathfinder).