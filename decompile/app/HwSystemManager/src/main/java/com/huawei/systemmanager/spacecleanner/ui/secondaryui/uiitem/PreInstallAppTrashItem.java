package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.PreInstalledAppTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;

public class PreInstallAppTrashItem extends CommonTrashItem<PreInstalledAppTrash> {
    public static final TrashTransFunc<PreInstallAppTrashItem> sTransFunc = new TrashTransFunc<PreInstallAppTrashItem>() {
        public PreInstallAppTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "PreInstallAppTrashItem trans, input is null!");
                return null;
            } else if (input instanceof PreInstalledAppTrash) {
                return new PreInstallAppTrashItem((PreInstalledAppTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "PreInstallAppTrashItem trans, trans error, origin type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 524288;
        }
    };

    public PreInstallAppTrashItem(PreInstalledAppTrash trash) {
        super(trash);
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize()) + "  " + ctx.getString(R.string.space_clean_apk_file_version, new Object[]{((PreInstalledAppTrash) this.mTrash).getVersionName()});
    }

    public String getSummary() {
        return super.getSummary();
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
        return ((PreInstalledAppTrash) this.mTrash).getAppLabel();
    }
}
