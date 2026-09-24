## Ideas: [[GSoC 2020 Project Ideas]]

The Java Pathfinder (JPF) team is a mentor organization for the [Google Summer of Code](https://summerofcode.withgoogle.com/.md) (GSoC) program for 2020. The GSoC program, is a program where Google sponsors university students to write code for open source projects at selected mentoring organizations. Each student is guided by a mentor throughout the program. The length of the program is about four months, and it can be done remotely, and it is generally fun. You can learn about the program rules and your eligibility [here](https://summerofcode.withgoogle.com/rules/.md).

## Java Pathfinder ##

The development of [Java Pathfinder (JPF)](https://github.com/javapathfinder/jpf-core/wiki.md) started at [NASA Ames Research Center](http://www.nasa.gov/centers/ames/home/index.html.md) in 1999. It became an open-source project in 2005, and it is now released under the [Apache license, 2.0](http://www.apache.org/licenses/license-2.0.md). JPF's infrastructure is refactored as a Java virtual machine (JVM), and itself is written in Java. JPF is applied directly on the code, and it can be used to verify code that is compiled into Java bytecode.

JPF is a very flexible tool. It offers a highly configurable structure, and introduces numerous extension mechanisms which make it a suitable engine for many existing tools. JPF has been used for a variety of application domains and research topics such as verification of multi-threaded applications, graphical user interfaces, networking, and distributed applications. 

Today, JPF is a mature tool with hundreds of active users. It is used as both a research platform and a production tool. Although it has had major contributions from industry and research labs, the main user community is academic - there are contributors from more than 20 universities worldwide.

You can learn more about JPF at the [JPF wiki](https://github.com/javapathfinder/jpf-core/wiki.md).

## Interested Students - Contact Us ##

You can find existing project ideas on our [[GSoC 2020 Project Ideas]] page. If you are interested in a JPF related project which is not listed here, we would love to hear about it. If you have any questions or suggestions regarding JPF and GSoC, email us at \<jpf.gsoc [at] gmail.com\>. Please be sure to describe your interests and background. The more we know about you, the better we will be able to answer you questions about JPF and its potential projects. Join our IRC channel #jpf on freenode to engage in a discussion about all things JPF.

## [Timeline](https://summerofcode.withgoogle.com/how-it-works/#timeline.md) ##

This list contains only the key deadlines; see above for the full timeline.

* | 03/16 - 03/31 | Student application period |
* | 04/27 | Announcement of accepted students |
* | 04/27 - 05/18 | Community Bonding Period |
* | 05/18 | Coding officially begins |
* | 08/10 - 08/17 | Students submit their final work product |
* | 08/25 | Final results of Google Summer of Code 2020 announced |

## Required Skills ##

JPF is written in Java, and it analyzes Java bytecode, so the minimum skill required is to be familiar with Java and have some development experience with Java (class projects or industry experience). At a minimum you should know there is more to it than just the language - it's the language, the libraries and the virtual machine/bytecodes. Not all projects require a deep understanding of Java or JPF though, please look at the project descriptions to determine which skills are most important.

JPF is a software verification tool. It is a customizable virtual machine that enables the development of various verification algorithms. It will be to your advantage if you are familiar with formal methods, software testing, or model checking. However, JPF is where research meets development, so for many projects it is not a show stopper. We are looking for students who are highly motivated, bright, willing to learn, and love to code.

JPF is a fairly complex system. The first step to start is to get JPF [running](running-jpf.md) and [configured](configuring-jpf.md). This in itself can be a steep learning curve. It also helps if you already know what [listeners](listeners.md), [bytecode factories](bytecode-factories.md) and [native peers](model-java-interface.md) are, but no worries - the mentors will help you there. One thing you have to look at, but what is now surprisingly simple is how to [set up JPF projects](create_project.md).

## Applying for GSoC ##

You will need to submit a proposal to Google during the student application phase. Check out the [GSoC FAQ](https://developers.google.com/open-source/gsoc/faq.md) page for more information. 

### Proposal template

A good proposal should contain the following:

1. Title and a short summary or abstract (about 100-150 words).

2. Self-introduction: your education, experience, past projects, and open source usage/involvement. This should include past contributions to open-source software (JPF or other). If you can submit a patch to JPF to fix a small problem, this is the best proof that you can successfully use and modify JPF!

3. Goals. Have multiple, prioritized goals, so that you have alternatives if something does not go as expected.

4. Detailed description and implementation plan. What you plan to implement (what feature is missing). Include initial steps you have already taken and/or an initial plan of how to reach the first goal(s). Be as concrete as possible.

5. Tentative timeline. A detailed weekly plan for the first 3 - 4 weeks, and outline the remainder as a list of larger steps ("sprints") for the remaining time, perhaps 2 - 3 weeks as an interval for that. Make sure you have enough time for testing and documentation. Leave open some extra time for unexpected problems.

6. A list of foreseen major challenges, a plan to deal with them, including a back-up plan if something does not work out as intended.

7. Plans for future improvements (and possible future involvement) after GSoC.

8. Any possible conflicts of interest. You can compensate for being absent for a week or two by starting earlier (during the community bonding period). However, finishing late is not possible as the evaluation deadlines set by Google are strict.

