package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Flog;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.MagnificationSpec;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.android.server.job.controllers.JobStatus;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;

public class WindowStateAnimator {
    static final int COMMIT_DRAW_PENDING = 2;
    static final int DRAW_PENDING = 1;
    static final int HAS_DRAWN = 4;
    static final int NO_SURFACE = 0;
    static final long PENDING_TRANSACTION_FINISH_WAIT_TIME = 100;
    static final int READY_TO_SHOW = 3;
    static final int STACK_CLIP_AFTER_ANIM = 0;
    static final int STACK_CLIP_BEFORE_ANIM = 1;
    static final int STACK_CLIP_NONE = 2;
    static final String TAG = "WindowManager";
    static final int WINDOW_FREEZE_LAYER = 2000000;
    float mAlpha = 0.0f;
    private int mAnimDx;
    private int mAnimDy;
    int mAnimLayer;
    private boolean mAnimateMove = false;
    boolean mAnimating;
    Animation mAnimation;
    boolean mAnimationIsEntrance;
    private boolean mAnimationStartDelayed;
    long mAnimationStartTime;
    final WindowAnimator mAnimator;
    AppWindowAnimator mAppAnimator;
    final WindowStateAnimator mAttachedWinAnimator;
    int mAttrType;
    private BlurParams mBlurParams;
    Rect mClipRect = new Rect();
    final Context mContext;
    long mDeferTransactionTime = -1;
    long mDeferTransactionUntilFrame = -1;
    private boolean mDestroyPreservedSurfaceUponRedraw;
    int mDrawState;
    float mDsDx = 1.0f;
    float mDsDy = 0.0f;
    float mDtDx = 0.0f;
    float mDtDy = 1.0f;
    boolean mEnterAnimationPending;
    boolean mEnteringAnimation;
    float mExtraHScale = 1.0f;
    float mExtraVScale = 1.0f;
    boolean mForceScaleUntilResize;
    float mHScale = 0.0f;
    boolean mHasClipRect;
    boolean mHasLocalTransformation;
    boolean mHasTransformation;
    boolean mHaveMatrix;
    final boolean mIsWallpaper;
    boolean mKeyguardGoingAwayAnimation;
    boolean mKeyguardGoingAwayWithWallpaper;
    float mLastAlpha = 0.0f;
    long mLastAnimationTime;
    Rect mLastClipRect = new Rect();
    float mLastDsDx = 1.0f;
    float mLastDsDy = 0.0f;
    float mLastDtDx = 0.0f;
    float mLastDtDy = 1.0f;
    Rect mLastFinalClipRect = new Rect();
    boolean mLastHidden;
    int mLastLayer;
    private final Rect mLastSystemDecorRect = new Rect();
    boolean mLazyIsEntering;
    boolean mLazyIsExiting;
    boolean mLocalAnimating;
    private WindowSurfaceController mPendingDestroySurface;
    final WindowManagerPolicy mPolicy;
    boolean mReportSurfaceResized;
    final WindowManagerService mService;
    final Session mSession;
    float mShownAlpha = 0.0f;
    int mStackClip = 1;
    WindowSurfaceController mSurfaceController;
    boolean mSurfaceDestroyDeferred;
    int mSurfaceFormat;
    boolean mSurfaceResized;
    private final Rect mSystemDecorRect = new Rect();
    Rect mTmpClipRect = new Rect();
    Rect mTmpFinalClipRect = new Rect();
    private final Rect mTmpSize = new Rect();
    Rect mTmpStackBounds = new Rect();
    final Transformation mTransformation = new Transformation();
    float mVScale = 0.0f;
    final WallpaperController mWallpaperControllerLocked;
    boolean mWasAnimating;
    final WindowState mWin;

    private class BlurParams {
        private static final int CHANGE_ALPHA = 4;
        private static final int CHANGE_BLANK = 16;
        private static final int CHANGE_MASK = 31;
        private static final int CHANGE_NONE = 0;
        private static final int CHANGE_RADIUS = 1;
        private static final int CHANGE_REGION = 8;
        private static final int CHANGE_ROUND = 2;
        float alpha;
        Rect blank;
        private int changes = 0;
        int radius;
        Region region;
        int rx;
        int ry;
        int surfaceHashCode;

        public BlurParams(int hashcode) {
            this.surfaceHashCode = hashcode;
        }

        public BlurParams(LayoutParams lp, int hashcode) {
            this.surfaceHashCode = hashcode;
            set(lp);
        }

        public boolean updateHashCode(int hashcode) {
            if (this.surfaceHashCode == hashcode) {
                return false;
            }
            this.surfaceHashCode = hashcode;
            return true;
        }

        public void set(LayoutParams lp) {
            setRadius(lp.blurRadius);
            setRound(lp.blurRoundx, lp.blurRoundy);
            setAlpha(lp.blurAlpha);
            setRegion(lp.blurRegion);
            setBlank(lp.blurBlankLeft, lp.blurBlankTop, lp.blurBlankRight, lp.blurBlankBottom);
        }

        public void setRadius(int radius) {
            int newRadius = Math.max(0, Math.min(radius, 100));
            if (this.radius != newRadius) {
                this.radius = newRadius;
                this.changes |= 1;
            }
        }

        public void setRound(int rx, int ry) {
            if (this.rx != rx || this.ry != ry) {
                this.rx = rx;
                this.ry = ry;
                this.changes |= 2;
            }
        }

        public void setAlpha(float alpha) {
            float newAlpha = Math.max(0.0f, Math.min(alpha, 1.0f));
            if (this.alpha != newAlpha) {
                this.alpha = newAlpha;
                this.changes |= 4;
            }
        }

        public void setRegion(Region region) {
            Region newRegion = region != null ? region : new Region();
            if (!newRegion.equals(this.region)) {
                this.region = newRegion;
                this.changes |= 8;
            }
        }

        public void setBlank(int l, int t, int r, int b) {
            Rect newBlank = new Rect(l, t, r, b);
            if (!newBlank.equals(this.blank)) {
                this.blank = newBlank;
                this.changes |= 16;
            }
        }

        public void refresh(WindowSurfaceController srufacecontroller, boolean force) {
            if (force || (this.changes & 1) == 1) {
                srufacecontroller.setBlurRadius(this.radius);
            }
            if (force || (this.changes & 2) == 2) {
                srufacecontroller.setBlurRound(this.rx, this.ry);
            }
            if (force || (this.changes & 4) == 4) {
                srufacecontroller.setBlurAlpha(this.alpha);
            }
            if (force || (this.changes & 8) == 8) {
                srufacecontroller.setBlurRegion(this.region);
            }
            if (force || (this.changes & 16) == 16) {
                srufacecontroller.setBlurBlank(this.blank);
            }
            this.changes = 0;
        }
    }

    String drawStateToString() {
        switch (this.mDrawState) {
            case 0:
                return "NO_SURFACE";
            case 1:
                return "DRAW_PENDING";
            case 2:
                return "COMMIT_DRAW_PENDING";
            case 3:
                return "READY_TO_SHOW";
            case 4:
                return "HAS_DRAWN";
            default:
                return Integer.toString(this.mDrawState);
        }
    }

    WindowStateAnimator(WindowState win) {
        WindowStateAnimator windowStateAnimator;
        AppWindowAnimator appWindowAnimator = null;
        WindowManagerService service = win.mService;
        this.mService = service;
        this.mAnimator = service.mAnimator;
        this.mPolicy = service.mPolicy;
        this.mContext = service.mContext;
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            this.mAnimDx = displayInfo.appWidth;
            this.mAnimDy = displayInfo.appHeight;
        } else {
            Slog.w(TAG, "WindowStateAnimator ctor: Display has been removed");
        }
        this.mWin = win;
        if (win.mAttachedWindow == null) {
            windowStateAnimator = null;
        } else {
            windowStateAnimator = win.mAttachedWindow.mWinAnimator;
        }
        this.mAttachedWinAnimator = windowStateAnimator;
        if (win.mAppToken != null) {
            appWindowAnimator = win.mAppToken.mAppAnimator;
        }
        this.mAppAnimator = appWindowAnimator;
        this.mSession = win.mSession;
        this.mAttrType = win.mAttrs.type;
        this.mIsWallpaper = win.mIsWallpaper;
        this.mWallpaperControllerLocked = this.mService.mWallpaperControllerLocked;
    }

    public void setAnimation(Animation anim, long startTime, int stackClip) {
        int i = 0;
        this.mAnimating = false;
        this.mLocalAnimating = false;
        this.mAnimation = anim;
        this.mAnimation.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        this.mAnimation.scaleCurrentDuration(this.mService.getWindowAnimationScaleLocked());
        this.mTransformation.clear();
        Transformation transformation = this.mTransformation;
        if (!this.mLastHidden) {
            i = 1;
        }
        transformation.setAlpha((float) i);
        this.mHasLocalTransformation = true;
        this.mAnimationStartTime = startTime;
        this.mStackClip = stackClip;
    }

    public void setAnimation(Animation anim, int stackClip) {
        setAnimation(anim, -1, stackClip);
    }

    public void setAnimation(Animation anim) {
        setAnimation(anim, -1, 0);
    }

    public void clearAnimation() {
        if (this.mAnimation != null) {
            this.mAnimating = true;
            this.mLocalAnimating = false;
            this.mAnimation.cancel();
            this.mAnimation = null;
            this.mKeyguardGoingAwayAnimation = false;
            this.mKeyguardGoingAwayWithWallpaper = false;
            this.mStackClip = 1;
        }
    }

    boolean isAnimationSet() {
        if (this.mAnimation == null && (this.mAttachedWinAnimator == null || this.mAttachedWinAnimator.mAnimation == null)) {
            return this.mAppAnimator != null ? this.mAppAnimator.isAnimating() : false;
        } else {
            return true;
        }
    }

    boolean isAnimationStarting() {
        return isAnimationSet() && !this.mAnimating;
    }

    boolean isDummyAnimation() {
        if (this.mAppAnimator == null || this.mAppAnimator.animation != AppWindowAnimator.sDummyAnimation) {
            return false;
        }
        return true;
    }

    boolean isWindowAnimationSet() {
        return this.mAnimation != null;
    }

    boolean isWaitingForOpening() {
        if (this.mService.mAppTransition.isTransitionSet() && isDummyAnimation()) {
            return this.mService.mOpeningApps.contains(this.mWin.mAppToken);
        }
        return false;
    }

    void cancelExitAnimationForNextAnimationLocked() {
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
            this.mAnimation = null;
            this.mLocalAnimating = false;
            this.mWin.destroyOrSaveSurface();
        }
    }

    private boolean stepAnimation(long currentTime) {
        if (this.mAnimation == null || !this.mLocalAnimating) {
            return false;
        }
        currentTime = getAnimationFrameTime(this.mAnimation, currentTime);
        this.mTransformation.clear();
        boolean more = this.mAnimation.getTransformation(currentTime, this.mTransformation);
        if (this.mAnimationStartDelayed && this.mAnimationIsEntrance) {
            this.mTransformation.setAlpha(0.0f);
        }
        return more;
    }

    boolean stepAnimationLocked(long currentTime) {
        this.mWasAnimating = this.mAnimating;
        DisplayContent displayContent = this.mWin.getDisplayContent();
        if (displayContent != null && this.mService.okToDisplay()) {
            if (this.mWin.isDrawnLw() && this.mAnimation != null) {
                this.mHasTransformation = true;
                this.mHasLocalTransformation = true;
                if (!this.mLocalAnimating) {
                    long j;
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    if (this.mAnimateMove) {
                        this.mAnimateMove = false;
                        this.mAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), this.mAnimDx, this.mAnimDy);
                    } else {
                        this.mAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), displayInfo.appWidth, displayInfo.appHeight);
                    }
                    this.mAnimDx = displayInfo.appWidth;
                    this.mAnimDy = displayInfo.appHeight;
                    Animation animation = this.mAnimation;
                    if (this.mAnimationStartTime != -1) {
                        j = this.mAnimationStartTime;
                    } else {
                        j = currentTime;
                    }
                    animation.setStartTime(j);
                    this.mLocalAnimating = true;
                    this.mAnimating = true;
                }
                if (this.mAnimation != null && this.mLocalAnimating) {
                    this.mLastAnimationTime = currentTime;
                    if (stepAnimation(currentTime)) {
                        return true;
                    }
                }
            }
            this.mHasLocalTransformation = false;
            if ((!this.mLocalAnimating || this.mAnimationIsEntrance) && this.mAppAnimator != null && this.mAppAnimator.animation != null) {
                this.mAnimating = true;
                this.mHasTransformation = true;
                this.mTransformation.clear();
                return false;
            } else if (this.mHasTransformation) {
                this.mAnimating = true;
            } else if (isAnimationSet()) {
                this.mAnimating = true;
            }
        } else if (this.mAnimation != null) {
            this.mAnimating = true;
        }
        if (!this.mAnimating && !this.mLocalAnimating) {
            return false;
        }
        this.mAnimating = false;
        this.mKeyguardGoingAwayAnimation = false;
        this.mKeyguardGoingAwayWithWallpaper = false;
        this.mLocalAnimating = false;
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
            this.mAnimation = null;
        }
        if (this.mAnimator.mWindowDetachedWallpaper == this.mWin) {
            this.mAnimator.mWindowDetachedWallpaper = null;
        }
        this.mAnimLayer = this.mWin.mLayer + this.mService.mLayersController.getSpecialWindowAnimLayerAdjustment(this.mWin);
        this.mHasTransformation = false;
        this.mHasLocalTransformation = false;
        this.mStackClip = 1;
        this.mWin.checkPolicyVisibilityChange();
        this.mTransformation.clear();
        if (this.mDrawState == 4 && this.mWin.mAttrs.type == 3 && this.mWin.mAppToken != null && this.mWin.mAppToken.firstWindowDrawn && this.mWin.mAppToken.startingData != null) {
            this.mService.mFinishedStarting.add(this.mWin.mAppToken);
            this.mService.mH.sendEmptyMessage(7);
        } else if (this.mAttrType == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && this.mWin.mPolicyVisibility && displayContent != null) {
            displayContent.layoutNeeded = true;
        }
        finishExit();
        this.mAnimator.setPendingLayoutChanges(this.mWin.getDisplayId(), 8);
        if (this.mWin.mAppToken != null) {
            this.mWin.mAppToken.updateReportedVisibilityLocked();
        }
        return false;
    }

    void finishExit() {
        if (!this.mWin.mChildWindows.isEmpty()) {
            WindowList childWindows = new WindowList(this.mWin.mChildWindows);
            for (int i = childWindows.size() - 1; i >= 0; i--) {
                ((WindowState) childWindows.get(i)).mWinAnimator.finishExit();
            }
        }
        if (this.mEnteringAnimation) {
            this.mEnteringAnimation = false;
            this.mService.requestTraversal();
            if (this.mWin.mAppToken == null) {
                try {
                    this.mWin.mClient.dispatchWindowShown();
                } catch (RemoteException e) {
                }
            }
        }
        if (!(isWindowAnimationSet() || this.mService.mAccessibilityController == null || this.mWin.getDisplayId() != 0)) {
            this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
        }
        if (this.mWin.mAnimatingExit && !isWindowAnimationSet()) {
            this.mWin.mDestroying = true;
            boolean hasSurface = hasSurface();
            if (hasSurface) {
                hide("finishExit");
            }
            if (this.mWin.mAppToken != null) {
                this.mWin.mAppToken.destroySurfaces();
            } else {
                if (hasSurface) {
                    this.mService.mDestroySurface.add(this.mWin);
                }
                if (this.mWin.mRemoveOnExit) {
                    this.mService.mPendingRemove.add(this.mWin);
                    this.mWin.mRemoveOnExit = false;
                }
            }
            this.mWin.mAnimatingExit = false;
            this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
        }
    }

    void hide(String reason) {
        if (!this.mLastHidden) {
            this.mLastHidden = true;
            if (this.mSurfaceController != null) {
                this.mSurfaceController.hideInTransaction(reason);
            }
        }
    }

    boolean finishDrawingLocked() {
        boolean layoutNeeded;
        if (this.mWin.mAttrs.type == 3) {
            layoutNeeded = this.mWin.clearAnimatingWithSavedSurface();
        } else {
            layoutNeeded = this.mWin.clearAnimatingWithSavedSurface();
        }
        if (this.mDrawState != 1) {
            return layoutNeeded;
        }
        this.mDrawState = 2;
        return true;
    }

    boolean commitFinishDrawingLocked() {
        if (this.mDrawState != 2 && this.mDrawState != 3) {
            return false;
        }
        if (this.mDrawState == 2) {
            Flog.i(307, "commitFinishDrawingLocked: mDrawState=READY_TO_SHOW " + this.mSurfaceController);
        }
        this.mDrawState = 3;
        boolean result = false;
        AppWindowToken atoken = this.mWin.mAppToken;
        if (atoken == null || atoken.allDrawn || this.mWin.mAttrs.type == 3) {
            result = performShowLocked();
        }
        return result;
    }

    void preserveSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            this.mSurfaceDestroyDeferred = false;
            destroySurfaceLocked();
            this.mSurfaceDestroyDeferred = true;
            return;
        }
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setLayer(this.mAnimLayer + 1);
        }
        this.mDestroyPreservedSurfaceUponRedraw = true;
        this.mSurfaceDestroyDeferred = true;
        destroySurfaceLocked();
    }

    void destroyPreservedSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            destroyDeferredSurfaceLocked();
            this.mDestroyPreservedSurfaceUponRedraw = false;
        }
    }

    void markPreservedSurfaceForDestroy() {
        if (this.mDestroyPreservedSurfaceUponRedraw && !this.mService.mDestroyPreservedSurface.contains(this.mWin)) {
            this.mService.mDestroyPreservedSurface.add(this.mWin);
        }
    }

    WindowSurfaceController createSurfaceLocked() {
        WindowState w = this.mWin;
        if (w.hasSavedSurface()) {
            w.restoreSavedSurface();
            return this.mSurfaceController;
        } else if (this.mSurfaceController != null) {
            return this.mSurfaceController;
        } else {
            w.setHasSurface(false);
            this.mDrawState = 1;
            if (w.mAppToken != null) {
                if (w.mAppToken.mAppAnimator.animation == null) {
                    w.mAppToken.clearAllDrawn();
                } else {
                    w.mAppToken.deferClearAllDrawn = true;
                }
            }
            this.mService.makeWindowFreezingScreenIfNeededLocked(w);
            int flags = 4;
            LayoutParams attrs = w.mAttrs;
            if (this.mService.isSecureLocked(w)) {
                flags = 132;
            }
            this.mTmpSize.set(w.mFrame.left + w.mXOffset, w.mFrame.top + w.mYOffset, 0, 0);
            calculateSurfaceBounds(w, attrs);
            int width = this.mTmpSize.width();
            int height = this.mTmpSize.height();
            this.mLastSystemDecorRect.set(0, 0, 0, 0);
            this.mHasClipRect = false;
            this.mClipRect.set(0, 0, 0, 0);
            this.mLastClipRect.set(0, 0, 0, 0);
            try {
                int format = (attrs.flags & 16777216) != 0 ? -3 : attrs.format;
                if (!PixelFormat.formatHasAlpha(attrs.format) && attrs.surfaceInsets.left == 0 && attrs.surfaceInsets.top == 0 && attrs.surfaceInsets.right == 0 && attrs.surfaceInsets.bottom == 0 && !w.isDragResizing()) {
                    flags |= 1024;
                }
                if ((attrs.flags & 4) != 0) {
                    flags |= DumpState.DUMP_INSTALLS;
                }
                this.mSurfaceController = new WindowSurfaceController(this.mSession.mSurfaceSession, attrs.getTitle().toString(), width, height, format, flags, this);
                w.setHasSurface(true);
                this.mSurfaceController.setPositionAndLayer((float) this.mTmpSize.left, (float) this.mTmpSize.top, w.getDisplayContent().getDisplay().getLayerStack(), this.mAnimLayer);
                this.mLastHidden = true;
                return this.mSurfaceController;
            } catch (OutOfResourcesException e) {
                Slog.w(TAG, "OutOfResourcesException creating surface");
                this.mService.reclaimSomeSurfaceMemoryLocked(this, "create", true);
                this.mDrawState = 0;
                return null;
            } catch (Exception e2) {
                Slog.e(TAG, "Exception creating surface", e2);
                this.mDrawState = 0;
                return null;
            }
        }
    }

    private void calculateSurfaceBounds(WindowState w, LayoutParams attrs) {
        if ((attrs.flags & DumpState.DUMP_KEYSETS) != 0) {
            this.mTmpSize.right = this.mTmpSize.left + w.mRequestedWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + w.mRequestedHeight;
        } else if (w.isDragResizing()) {
            if (w.getResizeMode() == 0) {
                this.mTmpSize.left = 0;
                this.mTmpSize.top = 0;
            }
            DisplayInfo displayInfo = w.getDisplayInfo();
            this.mTmpSize.right = this.mTmpSize.left + displayInfo.logicalWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + displayInfo.logicalHeight;
        } else {
            this.mTmpSize.right = this.mTmpSize.left + w.mCompatFrame.width();
            this.mTmpSize.bottom = this.mTmpSize.top + w.mCompatFrame.height();
        }
        if (this.mTmpSize.width() < 1) {
            this.mTmpSize.right = this.mTmpSize.left + 1;
        }
        if (this.mTmpSize.height() < 1) {
            this.mTmpSize.bottom = this.mTmpSize.top + 1;
        }
        Rect rect = this.mTmpSize;
        rect.left -= attrs.surfaceInsets.left;
        rect = this.mTmpSize;
        rect.top -= attrs.surfaceInsets.top;
        rect = this.mTmpSize;
        rect.right += attrs.surfaceInsets.right;
        rect = this.mTmpSize;
        rect.bottom += attrs.surfaceInsets.bottom;
    }

    boolean hasSurface() {
        if (this.mWin.hasSavedSurface() || this.mSurfaceController == null) {
            return false;
        }
        return this.mSurfaceController.hasSurface();
    }

    void destroySurfaceLocked() {
        AppWindowToken wtoken = this.mWin.mAppToken;
        if (wtoken != null && this.mWin == wtoken.startingWindow) {
            wtoken.startingDisplayed = false;
        }
        this.mWin.clearHasSavedSurface();
        if (this.mSurfaceController != null) {
            int i = this.mWin.mChildWindows.size();
            while (!this.mDestroyPreservedSurfaceUponRedraw && i > 0) {
                i--;
                ((WindowState) this.mWin.mChildWindows.get(i)).mAttachedHidden = true;
            }
            try {
                if (!this.mSurfaceDestroyDeferred) {
                    destroySurface();
                } else if (!(this.mSurfaceController == null || this.mPendingDestroySurface == this.mSurfaceController)) {
                    if (this.mPendingDestroySurface != null) {
                        this.mPendingDestroySurface.destroyInTransaction();
                    }
                    this.mPendingDestroySurface = this.mSurfaceController;
                }
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e.toString());
            }
            this.mWin.setHasSurface(false);
            if (this.mSurfaceController != null) {
                this.mSurfaceController.setShown(false);
            }
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
    }

    void destroyDeferredSurfaceLocked() {
        try {
            if (this.mPendingDestroySurface != null) {
                this.mPendingDestroySurface.destroyInTransaction();
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mPendingDestroySurface + " session " + this.mSession + ": " + e.toString());
        }
        this.mSurfaceDestroyDeferred = false;
        this.mPendingDestroySurface = null;
    }

    void computeShownFrameLocked() {
        boolean selfTransformation = this.mHasLocalTransformation;
        Transformation transformation = (this.mAttachedWinAnimator == null || !this.mAttachedWinAnimator.mHasLocalTransformation) ? null : this.mAttachedWinAnimator.mTransformation;
        Transformation transformation2 = (this.mAppAnimator == null || !this.mAppAnimator.hasTransformation) ? null : this.mAppAnimator.transformation;
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        if (this.mIsWallpaper && wallpaperTarget != null && this.mService.mAnimateWallpaperWithTarget) {
            WindowStateAnimator wallpaperAnimator = wallpaperTarget.mWinAnimator;
            if (!(!wallpaperAnimator.mHasLocalTransformation || wallpaperAnimator.mAnimation == null || wallpaperAnimator.mAnimation.getDetachWallpaper())) {
                transformation = wallpaperAnimator.mTransformation;
            }
            AppWindowAnimator wpAppAnimator = wallpaperTarget.mAppToken == null ? null : wallpaperTarget.mAppToken.mAppAnimator;
            if (!(wpAppAnimator == null || !wpAppAnimator.hasTransformation || wpAppAnimator.animation == null || wpAppAnimator.animation.getDetachWallpaper())) {
                transformation2 = wpAppAnimator.transformation;
            }
        }
        int displayId = this.mWin.getDisplayId();
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        boolean isAnimating = screenRotationAnimation != null ? screenRotationAnimation.isAnimating() : false;
        this.mHasClipRect = false;
        Rect frame;
        float[] tmpFloats;
        Matrix tmpMatrix;
        MagnificationSpec spec;
        if (selfTransformation || transformation != null || transformation2 != null || isAnimating || this.mHScale != this.mWin.mHScale || this.mVScale != this.mWin.mVScale) {
            frame = this.mWin.mFrame;
            tmpFloats = this.mService.mTmpFloats;
            tmpMatrix = this.mWin.mTmpMatrix;
            if (isAnimating && screenRotationAnimation.isRotating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale((2.0f / w) + 1.0f, (2.0f / h) + 1.0f, w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            if (selfTransformation) {
                tmpMatrix.postConcat(this.mTransformation.getMatrix());
            }
            if (transformation != null) {
                tmpMatrix.postConcat(transformation.getMatrix());
            }
            if (transformation2 != null) {
                tmpMatrix.postConcat(transformation2.getMatrix());
            }
            tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
            if (isAnimating) {
                tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
            }
            if (this.mService.mAccessibilityController != null && displayId == 0) {
                spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                if (!(spec == null || spec.isNop())) {
                    tmpMatrix.postScale(spec.scale, spec.scale);
                    tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                }
            }
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0];
            this.mDtDx = tmpFloats[3];
            this.mDsDy = tmpFloats[1];
            this.mDtDy = tmpFloats[4];
            float x = tmpFloats[2];
            float y = tmpFloats[5];
            this.mWin.mShownPosition.set((int) x, (int) y);
            this.mShownAlpha = this.mAlpha;
            if (!(this.mService.mLimitedAlphaCompositing && PixelFormat.formatHasAlpha(this.mWin.mAttrs.format) && (!this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy) || x != ((float) frame.left) || y != ((float) frame.top)))) {
                if (selfTransformation) {
                    this.mShownAlpha *= this.mTransformation.getAlpha();
                }
                if (transformation != null) {
                    this.mShownAlpha *= transformation.getAlpha();
                }
                if (transformation2 != null) {
                    this.mShownAlpha *= transformation2.getAlpha();
                    if (transformation2.hasClipRect()) {
                        this.mClipRect.set(transformation2.getClipRect());
                        this.mHScale = this.mWin.mHScale;
                        this.mVScale = this.mWin.mVScale;
                        this.mHasClipRect = true;
                        if (this.mWin.layoutInParentFrame()) {
                            this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                        }
                    }
                }
                if (isAnimating) {
                    this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
                }
            }
        } else if ((!this.mIsWallpaper || !this.mService.mWindowPlacerLocked.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            spec = null;
            if (this.mService.mAccessibilityController != null && displayId == 0) {
                spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
            }
            if (spec != null) {
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                if (!(spec == null || spec.isNop())) {
                    tmpMatrix.postScale(spec.scale, spec.scale);
                    tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                }
                tmpMatrix.getValues(tmpFloats);
                this.mHaveMatrix = true;
                this.mDsDx = tmpFloats[0];
                this.mDtDx = tmpFloats[3];
                this.mDsDy = tmpFloats[1];
                this.mDtDy = tmpFloats[4];
                this.mWin.mShownPosition.set((int) tmpFloats[2], (int) tmpFloats[5]);
                this.mShownAlpha = this.mAlpha;
            } else {
                this.mWin.mShownPosition.set(this.mWin.mFrame.left, this.mWin.mFrame.top);
                if (!(this.mWin.mXOffset == 0 && this.mWin.mYOffset == 0)) {
                    this.mWin.mShownPosition.offset(this.mWin.mXOffset, this.mWin.mYOffset);
                }
                this.mShownAlpha = this.mAlpha;
                this.mHaveMatrix = false;
                this.mDsDx = this.mWin.mGlobalScale;
                this.mDtDx = 0.0f;
                this.mDsDy = 0.0f;
                this.mDtDy = this.mWin.mGlobalScale;
            }
        }
    }

    private void calculateSystemDecorRect() {
        boolean cropToDecor = false;
        WindowState w = this.mWin;
        Rect decorRect = w.mDecorFrame;
        int width = w.mFrame.width();
        int height = w.mFrame.height();
        int left = w.mXOffset + w.mFrame.left;
        int top = w.mYOffset + w.mFrame.top;
        if (w.isDockedResizing() || (w.isChildWindow() && w.mAttachedWindow.isDockedResizing())) {
            DisplayInfo displayInfo = w.getDisplayContent().getDisplayInfo();
            this.mSystemDecorRect.set(0, 0, Math.max(width, displayInfo.logicalWidth), Math.max(height, displayInfo.logicalHeight));
        } else {
            this.mSystemDecorRect.set(0, 0, width, height);
        }
        if (!(w.inFreeformWorkspace() && w.isAnimatingLw())) {
            cropToDecor = true;
        }
        if (cropToDecor) {
            this.mSystemDecorRect.intersect(decorRect.left - left, decorRect.top - top, decorRect.right - left, decorRect.bottom - top);
        }
        if (w.mEnforceSizeCompat && w.mInvGlobalScale != 1.0f) {
            float scale = w.mInvGlobalScale;
            this.mSystemDecorRect.left = (int) ((((float) this.mSystemDecorRect.left) * scale) - TaskPositioner.RESIZING_HINT_ALPHA);
            this.mSystemDecorRect.top = (int) ((((float) this.mSystemDecorRect.top) * scale) - TaskPositioner.RESIZING_HINT_ALPHA);
            this.mSystemDecorRect.right = (int) ((((float) (this.mSystemDecorRect.right + 1)) * scale) - TaskPositioner.RESIZING_HINT_ALPHA);
            this.mSystemDecorRect.bottom = (int) ((((float) (this.mSystemDecorRect.bottom + 1)) * scale) - TaskPositioner.RESIZING_HINT_ALPHA);
        }
    }

    void calculateSurfaceWindowCrop(Rect clipRect, Rect finalClipRect) {
        WindowState w = this.mWin;
        DisplayContent displayContent = w.getDisplayContent();
        if (displayContent == null) {
            clipRect.setEmpty();
            finalClipRect.setEmpty();
            return;
        }
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (!w.isDefaultDisplay()) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
            this.mSystemDecorRect.intersect(-w.mCompatFrame.left, -w.mCompatFrame.top, displayInfo.logicalWidth - w.mCompatFrame.left, displayInfo.logicalHeight - w.mCompatFrame.top);
        } else if (w.mLayer >= this.mService.mSystemDecorLayer) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
        } else if (w.mDecorFrame.isEmpty()) {
            this.mSystemDecorRect.set(0, 0, w.mCompatFrame.width(), w.mCompatFrame.height());
        } else if (w.mAttrs.type == 2013 && this.mAnimator.isAnimating()) {
            this.mTmpClipRect.set(this.mSystemDecorRect);
            calculateSystemDecorRect();
            this.mSystemDecorRect.union(this.mTmpClipRect);
        } else {
            calculateSystemDecorRect();
        }
        boolean fullscreen = w.isFrameFullscreen(displayInfo);
        boolean isFreeformResizing = w.isDragResizing() && w.getResizeMode() == 0;
        Rect rect = (!this.mHasClipRect || fullscreen) ? this.mSystemDecorRect : this.mClipRect;
        clipRect.set(rect);
        if (isFreeformResizing && !w.isChildWindow()) {
            clipRect.offset(w.mShownPosition.x, w.mShownPosition.y);
        }
        LayoutParams attrs = w.mAttrs;
        clipRect.left -= attrs.surfaceInsets.left;
        clipRect.top -= attrs.surfaceInsets.top;
        clipRect.right += attrs.surfaceInsets.right;
        clipRect.bottom += attrs.surfaceInsets.bottom;
        if (this.mHasClipRect && fullscreen) {
            clipRect.intersect(this.mClipRect);
        }
        clipRect.offset(attrs.surfaceInsets.left, attrs.surfaceInsets.top);
        finalClipRect.setEmpty();
        adjustCropToStackBounds(w, clipRect, finalClipRect, isFreeformResizing);
        w.transformClipRectFromScreenToSurfaceSpace(clipRect);
        if (w.hasJustMovedInStack() && this.mLastClipRect.isEmpty() && !clipRect.isEmpty()) {
            clipRect.setEmpty();
        }
    }

    void updateSurfaceWindowCrop(Rect clipRect, Rect finalClipRect, boolean recoveringMemory) {
        if (clipRect == null) {
            this.mSurfaceController.clearCropInTransaction(recoveringMemory);
        } else if (!clipRect.equals(this.mLastClipRect)) {
            this.mLastClipRect.set(clipRect);
            this.mSurfaceController.setCropInTransaction(clipRect, recoveringMemory);
        }
        if (!finalClipRect.equals(this.mLastFinalClipRect)) {
            this.mLastFinalClipRect.set(finalClipRect);
            this.mSurfaceController.setFinalCropInTransaction(finalClipRect);
            if (this.mDestroyPreservedSurfaceUponRedraw && this.mPendingDestroySurface != null) {
                this.mPendingDestroySurface.setFinalCropInTransaction(finalClipRect);
            }
        }
    }

    private int resolveStackClip() {
        if (this.mAppAnimator == null || this.mAppAnimator.animation == null) {
            return this.mStackClip;
        }
        return this.mAppAnimator.getStackClip();
    }

    private void adjustCropToStackBounds(WindowState w, Rect clipRect, Rect finalClipRect, boolean isFreeformResizing) {
        DisplayContent displayContent = w.getDisplayContent();
        if (displayContent == null || displayContent.isDefaultDisplay) {
            Task task = w.getTask();
            if (task != null && task.cropWindowsToStackBounds()) {
                int stackClip = resolveStackClip();
                if (!isAnimationSet() || stackClip != 2) {
                    if (w != ((WindowState) this.mPolicy.getWinShowWhenLockedLw()) || !this.mPolicy.isKeyguardShowingOrOccluded()) {
                        int frameX;
                        int frameY;
                        boolean useFinalClipRect;
                        TaskStack stack = task.mStack;
                        stack.getDimBounds(this.mTmpStackBounds);
                        Rect surfaceInsets = w.getAttrs().surfaceInsets;
                        if (isFreeformResizing) {
                            frameX = (int) this.mSurfaceController.getX();
                        } else {
                            frameX = (w.mFrame.left + this.mWin.mXOffset) - surfaceInsets.left;
                        }
                        if (isFreeformResizing) {
                            frameY = (int) this.mSurfaceController.getY();
                        } else {
                            frameY = (w.mFrame.top + this.mWin.mYOffset) - surfaceInsets.top;
                        }
                        if (isAnimationSet() && stackClip == 0) {
                            useFinalClipRect = true;
                        } else {
                            useFinalClipRect = this.mDestroyPreservedSurfaceUponRedraw;
                        }
                        if (useFinalClipRect) {
                            finalClipRect.set(this.mTmpStackBounds);
                        } else {
                            if (StackId.hasWindowShadow(stack.mStackId) && !StackId.isTaskResizeAllowed(stack.mStackId)) {
                                this.mTmpStackBounds.inset(-surfaceInsets.left, -surfaceInsets.top, -surfaceInsets.right, -surfaceInsets.bottom);
                            }
                            clipRect.left = Math.max(0, Math.max(this.mTmpStackBounds.left, clipRect.left + frameX) - frameX);
                            clipRect.top = Math.max(0, Math.max(this.mTmpStackBounds.top, clipRect.top + frameY) - frameY);
                            clipRect.right = Math.max(0, Math.min(this.mTmpStackBounds.right, clipRect.right + frameX) - frameX);
                            clipRect.bottom = Math.max(0, Math.min(this.mTmpStackBounds.bottom, clipRect.bottom + frameY) - frameY);
                        }
                    }
                }
            }
        }
    }

    void setSurfaceBoundariesLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        Task task = w.getTask();
        if (!w.isResizedWhileNotDragResizing() || w.isGoneForLayoutLw()) {
            this.mTmpSize.set(w.mShownPosition.x, w.mShownPosition.y, 0, 0);
            calculateSurfaceBounds(w, w.getAttrs());
            this.mExtraHScale = 1.0f;
            this.mExtraVScale = 1.0f;
            boolean wasForceScaled = this.mForceScaleUntilResize;
            if (w.inPinnedWorkspace() && w.mRelayoutCalled && !w.mInRelayout) {
                this.mSurfaceResized = false;
            } else {
                this.mSurfaceResized = this.mSurfaceController.setSizeInTransaction(this.mTmpSize.width(), this.mTmpSize.height(), recoveringMemory);
                setSurfaceLowResolutionInfo();
            }
            boolean z = this.mForceScaleUntilResize && !this.mSurfaceResized;
            this.mForceScaleUntilResize = z;
            calculateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect);
            float surfaceWidth = this.mSurfaceController.getWidth();
            float surfaceHeight = this.mSurfaceController.getHeight();
            if ((task == null || !task.mStack.getForceScaleToCrop()) && !this.mForceScaleUntilResize) {
                this.mSurfaceController.setPositionInTransaction((float) this.mTmpSize.left, (float) this.mTmpSize.top, recoveringMemory);
            } else {
                int hInsets = w.getAttrs().surfaceInsets.left + w.getAttrs().surfaceInsets.right;
                int vInsets = w.getAttrs().surfaceInsets.top + w.getAttrs().surfaceInsets.bottom;
                if (!this.mForceScaleUntilResize) {
                    this.mSurfaceController.forceScaleableInTransaction(true);
                }
                this.mExtraHScale = ((float) (this.mTmpClipRect.width() - hInsets)) / (surfaceWidth - ((float) hInsets));
                this.mExtraVScale = ((float) (this.mTmpClipRect.height() - vInsets)) / (surfaceHeight - ((float) vInsets));
                this.mSurfaceController.setPositionInTransaction((float) Math.floor((double) ((int) (((float) ((int) (((float) this.mTmpSize.left) - (((float) w.mAttrs.x) * (1.0f - this.mExtraHScale))))) + (((float) w.getAttrs().surfaceInsets.left) * (1.0f - this.mExtraHScale))))), (float) Math.floor((double) ((int) (((float) ((int) (((float) this.mTmpSize.top) - (((float) w.mAttrs.y) * (1.0f - this.mExtraVScale))))) + (((float) w.getAttrs().surfaceInsets.top) * (1.0f - this.mExtraVScale))))), recoveringMemory);
                this.mTmpClipRect.set(0, 0, (int) surfaceWidth, (int) surfaceHeight);
                this.mTmpFinalClipRect.setEmpty();
                this.mForceScaleUntilResize = true;
            }
            if (wasForceScaled && !this.mForceScaleUntilResize) {
                this.mSurfaceController.setPositionAppliesWithResizeInTransaction(true);
                this.mSurfaceController.forceScaleableInTransaction(false);
            }
            Rect rect = this.mTmpClipRect;
            if (w.inPinnedWorkspace()) {
                rect = null;
                task.mStack.getDimBounds(this.mTmpFinalClipRect);
                this.mTmpFinalClipRect.inset(-w.mAttrs.surfaceInsets.left, -w.mAttrs.surfaceInsets.top, -w.mAttrs.surfaceInsets.right, -w.mAttrs.surfaceInsets.bottom);
            }
            updateSurfaceWindowCrop(rect, this.mTmpFinalClipRect, recoveringMemory);
            this.mSurfaceController.setMatrixInTransaction((this.mDsDx * w.mHScale) * this.mExtraHScale, (this.mDtDx * w.mVScale) * this.mExtraVScale, (this.mDsDy * w.mHScale) * this.mExtraHScale, (this.mDtDy * w.mVScale) * this.mExtraVScale, recoveringMemory);
            if (this.mSurfaceResized) {
                this.mReportSurfaceResized = true;
                this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 4);
                w.applyDimLayerIfNeeded();
            }
        }
    }

    void prepareSurfaceLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        if (!hasSurface()) {
            if (w.mOrientationChanging) {
                w.mOrientationChanging = false;
            }
        } else if (!isWaitingForOpening()) {
            boolean displayed = false;
            hwPrepareSurfaceLocked();
            setSurfaceBoundariesLocked(recoveringMemory);
            if (this.mIsWallpaper && !this.mWin.mWallpaperVisible) {
                hide("prepareSurfaceLocked");
            } else if (w.mAttachedHidden || !w.isOnScreen()) {
                hide("prepareSurfaceLocked");
                this.mWallpaperControllerLocked.hideWallpapers(w);
                if (w.mOrientationChanging) {
                    w.mOrientationChanging = false;
                }
            } else if (this.mLastLayer == this.mAnimLayer && this.mLastAlpha == this.mShownAlpha && this.mLastDsDx == this.mDsDx && this.mLastDtDx == this.mDtDx && this.mLastDsDy == this.mDsDy && this.mLastDtDy == this.mDtDy && w.mLastHScale == w.mHScale && w.mLastVScale == w.mVScale && !this.mLastHidden) {
                displayed = true;
            } else {
                displayed = true;
                this.mLastAlpha = this.mShownAlpha;
                this.mLastLayer = this.mAnimLayer;
                this.mLastDsDx = this.mDsDx;
                this.mLastDtDx = this.mDtDx;
                this.mLastDsDy = this.mDsDy;
                this.mLastDtDy = this.mDtDy;
                w.mLastHScale = w.mHScale;
                w.mLastVScale = w.mVScale;
                if (this.mSurfaceController.prepareToShowInTransaction(this.mShownAlpha, this.mAnimLayer, (this.mDsDx * w.mHScale) * this.mExtraHScale, (this.mDtDx * w.mVScale) * this.mExtraVScale, (this.mDsDy * w.mHScale) * this.mExtraHScale, (this.mDtDy * w.mVScale) * this.mExtraVScale, recoveringMemory) && this.mLastHidden && this.mDrawState == 4) {
                    if (showSurfaceRobustlyLocked()) {
                        markPreservedSurfaceForDestroy();
                        this.mAnimator.requestRemovalOfReplacedWindows(w);
                        this.mLastHidden = false;
                        if (this.mIsWallpaper) {
                            this.mWallpaperControllerLocked.dispatchWallpaperVisibility(w, true);
                        }
                        this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 8);
                    } else {
                        w.mOrientationChanging = false;
                    }
                }
                if (hasSurface()) {
                    w.mToken.hasVisible = true;
                }
            }
            if (displayed) {
                if (w.mOrientationChanging) {
                    if (w.isDrawnLw()) {
                        w.mOrientationChanging = false;
                    } else {
                        WindowAnimator windowAnimator = this.mAnimator;
                        windowAnimator.mBulkUpdateParams &= -9;
                        this.mAnimator.mLastWindowFreezeSource = w;
                    }
                }
                w.mToken.hasVisible = true;
            }
        }
    }

    void setTransparentRegionHintLocked(Region region) {
        if (this.mSurfaceController == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
        } else {
            this.mSurfaceController.setTransparentRegionHint(region);
        }
    }

    void setWallpaperOffset(Point shownPosition) {
        LayoutParams attrs = this.mWin.getAttrs();
        int left = shownPosition.x - attrs.surfaceInsets.left;
        int top = shownPosition.y - attrs.surfaceInsets.top;
        try {
            SurfaceControl.openTransaction();
            this.mSurfaceController.setPositionInTransaction((float) (this.mWin.mFrame.left + left), (float) (this.mWin.mFrame.top + top), false);
            calculateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect);
            updateSurfaceWindowCrop(this.mTmpClipRect, this.mTmpFinalClipRect, false);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error positioning surface of " + this.mWin + " pos=(" + left + "," + top + ")", e);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    boolean tryChangeFormatInPlaceLocked() {
        boolean z = false;
        if (this.mSurfaceController == null) {
            return false;
        }
        boolean isHwAccelerated;
        LayoutParams attrs = this.mWin.getAttrs();
        if ((attrs.flags & 16777216) != 0) {
            isHwAccelerated = true;
        } else {
            isHwAccelerated = false;
        }
        if ((isHwAccelerated ? -3 : attrs.format) != this.mSurfaceFormat) {
            return false;
        }
        if (!PixelFormat.formatHasAlpha(attrs.format)) {
            z = true;
        }
        setOpaqueLocked(z);
        return true;
    }

    void setOpaqueLocked(boolean isOpaque) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setOpaque(isOpaque);
        }
    }

    void setSecureLocked(boolean isSecure) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setSecure(isSecure);
        }
    }

    boolean performShowLocked() {
        if (this.mWin.isHiddenFromUserLocked()) {
            this.mWin.hideLw(false);
            return false;
        } else if (this.mDrawState != 3 || !this.mWin.isReadyForDisplayIgnoringKeyguard()) {
            return false;
        } else {
            this.mService.enableScreenIfNeededLocked();
            applyEnterAnimationLocked();
            this.mLastAlpha = -1.0f;
            this.mDrawState = 4;
            if (this.mWin.mAttrs.type == 3 && this.mAppAnimator != null && this.mAppAnimator.hasTransformation && this.mAppAnimator.animation != null) {
                this.mAppAnimator.stepAnimationLocked(SystemClock.uptimeMillis() - 12, 0);
                prepareSurfaceLocked(true);
                this.mAppAnimator.stepAnimationLocked(SystemClock.uptimeMillis() - 4, 0);
                prepareSurfaceLocked(true);
            }
            this.mService.scheduleAnimationLocked();
            int i = this.mWin.mChildWindows.size();
            while (i > 0) {
                i--;
                WindowState c = (WindowState) this.mWin.mChildWindows.get(i);
                if (c.mAttachedHidden) {
                    c.mAttachedHidden = false;
                    if (c.mWinAnimator.mSurfaceController != null) {
                        c.mWinAnimator.performShowLocked();
                        DisplayContent displayContent = c.getDisplayContent();
                        if (displayContent != null) {
                            displayContent.layoutNeeded = true;
                        }
                    }
                }
            }
            if (!(this.mWin.mAttrs.type == 3 || this.mWin.mAppToken == null)) {
                this.mWin.mAppToken.onFirstWindowDrawn(this.mWin, this);
            }
            if (this.mWin.mAttrs.type == 2011) {
                this.mWin.mDisplayContent.mDividerControllerLocked.resetImeHideRequested();
            }
            return true;
        }
    }

    private boolean showSurfaceRobustlyLocked() {
        Task task = this.mWin.getTask();
        if (task != null && StackId.windowsAreScaleable(task.mStack.mStackId)) {
            this.mSurfaceController.forceScaleableInTransaction(true);
        }
        if (!this.mSurfaceController.showRobustlyInTransaction()) {
            return false;
        }
        if (this.mWin.mTurnOnScreen) {
            this.mWin.mTurnOnScreen = false;
            WindowAnimator windowAnimator = this.mAnimator;
            windowAnimator.mBulkUpdateParams |= 16;
        }
        return true;
    }

    void applyEnterAnimationLocked() {
        if (!this.mWin.mSkipEnterAnimationForSeamlessReplacement) {
            int transit;
            if (this.mEnterAnimationPending) {
                this.mEnterAnimationPending = false;
                transit = 1;
            } else {
                transit = 3;
            }
            applyAnimationLocked(transit, true);
            if (this.mService.mAccessibilityController != null && this.mWin.getDisplayId() == 0) {
                this.mService.mAccessibilityController.onWindowTransitionLocked(this.mWin, transit);
            }
        }
    }

    boolean applyAnimationLocked(int transit, boolean isEntrance) {
        boolean z = true;
        if ((this.mLocalAnimating && this.mAnimationIsEntrance == isEntrance) || this.mKeyguardGoingAwayAnimation) {
            if (this.mAnimation != null && this.mKeyguardGoingAwayAnimation && transit == 5) {
                applyFadeoutDuringKeyguardExitAnimation();
            }
            return true;
        }
        if (this.mService.okToDisplay()) {
            int anim = this.mPolicy.selectAnimationLw(this.mWin, transit);
            int attr = -1;
            Animation a = null;
            if (anim == 0) {
                switch (transit) {
                    case 1:
                        attr = 0;
                        break;
                    case 2:
                        attr = 1;
                        break;
                    case 3:
                        attr = 2;
                        break;
                    case 4:
                        attr = 3;
                        break;
                }
                if (attr >= 0) {
                    a = this.mService.mAppTransition.loadAnimationAttr(this.mWin.mAttrs, attr);
                }
            } else if (anim != -1) {
                a = AnimationUtils.loadAnimation(this.mContext, anim);
            } else {
                a = null;
            }
            if (a != null) {
                setAnimation(a);
                this.mAnimationIsEntrance = isEntrance;
            }
        } else {
            clearAnimation();
        }
        if (this.mWin.mAttrs.type == 2011) {
            this.mService.adjustForImeIfNeeded(this.mWin.mDisplayContent);
            if (isEntrance) {
                this.mWin.setDisplayLayoutNeeded();
                this.mService.mWindowPlacerLocked.requestTraversal();
            }
        }
        if (this.mAnimation == null) {
            z = false;
        }
        return z;
    }

    private void applyFadeoutDuringKeyguardExitAnimation() {
        long startTime = this.mAnimation.getStartTime();
        long duration = this.mAnimation.getDuration();
        long elapsed = this.mLastAnimationTime - startTime;
        long fadeDuration = duration - elapsed;
        if (fadeDuration > 0) {
            AnimationSet newAnimation = new AnimationSet(false);
            newAnimation.setDuration(duration);
            newAnimation.setStartTime(startTime);
            newAnimation.addAnimation(this.mAnimation);
            Animation fadeOut = AnimationUtils.loadAnimation(this.mContext, 17432593);
            fadeOut.setDuration(fadeDuration);
            fadeOut.setStartOffset(elapsed);
            newAnimation.addAnimation(fadeOut);
            newAnimation.initialize(this.mWin.mFrame.width(), this.mWin.mFrame.height(), this.mAnimDx, this.mAnimDy);
            this.mAnimation = newAnimation;
        }
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (this.mAnimating || this.mLocalAnimating || this.mAnimationIsEntrance || this.mAnimation != null) {
            pw.print(prefix);
            pw.print("mAnimating=");
            pw.print(this.mAnimating);
            pw.print(" mLocalAnimating=");
            pw.print(this.mLocalAnimating);
            pw.print(" mAnimationIsEntrance=");
            pw.print(this.mAnimationIsEntrance);
            pw.print(" mAnimation=");
            pw.print(this.mAnimation);
            pw.print(" mStackClip=");
            pw.println(this.mStackClip);
        }
        if (this.mHasTransformation || this.mHasLocalTransformation) {
            pw.print(prefix);
            pw.print("XForm: has=");
            pw.print(this.mHasTransformation);
            pw.print(" hasLocal=");
            pw.print(this.mHasLocalTransformation);
            pw.print(" ");
            this.mTransformation.printShortString(pw);
            pw.println();
        }
        if (this.mSurfaceController != null) {
            this.mSurfaceController.dump(pw, prefix, dumpAll);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mDrawState=");
            pw.print(drawStateToString());
            pw.print(prefix);
            pw.print(" mLastHidden=");
            pw.println(this.mLastHidden);
            pw.print(prefix);
            pw.print("mSystemDecorRect=");
            this.mSystemDecorRect.printShortString(pw);
            pw.print(" last=");
            this.mLastSystemDecorRect.printShortString(pw);
            pw.print(" mHasClipRect=");
            pw.print(this.mHasClipRect);
            pw.print(" mLastClipRect=");
            this.mLastClipRect.printShortString(pw);
            if (!this.mLastFinalClipRect.isEmpty()) {
                pw.print(" mLastFinalClipRect=");
                this.mLastFinalClipRect.printShortString(pw);
            }
            pw.println();
        }
        if (this.mPendingDestroySurface != null) {
            pw.print(prefix);
            pw.print("mPendingDestroySurface=");
            pw.println(this.mPendingDestroySurface);
        }
        if (this.mSurfaceResized || this.mSurfaceDestroyDeferred) {
            pw.print(prefix);
            pw.print("mSurfaceResized=");
            pw.print(this.mSurfaceResized);
            pw.print(" mSurfaceDestroyDeferred=");
            pw.println(this.mSurfaceDestroyDeferred);
        }
        if (this.mShownAlpha == 1.0f && this.mAlpha == 1.0f) {
            if (this.mLastAlpha != 1.0f) {
            }
            if (this.mHaveMatrix || this.mWin.mGlobalScale != 1.0f) {
                pw.print(prefix);
                pw.print("mGlobalScale=");
                pw.print(this.mWin.mGlobalScale);
                pw.print(" mDsDx=");
                pw.print(this.mDsDx);
                pw.print(" mDtDx=");
                pw.print(this.mDtDx);
                pw.print(" mDsDy=");
                pw.print(this.mDsDy);
                pw.print(" mDtDy=");
                pw.println(this.mDtDy);
            }
            if (this.mAnimationStartDelayed) {
                pw.print(prefix);
                pw.print("mAnimationStartDelayed=");
                pw.print(this.mAnimationStartDelayed);
            }
        }
        pw.print(prefix);
        pw.print("mShownAlpha=");
        pw.print(this.mShownAlpha);
        pw.print(" mAlpha=");
        pw.print(this.mAlpha);
        pw.print(" mLastAlpha=");
        pw.println(this.mLastAlpha);
        pw.print(prefix);
        pw.print("mGlobalScale=");
        pw.print(this.mWin.mGlobalScale);
        pw.print(" mDsDx=");
        pw.print(this.mDsDx);
        pw.print(" mDtDx=");
        pw.print(this.mDtDx);
        pw.print(" mDsDy=");
        pw.print(this.mDsDy);
        pw.print(" mDtDy=");
        pw.println(this.mDtDy);
        if (this.mAnimationStartDelayed) {
            pw.print(prefix);
            pw.print("mAnimationStartDelayed=");
            pw.print(this.mAnimationStartDelayed);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("WindowStateAnimator{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.mWin.mAttrs.getTitle());
        sb.append('}');
        return sb.toString();
    }

    void reclaimSomeSurfaceMemory(String operation, boolean secure) {
        this.mService.reclaimSomeSurfaceMemoryLocked(this, operation, secure);
    }

    boolean getShown() {
        if (this.mSurfaceController != null) {
            return this.mSurfaceController.getShown();
        }
        return false;
    }

    void destroySurface() {
        try {
            if (this.mSurfaceController != null) {
                this.mSurfaceController.destroyInTransaction();
            }
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying surface " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e);
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
        } catch (Throwable th) {
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
        this.mDrawState = 0;
    }

    void setMoveAnimation(int left, int top) {
        setAnimation(AnimationUtils.loadAnimation(this.mContext, 17432749));
        this.mAnimDx = this.mWin.mLastFrame.left - left;
        this.mAnimDy = this.mWin.mLastFrame.top - top;
        this.mAnimateMove = true;
    }

    void deferTransactionUntilParentFrame(long frameNumber) {
        if (this.mWin.isChildWindow()) {
            this.mDeferTransactionUntilFrame = frameNumber;
            this.mDeferTransactionTime = System.currentTimeMillis();
            this.mSurfaceController.deferTransactionUntil(this.mWin.mAttachedWindow.mWinAnimator.mSurfaceController.getHandle(), frameNumber);
        }
    }

    void deferToPendingTransaction() {
        if (this.mDeferTransactionUntilFrame >= 0) {
            if (System.currentTimeMillis() > this.mDeferTransactionTime + PENDING_TRANSACTION_FINISH_WAIT_TIME) {
                this.mDeferTransactionTime = -1;
                this.mDeferTransactionUntilFrame = -1;
            } else if (this.mWin.mAttachedWindow.mWinAnimator.mSurfaceController != null) {
                this.mSurfaceController.deferTransactionUntil(this.mWin.mAttachedWindow.mWinAnimator.mSurfaceController.getHandle(), this.mDeferTransactionUntilFrame);
            }
        }
    }

    private long getAnimationFrameTime(Animation animation, long currentTime) {
        if (!this.mAnimationStartDelayed) {
            return currentTime;
        }
        animation.setStartTime(currentTime);
        return 1 + currentTime;
    }

    void startDelayingAnimationStart() {
        this.mAnimationStartDelayed = true;
    }

    void endDelayingAnimationStart() {
        this.mAnimationStartDelayed = false;
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        return animLayer;
    }

    public void hwPrepareSurfaceLocked() {
        computeShownFrameLocked();
    }

    void computeShownFrameRightLocked() {
    }

    void computeShownFrameLeftLocked() {
    }

    public void updateBlurLayer(LayoutParams lp) {
        if (this.mSurfaceController != null) {
            boolean forceRefresh;
            int surfaceHashCode = this.mSurfaceController.hashCode();
            if (this.mBlurParams == null) {
                forceRefresh = true;
                this.mBlurParams = new BlurParams(lp, surfaceHashCode);
            } else {
                forceRefresh = this.mBlurParams.updateHashCode(surfaceHashCode);
                this.mBlurParams.set(lp);
            }
            this.mBlurParams.refresh(this.mSurfaceController, forceRefresh);
        }
    }

    private void setSurfaceLowResolutionInfo() {
        WindowState w = this.mWin;
        this.mSurfaceController.setSurfaceLowResolutionInfo(w.mGlobalScale, w.getLowResolutionMode());
    }
}
