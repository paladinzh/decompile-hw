package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class MoreAssistanceSettings extends MoreSettings implements OnPreferenceChangeListener, Indexable {
    private static final boolean FORCEROTATION_FLAG = ForceRotationSettings.FORCEROTATION_FLAG;
    private static final boolean REMOVE_TOUCH_MODE = SystemProperties.getBoolean("ro.config.hw_remove_touch_mode", false);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230815;
            result.add(sir);
            return MoreAssistanceSettings.mHwCustSearchIndexProvider.addMoreAssistanceXmlResourcesToIndex(context, result);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (context == null) {
                return keys;
            }
            keys.add("virtual_key");
            if (!Utils.hasIntentService(context.getPackageManager(), "com.huawei.android.FloatTask.Service")) {
                keys.add("suspend_button_settings");
            }
            keys.add("choose_single_hand_settings");
            keys.add("sound_trigger_settings");
            keys.add("consonance_finger_settings");
            keys.add("motion_settings");
            Intent intent = new Intent();
            intent.setClassName("com.huawei.vdrive", "com.huawei.vdrive.ui.VDriveActivity");
            Intent vAssistantintent = new Intent();
            vAssistantintent.setClassName("com.huawei.vdrive", "com.huawei.vassistant.VAssistantPreferenceActivity");
            if (!(Utils.hasIntentActivity(context.getPackageManager(), intent) && Utils.hasIntentActivity(context.getPackageManager(), vAssistantintent))) {
                keys.add("voice_assistant_settings");
            }
            String[] smart_cover_location = SystemProperties.get("ro.config.huawei_smallwindow", "").split(",");
            if (MoreAssistanceSettings.isSimpleCover()) {
                keys.add("smart_cover_settings");
            } else {
                keys.add("smart_cover_switch");
                if (smart_cover_location.length != 4) {
                    keys.add("smart_cover_settings");
                }
            }
            if (!Utils.isOwnerUser()) {
                keys.add("timing_task_settings");
            }
            if (!Utils.isOwnerUser() || SystemProperties.getInt("ro.config.hwinternet_audio", 1) <= 0) {
                keys.add("smart_earphone_control");
            }
            if (!PressureUtil.isSupportPressureHabit(context)) {
                keys.add("pressure_response_settings");
            }
            keys.add("smart_e_assistance");
            keys.add("air_sharing");
            if (!MoreAssistanceSettings.FORCEROTATION_FLAG) {
                keys.add("force_rotation_mode");
            }
            if (MoreAssistanceSettings.REMOVE_TOUCH_MODE) {
                keys.add("touch_disable_mode");
            }
            return MoreAssistanceSettings.mHwCustSearchIndexProvider.addMoreAssistanceNonIndexableKeys(context, keys);
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            return MoreAssistanceSettings.mHwCustSearchIndexProvider.addMoreAssistanceRawDataToIndex(context, new ArrayList(), context.getResources());
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private Context mContext;
    private HwCustMoreAssistanceSettings mCustMoreAssistanceSettings;
    private HwCustOtgSettings mCustOtgSettings;
    private HwCustSmartKeySettings mCustSmartKeySettings;
    private ForceRotationSettings mForceRotationSettings;
    private SmartCoverEnabler mSmartCoverEnabler;
    private SmartEarphoneEnabler mSmartEarphoneEnabler;
    private SuspendButtonEnabler mSuspenButtonEnabler;
    private Preference mSuspendButtonPreference;
    private TouchDisableModeEnabler mTouchDisableModeEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        addPreferencesFromResource(2131230815);
        if (this.mContext != null) {
            ((Activity) this.mContext).setTitle(2131627498);
        }
        this.mCustMoreAssistanceSettings = (HwCustMoreAssistanceSettings) HwCustUtils.createObj(HwCustMoreAssistanceSettings.class, new Object[]{this});
        if (this.mCustMoreAssistanceSettings != null) {
            this.mCustMoreAssistanceSettings.updateCustPreference(getActivity());
        }
        this.mCustSmartKeySettings = (HwCustSmartKeySettings) HwCustUtils.createObj(HwCustSmartKeySettings.class, new Object[]{this});
        if (this.mCustSmartKeySettings != null) {
            this.mCustSmartKeySettings.updateCustPreference(getActivity());
        }
        this.mCustOtgSettings = (HwCustOtgSettings) HwCustUtils.createObj(HwCustOtgSettings.class, new Object[]{this});
        if (this.mCustOtgSettings != null) {
            this.mCustOtgSettings.updateCustPreference(getActivity());
        }
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mSuspenButtonEnabler != null) {
            this.mSuspenButtonEnabler.resume();
        }
        if (this.mSmartCoverEnabler != null) {
            this.mSmartCoverEnabler.resume();
        }
        if (this.mSmartEarphoneEnabler != null) {
            this.mSmartEarphoneEnabler.resume();
        }
        if (this.mTouchDisableModeEnabler != null) {
            this.mTouchDisableModeEnabler.resume();
        }
        if (this.mCustOtgSettings != null) {
            this.mCustOtgSettings.onResume();
        }
        if (this.mForceRotationSettings != null) {
            this.mForceRotationSettings.resume();
        }
    }

    public void onPause() {
        if (this.mSuspenButtonEnabler != null) {
            this.mSuspenButtonEnabler.pause();
        }
        if (this.mSmartCoverEnabler != null) {
            this.mSmartCoverEnabler.pause();
        }
        if (this.mSmartEarphoneEnabler != null) {
            this.mSmartEarphoneEnabler.pause();
        }
        if (this.mTouchDisableModeEnabler != null) {
            this.mTouchDisableModeEnabler.pause();
        }
        if (this.mCustOtgSettings != null) {
            this.mCustOtgSettings.onPause();
        }
        if (this.mForceRotationSettings != null) {
            this.mForceRotationSettings.pause();
        }
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private boolean hasNavigationBar() {
        boolean hasNavigationBar = getResources().getBoolean(17956970);
        String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
        if ("1".equals(navBarOverride)) {
            return false;
        }
        if ("0".equals(navBarOverride)) {
            return true;
        }
        return hasNavigationBar;
    }

    protected void updatePreferenceList() {
        Intent intent;
        boolean isOwnerUser = ActivityManager.getCurrentUser() == 0;
        if (!(findPreference("virtual_key") == null || hasNavigationBar())) {
            removePreference("virtual_key");
        }
        this.mSuspendButtonPreference = findPreference("suspend_button_settings");
        if (this.mSuspendButtonPreference != null) {
            if (Utils.hasIntentService(getPackageManager(), "com.huawei.android.FloatTask.Service")) {
                this.mSuspenButtonEnabler = new SuspendButtonEnabler(getActivity(), this.mSuspendButtonPreference);
            } else {
                removePreference("suspend_button_settings");
            }
        }
        if (!(findPreference("pressure_response_settings") == null || PressureUtil.isSupportPressureHabit(this.mContext))) {
            removePreference("pressure_response_settings");
        }
        Preference preferencMotionSettings = findPreference("motion_settings");
        if (preferencMotionSettings != null) {
            if (Utils.hasIntentActivity(getPackageManager(), preferencMotionSettings.getIntent())) {
                preferencMotionSettings.getIntent().putExtra("motion_settings_start_type", 1);
            } else {
                removePreference("motion_settings");
            }
        }
        if (findPreference("choose_single_hand_settings") != null && Utils.isHideSingleHandOperation(getActivity())) {
            removePreference("choose_single_hand_settings");
        }
        Preference soundTriggerPreference = findPreference("sound_trigger_settings");
        if (!(soundTriggerPreference == null || (Utils.hasIntentActivity(getPackageManager(), soundTriggerPreference.getIntent()) && isOwnerUser && !Utils.isWifiOnly(this.mContext)))) {
            removePreference("sound_trigger_settings");
        }
        Preference voiceAssistancePreference = findPreference("voice_assistant_settings");
        if (voiceAssistancePreference != null) {
            intent = new Intent();
            intent.setClassName("com.huawei.vdrive", "com.huawei.vdrive.ui.VDriveActivity");
            Intent vAssistantintent = new Intent();
            vAssistantintent.setClassName("com.huawei.vdrive", "com.huawei.vassistant.VAssistantPreferenceActivity");
            if (Utils.hasIntentActivity(getPackageManager(), intent) && Utils.hasIntentActivity(getPackageManager(), vAssistantintent)) {
                voiceAssistancePreference.setIntent(vAssistantintent);
            } else {
                removePreference("voice_assistant_settings");
            }
        }
        SwitchPreference forceRotationModePreference = (SwitchPreference) findPreference("force_rotation_mode");
        if (forceRotationModePreference != null) {
            if (FORCEROTATION_FLAG) {
                this.mForceRotationSettings = new ForceRotationSettings(this.mContext, forceRotationModePreference);
            } else {
                removePreference("force_rotation_mode");
            }
        }
        Preference smartEAssistancePreference = findPreference("smart_e_assistance");
        if (!(smartEAssistancePreference == null || Utils.hasIntentActivity(getPackageManager(), smartEAssistancePreference.getIntent()))) {
            removePreference("smart_e_assistance");
        }
        Preference consonanceFingerSettings = findPreference("consonance_finger_settings");
        if (!(consonanceFingerSettings == null || (Utils.hasIntentActivity(getPackageManager(), consonanceFingerSettings.getIntent()) && SystemProperties.getBoolean("ro.config.hw_touchplus_enabled", false)))) {
            removePreference("consonance_finger_settings");
        }
        String[] smart_cover_location = SystemProperties.get("ro.config.huawei_smallwindow", "").split(",");
        if (isSimpleCover()) {
            removePreference("smart_cover_settings");
            this.mSmartCoverEnabler = new SmartCoverEnabler(this.mContext, (SwitchPreference) findPreference("smart_cover_switch"));
        } else {
            removePreference("smart_cover_switch");
            if (smart_cover_location.length != 4) {
                removePreference("smart_cover_settings");
            } else {
                this.mSmartCoverEnabler = new SmartCoverEnabler(this.mContext, findPreference("smart_cover_settings"));
            }
        }
        Preference smartEarphonePreference = findPreference("smart_earphone_control");
        if (smartEarphonePreference != null) {
            if (SystemProperties.getInt("ro.config.hwinternet_audio", 1) <= 0 || !isOwnerUser) {
                removePreference("smart_earphone_control");
            } else {
                this.mSmartEarphoneEnabler = new SmartEarphoneEnabler(this.mContext, smartEarphonePreference);
            }
        }
        if (!isOwnerUser) {
            removePreference("timing_task_settings");
        }
        Preference airSharing = findPreference("air_sharing");
        if (airSharing != null) {
            if (Utils.isOwnerUser() && Utils.atLestOneSharingAppExist(this.mContext)) {
                intent = airSharing.getIntent();
                if (!Utils.isAirSharingExist(getActivity())) {
                    airSharing.setTitle(2131628188);
                    intent.setAction("com.huawei.android.mirrorshare.action.ACTION_DEVICE_SELECTOR");
                }
                intent.addFlags(268435456);
                intent.addFlags(262144);
                intent.putExtra("com.huawei.android.airsharing.DEVICE_SELECTOR_CALLER", "com.huawei.android.toolbox");
                try {
                    airSharing.setIntent(intent);
                } catch (Exception e) {
                    Log.e("MoreAssistanceSettings", "updatePreferenceList()--> e : " + e);
                }
            } else {
                removePreference("air_sharing");
            }
        }
        SwitchPreference touchDisableModePreference = (SwitchPreference) findPreference("touch_disable_mode");
        if (touchDisableModePreference != null) {
            this.mTouchDisableModeEnabler = new TouchDisableModeEnabler(this.mContext, touchDisableModePreference);
        }
        if (REMOVE_TOUCH_MODE) {
            removePreference("touch_disable_mode");
        }
        Index.getInstance(getActivity()).updateFromClassNameResource(MoreAssistanceSettings.class.getName(), true, true);
    }

    public static boolean isSimpleCover() {
        return SystemProperties.getBoolean("ro.config.hw_simplecover", false);
    }
}
