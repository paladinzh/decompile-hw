package com.huawei.harassmentinterception.util;

import android.content.Context;
import com.android.internal.telephony.SmsApplication;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class SmsInterceptionHelper {
    public static final String TAG = "SmsInterceptionHelper";

    public static boolean addToInterceptRecord(Context context, SmsMsgInfo smsInfo, int blockReason) {
        if (smsInfo == null) {
            HwLog.e(TAG, "Invalid sms info");
            return false;
        }
        smsInfo.setBlockReason(blockReason);
        try {
            DBAdapter.addInterceptedMsg(context, smsInfo);
            if (DBAdapter.getUnreadMsgCount(context) > 0) {
                CommonHelper.sendNotificationForAll(context, blockReason);
            }
            HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_MSG, HsmStatConst.PARAM_VAL, String.valueOf(smsInfo.getBlockReason()), HsmStatConst.PARAM_SUB, String.valueOf(smsInfo.getSubId() + 1));
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "addToInterceptRecord: Exception ", e);
            return false;
        }
    }

    public static boolean isDefaultSmsApp() {
        return SmsApplication.isDefaultSmsApplication(GlobalContext.getContext(), "com.android.mms");
    }
}
