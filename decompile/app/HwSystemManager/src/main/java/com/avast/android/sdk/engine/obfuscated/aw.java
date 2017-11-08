package com.avast.android.sdk.engine.obfuscated;

import java.io.File;
import java.util.Comparator;

/* compiled from: Unknown */
public class aw {
    private static final Comparator<aw> a = new ax();
    private final long b;
    private final File c;
    private final long d = System.currentTimeMillis();

    public aw(File file, long j) {
        this.c = file;
        this.b = j;
    }

    public static Comparator<aw> d() {
        return a;
    }

    public long a() {
        return this.b;
    }

    public File b() {
        return this.c;
    }

    public long c() {
        return this.d;
    }

    public boolean equals(Object obj) {
        return (obj instanceof aw) && ((aw) obj).b().equals(this.c) && ((aw) obj).a() == this.b && ((aw) obj).c() == this.d;
    }
}
