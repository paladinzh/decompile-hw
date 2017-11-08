package com.huawei.gallery.refocus.wideaperture.photo3dview.ui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.view.WindowManager;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.Photo3DViewProperty;
import com.huawei.gallery.refocus.wideaperture.photo3dview.app.WideAperturePhoto3DViewController;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class WideAperturePhoto3DView extends GLSurfaceView implements Renderer {
    private WideAperturePhoto3DViewController m3DViewController;
    private float[] mAngle;
    private Context mContext;
    private boolean mDestroyView;
    private boolean mSurfaceCreated;

    public static class Photo3DViewEGLConfigChooser implements EGLConfigChooser {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            EGLConfig[] configOut = new EGLConfig[1];
            egl.eglChooseConfig(display, new int[]{12324, 8, 12323, 8, 12322, 8, 12325, 16, 12338, 1, 12337, 8, 12344}, configOut, 1, new int[1]);
            return configOut[0];
        }
    }

    public WideAperturePhoto3DView(Context context) {
        this(context, null);
    }

    public WideAperturePhoto3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAngle = new float[]{0.0f, 0.0f};
        this.mContext = context;
        this.mSurfaceCreated = false;
        this.mDestroyView = false;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(new Photo3DViewEGLConfigChooser());
        setRenderer(this);
        setRenderMode(0);
    }

    public void set3DViewController(WideAperturePhoto3DViewController controller) {
        this.m3DViewController = controller;
    }

    public void setViewAngle(float angleX, float angleY) {
        this.mAngle[0] = angleX;
        this.mAngle[1] = angleY;
    }

    public void create3DView() {
        if (this.m3DViewController != null) {
            this.m3DViewController.create3DView();
        }
        this.mSurfaceCreated = true;
        this.mDestroyView = false;
    }

    public void destroyView() {
        this.mDestroyView = true;
        this.mSurfaceCreated = false;
        queueEvent(new Runnable() {
            public void run() {
                WideAperturePhoto3DViewController controller = WideAperturePhoto3DView.this.m3DViewController;
                if (controller != null) {
                    controller.destroy3DView();
                }
            }
        });
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        WideAperturePhoto3DViewController controller = this.m3DViewController;
        if (controller != null) {
            controller.set3DViewProperty(Photo3DViewProperty.SurfaceCreate.ordinal(), 0, 0, 0);
        }
    }

    public void onDrawFrame(GL10 gl) {
        WideAperturePhoto3DViewController controller = this.m3DViewController;
        if (this.mSurfaceCreated && !this.mDestroyView && controller != null) {
            controller.invalidate3DView(this.mAngle);
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        WideAperturePhoto3DViewController controller = this.m3DViewController;
        if (controller != null) {
            controller.set3DViewProperty(Photo3DViewProperty.Display.ordinal(), ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation(), width, height);
        }
    }
}
