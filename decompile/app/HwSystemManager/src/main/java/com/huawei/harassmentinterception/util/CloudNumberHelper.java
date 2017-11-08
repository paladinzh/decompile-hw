package com.huawei.harassmentinterception.util;

import android.content.Context;
import com.huawei.harassmentinterception.db.DBAdapter;

public class CloudNumberHelper {
    public static boolean isCloudNumber(Context context, String phone) {
        if (DBAdapter.isNumberMatch(context, phone) || DBAdapter.isNumberFromCloud(context, phone)) {
            return true;
        }
        return false;
    }
}
