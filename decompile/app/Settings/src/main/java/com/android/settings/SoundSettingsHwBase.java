package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import com.android.settings.navigation.NaviUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.huawei.android.os.BootanimEx;
import com.huawei.cust.HwCustUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class SoundSettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected static final boolean DUAL_SMARTPA_SUPPORT = "true".equals(AudioSystem.getParameters("audio_capability=dual_smartpa_support"));
    protected static final boolean IS_SUPPORT_HW_DTS = SystemProperties.getBoolean("ro.config.hw_dts", false);
    protected static final boolean SPK_RCV_STEREO_SUPPORT;
    protected static final boolean isRingtoneAscending = SystemProperties.getBoolean("ro.config.hw_ascend_ringtone", false);
    protected CustomSwitchPreference hwDTSPreference;
    protected CustomSwitchPreference hwHPXPreference;
    protected CustomSwitchPreference hwStereoPreference;
    protected CustomSwitchPreference mAscendRingtone;
    protected AudioManager mAudioManager;
    private final BroadcastReceiver mDTSReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.HEADSET_PLUG".equals(intent.getAction())) {
                boolean stateMode = intent.getIntExtra("state", 0) == 1;
                if (SoundSettingsHwBase.this.hwDTSPreference != null) {
                    SoundSettingsHwBase.this.hwDTSPreference.setEnabled(stateMode);
                }
                if (SoundSettingsHwBase.this.hwHPXPreference != null) {
                    SoundSettingsHwBase.this.hwHPXPreference.setEnabled(stateMode);
                }
            }
        }
    };
    protected CustomSwitchPreference mDockAudioMediaEnabled;
    protected CustomSwitchPreference mDockSounds;
    protected Preference mDolbyMobileSettings;
    protected ListPreference mDtmfTone;
    protected CustomSwitchPreference mHapticFeedback;
    private HwCustSoundSettingsHwBase mHwCustSoundSettingsHwBase;
    protected CustomSwitchPreference mLockSounds;
    protected Preference mMusicFx;
    protected DefaultRingtonePreference mNotificationPreference;
    protected CustomSwitchPreference mPhysicNaviHapticFeedback;
    protected DefaultRingtonePreference mRingtone1;
    protected DefaultRingtonePreference mRingtone2;
    protected DefaultRingtonePreference mRingtonePreference;
    protected int mRingtoneType = -1;
    private CustomSwitchPreference mShotSounds;
    private final BroadcastReceiver mSilentModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                SoundSettingsHwBase.this.updateSilentMode();
            }
        }
    };
    protected CustomSwitchPreference mSoundEffects;
    protected CustomSwitchPreference mSystemBootShutSound;
    protected CustomSwitchPreference mVibirateWhenSilent;
    protected CustomSwitchPreference mVibrateWhenRinging;
    protected RestrictedPreference mVolumePreference;
    protected CustomSwitchPreference mVolumeSilent;

    static {
        boolean z = false;
        if (DUAL_SMARTPA_SUPPORT) {
            z = "true".equals(AudioSystem.getParameters("audio_capability=spk_rcv_stereo_support"));
        }
        SPK_RCV_STEREO_SUPPORT = z;
    }

    protected void handleMessageExt(Message msg) {
        switch (msg.what) {
            case 3:
                this.mRingtone1.setSummary((CharSequence) msg.obj);
                return;
            case 4:
                this.mRingtone2.setSummary((CharSequence) msg.obj);
                return;
            default:
                return;
        }
    }

    protected void ringtoneLookupExt() {
        if (this.mRingtone1 != null) {
            updateRingtoneName(1, this.mRingtone1, 3);
        }
        if (this.mRingtone2 != null) {
            updateRingtoneName(8, this.mRingtone2, 4);
        }
    }

    protected void updateRingtoneName(int type, Preference preference, int msg) {
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    protected void init(Bundle icicle) {
        initPreference();
        initRingtone();
        initSilentMode();
        initDolbyMobile();
        initAscendRingtone();
        initCard2VibrateWhenRinging();
        initShotSound();
        initSystemBootShutSoundSwitch();
        initEarphoneEffect();
        initHwDTS();
        initHwHPX();
        initHwStereo();
        initKaraoke();
        initHapticFeedBackPref();
    }

    protected void initEarphoneEffect() {
        Preference earphoneEffectPreference = findPreference("earphone_effect");
        if (earphoneEffectPreference == null) {
            return;
        }
        if (IS_SUPPORT_HW_DTS || !Utils.hasIntentActivity(getPackageManager(), earphoneEffectPreference.getIntent())) {
            removePreference("earphone_effect");
        }
    }

    protected void initHwDTS() {
        this.hwDTSPreference = (CustomSwitchPreference) findPreference("hw_dts");
        if (this.hwDTSPreference == null) {
            return;
        }
        if (IS_SUPPORT_HW_DTS) {
            boolean z;
            Context mContext = getActivity();
            this.hwDTSPreference.setPersistent(false);
            this.hwDTSPreference.setOnPreferenceChangeListener(this);
            int dtsMode = Secure.getInt(getContentResolver(), "dts_mode", 0);
            CustomSwitchPreference customSwitchPreference = this.hwDTSPreference;
            if (dtsMode == 3) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
            this.hwDTSPreference.setEnabled(((AudioManager) mContext.getSystemService("audio")).isWiredHeadsetOn());
            return;
        }
        removePreference("hw_dts");
    }

    protected void initHwStereo() {
        this.hwStereoPreference = (CustomSwitchPreference) findPreference("hw_stereo");
        if (this.hwStereoPreference == null) {
            return;
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            this.hwStereoPreference.setPersistent(false);
            this.hwStereoPreference.setOnPreferenceChangeListener(this);
            this.hwStereoPreference.setChecked(Secure.getInt(getContentResolver(), "stereo_landscape_portrait", 1) == 1);
            return;
        }
        removePreference("hw_stereo");
    }

    protected void initKaraoke() {
        if (!isSupportKaraoke()) {
            removePreference("karaoke_effect_settings_category");
        }
    }

    protected boolean isSupportKaraoke() {
        return Utils.hasIntentActivity(getPackageManager(), "com.huawei.android.karaokeeffect.KaraokeEffectSettingsActivity");
    }

    protected void initPreference() {
        Preference pref = findPreference("ring_volume");
        if (pref instanceof RestrictedPreference) {
            this.mVolumePreference = (RestrictedPreference) pref;
        }
        RingerVolumePreferenceDialogActivity.setSilent(this.mVolumePreference);
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getContext(), "no_adjust_volume", UserHandle.myUserId());
        if (this.mVolumePreference != null) {
            this.mVolumePreference.setDisabledByAdmin(admin);
        }
    }

    private void initAscendRingtone() {
        this.mAscendRingtone = (CustomSwitchPreference) findPreference("ascend_ringtone");
        if (this.mAscendRingtone != null) {
            this.mAscendRingtone.setOnPreferenceChangeListener(this);
            if (isRingtoneAscending) {
                boolean z;
                this.mAscendRingtone.setPersistent(false);
                CustomSwitchPreference customSwitchPreference = this.mAscendRingtone;
                if (System.getInt(getContentResolver(), "ascend_ringtone", 1) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                customSwitchPreference.setChecked(z);
                return;
            }
            getPreferenceScreen().removePreference(this.mAscendRingtone);
        }
    }

    private void initDolbyMobile() {
        this.mDolbyMobileSettings = findPreference("dolby_mobile_settings");
        List<ResolveInfo> homes = getPackageManager().queryIntentActivities(new Intent("com.huawei.android.globaldolbyeffect.GlobalDolbyEffectActivity"), 0);
        if ((homes == null || homes.size() == 0) && this.mDolbyMobileSettings != null) {
            getPreferenceScreen().removePreference(this.mDolbyMobileSettings);
        }
    }

    protected CustomSwitchPreference findPreferenceAndSetListener(String key) {
        CustomSwitchPreference pref = (CustomSwitchPreference) findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
        }
        return pref;
    }

    private void initRingtone() {
        if (SystemProperties.getBoolean("ro.config.hide_phone_entry", false)) {
            removePreference("ringtone");
            removePreference("ringtone1");
            removePreference("ringtone2");
        } else if (Utils.isMultiSimEnabled() && SystemProperties.getBoolean("ro.dual.sim.phone", false)) {
            removePreference("ringtone");
            this.mRingtone1 = (DefaultRingtonePreference) findPreference("ringtone1");
            this.mRingtone2 = (DefaultRingtonePreference) findPreference("ringtone2");
            if (this.mRingtone1 != null) {
                this.mRingtone1.setRingtoneType(1);
                this.mRingtone1.setFragment(this);
            }
            if (this.mRingtone2 != null) {
                this.mRingtone2.setRingtoneType(8);
                this.mRingtone2.setFragment(this);
            }
        } else {
            removePreference("ringtone1");
            removePreference("ringtone2");
        }
    }

    private void initCard2VibrateWhenRinging() {
        boolean z = true;
        Vibrator vibrator = (Vibrator) getSystemService("vibrator");
        if (Utils.isVoiceCapable(getActivity()) && vibrator != null && vibrator.hasVibrator()) {
            if (Utils.isMultiSimEnabled()) {
                if (this.mVibrateWhenRinging != null) {
                    this.mVibrateWhenRinging.setTitle(2131627768);
                }
                CustomSwitchPreference vibrateWhenRinging2 = findPreferenceAndSetListener("vibrate_when_ringing_sim2");
                if (vibrateWhenRinging2 != null) {
                    if (System.getInt(getContentResolver(), "vibrate_when_ringing2", 1) != 1) {
                        z = false;
                    }
                    vibrateWhenRinging2.setChecked(z);
                }
            } else {
                removePreference("vibrate_when_ringing_sim2");
            }
            return;
        }
        removePreference("vibrate_when_ringing_sim2");
    }

    private void initShotSound() {
        this.mHwCustSoundSettingsHwBase = (HwCustSoundSettingsHwBase) HwCustUtils.createObj(HwCustSoundSettingsHwBase.class, new Object[]{getContext()});
        this.mShotSounds = (CustomSwitchPreference) findPreference("shot_sounds");
        if (this.mShotSounds == null) {
            return;
        }
        if (isRemoveShotSound(this.mHwCustSoundSettingsHwBase, getContext())) {
            removePreference("shot_sounds");
            return;
        }
        this.mShotSounds.setOnPreferenceChangeListener(this);
        this.mShotSounds.setChecked(System.getInt(getContentResolver(), "play_camera_sound", 1) == 1);
    }

    protected static boolean isRemoveShotSound(HwCustSoundSettingsHwBase custObj, Context context) {
        HwCustSoundSettingsHwBase custSoundSettingsBase = custObj;
        if (custObj == null) {
            custSoundSettingsBase = (HwCustSoundSettingsHwBase) HwCustUtils.createObj(HwCustSoundSettingsHwBase.class, new Object[]{context});
        }
        return custSoundSettingsBase != null && custSoundSettingsBase.isRemoveShotSounds();
    }

    public void onResume() {
        super.onResume();
        updateSilentMode();
        updateDtmfTone();
        updateCheckboxState();
        getActivity().registerReceiver(this.mSilentModeReceiver, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
        getActivity().registerReceiver(this.mDTSReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mSilentModeReceiver);
        getActivity().unregisterReceiver(this.mDTSReceiver);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mRingtone1) {
            this.mRingtoneType = this.mRingtone1.getRingtoneType();
        } else if (preference == this.mRingtone2) {
            this.mRingtoneType = this.mRingtone2.getRingtoneType();
        } else if (preference == this.mRingtonePreference) {
            this.mRingtoneType = this.mRingtonePreference.getRingtoneType();
        } else if (preference == this.mNotificationPreference) {
            this.mRingtoneType = this.mNotificationPreference.getRingtoneType();
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        int i = 0;
        String key = preference.getKey();
        ItemUseStat.getInstance().handleOnPreferenceChange(getActivity(), preference, objValue);
        Boolean value;
        if ("vibrate_when_ringing".equals(key)) {
            int i2;
            value = (Boolean) objValue;
            ContentResolver contentResolver = getContentResolver();
            String str = "vibrate_when_ringing";
            if (value.booleanValue()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            System.putInt(contentResolver, str, i2);
        } else if ("vibrate_when_ringing_sim2".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "vibrate_when_ringing2";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("vibirate_when_silent".equals(key)) {
            enableVibirateWhenSilent(((Boolean) objValue).booleanValue());
        } else if ("dtmf_tone".equals(key)) {
            int value2 = Integer.parseInt((String) objValue);
            if (value2 == 0) {
                System.putInt(getContentResolver(), "dtmf_tone", 0);
            } else {
                System.putInt(getContentResolver(), "dtmf_tone", 1);
                System.putInt(getContentResolver(), "dialpad_touch_tone_type", value2);
            }
            updateDtmfTone(value2);
        } else if ("sound_effects".equals(key)) {
            value = (Boolean) objValue;
            if (value.booleanValue()) {
                this.mAudioManager.loadSoundEffects();
            } else {
                this.mAudioManager.unloadSoundEffects();
            }
            r4 = getContentResolver();
            r7 = "sound_effects_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("lock_sounds".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "lockscreen_sounds_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("haptic_feedback".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "haptic_feedback_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("physical_navi_haptic_feedback".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "physic_navi_haptic_feedback_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("dock_sounds".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "dock_sounds_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            Global.putInt(r4, r7, i);
        } else if ("ascend_ringtone".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "ascend_ringtone";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("dock_audio_media_enabled".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "dock_audio_media_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            Global.putInt(r4, r7, i);
        } else if ("shot_sounds".equals(key)) {
            value = (Boolean) objValue;
            r4 = getContentResolver();
            r7 = "play_camera_sound";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r4, r7, i);
        } else if ("system_boot_shut_sound".equals(key)) {
            enableSystemBootShutSound(((Boolean) objValue).booleanValue());
        } else if ("hw_dts".equals(key)) {
            value = (Boolean) objValue;
            if (value.booleanValue()) {
                this.mAudioManager.setParameters("srs_cfg:trumedia_enable=1");
            } else {
                this.mAudioManager.setParameters("srs_cfg:trumedia_enable=0");
            }
            r4 = getContentResolver();
            r7 = "dts_mode";
            if (value.booleanValue()) {
                i = 3;
            }
            Secure.putInt(r4, r7, i);
        } else if ("hw_hpx".equals(key)) {
            if (((Boolean) objValue).booleanValue()) {
                i = 1;
            }
            setHeadphoneXEnabled(i);
        } else if ("hw_stereo".equals(key)) {
            value = (Boolean) objValue;
            if (value.booleanValue()) {
                this.mAudioManager.setParameters("stereo_landscape_portrait_enable=1");
                setRotationForAudioSystem(this.mAudioManager, ((WindowManager) getSystemService("window")).getDefaultDisplay().getRotation());
            } else {
                this.mAudioManager.setParameters("rotation=0");
                this.mAudioManager.setParameters("stereo_landscape_portrait_enable=0");
            }
            r4 = getContentResolver();
            r7 = "stereo_landscape_portrait";
            if (value.booleanValue()) {
                i = 1;
            }
            Secure.putInt(r4, r7, i);
        }
        return true;
    }

    private void setRotationForAudioSystem(AudioManager audioManager, int rotation) {
        switch (rotation) {
            case 0:
                audioManager.setParameters("rotation=0");
                return;
            case 1:
                audioManager.setParameters("rotation=90");
                return;
            case 2:
                audioManager.setParameters("rotation=180");
                return;
            case 3:
                audioManager.setParameters("rotation=270");
                return;
            default:
                Log.e("soudsettings", "Unknown device rotation");
                return;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && data != null) {
            if (!(requestCode == 12 || requestCode == 13)) {
                if (requestCode == 20) {
                }
            }
            Uri uri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            Log.d("soudsettings", "SoundSettingsHwBase-->onActivityResult()-->uri = " + uri);
            if (uri == null) {
                uri = data.getData();
            }
            if (1 == this.mRingtoneType) {
                try {
                    if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                        this.mRingtone1.changeRingtone(uri);
                    } else {
                        this.mRingtonePreference.changeRingtone(uri);
                    }
                } catch (Exception e) {
                    this.mRingtonePreference.changeRingtone(uri);
                    e.printStackTrace();
                    if (uri != null) {
                        HashMap<Short, Object> map = new HashMap();
                        map.put(Short.valueOf((short) 0), Integer.valueOf(resultCode));
                        map.put(Short.valueOf((short) 1), uri.toString());
                        RadarReporter.reportRadar(907018005, map);
                    }
                }
            } else if (8 == this.mRingtoneType) {
                this.mRingtone2.changeRingtone(uri);
            } else if (2 == this.mRingtoneType) {
                this.mNotificationPreference.changeRingtone(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        RingerVolumePreferenceDialogActivity.setSilent(null);
        super.onDestroy();
    }

    protected void updateCheckboxState() {
        boolean z;
        boolean z2 = true;
        ContentResolver resolver = getContentResolver();
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        CustomSwitchPreference customSwitchPreference = this.mSoundEffects;
        if (System.getInt(resolver, "sound_effects_enabled", 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        customSwitchPreference = this.mHapticFeedback;
        if (System.getInt(resolver, "haptic_feedback_enabled", 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        updatePhysicNaviHapticPref(resolver);
        customSwitchPreference = this.mLockSounds;
        if (System.getInt(resolver, "lockscreen_sounds_enabled", 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        customSwitchPreference = this.hwDTSPreference;
        if (Secure.getInt(resolver, "dts_mode", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        updateHPXPreference();
        customSwitchPreference = this.hwStereoPreference;
        if (Secure.getInt(resolver, "stereo_landscape_portrait", 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        if (2 == activePhoneType) {
            ListPreference emergencyTonePreference = (ListPreference) findPreference("emergency_tone");
            if (emergencyTonePreference != null) {
                emergencyTonePreference.setValue(String.valueOf(Global.getInt(resolver, "emergency_tone", 0)));
            }
        }
        if (this.mAscendRingtone != null && isRingtoneAscending) {
            CustomSwitchPreference customSwitchPreference2 = this.mAscendRingtone;
            if (System.getInt(resolver, "ascend_ringtone", 1) == 0) {
                z2 = false;
            }
            customSwitchPreference2.setChecked(z2);
        }
    }

    public void updateDtmfTone() {
        ContentResolver resolver = getContentResolver();
        int dtmfValue = System.getInt(resolver, "dtmf_tone", 1);
        int toneType = System.getInt(resolver, "dialpad_touch_tone_type", 1);
        if (dtmfValue > 0) {
            dtmfValue = toneType;
        }
        updateDtmfTone(dtmfValue);
    }

    public void updateDtmfTone(int dtmfValue) {
        if (this.mDtmfTone != null) {
            this.mDtmfTone.setSummary(getResources().getStringArray(2131361970)[dtmfValue]);
            this.mDtmfTone.setValue(String.valueOf(dtmfValue));
        }
    }

    protected void initSilentMode() {
        this.mVolumeSilent = (CustomSwitchPreference) findPreference("ring_volume_silent");
        this.mVolumeSilent.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(SoundSettingsHwBase.this.getActivity(), preference, newValue);
                SoundSettingsHwBase.this.enableRingVolumeSilent(((Boolean) newValue).booleanValue());
                return true;
            }
        });
        this.mVibirateWhenSilent = (CustomSwitchPreference) findPreference("vibirate_when_silent");
        this.mVibirateWhenSilent.setOnPreferenceChangeListener(this);
    }

    protected void updateSilentMode() {
        if (getActivity() != null) {
            this.mVolumeSilent.setChecked(isRingVolumeSilent());
            this.mVibirateWhenSilent.setChecked(isVibirateWhenSilent());
            updateSystemBootShutSoundSwitchState();
        }
    }

    protected void enableRingVolumeSilent(boolean enable) {
        if (enable) {
            changeVibrateMode(isStoredVibirateWhenSilent());
        } else {
            setRingerMode(this.mAudioManager, 2);
        }
    }

    protected boolean isRingVolumeSilent() {
        return getRingerMode(this.mAudioManager) != 2;
    }

    protected void changeVibrateMode(boolean enable) {
        if (enable) {
            setRingerMode(this.mAudioManager, 1);
        } else {
            setRingerMode(this.mAudioManager, 0);
        }
    }

    protected void enableVibirateWhenSilent(boolean enable) {
        Global.putInt(getContentResolver(), "vibirate_when_silent", enable ? 1 : 0);
        if (isRingVolumeSilent()) {
            changeVibrateMode(enable);
        }
    }

    protected boolean isStoredVibirateWhenSilent() {
        return Global.getInt(getContentResolver(), "vibirate_when_silent", 0) == 1;
    }

    protected boolean isVibirateWhenSilent() {
        int i = 1;
        boolean enable = isStoredVibirateWhenSilent();
        if (isRingVolumeSilent()) {
            enable = getRingerMode(this.mAudioManager) == 1;
            ContentResolver contentResolver = getContentResolver();
            String str = "vibirate_when_silent";
            if (!enable) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
        return enable;
    }

    public static final void setRingerMode(AudioManager audioManager, int ringerMode) {
        if (VERSION.SDK_INT >= 22) {
            try {
                AudioManager.class.getMethod("setRingerModeInternal", new Class[]{Integer.TYPE}).invoke(audioManager, new Object[]{Integer.valueOf(ringerMode)});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
            return;
        }
        audioManager.setRingerMode(ringerMode);
    }

    public static final int getRingerMode(AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        if (VERSION.SDK_INT >= 22) {
            try {
                Object mode = AudioManager.class.getMethod("getRingerModeInternal", new Class[0]).invoke(audioManager, new Object[0]);
                if (mode != null) {
                    ringerMode = Integer.parseInt(mode.toString());
                }
                return ringerMode;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            }
        }
        return ringerMode;
    }

    private void initSystemBootShutSoundSwitch() {
        this.mHwCustSoundSettingsHwBase = (HwCustSoundSettingsHwBase) HwCustUtils.createObj(HwCustSoundSettingsHwBase.class, new Object[0]);
        this.mSystemBootShutSound = findPreferenceAndSetListener("system_boot_shut_sound");
        if (!isSystemBootShutSoundCapable()) {
            getPreferenceScreen().removePreference(this.mSystemBootShutSound);
        } else if (this.mHwCustSoundSettingsHwBase == null || !this.mHwCustSoundSettingsHwBase.isNotShowPowerOnToneOption()) {
            updateSystemBootShutSoundSwitchState();
        } else {
            getPreferenceScreen().removePreference(this.mSystemBootShutSound);
        }
    }

    private void updateSystemBootShutSoundSwitchState() {
        boolean z = false;
        if (this.mSystemBootShutSound != null) {
            boolean z2;
            CustomSwitchPreference customSwitchPreference = this.mSystemBootShutSound;
            if (isRingVolumeSilent()) {
                z2 = false;
            } else {
                z2 = true;
            }
            customSwitchPreference.setEnabled(z2);
            this.mSystemBootShutSound.setOnPreferenceChangeListener(null);
            CustomSwitchPreference customSwitchPreference2 = this.mSystemBootShutSound;
            if (isSystemBootShutSoundEnable() && !isRingVolumeSilent()) {
                z = true;
            }
            customSwitchPreference2.setChecked(z);
            this.mSystemBootShutSound.setOnPreferenceChangeListener(this);
        }
    }

    private void enableSystemBootShutSound(boolean enable) {
        String str;
        Global.putInt(getContentResolver(), "system_boot_shut_sound_switch", enable ? 1 : 0);
        if (enable) {
            try {
                str = "open";
            } catch (Exception e) {
                e.printStackTrace();
                MLog.w("soudsettings", "bootOrShutSoundSwitchCapable api not supported");
                return;
            }
        }
        str = "close";
        BootanimEx.switchBootOrShutSound(str);
    }

    private boolean isSystemBootShutSoundEnable() {
        return Global.getInt(getContentResolver(), "system_boot_shut_sound_switch", 1) == 1;
    }

    protected static boolean isSystemBootShutSoundCapable() {
        boolean hasSound = false;
        try {
            hasSound = BootanimEx.isBootOrShutdownSoundCapable();
        } catch (Exception e) {
            e.printStackTrace();
            MLog.w("soudsettings", "isBootOrShutdownSoundCapable api not supported");
        }
        return hasSound;
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void initHwHPX() {
        if (Utils.isSupportHpx()) {
            this.hwHPXPreference = (CustomSwitchPreference) findPreference("hw_hpx");
            if (this.hwHPXPreference == null) {
                MLog.e("soudsettings", "initHwHPX, not find hw_hpx preference, not continue");
                removePreference("hw_hpx");
                return;
            }
            this.hwHPXPreference.setPersistent(false);
            this.hwHPXPreference.setOnPreferenceChangeListener(this);
            this.hwHPXPreference.setChecked(getHeadphoneXEnabled());
            this.hwHPXPreference.setEnabled(((AudioManager) getActivity().getSystemService("audio")).isWiredHeadsetOn());
            removePreference("hw_dts");
            return;
        }
        MLog.d("soudsettings", "initHwHPX, not support HPX, not continue");
        removePreference("hw_hpx");
    }

    private void updateHPXPreference() {
        if (this.hwHPXPreference == null) {
            MLog.d("soudsettings", "updateCustomDTSPreference, hwHPXPreference is null, not continue");
        } else {
            this.hwHPXPreference.setChecked(getHeadphoneXEnabled());
        }
    }

    private boolean getHeadphoneXEnabled() {
        try {
            MLog.d("soudsettings", "getHeadphoneXEnabled, start");
            Class<?> classHpx = Class.forName("com.dts.hpx.lite.sdk.thin.HpxLiteSdkThin");
            int rst = ((Integer) classHpx.getMethod("getHpxEnabled", new Class[0]).invoke(classHpx.newInstance(), (Object[]) null)).intValue();
            MLog.d("soudsettings", "getHeadphoneXEnabled, end, rst=" + rst);
            return rst == 1;
        } catch (IllegalAccessException e) {
            MLog.e("soudsettings", "getHeadphoneXEnabled, IllegalAccessException, e=" + e);
            return false;
        } catch (ClassNotFoundException e2) {
            MLog.e("soudsettings", "getHeadphoneXEnabled, ClassNotFoundException, e=" + e2);
            return false;
        } catch (NoSuchMethodException e3) {
            MLog.e("soudsettings", "getHeadphoneXEnabled, NoSuchMethodException, e=" + e3);
            return false;
        } catch (Exception e4) {
            MLog.e("soudsettings", "getHeadphoneXEnabled, Exception, e=" + e4);
            return false;
        }
    }

    private boolean setHeadphoneXEnabled(int enable) {
        boolean z = true;
        try {
            MLog.d("soudsettings", "setHeadphoneXEnabled, start");
            Class<?> classHpx = Class.forName("com.dts.hpx.lite.sdk.thin.HpxLiteSdkThin");
            int rst = ((Integer) classHpx.getMethod("setHpxEnabled", new Class[]{Integer.TYPE}).invoke(classHpx.newInstance(), new Object[]{Integer.valueOf(enable)})).intValue();
            MLog.d("soudsettings", "setHeadphoneXEnabled, end, rst=" + rst);
            if (rst != 0) {
                z = false;
            }
            return z;
        } catch (IllegalAccessException e) {
            MLog.e("soudsettings", "setHeadphoneXEnabled, IllegalAccessException, e=" + e);
            return false;
        } catch (ClassNotFoundException e2) {
            MLog.e("soudsettings", "setHeadphoneXEnabled, ClassNotFoundException, e=" + e2);
            return false;
        } catch (NoSuchMethodException e3) {
            MLog.e("soudsettings", "setHeadphoneXEnabled, NoSuchMethodException, e=" + e3);
            return false;
        } catch (Exception e4) {
            MLog.e("soudsettings", "setHeadphoneXEnabled, Exception, e=" + e4);
            return false;
        }
    }

    private void initHapticFeedBackPref() {
        Preference pref = findPreference("physical_navi_haptic_feedback");
        if (pref == null) {
            Log.e("soudsettings", "initHapticFeedBackPref physical_navi_haptic_feedback is null!");
        } else if (!NaviUtils.isFrontFingerNaviEnabled()) {
            Log.i("soudsettings", "initHapticFeedBackPref not front-finger device.");
            PreferenceScreen ps = getPreferenceScreen();
            if (ps != null) {
                ps.removePreference(pref);
                Log.i("soudsettings", "initHapticFeedBackPref not front-finger device, physical_navi_haptic_feedback removed!");
            }
        } else if (pref instanceof CustomSwitchPreference) {
            this.mPhysicNaviHapticFeedback = (CustomSwitchPreference) pref;
            this.mPhysicNaviHapticFeedback.setOnPreferenceChangeListener(this);
            this.mPhysicNaviHapticFeedback.setPersistent(false);
            if (this.mHapticFeedback != null) {
                if (NaviUtils.getTrikeyState() != 0) {
                    this.mHapticFeedback.setSummary(2131628908);
                } else if (Utils.isWifiOnly(getActivity())) {
                    this.mHapticFeedback.setSummary(2131627545);
                } else {
                    this.mHapticFeedback.setSummary(2131628906);
                }
            }
        } else {
            Log.w("soudsettings", "initHapticFeedBackPref strange, physical_navi_haptic_feedback type not matched!");
        }
    }

    private void updatePhysicNaviHapticPref(ContentResolver resolver) {
        boolean z = true;
        if (this.mPhysicNaviHapticFeedback == null) {
            Log.d("soudsettings", "PhysicNaviHapticFeedback is null!");
            return;
        }
        boolean z2;
        CustomSwitchPreference customSwitchPreference = this.mPhysicNaviHapticFeedback;
        if (System.getInt(resolver, "physic_navi_haptic_feedback_enabled", NaviUtils.getPhysicNaviHapticDefault()) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        customSwitchPreference.setChecked(z2);
        if (NaviUtils.getTrikeyState() == 0) {
            int currentType = System.getInt(resolver, "enable_navbar", NaviUtils.getEnableNaviDefaultValue());
            CustomSwitchPreference customSwitchPreference2 = this.mPhysicNaviHapticFeedback;
            if (currentType != 0) {
                z = false;
            }
            customSwitchPreference2.setEnabled(z);
        }
    }
}
