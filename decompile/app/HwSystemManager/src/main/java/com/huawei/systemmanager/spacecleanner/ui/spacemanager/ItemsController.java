package com.huawei.systemmanager.spacecleanner.ui.spacemanager;

import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.AppDataDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.AppDataResetItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DefaultStorageChangeItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DownloadItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.LargeFileDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.MusicDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.PhotoDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.PreInstallAppDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.TrashDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.UnusedAppDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.VideoDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.WeChatDeepItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemsController {
    private static final String TAG = "ItemsController";
    private int mAnalysisTrashType;
    private final OnClickListener mClicker;
    private final Context mContext;
    private List<DeepItem> mItemList = Lists.newArrayList();
    private final ViewGroup mParent;
    private Map<Integer, ViewController> mViewControllers = HsmCollections.newArrayMap();

    public ItemsController(ViewGroup parent, OnClickListener clicker, int analysisTrashType) {
        this.mContext = parent.getContext();
        this.mClicker = clicker;
        this.mParent = parent;
        this.mAnalysisTrashType = analysisTrashType;
    }

    public void checkItemFinished(TrashScanHandler scanHandler) {
        for (DeepItem item : this.mItemList) {
            if (!item.shouldCheckFinished()) {
                doCheckFinished(item);
            }
            if (!item.isFinished()) {
                item.checkIfFinished(scanHandler);
                if (item.isFinished()) {
                    doCheckFinished(item);
                }
            }
        }
    }

    private void doCheckFinished(DeepItem item) {
        int deepItemType = item.getDeepItemType();
        ViewController vc = (ViewController) this.mViewControllers.get(Integer.valueOf(deepItemType));
        if (vc != null) {
            vc.checkIfFinished();
        } else {
            HwLog.i(TAG, "checkItemFinished ViewController is null!" + deepItemType);
        }
    }

    public void updateViewState(int type) {
        ViewController vc = (ViewController) this.mViewControllers.get(Integer.valueOf(type));
        if (vc == null) {
            HwLog.i(TAG, "checkItemAfterHandlered, can not find type:" + type);
        } else {
            vc.updateState();
        }
    }

    public void updateAllViewState() {
        for (Entry<Integer, ViewController> vc : this.mViewControllers.entrySet()) {
            ViewController controller = (ViewController) vc.getValue();
            if (controller == null) {
                HwLog.e(TAG, "updateAllViewState, view controller is null.");
            } else {
                controller.updateState();
            }
        }
    }

    public List<DeepItem> getItems() {
        return this.mItemList;
    }

    public boolean initItems(TrashScanHandler scanHandler) {
        if (!this.mItemList.isEmpty()) {
            return false;
        }
        this.mItemList = buildAllItems();
        for (DeepItem item : this.mItemList) {
            item.checkIfFinished(scanHandler);
        }
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        for (DeepItem item2 : this.mItemList) {
            ViewController controller = ViewController.create(inflater, this.mParent, this.mClicker, item2, scanHandler);
            if (controller != null) {
                this.mViewControllers.put(Integer.valueOf(item2.getDeepItemType()), controller);
            }
        }
        initialContainerAnima();
        return true;
    }

    public boolean checkAllItemEmpty(TrashScanHandler scanHandler) {
        if (this.mItemList.isEmpty()) {
            HwLog.e(TAG, "checkAllItemEmpty called, but mItemList.isEmpty, ensure hava called initItems before!!");
            return true;
        }
        for (DeepItem item : this.mItemList) {
            if ((item.shouldCheckFinished() || item.isDeepItemDisplay(scanHandler)) && !(item.isFinished() && item.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private void initialContainerAnima() {
        LayoutTransition transition = new LayoutTransition();
        this.mParent.setLayoutTransition(transition);
        transition.setAnimator(3, AnimatorInflater.loadAnimator(this.mContext, R.animator.spacemanager_item_dismiss));
        transition.setStagger(1, 50);
    }

    private List<DeepItem> buildAllItems() {
        List<DeepItem> list = Lists.newArrayList();
        if (TMSEngineFeature.isSupportTMS()) {
            addItemToList(list, new WeChatDeepItem());
        }
        addItemToList(list, new VideoDeepItem());
        addItemToList(list, new LargeFileDeepItem());
        addItemToList(list, new MusicDeepItem());
        addItemToList(list, new PhotoDeepItem());
        addItemToList(list, new AppDataDeepItem());
        addItemToList(list, new UnusedAppDeepItem());
        addItemToList(list, new PreInstallAppDeepItem());
        addItemToList(list, new AppDataResetItem());
        addItemToList(list, new DownloadItem());
        addItemToList(list, new DefaultStorageChangeItem());
        return list;
    }

    private void addItemToList(List<DeepItem> list, DeepItem item) {
        if (item instanceof TrashDeepItem) {
            if ((this.mAnalysisTrashType & ((TrashDeepItem) item).getTrashType()) > 0) {
                list.add(0, item);
                return;
            } else {
                list.add(item);
                return;
            }
        }
        list.add(item);
    }
}
