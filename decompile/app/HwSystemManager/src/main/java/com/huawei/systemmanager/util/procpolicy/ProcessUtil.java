package com.huawei.systemmanager.util.procpolicy;

import android.os.Process;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.am.HsmActivityManager;
import java.util.List;

public class ProcessUtil {
    public static final String PROC_BG_NAME = "com.huawei.systemmanager:service";
    public static final String PROC_UI_NAME = "com.huawei.systemmanager";
    private static final String TAG = "ProcPolicy";
    private static ProcessUtil sInstance;
    private String mProcName;

    private ProcessUtil() {
        initProcName();
    }

    public static synchronized ProcessUtil getInstance() {
        ProcessUtil processUtil;
        synchronized (ProcessUtil.class) {
            if (sInstance == null) {
                sInstance = new ProcessUtil();
            }
            processUtil = sInstance;
        }
        return processUtil;
    }

    private void initProcName() {
        HwLog.v(TAG, "begin get procName.");
        int pid = Process.myPid();
        List<STProcessRecord> procList = HsmActivityManager.getInstance().getRunningList();
        if (procList == null) {
            HwLog.w(TAG, "initProcName ,procList is null");
            return;
        }
        for (STProcessRecord proc : procList) {
            if (pid == proc.pid) {
                this.mProcName = proc.processName;
                checkProcess();
                HwLog.i(TAG, "this proc is:" + this.mProcName);
                break;
            }
        }
        HwLog.v(TAG, "end get procName.");
    }

    private void checkProcess() {
        if (!isUiProcess() && !isServiceProcess()) {
            throw new RuntimeException("unknown process name:" + this.mProcName);
        }
    }

    boolean isUiProcess() {
        return "com.huawei.systemmanager".equals(this.mProcName);
    }

    public boolean isServiceProcess() {
        return PROC_BG_NAME.equals(this.mProcName);
    }
}
