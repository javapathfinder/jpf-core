# Java Pathfinder wiki repository

This repository is a mirror of the wiki contents. By having a separate
repository, we can accept pull requests for the documentation, which
is something the web interface of github does not offer.

We recommend the following setup with `.git/config` for this repo:

```
[core]
	repositoryformatversion = 0
	filemode = true
	bare = false
	logallrefupdates = true
	ignorecase = true
	precomposeunicode = true
[remote "origin"]
	url = git@github.com:javapathfinder/jpf-core.wiki.git
	fetch = +refs/heads/*:refs/remotes/origin/*
	pushurl = git@github.com:javapathfinder/jpf-wiki-sync.git
[branch "master"]
	remote = origin
	merge = refs/heads/master
```

This setup will ensure that *pushes* go to this repository; all
pushes will be mirrored on the actual wiki page of
https://github.com/javapathfinder/jpf-core within a few minutes.

Conversely, *pulls* will use the wiki of jpf-core, not this repository.
This ensures that changes made over the web interface are not lost.
