package com.android.server.pm;

import android.util.SparseArray;

public class ProtectedPackages {
    private String mDeviceOwnerPackage;
    private int mDeviceOwnerUserId;
    private final Object mLock = new Object();
    private SparseArray<String> mProfileOwnerPackages;

    public void setDeviceAndProfileOwnerPackages(int deviceOwnerUserId, String deviceOwnerPackage, SparseArray<String> profileOwnerPackages) {
        SparseArray sparseArray = null;
        synchronized (this.mLock) {
            this.mDeviceOwnerUserId = deviceOwnerUserId;
            if (deviceOwnerUserId == -10000) {
                deviceOwnerPackage = null;
            }
            this.mDeviceOwnerPackage = deviceOwnerPackage;
            if (profileOwnerPackages != null) {
                sparseArray = profileOwnerPackages.clone();
            }
            this.mProfileOwnerPackages = sparseArray;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasDeviceOwnerOrProfileOwner(int userId, String packageName) {
        if (packageName == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mDeviceOwnerPackage != null && this.mDeviceOwnerUserId == userId && packageName.equals(this.mDeviceOwnerPackage)) {
                return true;
            } else if (this.mProfileOwnerPackages == null || !packageName.equals(this.mProfileOwnerPackages.get(userId))) {
            } else {
                return true;
            }
        }
    }

    public boolean canPackageStateBeChanged(int userId, String packageName) {
        return hasDeviceOwnerOrProfileOwner(userId, packageName);
    }

    public boolean canPackageBeWiped(int userId, String packageName) {
        return hasDeviceOwnerOrProfileOwner(userId, packageName);
    }
}
