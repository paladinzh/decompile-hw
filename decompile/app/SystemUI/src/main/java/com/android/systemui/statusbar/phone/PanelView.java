package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.JanklogUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public abstract class PanelView extends FrameLayout {
    public static final String TAG = PanelView.class.getSimpleName();
    private boolean mAnimatingOnDown;
    PanelBar mBar;
    private Interpolator mBounceInterpolator;
    private boolean mClosing;
    private boolean mCollapseAfterPeek;
    private boolean mCollapsedAndHeadsUpOnDown;
    private boolean mDrawnAfterActionDown = true;
    private Runnable mDropdownPerfRunnable = new Runnable() {
        public void run() {
            if (!PanelView.this.mJustPeeked && PanelView.this.isFullyCollapsed()) {
                PerfDebugUtils.beginSystraceSection("PanelView_mDropdownPerfRunnable_run");
                if (HwRecentsHelper.IS_EMUI_LITE || PanelView.this.mInitialTouchY <= 0.0f) {
                    PanelView.this.setExpandedHeight(PerfAdjust.getDefaultStartPeekHeightLight());
                } else {
                    PanelView.this.setExpandedHeight(Math.min(PanelView.this.mInitialTouchY, 12.0f));
                }
                PerfDebugUtils.endSystraceSection();
            }
        }
    };
    private float mExpandedFraction = 0.0f;
    protected float mExpandedHeight = 0.0f;
    protected boolean mExpanding;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private final Runnable mFlingCollapseRunnable = new Runnable() {
        public void run() {
            PanelView.this.fling(0.0f, false, PanelView.this.mNextCollapseSpeedUpFactor, false);
        }
    };
    private boolean mGestureWaitForTouchSlop;
    private boolean mHasLayoutedSinceDown;
    protected HeadsUpManager mHeadsUpManager;
    private ValueAnimator mHeightAnimator;
    protected boolean mHintAnimationRunning;
    private float mHintDistance;
    private boolean mIgnoreXTouchSlop;
    private float mInitialOffsetOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mInstantExpanding;
    private boolean mJustPeeked;
    protected KeyguardBottomAreaView mKeyguardBottomArea;
    private boolean mMotionAborted;
    private float mNextCollapseSpeedUpFactor = 1.0f;
    private boolean mOverExpandedBeforeFling;
    private boolean mPanelClosedOnDown;
    private ObjectAnimator mPeekAnimator;
    private float mPeekHeight;
    private boolean mPeekPending;
    private Runnable mPeekRunnable = new Runnable() {
        public void run() {
            PanelView.this.mPeekPending = false;
            PanelView.this.runPeekAnimation();
        }
    };
    private boolean mPeekTouching;
    protected final Runnable mPostCollapseRunnable = new Runnable() {
        public void run() {
            PanelView.this.collapse(false, 1.0f);
        }
    };
    protected PhoneStatusBar mStatusBar;
    private boolean mTouchAboveFalsingThreshold;
    private boolean mTouchDisabled;
    protected int mTouchSlop;
    private boolean mTouchSlopExceeded;
    private boolean mTouchStartedInEmptyArea;
    protected boolean mTracking;
    private int mTrackingPointer;
    private int mUnlockFalsingThreshold;
    private boolean mUpdateFlingOnLayout;
    private float mUpdateFlingVelocity;
    private boolean mUpwardsWhenTresholdReached;
    private VelocityTrackerInterface mVelocityTracker;
    private String mViewName;

    protected abstract boolean fullyExpandedClearAllVisible();

    protected abstract float getCannedFlingDurationFactor();

    protected abstract int getClearAllHeight();

    public abstract boolean getFastUnlockMode();

    protected abstract int getMaxPanelHeight();

    protected abstract float getOverExpansionAmount();

    protected abstract float getOverExpansionPixels();

    protected abstract float getPeekHeight();

    protected abstract boolean hasConflictingGestures();

    protected abstract boolean isClearAllVisible();

    protected abstract boolean isInContentBounds(float f, float f2);

    protected abstract boolean isPanelVisibleBecauseOfHeadsUp();

    protected abstract boolean isTrackingBlocked();

    protected abstract void onHeightUpdated(float f);

    protected abstract boolean onMiddleClicked();

    public abstract void resetViews();

    protected abstract void setOverExpansion(float f, boolean z);

    protected abstract boolean shouldGestureIgnoreXTouchSlop(float f, float f2);

    protected void onExpandingFinished() {
        this.mBar.onExpandingFinished();
    }

    protected void onExpandingStarted() {
    }

    private void notifyExpandingStarted() {
        if (!this.mExpanding) {
            this.mExpanding = true;
            onExpandingStarted();
        }
    }

    protected final void notifyExpandingFinished() {
        endClosing();
        if (this.mExpanding) {
            this.mExpanding = false;
            onExpandingFinished();
        }
    }

    private void schedulePeek() {
        HwLog.i(TAG, "schedulePeek");
        PerfDebugUtils.beginSystraceSection("PanelView_schedulePeek");
        this.mPeekPending = true;
        this.mDrawnAfterActionDown = false;
        this.mDropdownPerfRunnable.run();
        postOnAnimationDelayed(this.mPeekRunnable, PerfAdjust.adjustPeekRunnableTimeout((long) ViewConfiguration.getTapTimeout()));
        notifyBarPanelExpansionChanged();
        PerfDebugUtils.endSystraceSection();
    }

    private void runPeekAnimation() {
        this.mPeekHeight = getPeekHeight();
        if (this.mHeightAnimator == null) {
            this.mPeekAnimator = ObjectAnimator.ofFloat(this, "expandedHeight", new float[]{PerfAdjust.getDefaultStartPeekHeightLight(), this.mPeekHeight}).setDuration(250);
            this.mPeekAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            this.mPeekAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animation) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animation) {
                    PanelView.this.mPeekAnimator = null;
                    if (PanelView.this.mCollapseAfterPeek && !this.mCancelled) {
                        PanelView.this.postOnAnimation(PanelView.this.mPostCollapseRunnable);
                    }
                    PanelView.this.mCollapseAfterPeek = false;
                }
            });
            notifyExpandingStarted();
            this.mPeekAnimator.start();
            this.mJustPeeked = true;
        }
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.6f);
        this.mBounceInterpolator = new BounceInterpolator();
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    protected void loadDimens() {
        Resources res = getContext().getResources();
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mHintDistance = res.getDimension(R.dimen.hint_move_distance);
        this.mUnlockFalsingThreshold = res.getDimensionPixelSize(R.dimen.unlock_falsing_threshold);
    }

    private void trackMovement(MotionEvent event) {
        float deltaX = event.getRawX() - event.getX();
        float deltaY = event.getRawY() - event.getY();
        event.offsetLocation(deltaX, deltaY);
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(event);
        }
        event.offsetLocation(-deltaX, -deltaY);
    }

    public void setTouchDisabled(boolean disabled) {
        this.mTouchDisabled = disabled;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDrawnAfterActionDown) {
            super.onDraw(canvas);
            return;
        }
        this.mDrawnAfterActionDown = true;
        PerfDebugUtils.beginSystraceSection("PanelView_FIRST_ON_DRAW");
        super.onDraw(canvas);
        PerfDebugUtils.endSystraceSection();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mInstantExpanding || this.mTouchDisabled || (this.mMotionAborted && event.getActionMasked() != 0)) {
            HwLog.i(TAG, "onTouchEvent::return because of mInstantExpanding=" + this.mInstantExpanding + ", mTouchDisabled=" + this.mTouchDisabled);
            return false;
        } else if (isFullyCollapsed() && event.isFromSource(8194)) {
            if (event.getAction() == 1) {
                expand(true);
            }
            return true;
        } else {
            boolean hasPinnedHeadsUp;
            int pointerIndex = event.findPointerIndex(this.mTrackingPointer);
            if (pointerIndex < 0) {
                pointerIndex = 0;
                this.mTrackingPointer = event.getPointerId(0);
            }
            float x = event.getX(pointerIndex);
            float y = event.getY(pointerIndex);
            int action = event.getActionMasked();
            if (action == 0) {
                this.mGestureWaitForTouchSlop = !isFullyCollapsed() ? hasConflictingGestures() : true;
                this.mIgnoreXTouchSlop = !isFullyCollapsed() ? shouldGestureIgnoreXTouchSlop(x, y) : true;
            }
            switch (action) {
                case 0:
                    PerfDebugUtils.beginSystraceSection("PanelView_onTouchEvent_ACTION_DOWN");
                    startExpandMotion(x, y, false, this.mExpandedHeight);
                    this.mJustPeeked = false;
                    this.mPanelClosedOnDown = isFullyCollapsed();
                    this.mHasLayoutedSinceDown = false;
                    this.mUpdateFlingOnLayout = false;
                    this.mMotionAborted = false;
                    this.mPeekTouching = this.mPanelClosedOnDown;
                    this.mTouchAboveFalsingThreshold = false;
                    if (isFullyCollapsed()) {
                        hasPinnedHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
                    } else {
                        hasPinnedHeadsUp = false;
                    }
                    this.mCollapsedAndHeadsUpOnDown = hasPinnedHeadsUp;
                    if (this.mVelocityTracker == null) {
                        initVelocityTracker();
                    }
                    trackMovement(event);
                    if (!(this.mGestureWaitForTouchSlop && ((this.mHeightAnimator == null || this.mHintAnimationRunning) && !this.mPeekPending && this.mPeekAnimator == null))) {
                        cancelHeightAnimator();
                        cancelPeek();
                        hasPinnedHeadsUp = ((this.mHeightAnimator == null || this.mHintAnimationRunning) && !this.mPeekPending) ? this.mPeekAnimator != null : true;
                        this.mTouchSlopExceeded = hasPinnedHeadsUp;
                        onTrackingStarted();
                    }
                    if (!(!isFullyCollapsed() || this.mHeadsUpManager.hasPinnedHeadsUp() || BackgrounCallingLayout.getCalllinearlayoutShowing())) {
                        JanklogUtils.eventBegin(133);
                        PerfDebugUtils.beginSystraceSection("PanelView_onTouchEvent_Call_PERF_EVENT");
                        JanklogUtils.perfEvent(11);
                        PerfDebugUtils.endSystraceSection();
                        schedulePeek();
                        JanklogUtils.eventEnd(133, "Dropdown response");
                    }
                    PerfDebugUtils.endSystraceSection();
                    break;
                case 1:
                case 3:
                    PerfDebugUtils.beginSystraceSection("PanelView_onTouchEvent_ACTION_UP_OR_CANCEL");
                    trackMovement(event);
                    endMotionEvent(event, x, y, false);
                    PerfDebugUtils.endSystraceSection();
                    reporter(event);
                    break;
                case 2:
                    PerfDebugUtils.beginSystraceSection("PanelView_onTouchEvent_ACTION_MOVE");
                    float h = y - this.mInitialTouchY;
                    if (Math.abs(h) > ((float) this.mTouchSlop) && (Math.abs(h) > Math.abs(x - this.mInitialTouchX) || this.mIgnoreXTouchSlop)) {
                        this.mTouchSlopExceeded = true;
                        if (!(!this.mGestureWaitForTouchSlop || this.mTracking || this.mCollapsedAndHeadsUpOnDown)) {
                            if (!(this.mJustPeeked || this.mInitialOffsetOnTouch == 0.0f)) {
                                startExpandMotion(x, y, false, this.mExpandedHeight);
                                h = 0.0f;
                            }
                            cancelHeightAnimator();
                            removeCallbacks(this.mPeekRunnable);
                            this.mPeekPending = false;
                            onTrackingStarted();
                        }
                    }
                    float newHeight = Math.max(0.0f, this.mInitialOffsetOnTouch + h);
                    if (newHeight > this.mPeekHeight) {
                        if (this.mPeekAnimator != null) {
                            this.mPeekAnimator.cancel();
                        }
                        this.mJustPeeked = false;
                    }
                    if ((-h) >= ((float) getFalsingThreshold())) {
                        this.mTouchAboveFalsingThreshold = true;
                        this.mUpwardsWhenTresholdReached = isDirectionUpwards(x, y);
                    }
                    if (!this.mJustPeeked && ((!this.mGestureWaitForTouchSlop || this.mTracking) && !isTrackingBlocked())) {
                        if (this.mDrawnAfterActionDown) {
                            PerfDebugUtils.beginSystraceSection("PanelView_onTouchEvent_Call_setExpandedHeightInternal");
                            setExpandedHeightInternal(newHeight);
                            PerfDebugUtils.endSystraceSection();
                        } else {
                            Log.i(TAG, "action_move don't setExpandedHeightInternal while first drawn not processed");
                        }
                    }
                    trackMovement(event);
                    PerfDebugUtils.endSystraceSection();
                    break;
                case 5:
                    if (this.mStatusBar.getBarState() == 1) {
                        this.mMotionAborted = true;
                        endMotionEvent(event, x, y, true);
                        return false;
                    }
                    break;
                case 6:
                    int upPointer = event.getPointerId(event.getActionIndex());
                    if (this.mTrackingPointer == upPointer) {
                        int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                        float newY = event.getY(newIndex);
                        float newX = event.getX(newIndex);
                        this.mTrackingPointer = event.getPointerId(newIndex);
                        startExpandMotion(newX, newY, true, this.mExpandedHeight);
                        break;
                    }
                    break;
            }
            if (this.mGestureWaitForTouchSlop) {
                hasPinnedHeadsUp = this.mTracking;
            } else {
                hasPinnedHeadsUp = true;
            }
            return hasPinnedHeadsUp;
        }
    }

    private void reporter(final MotionEvent event) {
        new Thread() {
            public void run() {
                PerfDebugUtils.beginSystraceSection("BDReporter_EVENT_ID_FLING_CLOSE_SYSTEMUI_PANEL");
                if (event.getY() - PanelView.this.mInitialTouchY < 0.0f) {
                    BDReporter.e(PanelView.this.getContext(), 39, "expand:false");
                } else {
                    BDReporter.e(PanelView.this.getContext(), 39, "expand:true");
                }
                PerfDebugUtils.endSystraceSection();
            }
        }.start();
    }

    private boolean isDirectionUpwards(float x, float y) {
        boolean z = false;
        float xDiff = x - this.mInitialTouchX;
        float yDiff = y - this.mInitialTouchY;
        if (yDiff >= 0.0f) {
            return false;
        }
        if (Math.abs(yDiff) >= Math.abs(xDiff)) {
            z = true;
        }
        return z;
    }

    protected void startExpandMotion(float newX, float newY, boolean startTracking, float expandedHeight) {
        this.mInitialOffsetOnTouch = expandedHeight;
        this.mInitialTouchY = newY;
        this.mInitialTouchX = newX;
        if (startTracking) {
            this.mTouchSlopExceeded = true;
            setExpandedHeight(this.mInitialOffsetOnTouch);
            onTrackingStarted();
        }
    }

    private void endMotionEvent(MotionEvent event, float x, float y, boolean forceCancel) {
        this.mTrackingPointer = -1;
        if ((this.mTracking && this.mTouchSlopExceeded) || Math.abs(x - this.mInitialTouchX) > ((float) this.mTouchSlop) || Math.abs(y - this.mInitialTouchY) > ((float) this.mTouchSlop) || event.getActionMasked() == 3 || forceCancel) {
            boolean expand;
            float vel = 0.0f;
            float vectorVel = 0.0f;
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.computeCurrentVelocity(1000);
                vel = this.mVelocityTracker.getYVelocity();
                vectorVel = (float) Math.hypot((double) this.mVelocityTracker.getXVelocity(), (double) this.mVelocityTracker.getYVelocity());
            }
            if (flingExpands(vel, vectorVel, x, y) || event.getActionMasked() == 3) {
                expand = true;
            } else {
                expand = forceCancel;
            }
            expand = isFalseTouchInTalking(event, expand);
            DozeLog.traceFling(expand, this.mTouchAboveFalsingThreshold, this.mStatusBar.isFalsingThresholdNeeded(), this.mStatusBar.isWakeUpComingFromTouch());
            if (!expand && this.mStatusBar.getBarState() == 1) {
                float displayDensity = this.mStatusBar.getDisplayDensity();
                EventLogTags.writeSysuiLockscreenGesture(1, (int) Math.abs((y - this.mInitialTouchY) / displayDensity), (int) Math.abs(vel / displayDensity));
            }
            fling(vel, expand, isFalseTouch(x, y));
            onTrackingStopped(expand);
            boolean z = expand && this.mPanelClosedOnDown && !this.mHasLayoutedSinceDown;
            this.mUpdateFlingOnLayout = z;
            if (this.mUpdateFlingOnLayout) {
                this.mUpdateFlingVelocity = vel;
            }
        } else {
            onTrackingStopped(onEmptySpaceClick(this.mInitialTouchX));
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        this.mPeekTouching = false;
    }

    private boolean isFalseTouchInTalking(MotionEvent event, boolean expand) {
        if (event.getActionMasked() != 3 || HwPhoneStatusBar.getInstance().getCallingLayout() == null || !HwPhoneStatusBar.getInstance().getCallingLayout().isTalking()) {
            return expand;
        }
        HwLog.i(TAG, "expand is canceled in talking");
        return false;
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mUnlockFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    protected void onTrackingStopped(boolean expand) {
        HwLog.i(TAG, "onTrackingStopped: " + expand);
        this.mTracking = false;
        this.mBar.onTrackingStopped(expand);
        notifyBarPanelExpansionChanged();
    }

    protected void onTrackingStarted() {
        HwLog.i(TAG, "onTrackingStarted");
        endClosing();
        this.mTracking = true;
        this.mCollapseAfterPeek = false;
        this.mBar.onTrackingStarted();
        notifyExpandingStarted();
        notifyBarPanelExpansionChanged();
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean z = true;
        if (this.mInstantExpanding || (this.mMotionAborted && event.getActionMasked() != 0)) {
            return false;
        }
        int pointerIndex = event.findPointerIndex(this.mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            this.mTrackingPointer = event.getPointerId(0);
        }
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        boolean scrolledToBottom = isScrolledToBottom();
        switch (event.getActionMasked()) {
            case 0:
                this.mStatusBar.userActivity();
                this.mAnimatingOnDown = this.mHeightAnimator != null;
                if ((!this.mAnimatingOnDown || !this.mClosing || this.mHintAnimationRunning) && !this.mPeekPending && this.mPeekAnimator == null) {
                    this.mInitialTouchY = y;
                    this.mInitialTouchX = x;
                    if (isInContentBounds(x, y)) {
                        z = false;
                    }
                    this.mTouchStartedInEmptyArea = z;
                    this.mTouchSlopExceeded = false;
                    this.mJustPeeked = false;
                    this.mMotionAborted = false;
                    this.mPanelClosedOnDown = isFullyCollapsed();
                    this.mCollapsedAndHeadsUpOnDown = false;
                    this.mHasLayoutedSinceDown = false;
                    this.mUpdateFlingOnLayout = false;
                    this.mTouchAboveFalsingThreshold = false;
                    initVelocityTracker();
                    trackMovement(event);
                    break;
                }
                cancelHeightAnimator();
                cancelPeek();
                this.mTouchSlopExceeded = true;
                return true;
                break;
            case 1:
            case 3:
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
            case 2:
                float h = y - this.mInitialTouchY;
                trackMovement(event);
                if (scrolledToBottom || this.mTouchStartedInEmptyArea || this.mAnimatingOnDown) {
                    float hAbs = Math.abs(h);
                    if ((h < ((float) (-this.mTouchSlop)) || (this.mAnimatingOnDown && hAbs > ((float) this.mTouchSlop))) && hAbs > Math.abs(x - this.mInitialTouchX)) {
                        cancelHeightAnimator();
                        startExpandMotion(x, y, true, this.mExpandedHeight);
                        return true;
                    }
                }
            case 5:
                if (this.mStatusBar.getBarState() == 1) {
                    this.mMotionAborted = true;
                    if (this.mVelocityTracker != null) {
                        this.mVelocityTracker.recycle();
                        this.mVelocityTracker = null;
                        break;
                    }
                }
                break;
            case 6:
                int upPointer = event.getPointerId(event.getActionIndex());
                if (this.mTrackingPointer == upPointer) {
                    int newIndex;
                    if (event.getPointerId(0) != upPointer) {
                        newIndex = 0;
                    } else {
                        newIndex = 1;
                    }
                    this.mTrackingPointer = event.getPointerId(newIndex);
                    this.mInitialTouchX = event.getX(newIndex);
                    this.mInitialTouchY = event.getY(newIndex);
                    break;
                }
                break;
        }
        return false;
    }

    protected void cancelHeightAnimator() {
        if (this.mHeightAnimator != null) {
            this.mHeightAnimator.cancel();
        }
        endClosing();
    }

    private void endClosing() {
        if (this.mClosing) {
            this.mClosing = false;
            onClosingFinished();
        }
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTrackerFactory.obtain(getContext());
    }

    protected boolean isScrolledToBottom() {
        return true;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        loadDimens();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadDimens();
    }

    protected boolean flingExpands(float vel, float vectorVel, float x, float y) {
        boolean z = true;
        if (isFalseTouch(x, y)) {
            return true;
        }
        if (Math.abs(vectorVel) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            if (getExpandedFraction() <= 0.5f) {
                z = false;
            }
            return z;
        }
        if (vel <= 0.0f) {
            z = false;
        }
        return z;
    }

    private boolean isFalseTouch(float x, float y) {
        boolean z = false;
        if (!this.mStatusBar.isFalsingThresholdNeeded()) {
            return false;
        }
        if (!this.mTouchAboveFalsingThreshold) {
            return true;
        }
        if (this.mUpwardsWhenTresholdReached) {
            return false;
        }
        if (!isDirectionUpwards(x, y)) {
            z = true;
        }
        return z;
    }

    protected void fling(float vel, boolean expand) {
        fling(vel, expand, 1.0f, false);
    }

    protected void fling(float vel, boolean expand, boolean expandBecauseOfFalsing) {
        fling(vel, expand, 1.0f, expandBecauseOfFalsing);
    }

    protected void fling(float vel, boolean expand, float collapseSpeedUpFactor, boolean expandBecauseOfFalsing) {
        cancelPeek();
        float target = expand ? (float) getMaxPanelHeight() : 0.0f;
        if (!expand) {
            this.mClosing = true;
        }
        flingToHeight(vel, expand, target, collapseSpeedUpFactor, expandBecauseOfFalsing);
    }

    protected void flingToHeight(float vel, boolean expand, float target, float collapseSpeedUpFactor, boolean expandBecauseOfFalsing) {
        boolean clearAllExpandHack;
        boolean z = true;
        HwLog.i(TAG, "flingToHeight: vel=" + vel + ", expand=" + expand + ", target=" + target + ", collapseSpeedUpFactor=" + collapseSpeedUpFactor + ", expandBecauseOfFalsing=" + expandBecauseOfFalsing);
        if (expand && fullyExpandedClearAllVisible() && this.mExpandedHeight < ((float) (getMaxPanelHeight() - getClearAllHeight()))) {
            boolean z2;
            if (isClearAllVisible()) {
                z2 = false;
            } else {
                z2 = true;
            }
            clearAllExpandHack = z2;
        } else {
            clearAllExpandHack = false;
        }
        if (clearAllExpandHack) {
            target = (float) (getMaxPanelHeight() - getClearAllHeight());
        }
        if (target == this.mExpandedHeight || (getOverExpansionAmount() > 0.0f && expand)) {
            notifyExpandingFinished();
            return;
        }
        if (getOverExpansionAmount() <= 0.0f) {
            z = false;
        }
        this.mOverExpandedBeforeFling = z;
        Animator animator = createHeightAnimator(target);
        if (expand) {
            if (expandBecauseOfFalsing) {
                vel = 0.0f;
            }
            this.mFlingAnimationUtils.apply(animator, this.mExpandedHeight, target, vel, (float) getHeight());
            if (expandBecauseOfFalsing) {
                animator.setDuration(350);
            }
        } else {
            this.mFlingAnimationUtils.applyDismissing(animator, this.mExpandedHeight, target, vel, (float) getHeight());
            if (this.mStatusBar != null && this.mStatusBar.getFpUnlockingStatus()) {
                this.mHeightAnimator = null;
                setExpandedHeightInternal(0.0f);
                notifyExpandingFinished();
                notifyBarPanelExpansionChanged();
                return;
            } else if (vel == 0.0f) {
                animator.setDuration((long) ((((float) animator.getDuration()) * getCannedFlingDurationFactor()) / collapseSpeedUpFactor));
            }
        }
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (clearAllExpandHack && !this.mCancelled) {
                    PanelView.this.setExpandedHeightInternal((float) PanelView.this.getMaxPanelHeight());
                }
                PanelView.this.mHeightAnimator = null;
                if (!this.mCancelled) {
                    PanelView.this.notifyExpandingFinished();
                }
                PanelView.this.notifyBarPanelExpansionChanged();
            }
        });
        this.mHeightAnimator = animator;
        animator.start();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mViewName = getResources().getResourceName(getId());
    }

    public void setExpandedHeight(float height) {
        setExpandedHeightInternal(getOverExpansionPixels() + height);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mStatusBar.onPanelLaidOut();
        requestPanelHeightUpdate();
        this.mHasLayoutedSinceDown = true;
        if (this.mUpdateFlingOnLayout) {
            abortAnimations();
            fling(this.mUpdateFlingVelocity, true);
            this.mUpdateFlingOnLayout = false;
        }
    }

    protected void requestPanelHeightUpdate() {
        float currentMaxPanelHeight = (float) getMaxPanelHeight();
        if ((!this.mTracking || isTrackingBlocked()) && this.mHeightAnimator == null && !isFullyCollapsed() && currentMaxPanelHeight != this.mExpandedHeight && !this.mPeekPending && this.mPeekAnimator == null && !this.mPeekTouching) {
            setExpandedHeight(currentMaxPanelHeight);
        }
    }

    public void setExpandedHeightInternal(float h) {
        float f = 0.0f;
        PerfDebugUtils.beginSystraceSection("PanelView_setExpandedHeightInternal");
        float fhWithoutOverExpansion = ((float) getMaxPanelHeight()) - getOverExpansionAmount();
        if (this.mHeightAnimator == null) {
            float overExpansionPixels = Math.max(0.0f, h - fhWithoutOverExpansion);
            if (getOverExpansionPixels() != overExpansionPixels && this.mTracking) {
                setOverExpansion(overExpansionPixels, true);
            }
            this.mExpandedHeight = Math.min(h, fhWithoutOverExpansion) + getOverExpansionAmount();
        } else {
            this.mExpandedHeight = h;
            if (this.mOverExpandedBeforeFling) {
                setOverExpansion(Math.max(0.0f, h - fhWithoutOverExpansion), false);
            }
        }
        this.mExpandedHeight = Math.max(0.0f, this.mExpandedHeight);
        if (fhWithoutOverExpansion != 0.0f) {
            f = this.mExpandedHeight / fhWithoutOverExpansion;
        }
        this.mExpandedFraction = Math.min(1.0f, f);
        onHeightUpdated(this.mExpandedHeight);
        notifyBarPanelExpansionChanged();
        PerfDebugUtils.endSystraceSection();
    }

    public void setExpandedFraction(float frac) {
        setExpandedHeight(((float) getMaxPanelHeight()) * frac);
    }

    public float getExpandedHeight() {
        return this.mExpandedHeight;
    }

    public float getExpandedFraction() {
        return this.mExpandedFraction;
    }

    public boolean isFullyExpanded() {
        return this.mExpandedHeight >= ((float) getMaxPanelHeight());
    }

    public boolean isFullyCollapsed() {
        return this.mExpandedHeight <= 0.0f;
    }

    public boolean isCollapsing() {
        return this.mClosing;
    }

    public boolean isTracking() {
        return this.mTracking;
    }

    public void setBar(PanelBar panelBar) {
        this.mBar = panelBar;
    }

    public void collapse(boolean delayed, float speedUpFactor) {
        this.mInstantExpanding = false;
        if (this.mPeekPending || this.mPeekAnimator != null) {
            this.mCollapseAfterPeek = true;
            if (this.mPeekPending) {
                removeCallbacks(this.mPeekRunnable);
                this.mPeekRunnable.run();
            }
        } else if (!isFullyCollapsed() && !this.mTracking && !this.mClosing) {
            cancelHeightAnimator();
            notifyExpandingStarted();
            this.mClosing = true;
            if (delayed) {
                this.mNextCollapseSpeedUpFactor = speedUpFactor;
                postDelayed(this.mFlingCollapseRunnable, 120);
                return;
            }
            fling(0.0f, false, speedUpFactor, false);
        }
    }

    public void cancelPeek() {
        HwLog.i(TAG, "cancelPeek: " + this.mPeekPending);
        boolean z = this.mPeekPending;
        if (this.mPeekAnimator != null) {
            z = true;
            this.mPeekAnimator.cancel();
        }
        removeCallbacks(this.mPeekRunnable);
        this.mPeekPending = false;
        if (z) {
            notifyBarPanelExpansionChanged();
        }
    }

    public void expand(final boolean animate) {
        HwLog.i(TAG, "expand: " + animate);
        if (isFullyCollapsed() || isCollapsing()) {
            this.mInstantExpanding = true;
            this.mUpdateFlingOnLayout = false;
            abortAnimations();
            cancelPeek();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            if (this.mExpanding) {
                notifyExpandingFinished();
            }
            notifyBarPanelExpansionChanged();
            getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (PanelView.this.mInstantExpanding) {
                        if (PanelView.this.mStatusBar.getStatusBarWindow().getHeight() != PanelView.this.mStatusBar.getStatusBarHeight()) {
                            PanelView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if (animate) {
                                PanelView.this.notifyExpandingStarted();
                                PanelView.this.fling(0.0f, true);
                            } else {
                                PanelView.this.setExpandedFraction(1.0f);
                            }
                            HwLog.i(PanelView.TAG, "expand: start");
                            PanelView.this.mInstantExpanding = false;
                        }
                        return;
                    }
                    PanelView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            requestLayout();
            return;
        }
        HwLog.i(TAG, "expand return");
    }

    public void instantCollapse() {
        HwLog.i(TAG, "instantCollapse");
        abortAnimations();
        setExpandedFraction(0.0f);
        if (this.mExpanding) {
            notifyExpandingFinished();
        }
        if (this.mInstantExpanding) {
            this.mInstantExpanding = false;
            notifyBarPanelExpansionChanged();
        }
    }

    private void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        removeCallbacks(this.mPostCollapseRunnable);
        removeCallbacks(this.mFlingCollapseRunnable);
    }

    protected void onClosingFinished() {
        this.mBar.onClosingFinished();
    }

    protected void startUnlockHintAnimation() {
        if (this.mHeightAnimator == null && !this.mTracking) {
            cancelPeek();
            notifyExpandingStarted();
            startUnlockHintAnimationPhase1(new Runnable() {
                public void run() {
                    PanelView.this.notifyExpandingFinished();
                    PanelView.this.mStatusBar.onHintFinished();
                    PanelView.this.mHintAnimationRunning = false;
                }
            });
            this.mStatusBar.onUnlockHintStarted();
            this.mHintAnimationRunning = true;
        }
    }

    private void startUnlockHintAnimationPhase1(final Runnable onAnimationFinished) {
        ValueAnimator animator = createHeightAnimator(Math.max(0.0f, ((float) getMaxPanelHeight()) - this.mHintDistance));
        animator.setDuration(250);
        animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        animator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (this.mCancelled) {
                    PanelView.this.mHeightAnimator = null;
                    onAnimationFinished.run();
                    return;
                }
                PanelView.this.startUnlockHintAnimationPhase2(onAnimationFinished);
            }
        });
        animator.start();
        this.mHeightAnimator = animator;
        if (this.mKeyguardBottomArea != null) {
            this.mKeyguardBottomArea.getIndicationView().animate().translationY(-this.mHintDistance).setDuration(250).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(new Runnable() {
                public void run() {
                    PanelView.this.mKeyguardBottomArea.getIndicationView().animate().translationY(0.0f).setDuration(450).setInterpolator(PanelView.this.mBounceInterpolator).start();
                }
            }).start();
        }
    }

    private void startUnlockHintAnimationPhase2(final Runnable onAnimationFinished) {
        ValueAnimator animator = createHeightAnimator((float) getMaxPanelHeight());
        animator.setDuration(450);
        animator.setInterpolator(this.mBounceInterpolator);
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                PanelView.this.mHeightAnimator = null;
                onAnimationFinished.run();
                HwLog.i(PanelView.TAG, "startUnlockHintAnimationPhase2 onAnimationEnd");
                PanelView.this.notifyBarPanelExpansionChanged();
            }
        });
        animator.start();
        this.mHeightAnimator = animator;
    }

    private ValueAnimator createHeightAnimator(float targetHeight) {
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.mExpandedHeight, targetHeight});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                PanelView.this.setExpandedHeightInternal(((Float) animation.getAnimatedValue()).floatValue());
            }
        });
        return animator;
    }

    protected void notifyBarPanelExpansionChanged() {
        boolean z = true;
        PerfDebugUtils.beginSystraceSection("PanelView_notifyBarPanelExpansionChanged");
        PanelBar panelBar = this.mBar;
        float f = this.mExpandedFraction;
        if (!(this.mExpandedFraction > 0.0f || this.mPeekPending || this.mPeekAnimator != null || this.mInstantExpanding || isPanelVisibleBecauseOfHeadsUp() || this.mTracking || this.mHeightAnimator != null)) {
            z = false;
        }
        panelBar.panelExpansionChanged(f, z);
        PerfDebugUtils.endSystraceSection();
    }

    protected boolean onEmptySpaceClick(float x) {
        if (this.mHintAnimationRunning) {
            return true;
        }
        return onMiddleClicked();
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }
}
