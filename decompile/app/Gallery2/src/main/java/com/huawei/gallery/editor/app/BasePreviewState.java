package com.huawei.gallery.editor.app;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.glrender.BasePreviewRender;
import com.huawei.gallery.editor.glrender.BaseRender;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.util.PermissionManager;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class BasePreviewState extends BaseActionState {
    protected BasePreviewRender mBasePreviewRender;
    protected OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (!BasePreviewState.this.jumpPituIfNeeded(view.getId())) {
                EditorState editorState = ((IconData) BasePreviewState.this.mIconDataContainer.get(view.getId())).getEditorState();
                if (editorState != null && BasePreviewState.this.mEditorView.inSimpleEditor()) {
                    if (!(editorState instanceof WaterMarkState) || PermissionManager.checkHasPermissions((Activity) BasePreviewState.this.mContext, PermissionManager.getPermissionsLocation())) {
                        GLRoot root = BasePreviewState.this.mEditorView.getGLRoot();
                        if (root != null) {
                            root.lockRenderThread();
                            try {
                                BasePreviewState.this.onClickIcon(view);
                                return;
                            } finally {
                                root.unlockRenderThread();
                            }
                        } else {
                            return;
                        }
                    }
                    PermissionManager.getInstance().requestPermissions((Activity) BasePreviewState.this.mContext, PermissionManager.getPermissionsLocation(), 1003);
                }
            }
        }
    };
    protected SparseArray<IconData> mIconDataContainer = new SparseArray();

    protected abstract EditorState selectEditorState(int i);

    public BasePreviewState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
    }

    protected BaseRender createRender() {
        this.mBasePreviewRender = new BasePreviewRender(this.mEditorView, this);
        return this.mBasePreviewRender;
    }

    public void onLeaveEditor() {
        super.onLeaveEditor();
        int size = this.mIconDataContainer.size();
        for (int index = 0; index < size; index++) {
            State state = ((IconData) this.mIconDataContainer.get(this.mIconDataContainer.keyAt(index))).getEditorState();
            if (state != null) {
                state.onLeaveEditor();
            }
        }
    }

    private boolean jumpPituIfNeeded(int view_id) {
        if (12 != view_id) {
            return false;
        }
        this.mEditorView.jumpPituIfNeeded(getImage().hasModifications());
        ReportToBigData.report(SmsCheckResult.ESCT_144, String.format("{EditMakeupAction:%s}", new Object[]{"Pitu"}));
        return true;
    }

    protected void onClickIcon(View view) {
        if (view != null) {
            EditorState editorState = ((IconData) this.mIconDataContainer.get(view.getId())).getEditorState();
            if (editorState != null && this.mEditorView.inSimpleEditor()) {
                checkOriginBmpNullIfNeed(view);
                this.mEditorView.changeState(editorState);
                ReportToBigData.report(113, String.format("{EditStateClick:%s}", new Object[]{editorState.getClass().getSimpleName()}));
            }
        }
    }

    protected void checkOriginBmpNullIfNeed(View view) {
        if (getImage().getOriginalBitmapLarge() == null) {
            getImage().switchTemporaryBitmap();
            getSimpleEditorManager().cancelTask();
        }
    }

    public void prepareEditorState() {
        for (int i = 0; i < this.mIconDataContainer.size(); i++) {
            int key = this.mIconDataContainer.keyAt(i);
            IconData iconData = (IconData) this.mIconDataContainer.get(key);
            if (iconData.getEditorState() == null) {
                iconData.setEditorState(selectEditorState(key));
            }
        }
    }

    public void updateParentLayout(RelativeLayout layout) {
        super.updateParentLayout(layout);
        int size = this.mIconDataContainer.size();
        for (int index = 0; index < size; index++) {
            EditorState state = ((IconData) this.mIconDataContainer.get(this.mIconDataContainer.keyAt(index))).getEditorState();
            if (state != null) {
                state.updateParentLayout(layout);
            }
        }
    }

    public void onStackChanged(boolean canUndo, boolean canRedo, boolean canCompare) {
        this.mActionInfo.setRedo(canRedo);
        this.mActionInfo.setUndo(canUndo);
        changeActionBar();
        this.mEditorView.setEditorOriginalCompareButtonVisible(canCompare);
    }

    public void onDetectedFace(boolean enable) {
    }
}
