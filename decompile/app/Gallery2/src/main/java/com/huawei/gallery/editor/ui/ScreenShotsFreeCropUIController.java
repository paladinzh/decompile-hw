package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;

public class ScreenShotsFreeCropUIController extends EditorUIController {
    public ScreenShotsFreeCropUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public int getSubMenuHeight() {
        return 0;
    }

    public void show() {
        GalleryLog.d("ScreenShotsFreeCropUIController", "no need show");
    }

    public void hide() {
        GalleryLog.d("ScreenShotsFreeCropUIController", "no need hide");
    }

    public void onConfigurationChanged() {
        GalleryLog.d("ScreenShotsFreeCropUIController", "no need onConfigurationChanged");
    }
}
