package com.huawei.systemmanager.util.numberlocation;

import java.text.NumberFormat;

public class NumberLocationPercent {
    public static String getPercentage(double number, int decimalDigits) {
        NumberFormat pnf = NumberFormat.getPercentInstance();
        double pvalue = number / 100.0d;
        pnf.setMinimumFractionDigits(decimalDigits);
        return pnf.format(pvalue);
    }

    public static String getPercent(double number) {
        return getPercentage(number, 0);
    }
}
