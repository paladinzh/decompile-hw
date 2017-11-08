package com.android.mms.transaction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import com.android.messaging.util.OsUtil;
import com.huawei.cspcommon.MLog;

public class SmsReceiver extends BroadcastReceiver {
    static WakeLock mStartingService;
    static final Object mStartingServiceSync = new Object();
    private static SmsReceiver sInstance;

    public static synchronized SmsReceiver getInstance() {
        SmsReceiver smsReceiver;
        synchronized (SmsReceiver.class) {
            if (sInstance == null) {
                sInstance = new SmsReceiver();
            }
            smsReceiver = sInstance;
        }
        return smsReceiver;
    }

    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        if (privileged || !"android.provider.Telephony.SMS_DELIVER".equals(intent.getAction())) {
            MLog.d("SmsReceiver", "onReceiveWithPrivilege: start SmsReceiverService.");
            intent.setClass(context, SmsReceiverService.class);
            intent.putExtra("result", getResultCode());
            beginStartingService(context, intent);
        }
    }

    public static void beginStartingService(Context context, Intent intent) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.e("SmsReceiver", "MultiUserCheck Error beginStartingService run in SecondaryUser");
            return;
        }
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                mStartingService = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null && service.stopSelfResult(startId)) {
                MLog.d("SmsReceiver", "service stopSelf, startId:" + startId + ", service:" + service);
                mStartingService.release();
            }
        }
    }

    public static void broadcastForSendSms(Context context) {
        Intent intent = new Intent("com.android.mms.transaction.SEND_MESSAGE", null, context, SmsReceiver.class);
        if (OsUtil.isAtLeastL() ? OsUtil.isSecondaryUser() : false) {
            context.sendBroadcastAsUser(intent, UserHandle.OWNER);
        } else {
            context.sendBroadcast(intent);
        }
    }
}
