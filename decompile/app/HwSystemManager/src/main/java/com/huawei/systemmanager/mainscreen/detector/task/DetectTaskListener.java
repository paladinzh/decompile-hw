package com.huawei.systemmanager.mainscreen.detector.task;

import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;

public interface DetectTaskListener {

    public static class SimpleDetectTaskListener implements DetectTaskListener {
        public void onStart(DetectTask task) {
        }

        public void onItemFount(DetectTask task, DetectItem item) {
        }

        public void onProgressChange(DetectTask task, String itemName, float progress) {
        }

        public void onItemScoreChange(int socre) {
        }

        public void onTaskFinish(DetectTask task) {
        }
    }

    void onItemFount(DetectTask detectTask, DetectItem detectItem);

    void onItemScoreChange(int i);

    void onProgressChange(DetectTask detectTask, String str, float f);

    void onStart(DetectTask detectTask);

    void onTaskFinish(DetectTask detectTask);
}
