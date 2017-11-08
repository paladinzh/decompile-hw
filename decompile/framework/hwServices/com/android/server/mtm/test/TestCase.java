package com.android.server.mtm.test;

import android.content.Context;
import java.io.PrintWriter;

public final class TestCase {
    public static final void test(Context context, PrintWriter pw, String[] args) {
        if (args != null && args[0] != null && pw != null) {
            String cmd = args[0];
            if ("procinfo".equals(cmd)) {
                TestProcInfo.test(pw, args);
            } else if ("policyinfo".equals(cmd)) {
                TestStaticPolicy.test(pw, args);
            } else if ("memorypolicy".equals(cmd)) {
                TestMemoryPolicy.test(pw, args);
            } else if ("mormalPolicy".equals(cmd)) {
                TestNormalPolicy.test(pw, args);
            } else if ("apptypeconditon".equals(cmd)) {
                TestAppTypeConditions.test(pw, args);
            } else if ("groupconditon".equals(cmd)) {
                TestGroupConditions.test(pw, args);
            } else if ("packageconditon".equals(cmd)) {
                TestPackageConditions.test(pw, args);
            } else if ("processconditon".equals(cmd)) {
                TestProcessConditions.test(pw, args);
            } else if ("combinedconditon".equals(cmd)) {
                TestvCombinedConditions.test(pw, args);
            } else if ("mtmutils".equals(cmd)) {
                TestMtmUtils.test(pw, args);
            } else if ("appMng".equals(cmd)) {
                TestAppMngListInfo.test(context, pw, args);
            } else {
                pw.println("Bad command :" + cmd);
            }
        }
    }
}
