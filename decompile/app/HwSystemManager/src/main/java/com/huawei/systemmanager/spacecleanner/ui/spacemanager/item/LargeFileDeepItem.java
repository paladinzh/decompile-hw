package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.RootFolderTrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ExpandeItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.RootFolderTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListAppCacheSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.LargeFileTrashItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class LargeFileDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "LargeFileDeepItem";
    public static final int TRASH_TYPE = 4;

    private static class LargeFileTrashCovertor extends Convertor {
        private LargeFileTrashCovertor() {
        }

        public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
            if (scanHandler == null) {
                HwLog.e(LargeFileDeepItem.TAG, "convert handler is null");
                return Lists.newArrayList();
            }
            TrashGroup largeFileGroup = scanHandler.getTrashByType(4);
            if (largeFileGroup == null) {
                HwLog.e(LargeFileDeepItem.TAG, "covert largeFileGroup is null");
                return Lists.newArrayList();
            }
            List<TrashItemGroup> result = Lists.newArrayList();
            for (Trash it : largeFileGroup.getTrashList()) {
                if (it instanceof RootFolderTrashGroup) {
                    Trash group = (RootFolderTrashGroup) it;
                    TrashTransFunc TransFunc = RootFolderTrashItem.getTransFunc(4);
                    int type = TransFunc.getTrashType();
                    ITrashItem item = TransFunc.apply(group);
                    if (item == null) {
                        HwLog.e(LargeFileDeepItem.TAG, "LargeFileTrashCovertor covert, item is null!");
                    } else {
                        String title = item.getName();
                        List<Trash> list = group.getTrashList();
                        List<ITrashItem> itemList = Lists.newArrayListWithCapacity(list.size());
                        List<ITrashItem> notCommonlyUseditemList = Lists.newArrayList();
                        for (Trash trash : list) {
                            LargeFileTrashItem trashItem = (LargeFileTrashItem) LargeFileTrashItem.sTransFunc.apply(trash);
                            if (trashItem != null) {
                                trashItem.setChecked(trash.isSuggestClean());
                                if (trashItem.isNotCommonlyUsed()) {
                                    notCommonlyUseditemList.add(trashItem);
                                } else {
                                    itemList.add(trashItem);
                                }
                            }
                        }
                        if (notCommonlyUseditemList.size() > 0) {
                            itemList.addAll(0, notCommonlyUseditemList);
                        }
                        result.add(ExpandeItemGroup.create(type, title, itemList));
                    }
                }
            }
            return result;
        }
    }

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_large_file);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_cache, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_folder);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(getTitle(ctx));
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListAppCacheSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkSingleTrashFinished(handler);
    }

    public int getTrashType() {
        return 4;
    }

    public int getDeepItemType() {
        return 2;
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
        for (Trash item : getTrashList()) {
            if (!item.isCleaned()) {
                if (!(item instanceof LargeFileTrash)) {
                    HwLog.e(TAG, "error.trash is not large file trash.");
                } else if (((LargeFileTrash) item).isNotCommonlyUsed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
        return new LargeFileTrashCovertor().convert(scanHandler);
    }
}
