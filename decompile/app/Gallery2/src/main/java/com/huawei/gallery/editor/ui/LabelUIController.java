package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.cache.BubbleCache;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation.LabelHolder;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import com.huawei.gallery.editor.ui.LabelBar.LabelBarDelegate;
import com.huawei.gallery.editor.ui.TextBubbleView.BubbleDelegate;

public class LabelUIController extends EditorUIController implements UIListener, BubbleDelegate, LabelBarDelegate {
    private LabelBar mLabelBar;
    private LabelListener mLabelListener;
    private RelativeLayout mLabelRootLayout;
    private boolean mSubMenuShowing;

    public interface LabelListener extends Listener, UIListener, BubbleDelegate, LabelBarDelegate {
    }

    public LabelUIController(Context context, ViewGroup parentLayout, LabelListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mLabelListener = listener;
    }

    public void show() {
        super.show();
        this.mSubMenuShowing = true;
    }

    protected int getControlLayoutId() {
        return R.layout.label_bar;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        this.mLabelRootLayout = (RelativeLayout) this.mContainer.findViewById(R.id.label_root);
        this.mLabelBar = (LabelBar) this.mContainer.findViewById(R.id.paint_bar);
        this.mLabelBar.initialize(this);
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_label_foot_bar : R.layout.editor_label_foot_bar_land;
    }

    protected void onShowFootAnimeEnd() {
        ViewGroup container = this.mContainer;
        if (container != null) {
            LabelMenu labelMenu = (LabelMenu) container.findViewById(R.id.paint_menu);
            labelMenu.onClick(labelMenu.findViewById(0));
        }
    }

    public void hide() {
        super.hide();
        this.mLabelBar.hide();
        this.mLabelRootLayout.removeAllViews();
        this.mSubMenuShowing = false;
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        this.mLabelBar = null;
    }

    public void pause() {
        for (int index = 0; index < this.mLabelRootLayout.getChildCount(); index++) {
            View v = this.mLabelRootLayout.getChildAt(index);
            if (v instanceof TextBubbleView) {
                ((TextBubbleView) v).pause();
            }
        }
    }

    public void clearFocus() {
        this.mContainer.clearFocus();
    }

    public void reLayoutLabelRoot(Rect labelRect) {
        int right;
        int bottom;
        LayoutParams params = new LayoutParams(labelRect.width(), labelRect.height());
        if (this.mEditorViewDelegate.getWidth() > this.mEditorViewDelegate.getHeight()) {
            right = (this.mEditorViewDelegate.getWidth() - labelRect.right) - this.mEditorViewDelegate.getNavigationBarHeight();
            bottom = this.mEditorViewDelegate.getHeight() - labelRect.bottom;
        } else {
            right = this.mEditorViewDelegate.getWidth() - labelRect.right;
            bottom = (this.mEditorViewDelegate.getHeight() - labelRect.bottom) - this.mEditorViewDelegate.getNavigationBarHeight();
        }
        params.setMargins(labelRect.left, labelRect.top, right, bottom);
        this.mLabelRootLayout.setLayoutParams(params);
    }

    public void onSubMenuShow() {
        this.mSubMenuShowing = true;
        this.mLabelListener.onSubMenuShow();
    }

    public void onSubMenuHide() {
        this.mSubMenuShowing = false;
        this.mLabelListener.onSubMenuHide();
    }

    public void onEraseSelectedChanged(boolean selected) {
    }

    public void onClickBar(View view) {
        for (int index = 0; index < this.mLabelRootLayout.getChildCount(); index++) {
            View v = this.mLabelRootLayout.getChildAt(index);
            if (v instanceof TextBubbleView) {
                TextBubbleView bubbleView = (TextBubbleView) v;
                if (bubbleView.isUserChoosed()) {
                    bubbleView.updateStaticLayout(this.mLabelBar.getCurrentPaintData());
                    return;
                }
            }
        }
        LabelPainData painData = this.mLabelBar.getCurrentPaintData();
        if (painData.valid && (view instanceof LabelIconView)) {
            this.mLabelRootLayout.addView(new TextBubbleView(this.mContext, this, painData), new RelativeLayout.LayoutParams(-1, -1));
        }
    }

    public void onUserChoosed(TextBubbleView bubbleView) {
        this.mLabelBar.updateSelectedView(bubbleView.getPaintData());
    }

    public void onUserDeleted(TextBubbleView bubbleView) {
        this.mLabelRootLayout.removeView(bubbleView);
    }

    public BubbleCache getBubbleCache() {
        return this.mLabelListener.getBubbleCache();
    }

    public boolean isSecureCameraMode() {
        return this.mLabelListener.isSecureCameraMode();
    }

    public boolean noBubbleChoosed() {
        for (int index = 0; index < this.mLabelRootLayout.getChildCount(); index++) {
            View v = this.mLabelRootLayout.getChildAt(index);
            if ((v instanceof TextBubbleView) && ((TextBubbleView) v).isUserChoosed()) {
                return false;
            }
        }
        return true;
    }

    public void updateRepresentation(FilterLabelRepresentation representation) {
        int count = this.mLabelRootLayout.getChildCount();
        for (int index = 0; index < count; index++) {
            View view = this.mLabelRootLayout.getChildAt(index);
            if (view instanceof TextBubbleView) {
                LabelHolder labelHolder = new LabelHolder();
                ((TextBubbleView) view).updateLabelHolderData(labelHolder);
                representation.addLabelHolder(labelHolder);
            }
        }
    }

    public int getSubMenuHeight() {
        return this.mLabelBar.getSubMenuHeight();
    }

    protected void saveUIController() {
        this.mLabelBar.saveUIController(this.mTransitionStore);
    }

    protected void restoreUIController() {
        this.mLabelBar.restoreUIController(this.mTransitionStore);
    }
}
