package com.huawei.gallery.map.app;

import android.view.Menu;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.app.SlotAlbumPage;

public class MapAlbumPage extends SlotAlbumPage {
    static final Action[] MAP_ALBUM_MENU = new Action[]{Action.SHARE, Action.DEL, Action.ALL, Action.NONE, Action.DETAIL};

    protected void initMenuItemCount() {
        this.mDefaultMenuItemCount = 4;
    }

    protected void onInflateMenu(Menu menu) {
        this.mMenu = MAP_ALBUM_MENU;
    }
}
