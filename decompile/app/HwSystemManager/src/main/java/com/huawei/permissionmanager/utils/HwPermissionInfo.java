package com.huawei.permissionmanager.utils;

public class HwPermissionInfo {
    static final /* synthetic */ boolean -assertionsDisabled = (!HwPermissionInfo.class.desiredAssertionStatus());
    public int mIndex;
    public int mPermissionCode;
    public String[] mPermissionStr = null;
    public boolean misUnit;

    public HwPermissionInfo(int indexCode, int permissionCode, String[] permissionStr, boolean isUnit) {
        this.mIndex = indexCode;
        this.mPermissionCode = permissionCode;
        this.mPermissionStr = (String[]) permissionStr.clone();
        this.misUnit = isUnit;
    }

    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj.getClass())) {
            return false;
        }
        if (this.mPermissionCode == ((HwPermissionInfo) obj).mPermissionCode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }
}
