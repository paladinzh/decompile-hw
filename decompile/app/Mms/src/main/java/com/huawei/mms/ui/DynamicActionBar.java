package com.huawei.mms.ui;

import com.huawei.mms.ui.EmuiListView_V3.ListViewDragoutListener;

public class DynamicActionBar implements ActionBarInterface, ListViewDragoutListener {
    private ActionBarInterface mActionBarUsed;
    private PeopleActionBar mPeopleActionBar;

    public void onMove(int offset) {
        if (isPeopleActionBarUsed()) {
            this.mPeopleActionBar.showOrHideMenu(true);
            this.mPeopleActionBar.setTranslateY(offset);
        }
    }

    public void onPullUP(int offset) {
        if (isPeopleActionBarUsed()) {
            this.mPeopleActionBar.expandOrCollapseActionBar(offset);
        }
    }

    public boolean isLayoutExpand() {
        if (isPeopleActionBarUsed()) {
            return this.mPeopleActionBar.isActionBarExpand();
        }
        return false;
    }

    public void hideTheKeyboard() {
        if (isPeopleActionBarUsed()) {
            this.mPeopleActionBar.hideTheKeyboard();
        }
    }

    public void resetHideKeyBoardSign() {
        if (isPeopleActionBarUsed()) {
            this.mPeopleActionBar.resetHideKeyBoardSign();
        }
    }

    public boolean isPeopleActionBarUsed() {
        return this.mPeopleActionBar == this.mActionBarUsed;
    }
}
