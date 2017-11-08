package com.android.settings.bluetooth;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.PowerManager;
import android.text.TextUtils;

public final class BluetoothPairingRequest extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            HwLog.e("BluetoothPairingRequest", "the content of intent is null!");
            return;
        }
        String action = intent.getAction();
        if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            PowerManager powerManager;
            boolean shouldShowDialog;
            Resources res;
            Builder builder;
            BitmapDrawable drawable;
            PendingIntent pending;
            String name;
            Intent intent2;
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            int type = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
            Intent pairingIntent = new Intent();
            pairingIntent.setClass(context, BluetoothPairingDialog.class);
            pairingIntent.putExtra("android.bluetooth.device.extra.DEVICE", device);
            pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_VARIANT", type);
            if (!(type == 2 || type == 4)) {
                if (type == 5) {
                }
                pairingIntent.setAction("android.bluetooth.device.action.PAIRING_REQUEST");
                pairingIntent.setFlags(268435456);
                powerManager = (PowerManager) context.getSystemService("power");
                shouldShowDialog = LocalBluetoothPreferences.shouldShowDialogInForeground(context, device == null ? device.getAddress() : null, device == null ? device.getName() : null);
                if (powerManager.isInteractive() || !shouldShowDialog) {
                    res = context.getResources();
                    builder = new Builder(context).setSmallIcon(17301632).setTicker(res.getString(2131624447));
                    drawable = (BitmapDrawable) context.getPackageManager().getApplicationIcon("com.android.bluetooth");
                    if (drawable != null) {
                        builder.setLargeIcon(drawable.getBitmap());
                    }
                    pending = PendingIntent.getActivity(context, 0, pairingIntent, 134217728);
                    name = intent.getStringExtra("android.bluetooth.device.extra.NAME");
                    if (TextUtils.isEmpty(name)) {
                        if (device == null) {
                            name = device.getAliasName();
                        } else {
                            name = context.getString(17039374);
                        }
                    }
                    builder.setContentTitle(res.getString(2131624448)).setContentText(res.getString(2131624449, new Object[]{name})).setContentIntent(pending).setAutoCancel(true).setDefaults(1).setColor(context.getColor(17170519));
                    ((NotificationManager) context.getSystemService("notification")).notify(17301632, builder.getNotification());
                    intent2 = new Intent(context, ScreenWakerService.class);
                    intent2.putExtra("timeout", 10000);
                    context.startService(intent2);
                } else {
                    context.startActivity(pairingIntent);
                }
            }
            pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_KEY", intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE));
            pairingIntent.setAction("android.bluetooth.device.action.PAIRING_REQUEST");
            pairingIntent.setFlags(268435456);
            powerManager = (PowerManager) context.getSystemService("power");
            if (device == null) {
            }
            if (device == null) {
            }
            shouldShowDialog = LocalBluetoothPreferences.shouldShowDialogInForeground(context, device == null ? device.getAddress() : null, device == null ? device.getName() : null);
            if (powerManager.isInteractive()) {
            }
            res = context.getResources();
            builder = new Builder(context).setSmallIcon(17301632).setTicker(res.getString(2131624447));
            try {
                drawable = (BitmapDrawable) context.getPackageManager().getApplicationIcon("com.android.bluetooth");
                if (drawable != null) {
                    builder.setLargeIcon(drawable.getBitmap());
                }
            } catch (NameNotFoundException e) {
                HwLog.e("BluetoothPairingRequest", "Unable to load application icon of BT");
            } catch (Exception ex) {
                HwLog.e("BluetoothPairingRequest", "ex=" + ex.getMessage());
            }
            pending = PendingIntent.getActivity(context, 0, pairingIntent, 134217728);
            name = intent.getStringExtra("android.bluetooth.device.extra.NAME");
            if (TextUtils.isEmpty(name)) {
                if (device == null) {
                    name = context.getString(17039374);
                } else {
                    name = device.getAliasName();
                }
            }
            builder.setContentTitle(res.getString(2131624448)).setContentText(res.getString(2131624449, new Object[]{name})).setContentIntent(pending).setAutoCancel(true).setDefaults(1).setColor(context.getColor(17170519));
            ((NotificationManager) context.getSystemService("notification")).notify(17301632, builder.getNotification());
            intent2 = new Intent(context, ScreenWakerService.class);
            intent2.putExtra("timeout", 10000);
            context.startService(intent2);
        } else if (action.equals("android.bluetooth.device.action.PAIRING_CANCEL")) {
            ((NotificationManager) context.getSystemService("notification")).cancel(17301632);
        } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
            int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
            if (intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", Integer.MIN_VALUE) == 11 && bondState == 10) {
                ((NotificationManager) context.getSystemService("notification")).cancel(17301632);
            }
        }
    }
}
