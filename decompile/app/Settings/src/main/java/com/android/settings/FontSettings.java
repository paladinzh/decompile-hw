package com.android.settings;

import android.app.ActivityManagerNative;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.huawei.cust.HwCustUtils;

public class FontSettings extends SettingsPreferenceFragment {
    private final Configuration mCurConfig = new Configuration();
    private PreferenceScreen mFontSizePref;
    private Preference mFontStylePreference;
    private HwCustFontSettings mHwCustFontSettings;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230793);
        this.mHwCustFontSettings = (HwCustFontSettings) HwCustUtils.createObj(HwCustFontSettings.class, new Object[]{getActivity()});
        this.mFontSizePref = (PreferenceScreen) findPreference("font_size");
        this.mFontStylePreference = findPreference("font_style");
        if (this.mFontStylePreference == null) {
            return;
        }
        if (!Utils.hasIntentActivity(getPackageManager(), this.mFontStylePreference.getIntent()) || (this.mHwCustFontSettings != null && this.mHwCustFontSettings.hideSettingsFontStyle())) {
            removePreference("font_style");
        }
    }

    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        updateFontSizePreference(this.mFontSizePref);
        updateFontStyleSummary();
    }

    public void updateFontSizePreference(PreferenceScreen pref) {
        if (getActivity() != null && pref != null) {
            try {
                this.mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            } catch (RemoteException e) {
                Log.w("DisplaySettings", "Unable to retrieve font size");
            }
            pref.setSummary(getResources().getStringArray(2131361838)[FontsizeSettingsActivity.floatToIndex(this.mCurConfig.fontScale, getActivity())]);
        }
    }

    private void updateFontStyleSummary() {
        new ThemeQueryHandler(getContentResolver(), this.mFontStylePreference).startQuery(0, null, ThemeQueryHandler.URI_MODULE_INFO, new String[]{"display_name_en", "display_name_zh"}, "module_name=?", new String[]{"fonts"}, null);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
