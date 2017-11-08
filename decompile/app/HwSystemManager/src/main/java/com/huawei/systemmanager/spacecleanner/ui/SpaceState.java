package com.huawei.systemmanager.spacecleanner.ui;

public enum SpaceState {
    NORMAL_SCANNING,
    NORMAL_SCAN_END,
    NORMAL_CLEANNING,
    NORMAL_CLEAN_END,
    STATE_NULL;

    public boolean isScanning() {
        return this == NORMAL_SCANNING;
    }

    public boolean isScanEnd() {
        return this == NORMAL_SCAN_END;
    }

    public boolean isCleanning() {
        return this == NORMAL_CLEANNING;
    }

    public boolean isCleanEnd() {
        return this == NORMAL_CLEAN_END;
    }

    public boolean isEnd() {
        if (this == NORMAL_SCAN_END || this == NORMAL_CLEAN_END) {
            return true;
        }
        return false;
    }

    public boolean isNormal() {
        if (this == NORMAL_SCANNING || this == NORMAL_SCAN_END || this == NORMAL_CLEANNING || this == NORMAL_CLEAN_END) {
            return true;
        }
        return false;
    }
}
