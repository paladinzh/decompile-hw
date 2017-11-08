package com.huawei.systemmanager.rainbow.comm.crossutil.cvtmeta;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class PermissionCvtData {
    private static final String TAG = PermissionCvtData.class.getSimpleName();
    private Set<String> mAndroidPermission = Sets.newHashSet();
    public int mItemId;
    public int mPermissionMask;
    public int mVerifyPower;

    public PermissionCvtData(int itemId, int mask, int power) {
        this.mItemId = itemId;
        this.mPermissionMask = mask;
        this.mVerifyPower = power;
    }

    public void appendPermission(String permissionName) {
        if (!Strings.isNullOrEmpty(permissionName)) {
            this.mAndroidPermission.add(permissionName);
        }
    }

    public boolean subOfPermissionSet(Set<String> applyList) {
        for (String per : this.mAndroidPermission) {
            if (applyList.contains(per)) {
                return true;
            }
        }
        return false;
    }

    public boolean valid() {
        int mask = 1 << this.mVerifyPower;
        if (this.mPermissionMask != mask) {
            HwLog.e(TAG, "valid failed of mask: " + mask + ", mPermissionMask: " + this.mPermissionMask + ", power: " + this.mVerifyPower);
            return false;
        } else if (!this.mAndroidPermission.isEmpty()) {
            return true;
        } else {
            HwLog.e(TAG, "valid failed of empty mAndroidPermission");
            return false;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append("PermissionCvtData of ItemId: " + this.mItemId + " is " + (valid() ? "" : "in") + "valid");
        buf.append("}");
        return buf.toString();
    }
}
