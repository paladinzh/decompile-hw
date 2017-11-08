package com.huawei.systemmanager.spacecleanner.engine.base;

import android.content.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task.TaskListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CombineTask extends Task {
    private boolean mFlagCalledTaskStart;
    private TaskListener mListener = new TaskListener() {
        public void onTaskStart(Task task) {
            CombineTask.this.onSingleTaskStart(task);
        }

        public void onProgressUpdate(Task task, int progress, String info) {
            CombineTask.this.onSingleProgressUpdate(task, progress, info);
        }

        public void onItemUpdate(Task task, Trash updateItem) {
            CombineTask.this.onSingleItemUpdate(task, updateItem);
        }

        public void onTaskEnd(Task task, boolean canceled) {
            CombineTask.this.onSingleTaskEnd(task, canceled);
            CombineTask.this.executeNextTask();
        }
    };
    private final Object mScannerLock = new Object();
    private final String mScannerName;
    private final List<Task> mSequenceTasks = Lists.newArrayList();
    private List<TaskInfo> mTaskInfoList = Lists.newArrayList();
    protected final List<Task> mTasks;

    private static class TaskInfo {
        private final Task mTask;
        float weight;

        private TaskInfo(Task task) {
            this.mTask = task;
        }

        public float getAdjustProgress() {
            return ((float) this.mTask.getProgress()) * this.weight;
        }
    }

    public CombineTask(Context ctx, String name, Task... tasks) {
        super(ctx);
        this.mScannerName = name;
        List<Task> tempList = Lists.newArrayList();
        for (Task task : tasks) {
            if (task != null) {
                if (task instanceof CombineTask) {
                    tempList.addAll(((CombineTask) task).getTasks());
                } else {
                    tempList.add(task);
                }
            }
        }
        this.mTasks = Collections.unmodifiableList(tempList);
    }

    public CombineTask(Context ctx, String name, List<Task> listTask) {
        super(ctx);
        this.mScannerName = name;
        List<Task> tempList = Lists.newArrayList();
        for (Task task : listTask) {
            if (task != null) {
                if (task instanceof CombineTask) {
                    tempList.addAll(((CombineTask) task).getTasks());
                } else {
                    tempList.add(task);
                }
            }
        }
        this.mTasks = Collections.unmodifiableList(tempList);
    }

    public String getTaskName() {
        return this.mScannerName;
    }

    protected void startWork(ScanParams p) {
        caculateWeight();
        for (Task task : this.mTasks) {
            task.setListener(this.mListener);
        }
        if (p.useSequenceExecute()) {
            List<Task> allTask = Lists.newArrayList(this.mTasks);
            synchronized (this.mSequenceTasks) {
                this.mSequenceTasks.addAll(allTask);
            }
            executeNextTask();
            return;
        }
        for (Task task2 : this.mTasks) {
            task2.start(p);
        }
    }

    public void setExecutor(ExecutorService excutor) {
        super.setExecutor(excutor);
        for (Task task : this.mTasks) {
            task.setExecutor(excutor);
        }
    }

    public void cancel() {
        super.cancel();
        synchronized (this.mSequenceTasks) {
            this.mSequenceTasks.clear();
        }
        for (Task task : this.mTasks) {
            task.cancel();
        }
    }

    protected void caculateWeight() {
        float totalWeight = 0.0f;
        for (Task task : this.mTasks) {
            totalWeight += (float) task.getWeight();
        }
        if (Float.compare(totalWeight, 0.0f) <= 0) {
            HwLog.e(getTaskName(), "caculateWeight failed! totalWeight:" + totalWeight);
            totalWeight = Utility.ALPHA_MAX;
        }
        for (Task task2 : this.mTasks) {
            TaskInfo info = new TaskInfo(task2);
            float weight = ((float) task2.getWeight()) / totalWeight;
            info.weight = weight;
            task2.setTag(info);
            this.mTaskInfoList.add(info);
            HwLog.i(getTaskName(), "caculateWeight task:" + task2.getTaskName() + ", weight is:" + weight);
        }
    }

    public List<Task> getTasks() {
        return Lists.newArrayList(this.mTasks);
    }

    public int getType() {
        return 0;
    }

    protected void onSingleTaskStart(Task task) {
        synchronized (this.mScannerLock) {
            HwLog.i(getTaskName(), "onTaskStart called, task name:" + task.getTaskName());
            if (this.mFlagCalledTaskStart) {
                return;
            }
            this.mFlagCalledTaskStart = true;
            onPublishStart();
        }
    }

    protected void onSingleProgressUpdate(Task task, int progress, String info) {
        onPublishProgress(progress, info);
    }

    protected void onSingleItemUpdate(Task task, Trash item) {
        onPublishItemUpdate(item);
    }

    protected void onSingleTaskEnd(Task task, boolean canceled) {
        synchronized (this.mScannerLock) {
            HwLog.i(getTaskName(), "onTaskEnd called, task name:" + task.getTaskName() + ",canceled:" + canceled);
            if (canceled) {
                setCanceled(canceled);
            }
            if (checkTaskEnd(SpaceConst.SCANNER_TYPE_ALL)) {
                onPublishEnd();
            }
        }
    }

    protected final boolean checkTaskEnd(int type) {
        for (Task task : this.mTasks) {
            if (task.getType() <= type && !task.isEnd()) {
                return false;
            }
        }
        return true;
    }

    protected final boolean checkTaskCanceled(int type) {
        for (Task task : this.mTasks) {
            if (task.getType() <= type && task.isCanceled()) {
                return true;
            }
        }
        return false;
    }

    protected Task getTaskByTaskType(int taskType) {
        for (Task task : this.mTasks) {
            if (task.getType() == taskType) {
                return task;
            }
        }
        return null;
    }

    public int getProgress() {
        float progress = 0.0f;
        for (TaskInfo info : this.mTaskInfoList) {
            progress += info.getAdjustProgress();
        }
        return (int) progress;
    }

    public List<Integer> getSupportTrashType() {
        Iterable result = Sets.newHashSet();
        for (Task task : this.mTasks) {
            result.addAll(task.getSupportTrashType());
        }
        return Lists.newArrayList(result);
    }

    public boolean isNormal() {
        for (Task task : this.mTasks) {
            if (!task.isNormal()) {
                return false;
            }
        }
        return true;
    }

    private boolean executeNextTask() {
        ScanParams p = getParams();
        if (p == null) {
            HwLog.e(getTaskName(), "executeNextTask, ScanParams is null!");
            return false;
        } else if (!p.useSequenceExecute()) {
            return false;
        } else {
            synchronized (this.mSequenceTasks) {
                if (this.mSequenceTasks.isEmpty()) {
                    return false;
                }
                Task task = (Task) this.mSequenceTasks.remove(0);
                task.start(getParams());
                return true;
            }
        }
    }
}
