package com.huawei.systemmanager.rainbow.comm.crossutil;

public class NetworkDataCvt {
    public static int wifiCfgType(int networkCfgValue) {
        if ((networkCfgValue & 16384) == 0) {
            return 0;
        }
        return 1;
    }

    public static int mobileCfgType(int networkCfgValue) {
        if ((networkCfgValue & 8192) == 0) {
            return 0;
        }
        return 1;
    }
}
