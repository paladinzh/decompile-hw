package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.HwCustSettingsUtils;
import java.util.List;

public class HwCustAccessibilitySettingsImpl extends HwCustAccessibilitySettings implements OnPreferenceChangeListener {
    public static final String CMD_TURN_OFF_ALL_SOUND = "trun_off_all_sound=off";
    public static final String CMD_TURN_ON_ALL_SOUND = "trun_off_all_sound=on";
    private static final String DB_GRAY_SCALE = "gray_scale";
    private static final int FLAG_DO_NOTHING = 0;
    private static final String HEARING_CATEGORY = "hearing_category";
    public static final boolean HIDE_KEEPER_FASTFILL = SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false);
    private static final String KEY_DALTONIZER_PREFERENCE = "daltonizer_preference_screen";
    private static final String KEY_DISPLAY_CATEGORY = "display_category";
    private static final String KEY_GRAY_SCALE = "gray_scale";
    private static final String LOG_TAG = "HwCustAccessibilitySettings";
    private static final String MOBILITY_CATEGORY = "mobility_category";
    public static final int STATE_TURNING_OFF_ALL_SOUND = 1;
    public static final int STATE_TURNING_ON_ALL_SOUND = 2;
    public static final int STATE_TURN_OFF_ALL_SOUND = 3;
    public static final int STATE_TURN_ON_ALL_SOUND = 0;
    private static final String SYSTEM_CATEGORY = "system_category";
    public static final String SYSTEM_TURNOFF_ALL_SOUND = "trun_off_all_sound";
    private static final String TOGGLE_PROXIMITY_RECEIVE_CALL_PREFERENCE = "toggle_proximity_receive_call_preference";
    private static final String TOGGLE_SINGLE_TRACKER_PREFERENCE = "toggle_single_tracker_preference";
    private static final String TOGGLE_TURN_OFF_ALL_SOUND_PREFERENCE = "toggle_turn_off_all_sound_preference";
    private static final String TOGGLE_VOLUME_RECEIVE_CALL_PREFERENCE = "toggle_volume_receive_call_preference";
    private static final String TalkBack_PACKAGE_NAME = "com.google.android.marvin.talkback";
    private static final String TalkBack_TITLE = "TalkBack";
    private static final String VISION_CATEGORY = "vision_category";
    private static final String VOLUME_BALANCE_PREFERENCE = "volume_balance_preference";
    private boolean isAtt;
    private boolean isGrayScaleSupported;
    private boolean isToggleSingleTrack = SystemProperties.getBoolean("persist.sys.cvaa_mono_channel", false);
    private boolean isTracfone;
    private boolean isUSA;
    private boolean isUSAChannel;
    private CustomSwitchPreference mGrayColorPreference;
    private PreferenceCategory mHearingCategory;
    private Preference mPersistentNotification;
    private PreferenceScreen mSoundBalancePreference;
    private PreferenceCategory mSystemsCategory;
    private TwoStatePreference mToggleProximityReceiveCall;
    private TwoStatePreference mToggleSingleTrackerPreference;
    private TwoStatePreference mToggleTurnOffAllSound;
    private TwoStatePreference mToggleVolumeReceiveCall;
    private boolean showProximityReceiveCall = SystemProperties.getBoolean("ro.show_proximity_call", false);
    private boolean showSoundBalance = SystemProperties.getBoolean("ro.show_sound_balance", false);
    private boolean showTalkbackTip = SystemProperties.getBoolean("ro.show_talkback_tip", false);
    private boolean showTurnOffAllSound = SystemProperties.getBoolean("ro.show_turn_off_all_sound", false);
    private boolean showVolumeReceiveCall = SystemProperties.getBoolean("ro.show_volume_call", false);

    public HwCustAccessibilitySettingsImpl(AccessibilitySettings setting) {
        boolean equals;
        super(setting);
        this.isAtt = "07".equals(SystemProperties.get("ro.config.hw_opta", "0")) ? "840".equals(SystemProperties.get("ro.config.hw_optb", "0")) : false;
        if ("00".equals(SystemProperties.get("ro.config.hw_opta", "0"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", "0"));
        } else {
            equals = false;
        }
        this.isUSA = equals;
        if ("567".equals(SystemProperties.get("ro.config.hw_opta", "0"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", "0"));
        } else {
            equals = false;
        }
        this.isUSAChannel = equals;
        if ("378".equals(SystemProperties.get("ro.config.hw_opta", "0"))) {
            equals = "840".equals(SystemProperties.get("ro.config.hw_optb", "0"));
        } else {
            equals = false;
        }
        this.isTracfone = equals;
        this.isGrayScaleSupported = SystemProperties.getBoolean("ro.config.gray_scale", false);
        init();
        initSprintPrefernce();
    }

    public boolean onTalkBackPreferenceClick() {
        Log.d(LOG_TAG, "onPreferenceTreeClick, first now this is mTalkBackPreferenceScreen");
        boolean isInstalled = isInstalledTalkBackServices(this.mAccessibilitySettings.getActivity());
        boolean isSwitchOn = isTalkBackServicesOn(this.mAccessibilitySettings.getActivity());
        Log.d(LOG_TAG, "onPreferenceTreeClick, isInstalled is " + isInstalled + " and isSwitchon is " + isSwitchOn + " isTalkBackPositiveButtonClicked is " + this.isTalkBackPositiveButtonClicked);
        if (!isInstalled || isSwitchOn || this.isTalkBackPositiveButtonClicked) {
            return false;
        }
        handleTalkBackPreferenceClick();
        return true;
    }

    public void addCustPreferences() {
        PreferenceScreen root = this.mAccessibilitySettings.getPreferenceScreen();
        if (this.isGrayScaleSupported) {
            PreferenceCategory displayCategory = (PreferenceCategory) root.findPreference(KEY_DISPLAY_CATEGORY);
            PreferenceScreen colorCheck = (PreferenceScreen) root.findPreference(KEY_DALTONIZER_PREFERENCE);
            this.mGrayColorPreference = new CustomSwitchPreference(this.mAccessibilitySettings.getActivity());
            this.mGrayColorPreference.setKey("gray_scale");
            this.mGrayColorPreference.setTitle(2131629254);
            this.mGrayColorPreference.setPersistent(false);
            displayCategory.addPreference(this.mGrayColorPreference);
            int insertionOrder = colorCheck.getOrder();
            int preferenceCount = displayCategory.getPreferenceCount();
            for (int i = 0; i < preferenceCount; i++) {
                Preference tempPreference = displayCategory.getPreference(i);
                if (tempPreference.getOrder() > insertionOrder) {
                    tempPreference.setOrder(tempPreference.getOrder() + 1);
                }
            }
            this.mGrayColorPreference.setOrder(insertionOrder);
        }
        if (HwCustSettingsUtils.isFlagPersistentNotificationEnabled()) {
            this.mPersistentNotification = new Preference(this.mAccessibilitySettings.getActivity());
            this.mPersistentNotification.setKey("key_pnotifcation");
            this.mPersistentNotification.setTitle(2131629235);
            this.mPersistentNotification.setLayoutResource(2130968977);
            this.mPersistentNotification.setWidgetLayoutResource(2130968998);
            this.mPersistentNotification.setIntent(new Intent("huawei.intent.action.PERSISTENT_NOTIFICATION_SETTINGS"));
            this.mSystemsCategory = (PreferenceCategory) root.findPreference(SYSTEM_CATEGORY);
            this.mSystemsCategory.addPreference(this.mPersistentNotification);
        }
    }

    private void handleTalkBackPreferenceClick() {
        if (this.showTalkbackTip) {
            new Builder(this.mAccessibilitySettings.getActivity()).setCancelable(true).setTitle(2131625399).setIcon(17301543).setMessage(2131629268).setPositiveButton(2131624783, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(HwCustAccessibilitySettingsImpl.LOG_TAG, "handleTalkBackPreferenceClick, begin click positive button");
                    HwCustAccessibilitySettingsImpl.this.isTalkBackPositiveButtonClicked = true;
                    HwCustAccessibilitySettingsImpl.this.mAccessibilitySettings.onPreferenceTreeClick(HwCustAccessibilitySettingsImpl.this.mTalkBackPreferenceScreen);
                }
            }).setNegativeButton(17039360, null).create().show();
            return;
        }
        this.isTalkBackPositiveButtonClicked = true;
        this.mAccessibilitySettings.onPreferenceTreeClick(this.mTalkBackPreferenceScreen);
    }

    private boolean isInstalledTalkBackServices(Context context) {
        if (context == null) {
            return false;
        }
        List<AccessibilityServiceInfo> installedServices = AccessibilityManager.getInstance(context).getInstalledAccessibilityServiceList();
        if (installedServices == null) {
            return false;
        }
        Log.d(LOG_TAG, "isInstalledTalkBackServices: installedServices.size()=" + installedServices.size());
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            AccessibilityServiceInfo info = (AccessibilityServiceInfo) installedServices.get(i);
            if (!(info == null || info.getResolveInfo() == null)) {
                String title = info.getResolveInfo().loadLabel(context.getPackageManager()).toString();
                Log.d(LOG_TAG, "isInstalledTalkBackServices: title=" + title);
                ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
                if (!(serviceInfo == null || serviceInfo.packageName == null || serviceInfo.name == null)) {
                    Log.d(LOG_TAG, "isInstalledTalkBackServices: componentName=" + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToString());
                    if ("TalkBack".equals(title)) {
                        Log.d(LOG_TAG, "isInstalledTalkBackServices: now we have installed TalkBack");
                        return true;
                    }
                }
            }
        }
        Log.d(LOG_TAG, "isInstalledTalkBackServices: unfortunately, we have not installed TalkBack");
        return false;
    }

    private boolean isTalkBackServicesOn(Context context) {
        if (context == null) {
            return false;
        }
        boolean accessibilityEnabled = Secure.getInt(context.getContentResolver(), "accessibility_enabled", 0) == 1;
        Log.d(LOG_TAG, "isTalkBackServicesOn: accessibilityEnabled is " + accessibilityEnabled);
        String enabledSerices = Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
        Log.d(LOG_TAG, "isTalkBackServicesOn: enabledSerices is " + enabledSerices);
        boolean contains = enabledSerices != null ? enabledSerices.contains(TalkBack_PACKAGE_NAME) : false;
        Log.d(LOG_TAG, "isTalkBackServicesOn: isContainsTalkBackService is " + contains);
        if (!accessibilityEnabled) {
            contains = false;
        }
        return contains;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mTalkBackPreferenceScreen == preference) {
            return onTalkBackPreferenceClick();
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        if (this.mToggleProximityReceiveCall == preference) {
            handleToggleProximityReceiveCallPreferenceChange(newValue);
            return true;
        } else if (this.mToggleVolumeReceiveCall == preference) {
            handleToggleVolumeReceiveCallPreferenceChange(newValue);
            return true;
        } else if (this.mToggleSingleTrackerPreference == preference) {
            handleToggleSingleTrackerPreferenceChange(newValue);
            this.isToggleSingleTrack = ((Boolean) newValue).booleanValue();
            return true;
        } else if (this.mToggleTurnOffAllSound == preference) {
            handleToggleTurnOffAllSoundPreferenceChange(newValue);
            return true;
        } else if (!"gray_scale".equals(key)) {
            return false;
        } else {
            ContentResolver contentResolver = this.mAccessibilitySettings.getActivity().getContentResolver();
            String str = "gray_scale";
            if (((Boolean) newValue).booleanValue()) {
                i = 1;
            }
            System.putInt(contentResolver, str, i);
            return true;
        }
    }

    private void handleToggleProximityReceiveCallPreferenceChange(Object newValue) {
        SystemProperties.set("persist.sys.proximity_call", String.valueOf((Boolean) newValue));
    }

    private void handleToggleVolumeReceiveCallPreferenceChange(Object newValue) {
        SystemProperties.set("persist.sys.volume_call", String.valueOf((Boolean) newValue));
    }

    public void handleToggleSingleTrackerPreferenceChange(Object newValue) {
        Boolean isChecked = (Boolean) newValue;
        Intent intent = new Intent();
        if (isChecked.booleanValue()) {
            AudioSystem.setParameters("cvaa_mono_channel=on");
            intent.setAction("android.intent.action.FMVolume_on");
        } else {
            AudioSystem.setParameters("cvaa_mono_channel=off");
            intent.setAction("android.intent.action.FMVolume_off");
        }
        SystemProperties.set("persist.sys.cvaa_mono_channel", String.valueOf(isChecked));
        this.mAccessibilitySettings.getActivity().sendBroadcast(intent);
    }

    private void handleToggleTurnOffAllSoundPreferenceChange(Object newValue) {
        Boolean isChecked = (Boolean) newValue;
        if (this.mToggleTurnOffAllSound == null) {
            Log.e(LOG_TAG, "mToggleTurnOffAllSound is null and return from handleToggleTurnOffAllSoundPreferenceClick");
            return;
        }
        AudioManager mAudioManager = (AudioManager) this.mAccessibilitySettings.getActivity().getSystemService("audio");
        if (mAudioManager == null) {
            Log.e(LOG_TAG, "mAudioManager is null and return from handleToggleTurnOffAllSoundPreferenceClick");
            return;
        }
        int i;
        String str;
        ContentResolver contentResolver = this.mAccessibilitySettings.getActivity().getContentResolver();
        String str2 = SYSTEM_TURNOFF_ALL_SOUND;
        if (isChecked.booleanValue()) {
            i = 1;
        } else {
            i = 2;
        }
        Global.putInt(contentResolver, str2, i);
        for (int streamType = 0; streamType < AudioSystem.getNumStreamTypes(); streamType++) {
            mAudioManager.setStreamVolume(streamType, mAudioManager.getStreamVolume(streamType), 0);
        }
        if (isChecked.booleanValue()) {
            str = CMD_TURN_ON_ALL_SOUND;
        } else {
            str = CMD_TURN_OFF_ALL_SOUND;
        }
        mAudioManager.setParameters(str);
        SystemProperties.set("persist.sys.turn_off_all_sound", String.valueOf(isChecked));
        contentResolver = this.mAccessibilitySettings.getActivity().getContentResolver();
        str2 = SYSTEM_TURNOFF_ALL_SOUND;
        if (isChecked.booleanValue()) {
            i = 3;
        } else {
            i = 0;
        }
        Global.putInt(contentResolver, str2, i);
    }

    private void init() {
        if (this.isAtt || this.isUSA || this.isUSAChannel || this.isTracfone) {
            PreferenceScreen root = this.mAccessibilitySettings.getPreferenceScreen();
            this.mSystemsCategory = (PreferenceCategory) root.findPreference(SYSTEM_CATEGORY);
            this.mHearingCategory = (PreferenceCategory) root.findPreference(HEARING_CATEGORY);
            this.mToggleProximityReceiveCall = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_PROXIMITY_RECEIVE_CALL_PREFERENCE);
            this.mToggleProximityReceiveCall.setOnPreferenceChangeListener(this);
            if (!(this.showProximityReceiveCall || this.mToggleProximityReceiveCall == null)) {
                this.mSystemsCategory.removePreference(this.mToggleProximityReceiveCall);
            }
            this.mToggleVolumeReceiveCall = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_VOLUME_RECEIVE_CALL_PREFERENCE);
            this.mToggleVolumeReceiveCall.setOnPreferenceChangeListener(this);
            if (!(this.showVolumeReceiveCall || this.mToggleVolumeReceiveCall == null)) {
                this.mSystemsCategory.removePreference(this.mToggleVolumeReceiveCall);
            }
            this.mToggleSingleTrackerPreference = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_SINGLE_TRACKER_PREFERENCE);
            this.mToggleSingleTrackerPreference.setOnPreferenceChangeListener(this);
            boolean showMonoAudio = SystemProperties.getBoolean("ro.show_mono_audio", false);
            if (!(this.mToggleSingleTrackerPreference == null || showMonoAudio)) {
                this.mHearingCategory.removePreference(this.mToggleSingleTrackerPreference);
            }
            this.mSoundBalancePreference = (PreferenceScreen) this.mAccessibilitySettings.findPreference(VOLUME_BALANCE_PREFERENCE);
            if (!(this.mSoundBalancePreference == null || this.showSoundBalance)) {
                this.mHearingCategory.removePreference(this.mSoundBalancePreference);
            }
            this.mToggleTurnOffAllSound = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_TURN_OFF_ALL_SOUND_PREFERENCE);
            this.mToggleTurnOffAllSound.setOnPreferenceChangeListener(this);
            if (!(this.showTurnOffAllSound || this.mToggleTurnOffAllSound == null)) {
                this.mHearingCategory.removePreference(this.mToggleTurnOffAllSound);
            }
        }
    }

    public void updateCustPreference() {
        if (this.mGrayColorPreference != null) {
            boolean grayScaleState = System.getInt(this.mAccessibilitySettings.getActivity().getContentResolver(), "gray_scale", 0) == 1;
            this.mGrayColorPreference.setOnPreferenceChangeListener(this);
            this.mGrayColorPreference.setChecked(grayScaleState);
        }
        if (this.mToggleProximityReceiveCall != null) {
            this.mToggleProximityReceiveCall.setChecked(SystemProperties.getBoolean("persist.sys.proximity_call", false));
        }
        if (this.mToggleVolumeReceiveCall != null) {
            this.mToggleVolumeReceiveCall.setChecked(SystemProperties.getBoolean("persist.sys.volume_call", false));
        }
        if (this.mToggleSingleTrackerPreference != null) {
            Log.d("updateCustPreference ", "isToggleSingleTrack=[" + this.isToggleSingleTrack + "]");
            this.mToggleSingleTrackerPreference.setChecked(this.isToggleSingleTrack);
        }
        if (this.showTurnOffAllSound && this.mToggleTurnOffAllSound != null) {
            this.mToggleTurnOffAllSound.setChecked(Global.getInt(this.mAccessibilitySettings.getActivity().getContentResolver(), SYSTEM_TURNOFF_ALL_SOUND, 0) == 3);
        }
        if (this.mPersistentNotification != null) {
            int i;
            int state = System.getInt(this.mAccessibilitySettings.getActivity().getContentResolver(), "persistent_notification", -1);
            Preference preference = this.mPersistentNotification;
            if (-1 == state) {
                i = 2131627699;
            } else {
                i = 2131627698;
            }
            preference.setSummary(i);
        }
    }

    private void initSprintPrefernce() {
        if (HwCustSettingsUtils.IS_SPRINT) {
            this.mHearingCategory = (PreferenceCategory) this.mAccessibilitySettings.getPreferenceScreen().findPreference(HEARING_CATEGORY);
            this.mToggleSingleTrackerPreference = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_SINGLE_TRACKER_PREFERENCE);
            this.mToggleSingleTrackerPreference.setOnPreferenceChangeListener(this);
            this.mToggleTurnOffAllSound = (TwoStatePreference) this.mAccessibilitySettings.findPreference(TOGGLE_TURN_OFF_ALL_SOUND_PREFERENCE);
            this.mToggleTurnOffAllSound.setOnPreferenceChangeListener(this);
        }
    }

    public void custamizeServicePreferences(ServiceInfo serviceInfo, PreferenceCategory servicesCategory, PreferenceScreen preference) {
        if (HIDE_KEEPER_FASTFILL && serviceInfo.packageName.equals("com.callpod.android_apps.keeper")) {
            servicesCategory.removePreference(preference);
        }
    }
}
