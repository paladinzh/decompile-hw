package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public abstract class BasePaintUIController extends BaseMosaicUIController implements UIListener {
    private BasePaintBar mBasePaintBar;
    private boolean mSubMenuShowing;

    public BasePaintUIController(Context context, ViewGroup parentLayout, BaseMosaicListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public int getSubMenuHeight() {
        if (this.mBasePaintBar == null) {
            return super.getSubMenuHeight();
        }
        return this.mBasePaintBar.getSubMenuHeight();
    }

    protected int getControlLayoutId() {
        return R.layout.editor_paint_controls;
    }

    public void show() {
        super.show();
        this.mMosaicView = (MosaicView) this.mContainer.findViewById(R.id.mosaic_view);
        this.mMosaicView.setListener(this);
        this.mMosaicView.setDelegate(this);
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        createView();
    }

    public void setFilterMosaicRepresentation(FilterMosaicRepresentation representation) {
        if (this.mMosaicView != null) {
            this.mMosaicView.setFilterMosaicRepresentation(representation);
        }
    }

    protected void createView() {
        this.mBasePaintBar = (BasePaintBar) this.mContainer.findViewById(R.id.paint_bar);
        this.mBasePaintBar.initialize(this);
    }

    protected void removeView() {
        if (this.mBasePaintBar != null) {
            this.mBasePaintBar.hide();
        }
    }

    public void hide() {
        super.hide();
        if (this.mMosaicView != null) {
            this.mMosaicView.setVisibility(8);
        }
        removeView();
        this.mSubMenuShowing = false;
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        this.mBasePaintBar = null;
    }

    public void onSubMenuShow() {
        this.mSubMenuShowing = true;
    }

    public void onSubMenuHide() {
        this.mSubMenuShowing = false;
    }

    public void onEraseSelectedChanged(boolean selected) {
        if (this.mMosaicView != null) {
            int i;
            MosaicView mosaicView = this.mMosaicView;
            if (selected) {
                i = 1;
            } else {
                i = 0;
            }
            mosaicView.setStrokeType(i);
            if (selected) {
                ReportToBigData.report(126);
            }
        }
    }

    public void onClickBar(View view) {
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

    public View getFootAnimationTargetView() {
        return this.mBasePaintBar;
    }

    @SuppressWarnings({"EI_EXPOSE_REP"})
    public float[] getValidDisplacement(float x, float y) {
        int i = 0;
        this.mTmpPoint[0] = 0.0f;
        this.mTmpPoint[1] = 0.0f;
        GalleryLog.v("BasePaintUIController", "move to edge need modify dx or dy");
        if (this.mMosaicView == null) {
            return this.mTmpPoint;
        }
        int left = this.mViewBounds.left;
        int top = this.mViewBounds.top;
        int right = this.mViewBounds.right;
        int bottom = this.mViewBounds.bottom;
        if (left <= 0 && x > 0.0f) {
            this.mTmpPoint[0] = x;
        }
        if (right >= this.mEditorViewDelegate.getWidth() - (this.mEditorViewDelegate.isPort() ? 0 : this.mEditorViewDelegate.getNavigationBarHeight()) && x < 0.0f) {
            this.mTmpPoint[0] = x;
        }
        if (top <= this.mEditorViewDelegate.getActionBarHeight() && y > 0.0f) {
            this.mTmpPoint[1] = y;
        }
        int height = this.mEditorViewDelegate.getHeight();
        int menuHeight = getMenuHeight() + getSubMenuHeight();
        if (this.mEditorViewDelegate.isPort()) {
            i = this.mEditorViewDelegate.getNavigationBarHeight();
        }
        if (bottom >= height - (i + menuHeight) && y < 0.0f) {
            this.mTmpPoint[1] = y;
        }
        return this.mTmpPoint;
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
