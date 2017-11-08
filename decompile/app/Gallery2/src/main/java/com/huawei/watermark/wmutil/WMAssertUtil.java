package com.huawei.watermark.wmutil;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class WMAssertUtil {
    @SuppressWarnings({"NM_METHOD_NAMING_CONVENTION"})
    public static void Assert(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }
}
