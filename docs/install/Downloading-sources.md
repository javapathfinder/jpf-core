<!-- This version of "Downloading sources" came from https://github.com/jeandersonbc/jpf-core/wiki/Downloading-sources-->
# JPF Source Repository Access #

JPF sources are kept as [Git.](https://git-scm.com/) repositories on GitHub within the [Java Pathfinder organization.](https://github.com/javapathfinder/). You need to clone the repository (e.g. https://github.com/javapathfinder/jpf-core) that you are interested in.

There are two stable branches in our repository:
1. `java-8`: It provides Java 8 support using Gradle build system.
2. `java-11`: It provides Java 11 support using Gradle build system and is also the **default** branch now.

Feel free to fork the desired repository. Contributions are welcome, and we invite you to subscribe to our mailing list: java-pathfinder@googlegroups.com

Git is a [Distributed Version Control System.](http://betterexplained.com/articles/intro-to-distributed-version-control-illustrated/) (DVCS), like Mercurial. If you are not familiar with this, it means "all repositories are created equal", and you have to read up a bit. The foremost authority is ["PRO Git Book".](https://git-scm.com/book/en).

We also encourage you to check the following GitHub guides to familiarize yourself with the GitHub development workflow:

1. [Fork a Repo.](https://help.github.com/articles/fork-a-repo/)
2. [About Pull Requests.](https://help.github.com/articles/about-pull-requests/)

## Command Line Access ##


#### Getting the source files

To check out the jpf-core, it is recommended to fork the repository.


> If you only want to download the project, you can just download the repository content as a zip file.
> On the repository page, click on the `Clone or Download` button, and proceed with `Download as ZIP`.


When you fork a GitHub repository, you create a copy of the project in your GitHub account.
Then, use the git command `clone` to check out your forked repository in your local machine.

> In the following example, we use SSH but you can also use HTTPS. Note that you will have to use your
> username and password when using HTTPS. See the [Connecting to GitHub with SSH.](https://help.github.com/articles/connecting-to-github-with-ssh/) guide for more info.

~~~~~~~~ {.bash}
> cd ~/projects

> git clone git@github.com:<your_username>/jpf-core.git
Cloning into 'jpf-core'...
remote: Counting objects: 2036, done.
remote: Compressing objects: 100% (63/63), done.
remote: Total 2036 (delta 33), reused 54 (delta 19), pack-reused 1941
Receiving objects: 100% (2036/2036), 1.75 MiB | 1.57 MiB/s, done.
Resolving deltas: 100% (903/903), done.
~~~~~~~~

The same process can be repeating by substituting `jpf-core` with other projects within the [Java Pathfinder organization.](https://github.com/javapathfinder/) you are interested in. You can install the projects wherever you want, but you have to remember where you installed them for the subsequent [site.properties.](Creating-site-properties-file) configuration.


#### Synchronizing your forked repository with our main repository

When you have a forked repository, it will not update automatically when the original repository updates.
To keep your forked repository synchronized, proceed with the following steps:

1. Add a reference to our main repository

~~~~~~~~ {.bash}
> cd ~/projects/jpf-core
> git remote add upstream http://github.com/javapathfinder/jpf-core
~~~~~~~~

2. Use the git command `pull` to fetch and merge the changes from `upstream` into your local repository

~~~~~~~~ {.bash}
> git pull upstream master
From https://github.com/javapathfinder/jpf-core
 * branch            master     -> FETCH_HEAD
Updating 9a10635..18a0c42
Fast-forward
 .gitignore                                           | 63 ++++++++++++++++++++++++++++++++++++++++++++++++++++++---------
 .travis.yml                                          | 17 +++++++++++++++++
 README.md                                            |  3 ++-
 src/main/gov/nasa/jpf/vm/ClassInfo.java              |  2 +-
 src/tests/gov/nasa/jpf/test/vm/basic/MethodTest.java | 20 ++++++++++++++++++++
 5 files changed, 94 insertions(+), 11 deletions(-)
 create mode 100644 .travis.yml
~~~~~~~~

Now, your local repostory is synchronized, but you need to update your remote (forked repository on GitHub) repository.

3. Use the git command `push` to submit the local changes:


~~~~~~~~ {.bash}
> git push origin master
~~~~~~~~

If you want to contribute to the project, you must make changes in your local repository and push them to your forked remote repository. In this situation, your remote repository is ahead of ours, and you must **create a pull request**. For more info, please, check the [Creating a Pull Request.](https://help.github.com/articles/creating-a-pull-request/) guide.

{% include navigation.html %}
