package com.huawei.android.freeshare.client.bluetooth;

import android.net.Uri;
import android.provider.BaseColumns;
import com.huawei.gallery.app.AbsAlbumPage;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public final class BluetoothShare implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.bluetooth.opp/btopp");

    private BluetoothShare() {
    }

    public static boolean isStatusSuccess(int status) {
        return status >= SmsCheckResult.ESCT_200 && status < 300;
    }

    public static boolean isStatusCompleted(int status) {
        if (status < SmsCheckResult.ESCT_200 || status >= 300) {
            return status >= AbsAlbumPage.LAUNCH_QUIK_ACTIVITY && status < 600;
        } else {
            return true;
        }
    }
}
