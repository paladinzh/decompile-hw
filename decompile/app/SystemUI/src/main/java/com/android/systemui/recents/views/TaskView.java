package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewOutlineProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.Task.TaskCallbacks;
import com.android.systemui.recents.model.TaskStack.DockState;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;

public class TaskView extends FixedSizeFrameLayout implements TaskCallbacks, OnClickListener, OnLongClickListener {
    public static final Property<TaskView, Float> DIM_ALPHA = new FloatProperty<TaskView>("dimAlpha") {
        public void setValue(TaskView tv, float dimAlpha) {
            tv.setDimAlpha(dimAlpha);
        }

        public Float get(TaskView tv) {
            return Float.valueOf(tv.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> DIM_ALPHA_WITHOUT_HEADER = new FloatProperty<TaskView>("dimAlphaWithoutHeader") {
        public void setValue(TaskView tv, float dimAlpha) {
            tv.setDimAlphaWithoutHeader(dimAlpha);
        }

        public Float get(TaskView tv) {
            return Float.valueOf(tv.getDimAlpha());
        }
    };
    public static final Property<TaskView, Float> VIEW_OUTLINE_ALPHA = new FloatProperty<TaskView>("viewOutlineAlpha") {
        public void setValue(TaskView tv, float alpha) {
            tv.getViewBounds().setAlpha(alpha);
        }

        public Float get(TaskView tv) {
            return Float.valueOf(tv.getViewBounds().getAlpha());
        }
    };
    private float mActionButtonTranslationZ;
    View mActionButtonView;
    TaskViewCallbacks mCb;
    @ExportedProperty(category = "recents")
    private boolean mClipViewInStack;
    @ExportedProperty(category = "recents")
    private float mDimAlpha;
    private ObjectAnimator mDimAnimator;
    private Toast mDisabledAppToast;
    @ExportedProperty(category = "recents")
    private Point mDownTouchPos;
    @ExportedProperty(deepExport = true, prefix = "header_")
    TaskViewHeader mHeaderView;
    View mIncompatibleAppToastView;
    @ExportedProperty(category = "recents")
    private boolean mIsDisabledInSafeMode;
    private ObjectAnimator mOutlineAnimator;
    private final TaskViewTransform mTargetAnimationTransform;
    @ExportedProperty(deepExport = true, prefix = "task_")
    private Task mTask;
    @ExportedProperty(deepExport = true, prefix = "thumbnail_")
    TaskViewThumbnail mThumbnailView;
    private ArrayList<Animator> mTmpAnimators;
    @ExportedProperty(category = "recents")
    private boolean mTouchExplorationEnabled;
    private AnimatorSet mTransformAnimation;
    @ExportedProperty(deepExport = true, prefix = "view_bounds_")
    private AnimateableViewBounds mViewBounds;

    interface TaskViewCallbacks {
        void onTaskViewClipStateChanged(TaskView taskView);
    }

    final /* synthetic */ class -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndCancelledEvent_event_LambdaImpl0 implements Runnable {
        private /* synthetic */ TaskView val$this;

        public /* synthetic */ -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndCancelledEvent_event_LambdaImpl0(TaskView taskView) {
            this.val$this = taskView;
        }

        public void run() {
            this.val$this.-com_android_systemui_recents_views_TaskView_lambda$2();
        }
    }

    final /* synthetic */ class -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndEvent_event_LambdaImpl0 implements Runnable {
        private /* synthetic */ TaskView val$this;

        public /* synthetic */ -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndEvent_event_LambdaImpl0(TaskView taskView) {
            this.val$this = taskView;
        }

        public void run() {
            this.val$this.-com_android_systemui_recents_views_TaskView_lambda$1();
        }
    }

    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mClipViewInStack = true;
        this.mTargetAnimationTransform = new TaskViewTransform();
        this.mTmpAnimators = new ArrayList();
        this.mDownTouchPos = new Point();
        RecentsConfiguration config = Recents.getConfiguration();
        Resources res = context.getResources();
        this.mViewBounds = new AnimateableViewBounds(this, res.getDimensionPixelSize(R.dimen.recents_task_view_shadow_rounded_corners_radius));
        if (config.fakeShadows) {
            setBackground(new FakeShadowDrawable(res, config));
        }
        setOutlineProvider(this.mViewBounds);
        setOnLongClickListener(this);
    }

    void setCallbacks(TaskViewCallbacks cb) {
        this.mCb = cb;
    }

    void onReload(boolean isResumingFromVisible) {
        resetNoUserInteractionState();
        if (!isResumingFromVisible) {
            resetViewProperties();
        }
    }

    public Task getTask() {
        return this.mTask;
    }

    AnimateableViewBounds getViewBounds() {
        return this.mViewBounds;
    }

    protected void onFinishInflate() {
        this.mHeaderView = (TaskViewHeader) findViewById(R.id.task_view_bar);
        this.mThumbnailView = (TaskViewThumbnail) findViewById(R.id.task_view_thumbnail);
        this.mThumbnailView.updateClipToTaskBar(this.mHeaderView);
        this.mActionButtonView = findViewById(R.id.lock_to_app_fab);
        this.mActionButtonView.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, TaskView.this.mActionButtonView.getWidth(), TaskView.this.mActionButtonView.getHeight());
                outline.setAlpha(0.35f);
            }
        });
        this.mActionButtonView.setOnClickListener(this);
        this.mActionButtonTranslationZ = this.mActionButtonView.getTranslationZ();
    }

    void onConfigurationChanged() {
        this.mHeaderView.onConfigurationChanged();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            this.mHeaderView.onTaskViewSizeChanged(w, h);
            this.mThumbnailView.onTaskViewSizeChanged(w, h);
            this.mActionButtonView.setTranslationX((float) (w - getMeasuredWidth()));
            this.mActionButtonView.setTranslationY((float) (h - getMeasuredHeight()));
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mDownTouchPos.set((int) (ev.getX() * getScaleX()), (int) (ev.getY() * getScaleY()));
        }
        return super.onInterceptTouchEvent(ev);
    }

    protected void measureContents(int width, int height) {
        measureChildren(MeasureSpec.makeMeasureSpec((width - this.mPaddingLeft) - this.mPaddingRight, 1073741824), MeasureSpec.makeMeasureSpec((height - this.mPaddingTop) - this.mPaddingBottom, 1073741824));
        setMeasuredDimension(width, height);
    }

    void updateViewPropertiesToTaskTransform(TaskViewTransform toTransform, AnimationProps toAnimation, AnimatorUpdateListener updateCallback) {
        boolean z;
        RecentsConfiguration config = Recents.getConfiguration();
        cancelTransformAnimation();
        this.mTmpAnimators.clear();
        ArrayList arrayList = this.mTmpAnimators;
        if (config.fakeShadows) {
            z = false;
        } else {
            z = true;
        }
        toTransform.applyToTaskView(this, arrayList, toAnimation, z);
        if (toAnimation.isImmediate()) {
            if (Float.compare(getDimAlpha(), toTransform.dimAlpha) != 0) {
                setDimAlpha(toTransform.dimAlpha);
            }
            if (Float.compare(this.mViewBounds.getAlpha(), toTransform.viewOutlineAlpha) != 0) {
                this.mViewBounds.setAlpha(toTransform.viewOutlineAlpha);
            }
            if (toAnimation.getListener() != null) {
                toAnimation.getListener().onAnimationEnd(null);
            }
            if (updateCallback != null) {
                updateCallback.onAnimationUpdate(null);
                return;
            }
            return;
        }
        if (Float.compare(getDimAlpha(), toTransform.dimAlpha) != 0) {
            this.mDimAnimator = ObjectAnimator.ofFloat(this, DIM_ALPHA, new float[]{getDimAlpha(), toTransform.dimAlpha});
            this.mTmpAnimators.add(toAnimation.apply(6, this.mDimAnimator));
        }
        if (Float.compare(this.mViewBounds.getAlpha(), toTransform.viewOutlineAlpha) != 0) {
            this.mOutlineAnimator = ObjectAnimator.ofFloat(this, VIEW_OUTLINE_ALPHA, new float[]{this.mViewBounds.getAlpha(), toTransform.viewOutlineAlpha});
            this.mTmpAnimators.add(toAnimation.apply(6, this.mOutlineAnimator));
        }
        if (updateCallback != null) {
            ValueAnimator updateCallbackAnim = ValueAnimator.ofInt(new int[]{0, 1});
            updateCallbackAnim.addUpdateListener(updateCallback);
            this.mTmpAnimators.add(toAnimation.apply(6, updateCallbackAnim));
        }
        this.mTransformAnimation = toAnimation.createAnimator(this.mTmpAnimators);
        this.mTransformAnimation.start();
        this.mTargetAnimationTransform.copyFrom(toTransform);
    }

    void resetViewProperties() {
        cancelTransformAnimation();
        setDimAlpha(0.0f);
        setVisibility(0);
        getViewBounds().reset();
        getHeaderView().reset();
        TaskViewTransform.reset(this);
        this.mActionButtonView.setScaleX(1.0f);
        this.mActionButtonView.setScaleY(1.0f);
        this.mActionButtonView.setAlpha(0.0f);
        this.mActionButtonView.setTranslationX(0.0f);
        this.mActionButtonView.setTranslationY(0.0f);
        this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
        if (this.mIncompatibleAppToastView != null) {
            this.mIncompatibleAppToastView.setVisibility(4);
        }
    }

    boolean isAnimatingTo(TaskViewTransform transform) {
        if (this.mTransformAnimation == null || !this.mTransformAnimation.isStarted()) {
            return false;
        }
        return this.mTargetAnimationTransform.isSame(transform);
    }

    public void cancelTransformAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mTransformAnimation);
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        Utilities.cancelAnimationWithoutCallbacks(this.mOutlineAnimator);
    }

    void setTouchEnabled(boolean enabled) {
        setOnClickListener(enabled ? this : null);
    }

    void startNoUserInteractionAnimation() {
        this.mHeaderView.startNoUserInteractionAnimation();
    }

    void setNoUserInteractionState() {
        this.mHeaderView.setNoUserInteractionState();
    }

    void resetNoUserInteractionState() {
        this.mHeaderView.resetNoUserInteractionState();
    }

    void dismissTask() {
        TaskView tv = this;
        DismissTaskViewEvent dismissEvent = new DismissTaskViewEvent(this);
        dismissEvent.addPostAnimationCallback(new Runnable() {
            public void run() {
                EventBus.getDefault().send(new TaskViewDismissedEvent(TaskView.this.mTask, this, new AnimationProps(200, Interpolators.FAST_OUT_SLOW_IN)));
            }
        });
        EventBus.getDefault().send(dismissEvent);
    }

    boolean shouldClipViewInStack() {
        if (this.mTask.isFreeformTask() || getVisibility() != 0) {
            return false;
        }
        return this.mClipViewInStack;
    }

    void setClipViewInStack(boolean clip) {
        if (clip != this.mClipViewInStack) {
            this.mClipViewInStack = clip;
            if (this.mCb != null) {
                this.mCb.onTaskViewClipStateChanged(this);
            }
        }
    }

    public TaskViewHeader getHeaderView() {
        return this.mHeaderView;
    }

    public void setDimAlpha(float dimAlpha) {
        this.mDimAlpha = dimAlpha;
        this.mThumbnailView.setDimAlpha(dimAlpha);
        this.mHeaderView.setDimAlpha(dimAlpha);
    }

    public void setDimAlphaWithoutHeader(float dimAlpha) {
        this.mDimAlpha = dimAlpha;
        this.mThumbnailView.setDimAlpha(dimAlpha);
    }

    public float getDimAlpha() {
        return this.mDimAlpha;
    }

    public void setFocusedState(boolean isFocused, boolean requestViewFocus) {
        if (isFocused) {
            if (requestViewFocus && !isFocused()) {
                requestFocus();
            }
        } else if (isAccessibilityFocused() && this.mTouchExplorationEnabled) {
            clearAccessibilityFocus();
        }
    }

    public void showActionButton(boolean fadeIn, int fadeInDuration) {
        this.mActionButtonView.setVisibility(0);
        if (!fadeIn || this.mActionButtonView.getAlpha() >= 1.0f) {
            this.mActionButtonView.setScaleX(1.0f);
            this.mActionButtonView.setScaleY(1.0f);
            this.mActionButtonView.setAlpha(1.0f);
            this.mActionButtonView.setTranslationZ(this.mActionButtonTranslationZ);
            return;
        }
        this.mActionButtonView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration((long) fadeInDuration).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public void hideActionButton(boolean fadeOut, int fadeOutDuration, boolean scaleDown, final AnimatorListener animListener) {
        if (!fadeOut || this.mActionButtonView.getAlpha() <= 0.0f) {
            this.mActionButtonView.setAlpha(0.0f);
            this.mActionButtonView.setVisibility(4);
            if (animListener != null) {
                animListener.onAnimationEnd(null);
                return;
            }
            return;
        }
        if (scaleDown) {
            this.mActionButtonView.animate().scaleX(0.9f).scaleY(0.9f);
        }
        this.mActionButtonView.animate().alpha(0.0f).setDuration((long) fadeOutDuration).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
            public void run() {
                if (animListener != null) {
                    animListener.onAnimationEnd(null);
                }
                TaskView.this.mActionButtonView.setVisibility(4);
            }
        }).start();
    }

    public void onPrepareLaunchTargetForEnterAnimation() {
        setDimAlphaWithoutHeader(0.0f);
        this.mActionButtonView.setAlpha(0.0f);
        if (this.mIncompatibleAppToastView != null && this.mIncompatibleAppToastView.getVisibility() == 0) {
            this.mIncompatibleAppToastView.setAlpha(0.0f);
        }
    }

    public void onStartLaunchTargetEnterAnimation(TaskViewTransform transform, int duration, boolean screenPinningEnabled, ReferenceCountedTrigger postAnimationTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        postAnimationTrigger.increment();
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(duration, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA_WITHOUT_HEADER, new float[]{getDimAlpha(), transform.dimAlpha}));
        this.mDimAnimator.addListener(postAnimationTrigger.decrementOnAnimationEnd());
        this.mDimAnimator.start();
        if (screenPinningEnabled) {
            showActionButton(true, duration);
        }
        if (this.mIncompatibleAppToastView != null && this.mIncompatibleAppToastView.getVisibility() == 0) {
            this.mIncompatibleAppToastView.animate().alpha(1.0f).setDuration((long) duration).setInterpolator(Interpolators.ALPHA_IN).start();
        }
    }

    public void onStartLaunchTargetLaunchAnimation(int duration, boolean screenPinningRequested, ReferenceCountedTrigger postAnimationTrigger) {
        Utilities.cancelAnimationWithoutCallbacks(this.mDimAnimator);
        this.mDimAnimator = (ObjectAnimator) new AnimationProps(duration, Interpolators.ALPHA_OUT).apply(7, ObjectAnimator.ofFloat(this, DIM_ALPHA, new float[]{getDimAlpha(), 0.0f}));
        this.mDimAnimator.start();
        postAnimationTrigger.increment();
        hideActionButton(true, duration, !screenPinningRequested, postAnimationTrigger.decrementOnAnimationEnd());
    }

    public void onStartFrontTaskEnterAnimation(boolean screenPinningEnabled) {
        if (screenPinningEnabled) {
            showActionButton(false, 0);
        }
    }

    public void onTaskBound(Task t, boolean touchExplorationEnabled, int displayOrientation, Rect displayRect) {
        boolean z;
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mTouchExplorationEnabled = touchExplorationEnabled;
        this.mTask = t;
        this.mTask.addCallback(this);
        if (this.mTask.isSystemApp) {
            z = false;
        } else {
            z = ssp.isInSafeMode();
        }
        this.mIsDisabledInSafeMode = z;
        this.mThumbnailView.bindToTask(this.mTask, this.mIsDisabledInSafeMode, displayOrientation, displayRect);
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        if (!t.isDockable && ssp.hasDockedTask()) {
            if (this.mIncompatibleAppToastView == null) {
                this.mIncompatibleAppToastView = Utilities.findViewStubById((View) this, (int) R.id.incompatible_app_toast_stub).inflate();
                ((TextView) findViewById(16908299)).setText(R.string.recents_incompatible_app_message);
            }
            this.mIncompatibleAppToastView.setVisibility(0);
        } else if (this.mIncompatibleAppToastView != null) {
            this.mIncompatibleAppToastView.setVisibility(4);
        }
    }

    public void onTaskDataLoaded(Task task, TaskThumbnailInfo thumbnailInfo) {
        this.mThumbnailView.onTaskDataLoaded(thumbnailInfo);
        this.mHeaderView.onTaskDataLoaded();
    }

    public void onTaskDataUnloaded() {
        this.mTask.removeCallback(this);
        this.mThumbnailView.unbindFromTask();
        this.mHeaderView.unbindFromTask(this.mTouchExplorationEnabled);
    }

    public void onTaskStackIdChanged() {
        this.mHeaderView.bindToTask(this.mTask, this.mTouchExplorationEnabled, this.mIsDisabledInSafeMode);
        this.mHeaderView.onTaskDataLoaded();
    }

    public void onClick(View v) {
        boolean z;
        Context context = this.mContext;
        StringBuilder append = new StringBuilder().append("status:");
        if (WindowManagerProxy.getInstance().getDockSide() != -1) {
            z = true;
        } else {
            z = false;
        }
        BDReporter.e(context, 338, append.append(z).append(",pkg:").append(this.mTask.packageName).toString());
        if (this.mIsDisabledInSafeMode) {
            Context context2 = getContext();
            String msg = context2.getString(R.string.recents_launch_disabled_message, new Object[]{this.mTask.title});
            if (this.mDisabledAppToast != null) {
                this.mDisabledAppToast.cancel();
            }
            this.mDisabledAppToast = Toast.makeText(context2, msg, 0);
            this.mDisabledAppToast.show();
            return;
        }
        boolean screenPinningRequested = false;
        if (v == this.mActionButtonView) {
            this.mActionButtonView.setTranslationZ(0.0f);
            screenPinningRequested = true;
        }
        EventBus.getDefault().send(new LaunchTaskEvent(this, this.mTask, null, -1, screenPinningRequested));
        MetricsLogger.action(v.getContext(), 277, this.mTask.key.getComponent().toString());
        if (screenPinningRequested) {
            Intent intent = new Intent("com.huawei.android.systemui.screenpinning");
            intent.setPackage(getContext().getPackageName());
            intent.putExtra("screenpinning_state", true);
            getContext().sendBroadcast(intent);
        }
    }

    public boolean onLongClick(View v) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        Rect clipBounds = new Rect(this.mViewBounds.mClipBounds);
        clipBounds.scale(getScaleX());
        boolean inBounds = clipBounds.contains(this.mDownTouchPos.x, this.mDownTouchPos.y);
        if (v != this || !inBounds || ssp.hasDockedTask()) {
            return false;
        }
        setClipViewInStack(false);
        Point point = this.mDownTouchPos;
        point.x = (int) (((float) point.x) + (((1.0f - getScaleX()) * ((float) getWidth())) / 2.0f));
        point = this.mDownTouchPos;
        point.y = (int) (((float) point.y) + (((1.0f - getScaleY()) * ((float) getHeight())) / 2.0f));
        EventBus.getDefault().register(this, 3);
        EventBus.getDefault().send(new DragStartEvent(this.mTask, this, this.mDownTouchPos));
        return true;
    }

    public final void onBusEvent(DragEndEvent event) {
        if (!(event.dropTarget instanceof DockState)) {
            event.addPostAnimationCallback(new -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndEvent_event_LambdaImpl0());
        }
        EventBus.getDefault().unregister(this);
    }

    /* synthetic */ void -com_android_systemui_recents_views_TaskView_lambda$1() {
        setClipViewInStack(true);
    }

    public final void onBusEvent(DragEndCancelledEvent event) {
        event.addPostAnimationCallback(new -void_onBusEvent_com_android_systemui_recents_events_ui_dragndrop_DragEndCancelledEvent_event_LambdaImpl0());
    }

    /* synthetic */ void -com_android_systemui_recents_views_TaskView_lambda$2() {
        setClipViewInStack(true);
    }
}
