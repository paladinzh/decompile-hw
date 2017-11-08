package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.internal.q.a;
import com.avast.android.sdk.engine.internal.q.b;

/* compiled from: Unknown */
/* synthetic */ class z {
    static final /* synthetic */ int[] a = new int[b.values().length];
    static final /* synthetic */ int[] b = new int[a.values().length];

    static {
        try {
            b[a.RESULT_OK.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[b.RESULT_UNKNOWN_ERROR.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[b.RESULT_ALREADY_DEREGISTERED.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            a[b.RESULT_OK.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
    }
}
