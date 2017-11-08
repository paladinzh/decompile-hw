package com.huawei.gallery.editor.screenshotseditor.app;

import android.content.Context;
import android.view.ViewGroup;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseMosaicUIController;
import com.huawei.gallery.editor.ui.ScreenShotsMosaicUIController;

public class ScreenShotsPaintMosaicState extends ScreenShotsBasePaintState {
    public ScreenShotsPaintMosaicState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected BaseMosaicUIController createBaseMosaicUIController() {
        return new ScreenShotsMosaicUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
    }
}
