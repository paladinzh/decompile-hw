package com.android.systemui.utils.analyze;

import android.content.Context;
import android.util.Log;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.huawei.bd.Reporter;

public class BDReporter {
    public static boolean e(final Context context, final int id, final String type) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                Log.d("ReporterUtil", " e id:" + id + " type:" + "{" + type + "}");
                Reporter.e(context, id, "{ " + type + " }");
                return false;
            }
        });
        return true;
    }

    public static boolean c(final Context context, final int id) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                Log.d("ReporterUtil", "c id:" + id);
                Reporter.c(context, id);
                return false;
            }
        });
        return true;
    }
}
