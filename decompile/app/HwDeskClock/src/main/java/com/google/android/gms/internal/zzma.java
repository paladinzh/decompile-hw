package com.google.android.gms.internal;

import android.os.WorkSource;
import java.lang.reflect.Method;

/* compiled from: Unknown */
public class zzma {
    private static final Method zzagj = zzqb();
    private static final Method zzagk = zzqc();
    private static final Method zzagl = zzqd();
    private static final Method zzagm = zzqe();
    private static final Method zzagn = zzqf();

    private static Method zzqb() {
        Method method = null;
        try {
            method = WorkSource.class.getMethod("add", new Class[]{Integer.TYPE});
        } catch (Exception e) {
        }
        return method;
    }

    private static Method zzqc() {
        Method method = null;
        if (zzlv.zzpW()) {
            try {
                method = WorkSource.class.getMethod("add", new Class[]{Integer.TYPE, String.class});
            } catch (Exception e) {
            }
        }
        return method;
    }

    private static Method zzqd() {
        Method method = null;
        try {
            method = WorkSource.class.getMethod("size", new Class[0]);
        } catch (Exception e) {
        }
        return method;
    }

    private static Method zzqe() {
        Method method = null;
        try {
            method = WorkSource.class.getMethod("get", new Class[]{Integer.TYPE});
        } catch (Exception e) {
        }
        return method;
    }

    private static Method zzqf() {
        Method method = null;
        if (zzlv.zzpW()) {
            try {
                method = WorkSource.class.getMethod("getName", new Class[]{Integer.TYPE});
            } catch (Exception e) {
            }
        }
        return method;
    }
}
