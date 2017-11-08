package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TencenTopVideoAppGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.Convertor;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ExpandeItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.TopVideoTrashGroupItem;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.TopVideoTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListAppCacheSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.AppModelTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.VideoTrashItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideoDeepItem extends TrashDeepItem {
    private static final int SHOW_TIP_SIZE_PERCENT = 20;
    private static final String TAG = "VideoDeepItem";
    public static final int TRASH_TYPE = 65792;

    public static class TopVideoCovertor extends Convertor {
        public static final Comparator<Trash> TOP_VIDEO_COMPARATOR = new Comparator<Trash>() {
            public int compare(Trash lhs, Trash rhs) {
                long lhsUnCleanSize = lhs.getTrashSizeCleaned(false);
                long rhsUnCleanSize = rhs.getTrashSizeCleaned(false);
                if (lhsUnCleanSize == rhsUnCleanSize) {
                    return 0;
                }
                if (lhsUnCleanSize > rhsUnCleanSize) {
                    return -1;
                }
                if (lhsUnCleanSize < rhsUnCleanSize) {
                    return 1;
                }
                return 0;
            }
        };

        public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
            if (scanHandler == null) {
                HwLog.e(VideoDeepItem.TAG, "convert scanHandler == null");
                return Lists.newArrayList();
            }
            List<TrashItemGroup> result = createTopVideo(scanHandler);
            TrashItemGroup videoFile = createVideo(scanHandler);
            if (videoFile != null) {
                result.add(videoFile);
            }
            return result;
        }

        private List<TrashItemGroup> createTopVideo(TrashScanHandler handler) {
            TrashGroup trashGroup = handler.getTrashByType(65536);
            if (trashGroup == null) {
                return Lists.newArrayList();
            }
            List<TrashItemGroup> result = Lists.newArrayListWithCapacity(trashGroup.getSize());
            List<Trash> listTrash = new ArrayList();
            listTrash.addAll(trashGroup.getTrashList());
            Collections.sort(listTrash, TOP_VIDEO_COMPARATOR);
            for (Trash it : listTrash) {
                List<Trash> list;
                List<ITrashItem> itemList;
                ITrashItem trashItem;
                if (it instanceof TencenTopVideoAppGroup) {
                    Trash group = (TencenTopVideoAppGroup) it;
                    TrashTransFunc TransFunc = TopVideoTrashGroupItem.getGroupTransFunc(65536);
                    int type = TransFunc.getTrashType();
                    ITrashItem item = TransFunc.apply(group);
                    if (item == null) {
                        HwLog.e(VideoDeepItem.TAG, "createTopVideo, item is null!");
                    } else {
                        String title = item.getName();
                        list = group.getTrashList();
                        itemList = Lists.newArrayListWithCapacity(list.size());
                        for (Trash trash : list) {
                            trashItem = TopVideoTrashItem.getTransFunc(65536).apply(trash);
                            if (trashItem != null) {
                                itemList.add(trashItem);
                            }
                        }
                        result.add(ExpandeItemGroup.create(type, title, itemList));
                    }
                } else if (it instanceof AppCustomTrash) {
                    AppCustomTrash group2 = (AppCustomTrash) it;
                    list = group2.getTrashList();
                    itemList = Lists.newArrayListWithCapacity(list.size());
                    for (Trash item2 : list) {
                        trashItem = AppModelTrashItem.sCustomDataTransFunc.apply(item2);
                        if (trashItem != null) {
                            itemList.add(trashItem);
                            result.add(ExpandeItemGroup.create(16384, group2.getName(), itemList));
                        }
                    }
                }
            }
            return result;
        }

        private TrashItemGroup createVideo(TrashScanHandler handler) {
            TrashGroup trashGroup = handler.getTrashByType(256);
            if (trashGroup == null) {
                return null;
            }
            List<Trash> list = trashGroup.getTrashList();
            if (HsmCollections.isEmpty(list)) {
                return null;
            }
            List<ITrashItem> itemList = Lists.newArrayListWithCapacity(list.size());
            for (Trash trash : list) {
                ITrashItem item = VideoTrashItem.sTransFunc.apply(trash);
                if (item != null) {
                    itemList.add(item);
                }
            }
            return ExpandeItemGroup.create(256, GlobalContext.getContext().getString(R.string.spaceclean_top_video_local_videos), itemList);
        }
    }

    public String getTitle(Context ctx) {
        return ctx.getString(R.string.space_clean_top_video_app_trash);
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_storgemanager_video, null);
    }

    public Intent getIntent(Context ctx) {
        OpenSecondaryParam params = new OpenSecondaryParam();
        params.setScanType(100);
        params.setTrashType(getTrashType());
        params.setOperationResId(R.string.common_delete);
        params.setEmptyTextID(R.string.no_file_trash_tip);
        params.setEmptyIconID(R.drawable.ic_no_folder);
        params.setDialogTitleId(R.plurals.space_clean_any_data_delete_title);
        params.setAllDialogTitleId(R.string.space_clean_all_data_delete_title);
        params.setDialogContentId(R.plurals.space_clean_data_delete_message);
        params.setDialogPositiveButtonId(R.string.common_delete);
        params.setTitleStr(getTitle(ctx));
        params.setDeepItemType(getDeepItemType());
        return new Intent(ctx, ListAppCacheSetActivity.class).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return checkMultiTrashFinished(handler);
    }

    public int getTrashType() {
        return TRASH_TYPE;
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

    public int getDeepItemType() {
        return 6;
    }

    public static List<TrashItemGroup> convert(TrashScanHandler handler) {
        return new TopVideoCovertor().convert(handler);
    }
}
