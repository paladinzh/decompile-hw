package com.huawei.systemmanager.spacecleanner.ui.deepscan;

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
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class TopVideoTrashItem extends CommonTrashItem<AppModelTrash> implements IAppInfo {
    private static final String TAG = "TopVideoTrashItem";

    public static class TopVideoTransFunc extends TrashTransFunc<TopVideoTrashItem> {
        private final int trashType;

        public TopVideoTransFunc(int type) {
            this.trashType = type;
        }

        public int getTrashType() {
            return this.trashType;
        }

        public TopVideoTrashItem apply(Trash input) {
            if (input == null) {
                HwLog.e(TrashTransFunc.TAG, "TopVideoTransFunc, input is null!");
                return null;
            } else if (input instanceof AppModelTrash) {
                return new TopVideoTrashItem((AppModelTrash) input);
            } else {
                HwLog.e(TrashTransFunc.TAG, "TopVideoTransFunc, input is not AppModelTrash!,input type:" + input.getType());
                return null;
            }
        }
    }

    private TopVideoTrashItem(AppModelTrash trash) {
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

    public boolean shouldLoadPic() {
        return true;
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

    public String getTrashPath() {
        StringBuilder result = new StringBuilder();
        List<String> files = ((AppModelTrash) this.mTrash).getFiles();
        if (files.size() == 1) {
            return (String) files.get(0);
        }
        if (files.size() == 0) {
            return "";
        }
        int i = 1;
        for (String file : files) {
            int i2 = i + 1;
            result.append(i).append(". ").append(file).append("\n");
            i = i2;
        }
        return result.toString();
    }

    public Drawable getItemIcon() {
        Context ctx = GlobalContext.getContext();
        Drawable icon = TrashUtils.getAppIcon(TrashUtils.getPackageInfo(((AppModelTrash) getTrash()).getPackageName()));
        if (icon == null) {
            return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app);
        }
        return icon;
    }

    public static TopVideoTransFunc getTransFunc(int type) {
        return new TopVideoTransFunc(type);
    }

    public boolean hasSecondary() {
        return false;
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
