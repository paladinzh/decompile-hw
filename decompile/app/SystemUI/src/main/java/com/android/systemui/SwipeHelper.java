package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.recents.views.TaskView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.HashMap;

public class SwipeHelper {
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200;
    private int MAX_DISMISS_VELOCITY = 4000;
    private int MAX_ESCAPE_ANIMATION_DURATION = 400;
    private float SWIPE_ESCAPE_VELOCITY = 100.0f;
    private Callback mCallback;
    private boolean mCanCurrViewBeDimissed;
    private View mCurrView;
    private float mDensityScale;
    private boolean mDisableHwLayers;
    private HashMap<View, Animator> mDismissPendingMap = new HashMap();
    private boolean mDragging;
    private FalsingManager mFalsingManager;
    private int mFalsingThreshold;
    private FlingAnimationUtils mFlingAnimationUtils;
    private Handler mHandler;
    private float mInitialTouchPos;
    private LongPressListener mLongPressListener;
    private boolean mLongPressSent;
    private long mLongPressTimeout;
    private float mMaxSwipeProgress = 1.0f;
    private float mMinSwipeProgress = 0.0f;
    private float mPagingTouchSlop;
    private float mPerpendicularInitialTouchPos;
    private boolean mSnappingChild;
    private int mSwipeDirection;
    private final int[] mTmpPos = new int[2];
    private boolean mTouchAboveFalsingThreshold;
    private float mTranslation = 0.0f;
    private VelocityTracker mVelocityTracker;
    private Runnable mWatchLongPress;
    private WindowManager mWindowManager;
    private int mWindowWidth;

    public interface Callback {
        boolean canChildBeDismissed(View view);

        View getChildAtPosition(MotionEvent motionEvent);

        float getFalsingThresholdFactor();

        boolean isAntiFalsingNeeded();

        void onBeginDrag(View view);

        void onChildDismissed(View view);

        void onChildSnappedBack(View view, float f);

        void onDragCancelled(View view);

        boolean updateSwipeProgress(View view, boolean z, float f);
    }

    public interface LongPressListener {
        boolean onLongPress(View view, int i, int i2);
    }

    public SwipeHelper(int swipeDirection, Callback callback, Context context) {
        this.mCallback = callback;
        this.mHandler = new Handler();
        this.mSwipeDirection = swipeDirection;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mDensityScale = context.getResources().getDisplayMetrics().density;
        this.mPagingTouchSlop = (float) ViewConfiguration.get(context).getScaledPagingTouchSlop();
        this.mLongPressTimeout = (long) (((float) ViewConfiguration.getLongPressTimeout()) * 1.5f);
        this.mFalsingThreshold = context.getResources().getDimensionPixelSize(R.dimen.swipe_helper_falsing_threshold);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, ((float) getMaxEscapeAnimDuration()) / 1000.0f);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mWindowWidth = this.mWindowManager.getDefaultDisplay().getWidth();
    }

    public void setLongPressListener(LongPressListener listener) {
        this.mLongPressListener = listener;
    }

    public void setDensityScale(float densityScale) {
        this.mDensityScale = densityScale;
    }

    public void setPagingTouchSlop(float pagingTouchSlop) {
        this.mPagingTouchSlop = pagingTouchSlop;
    }

    public void setDisableHardwareLayers(boolean disableHwLayers) {
        this.mDisableHwLayers = disableHwLayers;
    }

    private float getPos(MotionEvent ev) {
        return this.mSwipeDirection == 0 ? ev.getX() : ev.getY();
    }

    private float getPerpendicularPos(MotionEvent ev) {
        return this.mSwipeDirection == 0 ? ev.getY() : ev.getX();
    }

    protected float getTranslation(View v) {
        return this.mSwipeDirection == 0 ? v.getTranslationX() : v.getTranslationY();
    }

    private float getVelocity(VelocityTracker vt) {
        if (this.mSwipeDirection == 0) {
            return vt.getXVelocity();
        }
        return vt.getYVelocity();
    }

    protected ObjectAnimator createTranslationAnimation(View v, float newPos) {
        return ObjectAnimator.ofFloat(v, this.mSwipeDirection == 0 ? View.TRANSLATION_X : View.TRANSLATION_Y, new float[]{newPos});
    }

    protected Animator getViewTranslationAnimator(View v, float target, AnimatorUpdateListener listener) {
        ObjectAnimator anim = createTranslationAnimation(v, target);
        if (listener != null) {
            anim.addUpdateListener(listener);
        }
        return anim;
    }

    protected void setTranslation(View v, float translate) {
        if (v != null) {
            if (this.mSwipeDirection == 0) {
                v.setTranslationX(translate);
            } else {
                v.setTranslationY(translate);
            }
        }
    }

    protected float getSize(View v) {
        int measuredWidth;
        if (this.mSwipeDirection == 0) {
            measuredWidth = v.getMeasuredWidth();
        } else {
            measuredWidth = v.getMeasuredHeight();
        }
        return (float) measuredWidth;
    }

    private float getSwipeProgressForOffset(View view, float translation) {
        return Math.min(Math.max(this.mMinSwipeProgress, Math.abs(translation / getSize(view))), this.mMaxSwipeProgress);
    }

    private float getSwipeAlpha(float progress) {
        return Math.min(0.0f, Math.max(1.0f, progress / 0.5f));
    }

    private void updateSwipeProgressFromOffset(View animView, boolean dismissable) {
        updateSwipeProgressFromOffset(animView, dismissable, getTranslation(animView));
    }

    private void updateSwipeProgressFromOffset(View animView, boolean dismissable, float translation) {
        float swipeProgress = getSwipeProgressForOffset(animView, translation);
        if (!this.mCallback.updateSwipeProgress(animView, dismissable, swipeProgress) && dismissable) {
            float alpha = swipeProgress;
            if (!this.mDisableHwLayers) {
                if (swipeProgress == 0.0f || swipeProgress == 1.0f) {
                    animView.setLayerType(0, null);
                } else {
                    animView.setLayerType(2, null);
                }
            }
            animView.setAlpha(getSwipeAlpha(swipeProgress));
        }
        invalidateGlobalRegion(animView);
    }

    public static void invalidateGlobalRegion(View view) {
        invalidateGlobalRegion(view, new RectF((float) view.getLeft(), (float) view.getTop(), (float) view.getRight(), (float) view.getBottom()));
    }

    public static void invalidateGlobalRegion(View view, RectF childBounds) {
        while (view.getParent() != null && (view.getParent() instanceof View)) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(childBounds);
            view.invalidate((int) Math.floor((double) childBounds.left), (int) Math.floor((double) childBounds.top), (int) Math.ceil((double) childBounds.right), (int) Math.ceil((double) childBounds.bottom));
        }
    }

    public void removeLongPressCallback() {
        if (this.mWatchLongPress != null) {
            HwLog.i("com.android.systemui.SwipeHelper", "removeLongPressCallback");
            this.mHandler.removeCallbacks(this.mWatchLongPress);
            this.mWatchLongPress = null;
        }
    }

    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        boolean z = true;
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("com.android.systemui.SwipeHelper", "onInterceptTouchEvent: " + ev);
        }
        switch (ev.getAction()) {
            case 0:
                this.mTouchAboveFalsingThreshold = false;
                this.mDragging = false;
                this.mSnappingChild = false;
                this.mLongPressSent = false;
                this.mVelocityTracker.clear();
                this.mCurrView = this.mCallback.getChildAtPosition(ev);
                if (this.mCurrView != null) {
                    onDownUpdate(this.mCurrView);
                    this.mCanCurrViewBeDimissed = this.mCallback.canChildBeDismissed(this.mCurrView);
                    this.mVelocityTracker.addMovement(ev);
                    this.mInitialTouchPos = getPos(ev);
                    this.mPerpendicularInitialTouchPos = getPerpendicularPos(ev);
                    this.mTranslation = getTranslation(this.mCurrView);
                    if (this.mLongPressListener != null) {
                        if (this.mWatchLongPress == null) {
                            HwLog.i("com.android.systemui.SwipeHelper", "start watch long press: " + this.mCurrView);
                            this.mWatchLongPress = new Runnable() {
                                public void run() {
                                    HwLog.i("com.android.systemui.SwipeHelper", "execute long press: " + SwipeHelper.this.mCurrView);
                                    if (SwipeHelper.this.mCurrView != null && !SwipeHelper.this.mLongPressSent) {
                                        if (SwipeHelper.this.mCurrView instanceof ExpandableNotificationRow) {
                                            BDReporter.e(SwipeHelper.this.mCurrView.getContext(), 348, "pkg:" + ((ExpandableNotificationRow) SwipeHelper.this.mCurrView).getStatusBarNotification().getPackageName());
                                        }
                                        SwipeHelper.this.mLongPressSent = true;
                                        SwipeHelper.this.mCurrView.sendAccessibilityEvent(2);
                                        SwipeHelper.this.mCurrView.getLocationOnScreen(SwipeHelper.this.mTmpPos);
                                        SwipeHelper.this.mLongPressListener.onLongPress(SwipeHelper.this.mCurrView, ((int) ev.getRawX()) - SwipeHelper.this.mTmpPos[0], ((int) ev.getRawY()) - SwipeHelper.this.mTmpPos[1]);
                                    }
                                }
                            };
                        }
                        this.mHandler.removeCallbacks(this.mWatchLongPress);
                        this.mHandler.postDelayed(this.mWatchLongPress, this.mLongPressTimeout);
                        break;
                    }
                }
                break;
            case 1:
            case 3:
                boolean z2 = !this.mDragging ? this.mLongPressSent : true;
                this.mDragging = false;
                this.mCurrView = null;
                this.mLongPressSent = false;
                removeLongPressCallback();
                if (z2) {
                    return true;
                }
                break;
            case 2:
                if (!(this.mCurrView == null || this.mLongPressSent)) {
                    this.mVelocityTracker.addMovement(ev);
                    float delta = getPos(ev) - this.mInitialTouchPos;
                    float deltaPerpendicular = getPerpendicularPos(ev) - this.mPerpendicularInitialTouchPos;
                    if (Math.abs(delta) > this.mPagingTouchSlop && Math.abs(delta) > Math.abs(deltaPerpendicular)) {
                        this.mCallback.onBeginDrag(this.mCurrView);
                        this.mDragging = true;
                        this.mInitialTouchPos = getPos(ev);
                        this.mTranslation = getTranslation(this.mCurrView);
                        removeLongPressCallback();
                        break;
                    }
                }
        }
        if (!this.mDragging) {
            z = this.mLongPressSent;
        }
        return z;
    }

    public void dismissChild(View view, float velocity, boolean useAccelerateInterpolator) {
        dismissChild(view, velocity, null, 0, useAccelerateInterpolator, 0, false);
    }

    public void dismissChild(View animView, float velocity, Runnable endAction, long delay, boolean useAccelerateInterpolator, long fixedDuration, boolean isDismissAll) {
        boolean animateLeft;
        float newPos;
        long duration;
        HwLog.i("com.android.systemui.SwipeHelper", "dismissChild:animView=" + animView + ", velocity=" + velocity + ", delay=" + delay + ", useAccelerateInterpolator=" + useAccelerateInterpolator + ", fixedDuration=" + fixedDuration + ", isDismissAll=" + isDismissAll);
        final boolean canBeDismissed = this.mCallback.canChildBeDismissed(animView);
        boolean isLayoutRtl = animView.getLayoutDirection() == 1;
        boolean animateUpForMenu = (velocity == 0.0f && (getTranslation(animView) == 0.0f || isDismissAll)) ? this.mSwipeDirection == 1 : false;
        boolean z;
        if (velocity == 0.0f && (getTranslation(animView) == 0.0f || isDismissAll)) {
            z = isLayoutRtl;
        } else {
            z = false;
        }
        if (velocity < 0.0f) {
            animateLeft = true;
        } else if (velocity != 0.0f || getTranslation(animView) >= 0.0f) {
            animateLeft = false;
        } else {
            animateLeft = !isDismissAll;
        }
        if (animateLeft || r11 || animateUpForMenu) {
            newPos = -getSize(animView);
        } else {
            newPos = getSize(animView);
        }
        if (fixedDuration == 0) {
            duration = (long) this.MAX_ESCAPE_ANIMATION_DURATION;
            if (velocity != 0.0f) {
                duration = Math.min(duration, (long) ((int) ((Math.abs(newPos - getTranslation(animView)) * 1000.0f) / Math.abs(velocity))));
            } else {
                duration = (long) this.DEFAULT_ESCAPE_ANIMATION_DURATION;
            }
        } else {
            duration = fixedDuration;
        }
        if (!this.mDisableHwLayers) {
            animView.setLayerType(2, null);
        }
        final View view = animView;
        Animator anim = getViewTranslationAnimator(animView, newPos, new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                SwipeHelper.this.onTranslationUpdate(view, ((Float) animation.getAnimatedValue()).floatValue(), canBeDismissed);
            }
        });
        if (anim != null) {
            if (useAccelerateInterpolator) {
                anim.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
                anim.setDuration(PerfAdjust.getSwipeDeleteOneAnimationDuration(animView, duration));
            } else {
                this.mFlingAnimationUtils.applyDismissing(anim, getTranslation(animView), newPos, PerfAdjust.getSwipeDeleteOneAnimationVelocity(animView, velocity), getSize(animView));
            }
            if (delay > 0) {
                anim.setStartDelay(delay);
            }
            final View view2 = animView;
            final Runnable runnable = endAction;
            anim.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animation) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animation) {
                    SwipeHelper.this.updateSwipeProgressFromOffset(view2, canBeDismissed);
                    SwipeHelper.this.mDismissPendingMap.remove(view2);
                    if (!this.mCancelled) {
                        SwipeHelper.this.mCallback.onChildDismissed(view2);
                    }
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!SwipeHelper.this.mDisableHwLayers) {
                        view2.setLayerType(0, null);
                    }
                }
            });
            prepareDismissAnimation(animView, anim);
            this.mDismissPendingMap.put(animView, anim);
            anim.start();
        }
    }

    protected void prepareDismissAnimation(View view, Animator anim) {
    }

    public void snapChild(final View animView, final float targetLeft, float velocity) {
        final boolean canBeDismissed = this.mCallback.canChildBeDismissed(animView);
        Animator anim = getViewTranslationAnimator(animView, targetLeft, new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                SwipeHelper.this.onTranslationUpdate(animView, ((Float) animation.getAnimatedValue()).floatValue(), canBeDismissed);
            }
        });
        if (anim != null) {
            anim.setDuration(150);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    SwipeHelper.this.mSnappingChild = false;
                    SwipeHelper.this.updateSwipeProgressFromOffset(animView, canBeDismissed);
                    SwipeHelper.this.mCallback.onChildSnappedBack(animView, targetLeft);
                }
            });
            prepareSnapBackAnimation(animView, anim);
            this.mSnappingChild = true;
            anim.start();
        }
    }

    protected void prepareSnapBackAnimation(View view, Animator anim) {
    }

    public void onDownUpdate(View currView) {
    }

    protected void onMoveUpdate(View view, float totalTranslation, float delta) {
    }

    public void onTranslationUpdate(View animView, float value, boolean canBeDismissed) {
        updateSwipeProgressFromOffset(animView, canBeDismissed, value);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("com.android.systemui.SwipeHelper", "onTouchEvent: " + ev + ", mLongPressSent=" + this.mLongPressSent + ", mDragging=" + this.mDragging);
        }
        if (this.mLongPressSent) {
            return true;
        }
        if (this.mDragging) {
            this.mVelocityTracker.addMovement(ev);
            switch (ev.getAction()) {
                case 1:
                case 3:
                    if (this.mCurrView != null) {
                        this.mVelocityTracker.computeCurrentVelocity(1000, getMaxVelocity());
                        float velocity = getVelocity(this.mVelocityTracker);
                        if (!handleUpEvent(ev, this.mCurrView, velocity, getTranslation(this.mCurrView))) {
                            if (isDismissGesture(ev)) {
                                if (this.mCurrView instanceof TaskView) {
                                    BDReporter.e(this.mCurrView.getContext(), 38, "pkg:" + ((TaskView) this.mCurrView).getTask().packageName);
                                }
                                dismissChild(this.mCurrView, velocity, !swipedFastEnough());
                            } else {
                                this.mCallback.onDragCancelled(this.mCurrView);
                                snapChild(this.mCurrView, 0.0f, velocity);
                            }
                            this.mCurrView = null;
                        }
                        this.mDragging = false;
                        break;
                    }
                    break;
                case 2:
                case 4:
                    if (this.mCurrView != null) {
                        float delta = getPos(ev) - this.mInitialTouchPos;
                        float absDelta = Math.abs(delta);
                        if (absDelta >= ((float) getFalsingThreshold())) {
                            this.mTouchAboveFalsingThreshold = true;
                        }
                        if (!this.mCallback.canChildBeDismissed(this.mCurrView)) {
                            float size = getSize(this.mCurrView);
                            float maxScrollDistance = 0.25f * size;
                            delta = absDelta >= size ? delta > 0.0f ? maxScrollDistance : -maxScrollDistance : maxScrollDistance * ((float) Math.sin(((double) (delta / size)) * 1.5707963267948966d));
                        }
                        setTranslation(this.mCurrView, this.mTranslation + delta);
                        updateSwipeProgressFromOffset(this.mCurrView, this.mCanCurrViewBeDimissed);
                        onMoveUpdate(this.mCurrView, this.mTranslation + delta, delta);
                        break;
                    }
                    break;
            }
            return true;
        } else if (this.mCallback.getChildAtPosition(ev) != null) {
            onInterceptTouchEvent(ev);
            return true;
        } else {
            removeLongPressCallback();
            return false;
        }
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mFalsingThreshold) * this.mCallback.getFalsingThresholdFactor());
    }

    private float getMaxVelocity() {
        return ((float) this.MAX_DISMISS_VELOCITY) * this.mDensityScale;
    }

    protected float getEscapeVelocity() {
        return getUnscaledEscapeVelocity() * this.mDensityScale;
    }

    protected float getUnscaledEscapeVelocity() {
        return this.SWIPE_ESCAPE_VELOCITY;
    }

    protected long getMaxEscapeAnimDuration() {
        return (long) this.MAX_ESCAPE_ANIMATION_DURATION;
    }

    protected boolean swipedFarEnough() {
        boolean z = false;
        if (this.mCurrView == null) {
            HwLog.e("com.android.systemui.SwipeHelper", "mCurrView==null");
            return false;
        }
        float translation = getTranslation(this.mCurrView);
        HwLog.i("com.android.systemui.SwipeHelper", "translation:" + translation + ";mCurrView.getLeft():" + this.mCurrView.getLeft());
        if (Math.abs(translation) > ((float) ((this.mWindowWidth / 3) - this.mCurrView.getLeft()))) {
            z = true;
        }
        return z;
    }

    protected boolean isDismissGesture(MotionEvent ev) {
        boolean falsingDetected = this.mCallback.isAntiFalsingNeeded();
        falsingDetected = this.mFalsingManager.isClassiferEnabled() ? falsingDetected ? this.mFalsingManager.isFalseTouch() : false : falsingDetected && !this.mTouchAboveFalsingThreshold;
        if (falsingDetected || ((!swipedFastEnough() && !swipedFarEnough()) || ev.getActionMasked() != 1)) {
            return false;
        }
        return this.mCallback.canChildBeDismissed(this.mCurrView);
    }

    protected boolean swipedFastEnough() {
        float velocity = getVelocity(this.mVelocityTracker);
        float translation = getTranslation(this.mCurrView);
        if (Math.abs(velocity) <= getEscapeVelocity()) {
            return false;
        }
        boolean z;
        if (velocity > 0.0f) {
            z = true;
        } else {
            z = false;
        }
        if (z == (translation > 0.0f)) {
            return true;
        }
        return false;
    }

    protected boolean handleUpEvent(MotionEvent ev, View animView, float velocity, float translation) {
        return false;
    }
}
