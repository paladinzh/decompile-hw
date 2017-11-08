package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.settings.FaqTextPreference;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsExtUtils;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter;
import com.android.settingslib.bluetooth.BluetoothDeviceFilter.Filter;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import java.util.WeakHashMap;

public abstract class DeviceListPreferenceFragment extends RestrictedSettingsFragment implements BluetoothCallback {
    private PreferenceGroup mDeviceListGroup;
    final WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap = new WeakHashMap();
    private IntentFilter mFaqFilter;
    private BroadcastReceiver mFaqReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.FAQ".equals(intent.getAction())) {
                DeviceListPreferenceFragment.this.transferToFaqPage();
            }
        }
    };
    private Filter mFilter = BluetoothDeviceFilter.ALL_FILTER;
    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;
    protected FaqTextPreference mNotFindoutAvailDevices;
    protected FaqTextPreference mNotFindoutDestination;
    BluetoothDevice mSelectedDevice;

    abstract void addPreferencesForActivity();

    DeviceListPreferenceFragment(String restrictedKey) {
        super(restrictedKey);
    }

    final void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    final void setFilter(int filterType) {
        this.mFilter = BluetoothDeviceFilter.getFilter(filterType);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFaqFilter = new IntentFilter();
        this.mFaqFilter.addAction("android.bluetooth.FAQ");
        this.mLocalManager = Utils.getLocalBtManager(getActivity());
        if (this.mLocalManager == null) {
            Log.e("DeviceListPreferenceFragment", "Bluetooth is not supported on this device");
            return;
        }
        this.mLocalAdapter = this.mLocalManager.getBluetoothAdapter();
        addPreferencesForActivity();
        this.mDeviceListGroup = (PreferenceCategory) findPreference("bt_device_list");
        if (!Secure.putInt(getContentResolver(), "db_bluetooth_launch_pairing", 0)) {
            Log.e("DeviceListPreferenceFragment", "failed to save launch pairing status, key = db_bluetooth_launch_pairing, value = 0");
        }
        this.mLocalManager.getEventManager().registerCallback(this);
        getActivity().registerReceiver(this.mFaqReceiver, this.mFaqFilter);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFooterViewForList(view);
    }

    private void setFooterViewForList(View view) {
        if (view != null) {
            TextView v = (TextView) LayoutInflater.from(view.getContext()).inflate(2130969101, null);
            if (v != null) {
                setFooterView((View) v);
            }
        }
    }

    void setDeviceListGroup(PreferenceGroup preferenceGroup) {
        this.mDeviceListGroup = preferenceGroup;
    }

    public void onResume() {
        super.onResume();
        if (this.mLocalManager != null && !isUiRestricted()) {
            this.mLocalManager.setForegroundActivity(getActivity());
            updateProgressUi(this.mLocalAdapter.isDiscovering());
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mLocalManager != null && !isUiRestricted()) {
            this.mLocalManager.setForegroundActivity(null);
        }
    }

    public void onStop() {
        super.onStop();
        if (!getActivity().isChangingConfigurations()) {
            removeAllDevices();
        }
    }

    void removeAllDevices() {
        this.mLocalAdapter.stopScanning();
        this.mDevicePreferenceMap.clear();
        if (this.mDeviceListGroup instanceof BluetoothProgressCategory) {
            this.mDeviceListGroup.removeAll();
        } else {
            cleanDeviceList();
        }
    }

    void addCachedDevices() {
        for (CachedBluetoothDevice cachedDevice : this.mLocalManager.getCachedDeviceManager().getCachedDevicesCopy()) {
            onDeviceAdded(cachedDevice);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof BluetoothDevicePreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        BluetoothDevicePreference btPreference = (BluetoothDevicePreference) preference;
        this.mSelectedDevice = btPreference.getCachedDevice().getDevice();
        onDevicePreferenceClick(btPreference);
        return true;
    }

    boolean onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        return btPreference.onClicked();
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (this.mDevicePreferenceMap.get(cachedDevice) == null && this.mLocalAdapter.getBluetoothState() == 12 && this.mFilter.matches(cachedDevice.getDevice())) {
            createDevicePreference(cachedDevice);
        }
    }

    void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        if (this.mDeviceListGroup == null) {
            Log.w("DeviceListPreferenceFragment", "Trying to create a device preference before the list group/category exists!");
            return;
        }
        String key = cachedDevice.getDevice().getAddress();
        BluetoothDevicePreference preference = (BluetoothDevicePreference) getCachedPreference(key);
        if (preference == null) {
            preference = new BluetoothDevicePreference(getPrefContext(), cachedDevice);
            preference.setKey(key);
            this.mDeviceListGroup.addPreference(preference);
        } else {
            preference.rebind();
            this.mDeviceListGroup.addPreference(preference);
        }
        initDevicePreference(preference);
        this.mDevicePreferenceMap.put(cachedDevice, preference);
    }

    void initDevicePreference(BluetoothDevicePreference preference) {
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = (BluetoothDevicePreference) this.mDevicePreferenceMap.remove(cachedDevice);
        if (preference != null && this.mDeviceListGroup != null) {
            this.mDeviceListGroup.removePreference(preference);
        }
    }

    public void onScanningStateChanged(boolean started) {
        updateProgressUi(started);
    }

    private void updateProgressUi(boolean start) {
        if (this.mDeviceListGroup instanceof BluetoothProgressCategory) {
            ((BluetoothProgressCategory) this.mDeviceListGroup).setProgress(start);
        }
    }

    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == 10) {
            updateProgressUi(false);
        }
    }

    void cleanDeviceList() {
        if (this.mDeviceListGroup != null) {
            int i;
            Preference preference;
            if (isUiRestricted()) {
                for (i = this.mDeviceListGroup.getPreferenceCount() - 1; i >= 0; i--) {
                    preference = this.mDeviceListGroup.getPreference(i);
                    if (!"bluetooth_switch".equals(preference.getKey())) {
                        this.mDeviceListGroup.removePreference(preference);
                    }
                }
                return;
            }
            for (i = this.mDeviceListGroup.getPreferenceCount() - 1; i >= 0; i--) {
                preference = this.mDeviceListGroup.getPreference(i);
                if (!("bluetooth_switch".equals(preference.getKey()) || "bluetooth_visibility".equals(preference.getKey()) || "my_device_name".equals(preference.getKey()) || "show_received_files".equals(preference.getKey()) || "bt_location_scan_help_preference".equals(preference.getKey()))) {
                    this.mDeviceListGroup.removePreference(preference);
                }
            }
        }
    }

    protected void displayFaqPreference(PreferenceGroup listGroup) {
        if (listGroup != null && TextUtils.equals(getString(2131624838), listGroup.getTitle())) {
            if (listGroup.getPreferenceCount() == 0 || (listGroup.getPreferenceCount() == 1 && "no_device_found".equals(listGroup.getPreference(0).getKey()))) {
                if (this.mNotFindoutAvailDevices == null) {
                    this.mNotFindoutAvailDevices = new FaqTextPreference(getActivity(), 2130968914, 1);
                    this.mNotFindoutAvailDevices.setKey("no_available_device");
                }
                listGroup.removeAll();
                listGroup.addPreference(this.mNotFindoutAvailDevices);
                return;
            }
            removePreference(listGroup, "no_available_device");
            if (this.mNotFindoutDestination == null) {
                this.mNotFindoutDestination = new FaqTextPreference(getActivity(), 2130968915, 1);
            }
            this.mNotFindoutDestination.setOrder(1000);
            listGroup.setOrderingAsAdded(true);
            listGroup.addPreference(this.mNotFindoutDestination);
        }
    }

    protected void displayFaqPreference() {
        displayFaqPreference(this.mDeviceListGroup);
    }

    private void transferToFaqPage() {
        Context context = getActivity();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.FAQ_HELP");
        intent.putExtra("faq_device_type", 1);
        context.startActivity(intent);
        SettingsExtUtils.setAnimationReflection(context);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onDestroy() {
        this.mLocalManager.getEventManager().unregisterCallback(this);
        getActivity().unregisterReceiver(this.mFaqReceiver);
        super.onDestroy();
    }
}
