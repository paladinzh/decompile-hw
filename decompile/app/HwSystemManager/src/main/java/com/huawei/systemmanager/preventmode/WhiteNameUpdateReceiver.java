package com.huawei.systemmanager.preventmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class WhiteNameUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = WhiteNameUpdateReceiver.class.getName();
    private IWhiteNameUpdateListener mListener;

    public WhiteNameUpdateReceiver(IWhiteNameUpdateListener listener) {
        this.mListener = listener;
    }

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            HwLog.e(TAG, "onReceive new information");
            try {
                if (PreventConst.ACTION_PREVENT_MODE_UPDATE_WHITELIST.equals(intent.getAction())) {
                    ArrayList<ContactInfo> contacts = (ArrayList) intent.getSerializableExtra(PreventConst.PREVENT_UPDATE_WHITELIST_KEY);
                    if (contacts == null || contacts.size() == 0) {
                        HwLog.d(TAG, "intent.getSerializableExtra no data");
                        return;
                    }
                    this.mListener.sendUpdateNotification(contacts);
                }
            } catch (ClassCastException ex) {
                HwLog.e(TAG, "onReceive ClassCastException" + ex.toString());
            } catch (Exception e) {
                HwLog.e(TAG, "onReceive excpetion" + e.toString());
            }
        }
    }
}
