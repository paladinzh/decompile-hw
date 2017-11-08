package com.huawei.mms.util;

import android.content.Context;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.telephony.SmsMessage;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.internal.telephony.SmsApplication;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.huawei.android.telephony.SmsInterceptionManagerEx;
import com.huawei.cspcommon.MLog;

public class VerifitionSmsManager {
    private static VerifitionSmsManager sInstance;
    private static final Uri sThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("update_threads", "true").build();
    private Context mContext = MmsApp.getApplication().getApplicationContext();

    public static synchronized VerifitionSmsManager getInstance() {
        VerifitionSmsManager verifitionSmsManager;
        synchronized (VerifitionSmsManager.class) {
            if (sInstance == null) {
                sInstance = new VerifitionSmsManager();
            }
            verifitionSmsManager = sInstance;
        }
        return verifitionSmsManager;
    }

    private boolean isHwMmsDefaultSms() {
        return SmsApplication.isDefaultSmsApplication(this.mContext, "com.android.mms");
    }

    public void registerListener() {
        if (MmsConfig.isSupportSafeVerifitionSms()) {
            MLog.d("VerifitionSmsManager", "registerListener");
            SmsInterceptionManagerEx.getInstance().registerListener(new VerifitionSmsInterceptionListener(this.mContext), 20000);
        }
    }

    public void unregisterListener() {
        if (MmsConfig.isSupportSafeVerifitionSms()) {
            MLog.d("VerifitionSmsManager", "registerListener");
            SmsInterceptionManagerEx.getInstance().unregisterListener(20000);
        }
    }

    public boolean isVerifitionSms(SmsMessage[] msgs) {
        boolean isVerifitionSms = false;
        if (!MmsConfig.isVerifitionSmsProtectEnable(this.mContext) || !isHwMmsDefaultSms()) {
            return false;
        }
        if (SmartSmsSdkUtil.parseSmsType(this.mContext, msgs[0].getDisplayOriginatingAddress(), msgs[0].getMessageBody(), msgs[0].getServiceCenterAddress(), null, 1) == 1) {
            isVerifitionSms = true;
        }
        if (isVerifitionSms) {
            StatisticalHelper.incrementReportCount(this.mContext, 2164);
        }
        MLog.d("VerifitionSmsManager", "isVerifitionSms = " + isVerifitionSms);
        return isVerifitionSms;
    }

    public void resetSecretFlag(Context context) {
        new ResetVerifitionFlagThread(context).start();
    }
}
