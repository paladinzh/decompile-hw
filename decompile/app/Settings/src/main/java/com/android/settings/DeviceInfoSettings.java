package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.app.PlatLogoActivity;
import com.android.settings.deviceinfo.AuthenticationInformationActivity;
import com.android.settings.deviceinfo.CertificationListSettings;
import com.android.settings.deviceinfo.TelecInfo;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceInfoSettings extends DeviceInfoSettingsHwBase implements Indexable {
    private static final boolean HIDE_REGULATORY_INFO = SystemProperties.getBoolean("ro.config.hw_hideRegulatory", true);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230770;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (isPropertyMissing("ro.build.selinux")) {
                keys.add("selinux_status");
            }
            if (isPropertyMissing("ro.url.safetylegal")) {
                keys.add("safetylegal");
            }
            if (isPropertyMissing("ro.ril.fccid")) {
                keys.add("fcc_equipment_id");
            }
            if (Utils.isWifiOnly(context)) {
                keys.add("baseband_version");
            }
            if (TextUtils.isEmpty(DeviceInfoUtils.getFeedbackReporterPackage(context))) {
                keys.add("device_feedback");
            }
            if (!checkIntentAction(context, "android.settings.TERMS")) {
                keys.add("terms");
            }
            if (!checkIntentAction(context, "android.settings.LICENSE")) {
                keys.add("license");
            }
            if (!checkIntentAction(context, "android.settings.COPYRIGHT")) {
                keys.add("copyright");
            }
            if (!UserManager.get(context).isAdminUser()) {
                keys.add("system_update_settings");
            }
            if (!context.getResources().getBoolean(2131492874)) {
                keys.add("additional_system_update_settings");
            }
            if (!Utils.isCheckAppExist(context, "com.ctc.epush")) {
                keys.add("china_telecom_epush");
            }
            if (DeviceInfoSettings.HIDE_REGULATORY_INFO) {
                keys.add("regulatory_info");
            }
            DeviceInfoSettings.dealWithAuthInfo(context, keys);
            if (!TelecInfo.hasCertification(context)) {
                keys.add("telec_info");
            }
            if (DeviceInfoSettings.mHwCustSearchIndexProvider != null) {
                DeviceInfoSettings.mHwCustSearchIndexProvider.addDeviceInfoNonIndexableKeys(context, keys);
            }
            return keys;
        }

        private boolean isPropertyMissing(String property) {
            return SystemProperties.get(property).equals("");
        }

        private boolean checkIntentAction(Context context, String action) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(new Intent(action), 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((((ResolveInfo) list.get(i)).activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private HwCustDeviceInfoSettings mCustDeviceInfoSettings;
    private EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;
    int mDevHitCountdown;
    Toast mDevHitToast;
    private EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;
    long[] mHits = new long[3];
    private boolean mLogoShowing;
    private UserManager mUm;

    protected int getMetricsCategory() {
        return 40;
    }

    protected int getHelpResource() {
        return 2131626534;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUm = UserManager.get(getActivity());
        setStringSummary("firmware_version", VERSION.RELEASE);
        findPreference("firmware_version").setEnabled(true);
        String patch = DeviceInfoUtils.getSecurityPatch();
        if (TextUtils.isEmpty(patch)) {
            getPreferenceScreen().removePreference(findPreference("security_patch"));
        } else {
            setStringSummary("security_patch", patch);
        }
        setValueSummary("baseband_version", "gsm.version.baseband");
        setStringSummary("device_model", Build.MODEL + DeviceInfoUtils.getMsvSuffix());
        setValueSummary("fcc_equipment_id", "ro.ril.fccid");
        setStringSummary("device_model", Build.MODEL);
        setStringSummary("build_number", Build.DISPLAY);
        findPreference("build_number").setEnabled(true);
        findPreference("kernel_version").setSummary(DeviceInfoUtils.getFormattedKernelVersion());
        if (!SELinux.isSELinuxEnabled()) {
            setStringSummary("selinux_status", getResources().getString(2131626427));
        } else if (!SELinux.isSELinuxEnforced()) {
            setStringSummary("selinux_status", getResources().getString(2131626428));
        }
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "selinux_status", "ro.build.selinux");
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal", "ro.url.safetylegal");
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "fcc_equipment_id", "ro.ril.fccid");
        if (Utils.isWifiOnly(getActivity())) {
            getPreferenceScreen().removePreference(findPreference("baseband_version"));
        }
        if (TextUtils.isEmpty(DeviceInfoUtils.getFeedbackReporterPackage(getActivity()))) {
            getPreferenceScreen().removePreference(findPreference("device_feedback"));
        }
        Activity act = getActivity();
        PreferenceGroup parentPreference = (PreferenceGroup) findPreference("container");
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "terms", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "license", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "copyright", 1);
        parentPreference = getPreferenceScreen();
        if (this.mUm.isAdminUser()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "system_update_settings", 1);
        } else {
            removePreference("system_update_settings");
        }
        removePreferenceIfBoolFalse("additional_system_update_settings", 2131492874);
        removePreferenceIfBoolFalse("manual", 2131492876);
        Preference pref = findPreference("regulatory_info");
        if (pref != null && HIDE_REGULATORY_INFO) {
            getPreferenceScreen().removePreference(pref);
        }
        this.mCustDeviceInfoSettings = (HwCustDeviceInfoSettings) HwCustUtils.createObj(HwCustDeviceInfoSettings.class, new Object[]{this});
        if (this.mCustDeviceInfoSettings != null) {
            this.mCustDeviceInfoSettings.updateCustPreference(act);
        }
        if (SystemProperties.getBoolean("ro.config.vicky_demo_6G", false)) {
            setStringSummary("ram", "6.0 GB");
        }
    }

    public void onResume() {
        super.onResume();
        this.mLogoShowing = false;
        this.mDevHitCountdown = getActivity().getSharedPreferences("development", 0).getBoolean("show", Build.TYPE.equals("eng")) ? -1 : 7;
        this.mDevHitToast = null;
        this.mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_fun", UserHandle.myUserId());
        this.mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_fun", UserHandle.myUserId());
        this.mDebuggingFeaturesDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_debugging_features", UserHandle.myUserId());
        this.mDebuggingFeaturesDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_debugging_features", UserHandle.myUserId());
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals("firmware_version")) {
            System.arraycopy(this.mHits, 1, this.mHits, 0, this.mHits.length - 1);
            this.mHits[this.mHits.length - 1] = SystemClock.uptimeMillis();
            if (this.mHits[0] >= SystemClock.uptimeMillis() - 500) {
                if (this.mUm.hasUserRestriction("no_fun")) {
                    if (!(this.mFunDisallowedAdmin == null || this.mFunDisallowedBySystem)) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mFunDisallowedAdmin);
                    }
                    Log.d("DeviceInfoSettings", "Sorry, no fun for you!");
                    return false;
                } else if (!this.mLogoShowing) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setClassName("android", PlatLogoActivity.class.getName());
                    try {
                        startActivity(intent);
                        this.mLogoShowing = true;
                    } catch (Exception e) {
                        Log.e("DeviceInfoSettings", "Unable to start activity " + intent.toString());
                    }
                }
            }
        } else if (preference.getKey().equals("build_number")) {
            if (!this.mUm.isAdminUser() || !Utils.isDeviceProvisioned(getActivity()) || ParentControl.isChildModeOn(getActivity())) {
                return true;
            }
            if (this.mUm.hasUserRestriction("no_debugging_features")) {
                if (!(this.mDebuggingFeaturesDisallowedAdmin == null || this.mDebuggingFeaturesDisallowedBySystem)) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mDebuggingFeaturesDisallowedAdmin);
                }
                return true;
            } else if (this.mDevHitCountdown > 0) {
                this.mDevHitCountdown--;
                if (this.mDevHitCountdown == 0) {
                    getActivity().getSharedPreferences("development", 0).edit().putBoolean("show", true).apply();
                    if (this.mDevHitToast != null) {
                        this.mDevHitToast.cancel();
                    }
                    this.mDevHitToast = Toast.makeText(getActivity(), 2131627273, 1);
                    this.mDevHitToast.show();
                    Index.getInstance(getActivity().getApplicationContext()).updateFromClassNameResource(DevelopmentSettings.class.getName(), true, true);
                } else if (this.mDevHitCountdown > 0 && this.mDevHitCountdown < 5 && this.mDevHitToast != null) {
                    this.mDevHitToast.cancel();
                }
            } else if (this.mDevHitCountdown < 0) {
                if (this.mDevHitToast != null) {
                    this.mDevHitToast.cancel();
                }
                this.mDevHitToast = Toast.makeText(getActivity(), 2131627274, 1);
                this.mDevHitToast.show();
            }
        } else if (preference.getKey().equals("device_feedback")) {
            sendFeedback();
        } else if (preference.getKey().equals(HwCustDeviceInfoSettings.KEY_OPERATOR_COUNTRY_INFO)) {
            showDialog(1);
        } else {
            Log.d("DeviceInfoSettings", "Click on " + preference.getTitle());
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void removePreferenceIfPropertyMissing(PreferenceGroup preferenceGroup, String preference, String property) {
        if (SystemProperties.get(property).equals("")) {
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (Exception e) {
                Log.d("DeviceInfoSettings", "Property '" + property + "' missing and no '" + preference + "' preference");
            }
        }
    }

    private void removePreferenceIfBoolFalse(String preference, int resId) {
        if (!getResources().getBoolean(resId)) {
            Preference pref = findPreference(preference);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

    protected void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary((CharSequence) value);
        } catch (RuntimeException e) {
            Log.w("DeviceInfoSettings", "setStringSummary RuntimeException:", e);
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(SystemProperties.get(property, getResources().getString(2131624355)));
        } catch (RuntimeException e) {
            Log.w("DeviceInfoSettings", "setValueSummary RuntimeException:", e);
        }
    }

    private void sendFeedback() {
        String reporterPackage = DeviceInfoUtils.getFeedbackReporterPackage(getActivity());
        if (!TextUtils.isEmpty(reporterPackage)) {
            Intent intent = new Intent("android.intent.action.BUG_REPORT");
            intent.setPackage(reporterPackage);
            startActivityForResult(intent, 0);
        }
    }

    private static void dealWithAuthInfo(Context context, List<String> keys) {
        if (SettingsExtUtils.isGlobalVersion()) {
            if (!AuthenticationInformationActivity.shouldDisplay(context)) {
                keys.add("authentication_info");
            }
            if (!CertificationListSettings.shouldDisplay(context)) {
                keys.add("certification_list");
                return;
            }
            return;
        }
        keys.add("certification_list");
        if (!AuthenticationInformationActivity.shouldDisplay(context)) {
            keys.add("authentication_info");
        }
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                if (this.mCustDeviceInfoSettings != null) {
                    Dialog dialog = this.mCustDeviceInfoSettings.getOperatorAndCountryDialog();
                    if (dialog != null) {
                        return dialog;
                    }
                }
                break;
            default:
                Log.w("DeviceInfoSettings", "onCreateDialog unknown id:" + id);
                break;
        }
        return super.onCreateDialog(id);
    }
}
