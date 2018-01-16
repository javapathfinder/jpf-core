# Coding Conventions #
JPF is an open system. In order to keep the source format reasonably consistent, we strive to keep the following minimal set of conventions

  * Two space indentation (no tabs)
  * Opening brackets in same line (class declaration, method declaration, control statements)
  * No spaces after opening '(', or before closing ')'
  * Method declaration parameters indent on column
  * All files start with copyright and license information
  * All public class and method declarations have preceding Javadoc comments
  * We use *camelCase* instead of *underscore_names* for identifiers
  * Type names are upper case 

The following code snippet illustrates these rules.

~~~~~~~~ {.java}
/* <copyright notice goes here>
 * <license referral goes here>
 */

/**
 * this is my class declaration example
 */
    
public class MyClass {
   
  /**
   * this is my public method example
   */
  public void foo (int arg1, int arg2,
                   int arg3) {
    if (bar) {
      ..
    } else {
      ..
    }
  }
   ..
}
~~~~~~~~

We consider modularity to be of greater importance than source format. With its new configuration scheme, there is no need to introduce dependencies of core classes towards optional extensions anymore. If you add something that is optional, and does not seamlessly fit into an existing directory, keep it separate by adding new directories. The core JPF classes should not contain any additional dependencies to external code.
