package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class HwCustUpdateUserBehaviorImpl extends HwCustUpdateUserBehavior {
    public static final String MMS = "Mms";
    public static final int MMS_TYPE = 1;
    public static final String RECEIVE = "receive";
    public static final int RECEIVE_MESSAGE = 1009;
    public static final String SEND = "send";
    public static final int SEND_FAILED = 1008;
    public static final int SEND_SUCCEED = 1007;
    public static final String SMS = "Sms";
    public static final int SMS_TYPE = 0;
    public static final String TAG = "HwCustUpdateUserBehaviorImpl";
    public static SharedPreferences mSp = null;

    static class UploadThread extends Thread {
        Context context;
        int id;
        String str;

        public UploadThread(Context context, int id, String str) {
            this.id = id;
            this.str = str;
            this.context = context;
        }

        public void run() {
            super.run();
            Log.d(HwCustUpdateUserBehaviorImpl.TAG, "result:" + ReportTool.getInstance(this.context).report(this.id, this.str));
        }
    }

    public void upLoadReceiveMesInfo(Context context, int type) {
        switch (type) {
            case 0:
                upLoadMessageData(context, RECEIVE, SMS, 1009);
                return;
            case 1:
                upLoadMessageData(context, RECEIVE, MMS, 1009);
                return;
            default:
                return;
        }
    }

    public void upLoadSendMesFail(Context context, int type, String reason, long startTime, long endTime) {
        upLoadFailReasionData(context, type, reason, startTime, endTime);
    }

    public void upLoadSendMesSucc(Context context, int type, long startTime, long endTime) {
        upLoadSuccReasionData(context, type, startTime, endTime);
    }

    public void upLoadMessageData(Context context, String operate, String type, int id) {
        String[] text = new String[]{operate, type};
        new UploadThread(context, id, String.format("{Type:%s}", new Object[]{type})).start();
        Log.d(TAG, String.format("{Type:%s:%s}", text));
    }

    public void upLoadFailReasionData(Context context, int id, String reason, long startTime, long endTime) {
        String sendDuration = "";
        if (endTime == -1 || startTime == -1 || endTime < startTime) {
            Log.d(TAG, "endTime:" + endTime + " startTime:" + startTime);
            return;
        }
        String[] text = new String[]{(endTime - startTime) + "", reason};
        if (id == 0) {
            new UploadThread(context, 1008, String.format("{Type:Sms, count: %sms, reason is %s}", text)).start();
            Log.d(TAG, String.format("{Type:Sms, count: %sms, reason is %s}", text));
        }
        if (id == 1) {
            new UploadThread(context, 1008, String.format("{Type:Mms, count: %sms, reason is %s}", text)).start();
            Log.d(TAG, String.format("{Type:Mms, count: %sms, reason is %s}}", text));
        }
    }

    public void upLoadSuccReasionData(Context context, int id, long startTime, long endTime) {
        String sendDuration = "";
        if (endTime == -1 || startTime == -1 || endTime < startTime) {
            Log.d(TAG, "endTime:" + endTime + " startTime:" + startTime);
            return;
        }
        sendDuration = (endTime - startTime) + "";
        if (id == 0) {
            new UploadThread(context, 1007, String.format("{Type:Sms, count: %sms}", new Object[]{sendDuration})).start();
            Log.d(TAG, String.format("{Send Sms succeed, count: %sms}", new Object[]{sendDuration}));
        }
        if (id == 1) {
            new UploadThread(context, 1007, String.format("{Type:Mms, count: %sms}", new Object[]{sendDuration})).start();
            Log.d(TAG, String.format("{Mms, count: %sms}", new Object[]{sendDuration}));
        }
    }

    public void saveTime(Context context, String spName, String duration, long time) {
        mSp = context.getSharedPreferences(spName, 0);
        Editor editor = mSp.edit();
        editor.putLong(duration, time);
        editor.commit();
        Log.d(TAG, "saveTime to SharedPreferences time :" + time);
    }

    public long getTime(Context context, String spName, String duration) {
        if (mSp == null) {
            mSp = context.getSharedPreferences(spName, 0);
        }
        return mSp.getLong(duration, -1);
    }
}
