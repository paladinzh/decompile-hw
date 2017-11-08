package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.view.effect.ParticleRendererKeyguard;
import com.huawei.keyguard.view.effect.ParticleRendererKeyguard.DrawStates;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class CurveScreenAnimView extends GLSurfaceView implements Callback {
    private boolean ENABLE_CURVE_SCREEN;
    private boolean inited = false;
    private ParticleRendererKeyguard mParticleRenderer;
    private StopRunner mStopDraw = new StopRunner();
    private SwitchToDirtyModeRunner mStopDrawToDirtyMode = new SwitchToDirtyModeRunner();

    private class StopRunner implements Runnable {
        private StopRunner() {
        }

        public void run() {
            CurveScreenAnimView.this.mParticleRenderer.updateDrawState(DrawStates.NODRAW);
            CurveScreenAnimView.this.postDelayed(CurveScreenAnimView.this.mStopDrawToDirtyMode, 30);
        }
    }

    private class SwitchToDirtyModeRunner implements Runnable {
        private SwitchToDirtyModeRunner() {
        }

        public void run() {
            CurveScreenAnimView.this.setRenderMode(0);
        }
    }

    public CurveScreenAnimView(Context context) {
        super(context);
        this.ENABLE_CURVE_SCREEN = KeyguardCfg.isCurveScreen(context);
        init(context);
    }

    public CurveScreenAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ENABLE_CURVE_SCREEN = KeyguardCfg.isCurveScreen(context);
        init(context);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.ENABLE_CURVE_SCREEN) {
            setEnabled(false);
            setFocusable(false);
            setClickable(false);
            this.mStopDraw.run();
            AppHandler.addListener(this);
            return;
        }
        setVisibility(8);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.ENABLE_CURVE_SCREEN) {
            setVisibility(8);
            AppHandler.removeListener(this);
        }
    }

    public void setVisibility(int visibility) {
        HwLog.v("CurveScreenAnimView", "CurveScreen setVisibility. " + visibility + "-" + this.ENABLE_CURVE_SCREEN);
        if (!this.ENABLE_CURVE_SCREEN) {
            visibility = 8;
        }
        super.setVisibility(visibility);
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
        if (!this.inited && this.ENABLE_CURVE_SCREEN) {
            setEGLConfigChooser(getDefaultChooser());
            setZOrderOnTop(true);
            getHolder().setFormat(-3);
            Point sPoint = HwUnlockUtils.getPoint(context);
            this.mParticleRenderer = new ParticleRendererKeyguard(sPoint.x, sPoint.y);
            this.mParticleRenderer.setColor(getTouchColor());
            setRenderer(this.mParticleRenderer);
            this.mStopDraw.run();
            this.inited = true;
        }
    }

    public boolean procTouchEvent(MotionEvent event) {
        if (!this.ENABLE_CURVE_SCREEN) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                if (getVisibility() != 0) {
                    setVisibility(0);
                }
                removeCallbacks(this.mStopDraw);
                removeCallbacks(this.mStopDrawToDirtyMode);
                setRenderMode(1);
                this.mParticleRenderer.setTranslaterF(event.getX(), event.getY());
                this.mParticleRenderer.updateDrawState(DrawStates.STARTDRAW);
                break;
            case 1:
            case 3:
            case 6:
                this.mParticleRenderer.updateDrawState(DrawStates.DISDRAW);
                postDelayed(this.mStopDraw, 500);
                break;
            case 2:
                this.mParticleRenderer.setTranslaterF(event.getX(), event.getY());
                break;
        }
        return false;
    }

    public void checkVisibility() {
        int lockType = KeyguardTheme.getInst().getLockStyle();
        boolean showView = true;
        if (!(lockType == 5 || lockType == 4)) {
            if (lockType == 3) {
            }
            if (showView) {
                HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
                showView = (monitor.isShowing() || monitor.isInBouncer()) ? false : KeyguardCfg.isDoubleLockOn(this.mContext);
            }
            if (!showView) {
                setVisibility(8);
            }
        }
        showView = false;
        if (this.mParticleRenderer != null) {
            this.mParticleRenderer.setColor(2139062143);
        }
        if (showView) {
            HwKeyguardUpdateMonitor monitor2 = HwKeyguardUpdateMonitor.getInstance();
            if (monitor2.isShowing()) {
            }
        }
        if (!showView) {
            setVisibility(8);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
            case 10:
                checkVisibility();
                break;
            case 11:
                setVisibility(8);
                break;
            case 21:
            case 23:
                int color = getTouchColor();
                this.mParticleRenderer.setColor(color);
                HwLog.w("CurveScreenAnimView", "Set color as to : " + color + "; msg-color: " + msg.arg1);
                break;
        }
        return false;
    }

    private int getTouchColor() {
        Drawable drawable = KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper();
        if (this.mParticleRenderer != null && (drawable instanceof BokehDrawable)) {
            return ((BokehDrawable) drawable).getTouchColor();
        }
        HwLog.w("CurveScreenAnimView", "Skip set color as not ready: ");
        return -1090486785;
    }
}
