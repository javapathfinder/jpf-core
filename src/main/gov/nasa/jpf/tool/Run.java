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

package gov.nasa.jpf.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * common base for Run* classes
 */
public class Run {

  protected static void error (String msg){
    System.err.print("error: ");
    System.err.println(msg);
    System.exit(1);
  }

  protected static void warning (String msg){
    System.err.print("warning: ");
    System.err.println(msg);
  }
  
  // filter out leading '+' arguments (Config initialization)
  protected static String[] removeConfigArgs(String[]args){
    int i;
    for (i=0; i<args.length; i++){
      String a = args[i];
      if (a != null && a.length() > 0 && a.charAt(0) != '+'){
        break;
      }
    }

    String[] newArgs = new String[args.length - i];
    System.arraycopy(args,i,newArgs,0,newArgs.length);

    return newArgs;
  }

  protected static String checkClassName (String cls){
    if (cls == null || cls.isEmpty()){
      return null;
    }

    if (cls.charAt(0) == '.'){
      cls = "gov.nasa.jpf" + cls;
    }

    return cls;
  }

  protected static boolean call( Class<?> cls, String mthName, Object[] args) throws InvocationTargetException {
    try {
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i=0; i<args.length; i++){
        argTypes[i] = args[i].getClass();
      }

      Method m = cls.getMethod(mthName, argTypes);

      int modifiers = m.getModifiers();
      if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)){
        m.invoke(null, args);
        return true;
      }

    } catch (NoSuchMethodException nsmx){
      return false;
    } catch (IllegalAccessException iax){
      return false;
    }

    return false;
  }

}
