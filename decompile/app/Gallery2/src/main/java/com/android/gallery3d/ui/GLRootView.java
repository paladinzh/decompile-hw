package com.android.gallery3d.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.GLRoot.OnDetachListener;
import com.android.gallery3d.ui.GLRoot.OnGLIdleListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MotionEventHelper;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.anim.CanvasAnimation;
import com.huawei.gallery.ui.OpenAnimationProxyView;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GLRootView extends GLSurfaceView implements Renderer, GLRoot {
    private GLView mAnimationProxyView;
    private final ArrayList<CanvasAnimation> mAnimations;
    private GLCanvas mCanvas;
    private int mCompensation;
    private Matrix mCompensationMatrix;
    private GLView mContentView;
    private OnDetachListener mDetachListener;
    private int mDisplayRotation;
    private final GalleryEGLConfigChooser mEglConfigChooser;
    private int mFlags;
    private boolean mFreeze;
    private final Condition mFreezeCondition;
    private GL11 mGL;
    private long mGetLastTimeStamp;
    private Handler mHandler;
    private final ArrayDeque<OnGLIdleListener> mIdleListeners;
    private final IdleRunner mIdleRunner;
    private boolean mInDownState;
    private volatile boolean mInstantTouchingState;
    private int mInvalidateColor;
    private GLRootViewDebugger mLogger;
    private volatile boolean mNeedAutoChange;
    private OrientationSource mOrientationSource;
    private final ReentrantLock mProxyLock;
    private final ReentrantLock mRenderLock;
    private volatile boolean mRenderRequested;
    private volatile boolean mWantOnceOpenGLRender;

    private class IdleRunner implements Runnable {
        private boolean mActive;

        private IdleRunner() {
            this.mActive = false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (GLRootView.this.mIdleListeners) {
                this.mActive = false;
                if (GLRootView.this.mIdleListeners.isEmpty() || GLRootView.this.mCanvas == null) {
                } else {
                    OnGLIdleListener listener = (OnGLIdleListener) GLRootView.this.mIdleListeners.removeFirst();
                }
            }
        }

        public void enable() {
            if (!this.mActive) {
                this.mActive = true;
                GLRootView.this.queueEvent(this);
            }
        }
    }

    public GLRootView(Context context) {
        this(context, null);
    }

    public GLRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLogger = new GLRootViewDebugger();
        this.mCompensationMatrix = new Matrix();
        this.mInvalidateColor = 0;
        this.mFlags = 2;
        this.mRenderRequested = false;
        this.mInDownState = false;
        this.mInstantTouchingState = false;
        this.mEglConfigChooser = new GalleryEGLConfigChooser();
        this.mAnimations = new ArrayList();
        this.mIdleListeners = new ArrayDeque();
        this.mIdleRunner = new IdleRunner();
        this.mRenderLock = new ReentrantLock();
        this.mProxyLock = new ReentrantLock();
        this.mFreezeCondition = this.mRenderLock.newCondition();
        this.mGetLastTimeStamp = 0;
        this.mFlags |= 1;
        setBackgroundDrawable(null);
        setEGLConfigChooser(this.mEglConfigChooser);
        setRenderer(this);
        if (ApiHelper.USE_888_PIXEL_FORMAT) {
            getHolder().setFormat(3);
        } else {
            getHolder().setFormat(4);
        }
        setZOrderMediaOverlay(true);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        GLRootView.this.mInstantTouchingState = false;
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void registerLaunchedAnimation(CanvasAnimation animation) {
        this.mAnimations.add(animation);
    }

    public boolean addOnGLIdleListener(OnGLIdleListener listener) {
        if (this.mCanvas == null) {
            return false;
        }
        synchronized (this.mIdleListeners) {
            this.mIdleListeners.addLast(listener);
            this.mIdleRunner.enable();
        }
        return true;
    }

    protected void onAttachedToWindow() {
        TraceController.traceBegin("GLRootView.onAttachedToWindow");
        super.onAttachedToWindow();
        TraceController.traceEnd();
    }

    public void setContentPane(GLView content) {
        if (this.mContentView != content) {
            if (this.mContentView != null) {
                if (this.mInDownState) {
                    long now = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                    this.mContentView.dispatchTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                    this.mInDownState = false;
                }
                this.mContentView.detachFromRoot();
                GLCanvas canvas = this.mCanvas;
                if (canvas != null) {
                    canvas.yieldAllTextures();
                }
            }
            this.mContentView = content;
            if (content != null) {
                content.attachToRoot(this);
                requestLayoutContentPane();
            }
        }
    }

    public void setAnimationProxyView(GLView proxyView) {
        TraceController.traceBegin("GLRootView setAnimationProxyView");
        this.mAnimationProxyView = proxyView;
        this.mNeedAutoChange = false;
        if (proxyView != null) {
            proxyView.attachToRoot(this);
            requestLayoutContentPane();
        }
        this.mWantOnceOpenGLRender = false;
        unlockRenderThread();
        TraceController.traceBegin("GLRootView setAnimationProxyView unlockRenderThread");
        while (!this.mWantOnceOpenGLRender) {
            try {
                requestRender();
            } finally {
                lockRenderThread();
                TraceController.traceEnd();
            }
        }
        TraceController.traceEnd();
    }

    public void clearAnimationProxyView(boolean force) {
        TraceController.traceBegin("GLRootView clearAnimationProxyView");
        this.mProxyLock.lock();
        try {
            GLView proxyView = this.mAnimationProxyView;
            if (proxyView instanceof OpenAnimationProxyView) {
                if (force || !proxyView.isDoingStateTransitionAnimation()) {
                    ((OpenAnimationProxyView) proxyView).clear();
                    this.mAnimationProxyView = null;
                    requestLayoutContentPane();
                } else {
                    this.mNeedAutoChange = true;
                    TraceController.traceEnd();
                    return;
                }
            }
            this.mAnimationProxyView = null;
            this.mProxyLock.unlock();
            TraceController.traceEnd();
        } finally {
            this.mProxyLock.unlock();
            TraceController.traceEnd();
        }
    }

    public boolean hasAnimationProxyView() {
        return this.mAnimationProxyView != null;
    }

    public void requestRenderForced() {
        superRequestRender();
    }

    public void requestRender() {
        GLView contentView = this.mContentView;
        if ((contentView == null || !contentView.noRender()) && !this.mRenderRequested) {
            this.mLogger.onRequestRender();
            this.mRenderRequested = true;
            super.requestRender();
        }
    }

    private void superRequestRender() {
        super.requestRender();
    }

    public void requestLayoutContentPane() {
        this.mRenderLock.lock();
        try {
            if (!(this.mContentView == null && this.mAnimationProxyView == null)) {
                if ((this.mFlags & 2) == 0) {
                    if ((this.mFlags & 1) == 0) {
                        this.mRenderLock.unlock();
                        return;
                    }
                    this.mFlags |= 2;
                    requestRender();
                    this.mRenderLock.unlock();
                    return;
                }
            }
            this.mRenderLock.unlock();
        } catch (Throwable th) {
            this.mRenderLock.unlock();
        }
    }

    private void layoutContentPane(GLView view) {
        int displayRotation;
        int compensation;
        this.mFlags &= -3;
        int w = getWidth();
        int h = getHeight();
        if (this.mOrientationSource != null) {
            displayRotation = this.mOrientationSource.getDisplayRotation();
            compensation = this.mOrientationSource.getCompensation();
        } else {
            displayRotation = 0;
            compensation = 0;
        }
        if (this.mCompensation != compensation) {
            this.mCompensation = compensation;
            if (this.mCompensation % 180 != 0) {
                this.mCompensationMatrix.setRotate((float) this.mCompensation);
                this.mCompensationMatrix.preTranslate(((float) (-w)) / 2.0f, ((float) (-h)) / 2.0f);
                this.mCompensationMatrix.postTranslate(((float) h) / 2.0f, ((float) w) / 2.0f);
            } else {
                this.mCompensationMatrix.setRotate((float) this.mCompensation, ((float) w) / 2.0f, ((float) h) / 2.0f);
            }
        }
        this.mDisplayRotation = displayRotation;
        if (this.mCompensation % 180 != 0) {
            int tmp = w;
            w = h;
            h = tmp;
        }
        if (!(view == null || w == 0 || h == 0)) {
            view.layout(0, 0, w, h);
        }
        this.mLogger.onLayoutContentPane(w, h, compensation, view);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            requestLayoutContentPane();
        }
    }

    public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
        GL11 gl = (GL11) gl1;
        if (this.mGL != null) {
            GalleryLog.i("GLRootView", "GLObject has changed from " + this.mGL + " to " + gl);
        }
        this.mRenderLock.lock();
        try {
            this.mGL = gl;
            GLCanvas canvas = this.mCanvas;
            if (canvas != null) {
                canvas.invalidateAllTextures();
            }
            this.mCanvas = new GLCanvasImpl(gl);
            setRenderMode(0);
        } finally {
            this.mRenderLock.unlock();
        }
    }

    public void onSurfaceChanged(GL10 gl1, int width, int height) {
        Process.setThreadPriority(-4);
        GalleryUtils.setRenderThread();
        this.mLogger.onSurfaceChanged(gl1, width, height);
        Utils.assertTrue(this.mGL == ((GL11) gl1));
        this.mCanvas.setSize(width, height);
    }

    public void onDrawFrame(GL10 gl) {
        TraceController.traceBegin("GLRootView onDrawFrame");
        AnimationTime.update();
        long t0 = this.mLogger.onDrawFrameStart(gl);
        this.mWantOnceOpenGLRender = true;
        GLView proxyView = this.mAnimationProxyView;
        if (proxyView != null) {
            drawAnimationProxyView(proxyView);
            boolean isProxyTextureNull = false;
            if (proxyView instanceof OpenAnimationProxyView) {
                isProxyTextureNull = ((OpenAnimationProxyView) proxyView).isProxyTextureNull();
            }
            if ((isProxyTextureNull || this.mNeedAutoChange) && !proxyView.isDoingStateTransitionAnimation()) {
                post(new Runnable() {
                    public void run() {
                        GLRootView.this.lockRenderThread();
                        try {
                            GLRootView.this.clearAnimationProxyView(true);
                        } finally {
                            GLRootView.this.unlockRenderThread();
                        }
                    }
                });
            }
        } else {
            drawContentView(gl);
        }
        this.mLogger.onDrawFrameEnd(t0);
        TraceController.traceEnd();
    }

    private void drawAnimationProxyView(GLView proxyView) {
        TraceController.traceBegin("GLRootView drawAnimationProxyView");
        this.mProxyLock.lock();
        try {
            this.mCanvas.deleteRecycledResources();
            this.mRenderRequested = false;
            if (this.mAnimationProxyView != null) {
                if (this.mOrientationSource == null || this.mDisplayRotation == this.mOrientationSource.getDisplayRotation()) {
                    if ((this.mFlags & 2) != 0) {
                    }
                    this.mCanvas.save(-1);
                    rotateCanvas(-this.mCompensation);
                    if (proxyView != null) {
                        this.mCanvas.clearBuffer();
                    } else if (proxyView.getVisibility() == 0) {
                        proxyView.render(this.mCanvas);
                    }
                    this.mCanvas.restore();
                    this.mProxyLock.unlock();
                    TraceController.traceEnd();
                }
                layoutContentPane(proxyView);
                this.mCanvas.save(-1);
                rotateCanvas(-this.mCompensation);
                if (proxyView != null) {
                    this.mCanvas.clearBuffer();
                } else if (proxyView.getVisibility() == 0) {
                    proxyView.render(this.mCanvas);
                }
                this.mCanvas.restore();
                this.mProxyLock.unlock();
                TraceController.traceEnd();
            }
        } finally {
            this.mProxyLock.unlock();
            TraceController.traceEnd();
        }
    }

    private void drawContentView(GL10 gl) {
        TraceController.traceBegin("GLRootView drawContentView");
        this.mRenderLock.lock();
        try {
            TraceController.traceBegin("GLRootView drawContentView get lock");
            TraceController.traceEnd();
            GLView proxyView = this.mAnimationProxyView;
            if (proxyView != null) {
                drawAnimationProxyView(proxyView);
                requestRenderForced();
                TraceController.traceEnd();
                return;
            }
            TraceController.traceBegin("GLRootView drawContentView mFreeze:" + this.mFreeze);
            while (this.mFreeze) {
                this.mFreezeCondition.awaitUninterruptibly();
            }
            TraceController.traceEnd();
            onDrawFrameLocked(gl);
            this.mRenderLock.unlock();
            TraceController.traceEnd();
        } finally {
            this.mRenderLock.unlock();
            TraceController.traceEnd();
        }
    }

    private void onDrawFrameLocked(GL10 gl) {
        long now;
        int n;
        int i;
        CanvasAnimation canvasAnimation;
        this.mLogger.outputFps();
        this.mCanvas.deleteRecycledResources();
        this.mRenderRequested = false;
        if (this.mOrientationSource == null || this.mDisplayRotation == this.mOrientationSource.getDisplayRotation()) {
            if ((this.mFlags & 2) != 0) {
            }
            this.mCanvas.save(-1);
            rotateCanvas(-this.mCompensation);
            if (this.mContentView != null) {
                this.mCanvas.clearBuffer();
            } else if (this.mContentView.getVisibility() == 0) {
                this.mContentView.render(this.mCanvas);
            }
            this.mCanvas.restore();
            if (!this.mAnimations.isEmpty()) {
                now = AnimationTime.get();
                n = this.mAnimations.size();
                for (i = 0; i < n; i++) {
                    canvasAnimation = (CanvasAnimation) this.mAnimations.get(i);
                    if (canvasAnimation == null) {
                        canvasAnimation.setStartTime(now);
                    }
                }
                this.mAnimations.clear();
            }
            synchronized (this.mIdleListeners) {
                if (!this.mIdleListeners.isEmpty()) {
                    this.mIdleRunner.enable();
                }
            }
        }
        layoutContentPane(this.mContentView);
        this.mCanvas.save(-1);
        rotateCanvas(-this.mCompensation);
        if (this.mContentView != null) {
            this.mCanvas.clearBuffer();
        } else if (this.mContentView.getVisibility() == 0) {
            this.mContentView.render(this.mCanvas);
        }
        this.mCanvas.restore();
        if (this.mAnimations.isEmpty()) {
            now = AnimationTime.get();
            n = this.mAnimations.size();
            for (i = 0; i < n; i++) {
                canvasAnimation = (CanvasAnimation) this.mAnimations.get(i);
                if (canvasAnimation == null) {
                    canvasAnimation.setStartTime(now);
                }
            }
            this.mAnimations.clear();
        }
        synchronized (this.mIdleListeners) {
            if (this.mIdleListeners.isEmpty()) {
                this.mIdleRunner.enable();
            }
        }
    }

    private void rotateCanvas(int degrees) {
        if (degrees != 0) {
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            this.mCanvas.translate((float) cx, (float) cy);
            this.mCanvas.rotate((float) degrees, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            if (degrees % 180 != 0) {
                this.mCanvas.translate((float) (-cy), (float) (-cx));
            } else {
                this.mCanvas.translate((float) (-cx), (float) (-cy));
            }
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        TraceController.traceBegin("GLRootView dispatchTouchEvent");
        if (!isEnabled()) {
            TraceController.traceEnd();
            return false;
        } else if (this.mAnimationProxyView != null) {
            requestRender();
            GalleryLog.d("GLRootView", "dispatchTouchEvent AnimationProxyView is not null");
            TraceController.traceEnd();
            return false;
        } else {
            int action = event.getAction();
            if (action == 3 || action == 1) {
                this.mInDownState = false;
                this.mInstantTouchingState = false;
            } else if (!(this.mInDownState || action == 0)) {
                TraceController.traceEnd();
                return false;
            }
            if (this.mCompensation != 0) {
                event = MotionEventHelper.transformEvent(event, this.mCompensationMatrix);
            }
            TraceController.traceBegin("GLRootView.dispatchTouchEvent action=" + action);
            if (action == 0) {
                this.mHandler.removeMessages(0);
                this.mInstantTouchingState = true;
                this.mHandler.sendEmptyMessage(0);
            }
            this.mRenderLock.lock();
            try {
                boolean dispatchTouchEvent;
                if (this.mContentView != null) {
                    dispatchTouchEvent = this.mContentView.dispatchTouchEvent(event);
                } else {
                    dispatchTouchEvent = false;
                }
                if (action == 0 && dispatchTouchEvent) {
                    this.mInDownState = true;
                    this.mHandler.removeMessages(0);
                }
                this.mRenderLock.unlock();
                TraceController.traceEnd();
                TraceController.traceEnd();
                return dispatchTouchEvent;
            } catch (Throwable th) {
                this.mRenderLock.unlock();
                TraceController.traceEnd();
                TraceController.traceEnd();
            }
        }
    }

    public boolean getInstantTouchingState() {
        if (this.mInstantTouchingState) {
            this.mGetLastTimeStamp = System.currentTimeMillis();
            return this.mInstantTouchingState;
        }
        return System.currentTimeMillis() - this.mGetLastTimeStamp < 34;
    }

    public boolean isDoingStateTransitionAnimation() {
        GLView contentView = this.mContentView;
        return contentView != null ? contentView.isDoingStateTransitionAnimation() : false;
    }

    public void lockRenderThread() {
        this.mRenderLock.lock();
    }

    public void unlockRenderThread() {
        this.mRenderLock.unlock();
    }

    public void onPause() {
        unfreeze();
        super.onPause();
        this.mLogger.onPause();
    }

    public int getDisplayRotation() {
        return this.mDisplayRotation;
    }

    public int getCompensation() {
        return this.mCompensation;
    }

    public Matrix getCompensationMatrix() {
        return this.mCompensationMatrix;
    }

    @TargetApi(16)
    public void setLightsOutMode(boolean enabled) {
        if (ApiHelper.HAS_SET_SYSTEM_UI_VISIBILITY) {
            int flags = getSystemUiVisibility();
            if (enabled) {
                flags |= 1;
                if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
                    flags |= SmsCheckResult.ESCT_260;
                }
            } else {
                flags &= -6;
            }
            setSystemUiVisibility(flags);
        }
    }

    @TargetApi(16)
    public void requestFullScreenLayout() {
        if (ApiHelper.HAS_SET_SYSTEM_UI_VISIBILITY && ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
            setSystemUiVisibility(1280);
        }
    }

    protected void onDetachedFromWindow() {
        unfreeze();
        super.onDetachedFromWindow();
        if (this.mDetachListener != null) {
            this.mDetachListener.onDetachFromWindow();
        }
        GLCanvas canvas = this.mCanvas;
        if (canvas != null) {
            canvas.invalidateAllTextures();
        }
    }

    public void freeze() {
        this.mRenderLock.lock();
        try {
            this.mFreeze = true;
        } finally {
            this.mRenderLock.unlock();
        }
    }

    public void unfreeze() {
        this.mRenderLock.lock();
        try {
            this.mFreeze = false;
            this.mFreezeCondition.signalAll();
        } finally {
            this.mRenderLock.unlock();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        unfreeze();
        super.surfaceChanged(holder, format, w, h);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        unfreeze();
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        unfreeze();
        super.surfaceDestroyed(holder);
    }

    protected void finalize() throws Throwable {
        try {
            unfreeze();
        } finally {
            super.finalize();
        }
    }

    public void sendAccessibilityEvent(int eventType) {
        if (GalleryUtils.isCVAAMode()) {
            switch (eventType) {
                case 32768:
                    return;
            }
        }
        super.sendAccessibilityEvent(eventType);
    }

    public GLCanvas getCanvas() {
        return this.mCanvas;
    }
}
