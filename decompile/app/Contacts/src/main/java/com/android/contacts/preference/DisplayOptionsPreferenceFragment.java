package com.android.contacts.preference;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.ListView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.AboutActivity;
import com.android.contacts.hap.camcard.CCUtils;
import com.android.contacts.hap.camcard.PreferenceDialog;
import com.android.contacts.hap.util.SeparatedFeatureDelegate;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;

public class DisplayOptionsPreferenceFragment extends PreferenceFragment {
    private static boolean isCamcardEnabled = false;
    private Preference aboutPref;
    private Preference accountPreference;
    private Preference displayOrderPreference;
    private Context mContext;
    private UpdateListener mUpdateListener;
    private SwitchPreference notifacationSwitchPreference;
    private SharedPreferences pref;
    private Preference settingsPref;
    private Preference sortOrderPreference;
    private TwoSummaryPreference updateCCpref;
    private TwoSummaryPreference updateYPPref;

    public class UpdateListener implements OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (UpdateUtil.getSummaryKey(4).equals(key) || UpdateUtil.getItemKey(4).equals(key)) {
                DisplayOptionsPreferenceFragment.this.setSummaryInfo(DisplayOptionsPreferenceFragment.this.updateYPPref, 4);
            } else if (UpdateUtil.getSummaryKey(3).equals(key) || UpdateUtil.getItemKey(3).equals(key)) {
                DisplayOptionsPreferenceFragment.this.setSummaryInfo(DisplayOptionsPreferenceFragment.this.updateCCpref, 3);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity().getApplicationContext();
        this.pref = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        this.mUpdateListener = new UpdateListener();
        initPreference();
    }

    public void initPreference() {
        PreferenceGroup preferenceGroupCC;
        PreferenceGroup preferenceGroupYP;
        addPreferencesFromResource(R.xml.preference_display_options);
        initTitle();
        this.sortOrderPreference = findPreference("sortOrder");
        this.displayOrderPreference = findPreference("displayOrder");
        if (ContactsPreferenceActivity.isToRemoveOrderPreferences(this.mContext) || EmuiFeatureManager.isChinaArea()) {
            removeOrderPreferences();
        } else {
            ((PreferenceGroup) findPreference("camcard_category")).removePreference(findPreference("camcard_about_divider"));
        }
        this.accountPreference = findPreference(getString(R.string.menu_accounts));
        this.updateYPPref = (TwoSummaryPreference) findPreference("yellowpage_update");
        camcardSettingInit();
        removeUpdatePreference();
        if (EmuiFeatureManager.isSuperSaverMode()) {
            this.accountPreference.setEnabled(false);
            preferenceGroupCC = (PreferenceGroup) findPreference("camcard_category");
            if (preferenceGroupCC != null) {
                getPreferenceScreen().removePreference(preferenceGroupCC);
            }
            preferenceGroupYP = (PreferenceGroup) findPreference("yellowpage_category");
            if (preferenceGroupYP != null) {
                getPreferenceScreen().removePreference(preferenceGroupYP);
            }
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            HwCustContactPreferenceCustomization lHelperObj = (HwCustContactPreferenceCustomization) HwCustUtils.createObj(HwCustContactPreferenceCustomization.class, new Object[0]);
            if (lHelperObj != null) {
                lHelperObj.customizePreferences(this);
            }
        }
        if (!EmuiFeatureManager.isCamcardEnabled() || CommonUtilMethods.isSimpleModeOn()) {
            preferenceGroupCC = (PreferenceGroup) findPreference("camcard_category");
            if (preferenceGroupCC != null) {
                getPreferenceScreen().removePreference(preferenceGroupCC);
            }
        }
        Preference accountDivider = findPreferenceByKeyResourceId(R.string.key_prefs_contacts_account_divider);
        if (accountDivider != null) {
            getPreferenceScreen().removePreference(accountDivider);
        }
        if (!EmuiFeatureManager.isYellowPageEnable() || CommonUtilMethods.isSimpleModeOn()) {
            preferenceGroupYP = (PreferenceGroup) findPreference("yellowpage_category");
            if (preferenceGroupYP != null) {
                getPreferenceScreen().removePreference(preferenceGroupYP);
            }
            if (accountDivider != null) {
                getPreferenceScreen().addPreference(accountDivider);
            }
        }
    }

    public Preference findPreferenceByKeyResourceId(int aResId) {
        return super.findPreference(getString(aResId));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((ListView) getView().findViewById(16908298)).setDivider(null);
    }

    private void setSummaryInfo(TwoSummaryPreference pf, int id) {
        if (pf != null) {
            long version = this.pref.getLong(UpdateUtil.getSummaryKey(id), 0);
            if (version > 0) {
                pf.setNetherSummary(this.mContext.getString(R.string.update_version_summary) + DateUtils.convertDateToVersion(version));
            }
            pf.setSummary(UpdateUtil.getItemString(this.mContext, this.pref.getInt(UpdateUtil.getItemKey(id), 2)));
        }
    }

    private void removeOrderPreferences() {
        PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("key_display_options_category");
        if (preferenceGroup != null) {
            preferenceGroup.removePreference(this.sortOrderPreference);
            preferenceGroup.removePreference(this.displayOrderPreference);
            getPreferenceScreen().removePreference(preferenceGroup);
        }
    }

    private void removeUpdatePreference() {
        if (!EmuiFeatureManager.isChinaArea() || !SeparatedFeatureDelegate.isInstalled(this.mContext)) {
            getPreferenceScreen().removePreference((PreferenceGroup) findPreference("yellowpage_category"));
            PreferenceGroup ccGroup = (PreferenceGroup) findPreference("camcard_category");
            ccGroup.removePreference(this.updateCCpref);
            ccGroup.removePreference(findPreference("camcard_update_divider"));
        }
    }

    public void onStart() {
        super.onStart();
        setSummaryInfo(this.updateYPPref, 4);
        setSummaryInfo(this.updateCCpref, 3);
        this.pref.registerOnSharedPreferenceChangeListener(this.mUpdateListener);
    }

    public void onStop() {
        this.pref.unregisterOnSharedPreferenceChangeListener(this.mUpdateListener);
        super.onStop();
        if (this.sortOrderPreference != null) {
            Dialog sortOrderDialog = ((ListPreference) this.sortOrderPreference).getDialog();
            if (sortOrderDialog != null) {
                sortOrderDialog.dismiss();
            }
        }
        if (this.displayOrderPreference != null) {
            Dialog displayOrderdialog = ((ListPreference) this.displayOrderPreference).getDialog();
            if (displayOrderdialog != null) {
                displayOrderdialog.dismiss();
            }
        }
    }

    private void camcardSettingInit() {
        this.notifacationSwitchPreference = (SwitchPreference) findPreference("key_prefs_ccnotify");
        if (this.notifacationSwitchPreference != null && CCUtils.REQUEST_PRECISE_ENABLE) {
            this.notifacationSwitchPreference.setChecked(getUpdateNotifyPref());
            this.notifacationSwitchPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (((Boolean) newValue).booleanValue()) {
                        PreferenceDialog.show(DisplayOptionsPreferenceFragment.this.getFragmentManager(), DisplayOptionsPreferenceFragment.this, 0);
                    } else {
                        DisplayOptionsPreferenceFragment.this.setUpdateNotifyPref(false);
                        StatisticalHelper.sendReport(4020, 0);
                        ExceptionCapture.reportScene(72);
                    }
                    return true;
                }
            });
        } else if (this.notifacationSwitchPreference != null) {
            PreferenceGroup ccGroup = (PreferenceGroup) findPreference("camcard_category");
            ccGroup.removePreference(this.notifacationSwitchPreference);
            ccGroup.removePreference(findPreference("key_prefs_ccnotify_divider"));
        }
        this.settingsPref = findPreference("camcard_settings");
        this.updateCCpref = (TwoSummaryPreference) findPreference("camcard_update");
        this.aboutPref = findPreference("camcard_about");
    }

    private void startAccountSetting() {
        ExceptionCapture.reportScene(68);
        Intent intent = new Intent("android.settings.SYNC_SETTINGS_EMUI");
        intent.putExtra("authorities", new String[]{"com.android.contacts"});
        intent.setFlags(524288);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HwLog.e("DisplayOptionsPreferenceFragment", "Account settings Activity not found");
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == this.accountPreference) {
            startAccountSetting();
            StatisticalHelper.report(4035);
        } else if (preference == this.settingsPref && EmuiFeatureManager.isCamcardEnabled()) {
            ExceptionCapture.reportScene(70);
            Intent intent = CCUtils.createLangDialogActivityIntent();
            if (!(intent == null || intent.resolveActivity(getActivity().getPackageManager()) == null)) {
                getActivity().startActivity(intent);
            }
        } else if (preference == this.aboutPref && EmuiFeatureManager.isCamcardEnabled()) {
            ExceptionCapture.reportScene(69);
            startActivity(new Intent(getActivity(), AboutActivity.class));
        } else if (preference == this.updateYPPref) {
            UpdateUtil.startUpdateActivity(getActivity(), 4);
            StatisticalHelper.report(4036);
        } else if (preference == this.updateCCpref && EmuiFeatureManager.isCamcardEnabled()) {
            UpdateUtil.startUpdateActivity(getActivity(), 3);
            StatisticalHelper.report(4037);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && (resultCode == 0 || resultCode == 1)) {
            this.notifacationSwitchPreference.setChecked(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean getUpdateNotifyPref() {
        return this.pref.getBoolean("key_prefs_ccnotify", false);
    }

    private void setUpdateNotifyPref(boolean bool) {
        Editor editor = this.pref.edit();
        editor.putBoolean("key_prefs_ccnotify", bool);
        editor.apply();
    }

    public void onResume() {
        PreferenceGroup preferenceGroupYP;
        super.onResume();
        EmuiFeatureManager.isShowCamCard(getActivity());
        boolean isCamcardModeChanged = EmuiFeatureManager.isCamcardEnabled();
        PreferenceGroup preferenceGroupCC = (PreferenceGroup) findPreference("camcard_category");
        if (EmuiFeatureManager.isYellowPageEnable() && isCamcardModeChanged && !CommonUtilMethods.isSimpleModeOn()) {
            preferenceGroupYP = (PreferenceGroup) findPreference("yellowpage_category");
            if (preferenceGroupYP != null) {
                Preference preference_line = findPreference(getString(R.string.key_prefs_contacts_yellowpage_update_divider));
                if (preference_line != null) {
                    preferenceGroupYP.removePreference(preference_line);
                }
            }
        }
        if (!EmuiFeatureManager.isCamcardEnabled() || CommonUtilMethods.isSimpleModeOn()) {
            if (preferenceGroupCC != null) {
                getPreferenceScreen().removePreference(preferenceGroupCC);
            }
        } else if (!(!EmuiFeatureManager.isCamcardEnabled() || isCamcardEnabled == isCamcardModeChanged || CommonUtilMethods.isSimpleModeOn() || EmuiFeatureManager.isSuperSaverMode())) {
            this.pref.unregisterOnSharedPreferenceChangeListener(this.mUpdateListener);
            getPreferenceScreen().removeAll();
            initPreference();
            setSummaryInfo(this.updateCCpref, 3);
            setSummaryInfo(this.updateYPPref, 4);
            this.pref.registerOnSharedPreferenceChangeListener(this.mUpdateListener);
        }
        Preference accountDivider = findPreferenceByKeyResourceId(R.string.key_prefs_contacts_account_divider);
        if (accountDivider != null) {
            getPreferenceScreen().removePreference(accountDivider);
        }
        if (EmuiFeatureManager.isSuperSaverMode() || CommonUtilMethods.isSimpleModeOn()) {
            preferenceGroupYP = (PreferenceGroup) findPreference("yellowpage_category");
            if (preferenceGroupYP != null) {
                getPreferenceScreen().removePreference(preferenceGroupYP);
            }
            if (accountDivider != null) {
                getPreferenceScreen().addPreference(accountDivider);
            }
        }
    }

    public void initTitle() {
        PreferenceGroup preferenceGroupCC = (PreferenceGroup) findPreference("camcard_category");
        if (preferenceGroupCC != null) {
            preferenceGroupCC.setTitle(CommonUtilMethods.upPercase(getString(R.string.camcard_card)));
        }
        PreferenceGroup preferenceGroupYP = (PreferenceGroup) findPreference("yellowpage_category");
        if (preferenceGroupYP != null) {
            preferenceGroupYP.setTitle(CommonUtilMethods.upPercase(getString(R.string.contact_yellowpage_title)));
        }
    }
}
