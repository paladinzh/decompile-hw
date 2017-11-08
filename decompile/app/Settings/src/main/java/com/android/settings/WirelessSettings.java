package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.settings.nfc.NfcEnablerHwExt;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.Utils;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UnknownFormatConversionException;

public class WirelessSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230948;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            boolean isSecondaryUser;
            boolean isWimaxEnabled;
            ArrayList<String> result = new ArrayList();
            if (((UserManager) context.getSystemService("user")).isAdminUser()) {
                isSecondaryUser = false;
            } else {
                isSecondaryUser = true;
            }
            if (isSecondaryUser) {
                isWimaxEnabled = false;
            } else {
                isWimaxEnabled = context.getResources().getBoolean(17956972);
            }
            if (!isWimaxEnabled) {
                result.add("wimax_settings");
            }
            if (isSecondaryUser) {
                result.add("vpn_settings");
            }
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
            boolean disableNfc = SystemProperties.getBoolean("ro.config.nfcdisabled", false);
            if (adapter == null || disableNfc) {
                result.add("nfc_entry");
            }
            if (isSecondaryUser || Utils.isWifiOnly(context)) {
                result.add("mobile_network_settings");
                result.add("manage_mobile_plan");
            }
            if (!context.getResources().getBoolean(2131492879)) {
                result.add("manage_mobile_plan");
            }
            PackageManager pm = context.getPackageManager();
            result.add("proxy_settings");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (isSecondaryUser || !cm.isTetheringSupported() || Utils.isWifiOnly(context)) {
                result.add("tether_settings");
            }
            if (!ImsManager.isWfcEnabledByPlatform(context) || Utils.isWfcForcedHidden(context)) {
                result.add("wifi_calling_settings");
            }
            result.add("link_plus_live_update");
            if (Utils.isChinaTelecomArea() || ((Utils.isRemoveForCmccArea(context) && SystemProperties.getBoolean("ro.config.hw_remove_networkmode", true)) || Utils.isWifiOnly(context) || !Utils.hasPackageInfo(context.getPackageManager(), "com.android.phone") || SystemProperties.getBoolean("ro.config.hw_hide_lte", false) || Utils.isRemoveEnable4G(context))) {
                result.add("enable_4g_setting");
            }
            if (Utils.isWifiOnly(context) || !Utils.isVoiceCapable(context)) {
                result.add("category_link");
            }
            return result;
        }
    };
    private Enable4GEnabler m4GEnabler;
    private ContentObserver mAirPlaneObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (WirelessSettings.this.mAirplaneDependencies != null && WirelessSettings.this.getActivity() != null) {
                boolean isAirplaneModeOn = WirelessSettings.this.isAirplaneModeOn();
                for (Preference pref : WirelessSettings.this.mAirplaneDependencies) {
                    pref.setEnabled(!isAirplaneModeOn);
                }
            }
        }
    };
    private ArrayList<Preference> mAirplaneDependencies = new ArrayList();
    private PreferenceScreen mButtonWfc;
    private BroadcastReceiver mCarrierConfigReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                WirelessSettings.this.handleVowifiPreference(context);
            }
        }
    };
    private ConnectivityManager mCm;
    private CustomSwitchPreference mEnable4GPreference;
    private boolean mEnable4GSupported = true;
    private HwCustWirelessSettings mHwCustWirelessSettings;
    private String mManageMobilePlanMessage;
    private NfcAdapter mNfcAdapter;
    private NfcEnablerHwExt mNfcEnabler;
    private PackageManager mPm;
    private TelephonyManager mTm;
    private UserManager mUm;
    private boolean mWifiCallingSupported = true;

    private boolean isAirplaneModeOn() {
        return Global.getInt(getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        log("onPreferenceTreeClick: preference=" + preference);
        if (preference == findPreference("manage_mobile_plan")) {
            onManageMobilePlanClick();
        } else if ("mobile_network_settings".equals(preference.getKey())) {
            startActivity(preference.getIntent());
            SettingsExtUtils.setAnimationReflection(getActivity());
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "mobile_network_settings");
            return true;
        }
        ItemUseStat.getInstance().handleClick(getActivity(), 2, preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    public void onManageMobilePlanClick() {
        log("onManageMobilePlanClick:");
        this.mManageMobilePlanMessage = null;
        Resources resources = getActivity().getResources();
        NetworkInfo ni = this.mCm.getActiveNetworkInfo();
        if (this.mTm.hasIccCard() && ni != null) {
            Intent provisioningIntent = new Intent("android.intent.action.ACTION_CARRIER_SETUP");
            List<String> carrierPackages = this.mTm.getCarrierPackageNamesForIntent(provisioningIntent);
            if (carrierPackages == null || carrierPackages.isEmpty()) {
                String url = this.mCm.getMobileProvisioningUrl();
                if (TextUtils.isEmpty(url)) {
                    String operatorName = this.mTm.getSimOperatorName();
                    try {
                        if (TextUtils.isEmpty(operatorName)) {
                            operatorName = this.mTm.getNetworkOperatorName();
                            if (TextUtils.isEmpty(operatorName)) {
                                this.mManageMobilePlanMessage = resources.getString(2131625462);
                            } else {
                                this.mManageMobilePlanMessage = resources.getString(2131625463, new Object[]{operatorName});
                            }
                        } else {
                            this.mManageMobilePlanMessage = resources.getString(2131625463, new Object[]{operatorName});
                        }
                    } catch (UnknownFormatConversionException e) {
                        Log.e("WirelessSettings", "UnknownFormatConversionException: " + e + ", operatorName=" + operatorName);
                        e.printStackTrace();
                    }
                } else {
                    Intent intent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
                    intent.setData(Uri.parse(url));
                    intent.setFlags(272629760);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e2) {
                        Log.w("WirelessSettings", "onManageMobilePlanClick: startActivity failed" + e2);
                    }
                }
            } else {
                if (carrierPackages.size() != 1) {
                    Log.w("WirelessSettings", "Multiple matching carrier apps found, launching the first.");
                }
                provisioningIntent.setPackage((String) carrierPackages.get(0));
                try {
                    startActivity(provisioningIntent);
                } catch (ActivityNotFoundException e22) {
                    Log.w("WirelessSettings", "onManageMobilePlanClick: startActivity failed" + e22);
                }
                return;
            }
        } else if (this.mTm.hasIccCard()) {
            this.mManageMobilePlanMessage = resources.getString(2131625465);
        } else {
            this.mManageMobilePlanMessage = resources.getString(2131625464);
        }
        if (!TextUtils.isEmpty(this.mManageMobilePlanMessage)) {
            log("onManageMobilePlanClick: message=" + this.mManageMobilePlanMessage);
            showDialog(1);
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        log("onCreateDialog: dialogId=" + dialogId);
        switch (dialogId) {
            case 1:
                return new Builder(getActivity()).setMessage(this.mManageMobilePlanMessage).setCancelable(false).setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WirelessSettings.this.log("MANAGE_MOBILE_PLAN_DIALOG.onClickListener id=" + id);
                        WirelessSettings.this.mManageMobilePlanMessage = null;
                    }
                }).create();
            default:
                return super.onCreateDialog(dialogId);
        }
    }

    private void log(String s) {
        Log.d("WirelessSettings", s);
    }

    protected int getMetricsCategory() {
        return 110;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustWirelessSettings = (HwCustWirelessSettings) HwCustUtils.createObj(HwCustWirelessSettings.class, new Object[]{this});
        if (savedInstanceState != null) {
            this.mManageMobilePlanMessage = savedInstanceState.getString("mManageMobilePlanMessage");
        }
        log("onCreate: mManageMobilePlanMessage=" + this.mManageMobilePlanMessage);
        this.mCm = (ConnectivityManager) getSystemService("connectivity");
        this.mTm = (TelephonyManager) getSystemService("phone");
        this.mPm = getPackageManager();
        this.mUm = (UserManager) getSystemService("user");
        addPreferencesFromResource(2131230948);
        boolean isAdmin = this.mUm.isAdminUser();
        Activity activity = getActivity();
        Preference nfcEntryPreference = (PreferenceScreen) findPreference("nfc_entry");
        getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirPlaneObserver);
        if (this.mAirplaneDependencies == null) {
            this.mAirplaneDependencies = new ArrayList();
        } else {
            this.mAirplaneDependencies.clear();
        }
        this.mButtonWfc = (PreferenceScreen) findPreference("wifi_calling_settings");
        String toggleable = Global.getString(activity.getContentResolver(), "airplane_mode_toggleable_radios");
        boolean isAirplaneModeOn = isAirplaneModeOn();
        boolean isWimaxEnabled = isAdmin ? getResources().getBoolean(17956972) : false;
        Preference ps;
        if (!isWimaxEnabled || RestrictedLockUtils.hasBaseUserRestriction(activity, "no_config_mobile_networks", UserHandle.myUserId())) {
            PreferenceScreen root = getPreferenceScreen();
            ps = findPreference("wimax_settings");
            if (ps != null) {
                root.removePreference(ps);
            }
        } else if (toggleable == null || (!toggleable.contains("wimax") && isWimaxEnabled)) {
            ps = findPreference("wimax_settings");
            if (!this.mAirplaneDependencies.contains(ps)) {
                this.mAirplaneDependencies.add(ps);
            }
            ps.setEnabled(!isAirplaneModeOn);
        }
        if (toggleable == null || !toggleable.contains("wifi")) {
            Preference vpnPreference = findPreference("vpn_settings");
            if (!this.mAirplaneDependencies.contains(vpnPreference)) {
                this.mAirplaneDependencies.add(vpnPreference);
            }
            vpnPreference.setEnabled(!isAirplaneModeOn);
        }
        if (!isAdmin || RestrictedLockUtils.hasBaseUserRestriction(activity, "no_config_vpn", UserHandle.myUserId())) {
            removePreference("vpn_settings");
        }
        ConnectivityManager cm;
        boolean adminDisallowedTetherConfig;
        if (toggleable == null || toggleable.contains("bluetooth")) {
            if (toggleable == null || !toggleable.contains("nfc")) {
                if (!this.mAirplaneDependencies.contains(nfcEntryPreference)) {
                    this.mAirplaneDependencies.add(nfcEntryPreference);
                }
                nfcEntryPreference.setEnabled(isAirplaneModeOn);
            }
            this.mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (this.mNfcAdapter == null || (this.mHwCustWirelessSettings != null && this.mHwCustWirelessSettings.isNfcDisabled(activity))) {
                getPreferenceScreen().removePreference(nfcEntryPreference);
                this.mNfcEnabler = null;
            }
            if (isAdmin || Utils.isWifiOnly(getActivity()) || RestrictedLockUtils.hasBaseUserRestriction(activity, "no_config_mobile_networks", UserHandle.myUserId())) {
                removePreference("mobile_network_settings");
                removePreference("manage_mobile_plan");
            } else {
                Preference mobileNetwork = findPreference("mobile_network_settings");
                RestrictedPreference restrictedMobileNetwork = null;
                if (mobileNetwork != null && (mobileNetwork instanceof RestrictedPreference)) {
                    restrictedMobileNetwork = (RestrictedPreference) mobileNetwork;
                }
                if (restrictedMobileNetwork != null) {
                    restrictedMobileNetwork.setEnabled(!isAirplaneModeOn);
                    EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_config_mobile_networks", UserHandle.myUserId());
                    if (!(RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_config_mobile_networks", UserHandle.myUserId()) || admin == null)) {
                        restrictedMobileNetwork.setDisabledByAdmin(admin);
                    }
                    this.mAirplaneDependencies.add(restrictedMobileNetwork);
                }
            }
            if (!(getResources().getBoolean(2131492879) || findPreference("manage_mobile_plan") == null)) {
                removePreference("manage_mobile_plan");
            }
            Preference mGlobalProxy = findPreference("proxy_settings");
            DevicePolicyManager mDPM = (DevicePolicyManager) activity.getSystemService("device_policy");
            getPreferenceScreen().removePreference(mGlobalProxy);
            mGlobalProxy.setEnabled(mDPM.getGlobalProxyAdmin() != null);
            cm = (ConnectivityManager) activity.getSystemService("connectivity");
            adminDisallowedTetherConfig = RestrictedLockUtils.checkIfRestrictionEnforced(activity, "no_config_tethering", UserHandle.myUserId()) == null;
            if ((cm.isTetheringSupported() && !adminDisallowedTetherConfig) || RestrictedLockUtils.hasBaseUserRestriction(activity, "no_config_tethering", UserHandle.myUserId()) || Utils.isWifiOnly(activity) || !isAdmin) {
                getPreferenceScreen().removePreference(findPreference("tether_settings"));
            } else if (!adminDisallowedTetherConfig) {
                Preference p = findPreference("tether_settings");
                p.setTitle(Utils.getTetheringLabel(cm));
                p.setEnabled(TetherSettings.isProvisioningNeededButUnavailable(getActivity()));
            }
            if (!SystemProperties.getBoolean("ro.config.linkplus.liveupdate", false)) {
                Log.i("WirelessSettings", "remove link plus live update as it is not supported");
                removePreference("link_plus_live_update");
            }
            if (!Utils.isChinaTelecomArea() || ((Utils.isRemoveForCmccArea(activity) && SystemProperties.getBoolean("ro.config.hw_remove_networkmode", true)) || Utils.isWifiOnly(activity) || !Utils.hasPackageInfo(getPackageManager(), "com.android.phone") || SystemProperties.getBoolean("ro.config.hw_hide_lte", false) || Utils.isRemoveEnable4G(activity))) {
                removePreference("enable_4g_setting");
                this.mEnable4GSupported = false;
            } else {
                this.mEnable4GPreference = (CustomSwitchPreference) findPreference("enable_4g_setting");
                if (this.mHwCustWirelessSettings != null) {
                    this.mHwCustWirelessSettings.updateEnable4GPreferenceTitle(this.mEnable4GPreference);
                }
                this.m4GEnabler = new Enable4GEnabler(activity, this.mEnable4GPreference);
            }
            if (this.mHwCustWirelessSettings != null) {
                this.mHwCustWirelessSettings.updateCustPreference(activity);
            }
            setHasOptionsMenu(true);
        }
        if (this.mAirplaneDependencies.contains(nfcEntryPreference)) {
            this.mAirplaneDependencies.add(nfcEntryPreference);
        }
        if (isAirplaneModeOn) {
        }
        nfcEntryPreference.setEnabled(isAirplaneModeOn);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        getPreferenceScreen().removePreference(nfcEntryPreference);
        this.mNfcEnabler = null;
        if (isAdmin) {
        }
        removePreference("mobile_network_settings");
        removePreference("manage_mobile_plan");
        removePreference("manage_mobile_plan");
        Preference mGlobalProxy2 = findPreference("proxy_settings");
        DevicePolicyManager mDPM2 = (DevicePolicyManager) activity.getSystemService("device_policy");
        getPreferenceScreen().removePreference(mGlobalProxy2);
        if (mDPM2.getGlobalProxyAdmin() != null) {
        }
        mGlobalProxy2.setEnabled(mDPM2.getGlobalProxyAdmin() != null);
        cm = (ConnectivityManager) activity.getSystemService("connectivity");
        if (RestrictedLockUtils.checkIfRestrictionEnforced(activity, "no_config_tethering", UserHandle.myUserId()) == null) {
        }
        if (cm.isTetheringSupported()) {
        }
        if (adminDisallowedTetherConfig) {
            Preference p2 = findPreference("tether_settings");
            p2.setTitle(Utils.getTetheringLabel(cm));
            if (TetherSettings.isProvisioningNeededButUnavailable(getActivity())) {
            }
            p2.setEnabled(TetherSettings.isProvisioningNeededButUnavailable(getActivity()));
        }
        if (SystemProperties.getBoolean("ro.config.linkplus.liveupdate", false)) {
            Log.i("WirelessSettings", "remove link plus live update as it is not supported");
            removePreference("link_plus_live_update");
        }
        if (Utils.isChinaTelecomArea()) {
        }
        removePreference("enable_4g_setting");
        this.mEnable4GSupported = false;
        if (this.mHwCustWirelessSettings != null) {
            this.mHwCustWirelessSettings.updateCustPreference(activity);
        }
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mNfcEnabler != null) {
            this.mNfcEnabler.resume();
        }
        if (this.m4GEnabler != null) {
            this.m4GEnabler.resume();
        }
        Context context = getActivity();
        if (context != null) {
            context.registerReceiver(this.mCarrierConfigReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
            handleVowifiPreference(context);
        }
        if (this.mHwCustWirelessSettings != null) {
            this.mHwCustWirelessSettings.removeCustPreference(context, "wifi_calling_settings");
        }
        if (!this.mEnable4GSupported && !this.mWifiCallingSupported) {
            removePreference("category_link");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(this.mManageMobilePlanMessage)) {
            outState.putString("mManageMobilePlanMessage", this.mManageMobilePlanMessage);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mNfcEnabler != null) {
            this.mNfcEnabler.pause();
        }
        if (this.m4GEnabler != null) {
            this.m4GEnabler.pause();
        }
        Context context = getActivity();
        if (context != null) {
            context.unregisterReceiver(this.mCarrierConfigReceiver);
        }
    }

    public void onDestroy() {
        getContentResolver().unregisterContentObserver(this.mAirPlaneObserver);
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected int getHelpResource() {
        return 2131626540;
    }

    private void handleVowifiPreference(Context context) {
        if (!ImsManager.isWfcEnabledByPlatform(context) || Utils.isWfcForcedHidden(context)) {
            removePreference("wifi_calling_settings");
            this.mWifiCallingSupported = false;
            return;
        }
        int i;
        getPreferenceScreen().addPreference(this.mButtonWfc);
        PreferenceScreen preferenceScreen = this.mButtonWfc;
        if (ImsManager.isWfcEnabledByUser(context)) {
            i = 2131627698;
        } else {
            i = 2131627699;
        }
        preferenceScreen.setSummary(i);
    }
}
