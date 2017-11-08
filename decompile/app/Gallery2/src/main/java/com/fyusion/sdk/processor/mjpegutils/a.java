package com.fyusion.sdk.processor.mjpegutils;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Process;
import android.view.Surface;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.HardwareAbstractionLayer;

/* compiled from: Unknown */
public class a implements OnFrameAvailableListener {
    static int a = 0;
    static int b = 0;
    private static volatile EGLContext j = null;
    boolean c;
    private final boolean d = false;
    private JPEGTranscoder e = null;
    private c f;
    private SurfaceTexture g;
    private Surface h;
    private EGLDisplay i = EGL14.EGL_NO_DISPLAY;
    private EGLSurface k = EGL14.EGL_NO_SURFACE;
    private Object l = new Object();
    private boolean m;

    public a(int i, int i2) {
        boolean z = false;
        if (i > 0 && i2 > 0) {
            if (a != i || b != i2) {
                z = true;
            }
            this.c = z;
            a = i;
            b = i2;
            f();
            b();
            e();
            return;
        }
        throw new IllegalArgumentException();
    }

    private void a(String str) {
        int eglGetError = EGL14.eglGetError();
        if (eglGetError != 12288) {
            String str2 = "";
            switch (eglGetError) {
                case 12289:
                    str2 = "EGL_NOT_INITIALIZED";
                    break;
                case 12290:
                    str2 = "EGL_BAD_ACCESS";
                    break;
                case 12291:
                    str2 = "EGL_BAD_ALLOC";
                    break;
                case 12292:
                    str2 = "EGL_BAD_ATTRIBUTE";
                    break;
                case 12293:
                    str2 = "EGL_BAD_CONFIG";
                    break;
                case 12294:
                    str2 = "EGL_BAD_CONTEXT";
                    break;
                case 12295:
                    str2 = "EGL_BAD_CURRENT_SURFACE";
                    break;
                case 12296:
                    str2 = "EGL_BAD_DISPLAY";
                    break;
                case 12297:
                    str2 = "EGL_BAD_MATCH";
                    break;
                case 12298:
                    str2 = "EGL_BAD_NATIVE_PIXMAP";
                    break;
                case 12299:
                    str2 = "EGL_BAD_NATIVE_WINDOW";
                    break;
                case 12300:
                    str2 = "EGL_BAD_PARAMETER";
                    break;
                case 12301:
                    str2 = "EGL_BAD_SURFACE";
                    break;
                default:
                    str2 = "unknown EGL error code";
                    break;
            }
            throw new RuntimeException(str + ": EGL error: 0x" + Integer.toHexString(eglGetError) + " " + str2);
        }
    }

    private void e() {
        this.f = new c();
        this.f.b();
        this.g = new SurfaceTexture(this.f.a());
        this.g.setOnFrameAvailableListener(this);
        this.h = new Surface(this.g);
    }

    private void f() {
        this.i = EGL14.eglGetDisplay(0);
        if (this.i != EGL14.EGL_NO_DISPLAY) {
            int[] iArr = new int[2];
            if (EGL14.eglInitialize(this.i, iArr, 0, iArr, 1)) {
                int[] iArr2 = new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12339, 1, 12352, -1, 12344};
                switch (HardwareAbstractionLayer.supportedGLESVersion()) {
                    case 2:
                        iArr2[11] = 4;
                        break;
                    case 3:
                        iArr2[11] = 64;
                        break;
                    default:
                        iArr2[11] = 1;
                        break;
                }
                EGLConfig[] eGLConfigArr = new EGLConfig[1];
                if (EGL14.eglChooseConfig(this.i, iArr2, 0, eGLConfigArr, 0, eGLConfigArr.length, new int[1], 0)) {
                    EGLContext eglCreateContext;
                    iArr = new int[]{12440, r8, 12344};
                    if (j == null) {
                        eglCreateContext = EGL14.eglCreateContext(this.i, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, iArr, 0);
                    } else {
                        if (this.c) {
                            g();
                            eglCreateContext = EGL14.eglCreateContext(this.i, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, iArr, 0);
                        }
                        a("eglCreateContext");
                        if (j == null) {
                            this.k = EGL14.eglCreatePbufferSurface(this.i, eGLConfigArr[0], new int[]{12375, a, 12374, b, 12344}, 0);
                            if (this.k == EGL14.EGL_NO_SURFACE) {
                                DLog.e("EGLContextManager", "EGL NO SURFACE returned");
                            }
                            a("eglCreatePbufferSurface");
                            if (this.k != null) {
                                throw new RuntimeException("surface was null");
                            }
                            return;
                        }
                        throw new RuntimeException("null context");
                    }
                    j = eglCreateContext;
                    a("eglCreateContext");
                    if (j == null) {
                        throw new RuntimeException("null context");
                    }
                    this.k = EGL14.eglCreatePbufferSurface(this.i, eGLConfigArr[0], new int[]{12375, a, 12374, b, 12344}, 0);
                    if (this.k == EGL14.EGL_NO_SURFACE) {
                        DLog.e("EGLContextManager", "EGL NO SURFACE returned");
                    }
                    a("eglCreatePbufferSurface");
                    if (this.k != null) {
                        throw new RuntimeException("surface was null");
                    }
                    return;
                }
                throw new RuntimeException("unable to find RGB888+recordable EGL config");
            }
            this.i = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        throw new RuntimeException("unable to get EGL14 display");
    }

    private void g() {
        if (this.i != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.i, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(this.i, j);
        }
        j = EGL14.EGL_NO_CONTEXT;
        j = null;
    }

    public void a() {
        if (this.i != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.i, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.i, this.k);
            EGL14.eglDestroyContext(this.i, j);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.i);
        }
        j = EGL14.EGL_NO_CONTEXT;
        j = null;
        this.i = EGL14.EGL_NO_DISPLAY;
        this.k = EGL14.EGL_NO_SURFACE;
        if (this.h != null) {
            this.h.release();
        }
        if (this.g != null) {
            this.g.release();
        }
        this.f = null;
        this.h = null;
        this.g = null;
        if (this.e != null) {
            this.e.finishEncode();
        }
    }

    public void a(boolean z) {
        this.f.a(this.g, z);
    }

    public void a(float[] fArr) {
        this.f.a(fArr);
    }

    public boolean a(int i) {
        synchronized (this.l) {
            do {
                if (this.m) {
                    this.m = false;
                    this.f.a("before updateTexImage, thread " + Process.myTid());
                    this.g.updateTexImage();
                    return true;
                }
                try {
                    this.l.wait((long) i);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } while (this.m);
            return false;
        }
    }

    public void b() {
        if (!EGL14.eglMakeCurrent(this.i, this.k, this.k, j)) {
            a("eglMakeCurrent");
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public Surface c() {
        return this.h;
    }

    public boolean d() {
        synchronized (this.l) {
            do {
                if (this.m) {
                    this.m = false;
                    this.f.a("before updateTexImage, thread " + Process.myTid());
                    this.g.updateTexImage();
                    return true;
                }
                try {
                    this.l.wait(5000);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } while (this.m);
            DLog.d("awaitNewImage", "Timeout exceeded! no image found");
            this.f.a("before updateTexImage, thread " + Process.myTid());
            return false;
        }
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this.l) {
            if (this.m) {
                DLog.d("CodecOutputSurface", "onFrameAvailable mFrameAvailable already set!");
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            this.m = true;
            this.l.notify();
        }
    }
}
