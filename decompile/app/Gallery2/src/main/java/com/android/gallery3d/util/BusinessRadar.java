package com.android.gallery3d.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class BusinessRadar {

    public enum BugType {
        JOB_TIME_OUT(907015001, 2220),
        DECODE_THUMB_FAILED_IMAGE(907015600, 2211),
        DECODE_THUMB_FAILED_VIDEO(907015601, 2212),
        CRASH_TOO_OFFEN(907015700, 2240),
        RELAOD_IN_UI_THREAD(907015701, 2230),
        REGION_DECODER_IS_NULL_FAILED(907015702, 2213),
        REGION_DECODER_DECODE_GET_NULL_FAILED(907015703, 2214),
        JUST_PRINT(3999, 2299);
        
        private int errorCode;
        private int sceneValue;

        private BugType(int ec, int sv) {
            this.errorCode = ec;
            this.sceneValue = sv;
        }
    }

    public static void report(BugType bugType, String msg) {
        reportInner(67, bugType, msg);
    }

    public static void reportFile(BugType bugType, String filePath, String msg, Throwable e) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            bugType = BugType.JUST_PRINT;
        }
        reportInner(67, filePath, bugType, msg + "\n" + toString(e));
    }

    public static void report(BugType bugType, String msg, Throwable e) {
        reportInner(67, bugType, msg + "\n" + toString(e));
    }

    public static void report(BugType bugType, Throwable e) {
        reportInner(67, bugType, toString(e));
    }

    private static void reportInner(int level, BugType bugType, String msg) {
        reportInner(level, null, bugType, msg);
    }

    private static void reportInner(int level, String filePath, BugType bugType, String msg) {
        GalleryLog.i("BusinessRadar", "report message ? " + msg);
        if (bugType == BugType.JUST_PRINT) {
            GalleryLog.w("BusinessRadar", "JUST print, see msg printed before !!!");
        } else if (isBugTypeDeprecated(bugType)) {
            GalleryLog.w("BusinessRadar", "Deprecated bug type " + bugType);
        } else {
            GalleryBusinessMonitorService.startActionReportEvent(GalleryUtils.getContext(), GalleryBusinessMonitorService.packageData(String.valueOf(bugType.sceneValue), filePath, msg), bugType.errorCode);
        }
    }

    private static boolean isBugTypeDeprecated(BugType bugType) {
        boolean z = false;
        try {
            if (bugType.getClass().getField(bugType.name()).getAnnotation(Deprecated.class) != null) {
                z = true;
            }
            return z;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    private static String toString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
