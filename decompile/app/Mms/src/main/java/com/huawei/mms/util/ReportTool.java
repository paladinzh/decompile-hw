package com.huawei.mms.util;

import android.content.Context;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Method;

public class ReportTool {
    private static Context mContext = null;
    private static Method sReportMethod = null;
    private static Class<?> sReporterClazz = null;
    private static ReportTool sSingleInstance = null;

    public static ReportTool getInstance(Context context) {
        if (sSingleInstance == null) {
            sSingleInstance = new ReportTool(context.getApplicationContext());
        }
        return sSingleInstance;
    }

    private ReportTool(Context context) {
        initReporter(context);
    }

    private void initReporter(Context context) {
        try {
            sReporterClazz = new PathClassLoader("/system/framework/com.huawei.report.jar", context.getClassLoader()).loadClass("com.huawei.report.ReporterInterface");
            sReportMethod = sReporterClazz.getDeclaredMethod("e", new Class[]{Context.class, Integer.TYPE, String.class});
            mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e("ReportTools", "Can't find sReporterClazz");
            sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e("ReportTools", "Can't find sReportMethod");
            sReportMethod = null;
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (isBetaUser()) {
            try {
                if (!(sReportMethod == null || sReporterClazz == null)) {
                    return ((Boolean) sReportMethod.invoke(sReporterClazz, new Object[]{mContext, Integer.valueOf(eventID), eventMsg})).booleanValue();
                }
            } catch (Exception e) {
                Log.e("ReportTools", "got exception" + e.getMessage(), e);
            }
        } else {
            Log.e("ReportTools", "This is not beta user build");
        }
        return false;
    }

    private boolean isBetaUser() {
        return false;
    }
}
