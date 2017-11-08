package com.huawei.rcs.utils.videocompress;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Looper;
import android.view.Surface;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;

class RcsOutputSurface implements OnFrameAvailableListener {
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private boolean mFrameAvailable;
    private Object mFrameSyncObject = new Object();
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private RcsTextureRender mTextureRender;

    public RcsOutputSurface() {
        setup();
    }

    private void setup() {
        if (RcsCommonConfig.isRCSSwitchOn() && this.mTextureRender == null) {
            this.mTextureRender = new RcsTextureRender();
        }
        if (this.mTextureRender != null) {
            this.mTextureRender.surfaceCreated();
            this.mSurfaceTexture = new SurfaceTexture(this.mTextureRender.getTextureId());
        }
        if (this.mSurfaceTexture != null) {
            this.mSurfaceTexture.setOnFrameAvailableListener(this);
        }
        MLog.w("decode", "mSurfaceTexture created in thread " + Thread.currentThread() + ", has a Looper ? " + Looper.myLooper());
        if (this.mSurfaceTexture != null) {
            this.mSurface = new Surface(this.mSurfaceTexture);
        }
    }

    public void release() {
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mSurface.release();
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mTextureRender = null;
        this.mSurface = null;
        this.mSurfaceTexture = null;
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public void awaitNewImage() {
        synchronized (this.mFrameSyncObject) {
            do {
                if (this.mFrameAvailable) {
                    break;
                }
                try {
                    this.mFrameSyncObject.wait(500);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            } while (this.mFrameAvailable);
            MLog.e("decode", "Surface frame wait timed out");
            this.mFrameAvailable = false;
        }
        if (this.mTextureRender != null) {
            this.mTextureRender.checkGlError("before updateTexImage");
        }
        if (this.mSurfaceTexture != null) {
            this.mSurfaceTexture.updateTexImage();
        }
    }

    public void drawImage() {
        if (this.mTextureRender != null && this.mSurfaceTexture != null) {
            this.mTextureRender.drawFrame(this.mSurfaceTexture);
        }
    }

    public void onFrameAvailable(SurfaceTexture st) {
        synchronized (this.mFrameSyncObject) {
            if (this.mFrameAvailable) {
                MLog.e("decode", "mFrameAvailable already set, frame could be dropped");
            }
            this.mFrameAvailable = true;
            this.mFrameSyncObject.notifyAll();
        }
    }
}
