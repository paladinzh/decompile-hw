package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.SettingsExtUtils;
import com.android.settings.bluetooth.DockService.DockBluetoothCallback;
import com.android.settingslib.R$string;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager.BluetoothManagerCallback;
import com.android.settingslib.bluetooth.LocalBluetoothManager.BtDialogObserver;
import com.android.settingslib.bluetooth.Utils.ErrorListener;
import com.huawei.iconnect.wearable.CompanionAppHelper;
import com.huawei.iconnect.wearable.ManufacturerDataHelper;

public final class Utils {
    private static final ErrorListener mErrorListener = new ErrorListener() {
        public void onShowError(Context context, String name, int messageResId) {
            Utils.showError(context, name, messageResId);
        }
    };
    private static final BluetoothManagerCallback mOnInitCallback = new BluetoothManagerCallback() {
        public void onBluetoothManagerInitialized(Context appContext, LocalBluetoothManager bluetoothManager) {
            bluetoothManager.getEventManager().registerCallback(new DockBluetoothCallback(appContext));
            com.android.settingslib.bluetooth.Utils.setErrorListener(Utils.mErrorListener);
        }
    };

    public static class LaunchPairingAction {
    }

    private Utils() {
    }

    static AlertDialog showDisconnectDialog(Context context, AlertDialog dialog, OnClickListener disconnectListener, CharSequence title, CharSequence message) {
        if (dialog == null) {
            dialog = new Builder(context).setPositiveButton(17039370, disconnectListener).setNegativeButton(17039360, null).create();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog.setButton(-1, context.getText(17039370), disconnectListener);
        }
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    static void showError(Context context, String name, int messageResId) {
        String message = context.getString(messageResId, new Object[]{name});
        LocalBluetoothManager manager = getLocalBtManager(context);
        if (manager == null) {
            MLog.e("Bluetooth.Utils", "get LocalBluetoothManager return null in showError!");
        } else if (Secure.getInt(context.getContentResolver(), "db_bluetooth_launch_pairing", 0) == 2 && messageResId == 2131624258) {
            MLog.w("Bluetooth.Utils", "User launched pairing and canceled it, ignore message : " + message);
            if (!Secure.putInt(context.getContentResolver(), "db_bluetooth_launch_pairing", 0)) {
                MLog.e("Bluetooth.Utils", "failed to save launch pairing status, key = db_bluetooth_launch_pairing, value = 0");
            }
        } else {
            String queryStr = null;
            if (2131624258 == messageResId) {
                queryStr = context.getString(2131628037);
            } else if (2131624259 == messageResId) {
                queryStr = context.getString(2131628038);
            } else if (2131624257 == messageResId) {
                queryStr = context.getString(2131628039);
            } else if (2131624256 == messageResId) {
                queryStr = context.getString(2131628035);
            } else if (2131624269 == messageResId) {
                queryStr = context.getString(2131628036);
            }
            final String finalQueryStr = queryStr;
            final Context currentContext = manager.getContext();
            View layoutView = LayoutInflater.from(currentContext).inflate(2130968741, null);
            ((TextView) layoutView.findViewById(2131886507)).setText(message);
            TextView knowMoreView = (TextView) layoutView.findViewById(2131886508);
            if (hasPackageInfo(currentContext.getPackageManager(), "com.huawei.phoneservice")) {
                knowMoreView.setText(currentContext.getString(R$string.know_more));
                knowMoreView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction("android.bluetooth.FAQ");
                        Bundle b = new Bundle();
                        b.putString("query_word", finalQueryStr);
                        intent.putExtras(b);
                        currentContext.sendBroadcast(intent);
                    }
                });
            } else {
                knowMoreView.setVisibility(8);
            }
            if (manager.isForegroundActivity()) {
                Context activity = manager.getForegroundActivity();
                if ((activity instanceof Activity) && ((Activity) activity).isFinishing()) {
                    Log.w("Bluetooth.Utils", "showError() not show dialog because of activity is not alive.");
                    return;
                }
                AlertDialog dialog = new Builder(activity).setTitle(2131624822).setView(layoutView).setPositiveButton(2131626512, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                }).create();
                BtDialogObserver observer = manager.getBtDialogObserver();
                if (observer != null) {
                    observer.onDialogShow(dialog);
                }
                dialog.show();
            } else {
                SettingsExtUtils.showEmuiToast(context, message);
            }
        }
    }

    private static boolean hasPackageInfo(PackageManager manager, String name) {
        try {
            manager.getPackageInfo(name, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return LocalBluetoothManager.getInstance(context, mOnInitCallback);
    }

    public static boolean isIConnectApiExist() {
        try {
            ManufacturerDataHelper.getRemoteBleDeviceType(null, null, null);
            CompanionAppHelper.getPackageNameOfCompanion(null, null, null);
            return true;
        } catch (NoClassDefFoundError error) {
            MLog.e("Bluetooth.Utils", "NoClassDefFoundError, error msg: " + error.getMessage());
            return false;
        } catch (NoSuchMethodError error2) {
            MLog.e("Bluetooth.Utils", "NoSuchMethodError , error msg: " + error2.getMessage());
            return false;
        } catch (Exception e) {
            MLog.e("Bluetooth.Utils", "Exception , error msg: " + e.getMessage());
            return false;
        }
    }

    public static boolean isAppMarketInstalled(Context context) {
        if (context == null) {
            return false;
        }
        return com.android.settings.Utils.hasIntentActivity(context.getPackageManager(), new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.huawei.bone")));
    }

    public static void removeSprefData(Context context, String deviceAddress) {
        if (context != null && deviceAddress != null) {
            Log.d("Bluetooth.Utils", "removeSprefData:" + deviceAddress + "temporary_phone_perssion");
            String key = "key_phone_permission" + deviceAddress;
            Editor editor = context.getSharedPreferences("temporary_phone_perssion", 0).edit();
            editor.remove(key);
            editor.commit();
        }
    }

    public static int getSprefData(Context context, String deviceAddress) {
        if (context == null || deviceAddress == null) {
            return 2;
        }
        Log.d("Bluetooth.Utils", "getSprefData:" + deviceAddress + " permission choise ");
        return context.getSharedPreferences("temporary_phone_perssion", 0).getInt("key_phone_permission" + deviceAddress, 2);
    }

    public static void saveSprefData(Context context, String deviceAddress, int permissionChoice) {
        if (context != null && deviceAddress != null) {
            Log.d("Bluetooth.Utils", "saveSprefData:" + deviceAddress + " permission choise:" + permissionChoice);
            String key = "key_phone_permission" + deviceAddress;
            Editor editor = context.getSharedPreferences("temporary_phone_perssion", 0).edit();
            editor.putInt(key, permissionChoice);
            editor.commit();
        }
    }
}
