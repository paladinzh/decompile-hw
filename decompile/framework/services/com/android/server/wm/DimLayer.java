package com.android.server.wm;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import java.io.PrintWriter;

public class DimLayer {
    private static final String TAG = "WindowManager";
    private static final int TYPE_RIGHT = 2;
    private float mAlpha = 0.0f;
    private final Rect mBounds = new Rect();
    boolean mBoundsAdjustedForSingleHand = false;
    private boolean mDestroyed = false;
    private SurfaceControl mDimSurface;
    private final int mDisplayId;
    private long mDuration;
    private final Rect mLastBounds = new Rect();
    private int mLayer = -1;
    private final String mName;
    Rect mSavedBounds = new Rect();
    private final WindowManagerService mService;
    protected boolean mShowing = false;
    private float mStartAlpha = 0.0f;
    private long mStartTime;
    private float mTargetAlpha = 0.0f;
    Rect mTmpRect = new Rect();
    private final DimLayerUser mUser;

    interface DimLayerUser {
        boolean dimFullscreen();

        void getDimBounds(Rect rect);

        DisplayInfo getDisplayInfo();

        String toShortString();
    }

    DimLayer(WindowManagerService service, DimLayerUser user, int displayId, String name) {
        this.mUser = user;
        this.mDisplayId = displayId;
        this.mService = service;
        this.mName = name;
    }

    private void constructSurface(WindowManagerService service) {
        SurfaceControl.openTransaction();
        try {
            this.mDimSurface = new SurfaceControl(service.mFxSession, this.mName, 16, 16, -1, 131076);
            this.mDimSurface.setLayerStack(this.mDisplayId);
            adjustBounds();
            adjustAlpha(this.mAlpha);
            adjustLayer(this.mLayer);
        } catch (Exception e) {
            Slog.e("WindowManager", "Exception creating Dim surface", e);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    boolean isDimming() {
        return this.mTargetAlpha != 0.0f;
    }

    boolean isAnimating() {
        return this.mTargetAlpha != this.mAlpha;
    }

    float getTargetAlpha() {
        return this.mTargetAlpha;
    }

    void setLayer(int layer) {
        if (this.mLayer != layer) {
            this.mLayer = layer;
            adjustLayer(layer);
        }
    }

    private void adjustLayer(int layer) {
        if (this.mDimSurface != null) {
            this.mDimSurface.setLayer(layer);
        }
    }

    int getLayer() {
        return this.mLayer;
    }

    private void setAlpha(float alpha) {
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
            adjustAlpha(alpha);
        }
    }

    private void adjustAlpha(float alpha) {
        try {
            if (this.mDimSurface != null) {
                this.mDimSurface.setAlpha(alpha);
            }
            if (alpha == 0.0f && this.mShowing) {
                if (this.mDimSurface != null) {
                    this.mDimSurface.hide();
                    this.mShowing = false;
                }
            } else if (alpha > 0.0f && !this.mShowing && this.mDimSurface != null) {
                this.mDimSurface.show();
                this.mShowing = true;
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failure setting alpha immediately", e);
        }
    }

    private void adjustBounds() {
        if (this.mUser.dimFullscreen()) {
            getBoundsForFullscreen(this.mBounds);
        }
        if (this.mDimSurface != null) {
            this.mDimSurface.setPosition((float) this.mBounds.left, (float) this.mBounds.top);
            this.mDimSurface.setSize(this.mBounds.width(), this.mBounds.height());
        }
        this.mLastBounds.set(this.mBounds);
    }

    private void getBoundsForFullscreen(Rect outBounds) {
        DisplayInfo info = this.mUser.getDisplayInfo();
        int dw = (int) (((double) info.logicalWidth) * 1.5d);
        int dh = (int) (((double) info.logicalHeight) * 1.5d);
        float xPos = (float) ((dw * -1) / 6);
        float yPos = (float) ((dh * -1) / 6);
        outBounds.set((int) xPos, (int) yPos, ((int) xPos) + dw, ((int) yPos) + dh);
    }

    void setBoundsForFullscreen() {
        getBoundsForFullscreen(this.mBounds);
        setBounds(this.mBounds);
    }

    void setBounds(Rect bounds) {
        this.mTmpRect.set(bounds);
        int lazyMode = this.mService.getLazyMode();
        if (this.mService.mRotation == 0) {
            if (lazyMode != 0) {
                this.mBoundsAdjustedForSingleHand = true;
                this.mSavedBounds.set(bounds);
                getBoundsForSingleHand(this.mTmpRect, lazyMode);
                this.mBounds.set(this.mTmpRect);
            } else {
                this.mBounds.set(bounds);
            }
            if (this.mBoundsAdjustedForSingleHand && lazyMode == 0) {
                this.mBoundsAdjustedForSingleHand = false;
                bounds.set(this.mSavedBounds);
            }
        } else {
            this.mBounds.set(bounds);
        }
        if (isDimming() && !this.mLastBounds.equals(bounds)) {
            try {
                SurfaceControl.openTransaction();
                adjustBounds();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Failure setting size", e);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    private boolean durationEndsEarlier(long duration) {
        return SystemClock.uptimeMillis() + duration < this.mStartTime + this.mDuration;
    }

    void show() {
        if (isAnimating()) {
            show(this.mLayer, this.mTargetAlpha, 0);
        }
    }

    void show(int layer, float alpha, long duration) {
        if (this.mDestroyed) {
            Slog.e(TAG, "show: no Surface");
            this.mAlpha = 0.0f;
            this.mTargetAlpha = 0.0f;
            return;
        }
        if (this.mDimSurface == null) {
            constructSurface(this.mService);
        }
        if (!this.mLastBounds.equals(this.mBounds)) {
            adjustBounds();
        }
        setLayer(layer);
        long curTime = SystemClock.uptimeMillis();
        boolean animating = isAnimating();
        if ((animating && (this.mTargetAlpha != alpha || durationEndsEarlier(duration))) || !(animating || this.mAlpha == alpha)) {
            if (duration <= 0) {
                setAlpha(alpha);
            } else {
                this.mStartAlpha = this.mAlpha;
                this.mStartTime = curTime;
                this.mDuration = duration;
            }
        }
        this.mTargetAlpha = alpha;
    }

    void hide() {
        if (this.mShowing) {
            hide(0);
        }
    }

    void hide(long duration) {
        if (!this.mShowing) {
            return;
        }
        if (this.mTargetAlpha != 0.0f || durationEndsEarlier(duration)) {
            show(this.mLayer, 0.0f, duration);
        }
    }

    boolean stepAnimation() {
        if (this.mDestroyed) {
            Slog.e(TAG, "stepAnimation: surface destroyed");
            this.mAlpha = 0.0f;
            this.mTargetAlpha = 0.0f;
            return false;
        }
        if (isAnimating()) {
            float alphaDelta = this.mTargetAlpha - this.mStartAlpha;
            float alpha = this.mStartAlpha + ((((float) (SystemClock.uptimeMillis() - this.mStartTime)) * alphaDelta) / ((float) this.mDuration));
            if (alphaDelta <= 0.0f || alpha <= this.mTargetAlpha) {
                if (alphaDelta < 0.0f && alpha < this.mTargetAlpha) {
                }
                setAlpha(alpha);
            }
            alpha = this.mTargetAlpha;
            setAlpha(alpha);
        }
        return isAnimating();
    }

    void destroySurface() {
        if (this.mDimSurface != null) {
            this.mDimSurface.destroy();
            this.mDimSurface = null;
        }
        this.mDestroyed = true;
    }

    public void adjustBoundsForSingleHand() {
        SurfaceControl.openTransaction();
        setBounds(this.mBounds);
        show(this.mLayer, this.mService.mInputMethodWindow != null ? this.mService.mInputMethodWindow.isClosing() : true ? 0.0f : this.mTargetAlpha, 0);
        SurfaceControl.closeTransaction();
    }

    private void getBoundsForSingleHand(Rect outBounds, int lazyMode) {
        DisplayInfo info = this.mUser.getDisplayInfo();
        outBounds.scale(0.75f);
        float xPos = (float) (outBounds.left + (lazyMode == 2 ? info.logicalWidth / 4 : 0));
        float yPos = (float) (outBounds.top + (info.logicalHeight / 4));
        outBounds.set((int) xPos, (int) yPos, (int) (((float) outBounds.width()) + xPos), (int) (((float) outBounds.height()) + yPos));
    }

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mDimSurface=");
        pw.print(this.mDimSurface);
        pw.print(" mLayer=");
        pw.print(this.mLayer);
        pw.print(" mAlpha=");
        pw.println(this.mAlpha);
        pw.print(prefix);
        pw.print("mLastBounds=");
        pw.print(this.mLastBounds.toShortString());
        pw.print(" mBounds=");
        pw.println(this.mBounds.toShortString());
        pw.print(prefix);
        pw.print("Last animation: ");
        pw.print(" mDuration=");
        pw.print(this.mDuration);
        pw.print(" mStartTime=");
        pw.print(this.mStartTime);
        pw.print(" curTime=");
        pw.println(SystemClock.uptimeMillis());
        pw.print(prefix);
        pw.print(" mStartAlpha=");
        pw.print(this.mStartAlpha);
        pw.print(" mTargetAlpha=");
        pw.println(this.mTargetAlpha);
    }
}
