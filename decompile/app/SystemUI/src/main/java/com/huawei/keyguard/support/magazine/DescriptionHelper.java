package com.huawei.keyguard.support.magazine;

import android.text.TextUtils;
import com.huawei.keyguard.util.HwLog;
import java.nio.charset.Charset;

public class DescriptionHelper {
    private static byte ASCII_QUESTION = (byte) 63;
    private static int MAX_MESSY_NUM = 12;

    public static boolean isMessyDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return false;
        }
        int messyNum = 0;
        for (byte b : description.getBytes(Charset.forName("UTF-8"))) {
            if (ASCII_QUESTION == b) {
                messyNum++;
                if (messyNum > MAX_MESSY_NUM) {
                    HwLog.i("DescriptionHelper", "messy description, description = " + description);
                    return true;
                }
            }
        }
        return false;
    }
}
