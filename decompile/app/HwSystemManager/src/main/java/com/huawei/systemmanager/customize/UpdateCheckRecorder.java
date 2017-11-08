package com.huawei.systemmanager.customize;

import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;

/* compiled from: XmlUpdateChecker */
class UpdateCheckRecorder {
    private HashSet<String> mRecorder = new HashSet();

    UpdateCheckRecorder() {
    }

    public void record(String fileName) {
        HwLog.i("UpdateCheckRecorder", "Recorder, add " + fileName);
        synchronized (this.mRecorder) {
            this.mRecorder.add(fileName);
        }
    }

    public boolean isRecorded(String fileName) {
        boolean contains;
        synchronized (this.mRecorder) {
            contains = this.mRecorder.contains(fileName);
        }
        return contains;
    }
}
