package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack.DockState;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;

public class RecentsViewTouchHandler {
    private DividerSnapAlgorithm mDividerSnapAlgorithm;
    @ExportedProperty(category = "recents")
    private Point mDownPos = new Point();
    @ExportedProperty(category = "recents")
    private boolean mDragRequested;
    private float mDragSlop;
    @ExportedProperty(deepExport = true, prefix = "drag_task")
    private Task mDragTask;
    private ArrayList<DropTarget> mDropTargets = new ArrayList();
    @ExportedProperty(category = "recents")
    private boolean mIsDragging;
    private DropTarget mLastDropTarget;
    private RecentsView mRv;
    @ExportedProperty(deepExport = true, prefix = "drag_task_view_")
    private TaskView mTaskView;
    @ExportedProperty(category = "recents")
    private Point mTaskViewOffset = new Point();
    private ArrayList<DockState> mVisibleDockStates = new ArrayList();

    public RecentsViewTouchHandler(RecentsView rv) {
        this.mRv = rv;
        this.mDragSlop = (float) ViewConfiguration.get(rv.getContext()).getScaledTouchSlop();
        updateSnapAlgorithm();
    }

    private void updateSnapAlgorithm() {
        Rect insets = new Rect();
        SystemServicesProxy.getInstance(this.mRv.getContext()).getStableInsets(insets);
        this.mDividerSnapAlgorithm = DividerSnapAlgorithm.create(this.mRv.getContext(), insets);
    }

    public void registerDropTargetForCurrentDrag(DropTarget target) {
        this.mDropTargets.add(target);
    }

    public DockState[] getDockStatesForCurrentOrientation() {
        boolean isLandscape = this.mRv.getResources().getConfiguration().orientation == 2;
        RecentsConfiguration config = Recents.getConfiguration();
        return isLandscape ? config.isLargeScreen ? DockRegion.TABLET_LANDSCAPE : DockRegion.PHONE_LANDSCAPE : config.isLargeScreen ? DockRegion.TABLET_PORTRAIT : DockRegion.PHONE_PORTRAIT;
    }

    public ArrayList<DockState> getVisibleDockStates() {
        return this.mVisibleDockStates;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return this.mDragRequested;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        handleTouchEvent(ev);
        return this.mDragRequested;
    }

    public final void onBusEvent(DragStartEvent event) {
        int i = 0;
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mRv.getParent().requestDisallowInterceptTouchEvent(true);
        this.mDragRequested = true;
        this.mIsDragging = false;
        this.mDragTask = event.task;
        this.mTaskView = event.taskView;
        this.mDropTargets.clear();
        int[] recentsViewLocation = new int[2];
        this.mRv.getLocationInWindow(recentsViewLocation);
        this.mTaskViewOffset.set((this.mTaskView.getLeft() - recentsViewLocation[0]) + event.tlOffset.x, (this.mTaskView.getTop() - recentsViewLocation[1]) + event.tlOffset.y);
        float y = (float) (this.mDownPos.y - this.mTaskViewOffset.y);
        this.mTaskView.setTranslationX((float) (this.mDownPos.x - this.mTaskViewOffset.x));
        this.mTaskView.setTranslationY(y);
        this.mVisibleDockStates.clear();
        if (ActivityManager.supportsMultiWindow() && !ssp.hasDockedTask() && this.mDividerSnapAlgorithm.isSplitScreenFeasible()) {
            Recents.logDockAttempt(this.mRv.getContext(), event.task.getTopComponent(), event.task.resizeMode);
            if (event.task.isDockable) {
                BDReporter.e(this.mRv.getContext(), 336, "status : true");
                DockState[] dockStates = getDockStatesForCurrentOrientation();
                int length = dockStates.length;
                while (i < length) {
                    DockState dockState = dockStates[i];
                    registerDropTargetForCurrentDrag(dockState);
                    dockState.update(this.mRv.getContext());
                    this.mVisibleDockStates.add(dockState);
                    i++;
                }
            } else {
                BDReporter.e(this.mRv.getContext(), 336, "status : false");
                EventBus.getDefault().send(new ShowIncompatibleAppOverlayEvent());
            }
        }
        EventBus.getDefault().send(new DragStartInitializeDropTargetsEvent(event.task, event.taskView, this));
    }

    public final void onBusEvent(DragEndEvent event) {
        if (!this.mDragTask.isDockable) {
            EventBus.getDefault().send(new HideIncompatibleAppOverlayEvent());
        }
        this.mDragRequested = false;
        this.mDragTask = null;
        this.mTaskView = null;
        this.mLastDropTarget = null;
    }

    public final void onBusEvent(ConfigurationChangedEvent event) {
        if (event.fromDisplayDensityChange || event.fromDeviceOrientationChange) {
            updateSnapAlgorithm();
        }
    }

    private void handleTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case 0:
                this.mDownPos.set((int) ev.getX(), (int) ev.getY());
                return;
            case 1:
            case 3:
                if (this.mDragRequested) {
                    boolean cancelled = action == 3;
                    if (cancelled) {
                        EventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, null));
                    }
                    EventBus.getDefault().send(new DragEndEvent(this.mDragTask, this.mTaskView, !cancelled ? this.mLastDropTarget : null));
                    return;
                }
                return;
            case 2:
                float evX = ev.getX();
                float evY = ev.getY();
                float x = evX - ((float) this.mTaskViewOffset.x);
                float y = evY - ((float) this.mTaskViewOffset.y);
                if (this.mDragRequested) {
                    if (!this.mIsDragging) {
                        this.mIsDragging = Math.hypot((double) (evX - ((float) this.mDownPos.x)), (double) (evY - ((float) this.mDownPos.y))) > ((double) this.mDragSlop);
                    }
                    if (this.mIsDragging) {
                        int width = this.mRv.getMeasuredWidth();
                        int height = this.mRv.getMeasuredHeight();
                        DropTarget currentDropTarget = null;
                        if (this.mLastDropTarget != null && this.mLastDropTarget.acceptsDrop((int) evX, (int) evY, width, height, true)) {
                            currentDropTarget = this.mLastDropTarget;
                        }
                        if (currentDropTarget == null) {
                            for (DropTarget target : this.mDropTargets) {
                                if (target.acceptsDrop((int) evX, (int) evY, width, height, false)) {
                                    currentDropTarget = target;
                                }
                            }
                        }
                        if (this.mLastDropTarget != currentDropTarget) {
                            this.mLastDropTarget = currentDropTarget;
                            EventBus.getDefault().send(new DragDropTargetChangedEvent(this.mDragTask, currentDropTarget));
                        }
                    }
                    this.mTaskView.setTranslationX(x);
                    this.mTaskView.setTranslationY(y);
                    return;
                }
                return;
            default:
                return;
        }
    }
}
