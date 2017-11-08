package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;

public class MosaicUIController extends BasePaintUIController {
    public MosaicUIController(Context context, ViewGroup parentLayout, BaseMosaicListener baseMosaicListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, baseMosaicListener, EditorViewDelegate);
    }

    public int getPaintType() {
        return 1;
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_mosaic_foot_bar : R.layout.editor_mosaic_foot_bar_land;
    }
}
