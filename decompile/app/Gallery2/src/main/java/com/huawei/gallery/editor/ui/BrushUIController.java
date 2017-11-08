package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;

public class BrushUIController extends BasePaintUIController {
    public BrushUIController(Context context, ViewGroup parentLayout, BaseMosaicListener baseMosaicListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, baseMosaicListener, EditorViewDelegate);
    }

    public int getPaintType() {
        return 0;
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_brush_foot_bar : R.layout.editor_brush_foot_bar_land;
    }
}
