package com.android.server.wm;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.LinearInterpolator;

public class BoundsAnimationController {
    private static final boolean DEBUG = false;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;
    private static final boolean DEBUG_LOCAL = false;
    private static final String TAG = "WindowManager";
    private final AppTransition mAppTransition;
    private final AppTransitionNotifier mAppTransitionNotifier = new AppTransitionNotifier();
    private boolean mFinishAnimationAfterTransition = false;
    private final Handler mHandler;
    private ArrayMap<AnimateBoundsUser, BoundsAnimator> mRunningAnimations = new ArrayMap();

    public interface AnimateBoundsUser {
        void getFullScreenBounds(Rect rect);

        void moveToFullscreen();

        void onAnimationEnd();

        void onAnimationStart();

        boolean setPinnedStackSize(Rect rect, Rect rect2);

        boolean setSize(Rect rect);
    }

    private final class AppTransitionNotifier extends AppTransitionListener implements Runnable {
        private AppTransitionNotifier() {
        }

        public void onAppTransitionCancelledLocked() {
            animationFinished();
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            animationFinished();
        }

        private void animationFinished() {
            if (BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                BoundsAnimationController.this.mHandler.removeCallbacks(this);
                BoundsAnimationController.this.mHandler.post(this);
            }
        }

        public void run() {
            for (int i = 0; i < BoundsAnimationController.this.mRunningAnimations.size(); i++) {
                ((BoundsAnimator) BoundsAnimationController.this.mRunningAnimations.valueAt(i)).onAnimationEnd(null);
            }
        }
    }

    private final class BoundsAnimator extends ValueAnimator implements AnimatorUpdateListener, AnimatorListener {
        private final Rect mFrom;
        private final int mFrozenTaskHeight;
        private final int mFrozenTaskWidth;
        private final boolean mMoveToFullScreen;
        private final boolean mReplacement;
        private final AnimateBoundsUser mTarget;
        private final Rect mTmpRect = new Rect();
        private final Rect mTmpTaskBounds = new Rect();
        private final Rect mTo;
        private boolean mWillReplace;

        BoundsAnimator(AnimateBoundsUser target, Rect from, Rect to, boolean moveToFullScreen, boolean replacement) {
            this.mTarget = target;
            this.mFrom = from;
            this.mTo = to;
            this.mMoveToFullScreen = moveToFullScreen;
            this.mReplacement = replacement;
            addUpdateListener(this);
            addListener(this);
            if (animatingToLargerSize()) {
                this.mFrozenTaskWidth = this.mTo.width();
                this.mFrozenTaskHeight = this.mTo.height();
                return;
            }
            this.mFrozenTaskWidth = this.mFrom.width();
            this.mFrozenTaskHeight = this.mFrom.height();
        }

        boolean animatingToLargerSize() {
            if (this.mFrom.width() * this.mFrom.height() > this.mTo.width() * this.mTo.height()) {
                return false;
            }
            return true;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            float remains = 1.0f - value;
            this.mTmpRect.left = (int) (((((float) this.mFrom.left) * remains) + (((float) this.mTo.left) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.top = (int) (((((float) this.mFrom.top) * remains) + (((float) this.mTo.top) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.right = (int) (((((float) this.mFrom.right) * remains) + (((float) this.mTo.right) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpRect.bottom = (int) (((((float) this.mFrom.bottom) * remains) + (((float) this.mTo.bottom) * value)) + TaskPositioner.RESIZING_HINT_ALPHA);
            this.mTmpTaskBounds.set(this.mTmpRect.left, this.mTmpRect.top, this.mTmpRect.left + this.mFrozenTaskWidth, this.mTmpRect.top + this.mFrozenTaskHeight);
            if (!this.mTarget.setPinnedStackSize(this.mTmpRect, this.mTmpTaskBounds)) {
                animation.cancel();
            }
        }

        public void onAnimationStart(Animator animation) {
            BoundsAnimationController.this.mFinishAnimationAfterTransition = false;
            if (!this.mReplacement) {
                this.mTarget.onAnimationStart();
            }
            if (animatingToLargerSize()) {
                this.mTmpRect.set(this.mFrom.left, this.mFrom.top, this.mFrom.left + this.mFrozenTaskWidth, this.mFrom.top + this.mFrozenTaskHeight);
                this.mTarget.setPinnedStackSize(this.mFrom, this.mTmpRect);
            }
        }

        public void onAnimationEnd(Animator animation) {
            if (!BoundsAnimationController.this.mAppTransition.isRunning() || BoundsAnimationController.this.mFinishAnimationAfterTransition) {
                finishAnimation();
                this.mTarget.setPinnedStackSize(this.mTo, null);
                if (this.mMoveToFullScreen && !this.mWillReplace) {
                    this.mTarget.moveToFullscreen();
                }
                return;
            }
            BoundsAnimationController.this.mFinishAnimationAfterTransition = true;
        }

        public void onAnimationCancel(Animator animation) {
            finishAnimation();
        }

        public void cancel() {
            this.mWillReplace = true;
            super.cancel();
        }

        public boolean isAnimatingTo(Rect bounds) {
            return this.mTo.equals(bounds);
        }

        private void finishAnimation() {
            if (!this.mWillReplace) {
                this.mTarget.onAnimationEnd();
            }
            removeListener(this);
            removeUpdateListener(this);
            BoundsAnimationController.this.mRunningAnimations.remove(this.mTarget);
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    BoundsAnimationController(AppTransition transition, Handler handler) {
        this.mHandler = handler;
        this.mAppTransition = transition;
        this.mAppTransition.registerListenerLocked(this.mAppTransitionNotifier);
    }

    void animateBounds(AnimateBoundsUser target, Rect from, Rect to, int animationDuration) {
        boolean moveToFullscreen = false;
        if (to == null) {
            to = new Rect();
            target.getFullScreenBounds(to);
            moveToFullscreen = true;
        }
        BoundsAnimator existing = (BoundsAnimator) this.mRunningAnimations.get(target);
        boolean replacing = existing != null;
        if (replacing) {
            if (!existing.isAnimatingTo(to)) {
                existing.cancel();
            } else {
                return;
            }
        }
        BoundsAnimator animator = new BoundsAnimator(target, from, to, moveToFullscreen, replacing);
        this.mRunningAnimations.put(target, animator);
        animator.setFloatValues(new float[]{0.0f, 1.0f});
        if (animationDuration == -1) {
            animationDuration = 250;
        }
        animator.setDuration((long) (animationDuration * 1));
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}
