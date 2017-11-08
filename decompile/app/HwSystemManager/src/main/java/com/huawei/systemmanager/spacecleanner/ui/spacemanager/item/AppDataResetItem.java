package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;

public class AppDataResetItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "AppDataResetItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_app_restore);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storgemanager_restore, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setTitleStr(getTitle(ctx));
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setOperationResId(R.string.common_delete);
        params.setDialogTitleId(R.plurals.space_clean_any_app_restore_title);
        params.setAllDialogTitleId(R.string.space_clean_all_app_restore_title);
        params.setDialogContentId(R.plurals.space_clean_app_restore_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListTrashSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkSingleTrashFinished(handler);
    }

    public boolean showInfrequentlyTip() {
        return false;
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        long maxSize = (StorageHelper.getStorage().getTotalSize(0) * 20) / 100;
        HwLog.i(TAG, "DeepItemType:" + getDeepItemType() + " maxSize:" + maxSize);
        long trashSize = getTrashSize();
        if (maxSize <= 0 || trashSize <= 0 || trashSize <= maxSize) {
            return false;
        }
        return true;
    }

    public int getTrashType() {
        return 262144;
    }

    public int getDeepItemType() {
        return 8;
    }
}
