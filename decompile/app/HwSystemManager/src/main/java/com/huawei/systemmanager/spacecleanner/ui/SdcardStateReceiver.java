package com.huawei.systemmanager.spacecleanner.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.systemmanager.comm.misc.Utility;

public class SdcardStateReceiver {
    private final Context mContext;
    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                SdcardStateReceiver.this.handlerSdcardAction(action);
                if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                    SdcardStateReceiver.this.handlerSdcardError(action);
                } else if ("android.intent.action.MEDIA_EJECT".equals(action)) {
                    SdcardStateReceiver.this.handlerSdcardError(action);
                } else if ("android.intent.action.MEDIA_SHARED".equals(action)) {
                    SdcardStateReceiver.this.handlerSdcardError(action);
                }
            }
        }
    };
    private boolean mRegistered;

    public SdcardStateReceiver(Context ctx) {
        this.mContext = ctx;
    }

    public void register() {
        if (!this.mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.MEDIA_MOUNTED");
            filter.addAction("android.intent.action.MEDIA_EJECT");
            filter.addAction("android.intent.action.MEDIA_SHARED");
            filter.addDataScheme("file");
            this.mContext.registerReceiver(this.mReciever, filter);
            this.mRegistered = true;
        }
    }

    public void unRegseter() {
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReciever);
            this.mRegistered = false;
        }
    }

    protected void handlerSdcardAction(String action) {
    }

    protected void handlerSdcardError(String action) {
    }
}
