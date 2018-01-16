# Running JPF from within Eclipse using eclipse-jpf #

eclipse-jpf can be easily configured to run JPF from within Eclipse

 1. Install eclipse-jpf (see [Installing Eclipse JPF plugin](../install/eclipse-plugin))
 2. Make sure that the correct site.properties file is being used for JPF (The default is usually correct)
   To see which site.properties file is being used:
   3. From the Eclipse top menu go to **Window**->**Preferences**
   4. Select **JPF Preferences**
   5. Make sure that "Path to site.properties" is defined properly.
 6. From either the **Package Explorer** view (On the left of the main Eclipse screen by default) right-click on the *.jpf with the JPF configuration wanted and select **Verify...**
 7. Make sure that the **Console view** (Usually at the bottom of the main Eclipse screen) is opened to view the results from JPF