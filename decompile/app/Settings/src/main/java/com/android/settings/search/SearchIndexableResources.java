package com.android.settings.search;

import android.provider.SearchIndexableResource;
import com.android.settings.AdvancedSettings;
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
import com.android.settings.SecurityAndPrivacySettings;
import com.android.settings.SecuritySettings;
import com.android.settings.SoundSettings;
import com.android.settings.TetherSettings;
import com.android.settings.Utils;
import com.android.settings.VirtualKeySettings;
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
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.inputmethod.PhysicalKeyboardFragment;
import com.android.settings.location.LocationSettings;
import com.android.settings.location.ScanningSettings;
import com.android.settings.navigation.NaviTrikeySettingsFragment;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.navigation.NavigationSettingsFragment;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settings.pressure.EdgePressSettingsFragment;
import com.android.settings.pressure.PicturePressSettingsFragment;
import com.android.settings.pressure.PressureContentPreviewFragment;
import com.android.settings.pressure.PressureLauncherShortcutFragment;
import com.android.settings.pressure.PressureResponseSettingsFragment;
import com.android.settings.pressure.PressureSensitySettingsActivity;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.tts.TextToSpeechSettings;
import com.android.settings.userexperience.InvitationActivity;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settings.wifi.LiveUpdateSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;
import java.util.Collection;
import java.util.HashMap;

public final class SearchIndexableResources {
    public static int NO_DATA_RES_ID = 0;
    private static HashMap<String, SearchIndexableResource> sResMap = new HashMap();

    static {
        sResMap.put(WifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WifiSettings.class.getName()), NO_DATA_RES_ID, WifiSettings.class.getName(), 2130838433));
        sResMap.put(AdvancedWifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AdvancedWifiSettings.class.getName()), NO_DATA_RES_ID, AdvancedWifiSettings.class.getName(), 2130838433));
        sResMap.put(SavedAccessPointsWifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SavedAccessPointsWifiSettings.class.getName()), NO_DATA_RES_ID, SavedAccessPointsWifiSettings.class.getName(), 2130838433));
        sResMap.put(ConfigureWifiSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ConfigureWifiSettings.class.getName()), NO_DATA_RES_ID, ConfigureWifiSettings.class.getName(), 2130838433));
        sResMap.put(BluetoothSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(BluetoothSettings.class.getName()), NO_DATA_RES_ID, BluetoothSettings.class.getName(), 2130838350));
        sResMap.put(WirelessSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WirelessSettings.class.getName()), NO_DATA_RES_ID, WirelessSettings.class.getName(), 2130838381));
        sResMap.put(ScreenZoomSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScreenZoomSettings.class.getName()), NO_DATA_RES_ID, ScreenZoomSettings.class.getName(), 2130838366));
        sResMap.put(DisplaySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DisplaySettings.class.getName()), NO_DATA_RES_ID, DisplaySettings.class.getName(), 2130838366));
        sResMap.put(SoundSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SoundSettings.class.getName()), NO_DATA_RES_ID, SoundSettings.class.getName(), 2130838417));
        sResMap.put(ZenModeAutomationSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ZenModeAutomationSettings.class.getName()), NO_DATA_RES_ID, ZenModeAutomationSettings.class.getName(), 2130838389));
        sResMap.put(StorageSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(StorageSettings.class.getName()), NO_DATA_RES_ID, StorageSettings.class.getName(), 2130838342));
        sResMap.put(AdvancedAppSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AdvancedAppSettings.class.getName()), NO_DATA_RES_ID, AdvancedAppSettings.class.getName(), 2130838346));
        sResMap.put(SpecialAccessSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SpecialAccessSettings.class.getName()), NO_DATA_RES_ID, SpecialAccessSettings.class.getName(), 2130838346));
        sResMap.put(UserSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(UserSettings.class.getName()), NO_DATA_RES_ID, UserSettings.class.getName(), 2130838383));
        sResMap.put(LocationSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LocationSettings.class.getName()), NO_DATA_RES_ID, LocationSettings.class.getName(), 2130838428));
        sResMap.put(ScanningSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScanningSettings.class.getName()), 2131230810, ScanningSettings.class.getName(), 2130838342));
        sResMap.put(SecuritySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SecuritySettings.class.getName()), NO_DATA_RES_ID, SecuritySettings.class.getName(), 2130838428));
        sResMap.put(AccountSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AccountSettings.class.getName()), NO_DATA_RES_ID, AccountSettings.class.getName(), 2130838340));
        sResMap.put(InputMethodAndLanguageSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(InputMethodAndLanguageSettings.class.getName()), NO_DATA_RES_ID, InputMethodAndLanguageSettings.class.getName(), 2130838342));
        sResMap.put(PhysicalKeyboardFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PhysicalKeyboardFragment.class.getName()), NO_DATA_RES_ID, PhysicalKeyboardFragment.class.getName(), 2130838342));
        sResMap.put(PrivacySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PrivacySettings.class.getName()), NO_DATA_RES_ID, PrivacySettings.class.getName(), 2130838342));
        sResMap.put(DateTimeSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DateTimeSettings.class.getName()), NO_DATA_RES_ID, DateTimeSettings.class.getName(), 2130838342));
        sResMap.put(AccessibilitySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AccessibilitySettings.class.getName()), NO_DATA_RES_ID, AccessibilitySettings.class.getName(), 2130838342));
        sResMap.put(PrintSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PrintSettingsFragment.class.getName()), NO_DATA_RES_ID, PrintSettingsFragment.class.getName(), 2130838342));
        sResMap.put(DevelopmentSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DevelopmentSettings.class.getName()), NO_DATA_RES_ID, DevelopmentSettings.class.getName(), 2130838365));
        sResMap.put(DeviceInfoSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DeviceInfoSettings.class.getName()), NO_DATA_RES_ID, DeviceInfoSettings.class.getName(), 2130838338));
        sResMap.put(LegalInformation.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LegalInformation.class.getName()), NO_DATA_RES_ID, LegalInformation.class.getName(), 2130838338));
        sResMap.put(DataSubscriptionActivity.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(DataSubscriptionActivity.class.getName()), NO_DATA_RES_ID, DataSubscriptionActivity.class.getName(), 2130838362));
        sResMap.put(LauncherModeSettingsActivity.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LauncherModeSettingsActivity.class.getName()), NO_DATA_RES_ID, LauncherModeSettingsActivity.class.getName(), 2130838375));
        sResMap.put(ScreenLockSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ScreenLockSettings.class.getName()), NO_DATA_RES_ID, ScreenLockSettings.class.getName(), 2130838429));
        sResMap.put(NotificationAndStatusSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(NotificationAndStatusSettings.class.getName()), NO_DATA_RES_ID, NotificationAndStatusSettings.class.getName(), 2130838394));
        sResMap.put(ChooseSingleHandSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ChooseSingleHandSettings.class.getName()), NO_DATA_RES_ID, ChooseSingleHandSettings.class.getName(), 2130838339));
        sResMap.put(MoreAssistanceSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(MoreAssistanceSettings.class.getName()), NO_DATA_RES_ID, MoreAssistanceSettings.class.getName(), 2130838339));
        sResMap.put(AdvancedSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AdvancedSettings.class.getName()), NO_DATA_RES_ID, AdvancedSettings.class.getName(), 2130838342));
        sResMap.put(InvitationActivity.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(InvitationActivity.class.getName()), NO_DATA_RES_ID, InvitationActivity.class.getName(), 2130838342));
        sResMap.put(PreferredListSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PreferredListSettings.class.getName()), NO_DATA_RES_ID, PreferredListSettings.class.getName(), 2130838346));
        sResMap.put(ManageApplications.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ManageApplications.class.getName()), NO_DATA_RES_ID, ManageApplications.class.getName(), 2130838346));
        sResMap.put(ThirdPartyDummyIndexable.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(ThirdPartyDummyIndexable.class.getName()), NO_DATA_RES_ID, ThirdPartyDummyIndexable.class.getName(), 2130838338));
        sResMap.put(EdgePressSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PressureResponseSettingsFragment.class.getName()), NO_DATA_RES_ID, EdgePressSettingsFragment.class.getName(), 2130838339));
        sResMap.put(PressureLauncherShortcutFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PressureResponseSettingsFragment.class.getName()), NO_DATA_RES_ID, PressureLauncherShortcutFragment.class.getName(), 2130838339));
        sResMap.put(PicturePressSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PressureResponseSettingsFragment.class.getName()), NO_DATA_RES_ID, PicturePressSettingsFragment.class.getName(), 2130838339));
        sResMap.put(PressureContentPreviewFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PressureResponseSettingsFragment.class.getName()), NO_DATA_RES_ID, PressureContentPreviewFragment.class.getName(), 2130838339));
        sResMap.put(PressureSensitySettingsActivity.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(PressureResponseSettingsFragment.class.getName()), NO_DATA_RES_ID, PressureSensitySettingsActivity.class.getName(), 2130838339));
        sResMap.put(LauncherModeSimpleActivity.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(LauncherModeSimpleActivity.class.getName()), NO_DATA_RES_ID, LauncherModeSimpleActivity.class.getName(), 2130838342));
        sResMap.put(EyeComfortSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(EyeComfortSettings.class.getName()), NO_DATA_RES_ID, EyeComfortSettings.class.getName(), 2130838366));
        sResMap.put(TextToSpeechSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AccessibilitySettings.class.getName()), NO_DATA_RES_ID, TextToSpeechSettings.class.getName(), 2130838342));
        sResMap.put(AppCloneSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(AppCloneSettings.class.getName()), NO_DATA_RES_ID, AppCloneSettings.class.getName(), 2130838344));
        if (Utils.isOwnerUser()) {
            sResMap.put(TetherSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(TetherSettings.class.getName()), NO_DATA_RES_ID, TetherSettings.class.getName(), 2130838381));
        }
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            if (NaviUtils.isTrikeyDevice()) {
                sResMap.put(NaviTrikeySettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(NaviTrikeySettingsFragment.class.getName()), NO_DATA_RES_ID, NaviTrikeySettingsFragment.class.getName(), 2130838385));
            } else {
                sResMap.put(NavigationSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(NavigationSettingsFragment.class.getName()), NO_DATA_RES_ID, NavigationSettingsFragment.class.getName(), 2130838385));
                sResMap.put(VirtualKeySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(VirtualKeySettings.class.getName()), NO_DATA_RES_ID, VirtualKeySettings.class.getName(), 2130838385));
            }
            sResMap.put(FingerprintSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(FingerprintSettingsFragment.class.getName()), NO_DATA_RES_ID, FingerprintSettingsFragment.class.getName(), 2130837745));
        } else {
            sResMap.put(VirtualKeySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(VirtualKeySettings.class.getName()), NO_DATA_RES_ID, VirtualKeySettings.class.getName(), 2130838385));
            sResMap.put(FingerprintMainSettingsFragment.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(FingerprintMainSettingsFragment.class.getName()), NO_DATA_RES_ID, FingerprintMainSettingsFragment.class.getName(), 2130837745));
        }
        sResMap.put(LiveUpdateSettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(WirelessSettings.class.getName()), NO_DATA_RES_ID, LiveUpdateSettings.class.getName(), 2130838381));
        sResMap.put(SecurityAndPrivacySettings.class.getName(), new SearchIndexableResource(Ranking.getRankForClassName(SecurityAndPrivacySettings.class.getName()), NO_DATA_RES_ID, SecurityAndPrivacySettings.class.getName(), 2130838428));
    }

    private SearchIndexableResources() {
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return (SearchIndexableResource) sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}
