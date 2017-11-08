package com.android.rcs;

import com.autonavi.amap.mapcore.VTMCDataCache;

public class RcsMmsRcsConfig {
    private static int mDefaultIMMessagesPerThread = VTMCDataCache.MAXSIZE;
    private static boolean mFileTransferCapability = false;
    private static boolean mLocationSharingCapability = false;

    public boolean getFileTransferCapability() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return mFileTransferCapability;
        }
        return false;
    }

    public void setFileTransferCapability(boolean flag) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            setFTCapability(flag);
        }
    }

    private static void setFTCapability(boolean status) {
        mFileTransferCapability = status;
    }

    public void setLocationSharingCapability(boolean flag) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            setLocShareCapability(flag);
        }
    }

    private static void setLocShareCapability(boolean status) {
        mLocationSharingCapability = status;
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }
}
