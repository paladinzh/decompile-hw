package com.android.settings.search;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.HwCustSecuritySettingsHwBaseImpl;
import com.android.settings.HwCustSettingsUtils;
import com.android.settings.Utils;
import java.util.List;

public class HwCustSearchIndexProviderImpl extends HwCustSearchIndexProvider {
    private static final String ACTION_DL_BOOSTER_SETTINGS = "com.huawei.android.downloadbooster.DoubleDownloadOpenSettingActivity";
    private static final String ACTION_HONOR_UPDATE = "com.honor.broadcast.action.SUBSCRIPTION";
    private static final String ACTION_KARAOKE_EFFECT_SETTINGS = "com.huawei.android.karaokeeffect.KaraokeEffectSettingsActivity";
    private static final String ACTION_SMART_KEY_SETTINGS = "com.android.huawei.smartkeyActivity.SmartkeyMainActivity";
    private static final String ACTION_SOS = "com.huawei.sos.Settings$EmergencySettingsActivity";
    private static final String ACTION_SOUND_TRIGGER_SETTINGS = "com.huawei.vassistant.action.SOUND_TRIGGER_SETTINGS";
    private static final String ACTION_TRUST_AGENT = "huawei.intent.action.TRUST_AGENT_SETTINGS";
    private static final String CARRIER_NAME_SWITCH = "carrier_name_switch";
    private static final String COLOR_TEMPERATURE = "color_temperature";
    private static final String DL_BOOSTER = "com.huawei.android.downloadbooster";
    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
    private static final boolean FP_SHOW_NOTIFICATION_ON = SystemProperties.getBoolean("ro.config.fp_add_notification", false);
    private static final boolean HAS_FP_CUST_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation_plk", false);
    private static final boolean HAS_FP_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation", false);
    private static final boolean HAS_HONOR_UPDATE = SystemProperties.getBoolean("ro.config.enable_honor_push", false);
    private static final boolean IS_CHINA_TELECOM;
    private static final boolean IS_OTG_FEATURE_ENABLED = SystemProperties.getBoolean("ro.config.hw_otgFeature", false);
    private static final String KEY_ACCELEROMETER_SMART = "accelerometer_smart";
    private static final String KEY_BOOT_DATA_COST_TIP = "enable_boot_tip";
    private static final String KEY_CHINA_TELECOM_EPUSH = "china_telecom_epush";
    private static final String KEY_DTS_MODE = "dts_mode";
    protected static final String KEY_EMUI_VERSION = "emui_version";
    private static final String KEY_GLOVE_MODE_SETTING = "finger_glove_button_settings";
    private static final String KEY_HIDE_NOTIFICATION_BACKGROUND = "hide_notification_background";
    private static final String KEY_HUAWEI_SMART_LOCK = "huawei_smart_lock";
    private static final String KEY_KARAKKE = "karaoke_effect_settings";
    private static final String KEY_KARAKKE_CATEGORY = "karaoke_effect_settings_category";
    private static final String KEY_OTG_FEATURE_SETTING = "otg_feature_settings";
    private static final String KEY_SECURITY_ENCRYPTION = "security_encryption";
    private static final String KEY_SECURITY_ENCRYPTION_CATEGORY = "security_encryption_category";
    private static final String KEY_SMART_BACKLIGHT = "smart_backlight";
    private static final String KEY_SOUND_TRIGGER_SETTING = "sound_trigger_settings";
    private static final String KEY_SWS_MODE = "sws_mode";
    private static final String LOCAL_BACKUP_PASSWORD_KEY = "local_backup_password";
    private static final String PACKAGE_NAME_EPUSH = "com.ctc.epush";
    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
    private static final String SIGNAL_ICON_STYLE = "signal_icon_style";
    private static final String SMART_KEY = "com.android.huawei.smartkey";
    private static final String SOS = "com.huawei.sos";
    private static final String STRICT_MODE_KEY = "strict_mode";
    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
    private final boolean HAS_ROG = SystemProperties.getBoolean("ro.config.ROG", false);
    boolean IS_CHINA_TELECOM_OPTA_OPTB;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CHINA_TELECOM = equals;
    }

    public HwCustSearchIndexProviderImpl() {
        boolean z = false;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            z = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        }
        this.IS_CHINA_TELECOM_OPTA_OPTB = z;
    }

    public List<SearchIndexableRaw> addSoundRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        SearchIndexableRaw data = new SearchIndexableRaw(context);
        if (SystemProperties.getBoolean("ro.config.hw_dts", false)) {
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131628081);
            data.screenTitle = res.getString(2131625101);
            searchIdxRaw.add(data);
        }
        if (SystemProperties.getBoolean("ro.config.hw_sws", false)) {
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131629191);
            data.screenTitle = res.getString(2131625101);
            searchIdxRaw.add(data);
        }
        if (SystemProperties.getBoolean("ro.config.touch_vibrate", false)) {
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131629074);
            data.screenTitle = res.getString(2131625101);
            searchIdxRaw.add(data);
        }
        return searchIdxRaw;
    }

    public List<String> addSoundNonIndexableKeys(Context context, List<String> results) {
        return results;
    }

    private boolean isSupportKaraoke(Context context) {
        return Utils.hasIntentActivity(context.getPackageManager(), ACTION_KARAOKE_EFFECT_SETTINGS);
    }

    public List<SearchIndexableResource> addDisplayXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        SearchIndexableResource sir = new SearchIndexableResource(context);
        sir.xmlResId = 2131230782;
        searchIdxRes.add(sir);
        return searchIdxRes;
    }

    public List<SearchIndexableRaw> addDisplayRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        return searchIdxRaw;
    }

    public List<String> addDisplayNonIndexableKeys(Context context, List<String> results) {
        if (!SystemProperties.getBoolean("ro.config.hw_claro_boot_tip", false)) {
            results.add(KEY_BOOT_DATA_COST_TIP);
        }
        if (!SystemProperties.getBoolean("ro.config.colorTemperature_K3", false)) {
            results.add(COLOR_TEMPERATURE);
        }
        if (!SystemProperties.getBoolean("ro.config.smart_rotation", false)) {
            results.add(KEY_ACCELEROMETER_SMART);
        }
        if (SystemProperties.getInt("ro.config.hw_smart_backlight", 0) == 0) {
            results.add(KEY_SMART_BACKLIGHT);
        }
        if (!Utils.hasIntentActivity(context.getPackageManager(), new Intent(ACTION_TRUST_AGENT))) {
            results.add(KEY_HUAWEI_SMART_LOCK);
        }
        if (!isSupportKaraoke(context)) {
            results.add(KEY_KARAKKE_CATEGORY);
            results.add(KEY_KARAKKE);
        }
        if (!Utils.hasIntentActivity(context.getPackageManager(), new Intent(ACTION_SOUND_TRIGGER_SETTINGS))) {
            results.add(KEY_SOUND_TRIGGER_SETTING);
        }
        if (!SystemProperties.getBoolean("ro.config.HideNotification", false)) {
            results.add(KEY_HIDE_NOTIFICATION_BACKGROUND);
        }
        if (!((Utils.isMultiSimEnabled() ? IS_CHINA_TELECOM : false) && UserHandle.myUserId() == 0)) {
            results.add(SIGNAL_ICON_STYLE);
        }
        if (1 == Global.getInt(context.getContentResolver(), "hw_development_items_hide", 0)) {
            results.add(SHOW_SCREEN_UPDATES_KEY);
            results.add(STRICT_MODE_KEY);
            results.add(WINDOW_ANIMATION_SCALE_KEY);
            results.add(FORCE_HARDWARE_UI_KEY);
            results.add(LOCAL_BACKUP_PASSWORD_KEY);
        }
        return results;
    }

    public List<SearchIndexableResource> addMoreAssistanceXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        SearchIndexableResource sir = new SearchIndexableResource(context);
        sir.xmlResId = 2131230816;
        searchIdxRes.add(sir);
        return searchIdxRes;
    }

    public List<String> addMoreAssistanceNonIndexableKeys(Context context, List<String> keys) {
        boolean isSupportGloveButton = true;
        if (SystemProperties.getInt("ro.config.hw_glovemode_enabled", 0) != 1) {
            isSupportGloveButton = false;
        }
        if (!isSupportGloveButton) {
            keys.add(KEY_GLOVE_MODE_SETTING);
        }
        return keys;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<SearchIndexableRaw> addMoreAssistanceRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        if (!(context == null || searchIdxRaw == null || res == null || !isOtgFeatureEnabled())) {
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131629300);
            data.screenTitle = res.getString(2131627498);
            data.key = KEY_OTG_FEATURE_SETTING;
            searchIdxRaw.add(data);
        }
        return searchIdxRaw;
    }

    public List<SearchIndexableRaw> addLteSwitchRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        if (this.IS_CHINA_TELECOM_OPTA_OPTB) {
            String screenTitle = res.getString(2131628020);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838337;
            searchIdxRaw.add(data);
        }
        return searchIdxRaw;
    }

    public List<SearchIndexableRaw> addOtherAppsRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        String intentTargetClass;
        String intentTargetPackage;
        String action;
        if (Utils.hasPackageInfo(context.getPackageManager(), DL_BOOSTER)) {
            String screenTitle = res.getString(2131629190);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838381;
            data.intentAction = "com.android.settings.action.unknown";
            data.intentTargetClass = ACTION_DL_BOOSTER_SETTINGS;
            data.intentTargetPackage = DL_BOOSTER;
            searchIdxRaw.add(data);
        }
        if (Utils.hasPackageInfo(context.getPackageManager(), SMART_KEY)) {
            screenTitle = res.getString(2131629212);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838416;
            data.intentAction = "com.android.settings.action.unknown";
            data.intentTargetClass = ACTION_SMART_KEY_SETTINGS;
            data.intentTargetPackage = SMART_KEY;
            searchIdxRaw.add(data);
        }
        if (HAS_HONOR_UPDATE && Utils.hasIntentActivity(context.getPackageManager(), ACTION_HONOR_UPDATE)) {
            screenTitle = res.getString(2131629295);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838173;
            data.intentAction = ACTION_HONOR_UPDATE;
            searchIdxRaw.add(data);
        }
        if (HwCustSettingsUtils.IS_SOS_ENABLED && Utils.hasPackageInfo(context.getPackageManager(), SOS)) {
            screenTitle = res.getString(2131629294);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130837690;
            data.intentAction = "com.android.settings.action.unknown";
            data.intentTargetClass = ACTION_SOS;
            data.intentTargetPackage = SOS;
            searchIdxRaw.add(data);
        }
        if (SystemProperties.getBoolean("ro.config.StepCalculator", false)) {
            Intent intent;
            intent = new Intent();
            intentTargetClass = "com.huawei.health.ui.SplashActivity";
            intentTargetPackage = "com.huawei.health";
            intent.setClassName(intentTargetPackage, intentTargetClass);
            action = "com.android.settings.action.unknown";
            if (Utils.hasIntentActivity(context.getPackageManager(), intent)) {
                screenTitle = res.getString(2131629224);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838342;
                data.intentAction = action;
                data.intentTargetClass = intentTargetClass;
                data.intentTargetPackage = intentTargetPackage;
                searchIdxRaw.add(data);
            }
        }
        if (SystemProperties.getBoolean("ro.config.ChargingAlbum", false)) {
            intent = new Intent();
            intentTargetClass = "com.android.dreams.album.PhotoChargeSettings";
            intentTargetPackage = "com.android.dreams.album";
            intent.setClassName(intentTargetPackage, intentTargetClass);
            action = "com.android.settings.action.unknown";
            if (Utils.hasIntentActivity(context.getPackageManager(), intent)) {
                screenTitle = res.getString(2131629105);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.iconResId = 2130838342;
                data.screenTitle = screenTitle;
                data.intentAction = action;
                data.intentTargetClass = intentTargetClass;
                data.intentTargetPackage = intentTargetPackage;
                searchIdxRaw.add(data);
            }
        }
        if (this.HAS_ROG && !LockPatternUtils.isDeviceEncryptionEnabled()) {
            intent = new Intent();
            intentTargetClass = "com.android.settings.RogSettingsActivity";
            intentTargetPackage = "com.android.settings";
            intent.setClassName(intentTargetPackage, intentTargetClass);
            action = "com.android.settings.action.unknown";
            if (Utils.hasIntentActivity(context.getPackageManager(), intent)) {
                screenTitle = res.getString(2131629106);
                data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.iconResId = 2130838342;
                data.screenTitle = screenTitle;
                data.intentAction = action;
                data.intentTargetClass = intentTargetClass;
                data.intentTargetPackage = intentTargetPackage;
                searchIdxRaw.add(data);
            }
        }
        return searchIdxRaw;
    }

    public List<SearchIndexableResource> addDateTimeXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        if (this.IS_CHINA_TELECOM_OPTA_OPTB) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230765;
            searchIdxRes.add(sir);
        }
        return searchIdxRes;
    }

    public List<String> addDateTimeNonIndexableKeys(Context context, List<String> keys) {
        return keys;
    }

    public List<SearchIndexableRaw> addFingerprintMainRawDataToIndex(Context context, List<SearchIndexableRaw> searchIdxRaw, Resources res) {
        if (FP_SHOW_NOTIFICATION_ON) {
            String screenTitle = res.getString(2131627864);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            searchIdxRaw.add(data);
        }
        if (HAS_FP_NAVIGATION) {
            screenTitle = res.getString(2131628281);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            searchIdxRaw.add(data);
            screenTitle = res.getString(2131629227);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            searchIdxRaw.add(data);
        }
        return searchIdxRaw;
    }

    public List<String> addDeviceInfoNonIndexableKeys(Context context, List<String> keys) {
        if (SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false)) {
            keys.add(KEY_EMUI_VERSION);
        }
        if (!(SystemProperties.getBoolean("ro.config.enable.telecom_epush", false) && Utils.isCheckAppExist(context, PACKAGE_NAME_EPUSH))) {
            keys.add(KEY_CHINA_TELECOM_EPUSH);
        }
        return keys;
    }

    public boolean hasIndexForNavigation() {
        return HAS_FP_CUST_NAVIGATION;
    }

    public List<SearchIndexableResource> addSecurityXmlResourcesToIndex(Context context, List<SearchIndexableResource> searchIdxRes) {
        SearchIndexableResource sir = new SearchIndexableResource(context);
        sir.xmlResId = 2131230860;
        searchIdxRes.add(sir);
        return searchIdxRes;
    }

    public List<String> addSecurityNonIndexableKeys(Context context, List<String> keys) {
        if (!HwCustSecuritySettingsHwBaseImpl.showEncryptVersion()) {
            keys.add(KEY_SECURITY_ENCRYPTION_CATEGORY);
            keys.add(KEY_SECURITY_ENCRYPTION);
        }
        return keys;
    }

    public boolean isOtgFeatureEnabled() {
        return IS_OTG_FEATURE_ENABLED;
    }
}
