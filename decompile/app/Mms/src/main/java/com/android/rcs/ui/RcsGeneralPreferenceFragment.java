package com.android.rcs.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.RcsMmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.ui.RcsSettingsPreferenceActivity;
import com.huawei.rcs.utils.RcsProfile;

public class RcsGeneralPreferenceFragment {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    protected MessagingPreferenceActivity mActivity;
    private IfMsgplus mMsgPlus;
    private PreferenceFragment mPreferenceFragment;
    private Preference mRcsSettingsPref;
    private int mRcsSwitcherDefaultStatus = 1;

    public RcsGeneralPreferenceFragment(PreferenceFragment preferenceFragment) {
        this.mPreferenceFragment = preferenceFragment;
    }

    public void setGeneralPreferenceFragment(Activity activity, PreferenceFragment fragment) {
        if (this.isRcsOn || (activity instanceof MessagingPreferenceActivity)) {
            this.mActivity = (MessagingPreferenceActivity) activity;
            this.mPreferenceFragment = fragment;
        }
    }

    public String getKeyCustMessageRing(SharedPreferences sp) {
        return sp.getString("key_cust_message_ring", "no_cust_message_ring");
    }

    public void restoreKeyCustMessageRing(SharedPreferences sp, String custMessageRing) {
        sp.edit().putString("key_cust_message_ring", custMessageRing).commit();
    }

    public String getGeneralDefaultsBtlDigest(SharedPreferences sp) {
        return sp.getString("general_defaults_digest", "still_no_digest");
    }

    public void setGeneralDefaultsBtlDigest(SharedPreferences sp, String generalOldBtlDigest) {
        sp.edit().putString("general_defaults_digest", generalOldBtlDigest).commit();
    }

    public boolean getEnableCotaFeature() {
        return RcsMmsConfig.getEnableCotaFeature();
    }

    public void onCreateRCS() {
        if (this.isRcsOn) {
            initRcsSwitcherDefaultStatus();
            addRcsSettingsPrefToCommon();
        }
    }

    private void addRcsSettingsPrefToCommon() {
        PreferenceCategory commonSettingPref = (PreferenceCategory) this.mPreferenceFragment.findPreference("pref_key_common_settings");
        Preference listDivideLineRcs = new Preference(this.mActivity);
        listDivideLineRcs.setPreferenceId(R.id.list_divide_line_rcs);
        listDivideLineRcs.setLayoutResource(R.layout.listdivider);
        listDivideLineRcs.setOrder(-1);
        commonSettingPref.addPreference(listDivideLineRcs);
        this.mRcsSettingsPref = new Preference(this.mActivity);
        this.mRcsSettingsPref.setPreferenceId(R.id.rcs_setting_preference);
        this.mRcsSettingsPref.setKey("pref_key_rcs_settings");
        this.mRcsSettingsPref.setPersistent(false);
        this.mRcsSettingsPref.setTitle(R.string.rcs_message);
        this.mRcsSettingsPref.setLayoutResource(R.layout.mms_preference_status);
        this.mRcsSettingsPref.setWidgetLayoutResource(R.layout.mms_preference_widget_arrow);
        this.mRcsSettingsPref.setOrder(-1);
        commonSettingPref.addPreference(this.mRcsSettingsPref);
    }

    private void launchRcsSettings(Context context) {
        if (context != null) {
            try {
                context.startActivity(new Intent(context, RcsSettingsPreferenceActivity.class));
            } catch (ActivityNotFoundException e) {
                MLog.e("RcsGeneralPreferenceFragment", "RCS settings activity not found");
            }
        }
    }

    public void onPreferenceTreeClick(Context context, Preference preference) {
        if (this.isRcsOn && "pref_key_rcs_settings".equals(preference.getKey())) {
            launchRcsSettings(context);
        }
    }

    public void onResume() {
        if (this.isRcsOn && this.mRcsSettingsPref != null) {
            this.mRcsSettingsPref.setSummary(getRcsSwitchStatus() ? R.string.smart_sms_setting_status_open : R.string.smart_sms_setting_status_close);
        }
    }

    public void restoreDefaultPreferences() {
        if (this.isRcsOn) {
            addRcsSettingsPrefToCommon();
            setRcsSwitchStatus(this.mRcsSwitcherDefaultStatus);
            if (this.mRcsSettingsPref != null) {
                this.mRcsSettingsPref.setSummary(this.mRcsSwitcherDefaultStatus == 1 ? R.string.smart_sms_setting_status_open : R.string.smart_sms_setting_status_close);
            }
            RcsProfile.setIMThreadDisplayMergeStatus(this.mActivity, 1);
            RcsProfile.setGroupInviteAutoAccept(this.mActivity, 1);
            resetFtRcsSwitch();
        }
    }

    private void initRcsSwitcherDefaultStatus() {
        this.mMsgPlus = RcsProfile.getRcsService();
        if (this.mMsgPlus != null) {
            try {
                this.mRcsSwitcherDefaultStatus = this.mMsgPlus.getRcsSwitcherDefaultStatus();
            } catch (RemoteException e) {
                MLog.e("RcsGeneralPreferenceFragment", e.toString());
            }
        }
    }

    private boolean getRcsSwitchStatus() {
        int mRcsSwitchStatus = 0;
        try {
            mRcsSwitchStatus = Secure.getInt(this.mActivity.getContentResolver(), "huawei_rcs_switcher", 1);
        } catch (Exception e) {
            MLog.e("RcsGeneralPreferenceFragment", e.toString());
        }
        if (1 == mRcsSwitchStatus) {
            return true;
        }
        return false;
    }

    public void hideDisplayModePref(Preference preference) {
        if (this.isRcsOn && preference != null) {
            this.mPreferenceFragment.getPreferenceScreen().removePreference(preference);
        }
    }

    private boolean setRcsSwitchStatus(int value) {
        boolean bResult = false;
        try {
            bResult = Secure.putInt(this.mActivity.getContentResolver(), "huawei_rcs_switcher", value);
        } catch (Exception e) {
            MLog.e("RcsGeneralPreferenceFragment", e.toString());
        }
        return bResult;
    }

    private void resetFtRcsSwitch() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mActivity);
        boolean[] result = new boolean[]{sp.getBoolean("pref_key_auto_accept_file", true), sp.getBoolean("pref_key_Roam_auto_accept", false)};
        if (result[0]) {
            RcsProfile.setftFileAceeptSwitch(this.mActivity, 1, "pref_key_auto_accept_file");
        } else {
            RcsProfile.setftFileAceeptSwitch(this.mActivity, 0, "pref_key_auto_accept_file");
        }
        if (result[1]) {
            RcsProfile.setftFileAceeptSwitch(this.mActivity, 1, "pref_key_Roam_auto_accept");
        } else {
            RcsProfile.setftFileAceeptSwitch(this.mActivity, 0, "pref_key_Roam_auto_accept");
        }
    }
}
