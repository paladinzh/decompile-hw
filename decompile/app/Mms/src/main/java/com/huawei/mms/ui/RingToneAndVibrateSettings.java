package com.huawei.mms.ui;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.MenuItem;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.AssignRingTonePreference;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;

public class RingToneAndVibrateSettings extends HwPreferenceActivity {
    private RineToneAndVibrateSettingsFragment mFragment = null;

    public static class RineToneAndVibrateSettingsFragment extends HwPreferenceFragment implements OnPreferenceChangeListener {
        private AssignRingTonePreference mCurrentRingtonePref = null;
        private SwitchPreference mCustomizedInChatTone;
        private SwitchPreference mCustomizedOutChatTone;
        private HwCustRingToneAndVibrateSettings mHwRingToneAndVibrateSettings = ((HwCustRingToneAndVibrateSettings) HwCustUtils.createObj(HwCustRingToneAndVibrateSettings.class, new Object[0]));
        PreferenceScreen mPrefRoot;
        private BroadcastReceiver mRingerModeChangedReceiver;
        private AssignRingTonePreference mRingtonePref;
        private AssignRingTonePreference mRingtonePrefSub0;
        private AssignRingTonePreference mRingtonePrefSub1;
        private BroadcastReceiver mSdcardChangedReceiver;
        private SwitchPreference mVibrateWhenPref;
        private SwitchPreference mVibrateWhenPrefSub0;
        private SwitchPreference mVibrateWhenPrefSub1;

        private class RingerModeChangedReceiver extends BroadcastReceiver {
            private RingerModeChangedReceiver() {
            }

            public void onReceive(Context context, Intent intent) {
                if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                    if (MessageUtils.isMultiSimEnabled()) {
                        RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref(0);
                        RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref(1);
                    } else {
                        RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref();
                    }
                }
            }
        }

        private class SDcardChangedReceiver extends BroadcastReceiver {
            private SDcardChangedReceiver() {
            }

            public void onReceive(Context context, Intent intent) {
                if (!"android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction()) && !"android.intent.action.MEDIA_REMOVED".equals(intent.getAction()) && !"android.intent.action.MEDIA_BAD_REMOVAL".equals(intent.getAction())) {
                    return;
                }
                if (MessageUtils.isMultiSimEnabled()) {
                    RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref(0);
                    RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref(1);
                    return;
                }
                RineToneAndVibrateSettingsFragment.this.notifySetVibrateWhenPref();
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ringtong_and_vibrate_setting);
            this.mRingerModeChangedReceiver = new RingerModeChangedReceiver();
            this.mSdcardChangedReceiver = new SDcardChangedReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
            getActivity().registerReceiver(this.mRingerModeChangedReceiver, intentFilter);
            IntentFilter intentFilters = new IntentFilter();
            intentFilters.addAction("android.intent.action.MEDIA_UNMOUNTED");
            intentFilters.addAction("android.intent.action.MEDIA_REMOVED");
            intentFilters.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
            intentFilters.addDataScheme("file");
            getActivity().registerReceiver(this.mSdcardChangedReceiver, intentFilters);
            setPreference();
        }

        public void onResume() {
            super.onResume();
            if (MessageUtils.isMultiSimEnabled()) {
                notifySetVibrateWhenPref(0);
                notifySetVibrateWhenPref(1);
                return;
            }
            notifySetVibrateWhenPref();
        }

        public void onDestroy() {
            super.onDestroy();
            getActivity().unregisterReceiver(this.mRingerModeChangedReceiver);
            getActivity().unregisterReceiver(this.mSdcardChangedReceiver);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (this.mCurrentRingtonePref != null) {
                this.mCurrentRingtonePref.onActivityResult(requestCode, resultCode, data);
            }
            this.mCurrentRingtonePref = null;
        }

        private void setPreference() {
            this.mPrefRoot = (PreferenceScreen) findPreference("pref_key_root");
            this.mRingtonePref = (AssignRingTonePreference) findPreference("pref_key_ringtone");
            this.mRingtonePrefSub0 = (AssignRingTonePreference) findPreference("pref_key_ringtone_sub0");
            this.mRingtonePrefSub1 = (AssignRingTonePreference) findPreference("pref_key_ringtone_sub1");
            this.mVibrateWhenPref = (SwitchPreference) findPreference("pref_key_vibrateWhen");
            this.mVibrateWhenPrefSub0 = (SwitchPreference) findPreference("pref_key_vibrateWhen_sub0");
            this.mVibrateWhenPrefSub1 = (SwitchPreference) findPreference("pref_key_vibrateWhen_sub1");
            this.mCustomizedInChatTone = (SwitchPreference) findPreference("pref_key_ringtone_inchat_tone");
            this.mCustomizedOutChatTone = (SwitchPreference) findPreference("pref_key_ringtone_outchat_tone");
            this.mVibrateWhenPref.setOnPreferenceChangeListener(this);
            this.mVibrateWhenPrefSub0.setOnPreferenceChangeListener(this);
            this.mVibrateWhenPrefSub1.setOnPreferenceChangeListener(this);
            this.mCustomizedInChatTone.setOnPreferenceChangeListener(this);
            this.mCustomizedOutChatTone.setOnPreferenceChangeListener(this);
            this.mRingtonePrefSub0.setCurrentSubId(0);
            this.mRingtonePrefSub1.setCurrentSubId(1);
            if (MessageUtils.isMultiSimEnabled()) {
                this.mPrefRoot.removePreference(this.mRingtonePref);
                this.mPrefRoot.removePreference(this.mVibrateWhenPref);
                notifySetVibrateWhenPref(0);
                notifySetVibrateWhenPref(1);
            } else {
                this.mPrefRoot.removePreference(this.mRingtonePrefSub0);
                this.mPrefRoot.removePreference(this.mVibrateWhenPrefSub0);
                this.mPrefRoot.removePreference(this.mRingtonePrefSub1);
                this.mPrefRoot.removePreference(this.mVibrateWhenPrefSub1);
                notifySetVibrateWhenPref();
            }
            if (this.mHwRingToneAndVibrateSettings == null || !this.mHwRingToneAndVibrateSettings.isSmartRingtoneSupported() || MessageUtils.isMultiSimEnabled()) {
                this.mPrefRoot.removePreference(this.mCustomizedInChatTone);
                this.mPrefRoot.removePreference(this.mCustomizedOutChatTone);
                setCustomizedRingtones(getContext(), true);
                return;
            }
            this.mHwRingToneAndVibrateSettings.setRingtones(getContext(), this.mCustomizedInChatTone, this.mCustomizedOutChatTone);
            setCustomizedRingtones(getContext(), false);
        }

        private void setCustomizedRingtones(Context context, boolean setCust) {
            if (this.mHwRingToneAndVibrateSettings != null) {
                this.mHwRingToneAndVibrateSettings.setDefaultRingtones(context, setCust);
            }
        }

        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            boolean z = false;
            if (preference == this.mRingtonePref) {
                notifySetVibrateWhenPref();
                this.mCurrentRingtonePref = this.mRingtonePref;
            } else if (preference == this.mRingtonePrefSub0) {
                StatisticalHelper.incrementReportCount(getActivity(), 2223);
                notifySetVibrateWhenPref(0);
                this.mCurrentRingtonePref = this.mRingtonePrefSub0;
            } else if (preference == this.mRingtonePrefSub1) {
                StatisticalHelper.incrementReportCount(getActivity(), 2224);
                notifySetVibrateWhenPref(1);
                this.mCurrentRingtonePref = this.mRingtonePrefSub1;
            } else if (preference == this.mVibrateWhenPref) {
                notifySetVibrateWhenPref();
            } else if (preference == this.mVibrateWhenPrefSub0) {
                notifySetVibrateWhenPref(0);
            } else if (preference == this.mVibrateWhenPrefSub1) {
                notifySetVibrateWhenPref(1);
            } else if (preference == this.mCustomizedInChatTone) {
                boolean inChatToneChecked = this.mCustomizedInChatTone.isChecked();
                r4 = this.mCustomizedInChatTone;
                if (!inChatToneChecked) {
                    z = true;
                }
                r4.setChecked(z);
                if (this.mHwRingToneAndVibrateSettings != null) {
                    this.mHwRingToneAndVibrateSettings.setDefaultInChatTone(inChatToneChecked);
                }
            } else if (preference == this.mCustomizedOutChatTone) {
                boolean outChatToneChecked = this.mCustomizedOutChatTone.isChecked();
                r4 = this.mCustomizedOutChatTone;
                if (!outChatToneChecked) {
                    z = true;
                }
                r4.setChecked(z);
                if (this.mHwRingToneAndVibrateSettings != null) {
                    this.mHwRingToneAndVibrateSettings.setDefaultOutChatTone(outChatToneChecked);
                }
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void notifySetVibrateWhenPref() {
            int mode = ((AudioManager) getActivity().getSystemService("audio")).getRingerMode();
            boolean vibrate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_key_vibrateWhen", false);
            boolean enableNotify = PreferenceUtils.getNotificationEnabled(getActivity());
            boolean enabledVibrate = this.mVibrateWhenPref.isEnabled();
            boolean checked = this.mVibrateWhenPref.isChecked();
            boolean enabledRingtone = true;
            switch (mode) {
                case 0:
                    enabledVibrate = false;
                    checked = false;
                    enabledRingtone = false;
                    this.mRingtonePref.setKey("pref_key_ringtoneSp");
                    this.mRingtonePref.changeRingtone(null);
                    this.mVibrateWhenPref.setKey("pref_key_vibrateSp");
                    break;
                case 1:
                    enabledVibrate = true;
                    checked = vibrate;
                    enabledRingtone = false;
                    this.mRingtonePref.setKey("pref_key_ringtoneSp");
                    this.mRingtonePref.changeRingtone(null);
                    this.mVibrateWhenPref.setKey("pref_key_vibrateWhen");
                    break;
                case 2:
                    enabledVibrate = true;
                    checked = vibrate;
                    this.mRingtonePref.setKey("pref_key_ringtone");
                    setRingToneValue();
                    this.mVibrateWhenPref.setKey("pref_key_vibrateWhen");
                    break;
            }
            AssignRingTonePreference assignRingTonePreference = this.mRingtonePref;
            if (!enableNotify) {
                enabledRingtone = false;
            }
            assignRingTonePreference.setEnabled(enabledRingtone);
            SwitchPreference switchPreference = this.mVibrateWhenPref;
            if (!enableNotify) {
                enabledVibrate = false;
            }
            switchPreference.setEnabled(enabledVibrate);
            this.mVibrateWhenPref.setChecked(checked);
        }

        private void notifySetVibrateWhenPref(int subId) {
            int mode = ((AudioManager) getActivity().getSystemService("audio")).getRingerMode();
            String notificationVibrate = "pref_key_vibrateWhen_sub0";
            String ringToneKey = "pref_key_ringtone_sub0";
            String ringToneSpKey = "pref_key_ringtoneSp_sub0";
            String vibrateSpKey = "pref_key_vibrateSp_sub0";
            SwitchPreference vibrateWhenPref = this.mVibrateWhenPrefSub0;
            AssignRingTonePreference ringTonePref = this.mRingtonePrefSub0;
            if (subId == 1) {
                notificationVibrate = "pref_key_vibrateWhen_sub1";
                ringToneKey = "pref_key_ringtone_sub1";
                ringToneSpKey = "pref_key_ringtoneSp_sub1";
                vibrateSpKey = "pref_key_vibrateSp_sub1";
                vibrateWhenPref = this.mVibrateWhenPrefSub1;
                ringTonePref = this.mRingtonePrefSub1;
            }
            boolean vibrate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(notificationVibrate, false);
            boolean enableNotify = PreferenceUtils.getNotificationEnabled(getActivity());
            boolean oldEnabledVibrate = vibrateWhenPref.isEnabled();
            boolean oldCheckedVibrate = vibrateWhenPref.isChecked();
            boolean enabledVibrate = oldEnabledVibrate;
            boolean checked = oldCheckedVibrate;
            boolean enabledRingtone = true;
            switch (mode) {
                case 0:
                    enabledVibrate = false;
                    checked = false;
                    enabledRingtone = false;
                    ringTonePref.setKey(ringToneSpKey);
                    ringTonePref.changeRingtone(null);
                    vibrateWhenPref.setKey(vibrateSpKey);
                    break;
                case 1:
                    enabledVibrate = true;
                    checked = vibrate;
                    enabledRingtone = false;
                    if (vibrate == oldCheckedVibrate) {
                    }
                    ringTonePref.setKey(ringToneSpKey);
                    ringTonePref.changeRingtone(null);
                    vibrateWhenPref.setKey(notificationVibrate);
                    break;
                case 2:
                    enabledVibrate = true;
                    checked = vibrate;
                    if (vibrate == oldCheckedVibrate) {
                    }
                    ringTonePref.setKey(ringToneKey);
                    setRingToneValue(subId);
                    vibrateWhenPref.setKey(notificationVibrate);
                    break;
            }
            if (!enableNotify) {
                enabledRingtone = false;
            }
            ringTonePref.setEnabled(enabledRingtone);
            if (!enableNotify) {
                enabledVibrate = false;
            }
            vibrateWhenPref.setEnabled(enabledVibrate);
            vibrateWhenPref.setChecked(checked);
        }

        private void setRingToneValue() {
            boolean isFollowNotification;
            Uri uri = null;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int defaultFollowNotificationState = 1;
            if (!sp.contains("pref_mms_is_follow_notification")) {
                defaultFollowNotificationState = HwMessageUtils.getDefaultFollowNotificationState(getActivity());
                if (defaultFollowNotificationState != 0) {
                    boolean isDefaultFollowNotification = false;
                    if (defaultFollowNotificationState == 1) {
                        isDefaultFollowNotification = true;
                    }
                    if (!HwMessageUtils.isDefaultMessageRingtone(getActivity())) {
                        isDefaultFollowNotification = false;
                    }
                    if (sp.contains("pref_key_ringtone")) {
                        isDefaultFollowNotification = false;
                    }
                    Editor editor = sp.edit();
                    editor.putBoolean("pref_mms_is_follow_notification", isDefaultFollowNotification);
                    editor.commit();
                }
            }
            if (defaultFollowNotificationState == 0) {
                isFollowNotification = true;
            } else {
                isFollowNotification = sp.getBoolean("pref_mms_is_follow_notification", false);
            }
            if (isFollowNotification) {
                this.mRingtonePref.setSummary(R.string.mms_follow_notification_tone);
                return;
            }
            String customRingtoneStr = MmsConfig.getRingToneUriFromDatabase(getActivity(), 0);
            if (TextUtils.isEmpty(customRingtoneStr)) {
                this.mRingtonePref.setSummary(R.string.pref_summary_silent_ringstone);
            } else if (MessagingNotification.isUriAvalible(getActivity(), customRingtoneStr)) {
                this.mRingtonePref.changeRingtone(Uri.parse(customRingtoneStr));
            } else if (HwMessageUtils.getDefaultFollowNotificationState(getActivity()) != 2) {
                this.mRingtonePref.setSummary(R.string.mms_follow_notification_tone);
            } else {
                String defaultCustomRingtone = MessageUtils.getDefaultRintoneStr(getActivity());
                if (!(defaultCustomRingtone == null || MessagingNotification.isUriAvalible(getActivity(), defaultCustomRingtone))) {
                    this.mRingtonePref.setSummary(R.string.mms_follow_notification_tone);
                }
                AssignRingTonePreference assignRingTonePreference = this.mRingtonePref;
                if (!TextUtils.isEmpty(defaultCustomRingtone)) {
                    uri = Uri.parse(defaultCustomRingtone);
                }
                assignRingTonePreference.changeRingtone(uri);
            }
        }

        private void setRingToneValue(int subId) {
            boolean isFollowNotification;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int defaultFollowNotificationState = 1;
            AssignRingTonePreference ringTonePref = this.mRingtonePrefSub0;
            String followNotificationkey = "pref_mms_is_follow_notification_sub0";
            String ringToneKey = "pref_key_ringtone_sub0";
            if (subId == 1) {
                followNotificationkey = "pref_mms_is_follow_notification_sub1";
                ringToneKey = "pref_key_ringtone_sub1";
                ringTonePref = this.mRingtonePrefSub1;
            }
            if (!sp.contains(followNotificationkey)) {
                defaultFollowNotificationState = HwMessageUtils.getDefaultFollowNotificationState(getActivity());
                if (defaultFollowNotificationState != 0) {
                    boolean isDefaultFollowNotification = false;
                    if (defaultFollowNotificationState == 1) {
                        isDefaultFollowNotification = true;
                    }
                    if (!HwMessageUtils.isDefaultMessageRingtone(getActivity(), subId)) {
                        isDefaultFollowNotification = false;
                    }
                    if (sp.contains(ringToneKey)) {
                        isDefaultFollowNotification = false;
                    }
                    Editor editor = sp.edit();
                    editor.putBoolean(followNotificationkey, isDefaultFollowNotification);
                    editor.commit();
                }
            }
            if (defaultFollowNotificationState == 0) {
                isFollowNotification = true;
            } else {
                isFollowNotification = sp.getBoolean(followNotificationkey, false);
            }
            if (isFollowNotification) {
                ringTonePref.setSummary(R.string.mms_follow_notification_tone);
                return;
            }
            String ringToneStr = MmsConfig.getRingToneUriFromDatabase(getActivity(), subId);
            if (TextUtils.isEmpty(ringToneStr) || "null".equals(ringToneStr)) {
                ringTonePref.setSummary(R.string.pref_summary_silent_ringstone);
            } else if (MessagingNotification.isUriAvalible(getActivity(), ringToneStr)) {
                ringTonePref.changeRingtone(Uri.parse(ringToneStr));
            } else if (HwMessageUtils.getDefaultFollowNotificationState(getActivity()) != 2) {
                ringTonePref.setSummary(R.string.mms_follow_notification_tone);
            } else {
                Uri uri;
                String defaultCustomRingtone = MessageUtils.getDefaultRintoneStr(getActivity());
                if (!(defaultCustomRingtone == null || MessagingNotification.isUriAvalible(getActivity(), defaultCustomRingtone))) {
                    ringTonePref.setSummary(R.string.mms_follow_notification_tone);
                }
                if (TextUtils.isEmpty(defaultCustomRingtone)) {
                    uri = null;
                } else {
                    uri = Uri.parse(defaultCustomRingtone);
                }
                ringTonePref.changeRingtone(uri);
            }
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean isChecked = ((Boolean) newValue).booleanValue();
            Context activity;
            String str;
            if (preference == this.mVibrateWhenPref) {
                activity = getActivity();
                if (isChecked) {
                    str = "on";
                } else {
                    str = "off";
                }
                StatisticalHelper.reportEvent(activity, 2071, str);
                return true;
            } else if (preference == this.mVibrateWhenPrefSub0) {
                activity = getActivity();
                if (isChecked) {
                    str = "on";
                } else {
                    str = "off";
                }
                StatisticalHelper.reportEvent(activity, 2207, str);
                return true;
            } else if (preference != this.mVibrateWhenPrefSub1) {
                return false;
            } else {
                activity = getActivity();
                if (isChecked) {
                    str = "on";
                } else {
                    str = "off";
                }
                StatisticalHelper.reportEvent(activity, 2208, str);
                return true;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.pref_ringTong_and_vibrate);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.mFragment = new RineToneAndVibrateSettingsFragment();
        getFragmentManager().beginTransaction().replace(16908290, this.mFragment).commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mFragment != null) {
            this.mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
