package com.android.systemui.recents.model;

import android.util.ArrayMap;
import com.android.systemui.recents.model.Task.TaskKey;
import java.util.ArrayList;

public class TaskGrouping {
    int affiliation;
    long latestActiveTimeInGroup;
    TaskKey mFrontMostTaskKey;
    ArrayMap<TaskKey, Integer> mTaskKeyIndices = new ArrayMap();
    ArrayList<TaskKey> mTaskKeys = new ArrayList();

    public TaskGrouping(int affiliation) {
        this.affiliation = affiliation;
    }

    void addTask(Task t) {
        this.mTaskKeys.add(t.key);
        if (t.key.lastActiveTime > this.latestActiveTimeInGroup) {
            this.latestActiveTimeInGroup = t.key.lastActiveTime;
        }
        t.setGroup(this);
        updateTaskIndices();
    }

    void removeTask(Task t) {
        this.mTaskKeys.remove(t.key);
        this.latestActiveTimeInGroup = 0;
        int taskCount = this.mTaskKeys.size();
        for (int i = 0; i < taskCount; i++) {
            long lastActiveTime = ((TaskKey) this.mTaskKeys.get(i)).lastActiveTime;
            if (lastActiveTime > this.latestActiveTimeInGroup) {
                this.latestActiveTimeInGroup = lastActiveTime;
            }
        }
        t.setGroup(null);
        updateTaskIndices();
    }

    public TaskKey getNextTaskInGroup(Task t) {
        int i = indexOf(t);
        if (i + 1 < getTaskCount()) {
            return (TaskKey) this.mTaskKeys.get(i + 1);
        }
        return null;
    }

    public TaskKey getPrevTaskInGroup(Task t) {
        int i = indexOf(t);
        if (i - 1 >= 0) {
            return (TaskKey) this.mTaskKeys.get(i - 1);
        }
        return null;
    }

    public boolean isFrontMostTask(Task t) {
        return t.key == this.mFrontMostTaskKey;
    }

    public int indexOf(Task t) {
        return ((Integer) this.mTaskKeyIndices.get(t.key)).intValue();
    }

    public boolean isTaskAboveTask(Task t, Task below) {
        if (!this.mTaskKeyIndices.containsKey(t.key) || !this.mTaskKeyIndices.containsKey(below.key)) {
            return false;
        }
        if (((Integer) this.mTaskKeyIndices.get(t.key)).intValue() > ((Integer) this.mTaskKeyIndices.get(below.key)).intValue()) {
            return true;
        }
        return false;
    }

    public int getTaskCount() {
        return this.mTaskKeys.size();
    }

    private void updateTaskIndices() {
        if (this.mTaskKeys.isEmpty()) {
            this.mFrontMostTaskKey = null;
            this.mTaskKeyIndices.clear();
            return;
        }
        int taskCount = this.mTaskKeys.size();
        this.mFrontMostTaskKey = (TaskKey) this.mTaskKeys.get(this.mTaskKeys.size() - 1);
        this.mTaskKeyIndices.clear();
        for (int i = 0; i < taskCount; i++) {
            this.mTaskKeyIndices.put((TaskKey) this.mTaskKeys.get(i), Integer.valueOf(i));
        }
    }
}
