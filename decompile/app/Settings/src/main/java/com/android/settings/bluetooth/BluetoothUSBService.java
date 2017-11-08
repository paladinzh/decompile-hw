package com.android.settings.bluetooth;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import java.util.Set;

public class BluetoothUSBService extends Service {
    private final int REQ_TYPE_GET_MAC_AND_STATE = 161;
    private final int REQ_TYPE_GO_TO_MATCH_STATE = 33;
    private BluetoothUSBAutoPairCallback mCallback = null;
    private UsbDeviceConnection mConn = null;
    private Context mContext = null;
    private long mGetUsbInfoInterval = 10;
    private int mGetUsbInfoTimes = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            byte[] buffer = msg.obj;
            HwLog.i("BluetoothUSBService", "handleMessage: msg = " + msg + "; mGetUsbInfoInterval = " + BluetoothUSBService.this.mGetUsbInfoInterval);
            switch (msg.what) {
                case 11:
                    BluetoothUSBService.this.handleReadSmallWhistleInfo(buffer);
                    return;
                case 12:
                    BluetoothUSBService.this.handleGetSmallWhistleState(buffer);
                    return;
                default:
                    return;
            }
        }
    };
    private NotificationManager mNotificationManager = null;
    private UsbManager mUsbManager = null;
    private BluetoothUsbAutoPairUtils mUtils = null;

    public void onCreate() {
        HwLog.i("BluetoothUSBService", "onCreate");
        this.mContext = getBaseContext();
        this.mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mUtils = new BluetoothUsbAutoPairUtils();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i("BluetoothUSBService", "onStartCommand");
        if (intent == null) {
            stopSelf();
            return 2;
        }
        Bundle args = intent.getExtras();
        if (args == null) {
            stopSelf();
            return 2;
        }
        int cmd = args.getInt("cmdmessage");
        HwLog.i("BluetoothUSBService", "onStartCommand, cmd:" + cmd);
        switch (cmd) {
            case 1:
                onDeviceAttached((UsbDevice) args.getParcelable("usbdevice"));
                break;
            case 2:
            case 4:
                this.mNotificationManager.cancel(536880990);
                stopSelf();
                break;
            case 3:
                this.mNotificationManager.cancel(536880990);
                if (2 == this.mUtils.getDeviceStatus()) {
                    HwLog.i("BluetoothUSBService", "already READY_TO_PAIR_STATE, start discovery straight.");
                    startDiscovery();
                    break;
                }
                switchDeviceToParingMode();
                break;
        }
        return 1;
    }

    private void onDeviceAttached(UsbDevice device) {
        if (device != null) {
            HwLog.i("BluetoothUSBService", "onDeviceAttached: device =" + device);
            this.mConn = connect(device);
            getDeviceInfoByMessage(11);
        }
    }

    private UsbDeviceConnection connect(UsbDevice device) {
        UsbDeviceConnection conn = this.mUsbManager.openDevice(device);
        if (conn == null) {
            HwLog.e("BluetoothUSBService", "connect: Could not obtain device connection.");
            return null;
        } else if (conn.claimInterface(device.getInterface(0), true)) {
            return conn;
        } else {
            HwLog.e("BluetoothUSBService", "connect: Could not claim interface.");
            return null;
        }
    }

    private void getDeviceInfoByMessage(int msgid) {
        if (this.mConn != null) {
            byte[] buffer = new byte[10];
            this.mConn.controlTransfer(161, 1, 0, 0, buffer, 10, 0);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgid, buffer), this.mGetUsbInfoInterval);
        }
    }

    private void handleReadSmallWhistleInfo(byte[] buffer) {
        if (buffer[0] != (byte) 85) {
            this.mGetUsbInfoTimes++;
            this.mGetUsbInfoInterval += 20;
            if (this.mGetUsbInfoTimes != 10) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11, buffer), this.mGetUsbInfoInterval);
                return;
            }
            handleGetInfoFailure();
            return;
        }
        handleGetInfoSuccess(buffer);
    }

    private void handleGetSmallWhistleState(byte[] buffer) {
        this.mUtils.parseStatus(buffer);
        switch (this.mUtils.getDeviceStatus()) {
            case 2:
                startDiscovery();
                return;
            case 3:
                handleGetInfoFailure();
                return;
            default:
                this.mGetUsbInfoTimes++;
                this.mGetUsbInfoInterval += 20;
                if (this.mGetUsbInfoTimes != 10) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(12, buffer), this.mGetUsbInfoInterval);
                    return;
                }
                handleGetInfoFailure();
                return;
        }
    }

    private void switchDeviceToParingMode() {
        HwLog.i("BluetoothUSBService", "switchDeviceToParingMode()");
        if (this.mConn == null) {
            Log.e("BluetoothUSBService", "switchDeviceToParingMode: mConn == null, return.");
            return;
        }
        this.mConn.controlTransfer(33, 2, 0, 0, null, 0, 0);
        getDeviceInfoByMessage(12);
    }

    private void handleGetInfoSuccess(byte[] buffer) {
        this.mGetUsbInfoTimes = 0;
        this.mGetUsbInfoInterval = 10;
        this.mUtils.parseAddress(buffer);
        this.mUtils.parseStatus(buffer);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            if (devices != null) {
                HwLog.i("BluetoothUSBService", "handleGetInfoSuccess: devices = " + devices + "; size = " + devices.size());
                if (devices.size() > 0) {
                    for (BluetoothDevice device : devices) {
                        HwLog.i("BluetoothUSBService", "handleGetInfoSuccess: device.getAddress() = " + device.getAddress());
                        if (device.getAddress().equals(this.mUtils.getDeviceAddress())) {
                            HwLog.i("BluetoothUSBService", "handleGetInfoSuccess: Small Whistle has bonded.");
                            return;
                        }
                    }
                }
            }
            showPairingNotification();
        }
    }

    private void handleGetInfoFailure() {
        this.mGetUsbInfoTimes = 0;
        this.mGetUsbInfoInterval = 10;
        if (this.mCallback != null) {
            this.mCallback.handlePairingFailure(this.mContext);
        }
    }

    private void startDiscovery() {
        this.mCallback = new BluetoothUSBAutoPairCallback(this.mContext, this.mUtils);
        Utils.getLocalBtManager(this.mContext).getEventManager().registerCallback(this.mCallback);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        if (adapter.isEnabled()) {
            HwLog.i("BluetoothUSBService", "startDiscovery: startDiscovery");
            adapter.startDiscovery();
            return;
        }
        HwLog.i("BluetoothUSBService", "startDiscovery: force to open bluetooth.");
        adapter.enable();
    }

    private void showPairingNotification() {
        HwLog.i("BluetoothUSBService", "showPairingNotification");
        RemoteViews headsUpContentView = new RemoteViews(this.mContext.getPackageName(), 2130968656);
        RemoteViews notificationContentView = new RemoteViews(this.mContext.getPackageName(), 2130968657);
        headsUpContentView.setOnClickPendingIntent(2131886315, createAgreePendingIntent());
        headsUpContentView.setOnClickPendingIntent(2131886313, createCancelPendingIntent());
        notificationContentView.setOnClickPendingIntent(2131886315, createAgreePendingIntent());
        notificationContentView.setOnClickPendingIntent(2131886313, createCancelPendingIntent());
        notificationContentView.setImageViewResource(2131886317, 2130838638);
        Builder builder = new Builder(this.mContext).setAutoCancel(false).setShowWhen(false).setContentTitle(this.mContext.getResources().getString(2131624448)).setContentText(this.mContext.getResources().getString(2131628530)).setCustomBigContentView(notificationContentView).setCustomHeadsUpContentView(headsUpContentView).setSmallIcon(17301632);
        try {
            BitmapDrawable drawable = (BitmapDrawable) this.mContext.getPackageManager().getApplicationIcon("com.android.bluetooth");
            if (drawable != null) {
                builder.setLargeIcon(drawable.getBitmap());
            }
        } catch (NameNotFoundException e) {
            HwLog.e("BluetoothUSBService", "Unable to load application icon of BT");
        } catch (Exception ex) {
            HwLog.e("BluetoothUSBService", "ex=" + ex.getMessage());
        }
        Notification notification = builder.build();
        notification.priority = 1;
        notification.defaults |= 1;
        this.mNotificationManager.notify(536880990, notification);
    }

    private PendingIntent createAgreePendingIntent() {
        Intent intent = new Intent(this.mContext, BluetoothUSBReceiver.class);
        intent.setAction("com.android.settings.BTU_AGREE");
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
    }

    private PendingIntent createCancelPendingIntent() {
        Intent intent = new Intent(this.mContext, BluetoothUSBReceiver.class);
        intent.setAction("com.android.settings.BTU_CANCEL");
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
    }

    public void onDestroy() {
        HwLog.i("BluetoothUSBService", "onDestroy");
        if (this.mCallback != null) {
            Utils.getLocalBtManager(this.mContext).getEventManager().unregisterCallback(this.mCallback);
            this.mCallback = null;
        }
        if (this.mConn != null) {
            this.mConn.close();
            this.mConn = null;
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
