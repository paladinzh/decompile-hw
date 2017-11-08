package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;

public class ProcessConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "ProcessConditionMatchor";
    private static ProcessConditionMatchor mConditionMatchor;

    private boolean compareProcessName(String processname, String conditionname, int conditiontype) {
        if (processname == null || conditionname == null) {
            return false;
        }
        if (ConditionMatchor.PROCESSNAMECONTAINS == conditiontype) {
            if (processname.contains(conditionname)) {
                return true;
            }
        } else if (processname.equals(conditionname)) {
            return true;
        }
        return false;
    }

    public static synchronized ProcessConditionMatchor getInstance() {
        ProcessConditionMatchor processConditionMatchor;
        synchronized (ProcessConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new ProcessConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            processConditionMatchor = mConditionMatchor;
        }
        return processConditionMatchor;
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
        int conditionattribute = args.getInt("conditionattribute", -1);
        String conditionname = args.getString("conditionextend", null);
        if (conditionname == null || conditionname.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "conditiondescribe is not set ");
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
        ProcessInfo myproc = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (myproc == null) {
            if (DEBUG) {
                Log.d(TAG, "process not exist pid:" + pid);
            }
            return 0;
        }
        boolean matched = compareProcessName(myproc.mProcessName, conditionname, conditiontype);
        String combinedcondition = args.getString("combinedcondition", null);
        if (DEBUG) {
            Log.d(TAG, "combinedcondition is " + combinedcondition);
        }
        if (!(combinedcondition == null || combinedcondition.isEmpty())) {
            ConditionMatchor mConditionMatchor = ConditionMatchorImp.getConditionMatchor(1000);
            if (mConditionMatchor == null) {
                return 0;
            }
            if (mConditionMatchor.conditionMatch(1000, args) == 0) {
                if (DEBUG) {
                    Log.d(TAG, "combinedcondition is UNMATCHED");
                }
                return 0;
            }
        }
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
