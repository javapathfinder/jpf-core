## Ideas: [[GSoC 2024 Project Ideas]]

The Java Pathfinder (JPF) team will apply again as a mentor organization for the [Google Summer of Code](https://summerofcode.withgoogle.com/) (GSoC) program this year 2024.
The GSoC program is a program where Google sponsors student developers to write code for open-source projects at selected mentoring organizations. Each student is guided by a mentor throughout the program. The complete length of the program is about three months, which is done remotely, and it is generally fun. You can learn about the program rules and your eligibility [here](https://summerofcode.withgoogle.com/rules/).

## Java Pathfinder ##

The development of [Java Pathfinder (JPF)](https://github.com/javapathfinder/jpf-core/wiki) started at [NASA Ames Research Center](http://www.nasa.gov/centers/ames/home/index.html) in 1999. It became an open-source project in 2005, and it is now released under the [Apache license, 2.0](http://www.apache.org/licenses/LICENSE-2.0). JPF's infrastructure is refactored as a Java virtual machine (JVM), and it is written in Java. JPF is applied directly on the code, and it can be used to verify code that is compiled into Java bytecode.

JPF is a very flexible tool. It offers a highly configurable structure, and introduces numerous extension mechanisms which make it a suitable engine for many existing tools. JPF has been used for a variety of application domains and research topics, such as verification of multi-threaded applications, graphical user interfaces, networking, and distributed applications. 

Today, JPF is a mature tool with hundreds of active users. It is used as both a research platform and a production tool. Although it has had major contributions from industry and research labs, the main user community is academic - there are contributors from more than 20 universities worldwide.

You can learn more about JPF at the [JPF wiki](https://github.com/javapathfinder/jpf-core/wiki).

## Interested Students - Contact Us ##

<!-- *Note that JPF has not yet been accepted for GSoC 2024 and that our [project ideas](https://github.com/javapathfinder/jpf-core/wiki/GSoC-2024-Project-Ideas) are still being extended and refined.* -->

You can find existing project ideas on our [[GSoC 2024 Project Ideas]] page. If you are interested in a JPF-related project which is not listed here, we would love to hear about it. If you have any questions or suggestions regarding JPF and GSoC, use our Google Group: https://groups.google.com/g/java-pathfinder, or join our Discord server: https://discord.gg/sX4YZUVHK7.
Please be sure to describe your interests and background. The more we know about you, the better we will be able to answer your questions about JPF and its potential projects.

## Timeline ##

<!--The exact timeline has not been published yet, but you can find a timeline overview and more details on the [GSoC website](https://summerofcode.withgoogle.com/how-it-works/#timeline).-->
The official timeline of GSoC 2024 can be found here: [GSoC 2024 Timeline](https://developers.google.com/open-source/gsoc/timeline)

The application deadline for GSoC contributors is: **April 2 - 18:00 UTC**

<!--
This list contains only the key deadlines; see [here](https://summerofcode.withgoogle.com/how-it-works/#timeline) for the full timeline.

* | 03/30 - 04/14 | Student application period |
* | 05/18 | Announcement of accepted students |
* | 05/18 - 06/08 | Community Bonding Period |
* | 06/08 | Coding officially begins |
* | 08/24 | Students submit their final work product |
* | 09/01 | Final results of Google Summer of Code 2021 announced |
-->

## Required Skills ##

JPF is written in Java, and it analyzes Java bytecode, so the minimum skill required is to be familiar with Java and have some development experience with Java (class projects or industry experience). At a minimum, you should know there is more to it than just the language - it's the language, the libraries, and the virtual machine/bytecodes. Not all projects require a deep understanding of Java or JPF, though; please look at the project descriptions to determine which skills are most important.

JPF is a software verification tool. It is a customizable virtual machine that enables the development of various verification algorithms. It will be to your advantage if you are familiar with formal methods, software testing, or model checking. However, JPF is where research meets development, so for many projects, it is not a show-stopper. We are looking for students who are highly motivated, bright, willing to learn and love to code.

JPF is a fairly complex system. The first step to start is to get JPF [running](Running-JPF) and [configured](Configuring-JPF). This in itself can be a steep learning curve. It also helps if you already know what [listeners](Listeners), [bytecode factories](Bytecode-Factories), and [native peers](Model-Java-Interface) are, but no worries - the mentors will help you there. One thing you have to look at, but what is now surprisingly simple, is how to [set up JPF projects](create_project).

## Applying for GSoC ##

You will need to submit a proposal to Google during the student application phase. Check out the [GSoC FAQ](https://developers.google.com/open-source/gsoc/faq) page for more information. 

### Proposal template

A good proposal should contain the following:

1. Title and a short summary or abstract (about 100-150 words).

2. Self-introduction: your education, experience, past projects, and open source usage/involvement. This should include past contributions to open-source software (JPF or other). If you can submit a patch to JPF to fix a small problem, this is the best proof that you can successfully use and modify JPF!

3. Goals. Have multiple, prioritized goals, so that you have alternatives if something does not go as expected.

4. Detailed description and concrete implementation plan. What you plan to implement (what feature is missing). Include initial steps you have already taken and/or your plan of how to reach the first goal(s). Be as concrete as possible.

5. Tentative timeline. A detailed weekly plan for the first 3 - 4 weeks, and outline the remainder as a list of larger steps ("sprints") for the remaining time, perhaps 1 - 2 weeks as an interval for that. Make sure you have enough time for testing and documentation. Leave open some extra time for unexpected problems.

6. A list of foreseen major challenges and a plan to deal with them, including a backup plan if something does not work out as intended.

7. Plans for future improvements (and possible future involvement) after GSoC.

8. Any possible conflicts of interest.

9. Scheduling issues. GSoC 2024 allows some flexibility in planning the milestones for each project. If you already know when you will not be available (e.g., due to final exams or personal commitments), you can already highlight this in the proposal. That allows the mentors to plan with you ahead of time to adjust the schedule early during the community bonding period.
