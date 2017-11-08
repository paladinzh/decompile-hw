package com.huawei.systemmanager.spacecleanner.ui.trashlistadapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.DownloadAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;
import java.util.List;

public class AppTrashListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "AppTrashListAdapter";
    private boolean mCanShowProgress = false;
    private SparseArray<ChildType> mChildType = new SparseArray();
    private final Context mContext;
    private final OnClickListener mGroupCheckClicker;
    private SparseArray<GroupTypeFactory> mGroupType = new SparseArray();
    private final OnClickListener mItemClicker;
    private final LayoutInflater mLayoutInflater;
    private ExpandableListView mListView;
    private SpaceState mState = SpaceState.NORMAL_SCANNING;
    private final List<TrashItemGroup> mTrashes = Lists.newArrayList();

    public AppTrashListAdapter(Context context, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker, OnClickListener groupCheckClicker) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mItemClicker = itemClicker;
        this.mGroupCheckClicker = groupCheckClicker;
        initGroupViewType();
        initChildType(checkClicker, itemClicker, longClicker);
    }

    private void initChildType(OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker) {
        AppCacheChildTypeCheck AppCacheCheckType = new AppCacheChildTypeCheck(this.mLayoutInflater, checkClicker, itemClicker, longClicker);
        this.mChildType.put(AppCacheCheckType.getType(), AppCacheCheckType);
        ChildType emptyType = new ChildTypeEmpty(this.mLayoutInflater);
        this.mChildType.put(emptyType.getType(), emptyType);
        ChildTypeJump jumpType = new ChildTypeJump(this.mLayoutInflater, itemClicker);
        this.mChildType.put(jumpType.getType(), jumpType);
        VideoChildTypeCheck videoCheckType = new VideoChildTypeCheck(this.mLayoutInflater, checkClicker, itemClicker, longClicker);
        this.mChildType.put(videoCheckType.getType(), videoCheckType);
        ImageChildTypeCheck imageCheckType = new ImageChildTypeCheck(this.mLayoutInflater, checkClicker, itemClicker, longClicker);
        this.mChildType.put(imageCheckType.getType(), imageCheckType);
        ApkChildTypeCheck apkCheckType = new ApkChildTypeCheck(this.mLayoutInflater, checkClicker, itemClicker, longClicker);
        this.mChildType.put(apkCheckType.getType(), apkCheckType);
    }

    private void initGroupViewType() {
        for (GroupTypeFactory factory : Lists.newArrayList(AppCacheGroupTypeExpande.sExpandeGroupFactory, GroupTypeJump.sJumpGroupFactory, GroupTypeRound.sRopundGroupFactory)) {
            this.mGroupType.put(factory.getType(), factory);
        }
    }

    public void setList(ExpandableListView lv) {
        this.mListView = lv;
        lv.setAdapter(this);
    }

    public void setData(List<TrashItemGroup> data) {
        if (data != null) {
            this.mTrashes.clear();
            this.mTrashes.addAll(data);
        }
        refreshAllContentState();
        notifyDataSetChanged();
        expandAllGroup();
    }

    public List<TrashItemGroup> getData() {
        return this.mTrashes;
    }

    public int getGroupTypeCount() {
        return this.mGroupType.size();
    }

    public int getGroupType(int groupPosition) {
        if (getGroup(groupPosition).getSize() <= 0) {
            return 3;
        }
        return 1;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((GroupTypeFactory) this.mGroupType.get(getGroupType(groupPosition))).newView(this.mLayoutInflater, parent);
        }
        ((GroupType) convertView.getTag()).bindView(isExpanded, getGroup(groupPosition), this.mState, this.mItemClicker, Boolean.valueOf(this.mCanShowProgress), this.mGroupCheckClicker);
        return convertView;
    }

    public int getChildTypeCount() {
        return this.mChildType.size();
    }

    public int getChildType(int groupPosition, int childPosition) {
        ITrashItem item = getChild(groupPosition, childPosition);
        if (256 == item.getTrashType()) {
            return 3;
        }
        if (!(item instanceof DownloadAppTrashItem)) {
            return 0;
        }
        int childType = 0;
        switch (((DownloadAppTrashItem) item).getFileType()) {
            case 2:
                childType = 3;
                break;
            case 3:
                childType = 4;
                break;
            case 4:
                childType = 5;
                break;
        }
        return childType;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ITrashItem item = getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = ((ChildType) this.mChildType.get(getChildType(groupPosition, childPosition))).newView(groupPosition, childPosition, isLastChild, parent, item);
        }
        ((ChildViewHolder) convertView.getTag()).bindView(isLastChild, convertView, item);
        return convertView;
    }

    public int getGroupCount() {
        return this.mTrashes.size();
    }

    public int getChildrenCount(int groupPosition) {
        return ((TrashItemGroup) this.mTrashes.get(groupPosition)).getSize();
    }

    public TrashItemGroup getGroup(int groupPosition) {
        return (TrashItemGroup) this.mTrashes.get(groupPosition);
    }

    public ITrashItem getChild(int groupPosition, int childPosition) {
        return ((TrashItemGroup) this.mTrashes.get(groupPosition)).getItem(childPosition);
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void refreshAllContentState() {
        for (TrashItemGroup group : this.mTrashes) {
            group.refreshContent();
        }
    }

    public void expandAllGroup() {
        if (this.mListView != null) {
            int groupCount = getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                this.mListView.expandGroup(i);
            }
        }
    }

    public void collapseGroup() {
        if (this.mListView != null) {
            int groupCount = getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                this.mListView.collapseGroup(i);
            }
        }
    }
}
