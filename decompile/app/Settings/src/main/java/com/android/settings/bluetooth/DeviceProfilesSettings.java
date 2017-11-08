package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.TwoStatePreference;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import com.android.settings.SelectableEditTextPreference;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.MapProfile;
import com.android.settingslib.bluetooth.PanProfile;
import com.android.settingslib.bluetooth.PbapServerProfile;
import java.util.HashMap;

public final class DeviceProfilesSettings extends SettingsPreferenceFragment implements Callback, OnPreferenceChangeListener {
    private final HashMap<LocalBluetoothProfile, TwoStatePreference> mAutoConnectPrefs = new HashMap();
    private CachedBluetoothDevice mCachedDevice;
    private String mChangedPrefKey;
    private Context mContext;
    private SelectableEditTextPreference mDeviceNamePref;
    private AlertDialog mDisconnectDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwLog.w("DeviceProfilesSettings", "Begin to unpair device");
                    DeviceProfilesSettings.this.mCachedDevice.unpair();
                    Utils.removeSprefData(DeviceProfilesSettings.this.mContext, DeviceProfilesSettings.this.mCachedDevice.getDevice().getAddress());
                    return;
                default:
                    HwLog.w("DeviceProfilesSettings", "Incorrect message");
                    return;
            }
        }
    };
    private LocalBluetoothManager mManager;
    private BroadcastReceiver mProfileChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwLog.d("DeviceProfilesSettings", "profile receive action = " + action);
            if ("android.bluetooth.intent.action.GET_HUAWEI_DEVICE_BAND_MODE".equals(action)) {
                BluetoothDevice dev = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                HwLog.d("DeviceProfilesSettings", "talkband mode change, device = " + dev);
                if (dev != null && dev.getAddress().equalsIgnoreCase(DeviceProfilesSettings.this.mCachedDevice.getDevice().getAddress())) {
                    DeviceProfilesSettings.this.mTalkBandMode = intent.getIntExtra("bandmode", 0);
                    HwLog.d("DeviceProfilesSettings", "handle huawei talkband mode change, mTalkBandMode = " + DeviceProfilesSettings.this.mTalkBandMode);
                }
            }
        }
    };
    private PreferenceGroup mProfileContainer;
    private boolean mProfileGroupIsRemoved;
    private LocalBluetoothProfileManager mProfileManager;
    private RenameEditTextPreference mRenameDeviceNamePref;
    private int mTalkBandMode;

    private class RenameEditTextPreference implements TextWatcher {
        private RenameEditTextPreference() {
        }

        public void afterTextChanged(Editable s) {
            boolean z = false;
            Dialog d = DeviceProfilesSettings.this.mDeviceNamePref.getDialog();
            if (d instanceof AlertDialog) {
                Button button = ((AlertDialog) d).getButton(-1);
                if (s.length() > 0) {
                    z = true;
                }
                button.setEnabled(z);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        BluetoothDevice device;
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            device = (BluetoothDevice) savedInstanceState.getParcelable("device");
        } else {
            device = (BluetoothDevice) getArguments().getParcelable("device");
        }
        addPreferencesFromResource(2131230746);
        getPreferenceScreen().setOrderingAsAdded(false);
        this.mProfileContainer = (PreferenceGroup) findPreference("profile_container");
        this.mDeviceNamePref = (SelectableEditTextPreference) findPreference("rename_device");
        this.mDeviceNamePref.setInitialSelectionMode(0);
        if (device == null) {
            HwLog.w("DeviceProfilesSettings", "Activity started without a remote Bluetooth device");
            finish();
            return;
        }
        this.mRenameDeviceNamePref = new RenameEditTextPreference();
        this.mManager = Utils.getLocalBtManager(getActivity());
        if (this.mManager == null) {
            HwLog.w("DeviceProfilesSettings", "mManager == null");
            finish();
            return;
        }
        CachedBluetoothDeviceManager deviceManager = this.mManager.getCachedDeviceManager();
        this.mProfileManager = this.mManager.getProfileManager();
        this.mCachedDevice = deviceManager.findDevice(device);
        if (this.mCachedDevice == null) {
            HwLog.w("DeviceProfilesSettings", "Device not found, cannot connect to it");
            finish();
            return;
        }
        String deviceName = this.mCachedDevice.getName();
        this.mDeviceNamePref.setSummary((CharSequence) deviceName);
        this.mDeviceNamePref.setText(deviceName);
        this.mDeviceNamePref.setOnPreferenceChangeListener(this);
        addPreferencesForProfiles();
        this.mContext = getActivity();
        if (this.mContext == null) {
            finish();
            return;
        }
        Intent intent = this.mContext.registerReceiver(this.mProfileChangeReceiver, new IntentFilter("android.bluetooth.intent.action.GET_HUAWEI_DEVICE_BAND_MODE"), "android.permission.ACCESS_BLUETOOTH_SHARE", null);
        this.mTalkBandMode = 1;
        if (intent != null) {
            BluetoothDevice dev = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            if (dev != null && dev.getAddress().equalsIgnoreCase(this.mCachedDevice.getDevice().getAddress())) {
                this.mTalkBandMode = intent.getIntExtra("bandmode", 1);
                HwLog.d("DeviceProfilesSettings", "mTalkBandMode from first intent = " + this.mTalkBandMode);
            }
        }
        if (savedInstanceState != null) {
            String prefKey = savedInstanceState.getString("disconnect_preference");
            HwLog.d("DeviceProfilesSettings", "prefKey=" + prefKey);
            if (prefKey != null) {
                LocalBluetoothProfile profile = getProfileOf(prefKey);
                if (profile == null) {
                    HwLog.e("DeviceProfilesSettings", "Bluetooth device profile not found, preference key = " + prefKey);
                    return;
                }
                askDisconnect(getActivity(), profile, prefKey);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDisconnectDialog != null) {
            this.mDisconnectDialog.dismiss();
            this.mDisconnectDialog = null;
        }
        if (this.mCachedDevice != null) {
            this.mCachedDevice.unregisterCallback(this);
        }
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mProfileChangeReceiver);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mCachedDevice != null) {
            outState.putParcelable("device", this.mCachedDevice.getDevice());
        }
        if (this.mDisconnectDialog != null && this.mDisconnectDialog.isShowing() && this.mChangedPrefKey != null) {
            outState.putString("disconnect_preference", this.mChangedPrefKey);
        }
    }

    public void onResume() {
        boolean z = true;
        super.onResume();
        this.mManager.setForegroundActivity(getActivity());
        if (this.mCachedDevice != null) {
            this.mCachedDevice.registerCallback(this);
            if (this.mCachedDevice.getBondState() == 10) {
                finish();
                return;
            }
            refresh();
        }
        EditText et = this.mDeviceNamePref.getEditText();
        if (et != null) {
            et.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(247)});
            et.addTextChangedListener(this.mRenameDeviceNamePref);
            Dialog d = this.mDeviceNamePref.getDialog();
            if (d instanceof AlertDialog) {
                Button b = ((AlertDialog) d).getButton(-1);
                if (et.getText().length() <= 0) {
                    z = false;
                }
                b.setEnabled(z);
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mCachedDevice != null) {
            this.mCachedDevice.unregisterCallback(this);
        }
        this.mManager.setForegroundActivity(null);
    }

    private void addPreferencesForProfiles() {
        this.mProfileContainer.removeAll();
        for (LocalBluetoothProfile profile : this.mCachedDevice.getConnectableProfiles()) {
            if (profile instanceof PbapServerProfile) {
                HwLog.w("DeviceProfilesSettings", "Connectable PBAP Server Profile ignored.");
            } else if (profile instanceof MapProfile) {
                HwLog.w("DeviceProfilesSettings", "Connectable MAP Profile ignored.");
            } else {
                Preference pref = createProfilePreference(profile);
                this.mProfileContainer.addPreference(pref);
                HwLog.d("DeviceProfilesSettings", "Add connectable profile, pref title = " + pref.getTitle() + ", key = " + pref.getKey());
            }
        }
        int pbapPermission = this.mCachedDevice.getPhonebookPermissionChoice();
        HwLog.d("DeviceProfilesSettings", "pbapPermission = " + pbapPermission);
        if (pbapPermission != 0) {
            this.mProfileContainer.addPreference(createProfilePreference(this.mManager.getProfileManager().getPbapProfile()));
        } else {
            processPhonebookAccessDisplay();
        }
        MapProfile mapProfile = this.mManager.getProfileManager().getMapProfile();
        int mapPermission = this.mCachedDevice.getMessagePermissionChoice();
        HwLog.d("DeviceProfilesSettings", "mapPermission = " + mapPermission);
        if (mapPermission != 0) {
            this.mProfileContainer.addPreference(createProfilePreference(mapProfile));
        }
        showOrHideProfileGroup();
    }

    private void processPhonebookAccessDisplay() {
        if (this.mCachedDevice.getBondState() != 12) {
            HwLog.i("DeviceProfilesSettings", "processPhonebookAccessDisplay  device is not bonded.");
            return;
        }
        if (BluetoothUuid.containsAnyUuid(this.mCachedDevice.getDevice().getUuids(), PbapServerProfile.getPbapClientUuids())) {
            int btClass = this.mCachedDevice.getBtClass().getDeviceClass();
            HwLog.d("DeviceProfilesSettings", "processPhonebookAccessDisplay  btClass=" + btClass);
            if (btClass == 1032) {
                this.mCachedDevice.setPhonebookPermissionChoice(1);
            } else {
                this.mCachedDevice.setPhonebookPermissionChoice(2);
            }
            this.mProfileContainer.addPreference(createProfilePreference(this.mManager.getProfileManager().getPbapProfile()));
        } else {
            HwLog.d("DeviceProfilesSettings", "processPhonebookAccessDisplay  device donn't support PBAB_CLIENT_UUIDS");
        }
    }

    private void showOrHideProfileGroup() {
        int numProfiles = this.mProfileContainer.getPreferenceCount();
        if (!this.mProfileGroupIsRemoved && numProfiles == 0) {
            getPreferenceScreen().removePreference(this.mProfileContainer);
            this.mProfileGroupIsRemoved = true;
        } else if (this.mProfileGroupIsRemoved && numProfiles != 0) {
            getPreferenceScreen().addPreference(this.mProfileContainer);
            this.mProfileGroupIsRemoved = false;
        }
    }

    private TwoStatePreference createProfilePreference(LocalBluetoothProfile profile) {
        TwoStatePreference pref = new SwitchPreference(getActivity());
        pref.setKey(profile.toString());
        pref.setTitle(profile.getNameResource(this.mCachedDevice.getDevice()));
        pref.setPersistent(false);
        pref.setOrder(getProfilePreferenceIndex(profile.getOrdinal()));
        pref.setOnPreferenceChangeListener(this);
        pref.setLayoutResource(2130968913);
        int iconResource = profile.getDrawableResource(this.mCachedDevice.getBtClass());
        if (iconResource != 0) {
            pref.setIcon(getResources().getDrawable(iconResource));
        }
        refreshProfilePreference(pref, profile);
        return pref;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!preference.getKey().equals("unpair")) {
            return super.onPreferenceTreeClick(preference);
        }
        unpairDevice();
        Intent intent = new Intent();
        intent.putExtra("unpair", true);
        intent.putExtra("unpair_device", this.mCachedDevice.getDevice());
        ((SettingsActivity) getActivity()).finishPreferencePanel(this, 0, intent);
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mDeviceNamePref) {
            this.mCachedDevice.setName((String) newValue);
            return true;
        } else if (!(preference instanceof TwoStatePreference)) {
            return false;
        } else {
            if (((Boolean) newValue).booleanValue() == ((TwoStatePreference) preference).isChecked()) {
                return true;
            }
            onProfileClicked(getProfileOf(preference), (TwoStatePreference) preference);
            return false;
        }
    }

    private void onProfileClicked(LocalBluetoothProfile profile, TwoStatePreference profilePref) {
        boolean z = true;
        if (profile != null) {
            BluetoothDevice device = this.mCachedDevice.getDevice();
            if (device == null) {
                HwLog.e("DeviceProfilesSettings", "device not found!");
                return;
            }
            Utils.removeSprefData(this.mContext, device.getAddress());
            String profileKey = profilePref.getKey();
            HwLog.d("DeviceProfilesSettings", "onProfileClicked  profileKey=" + profileKey);
            if ("PBAP Server".equals(profileKey)) {
                int newPermission;
                if (this.mCachedDevice.getPhonebookPermissionChoice() == 1) {
                    newPermission = 2;
                } else {
                    newPermission = 1;
                }
                this.mCachedDevice.setPhonebookPermissionChoice(newPermission);
                if (newPermission != 1) {
                    z = false;
                }
                profilePref.setChecked(z);
                HwLog.d("DeviceProfilesSettings", "onProfileClicked  setPhonebookPermissionChoice newPermission=" + newPermission);
                return;
            }
            int status = profile.getConnectionStatus(device);
            HwLog.d("DeviceProfilesSettings", "onProfileClicked  status=" + status + ", isConnected=" + (status == 2));
            if (profilePref.isChecked()) {
                askDisconnect(this.mManager.getForegroundActivity(), profile, profilePref.getKey());
            } else {
                if (profile instanceof MapProfile) {
                    this.mCachedDevice.setMessagePermissionChoice(1);
                    refreshProfilePreference(profilePref, profile);
                }
                if (profile instanceof PanProfile) {
                    profile.setPreferred(device, true);
                    this.mCachedDevice.connectProfile(profile);
                } else if (profile.isPreferred(device)) {
                    boolean isA2dpProfile = profile instanceof A2dpProfile;
                    HwLog.d("DeviceProfilesSettings", "onProfileClicked isA2dpProfile =" + isA2dpProfile + ", isHeadsetProfile = " + (profile instanceof HeadsetProfile) + ", mTalkBandMode = " + this.mTalkBandMode + ", dev = " + device.getName());
                    if (!(this.mTalkBandMode == 0 && isA2dpProfile)) {
                        profile.setPreferred(device, false);
                    }
                    profile.setPreferred(device, true);
                    this.mCachedDevice.connectProfile(profile);
                    refreshProfilePreference(profilePref, profile);
                } else {
                    profile.setPreferred(device, true);
                    this.mCachedDevice.connectProfile(profile);
                }
            }
        }
    }

    private void askDisconnect(Context context, final LocalBluetoothProfile profile, String prefKey) {
        this.mChangedPrefKey = prefKey;
        final CachedBluetoothDevice device = this.mCachedDevice;
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(2131624444);
        }
        HwLog.d("DeviceProfilesSettings", "askDisconnect  device name=" + name + ", profileName=" + context.getString(profile.getNameResource(device.getDevice())));
        String title = context.getString(2131624441);
        String message = context.getString(2131624442, new Object[]{profileName, name});
        this.mDisconnectDialog = Utils.showDisconnectDialog(context, this.mDisconnectDialog, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                device.disconnect(profile);
                profile.setPreferred(device.getDevice(), false);
                HwLog.d("DeviceProfilesSettings", "askDisconnect  disconnect profile");
                if (profile instanceof MapProfile) {
                    device.setMessagePermissionChoice(2);
                    DeviceProfilesSettings.this.refreshProfilePreference((TwoStatePreference) DeviceProfilesSettings.this.findPreference(profile.toString()), profile);
                    HwLog.d("DeviceProfilesSettings", "askDisconnect  map profile disconnect");
                }
                DeviceProfilesSettings.this.mChangedPrefKey = null;
            }
        }, title, Html.fromHtml(message));
    }

    public void onDeviceAttributesChanged() {
        refresh();
    }

    private void refresh() {
        String deviceName = this.mCachedDevice.getName();
        this.mDeviceNamePref.setSummary((CharSequence) deviceName);
        this.mDeviceNamePref.setText(deviceName);
        refreshProfiles();
    }

    private void refreshProfiles() {
        for (LocalBluetoothProfile profile : this.mCachedDevice.getConnectableProfiles()) {
            TwoStatePreference profilePref = (TwoStatePreference) findPreference(profile.toString());
            if (profilePref == null) {
                this.mProfileContainer.addPreference(createProfilePreference(profile));
            } else {
                refreshProfilePreference(profilePref, profile);
            }
        }
        for (LocalBluetoothProfile profile2 : this.mCachedDevice.getRemovedProfiles()) {
            Preference profilePref2 = findPreference(profile2.toString());
            if (profilePref2 != null) {
                HwLog.d("DeviceProfilesSettings", "Removing " + profile2.toString() + " from profile list");
                this.mProfileContainer.removePreference(profilePref2);
            }
        }
        showOrHideProfileGroup();
    }

    private void refreshProfilePreference(TwoStatePreference profilePref, LocalBluetoothProfile profile) {
        boolean z;
        boolean z2 = true;
        BluetoothDevice device = this.mCachedDevice.getDevice();
        if (this.mCachedDevice.isBusy()) {
            z = false;
        } else {
            z = true;
        }
        profilePref.setEnabled(z);
        if (profile instanceof MapProfile) {
            if (this.mCachedDevice.getMessagePermissionChoice() != 1) {
                z2 = false;
            }
            profilePref.setChecked(z2);
        } else if (profile instanceof PbapServerProfile) {
            int permissonChoise = Utils.getSprefData(getActivity(), device.getAddress());
            if (this.mCachedDevice.getPhonebookPermissionChoice() == 1 || permissonChoise == 1) {
                profilePref.setChecked(true);
            } else {
                profilePref.setChecked(false);
            }
        } else {
            profilePref.setChecked(this.mCachedDevice.isConnectedProfile(profile));
        }
        profilePref.setSummary(profile.getSummaryResourceForDevice(device));
    }

    private LocalBluetoothProfile getProfileOf(Preference pref) {
        if ((pref instanceof TwoStatePreference) && !TextUtils.isEmpty(pref.getKey())) {
            return getProfileOf(pref.getKey());
        }
        return null;
    }

    private int getProfilePreferenceIndex(int profIndex) {
        return this.mProfileContainer.getOrder() + (profIndex * 10);
    }

    private void unpairDevice() {
        boolean isHidDevice = checkHidDevice();
        if (!this.mCachedDevice.isConnected() || isHidDevice) {
            this.mCachedDevice.unpair();
            HwLog.d("DeviceProfilesSettings", "unpairDevice  just unpair");
            return;
        }
        this.mCachedDevice.disconnect();
        delayUnpair();
        HwLog.d("DeviceProfilesSettings", "unpairDevice  first disconnect then unpair");
    }

    boolean checkHidDevice() {
        if (this.mCachedDevice.getBtClass().getMajorDeviceClass() != 1280) {
            return false;
        }
        HwLog.d("DeviceProfilesSettings", "unpair HID device");
        return true;
    }

    private void delayUnpair() {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        this.mHandler.sendMessageDelayed(msg, 1000);
    }

    private LocalBluetoothProfile getProfileOf(String prefKey) {
        try {
            return this.mProfileManager.getProfileByName(prefKey);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected int getMetricsCategory() {
        return 24;
    }
}
