jpf-core currently builds and runs on Java 8. In this work, we introduced partial support for Java 10, leveraging the new features like modularity, strong encapsulation, while also handling the deprecates and removes. 

JEPs that introduce internal changes to JPF include, but not limited to:

* [Project Jigsaw](#compiling-mji-model-classes)
* [JEP 220: Modular Run-Time Images](#new-jrtclassfilecontainer-to-load-classes-from-the-run-time-image)
* [JEP 254: Compact Strings](#update-mji-model-class-for-javalangstring-to-comply-with-jep-254)
* [JEP 259: Stack-Walking API](#miscellaneous)
* [JEP 260: Encapsulate Most Internal APIs](#handling-access-warnings)
* [JEP 277: Enhanced Deprecation](#deprecations-in-the-jdk)
* [JEP 280: Indify String Concatenation](#currently-unsupported-features)

**JDK Release Notes**

* [JDK 9 Release Notes](http://www.oracle.com/technetwork/java/javase/9-relnote-issues-3704069.html)
* [JDK 10 Release Notes](http://www.oracle.com/technetwork/java/javase/10-relnote-issues-4108729.html)

[jep-220]: http://openjdk.java.net/jeps/220
[jep-254]: http://openjdk.java.net/jeps/254
[jep-259]: http://openjdk.java.net/jeps/259
[jep-260]: http://openjdk.java.net/jeps/260
[jep-277]: http://openjdk.java.net/jeps/277
[jep-280]: http://openjdk.java.net/jeps/280

### Compiling MJI model classes

Split packages are not allowed since Java 9's [Project Jigsaw](http://openjdk.java.net/projects/jigsaw/quick-start) (packages having the same name exist in different modules). So in order to compile a model class we need to patch it. But since we had sources for multiple modules in the same tree in [src/classes][classes-dir], we first separated them into directories based on their respective modules, for ease of compilation.

The new directory structure looks as follows:
```
├── classes
│   ├── gov
│   │   └── nasa
│   │       └── jpf
│   ├── modules
│       ├── java.base
│       │   ├── java
│       │   │   ├── io
│       │   │   ├── lang
│       │   ├── jdk
│       │       └── internal
│       └── java.logging
│           └── java
│               └── util
```

These are then compiled, as follows:
```
javac --patch-module java.base=src/classes/modules/java.base
                     java.logging=src/classes/modules/java.logging src/classes
```

| Summary                                                               | PR(s)         |
| --------------------------------------------------------------------- |:-------------:|
| Changes made to be able to successfully compile MJI model classes     | [#28][28]     |


### Update MJI model class for java.lang.String to comply with JEP 254

String model class is modified, to follow a structure similar to the standard String class in JDK 9 and later which uses a byte array plus an encoding-flag instead of UTF-16 char array to represent the String.

| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Update String model class to comply with JEP 254                            | [#39][39]            |
| Add support for Compact Strings                                             | [#119][119]          |
| Update `String#length` to comply with JEP 254                               | [#129][129]          |
| Add package private constructor `java.lang.String#String(byte[], byte)`     | [#137][137]          |
| Add method `java.lang.String.getBytes(byte[], int, byte)`                   | [#136][136]          |
| Remove invalid assertion in `String#getBytes(byte[], int, byte)`            | [#144][144]          |

Implementation changes were also made to some of the methods that construct ElementInfo(s) for String objects, in gov.nasa.jpf.vm.GenericHeap, since the `String#value` field, has changed from `char[]` to a `byte[]`


| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Refactor other String related classes and methods to comply with JEP 254    | [#133][133]          |


### Update NativePeer class for java.lang.String to comply with JEP 254

Most methods in JPF_java_lang_String had failed as the `value` field have changed from char[] to a byte[] since [JEP 254][jep-254]. So instead of retrieving the value field, and performing operations on that value field to return a result (which is now complex as the value field now being a byte[] and having a coder which specifies different encodings), we turn JPF String object into a VM String object using `MJIEnv.getStringObject` and then delegates the method call to that VM object.

| Summary                                                                     | Commit(s)                |
| --------------------------------------------------------------------------- |:------------------------:|
|  Refactor JPF_java_lang_String to fix invalid casting of value field        | [1ccefdf][1ccefdf]       |

### Changes to class URI(s)

Accessor methods that are being used to retrieve class URIs were modified to comply with the new URI structure introduced by the new Module System. The new path entry also includes a path segment that specifies the module name of that class.

Before:

    /path/to/container/java/lang/Object.class

After:

    /path/to/container/java.base/java/lang/Object.class


| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Update JVMClassFileContainer#getClassURL to comply with Module System       | [#100][100]          |
| Update JVMClassFileContainer#getModuleName                                  | [#121][121]          |

### New JRTClassFileContainer to load classes from the run-time image

As stated in the JDK 9 Release Notes the system property `sun.boot.class.path` has been removed. Moreover, rt.jar has been removed since [JEP 220][jep-220] and is replaced by the new runtime. This causes JPF to fail resolve standard Java classes (classes that we don't have model classes for).

So if a class is not found in the classpath, now we try to load that class from the run-time image.

| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Add gov.nasa.jpf.jvm.JRTClassFileContainer                                  | [#102][102]          |

### Changes to NativePeer classes

Following PRs address UnsatisfiedLinkError(s) that appears due to missing NativePeer classes and NativePeer methods.

| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Add NativePeer JPF_java_lang_StringUTF16                                    | [#117][117]          |
| Add NativePeer JPF_java_lang_StackStreamFactory                             | [#122][122]          |
|                                                                             |                      |
| Implement missing, and/or unlinked native methods in Unsafe                 | [#105][105]          |
| Add NativePeer method for Reflection#getClassAccessFlags(Class)             | [#123][123]          |
|                                                                             |                      |
| Rename JPF_sun_reflect_Reflection to JPF_jdk_internal_reflect_Reflection    | [#104][104]          |
| Rename JPF_sun_misc_VM to JPF_jdk_internal_misc_VM                          | [#116][116]          |

### Changes to MJI model classes

Changes made to MJI model classes, primarily to prevent NoSuchMethodError(s):

| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Add accessor methods for SharedSecrets#javaNetURLAccess                     | [#110][110]          |
| Add accessor methods for SharedSecrets#javaObjectInputFilterAccess          | [#112][112]          | 
| Add accessor methods for SharedSecrets#javaLangInvokeAccess                 | [#131][131]          |
| Add java.lang.Class#getModule to MJI model class                            | [#125][125]          |

### Handling Access Warnings 

[JEP 260][jep-260] encapsulates most of the JDK's internal APIs, so that they are inaccessible by default. So to break the encapsulation, and to access them in non-modular context, `--add-reads`, `--add-exports`, or `--add-opens` command-line options are being passed to relevant ant compile and run targets.

    <compilerarg value="--add-exports"/>
    <compilerarg value="java.base/jdk.internal.misc=ALL-UNNAMED"/>

| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Open jdk.internal.misc to UNNAMED module in tests                           | [#108][108]          |

| Summary                                                                     | Commit(s)            |
| --------------------------------------------------------------------------- |:--------------------:|
| Modify ant target -compile-classes                                          | [880b4ca][880b4ca]   |

### Deprecations in the JDK

JEP 277: Enhanced Deprecation had introduced several new compiler warnings. We were able to fix these warnings that had appeared in the build logs.


| Summary                                                                     | PR(s)                |
| --------------------------------------------------------------------------- |:--------------------:|
| Suppress warnings with @SuppressWarnings annotation                         | [#77][77]            |
| Remove explicit manual boxing in main, peers, and tests                     | [#58][58]            | 
| Suppress warnings in which Number constructor is used intentionally         | [#58][58]            | 
| Add new auxiliary class EnumerationAdapter to ClassLoader                   | [#92][92]            | 
| Replaces all the occurrences of clazz.newInstance()                         | [#66][66]            | 
| Refactor URLClassLoaderTest.testGetPackage to testGetDefinedPackage         | [#80][80]            | 

### Miscellaneous

To access caller information, StackWalking API is used instead of the non-standard sun.misc.SharedSecrets.

| Summary                                                                     | PR(s)                         |
| --------------------------------------------------------------------------- |:-----------------------------:|
| Use StackWalker instread of sun.misc.SharedSecrets                          | [#24][24]                     | 
| Move/Refactor ReflectionTest -> StackWalkerTest                             | [#83][83]                     |
| Remove unused imports                                                       | [#23][23] [#26][26] [#36][36] |
| Add new entries to .gitignore to ignore IDE/OS and auto-generated files     | [#4][4]                       | 
| Setup Travis to automatically build against oracle-jdk 10                   | [#92][92]                     | 
| Remove methods that have been removed from JDK                              | [#40][40]                     | 
| Override hashCode where equals are overridden                               | [#86][86]                     | 
| Other                                                                       | [#63][63]                     | 

### Currently unsupported features

JPF is yet to support indified String concatenation which was introduced in [JEP 280][jep-280]. It will fail to handle invokedynamic calls to methods in StringConcatFactory which are typically used as bootstrap methods for invokedynamic call sites to support the string concatenation.

**Work-in-progress**

Issue: VMClassInfo$Initializer.setBootstrapMethod ArrayIndexOutOfBoundsException  [#53][issue-53] 

| Summary                                                                     | PR(s)                         |
| --------------------------------------------------------------------------- |:-----------------------------:|
| Add a new constructor BootstrapMethodInfo(enclosingClass, cpArgs)           | [#101][101]                   | 

[classes-dir]: https://github.com/javapathfinder/jpf-core/tree/c81801bd98c485b9bbe5cd0e711ac0bf242b100b/src/classes

[28]: https://github.com/javapathfinder/jpf-core/pull/28
[39]: https://github.com/javapathfinder/jpf-core/pull/39
[119]: https://github.com/javapathfinder/jpf-core/pull/119
[129]: https://github.com/javapathfinder/jpf-core/pull/129
[137]: https://github.com/javapathfinder/jpf-core/pull/137
[136]: https://github.com/javapathfinder/jpf-core/pull/136
[144]: https://github.com/javapathfinder/jpf-core/pull/144
[133]: https://github.com/javapathfinder/jpf-core/pull/133
[100]: https://github.com/javapathfinder/jpf-core/pull/100
[121]: https://github.com/javapathfinder/jpf-core/pull/121
[102]: https://github.com/javapathfinder/jpf-core/pull/102

[117]: https://github.com/javapathfinder/jpf-core/pull/117
[122]: https://github.com/javapathfinder/jpf-core/pull/122
[105]: https://github.com/javapathfinder/jpf-core/pull/105
[123]: https://github.com/javapathfinder/jpf-core/pull/123
[104]: https://github.com/javapathfinder/jpf-core/pull/104
[116]: https://github.com/javapathfinder/jpf-core/pull/116

[110]: https://github.com/javapathfinder/jpf-core/pull/110
[112]: https://github.com/javapathfinder/jpf-core/pull/112
[131]: https://github.com/javapathfinder/jpf-core/pull/131
[125]: https://github.com/javapathfinder/jpf-core/pull/125

[108]: https://github.com/javapathfinder/jpf-core/pull/108

[77]: https://github.com/javapathfinder/jpf-core/pull/77
[58]: https://github.com/javapathfinder/jpf-core/pull/58
[92]: https://github.com/javapathfinder/jpf-core/pull/92
[66]: https://github.com/javapathfinder/jpf-core/pull/66
[80]: https://github.com/javapathfinder/jpf-core/pull/80

[24]: https://github.com/javapathfinder/jpf-core/pull/24
[83]: https://github.com/javapathfinder/jpf-core/pull/83
[23]: https://github.com/javapathfinder/jpf-core/pull/23
[26]: https://github.com/javapathfinder/jpf-core/pull/26
[36]: https://github.com/javapathfinder/jpf-core/pull/36
[4]: https://github.com/javapathfinder/jpf-core/pull/4
[92]: https://github.com/javapathfinder/jpf-core/pull/92
[40]: https://github.com/javapathfinder/jpf-core/pull/40
[86]: https://github.com/javapathfinder/jpf-core/pull/86
[63]: https://github.com/javapathfinder/jpf-core/pull/63

[101]: https://github.com/javapathfinder/jpf-core/pull/101
[issue-53]: https://github.com/javapathfinder/jpf-core/issues/53

[1ccefdf]: https://github.com/javapathfinder/jpf-core/commit/1ccefdfd9eeecb9a093ed21b600534ca0f95679a
[880b4ca]: https://github.com/javapathfinder/jpf-core/commit/880b4cad0809b51036e77c49115823ac73432640#diff-2cccd7bf48b7a9cc113ff564acd802a8