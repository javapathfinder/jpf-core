# Creating  a site.properties file #

The site.properties file tells JPF at startup time where to look for installed projects, so that it can add classpaths accordingly without you having to type off your fingers. It is a normal [Java properties](http://en.wikipedia.org/wiki/.properties) file, which supports a few additional things like key expansion.

While you can tell JPF at startup time where to look for `site.properties`, we recommend using the default location, which is **`<user.home>/.jpf/site.properties`**. If you don't know what value the standard Java system property `user.home` has on your machine, please run the attached Java program. On Unix systems, this is your home directory.

Assuming that you installed your JPF projects under `<user.home>/projects/jpf`, a typical `site.properties` looks like this:

~~~~~~~~ {.bash}
# JPF site configuration

jpf-core = ${user.home}/projects/jpf/jpf-core

# numeric extension
jpf-numeric = ${user.home}/projects/jpf/jpf-numeric

# annotation-based program properties extension
jpf-aprop = ${user.home}/projects/jpf/jpf-aprop

extensions=${jpf-core},${jpf-aprop}

#... and all your other installed projects
~~~~~~~~ 

If you are a Windows user, and you want to enter absolute pathnames, don't use unquoted backslashes '\' since the `java.util.Properties` parser would interpret these as special chars (like `"\n"`). You can use ordinary slashes '/' instead. To avoid drive letters, use system properties like `${user.home}`.


A sample site.properties file is attached to this page. Note that the "`${..}`" terms are automatically expanded by JPF, i.e. you do not have to enter explicit paths.

Each installed project is defined by a "`<project-name> = <project-directory>`" key/value pair. The project name is usually the same as the repository name.

Note that we don't require anymore that all projects are in the extensions list, **but** jpf-core (or wherever your JPF core classes are) now needs to be in there. In fact, you probably want to have only `jpf-core` in `extensions`, and use the `@using <project-name>` for the other ones from either your project properties (jpf.properties, for project dependencies) or - usually - from your application properties (*.jpf) files. See [JPF configuration](../user/config) for details.