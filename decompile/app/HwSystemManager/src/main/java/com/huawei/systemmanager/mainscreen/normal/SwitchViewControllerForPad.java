package com.huawei.systemmanager.mainscreen.normal;

import android.app.Activity;
import android.os.Message;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HSMConst;

class SwitchViewControllerForPad extends SwitchViewController {
    private static final int COLUMN_LAND = 3;
    private static final int COLUMN_POR_DEFAULT = 4;
    private static final int COLUMN_POR_SPECIAL = 3;

    SwitchViewControllerForPad(Activity ac, View mainView) {
        super(ac, mainView);
    }

    protected void ensureEntryViewForDevice() {
        this.mMainView.findViewById(R.id.dot_bar_layout).setVisibility(8);
    }

    protected void sendHandlerMsg() {
    }

    public void onHandleMessage(Message msg) {
    }

    protected boolean setEmptyEntryViewVisibleGone(View view, int realPos, boolean isLand) {
        if (realPos < this.mEntries.size()) {
            return false;
        }
        if (isLand) {
            view.setVisibility(8);
            return true;
        }
        view.setVisibility(0);
        return false;
    }

    protected int getPageCount() {
        return 1;
    }

    protected int getEntryViewCountPerPage() {
        return 9;
    }

    protected int getColumn() {
        if (HSMConst.isLand() || this.mEntries.size() < 6 || this.mEntries.size() % 3 == 0) {
            return 3;
        }
        return 4;
    }
}
