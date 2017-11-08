package com.a.a;

import com.a.a.a.i;

/* compiled from: Unknown */
public class n<T> {
    public final T a;
    public final com.a.a.b.a b;
    public final s c;
    public boolean d;

    /* compiled from: Unknown */
    public interface a {
        void a(i iVar);
    }

    /* compiled from: Unknown */
    public interface b {
        void a(s sVar);
    }

    /* compiled from: Unknown */
    public interface c<T> {
        void a(T t);
    }

    /* compiled from: Unknown */
    public interface d {
        void a(long j, long j2);
    }

    private n(s sVar) {
        this.d = false;
        this.a = null;
        this.b = null;
        this.c = sVar;
    }

    private n(T t, com.a.a.b.a aVar) {
        this.d = false;
        this.a = t;
        this.b = aVar;
        this.c = null;
    }

    public static <T> n<T> a(s sVar) {
        return new n(sVar);
    }

    public static <T> n<T> a(T t, com.a.a.b.a aVar) {
        return new n(t, aVar);
    }

    public boolean a() {
        return this.c == null;
    }
}
