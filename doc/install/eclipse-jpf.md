# eclipse-jpf #

This is a plugin to launch JPF on selected application property files (*.jpf) from within Eclipse. This plugin is the Eclipse analog to netbeans-jpf. This plugin is minimal by design and serves mostly to start an external JPF process from within Eclipse.

To this end, the plugin adds a "Verify.." popup menu item for *.jpf files that are selected in the package explorer window. 

Depending on the selected application property file, output will appear either in the eclipse console view or a jpf-shell.

The site.properties location can be entered in the "JPF Preferences" pane within the normal Eclipse Preferences window. The default location is `$HOME/.jpf/site.properties`.
 

This project is *not* a normal JPF project, i.e. it does not follow the usual JPF directory structure and artifacts. This is an Eclipse plugin project that builds into a jar file (attached) that installs into Eclipse as a plugin. Of course that means you need an Eclipse installation that includes the Plugin Development Environment (PDE) in order to build this project.

### Repository ###
The eclipse-jpf source repository is located on http://babelfish.arc.nasa.gov/hg/jpf/eclipse-jpf

### Installation ###
If you have the eclipse PDE installed, the preferred way is to get the eclipse-jpf sources from the repository, build via the eclipse plugin Export Wizard into the <eclipse-home>/dropins folder and restart eclipse.

If you don't have the eclipse PDE, you can either install this plugin via "Eclipse/Help/Install New Software..." by entering the the eclipse-jpf update site URL: 

      http://babelfish.arc.nasa.gov/trac/jpf/raw-attachment/wiki/projects/eclipse-jpf/update

or download the attached eclipse-jpf_<version>.jar file and move it into your <eclipse-home>/dropins folder. In both cases, you need to restart eclipse in order to load the plugin. Note however that this might not be the latest eclipse-jpf version and it might or might not work with your eclipse.