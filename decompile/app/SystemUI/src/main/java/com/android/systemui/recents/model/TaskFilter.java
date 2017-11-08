package com.android.systemui.recents.model;

import android.util.SparseArray;

/* compiled from: TaskStack */
interface TaskFilter {
    boolean acceptTask(SparseArray<Task> sparseArray, Task task, int i);
}
