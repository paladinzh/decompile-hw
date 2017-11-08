package com.huawei.keyguard.view.charge.e50;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.util.AttributeSet;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class UnlockChargingView extends GLSurfaceView {
    private Runnable mAddDraw = new Runnable() {
        public void run() {
            UnlockChargingView.this.mUCRenderer.start(UnlockChargingView.this.mChargeLevel, UnlockChargingView.this.mCurrentChargineMode);
        }
    };
    private int mChargeLevel;
    private int mCurrentChargineMode = 0;
    private boolean mInited = false;
    private Runnable mStopDraw = new Runnable() {
        public void run() {
            UnlockChargingView.this.setRenderMode(0);
        }
    };
    private UCRenderer mUCRenderer;

    public UnlockChargingView(Context context) {
        super(context);
        init(context);
    }

    public UnlockChargingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private static EGLConfigChooser getDefaultChooser() {
        return new EGLConfigChooser() {
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                EGLConfig[] configOut = new EGLConfig[1];
                egl.eglChooseConfig(display, new int[]{12339, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 16, 12338, 1, 12337, 2, 12344}, configOut, 1, new int[1]);
                return configOut[0];
            }
        };
    }

    private void init(Context context) {
        if (!this.mInited) {
            setEGLConfigChooser(getDefaultChooser());
            setZOrderOnTop(true);
            getHolder().setFormat(-3);
            this.mUCRenderer = new UCRenderer(getContext());
            setRenderer(this.mUCRenderer);
            this.mInited = true;
        }
    }

    public void startRender(int delay, int chargingMode, int chargeLevel) {
        postDelayed(this.mAddDraw, (long) delay);
        this.mCurrentChargineMode = chargingMode;
        this.mChargeLevel = chargeLevel;
    }

    public void stopRender() {
        this.mUCRenderer.stop();
        post(this.mStopDraw);
    }
}
