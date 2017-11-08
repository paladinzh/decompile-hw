package com.android.server.mtm.test;

import android.app.mtm.MultiTaskManager;
import android.app.mtm.MultiTaskPolicy;
import android.os.Bundle;
import android.os.SystemClock;
import com.android.server.mtm.policy.MultiTaskPolicyCreatorImp;
import java.io.PrintWriter;

public final class TestNormalPolicy {
    private static MultiTaskPolicyCreatorImp testpolicycreator = null;

    public static final void test(PrintWriter pw, String[] args) {
        if (args[1] != null && pw != null) {
            if (args.length < 2) {
                pw.println("Bad command conditiontest:");
            }
            testpolicycreator = MultiTaskPolicyCreatorImp.getInstance();
            String cmd = args[1];
            MultiTaskPolicyCreatorImp multiTaskPolicyCreatorImp;
            if ("enable_log".equals(cmd)) {
                multiTaskPolicyCreatorImp = testpolicycreator;
                MultiTaskPolicyCreatorImp.enableDebug();
            } else if ("disable_log".equals(cmd)) {
                multiTaskPolicyCreatorImp = testpolicycreator;
                MultiTaskPolicyCreatorImp.disableDebug();
            } else if ("getresourcepolicy".equals(cmd)) {
                rungetResourcePolicy(pw, args);
            } else if ("getresourcepolicybymanager".equals(cmd)) {
                rungetResourcePolicyByManager(pw, args);
            } else {
                pw.println("Bad command :" + cmd);
            }
        }
    }

    private static void rungetResourcePolicyByManager(PrintWriter pw, String[] args) {
        if (args.length < 7) {
            pw.println("args is invalid");
            return;
        }
        int resourcetype = Integer.parseInt(args[2]);
        int resourcestatus = Integer.parseInt(args[3]);
        String resourceextend = args[4];
        int pid = Integer.parseInt(args[5]);
        int uid = Integer.parseInt(args[6]);
        int testtimes = Integer.parseInt(args[7]);
        pw.println("resourcetype:" + resourcetype);
        pw.println("resourceextend:" + resourceextend);
        pw.println("resourcestatus:" + resourcestatus);
        pw.println("pid:" + pid);
        pw.println("uid:" + uid);
        Bundle rsbundles = new Bundle();
        rsbundles.putInt("pid", pid);
        rsbundles.putInt("uid", uid);
        if (testtimes < 1) {
            testtimes = 1;
        }
        long starttime = SystemClock.uptimeMillis();
        MultiTaskPolicy testresult = null;
        for (int j = 0; j < testtimes; j++) {
            testresult = MultiTaskManager.getInstance().getMultiTaskPolicy(resourcetype, resourceextend, resourcestatus, rsbundles);
        }
        long durtime = SystemClock.uptimeMillis() - starttime;
        if (testresult == null) {
            pw.println("no policy ");
        } else {
            printPolicy(pw, testresult);
        }
        pw.println("total time:" + durtime + "(ms)");
    }

    private static void rungetResourcePolicy(PrintWriter pw, String[] args) {
        if (args.length < 7) {
            pw.println("args is invalid");
            return;
        }
        int resourcetype = Integer.parseInt(args[2]);
        int resourcestatus = Integer.parseInt(args[3]);
        String resourceextend = args[4];
        int pid = Integer.parseInt(args[5]);
        int uid = Integer.parseInt(args[6]);
        pw.println("resourcetype:" + resourcetype);
        pw.println("resourceextend:" + resourceextend);
        pw.println("resourcestatus:" + resourcestatus);
        pw.println("pid:" + pid);
        pw.println("uid:" + uid);
        Bundle rsbundles = new Bundle();
        rsbundles.putInt("pid", pid);
        rsbundles.putInt("uid", uid);
        long starttime = SystemClock.uptimeMillis();
        MultiTaskPolicy testresult = testpolicycreator.getResourcePolicy(resourcetype, resourceextend, resourcestatus, rsbundles);
        if (testresult == null) {
            pw.println("no policy ");
        } else {
            printPolicy(pw, testresult);
        }
        pw.println("total time:" + (SystemClock.uptimeMillis() - starttime) + "(ms)");
    }

    private static void printPolicy(PrintWriter pw, MultiTaskPolicy mPolicy) {
        int policy = mPolicy.getPolicy();
        Bundle policydata = mPolicy.getPolicyData();
        if ((policy & 2) != 0) {
            pw.println("Policy:Forbid");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue2", -1));
            }
        }
        if ((policy & 4) != 0) {
            pw.println("Policy:Delay");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue4", -1));
            }
        }
        if ((policy & 8) != 0) {
            pw.println("Policy:Proxy");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue8", -1));
            }
        }
        if ((policy & 16) != 0) {
            pw.println("Policy:ProcessCpuset");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue16", -1));
            }
        }
        if ((policy & 32) != 0) {
            pw.println("Policy:ProcessKill");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue32", -1));
            }
        }
        if ((policy & 1024) != 0) {
            pw.println("Policy:ProcessShrink");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue1024", -1));
            }
        }
        if ((policy & 1024) != 0) {
            pw.println("Policy:ProcessFreeze");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue1024", -1));
            }
        }
        if ((policy & 128) != 0) {
            pw.println("Policy:MemoryShrink");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue128", -1));
            }
        }
        if ((policy & 256) != 0) {
            pw.println("Policy:MemoryDropCache");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue256", -1));
            }
        }
        if ((policy & 512) != 0) {
            pw.println("Policy:ChangeStatus");
            if (policydata != null) {
                pw.println("Policy value:" + policydata.getInt("policyvalue512", -1));
            }
        }
    }
}
