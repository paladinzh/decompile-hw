package com.android.settings.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.preference.SeekBarVolumizer;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.RingtonePreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.notification.VolumeSeekBarPreference.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SoundSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String[] RESTRICTED_KEYS = new String[]{"media_volume", "alarm_volume", "ring_volume", "notification_volume", "zen_mode"};
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230901;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> rt = new ArrayList();
            if (Utils.isVoiceCapable(context)) {
                rt.add("notification_volume");
            } else {
                rt.add("ring_volume");
                rt.add("ringtone");
                rt.add("wifi_display");
                rt.add("vibrate_when_ringing");
            }
            PackageManager pm = context.getPackageManager();
            UserManager um = (UserManager) context.getSystemService("user");
            boolean isCellBroadcastAppLinkEnabled = context.getResources().getBoolean(17956981);
            if (isCellBroadcastAppLinkEnabled) {
                try {
                    if (pm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver") == 2) {
                        isCellBroadcastAppLinkEnabled = false;
                    }
                } catch (IllegalArgumentException e) {
                    isCellBroadcastAppLinkEnabled = false;
                }
            }
            if (!(um.isAdminUser() && r1)) {
                rt.add("cell_broadcast_settings");
            }
            return rt;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private Preference mAlarmRingtonePreference;
    private AudioManager mAudioManager;
    private Context mContext;
    private final H mHandler = new H();
    private final Runnable mLookupRingtoneNames = new Runnable() {
        public void run() {
            CharSequence summary;
            if (SoundSettings.this.mPhoneRingtonePreference != null) {
                summary = SoundSettings.updateRingtoneName(SoundSettings.this.mContext, 1);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(1, summary).sendToTarget();
                }
            }
            if (SoundSettings.this.mNotificationRingtonePreference != null) {
                summary = SoundSettings.updateRingtoneName(SoundSettings.this.mContext, 2);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(2, summary).sendToTarget();
                }
            }
            if (SoundSettings.this.mAlarmRingtonePreference != null) {
                summary = SoundSettings.updateRingtoneName(SoundSettings.this.mContext, 4);
                if (summary != null) {
                    SoundSettings.this.mHandler.obtainMessage(6, summary).sendToTarget();
                }
            }
        }
    };
    private Preference mNotificationRingtonePreference;
    private Preference mPhoneRingtonePreference;
    private PackageManager mPm;
    private final Receiver mReceiver = new Receiver();
    private RingtonePreference mRequestPreference;
    private VolumeSeekBarPreference mRingOrNotificationPreference;
    private int mRingerMode = -1;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private ComponentName mSuppressor;
    private UserManager mUserManager;
    private TwoStatePreference mVibrateWhenRinging;
    private Vibrator mVibrator;
    private boolean mVoiceCapable;
    private final VolumePreferenceCallback mVolumeCallback = new VolumePreferenceCallback();
    private final ArrayList<VolumeSeekBarPreference> mVolumePrefs = new ArrayList();

    private final class H extends Handler {
        private H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SoundSettings.this.mPhoneRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                case 2:
                    SoundSettings.this.mNotificationRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                case 3:
                    SoundSettings.this.mVolumeCallback.stopSample();
                    return;
                case 4:
                    SoundSettings.this.updateEffectsSuppressor();
                    return;
                case 5:
                    SoundSettings.this.updateRingerMode();
                    return;
                case 6:
                    SoundSettings.this.mAlarmRingtonePreference.setSummary((CharSequence) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    private class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        public void register(boolean register) {
            if (this.mRegistered != register) {
                if (register) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                    filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                    SoundSettings.this.mContext.registerReceiver(this, filter);
                } else {
                    SoundSettings.this.mContext.unregisterReceiver(this);
                }
                this.mRegistered = register;
            }
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(action)) {
                SoundSettings.this.mHandler.sendEmptyMessage(4);
            } else if ("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION".equals(action)) {
                SoundSettings.this.mHandler.sendEmptyMessage(5);
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri VIBRATE_WHEN_RINGING_URI = System.getUriFor("vibrate_when_ringing");

        public SettingsObserver() {
            super(SoundSettings.this.mHandler);
        }

        public void register(boolean register) {
            ContentResolver cr = SoundSettings.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.VIBRATE_WHEN_RINGING_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                SoundSettings.this.updateVibrateWhenRinging();
            }
        }
    }

    private static class SummaryProvider extends BroadcastReceiver implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final AudioManager mAudioManager = ((AudioManager) this.mContext.getSystemService("audio"));
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;
        private final int maxVolume = this.mAudioManager.getStreamMaxVolume(2);

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.media.VOLUME_CHANGED_ACTION");
                filter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
                filter.addAction("android.media.RINGER_MODE_CHANGED");
                filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
                filter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
                filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
                this.mContext.registerReceiver(this, filter);
                return;
            }
            this.mContext.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            String percent = NumberFormat.getPercentInstance().format(((double) this.mAudioManager.getStreamVolume(2)) / ((double) this.maxVolume));
            this.mSummaryLoader.setSummary(this, this.mContext.getString(2131626690, new Object[]{percent}));
        }
    }

    private final class VolumePreferenceCallback implements Callback {
        private SeekBarVolumizer mCurrent;

        private VolumePreferenceCallback() {
        }

        public void onSampleStarting(SeekBarVolumizer sbv) {
            if (!(this.mCurrent == null || this.mCurrent == sbv)) {
                this.mCurrent.stopSample();
            }
            this.mCurrent = sbv;
            if (this.mCurrent != null) {
                SoundSettings.this.mHandler.removeMessages(3);
                SoundSettings.this.mHandler.sendEmptyMessageDelayed(3, 2000);
            }
        }

        public void onStreamValueChanged(int stream, int progress) {
        }

        public void stopSample() {
            if (this.mCurrent != null) {
                this.mCurrent.stopSample();
            }
        }
    }

    protected int getMetricsCategory() {
        return 336;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        this.mPm = getPackageManager();
        this.mUserManager = UserManager.get(getContext());
        this.mVoiceCapable = Utils.isVoiceCapable(this.mContext);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mVibrator = (Vibrator) getActivity().getSystemService("vibrator");
        if (!(this.mVibrator == null || this.mVibrator.hasVibrator())) {
            this.mVibrator = null;
        }
        addPreferencesFromResource(2131230901);
        initVolumePreference("media_volume", 3, 17302252);
        initVolumePreference("alarm_volume", 4, 17302250);
        if (this.mVoiceCapable) {
            this.mRingOrNotificationPreference = initVolumePreference("ring_volume", 2, 17302258);
            removePreference("notification_volume");
        } else {
            this.mRingOrNotificationPreference = initVolumePreference("notification_volume", 5, 17302258);
            removePreference("ring_volume");
        }
        boolean isCellBroadcastAppLinkEnabled = getResources().getBoolean(17956981);
        if (isCellBroadcastAppLinkEnabled) {
            try {
                if (this.mPm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver") == 2) {
                    isCellBroadcastAppLinkEnabled = false;
                }
            } catch (IllegalArgumentException e) {
                isCellBroadcastAppLinkEnabled = false;
            }
        }
        if (!(this.mUserManager.isAdminUser() && r1 && !RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_cell_broadcasts", UserHandle.myUserId()))) {
            removePreference("cell_broadcast_settings");
        }
        initRingtones();
        initVibrateWhenRinging();
        updateRingerMode();
        updateEffectsSuppressor();
        if (savedInstanceState != null) {
            String selectedPreference = savedInstanceState.getString("selected_preference", null);
            if (!TextUtils.isEmpty(selectedPreference)) {
                this.mRequestPreference = (RingtonePreference) findPreference(selectedPreference);
            }
        }
    }

    public void onResume() {
        super.onResume();
        lookupRingtoneNames();
        this.mSettingsObserver.register(true);
        this.mReceiver.register(true);
        updateRingOrNotificationPreference();
        updateEffectsSuppressor();
        for (VolumeSeekBarPreference volumePref : this.mVolumePrefs) {
            volumePref.onActivityResume();
        }
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_adjust_volume", UserHandle.myUserId());
        boolean hasBaseRestriction = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_adjust_volume", UserHandle.myUserId());
        for (String key : RESTRICTED_KEYS) {
            Preference pref = findPreference(key);
            if (pref != null) {
                boolean z;
                if (hasBaseRestriction) {
                    z = false;
                } else {
                    z = true;
                }
                pref.setEnabled(z);
            }
            if ((pref instanceof RestrictedPreference) && !hasBaseRestriction) {
                ((RestrictedPreference) pref).setDisabledByAdmin(admin);
            }
        }
        RestrictedPreference broadcastSettingsPref = (RestrictedPreference) findPreference("cell_broadcast_settings");
        if (broadcastSettingsPref != null) {
            broadcastSettingsPref.checkRestrictionAndSetDisabled("no_config_cell_broadcasts");
        }
    }

    public void onPause() {
        super.onPause();
        for (VolumeSeekBarPreference volumePref : this.mVolumePrefs) {
            volumePref.onActivityPause();
        }
        this.mVolumeCallback.stopSample();
        this.mSettingsObserver.register(false);
        this.mReceiver.register(false);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof RingtonePreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        this.mRequestPreference = (RingtonePreference) preference;
        this.mRequestPreference.onPrepareRingtonePickerIntent(this.mRequestPreference.getIntent());
        startActivityForResult(preference.getIntent(), 200);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mRequestPreference != null) {
            this.mRequestPreference.onActivityResult(requestCode, resultCode, data);
            this.mRequestPreference = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRequestPreference != null) {
            outState.putString("selected_preference", this.mRequestPreference.getKey());
        }
    }

    private VolumeSeekBarPreference initVolumePreference(String key, int stream, int muteIcon) {
        VolumeSeekBarPreference volumePref = (VolumeSeekBarPreference) findPreference(key);
        volumePref.setCallback(this.mVolumeCallback);
        volumePref.setStream(stream);
        this.mVolumePrefs.add(volumePref);
        volumePref.setMuteIcon(muteIcon);
        return volumePref;
    }

    private void updateRingOrNotificationPreference() {
        int i;
        VolumeSeekBarPreference volumeSeekBarPreference = this.mRingOrNotificationPreference;
        if (this.mSuppressor != null) {
            i = 17302258;
        } else if (this.mRingerMode == 1 || wasRingerModeVibrate()) {
            i = 17302259;
        } else {
            i = 17302257;
        }
        volumeSeekBarPreference.showIcon(i);
    }

    private boolean wasRingerModeVibrate() {
        if (this.mVibrator != null && this.mRingerMode == 0 && this.mAudioManager.getLastAudibleStreamVolume(2) == 0) {
            return true;
        }
        return false;
    }

    private void updateRingerMode() {
        int ringerMode = this.mAudioManager.getRingerModeInternal();
        if (this.mRingerMode != ringerMode) {
            this.mRingerMode = ringerMode;
            updateRingOrNotificationPreference();
        }
    }

    private void updateEffectsSuppressor() {
        ComponentName suppressor = NotificationManager.from(this.mContext).getEffectsSuppressor();
        if (!Objects.equals(suppressor, this.mSuppressor)) {
            this.mSuppressor = suppressor;
            if (this.mRingOrNotificationPreference != null) {
                String string;
                if (suppressor != null) {
                    string = this.mContext.getString(17040815, new Object[]{getSuppressorCaption(suppressor)});
                } else {
                    string = null;
                }
                this.mRingOrNotificationPreference.setSuppressionText(string);
            }
            updateRingOrNotificationPreference();
        }
    }

    private String getSuppressorCaption(ComponentName suppressor) {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            ServiceInfo info = pm.getServiceInfo(suppressor, 0);
            if (info != null) {
                CharSequence seq = info.loadLabel(pm);
                if (seq != null) {
                    String str = seq.toString().trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Throwable e) {
            Log.w("SoundSettings", "Error loading suppressor caption", e);
        }
        return suppressor.getPackageName();
    }

    private void initRingtones() {
        this.mPhoneRingtonePreference = getPreferenceScreen().findPreference("ringtone");
        if (!(this.mPhoneRingtonePreference == null || this.mVoiceCapable)) {
            getPreferenceScreen().removePreference(this.mPhoneRingtonePreference);
            this.mPhoneRingtonePreference = null;
        }
        this.mNotificationRingtonePreference = getPreferenceScreen().findPreference("notification_ringtone");
        this.mAlarmRingtonePreference = getPreferenceScreen().findPreference("alarm_ringtone");
    }

    private void lookupRingtoneNames() {
        AsyncTask.execute(this.mLookupRingtoneNames);
    }

    private static CharSequence updateRingtoneName(Context context, int type) {
        if (context == null) {
            Log.e("SoundSettings", "Unable to update ringtone name, no context provided");
            return null;
        }
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
        CharSequence summary = context.getString(17040324);
        if (ringtoneUri == null) {
            summary = context.getString(17040322);
        } else {
            Cursor cursor = null;
            try {
                if ("media".equals(ringtoneUri.getAuthority())) {
                    cursor = context.getContentResolver().query(ringtoneUri, new String[]{"title"}, null, null, null);
                } else if ("content".equals(ringtoneUri.getScheme())) {
                    cursor = context.getContentResolver().query(ringtoneUri, new String[]{"_display_name"}, null, null, null);
                }
                if (cursor != null && cursor.moveToFirst()) {
                    summary = cursor.getString(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalArgumentException e2) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return summary;
    }

    private void initVibrateWhenRinging() {
        this.mVibrateWhenRinging = (TwoStatePreference) getPreferenceScreen().findPreference("vibrate_when_ringing");
        if (this.mVibrateWhenRinging == null) {
            Log.i("SoundSettings", "Preference not found: vibrate_when_ringing");
        } else if (this.mVoiceCapable) {
            this.mVibrateWhenRinging.setPersistent(false);
            updateVibrateWhenRinging();
            this.mVibrateWhenRinging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return System.putInt(SoundSettings.this.getContentResolver(), "vibrate_when_ringing", ((Boolean) newValue).booleanValue() ? 1 : 0);
                }
            });
        } else {
            getPreferenceScreen().removePreference(this.mVibrateWhenRinging);
            this.mVibrateWhenRinging = null;
        }
    }

    private void updateVibrateWhenRinging() {
        boolean z = false;
        if (this.mVibrateWhenRinging != null) {
            TwoStatePreference twoStatePreference = this.mVibrateWhenRinging;
            if (System.getInt(getContentResolver(), "vibrate_when_ringing", 0) != 0) {
                z = true;
            }
            twoStatePreference.setChecked(z);
        }
    }
}
