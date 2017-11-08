package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.UrlCheckResultStructure.UrlCheckResult;

/* compiled from: Unknown */
/* synthetic */ class c {
    static final /* synthetic */ int[] a = new int[UrlCheckResult.values().length];

    static {
        try {
            a[UrlCheckResult.RESULT_SUSPICIOUS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[UrlCheckResult.RESULT_OK.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[UrlCheckResult.RESULT_UNKNOWN_ERROR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
