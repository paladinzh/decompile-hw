package com.android.mms;

import com.autonavi.amap.mapcore.VTMCDataCache;

public class HwCustMmsRcsConfigImpl extends HwCustMmsRcsConfig {
    protected static final String TAG = "HwCustMmsRcsConfigImpl";
    private static int mDefaultIMMessagesPerThread = VTMCDataCache.MAXSIZE;
    private static boolean mFileTransferCapability = false;
    private static boolean mLocationSharingCapability = false;

    public int getDefaultIMMessagesPerThread() {
        if (HwCustCommonConfig.isRCSSwitchOn()) {
            return mDefaultIMMessagesPerThread;
        }
        return 0;
    }

    public boolean getFileTransferCapability() {
        if (HwCustCommonConfig.isRCSSwitchOn()) {
            return mFileTransferCapability;
        }
        return false;
    }

    public void setFileTransferCapability(boolean flag) {
        if (HwCustCommonConfig.isRCSSwitchOn()) {
            setFTCapability(flag);
        }
    }

    private static void setFTCapability(boolean status) {
        mFileTransferCapability = status;
    }

    public boolean getLocationSharingCapability() {
        if (HwCustCommonConfig.isRCSSwitchOn()) {
            return mLocationSharingCapability;
        }
        return false;
    }

    public void setLocationSharingCapability(boolean flag) {
        if (HwCustCommonConfig.isRCSSwitchOn()) {
            setLocShareCapability(flag);
        }
    }

    private static void setLocShareCapability(boolean status) {
        mLocationSharingCapability = status;
    }

    public boolean isRcsSwitchOn() {
        return HwCustCommonConfig.isRCSSwitchOn();
    }
}
