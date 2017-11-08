package com.huawei.harassmentinterception.util;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class MmsInterceptionHelper {
    private static final String TAG = "MmsInterceptionHelper";

    public static boolean addToInterceptRecord(Context context, MessageInfo mmsInfo, int blockReason) {
        try {
            mmsInfo.setBlockReason(blockReason);
            DBAdapter.addInterceptedMsg(context, mmsInfo);
            if (DBAdapter.getUnreadMsgCount(context) > 0) {
                CommonHelper.sendNotificationForAll(context, blockReason);
            }
            HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_MSG, HsmStatConst.PARAM_VAL, String.valueOf(mmsInfo.getBlockReason()), HsmStatConst.PARAM_SUB, String.valueOf(mmsInfo.getSubId() + 1));
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "addToInterceptRecord: Exception ", e);
            return false;
        }
    }
}
