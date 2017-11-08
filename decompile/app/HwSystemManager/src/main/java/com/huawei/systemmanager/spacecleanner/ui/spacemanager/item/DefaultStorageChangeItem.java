package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.SpaceClear;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;

public class DefaultStorageChangeItem extends TrashDeepItem {
    private static final String MEMORY_CARD_SETTINGS_ACTION = "android.settings.MEMORY_CARD_SETTINGS";
    private static final String TAG = "DefaultStorageChangeItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_default_storage_change_sub);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storgemanager_storageplace, null);
    }

    public String getDescription(Context ctx) {
        if (StorageHelper.getStorage().isDefaultSDCard()) {
            return ctx.getResources().getString(R.string.space_clean_default_storage_external_des);
        }
        return ctx.getResources().getString(R.string.space_clean_default_storage_internal_des);
    }

    public String getResolveDes(Context ctx) {
        return ctx.getResources().getString(R.string.space_manager_item_btn_modify_handler);
    }

    public Intent getIntent(Context ctx) {
        Intent intent = new Intent();
        intent.setAction(MEMORY_CARD_SETTINGS_ACTION);
        intent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, SpaceClear.ACTION_CLICK_LOW_STORAGE_NOTIFICATION);
        intent.putExtra(HsmStatConst.KEY_SHOULD_STAT, false);
        return intent;
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
        return 131072;
    }

    public int getDeepItemType() {
        return 9;
    }

    public boolean shouldCheckFinished() {
        return false;
    }

    public boolean isDeepItemDisplay(TrashScanHandler handler) {
        if (handler.hasSdcard()) {
            return true;
        }
        return false;
    }
}
