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
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.ui.SpaceState;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppProcessTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.ChildType.ChildViewHolder;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.GroupType.GroupTypeFactory;
import java.util.List;

public class TrashListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "TrashListAdapter";
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

    public TrashListAdapter(Context context, OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker, OnClickListener groupCheckClicker) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mItemClicker = itemClicker;
        this.mGroupCheckClicker = groupCheckClicker;
        initGroupViewType();
        initChildType(checkClicker, itemClicker, longClicker);
    }

    private void initChildType(OnClickListener checkClicker, OnClickListener itemClicker, OnLongClickListener longClicker) {
        ChildType checkType = new ChildTypeCheck(this.mLayoutInflater, checkClicker, itemClicker, longClicker);
        this.mChildType.put(checkType.getType(), checkType);
        ChildType emptyType = new ChildTypeEmpty(this.mLayoutInflater);
        this.mChildType.put(emptyType.getType(), emptyType);
        ChildTypeJump jumpType = new ChildTypeJump(this.mLayoutInflater, itemClicker);
        this.mChildType.put(jumpType.getType(), jumpType);
    }

    private void initGroupViewType() {
        for (GroupTypeFactory factory : Lists.newArrayList(GroupTypeEmpty.sEmptyGroupFactory, GroupTypeExpande.sExpandeGroupFactory, GroupTypeJump.sJumpGroupFactory, GroupTypeRound.sRopundGroupFactory)) {
            this.mGroupType.put(factory.getType(), factory);
        }
    }

    public void setList(ExpandableListView lv) {
        this.mListView = lv;
        lv.setAdapter(this);
    }

    public void setState(SpaceState state, List<TrashItemGroup> data) {
        this.mState = state;
        if (data != null) {
            this.mTrashes.clear();
            this.mTrashes.addAll(data);
        }
        refreshAllContentState();
        int size = this.mChildType.size();
        for (int i = 0; i < size; i++) {
            ((ChildType) this.mChildType.valueAt(i)).setSpace(this.mState);
        }
        notifyDataSetChanged();
        if (this.mState.isScanEnd() && !isNoTrash()) {
            expandAllGroup();
        }
    }

    private void refreshAllContentState() {
        for (TrashItemGroup group : this.mTrashes) {
            group.refreshContent();
        }
    }

    public List<Trash> getCheckedTrashes() {
        List<Trash> trashes = Lists.newArrayList();
        for (TrashItemGroup itemGroup : this.mTrashes) {
            trashes.addAll(itemGroup.getUncleanedCheckedTrash());
        }
        return trashes;
    }

    public long getCheckedSizeUncleaned() {
        long size = 0;
        for (TrashItemGroup<ITrashItem> itemGroup : this.mTrashes) {
            for (ITrashItem child : itemGroup) {
                if (child.isChecked() && !child.isCleaned()) {
                    size += child.getTrashSizeCleaned(false);
                }
            }
        }
        return size;
    }

    public boolean isNoTrash() {
        for (TrashItemGroup item : this.mTrashes) {
            if (!item.isNoTrash()) {
                return false;
            }
        }
        return true;
    }

    public long getTotalSize() {
        long size = 0;
        for (TrashItemGroup item : this.mTrashes) {
            size += item.getTrashSize();
        }
        return size;
    }

    public int getGroupTypeCount() {
        return this.mGroupType.size();
    }

    public int getGroupType(int groupPosition) {
        if (this.mState.isScanning()) {
            return 0;
        }
        TrashItemGroup itemGroup = getGroup(groupPosition);
        if (itemGroup.getSize() <= 0) {
            return 3;
        }
        if (itemGroup.getTrashType() == 131072) {
            return 2;
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
        if (item.getTrashType() == 131072) {
            return 2;
        }
        if (item.isNoTrash()) {
            return 1;
        }
        return 0;
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

    public void handlerRefreshSize() {
        refreshAllContentState();
        notifyDataSetChanged();
    }

    public boolean checkIsAllCleaned() {
        for (TrashItemGroup itemGroup : this.mTrashes) {
            if (!itemGroup.isEmpty() && !itemGroup.isCleaned()) {
                return false;
            }
        }
        return true;
    }

    public void expandAllGroup() {
        if (this.mListView != null) {
            int groupCount = getGroupCount();
            for (int i = 0; i < groupCount; i++) {
                if (((TrashItemGroup) this.mTrashes.get(i)).getTrashType() != 32768) {
                    this.mListView.expandGroup(i);
                }
            }
        }
    }

    public void refreshAppProcess(boolean notifyAdapter) {
        Iterable items = null;
        for (TrashItemGroup group : this.mTrashes) {
            if (group.getTrashType() == 32768) {
                items = group;
                break;
            }
        }
        if (r4 != null && !r4.isEmpty()) {
            for (ITrashItem item : r4) {
                ((AppProcessTrashItem) item).refreshProtectState();
            }
            if (notifyAdapter) {
                notifyDataSetChanged();
            }
        }
    }
}
