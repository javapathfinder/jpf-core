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

import gov.nasa.jpf.vm.Types;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;


/**
 * tool to automatically generate the framework of a native peer MJI class,
 * given it's model class. GenPeer collects all the native methods from the 
 * model class, and creates the corresponding native peer methods
 */
public class GenPeer {
  static final String SYS_PKG = "gov.nasa.jpf.vm";
  static final String MJI_ENV = "gov.nasa.jpf.vm.MJIEnv";
  static final String NATIVEPEER = "gov.nasa.jpf.vm.NativePeer";
  static final String INDENT = "  ";
  static final String SUPERCLASS = "NativePeer";
  static final String MJI_ANN = "@MJI";
  static final String METHOD_PREFIX = "public";
  static final String ENV_ARG = "MJIEnv env";
  static final String OBJ_ARG = "int objRef";
  static final String CLS_ARG = "int clsObjRef";
  static final String REF_TYPE = "int";
  static final String NULL = "MJIEnv.NULL";

  static String       clsName;
  static String[]     mths;

  // our options
  static boolean isSystemPkg;
  static boolean allMethods;
  static boolean mangleNames;
  static boolean clinit;

  public static void main (String[] args) {
    if ((args.length == 0) || !readOptions(args)) {
      showUsage();

      return;
    }

    PrintWriter pw = new PrintWriter(System.out, true);
    Class<?>       cls = getClass(clsName);

    if (cls != null) {
      printNativePeer(cls, pw);
    }
  }

  static Class<?> getClass (String cname) {
    Class<?> clazz = null;

    try {
      clazz = Class.forName(cname);
    } catch (ClassNotFoundException cnfx) {
      System.err.println("target class not found: " + cname);
    } catch (Throwable x) {
      x.printStackTrace();
    }

    return clazz;
  }

  static boolean isMJICandidate (Method m) {
    if (allMethods) {
      return true;
    }

    if (mths != null) {
      String name = m.getName();

      for (int i = 0; i < mths.length; i++) {
        if (name.equals(mths[i])) {
          return true;
        }
      }
    } else {
      if ((m.getModifiers() & Modifier.NATIVE) != 0) {
        return true;
      }
    }

    return false;
  }

  static void getMangledName (Method m) {
    StringBuilder sb = new StringBuilder(50);

    sb.append(m.getName());
    sb.append("__");
  }

  static boolean isPrimitiveType (String t) {
    return ("int".equals(t) || "long".equals(t) || "boolean".equals(t) || 
           "void".equals(t) || // not really, but useful for returnTypes
           "byte".equals(t) || "char".equals(t) || "short".equals(t) || 
           "float".equals(t) || "double".equals(t));
  }

  static void printClinit (PrintWriter pw) {
    pw.print(INDENT);
    pw.print(METHOD_PREFIX);
    pw.print(" void $clinit (");
    pw.print(ENV_ARG);
    pw.print(", ");
    pw.print(CLS_ARG);
    pw.println(") {");
    pw.print(INDENT);
    pw.println("}");
  }

  static void printFooter (Class<?> cls, PrintWriter pw) {
    pw.println("}");
  }

  static void printHeader (Class<?> cls, PrintWriter pw) {
    if (isSystemPkg) {
      pw.print("package ");
      pw.print(SYS_PKG);
      pw.println(';');
      pw.println();
    }

    pw.print("import ");
    pw.print(MJI_ENV);
    pw.println(";");
    pw.print("import ");
    pw.print(NATIVEPEER);
    pw.println(";");
    pw.println();

    String cname = cls.getName().replace('.', '_');

    pw.print("public class ");
    pw.print("JPF_");
    pw.print(cname);
    pw.print(" extends ");
    pw.print(SUPERCLASS);
    pw.println(" {");
  }

  static void printMethodBody (String rt, String t, PrintWriter pw) {
    if (!"void".equals(rt)) {
      pw.print(INDENT);
      pw.print(INDENT);
      pw.print(rt);

      if ((rt == REF_TYPE) && (rt != t)) {
        pw.print(" r");
        pw.print(t);
        pw.print(" = ");
        pw.print(NULL);
        pw.println(";");

        pw.print(INDENT);
        pw.print(INDENT);
        pw.print("return r");
        pw.print(t);
        pw.println(";");
      } else {
        pw.print(" v = (");
        pw.print(rt);
        pw.println(")0;");

        pw.print(INDENT);
        pw.print(INDENT);
        pw.println("return v;");
      }
    }
  }

  static void printMethodName (Method m, PrintWriter pw) {
    String name = null;

    if (mangleNames) {
      name = Types.getJNIMangledMethodName(m);
    } else {
      name = m.getName();
    }

    pw.print(name);
  }

  static void printMJIAnnotation(PrintWriter pw) {
    pw.print(INDENT);
    pw.println(MJI_ANN);
  }

  static void printMethodStub (String condPrefix, Method m, PrintWriter pw) {
    String t = null;
    String rt;

    printMJIAnnotation(pw);

    pw.print(INDENT);
    pw.print(METHOD_PREFIX);
    pw.print(' ');

    if (condPrefix == null) {
      t = rt = stripType(m.getReturnType().getName());

      if (!isPrimitiveType(rt)) {
        rt = REF_TYPE;
      }
    } else {
      rt = "boolean";
    }

    pw.print(rt);

    pw.print(' ');

    if (condPrefix != null) {
      pw.print(condPrefix);
    }

    printMethodName(m, pw);
    pw.print(" (");

    printStdArgs(m, pw);
    printTargetArgs(m, pw);

    pw.println(") {");

    if (condPrefix == null) {
      printMethodBody(rt, stripType(null, t), pw);
    } else {
      pw.print(INDENT);
      pw.print(INDENT);
      pw.println("return true;");
    }

    pw.print(INDENT);
    pw.println('}');
  }

  static void printNativePeer (Class<?> cls, PrintWriter pw) {
    Method[] mths = cls.getDeclaredMethods();

    printHeader(cls, pw);

    if (clinit) {
      printClinit(pw);
    }

    for (int i = 0; i < mths.length; i++) {
      Method m = mths[i];

      if (isMJICandidate(m)) {
        pw.println();
        printMethodStub(null, m, pw);
      }
    }

    printFooter(cls, pw);
  }

  static void printStdArgs (Method m, PrintWriter pw) {
    pw.print(ENV_ARG);
    pw.print(", ");

    if ((m.getModifiers() & Modifier.STATIC) != 0) {
      pw.print(CLS_ARG);
    } else {
      pw.print(OBJ_ARG);
    }
  }

  static void printTargetArgs (Method m, PrintWriter pw) {
    Class<?>[] pt = m.getParameterTypes();

    for (int i = 0; i < pt.length; i++) {
      String  t = stripType(pt[i].getName());
      boolean isPrim = isPrimitiveType(t);

      pw.print(", ");

      if (isPrim) {
        pw.print(t);
        pw.print(" v");
        pw.print(i);
      } else {
        pw.print(REF_TYPE);
        pw.print(" r");
        pw.print(stripType(null, t));
        pw.print(i);
      }
    }
  }

  static String[] readNames (String[] args, int i) {
    ArrayList<String> a = null;

    for (; (i < args.length) && (args[i].charAt(0) != '-'); i++) {
      if (a == null) {
        a = new ArrayList<String>();
      }

      a.add(args[i]);
    }

    if (a != null) {
      String[] names = new String[a.size()];
      a.toArray(names);

      return names;
    }

    return null;
  }

  static boolean readOptions (String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      if ("-s".equals(arg)) {
        isSystemPkg = true;
      } else if ("-m".equals(arg)) {
        mangleNames = true;
      } else if ("-a".equals(arg)) {
        allMethods = true;
      } else if ("-ci".equals(arg)) {
        clinit = true;
      } else if (arg.charAt(0) != '-') {
        // rather simple
        if (clsName == null) {
          clsName = arg;
        } else {
          mths = readNames(args, i);
          i += mths.length;
        }
      } else {
        System.err.println("unknown option: " + arg);
        showUsage();

        return false;
      }
    }

    return (clsName != null);
  }

  static void showUsage () {
    System.out.println(
          "usage:   'GenPeer [<option>..] <className> [<method>..]'");
    System.out.println("options:  -s  : system peer class (gov.nasa.jpf.vm)");
    System.out.println("          -ci : create <clinit> MJI method");
    System.out.println("          -m  : create mangled method names");
    System.out.println("          -a  : create MJI methods for all target class methods");
  }

  static String stripType (String s) {
    return stripType("java.lang", s);
  }

  static String stripType (String prefix, String s) {
    int i = s.lastIndexOf('.') + 1;
    int l = s.length() - 1;

    if (s.charAt(l) == ';') {
      s = s.substring(0, l);
    }

    if (prefix == null) {
      if (i == 0) {
        return s;
      } else {
        return s.substring(i);
      }
    } else {
      if (s.startsWith(prefix) && (prefix.length() + 1 == i)) {
        return s.substring(i);
      } else {
        return s;
      }
    }
  }
}
