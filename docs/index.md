## Welcome to the Java™Pathfinder System ##

This is the main page for Java™ Pathfinder (JPF). JPF is an extensible software model checking framework for Java™ bytecode programs. The system was developed at the [NASA Ames Research Center](http://arc.nasa.gov), open sourced in 2005, and is freely available on this server under the [Apache-2.0 license](http://www.apache.org/licenses/LICENSE-2.0).


This page is our primary source of documentation, and is divided into the following sections.

   ---

  * [Introduction](intro/index) -- a brief introduction into model checking and JPF
    * [What is JPF?](intro/what_is_jpf)
    * [Testing vs. Model Checking](intro/testing_vs_model_checking)
         - [Random value example](intro/random_example)
         - [Data race example](intro/race_example)
    * [JPF key features](intro/classification)
    
    ---

  * [How to obtain and install JPF](install/index) -- everything to get it running on your machine
    - [System requirements](install/requirements)
    - Downloading [binary snapshots](install/snapshot) and [sources](install/repositories)
    - [Creating a site properties file](install/site-properties)
    - [Building, testing, and running](install/build)
    - Installing the JPF plugins
         - [Eclipse](install/eclipse-plugin) 
         - [NetBeans](install/netbeans-plugin)
    
    ---
         
  * [How to use JPF](user/index) -- the user manual for JPF    
    - [Different applications of JPF](user/application_types)
    - [JPF's runtime components](user/components)
    - [Starting JPF](user/run)
    - [Configuring JPF](user/config)
    - [Understanding JPF output](user/output)
    - [Using JPF's Verify API in the system under test](user/api)
    
    ---
        
  * [Developer guide](devel/index) -- what's under the hood
    * [Top-level design](devel/design)
    * Key mechanisms, such as 
        - [ChoiceGenerators](devel/choicegenerator)
        - [Partial order reduction](devel/partial_order_reduction)
        - [Slot and field attributes](devel/attributes)
    * Extension mechanisms, such as
        - [Listeners](devel/listener)
        - [Search Strategies](devel/design)
        - [Model Java Interface (MJI)](devel/mji)
        - [Bytecode Factories](devel/bytecode_factory)
    * Common utility infrastructures, such as
        - [Logging system](devel/logging)
        - [Reporting system](devel/report)
    * [Running JPF from within your application](devel/embedded)
    * [Writing JPF tests](devel/jpf_tests)
    * [Coding conventions](devel/coding_conventions)
    * [Hosting an Eclipse plugin update site](devel/eclipse_plugin_update) 
        
    ---
        
  * [JPF core project](jpf-core/index) -- description and link to jpf-core
    
    ---
      
  * [Related research and publications](papers/index)    

