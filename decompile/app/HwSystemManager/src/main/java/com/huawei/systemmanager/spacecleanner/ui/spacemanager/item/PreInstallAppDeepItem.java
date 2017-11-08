package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;

public class PreInstallAppDeepItem extends TrashDeepItem {
    private static final String TAG = "PreInstallAppDeepItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_preinstall_app_uninstall);
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
        params.setEmptyTextID(R.string.spaceclean_no_preinstalled_app_trash_tip);
        params.setDialogTitleId(R.plurals.space_clean_any_app_uninstall_title);
        params.setAllDialogTitleId(R.string.space_clean_all_app_uninstall_title);
        params.setDialogPositiveButtonId(R.string.common_uninstall);
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListTrashSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkSingleTrashFinished(handler);
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        return false;
    }

    public boolean showInfrequentlyTip() {
        return false;
    }

    public int getTrashType() {
        return 524288;
    }

    public int getDeepItemType() {
        return 7;
    }
}
