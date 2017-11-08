package com.huawei.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import com.google.android.gms.R;
import com.huawei.mms.util.ResEx;

public class MmsMenuItemImpl implements MenuItem {
    private Context mContext;
    private int mId;
    private boolean mIsCheckable = true;
    private boolean mIsChecked = false;
    private boolean mIsEnabled = true;
    private boolean mIsVisiable = true;
    private EmuiMenuImage mMenuItemImage;
    private EmuiMenuText mMenuItemView;
    private int mShowAsAction;
    private CharSequence mTitle;

    public MmsMenuItemImpl(Context context, int id, CharSequence title) {
        this.mContext = context;
        this.mId = id;
        this.mTitle = title;
    }

    private void checkIfMenuItemExist() {
        if (this.mMenuItemView == null) {
            this.mMenuItemView = (EmuiMenuText) View.inflate(this.mContext, R.layout.mms_menu_item, null);
            this.mMenuItemView.setId(this.mId);
            this.mMenuItemView.setText(this.mTitle);
            this.mMenuItemView.setClickable(this.mIsEnabled);
            this.mMenuItemView.setFocusable(this.mIsEnabled);
            this.mMenuItemView.setActivated(this.mIsEnabled);
        }
    }

    private void checkIfMenuImageExist() {
        if (this.mMenuItemImage == null) {
            this.mMenuItemImage = new EmuiMenuImage(this.mContext);
            this.mMenuItemImage.setClickable(this.mIsEnabled);
            this.mMenuItemImage.setFocusable(this.mIsEnabled);
            this.mMenuItemImage.setActivated(this.mIsEnabled);
        }
    }

    public boolean collapseActionView() {
        return false;
    }

    public boolean expandActionView() {
        return false;
    }

    public ActionProvider getActionProvider() {
        return null;
    }

    public View getActionView() {
        return null;
    }

    public char getAlphabeticShortcut() {
        return '\u0000';
    }

    public int getGroupId() {
        return 0;
    }

    public Drawable getIcon() {
        return null;
    }

    public Intent getIntent() {
        return null;
    }

    public int getItemId() {
        return this.mId;
    }

    public ContextMenuInfo getMenuInfo() {
        return null;
    }

    public char getNumericShortcut() {
        return '\u0000';
    }

    public int getOrder() {
        return 0;
    }

    public SubMenu getSubMenu() {
        return null;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getTitleCondensed() {
        return null;
    }

    public boolean hasSubMenu() {
        return false;
    }

    public boolean isActionViewExpanded() {
        return false;
    }

    public boolean isCheckable() {
        return this.mIsCheckable;
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public boolean isVisible() {
        return this.mIsVisiable;
    }

    public MenuItem setActionProvider(ActionProvider actionProvider) {
        return null;
    }

    public MenuItem setActionView(View view) {
        return null;
    }

    public MenuItem setActionView(int resId) {
        return null;
    }

    public MenuItem setAlphabeticShortcut(char alphaChar) {
        return null;
    }

    public MenuItem setCheckable(boolean checkable) {
        this.mIsCheckable = checkable;
        return this;
    }

    public MenuItem setChecked(boolean checked) {
        this.mIsChecked = checked;
        return this;
    }

    public MenuItem setEnabled(boolean enabled) {
        this.mIsEnabled = enabled;
        if (this.mMenuItemView != null) {
            this.mMenuItemView.setClickable(enabled);
            this.mMenuItemView.setFocusable(enabled);
            this.mMenuItemView.setActivated(enabled);
        }
        if (this.mMenuItemImage != null) {
            this.mMenuItemImage.setClickable(enabled);
            this.mMenuItemImage.setFocusable(enabled);
            this.mMenuItemImage.setActivated(enabled);
        }
        return this;
    }

    public MenuItem setIcon(Drawable icon) {
        checkIfMenuItemExist();
        this.mMenuItemView.setIcon(icon);
        checkIfMenuImageExist();
        this.mMenuItemImage.setImageDrawable(icon);
        return this;
    }

    public MenuItem setIcon(int iconRes) {
        setIcon(ResEx.self().getCachedDrawable(iconRes));
        return this;
    }

    public MenuItem setIntent(Intent intent) {
        return null;
    }

    public MenuItem setNumericShortcut(char numericChar) {
        return null;
    }

    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        return null;
    }

    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        return null;
    }

    public MenuItem setShortcut(char numericChar, char alphaChar) {
        return null;
    }

    public void setShowAsAction(int actionEnum) {
        this.mShowAsAction = actionEnum;
        if (actionEnum > 0) {
            checkIfMenuItemExist();
        }
    }

    public MenuItem setShowAsActionFlags(int actionEnum) {
        return null;
    }

    public MenuItem setTitle(CharSequence title) {
        this.mTitle = title;
        if (this.mMenuItemView != null) {
            this.mMenuItemView.setText(title);
        }
        return this;
    }

    public MenuItem setTitle(int titleRes) {
        return setTitle(this.mContext.getResources().getString(titleRes));
    }

    public MenuItem setTitleCondensed(CharSequence title) {
        return null;
    }

    public MenuItem setVisible(boolean visible) {
        this.mIsVisiable = visible;
        return this;
    }

    public View getItemView(boolean isTop) {
        if (isTop) {
            checkIfMenuImageExist();
            return this.mMenuItemImage;
        }
        checkIfMenuItemExist();
        return this.mMenuItemView;
    }

    public boolean requiresActionButton() {
        return (this.mShowAsAction & 2) == 2;
    }
}
