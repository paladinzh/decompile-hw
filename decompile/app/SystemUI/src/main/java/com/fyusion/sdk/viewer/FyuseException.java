package com.fyusion.sdk.viewer;

import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class FyuseException extends Exception {
    private static final StackTraceElement[] a = new StackTraceElement[0];
    private final List<Exception> b;

    public FyuseException(String str) {
        this(str, Collections.emptyList());
    }

    public FyuseException(String str, List<Exception> list) {
        super(str);
        setStackTrace(a);
        this.b = list;
    }

    public String toString() {
        return (this.b == null || this.b.isEmpty()) ? super.toString() : ((Exception) this.b.get(0)).toString();
    }
}
