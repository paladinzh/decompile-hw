package com.android.contacts.util;

import android.content.Context;
import android.content.Intent;
import com.android.contacts.hap.encryptcall.EncryptCallUtils;
import com.google.android.gms.R;

public class HwCustEncryptCallUtilsImpl extends HwCustEncryptCallUtils {
    public void init(Context context) {
        EncryptCallUtils.init(context);
    }

    public boolean isEncryptCallEnable() {
        return EncryptCallUtils.isEncryptCallEnable();
    }

    public boolean isEncryptCallEnable(Context context) {
        return EncryptCallUtils.isEncryptCallEnable(context);
    }

    public boolean isCallCard1Encrypt() {
        return EncryptCallUtils.isCallCard1Encrypt();
    }

    public boolean isCallCard2Encrypt() {
        return EncryptCallUtils.isCallCard2Encrypt();
    }

    public void buildEncryptIntent(Intent intent) {
        EncryptCallUtils.buildEncryptIntent(intent);
    }

    public int getCallDetailHistoryItemLayout(int resoureID) {
        if (isEncryptCallEnable()) {
            return R.layout.call_detail_history_item_encrypt;
        }
        return resoureID;
    }

    public int getCalllogMultiSelectItemLayout(int resourceID) {
        if (isEncryptCallEnable()) {
            return R.layout.calllog_multi_select_item_time_axis_encrypt;
        }
        return resourceID;
    }

    public int getCallLogLisItemLayout(int resourceID) {
        if (isEncryptCallEnable()) {
            return R.layout.call_log_list_item_time_axis_encrypt;
        }
        return resourceID;
    }
}
