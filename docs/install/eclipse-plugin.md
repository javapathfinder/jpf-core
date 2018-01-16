# Installing the Eclipse JPF plugin #

Note that this is assuming the latest Eclipse and Java versions, which is Eclipse 4.3.x and Java 7 at the time of this writing. Older versions might or might not work.
There are three different ways to install the plugin, which are listed in the order of preference if you want to ensure that you are using the latest plugin version:

### Build from sources ###
If you have the Eclipse Plugin Development Environment installed (PDE -comes with standard Eclipse distribution), the preferred way is to download the [eclipse-jpf](./eclipse-jpf) sources from the repository, build the plugin into your <eclipse>/dropins/plugins directory, and restart Eclipse. This ensures you are using the latest version of the plugin, and the build process will tell you if your Eclipse is compatible.

### Install from attached plugin jar ###
Alternatively, you can download one of the attached eclipse-jpf_<version>.jar files, place it into your <eclipse>/dropins/plugins directory, and restart Eclipse.

### Install from update site ###
The most convenient, but least up-to-date way to install eclipse-jpf is to use the Eclipse/Help/Install New Software... dialog, by entering the the eclipse-jpf update site URL:

     http://babelfish.arc.nasa.gov/trac/jpf/raw-attachment/wiki/projects/eclipse-jpf/update
