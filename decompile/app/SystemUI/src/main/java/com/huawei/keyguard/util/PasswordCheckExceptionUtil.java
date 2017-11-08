package com.huawei.keyguard.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.monitor.RadarReporter;

public class PasswordCheckExceptionUtil {

    private static class ExceptionUploader implements Runnable {
        private Context mContext;
        private int mErrorType;

        public ExceptionUploader(Context context, int errorType) {
            this.mContext = context;
            this.mErrorType = errorType;
        }

        public void run() {
            if (this.mContext != null) {
                SharedPreferences sp = this.mContext.getSharedPreferences("lock_error_preferences", 0);
                long currentTime = System.currentTimeMillis();
                if (currentTime > 86400000 + sp.getLong("error_type_" + this.mErrorType, 0)) {
                    RadarReporter.uploadPWDExceptionRadar(this.mErrorType);
                    this.mContext.getSharedPreferences("lock_error_preferences", 0).edit().putLong("error_type_" + this.mErrorType, currentTime).apply();
                }
            }
        }
    }

    public static void sendPwdCheckException(Context context, int errorType) {
        if (1 == errorType || 2 == errorType || 3 == errorType) {
            GlobalContext.getBackgroundHandler().post(new ExceptionUploader(context, errorType));
        }
    }
}
