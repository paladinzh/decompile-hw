package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.wifi.HwCustHotspotAuthentication;
import com.android.settings.wifi.WifiApDialog;
import com.android.settings.wifi.WifiApEnabler;
import com.android.settingslib.R$string;
import com.android.settingslib.TetherUtil;
import com.huawei.cust.HwCustUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TetherSettings extends TetherSettingsHwBase implements OnClickListener, OnPreferenceChangeListener, Listener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (Utils.isWifiOnly(context)) {
                return null;
            }
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = 2131230911;
            indexables.add(indexable);
            return indexables;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            result.add("wifi_bridge_settings");
            result.add("wifi_ap_ssid_and_security");
            result.add("disabled_on_data_saver");
            if (Utils.isWifiOnly(context)) {
                result.add("usb_tether_settings");
            }
            return result;
        }
    };
    private boolean mBluetoothEnableForTether;
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference();
    private String[] mBluetoothRegexs;
    private TwoStatePreference mBluetoothTether;
    private ConnectivityManager mCm;
    private HwCustHotspotAuthentication mCust;
    private DataSaverBackend mDataSaverBackend;
    private boolean mDataSaverEnabled;
    private Preference mDataSaverFooter;
    private WifiApDialog mDialog;
    private Handler mHandler = new Handler();
    private HwCustTetherSettings mHwCustTetherSettings;
    private boolean mMassStorageActive;
    private ServiceListener mProfileServiceListener = new ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            TetherSettings.this.mBluetoothPan.set((BluetoothPan) proxy);
        }

        public void onServiceDisconnected(int profile) {
            TetherSettings.this.mBluetoothPan.set(null);
        }
    };
    private boolean mRestartWifiApAfterConfigChange;
    private OnStartTetheringCallback mStartTetheringCallback;
    private BroadcastReceiver mTetherChangeReceiver;
    private boolean mUnavailable;
    private boolean mUsbConnected;
    private WifiApEnabler mWifiApEnabler;
    private PreferenceScreen mWifiBridgeEntryPreference;

    private static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<TetherSettings> mTetherSettings;

        OnStartTetheringCallback(TetherSettings settings) {
            this.mTetherSettings = new WeakReference(settings);
        }

        public void onTetheringStarted() {
            update();
        }

        public void onTetheringFailed() {
            update();
        }

        private void update() {
            TetherSettings settings = (TetherSettings) this.mTetherSettings.get();
            if (settings != null) {
                settings.updateState();
            }
        }
    }

    private class TetherChangeReceiver extends BroadcastReceiver {
        private TetherChangeReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (TetherSettings.this.mCust != null) {
                TetherSettings.this.mCust.custTetherReceiver(intent);
            }
            if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                ArrayList<String> available = intent.getStringArrayListExtra("availableArray");
                ArrayList<String> active = intent.getStringArrayListExtra("activeArray");
                ArrayList<String> errored = intent.getStringArrayListExtra("erroredArray");
                if (!(available == null || active == null || errored == null)) {
                    TetherSettings.this.updateState((String[]) available.toArray(new String[available.size()]), (String[]) active.toArray(new String[active.size()]), (String[]) errored.toArray(new String[errored.size()]));
                }
                if (TetherSettings.this.mWifiManager.getWifiApState() == 11 && TetherSettings.this.mRestartWifiApAfterConfigChange) {
                    TetherSettings.this.mRestartWifiApAfterConfigChange = false;
                    Log.d("TetherSettings", "Restarting WifiAp due to prior config change.");
                    TetherSettings.this.startTethering(0);
                }
            } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                if (intent.getIntExtra("wifi_state", 0) == 11 && TetherSettings.this.mRestartWifiApAfterConfigChange) {
                    TetherSettings.this.mRestartWifiApAfterConfigChange = false;
                    Log.d("TetherSettings", "Restarting WifiAp due to prior config change.");
                    TetherSettings.this.startTethering(0);
                }
            } else if (action.equals("android.intent.action.MEDIA_SHARED")) {
                TetherSettings.this.mMassStorageActive = true;
                TetherSettings.this.updateState();
            } else if (action.equals("android.intent.action.MEDIA_UNSHARED")) {
                TetherSettings.this.mMassStorageActive = false;
                TetherSettings.this.updateState();
            } else if (action.equals("android.hardware.usb.action.USB_STATE")) {
                TetherSettings.this.mUsbConnected = intent.getBooleanExtra("connected", false);
                TetherSettings.this.updateState();
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                if (TetherSettings.this.mBluetoothEnableForTether) {
                    switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE)) {
                        case Integer.MIN_VALUE:
                        case 10:
                            TetherSettings.this.mBluetoothEnableForTether = false;
                            break;
                        case 12:
                            TetherSettings.this.startTethering(2);
                            TetherSettings.this.mBluetoothEnableForTether = false;
                            break;
                    }
                }
                TetherSettings.this.updateState();
            }
        }
    }

    protected int getMetricsCategory() {
        return 90;
    }

    public TetherSettings() {
        super("no_config_tethering");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230911);
        Activity activity = getActivity();
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataSaverEnabled = this.mDataSaverBackend.isDataSaverEnabled();
        this.mDataSaverFooter = findPreference("disabled_on_data_saver");
        this.mHwCustTetherSettings = (HwCustTetherSettings) HwCustUtils.createObj(HwCustTetherSettings.class, new Object[]{this});
        if (this.mHwCustTetherSettings != null) {
            this.mHwCustTetherSettings.configureDefaultWifiHotspotName(activity.getApplicationContext());
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(activity.getApplicationContext(), this.mProfileServiceListener, 5);
        }
        this.mCust = (HwCustHotspotAuthentication) HwCustUtils.createObj(HwCustHotspotAuthentication.class, new Object[0]);
        if (this.mCust != null) {
            this.mCust.initHwCustHotspotAuthenticationImpl(this.mContext);
        }
        this.mUsbTether = (TwoStatePreference) findPreference("usb_tether_settings");
        this.mUsbTether.setOnPreferenceChangeListener(this);
        this.mWifiBridgeEntryPreference = (PreferenceScreen) findPreference("wifi_bridge_settings");
        if (!SystemProperties.getBoolean("ro.config.hw_wifibridge", false)) {
            getPreferenceScreen().removePreference(this.mWifiBridgeEntryPreference);
        }
        if (this.mHwCustTetherSettings != null) {
            this.mHwCustTetherSettings.custUsbTetherDisable(this.mUsbTether, getActivity().getString(2131625440));
        }
        this.mBluetoothTether = (TwoStatePreference) findPreference("enable_bluetooth_tethering");
        this.mBluetoothTether.setOnPreferenceChangeListener(this);
        this.mDataSaverBackend.addListener(this);
        this.mCm = (ConnectivityManager) getSystemService("connectivity");
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mUsbRegexs = this.mCm.getTetherableUsbRegexs();
        this.mWifiRegexs = this.mCm.getTetherableWifiRegexs();
        this.mBluetoothRegexs = this.mCm.getTetherableBluetoothRegexs();
        boolean usbAvailable = this.mUsbRegexs.length != 0;
        boolean bluetoothAvailable = this.mBluetoothRegexs.length != 0;
        boolean hideSettingsUsbTether = this.mHwCustTetherSettings != null ? this.mHwCustTetherSettings.hideSettingsUsbTether() : false;
        if (!usbAvailable || Utils.isMonkeyRunning() || hideSettingsUsbTether || Utils.isWifiOnly(activity.getApplicationContext())) {
            Log.d("TetherSettings", "usbAvailable:" + usbAvailable + ", isMonkeyRunning:" + Utils.isMonkeyRunning() + ", hideSettingsUsbTether:" + hideSettingsUsbTether);
            getPreferenceScreen().removePreference(this.mUsbTether);
        }
        if (bluetoothAvailable) {
            boolean isBluetoothTetheringOn = true;
            if (this.mHwCustTetherSettings != null) {
                isBluetoothTetheringOn = this.mHwCustTetherSettings.setBluetoothTetheringVisibility(activity.getApplicationContext(), true);
            }
            if (isBluetoothTetheringOn) {
                BluetoothPan pan = (BluetoothPan) this.mBluetoothPan.get();
                if (pan == null || !pan.isTetheringOn()) {
                    this.mBluetoothTether.setChecked(false);
                } else {
                    this.mBluetoothTether.setChecked(true);
                }
            }
        } else {
            getPreferenceScreen().removePreference(this.mBluetoothTether);
        }
        if (this.mHwCustTetherSettings != null) {
            this.mHwCustTetherSettings.customizePreferenceScreen(getPreferenceScreen());
        }
        onDataSaverChanged(this.mDataSaverBackend.isDataSaverEnabled());
    }

    public void onDestroy() {
        this.mDataSaverBackend.remListener(this);
        super.onDestroy();
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        boolean z;
        boolean z2 = false;
        this.mDataSaverEnabled = isDataSaving;
        TwoStatePreference twoStatePreference = this.mUsbTether;
        if (this.mDataSaverEnabled) {
            z = false;
        } else {
            z = true;
        }
        twoStatePreference.setEnabled(z);
        TwoStatePreference twoStatePreference2 = this.mBluetoothTether;
        if (!this.mDataSaverEnabled) {
            z2 = true;
        }
        twoStatePreference2.setEnabled(z2);
        this.mDataSaverFooter.setVisible(this.mDataSaverEnabled);
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }

    public Dialog onCreateDialog(int id) {
        if (id != 2) {
            return super.onCreateDialog(id);
        }
        this.mDialog = new WifiApDialog(getActivity(), this, this.mWifiConfig);
        return this.mDialog;
    }

    public void onStart() {
        boolean z = true;
        super.onStart();
        if (this.mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R$string.tethering_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        Activity activity = getActivity();
        this.mStartTetheringCallback = new OnStartTetheringCallback(this);
        this.mMassStorageActive = isMassStorageActive();
        this.mTetherChangeReceiver = new TetherChangeReceiver();
        IntentFilter filter = new IntentFilter("android.net.conn.TETHER_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        Intent intent = activity.registerReceiver(this.mTetherChangeReceiver, filter);
        registerWifiApReceiver(this.mTetherChangeReceiver);
        filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNSHARED");
        filter.addDataScheme("file");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        if (intent != null) {
            this.mTetherChangeReceiver.onReceive(activity, intent);
        }
        if (this.mWifiApEnabler != null) {
            this.mWifiApEnabler.resume();
        }
        updateState();
        if (this.mBluetoothPan.get() == null) {
            int isBtThetherOn = System.getInt(getContentResolver(), "bt_tether_on", 0);
            TwoStatePreference twoStatePreference = this.mBluetoothTether;
            if (isBtThetherOn != 1) {
                z = false;
            }
            twoStatePreference.setChecked(z);
        }
    }

    public void onStop() {
        super.onStop();
        if (!this.mUnavailable) {
            getActivity().unregisterReceiver(this.mTetherChangeReceiver);
            this.mTetherChangeReceiver = null;
            this.mStartTetheringCallback = null;
            if (this.mWifiApEnabler != null) {
                this.mWifiApEnabler.pause();
            }
            if (this.mCust != null) {
                this.mCust.custStop();
            }
            if (this.mBluetoothTether != null) {
                System.putInt(getContentResolver(), "bt_tether_on", this.mBluetoothTether.isChecked() ? 1 : 0);
            }
        }
    }

    private void updateState() {
        updateState(this.mCm.getTetherableIfaces(), this.mCm.getTetheredIfaces(), this.mCm.getTetheringErroredIfaces());
    }

    private void updateState(String[] available, String[] tethered, String[] errored) {
        updateUsbState(available, tethered, errored);
        updateBluetoothState(available, tethered, errored);
    }

    private void updateUsbState(String[] available, String[] tethered, String[] errored) {
        boolean usbAvailable = this.mUsbConnected && !this.mMassStorageActive;
        int usbError = 0;
        for (String s : available) {
            for (String regex : this.mUsbRegexs) {
                if (s.matches(regex) && usbError == 0) {
                    usbError = this.mCm.getLastTetherError(s);
                }
            }
        }
        boolean usbTethered = false;
        for (String s2 : tethered) {
            for (String regex2 : this.mUsbRegexs) {
                if (s2.matches(regex2)) {
                    usbTethered = true;
                }
            }
        }
        boolean usbErrored = false;
        for (String s22 : errored) {
            for (String regex22 : this.mUsbRegexs) {
                if (s22.matches(regex22)) {
                    usbErrored = true;
                }
            }
        }
        if (usbTethered) {
            this.mUsbTether.setSummary(2131625438);
            this.mUsbTether.setEnabled(!this.mDataSaverEnabled);
            this.mUsbTether.setChecked(true);
        } else if (usbAvailable) {
            if (usbError == 0) {
                this.mUsbTether.setSummary(2131625437);
            } else {
                this.mUsbTether.setSummary(2131625442);
            }
            this.mUsbTether.setEnabled(!this.mDataSaverEnabled);
            this.mUsbTether.setChecked(false);
        } else if (usbErrored) {
            this.mUsbTether.setSummary(2131625442);
            this.mUsbTether.setEnabled(false);
            this.mUsbTether.setChecked(false);
        } else if (this.mMassStorageActive) {
            this.mUsbTether.setSummary(2131625439);
            this.mUsbTether.setEnabled(false);
            this.mUsbTether.setChecked(false);
        } else {
            this.mUsbTether.setSummary(2131625440);
            this.mUsbTether.setEnabled(false);
            this.mUsbTether.setChecked(false);
        }
        if (!(this.mHwCustTetherSettings == null || this.mContext == null)) {
            this.mHwCustTetherSettings.custUsbTetherDisable(this.mUsbTether, this.mContext.getString(2131625440));
        }
        updateUsbTetherChargingOnly();
    }

    private void updateBluetoothState(String[] available, String[] tethered, String[] errored) {
        boolean bluetoothErrored = false;
        for (String s : errored) {
            for (String regex : this.mBluetoothRegexs) {
                if (s.matches(regex)) {
                    bluetoothErrored = true;
                }
            }
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            int btState = adapter.getState();
            if (btState == 13) {
                this.mBluetoothTether.setEnabled(false);
                this.mBluetoothTether.setSummary(2131624462);
            } else if (btState == 11) {
                this.mBluetoothTether.setEnabled(false);
                this.mBluetoothTether.setSummary(2131624461);
            } else {
                BluetoothPan bluetoothPan = (BluetoothPan) this.mBluetoothPan.get();
                if (btState == 12 && bluetoothPan != null && bluetoothPan.isTetheringOn()) {
                    this.mBluetoothTether.setChecked(true);
                    this.mBluetoothTether.setEnabled(!this.mDataSaverEnabled);
                    int bluetoothTethered = bluetoothPan.getConnectedDevices().size();
                    if (bluetoothTethered > 1) {
                        this.mBluetoothTether.setSummary((CharSequence) getResources().getQuantityString(2131689513, bluetoothTethered, new Object[]{Integer.valueOf(bluetoothTethered)}));
                    } else if (bluetoothTethered == 1) {
                        this.mBluetoothTether.setSummary(2131625445);
                    } else if (bluetoothErrored) {
                        this.mBluetoothTether.setSummary(2131625449);
                        if (this.mCust != null) {
                            this.mCust.handleCustErrorView(this.mBluetoothTether);
                        }
                    } else {
                        this.mBluetoothTether.setSummary(2131625444);
                    }
                } else {
                    this.mBluetoothTether.setEnabled(!this.mDataSaverEnabled);
                    this.mBluetoothTether.setChecked(false);
                    this.mBluetoothTether.setSummary(2131625448);
                }
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        String key = preference.getKey();
        if ("enable_wifi_ap".equals(key)) {
            if (((Boolean) value).booleanValue()) {
                startTethering(0);
            } else {
                this.mCm.stopTethering(0);
            }
        } else if ("usb_tether_settings".equals(key)) {
            boolean newState = ((Boolean) value).booleanValue();
            Intent intent = new Intent("usb_tethered");
            if (newState) {
                intent.putExtra("usb_tethered_type", "usb_tethered_open");
                startTethering(1);
            } else {
                intent.putExtra("usb_tethered_type", "usb_tethered_close");
                this.mCm.stopTethering(1);
            }
            intent.setPackage("com.android.settings");
            getActivity().sendBroadcast(intent);
            return false;
        } else if ("enable_bluetooth_tethering".equals(key)) {
            if (this.mCust != null && !this.mCust.isTetheringAllowed(this.mBluetoothTether)) {
                Log.d("TetherSettings", "APN_TYPE_NOT_AVAILABLE");
                return false;
            } else if (((Boolean) value).booleanValue()) {
                startTethering(2);
            } else {
                this.mCm.stopTethering(2);
                BluetoothPan bluetoothPan = (BluetoothPan) this.mBluetoothPan.get();
                if (bluetoothPan != null) {
                    bluetoothPan.setBluetoothTethering(false);
                }
                updateState();
            }
        }
        return true;
    }

    public static boolean isProvisioningNeededButUnavailable(Context context) {
        if (!TetherUtil.isProvisioningNeeded(context) || isIntentAvailable(context)) {
            return false;
        }
        return true;
    }

    private static boolean isIntentAvailable(Context context) {
        String[] provisionApp = context.getResources().getStringArray(17235992);
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(provisionApp[0], provisionApp[1]);
        if (packageManager.queryIntentActivities(intent, 65536).size() > 0) {
            return true;
        }
        return false;
    }

    private void startTethering(int choice) {
        if (choice == 2) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.getState() == 10) {
                this.mBluetoothEnableForTether = true;
                adapter.enable();
                this.mBluetoothTether.setSummary(2131624461);
                this.mBluetoothTether.setEnabled(false);
                return;
            }
        }
        this.mCm.startTethering(choice, true, this.mStartTetheringCallback, this.mHandler);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == -1) {
            this.mWifiConfig = this.mDialog.getConfig();
            if (this.mWifiConfig != null) {
                if (this.mWifiManager.getWifiApState() == 13) {
                    Log.d("TetheringSettings", "Wifi AP config changed while enabled, stop and restart");
                    this.mRestartWifiApAfterConfigChange = true;
                    this.mCm.stopTethering(0);
                }
                this.mWifiManager.setWifiApConfiguration(this.mWifiConfig);
                int index = WifiApDialog.getSecurityTypeIndex(this.mWifiConfig);
                this.mCreateNetwork.setSummary(String.format(getActivity().getString(2131625063), new Object[]{this.mWifiConfig.SSID, this.mSecurityType[index]}));
            }
        }
    }

    public int getHelpResource() {
        return 2131626547;
    }
}
