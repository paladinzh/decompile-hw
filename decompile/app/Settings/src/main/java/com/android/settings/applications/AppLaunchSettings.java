package com.android.settings.applications;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.Utils;
import java.util.List;

public class AppLaunchSettings extends AppInfoWithHeader implements OnClickListener, OnPreferenceChangeListener {
    private static final Intent sBrowserIntent = new Intent().setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse("http:"));
    private AppDomainsPreference mAppDomainUrls;
    private ListPreference mAppLinkState;
    private ClearDefaultsPreference mClearDefaultsPreference;
    private boolean mHasDomainUrls;
    private boolean mIsBrowser;
    private PackageManager mPm;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230803);
        this.mAppDomainUrls = (AppDomainsPreference) findPreference("app_launch_supported_domain_urls");
        this.mClearDefaultsPreference = (ClearDefaultsPreference) findPreference("app_launch_clear_defaults");
        this.mAppLinkState = (ListPreference) findPreference("app_link_state");
        this.mPm = getActivity().getPackageManager();
        this.mIsBrowser = isBrowserApp(this.mPackageName);
        if (this.mAppEntry == null) {
            Log.e("AppLaunchSettings", "AppLaunchSettings-->onCreate-->mAppEntry is null !");
        }
        boolean z = this.mAppEntry != null ? (this.mAppEntry.info.privateFlags & 16) != 0 : false;
        this.mHasDomainUrls = z;
        CharSequence[] charSequenceArr = null;
        if (!this.mIsBrowser) {
            charSequenceArr = getEntries(this.mPackageName, this.mPm.getIntentFilterVerifications(this.mPackageName), this.mPm.getAllIntentFilters(this.mPackageName));
            this.mAppDomainUrls.setTitles(charSequenceArr);
            this.mAppDomainUrls.setValues(new int[charSequenceArr.length]);
        }
        buildStateDropDown();
        if (charSequenceArr == null || charSequenceArr.length == 0) {
            this.mAppDomainUrls.setEnabled(false);
            this.mAppLinkState.setEnabled(false);
        }
    }

    private boolean isBrowserApp(String packageName) {
        sBrowserIntent.setPackage(packageName);
        List<ResolveInfo> list = this.mPm.queryIntentActivitiesAsUser(sBrowserIntent, 131072, UserHandle.myUserId());
        int count = list.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (info.activityInfo != null && info.handleAllWebDataURI) {
                return true;
            }
        }
        return false;
    }

    private void buildStateDropDown() {
        if (this.mIsBrowser) {
            this.mAppLinkState.setShouldDisableView(true);
            this.mAppLinkState.setEnabled(false);
            this.mAppDomainUrls.setShouldDisableView(true);
            this.mAppDomainUrls.setEnabled(false);
            return;
        }
        this.mAppLinkState.setEntries(new CharSequence[]{getString(2131628163), getString(2131628164), getString(2131628165)});
        this.mAppLinkState.setEntryValues(new CharSequence[]{Integer.toString(2), Integer.toString(4), Integer.toString(3)});
        this.mAppLinkState.setEnabled(this.mHasDomainUrls);
        if (this.mHasDomainUrls) {
            int state = this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, UserHandle.myUserId());
            ListPreference listPreference = this.mAppLinkState;
            if (state == 0) {
                state = 4;
            }
            listPreference.setValue(Integer.toString(state));
            Utils.refreshListPreferenceSummary(this.mAppLinkState, null);
            this.mAppLinkState.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean success = AppLaunchSettings.this.updateAppLinkState(Integer.parseInt(newValue.toString()));
                    if (success) {
                        Utils.refreshListPreferenceSummary((ListPreference) preference, newValue.toString());
                    }
                    return success;
                }
            });
        }
    }

    private boolean updateAppLinkState(int newState) {
        if (this.mIsBrowser) {
            return false;
        }
        int userId = UserHandle.myUserId();
        if (this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, userId) == newState) {
            return false;
        }
        boolean success = this.mPm.updateIntentVerificationStatusAsUser(this.mPackageName, newState, userId);
        if (success) {
            success = newState == this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, userId);
        } else {
            Log.e("AppLaunchSettings", "Couldn't update intent verification status!");
        }
        return success;
    }

    private CharSequence[] getEntries(String packageName, List<IntentFilterVerificationInfo> list, List<IntentFilter> list2) {
        ArraySet<String> result = Utils.getHandledDomains(this.mPm, packageName);
        return (CharSequence[]) result.toArray(new CharSequence[result.size()]);
    }

    protected boolean refreshUi() {
        this.mClearDefaultsPreference.setPackageName(this.mPackageName);
        this.mClearDefaultsPreference.setAppEntry(this.mAppEntry);
        return true;
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public void onClick(View v) {
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    protected int getMetricsCategory() {
        return 17;
    }
}
