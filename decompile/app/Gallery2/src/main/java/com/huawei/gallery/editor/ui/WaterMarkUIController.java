package com.huawei.gallery.editor.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.huawei.gallery.editor.filters.FilterWaterMarkRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.step.WaterMarkEditorStep;
import com.huawei.gallery.editor.watermark.GalleryWMCategoryListView;
import com.huawei.gallery.editor.watermark.GalleryWMCategoryListView.Listener;
import com.huawei.gallery.editor.watermark.GalleryWMDotListView;
import com.huawei.gallery.editor.watermark.GalleryWMLocalLibBaseView;
import com.huawei.gallery.editor.watermark.GalleryWMLocalLibPager;
import com.huawei.watermark.WatermarkDelegate;
import com.huawei.watermark.WatermarkDelegate.WaterMarkHolder;
import com.huawei.watermark.ui.WMComponent;
import java.util.ArrayList;

public class WaterMarkUIController extends EditorUIController implements Listener {
    private com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate mLocationSettingDelegate = new LocationSettingDelegate();
    private WaterMarkListener mWaterMarkListener;
    private WatermarkDelegate mWatermarkDelegate = new WatermarkDelegate();

    public interface WaterMarkListener extends EditorUIController.Listener, Listener {
        String getToken();

        boolean isSecureCameraMode();
    }

    private static class LocationSettingDelegate implements com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate {
        private LocationSettingDelegate() {
        }

        public boolean getGPSMenuSetting() {
            return true;
        }
    }

    public WaterMarkUIController(Context context, ViewGroup parentLayout, WaterMarkListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mWaterMarkListener = listener;
    }

    protected int getControlLayoutId() {
        return R.layout.wm_jar_page;
    }

    protected void inflateFootLayout() {
        this.mWatermarkDelegate.setWatermarkBaseView((WMComponent) this.mContainer.findViewById(R.id.wm_component));
    }

    public void show() {
        if (this.mContainer == null) {
            this.mContainer = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(getControlLayoutId(), this.mParentLayout, false);
            this.mHeadGroupView = (EditorHeadGroupView) this.mContainer.findViewById(R.id.editor_head_layout);
            this.mHeadGroupView.setLayoutParams(new LayoutParams(-1, this.mEditorViewDelegate.getActionBarHeight()));
            this.mHeadGroupView.initView(this);
            inflateFootLayout();
        }
        attachToParent();
        updateContainerLayoutParams();
        setCustomView();
        this.mWatermarkDelegate.setAppIDInHealthPlatform(5);
        this.mWatermarkDelegate.setToken(this.mWaterMarkListener.getToken());
        this.mWatermarkDelegate.setLocationSettingDelegate(this.mLocationSettingDelegate);
        this.mWatermarkDelegate.initWatermarkOrientationStatus(getRotationStatus(), 16);
        this.mWatermarkDelegate.setCanShowWatermarkData(false);
        this.mWatermarkDelegate.setCanShowWhenLocked(this.mWaterMarkListener.isSecureCameraMode());
        this.mWatermarkDelegate.resume();
    }

    private void setCustomView() {
        GalleryWMCategoryListView wmCategoryListView;
        if (this.mWatermarkDelegate.getWMLocalLibBaseView() == null) {
            this.mWatermarkDelegate.setWMLocalLibBaseView(new GalleryWMLocalLibBaseView(this.mContext));
        }
        if (this.mWatermarkDelegate.getWMCategoryListView() == null) {
            wmCategoryListView = new GalleryWMCategoryListView(this.mContext);
            wmCategoryListView.setListener(this);
            this.mWatermarkDelegate.setWMCategoryListView(wmCategoryListView);
        } else {
            wmCategoryListView = (GalleryWMCategoryListView) this.mWatermarkDelegate.getWMCategoryListView();
        }
        wmCategoryListView.needAnimation();
        if (this.mWatermarkDelegate.getWMDotListView() == null) {
            this.mWatermarkDelegate.setWMDotListView(new GalleryWMDotListView(this.mContext));
        }
        if (this.mWatermarkDelegate.getWMLocalLibPager() == null) {
            this.mWatermarkDelegate.setWMLocalLibPager(new GalleryWMLocalLibPager(this.mContext));
        }
    }

    public void hide() {
        super.hide();
        this.mWatermarkDelegate.pause();
        this.mWatermarkDelegate.setLocationSettingDelegate(null);
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        this.mEditorViewDelegate.invalidate();
    }

    public boolean onBackPressed() {
        return this.mWatermarkDelegate.onBackPressed();
    }

    public void resume() {
        this.mWatermarkDelegate.refreshAllView();
    }

    public boolean updateEditorStep(WaterMarkEditorStep waterMarkEditorStep) {
        if (this.mWatermarkDelegate == null) {
            return false;
        }
        ArrayList<WaterMarkHolder> delegateMarkHolderList = this.mWatermarkDelegate.getCurrentWaterMarkHolderList();
        if (delegateMarkHolderList == null || delegateMarkHolderList.isEmpty()) {
            return false;
        }
        for (WaterMarkHolder delegateMarkHolder : delegateMarkHolderList) {
            FilterWaterMarkRepresentation.WaterMarkHolder waterMarkHolder = new FilterWaterMarkRepresentation.WaterMarkHolder();
            waterMarkHolder.left = delegateMarkHolder.getX() / delegateMarkHolder.width;
            waterMarkHolder.top = delegateMarkHolder.getY() / delegateMarkHolder.height;
            waterMarkHolder.width = delegateMarkHolder.width;
            waterMarkHolder.height = delegateMarkHolder.height;
            if (delegateMarkHolder.getWaterMarkBitmap() != null) {
                waterMarkHolder.waterMarkBitmap = delegateMarkHolder.getWaterMarkBitmap().copy(delegateMarkHolder.getWaterMarkBitmap().getConfig(), true);
            }
            waterMarkHolder.name = delegateMarkHolder.getWaterMarkName();
            FilterWaterMarkRepresentation representation = new FilterWaterMarkRepresentation();
            representation.setWaterMarkHolder(waterMarkHolder);
            waterMarkEditorStep.add(representation);
            delegateMarkHolder.release();
        }
        return true;
    }

    public void onConfigurationChanged() {
        this.mWatermarkDelegate.setOrientation(getRotationStatus(), 16);
        this.mEditorViewDelegate.updateOriginalCompareButton();
    }

    protected View getFootAnimationTargetView() {
        return this.mWatermarkDelegate.getWMCategoryListView();
    }

    public void onPhotoChange(int leftPadding, int topPadding, int rightPadding, int bottomPadding, int width, int height) {
        WatermarkDelegate delegate = this.mWatermarkDelegate;
        if (delegate != null) {
            boolean z;
            if (MultiWindowStatusHolder.isInMultiMaintained()) {
                z = false;
            } else {
                z = true;
            }
            delegate.setShouldHideSoftKeyboardWhenLayoutChanged(z);
            delegate.setAbsoluteLayout(new int[]{leftPadding, topPadding, rightPadding, bottomPadding, width, height}, false);
        }
    }

    protected void onShowFootAnimeEnd() {
        super.onShowFootAnimeEnd();
        this.mWatermarkDelegate.setCanShowWatermarkData(true);
        this.mContainer.bringChildToFront(this.mHeadGroupView);
    }

    public void onMenuPrepared() {
        showFootAnime();
    }

    private int getRotationStatus() {
        if (this.mWatermarkDelegate == null || this.mWatermarkDelegate.getWatermarkBaseView() == null) {
            return 0;
        }
        int ori;
        switch (((Activity) this.mWatermarkDelegate.getWatermarkBaseView().getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
            case 0:
                ori = 0;
                break;
            case 1:
                ori = 90;
                break;
            case 2:
                ori = 180;
                break;
            case 3:
                ori = 270;
                break;
            default:
                ori = 0;
                break;
        }
        return ori;
    }
}
