package com.avast.android.sdk.shield.webshield;

/* compiled from: Unknown */
/* synthetic */ class a {
    static final /* synthetic */ int[] a = new int[ScannedUrlAction.values().length];

    static {
        try {
            a[ScannedUrlAction.DO_NOTHING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[ScannedUrlAction.TYPOSQUATTING_AUTOCORRECT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[ScannedUrlAction.BLOCK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
    }
}
