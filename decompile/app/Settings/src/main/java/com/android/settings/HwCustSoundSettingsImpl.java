package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class HwCustSoundSettingsImpl extends HwCustSoundSettings implements OnPreferenceChangeListener {
    private static final boolean IS_SOS_SUPPORTED = SystemProperties.getBoolean("ro.config.enable_sos", false);
    private static final boolean IS_SUPPORT_HW_SWS = SystemProperties.getBoolean("ro.config.hw_sws", false);
    private static final boolean IS_TURN_OFF_ALL_SOUND = SystemProperties.getBoolean("ro.show_turn_off_all_sound", false);
    private static final String KEY_ASSISTED_GPS = "assisted_gps";
    private static final String KEY_ASSISTED_GPS_SETTINGS = "assisted_gps_settings";
    private static final String KEY_CALLS_NOTI = "category_calls_and_notification";
    protected static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    private static final String KEY_SOS_PREF = "sos_alarm";
    private static final String KEY_SOUND_CATEGORY_FEEDBACK = "sound_category_feedback";
    private static final String KEY_SWS_MODE = "sws_mode";
    private static final String KEY_TIME_SYNCHRONIZATION = "time_synchronization";
    private static final String KEY_TOGGLE_NANAGEMENT_PERMISSION = "toggle_management_permission";
    private static final String KEY_TOUCHVIBRATE_MODE = "touch_vibrate_mode";
    private static final String KEY_VIBRATE_RINGING = "vibrate_when_ringing";
    private static final String KEY_VIBRATE_SETTINGS = "vibrate_settings";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    private static final String SOS_ACTION = "com.huawei.action.SOSSETTINGS";
    private static final String SOS_PACKAGE_NAME = "com.huawei.sos";
    private static final int STATE_TURNING_OFF_ALL_SOUND = 1;
    private static final int STATE_TURN_OFF_ALL_SOUND = 3;
    private static final int STATE_TURN_ON_ALL_SOUND = 0;
    private static final int SWS_MODE_OFF = 0;
    private static final int SWS_MODE_ON = 3;
    private static final String SYSTEM_TURNOFF_ALL_SOUND = "trun_off_all_sound";
    private static final String TAG = "HwCustSoundSettingsImpl";
    private static final int TOUCH_VIBRATE_MODE_OFF = 0;
    private static final int TOUCH_VIBRATE_MODE_ON = 1;
    protected static final String VIBIRATE_WHEN_SILENT = "vibirate_when_silent";
    private static final String WIRED_HEADSET_ACTION = "android.intent.action.HEADSET_PLUG";
    private static final boolean isHW_DTS_SETTINGS = SystemProperties.getBoolean("ro.config.hw_dts_settings", false);
    private static final boolean isTOUCH_VIBRATE = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
    protected AudioManager mAudioManager;
    private Context mContext;
    BroadcastReceiver mHeadsetReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwCustSoundSettingsImpl.this.mSWSMode != null && intent != null && HwCustSoundSettingsImpl.WIRED_HEADSET_ACTION.equals(intent.getAction()) && HwCustSoundSettingsImpl.isHW_DTS_SETTINGS) {
                boolean stateMode;
                if (intent.getIntExtra("state", 0) == 1) {
                    stateMode = true;
                } else {
                    stateMode = false;
                }
                if (!stateMode) {
                    HwCustSoundSettingsImpl.this.mSoundSettings.mAudioManager.setParameters("HIFIPARA=STEREOWIDEN_Enable=false");
                } else if (System.getInt(HwCustSoundSettingsImpl.this.mSoundSettings.getContentResolver(), HwCustSoundSettingsImpl.KEY_SWS_MODE, 0) == 1) {
                    HwCustSoundSettingsImpl.this.mSoundSettings.mAudioManager.setParameters("HIFIPARA=STEREOWIDEN_Enable=true");
                }
                HwCustSoundSettingsImpl.this.mSWSMode.setEnabled(stateMode);
            }
        }
    };
    private Preference mPrefVibrate;
    private CustomSwitchPreference mSWSMode;
    private CustomSwitchPreference mTouchVibrateMode;
    private ContentResolver resolver;

    public HwCustSoundSettingsImpl(SoundSettings soundSettings) {
        super(soundSettings);
        this.mContext = soundSettings.getActivity();
        this.resolver = this.mContext.getContentResolver();
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
    }

    public void resume() {
        this.mContext.registerReceiver(this.mHeadsetReceiver, new IntentFilter(WIRED_HEADSET_ACTION));
    }

    public void updateCheckboxState() {
        boolean z = false;
        if (this.mSoundSettings != null) {
            int swsMode = System.getInt(this.mSoundSettings.getContentResolver(), KEY_SWS_MODE, 0);
            if (this.mSWSMode != null) {
                CustomSwitchPreference customSwitchPreference = this.mSWSMode;
                if (swsMode == 3) {
                    z = true;
                }
                customSwitchPreference.setChecked(z);
            }
        }
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mHeadsetReceiver);
    }

    public void updateCustPreference(Context context) {
        PreferenceScreen root = this.mSoundSettings.getPreferenceScreen();
        if (IS_SOS_SUPPORTED) {
            if (Utils.isCheckAppExist(context, SOS_PACKAGE_NAME)) {
                PreferenceCategory volumeCategory = (PreferenceCategory) root.findPreference(KEY_SOUND_CATEGORY_FEEDBACK);
                Preference sosPref = new Preference(this.mContext);
                sosPref.setTitle(2131629299);
                sosPref.setKey(KEY_SOS_PREF);
                sosPref.setIntent(new Intent(SOS_ACTION));
                sosPref.setWidgetLayoutResource(2130968998);
                volumeCategory.addPreference(sosPref);
            }
        }
        if (IS_SUPPORT_HW_SWS) {
            this.mSWSMode = new CustomSwitchPreference(context);
            this.mSWSMode.setKey(KEY_SWS_MODE);
            this.mSWSMode.setTitle(2131629191);
            this.mSWSMode.setPersistent(false);
            this.mSWSMode.setOnPreferenceChangeListener(this);
            ((PreferenceCategory) root.findPreference(KEY_SOUND_CATEGORY_FEEDBACK)).addPreference(this.mSWSMode);
            this.mSWSMode.setChecked(System.getInt(this.mSoundSettings.getContentResolver(), KEY_SWS_MODE, 0) == 3);
            if (isHW_DTS_SETTINGS) {
                this.mSWSMode.setEnabled(((AudioManager) this.mContext.getSystemService("audio")).isWiredHeadsetOn());
                this.mSWSMode.setTitle(2131629258);
            }
        }
        if (isTOUCH_VIBRATE) {
            this.mTouchVibrateMode = new CustomSwitchPreference(context);
            this.mTouchVibrateMode.setKey(KEY_TOUCHVIBRATE_MODE);
            this.mTouchVibrateMode.setTitle(2131629074);
            this.mTouchVibrateMode.setSummary(2131629075);
            this.mTouchVibrateMode.setPersistent(false);
            this.mTouchVibrateMode.setOnPreferenceChangeListener(this);
            root.addPreference(this.mTouchVibrateMode);
            Preference lockSounds = root.findPreference(KEY_HAPTIC_FEEDBACK);
            this.mTouchVibrateMode.setOrder(lockSounds.getOrder());
            root.removePreference(lockSounds);
            this.mTouchVibrateMode.setChecked(1 == System.getInt(this.mSoundSettings.getContentResolver(), KEY_TOUCHVIBRATE_MODE, 1));
        }
        if (isVibrationPatternAvailable()) {
            PreferenceCategory mCategoryCallsAndNoti = (PreferenceCategory) root.findPreference(KEY_CALLS_NOTI);
            CustomSwitchPreference mVibratePref = (CustomSwitchPreference) root.findPreference("vibrate_when_ringing");
            this.mPrefVibrate = new Preference(context);
            this.mPrefVibrate.setKey(KEY_VIBRATE_SETTINGS);
            this.mPrefVibrate.setTitle(2131629217);
            this.mPrefVibrate.setLayoutResource(2130968977);
            this.mPrefVibrate.setWidgetLayoutResource(2130968998);
            if (mVibratePref != null) {
                this.mPrefVibrate.setOrder(mVibratePref.getOrder() + 1);
            }
            this.mPrefVibrate.setIntent(new Intent("huawei.intent.action.VIBRATE_SETTINGS"));
            mCategoryCallsAndNoti.addPreference(this.mPrefVibrate);
            if ((System.getInt(this.resolver, "vibrate_when_ringing", 0) != 0) || isVibirateWhenSilent()) {
                this.mPrefVibrate.setEnabled(true);
            } else {
                this.mPrefVibrate.setEnabled(false);
            }
        }
    }

    public boolean isVibrationPatternAvailable() {
        return SystemProperties.getBoolean("ro.config.hw_vibration_type", false);
    }

    protected boolean isVibirateWhenSilent() {
        int i = 1;
        boolean enable = isStoredVibirateWhenSilent();
        if (isRingVolumeSilent()) {
            enable = this.mAudioManager.getRingerMode() == 1;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = VIBIRATE_WHEN_SILENT;
            if (!enable) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
        return enable;
    }

    protected boolean isRingVolumeSilent() {
        return this.mAudioManager.getRingerMode() != 2;
    }

    protected boolean isStoredVibirateWhenSilent() {
        return Global.getInt(this.mContext.getContentResolver(), VIBIRATE_WHEN_SILENT, 0) == 1;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Boolean valueObj;
        ContentResolver contentResolver;
        String str;
        int i;
        if (KEY_SWS_MODE.equals(key) && this.mSWSMode != null) {
            valueObj = (Boolean) newValue;
            Log.i(TAG, "valueObj = " + valueObj);
            this.mSWSMode.setChecked(valueObj.booleanValue());
            if (valueObj.booleanValue()) {
                this.mSoundSettings.mAudioManager.setParameters("HIFIPARA=STEREOWIDEN_Enable=true");
            } else {
                this.mSoundSettings.mAudioManager.setParameters("HIFIPARA=STEREOWIDEN_Enable=false");
            }
            contentResolver = this.mSoundSettings.getContentResolver();
            str = KEY_SWS_MODE;
            if (valueObj.booleanValue()) {
                i = 3;
            } else {
                i = 0;
            }
            System.putInt(contentResolver, str, i);
        } else if (KEY_TOUCHVIBRATE_MODE.equals(key) && this.mTouchVibrateMode != null) {
            valueObj = (Boolean) newValue;
            Log.i(TAG, "valueObj = " + valueObj);
            this.mTouchVibrateMode.setChecked(valueObj.booleanValue());
            contentResolver = this.mSoundSettings.getContentResolver();
            str = KEY_TOUCHVIBRATE_MODE;
            if (valueObj.booleanValue()) {
                i = 1;
            } else {
                i = 0;
            }
            System.putInt(contentResolver, str, i);
        }
        return false;
    }

    public void custOnCreate() {
        if (IS_TURN_OFF_ALL_SOUND && isTurnOffAllSound()) {
            Toast.makeText(this.mContext, this.mContext.getResources().getString(2131629154), 0).show();
        }
    }

    private boolean isTurnOffAllSound() {
        int state = System.getInt(this.mContext.getContentResolver(), "trun_off_all_sound", 0);
        if (state == 3 || state == 1) {
            return true;
        }
        return false;
    }
}
