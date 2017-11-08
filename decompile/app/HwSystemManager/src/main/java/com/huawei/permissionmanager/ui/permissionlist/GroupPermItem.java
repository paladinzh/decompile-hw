package com.huawei.permissionmanager.ui.permissionlist;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.ListItem;
import java.util.Collections;
import java.util.List;

public abstract class GroupPermItem implements ListItem {
    private final List<PermItem> mSubItemList = Lists.newArrayList();

    public static class CalendarPermItem extends GroupPermItem {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.permgrouplab_calendar);
        }
    }

    public static class ContactsPermItem extends GroupPermItem {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.permission_group_use_contact);
        }
    }

    public static class MessagePermItem extends GroupPermItem {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.permission_group_use_message);
        }
    }

    public static class PhonePermItem extends GroupPermItem {
        public String getTitle(Context ctx) {
            return ctx.getString(R.string.permission_group_use_phone);
        }
    }

    public void addItem(PermItem item) {
        if (item != null) {
            this.mSubItemList.add(item);
        }
    }

    public int getChildCount() {
        return this.mSubItemList.size();
    }

    public PermItem getSubItem(int position) {
        return (PermItem) this.mSubItemList.get(position);
    }

    public List<PermItem> getSubItems() {
        return Collections.unmodifiableList(this.mSubItemList);
    }
}
