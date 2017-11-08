package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityOptions.OnAnimationStartedListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.AppTransitionAnimationSpec;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.model.TaskStack.DockState;
import com.android.systemui.recents.model.TaskStack.DockState.ViewState;
import com.android.systemui.recents.views.RecentsTransitionHelper.AnimationSpecComposer;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.utils.analyze.BDReporter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class RecentsView extends FrameLayout {
    private boolean mAwaitingFirstLayout;
    private Drawable mBackgroundScrim;
    private Animator mBackgroundScrimAnimator;
    private int mDividerSize;
    private TextView mEmptyView;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private boolean mLastTaskLaunchedWasFreeform;
    protected TaskStack mStack;
    private TextView mStackActionButton;
    @ExportedProperty(category = "recents")
    protected Rect mSystemInsets;
    private TaskStackView mTaskStackView;
    @ExportedProperty(deepExport = true, prefix = "touch_")
    private RecentsViewTouchHandler mTouchHandler;
    private RecentsTransitionHelper mTransitionHelper;

    /* renamed from: com.android.systemui.recents.views.RecentsView$5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ int val$duration;
        final /* synthetic */ boolean val$translate;

        AnonymousClass5(boolean val$translate, int val$duration) {
            this.val$translate = val$translate;
            this.val$duration = val$duration;
        }

        public void run() {
            if (this.val$translate) {
                RecentsView.this.mStackActionButton.animate().translationY(0.0f);
            }
            RecentsView.this.mStackActionButton.animate().alpha(1.0f).setDuration((long) this.val$duration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
        }
    }

    /* renamed from: com.android.systemui.recents.views.RecentsView$6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ ReferenceCountedTrigger val$postAnimationTrigger;

        AnonymousClass6(ReferenceCountedTrigger val$postAnimationTrigger) {
            this.val$postAnimationTrigger = val$postAnimationTrigger;
        }

        public void run() {
            RecentsView.this.mStackActionButton.setVisibility(4);
            this.val$postAnimationTrigger.decrement();
        }
    }

    public RecentsView(Context context) {
        this(context, null);
    }

    public RecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mAwaitingFirstLayout = true;
        this.mSystemInsets = new Rect();
        this.mBackgroundScrim = new ColorDrawable(Color.argb(84, 0, 0, 0)).mutate();
        setWillNotDraw(false);
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mTransitionHelper = new RecentsTransitionHelper(getContext());
        this.mDividerSize = ssp.getDockedDividerSize(context);
        this.mTouchHandler = new RecentsViewTouchHandler(this);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);
        this.mEmptyView = (TextView) LayoutInflater.from(context).inflate(R.layout.hw_recents_empty, this, false);
        addView(this.mEmptyView);
    }

    public void onReload(boolean isResumingFromVisible, boolean isTaskStackEmpty) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.mTaskStackView == null) {
            isResumingFromVisible = false;
            this.mTaskStackView = new TaskStackView(getContext());
            this.mTaskStackView.setSystemInsets(this.mSystemInsets);
            addView(this.mTaskStackView);
        }
        this.mAwaitingFirstLayout = !isResumingFromVisible;
        this.mLastTaskLaunchedWasFreeform = false;
        this.mTaskStackView.onReload(isResumingFromVisible);
        if (isResumingFromVisible) {
            animateBackgroundScrim(1.0f, 200);
        } else if (launchState.launchedViaDockGesture || launchState.launchedFromApp || isTaskStackEmpty) {
            this.mBackgroundScrim.setAlpha(255);
        } else {
            this.mBackgroundScrim.setAlpha(0);
        }
    }

    public void updateStack(TaskStack stack, boolean setStackViewTasks) {
        this.mStack = stack;
        if (setStackViewTasks) {
            this.mTaskStackView.setTasks(stack, true);
        }
        if (stack.getTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView(R.string.hw_recents_empty_message);
        }
    }

    public TaskStack getStack() {
        return this.mStack;
    }

    public Drawable getBackgroundScrim() {
        return this.mBackgroundScrim;
    }

    public boolean isLastTaskLaunchedFreeform() {
        return this.mLastTaskLaunchedWasFreeform;
    }

    public boolean launchFocusedTask(int logEvent) {
        if (this.mTaskStackView != null) {
            Task task = this.mTaskStackView.getFocusedTask();
            if (task != null) {
                EventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(task), task, null, -1, false));
                if (logEvent != 0) {
                    MetricsLogger.action(getContext(), logEvent, task.key.getComponent().toString());
                }
                return true;
            }
        }
        return false;
    }

    public boolean launchPreviousTask() {
        if (this.mTaskStackView != null) {
            Task task = this.mTaskStackView.getStack().getLaunchTarget();
            if (task != null) {
                EventBus.getDefault().send(new LaunchTaskEvent(this.mTaskStackView.getChildViewForTask(task), task, null, -1, false));
                return true;
            }
        }
        return false;
    }

    public void showEmptyView(int msgResId) {
        this.mTaskStackView.setVisibility(4);
        this.mEmptyView.setText(msgResId);
        this.mEmptyView.setVisibility(0);
    }

    public void hideEmptyView() {
        this.mEmptyView.setVisibility(4);
        this.mTaskStackView.setVisibility(0);
    }

    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        EventBus.getDefault().register(this.mTouchHandler, 4);
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(this.mTouchHandler);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            measureChild(this.mEmptyView, MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
        }
        setMeasuredDimension(width, height);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mTaskStackView.getVisibility() != 8) {
            this.mTaskStackView.layout(left, top, getMeasuredWidth() + left, getMeasuredHeight() + top);
        }
        if (this.mEmptyView.getVisibility() != 8) {
            int leftRightInsets = this.mSystemInsets.left + this.mSystemInsets.right;
            int topBottomInsets = this.mSystemInsets.top + this.mSystemInsets.bottom;
            int childWidth = this.mEmptyView.getMeasuredWidth();
            int childHeight = this.mEmptyView.getMeasuredHeight();
            int childLeft = (this.mSystemInsets.left + left) + (Math.max(0, ((right - left) - leftRightInsets) - childWidth) / 2);
            int childTop = (this.mSystemInsets.top + top) + (Math.max(0, ((bottom - top) - topBottomInsets) - childHeight) / 2);
            this.mEmptyView.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
        if (this.mAwaitingFirstLayout) {
            this.mAwaitingFirstLayout = false;
            if (Recents.getConfiguration().getLaunchState().launchedViaDragGesture) {
                setTranslationY((float) getMeasuredHeight());
            } else {
                setTranslationY(0.0f);
            }
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mSystemInsets.set(insets.getSystemWindowInsets());
        this.mTaskStackView.setSystemInsets(this.mSystemInsets);
        requestLayout();
        return insets;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mTouchHandler.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return this.mTouchHandler.onTouchEvent(ev);
    }

    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        ArrayList<DockState> visDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            ((DockState) visDockStates.get(i)).viewState.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        ArrayList<DockState> visDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            if (((DockState) visDockStates.get(i)).viewState.dockAreaOverlay == who) {
                return true;
            }
        }
        return super.verifyDrawable(who);
    }

    public void onBusEvent(LaunchTaskEvent event) {
        this.mLastTaskLaunchedWasFreeform = event.task.isFreeformTask();
        this.mTransitionHelper.launchTaskFromRecents(this.mStack, event.task, this.mTaskStackView, event.taskView, event.screenPinningRequested, event.targetTaskBounds, event.targetTaskStack);
    }

    public void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        animateBackgroundScrim(0.0f, 200);
    }

    @FindBugsSuppressWarnings({"UWF_UNWRITTEN_FIELD"})
    public void onBusEvent(DragStartEvent event) {
        updateVisibleDockRegions(this.mTouchHandler.getDockStatesForCurrentOrientation(), true, DockState.NONE.viewState.dockAreaAlpha, DockState.NONE.viewState.hintTextAlpha, true, false);
        if (this.mStackActionButton != null) {
            this.mStackActionButton.animate().alpha(0.0f).setDuration(100).setInterpolator(Interpolators.ALPHA_OUT).start();
        }
    }

    public void onBusEvent(DragDropTargetChangedEvent event) {
        if (event.dropTarget == null || !(event.dropTarget instanceof DockState)) {
            updateVisibleDockRegions(this.mTouchHandler.getDockStatesForCurrentOrientation(), true, DockState.NONE.viewState.dockAreaAlpha, DockState.NONE.viewState.hintTextAlpha, true, true);
        } else {
            updateVisibleDockRegions(new DockState[]{event.dropTarget}, false, -1, -1, true, true);
        }
        if (this.mStackActionButton != null) {
            event.addPostAnimationCallback(new Runnable() {
                public void run() {
                    Rect buttonBounds = RecentsView.this.getStackActionButtonBoundsFromStackLayout();
                    RecentsView.this.mStackActionButton.setLeftTopRightBottom(buttonBounds.left, buttonBounds.top, buttonBounds.right, buttonBounds.bottom);
                }
            });
        }
    }

    public void onBusEvent(final DragEndEvent event) {
        if (event.dropTarget instanceof DockState) {
            DockState dockState = event.dropTarget;
            updateVisibleDockRegions(null, false, -1, -1, false, false);
            Utilities.setViewFrameFromTranslation(event.taskView);
            SystemServicesProxy ssp = Recents.getSystemServices();
            if (ssp.startTaskInDockedMode(event.task.key.id, dockState.createMode)) {
                BDReporter.e(getContext(), 337, "pkg:" + event.task.packageName);
                OnAnimationStartedListener startedListener = new OnAnimationStartedListener() {
                    public void onAnimationStarted() {
                        EventBus.getDefault().send(new DockedFirstAnimationFrameEvent());
                        RecentsView.this.mTaskStackView.getStack().removeTask(event.task, null, true);
                    }
                };
                final Rect taskRect = getTaskRect(event.taskView);
                ssp.overridePendingAppTransitionMultiThumbFuture(this.mTransitionHelper.getAppTransitionFuture(new AnimationSpecComposer() {
                    public List<AppTransitionAnimationSpec> composeSpecs() {
                        return RecentsView.this.mTransitionHelper.composeDockAnimationSpec(event.taskView, taskRect);
                    }
                }), this.mTransitionHelper.wrapStartedListener(startedListener), true);
                MetricsLogger.action(this.mContext, 270, event.task.getTopComponent().flattenToShortString());
            } else {
                EventBus.getDefault().send(new DragEndCancelledEvent(this.mStack, event.task, event.taskView));
            }
        } else {
            updateVisibleDockRegions(null, true, -1, -1, true, false);
        }
        if (this.mStackActionButton != null) {
            this.mStackActionButton.animate().alpha(1.0f).setDuration(134).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    public final void onBusEvent(DragEndCancelledEvent event) {
        updateVisibleDockRegions(null, true, -1, -1, true, false);
    }

    private Rect getTaskRect(TaskView taskView) {
        int[] location = taskView.getLocationOnScreen();
        int viewX = location[0];
        int viewY = location[1];
        return new Rect(viewX, viewY, (int) (((float) viewX) + (((float) taskView.getWidth()) * taskView.getScaleX())), (int) (((float) viewY) + (((float) taskView.getHeight()) * taskView.getScaleY())));
    }

    public void onBusEvent(DraggingInRecentsEvent event) {
        if (this.mTaskStackView.getTaskViews().size() > 0) {
            setTranslationY(event.distanceFromTop - ((TaskView) this.mTaskStackView.getTaskViews().get(0)).getY());
        }
    }

    public void onBusEvent(DraggingInRecentsEndedEvent event) {
        ViewPropertyAnimator animator = animate();
        if (event.velocity > this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            animator.translationY((float) getHeight());
            animator.withEndAction(new Runnable() {
                public void run() {
                    WindowManagerProxy.getInstance().maximizeDockedStack();
                }
            });
            this.mFlingAnimationUtils.apply(animator, getTranslationY(), (float) getHeight(), event.velocity);
        } else {
            animator.translationY(0.0f);
            animator.setListener(null);
            this.mFlingAnimationUtils.apply(animator, getTranslationY(), 0.0f, event.velocity);
        }
        animator.start();
    }

    public void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!launchState.launchedViaDockGesture && !launchState.launchedFromApp && this.mStack.getTaskCount() > 0) {
            animateBackgroundScrim(1.0f, 300);
        }
    }

    public void onBusEvent(AllTaskViewsDismissedEvent event) {
        hideStackActionButton(100, true);
    }

    public void onBusEvent(DismissAllTaskViewsEvent event) {
        if (!Recents.getSystemServices().hasDockedTask()) {
            animateBackgroundScrim(0.0f, 200);
        }
    }

    public void onBusEvent(ShowStackActionButtonEvent event) {
    }

    public void onBusEvent(HideStackActionButtonEvent event) {
    }

    public void onBusEvent(MultiWindowStateChangedEvent event) {
        updateStack(event.stack, false);
    }

    @FindBugsSuppressWarnings({"UPM_UNCALLED_PRIVATE_METHOD"})
    private void showStackActionButton(int duration, boolean translate) {
    }

    @FindBugsSuppressWarnings({"UPM_UNCALLED_PRIVATE_METHOD"})
    private void hideStackActionButton(int duration, boolean translate) {
    }

    @FindBugsSuppressWarnings({"UPM_UNCALLED_PRIVATE_METHOD"})
    private void hideStackActionButton(int duration, boolean translate, ReferenceCountedTrigger postAnimationTrigger) {
    }

    private void updateVisibleDockRegions(DockState[] newDockStates, boolean isDefaultDockState, int overrideAreaAlpha, int overrideHintAlpha, boolean animateAlpha, boolean animateBounds) {
        ArraySet<DockState> newDockStatesSet = Utilities.arrayToSet(newDockStates, new ArraySet());
        ArrayList<DockState> visDockStates = this.mTouchHandler.getVisibleDockStates();
        for (int i = visDockStates.size() - 1; i >= 0; i--) {
            DockState dockState = (DockState) visDockStates.get(i);
            ViewState viewState = dockState.viewState;
            if (newDockStates == null || !newDockStatesSet.contains(dockState)) {
                viewState.startAnimation(null, 0, 0, 250, Interpolators.FAST_OUT_SLOW_IN, animateAlpha, animateBounds);
            } else {
                int areaAlpha;
                int hintAlpha;
                Rect bounds;
                if (overrideAreaAlpha != -1) {
                    areaAlpha = overrideAreaAlpha;
                } else {
                    areaAlpha = viewState.dockAreaAlpha;
                }
                if (overrideHintAlpha != -1) {
                    hintAlpha = overrideHintAlpha;
                } else {
                    hintAlpha = viewState.hintTextAlpha;
                }
                if (isDefaultDockState) {
                    bounds = dockState.getPreDockedBounds(getMeasuredWidth(), getMeasuredHeight());
                } else {
                    bounds = dockState.getDockedBounds(getMeasuredWidth(), getMeasuredHeight(), this.mDividerSize, this.mSystemInsets, getResources());
                }
                if (viewState.dockAreaOverlay.getCallback() != this) {
                    viewState.dockAreaOverlay.setCallback(this);
                    viewState.dockAreaOverlay.setBounds(bounds);
                }
                viewState.startAnimation(bounds, areaAlpha, hintAlpha, 250, Interpolators.FAST_OUT_SLOW_IN, animateAlpha, animateBounds);
            }
        }
    }

    private void animateBackgroundScrim(float alpha, int duration) {
        TimeInterpolator timeInterpolator;
        Utilities.cancelAnimationWithoutCallbacks(this.mBackgroundScrimAnimator);
        int fromAlpha = (int) ((((float) this.mBackgroundScrim.getAlpha()) / 84.15f) * 255.0f);
        int toAlpha = (int) (alpha * 255.0f);
        this.mBackgroundScrimAnimator = ObjectAnimator.ofInt(this.mBackgroundScrim, Utilities.DRAWABLE_ALPHA, new int[]{fromAlpha, toAlpha});
        this.mBackgroundScrimAnimator.setDuration((long) duration);
        Animator animator = this.mBackgroundScrimAnimator;
        if (toAlpha > fromAlpha) {
            timeInterpolator = Interpolators.ALPHA_IN;
        } else {
            timeInterpolator = Interpolators.ALPHA_OUT;
        }
        animator.setInterpolator(timeInterpolator);
        this.mBackgroundScrimAnimator.start();
    }

    @FindBugsSuppressWarnings({"NP_UNWRITTEN_FIELD"})
    private Rect getStackActionButtonBoundsFromStackLayout() {
        int left;
        Rect actionButtonRect = new Rect(this.mTaskStackView.mLayoutAlgorithm.mStackActionButtonRect);
        if (isLayoutRtl()) {
            left = actionButtonRect.left - this.mStackActionButton.getPaddingLeft();
        } else {
            left = (actionButtonRect.right + this.mStackActionButton.getPaddingRight()) - this.mStackActionButton.getMeasuredWidth();
        }
        int top = actionButtonRect.top + ((actionButtonRect.height() - this.mStackActionButton.getMeasuredHeight()) / 2);
        actionButtonRect.set(left, top, this.mStackActionButton.getMeasuredWidth() + left, this.mStackActionButton.getMeasuredHeight() + top);
        return actionButtonRect;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        String id = Integer.toHexString(System.identityHashCode(this));
        writer.print(prefix);
        writer.print("RecentsView");
        writer.print(" awaitingFirstLayout=");
        writer.print(this.mAwaitingFirstLayout ? "Y" : "N");
        writer.print(" insets=");
        writer.print(Utilities.dumpRect(this.mSystemInsets));
        writer.print(" [0x");
        writer.print(id);
        writer.print("]");
        writer.println();
        if (this.mStack != null) {
            this.mStack.dump(innerPrefix, writer);
        }
        if (this.mTaskStackView != null) {
            this.mTaskStackView.dump(innerPrefix, writer);
        }
    }
}
