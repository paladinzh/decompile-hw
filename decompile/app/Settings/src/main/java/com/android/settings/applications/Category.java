package com.android.settings.applications;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private List<AppInfo> mCategoryItem = new ArrayList();
    private String mCategoryName;

    public Category(String mCategroyName) {
        this.mCategoryName = mCategroyName;
    }

    public Object getItem(int position) {
        if (position == 0) {
            return this.mCategoryName;
        }
        return this.mCategoryItem.get(position - 1);
    }

    public Object getOneTypeItem(int position) {
        return this.mCategoryItem.get(position);
    }

    public void setmCategoryItem(List<AppInfo> mCategoryItem) {
        this.mCategoryItem = mCategoryItem;
    }

    public int getItemCount() {
        return this.mCategoryItem.size() + 1;
    }

    public int getOneTypeItemCount() {
        return this.mCategoryItem.size();
    }
}
