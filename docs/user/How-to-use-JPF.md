This section is where the real fun starts. Here you learn about

  - [Different applications of JPF.](Different-applications-of-JPF)
  - [JPF's runtime components.](Runtime-components-of-JPF)
  - [Running JPF.](Running-JPF)
  - [Configuring JPF.](Configuring-JPF)
  - [Understanding JPF output.](Understanding-JPF-output)
  - [Using JPF's Verify API in the system under test.](Verify-API-of-JPF)

All this assumes you are more interested in running JPF than in developing with/for it, so we will leave most of the JPF internals for the [developer section.](Developer-guide) of this wiki. 

We do have to bother you with some basic concepts though. Keep in mind that JPF is usually not a black-box tool (such as a compiler). Most probably you have to configure it according to your needs because

  * you have specific verification goals (properties)
  * your application has a huge state space that is challenging for a model checker

On the other hand, JPF is also not a "works-or-fails" tool. Depending on how much time you want to invest, you can adapt it to almost all application types and verification goals. And since JPF is open sourced, chances are somebody has already done that

A comprehensive tutorial into JPF is also available as a book. A draft is [openly accessible.](https://www.eecs.yorku.ca/course_archive/2020-21/W/4315/material/book.pdf).
