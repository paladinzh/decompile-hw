package com.huawei.systemmanager.comm.component;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.component.Item.SimpleItem;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import libcore.util.Objects;

public class AppItem extends SimpleItem {
    private HsmPkgInfo mPkgInfo;

    protected AppItem() {
    }

    protected AppItem(HsmPkgInfo info) {
        this.mPkgInfo = info;
    }

    public String getName() {
        return getLabel();
    }

    public String getPackageName() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.mPkgName;
    }

    public Drawable getIcon() {
        if (this.mPkgInfo == null) {
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
        return this.mPkgInfo.icon();
    }

    public int getUid() {
        if (this.mPkgInfo == null) {
            return -1;
        }
        return this.mPkgInfo.mUid;
    }

    public boolean isPersistent() {
        if (this.mPkgInfo == null) {
            return false;
        }
        return this.mPkgInfo.isPersistent();
    }

    public String getLabel() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.label();
    }

    public HsmPkgInfo getPkgInfo() {
        return this.mPkgInfo;
    }

    public static boolean checkPkgEquals(AppItem l, AppItem r) {
        if (l == null || r == null) {
            return false;
        }
        return Objects.equal(l.getPackageName(), r.getPackageName());
    }
}
