package com.huawei.systemmanager.mainscreen.normal;

import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;

public class DetectItemEx {
    public static final int TYPE_BATTERY = 3;
    public static final int TYPE_MANAGEMENT = 4;
    public static final int TYPE_PERFORMANCE = 1;
    public static final int TYPE_SECURITY = 2;
    public static final int TYPE_UNKNOW = 0;
    private DetectItem mCurrent;
    private int mType;

    public int getmType() {
        return this.mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public DetectItem getmCurrent() {
        return this.mCurrent;
    }

    public void setmCurrent(DetectItem mCurrent) {
        this.mCurrent = mCurrent;
    }
}
