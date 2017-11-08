package com.huawei.systemmanager.spacecleanner.ui.deepscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustom.HwCustomAppDataGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;

public class DownloadAppGroupItem extends CommonTrashItem<HwCustomAppDataGroup> {
    private static final String TAG = "DownloadAppGroupItem";

    public static class DownloadAppGroupTransFunc extends TrashTransFunc<DownloadAppGroupItem> {
        private final int trashType;

        public DownloadAppGroupTransFunc(int type) {
            this.trashType = type;
        }

        public int getTrashType() {
            return this.trashType;
        }

        public DownloadAppGroupItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "DownloadAppGroupTransFunc, input is null!");
                return null;
            } else if (input instanceof HwCustomAppDataGroup) {
                return new DownloadAppGroupItem((HwCustomAppDataGroup) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "DownloadAppGroupTransFunc, input in not HwCustomDataGroup!,input type:" + input.getType());
                return null;
            }
        }
    }

    private DownloadAppGroupItem(HwCustomAppDataGroup trash) {
        super(trash);
    }

    public String getName() {
        return ((HwCustomAppDataGroup) getTrash()).getAppLabel();
    }

    public Drawable getItemIcon() {
        Context ctx = GlobalContext.getContext();
        Drawable icon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(((HwCustomAppDataGroup) getTrash()).getPackageName()));
        if (icon == null) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
        }
        return icon;
    }

    public static DownloadAppGroupTransFunc getGroupTransFunc(int type) {
        return new DownloadAppGroupTransFunc(type);
    }

    public boolean hasSecondary() {
        return false;
    }
}
