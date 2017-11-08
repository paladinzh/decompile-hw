package com.huawei.watermark.wmutil;

import java.text.NumberFormat;

public class LocalizeUtil {
    public static String getLocalizeNumber(int number) {
        return NumberFormat.getInstance().format((long) number);
    }
}
