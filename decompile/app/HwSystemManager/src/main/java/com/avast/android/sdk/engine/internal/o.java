package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.UrlSource;
import com.avast.android.sdk.engine.obfuscated.z.d.b;

/* compiled from: Unknown */
/* synthetic */ class o {
    static final /* synthetic */ int[] a = new int[UrlSource.values().length];
    static final /* synthetic */ int[] b = new int[b.values().length];

    static {
        try {
            b[b.REDIRECT_ID_EXISTS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            b[b.SUCCESS.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            b[b.FAILURE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            a[UrlSource.STOCK.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            a[UrlSource.STOCK_JB.ordinal()] = 2;
        } catch (NoSuchFieldError e5) {
        }
        try {
            a[UrlSource.CHROME.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            a[UrlSource.DOLPHIN_MINI.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        try {
            a[UrlSource.DOLPHIN.ordinal()] = 5;
        } catch (NoSuchFieldError e8) {
        }
        try {
            a[UrlSource.SILK.ordinal()] = 6;
        } catch (NoSuchFieldError e9) {
        }
        try {
            a[UrlSource.BOAT_MINI.ordinal()] = 7;
        } catch (NoSuchFieldError e10) {
        }
        try {
            a[UrlSource.BOAT.ordinal()] = 8;
        } catch (NoSuchFieldError e11) {
        }
        try {
            a[UrlSource.CHROME_M.ordinal()] = 9;
        } catch (NoSuchFieldError e12) {
        }
        try {
            a[UrlSource.SBROWSER.ordinal()] = 10;
        } catch (NoSuchFieldError e13) {
        }
    }
}
