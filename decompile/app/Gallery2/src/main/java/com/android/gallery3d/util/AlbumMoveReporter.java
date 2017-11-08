package com.android.gallery3d.util;

import android.os.Handler;
import android.os.Looper;
import com.huawei.gallery.actionbar.Action;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumMoveReporter {
    private static Handler sHandler;
    private static long sLastReportTime = -1;
    private static Object sLock = new Object();
    private static JSONObject sReportedJsonObject = new JSONObject();
    private static JSONArray sReportedPathArray = new JSONArray();

    public static void reportAlbumMoveToBigData(Action action, String path) {
        if (GalleryUtils.IS_BETA_VERSION) {
            reportEvent(action, path);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void reportEvent(final Action action, String path) {
        synchronized (sLock) {
            sReportedPathArray.put(path);
            if (System.currentTimeMillis() - sLastReportTime < 500) {
                GalleryLog.d("AlbumMoveReporter", "Reported interval is too short, delay 500ms to report");
                if (sHandler == null) {
                    sHandler = new Handler(Looper.getMainLooper());
                    sHandler.postDelayed(new Runnable() {
                        public void run() {
                            synchronized (AlbumMoveReporter.sLock) {
                                if (AlbumMoveReporter.sReportedPathArray.length() > 0) {
                                    AlbumMoveReporter.doReport(action);
                                }
                            }
                        }
                    }, 500);
                }
            } else {
                doReport(action);
            }
        }
    }

    private static void doReport(Action action) {
        synchronized (sLock) {
            try {
                sReportedJsonObject.put("Action", action.toString());
                sReportedJsonObject.put("AlbumPath", sReportedPathArray.length() == 1 ? sReportedPathArray.get(0) : sReportedPathArray);
            } catch (JSONException e) {
                GalleryLog.w("AlbumMoveReporter", "Report album move action failed. " + e.getMessage());
            }
            ReportToBigData.reportForAlbumMovePath(sReportedJsonObject.toString().replaceAll("\\\\", ""));
            sLastReportTime = System.currentTimeMillis();
            sReportedPathArray = new JSONArray();
            sReportedJsonObject = new JSONObject();
            if (sHandler != null) {
                sHandler.removeCallbacks(null);
                sHandler = null;
            }
        }
    }
}
