package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.RectF;
import android.util.ArrayMap;
import com.android.systemui.R;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.Task.TaskKey;
import java.util.Collections;
import java.util.List;

public class FreeformWorkspaceLayoutAlgorithm {
    private int mTaskPadding;
    private ArrayMap<TaskKey, RectF> mTaskRectMap = new ArrayMap();

    public FreeformWorkspaceLayoutAlgorithm(Context context) {
        reloadOnConfigurationChange(context);
    }

    public void reloadOnConfigurationChange(Context context) {
        this.mTaskPadding = context.getResources().getDimensionPixelSize(R.dimen.recents_freeform_layout_task_padding) / 2;
    }

    public void update(List<Task> freeformTasks, TaskStackLayoutAlgorithm stackLayout) {
        Collections.reverse(freeformTasks);
        this.mTaskRectMap.clear();
        int numFreeformTasks = stackLayout.mNumFreeformTasks;
        if (!freeformTasks.isEmpty()) {
            int i;
            Task task;
            float width;
            int workspaceWidth = stackLayout.mFreeformRect.width();
            int workspaceHeight = stackLayout.mFreeformRect.height();
            float normalizedWorkspaceWidth = ((float) workspaceWidth) / ((float) workspaceHeight);
            float[] normalizedTaskWidths = new float[numFreeformTasks];
            for (i = 0; i < numFreeformTasks; i++) {
                float rowTaskWidth;
                task = (Task) freeformTasks.get(i);
                if (task.bounds != null) {
                    rowTaskWidth = ((float) task.bounds.width()) / ((float) task.bounds.height());
                } else {
                    rowTaskWidth = normalizedWorkspaceWidth;
                }
                normalizedTaskWidths[i] = Math.min(rowTaskWidth, normalizedWorkspaceWidth);
            }
            float rowScale = 0.85f;
            float rowWidth = 0.0f;
            float maxRowWidth = 0.0f;
            int rowCount = 1;
            i = 0;
            while (i < numFreeformTasks) {
                width = normalizedTaskWidths[i] * rowScale;
                if (rowWidth + width <= normalizedWorkspaceWidth) {
                    rowWidth += width;
                    i++;
                } else if (((float) (rowCount + 1)) * rowScale > 1.0f) {
                    rowScale = Math.min(normalizedWorkspaceWidth / (rowWidth + width), 1.0f / ((float) (rowCount + 1)));
                    rowCount = 1;
                    rowWidth = 0.0f;
                    i = 0;
                } else {
                    rowWidth = width;
                    rowCount++;
                    i++;
                }
                maxRowWidth = Math.max(rowWidth, maxRowWidth);
            }
            float defaultRowLeft = ((1.0f - (maxRowWidth / normalizedWorkspaceWidth)) * ((float) workspaceWidth)) / 2.0f;
            float rowLeft = defaultRowLeft;
            float rowTop = ((1.0f - (((float) rowCount) * rowScale)) * ((float) workspaceHeight)) / 2.0f;
            float rowHeight = rowScale * ((float) workspaceHeight);
            for (i = 0; i < numFreeformTasks; i++) {
                task = (Task) freeformTasks.get(i);
                width = rowHeight * normalizedTaskWidths[i];
                if (rowLeft + width > ((float) workspaceWidth)) {
                    rowTop += rowHeight;
                    rowLeft = defaultRowLeft;
                }
                RectF rect = new RectF(rowLeft, rowTop, rowLeft + width, rowTop + rowHeight);
                rect.inset((float) this.mTaskPadding, (float) this.mTaskPadding);
                rowLeft += width;
                this.mTaskRectMap.put(task.key, rect);
            }
        }
    }

    public boolean isTransformAvailable(Task task, TaskStackLayoutAlgorithm stackLayout) {
        if (stackLayout.mNumFreeformTasks == 0 || task == null) {
            return false;
        }
        return this.mTaskRectMap.containsKey(task.key);
    }

    public TaskViewTransform getTransform(Task task, TaskViewTransform transformOut, TaskStackLayoutAlgorithm stackLayout) {
        if (!this.mTaskRectMap.containsKey(task.key)) {
            return null;
        }
        RectF ffRect = (RectF) this.mTaskRectMap.get(task.key);
        transformOut.scale = 1.0f;
        transformOut.alpha = 1.0f;
        transformOut.translationZ = (float) stackLayout.mMaxTranslationZ;
        transformOut.dimAlpha = 0.0f;
        transformOut.viewOutlineAlpha = 2.0f;
        transformOut.rect.set(ffRect);
        transformOut.rect.offset((float) stackLayout.mFreeformRect.left, (float) stackLayout.mFreeformRect.top);
        transformOut.visible = true;
        return transformOut;
    }
}
