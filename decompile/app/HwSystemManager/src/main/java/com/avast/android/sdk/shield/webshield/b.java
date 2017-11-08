package com.avast.android.sdk.shield.webshield;

/* compiled from: Unknown */
/* synthetic */ class b {
    static final /* synthetic */ int[] a = new int[ScannedUrlAction.values().length];
    static final /* synthetic */ int[] b = new int[UrlAction.values().length];

    static {
        try {
            b[UrlAction.ALLOW.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            b[UrlAction.BLOCK.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            b[UrlAction.SCAN.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            a[ScannedUrlAction.BLOCK.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            a[ScannedUrlAction.TYPOSQUATTING_AUTOCORRECT.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            a[ScannedUrlAction.DO_NOTHING.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
    }
}
