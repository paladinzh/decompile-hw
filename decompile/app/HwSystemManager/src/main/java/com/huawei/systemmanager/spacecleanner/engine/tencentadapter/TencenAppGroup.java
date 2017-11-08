package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import java.io.IOException;
import java.util.List;

public class TencenAppGroup extends AppCustomTrash {
    private String mAppName;
    private Drawable mIcon;
    private String mPkgName;

    public TencenAppGroup(int type, String pkg, String appName, boolean suggestClean) {
        super(type, suggestClean);
        this.mPkgName = pkg;
        this.mAppName = appName;
    }

    public String getAppLabel() {
        return this.mAppName;
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
        TencenAppGroup result = new TencenAppGroup(getType(), this.mPkgName, this.mAppName, true);
        result.addChildList(normalTrash);
        return result;
    }
}
