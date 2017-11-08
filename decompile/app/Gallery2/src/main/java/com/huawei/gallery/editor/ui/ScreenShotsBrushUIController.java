package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.ScreenShotsBasePaintUIController.ScreenBasePaintListener;

public class ScreenShotsBrushUIController extends ScreenShotsBasePaintUIController {
    public ScreenShotsBrushUIController(Context context, ViewGroup parentLayout, ScreenBasePaintListener basePaintListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, basePaintListener, EditorViewDelegate);
    }

    protected int getContainerLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.screen_shots_paint_brush_bar : R.layout.screen_shots_paint_brush_bar_land;
    }

    protected int getMosaicViewId() {
        return R.id.screenshots_editor_mosiac;
    }

    public int getPaintType() {
        return 0;
    }
}
