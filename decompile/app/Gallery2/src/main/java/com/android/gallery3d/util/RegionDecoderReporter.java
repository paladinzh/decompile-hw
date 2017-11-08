package com.android.gallery3d.util;

import com.android.gallery3d.util.BusinessRadar.BugType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RegionDecoderReporter {
    private static final /* synthetic */ int[] -com-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues = null;
    private static Map<FileIndex, FileReoprtRecord> sReportedFilePath = new HashMap(10);

    private static class FileIndex {
        public String filePath = "";
        public BugType type;

        public FileIndex(String path, BugType t) {
            if (path == null) {
                path = "";
            }
            this.filePath = path;
            this.type = t;
        }

        public int hashCode() {
            return ((this.filePath == null ? "" : this.filePath) + "+" + this.type).hashCode();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof FileIndex)) {
                return false;
            }
            FileIndex fileIndex = (FileIndex) o;
            if (this.filePath != null && this.filePath.equalsIgnoreCase(fileIndex.filePath) && this.type == fileIndex.type) {
                z = true;
            }
            return z;
        }
    }

    private static class FileReoprtRecord {
        private int errorCode;
        private String failReason = null;
        private int failTimes = 0;
        private String filePath = null;
        private long lastFailTime = -1;
        private boolean reported = false;

        FileReoprtRecord(int code, String path) {
            this.filePath = path;
            this.errorCode = code;
        }

        private synchronized void checkFail(BugType type) {
            if (this.lastFailTime == -1 || System.currentTimeMillis() - this.lastFailTime > 2000) {
                this.failTimes++;
                GalleryLog.d("RegionDecoderReporter", " record fail times : " + this.failTimes + ", filepath : " + this.filePath + ", reason : " + this.failReason);
            }
            this.lastFailTime = System.currentTimeMillis();
            String newReason = RegionDecoderReporter.checkFailReason(this.filePath, type, this.errorCode);
            if (this.failReason == null || !this.failReason.equals(newReason)) {
                GalleryLog.d("RegionDecoderReporter", " file changed force reset fail count to 1");
                this.failReason = newReason;
                this.failTimes = 1;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized void report(BugType type, Exception e) {
            if (this.reported) {
                GalleryLog.d("RegionDecoderReporter", " record has been reported: " + this.failReason);
                return;
            }
            checkFail(type);
            if (this.failTimes >= 1) {
                GalleryLog.d("RegionDecoderReporter", " report to radar. fail times : " + this.failTimes + ", reason : " + this.failReason);
                BusinessRadar.reportFile(type, this.filePath, "stack: " + ThumbnailReporter.getCallingStack() + ", reason: " + this.failReason, e);
                this.reported = true;
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues() {
        if (-com-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues != null) {
            return -com-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues;
        }
        int[] iArr = new int[BugType.values().length];
        try {
            iArr[BugType.CRASH_TOO_OFFEN.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[BugType.DECODE_THUMB_FAILED_IMAGE.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[BugType.DECODE_THUMB_FAILED_VIDEO.ordinal()] = 5;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[BugType.JOB_TIME_OUT.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[BugType.JUST_PRINT.ordinal()] = 7;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[BugType.REGION_DECODER_DECODE_GET_NULL_FAILED.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[BugType.REGION_DECODER_IS_NULL_FAILED.ordinal()] = 2;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[BugType.RELAOD_IN_UI_THREAD.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        -com-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues = iArr;
        return iArr;
    }

    private RegionDecoderReporter() {
    }

    public static void reportRegionDecoderFail(BugType type, String filepath, Exception e) {
        reportRegionDecoderFail(type, -1, filepath, e);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void reportRegionDecoderFail(BugType type, int errorCode, String filepath, Exception e) {
        synchronized (RegionDecoderReporter.class) {
            if (filepath == null) {
                return;
            }
            String folder = new File(filepath).getParent();
            if (folder == null || !folder.endsWith(Constant.CAMERA_PATH)) {
            } else {
                FileIndex fileIndex = new FileIndex(filepath, type);
                FileReoprtRecord record = (FileReoprtRecord) sReportedFilePath.get(fileIndex);
                if (record == null) {
                    record = new FileReoprtRecord(errorCode, filepath);
                    sReportedFilePath.put(fileIndex, record);
                }
                record.report(type, e);
            }
        }
    }

    private static String checkFailReason(String filepath, BugType type, int errorCode) {
        if (filepath == null) {
            return "invalide file path. null";
        }
        StringBuffer str;
        File file = new File(filepath);
        switch (-getcom-android-gallery3d-util-BusinessRadar$BugTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                str = new StringBuffer("region decoder decode bitmap faild. path:").append(filepath).append(", error code:").append(errorCode).append(", record:").append(StorageMonitorUtil.checkFileStatus(GalleryUtils.getContext(), filepath).errMsg);
                break;
            case 2:
                str = new StringBuffer("get region decoder faild. path:").append(filepath).append(", record:").append(StorageMonitorUtil.checkFileStatus(GalleryUtils.getContext(), filepath).errMsg);
                break;
            default:
                str = new StringBuffer("unknow faild. path:").append(filepath);
                break;
        }
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
