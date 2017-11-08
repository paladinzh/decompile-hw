package com.android.gallery3d.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.android.gallery3d.util.GalleryLog;

public class HicloudAccountReceiver extends BroadcastReceiver {
    public static final String ACTION_HICLOUD_ACCOUNT_CHANGE = "com.huawei.hwid.ACTION_ACCOUNTNAME_CHANGE";
    public static final String ACTION_HICLOUD_ACCOUNT_LOGOUT = "com.huawei.hwid.ACTION_REMOVE_ACCOUNT";
    public static final String ACTION_HICLOUD_ACCOUNT_USER_INFO_CHANGE = "com.huawei.hwid.ACTION_HEAD_PIC_CHANGE";
    private static final int MSG_BASE = 65535;
    public static final int MSG_HICLOUD_ACCOUNT_CHANGED = 65538;
    public static final int MSG_HICLOUD_ACCOUNT_LOGOUT = 65536;
    public static final int MSG_HICLOUD_ACCOUNT_USER_INFO_CHANGED = 65537;
    private static final String TAG = "HicloudAccountReceiver";
    private Handler mHandler;

    public HicloudAccountReceiver(Handler handler) {
        this.mHandler = handler;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (this.mHandler != null) {
            if (ACTION_HICLOUD_ACCOUNT_LOGOUT.equals(action)) {
                this.mHandler.obtainMessage(MSG_HICLOUD_ACCOUNT_LOGOUT).sendToTarget();
                GalleryLog.i(TAG, "account logout");
            } else if (ACTION_HICLOUD_ACCOUNT_USER_INFO_CHANGE.equals(action)) {
                this.mHandler.obtainMessage(MSG_HICLOUD_ACCOUNT_USER_INFO_CHANGED).sendToTarget();
                GalleryLog.i(TAG, "user info change");
            } else if (ACTION_HICLOUD_ACCOUNT_CHANGE.equals(action)) {
                this.mHandler.obtainMessage(MSG_HICLOUD_ACCOUNT_CHANGED).sendToTarget();
                GalleryLog.i(TAG, "account change");
            }
        }
    }
}
