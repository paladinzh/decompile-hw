package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;

public class AppTypeConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "AppTypeConditionMatchor";
    private static AppTypeConditionMatchor mConditionMatchor;

    private boolean compareAppType(int apptype, int conditiontype) {
        boolean z = true;
        switch (apptype) {
            case 1:
                if (1004 != conditiontype) {
                    z = false;
                }
                return z;
            case 2:
                if (1003 != conditiontype) {
                    z = false;
                }
                return z;
            case 3:
                if (1002 != conditiontype) {
                    z = false;
                }
                return z;
            case 4:
                if (1001 != conditiontype) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public static synchronized AppTypeConditionMatchor getInstance() {
        AppTypeConditionMatchor appTypeConditionMatchor;
        synchronized (AppTypeConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new AppTypeConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            appTypeConditionMatchor = mConditionMatchor;
        }
        return appTypeConditionMatchor;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public int conditionMatch(int conditiontype, Bundle args) {
        if (args == null) {
            if (DEBUG) {
                Log.d(TAG, "condition args is not exist ");
            }
            return 0;
        }
        Bundle rsbundle = args.getBundle("resourcebundle");
        if (rsbundle == null) {
            if (DEBUG) {
                Log.d(TAG, "resourcebundle is not set ");
            }
            return 0;
        }
        int pid = rsbundle.getInt("pid", -1);
        int conditionattribute = args.getInt("conditionattribute", -1);
        ProcessInfo myproc = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (myproc == null) {
            if (DEBUG) {
                Log.d(TAG, "process not exist pid:" + pid);
            }
            return 0;
        }
        boolean matched = compareAppType(myproc.mType, conditiontype);
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
