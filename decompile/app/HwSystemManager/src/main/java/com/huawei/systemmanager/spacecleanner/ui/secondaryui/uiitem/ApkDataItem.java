package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class ApkDataItem extends CommonTrashItem<AppDataTrash> {
    public static final TrashTransFunc<ApkDataItem> sTransFunc = new TrashTransFunc<ApkDataItem>() {
        public ApkDataItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "ApkDataItem trans, input is null!");
                return null;
            } else if (input instanceof AppDataTrash) {
                return new ApkDataItem((AppDataTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "ApkDataItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 262144;
        }
    };

    public ApkDataItem(AppDataTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize());
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
    }

    public boolean isUseIconAlways() {
        return false;
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public String getName() {
        return this.mTrash == null ? null : ((AppDataTrash) this.mTrash).getName();
    }

    public int getIconWidth(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_width);
    }

    public int getIconHeight(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_height);
    }
}
