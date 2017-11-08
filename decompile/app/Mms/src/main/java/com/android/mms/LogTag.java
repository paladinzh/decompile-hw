package com.android.mms;

import android.app.Activity;
import android.content.Context;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.data.RecipientIdCache;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;

public class LogTag {
    public static final boolean SHOW_MMS_LOG = MmsConfig.isEnableShowMmsLog();

    /* renamed from: com.android.mms.LogTag$1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void run() {
            try {
                RecipientIdCache.canonicalTableDump();
                RecipientIdCache.dump();
                Conversation.dumpThreadsTable(this.val$context);
                Conversation.dump();
                Conversation.dumpSmsTable(this.val$context);
                Contact.dump();
            } catch (Throwable a) {
                ErrorMonitor.reportErrorInfo(2, "dumpInternalTables exception", a);
            }
        }
    }

    private static String prettyArray(String[] array) {
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        int len = array.length - 1;
        for (int i = 0; i < len; i++) {
            sb.append(array[i]);
            sb.append(", ");
        }
        sb.append(array[len]);
        sb.append("]");
        return sb.toString();
    }

    public static String logFormat(String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String[]) {
                args[i] = prettyArray((String[]) args[i]);
            }
        }
        return "[" + Thread.currentThread().getId() + "] " + String.format(format, args);
    }

    public static void debug(String format, Object... args) {
        MLog.d(HwCustUpdateUserBehaviorImpl.MMS, logFormat(format, args));
    }

    public static void warn(String format, Object... args) {
        MLog.w(HwCustUpdateUserBehaviorImpl.MMS, logFormat(format, args));
    }

    public static void error(String msg) {
        MLog.e(HwCustUpdateUserBehaviorImpl.MMS, msg);
    }

    public static void error(String format, Object... args) {
        MLog.e(HwCustUpdateUserBehaviorImpl.MMS, logFormat(format, args));
    }

    public static void dumpInternalTables(Context context) {
    }

    public static void warnPossibleRecipientMismatch(String msg, Activity activity) {
        MLog.e(HwCustUpdateUserBehaviorImpl.MMS, "WARNING!!!! ", new RuntimeException());
    }
}
