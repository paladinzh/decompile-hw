package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.util.SparseIntArray;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class BlockReasonDescrp {
    private static final String TAG = "BlockReasonDescrp";
    private static final SparseIntArray sCallReasonDes = new SparseIntArray();
    private static final SparseIntArray sMsgReasonDes = new SparseIntArray();

    public static class CallBlockReasonStr {
        private final String mMarkStr;
        private final String mReason;

        public CallBlockReasonStr(String reason, String markStr) {
            this.mReason = reason;
            this.mMarkStr = markStr;
        }

        public String getReasonStr() {
            return this.mReason;
        }

        public String getMarkStr() {
            return this.mMarkStr;
        }
    }

    static {
        sMsgReasonDes.put(1, R.string.harassment_blockreason_blacklistnumber);
        sMsgReasonDes.put(5, R.string.harassment_blockreason_keywords);
        sMsgReasonDes.put(6, R.string.harassment_blockreason_spammessage);
        sMsgReasonDes.put(2, R.string.harassment_unknown_phonenumber);
        sMsgReasonDes.put(3, R.string.harassment_blockreason_strangenumber);
        sMsgReasonDes.put(100, R.string.interception_rule_blockall_title);
        sCallReasonDes.put(1, R.string.harassment_blockreason_blacklistnumber);
        sCallReasonDes.put(2, R.string.harassment_unknown_phonenumber);
        sCallReasonDes.put(3, R.string.harassment_blockreason_strangenumber);
        sCallReasonDes.put(100, R.string.interception_rule_blockall_title);
        sCallReasonDes.put(21, R.string.harassment_harassing_calls_reason);
        sCallReasonDes.put(22, R.string.harassment_scam_calls_reason);
        sCallReasonDes.put(23, R.string.harassment_advertising_calls_reason);
        sCallReasonDes.put(24, R.string.harassment_real_estate_calls_reason);
    }

    public static String getMessageBlockreasonStr(Context ctx, int reason) {
        if (reason == 0) {
            return "";
        }
        int resId = sMsgReasonDes.get(reason, -1);
        if (resId >= 0) {
            return ctx.getString(resId);
        }
        HwLog.i(TAG, "getMessageBlockreasonStr unkonw reason:" + reason);
        return "";
    }

    public static CallBlockReasonStr getCallBlockreasonStr(Context ctx, BlockReason blockReason) {
        if (blockReason == null || blockReason.getReason() == 0) {
            return new CallBlockReasonStr("", "");
        }
        int reasonResId = sCallReasonDes.get(blockReason.getReason(), -1);
        if (reasonResId < 0) {
            HwLog.i(TAG, "getMessageBlockreasonStr unkonw reason:" + blockReason.getReason());
            return new CallBlockReasonStr("", "");
        }
        String reasonStr = ctx.getString(reasonResId);
        int type = blockReason.getType();
        if (type == 1 && blockReason.getReason() == 2) {
            return new CallBlockReasonStr("", "");
        }
        if (type == 1) {
            return new CallBlockReasonStr(reasonStr, "");
        }
        if (type == 3) {
            return new CallBlockReasonStr(reasonStr, ctx.getString(R.string.harassment_blockreason_mark_by_user));
        }
        if (type == 2) {
            int markCount = blockReason.getMarkCount();
            return new CallBlockReasonStr(reasonStr, ctx.getResources().getQuantityString(R.plurals.harassment_blockreason_mark_by_other, markCount, new Object[]{Integer.valueOf(markCount)}));
        }
        HwLog.e(TAG, "getCallBlockreasonStr unknown type:" + type);
        return new CallBlockReasonStr("", "");
    }
}
