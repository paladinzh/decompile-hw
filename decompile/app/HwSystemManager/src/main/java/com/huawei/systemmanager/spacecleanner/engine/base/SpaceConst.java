package com.huawei.systemmanager.spacecleanner.engine.base;

import com.huawei.systemmanager.comm.concurrent.ExecutorUtil;
import java.util.concurrent.ExecutorService;

public class SpaceConst {
    public static final int FILE_ANALYSIS_FREE_PERCENT_VALUE = 50;
    public static final boolean FILE_ANALYSIS_ONLY_SUPPORT_INTERNAL_CARD = true;
    public static final long FILE_ANALYSIS_VALIDITY_PERIOD_DAYS = 1825;
    public static final long FILE_ANALYSIS_VALIDITY_PERIOD_TIME = 157680000000L;
    public static final long LARGE_FILE_EXCEED_INTERVAL_TIME = 2592000000L;
    public static final long LARGE_FILE_SIZE = 10485760;
    public static final int SCANNER_TYPE_ALL = Integer.MAX_VALUE;
    public static final int SCANNER_TYPE_DEEP = 100;
    public static final int SCANNER_TYPE_NORMAL = 50;
    public static final int SCAN_TYPE_BLUR_PHOTO = 56;
    public static final int SCAN_TYPE_HW_APKCACHE = 1;
    public static final int SCAN_TYPE_HW_APKFILE = 2;
    public static final int SCAN_TYPE_HW_CUSTOM = 3;
    public static final int SCAN_TYPE_HW_DEEP = 52;
    public static final int SCAN_TYPE_HW_MEDIA = 53;
    public static final int SCAN_TYPE_HW_UNUSED_APP = 51;
    public static final int SCAN_TYPE_PROCESS = 6;
    public static final int SCAN_TYPE_QUICK_APP = 5;
    public static final int SCAN_TYPE_SIMILAR_PHOTO = 55;
    public static final int SCAN_TYPE_TENCENT = 4;
    public static final int SCAN_TYPE_TENCENT_WECHAT = 54;
    public static final String TAG = "SpaceClean";
    public static final int UNUSED_APP_EXCEED_INTERVAL_DAYS = 90;
    public static final long UNUSED_APP_EXCEED_INTERVAL_TIME = 7776000000L;
    public static final int UNUSED_LARGE_FILE_EXCEED_INTERVAL_DAYS = 30;
    public static final ExecutorService sExecutor = ExecutorUtil.createNormalExecutor("SpaceCleanExecutor", true);
}
