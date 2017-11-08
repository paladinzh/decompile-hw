package com.huawei.android.freeshare.client;

import android.content.Context;
import com.huawei.android.freeshare.client.bluetooth.BluetoothTransmission;
import com.huawei.android.freeshare.client.device.DeviceChangeListener;
import com.huawei.android.freeshare.client.device.DeviceInfo;
import com.huawei.android.freeshare.client.device.DeviceManager;
import com.huawei.android.freeshare.client.transfer.ActionListener;
import com.huawei.android.freeshare.client.transfer.FileTransfer;
import com.huawei.android.freeshare.client.transfer.FileTransferListener;
import com.huawei.android.freeshare.client.transfer.Mission;
import com.huawei.android.freeshare.client.transfer.MissionManger;
import java.util.List;

public abstract class Transmission {
    private static BluetoothTransmission mBluetoothTransmission;
    protected ActionListener mActionListener;
    protected Context mContext;
    protected long mDelayTime = 30000;
    protected DeviceManager mDeviceManager;
    private boolean mIsInit;
    protected MissionManger mMissionManger;
    protected FileTransfer mTransfer;

    protected abstract FileTransfer getFileTransfer();

    public abstract boolean isEnabled();

    public abstract boolean setEnabled(boolean z);

    public static synchronized Transmission getTransmissionInstance(Context context) {
        Transmission transmission;
        synchronized (Transmission.class) {
            if (mBluetoothTransmission == null) {
                mBluetoothTransmission = new BluetoothTransmission(context);
            }
            transmission = mBluetoothTransmission;
        }
        return transmission;
    }

    protected Transmission(Context context) {
        this.mContext = context;
        this.mDeviceManager = new DeviceManager();
        this.mMissionManger = new MissionManger();
        this.mTransfer = getFileTransfer();
    }

    protected synchronized void setInit(boolean init) {
        this.mIsInit = init;
    }

    protected synchronized boolean isInit() {
        return this.mIsInit;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean init() {
        setInit(true);
        return true;
    }

    public boolean destroy() {
        setInit(false);
        return true;
    }

    public boolean discover() {
        return false;
    }

    public boolean cancelDiscover() {
        return false;
    }

    public Mission createSendMission(DeviceInfo target, String uri, String mimeType) {
        return new Mission(this, target, uri, mimeType);
    }

    public boolean startMission(Mission mission) {
        cancelDiscover();
        return this.mMissionManger.offer(mission);
    }

    public void setActionListener(ActionListener l) {
        this.mActionListener = l;
    }

    public void addDeviceListener(DeviceChangeListener l) {
        this.mDeviceManager.addDeviceChangeListener(l);
    }

    public void removeDeviceListener(DeviceChangeListener l) {
        this.mDeviceManager.removeDeviceChangeListener(l);
    }

    public List<DeviceInfo> getDeviceList() {
        return this.mDeviceManager.getDeviceList();
    }

    public void addFileTransferListener(FileTransferListener listener) {
        this.mMissionManger.addMissionListener(listener);
    }

    public boolean cancelRestMission() {
        return this.mMissionManger.cancelRestMission();
    }
}
