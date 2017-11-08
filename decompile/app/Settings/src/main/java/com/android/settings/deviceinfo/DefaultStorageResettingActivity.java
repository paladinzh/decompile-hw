package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.KeyEvent;
import com.android.settings.Utils;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultStorageResettingActivity extends Activity {
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.d("DialogActivityForProgress", "handleMessage REBOOT_SYSTEM");
                DefaultStorageResettingActivity.this.rebootSystem();
                DefaultStorageResettingActivity.this.finish();
            }
        }
    };
    AlertDialog mDialog;
    private BroadcastReceiver mReceiver;
    private Timer mTimer;

    class NotifyTimerTask extends TimerTask {
        NotifyTimerTask() {
        }

        public void run() {
            Message message = new Message();
            message.what = 1;
            Log.d("DialogActivityForProgress", "NotifyTimerTask REBOOT_SYSTEM");
            DefaultStorageResettingActivity.this.handler.sendMessage(message);
        }
    }

    private void rebootSystem() {
        DefaultStorageLocation.switchToInternal(this);
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isSwitchPrimaryVolumeSupported()) {
            this.mDialog = new Builder(this).setTitle(2131627359).setMessage(2131627507).setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DefaultStorageResettingActivity.this.rebootSystem();
                }
            }).create();
            this.mDialog.setCancelable(false);
            if (!getResources().getBoolean(17956877)) {
                this.mDialog.getWindow().addFlags(4);
            }
            this.mDialog.show();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addDataScheme("file");
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    DefaultStorageResettingActivity.this.handleEvent(context, intent);
                }
            };
            registerReceiver(this.mReceiver, filter);
            startTimer();
            return;
        }
        finish();
    }

    private void handleEvent(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("DialogActivityForProgress", "handleEvent:" + action);
        if ("android.intent.action.MEDIA_MOUNTED".equals(action) && !Utils.isVolumeUsb(context, (StorageVolume) intent.getExtra("android.os.storage.extra.STORAGE_VOLUME"))) {
            stopTimer();
            Log.d("DialogActivityForProgress", "dialog dismissed");
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    protected void onDestroy() {
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
        }
        super.onDestroy();
    }

    private void startTimer() {
        this.mTimer = new Timer();
        this.mTimer.schedule(new NotifyTimerTask(), 30000);
    }

    private void stopTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }
}
