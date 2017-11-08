package com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwCustomDataItemTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import java.io.IOException;

public class HwCustomAppDataGroup extends AppCustomTrash {
    private static final String TAG = "HwCustomDataGroupGroup";
    private String mAppName;
    private Drawable mIcon;
    private String mPkgName;

    public HwCustomAppDataGroup(int type, String pkg, String appName, boolean suggestClean) {
        super(type, suggestClean);
        this.mPkgName = pkg;
        this.mAppName = appName;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public Drawable getAppIcon() {
        if (this.mIcon != null) {
            return this.mIcon;
        }
        this.mIcon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(this.mPkgName));
        return this.mIcon;
    }

    public String getAppLabel() {
        return this.mAppName;
    }

    public boolean clean(Context context) {
        return super.clean(context);
    }

    public boolean addChild(Trash trash) {
        if (trash == null) {
            throw new IllegalArgumentException();
        } else if (trash.getType() != getType() || !(trash instanceof HwCustomDataItemTrash)) {
            return false;
        } else {
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
        return null;
    }
}
