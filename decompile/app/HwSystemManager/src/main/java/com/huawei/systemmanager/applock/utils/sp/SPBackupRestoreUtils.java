package com.huawei.systemmanager.applock.utils.sp;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.systemmanager.util.HwLog;

public class SPBackupRestoreUtils {
    private static final String TAG = "SPBackupRestoreUtils";

    interface BackupDefines {
        public static final String BIND_SWITCH = "bind_switch_backup_key";
        public static final String FUNCTION_SWITCH = "func_switch_backup_key";
        public static final int TOTAL_BACKUP_NUM = 2;
    }

    public static int restoreSharePreference(Context context, ContentValues values) {
        try {
            FunctionSwitchUtils.setFunctionSwitchStatus(context, values.getAsBoolean(BackupDefines.FUNCTION_SWITCH).booleanValue());
            FingerprintBindUtils.setFingerprintBindStatus(context, values.getAsBoolean(BackupDefines.BIND_SWITCH).booleanValue());
            return 2;
        } catch (NullPointerException ex) {
            HwLog.e(TAG, "restoreSharePreference catch NullPointerException");
            ex.printStackTrace();
            return -1;
        } catch (Exception ex2) {
            HwLog.e(TAG, "restoreSharePreference catch Exception");
            ex2.printStackTrace();
            return -1;
        }
    }
}
