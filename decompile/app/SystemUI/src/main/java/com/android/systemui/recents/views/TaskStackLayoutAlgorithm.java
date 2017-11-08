package com.android.systemui.recents.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewDebug.ExportedProperty;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.model.TaskStack;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TaskStackLayoutAlgorithm {
    TaskViewTransform mBackOfStackTransform = new TaskViewTransform();
    @ExportedProperty(category = "recents")
    private int mBaseBottomMargin;
    private int mBaseInitialBottomOffset;
    private int mBaseInitialTopOffset;
    @ExportedProperty(category = "recents")
    private int mBaseSideMargin;
    @ExportedProperty(category = "recents")
    private int mBaseTopMargin;
    private TaskStackLayoutAlgorithmCallbacks mCb;
    Context mContext;
    @ExportedProperty(category = "recents")
    private int mFocusState;
    @ExportedProperty(category = "recents")
    private int mFocusedBottomPeekHeight;
    private Path mFocusedCurve;
    private FreePathInterpolator mFocusedCurveInterpolator;
    private Path mFocusedDimCurve;
    private FreePathInterpolator mFocusedDimCurveInterpolator;
    private Range mFocusedRange;
    @ExportedProperty(category = "recents")
    private int mFocusedTopPeekHeight;
    FreeformWorkspaceLayoutAlgorithm mFreeformLayoutAlgorithm;
    @ExportedProperty(category = "recents")
    public Rect mFreeformRect = new Rect();
    @ExportedProperty(category = "recents")
    private int mFreeformStackGap;
    @ExportedProperty(category = "recents")
    float mFrontMostTaskP;
    TaskViewTransform mFrontOfStackTransform = new TaskViewTransform();
    @ExportedProperty(category = "recents")
    private int mInitialBottomOffset;
    @ExportedProperty(category = "recents")
    float mInitialScrollP;
    @ExportedProperty(category = "recents")
    private int mInitialTopOffset;
    @ExportedProperty(category = "recents")
    float mMaxScrollP;
    @ExportedProperty(category = "recents")
    int mMaxTranslationZ;
    private int mMinMargin;
    @ExportedProperty(category = "recents")
    float mMinScrollP;
    @ExportedProperty(category = "recents")
    int mMinTranslationZ;
    @ExportedProperty(category = "recents")
    int mNumFreeformTasks;
    @ExportedProperty(category = "recents")
    int mNumStackTasks;
    @ExportedProperty(category = "recents")
    public Rect mStackActionButtonRect = new Rect();
    @ExportedProperty(category = "recents")
    private int mStackBottomOffset;
    @ExportedProperty(category = "recents")
    public Rect mStackRect = new Rect();
    private StackState mState = StackState.SPLIT;
    @ExportedProperty(category = "recents")
    public Rect mSystemInsets = new Rect();
    private SparseIntArray mTaskIndexMap = new SparseIntArray();
    private SparseArray<Float> mTaskIndexOverrideMap = new SparseArray();
    @ExportedProperty(category = "recents")
    public Rect mTaskRect = new Rect();
    private Path mUnfocusedCurve;
    private FreePathInterpolator mUnfocusedCurveInterpolator;
    private Path mUnfocusedDimCurve;
    private FreePathInterpolator mUnfocusedDimCurveInterpolator;
    private Range mUnfocusedRange;

    public static class StackState {
        public static final StackState FREEFORM_ONLY = new StackState(1.0f, 255);
        public static final StackState SPLIT = new StackState(0.5f, 255);
        public static final StackState STACK_ONLY = new StackState(0.0f, 0);
        public final int freeformBackgroundAlpha;
        public final float freeformHeightPct;

        private StackState(float freeformHeightPct, int freeformBackgroundAlpha) {
            this.freeformHeightPct = freeformHeightPct;
            this.freeformBackgroundAlpha = freeformBackgroundAlpha;
        }

        public static StackState getStackStateForStack(TaskStack stack) {
            boolean hasFreeformWorkspaces = Recents.getSystemServices().hasFreeformWorkspaceSupport();
            int freeformCount = stack.getFreeformTaskCount();
            int stackCount = stack.getStackTaskCount();
            if (hasFreeformWorkspaces && stackCount > 0 && freeformCount > 0) {
                return SPLIT;
            }
            if (!hasFreeformWorkspaces || freeformCount <= 0) {
                return STACK_ONLY;
            }
            return FREEFORM_ONLY;
        }

        public void computeRects(Rect freeformRectOut, Rect stackRectOut, Rect taskStackBounds, int topMargin, int freeformGap, int stackBottomOffset) {
            int ffPaddedHeight = (int) (((float) ((taskStackBounds.height() - topMargin) - stackBottomOffset)) * this.freeformHeightPct);
            freeformRectOut.set(taskStackBounds.left, taskStackBounds.top + topMargin, taskStackBounds.right, (taskStackBounds.top + topMargin) + Math.max(0, ffPaddedHeight - freeformGap));
            stackRectOut.set(taskStackBounds.left, taskStackBounds.top, taskStackBounds.right, taskStackBounds.bottom);
            if (ffPaddedHeight > 0) {
                stackRectOut.top += ffPaddedHeight;
            } else {
                stackRectOut.top += topMargin;
            }
        }
    }

    public interface TaskStackLayoutAlgorithmCallbacks {
        void onFocusStateChanged(int i, int i2);
    }

    public class VisibilityReport {
        public int numVisibleTasks;
        public int numVisibleThumbnails;

        VisibilityReport(int tasks, int thumbnails) {
            this.numVisibleTasks = tasks;
            this.numVisibleThumbnails = thumbnails;
        }
    }

    public TaskStackLayoutAlgorithm(Context context, TaskStackLayoutAlgorithmCallbacks cb) {
        Resources res = context.getResources();
        this.mContext = context;
        this.mCb = cb;
        this.mFreeformLayoutAlgorithm = new FreeformWorkspaceLayoutAlgorithm(context);
        this.mMinMargin = res.getDimensionPixelSize(R.dimen.recents_layout_min_margin);
        this.mBaseTopMargin = getDimensionForDevice(context, R.dimen.recents_layout_top_margin_phone, R.dimen.recents_layout_top_margin_tablet, R.dimen.recents_layout_top_margin_tablet_xlarge);
        this.mBaseSideMargin = getDimensionForDevice(context, R.dimen.recents_layout_side_margin_phone, R.dimen.recents_layout_side_margin_tablet, R.dimen.recents_layout_side_margin_tablet_xlarge);
        this.mBaseBottomMargin = res.getDimensionPixelSize(R.dimen.recents_layout_bottom_margin);
        this.mFreeformStackGap = res.getDimensionPixelSize(R.dimen.recents_freeform_layout_bottom_margin);
        reloadOnConfigurationChange(context);
    }

    public void reloadOnConfigurationChange(Context context) {
        Resources res = context.getResources();
        this.mFocusedRange = new Range(res.getFloat(R.integer.recents_layout_focused_range_min), res.getFloat(R.integer.recents_layout_focused_range_max));
        this.mUnfocusedRange = new Range(res.getFloat(R.integer.recents_layout_unfocused_range_min), res.getFloat(R.integer.recents_layout_unfocused_range_max));
        this.mFocusState = getInitialFocusState();
        this.mFocusedTopPeekHeight = res.getDimensionPixelSize(R.dimen.recents_layout_top_peek_size);
        this.mFocusedBottomPeekHeight = res.getDimensionPixelSize(R.dimen.recents_layout_bottom_peek_size);
        this.mMinTranslationZ = res.getDimensionPixelSize(R.dimen.recents_layout_z_min);
        this.mMaxTranslationZ = res.getDimensionPixelSize(R.dimen.recents_layout_z_max);
        this.mBaseInitialTopOffset = getDimensionForDevice(context, R.dimen.recents_layout_initial_top_offset_phone_port, R.dimen.recents_layout_initial_top_offset_phone_land, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet, R.dimen.recents_layout_initial_top_offset_tablet);
        this.mBaseInitialBottomOffset = getDimensionForDevice(context, R.dimen.recents_layout_initial_bottom_offset_phone_port, R.dimen.recents_layout_initial_bottom_offset_phone_land, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet, R.dimen.recents_layout_initial_bottom_offset_tablet);
        this.mFreeformLayoutAlgorithm.reloadOnConfigurationChange(context);
    }

    public void reset() {
        this.mTaskIndexOverrideMap.clear();
        setFocusState(getInitialFocusState());
    }

    public boolean setSystemInsets(Rect systemInsets) {
        boolean changed = !this.mSystemInsets.equals(systemInsets);
        this.mSystemInsets.set(systemInsets);
        return changed;
    }

    public void setFocusState(int focusState) {
        int prevFocusState = this.mFocusState;
        this.mFocusState = focusState;
        updateFrontBackTransforms();
        if (this.mCb != null) {
            this.mCb.onFocusStateChanged(prevFocusState, focusState);
        }
    }

    public int getFocusState() {
        return this.mFocusState;
    }

    public void initialize(Rect displayRect, Rect windowRect, Rect taskStackBounds, StackState state) {
        Rect lastStackRect = new Rect(this.mStackRect);
        int topMargin = getScaleForExtent(windowRect, displayRect, this.mBaseTopMargin, this.mMinMargin, 1);
        int bottomMargin = getScaleForExtent(windowRect, displayRect, this.mBaseBottomMargin, this.mMinMargin, 1);
        this.mInitialTopOffset = getScaleForExtent(windowRect, displayRect, this.mBaseInitialTopOffset, this.mMinMargin, 1);
        this.mInitialBottomOffset = this.mBaseInitialBottomOffset;
        this.mState = state;
        this.mStackBottomOffset = this.mSystemInsets.bottom + bottomMargin;
        state.computeRects(this.mFreeformRect, this.mStackRect, taskStackBounds, topMargin, this.mFreeformStackGap, this.mStackBottomOffset);
        this.mStackActionButtonRect.set(this.mStackRect.left, this.mStackRect.top - topMargin, this.mStackRect.right, this.mStackRect.top + this.mFocusedTopPeekHeight);
        this.mTaskRect.set(this.mStackRect.left, this.mStackRect.top, this.mStackRect.right, this.mStackRect.top + ((this.mStackRect.height() - this.mInitialTopOffset) - this.mStackBottomOffset));
        if (!lastStackRect.equals(this.mStackRect)) {
            this.mUnfocusedCurve = constructUnfocusedCurve();
            this.mUnfocusedCurveInterpolator = new FreePathInterpolator(this.mUnfocusedCurve);
            this.mFocusedCurve = constructFocusedCurve();
            this.mFocusedCurveInterpolator = new FreePathInterpolator(this.mFocusedCurve);
            this.mUnfocusedDimCurve = constructUnfocusedDimCurve();
            this.mUnfocusedDimCurveInterpolator = new FreePathInterpolator(this.mUnfocusedDimCurve);
            this.mFocusedDimCurve = constructFocusedDimCurve();
            this.mFocusedDimCurveInterpolator = new FreePathInterpolator(this.mFocusedDimCurve);
            updateFrontBackTransforms();
        }
    }

    void update(TaskStack stack, ArraySet<TaskKey> ignoreTasksSet) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        this.mTaskIndexMap.clear();
        ArrayList<Task> tasks = stack.getStackTasks();
        if (tasks.isEmpty()) {
            this.mFrontMostTaskP = 0.0f;
            this.mInitialScrollP = 0.0f;
            this.mMaxScrollP = 0.0f;
            this.mMinScrollP = 0.0f;
            this.mNumFreeformTasks = 0;
            this.mNumStackTasks = 0;
            return;
        }
        int i;
        int launchTaskIndex;
        ArrayList<Task> freeformTasks = new ArrayList();
        ArrayList<Task> stackTasks = new ArrayList();
        for (i = 0; i < tasks.size(); i++) {
            Task task = (Task) tasks.get(i);
            if (!ignoreTasksSet.contains(task.key)) {
                if (task.isFreeformTask()) {
                    freeformTasks.add(task);
                } else {
                    stackTasks.add(task);
                }
            }
        }
        this.mNumStackTasks = stackTasks.size();
        this.mNumFreeformTasks = freeformTasks.size();
        int taskCount = stackTasks.size();
        for (i = 0; i < taskCount; i++) {
            task = (Task) stackTasks.get(i);
            this.mTaskIndexMap.put(task.key.id, i);
        }
        if (!freeformTasks.isEmpty()) {
            this.mFreeformLayoutAlgorithm.update(freeformTasks, this);
        }
        Task launchTask = stack.getLaunchTarget();
        if (launchTask != null) {
            launchTaskIndex = stack.indexOfStackTask(launchTask);
        } else {
            launchTaskIndex = this.mNumStackTasks - 1;
        }
        float maxBottomNormX;
        if (getInitialFocusState() == 1) {
            maxBottomNormX = getNormalizedXFromFocusedY((float) (this.mStackBottomOffset + this.mTaskRect.height()), 1);
            this.mFocusedRange.offset(0.0f);
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = Math.max(this.mMinScrollP, ((float) (this.mNumStackTasks - 1)) - Math.max(0.0f, this.mFocusedRange.getAbsoluteX(maxBottomNormX)));
            if (launchState.launchedFromHome) {
                this.mInitialScrollP = Utilities.clamp((float) launchTaskIndex, this.mMinScrollP, this.mMaxScrollP);
            } else {
                this.mInitialScrollP = Utilities.clamp((float) (launchTaskIndex - 1), this.mMinScrollP, this.mMaxScrollP);
            }
        } else if (ssp.hasFreeformWorkspaceSupport() || this.mNumStackTasks != 1) {
            boolean scrollToFront;
            maxBottomNormX = getNormalizedXFromUnfocusedY((float) (this.mStackBottomOffset + this.mTaskRect.height()), 1);
            this.mUnfocusedRange.offset(0.0f);
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = Math.max(this.mMinScrollP, ((float) (this.mNumStackTasks - 1)) - Math.max(0.0f, this.mUnfocusedRange.getAbsoluteX(maxBottomNormX)));
            if (launchState.launchedFromHome) {
                scrollToFront = true;
            } else {
                scrollToFront = launchState.launchedViaDockGesture;
            }
            if (launchState.launchedWithAltTab) {
                this.mInitialScrollP = Utilities.clamp((float) launchTaskIndex, this.mMinScrollP, this.mMaxScrollP);
            } else if (scrollToFront) {
                this.mInitialScrollP = Utilities.clamp((float) launchTaskIndex, this.mMinScrollP, this.mMaxScrollP);
            } else {
                this.mInitialScrollP = Math.max(this.mMinScrollP, Math.min(this.mMaxScrollP, (float) (this.mNumStackTasks - 2)) - Math.max(0.0f, this.mUnfocusedRange.getAbsoluteX(getNormalizedXFromUnfocusedY((float) this.mInitialTopOffset, 0))));
            }
        } else {
            this.mMinScrollP = 0.0f;
            this.mMaxScrollP = 0.0f;
            this.mInitialScrollP = 0.0f;
        }
    }

    public void setTaskOverridesForInitialState(TaskStack stack, boolean ignoreScrollToFront) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        this.mTaskIndexOverrideMap.clear();
        boolean scrollToFront;
        if (launchState.launchedFromHome) {
            scrollToFront = true;
        } else {
            scrollToFront = launchState.launchedViaDockGesture;
        }
        if (getInitialFocusState() == 0 && this.mNumStackTasks > 1) {
            if (ignoreScrollToFront || !(launchState.launchedWithAltTab || r7)) {
                float minBottomTaskNormX = getNormalizedXFromUnfocusedY((float) (this.mSystemInsets.bottom + this.mInitialBottomOffset), 1);
                float maxBottomTaskNormX = getNormalizedXFromUnfocusedY((float) ((this.mFocusedTopPeekHeight + this.mTaskRect.height()) - this.mMinMargin), 0);
                float[] initialNormX = this.mNumStackTasks <= 2 ? new float[]{Math.min(maxBottomTaskNormX, minBottomTaskNormX), getNormalizedXFromUnfocusedY((float) this.mFocusedTopPeekHeight, 0)} : new float[]{minBottomTaskNormX, getNormalizedXFromUnfocusedY((float) this.mInitialTopOffset, 0)};
                this.mUnfocusedRange.offset(0.0f);
                List<Task> tasks = stack.getStackTasks();
                int taskCount = tasks.size();
                int i = taskCount - 1;
                while (i >= 0) {
                    int indexFromFront = (taskCount - i) - 1;
                    if (indexFromFront < initialNormX.length) {
                        this.mTaskIndexOverrideMap.put(((Task) tasks.get(i)).key.id, Float.valueOf((this.mInitialScrollP + this.mUnfocusedRange.getAbsoluteX(initialNormX[indexFromFront])) * 0.9f));
                        i--;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public void addUnfocusedTaskOverride(Task task, float stackScroll) {
        if (this.mFocusState != 0) {
            this.mFocusedRange.offset(stackScroll);
            this.mUnfocusedRange.offset(stackScroll);
            float focusedRangeX = this.mFocusedRange.getNormalizedX((float) this.mTaskIndexMap.get(task.key.id));
            float unfocusedRangeX = this.mUnfocusedCurveInterpolator == null ? 0.0f : this.mUnfocusedCurveInterpolator.getX(this.mFocusedCurveInterpolator.getInterpolation(focusedRangeX));
            float unfocusedTaskProgress = stackScroll + this.mUnfocusedRange.getAbsoluteX(unfocusedRangeX);
            if (Float.compare(focusedRangeX, unfocusedRangeX) != 0) {
                this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(unfocusedTaskProgress));
            }
        }
    }

    public void addUnfocusedTaskOverride(TaskView taskView, float stackScroll) {
        this.mFocusedRange.offset(stackScroll);
        this.mUnfocusedRange.offset(stackScroll);
        Task task = taskView.getTask();
        int top = taskView.getTop() - this.mTaskRect.top;
        float focusedRangeX = getNormalizedXFromFocusedY((float) top, 0);
        float unfocusedRangeX = getNormalizedXFromUnfocusedY((float) top, 0);
        float unfocusedTaskProgress = stackScroll + this.mUnfocusedRange.getAbsoluteX(unfocusedRangeX);
        if (Float.compare(focusedRangeX, unfocusedRangeX) != 0) {
            this.mTaskIndexOverrideMap.put(task.key.id, Float.valueOf(unfocusedTaskProgress));
        }
    }

    public void clearUnfocusedTaskOverrides() {
        this.mTaskIndexOverrideMap.clear();
    }

    public float updateFocusStateOnScroll(float lastTargetStackScroll, float targetStackScroll, float lastStackScroll) {
        if (targetStackScroll == lastStackScroll) {
            return targetStackScroll;
        }
        float deltaScroll = targetStackScroll - lastStackScroll;
        float deltaTargetScroll = targetStackScroll - lastTargetStackScroll;
        float newScroll = targetStackScroll;
        this.mUnfocusedRange.offset(targetStackScroll);
        for (int i = this.mTaskIndexOverrideMap.size() - 1; i >= 0; i--) {
            int taskId = this.mTaskIndexOverrideMap.keyAt(i);
            float x = (float) this.mTaskIndexMap.get(taskId);
            float overrideX = ((Float) this.mTaskIndexOverrideMap.get(taskId, Float.valueOf(0.0f))).floatValue();
            float newOverrideX = overrideX + deltaScroll;
            if (isInvalidOverrideX(x, overrideX, newOverrideX)) {
                this.mTaskIndexOverrideMap.removeAt(i);
            } else if ((overrideX < x || deltaScroll > 0.0f) && (overrideX > x || deltaScroll < 0.0f)) {
                newScroll = lastStackScroll;
                newOverrideX = overrideX - deltaTargetScroll;
                if (isInvalidOverrideX(x, overrideX, newOverrideX)) {
                    this.mTaskIndexOverrideMap.removeAt(i);
                } else {
                    this.mTaskIndexOverrideMap.put(taskId, Float.valueOf(newOverrideX));
                }
            } else {
                this.mTaskIndexOverrideMap.put(taskId, Float.valueOf(newOverrideX));
            }
        }
        return newScroll;
    }

    private boolean isInvalidOverrideX(float x, float overrideX, float newOverrideX) {
        boolean outOfBounds;
        if (this.mUnfocusedRange.getNormalizedX(newOverrideX) < 0.0f) {
            outOfBounds = true;
        } else if (this.mUnfocusedRange.getNormalizedX(newOverrideX) > 1.0f) {
            outOfBounds = true;
        } else {
            outOfBounds = false;
        }
        if (outOfBounds) {
            return true;
        }
        if (overrideX >= x && x >= newOverrideX) {
            return true;
        }
        if (overrideX > x || x > newOverrideX) {
            return false;
        }
        return true;
    }

    public int getInitialFocusState() {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (Recents.getDebugFlags().isPagingEnabled() || launchState.launchedWithAltTab) {
            return 1;
        }
        return 0;
    }

    public TaskViewTransform getBackOfStackTransform() {
        return this.mBackOfStackTransform;
    }

    public TaskViewTransform getFrontOfStackTransform() {
        return this.mFrontOfStackTransform;
    }

    public StackState getStackState() {
        return this.mState;
    }

    public boolean isInitialized() {
        return !this.mStackRect.isEmpty();
    }

    public VisibilityReport computeStackVisibilityReport(ArrayList<Task> tasks) {
        if (tasks.size() <= 1) {
            return new VisibilityReport(1, 1);
        }
        if (this.mNumStackTasks == 0) {
            return new VisibilityReport(Math.max(this.mNumFreeformTasks, 1), Math.max(this.mNumFreeformTasks, 1));
        }
        TaskViewTransform tmpTransform = new TaskViewTransform();
        Range currentRange = ((float) getInitialFocusState()) > 0.0f ? this.mFocusedRange : this.mUnfocusedRange;
        currentRange.offset(this.mInitialScrollP);
        int taskBarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.recents_task_view_header_height);
        int numVisibleTasks = Math.max(this.mNumFreeformTasks, 1);
        int numVisibleThumbnails = Math.max(this.mNumFreeformTasks, 1);
        float prevScreenY = 2.14748365E9f;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = (Task) tasks.get(i);
            if (!task.isFreeformTask()) {
                float taskProgress = getStackScrollForTask(task);
                if (currentRange.isInRange(taskProgress)) {
                    boolean isFrontMostTaskInGroup = task.group != null ? task.group.isFrontMostTask(task) : true;
                    if (isFrontMostTaskInGroup) {
                        getStackTransform(taskProgress, taskProgress, this.mInitialScrollP, this.mFocusState, tmpTransform, null, false, false);
                        float screenY = tmpTransform.rect.top;
                        if (prevScreenY - screenY > ((float) taskBarHeight)) {
                            numVisibleThumbnails++;
                            numVisibleTasks++;
                            prevScreenY = screenY;
                        } else {
                            int j = i;
                            while (j >= 0) {
                                numVisibleTasks++;
                                j = !currentRange.isInRange(getStackScrollForTask((Task) tasks.get(j))) ? j - 1 : j - 1;
                            }
                            return new VisibilityReport(numVisibleTasks, numVisibleThumbnails);
                        }
                    } else if (!isFrontMostTaskInGroup) {
                        numVisibleTasks++;
                    }
                } else {
                    continue;
                }
            }
        }
        return new VisibilityReport(numVisibleTasks, numVisibleThumbnails);
    }

    public TaskViewTransform getStackTransform(Task task, float stackScroll, TaskViewTransform transformOut, TaskViewTransform frontTransform) {
        return getStackTransform(task, stackScroll, this.mFocusState, transformOut, frontTransform, false, false);
    }

    public TaskViewTransform getStackTransform(Task task, float stackScroll, TaskViewTransform transformOut, TaskViewTransform frontTransform, boolean ignoreTaskOverrides) {
        return getStackTransform(task, stackScroll, this.mFocusState, transformOut, frontTransform, false, ignoreTaskOverrides);
    }

    public TaskViewTransform getStackTransform(Task task, float stackScroll, int focusState, TaskViewTransform transformOut, TaskViewTransform frontTransform, boolean forceUpdate, boolean ignoreTaskOverrides) {
        if (this.mFreeformLayoutAlgorithm.isTransformAvailable(task, this)) {
            this.mFreeformLayoutAlgorithm.getTransform(task, transformOut, this);
            return transformOut;
        }
        int nonOverrideTaskProgress = this.mTaskIndexMap.get(task.key.id, -1);
        if (task == null || nonOverrideTaskProgress == -1) {
            transformOut.reset();
            return transformOut;
        }
        float taskProgress;
        if (ignoreTaskOverrides) {
            taskProgress = (float) nonOverrideTaskProgress;
        } else {
            taskProgress = getStackScrollForTask(task);
        }
        getStackTransform(taskProgress, (float) nonOverrideTaskProgress, stackScroll, focusState, transformOut, frontTransform, false, forceUpdate);
        return transformOut;
    }

    public TaskViewTransform getStackTransformScreenCoordinates(Task task, float stackScroll, TaskViewTransform transformOut, TaskViewTransform frontTransform, Rect windowOverrideRect) {
        return transformToScreenCoordinates(getStackTransform(task, stackScroll, this.mFocusState, transformOut, frontTransform, true, false), windowOverrideRect);
    }

    public TaskViewTransform transformToScreenCoordinates(TaskViewTransform transformOut, Rect windowOverrideRect) {
        Rect windowRect;
        if (windowOverrideRect != null) {
            windowRect = windowOverrideRect;
        } else {
            windowRect = Recents.getSystemServices().getWindowRect();
        }
        transformOut.rect.offset((float) windowRect.left, (float) windowRect.top);
        return transformOut;
    }

    public void getStackTransform(float taskProgress, float nonOverrideTaskProgress, float stackScroll, int focusState, TaskViewTransform transformOut, TaskViewTransform frontTransform, boolean ignoreSingleTaskCase, boolean forceUpdate) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mUnfocusedRange.offset(stackScroll);
        this.mFocusedRange.offset(stackScroll);
        boolean unfocusedVisible = this.mUnfocusedRange.isInRange(taskProgress);
        boolean focusedVisible = this.mFocusedRange.isInRange(taskProgress);
        if (forceUpdate || unfocusedVisible || focusedVisible) {
            int y;
            float z;
            float dimAlpha;
            float viewOutlineAlpha;
            this.mUnfocusedRange.offset(stackScroll);
            this.mFocusedRange.offset(stackScroll);
            float unfocusedRangeX = this.mUnfocusedRange.getNormalizedX(taskProgress);
            float focusedRangeX = this.mFocusedRange.getNormalizedX(taskProgress);
            float boundedStackScroll = Utilities.clamp(stackScroll, this.mMinScrollP, this.mMaxScrollP);
            this.mUnfocusedRange.offset(boundedStackScroll);
            this.mFocusedRange.offset(boundedStackScroll);
            float boundedScrollUnfocusedRangeX = this.mUnfocusedRange.getNormalizedX(taskProgress);
            float boundedScrollUnfocusedNonOverrideRangeX = this.mUnfocusedRange.getNormalizedX(nonOverrideTaskProgress);
            float lowerBoundedStackScroll = Utilities.clamp(stackScroll, -3.4028235E38f, this.mMaxScrollP);
            this.mUnfocusedRange.offset(lowerBoundedStackScroll);
            this.mFocusedRange.offset(lowerBoundedStackScroll);
            float lowerBoundedUnfocusedRangeX = this.mUnfocusedRange.getNormalizedX(taskProgress);
            float lowerBoundedFocusedRangeX = this.mFocusedRange.getNormalizedX(taskProgress);
            int x = (this.mStackRect.width() - this.mTaskRect.width()) / 2;
            if (ssp.hasFreeformWorkspaceSupport() || this.mNumStackTasks != 1 || ignoreSingleTaskCase) {
                int focusedY;
                int unfocusedY = (int) ((1.0f - (this.mUnfocusedCurveInterpolator == null ? 0.0f : this.mUnfocusedCurveInterpolator.getInterpolation(unfocusedRangeX))) * ((float) this.mStackRect.height()));
                if (this.mFocusedCurveInterpolator == null) {
                    focusedY = this.mStackRect.height();
                } else {
                    focusedY = (int) ((1.0f - this.mFocusedCurveInterpolator.getInterpolation(focusedRangeX)) * ((float) this.mStackRect.height()));
                }
                float unfocusedDim = this.mUnfocusedDimCurveInterpolator.getInterpolation(lowerBoundedUnfocusedRangeX);
                float focusedDim = this.mFocusedDimCurveInterpolator.getInterpolation(lowerBoundedFocusedRangeX);
                if (this.mNumStackTasks <= 2 && nonOverrideTaskProgress == 0.0f) {
                    if (boundedScrollUnfocusedRangeX >= 0.5f) {
                        unfocusedDim = 0.0f;
                    } else {
                        float offset = this.mUnfocusedDimCurveInterpolator.getInterpolation(0.5f);
                        unfocusedDim = (unfocusedDim - offset) * (0.25f / (0.25f - offset));
                    }
                }
                y = (this.mStackRect.top - this.mTaskRect.top) + ((int) Utilities.mapRange((float) focusState, (float) unfocusedY, (float) focusedY));
                z = Utilities.mapRange(Utilities.clamp01(boundedScrollUnfocusedNonOverrideRangeX), (float) this.mMinTranslationZ, (float) this.mMaxTranslationZ);
                dimAlpha = Utilities.mapRange((float) focusState, unfocusedDim, focusedDim);
                viewOutlineAlpha = Utilities.mapRange(Utilities.clamp01(boundedScrollUnfocusedRangeX), 0.0f, 2.0f);
            } else {
                y = ((this.mStackRect.top - this.mTaskRect.top) + (((this.mStackRect.height() - this.mSystemInsets.bottom) - this.mTaskRect.height()) / 2)) + getYForDeltaP((this.mMinScrollP - stackScroll) / ((float) this.mNumStackTasks), 0.0f);
                z = (float) this.mMaxTranslationZ;
                dimAlpha = 0.0f;
                viewOutlineAlpha = 1.0f;
            }
            transformOut.scale = 1.0f;
            transformOut.alpha = 1.0f;
            transformOut.translationZ = z;
            transformOut.dimAlpha = dimAlpha;
            transformOut.viewOutlineAlpha = viewOutlineAlpha;
            transformOut.rect.set(this.mTaskRect);
            transformOut.rect.offset((float) x, (float) y);
            Utilities.scaleRectAboutCenter(transformOut.rect, transformOut.scale);
            boolean z2 = transformOut.rect.top < ((float) this.mStackRect.bottom) ? frontTransform == null || transformOut.rect.top != frontTransform.rect.top : false;
            transformOut.visible = z2;
            return;
        }
        transformOut.reset();
    }

    public Rect getUntransformedTaskViewBounds() {
        return new Rect(this.mTaskRect);
    }

    float getStackScrollForTask(Task t) {
        Float overrideP = (Float) this.mTaskIndexOverrideMap.get(t.key.id, null);
        if (overrideP == null) {
            return (float) this.mTaskIndexMap.get(t.key.id, 0);
        }
        return overrideP.floatValue();
    }

    float getStackScrollForTaskIgnoreOverrides(Task t) {
        return (float) this.mTaskIndexMap.get(t.key.id, 0);
    }

    public float getDeltaPForY(int downY, int y) {
        return -((((float) (y - downY)) / ((float) this.mStackRect.height())) * (this.mUnfocusedCurveInterpolator == null ? 0.0f : this.mUnfocusedCurveInterpolator.getArcLength()));
    }

    public int getYForDeltaP(float downScrollP, float p) {
        float f;
        float height = ((float) this.mStackRect.height()) * (p - downScrollP);
        if (this.mUnfocusedCurveInterpolator == null) {
            f = 1.0f;
        } else {
            f = this.mUnfocusedCurveInterpolator.getArcLength();
        }
        return -((int) ((1.0f / f) * height));
    }

    public void getTaskStackBounds(Rect displayRect, Rect windowRect, int topInset, int rightInset, Rect taskStackBounds) {
        taskStackBounds.set(windowRect.left, windowRect.top + topInset, windowRect.right - rightInset, windowRect.bottom);
        int targetStackWidth = taskStackBounds.width() - (getScaleForExtent(windowRect, displayRect, this.mBaseSideMargin, this.mMinMargin, 0) * 2);
        if (Utilities.getAppConfiguration(this.mContext).orientation == 2) {
            Rect portraitDisplayRect = new Rect(0, 0, Math.min(displayRect.width(), displayRect.height()), Math.max(displayRect.width(), displayRect.height()));
            targetStackWidth = Math.min(targetStackWidth, portraitDisplayRect.width() - (getScaleForExtent(portraitDisplayRect, portraitDisplayRect, this.mBaseSideMargin, this.mMinMargin, 0) * 2));
        }
        taskStackBounds.inset((taskStackBounds.width() - targetStackWidth) / 2, 0);
    }

    public static int getDimensionForDevice(Context ctx, int phoneResId, int tabletResId, int xlargeTabletResId) {
        return getDimensionForDevice(ctx, phoneResId, phoneResId, tabletResId, tabletResId, xlargeTabletResId, xlargeTabletResId);
    }

    public static int getDimensionForDevice(Context ctx, int phonePortResId, int phoneLandResId, int tabletPortResId, int tabletLandResId, int xlargeTabletPortResId, int xlargeTabletLandResId) {
        RecentsConfiguration config = Recents.getConfiguration();
        Resources res = ctx.getResources();
        boolean isLandscape = Utilities.getAppConfiguration(ctx).orientation == 2;
        if (config.isXLargeScreen) {
            if (!isLandscape) {
                xlargeTabletLandResId = xlargeTabletPortResId;
            }
            return res.getDimensionPixelSize(xlargeTabletLandResId);
        } else if (config.isLargeScreen) {
            if (!isLandscape) {
                tabletLandResId = tabletPortResId;
            }
            return res.getDimensionPixelSize(tabletLandResId);
        } else {
            if (!isLandscape) {
                phoneLandResId = phonePortResId;
            }
            return res.getDimensionPixelSize(phoneLandResId);
        }
    }

    private float getNormalizedXFromUnfocusedY(float y, int fromSide) {
        float offset;
        if (fromSide == 0) {
            offset = ((float) this.mStackRect.height()) - y;
        } else {
            offset = y;
        }
        float offsetPct = offset / ((float) this.mStackRect.height());
        if (this.mUnfocusedCurveInterpolator == null) {
            return 0.0f;
        }
        return this.mUnfocusedCurveInterpolator.getX(offsetPct);
    }

    private float getNormalizedXFromFocusedY(float y, int fromSide) {
        float offset;
        if (fromSide == 0) {
            offset = ((float) this.mStackRect.height()) - y;
        } else {
            offset = y;
        }
        return this.mFocusedCurveInterpolator.getX(offset / ((float) this.mStackRect.height()));
    }

    private Path constructFocusedCurve() {
        float topPeekHeightPct = ((float) this.mFocusedTopPeekHeight) / ((float) this.mStackRect.height());
        float bottomPeekHeightPct = ((float) (this.mStackBottomOffset + this.mFocusedBottomPeekHeight)) / ((float) this.mStackRect.height());
        float minBottomPeekHeightPct = ((float) ((this.mFocusedTopPeekHeight + this.mTaskRect.height()) - this.mMinMargin)) / ((float) this.mStackRect.height());
        Path p = new Path();
        p.moveTo(0.0f, 1.0f);
        p.lineTo(0.5f, 1.0f - topPeekHeightPct);
        p.lineTo(1.0f - (0.5f / this.mFocusedRange.relativeMax), Math.max(1.0f - minBottomPeekHeightPct, bottomPeekHeightPct));
        p.lineTo(1.0f, 0.0f);
        return p;
    }

    private Path constructUnfocusedCurve() {
        float topPeekHeightPct = ((float) this.mFocusedTopPeekHeight) / ((float) this.mStackRect.height());
        float slope = ((1.0f - topPeekHeightPct) - 0.975f) / 0.099999994f;
        float cpoint2Y = (0.65f * slope) + (1.0f - (0.4f * slope));
        Path p = new Path();
        p.moveTo(0.0f, 1.0f);
        p.cubicTo(0.0f, 1.0f, 0.4f, 0.975f, 0.5f, 1.0f - topPeekHeightPct);
        p.cubicTo(0.5f, 1.0f - topPeekHeightPct, 0.65f, cpoint2Y, 1.0f, 0.0f);
        return p;
    }

    private Path constructFocusedDimCurve() {
        Path p = new Path();
        p.moveTo(0.0f, 0.25f);
        p.lineTo(0.5f, 0.0f);
        p.lineTo((0.5f / this.mFocusedRange.relativeMax) + 0.5f, 0.25f);
        p.lineTo(1.0f, 0.25f);
        return p;
    }

    private Path constructUnfocusedDimCurve() {
        float focusX = getNormalizedXFromUnfocusedY((float) this.mInitialTopOffset, 0);
        float cpoint2X = focusX + ((1.0f - focusX) / 2.0f);
        Path p = new Path();
        p.moveTo(0.0f, 0.25f);
        p.cubicTo(0.5f * focusX, 0.25f, 0.75f * focusX, 0.1875f, focusX, 0.0f);
        p.cubicTo(cpoint2X, 0.0f, cpoint2X, 0.15f, 1.0f, 0.15f);
        return p;
    }

    private int getScaleForExtent(Rect instance, Rect other, int value, int minValue, int extent) {
        if (extent == 0) {
            return Math.max(minValue, (int) (((float) value) * Utilities.clamp01(((float) instance.width()) / ((float) other.width()))));
        } else if (extent != 1) {
            return value;
        } else {
            return Math.max(minValue, (int) (((float) value) * Utilities.clamp01(((float) instance.height()) / ((float) other.height()))));
        }
    }

    private void updateFrontBackTransforms() {
        if (!this.mStackRect.isEmpty()) {
            float min = Utilities.mapRange((float) this.mFocusState, this.mUnfocusedRange.relativeMin, this.mFocusedRange.relativeMin);
            float max = Utilities.mapRange((float) this.mFocusState, this.mUnfocusedRange.relativeMax, this.mFocusedRange.relativeMax);
            getStackTransform(min, min, 0.0f, this.mFocusState, this.mBackOfStackTransform, null, true, true);
            getStackTransform(max, max, 0.0f, this.mFocusState, this.mFrontOfStackTransform, null, true, true);
            this.mBackOfStackTransform.visible = true;
            this.mFrontOfStackTransform.visible = true;
        }
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.print("TaskStackLayoutAlgorithm");
        writer.write(" numStackTasks=");
        writer.write(this.mNumStackTasks);
        writer.println();
        writer.print(innerPrefix);
        writer.print("insets=");
        writer.print(Utilities.dumpRect(this.mSystemInsets));
        writer.print(" stack=");
        writer.print(Utilities.dumpRect(this.mStackRect));
        writer.print(" task=");
        writer.print(Utilities.dumpRect(this.mTaskRect));
        writer.print(" freeform=");
        writer.print(Utilities.dumpRect(this.mFreeformRect));
        writer.print(" actionButton=");
        writer.print(Utilities.dumpRect(this.mStackActionButtonRect));
        writer.println();
        writer.print(innerPrefix);
        writer.print("minScroll=");
        writer.print(this.mMinScrollP);
        writer.print(" maxScroll=");
        writer.print(this.mMaxScrollP);
        writer.print(" initialScroll=");
        writer.print(this.mInitialScrollP);
        writer.println();
        writer.print(innerPrefix);
        writer.print("focusState=");
        writer.print(this.mFocusState);
        writer.println();
        if (this.mTaskIndexOverrideMap.size() > 0) {
            for (int i = this.mTaskIndexOverrideMap.size() - 1; i >= 0; i--) {
                int taskId = this.mTaskIndexOverrideMap.keyAt(i);
                float x = (float) this.mTaskIndexMap.get(taskId);
                float overrideX = ((Float) this.mTaskIndexOverrideMap.get(taskId, Float.valueOf(0.0f))).floatValue();
                writer.print(innerPrefix);
                writer.print("taskId= ");
                writer.print(taskId);
                writer.print(" x= ");
                writer.print(x);
                writer.print(" overrideX= ");
                writer.print(overrideX);
                writer.println();
            }
        }
    }
}
