package com.huawei.mms.util;

import android.content.Context;

public class HwCustUpdateUserBehavior {
    public void upLoadReceiveMesInfo(Context context, int type) {
    }

    public void upLoadSendMesFail(Context context, int type, String reason, long startTime, long endTime) {
    }

    public void upLoadSendMesSucc(Context context, int type, long startTime, long endTime) {
    }

    public void saveTime(Context context, String spName, String duration, long time) {
    }

    public long getTime(Context context, String spName, String duration) {
        return -1;
    }

    public void playSentSuccessTone(Context aContext) {
    }
}
