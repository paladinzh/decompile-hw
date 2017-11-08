package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.RootFolderTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppCacheTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppCustomTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListAppCacheSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class AppDataDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 10;
    private static final String TAG = "AppDataDeepItem";
    public static final int TRASH_TYPE = 2189369;

    private static class AppCacheTrashCovertor extends Convertor {
        private AppCacheTrashCovertor() {
        }

        public List<TrashItemGroup> convert(TrashScanHandler handler) {
            if (handler == null) {
                HwLog.e(AppDataDeepItem.TAG, "convert handler is null");
                return Lists.newArrayList();
            }
            Map<Integer, TrashGroup> trashMap = handler.getAllTrashes();
            if (trashMap == null) {
                HwLog.e(AppDataDeepItem.TAG, "covert trashMap is null");
                return Lists.newArrayList();
            }
            List<TrashItemGroup> result = Lists.newArrayList();
            result.add(Convertor.transToExpandeGroup(trashMap, AppCacheTrashItem.sTransFunc));
            result.add(Convertor.transToExpandeGroup(trashMap, AppCustomTrashItem.sAppDataTransFunc));
            result.add(Convertor.transToExpandeGroup(trashMap, AppCustomTrashItem.sAppResidueTransFunc));
            result.add(Convertor.transToExpandeGroup(trashMap, RootFolderTrashItem.getTransFunc(16)));
            result.add(Convertor.transToExpandeGroup(trashMap, RootFolderTrashItem.getTransFunc(32)));
            result.add(Convertor.transToExpandeGroup(trashMap, RootFolderTrashItem.getTransFunc(2048)));
            result.add(Convertor.transToExpandeGroup(trashMap, RootFolderTrashItem.getTransFunc(8)));
            result.add(Convertor.transToExpandeGroup(trashMap, RootFolderTrashItem.getTransFunc(2097152)));
            return result;
        }
    }

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_app_data);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_app, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setDialogTitleId(R.plurals.space_clean_any_data_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_data_delete_title);
        params.setDialogContentId(R.plurals.space_clean_data_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(getTitle(ctx));
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListAppCacheSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public String getTag() {
        return TAG;
    }

    public boolean showTip() {
        long maxSize = (StorageHelper.getStorage().getTotalSize(0) * 10) / 100;
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
        return TRASH_TYPE;
    }

    public int getDeepItemType() {
        return 1;
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkAppDataTrashFinished(handler);
    }

    private boolean checkAppDataTrashFinished(TrashScanHandler handler) {
        this.mTrashList.clear();
        int finishType = handler.getFinishedType();
        int trashType = getTrashType();
        HwLog.i(getTag(), "checkMultiTrashFinished, item:" + getTag() + ", trashType:" + Integer.toBinaryString(trashType) + ",finishType:" + Integer.toBinaryString(finishType));
        if (!checkIfScanEnd(handler, finishType, trashType)) {
            return isFinished();
        }
        Map<Integer, TrashGroup> result = handler.getAllTrashes();
        for (Integer intValue : Lists.newArrayList(result.keySet())) {
            int key = intValue.intValue();
            if (!((key & trashType) == 0 || key == 65536)) {
                Trash trash = (Trash) result.get(Integer.valueOf(key));
                if (trash == null) {
                    HwLog.i(getTag(), "checkMultiTrashFinished, trash is empty. Type:" + Integer.toBinaryString(key));
                } else if (trash.isCleaned()) {
                    HwLog.i(getTag(), "checkMultiTrashFinished, trash is cleaned. Type:" + Integer.toBinaryString(key));
                } else {
                    this.mTrashList.add(trash);
                    HwLog.i(getTag(), "add trash, type:" + Integer.toBinaryString(key) + ", size = " + trash.getTrashSizeCleaned(false));
                }
            }
        }
        setFinish();
        return isFinished();
    }

    public static List<TrashItemGroup> covert(TrashScanHandler handler) {
        return new AppCacheTrashCovertor().convert(handler);
    }
}
