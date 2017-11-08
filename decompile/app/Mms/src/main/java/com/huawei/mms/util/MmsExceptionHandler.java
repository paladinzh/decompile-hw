package com.huawei.mms.util;

import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ExceptionMonitor.IExceptionChecker;

public class MmsExceptionHandler implements IExceptionChecker {
    public boolean checkException(Thread thread, Throwable ex) {
        if (!ex.getClass().equals(MmsException.class)) {
            return false;
        }
        MLog.e("CSP_RADAR", "!!!!! please check this error !!!", ex);
        return true;
    }
}
