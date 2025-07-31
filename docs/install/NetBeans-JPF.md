# netbeans-jpf #

This is a plugin to launch JPF on selected application properties (*.jpf) files from within NetBeans. No worries, this is a minimal plugin that mainly starts an external process (JPF), so it doesn't muck with your NetBeans views.

The main functions are

 - start the JPF shell in an external process
 - wait for editor positioning requests from the JPF shell, and show the corresponding source lines

Why is this named differently than all the other projects? Because it is a different kind of project: this is *not* a normal JPF project, i.e. it is not a freeform NetBeans project with the usual JPF directory structure and artifacts. It is a NetBeans module, and it's main artifact is a *.nbm file in the build directory.
