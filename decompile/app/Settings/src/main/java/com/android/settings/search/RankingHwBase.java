package com.android.settings.search;

import android.util.SparseIntArray;
import com.android.settings.AdvancedSettings;
import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.ChooseSingleHandSettings;
import com.android.settings.DataSubscriptionActivity;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.EyeComfortSettings;
import com.android.settings.LauncherModeSettingsActivity;
import com.android.settings.LauncherModeSimpleActivity;
import com.android.settings.MoreAssistanceSettings;
import com.android.settings.NotificationAndStatusSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.ScreenLockSettings;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.SecurityAndPrivacySettings;
import com.android.settings.SecuritySettings;
import com.android.settings.SoundSettings;
import com.android.settings.TetherSettings;
import com.android.settings.Utils;
import com.android.settings.VirtualKeySettings;
import com.android.settings.WallpaperTypeSettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.applications.AdvancedAppSettings;
import com.android.settings.applications.AppCloneSettings;
import com.android.settings.applications.ManageApplications;
import com.android.settings.applications.PreferredListSettings;
import com.android.settings.applications.SpecialAccessSettings;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.deviceinfo.LegalInformation;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.display.ScreenZoomSettings;
import com.android.settings.fingerprint.FingerprintMainSettingsFragment;
import com.android.settings.fingerprint.FingerprintSettingsFragment;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.ScanningSettings;
import com.android.settings.mw.MultiWindowSettingsHelpOpenActivity;
import com.android.settings.navigation.NaviTrikeySettingsFragment;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.navigation.NavigationSettingsFragment;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settings.pressure.PressureResponseSettingsFragment;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.userexperience.InvitationActivity;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;
import java.util.HashMap;

class RankingHwBase {
    protected static HashMap<String, Integer> sBaseRankMap = new HashMap();
    public static int sCurrentBaseRank = 2048;
    protected static HashMap<String, Integer> sRankMap = new HashMap();
    private static SparseIntArray sRankTitleMap = new SparseIntArray();

    RankingHwBase() {
    }

    static {
        if (Utils.isChinaTelecomArea()) {
            sRankTitleMap.append(1, 2131627370);
        } else {
            sRankTitleMap.append(1, 2131627853);
        }
        sRankTitleMap.append(2, 2131624904);
        sRankTitleMap.append(3, 2131624808);
        sRankTitleMap.append(4, 2131627578);
        sRankTitleMap.append(5, 2131624582);
        sRankTitleMap.append(6, 2131627448);
        sRankTitleMap.append(7, 2131625149);
        sRankTitleMap.append(8, 2131625101);
        sRankTitleMap.append(9, 2131627934);
        sRankTitleMap.append(10, 2131626433);
        sRankTitleMap.append(11, 2131627616);
        sRankTitleMap.append(12, 2131627774);
        sRankTitleMap.append(13, 2131627498);
        sRankTitleMap.append(15, 2131624636);
        sRankTitleMap.append(16, 2131627409);
        sRankTitleMap.append(17, 2131625597);
        sRankTitleMap.append(18, 2131624060);
        sRankTitleMap.append(19, 2131627577);
        sRankTitleMap.append(20, 2131625501);
        sRankTitleMap.append(21, 2131625330);
        sRankTitleMap.append(22, 2131628554);
        sRankTitleMap.append(23, 2131628846);
        sRankTitleMap.append(24, 2131628921);
        sRankMap.put(DataSubscriptionActivity.class.getName(), Integer.valueOf(5));
        sRankMap.put(WifiSettings.class.getName(), Integer.valueOf(2));
        sRankMap.put(AdvancedWifiSettings.class.getName(), Integer.valueOf(2));
        sRankMap.put(SavedAccessPointsWifiSettings.class.getName(), Integer.valueOf(2));
        sRankMap.put(ConfigureWifiSettings.class.getName(), Integer.valueOf(2));
        sRankMap.put(BluetoothSettings.class.getName(), Integer.valueOf(3));
        sRankMap.put(SimSettings.class.getName(), Integer.valueOf(1));
        sRankMap.put(WirelessSettings.class.getName(), Integer.valueOf(5));
        sRankMap.put(DisplaySettings.class.getName(), Integer.valueOf(7));
        sRankMap.put(ScreenZoomSettings.class.getName(), Integer.valueOf(2));
        sRankMap.put(WallpaperTypeSettings.class.getName(), Integer.valueOf(7));
        sRankMap.put(ZenModeAutomationSettings.class.getName(), Integer.valueOf(14));
        sRankMap.put(SoundSettings.class.getName(), Integer.valueOf(8));
        sRankMap.put(StorageSettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(BatterySaverSettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(AdvancedAppSettings.class.getName(), Integer.valueOf(16));
        sRankMap.put(SpecialAccessSettings.class.getName(), Integer.valueOf(16));
        sRankMap.put(ManageApplications.class.getName(), Integer.valueOf(16));
        sRankMap.put(PreferredListSettings.class.getName(), Integer.valueOf(16));
        sRankMap.put(UserSettings.class.getName(), Integer.valueOf(10));
        sRankMap.put(LocationSettings.class.getName(), Integer.valueOf(24));
        sRankMap.put(ScanningSettings.class.getName(), Integer.valueOf(24));
        sRankMap.put(SecuritySettings.class.getName(), Integer.valueOf(24));
        sRankMap.put(ScreenLockSettings.class.getName(), Integer.valueOf(12));
        sRankMap.put(ChooseLockGenericFragment.class.getName(), Integer.valueOf(12));
        sRankMap.put(ScreenPinningSettings.class.getName(), Integer.valueOf(12));
        sRankMap.put(AccountSettings.class.getName(), Integer.valueOf(15));
        sRankMap.put(NotificationAndStatusSettings.class.getName(), Integer.valueOf(9));
        sRankMap.put(ChooseSingleHandSettings.class.getName(), Integer.valueOf(13));
        if (Utils.MULTI_WINDOW_ENABLED) {
            sRankMap.put(MultiWindowSettingsHelpOpenActivity.class.getName(), Integer.valueOf(13));
        }
        sRankMap.put(MoreAssistanceSettings.class.getName(), Integer.valueOf(13));
        sRankMap.put(InputMethodAndLanguageSettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(PrivacySettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(DateTimeSettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(AccessibilitySettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(PrintSettingsFragment.class.getName(), Integer.valueOf(17));
        sRankMap.put(DevelopmentSettings.class.getName(), Integer.valueOf(18));
        sRankMap.put(InvitationActivity.class.getName(), Integer.valueOf(17));
        sRankMap.put(DeviceInfoSettings.class.getName(), Integer.valueOf(20));
        sRankMap.put(LegalInformation.class.getName(), Integer.valueOf(20));
        sRankMap.put(ThirdPartyDummyIndexable.class.getName(), Integer.valueOf(21));
        sRankMap.put(AdvancedSettings.class.getName(), Integer.valueOf(17));
        sRankMap.put(LauncherModeSimpleActivity.class.getName(), Integer.valueOf(17));
        sRankMap.put(LauncherModeSettingsActivity.class.getName(), Integer.valueOf(6));
        sRankMap.put(EyeComfortSettings.class.getName(), Integer.valueOf(7));
        sRankMap.put(PressureResponseSettingsFragment.class.getName(), Integer.valueOf(13));
        sRankMap.put(TetherSettings.class.getName(), Integer.valueOf(5));
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            if (NaviUtils.isTrikeyDevice()) {
                sRankMap.put(NaviTrikeySettingsFragment.class.getName(), Integer.valueOf(23));
            } else {
                sRankMap.put(NavigationSettingsFragment.class.getName(), Integer.valueOf(23));
            }
            sRankMap.put(FingerprintSettingsFragment.class.getName(), Integer.valueOf(11));
        } else {
            sRankMap.put(FingerprintMainSettingsFragment.class.getName(), Integer.valueOf(11));
            sRankMap.put(VirtualKeySettings.class.getName(), Integer.valueOf(23));
        }
        sBaseRankMap.put("com.android.settings", Integer.valueOf(0));
        sBaseRankMap.put("com.huawei.android.dsdscardmanager", Integer.valueOf(1));
        sBaseRankMap.put("com.huawei.android.hwouc", Integer.valueOf(19));
        sBaseRankMap.put("com.huawei.android.launcher", Integer.valueOf(7));
        sBaseRankMap.put("com.huawei.vassistant", Integer.valueOf(13));
        sBaseRankMap.put("com.huawei.motionservice", Integer.valueOf(13));
        sBaseRankMap.put("com.android.phone", Integer.valueOf(5));
        sBaseRankMap.put("com.huawei.systemmanager", Integer.valueOf(21));
        sRankMap.put(AppCloneSettings.class.getName(), Integer.valueOf(22));
        sRankMap.put(SecurityAndPrivacySettings.class.getName(), Integer.valueOf(24));
    }

    public static int getTitleResId(int rank) {
        return sRankTitleMap.get(rank, -1);
    }
}
