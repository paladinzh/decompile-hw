package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.ScreenShotsBasePaintUIController.ScreenBasePaintListener;

public class ScreenShotsEraserUIController extends ScreenShotsMosaicUIController {
    public ScreenShotsEraserUIController(Context context, ViewGroup parentLayout, ScreenBasePaintListener basePaintListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, basePaintListener, EditorViewDelegate);
    }

    public void show() {
        super.show();
        if (this.mMosaicView != null) {
            this.mMosaicView.setStrokeType(1);
        }
    }

    public int getSubMenuHeight() {
        return 0;
    }

    public boolean forceAddData() {
        return this.mBasePaintListener.forceAddData();
    }

    protected int getContainerLayout() {
        return 0;
    }

    public boolean supportMenu() {
        return false;
    }

    public int getPaintType() {
        return 0;
    }
}
