package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;

public class UnusedAppDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 5;
    private static final String TAG = "UnusedAppDeepItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_unused_app);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setTitleStr(getTitle(ctx));
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setOperationResId(R.string.common_uninstall);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setEmptyTextID(R.string.spaceclean_no_unused_app_trash_tip);
        params.setDialogTitleId(R.plurals.space_clean_any_app_uninstall_title);
        params.setAllDialogTitleId(R.string.space_clean_all_app_uninstall_title);
        params.setDialogPositiveButtonId(R.string.common_uninstall);
        params.setDeepItemType(getDeepItemType());
        Intent intent = new Intent(ctx, ListTrashSetActivity.class);
        intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
        return intent;
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkSingleTrashFinished(handler);
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        long maxSize = (StorageHelper.getStorage().getTotalSize(0) * 5) / 100;
        long trashSize = getTrashSize();
        HwLog.i(TAG, "DeepItemType:" + getDeepItemType() + " maxSize:" + maxSize);
        if (maxSize <= 0 || trashSize <= 0 || trashSize <= maxSize) {
            return false;
        }
        return true;
    }

    public boolean showInfrequentlyTip() {
        for (Trash item : getTrashList()) {
            if (!item.isCleaned()) {
                if (!(item instanceof UnusedAppTrash)) {
                    HwLog.e(TAG, "error.trash is not unused app trash.");
                } else if (((UnusedAppTrash) item).isNotCommonlyUsed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTrashType() {
        return 2;
    }

    public int getDeepItemType() {
        return 5;
    }
}
