package com.huawei.systemmanager.spacecleanner.ui.spacemanager.item;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public abstract class TrashDeepItem extends DeepItem {
    private static final String TAG = "DeepTrashItem";
    protected List<Trash> mTrashList = Lists.newArrayList();

    public abstract int getTrashType();

    public boolean isDeepItemDisplay(TrashScanHandler handler) {
        return true;
    }

    public String getDescription(Context ctx) {
        String description = FileUtil.getFileSize(ctx, getTrashSize());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(getDeepItemType())).append(" , ");
        stringBuilder.append(description);
        SpaceStatsUtils.reportDeepItemTrashSize(stringBuilder.toString());
        return description;
    }

    public String getResolveDes(Context ctx) {
        return ctx.getResources().getString(R.string.space_manager_item_btn_handler);
    }

    public boolean isEmpty() {
        if (getTrashSize() <= 0) {
            return true;
        }
        for (Trash trash : this.mTrashList) {
            if (!trash.isCleaned()) {
                return false;
            }
        }
        return true;
    }

    public long getTrashSize() {
        long size = 0;
        for (Trash trash : this.mTrashList) {
            if (!trash.isCleaned()) {
                size += trash.getTrashSizeCleaned(false);
            }
        }
        return size;
    }

    public List<Trash> getTrashList() {
        return TrashUtils.getBaseTrashList(this.mTrashList);
    }

    protected boolean checkSingleTrashFinished(TrashScanHandler handler) {
        this.mTrashList.clear();
        int finishType = handler.getFinishedType();
        int trashType = getTrashType();
        if (!checkIfScanEnd(handler, finishType, trashType)) {
            return isFinished();
        }
        Trash trash = handler.getTrashByType(trashType);
        if (trash != null) {
            this.mTrashList.add(trash);
        }
        HwLog.i(getTag(), "checkSingleTrashFinished is finished, item:" + getTag());
        setFinish();
        return isFinished();
    }

    protected boolean checkMultiTrashFinished(TrashScanHandler handler) {
        this.mTrashList.clear();
        int finishType = handler.getFinishedType();
        int trashType = getTrashType();
        HwLog.i(getTag(), "checkMultiTrashFinished, item:" + getTag() + ", trashType:" + Integer.toBinaryString(trashType) + ",finishType:" + Integer.toBinaryString(finishType));
        if (!checkIfScanEnd(handler, finishType, trashType)) {
            return isFinished();
        }
        HwLog.i(getTag(), "checkMultiTrashFinished DeepItem is finished");
        for (int i = 0; i < 24; i++) {
            int type = 1 << i;
            if ((trashType & type) != 0) {
                Trash trash = handler.getTrashByType(type);
                if (trash == null) {
                    HwLog.i(getTag(), "checkMultiTrashFinished, trash is empty. Type:" + Integer.toBinaryString(type));
                } else if (trash.isCleaned()) {
                    HwLog.i(getTag(), "checkMultiTrashFinished, trash is cleaned. Type:" + Integer.toBinaryString(type));
                } else {
                    this.mTrashList.add(trash);
                    HwLog.i(getTag(), "add trash, type:" + Integer.toBinaryString(trashType) + ", size = " + trash.getTrashSizeCleaned(false));
                }
            }
        }
        setFinish();
        return isFinished();
    }

    protected boolean checkIfScanEnd(TrashScanHandler handler, int finishedType, int trashType) {
        if (handler.isScanEnd() || (finishedType & trashType) == trashType) {
            return true;
        }
        return false;
    }

    public boolean shouldCheckFinished() {
        return true;
    }

    public boolean onCheckFinished(TrashScanHandler handler) {
        return true;
    }
}
