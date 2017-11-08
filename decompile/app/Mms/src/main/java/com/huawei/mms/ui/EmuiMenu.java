package com.huawei.mms.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import com.android.mms.MmsApp;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.ui.RcsEmuiMenu;

@SuppressLint({"InlinedApi"})
public class EmuiMenu {
    private static RcsEmuiMenu mHwCust = new RcsEmuiMenu();
    private Context mContext;
    protected Menu mOptionMenu;

    public EmuiMenu(Menu optionMenu) {
        if (optionMenu == null) {
            MLog.e("Mms:UI", "EmuiMenu create with empty OptionMenu");
        }
        this.mOptionMenu = optionMenu;
    }

    private Context getContext() {
        if (this.mContext == null) {
            return MmsApp.getApplication().getApplicationContext();
        }
        return this.mContext;
    }

    public void clear() {
        if (this.mOptionMenu != null) {
            this.mOptionMenu.clear();
        }
    }

    public EmuiMenu setContext(Context context) {
        this.mContext = context;
        return this;
    }

    public EmuiMenu resetOptionMenu(Menu optionMenu) {
        if (optionMenu == null) {
            MLog.e("Mms:UI", "EmuiMenu reset will empty OptionMenu");
        }
        this.mOptionMenu = optionMenu;
        return this;
    }

    public void setItemVisible(int itemID, boolean isVisible) {
        if (this.mOptionMenu != null) {
            setItemVisible(this.mOptionMenu, itemID, isVisible);
        }
    }

    public void setItemEnabled(int itemID, boolean isVisible) {
        if (this.mOptionMenu != null) {
            setItemEnabled(this.mOptionMenu, itemID, isVisible);
        }
    }

    protected Drawable getMenuDrawable(int icon) {
        return ResEx.self().getStateListDrawable(getContext(), icon);
    }

    public MenuItem addMenu(int groupid, int itemId, int title, int icon) {
        return addMenu(this.mOptionMenu, groupid, itemId, title, getMenuDrawable(icon), false);
    }

    public MenuItem addMenu(int itemId, int title, int icon) {
        if (this.mOptionMenu == null) {
            return null;
        }
        return addMenu(this.mOptionMenu, itemId, title, getMenuDrawable(icon));
    }

    public MenuItem addOverflowMenu(int itemId, int title) {
        return addOverflowMenu(0, itemId, title);
    }

    public MenuItem addOverflowMenu(int groupid, int itemId, int title) {
        if (this.mOptionMenu == null) {
            return null;
        }
        return addOverflowMenu(this.mOptionMenu, groupid, itemId, title);
    }

    public void setAllChecked(boolean allChked, boolean isLandsCape) {
        if (this.mOptionMenu != null) {
            setAllChecked(getContext(), this.mOptionMenu, allChked, isLandsCape);
        }
    }

    public MenuItem addMenuChoice(boolean isLandsCape) {
        return addMenu(278925313, R.string.menu_select_all, getDrawableId(278925313, isLandsCape));
    }

    public MenuItem addMenuDelete(boolean isLandsCape) {
        return addMenu(278925315, R.string.delete, getDrawableId(278925315, isLandsCape));
    }

    public MenuItem addMenuFavorite(boolean isLandsCape) {
        return addMenu(278925318, R.string.pop_menu_multy_add_favorites, getDrawableId(278925318, isLandsCape));
    }

    public MenuItem addMenuForawrd(boolean isLandsCape) {
        return addMenu(278925316, R.string.forward_message, getDrawableId(278925316, isLandsCape));
    }

    public MenuItem addMenuLock(boolean isLandsCape) {
        return addMenu(278925331, R.string.menu_lock, getDrawableId(278925331, isLandsCape));
    }

    public MenuItem addMenuUnLock(boolean isLandsCape) {
        return addMenu(278925332, R.string.menu_unlock, getDrawableId(278925332, isLandsCape));
    }

    public MenuItem addMenuSetting(boolean isLandsCape) {
        return addMenu(278925317, R.string.menu_settings, getDrawableId(278925317, isLandsCape));
    }

    public MenuItem addMenuRestore(boolean isLandsCape) {
        return addMenu(278925339, R.string.restore_default, getDrawableId(278925339, isLandsCape));
    }

    public MenuItem addMenuSaveAttachments(boolean isLandsCape) {
        return addMenu(278925342, R.string.save_all_attachment, getDrawableId(278925342, isLandsCape));
    }

    public MenuItem addMenuCopyToPhone(boolean isLandsCape) {
        return addMenu(278925336, R.string.menu_copy_to_phone, getDrawableId(278925319, isLandsCape));
    }

    private void setAllChecked(Context context, Menu optionMenu, boolean checked, boolean isLandsCape) {
        int i;
        int resId = checked ? R.drawable.csp_selected_all_highlight : getDrawableId(278925313, isLandsCape);
        int controlColor = HwUiStyleUtils.getControlColor(context.getResources());
        Drawable checkboxDrawable = ResEx.self().getStateListDrawable(context, resId);
        if (controlColor != 0 && checked) {
            checkboxDrawable = ResEx.self().getStateListDrawable(context, 0);
            if (checkboxDrawable == null) {
                checkboxDrawable = HwUiStyleUtils.getColorfulThemeDrawable(context, controlColor);
                ResEx.self().addDrawableCache(checkboxDrawable, 0);
            }
        }
        if (checked) {
            i = R.string.menu_disselect_all_new;
        } else {
            i = R.string.menu_select_all;
        }
        resetMenu(optionMenu, 278925313, i, checkboxDrawable, checked);
    }

    public static void setItemVisible(Menu optionMenu, int itemID, boolean isVisible) {
        MenuItem menuItem = optionMenu.findItem(itemID);
        if (menuItem != null) {
            menuItem.setVisible(isVisible);
            setItemEnabled(optionMenu, itemID, isVisible);
        }
    }

    public static MenuItem addOverflowMenu(Menu optionMenu, int groupId, int itemId, int title) {
        return addMenu(optionMenu, groupId, itemId, title, null, false);
    }

    public static MenuItem addMenu(Menu optionMenu, int itemId, int title, Drawable icon) {
        return addMenu(optionMenu, 0, itemId, title, icon, false);
    }

    public static MenuItem addMenu(Menu optionMenu, int groupId, int itemId, int title, Drawable icon, boolean checked) {
        MenuItem item = optionMenu.findItem(itemId);
        if (item != null) {
            item.setVisible(true);
            item.setEnabled(true);
            item.setIcon(icon);
            return item;
        }
        item = optionMenu.add(groupId, itemId, 0, title);
        item.setCheckable(checked);
        if (icon != null) {
            item.setIcon(icon);
            item.setShowAsAction(2);
        } else {
            item.setShowAsAction(0);
        }
        return item;
    }

    public static MenuItem resetMenu(Menu optionMenu, int itemId, int title, Drawable icon) {
        return resetMenu(optionMenu, itemId, title, icon, false);
    }

    public static MenuItem resetMenu(Menu optionMenu, int itemId, int title, Drawable icon, boolean checked) {
        if (optionMenu == null) {
            return null;
        }
        MenuItem item = optionMenu.findItem(itemId);
        if (item != null) {
            item.setTitle(title);
            item.setIcon(icon);
            item.setShowAsAction(2);
            item.setChecked(checked);
            return item;
        }
        item = optionMenu.add(0, itemId, 0, title);
        item.setIcon(icon);
        item.setShowAsAction(2);
        item.setChecked(checked);
        return item;
    }

    public static void setItemEnabled(Menu optionMenu, int itemID, boolean enabled) {
        MenuItem menuItem = optionMenu.findItem(itemID);
        if (menuItem != null) {
            menuItem.setEnabled(enabled);
        }
    }

    protected int getDrawableId(int menuId, boolean isInLandscape) {
        switch (menuId) {
            case 278925313:
                return R.drawable.mms_menu_choose;
            case 278925315:
                return R.drawable.mms_menu_trash;
            case 278925316:
                return R.drawable.mms_menu_forward;
            case 278925317:
                return R.drawable.mms_menu_setting;
            case 278925318:
                return R.drawable.mms_menu_favorite;
            case 278925319:
                return R.drawable.mms_menu_reproduce;
            case 278925320:
                return R.drawable.mms_menu_search;
            case 278925321:
                return R.drawable.mms_menu_resend;
            case 278925322:
                return R.drawable.mms_menu_edit;
            case 278925331:
                return R.drawable.mms_menu_lock;
            case 278925332:
                return R.drawable.mms_menu_unlock;
            case 278925337:
                return R.drawable.mms_menu_reply;
            case 278925339:
                return R.drawable.mms_menu_restore;
            case 278925341:
                return R.drawable.mms_menu_resend_all;
            case 278925342:
                return R.drawable.mms_ic_save;
            case 278927460:
                return R.drawable.mms_menu_add;
            case 278927461:
                return R.drawable.mms_menu_pin;
            case 278927463:
                return R.drawable.mms_icon_bottom_filter;
            case 278927465:
                return R.drawable.mms_menu_all_read_image;
            default:
                if (mHwCust != null) {
                    int resId = mHwCust.getExtendNotLandscapeDrawableId(menuId);
                    if (resId != -1) {
                        return resId;
                    }
                }
                return -1;
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
    }
}
