package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;
import com.android.settingslib.R$string;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class CachedBluetoothDevice implements Comparable<CachedBluetoothDevice> {
    private boolean isHumanConnect = false;
    private BluetoothClass mBtClass;
    private final Collection<Callback> mCallbacks = new ArrayList();
    private boolean mConnectAfterPairing;
    private long mConnectAttempted;
    private final Context mContext;
    private final BluetoothDevice mDevice;
    private boolean mIsConnectingErrorPossible;
    private final LocalBluetoothAdapter mLocalAdapter;
    private boolean mLocalNapRoleConnected;
    private HashMap<Integer, byte[]> mManufacturerSpecificData = new HashMap(0);
    private int mMessageRejectionCount;
    private String mName;
    private HashMap<LocalBluetoothProfile, Integer> mProfileConnectionState;
    private final LocalBluetoothProfileManager mProfileManager;
    private final List<LocalBluetoothProfile> mProfiles = new ArrayList();
    private final List<LocalBluetoothProfile> mRemovedProfiles = new ArrayList();
    private short mRssi;
    private boolean mVisible;

    public interface Callback {
        void onDeviceAttributesChanged();
    }

    private String describe(LocalBluetoothProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:").append(this.mDevice);
        if (profile != null) {
            sb.append(" Profile:").append(profile);
        }
        return sb.toString();
    }

    void onProfileStateChanged(LocalBluetoothProfile profile, int newProfileState) {
        HwLog.d("CachedBluetoothDevice", "onProfileStateChanged() - profile " + profile + " newProfileState is " + newProfileState);
        if (this.mLocalAdapter.getBluetoothState() == 13) {
            HwLog.d("CachedBluetoothDevice", " BT Turninig Off...Profile conn state change ignored...");
            return;
        }
        this.mProfileConnectionState.put(profile, Integer.valueOf(newProfileState));
        if (newProfileState == 2) {
            if (profile instanceof MapProfile) {
                profile.setPreferred(this.mDevice, true);
            } else if (!this.mProfiles.contains(profile)) {
                this.mRemovedProfiles.remove(profile);
                this.mProfiles.add(profile);
                if ((profile instanceof PanProfile) && ((PanProfile) profile).isLocalRoleNap(this.mDevice)) {
                    this.mLocalNapRoleConnected = true;
                }
            }
        } else if ((profile instanceof MapProfile) && newProfileState == 0) {
            profile.setPreferred(this.mDevice, false);
        } else if (this.mLocalNapRoleConnected && (profile instanceof PanProfile) && ((PanProfile) profile).isLocalRoleNap(this.mDevice) && newProfileState == 0) {
            HwLog.d("CachedBluetoothDevice", "Removing PanProfile from device after NAP disconnect");
            this.mProfiles.remove(profile);
            this.mRemovedProfiles.add(profile);
            this.mLocalNapRoleConnected = false;
        } else if ((profile instanceof HidProfile) && newProfileState == 4) {
            Utils.showError(this.mContext, this.mName, R$string.mouse_connection_hint_Toast);
        } else if ((profile instanceof HidProfile) && newProfileState == 5) {
            Utils.showError(this.mContext, this.mName, R$string.kbd_connection_hint_Toast);
        } else if ((profile instanceof HidProfile) && newProfileState == 6) {
            Utils.showError(this.mContext, this.mName, R$string.hid_connection_error_too_many_devices_Toast);
        }
    }

    CachedBluetoothDevice(Context context, LocalBluetoothAdapter adapter, LocalBluetoothProfileManager profileManager, BluetoothDevice device) {
        this.mContext = context;
        this.mLocalAdapter = adapter;
        this.mProfileManager = profileManager;
        this.mDevice = device;
        this.mProfileConnectionState = new HashMap();
        fillData();
    }

    public void disconnect() {
        for (LocalBluetoothProfile profile : this.mProfiles) {
            disconnect(profile);
        }
        PbapServerProfile PbapProfile = this.mProfileManager.getPbapProfile();
        if (PbapProfile.getConnectionStatus(this.mDevice) == 2) {
            PbapProfile.disconnect(this.mDevice);
        }
    }

    public void disconnect(LocalBluetoothProfile profile) {
        if (profile.disconnect(this.mDevice)) {
            HwLog.d("CachedBluetoothDevice", "Command sent successfully:DISCONNECT " + describe(profile));
        }
    }

    public void connect(boolean connectAllProfiles) {
        if (ensurePaired()) {
            this.mConnectAttempted = SystemClock.elapsedRealtime();
            HwLog.d("CachedBluetoothDevice", "connect() - mConnectAttempted=" + this.mConnectAttempted + ", connectAllProfiles=" + connectAllProfiles);
            connectWithoutResettingTimer(connectAllProfiles);
        }
    }

    void onBondingDockConnect() {
        connect(false);
    }

    private void connectWithoutResettingTimer(boolean connectAllProfiles) {
        if (this.mProfiles.isEmpty()) {
            HwLog.d("CachedBluetoothDevice", "No profiles. Maybe we will connect later");
            return;
        }
        this.mIsConnectingErrorPossible = true;
        int preferredProfiles = 0;
        for (LocalBluetoothProfile profile : this.mProfiles) {
            boolean isConnectable;
            if (connectAllProfiles) {
                isConnectable = profile.isConnectable();
            } else {
                isConnectable = profile.isAutoConnectable();
            }
            if (isConnectable && profile.isPreferred(this.mDevice)) {
                preferredProfiles++;
                connectInt(profile);
            }
        }
        HwLog.d("CachedBluetoothDevice", "Preferred profiles = " + preferredProfiles);
        if (preferredProfiles == 0) {
            connectAutoConnectableProfiles();
        }
    }

    private void connectAutoConnectableProfiles() {
        HwLog.d("CachedBluetoothDevice", "connectAutoConnectableProfiles");
        if (ensurePaired()) {
            this.mIsConnectingErrorPossible = true;
            for (LocalBluetoothProfile profile : this.mProfiles) {
                if (profile.isAutoConnectable()) {
                    profile.setPreferred(this.mDevice, true);
                    connectInt(profile);
                }
            }
        }
    }

    public void connectProfile(LocalBluetoothProfile profile) {
        this.mConnectAttempted = SystemClock.elapsedRealtime();
        this.mIsConnectingErrorPossible = true;
        connectInt(profile);
        refresh();
    }

    synchronized void connectInt(LocalBluetoothProfile profile) {
        if (!ensurePaired()) {
            return;
        }
        if (profile.connect(this.mDevice)) {
            HwLog.d("CachedBluetoothDevice", "Command sent successfully:CONNECT " + describe(profile));
        } else {
            HwLog.i("CachedBluetoothDevice", "Failed to connect " + profile.toString() + " to " + this.mName);
        }
    }

    private boolean ensurePaired() {
        if (getBondState() != 10) {
            return true;
        }
        startPairing();
        return false;
    }

    public boolean startPairing() {
        if (this.mLocalAdapter.isDiscovering()) {
            this.mLocalAdapter.cancelDiscovery();
        }
        if (!this.mDevice.createBond()) {
            return false;
        }
        this.mConnectAfterPairing = true;
        return true;
    }

    public void unpair() {
        int state = getBondState();
        if (state == 11) {
            this.mDevice.cancelBondProcess();
        }
        if (state != 10) {
            BluetoothDevice dev = this.mDevice;
            if (dev == null) {
                return;
            }
            if (dev.removeBond()) {
                HwLog.d("CachedBluetoothDevice", "Command sent successfully:REMOVE_BOND " + describe(null));
            } else {
                HwLog.d("CachedBluetoothDevice", "Framework rejected command immediately:REMOVE_BOND " + describe(null));
            }
        }
    }

    public int getProfileConnectionState(LocalBluetoothProfile profile) {
        if (this.mProfileConnectionState.get(profile) == null) {
            this.mProfileConnectionState.put(profile, Integer.valueOf(profile.getConnectionStatus(this.mDevice)));
        }
        return ((Integer) this.mProfileConnectionState.get(profile)).intValue();
    }

    public void clearProfileConnectionState() {
        HwLog.d("CachedBluetoothDevice", " Clearing all connection state for dev:" + this.mDevice.getName());
        for (LocalBluetoothProfile profile : getProfiles()) {
            this.mProfileConnectionState.put(profile, Integer.valueOf(0));
        }
    }

    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        migratePhonebookPermissionChoice();
        migrateMessagePermissionChoice();
        fetchMessageRejectionCount();
        this.mVisible = false;
        dispatchAttributesChanged();
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public String getName() {
        return this.mName;
    }

    void setNewName(String name) {
        if (this.mName == null) {
            this.mName = name;
            if (this.mName == null || TextUtils.isEmpty(this.mName)) {
                this.mName = this.mDevice.getAddress();
            }
            dispatchAttributesChanged();
        }
    }

    public void setName(String name) {
        if (name != null && !name.equals(this.mName)) {
            this.mName = name;
            this.mDevice.setAlias(name);
            dispatchAttributesChanged();
        }
    }

    void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }

    private void fetchName() {
        this.mName = this.mDevice.getAliasName();
        if (TextUtils.isEmpty(this.mName)) {
            this.mName = this.mDevice.getAddress();
            HwLog.d("CachedBluetoothDevice", "Device has no name (yet), use address: " + this.mName);
        }
    }

    void refresh() {
        dispatchAttributesChanged();
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public void setVisible(boolean visible) {
        if (this.mVisible != visible) {
            this.mVisible = visible;
            dispatchAttributesChanged();
        }
    }

    public int getBondState() {
        return this.mDevice.getBondState();
    }

    void setRssi(short rssi) {
        if (this.mRssi != rssi) {
            this.mRssi = rssi;
            dispatchAttributesChanged();
        }
    }

    public short getRssi() {
        return this.mRssi;
    }

    void setManufacturerSpecificData(HashMap<Integer, byte[]> manufacturerSpecificData) {
        if (manufacturerSpecificData != null && !manufacturerSpecificData.equals(this.mManufacturerSpecificData)) {
            this.mManufacturerSpecificData = manufacturerSpecificData;
            dispatchAttributesChanged();
        }
    }

    public HashMap<Integer, byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public boolean isConnected() {
        for (LocalBluetoothProfile profile : this.mProfiles) {
            if (getProfileConnectionState(profile) == 2) {
                return true;
            }
        }
        return false;
    }

    public boolean isConnectedProfile(LocalBluetoothProfile profile) {
        return getProfileConnectionState(profile) == 2;
    }

    public boolean isBusy() {
        boolean z = true;
        for (LocalBluetoothProfile profile : this.mProfiles) {
            int status = getProfileConnectionState(profile);
            if (status != 1) {
                if (status == 3) {
                }
            }
            return true;
        }
        if (getBondState() != 11) {
            z = false;
        }
        return z;
    }

    private void fetchBtClass() {
        this.mBtClass = this.mDevice.getBluetoothClass();
    }

    private boolean updateProfiles() {
        ParcelUuid[] uuids = this.mDevice.getUuids();
        if (uuids == null) {
            return false;
        }
        ParcelUuid[] localUuids = this.mLocalAdapter.getUuids();
        if (localUuids == null) {
            return false;
        }
        processPhonebookAccess();
        this.mProfileManager.updateProfiles(uuids, localUuids, this.mProfiles, this.mRemovedProfiles, this.mLocalNapRoleConnected, this.mDevice);
        HwLog.e("CachedBluetoothDevice", "updateProfiles  for " + this.mDevice.getAliasName());
        BluetoothClass bluetoothClass = this.mDevice.getBluetoothClass();
        if (bluetoothClass != null) {
            HwLog.v("CachedBluetoothDevice", "updateProfiles  bluetoothClass: " + bluetoothClass.toString());
        }
        return true;
    }

    void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }

    void onUuidChanged() {
        updateProfiles();
        boolean containHogp = false;
        long timeout = 5000;
        if (BluetoothUuid.isUuidPresent(this.mDevice.getUuids(), BluetoothUuid.Hogp)) {
            HwLog.d("CachedBluetoothDevice", "onUuidChanged() device contain Hogp profile");
            containHogp = true;
            timeout = 30000;
        }
        HwLog.d("CachedBluetoothDevice", "onUuidChanged: Time since last connect is " + (SystemClock.elapsedRealtime() - this.mConnectAttempted));
        boolean isEmptyProfiles = this.mProfiles.isEmpty();
        boolean isTimeout = this.mConnectAttempted + timeout > SystemClock.elapsedRealtime();
        boolean isConnectAttempted = containHogp && this.mConnectAttempted == 0;
        HwLog.d("CachedBluetoothDevice", "isEmptyProfiles=" + isEmptyProfiles + ", mConnectAttempted=" + this.mConnectAttempted + ", isTimeout=" + isTimeout + ", isConnectAttempted=" + isConnectAttempted);
        if (!isEmptyProfiles && (isTimeout || isConnectAttempted)) {
            HwLog.d("CachedBluetoothDevice", "begin connectWithoutResettingTimer");
            connectWithoutResettingTimer(false);
        }
        dispatchAttributesChanged();
    }

    void onBondingStateChanged(int bondState) {
        if (bondState == 10) {
            this.mProfiles.clear();
            this.mConnectAfterPairing = false;
            setPhonebookPermissionChoice(0);
            setMessagePermissionChoice(0);
            setSimPermissionChoice(0);
            this.mMessageRejectionCount = 0;
            saveMessageRejectionCount();
        }
        refresh();
        if (bondState == 12) {
            if (this.mProfileManager != null) {
                ParcelUuid[] uuids = this.mLocalAdapter.getUuids();
                if (uuids != null) {
                    HwLog.d("CachedBluetoothDevice", "update LocalBluetoothProfileManager");
                    this.mProfileManager.updateLocalProfiles(uuids);
                }
            }
            if (this.mDevice.isBluetoothDock()) {
                onBondingDockConnect();
            } else if (this.mConnectAfterPairing) {
                connect(false);
            }
            this.mConnectAfterPairing = false;
        }
    }

    void setBtClass(BluetoothClass btClass) {
        if (btClass != null && this.mBtClass != btClass) {
            this.mBtClass = btClass;
            dispatchAttributesChanged();
        }
    }

    public BluetoothClass getBtClass() {
        return this.mBtClass;
    }

    public List<LocalBluetoothProfile> getProfiles() {
        return Collections.unmodifiableList(this.mProfiles);
    }

    public List<LocalBluetoothProfile> getConnectableProfiles() {
        List<LocalBluetoothProfile> connectableProfiles = new ArrayList();
        for (LocalBluetoothProfile profile : this.mProfiles) {
            if (profile.isConnectable()) {
                connectableProfiles.add(profile);
            }
        }
        return connectableProfiles;
    }

    public List<LocalBluetoothProfile> getRemovedProfiles() {
        return this.mRemovedProfiles;
    }

    public void registerCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    private void dispatchAttributesChanged() {
        synchronized (this.mCallbacks) {
            for (Callback callback : this.mCallbacks) {
                callback.onDeviceAttributesChanged();
            }
        }
    }

    public String toString() {
        return this.mDevice.toString();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof CachedBluetoothDevice)) {
            return false;
        }
        return this.mDevice.equals(((CachedBluetoothDevice) o).mDevice);
    }

    public int hashCode() {
        return this.mDevice.getAddress().hashCode();
    }

    public int compareTo(CachedBluetoothDevice another) {
        int i;
        int i2 = 1;
        if (another.isConnected()) {
            i = 1;
        } else {
            i = 0;
        }
        int comparison = i - (isConnected() ? 1 : 0);
        if (comparison != 0) {
            return comparison;
        }
        int i3;
        if (another.getBondState() == 12) {
            i = 1;
        } else {
            i = 0;
        }
        if (getBondState() == 12) {
            i3 = 1;
        } else {
            i3 = 0;
        }
        comparison = i - i3;
        if (comparison != 0) {
            return comparison;
        }
        if (another.mVisible) {
            i = 1;
        } else {
            i = 0;
        }
        if (!this.mVisible) {
            i2 = 0;
        }
        comparison = i - i2;
        if (comparison != 0) {
            return comparison;
        }
        comparison = another.mRssi - this.mRssi;
        if (comparison != 0) {
            return comparison;
        }
        return this.mName.compareTo(another.mName);
    }

    public int getPhonebookPermissionChoice() {
        int permission = this.mDevice.getPhonebookAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    public void setPhonebookPermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setPhonebookAccessPermission(permission);
    }

    private void migratePhonebookPermissionChoice() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("bluetooth_phonebook_permission", 0);
        if (preferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getPhonebookAccessPermission() == 0) {
                int oldPermission = preferences.getInt(this.mDevice.getAddress(), 0);
                if (oldPermission == 1) {
                    this.mDevice.setPhonebookAccessPermission(1);
                } else if (oldPermission == 2) {
                    this.mDevice.setPhonebookAccessPermission(2);
                }
            }
            Editor editor = preferences.edit();
            editor.remove(this.mDevice.getAddress());
            editor.commit();
        }
    }

    public int getMessagePermissionChoice() {
        int permission = this.mDevice.getMessageAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    public void setMessagePermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setMessageAccessPermission(permission);
    }

    public int getSimPermissionChoice() {
        int permission = this.mDevice.getSimAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    void setSimPermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setSimAccessPermission(permission);
    }

    private void migrateMessagePermissionChoice() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("bluetooth_message_permission", 0);
        if (preferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getMessageAccessPermission() == 0) {
                int oldPermission = preferences.getInt(this.mDevice.getAddress(), 0);
                if (oldPermission == 1) {
                    this.mDevice.setMessageAccessPermission(1);
                } else if (oldPermission == 2) {
                    this.mDevice.setMessageAccessPermission(2);
                }
            }
            Editor editor = preferences.edit();
            editor.remove(this.mDevice.getAddress());
            editor.commit();
        }
    }

    public boolean checkAndIncreaseMessageRejectionCount() {
        if (this.mMessageRejectionCount < 2) {
            this.mMessageRejectionCount++;
            saveMessageRejectionCount();
        }
        return this.mMessageRejectionCount >= 2;
    }

    private void fetchMessageRejectionCount() {
        this.mMessageRejectionCount = this.mContext.getSharedPreferences("bluetooth_message_reject", 0).getInt(this.mDevice.getAddress(), 0);
    }

    private void saveMessageRejectionCount() {
        Editor editor = this.mContext.getSharedPreferences("bluetooth_message_reject", 0).edit();
        if (this.mMessageRejectionCount == 0) {
            editor.remove(this.mDevice.getAddress());
        } else {
            editor.putInt(this.mDevice.getAddress(), this.mMessageRejectionCount);
        }
        editor.commit();
    }

    public boolean isHumanConnect() {
        return this.isHumanConnect;
    }

    public void setHumanConnect(boolean arg) {
        this.isHumanConnect = arg;
    }

    private void processPhonebookAccess() {
        if (this.mDevice.getBondState() == 12 && BluetoothUuid.containsAnyUuid(this.mDevice.getUuids(), PbapServerProfile.PBAB_CLIENT_UUIDS) && getPhonebookPermissionChoice() == 0) {
            BluetoothClass bluetoothClass = this.mDevice.getBluetoothClass();
            if (bluetoothClass == null) {
                HwLog.e("CachedBluetoothDevice", "processPhonebookAccess bluetoothClass=null");
                return;
            }
            int btClass = bluetoothClass.getDeviceClass();
            HwLog.d("CachedBluetoothDevice", "processPhonebookAccess  btClass=" + btClass);
            if (btClass == 1032) {
                setPhonebookPermissionChoice(1);
            } else {
                setPhonebookPermissionChoice(2);
            }
        }
    }

    public int getConnectionSummary() {
        boolean profileConnected = false;
        boolean a2dpNotConnected = false;
        boolean hfpNotConnected = false;
        for (LocalBluetoothProfile profile : getProfiles()) {
            int connectionStatus = getProfileConnectionState(profile);
            switch (connectionStatus) {
                case 0:
                    if (profile.isProfileReady()) {
                        if (!(profile instanceof A2dpProfile) && !(profile instanceof A2dpSinkProfile)) {
                            if (!(profile instanceof HeadsetProfile) && !(profile instanceof HfpClientProfile)) {
                                break;
                            }
                            hfpNotConnected = true;
                            break;
                        }
                        a2dpNotConnected = true;
                        break;
                    }
                    break;
                    break;
                case 1:
                case 3:
                    return Utils.getConnectionStateSummary(connectionStatus);
                case 2:
                    profileConnected = true;
                    break;
                default:
                    break;
            }
        }
        if (!profileConnected) {
            return getBondState() == 11 ? R$string.bluetooth_pairing : 0;
        } else if (a2dpNotConnected && hfpNotConnected) {
            return R$string.bluetooth_connected_no_headset_no_a2dp;
        } else {
            if (a2dpNotConnected) {
                return R$string.bluetooth_connected_no_a2dp;
            }
            if (hfpNotConnected) {
                return R$string.bluetooth_connected_no_headset;
            }
            return R$string.bluetooth_connected;
        }
    }
}
