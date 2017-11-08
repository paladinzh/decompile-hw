package com.android.gallery3d.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import com.android.gallery3d.util.BusinessRadar.BugType;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ThumbnailReporter {
    private static Map<String, FileRecord> mReportedFilePath = new HashMap(10);
    private static String sCallingStack = null;

    private static class FileRecord {
        private int errorCode;
        private String failReason = null;
        private int failTimes = 0;
        private String filePath = null;
        private long lastFailTime = -1;
        private boolean reported = false;

        FileRecord(int code, String path) {
            this.filePath = path;
            this.errorCode = code;
        }

        private synchronized void checkFail() {
            if (this.lastFailTime == -1 || System.currentTimeMillis() - this.lastFailTime > 2000) {
                this.failTimes++;
                GalleryLog.d("ThumbnailReporter", " record fail times : " + this.failTimes + ", filepath : " + this.filePath + ", reason : " + this.failReason);
            }
            this.lastFailTime = System.currentTimeMillis();
            String newReason = ThumbnailReporter.checkFailReason(this.filePath, this.errorCode);
            if (this.failReason == null || !this.failReason.equals(newReason)) {
                GalleryLog.d("ThumbnailReporter", " file changed force reset fail count to 1");
                this.failReason = newReason;
                this.failTimes = 1;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized void report(BugType type, Exception e) {
            if (this.reported) {
                GalleryLog.d("ThumbnailReporter", " record has been reported: " + this.failReason);
                return;
            }
            checkFail();
            if (this.failTimes >= 1) {
                GalleryLog.d("ThumbnailReporter", " report to radar. fail times : " + this.failTimes + ", reason : " + this.failReason);
                BusinessRadar.reportFile(type, this.filePath, "stack: " + ThumbnailReporter.sCallingStack + ", reason: " + this.failReason, e);
                this.reported = true;
            }
        }
    }

    private ThumbnailReporter() {
    }

    public static void updateCallingStack(Activity activity) {
        List<RunningTaskInfo> runningTasks = ((ActivityManager) activity.getSystemService("activity")).getRunningTasks(2);
        sCallingStack = "";
        if (runningTasks != null) {
            for (RunningTaskInfo task : runningTasks) {
                sCallingStack += "[baseActivity: " + task.baseActivity + ", " + task.topActivity + "] ";
            }
            GalleryLog.d("ThumbnailReporter", "update calling stack info: " + sCallingStack);
        }
    }

    public static String getCallingStack() {
        return sCallingStack;
    }

    public static void reportThumbnailFail(BugType type, String filepath, Exception e) {
        reportThumbnailFail(type, -1, filepath, e);
    }

    public static void reportThumbnailFail(BugType type, int errorCode, String filepath, Exception e) {
        if (filepath != null && new File(filepath).getParent() != null) {
            FileRecord record = (FileRecord) mReportedFilePath.get(filepath);
            if (record == null) {
                record = new FileRecord(errorCode, filepath);
                mReportedFilePath.put(filepath, record);
            }
            record.report(type, e);
        }
    }

    private static String checkFailReason(String filepath, int errorCode) {
        if (filepath == null) {
            return "invalide file path. null";
        }
        File file = new File(filepath);
        StringBuffer str = new StringBuffer("decode thumbnail faild. path:").append(filepath).append(", error code:").append(errorCode);
        if (!file.exists()) {
            str.append(" file not exist. ");
        } else if (file.length() == 0) {
            str.append(" file is empty. ");
        } else {
            str.append(" file size is ");
            str.append(file.length());
        }
        return str.toString();
    }
}
