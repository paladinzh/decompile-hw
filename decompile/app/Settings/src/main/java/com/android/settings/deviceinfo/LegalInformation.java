package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.HuaweiSafetyInfoActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class LegalInformation extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            if (context == null) {
                return result;
            }
            PackageManager manager = context.getPackageManager();
            Resources res = context.getResources();
            if (Utils.hasIntentActivity(manager, "com.android.settings.HuaweiPrivacyPolicyActivity")) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627418);
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            if (Utils.hasIntentActivity(manager, "android.settings.HUAWEI_COPYRIGHT")) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131628829);
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            if (Utils.hasIntentActivity(manager, "android.settings.SAFETY_INFO_SETTINGS")) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131625518);
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            if (Utils.hasIntentActivity(manager, "android.settings.LICENSE")) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131625515);
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            if (Utils.hasIntentActivity(manager, "android.settings.TERMS")) {
                data = new SearchIndexableRaw(context);
                List<ResolveInfo> list = manager.queryIntentActivities(new Intent("android.settings.TERMS"), 0);
                if (list.size() > 0) {
                    data.title = (String) ((ResolveInfo) list.get(0)).loadLabel(manager);
                }
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            if (Utils.hasIntentActivity(manager, "android.settings.WEBVIEW_LICENSE")) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131625510);
                data.screenTitle = res.getString(2131625503);
                result.add(data);
            }
            return result;
        }
    };
    private HwCustLegalInformation mHwCustLegalInformation;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230805);
        Activity act = getActivity();
        PreferenceGroup parentPreference = (PreferenceGroup) findPreference("legal_information");
        this.mHwCustLegalInformation = (HwCustLegalInformation) HwCustUtils.createObj(HwCustLegalInformation.class, new Object[]{act});
        if (this.mHwCustLegalInformation != null) {
            this.mHwCustLegalInformation.updateCustPreference((PreferenceScreen) parentPreference);
        }
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "terms", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "license", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "copyright", 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, "webview_license", 1);
        PreferenceScreen huawei_copyright = (PreferenceScreen) findPreference("huawei_copyright");
        if (System.getInt(getContentResolver(), "is_show_huawei_copyright", 1) == 0 && huawei_copyright != null) {
            parentPreference.removePreference(huawei_copyright);
        }
        Preference safetyInfoPrefs = findPreference("safety_info");
        if (safetyInfoPrefs != null) {
            Intent intent = safetyInfoPrefs.getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null && !HuaweiSafetyInfoActivity.isFileExist(extras.getString("file_name"))) {
                    removePreference("safety_info");
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 2;
    }
}
