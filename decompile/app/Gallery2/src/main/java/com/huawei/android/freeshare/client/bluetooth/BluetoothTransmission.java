package com.huawei.android.freeshare.client.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import com.android.gallery3d.util.GalleryLog;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import com.huawei.android.freeshare.client.Transmission;
import com.huawei.android.freeshare.client.transfer.FileTransfer;
import com.huawei.android.freeshare.client.transfer.FileTransferListener;
import java.lang.ref.WeakReference;

public class BluetoothTransmission extends Transmission implements FileTransferListener {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MyHandler mHandler = new MyHandler(this);
    private BroadcastReceiver mReceiver = new BluetoothReceiver();
    private boolean mReceiverRegisted = false;
    private boolean mShouldCloseBluetooth;

    private class BluetoothReceiver extends BroadcastReceiver {
        private BluetoothReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothTransmission.this.handleBluetoothDeviceUp((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"));
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                GalleryLog.d("Freeshare_BluetoothTrasmit", "onDiscoverFinished");
                BluetoothTransmission.this.callonDiscoverFinished();
            } else if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                GalleryLog.d("Freeshare_BluetoothTrasmit", "onDiscoverStarted");
                BluetoothTransmission.this.callonDiscoverStarted();
            } else if (!"android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action) && "android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Target.SIZE_ORIGINAL);
                GalleryLog.d("Freeshare_BluetoothTrasmit", "Bluetooth state changed ,state = " + state);
                if (12 == state) {
                    BluetoothTransmission.this.callonEnable();
                } else if (10 == state) {
                    BluetoothTransmission.this.mDeviceManager.clearDevices();
                    BluetoothTransmission.this.callonDisable();
                }
            }
        }
    }

    public static class MyHandler extends Handler {
        private WeakReference<BluetoothTransmission> mReference;

        public MyHandler(BluetoothTransmission transmission) {
            this.mReference = new WeakReference(transmission);
        }

        public void handleMessage(Message msg) {
            BluetoothTransmission transmission = (BluetoothTransmission) this.mReference.get();
            if (transmission != null) {
                transmission.handleMessage(msg);
            }
        }
    }

    public BluetoothTransmission(Context context) {
        super(context);
        this.mMissionManger.addMissionListener(this);
        this.mMissionManger.setFileTransfer(this.mTransfer);
    }

    public boolean init() {
        GalleryLog.d("Freeshare_BluetoothTrasmit", " BluetoothTransmission init");
        if (!checBluetoothSharekPermission() || this.mBluetoothAdapter == null) {
            return false;
        }
        registReceiver();
        if (this.mTransfer != null) {
            this.mTransfer.init();
        }
        cancelCloseBluetooth();
        return super.init();
    }

    protected FileTransfer getFileTransfer() {
        return new BluetoothFileTransfer(this.mContext);
    }

    public boolean destroy() {
        GalleryLog.i("Freeshare_BluetoothTrasmit", " BluetoothTransmission destroy");
        if (this.mBluetoothAdapter != null && this.mBluetoothAdapter.isDiscovering()) {
            this.mBluetoothAdapter.cancelDiscovery();
        }
        unRegistReceiver();
        if (this.mMissionManger.isEmpty()) {
            delayToCloseBluetooth();
        }
        return super.destroy();
    }

    public boolean discover() {
        GalleryLog.i("Freeshare_BluetoothTrasmit", "discover ");
        if (this.mBluetoothAdapter == null || !this.mBluetoothAdapter.isEnabled()) {
            return false;
        }
        cancelDiscover();
        this.mDeviceManager.clearDevices();
        return this.mBluetoothAdapter.startDiscovery();
    }

    public boolean cancelDiscover() {
        GalleryLog.i("Freeshare_BluetoothTrasmit", "cancelDiscover ");
        if (this.mBluetoothAdapter == null) {
            return false;
        }
        if (this.mBluetoothAdapter.isDiscovering()) {
            return this.mBluetoothAdapter.cancelDiscovery();
        }
        return true;
    }

    public boolean setEnabled(boolean enabled) {
        GalleryLog.i("Freeshare_BluetoothTrasmit", "setEnabled :" + enabled);
        if (this.mBluetoothAdapter == null) {
            GalleryLog.i("Freeshare_BluetoothTrasmit", "mBluetoothAdapter == null");
            return false;
        }
        boolean res = enabled ? open() : close();
        GalleryLog.i("Freeshare_BluetoothTrasmit", "setEnabled res=" + res);
        return res;
    }

    public boolean open() {
        if (this.mBluetoothAdapter == null) {
            return false;
        }
        int state = this.mBluetoothAdapter.getState();
        if (12 == state || 11 == state) {
            return true;
        }
        if (!this.mBluetoothAdapter.enable()) {
            return false;
        }
        this.mShouldCloseBluetooth = true;
        return true;
    }

    public boolean close() {
        if (this.mBluetoothAdapter == null) {
            return false;
        }
        if (this.mShouldCloseBluetooth) {
            this.mBluetoothAdapter.disable();
        }
        return true;
    }

    public boolean isOpened() {
        if (this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled()) {
            return true;
        }
        return false;
    }

    public boolean isEnabled() {
        GalleryLog.i("Freeshare_BluetoothTrasmit", "is Enalbe");
        return isOpened();
    }

    private boolean checBluetoothSharekPermission() {
        try {
            Bundle bundler = getContext().getContentResolver().call(BluetoothShare.CONTENT_URI, "check_freeshare", null, null);
            if (bundler == null) {
                GalleryLog.i("Freeshare_BluetoothTrasmit", "checBluetoothSharekPermission get bundler is null");
                return false;
            }
            if ("support".equals(bundler.getString("freeshare_support"))) {
                GalleryLog.i("Freeshare_BluetoothTrasmit", "checBluetoothSharekPermission success!");
                return true;
            }
            GalleryLog.i("Freeshare_BluetoothTrasmit", "checBluetoothSharekPermission failed");
            return false;
        } catch (SecurityException e) {
            GalleryLog.i("Freeshare_BluetoothTrasmit", " can not call method from bluetooth database:SecurityException");
            return false;
        } catch (Exception e2) {
            GalleryLog.i("Freeshare_BluetoothTrasmit", " can not call method from bluetooth database");
            return false;
        }
    }

    private void registReceiver() {
        if (!this.mReceiverRegisted) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.bluetooth.device.action.FOUND");
            filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
            filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
            filter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
            filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            getContext().registerReceiver(this.mReceiver, filter);
            this.mReceiverRegisted = true;
        }
    }

    private void unRegistReceiver() {
        if (this.mReceiverRegisted) {
            getContext().unregisterReceiver(this.mReceiver);
            this.mReceiverRegisted = false;
        }
    }

    private boolean filterDevice(BluetoothDevice btDevice) {
        if (btDevice == null) {
            return false;
        }
        ParcelUuid[] uuids = btDevice.getUuids();
        BluetoothClass btClass = btDevice.getBluetoothClass();
        if (uuids == null || !BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
            return btClass != null && btClass.doesClassMatch(2);
        } else {
            return true;
        }
    }

    private void handleBluetoothDeviceUp(BluetoothDevice device) {
        if (device == null) {
            GalleryLog.d("Freeshare_BluetoothTrasmit", "device == null");
        } else if (filterDevice(device)) {
            String name = device.getName();
            String mac = device.getAddress();
            if (name == null) {
                GalleryLog.d("Freeshare_BluetoothTrasmit", "handleBluetoothDeviceUp,device name == null");
                return;
            }
            if (mac == null) {
                GalleryLog.d("Freeshare_BluetoothTrasmit", "handleBluetoothDeviceUp,device mac == null");
            }
            GalleryLog.d("Freeshare_BluetoothTrasmit", "device up,name = " + name + ",mac=" + mac);
            this.mDeviceManager.addDevice(new BluetoothDeviceInfo(name, mac));
        } else {
            GalleryLog.d("Freeshare_BluetoothTrasmit", "device:" + device.getName() + " is not transfer device");
        }
    }

    private void callonDiscoverFinished() {
        if (this.mActionListener != null) {
            this.mActionListener.onDiscoverFinished();
        }
    }

    private void callonDiscoverStarted() {
        if (this.mActionListener != null) {
            this.mActionListener.onDiscoverStarted();
        }
    }

    private void callonEnable() {
        if (this.mActionListener != null) {
            this.mActionListener.onEnabled();
        }
    }

    private void callonDisable() {
        this.mDeviceManager.clearDevices();
        this.mShouldCloseBluetooth = false;
        cancelCloseBluetooth();
        if (this.mActionListener != null) {
            this.mActionListener.onDisabled();
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                this.mHandler.removeMessages(2);
                if (this.mBluetoothAdapter != null && this.mShouldCloseBluetooth && !isInit()) {
                    this.mShouldCloseBluetooth = false;
                    this.mBluetoothAdapter.disable();
                    this.mTransfer.destroy();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onProgressUpdate(String uri, int progress) {
    }

    public void onTransferFinish(String uri, boolean success) {
        if (this.mMissionManger.isEmpty() && this.mShouldCloseBluetooth) {
            delayToCloseBluetooth();
        }
    }

    private void cancelCloseBluetooth() {
        this.mHandler.removeMessages(2);
    }

    private void delayToCloseBluetooth() {
        this.mHandler.sendEmptyMessageDelayed(2, this.mDelayTime);
    }
}
