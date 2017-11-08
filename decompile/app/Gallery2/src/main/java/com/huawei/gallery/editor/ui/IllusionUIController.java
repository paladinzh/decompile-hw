package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.IllusionBar.STYLE;
import com.huawei.gallery.editor.ui.IllusionBar.UIListener;
import com.huawei.gallery.editor.ui.IllusionView.Listener;
import com.huawei.gallery.util.LayoutHelper;

public class IllusionUIController extends EditorUIController implements Listener, UIListener {
    private IllusionBar mIllusionBar;
    private IllusionListener mIllusionListener;
    private IllusionView mIllusionView;
    private boolean mSubMenuShowing;

    public interface IllusionListener extends EditorUIController.Listener {
        Bitmap getApplyBitmap();

        int getCurrentSeekbarValue();

        float getScaleScreenToImage(boolean z);

        void onProgressChanged(float f);

        void onStrokeDataChange(FilterIllusionRepresentation filterIllusionRepresentation);
    }

    public IllusionUIController(Context context, ViewGroup parentLayout, IllusionListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mIllusionListener = listener;
    }

    public void show() {
        super.show();
        this.mIllusionView = (IllusionView) this.mContainer.findViewById(R.id.illusion_view);
        this.mIllusionView.setListener(this);
        this.mSubMenuShowing = false;
    }

    protected int getControlLayoutId() {
        return R.layout.editor_illusion_controls;
    }

    public void hide() {
        super.hide();
        if (!(this.mIllusionView == null || this.mIllusionBar == null)) {
            this.mIllusionBar.hide();
            this.mIllusionView.hide();
        }
        this.mSubMenuShowing = false;
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        if (this.mIllusionBar != null) {
            this.mIllusionBar = null;
        }
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        this.mIllusionBar = (IllusionBar) this.mContainer.findViewById(R.id.paint_illusion_bar);
        this.mIllusionBar.initialize(this);
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_illusion_foot_bar : R.layout.editor_illusion_foot_bar_land;
    }

    protected void onShowFootAnimeEnd() {
        ViewGroup container = this.mContainer;
        if (container != null) {
            PaintIllusionMenu illusionMenu = (PaintIllusionMenu) container.findViewById(R.id.paint_illusion_menu);
            illusionMenu.onClick(illusionMenu.findViewById(STYLE.CIRCLE.ordinal()));
        }
    }

    public void setFilterIllusionRepresentation(FilterIllusionRepresentation fep) {
        if (this.mIllusionView != null) {
            this.mIllusionView.setFilterIllusionRepresentation(fep);
        }
    }

    public void layoutIllusionView(Rect viewBounds) {
        if (this.mIllusionView != null) {
            int right;
            int bottom;
            LayoutParams params = new LayoutParams(-1, -1);
            if (this.mEditorViewDelegate.getWidth() > this.mEditorViewDelegate.getHeight()) {
                right = (this.mEditorViewDelegate.getWidth() - viewBounds.right) - this.mEditorViewDelegate.getNavigationBarHeight();
                if (GalleryUtils.isTabletProduct(this.mContext) && LayoutHelper.isDefaultLandOrientationProduct()) {
                    bottom = (this.mEditorViewDelegate.getHeight() - viewBounds.bottom) - LayoutHelper.getNavigationBarHeightForDefaultLand();
                } else {
                    bottom = this.mEditorViewDelegate.getHeight() - viewBounds.bottom;
                }
            } else {
                right = this.mEditorViewDelegate.getWidth() - viewBounds.right;
                bottom = (this.mEditorViewDelegate.getHeight() - viewBounds.bottom) - this.mEditorViewDelegate.getNavigationBarHeight();
            }
            params.setMargins(0, 0, 0, bottom);
            this.mIllusionView.setPadding(viewBounds.left, viewBounds.top, right, 0);
            this.mIllusionView.setBounds(viewBounds);
            this.mIllusionView.setVisibility(0);
            this.mIllusionView.setLayoutParams(params);
            this.mIllusionView.invalidate();
        }
    }

    public void onStrokeDataChange(FilterIllusionRepresentation rep) {
        this.mIllusionListener.onStrokeDataChange(rep);
    }

    public float getScaleScreenToImage(boolean force) {
        return this.mIllusionListener.getScaleScreenToImage(force);
    }

    public Bitmap getApplyBitmap() {
        return this.mIllusionListener.getApplyBitmap();
    }

    public void onStyleChanged(STYLE style) {
        if (this.mIllusionView != null) {
            this.mIllusionView.setStyle(style);
        }
    }

    public void onProgressChanged(float progress) {
        this.mIllusionListener.onProgressChanged(progress);
    }

    public void onSubMenuShow() {
        this.mSubMenuShowing = true;
    }

    public void onSubMenuHide() {
        this.mSubMenuShowing = false;
    }

    public int getCurrentSeekbarValue() {
        return this.mIllusionListener.getCurrentSeekbarValue();
    }

    protected View getFootAnimationTargetView() {
        return this.mIllusionBar;
    }

    protected void saveUIController() {
        if (this.mIllusionBar != null) {
            this.mIllusionBar.saveUIController(this.mTransitionStore);
        }
    }

    protected void restoreUIController() {
        if (this.mIllusionBar != null) {
            this.mIllusionBar.restoreUIController(this.mTransitionStore);
        }
    }
}
