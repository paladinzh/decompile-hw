package com.fyusion.sdk.common;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecList;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.os.Build.VERSION;

/* compiled from: Unknown */
public class l {
    private static int a = -1;
    private static final Object b = new Object();
    private static boolean c = false;
    private static boolean d = false;
    private static boolean e = false;
    private static boolean f = false;

    public static synchronized int a() {
        synchronized (l.class) {
            if (a == -1) {
                EGLDisplay eglGetDisplay = EGL14.eglGetDisplay(0);
                if (eglGetDisplay != EGL14.EGL_NO_DISPLAY) {
                    int[] iArr = new int[2];
                    if (EGL14.eglInitialize(eglGetDisplay, iArr, 0, iArr, 1)) {
                        for (int i = 3; i >= 1; i--) {
                            iArr = new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12339, 1, 12352, -1, 12344};
                            int[] iArr2 = new int[]{12440, -1, 12344};
                            switch (i) {
                                case 2:
                                    iArr[11] = 4;
                                    break;
                                case 3:
                                    iArr[11] = 64;
                                    break;
                                default:
                                    iArr[11] = 1;
                                    break;
                            }
                            EGLConfig[] eGLConfigArr = new EGLConfig[1];
                            if (EGL14.eglChooseConfig(eglGetDisplay, iArr, 0, eGLConfigArr, 0, eGLConfigArr.length, new int[1], 0)) {
                                iArr2[1] = i;
                                EGLContext eglCreateContext = EGL14.eglCreateContext(eglGetDisplay, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, iArr2, 0);
                                if (eglCreateContext != null) {
                                    a = i;
                                    EGL14.eglDestroyContext(eglGetDisplay, eglCreateContext);
                                }
                            }
                        }
                    }
                }
                if (a == -1) {
                    a = 0;
                }
                if (eglGetDisplay != EGL14.EGL_NO_DISPLAY) {
                    EGL14.eglReleaseThread();
                    EGL14.eglTerminate(eglGetDisplay);
                }
                int i2 = a;
                return i2;
            }
            i2 = a;
            return i2;
        }
    }

    public static boolean b() {
        if (c) {
            return d;
        }
        synchronized (b) {
            if (c) {
                boolean z = d;
                return z;
            }
            d = d();
            c = true;
            return d;
        }
    }

    public static boolean c() {
        if (VERSION.SDK_INT < 24) {
            return false;
        }
        if (e) {
            return f;
        }
        synchronized (b) {
            if (e) {
                boolean z = f;
                return z;
            }
            f = e();
            e = true;
            return f;
        }
    }

    private static boolean d() {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            for (Object equals : codecInfoAt.getSupportedTypes()) {
                if ("video/avc".equals(equals)) {
                    for (CodecProfileLevel codecProfileLevel : codecInfoAt.getCapabilitiesForType("video/avc").profileLevels) {
                        if (codecProfileLevel.profile >= 8) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean e() {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                for (Object equals : codecInfoAt.getSupportedTypes()) {
                    if ("video/hevc".equals(equals)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }
}
