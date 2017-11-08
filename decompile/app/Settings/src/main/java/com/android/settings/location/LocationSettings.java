package com.android.settings.location;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.location.RadioButtonPreference.OnClickListener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.location.RecentLocationApps;
import com.android.settingslib.location.RecentLocationApps.Request;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationSettings extends LocationSettingsBase implements OnPreferenceChangeListener, OnClickListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230811;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (!Utils.isWifiOnly(context)) {
                return null;
            }
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String action = "com.android.settings.Settings$LocationSettingsActivity";
            String intentTargetPackage = "com.android.settings";
            String intentTargetClass = "com.android.settings.Settings$LocationSettingsActivity";
            String screenTitle = res.getString(2131628662);
            String summary = res.getString(2131627851);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.summaryOff = summary;
            data.summaryOn = summary;
            data.intentAction = action;
            data.intentTargetPackage = intentTargetPackage;
            data.intentTargetClass = intentTargetClass;
            result.add(data);
            screenTitle = res.getString(2131628663);
            summary = res.getString(2131627852);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.summaryOff = summary;
            data.summaryOn = summary;
            data.intentAction = action;
            data.intentTargetPackage = intentTargetPackage;
            data.intentTargetClass = intentTargetClass;
            result.add(data);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (!LocationSettings.hasLocationAssist(context)) {
                keys.add("location_assist_settings");
            }
            if (SystemProperties.getBoolean("ro.config.hw_hide_settings_gps", false)) {
                keys.add("location_toggle");
            }
            if (Utils.getManagedProfile((UserManager) context.getSystemService("user")) == null) {
                keys.add("managed_profile_location_switch");
            }
            keys.add("location_services");
            if (Utils.isWifiOnly(context)) {
                keys.add("high_accuracy");
                keys.add("battery_saving");
            }
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private SettingsInjector injector;
    private RadioButtonPreference mBatterySaving;
    private PreferenceCategory mCategoryRecentLocationRequests;
    private HwCustLocationSettingsBase mCustLocationSettingsBase;
    private RadioButtonPreference mHighAccuracy;
    private RestrictedSwitchPreference mLocationAccess;
    private Preference mLocationAssist;
    private Preference mLocationMode;
    private BroadcastReceiver mLocationRequestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.location.LOCATION_REQUEST_CHANGE_ACTION".equals(intent.getAction())) {
                LocationSettings.this.refreshCategoryRecent();
            }
        }
    };
    private UserHandle mManagedProfile;
    private RestrictedSwitchPreference mManagedProfileSwitch;
    private OnPreferenceClickListener mManagedProfileSwitchClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            boolean z;
            boolean switchState = LocationSettings.this.mManagedProfileSwitch.isChecked();
            UserManager -get3 = LocationSettings.this.mUm;
            String str = "no_share_location";
            if (switchState) {
                z = false;
            } else {
                z = true;
            }
            -get3.setUserRestriction(str, z, LocationSettings.this.mManagedProfile);
            LocationSettings.this.mManagedProfileSwitch.setSummary(switchState ? 2131626851 : 2131626852);
            return true;
        }
    };
    private BroadcastReceiver mReceiver;
    private RadioButtonPreference mSensorsOnly;
    private UserManager mUm;
    private boolean mValidListener = false;

    private class PackageEntryClickedListener implements OnPreferenceClickListener {
        private String mPackage;
        private UserHandle mUserHandle;

        public PackageEntryClickedListener(String packageName, UserHandle userHandle) {
            this.mPackage = packageName;
            this.mUserHandle = userHandle;
        }

        public boolean onPreferenceClick(Preference preference) {
            Bundle args = new Bundle();
            args.putString("package", this.mPackage);
            ((SettingsActivity) LocationSettings.this.getActivity()).startPreferencePanelAsUser(InstalledAppDetails.class.getName(), args, 2131625599, null, this.mUserHandle);
            return true;
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                if (Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0) {
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627102, new Object[]{this.mContext.getString(LocationSettings.getLocationString(mode))}));
                    return;
                }
                this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627103));
            }
        }
    }

    protected int getMetricsCategory() {
        return 63;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mUm = (UserManager) ((SettingsActivity) getActivity()).getSystemService("user");
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        createPreferenceHierarchy();
        if (!this.mValidListener) {
            this.mValidListener = true;
        }
        getActivity().registerReceiver(this.mLocationRequestReceiver, new IntentFilter("android.location.LOCATION_REQUEST_CHANGE_ACTION"));
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getActivity());
        try {
            getActivity().unregisterReceiver(this.mReceiver);
        } catch (RuntimeException e) {
            if (Log.isLoggable("LocationSettings", 2)) {
                Log.v("LocationSettings", "Swallowing " + e);
            }
        }
        if (this.mValidListener) {
            this.mValidListener = false;
        }
        getActivity().unregisterReceiver(this.mLocationRequestReceiver);
        super.onPause();
    }

    private void addPreferencesSorted(List<Preference> prefs, PreferenceGroup container) {
        Collections.sort(prefs, new Comparator<Preference>() {
            public int compare(Preference lhs, Preference rhs) {
                return lhs.getTitle().toString().compareTo(rhs.getTitle().toString());
            }
        });
        for (Preference entry : prefs) {
            container.addPreference(entry);
        }
    }

    private PreferenceScreen createPreferenceHierarchy() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230811);
        root = getPreferenceScreen();
        setupManagedProfileCategory(root);
        this.mLocationAccess = (RestrictedSwitchPreference) root.findPreference("location_toggle");
        this.mLocationAccess.setOnPreferenceChangeListener(this);
        this.mCustLocationSettingsBase = (HwCustLocationSettingsBase) HwCustUtils.createObj(HwCustLocationSettingsBase.class, new Object[0]);
        if (this.mCustLocationSettingsBase != null) {
            this.mCustLocationSettingsBase.custGpsEnable(this.mLocationAccess);
        }
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_share_location", UserHandle.myUserId());
        if (!(RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_share_location", UserHandle.myUserId()) || admin == null)) {
            this.mLocationAccess.setDisabledByAdmin(admin);
        }
        this.mLocationMode = new Preference(activity);
        this.mLocationMode.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(LocationSettings.this.getActivity(), preference);
                activity.startPreferencePanel(LocationMode.class.getName(), null, 2131625478, null, LocationSettings.this, 0);
                return true;
            }
        });
        this.mHighAccuracy = (RadioButtonPreference) root.findPreference("high_accuracy");
        this.mBatterySaving = (RadioButtonPreference) root.findPreference("battery_saving");
        this.mSensorsOnly = (RadioButtonPreference) root.findPreference("sensors_only");
        this.mLocationAssist = root.findPreference("location_assist_settings");
        updateSummaryForWifiOnly();
        this.mHighAccuracy.setOnClickListener(this);
        this.mBatterySaving.setOnClickListener(this);
        this.mSensorsOnly.setOnClickListener(this);
        refreshCategoryRecent();
        if (!hasLocationAssist(activity)) {
            removePreference("location_mode_category", "location_assist_settings");
        }
        boolean lockdownOnLocationAccess = false;
        if (this.mManagedProfile != null && this.mUm.hasUserRestriction("no_share_location", this.mManagedProfile)) {
            lockdownOnLocationAccess = true;
        }
        addLocationServices(activity, root, lockdownOnLocationAccess);
        checkRemoveGPS(root);
        refreshLocationMode();
        return root;
    }

    private void checkRemoveGPS(PreferenceScreen root) {
        if (SystemProperties.getBoolean("ro.config.hw_hide_settings_gps", false) && this.mLocationAccess != null) {
            root.removePreference(this.mLocationAccess);
            Log.v("LocationSettings", "checkRemoveGPS remove");
        }
    }

    private void setupManagedProfileCategory(PreferenceScreen root) {
        this.mManagedProfile = Utils.getManagedProfile(this.mUm);
        if (this.mManagedProfile == null) {
            root.removePreference(root.findPreference("managed_profile_location_switch"));
            this.mManagedProfileSwitch = null;
            return;
        }
        this.mManagedProfileSwitch = (RestrictedSwitchPreference) root.findPreference("managed_profile_location_switch");
        this.mManagedProfileSwitch.setOnPreferenceClickListener(null);
    }

    private void changeManagedProfileLocationAccessStatus(boolean mainSwitchOn) {
        boolean z = false;
        if (this.mManagedProfileSwitch != null) {
            this.mManagedProfileSwitch.setOnPreferenceClickListener(null);
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_share_location", this.mManagedProfile.getIdentifier());
            boolean isRestrictedByBase = isManagedProfileRestrictedByBase();
            if (isRestrictedByBase || admin == null) {
                boolean enabled = mainSwitchOn;
                this.mManagedProfileSwitch.setEnabled(mainSwitchOn);
                int summaryResId = 2131626852;
                if (mainSwitchOn) {
                    RestrictedSwitchPreference restrictedSwitchPreference = this.mManagedProfileSwitch;
                    if (!isRestrictedByBase) {
                        z = true;
                    }
                    restrictedSwitchPreference.setChecked(z);
                    summaryResId = isRestrictedByBase ? 2131626852 : 2131626851;
                    this.mManagedProfileSwitch.setOnPreferenceClickListener(this.mManagedProfileSwitchClickListener);
                } else {
                    this.mManagedProfileSwitch.setChecked(false);
                }
                this.mManagedProfileSwitch.setSummary(summaryResId);
            } else {
                this.mManagedProfileSwitch.setDisabledByAdmin(admin);
                this.mManagedProfileSwitch.setChecked(false);
            }
        }
    }

    private void addLocationServices(Context context, PreferenceScreen root, boolean lockdownOnLocationAccess) {
        PreferenceCategory categoryLocationServices = (PreferenceCategory) root.findPreference("location_services");
        this.injector = new SettingsInjector(context);
        List<Preference> locationServices = this.injector.getInjectedSettings(lockdownOnLocationAccess ? UserHandle.myUserId() : -2);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (Log.isLoggable("LocationSettings", 3)) {
                    Log.d("LocationSettings", "Received settings change intent: " + intent);
                }
                LocationSettings.this.injector.reloadStatusMessages();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.InjectedSettingChanged");
        context.registerReceiver(this.mReceiver, filter);
        if (locationServices.size() > 0) {
            addPreferencesSorted(locationServices, categoryLocationServices);
        } else {
            root.removePreference(categoryLocationServices);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, 2131628149);
        menu.add(0, 0, 0, 2131626521);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        SettingsActivity activity = (SettingsActivity) getActivity();
        switch (item.getItemId()) {
            case 0:
                Intent intent = new Intent();
                intent.setAction("com.android.settings.LocationProvisionHelp");
                startActivity(intent);
                return true;
            case 1:
                activity.startPreferencePanel(ScanningSettings.class.getName(), null, 2131628150, null, this, 0);
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "location_scan_settings");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int getHelpResource() {
        return 2131626550;
    }

    private static int getLocationString(int mode) {
        switch (mode) {
            case 0:
                return 2131625472;
            case 1:
                return 2131625471;
            case 2:
                return 2131625470;
            case 3:
                return 2131625469;
            default:
                return 0;
        }
    }

    public void onModeChanged(int mode, boolean restricted) {
        boolean z = false;
        switch (mode) {
            case 0:
                updateRadioButtons(null);
                break;
            case 1:
                updateRadioButtons(this.mSensorsOnly);
                break;
            case 2:
                updateRadioButtons(this.mBatterySaving);
                break;
            case 3:
                updateRadioButtons(this.mHighAccuracy);
                break;
        }
        boolean locationModeEnabled = (mode == 0 || restricted) ? false : true;
        this.mHighAccuracy.setEnabled(locationModeEnabled);
        this.mBatterySaving.setEnabled(locationModeEnabled);
        this.mSensorsOnly.setEnabled(locationModeEnabled);
        boolean assistLocationModeEnabled = (!locationModeEnabled || mode == 2) ? false : mode != 1;
        this.mLocationAssist.setEnabled(assistLocationModeEnabled);
        boolean enabled = mode != 0;
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_share_location", UserHandle.myUserId());
        boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_share_location", UserHandle.myUserId());
        Preference preference = this.mLocationMode;
        if (enabled && !restricted) {
            z = true;
        }
        preference.setEnabled(z);
        this.mCategoryRecentLocationRequests.setEnabled(enabled);
        refreshCategoryRecent();
        if (enabled != this.mLocationAccess.isChecked()) {
            if (this.mValidListener) {
                this.mLocationAccess.setOnPreferenceChangeListener(null);
            }
            this.mLocationAccess.setChecked(enabled);
            if (this.mValidListener) {
                this.mLocationAccess.setOnPreferenceChangeListener(this);
            }
        }
        if (!(hasBaseUserRestriction || admin == null)) {
            this.mLocationAccess.setDisabledByAdmin(admin);
        }
        changeManagedProfileLocationAccessStatus(enabled);
        this.injector.reloadStatusMessages();
    }

    private boolean isManagedProfileRestrictedByBase() {
        if (this.mManagedProfile == null) {
            return false;
        }
        return this.mUm.hasBaseUserRestriction("no_share_location", this.mManagedProfile);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), pref, newValue);
        if ("location_toggle".equals(pref.getKey())) {
            if (!((Boolean) newValue).booleanValue()) {
                setLocationMode(0);
            } else if (this.mCustLocationSettingsBase == null || !this.mCustLocationSettingsBase.isPowerSaveDefaultChoosen()) {
                setLocationMode(-1);
            } else {
                setLocationMode(2);
            }
        }
        return true;
    }

    private void updateSummaryForWifiOnly() {
        if (Utils.isWifiOnly(getActivity())) {
            this.mHighAccuracy.setTitle(2131628662);
            this.mBatterySaving.setTitle(2131628663);
            this.mHighAccuracy.setSummary(2131627851);
            this.mBatterySaving.setSummary(2131627852);
        }
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mHighAccuracy) {
            this.mHighAccuracy.setChecked(true);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mBatterySaving) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(true);
            this.mSensorsOnly.setChecked(false);
        } else if (activated == this.mSensorsOnly) {
            this.mHighAccuracy.setChecked(false);
            this.mBatterySaving.setChecked(false);
            this.mSensorsOnly.setChecked(true);
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        int mode = 0;
        int currentMode = Secure.getInt(getContentResolver(), "location_mode", 0);
        if (emiter == this.mHighAccuracy) {
            if (currentMode != 3) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "location_high_accuracy");
            }
            mode = 3;
        } else if (emiter == this.mBatterySaving) {
            if (currentMode != 2) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "location_battery_saving");
            }
            mode = 2;
        } else if (emiter == this.mSensorsOnly) {
            if (currentMode != 1) {
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "location_sensors_only");
            }
            mode = 1;
        }
        setLocationMode(mode);
    }

    private void refreshCategoryRecent() {
        this.mCategoryRecentLocationRequests = (PreferenceCategory) findPreference("recent_location_requests");
        this.mCategoryRecentLocationRequests.removeAll();
        List<Request> recentLocationRequests = new RecentLocationApps((SettingsActivity) getActivity()).getAppList();
        List<Preference> recentLocationPrefs = new ArrayList(recentLocationRequests.size());
        for (Request request : recentLocationRequests) {
            Preference pref = new Preference(getPrefContext());
            pref.setLayoutResource(2130969013);
            pref.setWidgetLayoutResource(2130968998);
            pref.setIcon(request.icon);
            pref.setTitle(request.label);
            if (request.isHighBattery) {
                if (request.isLocating()) {
                    pref.setSummary(2131627992);
                } else {
                    pref.setSummary(2131625476);
                }
            } else if (request.isLocating()) {
                pref.setSummary(2131627993);
            } else {
                pref.setSummary(2131625477);
            }
            pref.setOnPreferenceClickListener(new PackageEntryClickedListener(request.packageName, request.userHandle));
            recentLocationPrefs.add(pref);
        }
        if (recentLocationRequests.size() > 0) {
            addPreferencesSorted(recentLocationPrefs, this.mCategoryRecentLocationRequests);
            return;
        }
        Preference banner = new Preference(getPrefContext());
        banner.setLayoutResource(2130968856);
        banner.setTitle(2131625474);
        banner.setSelectable(false);
        this.mCategoryRecentLocationRequests.addPreference(banner);
    }

    private static boolean hasLocationAssist(Context context) {
        if (System.getInt(context.getContentResolver(), "has_time_synchronization", 0) == 0 && System.getInt(context.getContentResolver(), "has_agps_settings", 0) == 0 && System.getInt(context.getContentResolver(), "has_assisted_gps", 0) == 0 && System.getInt(context.getContentResolver(), "has_pgps_config", 0) == 0) {
            return false;
        }
        return true;
    }
}
