package gov.nasa.jpf.vm;

import java.util.Arrays;

/**
 * Author: Mahmoud Khawaja
 * Helper for Java string concatenation recipes as used by Java 9+ invokedynamic string concat.
 * Handles both primitive and reference types, including JPF's ElementInfo heap objects.
 */

public class JPFStringConcatHelper {

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

    public static String concatenate(String recipe, Class<?>[] argTypes, Object[] constants, Object... args)  {
        if (argTypes == null) {
            argTypes = new Class<?>[args.length];
            Arrays.fill(argTypes, Object.class);
        }

        // Add length validation
        if (argTypes.length != args.length) {
            argTypes = Arrays.copyOf(argTypes, args.length);
            for (int i=argTypes.length; i<args.length; i++) {
                argTypes[i] = Object.class;
            }
        }


        if (recipe.isEmpty()) {
            return handleEmptyRecipe(args,argTypes);
        }

        System.out.println("CONCAT: recipe=" + escapeUnicode(recipe) +
                ", args=" + Arrays.toString(args));

        if (args == null) args = new Object[0];

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;

        if (constants == null) {
            constants = new Object[0];
        }


        for (int i = 0; i < recipe.length(); i++) {
            char c = recipe.charAt(i);

            if (c == '\u0001') {
                if (argIndex < args.length) {
                    formatValue(sb, args[argIndex], argTypes[argIndex]);
                    argIndex++;
                }
            } else if (c == '\u0002' && i+1 < recipe.length()) {
                if (i+1 < recipe.length()) {
                    int constIndex = recipe.charAt(++i) - 1; // Decode index from next char
                    if (constIndex >= 0 && constIndex < constants.length) {
                        Object constant = constants[constIndex];
                        sb.append(constant != null ? constant : "null");
                    } else {
                        sb.append("[invalid_constant_index:").append(constIndex).append("]");
                        System.out.println("WARNING: Invalid constant index: " + constIndex +
                                " (constants.length=" + constants.length + ")");                    }
                }
            } else {
                sb.append(c);
            }
        }



        String result = sb.toString();
        System.out.println("Concat result: " + result);

        System.out.println("[DEBUG] concatenate() called with:");
        System.out.println("  recipe: " + recipe);
        System.out.println("  argTypes: " + Arrays.toString(argTypes));
        for (int i=0; i<args.length; i++) {
            Object arg = args[i];
            if (arg instanceof ElementInfo) {
                ElementInfo ei = (ElementInfo) arg;
                // SAFELY handle non-String ElementInfo
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
        return result;
    }

    private static void formatValue(StringBuilder sb, Object value, Class<?> type) {
        if (value instanceof ElementInfo) {
            handleElementInfo(sb, (ElementInfo) value, type);
        } else {
            handlePrimitiveValue(sb, value, type);
        }
    }
    private static String handleEmptyRecipe(Object[] args, Class<?>[] argTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Class<?> type = (argTypes != null && i < argTypes.length) ? argTypes[i] : Object.class;
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof ElementInfo) {
                ElementInfo ei = (ElementInfo) arg;
                sb.append(ei.isStringObject() ? ei.asString() : "null");
            } else {
                handlePrimitiveValue(sb, arg, type);
            }
        }
        return sb.toString();
    }

    private static String formatSingleArg(Object arg, Class<?> type) {
        if (arg == null) return "null";
        StringBuilder sb = new StringBuilder();
        handlePrimitiveValue(sb, arg, type);
        return sb.toString();
    }

    private static void handleElementInfo(StringBuilder sb, ElementInfo ei, Class<?> type) {
        if (ei == null) {
            sb.append("null");
            return;
        }

        if (ei.isStringObject()) {
            String s = ei.asString();
            sb.append(s != null ? s : "");
            return;
        }

        ClassInfo ci = ei.getClassInfo();
        String className = ci.getName();

        switch (className) {
            case "java.lang.Byte":
                sb.append(ei.getByteField("value"));
                break;
            case "java.lang.Character":
                if (type == char.class) {
                    sb.append((char) ei.getIntField("value"));
                } else {
                    sb.append(ei.getCharField("value"));
                }
                break;
            case "java.lang.Short":
                sb.append(ei.getShortField("value"));
                break;
            case "java.lang.Integer":
                sb.append(ei.getIntField("value"));
                break;
            case "java.lang.Boolean":
                sb.append(ei.getBooleanField("value"));
                break;
            case "java.lang.Float":
                int floatBits = ei.getIntField("value");
                sb.append(Float.intBitsToFloat(floatBits));
                break;
            case "java.lang.Double":
                long doubleBits = ei.getLongField("value");
                sb.append(Double.longBitsToDouble(doubleBits));
                break;
            case "java.lang.Long":
                sb.append(ei.getLongField("value"));
                break;
            default:
                break;
        }
    }

    private static void handlePrimitiveValue(StringBuilder sb, Object value, Class<?> type) {
        if (value == null) {
            sb.append("null");
            return;
        }

        if (type == float.class && value instanceof Float) {
            float f = (Float) value;
            if (Float.isNaN(f)) {
                sb.append("NaN");
            } else if (f == Float.POSITIVE_INFINITY) {
                sb.append("Infinity");
            } else if (f == Float.NEGATIVE_INFINITY) {
                sb.append("-Infinity");
            } else {
                sb.append(f);
            }
        } else if (type == double.class && value instanceof Double) {
            double d = (Double) value;
            if (Double.isNaN(d)) {
                sb.append("NaN");
            } else if (d == Double.POSITIVE_INFINITY) {
                sb.append("Infinity");
            } else if (d == Double.NEGATIVE_INFINITY) {
                sb.append("-Infinity");
            } else {
                sb.append(d);
            }
        } else {
            sb.append(String.valueOf(value));
        }
    }

}


