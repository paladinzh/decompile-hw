package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public abstract class ScreenShotsBasePaintUIController extends BaseMosaicUIController implements UIListener {
    private ViewGroup mBarContainer;
    private BasePaintBar mBasePaintBar;
    protected ScreenBasePaintListener mBasePaintListener;
    private int mContainerHeight;
    private boolean mSubMenuShowing;
    private Matrix mTransCanvasToBitmapMatrixWithOutCrop = new Matrix();

    public interface ScreenBasePaintListener extends BaseMosaicListener {
        float[] getOutPoint();
    }

    protected abstract int getContainerLayout();

    protected abstract int getMosaicViewId();

    public ScreenShotsBasePaintUIController(Context context, ViewGroup parentLayout, ScreenBasePaintListener basePaintListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, basePaintListener, EditorViewDelegate);
        this.mBasePaintListener = basePaintListener;
        this.mContainerHeight = EditorConstant.MENU_HEIGHT;
    }

    public void show() {
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        if (this.mBarContainer == null && getContainerLayout() > 0) {
            this.mBarContainer = (ViewGroup) inflater.inflate(getContainerLayout(), this.mParentLayout, false);
            this.mBasePaintBar = (BasePaintBar) this.mBarContainer;
            this.mBasePaintBar.initialize(this);
        }
        if (this.mBarContainer != null && this.mBarContainer.getParent() == null) {
            ((ViewGroup) this.mParentLayout.findViewById(R.id.screen_shots_sub_menu_root)).addView(this.mBarContainer);
        }
        if (this.mContainer == null) {
            this.mContainer = (ViewGroup) inflater.inflate(R.layout.screenshots_editor_mosaic_controls, this.mParentLayout, false);
        }
        if (this.mContainer.getParent() == null) {
            this.mParentLayout.addView(this.mContainer, this.mParentLayout.getChildCount() + -1 < 0 ? 0 : this.mParentLayout.getChildCount() - 1);
        }
        this.mMosaicView = (MosaicView) this.mContainer.findViewById(getMosaicViewId());
        this.mMosaicView.setListener(this);
        this.mMosaicView.setDelegate(this);
        super.show();
    }

    protected void startShowAnime() {
        EditorAnimation.startTranslationAnimationForViewGroup(this.mBarContainer, 1, getMenuHeight(), 0, this.mEditorViewDelegate.isPort(), null);
    }

    protected void startHideAnime() {
    }

    public void hide() {
        if (this.mMosaicView != null) {
            this.mMosaicView.setVisibility(8);
        }
        if (this.mBasePaintBar != null) {
            this.mBasePaintBar.hide();
        }
        if (this.mContainer != null) {
            this.mParentLayout.removeView(this.mContainer);
            this.mContainer = null;
        }
        if (this.mBarContainer != null) {
            this.mBarContainer.clearAnimation();
            ((ViewGroup) this.mParentLayout.findViewById(R.id.screen_shots_sub_menu_root)).removeView(this.mBarContainer);
            this.mBarContainer = null;
            this.mBasePaintBar = null;
        }
        View view = this.mParentLayout.findViewById(R.id.screen_shots_sub_menu_root);
        if (view != null) {
            view.setBackgroundColor(0);
        }
        super.hide();
    }

    public void onSubMenuShow() {
        this.mSubMenuShowing = true;
        layoutMosaicView();
    }

    public void onSubMenuHide() {
        this.mSubMenuShowing = false;
        layoutMosaicView();
    }

    public void onEraseSelectedChanged(boolean selected) {
    }

    public void onClickBar(View view) {
    }

    public void setFilterMosaicRepresentation(FilterMosaicRepresentation representation) {
        if (this.mMosaicView != null) {
            this.mMosaicView.setFilterMosaicRepresentation(representation);
        }
    }

    public void layoutMosaicView() {
        layoutMosaicView(this.mViewBounds);
    }

    public void layoutMosaicView(Rect viewBounds) {
        if (this.mMosaicView != null) {
            int right;
            int bottom;
            int subMenuHeight;
            this.mViewBounds.set(viewBounds);
            LayoutParams params = new LayoutParams(-1, -1);
            if (this.mEditorViewDelegate.getWidth() > this.mEditorViewDelegate.getHeight()) {
                right = (this.mEditorViewDelegate.getWidth() - viewBounds.right) - this.mEditorViewDelegate.getNavigationBarHeight();
                bottom = this.mEditorViewDelegate.getHeight() - viewBounds.bottom;
            } else {
                right = this.mEditorViewDelegate.getWidth() - viewBounds.right;
                bottom = (this.mEditorViewDelegate.getHeight() - viewBounds.bottom) - this.mEditorViewDelegate.getNavigationBarHeight();
            }
            bottom = Math.max(getMinBottom(), bottom);
            this.mMosaicView.setLayoutParams(params);
            MosaicView mosaicView = this.mMosaicView;
            int i = viewBounds.left;
            int i2 = viewBounds.top;
            if (this.mSubMenuShowing) {
                subMenuHeight = getSubMenuHeight();
            } else {
                subMenuHeight = 0;
            }
            mosaicView.setPadding(i, i2, right, bottom - subMenuHeight);
            this.mMosaicView.setVisibility(0);
            this.mMosaicView.setBounds(viewBounds, this.mSourceRectVisible);
            this.mMosaicView.invalidate();
        }
    }

    private int getMinBottom() {
        int i = 0;
        int menuHeight = (supportMenu() ? getMenuHeight() : 0) + this.mContainerHeight;
        if (this.mSubMenuShowing) {
            i = getSubMenuHeight();
        }
        return menuHeight + i;
    }

    public boolean supportMenu() {
        return true;
    }

    @SuppressWarnings({"EI_EXPOSE_REP"})
    public float[] getValidDisplacement(float x, float y) {
        int i = 0;
        this.mTmpPoint[0] = 0.0f;
        this.mTmpPoint[1] = 0.0f;
        if (this.mMosaicView == null) {
            return this.mTmpPoint;
        }
        int i2;
        int left = this.mViewBounds.left;
        int top = this.mViewBounds.top;
        int right = this.mViewBounds.right;
        int bottom = this.mViewBounds.bottom;
        if (left < 0 && x > 0.0f) {
            this.mTmpPoint[0] = x;
        }
        if (right >= this.mEditorViewDelegate.getWidth() - (this.mEditorViewDelegate.isPort() ? 0 : this.mEditorViewDelegate.getNavigationBarHeight()) && x < 0.0f) {
            this.mTmpPoint[0] = x;
        }
        if (top <= this.mEditorViewDelegate.getActionBarHeight() && y > 0.0f) {
            this.mTmpPoint[1] = y;
        }
        int height = this.mEditorViewDelegate.getHeight();
        if (supportMenu()) {
            i2 = this.mContainerHeight;
        } else {
            i2 = 0;
        }
        i2 += getMenuHeight();
        if (this.mEditorViewDelegate.isPort()) {
            i = this.mEditorViewDelegate.getNavigationBarHeight();
        }
        if (bottom >= height - (i2 + i) && y < 0.0f) {
            this.mTmpPoint[1] = y;
        }
        return this.mTmpPoint;
    }

    protected void updateScreenToImage(Rect source, Rect target) {
        if (this.mViewBounds.equals(target) && this.mSourceRectVisible.equals(source)) {
            Matrix matrix = new Matrix();
            matrix.postScale(((float) source.width()) / ((float) target.width()), ((float) source.height()) / ((float) target.height()));
            matrix.postTranslate((float) source.left, (float) source.top);
            this.mTransCanvasToBitmapMatrixWithOutCrop.set(matrix);
            float[] temp = this.mBasePaintListener.getOutPoint();
            matrix.postTranslate(temp[0], temp[1]);
            this.mTransCanvasToBitmapMatrix.set(matrix);
        }
    }

    @SuppressWarnings({"EI_EXPOSE_REP"})
    public float[] getAbsolutePreviewPointWithOutCrop(float x, float y) {
        this.mTmpPoint[0] = x;
        this.mTmpPoint[1] = y;
        this.mTransCanvasToBitmapMatrixWithOutCrop.mapPoints(this.mTmpPoint);
        return this.mTmpPoint;
    }

    public void onTouchChanged(boolean clickable) {
        if (this.mBasePaintBar == null) {
            return;
        }
        if (clickable) {
            this.mBasePaintBar.unLock();
        } else {
            this.mBasePaintBar.lock();
        }
    }

    public void onConfigurationChanged() {
        GalleryLog.d("ScreenShotsBasePaintUIController", "no need onConfigurationChanged");
    }

    protected void saveUIController() {
        if (this.mBasePaintBar != null) {
            this.mBasePaintBar.saveUIController(this.mTransitionStore);
        }
    }

    protected void restoreUIController() {
        if (this.mBasePaintBar != null) {
            this.mBasePaintBar.restoreUIController(this.mTransitionStore);
        }
    }
}
