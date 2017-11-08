package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCacheTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class AppCacheTrashItem extends CommonTrashItem<AppCacheTrash> implements IAppInfo {
    public static final TrashTransFunc<AppCacheTrashItem> sTransFunc = new TrashTransFunc<AppCacheTrashItem>() {
        public AppCacheTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppCacheTrashItem trans input is null!");
                return null;
            } else if (input instanceof AppCacheTrash) {
                return new AppCacheTrashItem((AppCacheTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "AppCacheTrashItem trans type error,orgin type is:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 1;
        }
    };

    public AppCacheTrashItem(AppCacheTrash trash) {
        super(trash);
    }

    public String getName() {
        return ((AppCacheTrash) this.mTrash).getAppLabel();
    }

    public Drawable getItemIcon() {
        if (this.mTrash != null) {
            return ((AppCacheTrash) this.mTrash).getAppIcon();
        }
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storgemanager_cachetrash);
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public String getPackageName() {
        return ((AppCacheTrash) this.mTrash).getPackageName();
    }

    public String getAppLabel() {
        return ((AppCacheTrash) this.mTrash).getAppLabel();
    }

    public Drawable getAppIcon() {
        return ((AppCacheTrash) this.mTrash).getAppIcon();
    }
}
