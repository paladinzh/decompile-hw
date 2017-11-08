package com.huawei.harassmentinterception.util;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;

public class MmsBlackListHelper {
    private static final String TAG = "MmsBlackListHelper";

    public static int handleMmsByBlackList(Context context, MessageInfo mmsInfo) {
        if (mmsInfo == null) {
            HwLog.e(TAG, "handleMms : Fail to get mms info from intent");
            return -1;
        } else if (DBAdapter.checkMatchBlacklist(context, mmsInfo.getPhone(), 1) != 0) {
            HwLog.d(TAG, "handleMms: Not in blacklist or option doesn't match, pass");
            return 0;
        } else {
            HwLog.i(TAG, "handleMms: The mms should be blocked");
            return 1;
        }
    }
}
