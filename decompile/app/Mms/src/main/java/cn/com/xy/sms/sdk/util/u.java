package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;

/* compiled from: Unknown */
public final class u {
    private static int a = 0;
    private static int b = 8;
    private static int c = 256;
    private static int d = 7;
    private static int e = 128;
    private static int f = 6;
    private static int g = 64;
    private static int h = 5;
    private static int i = 32;
    private static int j = 4;
    private static int k = 16;
    private static int l = 3;
    private static int m = 8;
    private static int n = 2;
    private static int o = 4;
    private static int p = 1;
    private static int q = 2;

    private static int a(int i, int i2) {
        return (Integer.parseInt(SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RECORD_FUNCTION_STATE)) & i) >> i2;
    }

    public static String a() {
        if (a == 0) {
            String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RECORD_FUNCTION_STATE);
            if (stringParam != null) {
                a = Integer.parseInt(stringParam);
            }
        }
        return String.valueOf(a);
    }

    public static void a(int i) {
        int i2 = 0;
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RECORD_FUNCTION_STATE);
        if (stringParam != null) {
            i2 = Integer.parseInt(stringParam);
        }
        int i3 = i2 | i;
        a = i3;
        if (i3 != i2) {
            SysParamEntityManager.setParam(Constant.RECORD_FUNCTION_STATE, String.valueOf(i3));
        }
    }

    private static void b(int i) {
        SysParamEntityManager.setParam(Constant.RECORD_FUNCTION_STATE, String.valueOf(Integer.parseInt(SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RECORD_FUNCTION_STATE)) & (i ^ -1)));
    }

    private static Boolean c(int i) {
        return (Integer.parseInt(SysParamEntityManager.getStringParam(Constant.getContext(), Constant.RECORD_FUNCTION_STATE)) & i) != i ? Boolean.valueOf(false) : Boolean.valueOf(true);
    }
}
