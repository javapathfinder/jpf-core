package java11;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class DynamicBootstrapSupport {
  private DynamicBootstrapSupport() {
  }

  public static CallSite bootstrapInt(MethodHandles.Lookup lookup, String name, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return new ConstantCallSite(
        lookup.findStatic(DynamicBootstrapSupport.class, "returnFortyTwo", type));
  }

  public static CallSite bootstrapString(MethodHandles.Lookup lookup, String name, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return new ConstantCallSite(
        lookup.findStatic(DynamicBootstrapSupport.class, "prefix", type));
  }

  public static CallSite bootstrapStringWithConstant(
      MethodHandles.Lookup lookup, String name, MethodType type, String prefix)
      throws NoSuchMethodException, IllegalAccessException {
    return new ConstantCallSite(
        MethodHandles.insertArguments(
            lookup.findStatic(DynamicBootstrapSupport.class, "prefixWithConstant",
                MethodType.methodType(String.class, String.class, String.class)),
            0,
            prefix).asType(type));
  }

  public static CallSite bootstrapDouble(MethodHandles.Lookup lookup, String name, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return new ConstantCallSite(
        lookup.findStatic(DynamicBootstrapSupport.class, "describeDouble", type));
  }

  public static int returnFortyTwo() {
    return 42;
  }

  public static String prefix(String value) {
    return "prefix:" + value;
  }

  public static String prefixWithConstant(String prefix, String value) {
    return prefix + value;
  }

  public static String describeDouble(double value) {
    return "double:" + value;
  }
}
