package com.android.settings.wifi;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.lcagent.client.LogCollectManager;
import dalvik.system.PathClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReportTool {
    private static ReportTool sSingleInstance = null;
    private LogCollectManager mClient = null;
    private Context mContext = null;
    private Method sReportMethod = null;
    private Class<?> sReporterClazz = null;

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
            this.sReporterClazz = new PathClassLoader("/system/framework/com.huawei.report.jar", context.getClassLoader()).loadClass("com.huawei.report.ReporterInterface");
            this.sReportMethod = this.sReporterClazz.getDeclaredMethod("e", new Class[]{Context.class, Integer.TYPE, String.class});
            this.mClient = new LogCollectManager(context);
            this.mContext = context;
        } catch (ClassNotFoundException e) {
            Log.e("ReportTools", "Can't find sReporterClazz");
            this.sReporterClazz = null;
        } catch (NoSuchMethodException e2) {
            Log.e("ReportTools", "Can't find sReportMethod");
            this.sReportMethod = null;
        }
    }

    public boolean report(int eventID, String eventMsg) {
        if (isBetaUser()) {
            try {
                if (!(this.sReportMethod == null || this.sReporterClazz == null)) {
                    return ((Boolean) this.sReportMethod.invoke(this.sReporterClazz, new Object[]{this.mContext, Integer.valueOf(eventID), eventMsg})).booleanValue();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            }
        } else {
            Log.e("ReportTools", "This is not beta user build");
        }
        return false;
    }

    private boolean isBetaUser() {
        return 3 == getUserType();
    }

    private int getUserType() {
        int userType = -1;
        if (this.mClient == null) {
            return userType;
        }
        try {
            userType = this.mClient.getUserType();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return userType;
    }
}
