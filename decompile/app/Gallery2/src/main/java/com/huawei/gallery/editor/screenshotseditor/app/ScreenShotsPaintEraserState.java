package com.huawei.gallery.editor.screenshotseditor.app;

import android.content.Context;
import android.view.ViewGroup;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.step.MosaicEditorStep;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseMosaicUIController;
import com.huawei.gallery.editor.ui.ScreenShotsEraserUIController;

public class ScreenShotsPaintEraserState extends ScreenShotsPaintMosaicState {
    public ScreenShotsPaintEraserState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected BaseMosaicUIController createBaseMosaicUIController() {
        return new ScreenShotsEraserUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
    }

    public boolean forceAddData() {
        for (EditorStep step : getImage().getAppliedStack()) {
            if (step instanceof MosaicEditorStep) {
                MosaicEditorStep editorStep = (MosaicEditorStep) step;
                if (editorStep.getFilterRepresentationList().size() > 0) {
                    FilterMosaicRepresentation mosaicRepresentation = (FilterMosaicRepresentation) editorStep.getFilterRepresentationList().get(0);
                    if (mosaicRepresentation.getAppliedMosaic().size() > 0 || mosaicRepresentation.isCurrentStrokeDataValid()) {
                        return true;
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }
}
