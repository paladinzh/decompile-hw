package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.bluetooth.DockEventReceiver;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundSettings extends SoundSettingsHwBase implements OnPreferenceChangeListener, Indexable {
    private static final String[] NEED_VOICE_CAPABILITY = new String[]{"ringtone", "dtmf_tone", "category_calls_and_notification", "emergency_tone", "vibrate_when_ringing", "ringtone1", "ringtone2", "vibrate_when_ringing_sim2"};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230901;
            result.add(sir);
            return SoundSettings.mHwCustSearchIndexProvider.addSoundXmlResourcesToIndex(context, result);
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            if (Utils.isMultiSimEnabled()) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627768);
                data.screenTitle = res.getString(2131625101);
                result.add(data);
            }
            return SoundSettings.mHwCustSearchIndexProvider.addSoundRawDataToIndex(context, result, res);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (context == null) {
                return keys;
            }
            if (!SoundSettings.isRingtoneAscending) {
                keys.add("ascend_ringtone");
            }
            List<ResolveInfo> homes = context.getPackageManager().queryIntentActivities(new Intent("com.huawei.android.globaldolbyeffect.GlobalDolbyEffectActivity"), 0);
            if (homes == null || homes.size() == 0) {
                keys.add("dolby_mobile_settings");
            }
            if (SystemProperties.getBoolean("ro.config.hide_phone_entry", false)) {
                keys.add("ringtone");
                keys.add("ringtone1");
                keys.add("ringtone2");
            } else if (Utils.isMultiSimEnabled() && SystemProperties.getBoolean("ro.dual.sim.phone", false)) {
                keys.add("ringtone");
            } else {
                keys.add("ringtone1");
                keys.add("ringtone2");
            }
            Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
            if (!Utils.isVoiceCapable(context) || vibrator == null || !vibrator.hasVibrator()) {
                keys.add("vibrate_when_ringing_sim2");
                if (vibrator == null || !vibrator.hasVibrator()) {
                    keys.add("vibrate_when_ringing");
                    keys.add("haptic_feedback");
                }
            }
            if (!NaviUtils.isFrontFingerNaviEnabled()) {
                keys.add("physical_navi_haptic_feedback");
            }
            if (!Utils.isMultiSimEnabled()) {
                keys.add("vibrate_when_ringing_sim2");
            }
            if (TelephonyManager.getDefault().getCurrentPhoneType() != 2) {
                keys.add("emergency_tone");
            }
            if (context.getResources().getBoolean(17956995)) {
                keys.add("ring_volume");
            }
            if (context.getPackageManager().queryIntentActivities(new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL"), 512).size() <= 2) {
                keys.add("musicfx");
            }
            if (!Utils.isVoiceCapable(context) || Utils.isWifiOnly(context)) {
                for (String prefKey : SoundSettings.NEED_VOICE_CAPABILITY) {
                    keys.add(prefKey);
                }
            }
            if (!context.getResources().getBoolean(2131492871)) {
                keys.add("dock_category");
                keys.add("dock_audio");
                keys.add("dock_sounds");
            }
            Intent earPhoneIntent = new Intent("android.settings.DTS_HEADPHONEX_SETTINGS");
            if (SoundSettings.IS_SUPPORT_HW_DTS || !Utils.hasIntentActivity(context.getPackageManager(), earPhoneIntent)) {
                keys.add("earphone_effect");
            }
            if (!SoundSettings.IS_SUPPORT_HW_DTS) {
                keys.add("hw_dts");
            }
            if (!Utils.isSupportHpx()) {
                keys.add("hw_hpx");
            } else if (!keys.contains("hw_dts")) {
                keys.add("hw_dts");
            }
            if (!Utils.hasIntentActivity(context.getPackageManager(), "com.huawei.android.karaokeeffect.KaraokeEffectSettingsActivity")) {
                keys.add("karaoke_effect_settings_category");
                keys.add("karaoke_effect_settings");
            }
            if (!SoundSettingsHwBase.isSystemBootShutSoundCapable() || (SoundSettings.mHwCustSoundSettingsHwBase != null && SoundSettings.mHwCustSoundSettingsHwBase.isNotShowPowerOnToneOption())) {
                keys.add("system_boot_shut_sound");
            }
            if (!SoundSettings.SPK_RCV_STEREO_SUPPORT) {
                keys.add("hw_stereo");
            }
            if (SoundSettingsHwBase.isRemoveShotSound(null, context)) {
                keys.add("shot_sounds");
            }
            UserManager um = (UserManager) context.getSystemService("user");
            Intent cellBroadcastSettingsIntent = new Intent("android.intent.action.MAIN");
            cellBroadcastSettingsIntent.setClassName("com.android.cellbroadcastreceiver", "com.android.cellbroadcastreceiver.ui.CellBroadcastSettings");
            if (!(um.isAdminUser() && Utils.hasIntentActivity(context.getPackageManager(), cellBroadcastSettingsIntent))) {
                keys.add("cell_broadcast_settings");
            }
            return keys;
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private static HwCustSoundSettingsHwBase mHwCustSoundSettingsHwBase = ((HwCustSoundSettingsHwBase) HwCustUtils.createObj(HwCustSoundSettingsHwBase.class, new Object[0]));
    private Preference mDockAudioSettings;
    private Intent mDockIntent;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SoundSettings.this.mRingtonePreference.setSummary((CharSequence) msg.obj);
                    break;
                case 2:
                    SoundSettings.this.mNotificationPreference.setSummary((CharSequence) msg.obj);
                    break;
            }
            SoundSettings.this.handleMessageExt(msg);
        }
    };
    private HwCustSoundSettings mHwCustSoundSettings;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.DOCK_EVENT".equals(intent.getAction())) {
                SoundSettings.this.handleDockChange(intent);
            }
        }
    };
    private Runnable mRingtoneLookupRunnable;
    private PreferenceGroup mSoundSettings;
    private UserManager mUserManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        int activePhoneType = TelephonyManager.getDefault().getCurrentPhoneType();
        this.mAudioManager = (AudioManager) getSystemService("audio");
        addPreferencesFromResource(2131230901);
        if (2 != activePhoneType) {
            removePreference("emergency_tone");
        }
        if (getResources().getBoolean(17956995)) {
            removePreference("ring_volume");
        }
        this.mVibrateWhenRinging = findPreferenceAndSetListener("vibrate_when_ringing");
        this.mVibrateWhenRinging.setPersistent(false);
        this.mVibrateWhenRinging.setChecked(System.getInt(resolver, "vibrate_when_ringing", 0) != 0);
        this.mDtmfTone = (ListPreference) findPreference("dtmf_tone");
        this.mDtmfTone.setOnPreferenceChangeListener(this);
        this.mSoundEffects = findPreferenceAndSetListener("sound_effects");
        this.mSoundEffects.setPersistent(false);
        this.mSoundEffects.setChecked(System.getInt(resolver, "sound_effects_enabled", 1) != 0);
        this.mHapticFeedback = findPreferenceAndSetListener("haptic_feedback");
        this.mHapticFeedback.setPersistent(false);
        this.mHapticFeedback.setChecked(System.getInt(resolver, "haptic_feedback_enabled", 1) != 0);
        this.mLockSounds = findPreferenceAndSetListener("lock_sounds");
        this.mLockSounds.setPersistent(false);
        this.mLockSounds.setChecked(System.getInt(resolver, "lockscreen_sounds_enabled", 1) != 0);
        this.mRingtonePreference = (DefaultRingtonePreference) findPreference("ringtone");
        this.mRingtonePreference.setFragment(this);
        this.mNotificationPreference = (DefaultRingtonePreference) findPreference("notification_sound");
        this.mNotificationPreference.setFragment(this);
        Vibrator vibrator = (Vibrator) getSystemService("vibrator");
        if (vibrator == null || !vibrator.hasVibrator()) {
            removePreference("vibrate_when_ringing");
            removePreference("haptic_feedback");
        }
        if (2 == activePhoneType) {
            ListPreference emergencyTonePreference = (ListPreference) findPreference("emergency_tone");
            emergencyTonePreference.setValue(String.valueOf(Global.getInt(resolver, "emergency_tone", 0)));
            emergencyTonePreference.setOnPreferenceChangeListener(this);
            emergencyTonePreference.setSummary(emergencyTonePreference.getEntry());
        }
        this.mSoundSettings = (PreferenceGroup) findPreference("sound_settings");
        this.mMusicFx = this.mSoundSettings.findPreference("musicfx");
        if (getPackageManager().queryIntentActivities(new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL"), 512).size() <= 2) {
            this.mSoundSettings.removePreference(this.mMusicFx);
        }
        if (!Utils.isVoiceCapable(getActivity()) || Utils.isWifiOnly(getActivity())) {
            for (String prefKey : NEED_VOICE_CAPABILITY) {
                Preference pref = findPreference(prefKey);
                if (pref != null) {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        }
        init(savedInstanceState);
        this.mRingtoneLookupRunnable = new Runnable() {
            public void run() {
                if (SoundSettings.this.mRingtonePreference != null) {
                    SoundSettings.this.updateRingtoneName(1, SoundSettings.this.mRingtonePreference, 1);
                }
                if (SoundSettings.this.mNotificationPreference != null) {
                    SoundSettings.this.updateRingtoneName(2, SoundSettings.this.mNotificationPreference, 2);
                }
                SoundSettings.this.ringtoneLookupExt();
            }
        };
        initDockSettings();
        boolean isCellBroadcastAppLinkEnabled = false;
        Preference cellBroadcastSettingsPrefs = findPreference("cell_broadcast_settings");
        if (cellBroadcastSettingsPrefs != null) {
            isCellBroadcastAppLinkEnabled = Utils.hasIntentActivity(getPackageManager(), cellBroadcastSettingsPrefs.getIntent());
        }
        this.mUserManager = UserManager.get(getContext());
        if (!(this.mUserManager.isAdminUser() && r4 && !RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_config_cell_broadcasts", UserHandle.myUserId()))) {
            removePreference("cell_broadcast_settings");
        }
        this.mHwCustSoundSettings = (HwCustSoundSettings) HwCustUtils.createObj(HwCustSoundSettings.class, new Object[]{this});
        if (this.mHwCustSoundSettings != null) {
            this.mHwCustSoundSettings.custOnCreate();
            this.mHwCustSoundSettings.updateCustPreference(getActivity());
        }
        Index.getInstance(getActivity()).updateFromClassNameResource(SoundSettings.class.getName(), true, true);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        lookupRingtoneNames();
        getActivity().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
        RestrictedPreference broadcastSettingsPref = (RestrictedPreference) findPreference("cell_broadcast_settings");
        if (broadcastSettingsPref != null) {
            broadcastSettingsPref.checkRestrictionAndSetDisabled("no_config_cell_broadcasts");
        }
        this.mHwCustSoundSettings.resume();
        this.mHwCustSoundSettings.updateCheckboxState();
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        ItemUseStat.getInstance().cacheData(getActivity());
        this.mHwCustSoundSettings.pause();
    }

    private boolean isUriAvailabe(Context context, Uri ringtoneUri) {
        boolean isFind = false;
        if (ringtoneUri == null) {
            return false;
        }
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(ringtoneUri, "r");
            if (!(assetFileDescriptor == null || assetFileDescriptor.getLength() == -1)) {
                isFind = true;
            }
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IllegalArgumentException e2) {
            Log.e("SoundSettings", "Failed to open ringtone" + ringtoneUri);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Exception e4) {
            Log.e("SoundSettings", "Failed to open ringtone" + ringtoneUri);
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        }
        return isFind;
    }

    protected void updateRingtoneName(int type, Preference preference, int msg) {
        if (preference != null) {
            Context context = getActivity();
            if (context != null) {
                Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
                CharSequence summary = context.getString(2131627332);
                if (ringtoneUri == null) {
                    summary = context.getString(2131627331);
                } else if (isUriAvailabe(context, ringtoneUri)) {
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(ringtoneUri, new String[]{"title"}, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                summary = cursor.getString(0);
                            }
                            cursor.close();
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (SQLiteException e) {
                        Log.d("SoundSettings", "Unknown title for the ringtone");
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (IllegalArgumentException e2) {
                        e2.printStackTrace();
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(msg, summary));
            }
        }
    }

    private void lookupRingtoneNames() {
        new Thread(this.mRingtoneLookupRunnable).start();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        int i = 1;
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if (preference == this.mMusicFx) {
            return false;
        }
        if (preference == this.mDockAudioSettings) {
            int dockState;
            if (this.mDockIntent != null) {
                dockState = this.mDockIntent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            } else {
                dockState = 0;
            }
            if (dockState == 0) {
                showDialog(1);
            } else {
                boolean isBluetooth;
                if (this.mDockIntent.getParcelableExtra("android.bluetooth.device.extra.DEVICE") != null) {
                    isBluetooth = true;
                } else {
                    isBluetooth = false;
                }
                if (isBluetooth) {
                    Intent i2 = new Intent(this.mDockIntent);
                    i2.setAction("com.android.settings.bluetooth.action.DOCK_SHOW_UI");
                    i2.setClass(getActivity(), DockEventReceiver.class);
                    getActivity().sendBroadcast(i2);
                } else {
                    boolean z;
                    PreferenceScreen ps = this.mDockAudioSettings;
                    Bundle extras = ps.getExtras();
                    String str = "checked";
                    if (Global.getInt(getContentResolver(), "dock_audio_media_enabled", 0) != 1) {
                        z = false;
                    }
                    extras.putBoolean(str, z);
                    super.onPreferenceTreeClick(ps);
                }
            }
        } else if (preference == this.mDockSounds) {
            r7 = getContentResolver();
            r8 = "dock_sounds_enabled";
            if (!this.mDockSounds.isChecked()) {
                i = 0;
            }
            Global.putInt(r7, r8, i);
        } else if (preference == this.mDockAudioMediaEnabled) {
            r7 = getContentResolver();
            r8 = "dock_audio_media_enabled";
            if (!this.mDockAudioMediaEnabled.isChecked()) {
                i = 0;
            }
            Global.putInt(r7, r8, i);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (!"emergency_tone".equals(preference.getKey())) {
            return super.onPreferenceChange(preference, objValue);
        }
        try {
            Global.putInt(getContentResolver(), "emergency_tone", Integer.parseInt((String) objValue));
        } catch (NumberFormatException e) {
            Log.e("SoundSettings", "could not persist emergency tone setting", e);
        }
        ListPreference etf = (ListPreference) preference;
        ItemUseStat.getInstance().handleOnPreferenceChange(getActivity(), preference, objValue);
        etf.setValue(objValue.toString());
        etf.setSummary(etf.getEntry());
        return true;
    }

    protected int getHelpResource() {
        return 2131626542;
    }

    private boolean needsDockSettings() {
        return getResources().getBoolean(2131492871);
    }

    private void initDockSettings() {
        boolean z = true;
        ContentResolver resolver = getContentResolver();
        if (needsDockSettings()) {
            this.mDockSounds = findPreferenceAndSetListener("dock_sounds");
            this.mDockSounds.setPersistent(false);
            CustomSwitchPreference customSwitchPreference = this.mDockSounds;
            if (Global.getInt(resolver, "dock_sounds_enabled", 0) == 0) {
                z = false;
            }
            customSwitchPreference.setChecked(z);
            this.mDockAudioSettings = findPreference("dock_audio");
            this.mDockAudioSettings.setEnabled(false);
            return;
        }
        removePreference("dock_category");
        removePreference("dock_audio");
        removePreference("dock_sounds");
        Global.putInt(resolver, "dock_audio_media_enabled", 1);
    }

    private void handleDockChange(Intent intent) {
        if (this.mDockAudioSettings != null) {
            int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
            boolean isBluetooth = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE") != null;
            this.mDockIntent = intent;
            if (dockState != 0) {
                try {
                    removeDialog(1);
                } catch (IllegalArgumentException e) {
                }
                if (isBluetooth) {
                    this.mDockAudioSettings.setEnabled(true);
                    return;
                } else if (dockState == 3) {
                    boolean z;
                    ContentResolver resolver = getContentResolver();
                    this.mDockAudioSettings.setEnabled(true);
                    if (Global.getInt(resolver, "dock_audio_media_enabled", -1) == -1) {
                        Global.putInt(resolver, "dock_audio_media_enabled", 0);
                    }
                    this.mDockAudioMediaEnabled = (CustomSwitchPreference) findPreference("dock_audio_media_enabled");
                    this.mDockAudioMediaEnabled.setOnPreferenceChangeListener(this);
                    this.mDockAudioMediaEnabled.setPersistent(false);
                    CustomSwitchPreference customSwitchPreference = this.mDockAudioMediaEnabled;
                    if (Global.getInt(resolver, "dock_audio_media_enabled", 0) != 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    customSwitchPreference.setChecked(z);
                    return;
                } else {
                    this.mDockAudioSettings.setEnabled(false);
                    return;
                }
            }
            this.mDockAudioSettings.setEnabled(false);
        }
    }

    public Dialog onCreateDialog(int id) {
        if (id == 1) {
            return createUndockedMessage();
        }
        return null;
    }

    private Dialog createUndockedMessage() {
        Builder ab = new Builder(getActivity());
        ab.setTitle(2131625137);
        ab.setMessage(2131625138);
        ab.setPositiveButton(17039370, null);
        return ab.create();
    }
}
