package com.huawei.gallery.editor.app;

import android.content.Context;
import android.view.ViewGroup;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.CommonFilterAdapter;
import com.huawei.gallery.editor.step.FaceEditorStep;
import com.huawei.gallery.editor.step.FaceOmronEditorStep;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.SkinOmronUIController;
import com.huawei.gallery.editor.ui.SkinUIController;

public class SkinOmronState extends SkinState {
    public SkinOmronState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected FaceEditorStep createEditorStep() {
        return new FaceOmronEditorStep();
    }

    protected SkinUIController createSkinUIController() {
        return new SkinOmronUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
    }

    protected BaseViewAdapter createAdapter() {
        return new CommonFilterAdapter(this.mContext, this.mEditorView.getEditorManager().getFacesViewAdapter());
    }
}
