package com.huawei.internal.telephony.gsm;

import android.os.SystemProperties;
import java.util.Arrays;

public class HwCustGsmCellBroadcastHandler {
    private static final String FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG = "2B";
    private static final boolean IS_CBSPDU_HANDLER_NULL_MSG = SystemProperties.getBoolean("ro.config.cbs_del_2B", false);

    public byte[] cbsPduAfterDiscardNullBlock(byte[] receivedPdu) {
        if (IS_CBSPDU_HANDLER_NULL_MSG) {
            int cbsPduLength = receivedPdu.length;
            if (cbsPduLength > 0) {
                StringBuilder sb = new StringBuilder();
                for (int j = cbsPduLength - 1; j > 0; j--) {
                    int b = receivedPdu[j] & 255;
                    if (b < 16) {
                        sb.append('0');
                    }
                    sb.append(Integer.toHexString(b));
                    if (!sb.toString().equalsIgnoreCase(FILLED_STRING_WHEN_BLOCK_IS_NULL_MSG)) {
                        break;
                    }
                    cbsPduLength--;
                    sb.delete(0, sb.length());
                }
                return Arrays.copyOf(receivedPdu, cbsPduLength);
            }
        }
        return receivedPdu;
    }
}
