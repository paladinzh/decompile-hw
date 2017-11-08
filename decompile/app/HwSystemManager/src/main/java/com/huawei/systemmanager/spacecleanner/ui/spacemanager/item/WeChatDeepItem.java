package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TecentWeChatTrashFile;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.WeChatTrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.WeChatTypeCons;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListAppCacheSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListGridActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.WeChatExpandListItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.WeChatListGridItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WeChatDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "WeChatDeepItem";
    public static final int TRASH_TYPE = 1048576;

    private static class WeChatCovertor extends Convertor {
        private int index;

        public WeChatCovertor(int index) {
            this.index = index;
        }

        public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
            List<TrashItemGroup> result = Lists.newArrayList();
            if (scanHandler == null || this.index == -1) {
                HwLog.e(WeChatDeepItem.TAG, "convert, arg is wrong");
                return result;
            }
            TrashGroup group = scanHandler.getTrashByType(1048576);
            if (group == null) {
                HwLog.e(WeChatDeepItem.TAG, "convert , group is null");
                return result;
            }
            WeChatTrashGroup weChatTrashGroup = (WeChatTrashGroup) group.getTrash(this.index);
            if (weChatTrashGroup == null) {
                HwLog.e(WeChatDeepItem.TAG, "convert , weChatTrashGroup is null");
                return result;
            }
            List<Trash> listTrash = weChatTrashGroup.getTrashListUnclened();
            Collections.sort(listTrash, TecentWeChatTrashFile.WECHAT_COMPARATOR);
            List<Trash> contentList = new ArrayList();
            String title = null;
            Calendar calendar = Calendar.getInstance();
            int totalCount = listTrash.size();
            int i = 0;
            while (i < totalCount) {
                Trash t = (Trash) listTrash.get(i);
                if (i == 0 || t.getMonth() != ((Trash) listTrash.get(i - 1)).getMonth()) {
                    if (contentList.size() != 0) {
                        result.add(Convertor.transToExpandeGroup(contentList, title, 1048576, WeChatExpandListItem.sTransFunc));
                        contentList.clear();
                    }
                    calendar.set(1, t.getYear());
                    calendar.set(2, t.getMonth() - 1);
                    title = DateFormat.format("yyyy/M", calendar).toString();
                }
                contentList.add(t);
                i++;
            }
            result.add(Convertor.transToExpandeGroup(contentList, title, 1048576, WeChatExpandListItem.sTransFunc));
            return result;
        }
    }

    public int getTrashType() {
        return 1048576;
    }

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_wechat);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storagecleaner_wechatclean);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setTitleStr(getTitle(ctx));
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setOperationResId(R.string.common_delete);
        params.setDialogTitleId(R.plurals.space_clean_any_data_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_data_delete_title);
        params.setDialogContentId(R.plurals.space_clean_data_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListTrashSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkMultiTrashFinished(handler);
    }

    public int getDeepItemType() {
        return 10;
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

    public static Intent getTrashIntent(Context cxt, int index, long handlerId, ITrashItem item) {
        Intent intent;
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(1048576);
        params.setTitleStr(item.getName());
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_apps);
        params.setOperationResId(R.string.common_delete);
        params.setDialogTitleId(R.plurals.space_clean_any_file_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_file_delete_title);
        params.setDialogContentId(R.plurals.space_clean_file_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setDeepItemType(10);
        params.setSubTrashType(item.getSubTrashType());
        if (WeChatTypeCons.isWeChatAudio(item.getSubTrashType())) {
            intent = new Intent(cxt, ListAppCacheSetActivity.class);
        } else {
            intent = new Intent(cxt, ListGridActivity.class);
        }
        intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
        intent.putExtra(SecondaryConstant.SUB_TRASH_ID_EXTRA, index);
        intent.putExtra("handler_id", handlerId);
        intent.putExtra(SecondaryConstant.NAME_ID_EXTRA, item.getName());
        return intent;
    }

    public static List<TrashItemGroup> getExpandListSource(TrashScanHandler handler, int subIndex) {
        return new WeChatCovertor(subIndex).convert(handler);
    }

    public static List<ITrashItem> getListGridSource(TrashScanHandler scanHandler, int index) {
        List<ITrashItem> result = new LinkedList();
        if (scanHandler == null || index == -1) {
            HwLog.e(TAG, "getListGridSource, arg is wrong");
            return result;
        }
        TrashGroup group = scanHandler.getTrashByType(1048576);
        if (group == null) {
            HwLog.e(TAG, "getListGridSource, group is null");
            return result;
        }
        WeChatTrashGroup weChatTrashGroup = (WeChatTrashGroup) group.getTrash(index);
        if (weChatTrashGroup == null) {
            HwLog.e(TAG, "getListGridSource, weChatTrashGroup is null");
            return result;
        }
        List<Trash> listTrash = weChatTrashGroup.getTrashListUnclened();
        Collections.sort(listTrash, TecentWeChatTrashFile.WECHAT_COMPARATOR);
        int count = listTrash.size();
        for (int i = 0; i < count; i++) {
            result.add(WeChatListGridItem.sTransFunc.apply((Trash) listTrash.get(i)));
        }
        return result;
    }
}
