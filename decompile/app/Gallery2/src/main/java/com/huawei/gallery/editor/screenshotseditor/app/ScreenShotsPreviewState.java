package com.huawei.gallery.editor.screenshotseditor.app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.editor.app.BasePreviewState;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.EditorUIController;
import com.huawei.gallery.editor.ui.ScreenShotsPreviewUIController;
import com.huawei.gallery.editor.ui.ScreenShotsPreviewUIController.Listener;

public class ScreenShotsPreviewState extends BasePreviewState implements Listener {
    private Handler mHandler;
    private boolean mLock;
    private ScreenShotsPreviewUIController mScreenShotsPreviewUIController;

    public ScreenShotsPreviewState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
        this.mLock = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        ScreenShotsPreviewState.this.lock();
                        return;
                    case 1:
                        ScreenShotsPreviewState.this.unLock();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mActionInfo = new ActionInfo(null, Action.NO, Action.SAVE, Action.REDO, Action.UNDO, Action.SHARE, true);
        this.mIconDataContainer.put(R.id.textview_screenshots_cut, new IconData((int) R.id.textview_screenshots_cut, (int) R.drawable.ic_gallery_edit_crop, (int) R.string.crop));
        this.mIconDataContainer.put(R.id.textview_screenshots_mosaic, new IconData((int) R.id.textview_screenshots_mosaic, (int) R.drawable.ic_gallery_edit_mosaic, (int) R.string.simple_editor_mosaic));
        this.mIconDataContainer.put(R.id.textview_screenshots_brush, new IconData((int) R.id.textview_screenshots_brush, (int) R.drawable.ic_gallery_edit_pen, (int) R.string.editor_brush));
        this.mIconDataContainer.put(R.id.textview_screenshots_eraser, new IconData((int) R.id.textview_screenshots_eraser, (int) R.drawable.ic_gallery_edit_eraser, (int) R.string.simple_editor_eraser));
    }

    protected EditorUIController createUIController() {
        this.mScreenShotsPreviewUIController = new ScreenShotsPreviewUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
        return this.mScreenShotsPreviewUIController;
    }

    public void show() {
        super.show();
        getImage().requestStackChangeCall();
    }

    public void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (changeSize) {
            this.mEditorView.invalidate();
        }
    }

    public void onLeaveEditor() {
        super.onLeaveEditor();
        if (this.mScreenShotsPreviewUIController != null) {
            this.mScreenShotsPreviewUIController.onLeaveEditor();
        }
    }

    public void onActionItemClick(Action action) {
        this.mEditorView.commitCurrentStateAction(action);
    }

    private void unLock() {
        this.mLock = false;
    }

    private void lock() {
        this.mLock = true;
    }

    public void onClickIcon(View view, boolean force) {
        if (force) {
            onClickIcon(view);
        } else if (!this.mLock) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(0);
            this.mHandler.sendEmptyMessageDelayed(1, 300);
            if (this.mScreenShotsPreviewUIController != null) {
                this.mScreenShotsPreviewUIController.setSelectedAspect(view, true);
            }
            if (!(view == null || view.getId() == R.id.textview_screenshots_cut)) {
                super.onClickIcon(view);
            }
        }
    }

    public void onClickIcon(View view) {
        if (!this.mLock) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(0);
            this.mHandler.sendEmptyMessageDelayed(1, 300);
            if (this.mScreenShotsPreviewUIController == null || this.mScreenShotsPreviewUIController.setSelectedAspect(view, false)) {
                this.mEditorView.commitCurrentStateChanges();
                super.onClickIcon(view);
            }
        }
    }

    public OnClickListener getClickListener() {
        return this.mClickListener;
    }

    protected void checkOriginBmpNullIfNeed(View view) {
        if (view.getId() != R.id.textview_screenshots_cut) {
            super.checkOriginBmpNullIfNeed(view);
        }
    }

    protected EditorState selectEditorState(int key) {
        switch (key) {
            case R.id.textview_screenshots_cut:
                return new ScreenShotsFreeCropState(this.mContext, this.mParentLayout, this.mEditorView);
            case R.id.textview_screenshots_mosaic:
                return new ScreenShotsPaintMosaicState(this.mContext, this.mParentLayout, this.mEditorView);
            case R.id.textview_screenshots_brush:
                return new ScreenShotsPaintBrushState(this.mContext, this.mParentLayout, this.mEditorView);
            case R.id.textview_screenshots_eraser:
                return new ScreenShotsPaintEraserState(this.mContext, this.mParentLayout, this.mEditorView);
            default:
                return null;
        }
    }

    public GLRoot getGLRoot() {
        return this.mEditorView.getGLRoot();
    }
}
