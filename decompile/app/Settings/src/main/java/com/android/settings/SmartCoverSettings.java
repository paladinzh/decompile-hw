package com.android.settings;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import com.huawei.cust.HwCustUtils;

public class SmartCoverSettings extends SettingsPreferenceFragment {
    private static final boolean IS_SHOW_GRID_SMART_COVER = SystemProperties.getBoolean("ro.config.show_smart_cover", false);
    private ImageViewPreference mCoverTutorialPreference;
    private SmartCoverSelectionPreference mCoverTypePreference;
    private HwCustSmartCoverSettings mHwCustSmartCoverSettings;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (SmartCoverSettings.this.mCoverTypePreference != null) {
                SmartCoverSettings.this.mCoverTypePreference.setSelectionEnabled(SmartCoverSettings.this.isSmartCoverEnabled());
            }
        }
    };
    private ShowPedometerEnabler mShowPedometerEnabler;
    private SwitchPreference mShowPedometerSwitchPreference;
    private SmartCoverEnabler mSmartCoverEnabler;
    private SwitchPreference mSmartCoverSwitchPreference;

    private boolean isSmartCoverEnabled() {
        if (1 == Global.getInt(getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, 1)) {
            return true;
        }
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IS_SHOW_GRID_SMART_COVER) {
            addPreferencesFromResource(2131230896);
            this.mCoverTutorialPreference = (ImageViewPreference) findPreference("smart_cover_tutorial");
            this.mHwCustSmartCoverSettings = (HwCustSmartCoverSettings) HwCustUtils.createObj(HwCustSmartCoverSettings.class, new Object[]{getActivity()});
            this.mHwCustSmartCoverSettings.inflateCustPreferenceScreen(this, this.mCoverTutorialPreference);
        } else {
            addPreferencesFromResource(2131230895);
            this.mCoverTypePreference = (SmartCoverSelectionPreference) findPreference("smart_cover_tutorial");
        }
        this.mSmartCoverSwitchPreference = (SwitchPreference) findPreference("smart_cover_switch");
        this.mShowPedometerSwitchPreference = (SwitchPreference) findPreference("show_pedometer_switch");
        this.mSmartCoverEnabler = new SmartCoverEnabler(getActivity(), this.mSmartCoverSwitchPreference);
        this.mShowPedometerEnabler = new ShowPedometerEnabler(getActivity(), this.mShowPedometerSwitchPreference);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (!Utils.isHwHealthPackageExist(getActivity())) {
            removePreference("show_pedometer_switch");
        }
    }

    public void onResume() {
        super.onResume();
        this.mSmartCoverEnabler.resume();
        this.mShowPedometerEnabler.resume();
        getContentResolver().registerContentObserver(Global.getUriFor(HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED), true, this.mObserver);
        if (this.mHwCustSmartCoverSettings != null) {
            this.mHwCustSmartCoverSettings.onResume();
        }
    }

    public void onPause() {
        super.onPause();
        this.mSmartCoverEnabler.pause();
        this.mShowPedometerEnabler.pause();
        getContentResolver().unregisterContentObserver(this.mObserver);
        ItemUseStat.getInstance().cacheData(getActivity());
        if (this.mHwCustSmartCoverSettings != null) {
            this.mHwCustSmartCoverSettings.onPause();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
