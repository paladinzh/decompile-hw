package com.huawei.systemmanager.spacecleanner.ui.normalscan;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;

public class AppCustomTrashItem extends CommonTrashItem<AppCustomTrash> implements IAppInfo {
    public static final AppCustomTrashTransFunc sAppDataTransFunc = new AppCustomTrashTransFunc() {
        public int getTrashType() {
            return 81920;
        }

        public AppCustomTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppCustomTrashItem trans input is null!");
                return null;
            } else if (!(input instanceof AppCustomTrash)) {
                HwLog.e(TrashTransFunc.TAG, "AppCustomTrashItem trans type error,orgin type is:" + input.getType());
                return null;
            } else if (((AppCustomTrash) input).getTrashSize() <= 0) {
                return null;
            } else {
                return new AppCustomTrashItem((AppCustomTrash) input);
            }
        }
    };
    public static final AppCustomTrashTransFunc sAppResidueTransFunc = new AppCustomTrashTransFunc() {
        public int getTrashType() {
            return 8192;
        }
    };
    public static final AppCustomTrashTransFunc sAppTopVideoTransFunc = new AppCustomTrashTransFunc() {
        public int getTrashType() {
            return 65536;
        }
    };

    private static abstract class AppCustomTrashTransFunc extends TrashTransFunc<AppCustomTrashItem> {
        private AppCustomTrashTransFunc() {
        }

        public AppCustomTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppCustomTrashItem trans input is null!");
                return null;
            } else if (input instanceof AppCustomTrash) {
                return new AppCustomTrashItem((AppCustomTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "AppCustomTrashItem trans type error,orgin type is:" + input.getType());
                return null;
            }
        }
    }

    public AppCustomTrashItem(AppCustomTrash trash) {
        super(trash);
    }

    public String getName() {
        return ((AppCustomTrash) this.mTrash).getAppLabel();
    }

    public String getPackageName() {
        return ((AppCustomTrash) this.mTrash).getPackageName();
    }

    public String getAppLabel() {
        return ((AppCustomTrash) this.mTrash).getAppLabel();
    }

    public Drawable getAppIcon() {
        return ((AppCustomTrash) this.mTrash).getAppIcon();
    }

    public Drawable getIcon(Context context) {
        return getItemIcon();
    }

    public Drawable getItemIcon() {
        Drawable icon = null;
        Context ctx = GlobalContext.getContext();
        if (getTrashType() != 8192) {
            icon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(getPackageName()));
        }
        if (icon == null) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
        }
        return icon;
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public OpenSecondaryParam getOpenSecondaryParam() {
        OpenSecondaryParam params = buildCommonSecondaryParam();
        params.setUniqueDescription(getPackageName());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyIconID(R.drawable.ic_no_folder);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setTitleStr(getAppLabel());
        if ((81920 & getTrashType()) != 0) {
            params.setTrashType(81920);
        }
        return params;
    }

    public boolean hasSecondary() {
        return true;
    }
}
