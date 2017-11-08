package com.huawei.gallery.editor.ui;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.app.PreviewState;
import com.huawei.gallery.editor.ui.BaseEditorView.Delegate;
import com.huawei.gallery.util.LayoutHelper;
import java.util.Locale;

public class EditorView extends BaseEditorView {
    private ViewGroup mCompareContainer;

    public EditorView(Delegate delegate, Activity activity) {
        super(delegate, activity);
    }

    public void enterEditor() {
        setVisibility(0);
        requestLayout();
        super.enterEditor();
        if (this.mPreviewState == null) {
            this.mPreviewState = new PreviewState(this.mActivity, this.mParentLayout, this);
        }
        addEditorOriginalCompareButton();
        updateBackground();
        setBackgroundVisible(false);
        changeState(this.mPreviewState);
    }

    public void setBackgroundVisible(boolean isVisible) {
        this.mHeadBackground.setVisibility(isVisible ? 0 : 8);
    }

    public void onServiceConnected() {
        if (this.mPreviewState == null) {
            this.mPreviewState = new PreviewState(this.mActivity, this.mParentLayout, this);
        }
        this.mPreviewState.prepareEditorState();
    }

    public void leaveEditor(Uri uri, boolean needAnime) {
        if (!(this.mCompareContainer == null || this.mCompareContainer.getParent() == null)) {
            this.mParentLayout.removeView(this.mCompareContainer);
        }
        super.leaveEditor(uri, needAnime);
    }

    protected void updateContainerLayoutParams() {
        if (this.mCompareContainer != null) {
            LayoutParams params = (LayoutParams) this.mCompareContainer.getLayoutParams();
            if (this.mCompareContainer.getResources().getConfiguration().orientation == 2) {
                params.rightMargin = getNavigationBarHeight();
                params.bottomMargin = 0;
            } else {
                params.rightMargin = 0;
                params.bottomMargin = getNavigationBarHeight();
            }
        }
    }

    public int getNavigationBarHeight() {
        if (MultiWindowStatusHolder.isInMultiWindowMode()) {
            return 0;
        }
        return super.getNavigationBarHeight();
    }

    public void bringCompareButtonFront() {
        if (this.mCompareContainer != null && this.mCompareContainer.getParent() != null) {
            this.mParentLayout.bringChildToFront(this.mCompareContainer);
        }
    }

    private void addEditorOriginalCompareButton() {
        int i;
        boolean isVisible = false;
        if (!(this.mCompareContainer == null || this.mCompareContainer.getParent() == null)) {
            this.mParentLayout.removeView(this.mCompareContainer);
            isVisible = this.mCompareContainer.getVisibility() == 0;
        }
        LayoutInflater inflater = (LayoutInflater) this.mActivity.getSystemService("layout_inflater");
        if (isPort()) {
            i = R.layout.editor_compare_button_controls;
        } else {
            i = R.layout.editor_compare_button_controls_land;
        }
        this.mCompareContainer = (ViewGroup) inflater.inflate(i, this.mParentLayout, false);
        if (!isPort() && LayoutHelper.isDefaultLandOrientationProduct()) {
            this.mCompareContainer.setPadding(0, 0, 0, LayoutHelper.getNavigationBarHeightForDefaultLand());
        }
        this.mCompareContainer.setVisibility(isVisible ? 0 : 8);
        if (this.mCompareContainer.getParent() == null) {
            this.mParentLayout.addView(this.mCompareContainer);
        }
        updateContainerLayoutParams();
        TextView editorOriginalCompareButton = (TextView) this.mCompareContainer.findViewById(R.id.editor_original_compare_button);
        editorOriginalCompareButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (EditorView.this.mCurrentState == null) {
                    return false;
                }
                GLRoot root = EditorView.this.getGLRoot();
                if (root == null) {
                    return false;
                }
                root.lockRenderThread();
                try {
                    EditorView.this.editorOriginalCompareButtonBeTouched(v, event);
                    return false;
                } finally {
                    root.unlockRenderThread();
                }
            }
        });
        editorOriginalCompareButton.setText(((String) editorOriginalCompareButton.getText()).toUpperCase(Locale.US));
    }

    public void updateOriginalCompareButton() {
        addEditorOriginalCompareButton();
    }

    private boolean editorOriginalCompareButtonBeTouched(View v, MotionEvent event) {
        if (this.mCurrentState == null) {
            return false;
        }
        boolean ret = false;
        switch (event.getAction() & 255) {
            case 0:
                ReportToBigData.report(98);
                this.mCurrentState.setShowOriginal(true);
                ret = true;
                break;
            case 1:
                this.mCurrentState.setShowOriginal(false);
                ret = true;
                break;
        }
        if (ret) {
            invalidate();
        }
        return ret;
    }

    public void setEditorOriginalCompareButtonVisible(boolean visible) {
        if (this.mCompareContainer != null && this.mCompareContainer.getParent() != null) {
            this.mCompareContainer.setVisibility(visible ? 0 : 8);
        }
    }
}
