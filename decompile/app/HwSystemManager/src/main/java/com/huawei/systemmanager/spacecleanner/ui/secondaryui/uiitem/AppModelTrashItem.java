package com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppModelTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class AppModelTrashItem extends CommonTrashItem<AppModelTrash> implements IAppInfo {
    public static final TrashTransFunc sCustomDataTransFunc = new TrashTransFunc<AppModelTrashItem>() {
        public AppModelTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sCustomDataTransFunc trans, input is null!");
                return null;
            } else if (input instanceof AppModelTrash) {
                return new AppModelTrashItem((AppModelTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sCustomDataTransFunc trans, type error, type:" + input.getType());
                return null;
            }
        }

        public int getTrashType() {
            return 81920;
        }
    };
    public static final TrashTransFunc sResidualTransFunc = new TrashTransFunc<AppModelTrashItem>() {
        public AppModelTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sResidualTransFunc trans, input is null!");
                return null;
            }
            if (!(input instanceof AppModelTrash)) {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sResidualTransFunc trans, type error, type:" + input.getType());
            }
            return new AppModelTrashItem((AppModelTrash) input);
        }

        public int getTrashType() {
            return 8192;
        }
    };
    public static final TrashTransFunc sTopVideoTrashFunc = new TrashTransFunc<AppModelTrashItem>() {
        public AppModelTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sTopVideoTrashFunc trans, input is null!");
                return null;
            }
            if (!(input instanceof AppModelTrash)) {
                HwLog.e(TrashTransFunc.TAG, "AppModelTrashItem sTopVideoTrashFunc trans, type error, type:" + input.getType());
            }
            return new AppModelTrashItem((AppModelTrash) input);
        }

        public int getTrashType() {
            return 65536;
        }
    };

    public AppModelTrashItem(AppModelTrash trash) {
        super(trash);
    }

    public String getName() {
        int diffDays = ((AppModelTrash) this.mTrash).getDiffDays();
        String modelName = ((AppModelTrash) this.mTrash).getModelName();
        if (diffDays == 0) {
            return modelName;
        }
        Context ctx = GlobalContext.getContext();
        if (diffDays < 0) {
            int inDays = -diffDays;
            return ctx.getResources().getQuantityString(R.plurals.space_clean_trasy_in_days, inDays, new Object[]{modelName, Integer.valueOf(inDays)});
        }
        int daysAgo = diffDays;
        return ctx.getResources().getQuantityString(R.plurals.space_clean_trasy_days_ago, diffDays, new Object[]{modelName, Integer.valueOf(diffDays)});
    }

    public Drawable getItemIcon() {
        return GlobalContext.getContext().getResources().getDrawable(R.drawable.ic_storagecleaner_file);
    }

    public String getDescription(Context ctx) {
        return FileUtil.getFileSize(getTrashSize());
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_cache);
    }

    public boolean isUseIconAlways() {
        return true;
    }

    public boolean shouldLoadPic() {
        return true;
    }

    public String getTrashPath() {
        StringBuilder result = new StringBuilder();
        List<String> files = ((AppModelTrash) this.mTrash).getFiles();
        if (files.size() == 0) {
            return "";
        }
        if (files.size() == 1) {
            return (String) files.get(0);
        }
        int i = 1;
        for (String file : files) {
            int i2 = i + 1;
            result.append(i).append(". ").append(file).append("\n");
            i = i2;
        }
        return result.toString();
    }

    public String getPackageName() {
        return ((AppModelTrash) this.mTrash).getPackageName();
    }

    public String getAppLabel() {
        return ((AppModelTrash) this.mTrash).getAppLabel();
    }

    public Drawable getAppIcon() {
        return ((AppModelTrash) this.mTrash).getAppIcon();
    }
}
