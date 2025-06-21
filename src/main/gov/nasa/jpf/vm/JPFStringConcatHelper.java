package gov.nasa.jpf.vm;

import java.util.Arrays;

/**
 * Author: Mahmoud Khawaja <mahmoud.khawaja97@gmail.com>
 * Helper for Java string concatenation recipes as used by Java 9+ invokedynamic string concat.
 * Handles both primitive and reference types, including JPF's ElementInfo heap objects.
 */
public class JPFStringConcatHelper {

    private static final char ARG_PLACEHOLDER = '\u0001';
    private static final char CONST_PLACEHOLDER = '\u0002';

    public static String escapeUnicode(String s) {
        if (s == null) return "null";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 32 || c > 126) {
                sb.append(String.format("\\u%04x", (int)c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String concatenate(String recipe, Class<?>[] argTypes, Object[] constants, Object... args) {
        // Normalize inputs
        if (args == null) args = new Object[0];
        if (constants == null) constants = new Object[0];

        argTypes = validateArgTypes(argTypes, args);

        System.out.println("[DEBUG] concatenate() called with:");
        System.out.println("  recipe: " + recipe);
        System.out.println("  argTypes: " + Arrays.toString(argTypes));
        logArguments(args);

        if (recipe == null || recipe.isEmpty()) {
            return handleEmptyRecipe(args, argTypes);
        }

        System.out.println("CONCAT: recipe=" + escapeUnicode(recipe) + ", args=" + Arrays.toString(args));

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;

        for (int i = 0; i < recipe.length(); i++) {
            char c = recipe.charAt(i);

            if (c == ARG_PLACEHOLDER) {
                if (argIndex < args.length) {
                    appendValue(sb, args[argIndex], argTypes[argIndex]);
                    argIndex++;
                }
            } else if (c == CONST_PLACEHOLDER && i + 1 < recipe.length()) {
                i++; // Move to next character
                int constIndex = recipe.charAt(i) - 1;
                if (constIndex >= 0 && constIndex < constants.length) {
                    Object constant = constants[constIndex];
                    sb.append(constant != null ? constant : "null");
                } else {
                    sb.append("[invalid_constant_index:").append(constIndex).append("]");
                    System.out.println("WARNING: Invalid constant index: " + constIndex);
                }
            } else {
                sb.append(c);
            }
        }

        String result = sb.toString();
        System.out.println("Concat result: " + result);
        return result;
    }

    private static Class<?>[] validateArgTypes(Class<?>[] argTypes, Object[] args) {
        if (argTypes == null) {
            argTypes = new Class<?>[args.length];
            Arrays.fill(argTypes, Object.class);
            return argTypes;
        }

        if (argTypes.length != args.length) {
            int originalLength = argTypes.length;
            argTypes = Arrays.copyOf(argTypes, args.length);
            for (int i = originalLength; i < args.length; i++) {
                argTypes[i] = Object.class;
            }
        }
        return argTypes;
    }

    private static void logArguments(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof ElementInfo) {
                ElementInfo ei = (ElementInfo) arg;
                if (ei.isStringObject()) {
                    System.out.printf("  args[%d]: \"%s\" (String)%n", i, ei.asString());
                } else {
                    System.out.printf("  args[%d]: %s (non-String ElementInfo)%n", i, ei);
                }
            } else {
                System.out.printf("  args[%d]: %s (%s)%n",
                        i, arg, (arg != null) ? arg.getClass().getSimpleName() : "null");
            }
        }
    }

    private static String handleEmptyRecipe(Object[] args, Class<?>[] argTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> type = (i < argTypes.length) ? argTypes[i] : Object.class;
            appendValue(sb, arg, type);
        }
        return sb.toString();
    }

    private static void appendValue(StringBuilder sb, Object value, Class<?> type) {
        if (value instanceof ElementInfo) {
            handleElementInfo(sb, (ElementInfo) value);
        } else {
            handlePrimitive(sb, value, type);
        }
    }

    private static void handleElementInfo(StringBuilder sb, ElementInfo ei) {
        if (ei == null) {
            sb.append("null");
            return;
        }

        if (ei.isStringObject()) {
            String s = ei.asString();
            sb.append(s != null ? s : "");
            return;
        }

        String className = ei.getClassInfo().getName();
        switch (className) {
            case "java.lang.Byte":    sb.append(ei.getByteField("value")); break;
            case "java.lang.Character": sb.append(ei.getCharField("value")); break;
            case "java.lang.Short":   sb.append(ei.getShortField("value")); break;
            case "java.lang.Integer": sb.append(ei.getIntField("value")); break;
            case "java.lang.Boolean": sb.append(ei.getBooleanField("value")); break;
            case "java.lang.Float":
                sb.append(Float.intBitsToFloat(ei.getIntField("value"))); break;
            case "java.lang.Double":
                sb.append(Double.longBitsToDouble(ei.getLongField("value"))); break;
            case "java.lang.Long":    sb.append(ei.getLongField("value")); break;
            default: sb.append(ei.toString()); break;
        }
    }

    private static void handlePrimitive(StringBuilder sb, Object value, Class<?> type) {
        if (value == null) {
            sb.append("null");
            return;
        }

        if (type == float.class && value instanceof Float) {
            float f = (Float) value;
            if (Float.isNaN(f)) sb.append("NaN");
            else if (f == Float.POSITIVE_INFINITY) sb.append("Infinity");
            else if (f == Float.NEGATIVE_INFINITY) sb.append("-Infinity");
            else sb.append(f);
        } else if (type == double.class && value instanceof Double) {
            double d = (Double) value;
            if (Double.isNaN(d)) sb.append("NaN");
            else if (d == Double.POSITIVE_INFINITY) sb.append("Infinity");
            else if (d == Double.NEGATIVE_INFINITY) sb.append("-Infinity");
            else sb.append(d);
        } else {
            sb.append(String.valueOf(value));
        }
    }
}
