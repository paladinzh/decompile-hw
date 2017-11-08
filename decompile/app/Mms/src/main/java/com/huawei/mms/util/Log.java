package com.huawei.mms.util;

import com.huawei.cspcommon.MLog;

public class Log extends MLog {
    public static final int logPerformance(String msg) {
        return MLog.logPerformance("Mms_app", msg);
    }

    public static final int logCallMethod() {
        return MLog.logCallMethod("Mms_app");
    }

    public static final int logCallMethod(int counter) {
        return MLog.logCallMethod("Mms_app", counter);
    }

    public static final int log(String catTag, String msgTag, String msg) {
        if (MLog.isLoggable(catTag, 2)) {
            return MLog.v(msgTag, msg);
        }
        return 0;
    }
}
