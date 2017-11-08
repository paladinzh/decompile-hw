package com.huawei.systemmanager.addviewmonitor;

import com.huawei.systemmanager.comparator.AlpComparator;

public class AddViewAppInfo {
    public static final AlpComparator<AddViewAppInfo> ADDVIEW_APP_ALP_COMPARATOR = new AlpComparator<AddViewAppInfo>() {
        public String getStringKey(AddViewAppInfo t) {
            return t.mLabel;
        }

        public int compare(AddViewAppInfo lhs, AddViewAppInfo rhs) {
            boolean isLeftAllowed = lhs.mAddViewAllow;
            if ((isLeftAllowed ^ rhs.mAddViewAllow) == 0) {
                return super.compare(lhs, rhs);
            }
            return isLeftAllowed ? -1 : 1;
        }
    };
    public static final boolean DEF_CFG = false;
    public boolean mAddViewAllow;
    public String mLabel;
    public String mPkgName;
    public int mUid;

    public AddViewAppInfo() {
        this.mPkgName = null;
        this.mLabel = null;
        this.mUid = -1;
        this.mAddViewAllow = false;
    }

    public AddViewAppInfo(AddViewAppInfo appInfo) {
        this.mPkgName = appInfo.mPkgName;
        this.mLabel = appInfo.mLabel;
        this.mUid = appInfo.mUid;
        this.mAddViewAllow = appInfo.mAddViewAllow;
    }
}
