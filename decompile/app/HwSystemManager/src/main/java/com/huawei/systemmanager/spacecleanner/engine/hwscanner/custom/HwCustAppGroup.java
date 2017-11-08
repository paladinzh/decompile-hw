package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppCustomDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import java.io.IOException;
import java.util.List;

public class HwCustAppGroup extends AppCustomTrash {
    private String mAppName;
    private Drawable mIcon;
    private String mPkgName;

    public HwCustAppGroup(int type, String pkg, String appName, boolean suggestClean) {
        super(type, suggestClean);
        this.mPkgName = pkg;
        this.mAppName = appName;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public String getAppLabel() {
        return this.mAppName;
    }

    public Drawable getAppIcon() {
        if (this.mIcon != null) {
            return this.mIcon;
        }
        this.mIcon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(this.mPkgName));
        return this.mIcon;
    }

    public boolean clean(Context context) {
        return super.clean(context);
    }

    public boolean addChild(Trash trash) {
        if (trash == null) {
            throw new IllegalArgumentException();
        } else if (trash.getType() != getType() || !(trash instanceof HwAppCustomDataTrash)) {
            return false;
        } else {
            HwAppCustomDataTrash detail = (HwAppCustomDataTrash) trash;
            for (Trash cust : this.mTrashList) {
                if (cust instanceof HwAppCustomDataTrash) {
                    HwAppCustomDataTrash index = (HwAppCustomDataTrash) cust;
                    if (index.isTrashEquals(detail)) {
                        index.addFolderTrash(detail);
                        return true;
                    }
                }
            }
            this.mTrashList.add(trash);
            return true;
        }
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("  ").append("appName:").append(getAppLabel()).append(",pkg:").append(getPackageName()).append("\n");
        for (Trash trash : this) {
            trash.printf(appendable);
            appendable.append("\n");
        }
        appendable.append("\n");
    }

    public AppCustomTrash splitNormalTrash() {
        List<Trash> normalTrash = getNormalChildren();
        if (normalTrash.isEmpty()) {
            return null;
        }
        HwCustAppGroup result = new HwCustAppGroup(getType(), this.mPkgName, this.mAppName, true);
        result.addChildList(normalTrash);
        return result;
    }
}
