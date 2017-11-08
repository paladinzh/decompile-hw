package com.android.systemui.recents.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.IntDef;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.MutableBoolean;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.recents.HwRecentsLockUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsTaskStackAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.HwTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.model.TaskStack.DockState;
import com.android.systemui.recents.model.TaskStack.TaskStackCallbacks;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm.StackState;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm.TaskStackLayoutAlgorithmCallbacks;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm.VisibilityReport;
import com.android.systemui.recents.views.TaskStackViewScroller.TaskStackViewScrollerCallbacks;
import com.android.systemui.recents.views.ViewPool.ViewPoolConsumer;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import fyusion.vislib.BuildConfig;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class TaskStackView extends FrameLayout implements TaskStackCallbacks, TaskViewCallbacks, TaskStackViewScrollerCallbacks, TaskStackLayoutAlgorithmCallbacks, ViewPoolConsumer<TaskView, Task> {
    private boolean enterRecentsWindowAnimationCompleted = false;
    private TaskStackAnimationHelper mAnimationHelper;
    @ExportedProperty(category = "recents")
    private boolean mAwaitingFirstLayout = true;
    private ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList();
    private AnimationProps mDeferredTaskViewLayoutAnimation = null;
    @ExportedProperty(category = "recents")
    private int mDisplayOrientation = 0;
    @ExportedProperty(category = "recents")
    private Rect mDisplayRect = new Rect();
    private int mDividerSize;
    @ExportedProperty(category = "recents")
    private boolean mEnterAnimationComplete = false;
    @ExportedProperty(deepExport = true, prefix = "focused_task_")
    private Task mFocusedTask;
    private GradientDrawable mFreeformWorkspaceBackground;
    private ObjectAnimator mFreeformWorkspaceBackgroundAnimator;
    private DropTarget mFreeformWorkspaceDropTarget = new DropTarget() {
        public boolean acceptsDrop(int x, int y, int width, int height, boolean isCurrentTarget) {
            if (isCurrentTarget) {
                return false;
            }
            return TaskStackView.this.mLayoutAlgorithm.mFreeformRect.contains(x, y);
        }
    };
    private ArraySet<TaskKey> mIgnoreTasks = new ArraySet();
    @ExportedProperty(category = "recents")
    private boolean mInMeasureLayout = false;
    private LayoutInflater mInflater;
    @ExportedProperty(category = "recents")
    private int mInitialState = 1;
    private int mLastHeight;
    private int mLastWidth;
    @ExportedProperty(deepExport = true, prefix = "layout_")
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    private AnimatorUpdateListener mRequestUpdateClippingListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            if (!TaskStackView.this.mTaskViewsClipDirty) {
                TaskStackView.this.mTaskViewsClipDirty = true;
                TaskStackView.this.invalidate();
            }
        }
    };
    private boolean mResetToInitialStateWhenResized;
    @ExportedProperty(category = "recents")
    boolean mScreenPinningEnabled;
    private TaskStackLayoutAlgorithm mStableLayoutAlgorithm;
    @ExportedProperty(category = "recents")
    private Rect mStableStackBounds = new Rect();
    @ExportedProperty(category = "recents")
    private Rect mStableWindowRect = new Rect();
    private TaskStack mStack = new TaskStack();
    @ExportedProperty(category = "recents")
    private Rect mStackBounds = new Rect();
    private DropTarget mStackDropTarget = new DropTarget() {
        public boolean acceptsDrop(int x, int y, int width, int height, boolean isCurrentTarget) {
            if (isCurrentTarget) {
                return false;
            }
            return TaskStackView.this.mLayoutAlgorithm.mStackRect.contains(x, y);
        }
    };
    @ExportedProperty(deepExport = true, prefix = "scroller_")
    private TaskStackViewScroller mStackScroller;
    private int mStartTimerIndicatorDuration;
    private int mTaskCornerRadiusPx;
    private ArrayList<TaskView> mTaskViews = new ArrayList();
    @ExportedProperty(category = "recents")
    private boolean mTaskViewsClipDirty = true;
    private int[] mTmpIntPair = new int[2];
    private Rect mTmpRect = new Rect();
    private ArrayMap<TaskKey, TaskView> mTmpTaskViewMap = new ArrayMap();
    private List<TaskView> mTmpTaskViews = new ArrayList();
    private TaskViewTransform mTmpTransform = new TaskViewTransform();
    @ExportedProperty(category = "recents")
    boolean mTouchExplorationEnabled;
    @ExportedProperty(deepExport = true, prefix = "touch_")
    private TaskStackViewTouchHandler mTouchHandler;
    @ExportedProperty(deepExport = true, prefix = "doze_")
    private DozeTrigger mUIDozeTrigger;
    private ViewPool<TaskView, Task> mViewPool;
    @ExportedProperty(category = "recents")
    private Rect mWindowRect = new Rect();

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface InitialStateAction {
    }

    public TaskStackView(Context context) {
        super(context);
        SystemServicesProxy ssp = Recents.getSystemServices();
        Resources res = context.getResources();
        this.mStack.setCallbacks(this);
        this.mViewPool = new ViewPool(context, this);
        this.mInflater = LayoutInflater.from(context);
        this.mLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, this);
        this.mStableLayoutAlgorithm = new TaskStackLayoutAlgorithm(context, null);
        this.mStackScroller = new TaskStackViewScroller(context, this, this.mLayoutAlgorithm);
        this.mTouchHandler = new TaskStackViewTouchHandler(context, this, this.mStackScroller);
        this.mAnimationHelper = new TaskStackAnimationHelper(context, this);
        this.mTaskCornerRadiusPx = res.getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mDividerSize = ssp.getDockedDividerSize(context);
        this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
        this.mDisplayRect = ssp.getDisplayRect();
        this.mUIDozeTrigger = new DozeTrigger(getResources().getInteger(R.integer.recents_task_bar_dismiss_delay_seconds), new Runnable() {
            public void run() {
                List<TaskView> taskViews = TaskStackView.this.getTaskViews();
                int taskViewCount = taskViews.size();
                for (int i = 0; i < taskViewCount; i++) {
                    ((TaskView) taskViews.get(i)).startNoUserInteractionAnimation();
                }
            }
        });
        setImportantForAccessibility(1);
        this.mFreeformWorkspaceBackground = (GradientDrawable) getContext().getDrawable(R.drawable.recents_freeform_workspace_bg);
        this.mFreeformWorkspaceBackground.setCallback(this);
        if (ssp.hasFreeformWorkspaceSupport()) {
            this.mFreeformWorkspaceBackground.setColor(getContext().getColor(R.color.recents_freeform_workspace_bg_color));
        }
    }

    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
        readSystemFlags();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    void onReload(boolean isResumingFromVisible) {
        if (!isResumingFromVisible) {
            resetFocusedTask(getFocusedTask());
        }
        List<TaskView> taskViews = new ArrayList();
        taskViews.addAll(getTaskViews());
        taskViews.addAll(this.mViewPool.getViews());
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            ((TaskView) taskViews.get(i)).onReload(isResumingFromVisible);
        }
        readSystemFlags();
        this.mTaskViewsClipDirty = true;
        this.mEnterAnimationComplete = false;
        this.mUIDozeTrigger.stopDozing();
        if (isResumingFromVisible) {
            animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        } else {
            this.mStackScroller.reset();
            this.mStableLayoutAlgorithm.reset();
            this.mLayoutAlgorithm.reset();
        }
        this.mAwaitingFirstLayout = true;
        this.mInitialState = 1;
        requestLayout();
    }

    public void setTasks(TaskStack stack, boolean allowNotifyStackChanges) {
        boolean isInitialized = this.mLayoutAlgorithm.isInitialized();
        TaskStack taskStack = this.mStack;
        Context context = getContext();
        List computeAllTasksList = stack.computeAllTasksList();
        if (!allowNotifyStackChanges) {
            isInitialized = false;
        }
        taskStack.setTasks(context, computeAllTasksList, isInitialized);
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public void updateToInitialState() {
        this.mStackScroller.setStackScrollToInitialState();
        this.mLayoutAlgorithm.setTaskOverridesForInitialState(this.mStack, false);
    }

    void updateTaskViewsList() {
        this.mTaskViews.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof TaskView) {
                this.mTaskViews.add((TaskView) v);
            }
        }
    }

    List<TaskView> getTaskViews() {
        return this.mTaskViews;
    }

    private TaskView getFrontMostTaskView(boolean stackTasksOnly) {
        List<TaskView> taskViews = getTaskViews();
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            TaskView tv = (TaskView) taskViews.get(i);
            Task task = tv.getTask();
            if (!stackTasksOnly || !task.isFreeformTask()) {
                return tv;
            }
        }
        return null;
    }

    public TaskView getChildViewForTask(Task t) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            if (tv.getTask() == t) {
                return tv;
            }
        }
        return null;
    }

    public TaskStackLayoutAlgorithm getStackAlgorithm() {
        return this.mLayoutAlgorithm;
    }

    public TaskStackViewTouchHandler getTouchHandler() {
        return this.mTouchHandler;
    }

    void addIgnoreTask(Task task) {
        this.mIgnoreTasks.add(task.key);
    }

    void removeIgnoreTask(Task task) {
        this.mIgnoreTasks.remove(task.key);
    }

    boolean isIgnoredTask(Task task) {
        return this.mIgnoreTasks.contains(task.key);
    }

    int[] computeVisibleTaskTransforms(ArrayList<TaskViewTransform> taskTransforms, ArrayList<Task> tasks, float curStackScroll, float targetStackScroll, ArraySet<TaskKey> ignoreTasksSet, boolean ignoreTaskOverrides) {
        int taskCount = tasks.size();
        int[] visibleTaskRange = this.mTmpIntPair;
        visibleTaskRange[0] = -1;
        visibleTaskRange[1] = -1;
        boolean useTargetStackScroll = Float.compare(curStackScroll, targetStackScroll) != 0;
        Utilities.matchTaskListSize(tasks, taskTransforms);
        TaskViewTransform frontTransform = null;
        TaskViewTransform frontTransformAtTarget = null;
        TaskViewTransform transformAtTarget = null;
        for (int i = taskCount - 1; i >= 0; i--) {
            Task task = (Task) tasks.get(i);
            TaskViewTransform transform = this.mLayoutAlgorithm.getStackTransform(task, curStackScroll, (TaskViewTransform) taskTransforms.get(i), frontTransform, ignoreTaskOverrides);
            if (useTargetStackScroll && !transform.visible) {
                transformAtTarget = this.mLayoutAlgorithm.getStackTransform(task, targetStackScroll, new TaskViewTransform(), frontTransformAtTarget);
                if (transformAtTarget.visible) {
                    transform.copyFrom(transformAtTarget);
                }
            }
            if (!(ignoreTasksSet.contains(task.key) || task.isFreeformTask())) {
                frontTransform = transform;
                frontTransformAtTarget = transformAtTarget;
                if (transform.visible) {
                    if (visibleTaskRange[0] < 0) {
                        visibleTaskRange[0] = i;
                    }
                    visibleTaskRange[1] = i;
                }
            }
        }
        return visibleTaskRange;
    }

    void bindVisibleTaskViews(float targetStackScroll) {
        bindVisibleTaskViews(targetStackScroll, false);
    }

    void bindVisibleTaskViews(float targetStackScroll, boolean ignoreTaskOverrides) {
        int i;
        TaskViewTransform taskViewTransform;
        ArrayList<Task> tasks = this.mStack.getStackTasks();
        int[] visibleTaskRange = computeVisibleTaskTransforms(this.mCurrentTaskTransforms, tasks, this.mStackScroller.getStackScroll(), targetStackScroll, this.mIgnoreTasks, ignoreTaskOverrides);
        this.mTmpTaskViewMap.clear();
        List<TaskView> taskViews = getTaskViews();
        int lastFocusedTaskIndex = -1;
        for (i = taskViews.size() - 1; i >= 0; i--) {
            TaskView tv = (TaskView) taskViews.get(i);
            Task task = tv.getTask();
            if (!this.mIgnoreTasks.contains(task.key)) {
                int taskIndex = this.mStack.indexOfStackTask(task);
                taskViewTransform = null;
                if (taskIndex != -1) {
                    taskViewTransform = (TaskViewTransform) this.mCurrentTaskTransforms.get(taskIndex);
                }
                if (task.isFreeformTask() || (r19 != null && r19.visible)) {
                    this.mTmpTaskViewMap.put(task.key, tv);
                } else {
                    if (this.mTouchExplorationEnabled && Utilities.isDescendentAccessibilityFocused(tv)) {
                        lastFocusedTaskIndex = taskIndex;
                        resetFocusedTask(task);
                    }
                    this.mViewPool.returnViewToPool(tv);
                }
            }
        }
        for (i = tasks.size() - 1; i >= 0; i--) {
            task = (Task) tasks.get(i);
            taskViewTransform = (TaskViewTransform) this.mCurrentTaskTransforms.get(i);
            if (!this.mIgnoreTasks.contains(task.key) && (task.isFreeformTask() || taskViewTransform.visible)) {
                View tv2 = (TaskView) this.mTmpTaskViewMap.get(task.key);
                if (tv2 == null) {
                    tv = (TaskView) this.mViewPool.pickUpViewFromPool(task, task);
                    if (task.isFreeformTask()) {
                        updateTaskViewToTransform(tv, taskViewTransform, AnimationProps.IMMEDIATE);
                    } else if (taskViewTransform.rect.top <= ((float) this.mLayoutAlgorithm.mStackRect.top)) {
                        updateTaskViewToTransform(tv, this.mLayoutAlgorithm.getBackOfStackTransform(), AnimationProps.IMMEDIATE);
                    } else {
                        updateTaskViewToTransform(tv, this.mLayoutAlgorithm.getFrontOfStackTransform(), AnimationProps.IMMEDIATE);
                    }
                } else {
                    int insertIndex = findTaskViewInsertIndex(task, this.mStack.indexOfStackTask(task));
                    if (insertIndex != getTaskViews().indexOf(tv2)) {
                        detachViewFromParent(tv2);
                        attachViewToParent(tv2, insertIndex, tv2.getLayoutParams());
                        updateTaskViewsList();
                    }
                }
            }
        }
        if (lastFocusedTaskIndex != -1) {
            int newFocusedTaskIndex;
            if (lastFocusedTaskIndex < visibleTaskRange[1]) {
                newFocusedTaskIndex = visibleTaskRange[1];
            } else {
                newFocusedTaskIndex = visibleTaskRange[0];
            }
            setFocusedTask(newFocusedTaskIndex, false, true);
            TaskView focusedTaskView = getChildViewForTask(this.mFocusedTask);
            if (focusedTaskView != null) {
                focusedTaskView.requestAccessibilityFocus();
            }
        }
    }

    public void relayoutTaskViews(AnimationProps animation) {
        relayoutTaskViews(animation, null, false);
    }

    private void relayoutTaskViews(AnimationProps animation, ArrayMap<Task, AnimationProps> animationOverrides, boolean ignoreTaskOverrides) {
        cancelDeferredTaskViewLayoutAnimation();
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), ignoreTaskOverrides);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            Task task = tv.getTask();
            int taskIndex = this.mStack.indexOfStackTask(task);
            if (taskIndex < 0 || taskIndex >= this.mCurrentTaskTransforms.size()) {
                HwLog.e("TaskStackView", "relayoutTaskViews::taskIndex is not valid, taskIndex=" + taskIndex);
            } else {
                TaskViewTransform transform = (TaskViewTransform) this.mCurrentTaskTransforms.get(taskIndex);
                if (!this.mIgnoreTasks.contains(task.key)) {
                    if (animationOverrides != null && animationOverrides.containsKey(task)) {
                        animation = (AnimationProps) animationOverrides.get(task);
                    }
                    updateTaskViewToTransform(tv, transform, animation);
                }
            }
        }
    }

    void relayoutTaskViewsOnNextFrame(AnimationProps animation) {
        this.mDeferredTaskViewLayoutAnimation = animation;
        invalidate();
    }

    public void updateTaskViewToTransform(TaskView taskView, TaskViewTransform transform, AnimationProps animation) {
        if (!taskView.isAnimatingTo(transform)) {
            taskView.cancelTransformAnimation();
            taskView.updateViewPropertiesToTaskTransform(transform, animation, this.mRequestUpdateClippingListener);
        }
    }

    public void getCurrentTaskTransforms(ArrayList<Task> tasks, ArrayList<TaskViewTransform> transformsOut) {
        Utilities.matchTaskListSize(tasks, transformsOut);
        int focusState = this.mLayoutAlgorithm.getFocusState();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = (Task) tasks.get(i);
            TaskViewTransform transform = (TaskViewTransform) transformsOut.get(i);
            TaskView tv = getChildViewForTask(task);
            if (tv != null) {
                transform.fillIn(tv);
            } else {
                this.mLayoutAlgorithm.getStackTransform(task, this.mStackScroller.getStackScroll(), focusState, transform, null, true, false);
            }
            transform.visible = true;
        }
    }

    public void getLayoutTaskTransforms(float stackScroll, int focusState, ArrayList<Task> tasks, boolean ignoreTaskOverrides, ArrayList<TaskViewTransform> transformsOut) {
        Utilities.matchTaskListSize(tasks, transformsOut);
        for (int i = tasks.size() - 1; i >= 0; i--) {
            TaskViewTransform transform = (TaskViewTransform) transformsOut.get(i);
            this.mLayoutAlgorithm.getStackTransform((Task) tasks.get(i), stackScroll, focusState, transform, null, true, ignoreTaskOverrides);
            transform.visible = true;
        }
    }

    void cancelDeferredTaskViewLayoutAnimation() {
        this.mDeferredTaskViewLayoutAnimation = null;
    }

    void cancelAllTaskViewAnimations() {
        List<TaskView> taskViews = getTaskViews();
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            TaskView tv = (TaskView) taskViews.get(i);
            if (!this.mIgnoreTasks.contains(tv.getTask().key)) {
                tv.cancelTransformAnimation();
            }
        }
    }

    private void clipTaskViews() {
        List<TaskView> taskViews = getTaskViews();
        TaskView prevVisibleTv = null;
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            TaskView frontTv = null;
            int clipBottom = 0;
            if (isIgnoredTask(tv.getTask()) && prevVisibleTv != null) {
                tv.setTranslationZ(Math.max(tv.getTranslationZ(), prevVisibleTv.getTranslationZ() + 0.1f));
            }
            if (i < taskViewCount - 1 && tv.shouldClipViewInStack()) {
                for (int j = i + 1; j < taskViewCount; j++) {
                    TaskView tmpTv = (TaskView) taskViews.get(j);
                    if (tmpTv.shouldClipViewInStack()) {
                        frontTv = tmpTv;
                        break;
                    }
                }
                if (frontTv != null) {
                    float taskBottom = (float) tv.getBottom();
                    float frontTaskTop = (float) frontTv.getTop();
                    if (frontTaskTop < taskBottom) {
                        clipBottom = ((int) (taskBottom - frontTaskTop)) - this.mTaskCornerRadiusPx;
                    }
                }
            }
            tv.getViewBounds().setClipBottom(clipBottom);
            tv.mThumbnailView.updateThumbnailVisibility(clipBottom - tv.getPaddingBottom());
            prevVisibleTv = tv;
        }
        this.mTaskViewsClipDirty = false;
    }

    public void updateLayoutAlgorithm(boolean boundScrollToNewMinMax) {
        this.mLayoutAlgorithm.update(this.mStack, this.mIgnoreTasks);
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            this.mTmpRect.set(this.mLayoutAlgorithm.mFreeformRect);
            this.mFreeformWorkspaceBackground.setBounds(this.mTmpRect);
        }
        if (boundScrollToNewMinMax) {
            this.mStackScroller.boundScroll();
        }
    }

    private void updateLayoutToStableBounds() {
        this.mWindowRect.set(this.mStableWindowRect);
        this.mStackBounds.set(this.mStableStackBounds);
        this.mLayoutAlgorithm.setSystemInsets(this.mStableLayoutAlgorithm.mSystemInsets);
        this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, StackState.getStackStateForStack(this.mStack));
        updateLayoutAlgorithm(true);
    }

    public TaskStackViewScroller getScroller() {
        return this.mStackScroller;
    }

    private boolean setFocusedTask(int taskIndex, boolean scrollToTask, boolean requestViewFocus) {
        return setFocusedTask(taskIndex, scrollToTask, requestViewFocus, 0);
    }

    private boolean setFocusedTask(int focusTaskIndex, boolean scrollToTask, boolean requestViewFocus, int timerIndicatorDuration) {
        int newFocusedTaskIndex;
        TaskView tv;
        if (this.mStack.getTaskCount() > 0) {
            newFocusedTaskIndex = Utilities.clamp(focusTaskIndex, 0, this.mStack.getTaskCount() - 1);
        } else {
            newFocusedTaskIndex = -1;
        }
        Task task = newFocusedTaskIndex != -1 ? (Task) this.mStack.getStackTasks().get(newFocusedTaskIndex) : null;
        if (this.mFocusedTask != null) {
            if (timerIndicatorDuration > 0) {
                tv = getChildViewForTask(this.mFocusedTask);
                if (tv != null) {
                    tv.getHeaderView().cancelFocusTimerIndicator();
                }
            }
            resetFocusedTask(this.mFocusedTask);
        }
        this.mFocusedTask = task;
        if (task == null) {
            return false;
        }
        if (timerIndicatorDuration > 0) {
            tv = getChildViewForTask(this.mFocusedTask);
            if (tv != null) {
                tv.getHeaderView().startFocusTimerIndicator(timerIndicatorDuration);
            } else {
                this.mStartTimerIndicatorDuration = timerIndicatorDuration;
            }
        }
        if (scrollToTask) {
            if (!this.mEnterAnimationComplete) {
                cancelAllTaskViewAnimations();
            }
            this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return this.mAnimationHelper.startScrollToFocusedTaskAnimation(task, requestViewFocus);
        }
        TaskView newFocusedTaskView = getChildViewForTask(task);
        if (newFocusedTaskView == null) {
            return false;
        }
        newFocusedTaskView.setFocusedState(true, requestViewFocus);
        return false;
    }

    public void setRelativeFocusedTask(boolean forward, boolean stackTasksOnly, boolean animated) {
        setRelativeFocusedTask(forward, stackTasksOnly, animated, false, 0);
    }

    public void setRelativeFocusedTask(boolean forward, boolean stackTasksOnly, boolean animated, boolean cancelWindowAnimations, int timerIndicatorDuration) {
        Task focusedTask = getFocusedTask();
        int newIndex = this.mStack.indexOfStackTask(focusedTask);
        int taskCount;
        if (focusedTask == null) {
            float stackScroll = this.mStackScroller.getStackScroll();
            ArrayList<Task> tasks = this.mStack.getStackTasks();
            taskCount = tasks.size();
            if (forward) {
                newIndex = taskCount - 1;
                while (newIndex >= 0 && Float.compare(this.mLayoutAlgorithm.getStackScrollForTask((Task) tasks.get(newIndex)), stackScroll) > 0) {
                    newIndex--;
                }
            } else {
                newIndex = 0;
                while (newIndex < taskCount && Float.compare(this.mLayoutAlgorithm.getStackScrollForTask((Task) tasks.get(newIndex)), stackScroll) < 0) {
                    newIndex++;
                }
            }
        } else if (stackTasksOnly) {
            List<Task> tasks2 = this.mStack.getStackTasks();
            if (focusedTask.isFreeformTask()) {
                TaskView tv = getFrontMostTaskView(stackTasksOnly);
                if (tv != null) {
                    newIndex = this.mStack.indexOfStackTask(tv.getTask());
                }
            } else {
                int tmpNewIndex = newIndex + (forward ? -1 : 1);
                if (tmpNewIndex >= 0 && tmpNewIndex < tasks2.size() && !((Task) tasks2.get(tmpNewIndex)).isFreeformTask()) {
                    newIndex = tmpNewIndex;
                }
            }
        } else {
            taskCount = this.mStack.getTaskCount();
            newIndex = (((forward ? -1 : 1) + newIndex) + taskCount) % taskCount;
        }
        if (newIndex != -1 && setFocusedTask(newIndex, true, true, timerIndicatorDuration) && cancelWindowAnimations) {
            EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
        }
    }

    void resetFocusedTask(Task task) {
        if (task != null) {
            TaskView tv = getChildViewForTask(task);
            if (tv != null) {
                tv.setFocusedState(false, false);
            }
        }
        this.mFocusedTask = null;
    }

    Task getFocusedTask() {
        return this.mFocusedTask;
    }

    Task getAccessibilityFocusedTask() {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            if (Utilities.isDescendentAccessibilityFocused(tv)) {
                return tv.getTask();
            }
        }
        TaskView frontTv = getFrontMostTaskView(true);
        if (frontTv != null) {
            return frontTv.getTask();
        }
        return null;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        if (taskViewCount > 0) {
            TaskView frontMostTask = (TaskView) taskViews.get(taskViewCount - 1);
            event.setFromIndex(this.mStack.indexOfStackTask(((TaskView) taskViews.get(0)).getTask()));
            event.setToIndex(this.mStack.indexOfStackTask(frontMostTask.getTask()));
            event.setContentDescription(frontMostTask.getTask().title);
        }
        event.setItemCount(this.mStack.getTaskCount());
        int stackHeight = this.mLayoutAlgorithm.mStackRect.height();
        event.setScrollY((int) (this.mStackScroller.getStackScroll() * ((float) stackHeight)));
        event.setMaxScrollY((int) (this.mLayoutAlgorithm.mMaxScrollP * ((float) stackHeight)));
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (getTaskViews().size() > 1) {
            Task focusedTask = getAccessibilityFocusedTask();
            info.setScrollable(true);
            int focusedTaskIndex = this.mStack.indexOfStackTask(focusedTask);
            if (focusedTaskIndex > 0) {
                info.addAction(8192);
            }
            if (focusedTaskIndex >= 0 && focusedTaskIndex < this.mStack.getTaskCount() - 1) {
                info.addAction(4096);
            }
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        int taskIndex = this.mStack.indexOfStackTask(getAccessibilityFocusedTask());
        if (taskIndex >= 0 && taskIndex < this.mStack.getTaskCount()) {
            switch (action) {
                case 4096:
                    setFocusedTask(taskIndex + 1, true, true, 0);
                    return true;
                case 8192:
                    setFocusedTask(taskIndex - 1, true, true, 0);
                    return true;
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mTouchHandler.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return this.mTouchHandler.onTouchEvent(ev);
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        return this.mTouchHandler.onGenericMotionEvent(ev);
    }

    public void computeScroll() {
        if (this.mStackScroller.computeScroll()) {
            sendAccessibilityEvent(4096);
        }
        if (this.mDeferredTaskViewLayoutAnimation != null) {
            relayoutTaskViews(this.mDeferredTaskViewLayoutAnimation);
            this.mTaskViewsClipDirty = true;
            this.mDeferredTaskViewLayoutAnimation = null;
        }
        if (this.mTaskViewsClipDirty) {
            clipTaskViews();
        }
    }

    public VisibilityReport computeStackVisibilityReport() {
        return this.mLayoutAlgorithm.computeStackVisibilityReport(this.mStack.getStackTasks());
    }

    public void setSystemInsets(Rect systemInsets) {
        if (this.mStableLayoutAlgorithm.setSystemInsets(systemInsets) | this.mLayoutAlgorithm.setSystemInsets(systemInsets)) {
            requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean resetToInitialState;
        int taskViewCount;
        int i;
        this.mInMeasureLayout = true;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this.mLayoutAlgorithm.getTaskStackBounds(this.mDisplayRect, new Rect(0, 0, width, height), this.mLayoutAlgorithm.mSystemInsets.top, this.mLayoutAlgorithm.mSystemInsets.right, this.mTmpRect);
        if (!this.mTmpRect.equals(this.mStableStackBounds)) {
            this.mStableStackBounds.set(this.mTmpRect);
            this.mStackBounds.set(this.mTmpRect);
            this.mStableWindowRect.set(0, 0, width, height);
            this.mWindowRect.set(0, 0, width, height);
        }
        this.mStableLayoutAlgorithm.initialize(this.mDisplayRect, this.mStableWindowRect, this.mStableStackBounds, StackState.getStackStateForStack(this.mStack));
        this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, StackState.getStackStateForStack(this.mStack));
        updateLayoutAlgorithm(false);
        if (width == this.mLastWidth && height == this.mLastHeight) {
            resetToInitialState = false;
        } else {
            resetToInitialState = this.mResetToInitialStateWhenResized;
        }
        if (!this.mAwaitingFirstLayout && this.mInitialState == 0) {
            if (resetToInitialState) {
            }
            bindVisibleTaskViews(this.mStackScroller.getStackScroll(), false);
            this.mTmpTaskViews.clear();
            this.mTmpTaskViews.addAll(getTaskViews());
            this.mTmpTaskViews.addAll(this.mViewPool.getViews());
            taskViewCount = this.mTmpTaskViews.size();
            for (i = 0; i < taskViewCount; i++) {
                measureTaskView((TaskView) this.mTmpTaskViews.get(i));
            }
            setMeasuredDimension(width, height);
            this.mLastWidth = width;
            this.mLastHeight = height;
            this.mInMeasureLayout = false;
        }
        if (this.mInitialState != 2 || resetToInitialState) {
            updateToInitialState();
            this.mResetToInitialStateWhenResized = false;
        }
        if (!this.mAwaitingFirstLayout) {
            this.mInitialState = 0;
        }
        bindVisibleTaskViews(this.mStackScroller.getStackScroll(), false);
        this.mTmpTaskViews.clear();
        this.mTmpTaskViews.addAll(getTaskViews());
        this.mTmpTaskViews.addAll(this.mViewPool.getViews());
        taskViewCount = this.mTmpTaskViews.size();
        for (i = 0; i < taskViewCount; i++) {
            measureTaskView((TaskView) this.mTmpTaskViews.get(i));
        }
        setMeasuredDimension(width, height);
        this.mLastWidth = width;
        this.mLastHeight = height;
        this.mInMeasureLayout = false;
    }

    private void measureTaskView(TaskView tv) {
        Rect padding = new Rect();
        if (tv.getBackground() != null) {
            tv.getBackground().getPadding(padding);
        }
        this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
        this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
        tv.measure(MeasureSpec.makeMeasureSpec((this.mTmpRect.width() + padding.left) + padding.right, 1073741824), MeasureSpec.makeMeasureSpec((this.mTmpRect.height() + padding.top) + padding.bottom, 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mTmpTaskViews.clear();
        this.mTmpTaskViews.addAll(getTaskViews());
        this.mTmpTaskViews.addAll(this.mViewPool.getViews());
        int taskViewCount = this.mTmpTaskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            layoutTaskView(changed, (TaskView) this.mTmpTaskViews.get(i));
        }
        if (changed && this.mStackScroller.isScrollOutOfBounds()) {
            this.mStackScroller.boundScroll();
        }
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        clipTaskViews();
        if (!this.enterRecentsWindowAnimationCompleted) {
            if (this.mAwaitingFirstLayout || !this.mEnterAnimationComplete) {
                this.mAwaitingFirstLayout = false;
                this.mInitialState = 0;
                onFirstLayout();
            }
        }
    }

    private void layoutTaskView(boolean changed, TaskView tv) {
        if (changed) {
            Rect padding = new Rect();
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(padding);
            }
            this.mTmpRect.set(this.mStableLayoutAlgorithm.mTaskRect);
            this.mTmpRect.union(this.mLayoutAlgorithm.mTaskRect);
            tv.cancelTransformAnimation();
            tv.layout(this.mTmpRect.left - padding.left, this.mTmpRect.top - padding.top, this.mTmpRect.right + padding.right, this.mTmpRect.bottom + padding.bottom);
            return;
        }
        tv.layout(tv.getLeft(), tv.getTop(), tv.getRight(), tv.getBottom());
    }

    void onFirstLayout() {
        this.mAnimationHelper.prepareForEnterAnimation();
        animateFreeformWorkspaceBackgroundAlpha(this.mLayoutAlgorithm.getStackState().freeformBackgroundAlpha, new AnimationProps(150, Interpolators.FAST_OUT_SLOW_IN));
        int focusedTaskIndex = Recents.getConfiguration().getLaunchState().getInitialFocusTaskIndex(this.mStack.getTaskCount());
        if (focusedTaskIndex != -1) {
            setFocusedTask(focusedTaskIndex, false, false);
        }
        if (this.mStackScroller.getStackScroll() >= 0.3f || this.mStack.getTaskCount() <= 0) {
            EventBus.getDefault().send(new HideStackActionButtonEvent());
        } else {
            EventBus.getDefault().send(new ShowStackActionButtonEvent(false));
        }
    }

    public boolean isTouchPointInView(float x, float y, TaskView tv) {
        this.mTmpRect.set(tv.getLeft(), tv.getTop(), tv.getRight(), tv.getBottom());
        this.mTmpRect.offset((int) tv.getTranslationX(), (int) tv.getTranslationY());
        return this.mTmpRect.contains((int) x, (int) y);
    }

    public Task findAnchorTask(List<Task> tasks, MutableBoolean isFrontMostTask) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = (Task) tasks.get(i);
            if (!isIgnoredTask(task)) {
                return task;
            }
            if (i == tasks.size() - 1) {
                isFrontMostTask.value = true;
            }
        }
        return null;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport() && this.mFreeformWorkspaceBackground.getAlpha() > 0) {
            this.mFreeformWorkspaceBackground.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        if (who == this.mFreeformWorkspaceBackground) {
            return true;
        }
        return super.verifyDrawable(who);
    }

    public boolean launchFreeformTasks() {
        ArrayList<Task> tasks = this.mStack.getFreeformTasks();
        if (!tasks.isEmpty()) {
            Task frontTask = (Task) tasks.get(tasks.size() - 1);
            if (frontTask != null && frontTask.isFreeformTask()) {
                EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(frontTask), frontTask, null, -1, false));
                return true;
            }
        }
        return false;
    }

    public void onStackTaskAdded(TaskStack stack, Task newTask) {
        AnimationProps animationProps;
        updateLayoutAlgorithm(true);
        if (this.mAwaitingFirstLayout) {
            animationProps = AnimationProps.IMMEDIATE;
        } else {
            animationProps = new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN);
        }
        relayoutTaskViews(animationProps);
    }

    public void onStackTaskRemoved(TaskStack stack, Task removedTask, Task newFrontMostTask, AnimationProps animation, boolean fromDockGesture) {
        if (this.mFocusedTask == removedTask) {
            resetFocusedTask(removedTask);
        }
        TaskView tv = getChildViewForTask(removedTask);
        if (tv != null) {
            this.mViewPool.returnViewToPool(tv);
        }
        removeIgnoreTask(removedTask);
        if (animation != null) {
            updateLayoutAlgorithm(true);
            relayoutTaskViews(animation);
        }
        if (this.mScreenPinningEnabled && newFrontMostTask != null) {
            TaskView frontTv = getChildViewForTask(newFrontMostTask);
            if (frontTv != null) {
                frontTv.showActionButton(true, 200);
            }
        }
        if (this.mStack.getTaskCount() == 0) {
            int i;
            EventBus eventBus = EventBus.getDefault();
            if (fromDockGesture) {
                i = R.string.recents_empty_message;
            } else {
                i = R.string.recents_empty_message_dismissed_all;
            }
            eventBus.send(new AllTaskViewsDismissedEvent(i));
            return;
        }
        EventBus.getDefault().send(new HwTaskViewsDismissedEvent(removedTask));
    }

    public void onStackTasksRemoved(TaskStack stack) {
        resetFocusedTask(getFocusedTask());
        List<TaskView> taskViews = new ArrayList();
        taskViews.addAll(getTaskViews());
        for (int i = taskViews.size() - 1; i >= 0; i--) {
            this.mViewPool.returnViewToPool((TaskView) taskViews.get(i));
        }
        this.mIgnoreTasks.clear();
    }

    public void onStackTasksUpdated(TaskStack stack) {
        updateLayoutAlgorithm(false);
        relayoutTaskViews(AnimationProps.IMMEDIATE);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            bindTaskView(tv, tv.getTask());
        }
    }

    public TaskView createView(Context context) {
        return (TaskView) this.mInflater.inflate(R.layout.recents_task_view, this, false);
    }

    public void onReturnViewToPool(TaskView tv) {
        unbindTaskView(tv, tv.getTask());
        tv.clearAccessibilityFocus();
        tv.resetViewProperties();
        tv.setFocusedState(false, false);
        tv.setClipViewInStack(false);
        if (this.mScreenPinningEnabled) {
            tv.hideActionButton(false, 0, false, null);
        }
        detachViewFromParent(tv);
        updateTaskViewsList();
    }

    public void onPickUpViewFromPool(TaskView tv, Task task, boolean isNewView) {
        int insertIndex = findTaskViewInsertIndex(task, this.mStack.indexOfStackTask(task));
        if (!isNewView) {
            attachViewToParent(tv, insertIndex, tv.getLayoutParams());
        } else if (this.mInMeasureLayout) {
            addView(tv, insertIndex);
        } else {
            LayoutParams params = tv.getLayoutParams();
            if (params == null) {
                params = generateDefaultLayoutParams();
            }
            addViewInLayout(tv, insertIndex, params, true);
            measureTaskView(tv);
            layoutTaskView(true, tv);
        }
        updateTaskViewsList();
        bindTaskView(tv, task);
        if (this.mUIDozeTrigger.isAsleep()) {
            tv.setNoUserInteractionState();
        }
        tv.setCallbacks(this);
        tv.setTouchEnabled(true);
        tv.setClipViewInStack(true);
        if (this.mFocusedTask == task) {
            tv.setFocusedState(true, false);
            if (this.mStartTimerIndicatorDuration > 0) {
                tv.getHeaderView().startFocusTimerIndicator(this.mStartTimerIndicatorDuration);
                this.mStartTimerIndicatorDuration = 0;
            }
        }
        if (this.mScreenPinningEnabled && tv.getTask() == this.mStack.getStackFrontMostTask(false)) {
            tv.showActionButton(false, 0);
        }
    }

    public boolean hasPreferredData(TaskView tv, Task preferredData) {
        return tv.getTask() == preferredData;
    }

    private void bindTaskView(TaskView tv, Task task) {
        tv.onTaskBound(task, this.mTouchExplorationEnabled, this.mDisplayOrientation, this.mDisplayRect);
        Recents.getTaskLoader().loadTaskData(task);
    }

    private void unbindTaskView(TaskView tv, Task task) {
        Recents.getTaskLoader().unloadTaskData(task);
    }

    public void onTaskViewClipStateChanged(TaskView tv) {
        if (!this.mTaskViewsClipDirty) {
            this.mTaskViewsClipDirty = true;
            invalidate();
        }
    }

    public void onFocusStateChanged(int prevFocusState, int curFocusState) {
        if (this.mDeferredTaskViewLayoutAnimation == null) {
            this.mUIDozeTrigger.poke();
            relayoutTaskViewsOnNextFrame(AnimationProps.IMMEDIATE);
        }
    }

    public void onStackScrollChanged(float prevScroll, float curScroll, AnimationProps animation) {
        this.mUIDozeTrigger.poke();
        if (animation != null) {
            relayoutTaskViewsOnNextFrame(animation);
        }
        if (!this.mEnterAnimationComplete) {
            return;
        }
        if (prevScroll > 0.3f && curScroll <= 0.3f && this.mStack.getTaskCount() > 0) {
            EventBus.getDefault().send(new ShowStackActionButtonEvent(true));
        } else if (prevScroll < 0.3f && curScroll >= 0.3f) {
            EventBus.getDefault().send(new HideStackActionButtonEvent());
        }
    }

    public final void onBusEvent(PackagesChangedEvent event) {
        ArraySet<ComponentName> removedComponents = this.mStack.computeComponentsRemoved(event.packageName, event.userId);
        ArrayList<Task> tasks = this.mStack.getStackTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task t = (Task) tasks.get(i);
            if (removedComponents.contains(t.key.getComponent())) {
                TaskView tv = getChildViewForTask(t);
                if (tv != null) {
                    tv.dismissTask();
                } else {
                    this.mStack.removeTask(t, AnimationProps.IMMEDIATE, false);
                }
            }
        }
    }

    public final void onBusEvent(LaunchTaskEvent event) {
        this.mUIDozeTrigger.stopDozing();
    }

    public final void onBusEvent(LaunchNextTaskRequestEvent event) {
        int launchTaskIndex = this.mStack.indexOfStackTask(this.mStack.getLaunchTarget());
        if (launchTaskIndex != -1) {
            launchTaskIndex = Math.max(0, launchTaskIndex - 1);
        } else {
            launchTaskIndex = this.mStack.getTaskCount() - 1;
        }
        if (launchTaskIndex != -1) {
            cancelAllTaskViewAnimations();
            Task launchTask = (Task) this.mStack.getStackTasks().get(launchTaskIndex);
            EventBus.getDefault().send(new LaunchTaskEvent(getChildViewForTask(launchTask), launchTask, null, -1, false));
            MetricsLogger.action(getContext(), 318, launchTask.key.getComponent().toString());
        } else if (this.mStack.getTaskCount() == 0) {
            EventBus.getDefault().send(new HideRecentsEvent(false, true));
        }
    }

    public final void onBusEvent(LaunchTaskStartedEvent event) {
        this.mAnimationHelper.startLaunchTaskAnimation(event.taskView, event.screenPinningRequested, event.getAnimationTrigger());
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        this.mTouchHandler.cancelNonDismissTaskAnimations();
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        cancelDeferredTaskViewLayoutAnimation();
        this.mAnimationHelper.startExitToHomeAnimation(event.animated, event.getAnimationTrigger());
        animateFreeformWorkspaceBackgroundAlpha(0, new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(DismissFocusedTaskViewEvent event) {
        if (this.mFocusedTask != null) {
            TaskView tv = getChildViewForTask(this.mFocusedTask);
            if (tv != null) {
                tv.dismissTask();
            }
            resetFocusedTask(this.mFocusedTask);
        }
    }

    public final void onBusEvent(DismissTaskViewEvent event) {
        this.mAnimationHelper.startDeleteTaskAnimation(event.taskView, event.getAnimationTrigger());
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public final void onBusEvent(DismissAllTaskViewsEvent event) {
        PerfDebugUtils.perfRecentsRemoveAllElapsedTimeEnd(1);
        this.mAnimationHelper.startDeleteAllTasksAnimation(getTaskViews(), event.getAnimationTrigger());
        event.addPostAnimationCallback(new Runnable() {
            public void run() {
                HwRecentsHelper.setAllTaskRemovingAllFlag(true);
                EventBus.getDefault().send(new AllTaskViewsDismissedEvent(R.string.recents_empty_message_dismissed_all));
                PerfDebugUtils.perfRecentsRemoveAllElapsedTimeEnd(3);
            }
        });
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent event) {
        if (!event.visible && HwRecentsHelper.getAllTaskRemovingAllFlag()) {
            ArrayList<Task> tasks = new ArrayList(this.mStack.getStackTasks());
            announceForAccessibility(getContext().getString(R.string.accessibility_recents_all_items_dismissed));
            this.mStack.removeAllTasks();
            for (int i = tasks.size() - 1; i >= 0; i--) {
                Task task = (Task) tasks.get(i);
                if (!(task.isLocked || HwRecentsHelper.getPlayingMusicUid(getContext(), task))) {
                    EventBus.getDefault().send(new DeleteTaskDataEvent(task, true));
                }
            }
            MetricsLogger.action(getContext(), 357);
            HwRecentsHelper.setAllTaskRemovingAllFlag(false);
        }
    }

    public final void onBusEvent(TaskViewDismissedEvent event) {
        announceForAccessibility(getContext().getString(R.string.accessibility_recents_item_dismissed, new Object[]{event.task.title}));
        this.mStack.removeTask(event.task, event.animation, false);
        if (event.task.isLocked) {
            event.task.isLocked = false;
            HwRecentsLockUtils.insertOrUpdate(getContext(), event.task);
        }
        EventBus.getDefault().send(new DeleteTaskDataEvent(event.task));
        MetricsLogger.action(getContext(), 289, event.task.key.getComponent().toString());
    }

    public final void onBusEvent(FocusNextTaskViewEvent event) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(true, false, true, false, event.timerIndicatorDuration);
    }

    public final void onBusEvent(FocusPreviousTaskViewEvent event) {
        this.mStackScroller.stopScroller();
        this.mStackScroller.stopBoundScrollAnimation();
        setRelativeFocusedTask(false, false, true);
    }

    public final void onBusEvent(UserInteractionEvent event) {
        this.mUIDozeTrigger.poke();
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled() && this.mFocusedTask != null) {
            TaskView tv = getChildViewForTask(this.mFocusedTask);
            if (tv != null) {
                tv.getHeaderView().cancelFocusTimerIndicator();
            }
        }
    }

    public final void onBusEvent(DragStartEvent event) {
        addIgnoreTask(event.task);
        if (event.task.isFreeformTask()) {
            this.mStackScroller.animateScroll(this.mLayoutAlgorithm.mInitialScrollP, null);
        }
        float finalScale = event.taskView.getScaleX() * 1.05f;
        this.mLayoutAlgorithm.getStackTransform(event.task, getScroller().getStackScroll(), this.mTmpTransform, null);
        this.mTmpTransform.scale = finalScale;
        this.mTmpTransform.translationZ = (float) (this.mLayoutAlgorithm.mMaxTranslationZ + 1);
        this.mTmpTransform.dimAlpha = 0.0f;
        updateTaskViewToTransform(event.taskView, this.mTmpTransform, new AnimationProps(175, Interpolators.FAST_OUT_SLOW_IN));
    }

    public final void onBusEvent(DragStartInitializeDropTargetsEvent event) {
        if (Recents.getSystemServices().hasFreeformWorkspaceSupport()) {
            event.handler.registerDropTargetForCurrentDrag(this.mStackDropTarget);
            event.handler.registerDropTargetForCurrentDrag(this.mFreeformWorkspaceDropTarget);
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        AnimationProps animation = new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN);
        boolean ignoreTaskOverrides = false;
        if (event.dropTarget instanceof DockState) {
            DockState dockState = event.dropTarget;
            Rect systemInsets = new Rect(this.mStableLayoutAlgorithm.mSystemInsets);
            int height = getMeasuredHeight() - systemInsets.bottom;
            systemInsets.bottom = 0;
            this.mStackBounds.set(dockState.getDockedTaskStackBounds(this.mDisplayRect, getMeasuredWidth(), height, this.mDividerSize, systemInsets, this.mLayoutAlgorithm, getResources(), this.mWindowRect));
            this.mLayoutAlgorithm.setSystemInsets(systemInsets);
            this.mLayoutAlgorithm.initialize(this.mDisplayRect, this.mWindowRect, this.mStackBounds, StackState.getStackStateForStack(this.mStack));
            updateLayoutAlgorithm(true);
            ignoreTaskOverrides = true;
        } else {
            removeIgnoreTask(event.task);
            updateLayoutToStableBounds();
            addIgnoreTask(event.task);
        }
        relayoutTaskViews(animation, null, ignoreTaskOverrides);
    }

    public final void onBusEvent(final DragEndEvent event) {
        boolean hasChangedStacks = false;
        if (event.dropTarget instanceof DockState) {
            this.mLayoutAlgorithm.clearUnfocusedTaskOverrides();
            return;
        }
        boolean isFreeformTask = event.task.isFreeformTask();
        if (!isFreeformTask && event.dropTarget == this.mFreeformWorkspaceDropTarget) {
            hasChangedStacks = true;
        } else if (isFreeformTask && event.dropTarget == this.mStackDropTarget) {
            hasChangedStacks = true;
        }
        if (hasChangedStacks) {
            if (event.dropTarget == this.mFreeformWorkspaceDropTarget) {
                this.mStack.moveTaskToStack(event.task, 2);
            } else if (event.dropTarget == this.mStackDropTarget) {
                this.mStack.moveTaskToStack(event.task, 1);
            }
            updateLayoutAlgorithm(true);
            event.addPostAnimationCallback(new Runnable() {
                public void run() {
                    Recents.getSystemServices().moveTaskToStack(event.task.key.id, event.task.key.stackId);
                }
            });
        }
        removeIgnoreTask(event.task);
        Utilities.setViewFrameFromTranslation(event.taskView);
        new ArrayMap().put(event.task, new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN, event.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN));
        event.getAnimationTrigger().increment();
    }

    public final void onBusEvent(DragEndCancelledEvent event) {
        removeIgnoreTask(event.task);
        updateLayoutToStableBounds();
        Utilities.setViewFrameFromTranslation(event.taskView);
        new ArrayMap().put(event.task, new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN, event.getAnimationTrigger().decrementOnAnimationEnd()));
        relayoutTaskViews(new AnimationProps(250, Interpolators.FAST_OUT_SLOW_IN));
        event.getAnimationTrigger().increment();
    }

    public final void onBusEvent(IterateRecentsEvent event) {
        if (!this.mEnterAnimationComplete) {
            EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
        }
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        this.mEnterAnimationComplete = true;
        if (this.mStack.getTaskCount() > 0) {
            this.enterRecentsWindowAnimationCompleted = true;
            PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(6);
            this.mAnimationHelper.startEnterAnimation(event.getAnimationTrigger());
            event.addPostAnimationCallback(new Runnable() {
                public void run() {
                    TaskStackView.this.enterRecentsWindowAnimationCompleted = false;
                    if (TaskStackView.this.mFocusedTask != null) {
                        TaskStackView.this.setFocusedTask(TaskStackView.this.mStack.indexOfStackTask(TaskStackView.this.mFocusedTask), false, Recents.getConfiguration().getLaunchState().launchedWithAltTab);
                        TaskView focusedTaskView = TaskStackView.this.getChildViewForTask(TaskStackView.this.mFocusedTask);
                        if (TaskStackView.this.mTouchExplorationEnabled && focusedTaskView != null) {
                            focusedTaskView.requestAccessibilityFocus();
                        }
                    }
                    EventBus.getDefault().send(new EnterRecentsTaskStackAnimationCompletedEvent());
                    PerfDebugUtils.perfRecentsLaunchElapsedTimeEnd(6);
                    PerfDebugUtils.dumpRecentsLaunchTime();
                }
            });
            this.mUIDozeTrigger.startDozing();
        }
    }

    public final void onBusEvent(UpdateFreeformTaskViewVisibilityEvent event) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = (TaskView) taskViews.get(i);
            if (tv.getTask().isFreeformTask()) {
                tv.setVisibility(event.visible ? 0 : 4);
            }
        }
    }

    public final void onBusEvent(final MultiWindowStateChangedEvent event) {
        if (event.inMultiWindow || !event.showDeferredAnimation) {
            setTasks(event.stack, true);
            return;
        }
        Recents.getConfiguration().getLaunchState().reset();
        event.getAnimationTrigger().increment();
        post(new Runnable() {
            public void run() {
                TaskStackView.this.mAnimationHelper.startNewStackScrollAnimation(event.stack, event.getAnimationTrigger());
                event.getAnimationTrigger().decrement();
            }
        });
    }

    public final void onBusEvent(ConfigurationChangedEvent event) {
        if (event.fromDeviceOrientationChange) {
            this.mDisplayOrientation = Utilities.getAppConfiguration(this.mContext).orientation;
            this.mDisplayRect = Recents.getSystemServices().getDisplayRect();
            this.mStackScroller.stopScroller();
        }
        reloadOnConfigurationChange();
        if (!event.fromMultiWindow) {
            this.mTmpTaskViews.clear();
            this.mTmpTaskViews.addAll(getTaskViews());
            this.mTmpTaskViews.addAll(this.mViewPool.getViews());
            int taskViewCount = this.mTmpTaskViews.size();
            for (int i = 0; i < taskViewCount; i++) {
                ((TaskView) this.mTmpTaskViews.get(i)).onConfigurationChanged();
            }
        }
        if (event.fromMultiWindow) {
            this.mInitialState = 2;
            requestLayout();
        } else if (event.fromDeviceOrientationChange) {
            this.mInitialState = 1;
            requestLayout();
        }
    }

    public final void onBusEvent(RecentsGrowingEvent event) {
        this.mResetToInitialStateWhenResized = true;
    }

    public void reloadOnConfigurationChange() {
        this.mStableLayoutAlgorithm.reloadOnConfigurationChange(getContext());
        this.mLayoutAlgorithm.reloadOnConfigurationChange(getContext());
    }

    private void animateFreeformWorkspaceBackgroundAlpha(int targetAlpha, AnimationProps animation) {
        if (this.mFreeformWorkspaceBackground.getAlpha() != targetAlpha) {
            Utilities.cancelAnimationWithoutCallbacks(this.mFreeformWorkspaceBackgroundAnimator);
            this.mFreeformWorkspaceBackgroundAnimator = ObjectAnimator.ofInt(this.mFreeformWorkspaceBackground, Utilities.DRAWABLE_ALPHA, new int[]{this.mFreeformWorkspaceBackground.getAlpha(), targetAlpha});
            this.mFreeformWorkspaceBackgroundAnimator.setStartDelay(animation.getDuration(4));
            this.mFreeformWorkspaceBackgroundAnimator.setDuration(animation.getDuration(4));
            this.mFreeformWorkspaceBackgroundAnimator.setInterpolator(animation.getInterpolator(4));
            this.mFreeformWorkspaceBackgroundAnimator.start();
        }
    }

    private int findTaskViewInsertIndex(Task task, int taskIndex) {
        if (taskIndex != -1) {
            List<TaskView> taskViews = getTaskViews();
            boolean foundTaskView = false;
            int taskViewCount = taskViews.size();
            for (int i = 0; i < taskViewCount; i++) {
                Task tvTask = ((TaskView) taskViews.get(i)).getTask();
                if (tvTask == task) {
                    foundTaskView = true;
                } else if (taskIndex < this.mStack.indexOfStackTask(tvTask)) {
                    if (foundTaskView) {
                        return i - 1;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    private void readSystemFlags() {
        boolean z = false;
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mTouchExplorationEnabled = ssp.isTouchExplorationEnabled();
        if (ssp.getSystemSetting(getContext(), "lock_to_app_enabled") != 0) {
            z = true;
        }
        this.mScreenPinningEnabled = z;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        String id = Integer.toHexString(System.identityHashCode(this));
        writer.print(prefix);
        writer.print("TaskStackView");
        writer.print(" hasDefRelayout=");
        writer.print(this.mDeferredTaskViewLayoutAnimation != null ? "Y" : "N");
        writer.print(" clipDirty=");
        writer.print(this.mTaskViewsClipDirty ? "Y" : "N");
        writer.print(" awaitingFirstLayout=");
        writer.print(this.mAwaitingFirstLayout ? "Y" : "N");
        writer.print(" initialState=");
        writer.print(this.mInitialState);
        writer.print(" inMeasureLayout=");
        writer.print(this.mInMeasureLayout ? "Y" : "N");
        writer.print(" enterAnimCompleted=");
        writer.print(this.mEnterAnimationComplete ? "Y" : "N");
        writer.print(" touchExplorationOn=");
        writer.print(this.mTouchExplorationEnabled ? "Y" : "N");
        writer.print(" screenPinningOn=");
        writer.print(this.mScreenPinningEnabled ? "Y" : "N");
        writer.print(" numIgnoreTasks=");
        writer.print(this.mIgnoreTasks.size());
        writer.print(" numViewPool=");
        writer.print(this.mViewPool.getViews().size());
        writer.print(" stableStackBounds=");
        writer.print(Utilities.dumpRect(this.mStableStackBounds));
        writer.print(" stackBounds=");
        writer.print(Utilities.dumpRect(this.mStackBounds));
        writer.print(" stableWindow=");
        writer.print(Utilities.dumpRect(this.mStableWindowRect));
        writer.print(" window=");
        writer.print(Utilities.dumpRect(this.mWindowRect));
        writer.print(" display=");
        writer.print(Utilities.dumpRect(this.mDisplayRect));
        writer.print(" orientation=");
        writer.print(this.mDisplayOrientation);
        writer.print(" [0x");
        writer.print(id);
        writer.print("]");
        writer.println();
        if (this.mFocusedTask != null) {
            writer.print(innerPrefix);
            writer.print("Focused task: ");
            this.mFocusedTask.dump(BuildConfig.FLAVOR, writer);
        }
        this.mLayoutAlgorithm.dump(innerPrefix, writer);
        this.mStackScroller.dump(innerPrefix, writer);
    }
}
