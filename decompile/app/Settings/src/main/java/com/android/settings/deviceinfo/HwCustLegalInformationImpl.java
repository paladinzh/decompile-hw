package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.HwCustDeviceInfoSettingsImpl;
import org.json.JSONException;
import org.json.JSONObject;

public class HwCustLegalInformationImpl extends HwCustLegalInformation {
    private static final String AUTHORITY = "com.huawei.sprint.provider";
    private static final String CALLING_PACKAGE = "callingpackage";
    private static final String CALLING_PACKAGE_VALUE = "com.android.settings";
    private static final String CHAMELEON = "Chameleon";
    private static final String CHAMELEON_PACKAGE_VALUE = "com.huawei.sprint.chameleon";
    private static final String IS_SHOW_HUAWEI_PRIVACY_POLICY = "is_show_huawei_privacypolicy";
    private static final String KEY_CARRIER_LEGAL = "carrier_legal";
    private static final String KEY_HUAWEI_PRIVACY_POLICY = "huawei_privacy_policy";
    private static final String KEY_TERMS = "terms";
    private static final boolean LOG_DEBUG = true;
    private static final boolean LOG_ERROR = true;
    private static final String NA = "N/A";
    private static final String NONE = "NONE";
    private static final String TAG = "HwCustLegalInformationImpl";
    private String mBrandAlpha;
    private String mCarrierHomepage;
    private Preference mCarrierLegal;
    private PreferenceScreen mPrefRoot;

    public HwCustLegalInformationImpl(Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    public void updateCustPreference(PreferenceScreen prefRoot) {
        this.mPrefRoot = prefRoot;
        boolean showSutelVersion = SystemProperties.getBoolean("ro.config.showSutelInLegalInfo", false);
        String sutelVersion = HwCustDeviceInfoSettingsImpl.getSutelVersion(this.mActivity);
        if (showSutelVersion && !"".equals(sutelVersion)) {
            addSutelNumber(sutelVersion);
        }
        if (checkChameleonAppInstalledOrNot(CHAMELEON_PACKAGE_VALUE)) {
            addCarrierLegalPreference();
        }
        if (isShowPrivacyPolicy()) {
            PreferenceScreen huawei_privacy_policy = (PreferenceScreen) this.mPrefRoot.findPreference(KEY_HUAWEI_PRIVACY_POLICY);
            if (huawei_privacy_policy != null) {
                this.mPrefRoot.removePreference(huawei_privacy_policy);
            }
        }
    }

    private boolean isShowPrivacyPolicy() {
        if (Systemex.getInt(this.mActivity.getContentResolver(), IS_SHOW_HUAWEI_PRIVACY_POLICY, 1) == 0) {
            return true;
        }
        return false;
    }

    private void addSutelNumber(String number) {
        Preference sutelNumberPre = new Preference(this.mActivity);
        sutelNumberPre.setTitle(2131629104);
        sutelNumberPre.setSummary((CharSequence) number);
        int prefCount = this.mPrefRoot.getPreferenceCount();
        this.mPrefRoot.getPreference(prefCount - 1).setOrder(prefCount + 1);
        sutelNumberPre.setOrder(prefCount);
        this.mPrefRoot.addPreference(sutelNumberPre);
    }

    private void addCarrierLegalPreference() {
        new AsyncTask<Void, Void, Boolean>() {
            protected void onPostExecute(Boolean result) {
                if (result.booleanValue()) {
                    HwCustLegalInformationImpl.this.mCarrierLegal = new Preference(HwCustLegalInformationImpl.this.mActivity);
                    HwCustLegalInformationImpl.this.mCarrierLegal.setKey(HwCustLegalInformationImpl.KEY_CARRIER_LEGAL);
                    HwCustLegalInformationImpl.this.mCarrierLegal.setTitle(2131629176);
                    HwCustLegalInformationImpl.this.mCarrierLegal.setWidgetLayoutResource(2130968998);
                    Intent startCarrierLegalActivity = new Intent();
                    startCarrierLegalActivity.setAction("com.huawei.sprint.chameleon.ShowCarrierLegalText");
                    startCarrierLegalActivity.setType("text/plain");
                    startCarrierLegalActivity.putExtra(String.valueOf(500), HwCustLegalInformationImpl.this.mBrandAlpha);
                    startCarrierLegalActivity.putExtra(String.valueOf(592), HwCustLegalInformationImpl.this.mCarrierHomepage);
                    HwCustLegalInformationImpl.this.mCarrierLegal.setIntent(startCarrierLegalActivity);
                    int prefCount = HwCustLegalInformationImpl.this.mPrefRoot.getPreferenceCount();
                    HwCustLegalInformationImpl.this.mPrefRoot.getPreference(prefCount - 1).setOrder(prefCount + 1);
                    HwCustLegalInformationImpl.this.mCarrierLegal.setOrder(prefCount);
                    HwCustLegalInformationImpl.this.mPrefRoot.addPreference(HwCustLegalInformationImpl.this.mCarrierLegal);
                }
                super.onPostExecute(result);
            }

            protected Boolean doInBackground(Void... arg) {
                Uri uri = null;
                ContentValues cv = new ContentValues();
                cv.put(HwCustLegalInformationImpl.CALLING_PACKAGE, HwCustLegalInformationImpl.CALLING_PACKAGE_VALUE);
                try {
                    uri = HwCustLegalInformationImpl.this.mActivity.getContentResolver().insert(Uri.parse("content://com.huawei.sprint.provider/activitylauncher/supportcarrierlegal"), cv);
                } catch (IllegalArgumentException e) {
                    Log.e(HwCustLegalInformationImpl.TAG, "Uri Not Found");
                    Log.e(HwCustLegalInformationImpl.TAG, "Exception :" + e);
                }
                if (uri == null) {
                    return Boolean.valueOf(false);
                }
                String result = uri.toString();
                Log.d(HwCustLegalInformationImpl.TAG, "Result :" + result);
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    HwCustLegalInformationImpl.this.mBrandAlpha = jsonObj.getString(String.valueOf(500));
                    HwCustLegalInformationImpl.this.mCarrierHomepage = jsonObj.getString(String.valueOf(592));
                } catch (JSONException e2) {
                    Log.e(HwCustLegalInformationImpl.TAG, "Exception :" + e2);
                }
                if (HwCustLegalInformationImpl.NONE.equalsIgnoreCase(HwCustLegalInformationImpl.this.mCarrierHomepage)) {
                    return Boolean.valueOf(false);
                }
                return Boolean.valueOf(true);
            }
        }.execute(new Void[0]);
    }

    private boolean checkChameleonAppInstalledOrNot(String packagename) {
        try {
            this.mActivity.getPackageManager().getPackageInfo(packagename, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
