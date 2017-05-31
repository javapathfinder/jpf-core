# Running JPF from within NetBeans with netbeans-jpf plugin #
netbeans-jpf can be easily configured to run JPF at the click of a mouse.

 1. Install netbeans-jpf (see: [Installing the NetBeans JPF plugin](../install/netbeans-plugin))
 2. Make sure the correct `site.properties` file is being used for JPF (The default is usually correct)\
     To see which `site.properties` file is being used:
   3. From the NetBeans top menu go to "Tools"->"Options" (Alt+T followed by Alt+O)
   4. Select "Miscellaneous" from the top of the Options Window
   5. Select the "Java Pathfinder" tab
   6. Make sure that "Path to `site.properties`" is defined properly, uncheck "Use default `site.properties` location" to change the path
 7. From either the "Projects" or "Files" view (on the left of the main NetBeans screen by default,) select the JPF properties file (i.e., a file with a `.jpf` extension) you would like to run. Right click on this file and select the "Verify..." menu item to run JPF.
 8. To view the results, make sure that the "Output" View is open (On the bottom of the main NetBeans screen by default)