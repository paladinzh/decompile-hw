package com.fyusion.sdk.core.a;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Process;
import android.util.Log;
import android.view.Surface;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.HardwareAbstractionLayer;
import com.fyusion.sdk.common.OpenGLUtils;

/* compiled from: Unknown */
public abstract class a implements OnFrameAvailableListener {
    protected int a;
    protected int b;
    private i c;
    private SurfaceTexture d;
    private Surface e;
    private EGLDisplay f = EGL14.EGL_NO_DISPLAY;
    private EGLContext g = EGL14.EGL_NO_CONTEXT;
    private EGLSurface h = EGL14.EGL_NO_SURFACE;
    private final Object i = new Object();
    private boolean j;

    public a(int i, int i2, i iVar) {
        if (i > 0 && i2 > 0) {
            this.a = i;
            this.b = i2;
            this.c = iVar;
            f();
            g();
            e();
            return;
        }
        throw new IllegalArgumentException();
    }

    private void a(String str) {
        int eglGetError = EGL14.eglGetError();
        if (eglGetError != 12288) {
            throw new RuntimeException(str + ": EGL error: 0x" + Integer.toHexString(eglGetError));
        }
    }

    private void e() {
        this.c.a();
        this.d = new SurfaceTexture(this.c.b());
        this.d.setOnFrameAvailableListener(this);
        this.e = new Surface(this.d);
    }

    private void f() {
        this.f = EGL14.eglGetDisplay(0);
        if (this.f != EGL14.EGL_NO_DISPLAY) {
            int[] iArr = new int[2];
            if (EGL14.eglInitialize(this.f, iArr, 0, iArr, 1)) {
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
                if (EGL14.eglChooseConfig(this.f, iArr2, 0, eGLConfigArr, 0, eGLConfigArr.length, new int[1], 0)) {
                    this.g = EGL14.eglCreateContext(this.f, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, r8, 12344}, 0);
                    a("eglCreateContext");
                    if (this.g != null) {
                        this.h = EGL14.eglCreatePbufferSurface(this.f, eGLConfigArr[0], this.c.a(this.a, this.b), 0);
                        a("eglCreatePbufferSurface");
                        if (this.h == null) {
                            throw new RuntimeException("surface was null");
                        }
                        return;
                    }
                    throw new RuntimeException("null context");
                }
                throw new RuntimeException("unable to find RGBA8888+recordable EGL config");
            }
            this.f = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        throw new RuntimeException("unable to get EGL14 display");
    }

    private void g() {
        if (!EGL14.eglMakeCurrent(this.f, this.h, this.h, this.g)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public void a() {
        if (this.f != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.f, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.f, this.h);
            EGL14.eglDestroyContext(this.f, this.g);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.f);
        }
        this.f = EGL14.EGL_NO_DISPLAY;
        this.g = EGL14.EGL_NO_CONTEXT;
        this.h = EGL14.EGL_NO_SURFACE;
        this.e.release();
        this.c = null;
        this.e = null;
        this.d = null;
    }

    public void a(boolean z) {
        this.c.a(this.d, z);
    }

    public Surface b() {
        return this.e;
    }

    public boolean c() {
        synchronized (this.i) {
            do {
                if (this.j) {
                    this.j = false;
                    OpenGLUtils.checkGlError("before updateTexImage, thread " + Process.myTid());
                    this.d.updateTexImage();
                    return true;
                }
                try {
                    this.i.wait(2500);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } while (this.j);
            DLog.d("awaitNewImage", "Timeout exceeded! no image found");
            OpenGLUtils.checkGlError("before updateTexImage, thread " + Process.myTid());
            return false;
        }
    }

    public abstract b d();

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this.i) {
            if (this.j) {
                Log.d("CodecOutputSurface", "onFrameAvailable mFrameAvailable already set!");
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            this.j = true;
            this.i.notifyAll();
        }
    }
}
