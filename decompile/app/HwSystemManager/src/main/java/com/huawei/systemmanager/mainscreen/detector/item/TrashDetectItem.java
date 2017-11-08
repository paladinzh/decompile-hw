package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.text.format.Formatter;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TrashDetectItem extends DetectItem {
    private static final String TAG = "TrashDetectItem";
    private static final long TRASH_SIZE_LIMIT_1 = 10485760;
    private static final long TRASH_SIZE_LIMIT_2 = 52428800;
    private List<Trash> mTrashList = Lists.newArrayList();
    private long mTrashSize;

    public String getTitle(Context ctx) {
        String title = "";
        int state = getState();
        switch (state) {
            case 1:
                return ctx.getString(R.string.no_have_rubbish);
            case 2:
                return ctx.getString(R.string.have_rubbish, new Object[]{Formatter.formatFileSize(getContext(), this.mTrashSize)});
            case 3:
                return ctx.getString(R.string.have_rubbish_optimize, new Object[]{Formatter.formatFileSize(getContext(), this.mTrashSize)});
            default:
                HwLog.e(TAG, "getTitle unkonw state:" + state);
                return title;
        }
    }

    public int getItemType() {
        return 11;
    }

    public int getOptimizeActionType() {
        return 2;
    }

    public void doOptimize(Context ctx) {
        for (Trash trash : this.mTrashList) {
            trash.clean(ctx);
        }
        setState(3);
    }

    protected int score() {
        if (this.mTrashSize <= 10485760) {
            return 5;
        }
        if (this.mTrashSize <= TRASH_SIZE_LIMIT_2) {
            return 10;
        }
        return 15;
    }

    public boolean isManulOptimize() {
        return false;
    }

    public String getName() {
        return getContext().getString(R.string.sys_optimize_trash_cleaner_title);
    }

    private void initDatas(Collection<TrashGroup> trashs, long totalSize) {
        this.mTrashList.clear();
        this.mTrashList.addAll(trashs);
        this.mTrashSize = totalSize;
        if (trashs.isEmpty()) {
            setState(1);
        } else {
            setState(2);
        }
    }

    public static TrashDetectItem create(TrashScanHandler handler) {
        Map<Integer, TrashGroup> trashMap;
        if (handler != null) {
            trashMap = handler.getNormalTrashes();
        } else {
            HwLog.e(TAG, "create, but hanlder is null!!");
            trashMap = Maps.newHashMap();
        }
        long totalSize = 0;
        Collection<TrashGroup> trashes = trashMap.values();
        for (TrashGroup trash : trashes) {
            totalSize += trash.getTrashSize();
        }
        TrashDetectItem item = new TrashDetectItem();
        item.initDatas(trashes, totalSize);
        HwLog.i(TAG, "create, trash count:" + trashes.size() + ", totalsize:" + totalSize);
        return item;
    }

    public void refresh() {
        if (getState() == 3) {
            setState(1);
            HwLog.i(TAG, "refresh called, change state from: STATE_OPTIMIZED, to: STATE_SECURITY");
            return;
        }
        HwLog.i(TAG, "refresh called, do nothing");
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        return this;
    }
}
