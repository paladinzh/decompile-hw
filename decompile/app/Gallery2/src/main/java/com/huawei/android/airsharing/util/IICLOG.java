package com.huawei.android.airsharing.util;

import android.util.Log;

public class IICLOG {
    private static IICLOG mStInstance;
    private boolean mBDebugSwithch = true;
    private boolean mBDetailSwitch = true;
    private boolean mBErrorSwitch = true;
    private boolean mBInfoSwitch = false;
    private boolean mBVerboseSwitch = false;
    private boolean mBWarningSwitch = true;

    public static IICLOG getInstance() {
        if (mStInstance == null) {
            mStInstance = new IICLOG();
        }
        return mStInstance;
    }

    private String buildMsg(String msg) {
        StringBuilder buffer = new StringBuilder();
        if (this.mBDetailSwitch) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
            buffer.append("[ ");
            buffer.append(stackTraceElement.getFileName());
            buffer.append(": ");
            buffer.append(stackTraceElement.getLineNumber());
            buffer.append("]");
        }
        buffer.append(msg);
        return buffer.toString();
    }

    public void d(String strTag, String strLog) {
        if (this.mBDebugSwithch) {
            Log.d(strTag, buildMsg(strLog));
        }
    }

    public void w(String strTag, String strLog) {
        if (this.mBWarningSwitch) {
            Log.w(strTag, buildMsg(strLog));
        }
    }
}
