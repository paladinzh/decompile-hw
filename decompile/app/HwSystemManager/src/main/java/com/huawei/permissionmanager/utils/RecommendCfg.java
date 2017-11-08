package com.huawei.permissionmanager.utils;

public class RecommendCfg {
    public static int getCfgFromRecommendVaule(int recommendStatus) {
        switch (recommendStatus) {
            case 0:
                return 1;
            case 1:
                return 2;
            default:
                return 0;
        }
    }
}
