package com.android.systemui.recents.model;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task.TaskKey;
import com.android.systemui.recents.views.AnimationProps;
import com.android.systemui.recents.views.DropTarget;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskStack {
    private Comparator<Task> FREEFORM_COMPARATOR = new Comparator<Task>() {
        public int compare(Task o1, Task o2) {
            if (o1.isFreeformTask() && !o2.isFreeformTask()) {
                return 1;
            }
            if (!o2.isFreeformTask() || o1.isFreeformTask()) {
                return Long.compare((long) o1.temporarySortIndexInStack, (long) o2.temporarySortIndexInStack);
            }
            return -1;
        }
    };
    ArrayMap<Integer, TaskGrouping> mAffinitiesGroups = new ArrayMap();
    TaskStackCallbacks mCb;
    ArrayList<TaskGrouping> mGroups = new ArrayList();
    ArrayList<Task> mRawTaskList = new ArrayList();
    FilteredTaskList mStackTaskList = new FilteredTaskList();

    public static class DockState implements DropTarget {
        public static final DockState BOTTOM = new DockState(4, 1, 80, 0, 0, new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.875f, 1.0f, 1.0f), new RectF(0.0f, 0.5f, 1.0f, 1.0f));
        public static final DockState LEFT = new DockState(1, 0, 80, 0, 1, new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.125f, 1.0f), new RectF(0.0f, 0.0f, 0.5f, 1.0f));
        public static final DockState NONE = new DockState(-1, -1, 80, 255, 0, null, null, null);
        public static final DockState RIGHT = new DockState(3, 1, 80, 0, 1, new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.875f, 0.0f, 1.0f, 1.0f), new RectF(0.5f, 0.0f, 1.0f, 1.0f));
        public static final DockState TOP = new DockState(2, 0, 80, 0, 0, new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.125f), new RectF(0.0f, 0.0f, 1.0f, 0.5f));
        public final int createMode;
        private final RectF dockArea;
        public final int dockSide;
        private final RectF expandedTouchDockArea;
        private final RectF touchArea;
        public final ViewState viewState;

        public static class ViewState {
            private static final IntProperty<ViewState> HINT_ALPHA = new IntProperty<ViewState>("drawableAlpha") {
                public void setValue(ViewState object, int alpha) {
                    object.mHintTextAlpha = alpha;
                    object.dockAreaOverlay.invalidateSelf();
                }

                public Integer get(ViewState object) {
                    return Integer.valueOf(object.mHintTextAlpha);
                }
            };
            public final int dockAreaAlpha;
            public final ColorDrawable dockAreaOverlay;
            public final int hintTextAlpha;
            public final int hintTextOrientation;
            private AnimatorSet mDockAreaOverlayAnimator;
            private String mHintText;
            private int mHintTextAlpha;
            private Point mHintTextBounds;
            private Paint mHintTextPaint;
            private final int mHintTextResId;
            private Rect mTmpRect;

            private ViewState(int areaAlpha, int hintAlpha, int hintOrientation, int hintTextResId) {
                this.mHintTextBounds = new Point();
                this.mHintTextAlpha = 255;
                this.mTmpRect = new Rect();
                this.dockAreaAlpha = areaAlpha;
                this.dockAreaOverlay = new ColorDrawable(-1);
                this.dockAreaOverlay.setAlpha(0);
                this.hintTextAlpha = hintAlpha;
                this.hintTextOrientation = hintOrientation;
                this.mHintTextResId = hintTextResId;
                this.mHintTextPaint = new Paint(1);
                this.mHintTextPaint.setColor(-16777216);
            }

            public void update(Context context) {
                Resources res = context.getResources();
                this.mHintText = context.getString(this.mHintTextResId);
                this.mHintTextPaint.setTextSize((float) res.getDimensionPixelSize(R.dimen.recents_drag_hint_text_size));
                this.mHintTextPaint.getTextBounds(this.mHintText, 0, this.mHintText.length(), this.mTmpRect);
                this.mHintTextBounds.set((int) this.mHintTextPaint.measureText(this.mHintText), this.mTmpRect.height());
            }

            public void draw(Canvas canvas) {
                if (this.dockAreaOverlay.getAlpha() > 0) {
                    this.dockAreaOverlay.draw(canvas);
                }
                if (this.mHintTextAlpha > 0) {
                    Rect bounds = this.dockAreaOverlay.getBounds();
                    int x = bounds.left + ((bounds.width() - this.mHintTextBounds.x) / 2);
                    int y = bounds.top + ((bounds.height() + this.mHintTextBounds.y) / 2);
                    this.mHintTextPaint.setAlpha(this.mHintTextAlpha);
                    StaticLayout staticLayout;
                    if (this.hintTextOrientation == 1) {
                        canvas.save();
                        canvas.rotate(-90.0f, (float) bounds.centerX(), (float) bounds.centerY());
                        if (bounds.top + ((bounds.height() - this.mHintTextBounds.x) / 2) > 0) {
                            canvas.drawText(this.mHintText, (float) x, (float) y, this.mHintTextPaint);
                        } else {
                            staticLayout = new StaticLayout(this.mHintText, new TextPaint(this.mHintTextPaint), bounds.height(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                            canvas.translate((float) x, (float) (y - this.mHintTextBounds.y));
                            staticLayout.draw(canvas);
                        }
                        canvas.restore();
                    } else if (x > 0) {
                        canvas.drawText(this.mHintText, (float) x, (float) y, this.mHintTextPaint);
                    } else {
                        staticLayout = new StaticLayout(this.mHintText, new TextPaint(this.mHintTextPaint), bounds.width(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
                        canvas.translate(0.0f, ((float) y) / 2.0f);
                        staticLayout.draw(canvas);
                    }
                }
            }

            public void startAnimation(Rect bounds, int areaAlpha, int hintAlpha, int duration, Interpolator interpolator, boolean animateAlpha, boolean animateBounds) {
                if (this.mDockAreaOverlayAnimator != null) {
                    this.mDockAreaOverlayAnimator.cancel();
                }
                ArrayList<Animator> animators = new ArrayList();
                if (this.dockAreaOverlay.getAlpha() != areaAlpha) {
                    if (animateAlpha) {
                        ObjectAnimator anim = ObjectAnimator.ofInt(this.dockAreaOverlay, Utilities.DRAWABLE_ALPHA, new int[]{this.dockAreaOverlay.getAlpha(), areaAlpha});
                        anim.setDuration((long) duration);
                        anim.setInterpolator(interpolator);
                        animators.add(anim);
                    } else {
                        this.dockAreaOverlay.setAlpha(areaAlpha);
                    }
                }
                if (this.mHintTextAlpha != hintAlpha) {
                    if (animateAlpha) {
                        TimeInterpolator timeInterpolator;
                        anim = ObjectAnimator.ofInt(this, HINT_ALPHA, new int[]{this.mHintTextAlpha, hintAlpha});
                        anim.setDuration(150);
                        if (hintAlpha > this.mHintTextAlpha) {
                            timeInterpolator = Interpolators.ALPHA_IN;
                        } else {
                            timeInterpolator = Interpolators.ALPHA_OUT;
                        }
                        anim.setInterpolator(timeInterpolator);
                        animators.add(anim);
                    } else {
                        this.mHintTextAlpha = hintAlpha;
                        this.dockAreaOverlay.invalidateSelf();
                    }
                }
                if (!(bounds == null || this.dockAreaOverlay.getBounds().equals(bounds))) {
                    if (animateBounds) {
                        PropertyValuesHolder prop = PropertyValuesHolder.ofObject(Utilities.DRAWABLE_RECT, Utilities.RECT_EVALUATOR, new Rect[]{new Rect(this.dockAreaOverlay.getBounds()), bounds});
                        anim = ObjectAnimator.ofPropertyValuesHolder(this.dockAreaOverlay, new PropertyValuesHolder[]{prop});
                        anim.setDuration((long) duration);
                        anim.setInterpolator(interpolator);
                        animators.add(anim);
                    } else {
                        this.dockAreaOverlay.setBounds(bounds);
                    }
                }
                if (!animators.isEmpty()) {
                    this.mDockAreaOverlayAnimator = new AnimatorSet();
                    this.mDockAreaOverlayAnimator.playTogether(animators);
                    this.mDockAreaOverlayAnimator.start();
                }
            }
        }

        public boolean acceptsDrop(int x, int y, int width, int height, boolean isCurrentTarget) {
            if (isCurrentTarget) {
                return areaContainsPoint(this.expandedTouchDockArea, width, height, (float) x, (float) y);
            }
            return areaContainsPoint(this.touchArea, width, height, (float) x, (float) y);
        }

        DockState(int dockSide, int createMode, int dockAreaAlpha, int hintTextAlpha, int hintTextOrientation, RectF touchArea, RectF dockArea, RectF expandedTouchDockArea) {
            this.dockSide = dockSide;
            this.createMode = createMode;
            this.viewState = new ViewState(dockAreaAlpha, hintTextAlpha, hintTextOrientation, R.string.recents_drag_hint_message);
            this.dockArea = dockArea;
            this.touchArea = touchArea;
            this.expandedTouchDockArea = expandedTouchDockArea;
        }

        public void update(Context context) {
            this.viewState.update(context);
        }

        public boolean areaContainsPoint(RectF area, int width, int height, float x, float y) {
            int top = (int) (area.top * ((float) height));
            int right = (int) (area.right * ((float) width));
            int bottom = (int) (area.bottom * ((float) height));
            if (x < ((float) ((int) (area.left * ((float) width)))) || y < ((float) top) || x > ((float) right) || y > ((float) bottom)) {
                return false;
            }
            return true;
        }

        public Rect getPreDockedBounds(int width, int height) {
            return new Rect((int) (this.dockArea.left * ((float) width)), (int) (this.dockArea.top * ((float) height)), (int) (this.dockArea.right * ((float) width)), (int) (this.dockArea.bottom * ((float) height)));
        }

        public Rect getDockedBounds(int width, int height, int dividerSize, Rect insets, Resources res) {
            boolean isHorizontalDivision = true;
            if (res.getConfiguration().orientation != 1) {
                isHorizontalDivision = false;
            }
            int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision, insets, width, height, dividerSize);
            Rect newWindowBounds = new Rect();
            DockedDividerUtils.calculateBoundsForPosition(position, this.dockSide, newWindowBounds, width, height, dividerSize);
            return newWindowBounds;
        }

        public Rect getDockedTaskStackBounds(Rect displayRect, int width, int height, int dividerSize, Rect insets, TaskStackLayoutAlgorithm layoutAlgorithm, Resources res, Rect windowRectOut) {
            int top;
            DockedDividerUtils.calculateBoundsForPosition(DockedDividerUtils.calculateMiddlePosition(res.getConfiguration().orientation == 1, insets, width, height, dividerSize), DockedDividerUtils.invertDockSide(this.dockSide), windowRectOut, width, height, dividerSize);
            Rect taskStackBounds = new Rect();
            if (this.dockArea.bottom < 1.0f) {
                top = 0;
            } else {
                top = insets.top;
            }
            layoutAlgorithm.getTaskStackBounds(displayRect, windowRectOut, top, insets.right, taskStackBounds);
            return taskStackBounds;
        }
    }

    public interface TaskStackCallbacks {
        void onStackTaskAdded(TaskStack taskStack, Task task);

        void onStackTaskRemoved(TaskStack taskStack, Task task, Task task2, AnimationProps animationProps, boolean z);

        void onStackTasksRemoved(TaskStack taskStack);

        void onStackTasksUpdated(TaskStack taskStack);
    }

    public TaskStack() {
        this.mStackTaskList.setFilter(new TaskFilter() {
            public boolean acceptTask(SparseArray<Task> sparseArray, Task t, int index) {
                return t.isStackTask;
            }
        });
    }

    public void setCallbacks(TaskStackCallbacks cb) {
        this.mCb = cb;
    }

    public void moveTaskToStack(Task task, int newStackId) {
        ArrayList<Task> taskList = this.mStackTaskList.getTasks();
        int taskCount = taskList.size();
        if (!task.isFreeformTask() && newStackId == 2) {
            this.mStackTaskList.moveTaskToStack(task, taskCount, newStackId);
        } else if (task.isFreeformTask() && newStackId == 1) {
            int insertIndex = 0;
            for (int i = taskCount - 1; i >= 0; i--) {
                if (!((Task) taskList.get(i)).isFreeformTask()) {
                    insertIndex = i + 1;
                    break;
                }
            }
            this.mStackTaskList.moveTaskToStack(task, insertIndex, newStackId);
        }
    }

    void removeTaskImpl(FilteredTaskList taskList, Task t) {
        taskList.remove(t);
        TaskGrouping group = t.group;
        if (group != null) {
            group.removeTask(t);
            if (group.getTaskCount() == 0) {
                removeGroup(group);
            }
        }
    }

    public void removeTask(Task t, AnimationProps animation, boolean fromDockGesture) {
        if (this.mStackTaskList.contains(t)) {
            removeTaskImpl(this.mStackTaskList, t);
            Task newFrontMostTask = getStackFrontMostTask(false);
            if (this.mCb != null) {
                this.mCb.onStackTaskRemoved(this, t, newFrontMostTask, animation, fromDockGesture);
            }
        }
        this.mRawTaskList.remove(t);
    }

    public void removeAllTasks() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task t = (Task) tasks.get(i);
            removeTaskImpl(this.mStackTaskList, t);
            this.mRawTaskList.remove(t);
        }
        if (this.mCb != null) {
            this.mCb.onStackTasksRemoved(this);
        }
    }

    public void setTasks(Context context, List<Task> tasks, boolean notifyStackChanges) {
        int i;
        ArrayMap<TaskKey, Task> currentTasksMap = createTaskKeyMapFromList(this.mRawTaskList);
        ArrayMap<TaskKey, Task> newTasksMap = createTaskKeyMapFromList(tasks);
        ArrayList<Task> addedTasks = new ArrayList();
        ArrayList<Task> removedTasks = new ArrayList();
        ArrayList<Task> allTasks = new ArrayList();
        if (this.mCb == null) {
            notifyStackChanges = false;
        }
        for (i = this.mRawTaskList.size() - 1; i >= 0; i--) {
            Task task = (Task) this.mRawTaskList.get(i);
            if (!newTasksMap.containsKey(task.key) && notifyStackChanges) {
                removedTasks.add(task);
            }
            task.setGroup(null);
        }
        int taskCount = tasks.size();
        for (i = 0; i < taskCount; i++) {
            Task newTask = (Task) tasks.get(i);
            Task currentTask = (Task) currentTasksMap.get(newTask.key);
            if (currentTask == null && notifyStackChanges) {
                addedTasks.add(newTask);
            } else if (currentTask != null) {
                currentTask.copyFrom(newTask);
                newTask = currentTask;
            }
            allTasks.add(newTask);
        }
        for (i = allTasks.size() - 1; i >= 0; i--) {
            ((Task) allTasks.get(i)).temporarySortIndexInStack = i;
        }
        Collections.sort(allTasks, this.FREEFORM_COMPARATOR);
        this.mStackTaskList.set(allTasks);
        this.mRawTaskList = allTasks;
        createAffiliatedGroupings(context);
        int removedTaskCount = removedTasks.size();
        Task newFrontMostTask = getStackFrontMostTask(false);
        for (i = 0; i < removedTaskCount; i++) {
            this.mCb.onStackTaskRemoved(this, (Task) removedTasks.get(i), newFrontMostTask, AnimationProps.IMMEDIATE, false);
        }
        int addedTaskCount = addedTasks.size();
        for (i = 0; i < addedTaskCount; i++) {
            this.mCb.onStackTaskAdded(this, (Task) addedTasks.get(i));
        }
        if (notifyStackChanges) {
            this.mCb.onStackTasksUpdated(this);
        }
    }

    public Task getStackFrontMostTask(boolean includeFreeformTasks) {
        ArrayList<Task> stackTasks = this.mStackTaskList.getTasks();
        if (stackTasks.isEmpty()) {
            return null;
        }
        for (int i = stackTasks.size() - 1; i >= 0; i--) {
            Task task = (Task) stackTasks.get(i);
            if (!task.isFreeformTask() || includeFreeformTasks) {
                return task;
            }
        }
        return null;
    }

    public ArrayList<TaskKey> getTaskKeys() {
        ArrayList<TaskKey> taskKeys = new ArrayList();
        ArrayList<Task> tasks = computeAllTasksList();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            taskKeys.add(((Task) tasks.get(i)).key);
        }
        return taskKeys;
    }

    public ArrayList<Task> getStackTasks() {
        return this.mStackTaskList.getTasks();
    }

    public ArrayList<Task> getFreeformTasks() {
        ArrayList<Task> freeformTasks = new ArrayList();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = (Task) tasks.get(i);
            if (task.isFreeformTask()) {
                freeformTasks.add(task);
            }
        }
        return freeformTasks;
    }

    public ArrayList<Task> computeAllTasksList() {
        ArrayList<Task> tasks = new ArrayList();
        tasks.addAll(this.mStackTaskList.getTasks());
        return tasks;
    }

    public int getTaskCount() {
        return this.mStackTaskList.size();
    }

    public int getStackTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int stackCount = 0;
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            if (!((Task) tasks.get(i)).isFreeformTask()) {
                stackCount++;
            }
        }
        return stackCount;
    }

    public int getFreeformTaskCount() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int freeformCount = 0;
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            if (((Task) tasks.get(i)).isFreeformTask()) {
                freeformCount++;
            }
        }
        return freeformCount;
    }

    public Task getLaunchTarget() {
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = (Task) tasks.get(i);
            if (task.isLaunchTarget) {
                return task;
            }
        }
        return null;
    }

    public int indexOfStackTask(Task t) {
        return this.mStackTaskList.indexOf(t);
    }

    public Task findTaskWithId(int taskId) {
        ArrayList<Task> tasks = computeAllTasksList();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = (Task) tasks.get(i);
            if (task.key.id == taskId) {
                return task;
            }
        }
        return null;
    }

    public void addGroup(TaskGrouping group) {
        this.mGroups.add(group);
        this.mAffinitiesGroups.put(Integer.valueOf(group.affiliation), group);
    }

    public void removeGroup(TaskGrouping group) {
        this.mGroups.remove(group);
        this.mAffinitiesGroups.remove(Integer.valueOf(group.affiliation));
    }

    void createAffiliatedGroupings(Context context) {
        int i;
        this.mGroups.clear();
        this.mAffinitiesGroups.clear();
        ArrayMap<TaskKey, Task> tasksMap = new ArrayMap();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (i = 0; i < taskCount; i++) {
            Task t = (Task) tasks.get(i);
            TaskGrouping group = new TaskGrouping(t.key.id);
            addGroup(group);
            group.addTask(t);
            tasksMap.put(t.key, t);
        }
        float minAlpha = context.getResources().getFloat(R.dimen.recents_task_affiliation_color_min_alpha_percentage);
        int taskGroupCount = this.mGroups.size();
        for (i = 0; i < taskGroupCount; i++) {
            group = (TaskGrouping) this.mGroups.get(i);
            taskCount = group.getTaskCount();
            if (taskCount > 1) {
                int affiliationColor = ((Task) tasksMap.get(group.mTaskKeys.get(0))).affiliationColor;
                float alphaStep = (1.0f - minAlpha) / ((float) taskCount);
                float alpha = 1.0f;
                for (int j = 0; j < taskCount; j++) {
                    ((Task) tasksMap.get(group.mTaskKeys.get(j))).colorPrimary = Utilities.getColorWithOverlay(affiliationColor, -1, alpha);
                    alpha -= alphaStep;
                }
            }
        }
    }

    public ArraySet<ComponentName> computeComponentsRemoved(String packageName, int userId) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        ArraySet<ComponentName> existingComponents = new ArraySet();
        ArraySet<ComponentName> removedComponents = new ArraySet();
        ArrayList<TaskKey> taskKeys = getTaskKeys();
        int taskKeyCount = taskKeys.size();
        for (int i = 0; i < taskKeyCount; i++) {
            TaskKey t = (TaskKey) taskKeys.get(i);
            if (t.userId == userId) {
                ComponentName cn = t.getComponent();
                if (cn.getPackageName().equals(packageName) && !existingComponents.contains(cn)) {
                    if (ssp.getActivityInfo(cn, userId) != null) {
                        existingComponents.add(cn);
                    } else {
                        removedComponents.add(cn);
                    }
                }
            }
        }
        return removedComponents;
    }

    public String toString() {
        String str = "Stack Tasks (" + this.mStackTaskList.size() + "):\n";
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            str = str + "    " + ((Task) tasks.get(i)).toString() + "\n";
        }
        return str;
    }

    private ArrayMap<TaskKey, Task> createTaskKeyMapFromList(List<Task> tasks) {
        ArrayMap<TaskKey, Task> map = new ArrayMap(tasks.size());
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = (Task) tasks.get(i);
            map.put(task.key, task);
        }
        return map;
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.print("TaskStack");
        writer.print(" numStackTasks=");
        writer.print(this.mStackTaskList.size());
        writer.println();
        ArrayList<Task> tasks = this.mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            ((Task) tasks.get(i)).dump(innerPrefix, writer);
        }
    }
}
