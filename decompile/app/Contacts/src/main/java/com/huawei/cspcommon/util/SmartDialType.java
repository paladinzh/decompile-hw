package com.huawei.cspcommon.util;

public class SmartDialType extends SortUtils {
    private static int IS_CLOUD_MARK = 14;
    private static int MARK_CONTENT = 13;
    private static int MARK_COUNT = 15;
    private static int MARK_TYPE = 12;
    static final String[] PROJECTION_DATA_SMART_DIAL_PRIVATE;

    static {
        if (CommonConstants.isPrivacyFeatureEnabled()) {
            int smartDialProjectionLen = SearchContract$ContactQuery.SMART_DIAL_SEARCH_PROJECTION.length;
            PROJECTION_DATA_SMART_DIAL_PRIVATE = new String[(smartDialProjectionLen + 1)];
            System.arraycopy(SearchContract$ContactQuery.SMART_DIAL_SEARCH_PROJECTION, 0, PROJECTION_DATA_SMART_DIAL_PRIVATE, 0, smartDialProjectionLen);
            PROJECTION_DATA_SMART_DIAL_PRIVATE[smartDialProjectionLen] = "is_private";
            return;
        }
        PROJECTION_DATA_SMART_DIAL_PRIVATE = SearchContract$ContactQuery.SMART_DIAL_SEARCH_PROJECTION;
    }

    public static int getMarkTypeColumnIndex() {
        return MARK_TYPE;
    }

    public static int getMarkContentColumnIndex() {
        return MARK_CONTENT;
    }

    public static int getIsCloudMarkColumnIndex() {
        return IS_CLOUD_MARK;
    }

    public static int getMarkCountColumnIndex() {
        return MARK_COUNT;
    }

    public static void setMarkTypeColumnIndex(int index) {
        MARK_TYPE = index;
    }

    public static void setMarkContentColumnIndex(int index) {
        MARK_CONTENT = index;
    }

    public static void setIsCloudMarkColumnIndex(int index) {
        IS_CLOUD_MARK = index;
    }

    public static void setMarkCountColumnIndex(int index) {
        MARK_COUNT = index;
    }

    public static String[] getProjection() {
        String[] lSourceProjection;
        if (CommonConstants.isPrivacyFeatureEnabled()) {
            lSourceProjection = PROJECTION_DATA_SMART_DIAL_PRIVATE;
        } else {
            lSourceProjection = SearchContract$ContactQuery.SMART_DIAL_SEARCH_PROJECTION;
        }
        int lSourceProjectionLength = lSourceProjection.length;
        String[] projection = new String[lSourceProjectionLength];
        System.arraycopy(lSourceProjection, 0, projection, 0, lSourceProjectionLength);
        return projection;
    }
}
