package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.settingslib.R$bool;
import com.android.settingslib.R$string;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class LocalBluetoothProfileManager {
    private A2dpProfile mA2dpProfile;
    private A2dpSinkProfile mA2dpSinkProfile;
    private final Context mContext;
    private HwCustLocalBluetoothProfileManager mCustLocalBluetoothProfileManager;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private final BluetoothEventManager mEventManager;
    private HeadsetProfile mHeadsetProfile;
    private HfpClientProfile mHfpClientProfile;
    private final HidProfile mHidProfile;
    private final LocalBluetoothAdapter mLocalAdapter;
    private MapProfile mMapProfile;
    private OppProfile mOppProfile;
    private final PanProfile mPanProfile;
    private PbapClientProfile mPbapClientProfile;
    private final PbapServerProfile mPbapProfile;
    private final Map<String, LocalBluetoothProfile> mProfileNameMap = new HashMap();
    private final Collection<ServiceListener> mServiceListeners = new ArrayList();
    private final boolean mUsePbapPce;

    public interface ServiceListener {
        void onServiceConnected();

        void onServiceDisconnected();
    }

    private class StateChangedHandler implements Handler {
        final LocalBluetoothProfile mProfile;

        StateChangedHandler(LocalBluetoothProfile profile) {
            this.mProfile = profile;
        }

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            if (device != null && LocalBluetoothProfileManager.this.mDeviceManager != null) {
                CachedBluetoothDevice cachedDevice = LocalBluetoothProfileManager.this.mDeviceManager.findDevice(device);
                if (cachedDevice == null) {
                    HwLog.w("LocalBluetoothProfileManager", "StateChangedHandler found new device: " + device);
                    cachedDevice = LocalBluetoothProfileManager.this.mDeviceManager.addDevice(LocalBluetoothProfileManager.this.mLocalAdapter, LocalBluetoothProfileManager.this, device);
                }
                if (LocalBluetoothProfileManager.this.mCustLocalBluetoothProfileManager != null) {
                    LocalBluetoothProfileManager.this.mCustLocalBluetoothProfileManager.handleCustIntent(intent, device);
                }
                int newState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", 0);
                int oldState = intent.getIntExtra("android.bluetooth.profile.extra.PREVIOUS_STATE", 0);
                HwLog.d("LocalBluetoothProfileManager", "StateChangedHandler  " + this.mProfile + " state change " + oldState + " -> " + newState);
                cachedDevice.onProfileStateChanged(this.mProfile, newState);
                cachedDevice.refresh();
                if (newState == 0 && oldState == 1) {
                    HwLog.i("LocalBluetoothProfileManager", "OoO Failed to connect " + this.mProfile + " device");
                    if (cachedDevice.isHumanConnect()) {
                        LocalBluetoothProfileManager.this.bluetoothSelfCheck(context, cachedDevice);
                    }
                }
                if (newState == 2 && oldState == 1) {
                    HwLog.i("LocalBluetoothProfileManager", "OoO Success to connect " + this.mProfile + " device");
                    if (cachedDevice.isHumanConnect()) {
                        cachedDevice.setHumanConnect(false);
                    }
                }
            }
        }
    }

    private class PanStateChangedHandler extends StateChangedHandler {
        PanStateChangedHandler(LocalBluetoothProfile profile) {
            super(profile);
        }

        public void onReceive(Context context, Intent intent, BluetoothDevice device) {
            this.mProfile.setLocalRole(device, intent.getIntExtra("android.bluetooth.pan.extra.LOCAL_ROLE", 0));
            super.onReceive(context, intent, device);
        }
    }

    LocalBluetoothProfileManager(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, BluetoothEventManager eventManager) {
        this.mContext = context;
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mEventManager = eventManager;
        this.mUsePbapPce = this.mContext.getResources().getBoolean(R$bool.enable_pbap_pce_profile);
        this.mLocalAdapter.setProfileManager(this);
        this.mEventManager.setProfileManager(this);
        ParcelUuid[] uuids = adapter.getUuids();
        if (uuids != null) {
            updateLocalProfiles(uuids);
        }
        this.mHidProfile = new HidProfile(context, this.mLocalAdapter, this.mDeviceManager, this);
        addProfile(this.mHidProfile, "HID", "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
        this.mPanProfile = new PanProfile(context);
        addPanProfile(this.mPanProfile, "PAN", "android.bluetooth.pan.profile.action.CONNECTION_STATE_CHANGED");
        HwLog.d("LocalBluetoothProfileManager", "Adding local MAP profile");
        this.mMapProfile = new MapProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
        addProfile(this.mMapProfile, "MAP", "android.bluetooth.map.profile.action.CONNECTION_STATE_CHANGED");
        this.mPbapProfile = new PbapServerProfile(context);
        addProfile(this.mPbapProfile, "PBAP Server", "android.bluetooth.pbap.intent.action.PBAP_STATE_CHANGED");
        HwLog.d("LocalBluetoothProfileManager", "LocalBluetoothProfileManager construction complete");
    }

    void updateLocalProfiles(ParcelUuid[] uuids) {
        HwLog.d("LocalBluetoothProfileManager", "updateLocalProfiles");
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.AudioSource)) {
            if (this.mA2dpProfile == null) {
                HwLog.d("LocalBluetoothProfileManager", "Adding local A2DP SRC profile");
                this.mA2dpProfile = new A2dpProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mA2dpProfile, "A2DP", "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mA2dpProfile != null) {
            HwLog.w("LocalBluetoothProfileManager", "Warning: A2DP profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.AudioSink)) {
            if (this.mA2dpSinkProfile == null) {
                HwLog.d("LocalBluetoothProfileManager", "Adding local A2DP Sink profile");
                this.mA2dpSinkProfile = new A2dpSinkProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mA2dpSinkProfile, "A2DPSink", "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mA2dpSinkProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: A2DP Sink profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Handsfree_AG) || BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.HSP_AG)) {
            if (this.mHeadsetProfile == null) {
                HwLog.d("LocalBluetoothProfileManager", "Adding local HEADSET profile");
                this.mHeadsetProfile = new HeadsetProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mHeadsetProfile, "HEADSET", "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mHeadsetProfile != null) {
            HwLog.w("LocalBluetoothProfileManager", "Warning: HEADSET profile was previously added but the UUID is now missing.");
        }
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Handsfree)) {
            if (this.mHfpClientProfile == null) {
                HwLog.d("LocalBluetoothProfileManager", "Adding local HfpClient profile");
                this.mHfpClientProfile = new HfpClientProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mHfpClientProfile, "HEADSET_CLIENT", "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mHfpClientProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: Hfp Client profile was previously added but the UUID is now missing.");
        } else {
            Log.d("LocalBluetoothProfileManager", "Handsfree Uuid not found.");
        }
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
            if (this.mOppProfile == null) {
                HwLog.d("LocalBluetoothProfileManager", "Adding local OPP profile");
                this.mOppProfile = new OppProfile();
                this.mProfileNameMap.put("OPP", this.mOppProfile);
            }
        } else if (this.mOppProfile != null) {
            HwLog.w("LocalBluetoothProfileManager", "Warning: OPP profile was previously added but the UUID is now missing.");
        }
        if (this.mUsePbapPce) {
            if (this.mPbapClientProfile == null) {
                Log.d("LocalBluetoothProfileManager", "Adding local PBAP Client profile");
                this.mPbapClientProfile = new PbapClientProfile(this.mContext, this.mLocalAdapter, this.mDeviceManager, this);
                addProfile(this.mPbapClientProfile, "PbapClient", "android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED");
            }
        } else if (this.mPbapClientProfile != null) {
            Log.w("LocalBluetoothProfileManager", "Warning: PBAP Client profile was previously added but the UUID is now missing.");
        }
        this.mEventManager.registerProfileIntentReceiver();
    }

    private void addProfile(LocalBluetoothProfile profile, String profileName, String stateChangedAction) {
        this.mEventManager.addProfileHandler(stateChangedAction, new StateChangedHandler(profile));
        this.mProfileNameMap.put(profileName, profile);
    }

    private void addPanProfile(LocalBluetoothProfile profile, String profileName, String stateChangedAction) {
        this.mEventManager.addProfileHandler(stateChangedAction, new PanStateChangedHandler(profile));
        this.mProfileNameMap.put(profileName, profile);
    }

    public LocalBluetoothProfile getProfileByName(String name) {
        return (LocalBluetoothProfile) this.mProfileNameMap.get(name);
    }

    void setBluetoothStateOn() {
        ParcelUuid[] uuids = this.mLocalAdapter.getUuids();
        if (uuids != null) {
            updateLocalProfiles(uuids);
        }
        this.mEventManager.readPairedDevices();
    }

    private int bluetoothSelfCheck(Context context, CachedBluetoothDevice cachedDevice) {
        boolean isAllProfileDisconnected = true;
        for (LocalBluetoothProfile profile : cachedDevice.getProfiles()) {
            switch (cachedDevice.getProfileConnectionState(profile)) {
                case 0:
                    break;
                case 1:
                case 2:
                case 3:
                    isAllProfileDisconnected = false;
                    break;
                default:
                    break;
            }
        }
        if (isAllProfileDisconnected && cachedDevice.getBondState() == 12) {
            HwLog.i("LocalBluetoothProfileManager", "OoO received connecting->disConnected msg && isHumanConnect && allProfileDisConnected");
            HwLog.i("LocalBluetoothProfileManager", "OoO show bluetooth self-check connect fail dialog");
            Utils.showError(context, cachedDevice.getName(), R$string.bluetooth_connect_fail_message, R$string.know_more);
            cachedDevice.setHumanConnect(false);
        }
        return 0;
    }

    public void addServiceListener(ServiceListener l) {
        this.mServiceListeners.add(l);
    }

    public void removeServiceListener(ServiceListener l) {
        this.mServiceListeners.remove(l);
    }

    void callServiceConnectedListeners() {
        for (ServiceListener l : this.mServiceListeners) {
            l.onServiceConnected();
        }
    }

    void callServiceDisconnectedListeners() {
        for (ServiceListener listener : this.mServiceListeners) {
            listener.onServiceDisconnected();
        }
    }

    public synchronized boolean isManagerReady() {
        LocalBluetoothProfile profile = this.mHeadsetProfile;
        if (profile != null) {
            return profile.isProfileReady();
        }
        profile = this.mA2dpProfile;
        if (profile != null) {
            return profile.isProfileReady();
        }
        profile = this.mA2dpSinkProfile;
        if (profile == null) {
            return false;
        }
        return profile.isProfileReady();
    }

    public A2dpProfile getA2dpProfile() {
        return this.mA2dpProfile;
    }

    public A2dpSinkProfile getA2dpSinkProfile() {
        if (this.mA2dpSinkProfile == null || !this.mA2dpSinkProfile.isProfileReady()) {
            return null;
        }
        return this.mA2dpSinkProfile;
    }

    public HeadsetProfile getHeadsetProfile() {
        return this.mHeadsetProfile;
    }

    public PbapClientProfile getPbapClientProfile() {
        return this.mPbapClientProfile;
    }

    public PbapServerProfile getPbapProfile() {
        return this.mPbapProfile;
    }

    public MapProfile getMapProfile() {
        return this.mMapProfile;
    }

    synchronized void updateProfiles(ParcelUuid[] uuids, ParcelUuid[] localUuids, Collection<LocalBluetoothProfile> profiles, Collection<LocalBluetoothProfile> removedProfiles, boolean isPanNapConnected, BluetoothDevice device) {
        removedProfiles.clear();
        removedProfiles.addAll(profiles);
        Log.d("LocalBluetoothProfileManager", "Current Profiles" + profiles.toString());
        profiles.clear();
        if (uuids != null) {
            if (this.mHeadsetProfile != null && ((BluetoothUuid.isUuidPresent(localUuids, BluetoothUuid.HSP_AG) && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.HSP)) || (BluetoothUuid.isUuidPresent(localUuids, BluetoothUuid.Handsfree_AG) && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Handsfree)))) {
                profiles.add(this.mHeadsetProfile);
                removedProfiles.remove(this.mHeadsetProfile);
            }
            if (this.mHfpClientProfile != null && BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Handsfree_AG) && BluetoothUuid.isUuidPresent(localUuids, BluetoothUuid.Handsfree)) {
                profiles.add(this.mHfpClientProfile);
                removedProfiles.remove(this.mHfpClientProfile);
            }
            if (BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SINK_UUIDS) && this.mA2dpProfile != null) {
                profiles.add(this.mA2dpProfile);
                removedProfiles.remove(this.mA2dpProfile);
            }
            if (BluetoothUuid.containsAnyUuid(uuids, A2dpSinkProfile.SRC_UUIDS) && this.mA2dpSinkProfile != null) {
                profiles.add(this.mA2dpSinkProfile);
                removedProfiles.remove(this.mA2dpSinkProfile);
            }
            if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush) && this.mOppProfile != null) {
                profiles.add(this.mOppProfile);
                removedProfiles.remove(this.mOppProfile);
            }
            if ((BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hid) || BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hogp)) && this.mHidProfile != null) {
                profiles.add(this.mHidProfile);
                removedProfiles.remove(this.mHidProfile);
            }
            if (isPanNapConnected) {
                HwLog.d("LocalBluetoothProfileManager", "Valid PAN-NAP connection exists.");
            }
            if (!BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.NAP) || this.mPanProfile == null) {
                if (isPanNapConnected) {
                }
                if (this.mMapProfile != null && this.mMapProfile.getConnectionStatus(device) == 2) {
                    profiles.add(this.mMapProfile);
                    removedProfiles.remove(this.mMapProfile);
                    this.mMapProfile.setPreferred(device, true);
                }
                if (this.mUsePbapPce) {
                    profiles.add(this.mPbapClientProfile);
                    removedProfiles.remove(this.mPbapClientProfile);
                    profiles.remove(this.mPbapProfile);
                    removedProfiles.add(this.mPbapProfile);
                }
                Log.d("LocalBluetoothProfileManager", "New Profiles" + profiles.toString());
            }
            profiles.add(this.mPanProfile);
            removedProfiles.remove(this.mPanProfile);
            profiles.add(this.mMapProfile);
            removedProfiles.remove(this.mMapProfile);
            this.mMapProfile.setPreferred(device, true);
            if (this.mUsePbapPce) {
                profiles.add(this.mPbapClientProfile);
                removedProfiles.remove(this.mPbapClientProfile);
                profiles.remove(this.mPbapProfile);
                removedProfiles.add(this.mPbapProfile);
            }
            Log.d("LocalBluetoothProfileManager", "New Profiles" + profiles.toString());
        }
    }
}
