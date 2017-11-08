package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class ApkTrashItem extends FileTrashItem<ApkFileTrash> {
    public static final TrashTransFunc<ApkTrashItem> sTransFunc = new TrashTransFunc<ApkTrashItem>() {
        public ApkTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, input is null!");
                return null;
            } else if (input instanceof ApkFileTrash) {
                return new ApkTrashItem((ApkFileTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "VideoTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 1024;
        }
    };

    public ApkTrashItem(ApkFileTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        int version = ((ApkFileTrash) this.mTrash).getVersionCode();
        int installedVersion = ((ApkFileTrash) this.mTrash).getInstalledVersionCode();
        String newOrOldTag = "";
        if (installedVersion == Integer.MIN_VALUE) {
            newOrOldTag = "";
        } else if (installedVersion > version) {
            newOrOldTag = ctx.getString(R.string.space_cleann_apk_old_version);
        } else if (installedVersion != version) {
            newOrOldTag = ctx.getString(R.string.space_cleann_apk_new_version);
        }
        if (TextUtils.isEmpty(newOrOldTag)) {
            return FileUtil.getFileSize(getTrashSize());
        }
        return FileUtil.getFileSize(getTrashSize()) + "  " + newOrOldTag;
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
    }

    public boolean isUseIconAlways() {
        return true;
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public String getName() {
        return FileUtil.getFileName(((ApkFileTrash) this.mTrash).getPath());
    }

    public int getIconWidth(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_width);
    }

    public int getIconHeight(Context ctx) {
        return ctx.getResources().getDimensionPixelSize(R.dimen.spaceclean_load_apk_icon_height);
    }
}
