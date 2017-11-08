package com.huawei.gallery.actionbar;

public class StandardTitleActionMode extends ActionMode {
    protected void showHeadView() {
        if (this.mActionBar.getDisplayOptions() != 12) {
            this.mActionBar.setDisplayOptions(12);
        }
        if (this.mTitleStr != null) {
            this.mActionBar.setTitle(this.mTitleStr);
        } else if (this.mTitleRes > 0) {
            this.mActionBar.setTitle(this.mTitleRes);
        } else {
            this.mActionBar.setTitle("");
        }
    }

    public void setTitle(String title) {
        if (title != null) {
            this.mTitleStr = title;
            this.mTitleRes = -1;
            this.mActionBar.setTitle(title);
            return;
        }
        this.mActionBar.setTitle("");
    }

    public void setTitle(int titleResID) {
        if (titleResID > 0) {
            this.mTitleRes = titleResID;
            this.mTitleStr = null;
            this.mActionBar.setTitle(titleResID);
            return;
        }
        this.mActionBar.setTitle("");
    }

    public int getMode() {
        return 8;
    }

    protected void initViewItem() {
    }
}
