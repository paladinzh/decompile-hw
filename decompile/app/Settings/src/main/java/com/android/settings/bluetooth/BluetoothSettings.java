package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;
import com.android.settings.ScanSettingsPreference;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.bluetooth.BluetoothDevicePreference.BluetoothDevicePreferenceListener;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager.BtDialogObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BluetoothSettings extends DeviceListPreferenceFragment implements Indexable, BluetoothDevicePreferenceListener, BtDialogObserver {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131624807);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131627569);
            data.screenTitle = screenTitle;
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131627570);
            data.screenTitle = screenTitle;
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = res.getString(2131624450);
            data.screenTitle = screenTitle;
            result.add(data);
            LocalBluetoothManager lbtm = Utils.getLocalBtManager(context);
            if (lbtm != null) {
                Set<BluetoothDevice> bondedDevices = lbtm.getBluetoothAdapter().getBondedDevices();
                if (bondedDevices != null) {
                    for (BluetoothDevice device : bondedDevices) {
                        data = new SearchIndexableRaw(context);
                        data.title = device.getName();
                        data.screenTitle = res.getString(2131624807);
                        data.enabled = enabled;
                        result.add(data);
                    }
                }
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static View mSettingsDialogView = null;
    private PreferenceGroup mAvailableDevicesCategory;
    private boolean mAvailableDevicesCategoryIsPresent;
    private BluetoothEnabler mBluetoothEnabler;
    private SwitchPreference mBluetoothSwitchPreference;
    private final OnClickListener mDeviceProfilesListener = new OnClickListener() {
        public void onClick(View v) {
            if (!(v.getTag() instanceof CachedBluetoothDevice)) {
                HwLog.w("BluetoothSettings", "onClick() called for other View: " + v);
            } else if (!BluetoothSettings.this.isUiRestricted()) {
                CachedBluetoothDevice device = (CachedBluetoothDevice) v.getTag();
                Bundle args = new Bundle(1);
                args.putParcelable("device", device.getDevice());
                ((SettingsActivity) BluetoothSettings.this.getActivity()).startPreferencePanel(DeviceProfilesSettings.class.getName(), args, 2131624856, null, BluetoothSettings.this, 0);
                v.setOnClickListener(null);
            }
        }
    };
    private BluetoothDiscoverableEnabler mDiscoverableEnabler;
    private Dialog mErrorDialog;
    private boolean mFirstScanStateChange = true;
    private boolean mInPairing = false;
    private boolean mInitialScanStarted;
    private final IntentFilter mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
    private Preference mMyDevicePreference;
    private PreferenceGroup mPairedDevicesCategory;
    private Preference mReceivedFilesPreference;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(intent.getAction())) {
                BluetoothSettings.this.updateDeviceName(context);
            }
        }
    };
    private ArrayList<String> mUnpairedDevices;
    private TwoStatePreference mVisibilityPreference;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider, BluetoothCallback {
        private final LocalBluetoothManager mBluetoothManager;
        private final Context mContext;
        private boolean mEnabled;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mBluetoothManager = Utils.getLocalBtManager(context);
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            if (defaultAdapter != null) {
                if (listening) {
                    this.mEnabled = defaultAdapter.isEnabled();
                    this.mSummaryLoader.setSummary(this, getSummary());
                    this.mBluetoothManager.getEventManager().registerCallback(this);
                } else {
                    this.mBluetoothManager.getEventManager().unregisterCallback(this);
                }
            }
        }

        private CharSequence getSummary() {
            int i;
            Context context = this.mContext;
            if (this.mEnabled) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            return context.getString(i);
        }

        public void onBluetoothStateChanged(int bluetoothState) {
            this.mEnabled = bluetoothState == 12;
            this.mSummaryLoader.setSummary(this, getSummary());
        }

        public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
            this.mSummaryLoader.setSummary(this, getSummary());
        }

        public void onScanningStateChanged(boolean started) {
        }

        public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        }

        public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        }

        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        }
    }

    public BluetoothSettings() {
        super("no_config_bluetooth");
    }

    protected int getMetricsCategory() {
        return 24;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    void addPreferencesForActivity() {
        addPreferencesFromResource(2131230747);
        this.mPairedDevicesCategory = new PreferenceCategory(getPrefContext());
        this.mPairedDevicesCategory.setLayoutResource(2130968916);
        this.mPairedDevicesCategory.setKey("paired_devices");
        this.mPairedDevicesCategory.setOrder(4);
        getPreferenceScreen().addPreference(this.mPairedDevicesCategory);
        this.mAvailableDevicesCategory = new BluetoothProgressCategory(getActivity());
        this.mAvailableDevicesCategory.setSelectable(false);
        this.mAvailableDevicesCategory.setOrder(5);
        getPreferenceScreen().addPreference(this.mAvailableDevicesCategory);
        Activity activity = getActivity();
        this.mBluetoothSwitchPreference = (SwitchPreference) findPreference("bluetooth_switch");
        this.mVisibilityPreference = (TwoStatePreference) findPreference("bluetooth_visibility");
        this.mMyDevicePreference = findPreference("my_device_name");
        updateDeviceName(activity);
        this.mMyDevicePreference.setEnabled(this.mLocalAdapter.getBluetoothState() == 12);
        this.mReceivedFilesPreference = findPreference("show_received_files");
        this.mBluetoothEnabler = new BluetoothEnabler(activity, this.mBluetoothSwitchPreference);
        this.mDiscoverableEnabler = new BluetoothDiscoverableEnabler(this.mLocalAdapter, this.mVisibilityPreference);
        if (Utils.getLocalBtManager(getActivity()) != null) {
            Utils.getLocalBtManager(getActivity()).setDiscoverableEnabler(this.mDiscoverableEnabler);
        }
        setHasOptionsMenu(true);
    }

    private void updateDeviceName(Context context) {
        if (this.mMyDevicePreference == null || context == null) {
            Log.d("BluetoothSettings", "input para is illegal.");
            return;
        }
        CharSequence deviceName = Global.getString(getContentResolver(), "unified_device_name");
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = this.mLocalAdapter != null ? this.mLocalAdapter.getName() : "";
        }
        this.mMyDevicePreference.setSummary(deviceName);
    }

    public void onResume() {
        if (this.mBluetoothEnabler != null) {
            this.mBluetoothEnabler.resume();
        }
        super.onResume();
        Utils.getLocalBtManager(getActivity()).setBtDialogObserver(this);
        updateDeviceName(getActivity());
        if (this.mDiscoverableEnabler != null) {
            this.mDiscoverableEnabler.resume(getActivity());
        }
        if (isUiRestricted()) {
            setDeviceListGroup(getPreferenceScreen());
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(2131624437);
            }
            removeAllDevices();
            return;
        }
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
        if (this.mLocalAdapter != null) {
            updateContent(this.mLocalAdapter.getBluetoothState());
            onScanningStateChanged(this.mLocalAdapter.isDiscovering());
        }
        if (UserHandle.getCallingUserId() == 0) {
            BluetoothExtUtils.setBeamPushUrisCallback(getActivity());
        } else {
            HwLog.d("BluetoothSettings", "/ BluetoothSettings:The sub UserId = " + UserHandle.getCallingUserId());
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mPairedDevicesCategory != null) {
            this.mPairedDevicesCategory.removeAll();
        }
        if (this.mAvailableDevicesCategory != null) {
            this.mAvailableDevicesCategory.removeAll();
        }
        if (this.mUnpairedDevices != null) {
            this.mUnpairedDevices.clear();
        }
        if (this.mErrorDialog != null && this.mErrorDialog.isShowing()) {
            this.mErrorDialog.dismiss();
        }
        LocalBluetoothManager manager = Utils.getLocalBtManager(getActivity());
        if (manager != null) {
            manager.setBtDialogObserver(null);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mBluetoothEnabler != null) {
            this.mBluetoothEnabler.pause();
        }
        if (this.mDiscoverableEnabler != null) {
            this.mDiscoverableEnabler.pause();
        }
        if (!isUiRestricted()) {
            getActivity().unregisterReceiver(this.mReceiver);
            ItemUseStat.getInstance().cacheData(getActivity());
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mLocalAdapter != null && !isUiRestricted()) {
            int textId;
            boolean z;
            boolean bluetoothIsEnabled = this.mLocalAdapter.getBluetoothState() == 12;
            boolean isDiscovering = this.mLocalAdapter.isDiscovering();
            if (isDiscovering) {
                textId = 2131627572;
            } else {
                textId = 2131627571;
            }
            int drawableId = isDiscovering ? Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE) : Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_SEARCH);
            MenuItem add = menu.add(0, 1, 0, textId);
            if (!bluetoothIsEnabled || this.mInPairing) {
                z = false;
            } else {
                z = true;
            }
            add.setEnabled(z).setTitle(getString(textId)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), drawableId)).setShowAsAction(1);
            menu.add(0, 3, 0, 2131624424).setEnabled(bluetoothIsEnabled).setShowAsAction(0);
            menu.add(0, 4, 0, 2131626521).setEnabled(true).setShowAsAction(0);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "bluetooth_scan_or_stop");
                if (this.mLocalAdapter.isDiscovering()) {
                    this.mLocalAdapter.stopScanning();
                    this.mLocalAdapter.cancelDiscovery();
                } else if (this.mLocalAdapter.getBluetoothState() == 12) {
                    MetricsLogger.action(getActivity(), 160);
                    startScanning();
                }
                return true;
            case 3:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "detcet_timeout_setting");
                new BluetoothVisibilityTimeoutFragment().show(getFragmentManager(), "visibility timeout");
                return true;
            case 4:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "bt_menu_help");
                Intent intent = new Intent("android.intent.action.FAQ_HELP");
                intent.putExtra("faq_device_type", 1);
                getActivity().startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startScanning() {
        if (!isUiRestricted()) {
            if (!this.mAvailableDevicesCategoryIsPresent) {
                getPreferenceScreen().addPreference(this.mAvailableDevicesCategory);
                this.mAvailableDevicesCategoryIsPresent = true;
            }
            if (this.mAvailableDevicesCategory != null) {
                setDeviceListGroup(this.mAvailableDevicesCategory);
                removeAllDevices();
            }
            if (this.mUnpairedDevices != null) {
                this.mUnpairedDevices.clear();
            }
            this.mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
            this.mAvailableDevicesCategory.removeAll();
            this.mInitialScanStarted = true;
            this.mLocalAdapter.startScanning(true);
        }
    }

    boolean onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        this.mLocalAdapter.stopScanning();
        CachedBluetoothDevice device = btPreference.getCachedDevice();
        if (device == null) {
            HwLog.e("BluetoothSettings", "device not found!");
            return false;
        }
        int bondState = device.getBondState();
        boolean isConnected = device.isConnected();
        boolean ret = super.onDevicePreferenceClick(btPreference);
        HwLog.d("BluetoothSettings", "device = " + device.getName() + ", connect = " + isConnected + ", bond = " + bondState + ", ret = " + ret);
        if (!isConnected && bondState == 10 && ret) {
            this.mInPairing = true;
            getActivity().invalidateOptionsMenu();
        }
        return ret;
    }

    private void addDeviceCategory(PreferenceGroup preferenceGroup, int titleId, Filter filter, boolean addCachedDevices) {
        HwLog.d("BluetoothSettings", "addDeviceCategory  addCachedDevices=" + addCachedDevices);
        cacheRemoveAllPrefs(preferenceGroup);
        getPreferenceScreen().addPreference(preferenceGroup);
        preferenceGroup.setTitle(titleId);
        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        if (addCachedDevices) {
            if (BluetoothDeviceFilter.BONDED_DEVICE_FILTER.equals(filter)) {
                addCachedDevices();
            } else {
                addNonHiddenDevices();
            }
        }
        preferenceGroup.setEnabled(true);
        removeCachedPrefs(preferenceGroup);
    }

    private void updateContent(int bluetoothState) {
        requestJlogEnable(false);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        switch (bluetoothState) {
            case 10:
                if (isUiRestricted()) {
                    setOffMessage();
                    break;
                }
                setOffMessage();
            case 11:
                this.mInitialScanStarted = false;
                break;
            case 12:
                cleanDeviceList();
                Preference locationHelpPref = findPreference("bt_location_scan_help_preference");
                if (locationHelpPref != null) {
                    preferenceScreen.removePreference(locationHelpPref);
                }
                preferenceScreen.setOrderingAsAdded(true);
                this.mDevicePreferenceMap.clear();
                if (!isUiRestricted()) {
                    if (getPreferenceScreen().getPreferenceCount() == 0) {
                        getPreferenceScreen().addPreference(this.mPairedDevicesCategory);
                        getPreferenceScreen().addPreference(this.mAvailableDevicesCategory);
                    }
                    if (this.mDiscoverableEnabler == null && Utils.getLocalBtManager(getActivity()) != null) {
                        this.mDiscoverableEnabler = new BluetoothDiscoverableEnabler(this.mLocalAdapter, this.mVisibilityPreference);
                        this.mDiscoverableEnabler.resume(getActivity());
                        Utils.getLocalBtManager(getActivity()).setDiscoverableEnabler(this.mDiscoverableEnabler);
                    }
                    addDeviceCategory(this.mPairedDevicesCategory, 2131624837, BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
                    int numberOfPairedDevices = this.mPairedDevicesCategory.getPreferenceCount();
                    handClickTypeOfPairedBtDevices();
                    if (this.mDiscoverableEnabler != null) {
                        this.mDiscoverableEnabler.setNumberOfPairedDevices(numberOfPairedDevices);
                    }
                    if (isUiRestricted() || numberOfPairedDevices <= 0) {
                        if (preferenceScreen.findPreference("paired_devices") != null) {
                            preferenceScreen.removePreference(this.mPairedDevicesCategory);
                        }
                    } else if (preferenceScreen.findPreference("paired_devices") == null) {
                        preferenceScreen.addPreference(this.mPairedDevicesCategory);
                    }
                    addDeviceCategory(this.mAvailableDevicesCategory, 2131624838, BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, this.mInitialScanStarted);
                    if (!this.mInitialScanStarted) {
                        startScanning();
                    }
                    getActivity().invalidateOptionsMenu();
                    this.mVisibilityPreference.setEnabled(true);
                    this.mMyDevicePreference.setEnabled(true);
                    requestJlogEnable(true);
                    return;
                }
                break;
        }
        setDeviceListGroup(preferenceScreen);
        removeAllDevices();
        if (!isUiRestricted()) {
            getActivity().invalidateOptionsMenu();
        }
        this.mVisibilityPreference.setEnabled(false);
        this.mMyDevicePreference.setEnabled(false);
        requestJlogEnable(true);
    }

    private void setOffMessage() {
        Preference locationHelpPref = findPreference("bt_location_scan_help_preference");
        boolean bleScanningMode = Global.getInt(getContentResolver(), "ble_scan_always_enabled", 0) == 1;
        if (locationHelpPref == null) {
            if (bleScanningMode) {
                locationHelpPref = new ScanSettingsPreference(getActivity(), getText(2131624849));
                locationHelpPref.setKey("bt_location_scan_help_preference");
                getPreferenceScreen().addPreference(locationHelpPref);
            }
        } else if (!bleScanningMode) {
            getPreferenceScreen().removePreference(locationHelpPref);
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        this.mInPairing = false;
        BluetoothExtUtils.handleAdapterStateChange(this.mLocalAdapter, bluetoothState, getActivity());
        updateContent(bluetoothState);
    }

    public void onScanningStateChanged(boolean started) {
        super.onScanningStateChanged(started);
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
        if (this.mFirstScanStateChange) {
            this.mFirstScanStateChange = false;
            return;
        }
        if (!started) {
            displayFaqPreference(this.mAvailableDevicesCategory);
        }
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (bondState != 11) {
            this.mInPairing = false;
            handlePbapCautionState(cachedDevice, bondState);
        }
        setDeviceListGroup(getPreferenceScreen());
        removeAllDevices();
        updateContent(this.mLocalAdapter.getBluetoothState());
        displayFaqPreference(this.mAvailableDevicesCategory);
    }

    void initDevicePreference(BluetoothDevicePreference preference) {
        if (preference.getCachedDevice().getBondState() == 12) {
            preference.setOnSettingsClickListener(this.mDeviceProfilesListener);
        }
        preference.setBluetoothDevicePreferenceListener(this);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Intent intent;
        if (!(preference == this.mBluetoothSwitchPreference || preference == this.mVisibilityPreference)) {
            String str;
            ItemUseStat instance = ItemUseStat.getInstance();
            Context activity = getActivity();
            if (preference.getKey() == null) {
                str = "available_device";
            } else {
                str = preference.getKey();
            }
            instance.handleClick(activity, 2, str);
        }
        if (preference == this.mMyDevicePreference) {
            intent = new Intent("android.settings.DEVICE_NAME_SETTINGS");
            intent.putExtra("device_name", preference.getSummary());
            startActivity(intent);
        } else if (preference == this.mReceivedFilesPreference) {
            intent = new Intent("android.btopp.intent.action.OPEN_RECEIVED_FILES");
            try {
                intent.setFlags(67108864);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                getActivity().sendBroadcast(intent);
                e.printStackTrace();
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getBooleanExtra("unpair", false)) {
            BluetoothDevice device = (BluetoothDevice) data.getParcelableExtra("unpair_device");
            if (device != null) {
                HwLog.i("BluetoothSettings", "onActivityResult unpair device, name = " + device.getName());
                if (!this.mUnpairedDevices.contains(device.getAddress())) {
                    this.mUnpairedDevices.add(device.getAddress());
                }
            } else {
                HwLog.e("BluetoothSettings", "onActivityResult unpair NULL device!");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestJlogEnable(boolean enable) {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).requestJlogEnable(enable);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        HwLog.d("BluetoothSettings", "onSaveInstanceState mInitialScanStarted = " + this.mInitialScanStarted);
        outState.putBoolean("key_scan_started", this.mInitialScanStarted);
        outState.putBoolean("key_first_scan_changed", this.mFirstScanStateChange);
        outState.putStringArrayList("key_unpaired_devices", this.mUnpairedDevices);
        super.onSaveInstanceState(outState);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mInitialScanStarted = savedInstanceState.getBoolean("key_scan_started", false);
            HwLog.d("BluetoothSettings", "onCreated mInitialScanStarted = " + this.mInitialScanStarted);
            this.mFirstScanStateChange = savedInstanceState.getBoolean("key_first_scan_changed", true);
            HwLog.d("BluetoothSettings", "onCreated mFirstScanStateChange = " + this.mFirstScanStateChange);
            this.mUnpairedDevices = savedInstanceState.getStringArrayList("key_unpaired_devices");
        }
        if (this.mUnpairedDevices == null) {
            this.mUnpairedDevices = new ArrayList();
        } else {
            Log.d("BluetoothSettings", "Unpaired devices will be invisible!");
        }
    }

    private void handClickTypeOfPairedBtDevices() {
        if (this.mLocalManager != null) {
            int type_computer = 0;
            int type_phone = 0;
            int type_peripheral = 0;
            int type_imaging = 0;
            int type_headset = 0;
            for (CachedBluetoothDevice cachedDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
                if (12 == cachedDevice.getBondState()) {
                    BluetoothClass btClass = cachedDevice.getBtClass();
                    if (btClass != null) {
                        switch (btClass.getMajorDeviceClass()) {
                            case 256:
                                type_computer++;
                                break;
                            case 512:
                                type_phone++;
                                break;
                            case 1280:
                                type_peripheral++;
                                break;
                            case 1536:
                                type_imaging++;
                                break;
                        }
                        if (btClass.doesClassMatch(0)) {
                            type_headset++;
                        }
                    } else {
                        HwLog.w("BluetoothSettings", "btClass is null");
                    }
                }
            }
            if ((((type_computer + type_phone) + type_peripheral) + type_imaging) + type_headset != 0) {
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_all_devices", (((type_computer + type_phone) + type_peripheral) + type_imaging) + type_headset);
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_computer", type_computer);
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_phone", type_phone);
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_peripheral", type_peripheral);
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_imaging", type_imaging);
                ItemUseStat.getInstance().handleClick(getActivity(), 5, "bt_headset", type_headset);
            }
        }
    }

    private void addNonHiddenDevices() {
        for (CachedBluetoothDevice cachedDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
            if (this.mUnpairedDevices.contains(cachedDevice.getDevice().getAddress())) {
                HwLog.d("BluetoothSettings", "Device hidden, name = " + cachedDevice.getDevice().getName());
            } else {
                onDeviceAdded(cachedDevice);
            }
        }
    }

    private void handlePbapCautionState(CachedBluetoothDevice cachedDevice, int bondState) {
        Context context = getActivity();
        BluetoothDevice device = cachedDevice.getDevice();
        if (context != null && device != null) {
            try {
                Editor editor = context.getSharedPreferences("pbap_caution_state", 0).edit();
                if (bondState == 12) {
                    editor.putInt(device.getAddress(), 0);
                    editor.apply();
                }
                if (bondState == 10) {
                    editor.remove(device.getAddress());
                    editor.apply();
                }
            } catch (Exception e) {
                HwLog.e("BluetoothSettings", "Failed to update PBAP caution state.");
                e.printStackTrace();
            }
        }
    }

    public void onBluetoothWearDialogDismiss() {
        this.mInPairing = false;
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public void onBluetoothWearRequireDeleteDevice(CachedBluetoothDevice cachedDevice) {
        setDeviceListGroup(this.mAvailableDevicesCategory);
        onDeviceDeleted(cachedDevice);
    }

    public void onDialogShow(Dialog dialog) {
        this.mErrorDialog = dialog;
    }
}
