## Projects ##
There are a number of currently active projects that use JPF, as well as legacy code.

# Active Projects #
* [JPF-HJ](https://jpf.byu.edu/jpf-hj) - Custom Habanero Java Runtime that allows for verification of Habanero Java programs -- Eric Mercer <egm@byu.edu> and Peter Anderson <anderson.peter@byu.edu>
* [Mango](https://jpf.byu.edu/hg/jpf-mango) - specification and proof artifact generation
* [Net-iocache](https://bitbucket.org/cyrille.artho/net-iocache) - I/O cache extension to handle network communication
* [Trace Server](https://babelfish.arc.nasa.gov/trac/jpf/wiki/projects/jpf-trace-server) - enables storing, querying and analysis of the execution trace
* [jpf-shell](https://jpf.byu.edu/hg/jpf-shell) - a graphical user interface for JPF
* [JDart](https://github.com/psycopaths/jdart/) - a concolic execution engine Java based on JPF. jpf-jdart can be used to generate test cases as well as the symbolic summaries of methods. The tool executes Java programs with concrete and symbolic values at the same time and records symbolic constraints describing all the decisions along a particular path of the execution. These path constraints are then used to find new paths in the program. Concrete data values for exercising these paths are generated using a constraint solver.
* [PSYCO](https://github.com/psycopaths/psyco/) - generates and verifies symbolic behavioral interfaces for software components using a combination of multiple dynamic and static analysis techniques: active automata learning, concolic execution, static code analysis, symbolic search, predicate abstraction, and model-based testing.
* [jConstraints](https://github.com/psycopaths/jconstraints/) - a constraint solver abstraction layer for Java. It provides an object representation for logic expressions, unified access to different SMT and interpolation solvers, and some useful tools and algorithms for working with constraints. While JConstraints has been developed for jpf-jdart, it is maintained as a stand-alone library that can be used independently. Currently, plugins exist for connecting to the SMT solver Z3, the interpolation solver SMTInterpol, the meta-heuristic based constraint solver Coral. Available plugins can be found [here](https://github.com/psycopaths/).
* [Symbolic PathFinder](https://github.com/SymbolicPathFinder) - combines symbolic execution with model checking and constraint solving for test case generation. In this tool, programs are executed on symbolic inputs representing multiple concrete inputs. Values of variables are represented as numeric constraints, generated from analysis of the code structure. These constraints are then solved to generate test inputs guaranteed to reach that part of code. Essentially SPF performs symbolic execution for Java programs at the bytecode level. Symbolic PathFinder uses the analysis engine of the Ames JPF model checking tool (i.e. jpf-core).

# Inactive Projects #
* [Basset](https://babelfish.arc.nasa.gov/hg/jpf/jpf-actor) - Tool and framework for systematic testing of actor programs (e.g. Scala) -- Steven Lauterburg <steven.lauterburg@gmail.com>
* [jpf-concurrent](https://babelfish.arc.nasa.gov/hg/jpf/jpf-concurrent/summary) - optimized java.util.concurrent library implementation for JPF
* [jpf-delayed](https://babelfish.arc.nasa.gov/hg/jpf/jpf-delayed) - postpones non-deterministic choice of values until they are used
* [jpf-guided-test](https://jpf.byu.edu/hg/jpf-guided-test) - Framework for guiding the search using heuristics and static analysis
* [jpf-racefinder](https://babelfish.arc.nasa.gov/hg/jpf/jpf-racefinder) - a precise data race detector in a relaxed Java memory model
* [jpf-rtembed](https://babelfish.arc.nasa.gov/hg/jpf/jpf-rtembed) - programs for real-time and embedded platforms (e.g., RTSJ and SCJ)
* [Extended-Test-Gen](https://babelfish.arc.nasa.gov/hg/jpf/jpf-extended-test-gen) - Using JPF and SPF for generating tests with respect to MC/DC coverage

# Offsite Projects #
* jpf-awt - JPF specific library implementations for java.awt and javax.swing -- Peter Mehlitz <Peter.C.Mehlitz@nasa.gov>
* jpf-awt-shell - specialized JPF shell for model checking java.awt and javax.swing applications -- Peter Mehlitz <Peter.C.Mehlitz@nasa.gov>
* jpf-aprop - Java annotation based properties and their corresponding checkers
* jpf-numeric - an alternative bytecode set for inspection of numeric programs
* jpf-statechart - UML statechart modeling
* jpf-cv - compositional verification using JPF
* jpf-regression - Directed Incremental Symbolic Execution (needs jpf-guided-test, jpf-symbc, jpf-core)
* jpf-abstraction - Abstract PathFinder
* jpf-template - a tool for creating new JPF projects
* Unit Checking for Java IDE](http://aiya.ms.mff.cuni.cz/unitchecking/dist) - JPF extension that allows to run JUnit tests under JPF -- Michal Kebrt <michal.kebrt@gmail.com>
* LTL Listener - Extension which enables the verification of temporal properties for sequential and concurrency Java programs -- Nguyen Anh Cuong <anhcuong@nus.edu.sg>
* [jpf-nhandler](https://bitbucket.org/nastaran/jpf-nhandler) - Extension of JPF that automatically delegates the execution of the system under test methods from JPF to the host JVM -- Nastaran Shafiei <nastaran.shafiei@gmail.com> and Franck van Breugel <franck@cse.yorku.ca>
* [JPF-Inspector](https://github.com/d3sformal/jpf-inspector/) - a GDB-like debugger for programs running under JPF


