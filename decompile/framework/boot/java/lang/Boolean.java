package java.lang;

import java.io.Serializable;

public final class Boolean implements Serializable, Comparable<Boolean> {
    public static final Boolean FALSE = new Boolean(false);
    public static final Boolean TRUE = new Boolean(true);
    public static final Class<Boolean> TYPE = boolean[].class.getComponentType();
    private static final long serialVersionUID = -3665804199014368530L;
    private final boolean value;

    public Boolean(boolean value) {
        this.value = value;
    }

    public Boolean(String s) {
        this(toBoolean(s));
    }

    public static boolean parseBoolean(String s) {
        return toBoolean(s);
    }

    public boolean booleanValue() {
        return this.value;
    }

    public static Boolean valueOf(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static Boolean valueOf(String s) {
        return toBoolean(s) ? TRUE : FALSE;
    }

    public static String toString(boolean b) {
        return b ? "true" : "false";
    }

    public String toString() {
        return this.value ? "true" : "false";
    }

    public int hashCode() {
        return this.value ? 1231 : 1237;
    }

    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Boolean)) {
            return false;
        }
        if (this.value == ((Boolean) obj).booleanValue()) {
            z = true;
        }
        return z;
    }

    public static boolean getBoolean(String name) {
        boolean result = false;
        try {
            result = toBoolean(System.getProperty(name));
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        return result;
    }

    public int compareTo(Boolean b) {
        return compare(this.value, b.value);
    }

    public static int compare(boolean x, boolean y) {
        if (x == y) {
            return 0;
        }
        return x ? 1 : -1;
    }

    private static boolean toBoolean(String name) {
        return name != null ? name.equalsIgnoreCase("true") : false;
    }

    public static boolean logicalAnd(boolean a, boolean b) {
        return a ? b : false;
    }

    public static boolean logicalOr(boolean a, boolean b) {
        return !a ? b : true;
    }

    public static boolean logicalXor(boolean a, boolean b) {
        return a ^ b;
    }
}
