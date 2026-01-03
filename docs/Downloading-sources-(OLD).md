# JPF Source Repository Access #

***
**This is out-dated content. Please, refer to the page [Downloading Sources](https://github.com/javapathfinder/jpf-core/wiki/Downloading-sources) for an updated version.**
***

JPF sources are kept as [Mercurial](http://www.selenic.com/mercurial) repositories within the http://babelfish.arc.nasa.gov/hg/jpf directory. You need to clone the subrepositories (e.g. http://babelfish.arc.nasa.gov/hg/jpf/jpf-core) that you are interested in, **not** the root directory ../hg/jpf itself (which most likely will give you old subrepo revisions).

We provide anonymous, public read access. If you want to push your changes back to the repository, and you are not a NASA Ames employee, you need to [obtain a JPF contributor account](wiki:about/account).

Mercurial is a [Distributed Version Control System](http://betterexplained.com/articles/intro-to-distributed-version-control-illustrated/) (DVCS), like Git. If you are not familiar with this, it means "all repositories are created equal", and you have to read up a bit. The foremost authority is ["Mercurial: The Definite Guide"](http://hgbook.red-bean.com/).

For the inpatient, we also provide a short [Mercurial primer](../devel/mercurial).

## Command Line Access ##

To check out the jpf-core use the mercurial command `clone`:

~~~~~~~~ {.bash}
> cd ~/projects

> hg clone http://babelfish.arc.nasa.gov/hg/jpf/jpf-core
destination directory: jpf-core
requesting all changes
...
added 71 changesets with 2045 changes to 1694 files
updating working directory
683 files updated, 0 files merged, 0 files removed, 0 files unresolved
~~~~~~~~

The same process can be repeating by substituting `jpf-core` with the [project](../projects/index) you are interested in. You can install the projects wherever you want, but you have to remember where you installed them for the subsequent [site.properties](../install/site-properties) configuration.

To update your local repository, change to its directory and do `pull` (don't forget the `-u` option, or your working directories will not get updated) 

~~~~~~~~ {.bash}
> cd ~/projects/jpf-core
> hg pull -u
~~~~~~~~

If you want - and are allowed - to push back your changes, you use **`https://`**`babelfish.arc.nasa.gov/hg/jpf/<project>` as the URL, which will require entering your user-name and password. Before pushing the changes you have to commit the changes from your working directory to your local repository. 

~~~~~~~~ {.bash}
> cd ~/projects/jpf-core
> hg commit -m "this commits to the local repository"
~~~~~~~~

The changes now can be pushed to the central repository using the following command

~~~~~~~~ {.bash}
> hg push https://babelfish.arc.nasa.gov/hg/jpf/jpf-core
~~~~~~~~

## Mercurial Support within NetBeans ##

There is no need to install any plugins, NetBeans is distributed with Mercurial support.


## Mercurial Plugin for Eclipse ##

To work within Eclipse

* Download and install the [MercurialEclipse](http://javaforge.com/project/HGE) plugin, which at the time of this writing is available from the update site: http://cbes.javaforge.com/update (the usual Eclipse spiel: **Help** -> **Install New Software...** -> **add site**, enter the update URL above) 

* In the eclipse menu: **File** -> **Import** -> **Mercurial** -> **Clone repository using Mercurial** -> **Next**

* In the repository location, URL, specify http://babelfish.arc.nasa.gov/hg/jpf/jpf-core

* Check the box for 'Search for .project files in clone and use them to create projects'

* Click on **Finish** 

The steps listed above will clone the repository in your workspace. Right clicking on the project will show a 'Team' option that allows to perform all the version control operations. 
