package com.android.settings.bluetooth;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserManager;
import com.android.settings.Utils;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public final class BluetoothPermissionRequest extends BroadcastReceiver {
    Context mContext;
    BluetoothDevice mDevice;
    int mRequestType;
    String mReturnClass = null;
    String mReturnPackage = null;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        if (action == null) {
            HwLog.e("BluetoothPermissionRequest", "received action == null");
            return;
        }
        if (action.equals("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST")) {
            if (Utils.isManagedProfile((UserManager) context.getSystemService("user"))) {
                HwLog.d("BluetoothPermissionRequest", "Blocking notification for managed profile.");
                return;
            }
            this.mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            this.mRequestType = intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 1);
            this.mReturnPackage = intent.getStringExtra("android.bluetooth.device.extra.PACKAGE_NAME");
            this.mReturnClass = intent.getStringExtra("android.bluetooth.device.extra.CLASS_NAME");
            if (checkUserChoice()) {
                HwLog.d("BluetoothPermissionRequest", "UserChoice is unknown");
                return;
            }
            Intent connectionAccessIntent = new Intent(action);
            connectionAccessIntent.setClass(context, BluetoothPermissionActivity.class);
            connectionAccessIntent.setFlags(402653184);
            connectionAccessIntent.setType(Integer.toString(this.mRequestType));
            connectionAccessIntent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
            connectionAccessIntent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
            connectionAccessIntent.putExtra("android.bluetooth.device.extra.PACKAGE_NAME", this.mReturnPackage);
            connectionAccessIntent.putExtra("android.bluetooth.device.extra.CLASS_NAME", this.mReturnClass);
            String address = this.mDevice != null ? this.mDevice.getAddress() : null;
            String name = this.mDevice != null ? this.mDevice.getName() : null;
            PowerManager powerManager = (PowerManager) context.getSystemService("power");
            if (powerManager.isScreenOn() && LocalBluetoothPreferences.shouldShowDialogInForeground(context, address, name)) {
                context.startActivity(connectionAccessIntent);
            } else {
                String title;
                String message;
                WakeLock wakeLock = powerManager.newWakeLock(805306394, "ConnectionAccessActivity");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
                Intent deleteIntent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
                deleteIntent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
                deleteIntent.putExtra("android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT", 2);
                deleteIntent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
                String aliasName = this.mDevice != null ? this.mDevice.getAliasName() : null;
                HwLog.d("BluetoothPermissionRequest", "display on notification tar, mRequestType =" + this.mRequestType);
                switch (this.mRequestType) {
                    case 2:
                        title = context.getString(2131624467);
                        message = context.getString(2131624468, new Object[]{aliasName, aliasName});
                        break;
                    case 3:
                        title = context.getString(2131627781);
                        message = context.getString(2131627782, new Object[]{aliasName});
                        break;
                    case 4:
                        title = context.getString(2131624473);
                        message = context.getString(2131624474, new Object[]{aliasName, aliasName});
                        break;
                    default:
                        title = context.getString(2131624464);
                        message = context.getString(2131624466, new Object[]{aliasName, aliasName});
                        break;
                }
                Notification notification = new Builder(context).setContentTitle(title).setTicker(message).setContentText(message).setSmallIcon(17301632).setAutoCancel(true).setPriority(2).setOnlyAlertOnce(false).setDefaults(-1).setContentIntent(PendingIntent.getActivity(context, 0, connectionAccessIntent, 0)).setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0)).build();
                notification.flags |= 32;
                ((NotificationManager) context.getSystemService("notification")).notify(getNotificationTag(this.mRequestType), 17301632, notification);
                wakeLock.release();
            }
        } else if (action.equals("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL")) {
            NotificationManager manager = (NotificationManager) context.getSystemService("notification");
            this.mRequestType = intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2);
            manager.cancel(getNotificationTag(this.mRequestType), 17301632);
            HwLog.d("BluetoothPermissionRequest", "ACTION_CONNECTION_ACCESS_CANCEL  mRequestType=" + this.mRequestType);
        }
    }

    private String getNotificationTag(int requestType) {
        if (requestType == 2) {
            return "Phonebook Access";
        }
        if (this.mRequestType == 3) {
            return "Message Access";
        }
        if (this.mRequestType == 4) {
            return "SIM Access";
        }
        return null;
    }

    private boolean checkUserChoice() {
        boolean processed = false;
        if (this.mRequestType == 2 || this.mRequestType == 3 || this.mRequestType == 4) {
            LocalBluetoothManager bluetoothManager = Utils.getLocalBtManager(this.mContext);
            if (bluetoothManager == null) {
                HwLog.e("BluetoothPermissionRequest", "bluetoothManager is null");
                return false;
            }
            CachedBluetoothDeviceManager cachedDeviceManager = bluetoothManager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(this.mDevice);
            if (cachedDevice == null) {
                cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(), bluetoothManager.getProfileManager(), this.mDevice);
            }
            String intentName = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
            HwLog.d("BluetoothPermissionRequest", "mRequestType=" + this.mRequestType);
            if (this.mRequestType == 2) {
                int phonebookPermission = cachedDevice.getPhonebookPermissionChoice();
                BluetoothClass bluetoothClass = null;
                if (this.mDevice != null) {
                    bluetoothClass = this.mDevice.getBluetoothClass();
                }
                if (bluetoothClass != null) {
                    HwLog.v("BluetoothPermissionRequest", "checkUserChoice  phonebookPermission= " + phonebookPermission + ", btDeviceClass=" + bluetoothClass.getDeviceClass());
                    if (shouldResetPbapPermission(phonebookPermission)) {
                        phonebookPermission = 0;
                        HwLog.i("BluetoothPermissionRequest", "just for Bluetooth pbap, phonebookPermission = " + 0);
                    }
                }
                if (phonebookPermission != 0) {
                    if (phonebookPermission == 1) {
                        sendReplyIntentToReceiver(true);
                        processed = true;
                    } else if (phonebookPermission == 2) {
                        sendReplyIntentToReceiver(false);
                        processed = true;
                    } else {
                        HwLog.e("BluetoothPermissionRequest", "Bad phonebookPermission: " + phonebookPermission);
                    }
                }
            } else if (this.mRequestType == 3) {
                int messagePermission = cachedDevice.getMessagePermissionChoice();
                if (messagePermission != 0) {
                    if (messagePermission == 1) {
                        sendReplyIntentToReceiver(true);
                        processed = true;
                    } else if (messagePermission == 2) {
                        sendReplyIntentToReceiver(false);
                        processed = true;
                    } else {
                        HwLog.e("BluetoothPermissionRequest", "Bad messagePermission: " + messagePermission);
                    }
                }
            } else if (this.mRequestType == 4) {
                int simPermission = cachedDevice.getSimPermissionChoice();
                if (simPermission != 0) {
                    if (simPermission == 1) {
                        sendReplyIntentToReceiver(true);
                        processed = true;
                    } else if (simPermission == 2) {
                        sendReplyIntentToReceiver(false);
                        processed = true;
                    } else {
                        HwLog.e("BluetoothPermissionRequest", "Bad simPermission: " + simPermission);
                    }
                }
            }
            HwLog.d("BluetoothPermissionRequest", "checkUserChoice(): returning " + processed);
            return processed;
        }
        HwLog.d("BluetoothPermissionRequest", "checkUserChoice(): Unknown RequestType " + this.mRequestType);
        return false;
    }

    private void sendReplyIntentToReceiver(boolean allowed) {
        int i;
        Intent intent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        HwLog.d("BluetoothPermissionRequest", "sendReplyIntentToReceiver allowed=" + allowed + ", mReturnPackage=" + this.mReturnPackage + ", mReturnClass=" + this.mReturnClass);
        if (!(this.mReturnPackage == null || this.mReturnClass == null)) {
            intent.setClassName(this.mReturnPackage, this.mReturnClass);
        }
        String str = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
        if (allowed) {
            i = 1;
        } else {
            i = 2;
        }
        intent.putExtra(str, i);
        intent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
        intent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
        this.mContext.sendBroadcast(intent, "android.permission.BLUETOOTH_ADMIN");
    }

    private boolean shouldResetPbapPermission(int permission) {
        boolean ret = false;
        if (this.mDevice == null || this.mContext == null) {
            HwLog.e("BluetoothPermissionRequest", "null device or context. Failed to query PBAP caution state.");
            return false;
        }
        if (permission == 2) {
            try {
                if (this.mContext.getSharedPreferences("pbap_caution_state", 0).getInt(this.mDevice.getAddress(), 0) == 0) {
                    ret = true;
                }
            } catch (Exception e) {
                HwLog.e("BluetoothPermissionRequest", "Failed to query PBAP caution state.");
                e.printStackTrace();
            }
        }
        return ret;
    }
}
