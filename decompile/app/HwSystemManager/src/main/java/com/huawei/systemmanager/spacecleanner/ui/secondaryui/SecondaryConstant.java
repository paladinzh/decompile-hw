package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.ApkDataItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.ApkTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.AppModelTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.AudioTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.BakFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.EmptyFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.FileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.LargeFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.LogFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.PhotoTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.PreInstallAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.SimilarPhotoTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.TempFileTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.ThumbnailTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.UnusedAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.VideoTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.WeChatTrashGroupItem;
import java.util.List;
import java.util.Map;

public class SecondaryConstant {
    public static final String AUDIO_TRASH_UNKNOWN_STRING = "unknown";
    public static final int DATA_CHANGE_RESULT_CODE = 1000;
    public static final int DATA_NOT_CHANGE_RESULT_CODE = 1001;
    public static final String NAME_ID_EXTRA = "name_id";
    public static final String OPEN_SECONDARY_PARAM = "key_param";
    public static final String PHOTO_TRASH_PATH_EXTRA = "photo_trash_path";
    public static final String SUB_TRASH_ID_EXTRA = "sub_trash_id";
    public static final String TRASH_HANDLER_ID_EXTRA = "handler_id";
    private static final Map<Integer, TrashTransFunc<? extends ITrashItem>> sTransFunc = HsmCollections.newArrayMap();

    public static TrashTransFunc getTransFunc(int trashType) {
        if (sTransFunc.isEmpty()) {
            List<TrashTransFunc> tempList = Lists.newArrayList();
            tempList.add(AppModelTrashItem.sCustomDataTransFunc);
            tempList.add(AppModelTrashItem.sResidualTransFunc);
            tempList.add(AppModelTrashItem.sTopVideoTrashFunc);
            tempList.add(UnusedAppTrashItem.sTransFunc);
            tempList.add(AudioTrashItem.sTransFunc);
            tempList.add(PhotoTrashItem.sTransFunc);
            tempList.add(VideoTrashItem.sTransFunc);
            tempList.add(ApkTrashItem.sTransFunc);
            tempList.add(EmptyFileTrashItem.sTransFunc);
            tempList.add(LogFileTrashItem.sTransFunc);
            tempList.add(BakFileTrashItem.sTransFunc);
            tempList.add(TempFileTrashItem.sTransFunc);
            tempList.add(ThumbnailTrashItem.sTransFunc);
            tempList.add(LargeFileTrashItem.sTransFunc);
            tempList.add(ApkDataItem.sTransFunc);
            tempList.add(PreInstallAppTrashItem.sTransFunc);
            tempList.add(WeChatTrashGroupItem.sTransFunc);
            tempList.add(SimilarPhotoTrashItem.sTransFunc);
            for (TrashTransFunc func : tempList) {
                sTransFunc.put(Integer.valueOf(func.getTrashType()), func);
            }
        }
        TrashTransFunc transFunc = (TrashTransFunc) sTransFunc.get(Integer.valueOf(trashType));
        if (transFunc == null) {
            return FileTrashItem.getTransFunc(trashType);
        }
        return transFunc;
    }
}
