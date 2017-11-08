package com.google.android.gms.internal;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public abstract class eg implements SafeParcelable {
    private static final Object Br = new Object();
    private static ClassLoader Bs = null;
    private static Integer Bt = null;
    private boolean Bu = false;

    private static boolean a(Class<?> cls) {
        try {
            return "SAFE_PARCELABLE_NULL_STRING".equals(cls.getField("NULL").get(null));
        } catch (NoSuchFieldException e) {
            return false;
        } catch (IllegalAccessException e2) {
            return false;
        }
    }

    protected static boolean ae(String str) {
        ClassLoader dX = dX();
        if (dX == null) {
            return true;
        }
        try {
            return a(dX.loadClass(str));
        } catch (Exception e) {
            return false;
        }
    }

    protected static ClassLoader dX() {
        ClassLoader classLoader;
        synchronized (Br) {
            classLoader = Bs;
        }
        return classLoader;
    }

    protected static Integer dY() {
        Integer num;
        synchronized (Br) {
            num = Bt;
        }
        return num;
    }

    protected boolean dZ() {
        return this.Bu;
    }
}
