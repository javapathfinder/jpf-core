## Ideas: [[GSoC 2019 Project Ideas]]

The Java Pathfinder (JPF) team is a mentor organization for the [Google Summer of Code](https://developers.google.com/open-source/gsoc/) (GSoC) program for 2019. The GSoC program, is a program where Google sponsors university students to write code for open source projects at selected mentoring organizations. Each student is guided by a mentor throughout the program. The length of the program is about three month, and it can be done remotely, and are generally fun. You can learn about the program rules and your eligibility [here](https://summerofcode.withgoogle.com/rules/).

## Java Pathfinder ##

The development of [Java Pathfinder (JPF)](https://github.com/javapathfinder/jpf-core/wiki)  started at [NASA Ames Research Center](http://www.nasa.gov/centers/ames/home/index.html) in 1999. It became an open-source project in 2005, and it is now released under the [Apache license, 2.0](http://www.apache.org/licenses/LICENSE-2.0). JPF's infrastructure is refactored as a Java virtual machine (JVM), and itself is written in Java. JPF is applied directly on the code, and it can be used to verify code that is compiled into bytecode.

JPF is a very flexible tool. It offers a highly configurable structure, and introduces numerous extension mechanisms which make it a suitable engine for many existing tools. JPF has been used for a variety of application domains and research topics such as verification of multi-threaded applications, graphical user interfaces, networking, and distributed applications. 

Today, JPF is a mature tool with hundreds of active users. It is used as both a research platform and a production tool. Although it has had major contributions from industry and research labs, the main user community is academic - there are contributors from more than 20 universities worldwide.

You can learn more about JPF at the [JPF wiki](https://github.com/javapathfinder/jpf-core/wiki).

## Interested Students - Contact Us ##

You can find existing project ideas on our [[GSoC 2019 Project Ideas]] page. If you are interested in a JPF related project which is not listed here, we would love to hear about it. If you have any questions or suggestions regarding JPF and GSoC, email us at \<jpf.gsoc [at] gmail.com\>. Please be sure to describe your interests and background. The more we know about you, the better we will be able to answer you questions about JPF and its potential projects. Join our IRC channel #jpf on freenode to engage in a discussion about all things JPF.

## [Timeline](https://summerofcode.withgoogle.com/how-it-works/#timeline) ##

This list contains only the key deadlines; see above for the full timeline.

* | 03/25 - 04/09 | Student application period |
* | 05/06 | Announcement of accepted students |
* | 05/06 - 05/27 | Community Bonding Period |
* | 05/27 | Coding officially begins |
* | 08/19 - 08/26 | Students submit their final work product |
* | 09/03 | Final results of Google Summer of Code 2019 announced |

## Required Skills ##

JPF is written in Java, and it analyzes Java bytecode, so the minimum skill required is to be familiar with Java and have some development experience with Java (class projects or industry experience). At a minimum you should know there is more to it than just the language - it's the language, the libraries and the virtual machine/bytecodes. Not all projects require a deep understanding of Java or JPF though, please look at the project descriptions to determine which skills are most important.

JPF is a software verification tool. It is a customizable virtual machine that enables the development of various verification algorithms. It will be to your advantage if you are familiar with formal methods, software testing, or model checking. However, JPF is where research meets development, so for many projects it is not a show stopper. We are looking for students who are highly motivated, bright, willing to learn, and love to code.

JPF is a fairly complex system. The first step to start is to get JPF [running](Running-JPF) and [configured](Configuring-JPF). This in itself can be a steep learning curve. It also helps if you already know what [listeners](Listeners), [bytecode factories](Bytecode-Factories) and [native peers](Model-Java-Interface) are, but no worries - the mentors will help you there. One thing you have to look at, but what is now surprisingly simple is how to [set up JPF projects](create_project).

## Applying for GSoC ##

You will need to submit a proposal to Google during the student application phase. Check out the [GSoC FAQ](https://developers.google.com/open-source/gsoc/faq) page for more information. 

### Proposal outline

A good proposal should contain the following:

1. Title, summary (about 100 words).

2. Goals. Have multiple, prioritized goals, so you have alternatives if something does not go as expected.

3. Implementation plan. An initial plan of how to reach the first goal(s).

4. Tentative timeline. Be detailed for the first three weeks, and outline the remainder. Leave open some extra time for unexpected problems.

5. Self-introduction. This should include past contributions to open-source software (JPF or other). If you can submit a patch to JPF to fixes a small problem, this is the best proof that you can successfully use and modify JPF!
