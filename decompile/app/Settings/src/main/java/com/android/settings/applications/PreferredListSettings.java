package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.MLog;
import com.android.settings.PrivacyModeManager;
import com.android.settings.Settings.PreferredSettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.PreferredSettingsUtils.PreferredApplication;
import com.android.settings.fingerprint.HwCustFingerprintSettingsFragmentImpl;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class PreferredListSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String screenTitle = context.getResources().getString(2131627587);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            result.add(data);
            return result;
        }
    };
    private PackageManager mPm;
    private ContentObserver mPrivacyModeObserver;

    private void initPreferenceList() {
        getPreferenceScreen().removeAll();
        IntentFilter filterHome = new IntentFilter();
        filterHome.addAction("android.intent.action.MAIN");
        filterHome.addCategory("android.intent.category.HOME");
        Intent homeIntent = getIntent(filterHome);
        buildPreferredPreference(filterHome, homeIntent, PreferredApplication.PREFERRED_HOME, this.mPm.queryIntentActivities(homeIntent, 0));
        if (PreferredSettingsUtils.isTelephoneyOperationsSupported(getActivity()) && !Utils.isWifiOnly(getActivity()) && Utils.isVoiceCapable(getActivity())) {
            IntentFilter filterDial = new IntentFilter();
            filterDial.addAction("android.intent.action.DIAL");
            filterDial.addDataScheme(HwCustFingerprintSettingsFragmentImpl.TEL_PATTERN);
            Intent dialIntent = getIntent(filterDial);
            buildPreferredPreference(filterDial, dialIntent, PreferredApplication.PREFERRED_DAILER, this.mPm.queryIntentActivities(dialIntent, 0));
            if (!(PrivacyModeManager.isFeatrueSupported() ? new PrivacyModeManager(getActivity()).isGuestModeOn() : false)) {
                IntentFilter filterMMS = new IntentFilter();
                filterMMS.addAction("android.intent.action.SENDTO");
                filterMMS.addDataScheme("smsto");
                Intent mmsIntent = getIntent(filterMMS);
                buildPreferredPreference(filterMMS, mmsIntent, PreferredApplication.PREFERRED_MMS, this.mPm.queryIntentActivities(mmsIntent, 0));
            }
        }
        IntentFilter filterCamera = new IntentFilter();
        filterCamera.addAction("android.media.action.IMAGE_CAPTURE");
        Intent cameraIntent = getIntent(filterCamera);
        buildPreferredPreference(filterCamera, cameraIntent, PreferredApplication.PREFERRED_CAMERA, this.mPm.queryIntentActivities(cameraIntent, 0));
        IntentFilter filterGallery = new IntentFilter();
        filterGallery.addAction("android.intent.action.VIEW");
        filterGallery.addDataScheme("file");
        filterGallery.addDataScheme("content");
        try {
            filterGallery.addDataType("image/*");
        } catch (MalformedMimeTypeException ex) {
            MLog.e("PreferredListSettings", ex.getMessage());
        }
        Intent galleryIntent = getIntent(filterGallery);
        buildPreferredPreference(filterGallery, galleryIntent, PreferredApplication.PREFERRED_GALLERY, this.mPm.queryIntentActivities(galleryIntent, 0));
        IntentFilter filterMusic = new IntentFilter();
        filterMusic.addAction("android.intent.action.VIEW");
        filterMusic.addDataScheme("file");
        filterMusic.addDataScheme("content");
        try {
            filterMusic.addDataType("audio/*");
        } catch (MalformedMimeTypeException ex2) {
            MLog.e("PreferredListSettings", ex2.getMessage());
        }
        Intent musicIntent = getIntent(filterMusic);
        buildPreferredPreference(filterMusic, musicIntent, PreferredApplication.PREFERRED_MUSIC, this.mPm.queryIntentActivities(musicIntent, 0));
        IntentFilter filterEmail = new IntentFilter();
        filterEmail.addAction("android.intent.action.VIEW");
        filterEmail.addDataScheme("mailto");
        Intent emailIntent = getIntent(filterEmail);
        buildPreferredPreference(filterEmail, emailIntent, PreferredApplication.PREFERRED_MAIL, this.mPm.queryIntentActivities(emailIntent, 0));
        IntentFilter filterBrowser = new IntentFilter();
        filterBrowser.addAction("android.intent.action.VIEW");
        filterBrowser.addDataScheme("http");
        filterBrowser.addDataScheme("https");
        Intent browserIntent = getIntent(filterBrowser);
        buildPreferredPreference(filterBrowser, browserIntent, PreferredApplication.PREFERRED_BROWSER, this.mPm.queryIntentActivities(browserIntent, 0));
        addAssistAndVoiceInputPreference();
    }

    private Intent getIntent(IntentFilter filter) {
        String str = null;
        Intent intent = new Intent(filter.getAction(0));
        if (filter.countCategories() > 0 && !TextUtils.isEmpty(filter.getCategory(0))) {
            intent.addCategory(filter.getCategory(0));
        }
        if (filter.countDataSchemes() > 0 && !TextUtils.isEmpty(filter.getDataScheme(0))) {
            Uri localUri = Uri.parse(filter.getDataScheme(0) + ":");
            if (filter.countDataTypes() > 0 && !TextUtils.isEmpty(filter.getDataType(0))) {
                str = filter.getDataType(0);
                if (!(str.contains("\\") || str.contains("/"))) {
                    str = str + "/*";
                }
            }
            intent.setDataAndType(localUri, str);
        }
        return intent;
    }

    private void buildPreferredPreference(IntentFilter intentFilter, Intent preferredAppIntent, PreferredApplication preferredApplication, List<ResolveInfo> resolveInfoList) {
        String[] preferredAppEntries = getResources().getStringArray(2131361942);
        CharSequence label = "";
        if (preferredApplication.ordinal() < preferredAppEntries.length) {
            label = preferredAppEntries[preferredApplication.ordinal()];
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), PreferredSettingsActivity.class);
        intent.putExtra("preferred_app_intent", preferredAppIntent);
        intent.putExtra("preferred_app_intent_filter", intentFilter);
        intent.putExtra("preferred_app_type", preferredApplication);
        intent.putExtra("preferred_app_label", label);
        Preference preference = new Preference(getActivity());
        preference.setLayoutResource(2130968977);
        preference.setWidgetLayoutResource(2130968998);
        preference.setTitle(label);
        preference.setSummary(2131627588);
        preference.setIntent(intent);
        String systemPreferredPackageName = Global.getString(getContentResolver(), preferredApplication.name());
        if (TextUtils.isEmpty(systemPreferredPackageName)) {
            systemPreferredPackageName = PreferredSettingsUtils.getSystemPreferredPackageName(preferredAppIntent, resolveInfoList);
            Global.putString(getContentResolver(), preferredApplication.name(), systemPreferredPackageName);
        }
        String preferredePackageName = PreferredSettingsUtils.getPreferredPackageName(this.mPm, resolveInfoList, intentFilter, preferredAppIntent, getActivity());
        if (resolveInfoList != null) {
            for (int i = 0; i < resolveInfoList.size(); i++) {
                String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
                try {
                    if (packageName.equals(preferredePackageName)) {
                        intent.putExtra("preferred_app_package_name", packageName);
                        ApplicationInfo info = this.mPm.getApplicationInfo(packageName, 0);
                        if (!(TextUtils.isEmpty(systemPreferredPackageName) || systemPreferredPackageName.equals(info.packageName))) {
                            CharSequence summary = PreferredSettingsUtils.getApplicationlabel(this.mPm, intentFilter, info.packageName, info.loadLabel(this.mPm).toString());
                            if (!TextUtils.isEmpty(summary)) {
                                preference.setSummary(summary);
                            }
                            String newSystemPreferredPackageName = PreferredSettingsUtils.getSystemPreferredPackageName(preferredAppIntent, resolveInfoList);
                            if (!TextUtils.equals(systemPreferredPackageName, newSystemPreferredPackageName)) {
                                Global.putString(getContentResolver(), preferredApplication.name(), newSystemPreferredPackageName);
                                Log.e("PreferredListSettings", "buildPreferredPreference.correct system preferred application, old pkg:" + systemPreferredPackageName + "|new pkg:" + newSystemPreferredPackageName + "|type:" + preferredApplication.name());
                            }
                        }
                    }
                } catch (NameNotFoundException e) {
                    Log.e("PreferredListSettings", e.getMessage());
                }
            }
        }
        if (HwDeviceManager.disallowOp(17) && preferredApplication == PreferredApplication.PREFERRED_HOME) {
            preference.setEnabled(false);
        }
        getPreferenceScreen().addPreference(preference);
    }

    private void registerPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            this.mPrivacyModeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    PreferredListSettings.this.initPreferenceList();
                }
            };
            getContentResolver().registerContentObserver(Secure.getUriFor("privacy_mode_state"), true, this.mPrivacyModeObserver);
        }
    }

    private void unregisterPrivacyModeObserver() {
        if (PrivacyModeManager.isFeatrueSupported()) {
            getContentResolver().unregisterContentObserver(this.mPrivacyModeObserver);
        }
    }

    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(2131230837);
        this.mPm = getPackageManager();
        registerPrivacyModeObserver();
    }

    public void onResume() {
        super.onResume();
        initPreferenceList();
    }

    public void onDestroy() {
        unregisterPrivacyModeObserver();
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void addAssistAndVoiceInputPreference() {
        Preference assistAndVoiceInputPreference = new Preference(getActivity());
        assistAndVoiceInputPreference.setLayoutResource(2130968977);
        assistAndVoiceInputPreference.setWidgetLayoutResource(2130968998);
        assistAndVoiceInputPreference.setTitle(2131626943);
        assistAndVoiceInputPreference.setFragment("com.android.settings.applications.ManageAssist");
        getPreferenceScreen().addPreference(assistAndVoiceInputPreference);
    }
}
