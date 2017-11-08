package com.android.settings;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class DataOnlyChangeReceiver extends BroadcastReceiver {
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("DataOnlyChangeReceiver", "handleMessage msg = " + msg.what);
            switch (msg.what) {
                case 1011:
                    if (DataOnlyChangeReceiver.this.mpDialog != null && DataOnlyChangeReceiver.this.mpDialog.isShowing()) {
                        DataOnlyChangeReceiver.this.mpDialog.dismiss();
                        if (DataOnlyChangeReceiver.this.mTimer != null) {
                            DataOnlyChangeReceiver.this.mTimer.cancel();
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    Log.d("DataOnlyChangeReceiver", "received unknow event, just return");
                    return;
            }
        }
    };
    private int mThemeID;
    private Timer mTimer;
    private ProgressDialog mpDialog;

    public void onReceive(Context arg0, Intent arg1) {
        if (SystemProperties.getBoolean("ro.config.hw_lteonly_for_qcomm", false)) {
            this.mContext = arg0;
            this.mThemeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            if (arg1 != null && "com.android.huawei.ACTION_LET_MODE_CHANGE".equals(arg1.getAction()) && arg1.getBooleanExtra("LTE_DATA_ONLY_OFF", false)) {
                if (SystemProperties.getBoolean("ro.config.hw_lteonly_for_qcomm", false)) {
                    showDialog();
                    sendMessage(1011, 15000);
                }
                deviceRebootOrShutdown(false, "reboot_modem");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void deviceRebootOrShutdown(boolean reboot, String reason) {
        try {
            Class<?> cl = Class.forName("com.qti.server.power.ShutdownOem");
            cl.getMethod("rebootOrShutdown", new Class[]{Boolean.TYPE, String.class}).invoke(cl.newInstance(), new Object[]{Boolean.valueOf(reboot), reason});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (Exception ex2) {
            ex2.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        this.mpDialog = new ProgressDialog(this.mContext, this.mThemeID);
        this.mpDialog.setProgressStyle(1);
        this.mpDialog.setTitle(2131627311);
        this.mpDialog.setCancelable(false);
        this.mpDialog.setCanceledOnTouchOutside(false);
        this.mpDialog.setMax(15);
        this.mpDialog.setIndeterminate(false);
        this.mpDialog.getWindow().setType(2003);
        this.mpDialog.show();
        this.mTimer = new Timer(false);
        this.mTimer.schedule(new TimerTask() {
            public void run() {
                DataOnlyChangeReceiver.this.mHandler.post(new Runnable() {
                    public void run() {
                        DataOnlyChangeReceiver.this.mpDialog.incrementProgressBy(1);
                    }
                });
            }
        }, 1000, 1000);
    }

    private void sendMessage(int what, int delay) {
        this.mHandler.removeMessages(what);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what), (long) delay);
    }
}
