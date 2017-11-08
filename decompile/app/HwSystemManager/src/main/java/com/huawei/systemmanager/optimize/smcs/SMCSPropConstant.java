package com.huawei.systemmanager.optimize.smcs;

import android.os.SystemProperties;

final class SMCSPropConstant {
    static final String RO_SMCS_DB_DEBUG_ENABLE = "ro.enable.st_db_debug";
    static final String RO_SMCS_DEBUG_ENABLE = "ro.enable.st_debug";
    static final boolean localDBLOGV = SystemProperties.getBoolean(RO_SMCS_DB_DEBUG_ENABLE, false);
    static final boolean localLOGV = SystemProperties.getBoolean(RO_SMCS_DEBUG_ENABLE, false);

    SMCSPropConstant() {
    }
}
