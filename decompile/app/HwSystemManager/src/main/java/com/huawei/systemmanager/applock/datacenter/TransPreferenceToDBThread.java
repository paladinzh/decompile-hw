package com.huawei.systemmanager.applock.datacenter;

import android.content.Context;
import android.content.SharedPreferences;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;
import com.huawei.systemmanager.applock.utils.sp.FunctionSwitchUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map.Entry;

public class TransPreferenceToDBThread extends Thread {
    private static final String APP_LOCK_PREF_FILE_NAME = "app_lock_pref";
    private static final String TAG = "TransPreferenceToDBThread";
    private Context mContext = null;

    public TransPreferenceToDBThread(Context context) {
        super("applock_TransPreferenceToDBThread");
        this.mContext = context;
    }

    public void run() {
        HwLog.d(TAG, "run TransPreferenceToDBThread");
        SharedPreferences sharedPref = this.mContext.getSharedPreferences(APP_LOCK_PREF_FILE_NAME, 4);
        readAndTransToDB(sharedPref);
        truncateSharedFile(sharedPref);
    }

    private void readAndTransToDB(SharedPreferences sp) {
        for (Entry<String, ?> entry : sp.getAll().entrySet()) {
            String key = (String) entry.getKey();
            String value = String.valueOf(entry.getValue());
            HwLog.v(TAG, "run write old share preference to db: " + key + SqlMarker.COMMA_SEPARATE + value);
            DatabaseSharePrefUtil.setPref(this.mContext, key, value, needBackupKey(key));
        }
    }

    private void truncateSharedFile(SharedPreferences sp) {
        sp.edit().clear().commit();
    }

    private boolean needBackupKey(String key) {
        if (key.equals(FingerprintBindUtils.FINGERPRINT_BIND_STATUS_KEY) || key.equals(FunctionSwitchUtils.APP_LOCK_FUNC_STATUS_KEY)) {
            return true;
        }
        return false;
    }
}
