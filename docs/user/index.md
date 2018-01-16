# How to Use JPF #

This section is where the real fun starts. Here you learn about

  - [Different applications of JPF](application_types)
  - [JPF's runtime components](components)
  - [Starting JPF](run)
  - [Configuring JPF](config)
  - [Understanding JPF output](output)
  - [Using JPF's Verify API in the system under test](api)

All this assumes you are more interested in running JPF than in developing with/for it, so we will leave most of the JPF internals for the [developer section](../devel/index) of this wiki. 

We do have to bother you with some basic concepts though. Keep in mind that JPF is usually not a black-box tool (such as a compiler). Most probably you have to configure it according to your needs because

  * you have specific verification goals (properties)
  * your application has a huge state space that is challenging for a model checker

On the other hand, JPF is also not a "works-or-fails" tool. Depending on how much time you want to invest, you can adapt it to almost all application types and verification goals. And since JPF is open sourced, chances are somebody has already done that
