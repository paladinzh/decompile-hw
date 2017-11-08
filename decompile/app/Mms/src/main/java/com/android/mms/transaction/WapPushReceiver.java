package com.android.mms.transaction;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import com.android.messaging.util.BugleActivityUtil;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;

public class WapPushReceiver extends BroadcastReceiver {
    private static HwCustWapPushReceiver mHwCustWapPushReceiver = ((HwCustWapPushReceiver) HwCustUtils.createObj(HwCustWapPushReceiver.class, new Object[0]));

    @SuppressLint({"NewApi"})
    private static class ReceivePushTask extends AsyncTask<Intent, Void, Void> {
        private Context mContext;

        public ReceivePushTask(Context context) {
            this.mContext = context;
        }

        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];
            byte[] pushData = intent.getByteArrayExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
            if (pushData == null) {
                return null;
            }
            if (pushData.length > 4096) {
                MLog.e("WapPushReceiver", "Exceed the max byte : 4096 ,data length = " + pushData.length);
                return null;
            }
            WapPushParser parser = new WapPushParser(pushData);
            WapPushMsg pushMsg = null;
            if ("application/vnd.wap.sic".equals(intent.getType())) {
                pushMsg = parser.parse(0);
            } else if ("application/vnd.wap.slc".equals(intent.getType())) {
                pushMsg = parser.parse(1);
            }
            if (pushMsg == null) {
                MLog.e("WapPushReceiver", "Invalid WAP PUSH data");
                return null;
            }
            String wappushFrom = intent.getStringExtra("sender");
            if (!TextUtils.isEmpty(wappushFrom)) {
                pushMsg.setAttributeValue(0, wappushFrom);
            }
            String serviceCenterAddress = intent.getStringExtra("ServiceCenterAddress");
            if (!TextUtils.isEmpty(serviceCenterAddress)) {
                pushMsg.setAttributeValue(9, serviceCenterAddress);
            }
            String subIdStr = String.valueOf(MessageUtils.getSimIdFromIntent(intent, 0));
            if (!TextUtils.isEmpty(subIdStr)) {
                pushMsg.setAttributeValue(10, subIdStr);
            }
            if (pushMsg.storeWapPushMessage(this.mContext) != null) {
                long threadId;
                if (MmsConfig.getEnableWapSenderAddress()) {
                    threadId = Threads.getOrCreateThreadId(this.mContext, pushMsg.getAttributeValueString(0));
                } else {
                    threadId = Threads.getOrCreateThreadId(this.mContext, WapPushMsg.WAP_PUSH_MESSAGE_ID);
                }
                if (MmsConfig.isSmsDefaultApp(this.mContext)) {
                    Context context = this.mContext;
                    if (threadId == 0) {
                        threadId = -2;
                    }
                    MessagingNotification.nonBlockingUpdateNewMessageIndicatorForWap(context, threadId, false);
                }
                ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(805306378, "WapPushReceiver").acquire(5000);
            }
            if (WapPushReceiver.mHwCustWapPushReceiver != null) {
                WapPushReceiver.mHwCustWapPushReceiver.handleSlWapPushMessageOpenBrowser(this.mContext, intent, pushMsg);
            }
            return null;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_sms_wappush_enable", false)) {
            if ("android.provider.Telephony.WAP_PUSH_RECEIVED".equals(intent.getAction()) && ("application/vnd.wap.sic".equals(intent.getType()) || "application/vnd.wap.slc".equals(intent.getType()))) {
                startWapPushTask(context, intent);
            }
            return;
        }
        MLog.i("WapPushReceiver", "WAP Push is not supported!!!");
    }

    private static void startWapPushTask(final Context rContext, final Intent rIntent) {
        BugleActivityUtil.checkPermissionWithOutCheckFirstLaunch(rContext, new Runnable() {
            public void run() {
                new ReceivePushTask(rContext).executeOnExecutor(ThreadEx.getDefaultExecutor(), new Intent[]{rIntent});
            }
        });
    }
}
