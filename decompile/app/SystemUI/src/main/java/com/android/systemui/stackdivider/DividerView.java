package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DividerSnapAlgorithm.SnapTarget;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.BDReporter;

public class DividerView extends FrameLayout implements OnTouchListener, OnComputeInternalInsetsListener {
    private static final PathInterpolator DIM_INTERPOLATOR = new PathInterpolator(0.23f, 0.87f, 0.52f, -0.11f);
    private static final Interpolator IME_ADJUST_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);
    private static final PathInterpolator SLOWDOWN_INTERPOLATOR = new PathInterpolator(0.5f, 1.0f, 0.5f, 1.0f);
    private boolean mAdjustedForIme;
    private View mBackground;
    private int mCenterX;
    private int mCenterY;
    private ValueAnimator mCurrentAnimator;
    private int mDisplayHeight;
    private final Rect mDisplayRect;
    private int mDisplayWidth;
    private int mDividerInsets;
    private DividerMenusView mDividerMenusView;
    private int mDividerSize;
    private int mDividerWindowWidth;
    private int mDockSide;
    private final Rect mDockedInsetRect;
    private final Rect mDockedRect;
    private boolean mDockedStackMinimized;
    private final Rect mDockedTaskRect;
    private boolean mEntranceAnimationRunning;
    private boolean mExitAnimationRunning;
    private int mExitStartPosition;
    private FlingAnimationUtils mFlingAnimationUtils;
    private GestureDetector mGestureDetector;
    private boolean mGrowRecents;
    private ImageView mHandle;
    private final AccessibilityDelegate mHandleDelegate;
    private final Handler mHandler;
    private final Rect mLastResizeRect;
    private int mLongPressEntraceAnimDuration;
    private boolean mMoving;
    private final Rect mOtherInsetRect;
    private final Rect mOtherRect;
    private final Rect mOtherTaskRect;
    private final Runnable mResetBackgroundRunnable;
    private DividerSnapAlgorithm mSnapAlgorithm;
    private final Rect mStableInsets;
    private int mStartPosition;
    private int mStartX;
    private int mStartY;
    private DividerState mState;
    private long mSurfaceFlingerOffsetMs;
    private final int[] mTempInt2;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private DividerWindowManager mWindowManager;
    private final WindowManagerProxy mWindowManagerProxy;

    final /* synthetic */ class -void_onBusEvent_com_android_systemui_recents_events_ui_RecentsDrawnEvent_drawnEvent_LambdaImpl0 implements Runnable {
        private /* synthetic */ int val$position;
        private /* synthetic */ SnapTarget val$taskSnapTarget;
        private /* synthetic */ DividerView val$this;

        public /* synthetic */ -void_onBusEvent_com_android_systemui_recents_events_ui_RecentsDrawnEvent_drawnEvent_LambdaImpl0(DividerView dividerView, int i, SnapTarget snapTarget) {
            this.val$this = dividerView;
            this.val$position = i;
            this.val$taskSnapTarget = snapTarget;
        }

        public void run() {
            this.val$this.-com_android_systemui_stackdivider_DividerView_lambda$3(this.val$position, this.val$taskSnapTarget);
        }
    }

    public DividerView(Context context) {
        super(context);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        DividerView.this.resizeStack(msg.arg1, msg.arg2, (SnapTarget) msg.obj);
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
        this.mHandleDelegate = new AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                if (DividerView.this.isHorizontalDivision()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_full)));
                    if (DividerView.this.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        info.addAction(new AccessibilityAction(R.id.action_move_tl_70, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_70)));
                    }
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_50, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_50)));
                    if (DividerView.this.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        info.addAction(new AccessibilityAction(R.id.action_move_tl_30, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_30)));
                    }
                    info.addAction(new AccessibilityAction(R.id.action_move_rb_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_bottom_full)));
                    return;
                }
                info.addAction(new AccessibilityAction(R.id.action_move_tl_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_full)));
                if (DividerView.this.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_70, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_70)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_tl_50, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_50)));
                if (DividerView.this.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    info.addAction(new AccessibilityAction(R.id.action_move_tl_30, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_30)));
                }
                info.addAction(new AccessibilityAction(R.id.action_move_rb_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_right_full)));
            }

            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                int currentPosition = DividerView.this.getCurrentPosition();
                SnapTarget nextTarget = null;
                switch (action) {
                    case R.id.action_move_tl_full:
                        nextTarget = DividerView.this.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    case R.id.action_move_tl_70:
                        nextTarget = DividerView.this.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case R.id.action_move_tl_50:
                        nextTarget = DividerView.this.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case R.id.action_move_tl_30:
                        nextTarget = DividerView.this.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case R.id.action_move_rb_full:
                        nextTarget = DividerView.this.mSnapAlgorithm.getDismissStartTarget();
                        break;
                }
                if (nextTarget == null) {
                    return super.performAccessibilityAction(host, action, args);
                }
                DividerView.this.startDragging(true, false);
                DividerView.this.stopDragging(currentPosition, nextTarget, 250, Interpolators.FAST_OUT_SLOW_IN);
                return true;
            }
        };
        this.mResetBackgroundRunnable = new Runnable() {
            public void run() {
                DividerView.this.resetBackground();
            }
        };
    }

    public DividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = /* anonymous class already generated */;
        this.mHandleDelegate = /* anonymous class already generated */;
        this.mResetBackgroundRunnable = /* anonymous class already generated */;
    }

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = /* anonymous class already generated */;
        this.mHandleDelegate = /* anonymous class already generated */;
        this.mResetBackgroundRunnable = /* anonymous class already generated */;
    }

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = /* anonymous class already generated */;
        this.mHandleDelegate = /* anonymous class already generated */;
        this.mResetBackgroundRunnable = /* anonymous class already generated */;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHandle = (ImageView) findViewById(R.id.docked_divider_handle);
        this.mBackground = findViewById(R.id.docked_divider_background);
        this.mHandle.setOnTouchListener(this);
        this.mDividerWindowWidth = getResources().getDimensionPixelSize(17104929);
        this.mDividerInsets = getResources().getDimensionPixelSize(17104930);
        this.mDividerSize = this.mDividerWindowWidth - (this.mDividerInsets * 2);
        this.mLongPressEntraceAnimDuration = getResources().getInteger(R.integer.long_press_dock_anim_duration);
        this.mGrowRecents = getResources().getBoolean(R.bool.recents_grow_in_multiwindow);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.3f);
        updateDisplayInfo();
        boolean landscape = getResources().getConfiguration().orientation == 2;
        if (Recents.getSystemServices().isCurrentHomeActivity()) {
            this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), 1000));
        } else {
            this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), landscape ? 1014 : 1015));
        }
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mHandle.setAccessibilityDelegate(this.mHandleDelegate);
        this.mGestureDetector = new GestureDetector(this.mContext, new SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                if (DividerView.this.mDividerMenusView == null) {
                    return false;
                }
                if (DividerView.this.mDividerMenusView.getVisibility() != 0) {
                    DividerView.this.mDividerMenusView.showMenusView();
                } else {
                    DividerView.this.mDividerMenusView.setVisibility(8);
                }
                return true;
            }
        });
    }

    public void setDividerMenuView(DividerMenusView dividerMenusView) {
        this.mDividerMenusView = dividerMenusView;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        this.mSurfaceFlingerOffsetMs = calculateAppSurfaceFlingerVsyncOffsetMs();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    private long calculateAppSurfaceFlingerVsyncOffsetMs() {
        Display display = getDisplay();
        return Math.max(0, ((((long) (1.0E9f / display.getRefreshRate())) - (display.getPresentationDeadlineNanos() - 1000000)) - display.getAppVsyncOffsetNanos()) / 1000000);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (this.mStableInsets.left == insets.getStableInsetLeft() && this.mStableInsets.top == insets.getStableInsetTop() && this.mStableInsets.right == insets.getStableInsetRight()) {
            if (this.mStableInsets.bottom != insets.getStableInsetBottom()) {
            }
            return super.onApplyWindowInsets(insets);
        }
        this.mStableInsets.set(insets.getStableInsetLeft(), insets.getStableInsetTop(), insets.getStableInsetRight(), insets.getStableInsetBottom());
        if (this.mSnapAlgorithm != null) {
            this.mSnapAlgorithm = null;
            initializeSnapAlgorithm();
        }
        return super.onApplyWindowInsets(insets);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            this.mWindowManagerProxy.setTouchRegion(new Rect(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom()));
        }
    }

    public void injectDependencies(DividerWindowManager windowManager, DividerState dividerState) {
        this.mWindowManager = windowManager;
        this.mState = dividerState;
    }

    public WindowManagerProxy getWindowManagerProxy() {
        return this.mWindowManagerProxy;
    }

    public boolean startDragging(boolean animate, boolean touching) {
        cancelFlingAnimation();
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
        initializeSnapAlgorithm();
        this.mWindowManagerProxy.setResizing(true);
        if (touching) {
            this.mWindowManager.setSlippery(false);
        }
        EventBus.getDefault().send(new StartedDragingEvent());
        if (this.mDockSide != -1) {
            return true;
        }
        return false;
    }

    public void stopDragging(int position, float velocity, boolean avoidDismissStart, boolean logMetrics) {
        fling(position, velocity, avoidDismissStart, logMetrics);
        this.mWindowManager.setSlippery(true);
        setGestureCoordinates(0, 0);
    }

    public void stopDragging(int position, SnapTarget target, long duration, Interpolator interpolator) {
        stopDragging(position, target, duration, 0, 0, interpolator);
    }

    public void stopDragging(int position, SnapTarget target, long duration, Interpolator interpolator, long endDelay) {
        stopDragging(position, target, duration, 0, endDelay, interpolator);
    }

    public void stopDragging(int position, SnapTarget target, long duration, long startDelay, long endDelay, Interpolator interpolator) {
        flingTo(position, target, duration, startDelay, endDelay, interpolator);
        this.mWindowManager.setSlippery(true);
        setGestureCoordinates(0, 0);
    }

    private void stopDragging() {
        this.mWindowManager.setSlippery(true);
    }

    public void updateDockSide() {
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
    }

    public int getDockSide() {
        return this.mDockSide;
    }

    public void hideMenusView() {
        if (this.mDividerMenusView != null) {
            this.mDividerMenusView.setVisibility(8);
        }
    }

    public void removeMenusView() {
        if (this.mDividerMenusView != null) {
            this.mDividerMenusView.removeViewToWindow();
        }
    }

    private void initializeSnapAlgorithm() {
        if (this.mSnapAlgorithm == null) {
            this.mSnapAlgorithm = new DividerSnapAlgorithm(getContext().getResources(), this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize, isHorizontalDivision(), this.mStableInsets);
        }
    }

    public DividerSnapAlgorithm getSnapAlgorithm() {
        initializeSnapAlgorithm();
        return this.mSnapAlgorithm;
    }

    public int getCurrentPosition() {
        getLocationOnScreen(this.mTempInt2);
        if (isHorizontalDivision()) {
            return this.mTempInt2[1] + this.mDividerInsets;
        }
        return this.mTempInt2[0] + this.mDividerInsets;
    }

    public boolean onTouch(View v, MotionEvent event) {
        setGestureCoordinates(0, 0);
        convertToScreenCoordinates(event);
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction() & 255;
        if (action == 0) {
            this.mHandle.setImageDrawable(this.mContext.getDrawable(R.drawable.btn_tskm_drag_pressed));
        } else if (action == 1 || action == 3) {
            this.mHandle.setImageDrawable(this.mContext.getDrawable(R.drawable.btn_tskm_drag_normal));
        }
        if (Recents.getSystemServices().isCurrentHomeActivity()) {
            return true;
        }
        int x;
        int y;
        switch (action) {
            case 0:
                this.mVelocityTracker = VelocityTracker.obtain();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(event);
                }
                this.mStartX = (int) event.getX();
                this.mStartY = (int) event.getY();
                boolean result = startDragging(true, true);
                if (!result) {
                    stopDragging();
                }
                this.mStartPosition = getCurrentPosition();
                this.mMoving = false;
                return result;
            case 1:
            case 3:
                if (this.mVelocityTracker != null) {
                    float yVelocity;
                    this.mVelocityTracker.addMovement(event);
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    int position = calculatePosition(x, y);
                    if (isHorizontalDivision()) {
                        yVelocity = this.mVelocityTracker.getYVelocity();
                    } else {
                        yVelocity = this.mVelocityTracker.getXVelocity();
                    }
                    stopDragging(position, yVelocity, false, true);
                }
                this.mMoving = false;
                break;
            case 2:
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(event);
                }
                x = (int) event.getX();
                y = (int) event.getY();
                boolean exceededTouchSlop = (!isHorizontalDivision() || Math.abs(y - this.mStartY) <= this.mTouchSlop) ? !isHorizontalDivision() && Math.abs(x - this.mStartX) > this.mTouchSlop : true;
                if (!this.mMoving && exceededTouchSlop) {
                    this.mStartX = x;
                    this.mStartY = y;
                    this.mMoving = true;
                }
                if (this.mMoving && this.mDockSide != -1) {
                    resizeStackDelayed(calculatePosition(x, y), this.mStartPosition, this.mSnapAlgorithm.calculateSnapTarget(this.mStartPosition, 0.0f, false));
                    break;
                }
                break;
        }
        return true;
    }

    private void logResizeEvent(SnapTarget snapTarget) {
        int i = 2;
        int i2 = 1;
        Context context;
        if (snapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
            context = this.mContext;
            if (!dockSideTopLeft(this.mDockSide)) {
                i2 = 0;
            }
            MetricsLogger.action(context, 390, i2);
        } else if (snapTarget == this.mSnapAlgorithm.getDismissEndTarget()) {
            context = this.mContext;
            if (!dockSideBottomRight(this.mDockSide)) {
                i2 = 0;
            }
            MetricsLogger.action(context, 390, i2);
        } else if (snapTarget == this.mSnapAlgorithm.getMiddleTarget()) {
            MetricsLogger.action(this.mContext, 389, 0);
        } else if (snapTarget == this.mSnapAlgorithm.getFirstSplitTarget()) {
            r1 = this.mContext;
            if (!dockSideTopLeft(this.mDockSide)) {
                i2 = 2;
            }
            MetricsLogger.action(r1, 389, i2);
        } else if (snapTarget == this.mSnapAlgorithm.getLastSplitTarget()) {
            r1 = this.mContext;
            if (!dockSideTopLeft(this.mDockSide)) {
                i = 1;
            }
            MetricsLogger.action(r1, 389, i);
        }
    }

    private void convertToScreenCoordinates(MotionEvent event) {
        event.setLocation(event.getRawX(), event.getRawY());
    }

    private void fling(int position, float velocity, boolean avoidDismissStart, boolean logMetrics) {
        SnapTarget snapTarget = this.mSnapAlgorithm.calculateSnapTarget(position, velocity);
        if (avoidDismissStart && snapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
            snapTarget = this.mSnapAlgorithm.getFirstSplitTarget();
        }
        if (logMetrics) {
            logResizeEvent(snapTarget);
        }
        Animator anim = getFlingAnimator(position, snapTarget, 0);
        this.mFlingAnimationUtils.apply(anim, (float) position, (float) snapTarget.position, velocity);
        anim.start();
    }

    private void flingTo(int position, SnapTarget target, long duration, long startDelay, long endDelay, Interpolator interpolator) {
        ValueAnimator anim = getFlingAnimator(position, target, endDelay);
        anim.setDuration(duration);
        anim.setStartDelay(startDelay);
        anim.setInterpolator(interpolator);
        anim.start();
    }

    private ValueAnimator getFlingAnimator(int position, SnapTarget snapTarget, final long endDelay) {
        boolean taskPositionSameAtEnd = snapTarget.flag == 0;
        ValueAnimator anim = ValueAnimator.ofInt(new int[]{position, snapTarget.position});
        anim.addUpdateListener(new DividerView$-android_animation_ValueAnimator_getFlingAnimator_int_position_com_android_internal_policy_DividerSnapAlgorithm$SnapTarget_snapTarget_long_endDelay_LambdaImpl0(this, taskPositionSameAtEnd, snapTarget));
        final Runnable endAction = new DividerView$-android_animation_ValueAnimator_getFlingAnimator_int_position_com_android_internal_policy_DividerSnapAlgorithm$SnapTarget_snapTarget_long_endDelay_LambdaImpl1(this, snapTarget);
        anim.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                DividerView.this.mHandler.removeMessages(0);
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                long delay = 0;
                if (endDelay != 0) {
                    delay = endDelay;
                } else if (this.mCancelled) {
                    delay = 0;
                } else if (DividerView.this.mSurfaceFlingerOffsetMs != 0) {
                    delay = DividerView.this.mSurfaceFlingerOffsetMs;
                }
                if (delay == 0) {
                    endAction.run();
                } else {
                    DividerView.this.mHandler.postDelayed(endAction, delay);
                }
            }
        });
        this.mCurrentAnimator = anim;
        return anim;
    }

    /* synthetic */ void -com_android_systemui_stackdivider_DividerView_lambda$1(boolean taskPositionSameAtEnd, SnapTarget snapTarget, ValueAnimator animation) {
        int i;
        int intValue = ((Integer) animation.getAnimatedValue()).intValue();
        if (taskPositionSameAtEnd && animation.getAnimatedFraction() == 1.0f) {
            i = Integer.MAX_VALUE;
        } else {
            i = snapTarget.taskPosition;
        }
        resizeStackDelayed(intValue, i, snapTarget);
    }

    /* synthetic */ void -com_android_systemui_stackdivider_DividerView_lambda$2(SnapTarget snapTarget) {
        commitSnapFlags(snapTarget);
        this.mWindowManagerProxy.setResizing(false);
        this.mDockSide = -1;
        this.mCurrentAnimator = null;
        this.mEntranceAnimationRunning = false;
        this.mExitAnimationRunning = false;
        EventBus.getDefault().send(new StoppedDragingEvent());
    }

    private void cancelFlingAnimation() {
        if (this.mCurrentAnimator != null) {
            this.mCurrentAnimator.cancel();
        }
    }

    private void commitSnapFlags(SnapTarget target) {
        if (target.flag != 0) {
            boolean dismissOrMaximize = target.flag == 1 ? this.mDockSide != 1 ? this.mDockSide == 2 : true : this.mDockSide != 3 ? this.mDockSide == 4 : true;
            if (dismissOrMaximize) {
                BDReporter.c(this.mContext, 339);
                this.mWindowManagerProxy.dismissDockedStack();
            } else {
                this.mWindowManagerProxy.maximizeDockedStack();
            }
            this.mWindowManagerProxy.setResizeDimLayer(false, -1, 0.0f);
        }
    }

    public void setMinimizedDockStack(boolean minimized) {
        updateDockSide();
        this.mDockedStackMinimized = minimized;
        if (!Recents.getSystemServices().isCurrentHomeActivity()) {
            this.mHandle.setAlpha(minimized ? 0.0f : 1.0f);
            if (!minimized) {
                resetBackground();
            } else if (this.mDockSide == 2) {
                this.mBackground.setPivotY(0.0f);
                this.mBackground.setScaleY(0.0f);
            } else if (this.mDockSide == 1 || this.mDockSide == 3) {
                int i;
                View view = this.mBackground;
                if (this.mDockSide == 1) {
                    i = 0;
                } else {
                    i = this.mBackground.getWidth();
                }
                view.setPivotX((float) i);
                this.mBackground.setScaleX(0.0f);
            }
        }
    }

    public void setMinimizedDockStack(boolean minimized, long animDuration) {
        float f = 0.0f;
        updateDockSide();
        this.mDockedStackMinimized = minimized;
        boolean landscape = getResources().getConfiguration().orientation == 2;
        if (Recents.getSystemServices().isCurrentHomeActivity()) {
            this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), 1000));
            return;
        }
        float f2;
        this.mHandle.setPointerIcon(PointerIcon.getSystemIcon(getContext(), landscape ? 1014 : 1015));
        ViewPropertyAnimator duration = this.mHandle.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(animDuration);
        if (minimized) {
            f2 = 0.0f;
        } else {
            f2 = 1.0f;
        }
        duration.alpha(f2).start();
        ViewPropertyAnimator animate;
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            animate = this.mBackground.animate();
            if (!minimized) {
                f = 1.0f;
            }
            animate.scaleY(f);
        } else if (this.mDockSide == 1 || this.mDockSide == 3) {
            int i;
            View view = this.mBackground;
            if (this.mDockSide == 1) {
                i = 0;
            } else {
                i = this.mBackground.getWidth();
            }
            view.setPivotX((float) i);
            animate = this.mBackground.animate();
            if (!minimized) {
                f = 1.0f;
            }
            animate.scaleX(f);
        }
        if (!minimized) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(animDuration).start();
    }

    public void setAdjustedForIme(boolean adjustedForIme) {
        updateDockSide();
        this.mHandle.setAlpha(adjustedForIme ? 0.0f : 1.0f);
        if (!adjustedForIme) {
            resetBackground();
        } else if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.setScaleY(0.5f);
        }
        this.mAdjustedForIme = adjustedForIme;
    }

    public void setAdjustedForIme(boolean adjustedForIme, long animDuration) {
        float f;
        float f2 = 1.0f;
        updateDockSide();
        ViewPropertyAnimator duration = this.mHandle.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(animDuration);
        if (adjustedForIme) {
            f = 0.0f;
        } else {
            f = 1.0f;
        }
        duration.alpha(f).start();
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            ViewPropertyAnimator animate = this.mBackground.animate();
            if (adjustedForIme) {
                f2 = 0.5f;
            }
            animate.scaleY(f2);
        }
        if (!adjustedForIme) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(animDuration).start();
        this.mAdjustedForIme = adjustedForIme;
    }

    private void resetBackground() {
        this.mBackground.setPivotX((float) (this.mBackground.getWidth() / 2));
        this.mBackground.setPivotY((float) (this.mBackground.getHeight() / 2));
        this.mBackground.setScaleX(1.0f);
        this.mBackground.setScaleY(1.0f);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDisplayInfo();
    }

    public void notifyDockSideChanged(int newDockSide) {
        this.mDockSide = newDockSide;
        requestLayout();
    }

    private void updateDisplayInfo() {
        Display display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
        DisplayInfo info = new DisplayInfo();
        display.getDisplayInfo(info);
        this.mDisplayWidth = info.logicalWidth;
        this.mDisplayHeight = info.logicalHeight;
        this.mSnapAlgorithm = null;
        initializeSnapAlgorithm();
    }

    private int calculatePosition(int touchX, int touchY) {
        return isHorizontalDivision() ? calculateYPosition(touchY) : calculateXPosition(touchX);
    }

    public boolean isHorizontalDivision() {
        return getResources().getConfiguration().orientation == 1;
    }

    private int calculateXPosition(int touchX) {
        return (this.mStartPosition + touchX) - this.mStartX;
    }

    private int calculateYPosition(int touchY) {
        return (this.mStartPosition + touchY) - this.mStartY;
    }

    private void alignTopLeft(Rect containingRect, Rect rect) {
        rect.set(containingRect.left, containingRect.top, containingRect.left + rect.width(), containingRect.top + rect.height());
    }

    private void alignBottomRight(Rect containingRect, Rect rect) {
        rect.set(containingRect.right - rect.width(), containingRect.bottom - rect.height(), containingRect.right, containingRect.bottom);
    }

    public void calculateBoundsForPosition(int position, int dockSide, Rect outRect) {
        DockedDividerUtils.calculateBoundsForPosition(position, dockSide, outRect, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
    }

    public void resizeStackDelayed(int position, int taskPosition, SnapTarget taskSnapTarget) {
        if (this.mSurfaceFlingerOffsetMs != 0) {
            Message message = this.mHandler.obtainMessage(0, position, taskPosition, taskSnapTarget);
            message.setAsynchronous(true);
            this.mHandler.sendMessageDelayed(message, this.mSurfaceFlingerOffsetMs);
            return;
        }
        resizeStack(position, taskPosition, taskSnapTarget);
    }

    public void resizeStack(int position, int taskPosition, SnapTarget taskSnapTarget) {
        calculateBoundsForPosition(position, this.mDockSide, this.mDockedRect);
        if (!this.mDockedRect.equals(this.mLastResizeRect) || this.mEntranceAnimationRunning) {
            if (this.mBackground.getZ() > 0.0f) {
                this.mBackground.invalidate();
            }
            BDReporter.c(this.mContext, 340);
            this.mLastResizeRect.set(this.mDockedRect);
            if (this.mEntranceAnimationRunning && taskPosition != Integer.MAX_VALUE) {
                if (this.mCurrentAnimator != null) {
                    calculateBoundsForPosition(taskPosition, this.mDockSide, this.mDockedTaskRect);
                } else {
                    calculateBoundsForPosition(isHorizontalDivision() ? this.mDisplayHeight : this.mDisplayWidth, this.mDockSide, this.mDockedTaskRect);
                }
                calculateBoundsForPosition(taskPosition, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, null, this.mOtherTaskRect, null);
            } else if (this.mExitAnimationRunning && taskPosition != Integer.MAX_VALUE) {
                calculateBoundsForPosition(taskPosition, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(this.mExitStartPosition, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                applyExitAnimationParallax(this.mOtherTaskRect, position);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, null, this.mOtherTaskRect, this.mOtherInsetRect);
            } else if (taskPosition != Integer.MAX_VALUE) {
                calculateBoundsForPosition(position, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
                int dockSideInverted = DockedDividerUtils.invertDockSide(this.mDockSide);
                int taskPositionDocked = restrictDismissingTaskPosition(taskPosition, this.mDockSide, taskSnapTarget);
                int taskPositionOther = restrictDismissingTaskPosition(taskPosition, dockSideInverted, taskSnapTarget);
                calculateBoundsForPosition(taskPositionDocked, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(taskPositionOther, dockSideInverted, this.mOtherTaskRect);
                this.mDisplayRect.set(0, 0, this.mDisplayWidth, this.mDisplayHeight);
                alignTopLeft(this.mDockedRect, this.mDockedTaskRect);
                alignTopLeft(this.mOtherRect, this.mOtherTaskRect);
                this.mDockedInsetRect.set(this.mDockedTaskRect);
                this.mOtherInsetRect.set(this.mOtherTaskRect);
                if (dockSideTopLeft(this.mDockSide)) {
                    alignTopLeft(this.mDisplayRect, this.mDockedInsetRect);
                    alignBottomRight(this.mDisplayRect, this.mOtherInsetRect);
                } else {
                    alignBottomRight(this.mDisplayRect, this.mDockedInsetRect);
                    alignTopLeft(this.mDisplayRect, this.mOtherInsetRect);
                }
                applyDismissingParallax(this.mDockedTaskRect, this.mDockSide, taskSnapTarget, position, taskPositionDocked);
                applyDismissingParallax(this.mOtherTaskRect, dockSideInverted, taskSnapTarget, position, taskPositionOther);
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, this.mDockedInsetRect, this.mOtherTaskRect, this.mOtherInsetRect);
            } else {
                this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, null, null, null, null);
            }
            SnapTarget closestDismissTarget = this.mSnapAlgorithm.getClosestDismissTarget(position);
            float dimFraction = getDimFraction(position, closestDismissTarget);
            this.mWindowManagerProxy.setResizeDimLayer(dimFraction != 0.0f, getStackIdForDismissTarget(closestDismissTarget), dimFraction);
        }
    }

    private void applyExitAnimationParallax(Rect taskRect, int position) {
        if (this.mDockSide == 2) {
            taskRect.offset(0, (int) (((float) (position - this.mExitStartPosition)) * 0.25f));
        } else if (this.mDockSide == 1) {
            taskRect.offset((int) (((float) (position - this.mExitStartPosition)) * 0.25f), 0);
        } else if (this.mDockSide == 3) {
            taskRect.offset((int) (((float) (this.mExitStartPosition - position)) * 0.25f), 0);
        }
    }

    private float getDimFraction(int position, SnapTarget dismissTarget) {
        if (this.mEntranceAnimationRunning) {
            return 0.0f;
        }
        float fraction = DIM_INTERPOLATOR.getInterpolation(Math.max(0.0f, Math.min(this.mSnapAlgorithm.calculateDismissingFraction(position), 1.0f)));
        if (hasInsetsAtDismissTarget(dismissTarget)) {
            fraction *= 0.8f;
        }
        return fraction;
    }

    private boolean hasInsetsAtDismissTarget(SnapTarget dismissTarget) {
        boolean z = true;
        if (isHorizontalDivision()) {
            if (dismissTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
                if (this.mStableInsets.top == 0) {
                    z = false;
                }
                return z;
            }
            if (this.mStableInsets.bottom == 0) {
                z = false;
            }
            return z;
        } else if (dismissTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
            if (this.mStableInsets.left == 0) {
                z = false;
            }
            return z;
        } else {
            if (this.mStableInsets.right == 0) {
                z = false;
            }
            return z;
        }
    }

    private int restrictDismissingTaskPosition(int taskPosition, int dockSide, SnapTarget snapTarget) {
        if (snapTarget.flag == 1 && dockSideTopLeft(dockSide)) {
            return Math.max(this.mSnapAlgorithm.getFirstSplitTarget().position, this.mStartPosition);
        }
        if (snapTarget.flag == 2 && dockSideBottomRight(dockSide)) {
            return Math.min(this.mSnapAlgorithm.getLastSplitTarget().position, this.mStartPosition);
        }
        return taskPosition;
    }

    private void applyDismissingParallax(Rect taskRect, int dockSide, SnapTarget snapTarget, int position, int taskPosition) {
        float fraction = Math.min(1.0f, Math.max(0.0f, this.mSnapAlgorithm.calculateDismissingFraction(position)));
        SnapTarget dismissTarget = null;
        SnapTarget splitTarget = null;
        int start = 0;
        if (position <= this.mSnapAlgorithm.getLastSplitTarget().position && dockSideTopLeft(dockSide)) {
            dismissTarget = this.mSnapAlgorithm.getDismissStartTarget();
            splitTarget = this.mSnapAlgorithm.getFirstSplitTarget();
            start = taskPosition;
        } else if (position >= this.mSnapAlgorithm.getLastSplitTarget().position && dockSideBottomRight(dockSide)) {
            dismissTarget = this.mSnapAlgorithm.getDismissEndTarget();
            splitTarget = this.mSnapAlgorithm.getLastSplitTarget();
            start = splitTarget.position;
        }
        if (dismissTarget != null && fraction > 0.0f && isDismissing(splitTarget, position, dockSide)) {
            int offsetPosition = (int) (((float) start) + (((float) (dismissTarget.position - splitTarget.position)) * calculateParallaxDismissingFraction(fraction, dockSide)));
            int width = taskRect.width();
            int height = taskRect.height();
            switch (dockSide) {
                case 1:
                    taskRect.left = offsetPosition - width;
                    taskRect.right = offsetPosition;
                    return;
                case 2:
                    taskRect.top = offsetPosition - height;
                    taskRect.bottom = offsetPosition;
                    return;
                case 3:
                    taskRect.left = this.mDividerSize + offsetPosition;
                    taskRect.right = (offsetPosition + width) + this.mDividerSize;
                    return;
                case 4:
                    taskRect.top = this.mDividerSize + offsetPosition;
                    taskRect.bottom = (offsetPosition + height) + this.mDividerSize;
                    return;
                default:
                    return;
            }
        }
    }

    private static float calculateParallaxDismissingFraction(float fraction, int dockSide) {
        float result = SLOWDOWN_INTERPOLATOR.getInterpolation(fraction) / 3.5f;
        if (dockSide == 2) {
            return result / 2.0f;
        }
        return result;
    }

    private static boolean isDismissing(SnapTarget snapTarget, int position, int dockSide) {
        boolean z = true;
        if (dockSide == 2 || dockSide == 1) {
            if (position >= snapTarget.position) {
                z = false;
            }
            return z;
        }
        if (position <= snapTarget.position) {
            z = false;
        }
        return z;
    }

    private int getStackIdForDismissTarget(SnapTarget dismissTarget) {
        if ((dismissTarget.flag == 1 && dockSideTopLeft(this.mDockSide)) || (dismissTarget.flag == 2 && dockSideBottomRight(this.mDockSide))) {
            return 3;
        }
        return 0;
    }

    private static boolean dockSideTopLeft(int dockSide) {
        return dockSide == 2 || dockSide == 1;
    }

    private static boolean dockSideBottomRight(int dockSide) {
        return dockSide == 4 || dockSide == 3;
    }

    public void onComputeInternalInsets(InternalInsetsInfo inoutInfo) {
        inoutInfo.setTouchableInsets(3);
        inoutInfo.touchableRegion.set(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom());
        inoutInfo.touchableRegion.op(this.mBackground.getLeft(), this.mBackground.getTop(), this.mBackground.getRight(), this.mBackground.getBottom(), Op.UNION);
    }

    public int growsRecents() {
        boolean result;
        boolean z = false;
        if (this.mGrowRecents && this.mWindowManagerProxy.getDockSide() == 2) {
            if (getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
                z = true;
            }
            result = z;
        } else {
            result = false;
        }
        if (result) {
            return getSnapAlgorithm().getMiddleTarget().position;
        }
        return -1;
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        if (this.mGrowRecents && getWindowManagerProxy().getDockSide() == 2 && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            this.mState.growAfterRecentsDrawn = true;
            startDragging(false, false);
        }
    }

    public final void onBusEvent(DockedTopTaskEvent event) {
        if (event.dragMode == -1) {
            this.mState.growAfterRecentsDrawn = false;
            this.mState.animateAfterRecentsDrawn = true;
            startDragging(false, false);
        }
        updateDockSide();
        int position = DockedDividerUtils.calculatePositionForBounds(event.initialRect, this.mDockSide, this.mDividerSize);
        this.mEntranceAnimationRunning = true;
        if (disableAnimatorByGesture()) {
            SnapTarget taskSnapTarget = getSnapTargetByGesture();
            resizeStack(taskSnapTarget.position, taskSnapTarget.position, taskSnapTarget);
            return;
        }
        resizeStack(position, this.mSnapAlgorithm.getMiddleTarget().position, this.mSnapAlgorithm.getMiddleTarget());
    }

    public final void onBusEvent(RecentsDrawnEvent drawnEvent) {
        if (this.mState.animateAfterRecentsDrawn) {
            SnapTarget taskSnapTarget;
            int position;
            this.mState.animateAfterRecentsDrawn = false;
            updateDockSide();
            if (disableAnimatorByGesture()) {
                taskSnapTarget = getSnapTargetByGesture();
                position = taskSnapTarget.position;
            } else {
                taskSnapTarget = this.mSnapAlgorithm.getMiddleTarget();
                position = getCurrentPosition();
            }
            this.mHandler.post(new -void_onBusEvent_com_android_systemui_recents_events_ui_RecentsDrawnEvent_drawnEvent_LambdaImpl0(this, position, taskSnapTarget));
        }
        if (this.mState.growAfterRecentsDrawn) {
            this.mState.growAfterRecentsDrawn = false;
            updateDockSide();
            EventBus.getDefault().send(new RecentsGrowingEvent());
            stopDragging(getCurrentPosition(), this.mSnapAlgorithm.getMiddleTarget(), 336, Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    /* synthetic */ void -com_android_systemui_stackdivider_DividerView_lambda$3(int position, SnapTarget taskSnapTarget) {
        stopDragging(position, taskSnapTarget, (long) this.mLongPressEntraceAnimDuration, Interpolators.FAST_OUT_SLOW_IN, 200);
    }

    public final void onBusEvent(UndockingTaskEvent undockingTaskEvent) {
        int dockSide = this.mWindowManagerProxy.getDockSide();
        if (dockSide != -1 && !this.mDockedStackMinimized) {
            SnapTarget target;
            startDragging(false, false);
            if (dockSideTopLeft(dockSide)) {
                target = this.mSnapAlgorithm.getDismissEndTarget();
            } else {
                target = this.mSnapAlgorithm.getDismissStartTarget();
            }
            this.mExitAnimationRunning = true;
            this.mExitStartPosition = getCurrentPosition();
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                long duration = 0;

                public boolean runInThread() {
                    if (!DividerView.this.mWindowManagerProxy.isDimLayerVisible()) {
                        this.duration = 336;
                    }
                    return true;
                }

                public void runInUI() {
                    DividerView.this.stopDragging(DividerView.this.mExitStartPosition, target, this.duration, 100, 0, Interpolators.FAST_OUT_SLOW_IN);
                }
            });
        }
    }

    public void setGestureCoordinates(int x, int y) {
        this.mCenterX = x;
        this.mCenterY = y;
    }

    private boolean disableAnimatorByGesture() {
        return isHorizontalDivision() && !(this.mCenterX == 0 && this.mCenterY == 0);
    }

    public void setStartPosition(int position) {
        this.mStartPosition = position;
    }

    private SnapTarget getSnapTargetByGesture() {
        SnapTarget middleTarget = this.mSnapAlgorithm.getMiddleTarget();
        SnapTarget previousTarget = this.mSnapAlgorithm.getPreviousTarget(middleTarget);
        SnapTarget nextTarget = this.mSnapAlgorithm.getNextTarget(middleTarget);
        if (this.mCenterY < (middleTarget.position + previousTarget.position) / 2) {
            return previousTarget;
        }
        return this.mCenterY > (middleTarget.position + nextTarget.position) / 2 ? nextTarget : middleTarget;
    }
}
