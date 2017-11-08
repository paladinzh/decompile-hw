package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.io.IOException;
import java.util.List;

public class TencenTopVideoAppGroup extends AppCustomTrash {
    private String mAppName;
    private String mPkgName;

    public TencenTopVideoAppGroup(int type, String pkg, String appName, boolean suggestClean) {
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
        return null;
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
        TencenTopVideoAppGroup result = new TencenTopVideoAppGroup(getType(), this.mPkgName, this.mAppName, true);
        result.addChildList(normalTrash);
        return result;
    }
}
