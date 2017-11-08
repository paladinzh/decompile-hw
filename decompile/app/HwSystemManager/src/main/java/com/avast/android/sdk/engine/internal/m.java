package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.UpdateCheckResultStructure.UpdateCheck;

/* compiled from: Unknown */
/* synthetic */ class m {
    static final /* synthetic */ int[] a = new int[UpdateCheck.values().length];

    static {
        try {
            a[UpdateCheck.RESULT_UP_TO_DATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[UpdateCheck.ERROR_OLD_APPLICATION_VERSION.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[UpdateCheck.ERROR_CURRENT_VPS_INVALID.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
