package com.huawei.systemmanager.backup;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.systemmanager.antivirus.AntiVirusBackup;
import com.huawei.systemmanager.rainbow.MainScreenSettingsBackup;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference.SpaceSettingBackup;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CommonPrefBackupProvider extends HsmContentProvider {
    public static final String AUTH = "com.huawei.systemmanager.CommonPrefBackupProvider";
    private static final int DB_VERSION = 1;
    private static final String TAG = "CommonPrefBackupProvider";
    public static final String URI_CONTENT = "content://com.huawei.systemmanager.CommonPrefBackupProvider/";
    public static final Uri URI_RECOVER_COMPLETE = Uri.parse("content://com.huawei.systemmanager.CommonPrefBackupProvider/complete");
    private HashMap<String, IPreferenceBackup> mPrefKeyMap = new HashMap();
    private List<IPreferenceBackup> mPrefModules = new ArrayList();

    public interface IPreferenceBackup {

        public static abstract class BasePreferenceBackup implements IPreferenceBackup {
            protected static final String TAG = "BasePreferenceBackup";

            public void onRecoverComplete() {
            }

            public void onRecoverStart() {
            }
        }

        Set<String> onQueryPreferenceKeys();

        ContentValues onQueryPreferences();

        void onRecoverComplete();

        int onRecoverPreference(String str, String str2);

        void onRecoverStart();
    }

    public boolean onCreate() {
        this.mPrefModules.add(new AntiVirusBackup());
        this.mPrefModules.add(new SpaceSettingBackup());
        this.mPrefModules.add(new MainScreenSettingsBackup());
        return true;
    }

    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
        HwLog.v(TAG, "query starts");
        if (this.mPrefModules.isEmpty()) {
            HwLog.i(TAG, "query: No Module needs to backup preferences");
            return null;
        }
        ContentValues allPreferences = new ContentValues();
        for (IPreferenceBackup prefModule : this.mPrefModules) {
            ContentValues modulePreference = prefModule.onQueryPreferences();
            if (modulePreference.size() > 0) {
                allPreferences.putAll(modulePreference);
            }
        }
        if (allPreferences.size() > 0) {
            return BackupUtil.getPreferenceCursor(allPreferences);
        }
        HwLog.i(TAG, "query: No preference needs backup");
        return null;
    }

    public Uri insert(Uri uri, ContentValues preference) {
        if (preference == null) {
            HwLog.e(TAG, "insert: Invalid values");
            return uri;
        } else if (this.mPrefKeyMap.isEmpty()) {
            HwLog.i(TAG, "insert: Empty key map");
            return uri;
        } else {
            String prefKey = preference.getAsString(BackupConst.PREFERENCE_KEY);
            String prefValue = preference.getAsString(BackupConst.PREFERENCE_VALUE);
            IPreferenceBackup prefModule = (IPreferenceBackup) this.mPrefKeyMap.get(prefKey);
            if (prefModule == null) {
                HwLog.w(TAG, "insert: Can not find module for " + prefKey);
                return uri;
            }
            int nRecoverResult = prefModule.onRecoverPreference(prefKey, prefValue);
            if (nRecoverResult > 0) {
                increaseRecoverSucceedCount();
                HwLog.v(TAG, String.format("insert : Succeeds  %1$s = %2$s", new Object[]{prefKey, prefValue}));
            } else {
                increaseRecoverFailedCount();
                HwLog.v(TAG, String.format("insert : Fails  %1$s = %2$s", new Object[]{prefKey, prefValue}));
            }
            Uri retUri = Uri.withAppendedPath(uri, String.valueOf(nRecoverResult));
            notifiChanged(Uri.parse(URI_CONTENT + prefKey));
            return retUri;
        }
    }

    protected int getDBVersion() {
        return 1;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        HwLog.i(TAG, "canRecoverDB: Try to recover from version : " + nRecoverVersion + ", Current version : " + getDBVersion());
        return true;
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> uriList = new ArrayList();
        uriList.add("content://com.huawei.systemmanager.CommonPrefBackupProvider/CommonPreferences");
        return uriList;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        if (initPreferenceKeyMap()) {
            HwLog.v(TAG, "onRecoverStart: currentVersion = " + getDBVersion() + ", recoverVersion = " + nRecoverVersion);
            for (IPreferenceBackup module : this.mPrefModules) {
                module.onRecoverStart();
            }
            return true;
        }
        HwLog.v(TAG, "onRecoverStart: No preference, currentVersion = " + getDBVersion() + ", recoverVersion = " + nRecoverVersion);
        return false;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        for (IPreferenceBackup module : this.mPrefModules) {
            module.onRecoverComplete();
        }
        notifiChanged(URI_RECOVER_COMPLETE);
        HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        return true;
    }

    private boolean initPreferenceKeyMap() {
        boolean z = false;
        if (this.mPrefModules.isEmpty()) {
            HwLog.i(TAG, "initPreferenceKeyMap: No Module needs to backup preferences");
            return false;
        }
        this.mPrefKeyMap.clear();
        for (IPreferenceBackup prefModule : this.mPrefModules) {
            Set<String> prefKeys = prefModule.onQueryPreferenceKeys();
            if (prefKeys.size() > 0) {
                for (String key : prefKeys) {
                    if (this.mPrefKeyMap.containsKey(key)) {
                        HwLog.w(TAG, "initPreferenceKeyMap: Find duplicate key : " + key);
                    } else {
                        this.mPrefKeyMap.put(key, prefModule);
                    }
                }
            }
        }
        if (!this.mPrefKeyMap.isEmpty()) {
            z = true;
        }
        return z;
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        HwLog.w(TAG, "update : Not support");
        return 0;
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        HwLog.w(TAG, "delete : Not support");
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }
}
