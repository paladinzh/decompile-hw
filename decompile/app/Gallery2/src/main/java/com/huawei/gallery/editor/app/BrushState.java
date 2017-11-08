package com.huawei.gallery.editor.app;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseMosaicUIController;
import com.huawei.gallery.editor.ui.BrushUIController;

public class BrushState extends BasePaintState {
    public BrushState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected BaseMosaicUIController createBaseMosaicUIController() {
        return new BrushUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
    }

    protected int getActionNameId() {
        return R.string.editor_brush;
    }
}
