package java.lang;

import libcore.util.EmptyArray;

public final class Void {
    public static final Class<Void> TYPE = lookupType();

    private static Class<Void> lookupType() {
        try {
            return Runnable.class.getMethod("run", EmptyArray.CLASS).getReturnType();
        } catch (Object e) {
            throw new AssertionError(e);
        }
    }

    private Void() {
    }
}
