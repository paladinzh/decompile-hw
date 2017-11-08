package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.ToggleAccessibilityServicePreferenceFragment;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.applications.InstalledAppDetailsTop;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.datausage.UnrestrictedDataAccess;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.fingerprint.FingerprintMainSettingsActivity;
import com.android.settings.fuelgauge.BatteryHistoryDetail;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.localepicker.LocaleListEditor;
import com.android.settings.location.LocationSettings;
import com.android.settings.navigation.NavigationSettingsActivity;
import com.android.settings.navigation.NavigationSettingsFragment;
import com.android.settings.nfc.AndroidBeamHwExt;
import com.android.settings.notification.NotificationAccessSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.WifiSettings;

public class Settings extends SettingsActivity {

    public static class AGPSSettingsActivity extends SettingsActivity {
    }

    public static class AccessibilityDaltonizerSettingsActivity extends SettingsActivity {
    }

    public static class AccessibilitySettingsActivity extends SettingsActivity {
    }

    public static class AccountCategorySettings extends SettingsActivity {
    }

    public static class AccountSettingsActivity extends SettingsActivity {
    }

    public static class AccountSyncSettingsActivity extends SettingsActivity {
    }

    public static class AdvancedAppsActivity extends SettingsActivity {
    }

    public static class AdvancedSettingsActivity extends SettingsActivity {
    }

    public static class AdvancedWifiSettingsActivity extends SettingsActivity {
    }

    public static class AllApplicationsActivity extends SettingsActivity {
    }

    public static class AndroidBeamSettingsActivity extends SettingsActivity {
        protected void onCreate(Bundle savedState) {
            if (NfcAdapter.getDefaultAdapter(this) == null) {
                finish();
            }
            super.onCreate(savedState);
        }
    }

    public static class ApnEditorActivity extends SettingsActivity {
        public void showBackIcon() {
        }
    }

    public static class ApnSettingsActivity extends SettingsActivity {
    }

    public static class AppCloneActivity extends SettingsActivity {
    }

    public static class AppDrawOverlaySettingsActivity extends SettingsActivity {
    }

    public static class AppMemoryUsageActivity extends SettingsActivity {
    }

    public static class AppNotificationSettingsActivity extends SettingsActivity {
    }

    public static class AppWriteSettingsActivity extends SettingsActivity {
    }

    public static class ApplicationCategorySettings extends SettingsActivity {
    }

    public static class ApplicationListPreferenceActivity extends SettingsActivity {
    }

    public static class AvailableVirtualKeyboardActivity extends SettingsActivity {
    }

    public static class BackgroundCheckSummaryActivity extends SettingsActivity {
    }

    public static class BatteryHistoryDetailActivity extends SettingsActivity {
        public boolean isValidFragment(String className) {
            if (BatteryHistoryDetail.class.getName().equals(className)) {
                return true;
            }
            return super.isValidFragment(className);
        }
    }

    public static class BatterySaverSettingsActivity extends SettingsActivity {
    }

    public static class BluetoothSettingsActivity extends SettingsActivity {
    }

    public static class BrightnessSettingsActivity extends SettingsActivity {
    }

    public static class CaptioningSettingsActivity extends SettingsActivity {
        public void finish() {
            super.finish();
            new HwAnimationReflection(this).overrideTransition(2);
        }
    }

    public static class ChooseAccountActivity extends SettingsActivity {
    }

    public static class ConfigureNotificationSettingsActivity extends SettingsActivity {
    }

    public static class CryptKeeperSettingsActivity extends SettingsActivity {
    }

    public static class DataUsageSummaryActivity extends SettingsActivity {
    }

    public static class DateTimeSettingsActivity extends SettingsActivity {
    }

    public static class DevelopmentSettingsActivity extends SettingsActivity {
    }

    public static class DeviceAdminSettingsActivity extends SettingsActivity {
    }

    public static class DeviceInfoSettingsActivity extends SettingsActivity {
    }

    public static class DeviceSettings extends SettingsActivity {
    }

    public static class DisplaySettingsActivity extends SettingsActivity {
    }

    public static class DomainsURLsAppListActivity extends SettingsActivity {
    }

    public static class DreamSettingsActivity extends SettingsActivity {
    }

    public static class EyeComfortSettingsActivity extends SettingsActivity {
    }

    public static class FingerprintEnrollSuggestionActivity extends FingerprintMainSettingsActivity {
    }

    public static class FingerprintShortcutActivity extends SettingsActivity {
    }

    public static class FingerprintSuggestionActivity extends FingerprintMainSettingsActivity {
    }

    public static class FontSizePreferenceFragmentForSetupWizardActivity extends SettingsActivity {
        public void finish() {
            super.finish();
            new HwAnimationReflection(this).overrideTransition(2);
        }
    }

    public static class HighPowerApplicationsActivity extends SettingsActivity {
    }

    public static class HomeSettingsActivity extends SettingsActivity {
    }

    public static class IccLockSettingsActivity extends SettingsActivity {
    }

    public static class ImeiInformationActivity extends SettingsActivity {
    }

    public static class InputMethodAndLanguageSettingsActivity extends SettingsActivity {
    }

    public static class InputMethodAndLanguageSettingsHomeActivity extends SettingsActivity {
    }

    public static class InternationalRoamingActivity extends SettingsActivity {
    }

    public static class KeyboardLayoutPickerActivity extends SettingsActivity {
    }

    public static class LocalePickerActivity extends SettingsActivity {
    }

    public static class LocationSettingsActivity extends SettingsActivity {
    }

    public static class MSimStatusActivity extends SettingsActivity {
    }

    public static class ManageAccountsSettingsActivity extends SettingsActivity {
    }

    public static class ManageApplicationsActivity extends SettingsActivity {
    }

    public static class ManageAssistActivity extends SettingsActivity {
    }

    public static class MemorySettingsActivity extends SettingsActivity {
    }

    public static class MoreAssistanceSettingsActivity extends SettingsActivity {
    }

    public static class NotificationAccessSettingsActivity extends SettingsActivity {
    }

    public static class NotificationAppListActivity extends SettingsActivity {
    }

    public static class OtgSettingsActivity extends SettingsActivity {
    }

    public static class OtherSoundSettingsActivity extends SettingsActivity {
    }

    public static class OverlaySettingsActivity extends SettingsActivity {
    }

    public static class PaymentSettingsActivity extends SettingsActivity {
    }

    public static class PersonalSettings extends SettingsActivity {
    }

    public static class PhysicalKeyboardActivity extends SettingsActivity {
    }

    public static class PowerUsageSummaryActivity extends SettingsActivity {
    }

    public static class PreferredListSettingsActivity extends SettingsActivity {
    }

    public static class PreferredSettingsActivity extends SettingsActivity {
    }

    public static class PrintJobSettingsActivity extends SettingsActivity {
    }

    public static class PrintSettingsActivity extends SettingsActivity {
    }

    public static class PrivacySettingsActivity extends SettingsActivity {
    }

    public static class PrivateVolumeForgetActivity extends SettingsActivity {
    }

    public static class PrivateVolumeSettingsActivity extends SettingsActivity {
    }

    public static class PublicVolumeSettingsActivity extends SettingsActivity {
    }

    public static class ResetSettingsActivity extends SettingsActivity {
    }

    public static class RunningServicesActivity extends SettingsActivity {
    }

    public static class SavedAccessPointsSettingsActivity extends SettingsActivity {
    }

    public static class ScreenLockSettingsActivity extends SettingsActivity {
    }

    public static class ScreenLockSuggestionActivity extends ChooseLockGeneric {
    }

    public static class ScreenZoomPreferenceFragmentForSetupWizardActivity extends SettingsActivity {
    }

    public static class SecurityAndPrivacySettingsActivity extends SettingsActivity {
    }

    public static class SecuritySettingsActivity extends SettingsActivity {
    }

    public static class SimSettingsActivity extends SettingsActivity {
    }

    public static class SimStatusActivity extends SettingsActivity {
    }

    public static class SingleHandScreenZoomSettingsActivity extends SettingsActivity {
    }

    public static class SingleHandeSettingsActivity extends SettingsActivity {
    }

    public static class SmartCoverSettingsActivity extends SettingsActivity {
    }

    public static class SmartEarphoneSettingsActivity extends SettingsActivity {
    }

    public static class SoundSettingsActivity extends SettingsActivity {
    }

    public static class SpellCheckersSettingsActivity extends SettingsActivity {
    }

    public static class StatusActivity extends SettingsActivity {
    }

    public static class StorageSettingsActivity extends SettingsActivity {
    }

    public static class StorageUseActivity extends SettingsActivity {
    }

    public static class SuspendButtonSettingsActivity extends SettingsActivity {
    }

    public static class SystemSettings extends SettingsActivity {
    }

    public static class TestingSettingsActivity extends SettingsActivity {
    }

    public static class TestingWifiMacAddr extends SettingsActivity {
    }

    public static class TetherSettingsActivity extends SettingsActivity {
    }

    public static class TextToSpeechSettingsActivity extends SettingsActivity {
    }

    public static class TimingTaskSettingsActivity extends SettingsActivity {
    }

    public static class TopLevelSettings extends SettingsActivity {
    }

    public static class TrustAgentSettingsActivity extends SettingsActivity {
    }

    public static class TrustedCredentialsSettingsActivity extends SettingsActivity {
    }

    public static class UsageAccessSettingsActivity extends SettingsActivity {
    }

    public static class UserDictionarySettingsActivity extends SettingsActivity {
    }

    public static class UserSettingsActivity extends SettingsActivity {
    }

    public static class VersionRequireActivity extends SettingsActivity {
    }

    public static class VirtualKeySettingsActivity extends SettingsActivity {
    }

    public static class VpnSettingsActivity extends SettingsActivity {
    }

    public static class VrListenersSettingsActivity extends SettingsActivity {
    }

    public static class WallpaperSettingsActivity extends SettingsActivity {
    }

    public static class WallpaperSuggestionActivity extends SettingsActivity {
        protected void onCreate(Bundle savedState) {
            super.onCreate(savedState);
            Intent huaweiWallpaperIntent = new Intent("com.huawei.launcher.wallpaper_setting");
            if (Utils.hasIntentActivity(getPackageManager(), huaweiWallpaperIntent)) {
                startActivity(huaweiWallpaperIntent);
                finish();
            }
        }
    }

    public static class WifiAPITestActivity extends SettingsActivity {
    }

    public static class WifiApClientManagementActivity extends SettingsActivity {
    }

    public static class WifiApSettingsActivity extends SettingsActivity {
    }

    public static class WifiBridgeSettingsActivity extends SettingsActivity {
    }

    public static class WifiCallingSettingsActivity extends SettingsActivity {
    }

    public static class WifiCallingSuggestionActivity extends SettingsActivity {
    }

    public static class WifiDisplaySettingsActivity extends SettingsActivity {
    }

    public static class WifiInfoActivity extends SettingsActivity {
    }

    public static class WifiP2pSettingsActivity extends SettingsActivity {
    }

    public static class WifiSettingsActivity extends SettingsActivity {
    }

    public static class WirelessSettings extends SettingsActivity {
    }

    public static class WirelessSettingsActivity extends SettingsActivity {
    }

    public static class WriteSettingsActivity extends SettingsActivity {
    }

    public static class ZenAccessSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeAutomationSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeAutomationSuggestionActivity extends SettingsActivity {
    }

    public static class ZenModeEventRuleSettingsActivity extends SettingsActivity {
    }

    public static class ZenModePrioritySettingsActivity extends SettingsActivity {
    }

    public static class ZenModeScheduleRuleSettingsActivity extends SettingsActivity {
    }

    public static class ZenModeSettingsActivity extends SettingsActivity {
    }

    public static class ZonePickerActivity extends SettingsActivity {
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (!Utils.isSimpleModeOn()) {
            finish();
        }
    }

    public static void resetIntentClass(Context context, Intent intent, String fragmentName) {
        if (DataUsageSummary.class.getName().equals(fragmentName)) {
            intent.setClass(context, DataUsageSummaryActivity.class);
        } else if (DevelopmentSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, DevelopmentSettingsActivity.class);
        } else if (InputMethodAndLanguageSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, InputMethodAndLanguageSettingsHomeActivity.class);
        } else if (WifiSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, WifiSettingsActivity.class);
        } else if (AdvancedWifiSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, AdvancedWifiSettingsActivity.class);
        } else if (BluetoothSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, BluetoothSettingsActivity.class);
        } else if (PrivacySettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, PrivacySettingsActivity.class);
        } else if (ResetSettingsConfirm.class.getName().equals(fragmentName)) {
            intent.setClass(context, ResetSettingsActivity.class);
        } else if (AndroidBeamHwExt.class.getName().equals(fragmentName)) {
            intent.setClass(context, AndroidBeamSettingsActivity.class);
        } else if (NotificationAccessSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, NotificationAccessSettingsActivity.class);
        } else if (LocationSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, LocationSettingsActivity.class);
        } else if (DisplaySettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, DisplaySettingsActivity.class);
        } else if (SoundSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, SoundSettingsActivity.class);
        } else if (WirelessSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, WirelessSettingsActivity.class);
        } else if (TrustAgentSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, TrustAgentSettingsActivity.class);
        } else if (SecuritySettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, SecuritySettingsActivity.class);
        } else if (AccessibilitySettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, AccessibilitySettingsActivity.class);
        } else if (NotificationAndStatusSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, NotificationAndStatusSettingsActivity.class);
        } else if (StorageSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, StorageSettingsActivity.class);
        } else if (UserSettings.class.getName().equals(fragmentName)) {
            intent.setClass(context, UserSettingsActivity.class);
        } else if (ZonePicker.class.getName().equals(fragmentName)) {
            intent.setClass(context, ZonePickerActivity.class);
        } else if (NavigationSettingsFragment.class.getName().equals(fragmentName)) {
            intent.setClass(context, NavigationSettingsActivity.class);
        } else if (InstalledAppDetails.class.getName().equals(fragmentName)) {
            intent.setClass(context, InstalledAppDetailsTop.class);
        } else if (ToggleAccessibilityServicePreferenceFragment.class.getName().equals(fragmentName) || LocaleListEditor.class.getName().equals(fragmentName) || UnrestrictedDataAccess.class.getName().equals(fragmentName)) {
            intent.setClass(context, CleanSubSettings.class);
        }
    }
}
