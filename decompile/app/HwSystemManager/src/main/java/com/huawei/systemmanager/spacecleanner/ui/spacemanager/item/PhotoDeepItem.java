package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.photomanager.PhotoManagerActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;

public class PhotoDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 30;
    private static final String TAG = "PhotoDeepItem";

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_trash_photo);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_pic, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setDeepItemType(getDeepItemType());
        Intent intent = new Intent(ctx, PhotoManagerActivity.class);
        intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
        return intent;
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        int finishType = handler.getFinishedType();
        int trashType = getTrashType();
        if (!checkIfScanEnd(handler, finishType, trashType)) {
            return isFinished();
        }
        this.mTrashList.clear();
        HwLog.i(TAG, "checkMultiTrashFinished DeepItem is finished");
        for (int i = 0; i < 24; i++) {
            int type = 1 << i;
            if ((trashType & type) != 0) {
                Trash trash = handler.getTrashByType(type);
                if (!(trash == null || trash.isCleaned() || 128 != trash.getType())) {
                    this.mTrashList.add(trash);
                }
            }
        }
        setFinish();
        return isFinished();
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        long maxSize = (StorageHelper.getStorage().getTotalSize(0) * 30) / 100;
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

    public int getTrashType() {
        return 12583040;
    }

    public int getDeepItemType() {
        return 4;
    }
}
