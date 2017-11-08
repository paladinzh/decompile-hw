package com.huawei.systemmanager.netassistant.traffic.appdetail;

public class Constant {
    public static final String EXTRA_ACTIVITY_FROM = "ext_from";
    public static final String EXTRA_APP_LABEL = "ext_label";
    public static final String EXTRA_IMSI = "ext_imsi";
    public static final String EXTRA_PACKAGE_NAME = "package";
    public static final String EXTRA_SUBID = "ext_subid";
    public static final String EXTRA_TYPE = "ext_type";
    public static final String EXTRA_UID = "uid";
    public static final int FROM_4G_ACTIVITY = 1;
    public static final int FROM_NORMAL_ACTIVITY = 0;
    static final int[] PERIOD_ARRAY = new int[]{0, 1, 2, 3};
    private static final int PERIOD_DAILY_4G = 3;
    private static final int PERIOD_DAILY_MOBILE = 1;
    private static final int PERIOD_MONTHLY_4G = 2;
    private static final int PERIOD_MONTHLY_MOBILE = 0;
}
