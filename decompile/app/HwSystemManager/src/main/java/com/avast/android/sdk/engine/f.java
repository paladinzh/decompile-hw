package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.ScanResultStructure.DetectionType;

/* compiled from: Unknown */
/* synthetic */ class f {
    static final /* synthetic */ int[] a = new int[a.values().length];
    static final /* synthetic */ int[] b = new int[DetectionType.values().length];

    static {
        try {
            b[DetectionType.TYPE_DIALER.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            b[DetectionType.TYPE_ADWARE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            b[DetectionType.TYPE_CRYPTOR.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            b[DetectionType.TYPE_DROPPER.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            b[DetectionType.TYPE_EXPLOIT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            b[DetectionType.TYPE_VIRUS_MAKING_KIT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            b[DetectionType.TYPE_ROOTKIT.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            b[DetectionType.TYPE_SPYWARE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            b[DetectionType.TYPE_TROJAN.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            b[DetectionType.TYPE_WORM.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            b[DetectionType.TYPE_PUP.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            b[DetectionType.TYPE_JOKE.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            b[DetectionType.TYPE_TOOL.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            b[DetectionType.TYPE_HEURISTICS.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            b[DetectionType.TYPE_SUSPICIOUS.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            a[a.PAYLOAD_RESULT.ordinal()] = 1;
        } catch (NoSuchFieldError e16) {
        }
        try {
            a[a.PAYLOAD_INFECTION_TYPE.ordinal()] = 2;
        } catch (NoSuchFieldError e17) {
        }
        try {
            a[a.PAYLOAD_ADDON_CATEGORIES.ordinal()] = 3;
        } catch (NoSuchFieldError e18) {
        }
    }
}
