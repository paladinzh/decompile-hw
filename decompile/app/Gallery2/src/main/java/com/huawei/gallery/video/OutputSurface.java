package com.huawei.gallery.video;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Looper;
import android.view.Surface;
import com.android.gallery3d.util.GalleryLog;

class OutputSurface implements OnFrameAvailableListener {
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private boolean mFrameAvailable;
    private Object mFrameSyncObject = new Object();
    private int mFrameTorlerance = 0;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private TextureRender mTextureRender;

    public OutputSurface() {
        setup();
    }

    private void setup() {
        this.mTextureRender = new TextureRender();
        this.mTextureRender.surfaceCreated();
        this.mSurfaceTexture = new SurfaceTexture(this.mTextureRender.getTextureId());
        this.mSurfaceTexture.setOnFrameAvailableListener(this);
        GalleryLog.w("decode", "mSurfaceTexture created in thread " + Thread.currentThread() + ", has a Looper ? " + Looper.myLooper());
        this.mSurface = new Surface(this.mSurfaceTexture);
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
            while (!this.mFrameAvailable) {
                try {
                    this.mFrameSyncObject.wait(500);
                    if (!this.mFrameAvailable && outOfTorlerance(1)) {
                        GalleryLog.e("decode", "Surface frame wait timed out");
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
            this.mFrameAvailable = false;
        }
        this.mTextureRender.checkGlError("before updateTexImage");
        this.mSurfaceTexture.updateTexImage();
    }

    public void drawImage() {
        this.mTextureRender.drawFrame(this.mSurfaceTexture);
    }

    public void onFrameAvailable(SurfaceTexture st) {
        if (this.mSurfaceTexture != st) {
            GalleryLog.w("OutputSurface", "invalid texture arrived !!! ");
            return;
        }
        synchronized (this.mFrameSyncObject) {
            if (this.mFrameAvailable && outOfTorlerance(-1)) {
                GalleryLog.e("decode", "mFrameAvailable already set, frame could be dropped");
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            this.mFrameAvailable = true;
            this.mFrameSyncObject.notifyAll();
        }
    }

    private boolean outOfTorlerance(int change) {
        this.mFrameTorlerance += change;
        GalleryLog.e("decode", "mFrameTorlerance changed by " + change + ", upto " + this.mFrameTorlerance);
        if (this.mFrameTorlerance < -1 || this.mFrameTorlerance > 5) {
            return true;
        }
        return false;
    }
}
