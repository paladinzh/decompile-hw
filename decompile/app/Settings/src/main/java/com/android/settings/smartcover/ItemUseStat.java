package com.android.settings.smartcover;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.support.v7.preference.PreferenceManager;
import org.json.JSONObject;

public class ItemUseStat {
    private static final String[] mExcludeItems = new String[]{"WirelessSettings", "MorePrivacySettings", "MoreApplicationSettings", "MoreAssistanceSettings"};
    private static ItemUseStat sInstance = null;
    private boolean mIsCached = false;
    private JSONObject mLevelDataJson1 = new JSONObject();
    private JSONObject mLevelDataJson2 = new JSONObject();
    private boolean mNeedSendCachedData = true;

    public static synchronized ItemUseStat getInstance() {
        ItemUseStat itemUseStat;
        synchronized (ItemUseStat.class) {
            if (sInstance == null) {
                sInstance = new ItemUseStat();
            }
            itemUseStat = sInstance;
        }
        return itemUseStat;
    }

    private ItemUseStat() {
    }

    public void cacheData(Context context) {
        boolean needSave = false;
        if (!Utils.isMonkeyRunning()) {
            if (this.mLevelDataJson1.length() > 0) {
                needSave = true;
            } else if (this.mLevelDataJson2.length() > 0) {
                needSave = true;
            }
            if (needSave) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                if (this.mLevelDataJson1.length() > 0) {
                    editor.putString("level1_data_key", this.mLevelDataJson1.toString());
                    this.mIsCached = true;
                }
                if (this.mLevelDataJson2.length() > 0) {
                    editor.putString("level2_data_key", this.mLevelDataJson2.toString());
                    this.mIsCached = true;
                }
                if (this.mIsCached) {
                    editor.apply();
                }
            }
        }
    }
}
