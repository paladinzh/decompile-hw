package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;

public class HwCustGlobalTrafficSettingFragmentImpl extends HwCustGlobalTrafficSettingFragment {
    private static final int PREF_SORT_ORDER_1 = 45;
    private static final int PREF_SORT_ORDER_2 = 65;
    private static final String RESET_DATA_LIMIT_KEY = "reset_data_limit";
    private static final int SIM_INDEX_0 = 0;
    private static final int SIM_INDEX_1 = 1;
    private boolean isResetPrefRequired = SystemProperties.getBoolean("ro.config.reset_pref_enable", false);
    private Preference resetPreferenceCard1;
    private Preference resetPreferenceCard2;

    public HwCustGlobalTrafficSettingFragmentImpl(GlobalTrafficSettingFragment globalTrafficSettingFragment) {
        super(globalTrafficSettingFragment);
    }

    public void addResetPreferenceToGroup(Activity activity, PreferenceGroup preferenceGroup, String imsi, int cardPreferIndex) {
        if (this.isResetPrefRequired && preferenceGroup != null && imsi != null && activity != null) {
            if (cardPreferIndex == 0) {
                if (this.resetPreferenceCard1 == null) {
                    this.resetPreferenceCard1 = new Preference(activity);
                    initPreference(activity, imsi, this.resetPreferenceCard1, 45);
                }
                preferenceGroup.addPreference(this.resetPreferenceCard1);
            } else if (cardPreferIndex == 1) {
                if (this.resetPreferenceCard2 == null) {
                    this.resetPreferenceCard2 = new Preference(activity);
                    initPreference(activity, imsi, this.resetPreferenceCard2, 65);
                }
                preferenceGroup.addPreference(this.resetPreferenceCard2);
            }
        }
    }

    private void initPreference(final Activity activity, final String imsi, Preference resetPreference, int sortOrder) {
        if (resetPreference != null) {
            resetPreference.setOrder(sortOrder);
            resetPreference.setTitle(R.string.reset_limit);
            resetPreference.setKey(RESET_DATA_LIMIT_KEY);
            resetPreference.setWidgetLayoutResource(R.layout.preference_widget_arrow);
            resetPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    HwCustGlobalTrafficSettingFragmentImpl.this.showResetDataLimitDialog(activity, imsi);
                    return true;
                }
            });
        }
    }

    private void showResetDataLimitDialog(Activity activity, final String imsi) {
        new Builder(activity).setTitle(R.string.reset_limit).setMessage(R.string.reset_confirmation_message).setPositiveButton(R.string.confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                NatSettingManager.deletePackageSetting(imsi);
                if (HwCustGlobalTrafficSettingFragmentImpl.this.mGlobalTrafficSettingFragment != null) {
                    HwCustGlobalTrafficSettingFragmentImpl.this.mGlobalTrafficSettingFragment.refreshCardSettins();
                }
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void removeResetPreferenceFromGroup(PreferenceGroup preferenceGroup, int cardPreferIndex) {
        if (this.isResetPrefRequired && preferenceGroup != null) {
            if (cardPreferIndex == 0 && this.resetPreferenceCard1 != null) {
                preferenceGroup.removePreference(this.resetPreferenceCard1);
            } else if (cardPreferIndex == 1 && this.resetPreferenceCard2 != null) {
                preferenceGroup.removePreference(this.resetPreferenceCard2);
            }
        }
    }
}
