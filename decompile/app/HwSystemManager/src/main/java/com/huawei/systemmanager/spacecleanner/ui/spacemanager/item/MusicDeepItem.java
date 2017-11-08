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

public class MusicDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "MusicDeepItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_trash_audio);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_music, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_audio_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_music);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(getTitle(ctx));
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListTrashSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkSingleTrashFinished(handler);
    }

    public int getTrashType() {
        return 512;
    }

    public int getDeepItemType() {
        return 3;
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

    public boolean showInfrequentlyTip() {
        return false;
    }
}
