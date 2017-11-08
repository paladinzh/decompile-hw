package com.android.settings;

import android.os.SystemProperties;
import android.view.ViewGroup;
import com.android.settings.fingerprint.ApplicationListPreference;
import com.android.settings.fingerprint.FingerprintShortcutActivity;
import com.android.settingslib.drawer.DashboardCategory;
import java.util.List;

public class HwCustSettingsActivityImpl extends HwCustSettingsActivity {
    private static final String ACTION_HONOR_UPDATE = "com.honor.broadcast.action.SUBSCRIPTION";
    private static final int ALL_TAB = 1;
    private static final String BORWSER_APP = "com.skyfire.browser.toolbar.intent.action.VIEW_SETTINGS";
    private static final String DMCLIENT_APP = "com.huawei.android.dmclient";
    private static final String DSS_PACKAGE_NAME = "com.sprint.dsa";
    private static final String[] ENTRY_FRAGMENTS = new String[]{OtgSettings.class.getName(), TestingWifiMacAddr.class.getName(), ApplicationListPreference.class.getName(), FingerprintShortcutActivity.class.getName()};
    private static final int GENERAL_TAB = 0;
    private static final boolean HAS_HONOR_UPDATE = SystemProperties.getBoolean("ro.config.enable_honor_push", false);
    private static final boolean HAS_PEDOMETER = SystemProperties.getBoolean("ro.config.StepCalculator", false);
    private static final boolean HIDE_SMART_ASSISTANCE = SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false);
    private static final boolean IS_ATT_PHONE_OPTA = SystemProperties.get("ro.config.hw_opta", "0").equals("07");
    private static final boolean IS_ATT_PHONE_OPTB = SystemProperties.get("ro.config.hw_optb", "0").equals("840");
    private static final boolean IS_CHINA_TELECOM_OPTA_OPTB = SystemProperties.get("ro.config.hw_opta", "0").equals("92");
    private static final String ROAMING_UI_STATUS = "1";
    private static final String SMART_KEY = "com.android.huawei.smartkey";
    private static final String SOS_PACKAGE_NAME = "com.huawei.sos";
    private static final String TAG = "HwCustSettingsActivityImpl";
    private ViewGroup mWrapMainContentView;

    public HwCustSettingsActivityImpl(SettingsActivity settingsActivity) {
        super(settingsActivity);
    }

    public boolean isValidFragment(String fragmentName) {
        for (String equals : ENTRY_FRAGMENTS) {
            if (equals.equals(fragmentName)) {
                return true;
            }
        }
        return false;
    }

    public void configChangedForPaddingOrColor() {
        if (isShowPaddingorColor() && this.mWrapMainContentView != null) {
            if (this.mSettingsActivity.getResources().getConfiguration().orientation == 2) {
                this.mWrapMainContentView.setPadding(this.mSettingsActivity.getResources().getDimensionPixelSize(2131558633), 0, this.mSettingsActivity.getResources().getDimensionPixelSize(2131558633), 0);
            } else if (this.mSettingsActivity.getResources().getConfiguration().orientation == 1) {
                this.mWrapMainContentView.setPadding(0, 0, 0, 0);
            }
        }
    }

    public void initWrapMainContentView() {
        if (isShowPaddingorColor()) {
            this.mWrapMainContentView = (ViewGroup) this.mSettingsActivity.findViewById(2131887153);
        }
    }

    public boolean isShowPaddingorColor() {
        return this.mSettingsActivity.getResources().getBoolean(2131492926);
    }

    public boolean hideNfcMenu() {
        return SystemProperties.getBoolean("ro.config.hide_nfc", true);
    }

    public void hideSmartAssistance(List<DashboardCategory> dashboardCategories) {
        if (HIDE_SMART_ASSISTANCE) {
            int size = dashboardCategories.size();
            for (int i = 0; i < size; i++) {
                DashboardCategory category = (DashboardCategory) dashboardCategories.get(i);
                if (category.priority == 2131887622) {
                    dashboardCategories.remove(category);
                    return;
                }
            }
        }
    }
}
