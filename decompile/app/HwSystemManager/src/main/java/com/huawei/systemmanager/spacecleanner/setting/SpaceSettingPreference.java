package com.huawei.systemmanager.spacecleanner.setting;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.SharedPreferences;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.backup.CommonPrefBackupProvider.IPreferenceBackup.BasePreferenceBackup;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.Const;
import com.huawei.systemmanager.spacecleanner.utils.AppCleanUpAndStorageNotifyUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpaceSettingPreference {
    public static final String CATEGORY_SPACE_LIB = "space_category_space_lib";
    public static final String CATEGORY_WHITE_LIST = "white_list";
    public static final String KEY_MANUAL_UPDATE_LIB = "space_manual_update_space_lib";
    public static final String KEY_MEMORY_WHITE_LIST = "memory_white_list";
    public static final String PREFERENCE_NAME = "space_prefence";
    private static final String TAG = "SpaceSettingPreference";
    private static final SpaceSettingPreference sInstance = new SpaceSettingPreference();
    private CacheCleanSetting mCacheCleanSetting = new CacheCleanSetting(SwitchKey.KEY_IS_CLEAN_CACHE_DAILY);
    private NotCommonlyUsedSetting mNotCommonlyUsedSetting = new NotCommonlyUsedSetting(SwitchKey.KEY_IS_NOT_COMMONLY_USED_NOTIFY);
    private OnlyWifiUpdateSetting mOnlyWifiUpdateSetting = new OnlyWifiUpdateSetting(SwitchKey.KEY_IS_WIFI_ONLY_UPDATE);
    private PhoneSlowSetting mPhoneSlowSetting = new PhoneSlowSetting();
    private final Map<String, SpaceSwitchSetting> mSwitchSettings = HsmCollections.newArrayMap();
    private UpdateSetting mUpdateSetting = new UpdateSetting(SwitchKey.KEY_IS_AUTO_UPDATE_LIB);

    public static class SpaceSettingBackup extends BasePreferenceBackup {
        public ContentValues onQueryPreferences() {
            ContentValues cv = new ContentValues();
            for (SpaceSwitchSetting setting : SpaceSettingPreference.getDefault().getSwitchSettings()) {
                cv.put(setting.getKey(), setting.getValue());
            }
            return cv;
        }

        public Set<String> onQueryPreferenceKeys() {
            HashSet<String> set = Sets.newHashSet();
            for (SpaceSwitchSetting setting : SpaceSettingPreference.getDefault().getSwitchSettings()) {
                set.add(setting.getKey());
            }
            return set;
        }

        public int onRecoverPreference(String key, String value) {
            SpaceSwitchSetting setting = SpaceSettingPreference.getDefault().getSwitchSetting(key);
            if (setting != null) {
                setting.onBackup(value);
            }
            return 1;
        }
    }

    public static class SwitchKey {
        public static final String KEY_IS_AUTO_UPDATE_LIB = "space_is_auto_update_space_lib";
        public static final String KEY_IS_CLEAN_CACHE_DAILY = "space_clean_cache_daily";
        public static final String KEY_IS_NOT_COMMONLY_USED_NOTIFY = "space_clean_not_commonly_used_notify";
        public static final String KEY_IS_WIFI_ONLY_UPDATE = "space_is_wifi_only_update";
        public static final String KEY_IS_WIFI_ONLY_UPDATE_DIVIDER = "space_is_wifi_only_update_divider";
        public static final String KEY_PHONE_SLOW_NOFITY = "processmanagersetting";
    }

    @TargetApi(19)
    private SpaceSettingPreference() {
        for (SpaceSwitchSetting setting : HsmCollections.newArrayList(this.mCacheCleanSetting, this.mNotCommonlyUsedSetting, this.mOnlyWifiUpdateSetting, this.mUpdateSetting, this.mPhoneSlowSetting)) {
            this.mSwitchSettings.put(setting.getKey(), setting);
        }
    }

    public static SpaceSettingPreference getDefault() {
        return sInstance;
    }

    public CacheCleanSetting getCacheCleanSetting() {
        return this.mCacheCleanSetting;
    }

    public NotCommonlyUsedSetting getNotCommonlyUsedSetting() {
        return this.mNotCommonlyUsedSetting;
    }

    public OnlyWifiUpdateSetting getOnlyWifiUpdateSetting() {
        return this.mOnlyWifiUpdateSetting;
    }

    public UpdateSetting getUpdateSetting() {
        return this.mUpdateSetting;
    }

    public SpaceSwitchSetting getSwitchSetting(String key) {
        return (SpaceSwitchSetting) this.mSwitchSettings.get(key);
    }

    public PhoneSlowSetting getPhoneSlowSetting() {
        return this.mPhoneSlowSetting;
    }

    public void initSettings() {
        this.mCacheCleanSetting.setValue(Boolean.valueOf(true));
        this.mNotCommonlyUsedSetting.setValue(Boolean.valueOf(false));
        this.mUpdateSetting.setValue(Boolean.valueOf(false));
        this.mOnlyWifiUpdateSetting.setValue(Boolean.valueOf(true));
    }

    public void checkSettings() {
        actionHOTAUpdate();
        this.mCacheCleanSetting.doSettingChanged(Boolean.valueOf(this.mCacheCleanSetting.isSwitchOn()));
        this.mNotCommonlyUsedSetting.doSettingChanged(Boolean.valueOf(this.mNotCommonlyUsedSetting.isSwitchOn()));
        this.mUpdateSetting.doSettingChanged(Boolean.valueOf(this.mUpdateSetting.isSwitchOn()));
        this.mPhoneSlowSetting.doSettingChanged(Boolean.valueOf(this.mPhoneSlowSetting.isSwitchOn()));
    }

    private boolean actionHOTAUpdate() {
        SharedPreferences sp = GlobalContext.getContext().getSharedPreferences(Const.SPACE_CLEAN_SHARED_PERFERENCE, 0);
        if (sp == null) {
            HwLog.e(TAG, "actionHOTAUpdate,but sp is null!");
        } else if (sp.contains(AppCleanUpAndStorageNotifyUtils.NOT_COMMONLY_USED_NOTIFY)) {
            HwLog.i(TAG, "not hota for not commonly used notify");
        } else {
            HwLog.i(TAG, "hota for not commonly used notify,set open");
            sp.edit().putBoolean(AppCleanUpAndStorageNotifyUtils.NOT_COMMONLY_USED_NOTIFY, false).commit();
        }
        return false;
    }

    public Collection<SpaceSwitchSetting> getSwitchSettings() {
        return Collections.unmodifiableCollection(this.mSwitchSettings.values());
    }
}
