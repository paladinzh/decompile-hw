package com.huawei.cspcommon.ex;

import android.net.Uri;
import com.huawei.cspcommon.MLog;
import java.io.PrintStream;
import java.io.PrintWriter;

public class SqliteExceptionEx extends Throwable {
    int mOperation = 0;
    Uri mUri = null;

    public SqliteExceptionEx(int type, Uri uri, Exception e) {
        super(e.getMessage(), e, false, false);
        this.mUri = uri;
        this.mOperation = type;
    }

    public StackTraceElement[] getStackTrace() {
        if (getCause() != null) {
            return getCause().getStackTrace();
        }
        MLog.e("SqliteExceptionEx", "getStackTrace cause is null");
        return new StackTraceElement[0];
    }

    public void printStackTrace(PrintStream err) {
        if (getCause() == null) {
            MLog.e("SqliteExceptionEx", "printStackTrace.PrintStream cause is null");
        } else {
            getCause().printStackTrace(err);
        }
    }

    public void printStackTrace(PrintWriter err) {
        if (getCause() == null) {
            MLog.e("SqliteExceptionEx", "printStackTrace.PrintWriter cause is null");
        } else {
            getCause().printStackTrace(err);
        }
    }
}
