## Running JPF within eclipse ##

To run JPF in eclipse after building 

* JPF click "Run"-->"Run" or click "Play"-->"Run As Dialog".

* Select Java Application and click "New".

* Click on the "Main" tab. Ensure the project selected is jpf-core and the in the Main Class pick `gov.nasa.jpf.JPF`.

* Click on the "Arguments" tab. Specify the `<application-main-class>` in the Program Arguments. Any additional configuration properties can be specified. For example, 

~~~~~~~~ {.bash}
+cg.randomize_choices=true
oldclassic
~~~~~~~~

* Click on "Run" to verify the application in JPF. The output of the program and the results will be displayed on the eclipse console window. 

Alternatively you pick the run-jpf-core launch configuration and add the `<application-main-class>` of the system under test. The eclipse IDE detects the various launch configurations. When you click on the Run button they appear on the left column, pick "run-jpf-core". It is, however, recommended you run the application with the eclipse plugin for better performance and ease of usability.  
