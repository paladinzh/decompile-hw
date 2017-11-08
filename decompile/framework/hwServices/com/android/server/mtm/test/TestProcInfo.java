package com.android.server.mtm.test;

import android.app.mtm.MultiTaskManager;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import java.io.PrintWriter;

public final class TestProcInfo {
    private static MultiTaskManager mMultiTaskManager = null;
    private static ProcessInfoCollector mProcInfo = null;

    public static final void test(PrintWriter pw, String[] args) {
        mProcInfo = ProcessInfoCollector.getInstance();
        mMultiTaskManager = MultiTaskManager.getInstance();
        if (args[1] != null && pw != null) {
            String cmd = args[1];
            if ("dump".equals(cmd)) {
                mProcInfo.dump(pw);
            } else if ("enable_log".equals(cmd)) {
                mProcInfo.enableDebug();
            } else if ("disable_log".equals(cmd)) {
                mProcInfo.disableDebug();
            } else if ("ut_lru".equals(cmd)) {
                mProcInfo.getAMSLru();
            } else if ("ut_lrubyid".equals(cmd)) {
                if (args[2] != null) {
                    mProcInfo.getAMSLruBypid(Integer.parseInt(args[2]));
                }
            } else if ("ut_killprocess".equals(cmd)) {
                if (args[2] != null) {
                    mMultiTaskManager.killProcess(Integer.parseInt(args[2]), false);
                }
            } else if ("ut_killprocessRestart".equals(cmd)) {
                if (args.length >= 3 && args[2] != null) {
                    mMultiTaskManager.killProcess(Integer.parseInt(args[2]), true);
                }
            } else if (!"ut_forcestop".equals(cmd)) {
                pw.println("Bad command :" + cmd);
            } else if (args[2] != null) {
                mMultiTaskManager.forcestopApps(Integer.parseInt(args[2]));
            }
        }
    }
}
