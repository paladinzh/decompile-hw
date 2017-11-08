package com.huawei.permissionmanager.ui.permissionlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.ui.PermissionTableManager;
import com.huawei.permissionmanager.ui.permissionlist.GeneralItem.AddViewItem;
import com.huawei.permissionmanager.ui.permissionlist.GroupPermItem.CalendarPermItem;
import com.huawei.permissionmanager.ui.permissionlist.GroupPermItem.ContactsPermItem;
import com.huawei.permissionmanager.ui.permissionlist.GroupPermItem.MessagePermItem;
import com.huawei.permissionmanager.ui.permissionlist.GroupPermItem.PhonePermItem;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class PermissionAdapter extends BaseExpandableListAdapter {
    private static final int GROUP_TYPE_COUNT = 4;
    private static final int GROUP_TYPE_EXPANDEL = 3;
    private static final int GROUP_TYPE_GENERAL = 0;
    private static final int GROUP_TYPE_LABEL = 2;
    private static final int GROUP_TYPE_PERM = 1;
    private static final String TAG = "PermissionAdapter";
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<ListItem> mItems = Lists.newArrayList();

    public static class GenralHolder {
        public View divider;
        public TextView mDescription;
        public TextView mTitle;
    }

    public static class GroupPermHolder {
        public ImageView mIndicator;
        public TextView mTitle;
    }

    public static class LableHolder {
        public TextView mTitle;
    }

    public static class PermHolder {
        public View divider;
        public TextView mDescription;
        public TextView mTitle;
    }

    public static class Position {
        public int mChildPos;
        public int mGroupPos;
    }

    public PermissionAdapter(Context ctx) {
        this.mContext = ctx;
        this.mInflater = LayoutInflater.from(ctx);
    }

    public int getGroupCount() {
        return this.mItems.size();
    }

    public int getChildrenCount(int groupPosition) {
        ListItem item = (ListItem) this.mItems.get(groupPosition);
        if (item instanceof GroupPermItem) {
            return ((GroupPermItem) item).getChildCount();
        }
        return 0;
    }

    public Object getGroup(int groupPosition) {
        return this.mItems.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        ListItem item = (ListItem) this.mItems.get(groupPosition);
        if (item instanceof GroupPermItem) {
            return ((GroupPermItem) item).getSubItem(childPosition);
        }
        return null;
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

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newGroupView(groupPosition, isExpanded, parent);
        }
        bindGroupView(groupPosition, isExpanded, convertView, parent);
        return convertView;
    }

    private View newGroupView(int groupPosition, boolean isExpanded, ViewGroup parent) {
        View view;
        switch (getGroupType(groupPosition)) {
            case 0:
                view = this.mInflater.inflate(R.layout.permission_general_list_item, parent, false);
                GenralHolder holder = new GenralHolder();
                holder.mTitle = (TextView) view.findViewById(R.id.PermissionName);
                holder.divider = view.findViewById(R.id.divider);
                holder.mDescription = (TextView) view.findViewById(R.id.ApplicationCount);
                view.setTag(holder);
                return view;
            case 2:
                view = this.mInflater.inflate(R.layout.permission_list_tab_item_tag, parent, false);
                LableHolder holder2 = new LableHolder();
                holder2.mTitle = (TextView) view.findViewById(R.id.tvTagName);
                view.setTag(holder2);
                return view;
            case 3:
                view = this.mInflater.inflate(R.layout.permission_list_expande, parent, false);
                GroupPermHolder holder3 = new GroupPermHolder();
                holder3.mTitle = (TextView) view.findViewById(R.id.title);
                holder3.mIndicator = (ImageView) view.findViewById(R.id.indicate_arrow);
                view.setTag(holder3);
                return view;
            default:
                view = this.mInflater.inflate(R.layout.private_permission_list_view_item, parent, false);
                PermHolder holder4 = new PermHolder();
                holder4.mTitle = (TextView) view.findViewById(R.id.PermissionName);
                holder4.mDescription = (TextView) view.findViewById(R.id.ApplicationCount);
                holder4.divider = view.findViewById(R.id.divider);
                view.setTag(holder4);
                return view;
        }
    }

    private void bindGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            HwLog.e(TAG, "bindGroupView convertview is null!!");
            return;
        }
        ListItem item = (ListItem) this.mItems.get(groupPosition);
        Context ctx = this.mContext;
        if (item instanceof GeneralItem) {
            GenralHolder holder = (GenralHolder) convertView.getTag();
            AddViewItem adViewItem = (AddViewItem) item;
            holder.mTitle.setText(item.getTitle(ctx));
            holder.mDescription.setText(adViewItem.getDescription(ctx));
            if (groupPosition + 1 < getGroupCount()) {
                if (((ListItem) this.mItems.get(groupPosition + 1)) instanceof LabelItem) {
                    holder.divider.setVisibility(8);
                } else {
                    holder.divider.setVisibility(0);
                }
            }
        } else if (item instanceof LabelItem) {
            ((LableHolder) convertView.getTag()).mTitle.setText(item.getTitle(ctx));
        } else if (item instanceof GroupPermItem) {
            GroupPermHolder holder2 = (GroupPermHolder) convertView.getTag();
            holder2.mTitle.setText(item.getTitle(ctx));
            holder2.mIndicator.setImageResource(isExpanded ? R.drawable.expander_close_emui : R.drawable.expander_open_emui);
        } else if (item instanceof PermItem) {
            PermHolder holder3 = (PermHolder) convertView.getTag();
            PermItem permItem = (PermItem) item;
            holder3.mTitle.setText(permItem.getTitle(ctx));
            holder3.mDescription.setText(permItem.getDescription(ctx));
            if (groupPosition + 1 < getGroupCount()) {
                if (((ListItem) this.mItems.get(groupPosition + 1)) instanceof LabelItem) {
                    holder3.divider.setVisibility(8);
                } else {
                    holder3.divider.setVisibility(0);
                }
            }
        }
        convertView.setTag(R.id.convertview_tag_item, item);
    }

    public int getGroupTypeCount() {
        return 4;
    }

    public int getGroupType(int groupPosition) {
        ListItem item = (ListItem) this.mItems.get(groupPosition);
        if (item instanceof GeneralItem) {
            return 0;
        }
        if (item instanceof LabelItem) {
            return 2;
        }
        if (item instanceof GroupPermItem) {
            return 3;
        }
        return 1;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PermHolder holder;
        Context ctx = this.mContext;
        if (convertView == null) {
            View view = this.mInflater.inflate(R.layout.private_child_permission_list_view_item, parent, false);
            holder = new PermHolder();
            holder.mTitle = (TextView) view.findViewById(R.id.PermissionName);
            holder.mDescription = (TextView) view.findViewById(R.id.ApplicationCount);
            view.setTag(holder);
            convertView = view;
        }
        holder = (PermHolder) convertView.getTag();
        PermItem permItem = ((GroupPermItem) ((ListItem) this.mItems.get(groupPosition))).getSubItem(childPosition);
        holder.mTitle.setText(permItem.getTitle(ctx));
        holder.mDescription.setText(permItem.getDescription(ctx));
        convertView.setTag(R.id.convertview_tag_item, permItem);
        return convertView;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public List<Integer> getExpandablePos() {
        List<Integer> result = Lists.newArrayList();
        int size = this.mItems.size();
        for (int i = 0; i < size; i++) {
            if (((ListItem) this.mItems.get(i)) instanceof GroupPermItem) {
                result.add(Integer.valueOf(i));
            }
        }
        return result;
    }

    public Position findPositionByKey(String key) {
        Position position = new Position();
        int groupSize = this.mItems.size();
        for (int gPos = 0; gPos < groupSize; gPos++) {
            ListItem gItem = (ListItem) this.mItems.get(gPos);
            if (gItem instanceof GroupPermItem) {
                List<PermItem> children = ((GroupPermItem) gItem).getSubItems();
                int childSize = children.size();
                for (int cPos = 0; cPos < childSize; cPos++) {
                    if (Objects.equal(key, ((PermItem) children.get(cPos)).getKey())) {
                        position.mGroupPos = gPos;
                        position.mChildPos = cPos;
                        return position;
                    }
                }
                continue;
            } else if ((gItem instanceof ISearchKey) && Objects.equal(key, ((ISearchKey) gItem).getKey())) {
                position.mGroupPos = gPos;
                position.mChildPos = -1;
                return position;
            }
        }
        return position;
    }

    public void initData(PermissionTableManager permMgr, int addViewCount) {
        this.mItems.addAll(initPermList(permMgr, addViewCount));
        notifyDataSetChanged();
    }

    public static List<ListItem> initPermList(PermissionTableManager permMgr, int addViewCount) {
        boolean isWifiOnly = Utility.isWifiOnlyMode();
        List<ListItem> list = Lists.newArrayList();
        list.add(new LabelItem(R.string.BasicPermissionType));
        if (!isWifiOnly) {
            GroupPermItem messageItem = new MessagePermItem();
            messageItem.addItem(new PermItem(permMgr.getPermission(4)));
            messageItem.addItem(new PermItem(permMgr.getPermission(32)));
            list.add(messageItem);
            GroupPermItem phoneItem = new PhonePermItem();
            phoneItem.addItem(new PermItem(permMgr.getPermission(64)));
            phoneItem.addItem(new PermItem(permMgr.getPermission(2)));
            phoneItem.addItem(new PermItem(permMgr.getPermission(32768)));
            phoneItem.addItem(new PermItem(permMgr.getPermission(16)));
            list.add(phoneItem);
        }
        GroupPermItem contactItem = new ContactsPermItem();
        contactItem.addItem(new PermItem(permMgr.getPermission(1)));
        contactItem.addItem(new PermItem(permMgr.getPermission(16384)));
        list.add(contactItem);
        GroupPermItem calendarItem = new CalendarPermItem();
        calendarItem.addItem(new PermItem(permMgr.getPermission(2048)));
        calendarItem.addItem(new PermItem(permMgr.getPermission(ShareCfg.PERMISSION_MODIFY_CALENDAR)));
        list.add(calendarItem);
        list.add(new PermItem(permMgr.getPermission(1024)));
        list.add(new PermItem(permMgr.getPermission(128)));
        list.add(new PermItem(permMgr.getPermission(134217728)));
        list.add(new PermItem(permMgr.getPermission(8)));
        if (!isWifiOnly) {
            list.add(new PermItem(permMgr.getPermission(1048576)));
        }
        list.add(new LabelItem(R.string.PrivacyPermissionType));
        if (!isWifiOnly) {
            list.add(new PermItem(permMgr.getPermission(8192)));
        }
        list.add(new PermItem(permMgr.getPermission(67108864)));
        list.add(new PermItem(permMgr.getPermission(1073741824)));
        list.add(new PermItem(permMgr.getPermission(33554432)));
        list.add(new LabelItem(R.string.SettingsPermissionType));
        list.add(new PermItem(permMgr.getPermission(16777216)));
        list.add(new AddViewItem(addViewCount));
        return list;
    }

    public void changeAddViewCount(int addViewCount) {
        this.mItems.remove(this.mItems.size() - 1);
        this.mItems.add(new AddViewItem(addViewCount));
        notifyDataSetChanged();
    }
}
