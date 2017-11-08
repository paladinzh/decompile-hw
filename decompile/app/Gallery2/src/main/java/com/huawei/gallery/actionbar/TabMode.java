package com.huawei.gallery.actionbar;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import com.android.gallery3d.util.ActionBarExWrapper;
import com.android.gallery3d.util.GalleryLog;

public class TabMode extends ActionBarStateBase {
    public void addTab(CharSequence title, TabListener listener, int tabId) {
        if (this.mActionBar.getNavigationMode() == 2) {
            Tab tab = this.mActionBar.newTab();
            ActionBarExWrapper.setTabViewId(tab, tabId);
            GalleryLog.d("TabMode", "addTab: tab = " + tab.hashCode() + "tabId = " + tabId + " title = " + title);
            this.mActionBar.addTab(tab.setText(title).setTabListener(listener));
            GalleryLog.d("TabMode", "NavigationItemCount = " + this.mActionBar.getNavigationItemCount());
        }
    }

    public void setSelectedNavigationItem(int position) {
        if (this.mActionBar.getNavigationMode() == 2) {
            GalleryLog.d("TabMode", "NavigationItemCount = " + this.mActionBar.getNavigationItemCount());
            this.mActionBar.setSelectedNavigationItem(position);
        }
    }

    public int getMode() {
        return 1;
    }

    protected void showHeadView() {
        if (this.mActionBar.getNavigationMode() != 2) {
            this.mActionBar.setNavigationMode(2);
            this.mActionBar.setDisplayOptions(0);
        }
    }
}
