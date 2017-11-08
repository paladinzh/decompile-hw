package com.android.mms.transaction;

import android.content.Context;
import android.os.SystemProperties;
import com.android.mms.MmsConfig;
import com.huawei.cspcommon.MLog;
import java.util.Arrays;

public class DefaultRetryScheme extends AbstractRetryScheme {
    private static final boolean isCustomizedRetry;
    private static final int[] sCTDefaultRetryScheme = new int[]{0, 300000, 300000, 300000};
    private static final int[] sCustomizedRetryScheme;
    private static final int[] sDefaultRetryScheme = new int[]{0, 60000, 300000, 600000, 1800000};
    private int iCustomizeIndex = 0;

    static {
        String[] values = SystemProperties.get("ro.config.customized_mms_retry").split(",");
        if (values.length != 3) {
            isCustomizedRetry = false;
            sCustomizedRetryScheme = sCTDefaultRetryScheme;
        } else {
            isCustomizedRetry = true;
            sCustomizedRetryScheme = new int[4];
            sCustomizedRetryScheme[0] = 0;
            sCustomizedRetryScheme[1] = (Integer.parseInt(values[0]) * 60) * 1000;
            sCustomizedRetryScheme[2] = (Integer.parseInt(values[1]) * 60) * 1000;
            sCustomizedRetryScheme[3] = (Integer.parseInt(values[2]) * 60) * 1000;
        }
        MLog.d("Mms_TXM_RScheme", "isCustomizedRetry: " + isCustomizedRetry + " CustomizedRetryScheme: " + Arrays.toString(sCustomizedRetryScheme));
    }

    public DefaultRetryScheme(Context context, int retriedTimes) {
        boolean isHWRetry;
        super(retriedTimes);
        this.mRetriedTimes = this.mRetriedTimes < 0 ? 0 : this.mRetriedTimes;
        if (MmsConfig.getModifyMmsRetryScheme() == 1) {
            isHWRetry = true;
        } else {
            isHWRetry = false;
        }
        if (isHWRetry) {
            this.iCustomizeIndex = 1;
        }
        if (1 != this.iCustomizeIndex) {
            this.mRetriedTimes = this.mRetriedTimes >= sDefaultRetryScheme.length ? sDefaultRetryScheme.length - 1 : this.mRetriedTimes;
        } else if (isCustomizedRetry) {
            this.mRetriedTimes = this.mRetriedTimes >= sCustomizedRetryScheme.length ? sCustomizedRetryScheme.length - 1 : this.mRetriedTimes;
        } else {
            this.mRetriedTimes = this.mRetriedTimes >= sCTDefaultRetryScheme.length ? sCTDefaultRetryScheme.length - 1 : this.mRetriedTimes;
        }
    }

    public int getRetryLimit() {
        if (1 != this.iCustomizeIndex) {
            return sDefaultRetryScheme.length;
        }
        if (isCustomizedRetry) {
            return sCustomizedRetryScheme.length;
        }
        return sCTDefaultRetryScheme.length;
    }

    public long getWaitingInterval() {
        if (1 != this.iCustomizeIndex) {
            return (long) sDefaultRetryScheme[this.mRetriedTimes];
        }
        if (isCustomizedRetry) {
            return (long) sCustomizedRetryScheme[this.mRetriedTimes];
        }
        return (long) sCTDefaultRetryScheme[this.mRetriedTimes];
    }
}
