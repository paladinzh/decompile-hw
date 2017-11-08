package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager.OnPreferenceTreeClickListener;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Jlog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment;
import com.android.settings.ChooseLockPattern.ChooseLockPatternFragment;
import com.android.settings.Settings.AccessibilitySettingsActivity;
import com.android.settings.Settings.AccountSettingsActivity;
import com.android.settings.Settings.AdvancedSettingsActivity;
import com.android.settings.Settings.AppCloneActivity;
import com.android.settings.Settings.BluetoothSettingsActivity;
import com.android.settings.Settings.DataUsageSummaryActivity;
import com.android.settings.Settings.DateTimeSettingsActivity;
import com.android.settings.Settings.DevelopmentSettingsActivity;
import com.android.settings.Settings.DeviceInfoSettingsActivity;
import com.android.settings.Settings.DeviceSettings;
import com.android.settings.Settings.DisplaySettingsActivity;
import com.android.settings.Settings.HomeSettingsActivity;
import com.android.settings.Settings.InputMethodAndLanguageSettingsActivity;
import com.android.settings.Settings.LocationSettingsActivity;
import com.android.settings.Settings.ManageApplicationsActivity;
import com.android.settings.Settings.MoreAssistanceSettingsActivity;
import com.android.settings.Settings.PaymentSettingsActivity;
import com.android.settings.Settings.PersonalSettings;
import com.android.settings.Settings.PowerUsageSummaryActivity;
import com.android.settings.Settings.PrintSettingsActivity;
import com.android.settings.Settings.ScreenLockSettingsActivity;
import com.android.settings.Settings.SecurityAndPrivacySettingsActivity;
import com.android.settings.Settings.SecuritySettingsActivity;
import com.android.settings.Settings.SimSettingsActivity;
import com.android.settings.Settings.SoundSettingsActivity;
import com.android.settings.Settings.StorageSettingsActivity;
import com.android.settings.Settings.UserSettingsActivity;
import com.android.settings.Settings.VirtualKeySettingsActivity;
import com.android.settings.Settings.WallpaperSuggestionActivity;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settings.Settings.WirelessSettings;
import com.android.settings.Settings.WirelessSettingsActivity;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.AccessibilitySettingsForSetupWizard;
import com.android.settings.accessibility.CaptionPropertiesFragment;
import com.android.settings.accessibility.FontSizePreferenceFragmentForSetupWizard;
import com.android.settings.accessibility.ToggleDaltonizerPreferenceFragment;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.accounts.AccountSyncSettings;
import com.android.settings.accounts.ChooseAccountActivity;
import com.android.settings.accounts.ManageAccountsSettings;
import com.android.settings.accounts.ManagedProfileSettings;
import com.android.settings.applications.AdvancedAppSettings;
import com.android.settings.applications.AppCloneSettings;
import com.android.settings.applications.AppCloneUtils;
import com.android.settings.applications.DrawOverlayDetails;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.applications.ManageApplications;
import com.android.settings.applications.ManageAssist;
import com.android.settings.applications.NotificationApps;
import com.android.settings.applications.PreferredListSettings;
import com.android.settings.applications.PreferredSettings;
import com.android.settings.applications.ProcessStatsSummary;
import com.android.settings.applications.ProcessStatsUi;
import com.android.settings.applications.UsageAccessDetails;
import com.android.settings.applications.VrListenerSettings;
import com.android.settings.applications.WriteSettingsDetails;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.dashboard.DashboardSummary;
import com.android.settings.dashboard.SearchResultsSummary;
import com.android.settings.datausage.DataUsageMeteredSettings;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.deviceinfo.ImeiInformation;
import com.android.settings.deviceinfo.MSimStatus;
import com.android.settings.deviceinfo.PrivateVolumeForget;
import com.android.settings.deviceinfo.PrivateVolumeSettings;
import com.android.settings.deviceinfo.PublicVolumeSettings;
import com.android.settings.deviceinfo.SimStatus;
import com.android.settings.deviceinfo.Status;
import com.android.settings.deviceinfo.StorageSettings;
import com.android.settings.display.ScreenZoomPreferenceFragmentForSetupWizard;
import com.android.settings.fingerprint.FingerprintMainSettingsActivity;
import com.android.settings.fingerprint.FingerprintSettingsActivity;
import com.android.settings.fingerprint.FingerprintSettingsFragment;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageDetail;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.AvailableVirtualKeyboardFragment;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.inputmethod.KeyboardLayoutPickerFragment;
import com.android.settings.inputmethod.KeyboardLayoutPickerFragment2;
import com.android.settings.inputmethod.PhysicalKeyboardFragment;
import com.android.settings.inputmethod.SpellCheckersSettings;
import com.android.settings.inputmethod.UserDictionaryList;
import com.android.settings.localepicker.LocaleListEditor;
import com.android.settings.location.LocationAssistSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.navigation.NaviTrikeySettingsActivity;
import com.android.settings.navigation.NaviTrikeySettingsFragment;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.navigation.NavigationSettingsActivity;
import com.android.settings.navigation.NavigationSettingsFragment;
import com.android.settings.nfc.AndroidBeam;
import com.android.settings.nfc.AndroidBeamHwExt;
import com.android.settings.nfc.DefaultPayAPPSetting;
import com.android.settings.nfc.PaymentSettings;
import com.android.settings.notification.AppNotificationSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.NotificationAccessSettings;
import com.android.settings.notification.NotificationStation;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.ZenAccessSettings;
import com.android.settings.notification.ZenModeAutomationSettings;
import com.android.settings.notification.ZenModeEventRuleSettings;
import com.android.settings.notification.ZenModePrioritySettings;
import com.android.settings.notification.ZenModeScheduleRuleSettings;
import com.android.settings.notification.ZenModeVisualInterruptionSettings;
import com.android.settings.print.PrintJobSettingsFragment;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.qstile.DevelopmentTiles;
import com.android.settings.search.DynamicIndexableContentMonitor;
import com.android.settings.search.Index;
import com.android.settings.search.IndexDatabaseHelper;
import com.android.settings.search.ThirdPartyDummyIndexable;
import com.android.settings.sim.SimSettings;
import com.android.settings.tts.TextToSpeechSettings;
import com.android.settings.users.UserSettings;
import com.android.settings.vpn2.VpnSettings;
import com.android.settings.wfd.WifiDisplaySettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiAPITest;
import com.android.settings.wifi.WifiInfo;
import com.android.settings.wifi.WifiSettings;
import com.android.settings.wifi.ap.WifiApClientManagement;
import com.android.settings.wifi.ap.WifiApSettings;
import com.android.settings.wifi.bridge.WifiBridgeSettings;
import com.android.settings.wifi.cmcc.WifiPrioritySettings;
import com.android.settings.wifi.p2p.WifiP2pSettings;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.DrawerLayoutEx;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settingslib.drawer.Tile;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SettingsActivity extends SettingsDrawerActivity implements OnPreferenceTreeClickListener, OnPreferenceStartFragmentCallback, ButtonBarHandler, OnBackStackChangedListener, OnQueryTextListener, OnCloseListener, OnActionExpandListener {
    private static final String[] ENTRY_FRAGMENTS = new String[]{WirelessSettings.class.getName(), WifiSettings.class.getName(), AdvancedWifiSettings.class.getName(), SavedAccessPointsWifiSettings.class.getName(), BluetoothSettings.class.getName(), SimSettings.class.getName(), TetherSettings.class.getName(), WifiP2pSettings.class.getName(), VpnSettings.class.getName(), DateTimeSettings.class.getName(), LocaleListEditor.class.getName(), InputMethodAndLanguageSettings.class.getName(), AvailableVirtualKeyboardFragment.class.getName(), SpellCheckersSettings.class.getName(), UserDictionaryList.class.getName(), UserDictionarySettings.class.getName(), HomeSettings.class.getName(), DisplaySettings.class.getName(), DeviceInfoSettings.class.getName(), ManageApplications.class.getName(), NotificationApps.class.getName(), ManageAssist.class.getName(), ProcessStatsUi.class.getName(), NotificationStation.class.getName(), LocationSettings.class.getName(), SecuritySettings.class.getName(), UsageAccessDetails.class.getName(), PrivacySettings.class.getName(), DeviceAdminSettings.class.getName(), AccessibilitySettings.class.getName(), AccessibilitySettingsForSetupWizard.class.getName(), CaptionPropertiesFragment.class.getName(), ToggleDaltonizerPreferenceFragment.class.getName(), TextToSpeechSettings.class.getName(), StorageSettings.class.getName(), PrivateVolumeForget.class.getName(), PrivateVolumeSettings.class.getName(), PublicVolumeSettings.class.getName(), DevelopmentSettings.class.getName(), AndroidBeam.class.getName(), AndroidBeamHwExt.class.getName(), WifiDisplaySettings.class.getName(), PowerUsageSummary.class.getName(), AccountSyncSettings.class.getName(), AccountSettings.class.getName(), CryptKeeperSettings.class.getName(), DataUsageSummary.class.getName(), DreamSettingsHw.class.getName(), UserSettings.class.getName(), NotificationAccessSettings.class.getName(), ZenAccessSettings.class.getName(), PrintSettingsFragment.class.getName(), PrintJobSettingsFragment.class.getName(), TrustedCredentialsSettings.class.getName(), PaymentSettings.class.getName(), DefaultPayAPPSetting.class.getName(), KeyboardLayoutPickerFragment.class.getName(), KeyboardLayoutPickerFragment2.class.getName(), PhysicalKeyboardFragment.class.getName(), SoundSettings.class.getName(), ConfigureNotificationSettings.class.getName(), ChooseLockPasswordFragment.class.getName(), ChooseLockPatternFragment.class.getName(), InstalledAppDetails.class.getName(), BatterySaverSettings.class.getName(), AppNotificationSettings.class.getName(), OtherSoundSettings.class.getName(), ApnSettings.class.getName(), ApnEditor.class.getName(), WifiCallingSettings.class.getName(), ZenModePrioritySettings.class.getName(), ZenModeAutomationSettings.class.getName(), ZenModeScheduleRuleSettings.class.getName(), ZenModeEventRuleSettings.class.getName(), ZenModeVisualInterruptionSettings.class.getName(), ProcessStatsUi.class.getName(), PowerUsageDetail.class.getName(), ProcessStatsSummary.class.getName(), DrawOverlayDetails.class.getName(), WriteSettingsDetails.class.getName(), AdvancedAppSettings.class.getName(), WallpaperTypeSettings.class.getName(), VrListenerSettings.class.getName(), ManagedProfileSettings.class.getName(), ChooseAccountActivity.class.getName(), IccLockSettings.class.getName(), ImeiInformation.class.getName(), SimStatus.class.getName(), Status.class.getName(), TestingSettings.class.getName(), WifiAPITest.class.getName(), WifiInfo.class.getName(), ManageAccountsSettings.class.getName(), ScreenLockSettings.class.getName(), ZonePicker.class.getName(), ResetSettingsConfirm.class.getName(), ProxySelector.class.getName(), SingleHandSettings.class.getName(), SingleHandScreenZoomSettings.class.getName(), SuspendButtonSettings.class.getName(), PreferredListSettings.class.getName(), PreferredSettings.class.getName(), SmartCoverSettings.class.getName(), TrustAgentSettings.class.getName(), TimingTaskSettings.class.getName(), SmartEarphoneSettings.class.getName(), LocationAssistSettings.class.getName(), BrightnessSettings.class.getName(), MSimStatus.class.getName(), InternationalRoaming.class.getName(), AdvancedSettings.class.getName(), SecurityAndPrivacySettings.class.getName(), MoreAssistanceSettings.class.getName(), DataUsageMeteredSettings.class.getName(), AGPSSettings.class.getName(), VersionRequire.class.getName(), WifiApSettings.class.getName(), WifiApClientManagement.class.getName(), WifiBridgeSettings.class.getName(), FontSizePreferenceFragmentForSetupWizard.class.getName(), ScreenZoomPreferenceFragmentForSetupWizard.class.getName(), WifiPrioritySettings.class.getName(), EyeComfortSettings.class.getName(), AppCloneSettings.class.getName(), CallEncryptionSettings.class.getName(), VirtualKeySettings.class.getName(), NavigationSettingsFragment.class.getName(), FingerprintSettingsFragment.class.getName(), NaviTrikeySettingsFragment.class.getName()};
    private static final String[] LIKE_SHORTCUT_INTENT_ACTION_ARRAY = new String[]{"android.settings.APPLICATION_DETAILS_SETTINGS"};
    private String[] SETTINGS_FOR_RESTRICTED = new String[]{WifiSettingsActivity.class.getName(), BluetoothSettingsActivity.class.getName(), DataUsageSummaryActivity.class.getName(), SimSettingsActivity.class.getName(), WirelessSettingsActivity.class.getName(), DataSubscriptionActivity.class.getName(), LauncherModeSettingsActivity.class.getName(), HomeSettingsActivity.class.getName(), SoundSettingsActivity.class.getName(), DisplaySettingsActivity.class.getName(), StorageSettingsActivity.class.getName(), ManageApplicationsActivity.class.getName(), PowerUsageSummaryActivity.class.getName(), NotificationAndStatusSettingsActivity.class.getName(), FingerprintMainSettingsActivity.class.getName(), ScreenLockSettingsActivity.class.getName(), MoreAssistanceSettingsActivity.class.getName(), LocationSettingsActivity.class.getName(), SecuritySettingsActivity.class.getName(), InputMethodAndLanguageSettingsActivity.class.getName(), UserSettingsActivity.class.getName(), AccountSettingsActivity.class.getName(), SecurityAndPrivacySettingsActivity.class.getName(), AdvancedSettingsActivity.class.getName(), DateTimeSettingsActivity.class.getName(), DeviceInfoSettingsActivity.class.getName(), AccessibilitySettingsActivity.class.getName(), PrintSettingsActivity.class.getName(), PaymentSettingsActivity.class.getName(), NavigationSettingsActivity.class.getName(), VirtualKeySettingsActivity.class.getName(), FingerprintSettingsActivity.class.getName(), NaviTrikeySettingsActivity.class.getName()};
    protected ActionBar mActionBar;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean batteryPresent = Utils.isBatteryPresent(intent);
                if (SettingsActivity.this.mBatteryPresent != batteryPresent) {
                    SettingsActivity.this.mBatteryPresent = batteryPresent;
                    SettingsActivity.this.updateTilesList();
                }
            }
        }
    };
    private boolean mBatteryPresent = true;
    private ArrayList<DashboardCategory> mCategories = new ArrayList();
    protected ViewGroup mContent;
    private ContextMenuClosedListener mContextMenuClosedListener;
    private ComponentName mCurrentSuggestion;
    private SharedPreferences mDevelopmentPreferences;
    private OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;
    protected boolean mDisplayHomeAsUpEnabled;
    protected boolean mDisplaySearch;
    private final DynamicIndexableContentMonitor mDynamicIndexableContentMonitor = new DynamicIndexableContentMonitor();
    private String mFragmentClass;
    protected HwCustSettingsActivity mHwCustSettingsActivity;
    private HwCustSplitUtils mHwCustSplitUtils;
    private CharSequence mInitialTitle;
    private int mInitialTitleResId;
    private boolean mIsJlogAlreadyEnabled = false;
    protected boolean mIsShortcut;
    protected boolean mIsShowingDashboard;
    private int mMainContentId = 2131887151;
    private boolean mNeedToClearFocusOfSearchView = false;
    protected boolean mNeedToRevertToInitialFragment = false;
    protected boolean mNeedToSwitchToFragment = true;
    private Button mNextButton;
    private ContentObserver mParentCtrlObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Index.getInstance(SettingsActivity.this).updateFromClassNameResource(DevelopmentSettings.class.getName(), true, true);
            Index.getInstance(SettingsActivity.this).updateFromClassNameResource(UserSettings.class.getName(), true, true);
        }
    };
    private boolean mRegistered;
    private boolean mRequestEnableJlog = true;
    private Intent mResultIntentData;
    protected MenuItem mSearchMenuItem;
    protected boolean mSearchMenuItemExpanded = false;
    protected String mSearchQuery;
    protected SearchResultsSummary mSearchResultsFragment;
    protected SearchView mSearchView;
    private final BroadcastReceiver mUserAddRemoveReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.USER_ADDED") || action.equals("android.intent.action.USER_REMOVED")) {
                Index.getInstance(SettingsActivity.this.getApplicationContext()).update();
            }
        }
    };

    public interface ContextMenuClosedListener {
        void onContextMenuClosed();
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        CharSequence title = pref.getTitle();
        if (pref.getFragment().equals(WallpaperTypeSettings.class.getName())) {
            title = getString(2131625164);
        }
        startPreferencePanel(pref.getFragment(), pref.getExtras(), -1, title, null, 0);
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Index.getInstance(this).update();
    }

    public void setSpliterLineVis(boolean visible) {
        View view = this.mContent.findViewById(2131887629);
        if (view != null && (this.mContent instanceof FrameLayout)) {
            FrameLayout frame = this.mContent;
            view.setVisibility(8);
            frame.removeView(view);
            if (this.mActionBar != null) {
                this.mActionBar.setBackgroundDrawable(getResources().getDrawable(2130837582));
            }
        }
        if (visible && this.mHwCustSplitUtils.reachSplitSize()) {
            getLayoutInflater().inflate(2131230904, this.mContent);
            if (this.mActionBar != null) {
                this.mActionBar.setBackgroundDrawable(getResources().getDrawable(2130837583));
            }
        }
    }

    public void onStart() {
        super.onStart();
        resume();
        if (this.mNeedToRevertToInitialFragment) {
            revertToInitialFragment();
        }
        if (this.mNeedToClearFocusOfSearchView) {
            this.mNeedToClearFocusOfSearchView = false;
            if (this.mSearchView != null) {
                this.mSearchView.clearFocus();
            }
        }
        registerReceiver();
        if (this.mDisplaySearch && !TextUtils.isEmpty(this.mSearchQuery)) {
            onQueryTextSubmit(this.mSearchQuery);
        }
        updateTilesList();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (!this.mDisplaySearch) {
            return false;
        }
        getMenuInflater().inflate(2132017157, menu);
        this.mSearchMenuItem = menu.findItem(2131887652);
        if (this.mSearchMenuItem == null) {
            return false;
        }
        View actionView = this.mSearchMenuItem.getActionView();
        if (actionView == null) {
            return false;
        }
        this.mSearchView = (SearchView) actionView.findViewById(2131886199);
        if (this.mSearchView == null) {
            return false;
        }
        if (this.mSearchResultsFragment != null) {
            this.mSearchResultsFragment.setSearchView(this.mSearchView);
        }
        this.mSearchMenuItem.setOnActionExpandListener(this);
        this.mSearchView.setOnQueryTextListener(this);
        this.mSearchView.setOnCloseListener(this);
        MLog.d("Settings", "Expand option menu to switch to search fragment");
        this.mSearchMenuItem.expandActionView();
        this.mSearchView.setQuery("", true);
        ImageView back = (ImageView) actionView.findViewById(2131886198);
        if (back == null) {
            return true;
        }
        back.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.this.hideSoftInputIfNeeded();
                SettingsActivity.this.revertToInitialFragment();
            }
        });
        return true;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

    private static boolean isShortCutIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        return categories != null ? categories.contains("com.android.settings.SHORTCUT") : false;
    }

    private static boolean isLikeShortCutIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        for (String equals : LIKE_SHORTCUT_INTENT_ACTION_ARRAY) {
            if (equals.equals(action)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSettings(Activity activity) {
        if (activity == null) {
            return false;
        }
        return activity.getClass().getName().equals(Settings.class.getName());
    }

    protected void onCreate(Bundle savedState) {
        boolean z;
        boolean booleanExtra;
        boolean isFromLauncher;
        SharedPreferences state;
        ArrayList<DashboardCategory> categories;
        View buttonBarContainer;
        View buttonBar;
        Button backButton;
        Button skipButton;
        String buttonText;
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{this});
        super.onCreate(savedState);
        long startTime = System.currentTimeMillis();
        Jlog.d(82, "com.android.settings", "");
        this.mIsJlogAlreadyEnabled = false;
        this.mHwCustSettingsActivity = (HwCustSettingsActivity) HwCustUtils.createObj(HwCustSettingsActivity.class, new Object[]{this});
        getMetaData();
        Intent intent = getIntent();
        if (intent.hasExtra("settings:ui_options")) {
            getWindow().setUiOptions(intent.getIntExtra("settings:ui_options", 0));
        }
        if (intent.getBooleanExtra(":settings:hide_drawer", false) || SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            setIsDrawerPresent(false);
        }
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        this.mDevelopmentPreferences = getSharedPreferences("development", 0);
        String initialFragmentName = intent.getStringExtra(":settings:show_fragment");
        if (isShortCutIntent(intent) || isLikeShortCutIntent(intent)) {
            z = true;
        } else {
            z = intent.getBooleanExtra(":settings:show_fragment_as_shortcut", false);
        }
        this.mIsShortcut = z;
        String className = intent.getComponent().getClassName();
        if (!className.equals(Settings.class.getName())) {
            if (!className.equals(WirelessSettings.class.getName())) {
                if (!className.equals(DeviceSettings.class.getName())) {
                    if (!className.equals(PersonalSettings.class.getName())) {
                        z = className.equals(WirelessSettings.class.getName());
                        this.mIsShowingDashboard = z;
                        if (this instanceof SubSettings) {
                            booleanExtra = intent.getBooleanExtra(":settings:show_fragment_as_subsetting", false);
                        } else {
                            booleanExtra = true;
                        }
                        isFromLauncher = className.equals(HWSettings.class.getName());
                        if (isFromLauncher) {
                            state = getSharedPreferences("indexablestate", 0);
                            if (!IndexDatabaseHelper.isLocaleAlreadyIndexedEx(this)) {
                                if (!state.getString("indexed_for_version", "").equalsIgnoreCase(Build.DISPLAY)) {
                                    AsyncTask.execute(new Runnable() {
                                        public void run() {
                                            Index.getInstance(SettingsActivity.this.getApplicationContext()).update(true);
                                        }
                                    });
                                } else if (Utils.isLowStorage(this)) {
                                    long indexStartTime = System.currentTimeMillis();
                                    Index.getInstance(getApplicationContext()).update();
                                } else {
                                    Log.w("Settings", "Cannot update the Indexer as we are running low on storage space!");
                                }
                            }
                        }
                        if (this.mNeedToSwitchToFragment) {
                            System.putInt(getContentResolver(), "Standard mode", 0);
                        }
                        if (!booleanExtra && 1 == System.getInt(getContentResolver(), "Standard mode", 0)) {
                            System.putInt(getContentResolver(), "Standard mode", 0);
                        }
                        setContentView(this.mIsShowingDashboard ? 2130969103 : 2130969104);
                        this.mContent = (ViewGroup) findViewById(this.mMainContentId);
                        if (isFromLauncher || isSettings(this)) {
                            this.mHwCustSplitUtils.setSplit(this.mContent);
                            Intent intent2 = new Intent("android.settings.WIFI_SETTINGS");
                            intent2.setClass(this, WifiSettingsActivity.class);
                            this.mHwCustSplitUtils.setFirstIntent(intent2);
                        }
                        getFragmentManager().addOnBackStackChangedListener(this);
                        if (savedState == null) {
                            this.mSearchMenuItemExpanded = savedState.getBoolean(":settings:search_menu_expanded");
                            this.mSearchQuery = savedState.getString(":settings:search_query");
                            setTitleFromIntent(intent);
                            categories = savedState.getParcelableArrayList(":settings:categories");
                            if (categories != null) {
                                this.mCategories.clear();
                                this.mCategories.addAll(categories);
                                setTitleFromBackStack();
                            }
                            this.mDisplayHomeAsUpEnabled = savedState.getBoolean(":settings:show_home_as_up");
                            this.mDisplaySearch = savedState.getBoolean(":settings:show_search");
                        } else {
                            if (initialFragmentName == null) {
                                this.mIsShowingDashboard = true;
                            }
                            setDelayUpdateCategories(this.mIsShowingDashboard);
                            if (this.mIsShowingDashboard) {
                                this.mDisplaySearch = false;
                                if (this.mIsShortcut) {
                                    this.mDisplayHomeAsUpEnabled = booleanExtra;
                                } else if (booleanExtra) {
                                    this.mDisplayHomeAsUpEnabled = false;
                                } else {
                                    this.mDisplayHomeAsUpEnabled = true;
                                }
                                setTitleFromIntent(intent);
                                switchToFragment(initialFragmentName, intent.getBundleExtra(":settings:show_fragment_args"), true, false, this.mInitialTitleResId, this.mInitialTitle, false);
                            } else {
                                this.mDisplayHomeAsUpEnabled = false;
                                this.mDisplaySearch = false;
                                this.mInitialTitleResId = 2131626636;
                                switchToFragment(DashboardSummary.class.getName(), null, false, false, this.mInitialTitleResId, this.mInitialTitle, false);
                            }
                        }
                        this.mActionBar = getActionBar();
                        if (this.mActionBar != null) {
                            this.mActionBar.setDisplayHomeAsUpEnabled(this.mDisplayHomeAsUpEnabled);
                            this.mActionBar.setHomeButtonEnabled(this.mDisplayHomeAsUpEnabled);
                        }
                        if (intent.getBooleanExtra("extra_prefs_show_button_bar", false)) {
                            buttonBarContainer = ((ViewStub) findViewById(2131887152)).inflate();
                            buttonBar = buttonBarContainer.findViewById(2131886327);
                            if (buttonBar != null) {
                                buttonBar.setVisibility(0);
                                backButton = (Button) buttonBarContainer.findViewById(2131886924);
                                backButton.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        SettingsActivity.this.setResult(0, SettingsActivity.this.getResultIntentData());
                                        SettingsActivity.this.finish();
                                    }
                                });
                                skipButton = (Button) buttonBarContainer.findViewById(2131886925);
                                skipButton.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        SettingsActivity.this.setResult(-1, SettingsActivity.this.getResultIntentData());
                                        SettingsActivity.this.finish();
                                    }
                                });
                                this.mNextButton = (Button) buttonBarContainer.findViewById(2131886371);
                                this.mNextButton.setOnClickListener(new OnClickListener() {
                                    public void onClick(View v) {
                                        SettingsActivity.this.setResult(-1, SettingsActivity.this.getResultIntentData());
                                        SettingsActivity.this.finish();
                                    }
                                });
                                if (intent.hasExtra("extra_prefs_set_next_text")) {
                                    buttonText = intent.getStringExtra("extra_prefs_set_next_text");
                                    if (TextUtils.isEmpty(buttonText)) {
                                        this.mNextButton.setText(buttonText);
                                    } else {
                                        this.mNextButton.setVisibility(8);
                                    }
                                }
                                if (intent.hasExtra("extra_prefs_set_back_text")) {
                                    buttonText = intent.getStringExtra("extra_prefs_set_back_text");
                                    if (TextUtils.isEmpty(buttonText)) {
                                        backButton.setText(buttonText);
                                    } else {
                                        backButton.setVisibility(8);
                                    }
                                }
                                if (intent.getBooleanExtra("extra_prefs_show_skip", false)) {
                                    skipButton.setVisibility(0);
                                }
                            }
                        }
                        if ((this instanceof HWSettings) || (this instanceof Settings)) {
                            getContentResolver().registerContentObserver(ParentControl.STATUS_URI, true, this.mParentCtrlObserver);
                        }
                        this.mDevelopmentPreferencesListener = new OnSharedPreferenceChangeListener() {
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                SettingsActivity.this.updateTilesList();
                            }
                        };
                        this.mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(this.mDevelopmentPreferencesListener);
                        return;
                    }
                }
            }
        }
        z = true;
        this.mIsShowingDashboard = z;
        if (this instanceof SubSettings) {
            booleanExtra = true;
        } else {
            booleanExtra = intent.getBooleanExtra(":settings:show_fragment_as_subsetting", false);
        }
        isFromLauncher = className.equals(HWSettings.class.getName());
        if (isFromLauncher) {
            state = getSharedPreferences("indexablestate", 0);
            if (IndexDatabaseHelper.isLocaleAlreadyIndexedEx(this)) {
                if (!state.getString("indexed_for_version", "").equalsIgnoreCase(Build.DISPLAY)) {
                    AsyncTask.execute(/* anonymous class already generated */);
                } else if (Utils.isLowStorage(this)) {
                    Log.w("Settings", "Cannot update the Indexer as we are running low on storage space!");
                } else {
                    long indexStartTime2 = System.currentTimeMillis();
                    Index.getInstance(getApplicationContext()).update();
                }
            }
        }
        if (this.mNeedToSwitchToFragment) {
            System.putInt(getContentResolver(), "Standard mode", 0);
            if (this.mIsShowingDashboard) {
            }
            setContentView(this.mIsShowingDashboard ? 2130969103 : 2130969104);
            this.mContent = (ViewGroup) findViewById(this.mMainContentId);
            this.mHwCustSplitUtils.setSplit(this.mContent);
            Intent intent22 = new Intent("android.settings.WIFI_SETTINGS");
            intent22.setClass(this, WifiSettingsActivity.class);
            this.mHwCustSplitUtils.setFirstIntent(intent22);
            getFragmentManager().addOnBackStackChangedListener(this);
            if (savedState == null) {
                if (initialFragmentName == null) {
                    this.mIsShowingDashboard = true;
                }
                if (this.mIsShowingDashboard) {
                }
                setDelayUpdateCategories(this.mIsShowingDashboard);
                if (this.mIsShowingDashboard) {
                    this.mDisplayHomeAsUpEnabled = false;
                    this.mDisplaySearch = false;
                    this.mInitialTitleResId = 2131626636;
                    switchToFragment(DashboardSummary.class.getName(), null, false, false, this.mInitialTitleResId, this.mInitialTitle, false);
                } else {
                    this.mDisplaySearch = false;
                    if (this.mIsShortcut) {
                        this.mDisplayHomeAsUpEnabled = booleanExtra;
                    } else if (booleanExtra) {
                        this.mDisplayHomeAsUpEnabled = false;
                    } else {
                        this.mDisplayHomeAsUpEnabled = true;
                    }
                    setTitleFromIntent(intent);
                    switchToFragment(initialFragmentName, intent.getBundleExtra(":settings:show_fragment_args"), true, false, this.mInitialTitleResId, this.mInitialTitle, false);
                }
            } else {
                this.mSearchMenuItemExpanded = savedState.getBoolean(":settings:search_menu_expanded");
                this.mSearchQuery = savedState.getString(":settings:search_query");
                setTitleFromIntent(intent);
                categories = savedState.getParcelableArrayList(":settings:categories");
                if (categories != null) {
                    this.mCategories.clear();
                    this.mCategories.addAll(categories);
                    setTitleFromBackStack();
                }
                this.mDisplayHomeAsUpEnabled = savedState.getBoolean(":settings:show_home_as_up");
                this.mDisplaySearch = savedState.getBoolean(":settings:show_search");
            }
            this.mActionBar = getActionBar();
            if (this.mActionBar != null) {
                this.mActionBar.setDisplayHomeAsUpEnabled(this.mDisplayHomeAsUpEnabled);
                this.mActionBar.setHomeButtonEnabled(this.mDisplayHomeAsUpEnabled);
            }
            if (intent.getBooleanExtra("extra_prefs_show_button_bar", false)) {
                buttonBarContainer = ((ViewStub) findViewById(2131887152)).inflate();
                buttonBar = buttonBarContainer.findViewById(2131886327);
                if (buttonBar != null) {
                    buttonBar.setVisibility(0);
                    backButton = (Button) buttonBarContainer.findViewById(2131886924);
                    backButton.setOnClickListener(/* anonymous class already generated */);
                    skipButton = (Button) buttonBarContainer.findViewById(2131886925);
                    skipButton.setOnClickListener(/* anonymous class already generated */);
                    this.mNextButton = (Button) buttonBarContainer.findViewById(2131886371);
                    this.mNextButton.setOnClickListener(/* anonymous class already generated */);
                    if (intent.hasExtra("extra_prefs_set_next_text")) {
                        buttonText = intent.getStringExtra("extra_prefs_set_next_text");
                        if (TextUtils.isEmpty(buttonText)) {
                            this.mNextButton.setText(buttonText);
                        } else {
                            this.mNextButton.setVisibility(8);
                        }
                    }
                    if (intent.hasExtra("extra_prefs_set_back_text")) {
                        buttonText = intent.getStringExtra("extra_prefs_set_back_text");
                        if (TextUtils.isEmpty(buttonText)) {
                            backButton.setText(buttonText);
                        } else {
                            backButton.setVisibility(8);
                        }
                    }
                    if (intent.getBooleanExtra("extra_prefs_show_skip", false)) {
                        skipButton.setVisibility(0);
                    }
                }
            }
            getContentResolver().registerContentObserver(ParentControl.STATUS_URI, true, this.mParentCtrlObserver);
            this.mDevelopmentPreferencesListener = /* anonymous class already generated */;
            this.mDevelopmentPreferences.registerOnSharedPreferenceChangeListener(this.mDevelopmentPreferencesListener);
            return;
        }
        System.putInt(getContentResolver(), "Standard mode", 0);
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        if (isInMultiWindowMode) {
            this.mHwCustSplitUtils.hideAllContent();
            return;
        }
        if (getClass().getName().equals(HWSettings.class.getName()) || isSettings(this)) {
            this.mHwCustSplitUtils.setSplit(this.mContent);
        }
        this.mHwCustSplitUtils.hideAllContent();
    }

    protected void setMainContentId(int contentId) {
        this.mMainContentId = contentId;
    }

    private void setTitleFromIntent(Intent intent) {
        int initialTitleResId = intent.getIntExtra(":settings:show_fragment_title_resid", -1);
        if (initialTitleResId > 0) {
            this.mInitialTitle = null;
            this.mInitialTitleResId = initialTitleResId;
            String initialTitleResPackageName = intent.getStringExtra(":settings:show_fragment_title_res_package_name");
            if (initialTitleResPackageName != null) {
                try {
                    this.mInitialTitle = createPackageContextAsUser(initialTitleResPackageName, 0, new UserHandle(UserHandle.myUserId())).getResources().getText(this.mInitialTitleResId);
                    setTitle(this.mInitialTitle);
                    this.mInitialTitleResId = -1;
                } catch (NameNotFoundException e) {
                    Log.w("Settings", "Could not find package" + initialTitleResPackageName);
                }
            } else {
                setTitle(this.mInitialTitleResId);
            }
        } else {
            this.mInitialTitleResId = -1;
            String initialTitle = intent.getStringExtra(":settings:show_fragment_title");
            if (initialTitle == null) {
                initialTitle = getTitle();
            }
            this.mInitialTitle = initialTitle;
            setTitle(this.mInitialTitle);
        }
    }

    public void onBackStackChanged() {
        setTitleFromBackStack();
    }

    private int setTitleFromBackStack() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (this.mInitialTitleResId > 0) {
                setTitle(this.mInitialTitleResId);
            } else {
                setTitle(this.mInitialTitle);
            }
            return 0;
        }
        setTitleFromBackStackEntry(getFragmentManager().getBackStackEntryAt(count - 1));
        return count;
    }

    private void setTitleFromBackStackEntry(BackStackEntry bse) {
        CharSequence title;
        int titleRes = bse.getBreadCrumbTitleRes();
        if (titleRes > 0) {
            title = getText(titleRes);
        } else {
            title = bse.getBreadCrumbTitle();
        }
        if (title != null) {
            setTitle(title);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mCategories.size() > 0) {
            outState.putParcelableArrayList(":settings:categories", this.mCategories);
        }
        outState.putBoolean(":settings:show_home_as_up", this.mDisplayHomeAsUpEnabled);
        outState.putBoolean(":settings:show_search", this.mDisplaySearch);
        if (this.mDisplaySearch) {
            outState.putBoolean(":settings:search_menu_expanded", this.mSearchMenuItem != null ? this.mSearchMenuItem.isActionViewExpanded() : false);
            outState.putString(":settings:search_query", this.mSearchView != null ? this.mSearchView.getQuery().toString() : "");
        }
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        if (!this.mIsJlogAlreadyEnabled && this.mRequestEnableJlog) {
            Jlog.d(81, "com.android.settings", "");
            this.mIsJlogAlreadyEnabled = true;
        }
    }

    public void requestJlogEnable(boolean enable) {
        this.mRequestEnableJlog = enable;
        if (!enable && this.mIsJlogAlreadyEnabled) {
            Jlog.d(82, "com.android.settings", "");
            this.mIsJlogAlreadyEnabled = false;
        }
    }

    private void resume() {
        if (this.mIsJlogAlreadyEnabled) {
            Jlog.d(82, "com.android.settings", "");
            this.mIsJlogAlreadyEnabled = false;
        }
        if ((this instanceof HWSettings) || (this instanceof Settings)) {
            updateOtherSearchIndex();
        }
    }

    private void registerReceiver() {
        if (!this.mRegistered) {
            registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_ADDED");
            intentFilter.addAction("android.intent.action.USER_REMOVED");
            registerReceiver(this.mUserAddRemoveReceiver, intentFilter);
            this.mRegistered = true;
        }
        if ((this instanceof HWSettings) || (this instanceof Settings)) {
            this.mDynamicIndexableContentMonitor.register(this, 1);
        }
    }

    private void unregisterReceiver() {
        if (this.mRegistered) {
            unregisterReceiver(this.mBatteryInfoReceiver);
            unregisterReceiver(this.mUserAddRemoveReceiver);
            this.mRegistered = false;
        }
        if ((this instanceof HWSettings) || (this instanceof Settings)) {
            this.mDynamicIndexableContentMonitor.unregister();
        }
    }

    protected void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mDevelopmentPreferences.unregisterOnSharedPreferenceChangeListener(this.mDevelopmentPreferencesListener);
        this.mDevelopmentPreferencesListener = null;
        getFragmentManager().removeOnBackStackChangedListener(this);
        if ((this instanceof HWSettings) || (this instanceof Settings)) {
            getContentResolver().unregisterContentObserver(this.mParentCtrlObserver);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ThirdPartyDummyIndexable.class.getName().equals(fragmentName)) {
            Log.e("Settings", "SettingsActivity-->isValidFragment():fragmentName is : " + fragmentName);
            return false;
        }
        for (String equals : ENTRY_FRAGMENTS) {
            if (equals.equals(fragmentName)) {
                return true;
            }
        }
        return this.mHwCustSettingsActivity.isValidFragment(fragmentName);
    }

    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        if (startingFragment == null) {
            return superIntent;
        }
        Intent modIntent = new Intent(superIntent);
        modIntent.putExtra(":settings:show_fragment", startingFragment);
        Bundle args = superIntent.getExtras();
        if (args != null) {
            args = new Bundle(args);
        } else {
            args = new Bundle();
        }
        args.putParcelable("intent", superIntent);
        modIntent.putExtra(":settings:show_fragment_args", args);
        return modIntent;
    }

    private String getStartingFragmentClass(Intent intent) {
        if (this.mFragmentClass != null) {
            return this.mFragmentClass;
        }
        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) {
            return null;
        }
        if ("com.android.settings.ManageApplications".equals(intentClass) || "com.android.settings.RunningServices".equals(intentClass) || "com.android.settings.applications.StorageUse".equals(intentClass)) {
            intentClass = ManageApplications.class.getName();
        }
        return intentClass;
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
        CharSequence title = null;
        if (titleRes < 0) {
            if (titleText != null) {
                title = titleText.toString();
            } else {
                title = "";
            }
        }
        Utils.startWithFragment((Context) this, fragmentClass, args, resultTo, resultRequestCode, titleRes, title, this.mIsShortcut);
    }

    public void startPreferencePanelAsUser(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, UserHandle userHandle) {
        if (userHandle.getIdentifier() == UserHandle.myUserId()) {
            startPreferencePanel(fragmentClass, args, titleRes, titleText, null, 0);
            return;
        }
        CharSequence title = null;
        if (titleRes < 0) {
            if (titleText != null) {
                title = titleText.toString();
            } else {
                title = "";
            }
        }
        Utils.startWithFragmentAsUser(this, fragmentClass, args, titleRes, title, this.mIsShortcut, userHandle);
    }

    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        setResult(resultCode, resultData);
        finish();
    }

    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(this.mMainContentId, fragment);
        if (push) {
            transaction.setTransition(4097);
            transaction.addToBackStack(":settings:prefs");
        } else {
            transaction.setTransition(4099);
        }
        transaction.commitAllowingStateLoss();
    }

    private Fragment switchToFragment(String fragmentName, Bundle args, boolean validate, boolean addToBackStack, int titleResId, CharSequence title, boolean withTransition) {
        if (!validate || isValidFragment(fragmentName)) {
            Fragment f = Fragment.instantiate(this, fragmentName, args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(this.mMainContentId, f, fragmentName);
            if (withTransition) {
                TransitionManager.beginDelayedTransition(this.mContent);
            }
            if (addToBackStack) {
                transaction.addToBackStack(":settings:prefs");
            }
            if (titleResId > 0) {
                transaction.setBreadCrumbTitle(titleResId);
            } else if (title != null) {
                transaction.setBreadCrumbTitle(title);
            }
            transaction.commitAllowingStateLoss();
            getFragmentManager().executePendingTransactions();
            return f;
        }
        HashMap<Short, Object> map = new HashMap();
        map.put(Short.valueOf((short) 0), fragmentName);
        RadarReporter.reportRadar(907018001, map);
        throw new IllegalArgumentException("Invalid fragment for this activity: " + fragmentName);
    }

    private void updateTilesList() {
        AsyncTask.execute(new Runnable() {
            public void run() {
                SettingsActivity.this.doUpdateTilesList();
            }
        });
    }

    private boolean isShowDev(UserManager um) {
        if (!this.mDevelopmentPreferences.getBoolean("show", Build.TYPE.equals("eng")) || ParentControl.isChildModeOn((Context) this) || um.hasUserRestriction("no_debugging_features")) {
            return false;
        }
        return true;
    }

    private void doUpdateTilesList() {
        boolean isEnabled;
        PackageManager pm = getPackageManager();
        UserManager um = UserManager.get(this);
        boolean isAdmin = um.isAdminUser();
        String packageName = getPackageName();
        setTileEnabled(new ComponentName(packageName, WifiSettingsActivity.class.getName()), pm.hasSystemFeature("android.hardware.wifi"), isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, BluetoothSettingsActivity.class.getName()), pm.hasSystemFeature("android.hardware.bluetooth"), isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, DataUsageSummaryActivity.class.getName()), Utils.isBandwidthControlEnabled(), isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, SimSettingsActivity.class.getName()), Utils.showSimCardTile(this), isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, PowerUsageSummaryActivity.class.getName()), this.mBatteryPresent, isAdmin, pm);
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        ComponentName componentName = new ComponentName(packageName, PaymentSettingsActivity.class.getName());
        if (pm.hasSystemFeature("android.hardware.nfc") && pm.hasSystemFeature("android.hardware.nfc.hce") && adapter != null) {
            isEnabled = adapter.isEnabled();
        } else {
            isEnabled = false;
        }
        setTileEnabled(componentName, isEnabled, isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, PrintSettingsActivity.class.getName()), pm.hasSystemFeature("android.software.print"), isAdmin, pm);
        boolean showDev = isShowDev(um);
        setTileEnabled(new ComponentName(packageName, DevelopmentSettingsActivity.class.getName()), showDev, isAdmin, pm);
        DevelopmentTiles.setTilesEnabled(this, showDev);
        componentName = new ComponentName(packageName, DataSubscriptionActivity.class.getName());
        if (!Utils.isChinaTelecomArea() || Utils.isWifiOnly(this)) {
            isEnabled = false;
        } else {
            isEnabled = Utils.hasPackageInfo(pm, "com.android.phone");
        }
        setTileEnabled(componentName, isEnabled, isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, AppCloneActivity.class.getName()), isCloneAppEnabled(), isAdmin, pm);
        boolean isFrontFinger = NaviUtils.isFrontFingerNaviEnabled();
        boolean isTrikeyDevice = NaviUtils.isTrikeyDevice();
        setTileEnabled(new ComponentName(packageName, NaviTrikeySettingsActivity.class.getName()), isFrontFinger ? isTrikeyDevice : false, isAdmin, pm);
        componentName = new ComponentName(packageName, NavigationSettingsActivity.class.getName());
        isEnabled = isFrontFinger && !isTrikeyDevice;
        setTileEnabled(componentName, isEnabled, isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, VirtualKeySettingsActivity.class.getName()), !isFrontFinger, isAdmin, pm);
        setFingerEntryEnabled(packageName, pm, isAdmin);
        setStorageEntryEnabled(packageName, pm, isAdmin);
        removeScreenLock(packageName, isAdmin, pm);
        if (!isAdmin) {
            for (DashboardCategory category : getDashboardCategories()) {
                for (Tile tile : category.tiles) {
                    ComponentName component = tile.intent.getComponent();
                    if (!(component == null || !packageName.equals(component.getPackageName()) || ArrayUtils.contains(this.SETTINGS_FOR_RESTRICTED, component.getClassName()))) {
                        setTileEnabled(component, false, isAdmin, pm);
                    }
                }
            }
        }
    }

    private void setStorageEntryEnabled(String packageName, PackageManager pm, boolean isAdmin) {
        setTileEnabled(new ComponentName(packageName, StorageSettingsActivity.class.getName()), !SystemProperties.getBoolean("ro.config.vicky_demo_6G", false), isAdmin, pm);
    }

    private void setFingerEntryEnabled(String packageName, PackageManager pm, boolean isAdmin) {
        boolean fingerSupported = BiometricManager.isFingerprintSupported(this);
        setTileEnabled(new ComponentName(packageName, FingerprintSettingsActivity.class.getName()), fingerSupported, isAdmin, pm);
        setTileEnabled(new ComponentName(packageName, FingerprintMainSettingsActivity.class.getName()), fingerSupported, isAdmin, pm);
    }

    private boolean isCloneAppEnabled() {
        if (!AppCloneUtils.hasAppCloneCust() || !AppCloneUtils.isSupportAppClone()) {
            return false;
        }
        AppCloneUtils.initAppCloneXml(this);
        return true;
    }

    private void setTileEnabled(ComponentName component, boolean enabled, boolean isAdmin, PackageManager pm) {
        if (!(isAdmin || !getPackageName().equals(component.getPackageName()) || ArrayUtils.contains(this.SETTINGS_FOR_RESTRICTED, component.getClassName()))) {
            enabled = false;
        }
        setTileEnabled(component, enabled);
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(), 128);
            if (ai != null && ai.metaData != null) {
                this.mFragmentClass = ai.metaData.getString("com.android.settings.FRAGMENT_CLASS");
                this.mFragmentClass = this.mHwCustSettingsActivity.getMetaData(this.mFragmentClass);
            }
        } catch (NameNotFoundException e) {
            Log.d("Settings", "Cannot get Metadata for: " + getComponentName().toString());
        }
    }

    public boolean hasNextButton() {
        return this.mNextButton != null;
    }

    public Button getNextButton() {
        return this.mNextButton;
    }

    public boolean shouldUpRecreateTask(Intent targetIntent) {
        return super.shouldUpRecreateTask(new Intent(this, SettingsActivity.class));
    }

    public boolean onQueryTextSubmit(String query) {
        switchToSearchResultsFragmentIfNeeded();
        this.mSearchQuery = query;
        return this.mSearchResultsFragment.onQueryTextSubmit(query);
    }

    public boolean onQueryTextChange(String newText) {
        this.mSearchQuery = newText;
        if (this.mSearchResultsFragment == null) {
            return false;
        }
        return this.mSearchResultsFragment.onQueryTextChange(newText);
    }

    public boolean onClose() {
        return false;
    }

    public boolean onMenuItemActionExpand(MenuItem item) {
        if (item.getItemId() == this.mSearchMenuItem.getItemId()) {
            switchToSearchResultsFragmentIfNeeded();
        }
        return true;
    }

    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (item.getItemId() == this.mSearchMenuItem.getItemId() && this.mSearchMenuItemExpanded) {
            revertToInitialFragment();
        }
        return true;
    }

    protected void onTileClicked(Tile tile) {
        if (this.mIsShowingDashboard) {
            openTile(tile);
        } else {
            super.onTileClicked(tile);
        }
    }

    public void onProfileTileOpen() {
        if (!this.mIsShowingDashboard) {
            finish();
        }
    }

    protected void switchToSearchResultsFragmentIfNeeded() {
        if (this.mSearchResultsFragment == null) {
            Fragment current = getFragmentManager().findFragmentById(this.mMainContentId);
            if (current == null || !(current instanceof SearchResultsSummary)) {
                this.mSearchResultsFragment = (SearchResultsSummary) switchToFragment(SearchResultsSummary.class.getName(), null, false, true, 2131626637, null, true);
            } else {
                this.mSearchResultsFragment = (SearchResultsSummary) current;
            }
            this.mSearchResultsFragment.setSearchView(this.mSearchView);
            if (this.mSearchView != null) {
                this.mSearchView.requestFocus();
            }
            showSoftInputIfNeeded();
            this.mSearchMenuItemExpanded = true;
        }
    }

    public void needToClearFocusOfSearchView() {
        this.mNeedToClearFocusOfSearchView = true;
    }

    protected void showInitialViewIfNeeded() {
    }

    protected void revertToInitialFragment() {
        this.mDisplaySearch = false;
        invalidateOptionsMenu();
        this.mNeedToRevertToInitialFragment = false;
        this.mSearchResultsFragment = null;
        this.mSearchMenuItemExpanded = false;
        getFragmentManager().popBackStackImmediate(":settings:prefs", 1);
        showInitialViewIfNeeded();
        if (this.mSearchMenuItem != null) {
            this.mSearchMenuItem.collapseActionView();
        }
    }

    public Intent getResultIntentData() {
        return this.mResultIntentData;
    }

    public void startSuggestion(Intent intent) {
        this.mCurrentSuggestion = intent.getComponent();
        this.mHwCustSplitUtils.cancelSplit(intent);
        Intent huaweiWallpaperIntent = new Intent("com.huawei.launcher.wallpaper_setting");
        ItemUseStat.getInstance().handleClick((Context) this, 12, "start suggestion", ItemUseStat.getShortName(this.mCurrentSuggestion.getClassName()));
        if (WallpaperSuggestionActivity.class.getName().equals(this.mCurrentSuggestion.getClassName()) && Utils.hasIntentActivity(getPackageManager(), huaweiWallpaperIntent)) {
            this.mHwCustSplitUtils.cancelSplit(huaweiWallpaperIntent);
            try {
                startActivity(huaweiWallpaperIntent);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            startActivityForResult(intent, 42);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!(requestCode != 42 || this.mCurrentSuggestion == null || resultCode == 0)) {
            getPackageManager().setComponentEnabledSetting(this.mCurrentSuggestion, 2, 1);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void switchToSearchResult() {
        this.mDisplaySearch = true;
        invalidateOptionsMenu();
    }

    protected void showSoftInputIfNeeded() {
        ((InputMethodManager) getSystemService("input_method")).toggleSoftInput(1, 2);
    }

    protected void hideSoftInputIfNeeded() {
        InputMethodManager imm = (InputMethodManager) getSystemService("input_method");
        View focus = getCurrentFocus();
        if (focus != null) {
            imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 2);
        }
    }

    protected void updateOtherSearchIndex() {
        if (LockPatternUtils.isDeviceEncryptionEnabled()) {
            String KEY_LAST_ENCRYPTION_STATE = "last_encryption_state";
            if (Secure.getInt(getContentResolver(), "last_encryption_state", 0) == 0) {
                Index.getInstance(this).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
                Secure.putInt(getContentResolver(), "last_encryption_state", 1);
            }
        }
        Index.getInstance(this).updateFromClassNameResource(ThirdPartyDummyIndexable.class.getName(), true, true);
        if (!MdppUtils.isCcModeDisabled()) {
            Index.getInstance(this).updateFromClassNameResource(PrivacySettings.class.getName(), true, true);
            Index.getInstance(this).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
            Index.getInstance(this).updateFromClassNameResource(DevelopmentSettings.class.getName(), true, true);
        }
    }

    private void removeScreenLock(String packageName, boolean isAdmin, PackageManager pm) {
        if ("true".equals(System.getString(getContentResolver(), "isDisableKeyGuard"))) {
            setTileEnabled(new ComponentName(packageName, ScreenLockSettingsActivity.class.getName()), false, isAdmin, pm);
            new LockPatternUtils(this).setLockScreenDisabled(true, UserHandle.myUserId());
        }
    }

    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        if (this instanceof WifiSettingsActivity) {
            DrawerLayoutEx.setPreventMeasure(false);
        }
        if (this.mContextMenuClosedListener != null) {
            this.mContextMenuClosedListener.onContextMenuClosed();
        }
    }

    public void setContextMenuClosedListener(ContextMenuClosedListener listener) {
        this.mContextMenuClosedListener = listener;
    }
}
