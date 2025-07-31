<!--
Available binary snapshots are attached as *.zip archives to the [jpf-core](./(https://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-core#no1) page. Just click the on the download link and tell your browser where to store them on disk, which you need to remember for your subsequent [site.properties](./(../install/site-properties) configuration.
-->

Currently, we do not have binary snapshots. We hope to provide them again soon.

We recommend putting all JPF modules under a single parent directory that holds the site.properties file:

~~~~~~~~ {.bash}
jpf/
     site.properties
     jpf-core/
     jpf-symbc/
     …
~~~~~~~~

Many JPF modules are still fast moving, so we recommend using the source repositories to stay up-to-date. Our policy is to only push changes to this server which pass all regression tests
{% include navigation.html %}
