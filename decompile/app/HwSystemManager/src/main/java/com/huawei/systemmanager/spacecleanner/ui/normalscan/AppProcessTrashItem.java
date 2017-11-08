package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppProcessTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class AppProcessTrashItem extends CommonTrashItem<AppProcessTrash> implements IAppInfo {
    private static final String TAG = "AppProcessTrashItem";
    public static final TrashTransFunc<AppProcessTrashItem> sTransFunc = new TrashTransFunc<AppProcessTrashItem>() {
        public AppProcessTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppProcessTrashItem trans input is null!");
                return null;
            } else if (input instanceof AppProcessTrash) {
                return new AppProcessTrashItem((AppProcessTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "AppProcessTrashItem trans type error,orgin type is:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 32768;
        }
    };
    private boolean mProtect;

    public AppProcessTrashItem(AppProcessTrash trash) {
        super(trash);
        this.mProtect = trash.isProtect();
    }

    public String getName() {
        return ((AppProcessTrash) this.mTrash).getAppLabel();
    }

    public boolean isProtect() {
        return this.mProtect;
    }

    public void doSetItemProtect(boolean protect) {
        this.mProtect = protect;
        setChecked(!this.mProtect);
        ((AppProcessTrash) this.mTrash).setProtect(this.mProtect);
    }

    public String getPkgName() {
        return ((AppProcessTrash) this.mTrash).getPackageName();
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public Drawable getItemIcon() {
        Drawable icon = null;
        if (this.mTrash != null) {
            icon = ((AppProcessTrash) this.mTrash).getAppIcon();
        }
        if (icon == null) {
            return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storgemanager_acclelerator);
        }
        return icon;
    }

    public boolean checkAliveStateChanged() {
        ((AppProcessTrash) this.mTrash).checkAlive();
        if (!((AppProcessTrash) this.mTrash).isCleaned() || isCleaned()) {
            return false;
        }
        setCleaned();
        return true;
    }

    public boolean hasSecondary() {
        return true;
    }

    public void refreshProtectState() {
        ((AppProcessTrash) this.mTrash).refreshProtectState();
        if (((AppProcessTrash) this.mTrash).isCleaned()) {
            setCleaned();
            return;
        }
        boolean protectState = ((AppProcessTrash) this.mTrash).isProtect();
        if ((this.mProtect ^ protectState) != 0) {
            boolean z;
            HwLog.i(TAG, "refreshProtectState, protect state changed, pkg:" + getPkgName());
            this.mProtect = protectState;
            if (this.mProtect) {
                z = false;
            } else {
                z = true;
            }
            setChecked(z);
        }
    }

    public String getPackageName() {
        return ((AppProcessTrash) this.mTrash).getPackageName();
    }

    public String getAppLabel() {
        return ((AppProcessTrash) this.mTrash).getAppLabel();
    }

    public Drawable getAppIcon() {
        return ((AppProcessTrash) this.mTrash).getAppIcon();
    }
}
