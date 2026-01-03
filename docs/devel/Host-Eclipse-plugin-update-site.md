The first step is to create the local version of the update site.  For example, chapter 18, section 3 of "Eclipse Plug-ins, 3rd edition" will explain how to do this.
  
> **Tip:** do not attempt to put the update site in the code repository.  

The plugin and feature files are treated like binaries and bad things will happen.  Here is a sample update site for the mango plugin.

[JPF-Mango](https://jpf.byu.edu/hg/jpf-mango)

Now you will re-create this directory structure within the wiki.  For the purpose of this discussion, let's pin down the jpf site:

~~~~~~~~ {.bash}
JPFHOME=https://github.com/javapathfinder/jpf-core
~~~~~~~~

Now chose a home directory, say `HOME`.  For the mango plugin, 

~~~~~~~~ {.bash}
HOME=wiki/projects/jpf-mango
~~~~~~~~

Whatever choice of `HOME` you make, the update site you advertise to the world will be `JPFHOME/raw-attachment/HOME/update/`.


The `raw-attachment` segment is the *trick* that makes everything work out.  The next step is to create the directory structure for the mirrored update site.  Within `JPFHOME/HOME`, create a link to `JPFHOME/HOME/update`.  Now go to the update page and add the attachments artifacts.jar, content.jar, and site.xml from your local update site.  Create links within `JPFHOME/HOME/update` to `JPFHOME/HOME/update/features` and `JPFHOME/HOME/update/plugins`.  

Attach your feature jar to the features page, and your plugin jar to the plugins page.  That's all there is to it.

> **Tip:** when updating your update site, be sure to sync your plugin and feature with new, higher, revision numbers.  Now rebuild the local site.  Delete all the corresponding attachments in the wiki, and repopulate with the updated versions.

> **Bonus tip:** Once everything is working, you can delete the link to the update site.  This will prevent your visitors from accidentally going to an uninteresting page.  You can always access this page directly from the browser by entering `JPFHOME/HOME/update`.
