package com.huawei.gallery.editor.screenshotseditor.app;

import android.content.Context;
import android.view.ViewGroup;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseMosaicUIController;
import com.huawei.gallery.editor.ui.ScreenShotsBrushUIController;

public class ScreenShotsPaintBrushState extends ScreenShotsBasePaintState {
    public ScreenShotsPaintBrushState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected BaseMosaicUIController createBaseMosaicUIController() {
        return new ScreenShotsBrushUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
    }
}
