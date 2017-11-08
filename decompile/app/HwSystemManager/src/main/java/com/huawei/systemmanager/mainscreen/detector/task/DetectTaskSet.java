package com.huawei.systemmanager.mainscreen.detector.task;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskListener.SimpleDetectTaskListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class DetectTaskSet extends DetectTask {
    private static final String TAG = "DetectTaskSet";
    private final ArrayList<DetectTask> mAllDetectTasks;
    private DetectTaskListener mListener = new SimpleDetectTaskListener() {
        public void onStart(DetectTask task) {
        }

        public void onItemFount(DetectTask task, DetectItem item) {
            DetectTaskSet.this.publishItemFount(item);
        }

        public void onProgressChange(DetectTask task, String itemName, float progress) {
            DetectTaskSet.this.publishProgressChange(itemName, progress);
        }

        public void onTaskFinish(DetectTask task) {
            HwLog.i(DetectTaskSet.TAG, "onTaskFinish, taskName:" + task.getTaskName());
            boolean allTaskEnd = true;
            for (DetectTask t : DetectTaskSet.this.mAllDetectTasks) {
                if (!t.isFinish()) {
                    allTaskEnd = false;
                    break;
                }
            }
            if (allTaskEnd) {
                DetectTaskSet.this.publishTaskFinish();
            }
        }
    };
    private List<DetectTaskInfo> mTaskInfo = Lists.newArrayList();

    private static class DetectTaskInfo {
        float weight;

        private DetectTaskInfo() {
        }
    }

    public DetectTaskSet(Context context, List<DetectTask> tasks) {
        super(context);
        this.mAllDetectTasks = Lists.newArrayList((Iterable) tasks);
    }

    public String getTaskName() {
        return TAG;
    }

    public int getWeight() {
        int weight = 0;
        for (DetectTask task : this.mAllDetectTasks) {
            weight += task.getWeight();
        }
        return weight;
    }

    protected void doTask() {
        publishTaskStart();
        if (this.mAllDetectTasks.isEmpty()) {
            HwLog.w(TAG, "execute, but mScanTasks is empty!!");
            publishTaskFinish();
            return;
        }
        prepareTasks();
        for (DetectTask task : this.mAllDetectTasks) {
            task.execute();
        }
    }

    public void cancel() {
        super.cancel();
        for (DetectTask task : this.mAllDetectTasks) {
            task.cancel();
        }
    }

    public void destory() {
        for (DetectTask task : this.mAllDetectTasks) {
            task.destory();
        }
    }

    public float getProgress() {
        float totalProgress = 0.0f;
        for (DetectTask task : this.mAllDetectTasks) {
            totalProgress += ((DetectTaskInfo) task.getTag()).weight * task.getProgress();
        }
        return totalProgress;
    }

    private void prepareTasks() {
        int totalWeight = 0;
        for (DetectTask task : this.mAllDetectTasks) {
            totalWeight += task.getWeight();
        }
        if (totalWeight <= 0) {
            HwLog.e(TAG, "calacWeight error! Totalweight:" + totalWeight);
            totalWeight = 1;
        }
        for (DetectTask task2 : this.mAllDetectTasks) {
            DetectTaskInfo info = new DetectTaskInfo();
            info.weight = ((float) task2.getWeight()) / ((float) totalWeight);
            task2.setTag(info);
            this.mTaskInfo.add(info);
            task2.setListener(this.mListener);
            HwLog.e(TAG, "caculateWeight, task:" + task2.getTaskName() + ", weight:" + info.weight);
        }
    }
}
