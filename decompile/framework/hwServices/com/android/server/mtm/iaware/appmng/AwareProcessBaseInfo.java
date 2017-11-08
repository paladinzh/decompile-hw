package com.android.server.mtm.iaware.appmng;

import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class AwareProcessBaseInfo {
    public String mAdjType = AppHibernateCst.INVALID_PKG;
    public int mCurAdj = 0;
    public boolean mForegroundActivities = false;
    public boolean mHasShownUi;
    public int mUid;

    public AwareProcessBaseInfo copy() {
        AwareProcessBaseInfo dst = new AwareProcessBaseInfo();
        dst.mUid = this.mUid;
        dst.mCurAdj = this.mCurAdj;
        dst.mAdjType = this.mAdjType;
        dst.mForegroundActivities = this.mForegroundActivities;
        dst.mHasShownUi = this.mHasShownUi;
        return dst;
    }
}
