# Introduction #
JPF started as a software model checker, but nowadays, JPF is a runtime system with many different execution modes and extensions which go beyond model checking. All the various modes of JPF, while serving different purposes, used to verify Java programs by

 * detecting and explaining defects
 * collecting "deep" runtime information like coverage metrics
 * deducing interesting test vectors and creating corresponding test drivers
 * and many more...   

Although JPF is mostly associated with software model checking, it can be applied in variety of ways.  People often confuse this with testing, and indeed JPF's notion of model checking can be close to systematic testing. Below we use a simple example that illustrates the differences.

Here is the outline of this section:

  * [What is JPF?](what_is_jpf)
  * [Testing vs. Model Checking](testing_vs_model_checking)
    * [Random value example](random_example)
    * [Data race example](race_example)
  * [JPF key features](classification)
