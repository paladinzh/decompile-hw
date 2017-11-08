package com.huawei.systemmanager.spacecleanner.ui.suggestcleaner;

import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.NormalCovertor;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.TrashListAdapter;
import com.huawei.systemmanager.spacecleanner.ui.upperview.HeadViewController;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class ListController {
    private static final String TAG = "ListController";
    private final Context mContext;
    private HeadViewController mHeadViewController;
    private final LayoutInflater mInflater = LayoutInflater.from(this.mContext);
    private ListView mItemList;
    private ScanningAdapter mScanningAdapter;
    private TrashListAdapter mTrashAdapter;
    private ExpandableListView mTrashList;

    public ListController(Context ctx, View view) {
        this.mContext = ctx;
        this.mTrashList = (ExpandableListView) view.findViewById(R.id.trash_expande_list);
        this.mItemList = (ListView) view.findViewById(R.id.spaceclean_normal_cleanned_list_view);
    }

    public void setHeadViewController(HeadViewController controller) {
        this.mHeadViewController = controller;
    }

    public void setScanning() {
        HwLog.i(TAG, "setScanning called");
        this.mScanningAdapter = new ScanningAdapter(this.mContext, this.mInflater);
        this.mHeadViewController.addScanHeadView(this.mItemList);
        this.mItemList.setDivider(null);
        this.mItemList.setAdapter(this.mScanningAdapter);
        this.mItemList.setOverScrollMode(2);
    }

    public void setScanEnd(Map<Integer, TrashGroup> trashMap, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker, OnClickListener groupCheckClicker) {
        this.mItemList.setVisibility(8);
        this.mTrashList.setVisibility(0);
        this.mTrashAdapter = new TrashListAdapter(this.mContext, checkClicker, itemClicker, longClicker, groupCheckClicker);
        List<TrashItemGroup> data = new NormalCovertor().getScanEndList(trashMap);
        this.mHeadViewController.addTrashHeadView(this.mTrashList);
        this.mTrashAdapter.setList(this.mTrashList);
        this.mTrashAdapter.setState(SpaceState.NORMAL_SCAN_END, data);
        this.mTrashList.setOverScrollMode(2);
        this.mTrashList.setOnGroupExpandListener(new OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition) {
                if (ListController.this.mTrashAdapter != null) {
                    TrashItemGroup group = ListController.this.mTrashAdapter.getGroup(groupPosition);
                    if (group == null) {
                        HwLog.e(ListController.TAG, "onGroupExpand , group is null!");
                        return;
                    }
                    SpaceStatsUtils.reportExpandSpaceScanItemOp(group.getTrashType(), "1");
                }
            }
        });
        this.mTrashList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            public void onGroupCollapse(int groupPosition) {
                if (ListController.this.mTrashAdapter != null) {
                    TrashItemGroup group = ListController.this.mTrashAdapter.getGroup(groupPosition);
                    if (group == null) {
                        HwLog.e(ListController.TAG, "onGroupCollapse , group is null!");
                        return;
                    }
                    SpaceStatsUtils.reportExpandSpaceScanItemOp(group.getTrashType(), "0");
                }
            }
        });
    }

    public void setCleanning() {
        if (this.mTrashAdapter == null) {
            HwLog.e(TAG, "setCleanning called, but mTrashAdapter == null");
        } else {
            this.mTrashAdapter.setState(SpaceState.NORMAL_CLEANNING, null);
        }
    }

    public void setCleanEnd(OnClickListener mEnterDeepClicker, Fragment fragment, TrashScanHandler scanHandler) {
        this.mTrashList.setVisibility(8);
        this.mItemList.setVisibility(8);
    }

    public void refreshScanEndAdapter() {
        if (this.mTrashAdapter == null) {
            HwLog.e(TAG, "refreshScanEndAdapter called, but mTrashAdapter == null");
        } else {
            this.mTrashAdapter.notifyDataSetChanged();
        }
    }

    public TrashListAdapter getTrashListAdapter() {
        return this.mTrashAdapter;
    }

    public long getTrashSize() {
        if (this.mTrashAdapter != null) {
            return this.mTrashAdapter.getTotalSize();
        }
        HwLog.e(TAG, "getTrashSize called, but mTrashAdapter == null");
        return 0;
    }

    public void checkScanEnd(int trashTypes) {
        if (this.mScanningAdapter == null) {
            HwLog.e(TAG, "checkScanEnd, but mScanningAdapter == null !!");
        } else {
            this.mScanningAdapter.notifyScanEnd(trashTypes);
        }
    }
}
