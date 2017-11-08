package com.huawei.openalliance.ad.a.b;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class a {
    public static final a a = a.FORMAL;
    public static String b = "https://sdkserver.op.hicloud.com/sdkserver/query";
    public static String c = "https://acd.op.hicloud.com/result.ad";
    public static String d = "https://events.op.hicloud.com/contserver/newcontent/action";
    public static final List<String> e = Collections.unmodifiableList(Arrays.asList(g));
    public static final List<Integer> f = Collections.unmodifiableList(Arrays.asList(h));
    private static final String[] g = new String[]{"android.permission.READ_PHONE_STATE"};
    private static final Integer[] h = new Integer[]{Integer.valueOf(801)};

    /* compiled from: Unknown */
    public enum a {
        DEV,
        TEST,
        MIRROR,
        FORMAL
    }
}
