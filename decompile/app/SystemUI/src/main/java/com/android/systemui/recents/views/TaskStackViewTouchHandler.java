package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.MutableBoolean;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.SwipeHelper.Callback;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;
import java.util.List;

class TaskStackViewTouchHandler implements Callback {
    private static final Interpolator OVERSCROLL_INTERP;
    int mActivePointerId = -1;
    TaskView mActiveTaskView = null;
    Context mContext;
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList();
    private ArrayList<Task> mCurrentTasks = new ArrayList();
    float mDownScrollP;
    int mDownX;
    int mDownY;
    private ArrayList<TaskViewTransform> mFinalTaskTransforms = new ArrayList();
    FlingAnimationUtils mFlingAnimUtils;
    boolean mInterceptedBySwipeHelper;
    @ExportedProperty(category = "recents")
    boolean mIsScrolling;
    int mLastY;
    int mMaximumVelocity;
    int mMinimumVelocity;
    int mOverscrollSize;
    ValueAnimator mScrollFlingAnimator;
    int mScrollTouchSlop;
    TaskStackViewScroller mScroller;
    private final StackViewScrolledEvent mStackViewScrolledEvent = new StackViewScrolledEvent();
    TaskStackView mSv;
    SwipeHelper mSwipeHelper;
    private ArrayMap<View, Animator> mSwipeHelperAnimations = new ArrayMap();
    private float mTargetStackScroll;
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    VelocityTracker mVelocityTracker;
    final int mWindowTouchSlop;

    static {
        Path OVERSCROLL_PATH = new Path();
        OVERSCROLL_PATH.moveTo(0.0f, 0.0f);
        OVERSCROLL_PATH.cubicTo(0.2f, 0.175f, 0.25f, 0.3f, 1.0f, 0.3f);
        OVERSCROLL_INTERP = new FreePathInterpolator(OVERSCROLL_PATH);
    }

    public TaskStackViewTouchHandler(Context context, TaskStackView sv, TaskStackViewScroller scroller) {
        Resources res = context.getResources();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mContext = context;
        this.mSv = sv;
        this.mScroller = scroller;
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mScrollTouchSlop = configuration.getScaledTouchSlop();
        this.mWindowTouchSlop = configuration.getScaledWindowTouchSlop();
        this.mFlingAnimUtils = new FlingAnimationUtils(context, 0.2f);
        this.mOverscrollSize = res.getDimensionPixelSize(R.dimen.recents_fling_overscroll_distance);
        this.mSwipeHelper = new SwipeHelper(0, this, context) {
            protected float getSize(View v) {
                return TaskStackViewTouchHandler.this.getScaledDismissSize();
            }

            protected void prepareDismissAnimation(View v, Animator anim) {
                TaskStackViewTouchHandler.this.mSwipeHelperAnimations.put(v, anim);
            }

            protected void prepareSnapBackAnimation(View v, Animator anim) {
                anim.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                TaskStackViewTouchHandler.this.mSwipeHelperAnimations.put(v, anim);
            }

            protected float getUnscaledEscapeVelocity() {
                return 800.0f;
            }

            protected long getMaxEscapeAnimDuration() {
                return 700;
            }
        };
        this.mSwipeHelper.setDisableHardwareLayers(true);
    }

    void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.mInterceptedBySwipeHelper = this.mSwipeHelper.onInterceptTouchEvent(ev);
        if (this.mInterceptedBySwipeHelper) {
            return true;
        }
        return handleTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mInterceptedBySwipeHelper && this.mSwipeHelper.onTouchEvent(ev)) {
            return true;
        }
        handleTouchEvent(ev);
        return true;
    }

    public void cancelNonDismissTaskAnimations() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollFlingAnimator);
        if (!this.mSwipeHelperAnimations.isEmpty()) {
            List<TaskView> taskViews = this.mSv.getTaskViews();
            for (int i = taskViews.size() - 1; i >= 0; i--) {
                TaskView tv = (TaskView) taskViews.get(i);
                if (!this.mSv.isIgnoredTask(tv.getTask())) {
                    tv.cancelTransformAnimation();
                    this.mSv.getStackAlgorithm().addUnfocusedTaskOverride(tv, this.mTargetStackScroll);
                }
            }
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getScroller().setStackScroll(this.mTargetStackScroll, null);
            this.mSwipeHelperAnimations.clear();
        }
        this.mActiveTaskView = null;
    }

    private boolean handleTouchEvent(MotionEvent ev) {
        if (this.mSv.getTaskViews().size() == 0) {
            return false;
        }
        TaskStackLayoutAlgorithm layoutAlgorithm = this.mSv.mLayoutAlgorithm;
        int activePointerIndex;
        int y;
        switch (ev.getAction() & 255) {
            case 0:
                this.mScroller.stopScroller();
                this.mScroller.stopBoundScrollAnimation();
                this.mScroller.resetDeltaScroll();
                cancelNonDismissTaskAnimations();
                this.mSv.cancelDeferredTaskViewLayoutAnimation();
                this.mDownX = (int) ev.getX();
                this.mDownY = (int) ev.getY();
                this.mLastY = this.mDownY;
                this.mDownScrollP = this.mScroller.getStackScroll();
                this.mActivePointerId = ev.getPointerId(0);
                this.mActiveTaskView = findViewAtPoint(this.mDownX, this.mDownY);
                initOrResetVelocityTracker();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(ev);
                    break;
                }
                break;
            case 1:
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(ev);
                    this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                }
                activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex >= 0 && activePointerIndex < ev.getPointerCount()) {
                    y = (int) ev.getY(activePointerIndex);
                    int velocity = this.mVelocityTracker == null ? 0 : (int) this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                    if (this.mIsScrolling) {
                        if (this.mScroller.isScrollOutOfBounds()) {
                            BDReporter.c(this.mContext, 365);
                            this.mScroller.animateBoundScroll();
                        } else if (Math.abs(velocity) > this.mMinimumVelocity) {
                            this.mScroller.fling(this.mDownScrollP, this.mDownY, y, velocity, (int) ((float) (this.mDownY + layoutAlgorithm.getYForDeltaP(this.mDownScrollP, layoutAlgorithm.mMaxScrollP))), (int) ((float) (this.mDownY + layoutAlgorithm.getYForDeltaP(this.mDownScrollP, layoutAlgorithm.mMinScrollP))), this.mOverscrollSize);
                            this.mSv.invalidate();
                        }
                        if (!this.mSv.mTouchExplorationEnabled) {
                            this.mSv.resetFocusedTask(this.mSv.getFocusedTask());
                        }
                    } else if (this.mActiveTaskView == null) {
                        maybeHideRecentsFromBackgroundTap((int) ev.getX(), (int) ev.getY());
                    }
                    this.mActivePointerId = -1;
                    this.mIsScrolling = false;
                    recycleVelocityTracker();
                    break;
                }
            case 2:
                activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex >= 0 && activePointerIndex < ev.getPointerCount()) {
                    y = (int) ev.getY(activePointerIndex);
                    int x = (int) ev.getX(activePointerIndex);
                    if (!this.mIsScrolling) {
                        int yDiff = Math.abs(y - this.mDownY);
                        int xDiff = Math.abs(x - this.mDownX);
                        if (Math.abs(y - this.mDownY) > this.mScrollTouchSlop && yDiff > xDiff) {
                            this.mIsScrolling = true;
                            float stackScroll = this.mScroller.getStackScroll();
                            List<TaskView> taskViews = this.mSv.getTaskViews();
                            for (int i = taskViews.size() - 1; i >= 0; i--) {
                                layoutAlgorithm.addUnfocusedTaskOverride(((TaskView) taskViews.get(i)).getTask(), stackScroll);
                            }
                            layoutAlgorithm.setFocusState(0);
                            ViewParent parent = this.mSv.getParent();
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true);
                            }
                            MetricsLogger.action(this.mSv.getContext(), 287);
                        }
                    }
                    if (this.mIsScrolling) {
                        float deltaP = layoutAlgorithm.getDeltaPForY(this.mDownY, y);
                        float minScrollP = layoutAlgorithm.mMinScrollP;
                        float maxScrollP = layoutAlgorithm.mMaxScrollP;
                        float curScrollP = this.mDownScrollP + deltaP;
                        if (curScrollP < minScrollP || curScrollP > maxScrollP) {
                            float clampedScrollP = Utilities.clamp(curScrollP, minScrollP, maxScrollP);
                            float overscrollP = curScrollP - clampedScrollP;
                            curScrollP = clampedScrollP + (Math.signum(overscrollP) * (2.3333333f * OVERSCROLL_INTERP.getInterpolation(Math.abs(overscrollP) / 2.3333333f)));
                        }
                        this.mDownScrollP += this.mScroller.setDeltaStackScroll(this.mDownScrollP, curScrollP - this.mDownScrollP);
                        this.mStackViewScrolledEvent.updateY(y - this.mLastY);
                        EventBus.getDefault().send(this.mStackViewScrolledEvent);
                    }
                    this.mLastY = y;
                    if (this.mVelocityTracker != null) {
                        this.mVelocityTracker.addMovement(ev);
                        break;
                    }
                }
                break;
            case 3:
                this.mActivePointerId = -1;
                this.mIsScrolling = false;
                recycleVelocityTracker();
                break;
            case 5:
                int index = ev.getActionIndex();
                this.mActivePointerId = ev.getPointerId(index);
                this.mDownX = (int) ev.getX(index);
                this.mDownY = (int) ev.getY(index);
                this.mLastY = this.mDownY;
                this.mDownScrollP = this.mScroller.getStackScroll();
                this.mScroller.resetDeltaScroll();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(ev);
                    break;
                }
                break;
            case 6:
                int pointerIndex = ev.getActionIndex();
                if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
                    this.mActivePointerId = ev.getPointerId(pointerIndex == 0 ? 1 : 0);
                    this.mDownX = (int) ev.getX(pointerIndex);
                    this.mDownY = (int) ev.getY(pointerIndex);
                    this.mLastY = this.mDownY;
                    this.mDownScrollP = this.mScroller.getStackScroll();
                }
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.addMovement(ev);
                    break;
                }
                break;
        }
        return this.mIsScrolling;
    }

    void maybeHideRecentsFromBackgroundTap(int x, int y) {
        int dx = Math.abs(this.mDownX - x);
        int dy = Math.abs(this.mDownY - y);
        if (dx <= this.mScrollTouchSlop && dy <= this.mScrollTouchSlop) {
            int shiftedX = x;
            if (x > (this.mSv.getRight() - this.mSv.getLeft()) / 2) {
                shiftedX = x - this.mWindowTouchSlop;
            } else {
                shiftedX = x + this.mWindowTouchSlop;
            }
            if (findViewAtPoint(shiftedX, y) == null) {
                if (x <= this.mSv.mLayoutAlgorithm.mStackRect.left || x >= this.mSv.mLayoutAlgorithm.mStackRect.right) {
                    if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
                        Rect freeformRect = this.mSv.mLayoutAlgorithm.mFreeformRect;
                        if (freeformRect.top <= y && y <= freeformRect.bottom && this.mSv.launchFreeformTasks()) {
                            return;
                        }
                    }
                    EventBus.getDefault().send(new HideRecentsEvent(false, true));
                }
            }
        }
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        if ((ev.getSource() & 2) == 2) {
            switch (ev.getAction() & 255) {
                case 8:
                    if (ev.getAxisValue(9) > 0.0f) {
                        this.mSv.setRelativeFocusedTask(true, true, false);
                    } else {
                        this.mSv.setRelativeFocusedTask(false, true, false);
                    }
                    return true;
            }
        }
        return false;
    }

    public View getChildAtPosition(MotionEvent ev) {
        TaskView tv = findViewAtPoint((int) ev.getX(), (int) ev.getY());
        if (tv == null || !canChildBeDismissed(tv)) {
            return null;
        }
        return tv;
    }

    public boolean canChildBeDismissed(View v) {
        Task task = ((TaskView) v).getTask();
        if (this.mSwipeHelperAnimations.containsKey(v) || this.mSv.getStack().indexOfStackTask(task) == -1) {
            return false;
        }
        return true;
    }

    public void onBeginManualDrag(TaskView v) {
        this.mActiveTaskView = v;
        this.mSwipeHelperAnimations.put(v, null);
        onBeginDrag(v);
    }

    public void onBeginDrag(View v) {
        TaskView tv = (TaskView) v;
        tv.setClipViewInStack(false);
        tv.setTouchEnabled(false);
        ViewParent parent = this.mSv.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        this.mSv.addIgnoreTask(tv.getTask());
        this.mCurrentTasks = new ArrayList(this.mSv.getStack().getStackTasks());
        MutableBoolean isFrontMostTask = new MutableBoolean(false);
        Task anchorTask = this.mSv.findAnchorTask(this.mCurrentTasks, isFrontMostTask);
        TaskStackLayoutAlgorithm layoutAlgorithm = this.mSv.getStackAlgorithm();
        TaskStackViewScroller stackScroller = this.mSv.getScroller();
        if (anchorTask != null) {
            this.mSv.getCurrentTaskTransforms(this.mCurrentTasks, this.mCurrentTaskTransforms);
            float prevAnchorTaskScroll = 0.0f;
            boolean pullStackForward = this.mCurrentTasks.size() > 0;
            if (pullStackForward) {
                prevAnchorTaskScroll = layoutAlgorithm.getStackScrollForTask(anchorTask);
            }
            this.mSv.updateLayoutAlgorithm(false);
            float newStackScroll = stackScroller.getStackScroll();
            if (isFrontMostTask.value) {
                newStackScroll = stackScroller.getBoundedStackScroll(newStackScroll);
            } else if (pullStackForward) {
                float stackScrollOffset = layoutAlgorithm.getStackScrollForTaskIgnoreOverrides(anchorTask) - prevAnchorTaskScroll;
                if (layoutAlgorithm.getFocusState() != 1) {
                    stackScrollOffset *= 0.75f;
                }
                newStackScroll = stackScroller.getBoundedStackScroll(stackScroller.getStackScroll() + stackScrollOffset);
            }
            this.mSv.bindVisibleTaskViews(newStackScroll, true);
            this.mSv.getLayoutTaskTransforms(newStackScroll, 0, this.mCurrentTasks, true, this.mFinalTaskTransforms);
            this.mTargetStackScroll = newStackScroll;
        }
    }

    public boolean updateSwipeProgress(View v, boolean dismissable, float swipeProgress) {
        if (this.mActiveTaskView == v || this.mSwipeHelperAnimations.containsKey(v)) {
            updateTaskViewTransforms(Interpolators.FAST_OUT_SLOW_IN.getInterpolation(swipeProgress));
        }
        return true;
    }

    public void onChildDismissed(View v) {
        AnimationProps animationProps;
        TaskView tv = (TaskView) v;
        tv.setClipViewInStack(true);
        tv.setTouchEnabled(true);
        EventBus eventBus = EventBus.getDefault();
        Task task = tv.getTask();
        if (this.mSwipeHelperAnimations.containsKey(v)) {
            animationProps = new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN);
        } else {
            animationProps = null;
        }
        eventBus.send(new TaskViewDismissedEvent(task, tv, animationProps));
        if (this.mSwipeHelperAnimations.containsKey(v)) {
            this.mSv.getScroller().setStackScroll(this.mTargetStackScroll, null);
            this.mSv.getStackAlgorithm().setFocusState(0);
            this.mSv.getStackAlgorithm().clearUnfocusedTaskOverrides();
            this.mSwipeHelperAnimations.remove(v);
        }
        MetricsLogger.histogram(tv.getContext(), "overview_task_dismissed_source", 1);
    }

    public void onChildSnappedBack(View v, float targetLeft) {
        TaskView tv = (TaskView) v;
        tv.setClipViewInStack(true);
        tv.setTouchEnabled(true);
        this.mSv.removeIgnoreTask(tv.getTask());
        this.mSv.updateLayoutAlgorithm(false);
        this.mSv.relayoutTaskViews(AnimationProps.IMMEDIATE);
        this.mSwipeHelperAnimations.remove(v);
    }

    public void onDragCancelled(View v) {
    }

    public boolean isAntiFalsingNeeded() {
        return false;
    }

    public float getFalsingThresholdFactor() {
        return 0.0f;
    }

    private void updateTaskViewTransforms(float dismissFraction) {
        List<TaskView> taskViews = this.mSv.getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            Task task = tv.getTask();
            if (!this.mSv.isIgnoredTask(task)) {
                int taskIndex = this.mCurrentTasks.indexOf(task);
                if (taskIndex < 0 || taskIndex >= this.mCurrentTaskTransforms.size()) {
                    HwLog.i("TAG", "mCurrentTasks.indexOf(task)=" + taskIndex);
                } else {
                    TaskViewTransform fromTransform = (TaskViewTransform) this.mCurrentTaskTransforms.get(taskIndex);
                    TaskViewTransform toTransform = (TaskViewTransform) this.mFinalTaskTransforms.get(taskIndex);
                    this.mTmpTransform.copyFrom(fromTransform);
                    this.mTmpTransform.rect.set(Utilities.RECTF_EVALUATOR.evaluate(dismissFraction, fromTransform.rect, toTransform.rect));
                    this.mTmpTransform.dimAlpha = fromTransform.dimAlpha + ((toTransform.dimAlpha - fromTransform.dimAlpha) * dismissFraction);
                    this.mTmpTransform.viewOutlineAlpha = fromTransform.viewOutlineAlpha + ((toTransform.viewOutlineAlpha - fromTransform.viewOutlineAlpha) * dismissFraction);
                    this.mTmpTransform.translationZ = fromTransform.translationZ + ((toTransform.translationZ - fromTransform.translationZ) * dismissFraction);
                    this.mSv.updateTaskViewToTransform(tv, this.mTmpTransform, AnimationProps.IMMEDIATE);
                }
            }
        }
    }

    private TaskView findViewAtPoint(int x, int y) {
        List<Task> tasks = this.mSv.getStack().getStackTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            TaskView tv = this.mSv.getChildViewForTask((Task) tasks.get(i));
            if (tv != null && tv.getVisibility() == 0 && this.mSv.isTouchPointInView((float) x, (float) y, tv)) {
                return tv;
            }
        }
        return null;
    }

    public float getScaledDismissSize() {
        return ((float) Math.max(this.mSv.getWidth(), this.mSv.getHeight())) * 1.5f;
    }
}
