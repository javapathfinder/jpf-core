# Java PathFinder

An extensible software model checking framework for Java bytecode programs

## General Information about JPF

All the latest developments, changes, documentation can be found on our
[wiki](https://github.com/javapathfinder/jpf-core/wiki) page.

## Building and Installing

If you are having problems installing and running JPF, please look at the [How
to install
JPF](https://github.com/javapathfinder/jpf-core/wiki/How-to-install-JPF) guide.

Note that we have transitioned to OpenJDK with the new version of JPF (for Java 11), so unlike branch `java-8`, the new default version for Java 11 no longer compiles using Oracle's JDK.

We have documented on the wiki a lot of common problems during the install and
build processes reported by users.  If you are facing any issue, please, make
sure that we have not addressed it in documentation. Otherwise, feel free to
contact us at java-pathfinder@googlegroups.com or open an issue on the Issue
Tracker.

## Documentation

There is a constant effort to update and add JPF documentation on the wiki.
If you would like to contribute in that, please, contact us at
java-pathfinder@googlegroups.com.

Contributions are welcomed and we encourage you to get involved with the
community.

Happy Verification
*-- the Java PathFinder team*


---

## Running with Docker

You can build and run JPF inside a container using the provided `Dockerfile` and `docker-compose.yml`.

### 1. Build the image
```bash
docker-compose build
```
### 2. Start a container
```bash
docker-compose run --rm jpf-dev
```
### 3. Build JPF
```bash
./gradlew clean build
```
