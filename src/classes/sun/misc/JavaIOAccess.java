/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package sun.misc;

//import java.io.Console;
import java.nio.charset.Charset;

/**
 * this is a placeholder for a Java 6 class, which we only have here to
 * support both Java 1.5 and 6 with the same set of env/ classes
 *
 * see sun.msic.SharedSecrets for details
 *
 * <2do> THIS IS GOING AWAY AS SOON AS WE OFFICIALLY SWITCH TO JAVA 6
 */
public interface JavaIOAccess {
    //public Console console(); // not in Java 1.5, so we skip for now
    public Runnable consoleRestoreHook();
    public Charset charset();
}
