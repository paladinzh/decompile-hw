package com.huawei.mms.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;

public class MmsMenuImpl implements Menu {
    private Context mContext;
    private ArrayList<MenuItem> mMenuItems = new ArrayList();

    public MmsMenuImpl(Context context) {
        this.mContext = context;
    }

    public MenuItem add(CharSequence title) {
        return null;
    }

    public MenuItem add(int titleRes) {
        return null;
    }

    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        if (itemId < 0) {
            MLog.e("MmsMenuImpl", "menuId must larger than 0");
            return null;
        }
        MenuItem item = findItem(itemId);
        if (item != null) {
            item.setTitle(title);
            return item;
        }
        item = new MmsMenuItemImpl(this.mContext, itemId, title);
        this.mMenuItems.add(item);
        return item;
    }

    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, this.mContext.getText(titleRes));
    }

    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return 0;
    }

    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    public void clear() {
        this.mMenuItems.clear();
    }

    public void close() {
    }

    public MenuItem findItem(int id) {
        for (MenuItem item : this.mMenuItems) {
            if (item.getItemId() == id) {
                return item;
            }
        }
        return null;
    }

    public MenuItem getItem(int index) {
        return (MenuItem) this.mMenuItems.get(index);
    }

    public boolean hasVisibleItems() {
        return false;
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean performIdentifierAction(int id, int flags) {
        return false;
    }

    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return false;
    }

    public void removeGroup(int groupId) {
        clear();
    }

    public void removeItem(int id) {
        this.mMenuItems.remove(findItem(id));
    }

    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
    }

    public void setGroupEnabled(int group, boolean enabled) {
    }

    public void setGroupVisible(int group, boolean visible) {
    }

    public void setQwertyMode(boolean isQwerty) {
    }

    public int size() {
        return this.mMenuItems.size();
    }
}
