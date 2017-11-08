package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.optimize.ProcessManager;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppProcessTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;

public class HwAppProcessTrash extends AppProcessTrash {
    private final ProcessAppItem mAppItem;

    private HwAppProcessTrash(ProcessAppItem item) {
        this.mAppItem = item;
    }

    public String getPackageName() {
        return this.mAppItem.getPackageName();
    }

    public String getAppLabel() {
        return this.mAppItem.getLabel();
    }

    public Drawable getAppIcon() {
        return this.mAppItem.getIcon();
    }

    public long getTrashSize() {
        return this.mAppItem.getMemoryCost();
    }

    public boolean clean(Context context) {
        ProcessManager.clearPackages(HsmCollections.newArrayList(getPackageName()));
        setCleaned();
        return true;
    }

    public boolean isSuggestClean() {
        return !this.mAppItem.isProtect();
    }

    public boolean isProtect() {
        return this.mAppItem.isProtect();
    }

    public void setProtect(boolean protect) {
        ProtectAppControl.getInstance(GlobalContext.getContext()).setProtect(this.mAppItem, protect);
    }

    public void refreshProtectState() {
        String pkgName = getPackageName();
        Boolean protectState = ProtectAppControl.getInstance(GlobalContext.getContext()).isProtect(getPackageName());
        if (protectState == null) {
            HwLog.i(Trash.TAG, "refreshProtectState, protectstate is null! pkg:" + pkgName);
            setCleaned();
            return;
        }
        this.mAppItem.setProtect(protectState.booleanValue());
    }

    public boolean checkAlive() {
        if (isCleaned()) {
            return false;
        }
        String pkg = getPackageName();
        boolean alive = ProcessFilterPolicy.queryIfAppAlive(GlobalContext.getContext(), pkg);
        HwLog.i(Trash.TAG, "checkAlive, pkg:" + pkg + ", alive:" + alive);
        if (alive) {
            return true;
        }
        setCleaned();
        return false;
    }

    public static HwAppProcessTrash create(ProcessAppItem item) {
        return new HwAppProcessTrash(item);
    }
}
