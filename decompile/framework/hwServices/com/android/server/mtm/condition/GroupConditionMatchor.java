package com.android.server.mtm.condition;

import android.os.Bundle;
import android.util.Log;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;

public class GroupConditionMatchor extends ConditionMatchorImp {
    private static boolean DEBUG = false;
    private static final String TAG = "GroupConditionMatchor";
    private static GroupConditionMatchor mConditionMatchor;

    private boolean compareGroupType(int grouptype, int conditiontype) {
        boolean z = true;
        switch (grouptype) {
            case -1:
                if (1008 != conditiontype) {
                    z = false;
                }
                return z;
            case 0:
                if (1006 != conditiontype) {
                    z = false;
                }
                return z;
            case 5:
                if (1005 != conditiontype) {
                    z = false;
                }
                return z;
            case 10:
                if (1007 != conditiontype) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public static synchronized GroupConditionMatchor getInstance() {
        GroupConditionMatchor groupConditionMatchor;
        synchronized (GroupConditionMatchor.class) {
            if (mConditionMatchor == null) {
                mConditionMatchor = new GroupConditionMatchor();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new resource");
                }
            }
            groupConditionMatchor = mConditionMatchor;
        }
        return groupConditionMatchor;
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
        int myschedgroup = myproc.mCurSchedGroup;
        if (200 == myproc.mCurAdj && myproc.mCurSchedGroup != 10) {
            if (myproc.mCurSchedGroup == 0) {
                if (DEBUG) {
                    Log.d(TAG, "group is adjust to :10");
                }
                myschedgroup = 10;
            } else if (myproc.mCurSchedGroup == 5 && (AwareAppMngSort.FG_SERVICE.equals(myproc.mAdjType) || "force-fg".equals(myproc.mAdjType))) {
                if (DEBUG) {
                    Log.d(TAG, "group is just to :10");
                }
                myschedgroup = 10;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "process schedgroup is :" + myschedgroup + " conditiontype is:" + conditiontype);
        }
        boolean matched = compareGroupType(myschedgroup, conditiontype);
        if (matched && 2 == conditionattribute) {
            return 2;
        }
        if (matched) {
            return 1;
        }
        return 0;
    }
}
