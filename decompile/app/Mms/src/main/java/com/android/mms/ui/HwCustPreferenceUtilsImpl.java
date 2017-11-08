package com.android.mms.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cspcommon.MLog;
import com.huawei.sprint.chameleon.provider.ChameleonContract;

public class HwCustPreferenceUtilsImpl extends HwCustPreferenceUtils {
    private static final String CHAMELEON_BASE_DB_VERSION = "chameleon_base_db_version";
    public static final boolean DEBUG_ENABLED = false;
    public static final boolean IS_SPRINT = SystemProperties.getBoolean("ro.config.sprint_pim_ext", false);
    private static final String IS_USER_MODIFIED_MMS_AUTO_RETREIVE = "is_user_modified_auto_retreive";
    private static final String NODE_KEY = "name";
    private static final String NODE_VALUE = "value";
    private static final String PREFERENCE_AUTO_RETREIVE_MODIFIED = "mms_auto_retreive_modified";
    private static final String[] PROJECTION = new String[]{"name", "value"};
    public static final String WHERE_AUTO_RERTREIVE_INDEX = "_index='646'";
    private String AUTO_RETREIVE_DISABLED_VALUE = "0";
    private String AUTO_RETREIVE_ENABLED_VALUE = "1";
    private String TAG = "HwCustPreferenceUtilsImpl";
    Runnable doTask = new Runnable() {
        public void run() {
            int chameleon_current_db_version = HwCustPreferenceUtilsImpl.this.getChameleonDBMmsVersion();
            SharedPreferences sp = HwCustPreferenceUtilsImpl.this.mContext.getSharedPreferences(HwCustPreferenceUtilsImpl.PREFERENCE_AUTO_RETREIVE_MODIFIED, 0);
            if (sp.getInt(HwCustPreferenceUtilsImpl.CHAMELEON_BASE_DB_VERSION, 0) != chameleon_current_db_version) {
                Editor edit = sp.edit();
                edit.putInt(HwCustPreferenceUtilsImpl.CHAMELEON_BASE_DB_VERSION, chameleon_current_db_version);
                edit.commit();
                HwCustPreferenceUtilsImpl.this.updateChameleonMmsAutoRetreiveIfRequired();
            }
            HwCustPreferenceUtilsImpl.this.mJobInProgress = false;
        }
    };
    Context mContext;
    private volatile boolean mJobInProgress = false;

    public HwCustPreferenceUtilsImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isChameleonEnabled() {
        return IS_SPRINT;
    }

    public boolean getCustDefaultMmsAutoRetreive(boolean defaultMMSAutoRetrieval) {
        if (isChameleonEnabled()) {
            return getChameleonMmsAutoRetreiveStatus();
        }
        return defaultMMSAutoRetrieval;
    }

    public void checkMmsAutoRetreiveUpdate() {
        if (isChameleonEnabled()) {
            doJobInBackground();
        }
    }

    private void doJobInBackground() {
        if (!this.mJobInProgress) {
            this.mJobInProgress = true;
            new Thread(this.doTask).start();
        }
    }

    public int getChameleonDBMmsVersion() {
        Cursor cursor = null;
        int chameleonDBMmsVersion = 0;
        try {
            cursor = this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_VERSION, null, "category='mms'", null, null);
            if (cursor != null && cursor.moveToNext()) {
                chameleonDBMmsVersion = cursor.getInt(cursor.getColumnIndexOrThrow(NumberInfo.VERSION_KEY));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            MLog.e(this.TAG, "Exception while retriving chameleon Mms db version" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return chameleonDBMmsVersion;
    }

    private void updateChameleonMmsAutoRetreiveIfRequired() {
        boolean chameleon_mms_auto_enabled = getChameleonMmsAutoRetreiveStatus();
        if (!(chameleon_mms_auto_enabled == getAutoDownloadState() || isAutoRetreiveModifiedByUser())) {
            updateMmsAutoRetreive(chameleon_mms_auto_enabled);
        }
    }

    public boolean getChameleonMmsAutoRetreiveStatus() {
        boolean chameleonMmsAutoRetreiveStatus = Boolean.FALSE.booleanValue();
        Cursor MmsAutoRetreiveCursor = this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_CHAMELEON, PROJECTION, WHERE_AUTO_RERTREIVE_INDEX, null, null);
        if (MmsAutoRetreiveCursor != null) {
            while (MmsAutoRetreiveCursor.moveToNext()) {
                try {
                    String chameleonMMSAutoRetreive = MmsAutoRetreiveCursor.getString(1);
                    if (TextUtils.isEmpty(chameleonMMSAutoRetreive)) {
                        chameleonMmsAutoRetreiveStatus = Boolean.FALSE.booleanValue();
                    } else if (this.AUTO_RETREIVE_ENABLED_VALUE.equals(chameleonMMSAutoRetreive)) {
                        chameleonMmsAutoRetreiveStatus = Boolean.TRUE.booleanValue();
                    } else if (this.AUTO_RETREIVE_DISABLED_VALUE.equals(chameleonMMSAutoRetreive)) {
                        chameleonMmsAutoRetreiveStatus = Boolean.FALSE.booleanValue();
                    }
                } catch (SQLException e) {
                    MLog.e(this.TAG, "getChameleonMmsAutoRetreiveStatus Exception while retriving chameleon_mms_auto_enabled" + e);
                } finally {
                    MmsAutoRetreiveCursor.close();
                }
            }
        }
        return chameleonMmsAutoRetreiveStatus;
    }

    private void updateMmsAutoRetreive(boolean chameleon_mms_auto_enabled) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean("pref_key_mms_retrieval_during_roaming", chameleon_mms_auto_enabled);
        editor.commit();
    }

    private boolean getAutoDownloadState() {
        return PreferenceManager.getDefaultSharedPreferences(this.mContext).getBoolean("pref_key_mms_retrieval_during_roaming", true);
    }

    private boolean isAutoRetreiveModifiedByUser() {
        return this.mContext.getSharedPreferences(PREFERENCE_AUTO_RETREIVE_MODIFIED, 0).getBoolean(IS_USER_MODIFIED_MMS_AUTO_RETREIVE, false);
    }
}
