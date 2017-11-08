package com.huawei.systemmanager.mainscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.mainscreen.entrance.setting.FeedbackEntry;
import com.huawei.systemmanager.rainbow.CloudClientOperation;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.shortcut.ShortCutMainActivity;

public class SettingsFragment extends PreferenceFragment {
    private static final String PREFER_KEY_ABOUT = "about";
    private static final String PREFER_KEY_CLOUD = "cloud_switch";
    private static final String PREFER_KEY_FEEDBACK = "feedback";
    private static final String PREFER_KEY_SHORTCUT = "shortcut";
    public static final String TAG = "SettingsFragment";
    private Preference mAboutPrefer;
    private SwitchPreference mCloudPrefer;
    private Preference mFeedbackPrefer;
    private Preference mShourcutPrefer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_screen_setting_preference);
        initPrefres();
    }

    private void initPrefres() {
        initCloudPrefer();
        initShortcutPrefer();
        initFeedbackPrefer();
        initAboutPrefer();
    }

    private void initCloudPrefer() {
        this.mCloudPrefer = (SwitchPreference) findPreference(PREFER_KEY_CLOUD);
        if (CloudSwitchHelper.isCloudEnabled()) {
            this.mCloudPrefer.setChecked(CloudClientOperation.getSystemManageCloudsStatus(getGlbContext()));
            this.mCloudPrefer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isChecked = ((Boolean) newValue).booleanValue();
                    if (isChecked) {
                        CloudClientOperation.openSystemManageClouds(SettingsFragment.this.getGlbContext());
                    } else {
                        CloudClientOperation.closeSystemManageClouds(SettingsFragment.this.getGlbContext());
                    }
                    String[] strArr = new String[2];
                    strArr[0] = HsmStatConst.PARAM_OP;
                    strArr[1] = isChecked ? "1" : "0";
                    HsmStat.statE(38, HsmStatConst.constructJsonParams(strArr));
                    return true;
                }
            });
            return;
        }
        getPreferenceScreen().removePreference(this.mCloudPrefer);
    }

    private void initShortcutPrefer() {
        this.mShourcutPrefer = (TextArrowPreference) findPreference("shortcut");
        this.mShourcutPrefer.setEnabled(Utility.isOwnerUser(false));
        this.mShourcutPrefer.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Activity ac = SettingsFragment.this.getActivity();
                if (ac == null) {
                    return false;
                }
                SettingsFragment.this.tryStartActivity(new Intent(ac, ShortCutMainActivity.class));
                return true;
            }
        });
    }

    private void initFeedbackPrefer() {
        this.mFeedbackPrefer = (TextArrowPreference) findPreference(PREFER_KEY_FEEDBACK);
        this.mFeedbackPrefer.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = SettingsFragment.this.getActivity();
                if (activity == null) {
                    return false;
                }
                SettingsFragment.this.tryStartActivity(FeedbackEntry.getSettingEntryIntent(activity));
                HsmStat.statE(Events.E_MAINSCREEN_CLICK_FEED_BACK);
                return true;
            }
        });
        if (!FeedbackEntry.isEnable(getGlbContext())) {
            getPreferenceScreen().removePreference(this.mFeedbackPrefer);
        }
    }

    private void initAboutPrefer() {
        this.mAboutPrefer = (TextArrowPreference) findPreference(PREFER_KEY_ABOUT);
        this.mAboutPrefer.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Activity ac = SettingsFragment.this.getActivity();
                if (ac == null) {
                    return false;
                }
                SettingsFragment.this.tryStartActivity(new Intent(ac, AboutActivity.class));
                return false;
            }
        });
    }

    public void tryStartActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Context getGlbContext() {
        return GlobalContext.getContext();
    }
}
