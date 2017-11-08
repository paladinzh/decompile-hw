package com.android.systemui.recents.model;

import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.systemui.recents.model.Task.TaskKey;
import java.util.ArrayList;
import java.util.List;

/* compiled from: TaskStack */
class FilteredTaskList {
    TaskFilter mFilter;
    ArrayList<Task> mFilteredTasks = new ArrayList();
    ArrayMap<TaskKey, Integer> mTaskIndices = new ArrayMap();
    ArrayList<Task> mTasks = new ArrayList();

    FilteredTaskList() {
    }

    boolean setFilter(TaskFilter filter) {
        ArrayList<Task> prevFilteredTasks = new ArrayList(this.mFilteredTasks);
        this.mFilter = filter;
        updateFilteredTasks();
        if (prevFilteredTasks.equals(this.mFilteredTasks)) {
            return false;
        }
        return true;
    }

    public void moveTaskToStack(Task task, int insertIndex, int newStackId) {
        int taskIndex = indexOf(task);
        if (taskIndex != insertIndex) {
            this.mTasks.remove(taskIndex);
            if (taskIndex < insertIndex) {
                insertIndex--;
            }
            this.mTasks.add(insertIndex, task);
        }
        task.setStackId(newStackId);
        updateFilteredTasks();
    }

    void set(List<Task> tasks) {
        this.mTasks.clear();
        this.mTasks.addAll(tasks);
        updateFilteredTasks();
    }

    boolean remove(Task t) {
        if (!this.mFilteredTasks.contains(t)) {
            return false;
        }
        boolean removed = this.mTasks.remove(t);
        updateFilteredTasks();
        return removed;
    }

    int indexOf(Task t) {
        if (t == null || !this.mTaskIndices.containsKey(t.key)) {
            return -1;
        }
        return ((Integer) this.mTaskIndices.get(t.key)).intValue();
    }

    int size() {
        return this.mFilteredTasks.size();
    }

    boolean contains(Task t) {
        return this.mTaskIndices.containsKey(t.key);
    }

    private void updateFilteredTasks() {
        this.mFilteredTasks.clear();
        if (this.mFilter != null) {
            int i;
            Task t;
            SparseArray<Task> taskIdMap = new SparseArray();
            int taskCount = this.mTasks.size();
            for (i = 0; i < taskCount; i++) {
                t = (Task) this.mTasks.get(i);
                taskIdMap.put(t.key.id, t);
            }
            for (i = 0; i < taskCount; i++) {
                t = (Task) this.mTasks.get(i);
                if (this.mFilter.acceptTask(taskIdMap, t, i)) {
                    this.mFilteredTasks.add(t);
                }
            }
        } else {
            this.mFilteredTasks.addAll(this.mTasks);
        }
        updateFilteredTaskIndices();
    }

    private void updateFilteredTaskIndices() {
        int taskCount = this.mFilteredTasks.size();
        this.mTaskIndices.clear();
        for (int i = 0; i < taskCount; i++) {
            this.mTaskIndices.put(((Task) this.mFilteredTasks.get(i)).key, Integer.valueOf(i));
        }
    }

    ArrayList<Task> getTasks() {
        return this.mFilteredTasks;
    }
}
