package com.huawei.gallery.report;

import com.android.gallery3d.util.BusinessRadar;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.GalleryLog;

public class TimeReporter {
    private static ThreadLocal<Data> sTime = new ThreadLocal();

    private static class Data {
        String msg;
        long time;

        private Data() {
        }
    }

    public static void start(String msg) {
        Data data = (Data) sTime.get();
        if (data == null) {
            data = new Data();
            sTime.set(data);
        }
        data.time = System.currentTimeMillis();
        data.msg = msg;
    }

    public static void end(long threshHold) {
        Data data = (Data) sTime.get();
        if (data == null) {
            GalleryLog.d("TimeReporter", "there is no data.");
            return;
        }
        long timeCost = System.currentTimeMillis() - data.time;
        if (timeCost >= threshHold) {
            BusinessRadar.report(BugType.JUST_PRINT, "run task:" + data.msg + ", cost time: " + timeCost);
        }
    }
}
