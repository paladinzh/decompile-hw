package com.huawei.watermark;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.ui.watermarklib.WMCategoryListView;
import com.huawei.watermark.ui.watermarklib.WMDotListView;
import com.huawei.watermark.ui.watermarklib.WMLocalLibBaseView;
import com.huawei.watermark.ui.watermarklib.WMLocalLibPager;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmdata.WMSettingData;
import com.huawei.watermark.wmdata.wmlistdata.WMWatermarkListData;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmdata.wmlogicdata.WMShowRectData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMCustomConfigurationUtil;
import java.util.ArrayList;

public class WatermarkDelegate {
    public static final int ORIENTATION_TYPE_ROTATE_WATERMARK = 1;
    public static final int ORIENTATION_TYPE_ROTATE_WMLOCALLIB = 16;
    private int mAppIDInHealthPlatform = -1;
    private WMComponent mWMComponent = null;

    public interface LocationSettingDelegate {
        boolean getGPSMenuSetting();
    }

    public interface CategoryDataPreparedListener {
        void onCategoryDataPrepared();
    }

    public interface CurrentWMAvailableCallBack {
        void currentWMAvailable();

        void currentWMUnAvailable();
    }

    public interface DialogCallback {
        void onRequestConnectNet();

        void onShow(Dialog dialog);
    }

    public interface ExitWatermarkBySelfCallBack {
        void exit();
    }

    public interface TouchEventDelegateCallBack {
        void clearSuperDelegate();

        void setSuperDelegate();
    }

    public static class WaterMarkHolder {
        public float height;
        public float left = GroundOverlayOptions.NO_DIMENSION;
        private WaterMark mWaterMark;
        private View mWaterMarkView;
        public String name;
        public float top = GroundOverlayOptions.NO_DIMENSION;
        public Bitmap waterMarkBitmap;
        public float width;

        public WaterMarkHolder(WaterMark temp) {
            this.mWaterMark = temp;
            this.mWaterMark.setBeConvertToBitmap(true);
            this.name = this.mWaterMark.getLocationKey();
        }

        public float getX() {
            if (!viewEffective()) {
                return 0.0f;
            }
            this.left = this.mWaterMarkView.getX();
            return this.left;
        }

        public float getY() {
            if (!viewEffective()) {
                return 0.0f;
            }
            this.top = this.mWaterMarkView.getY();
            return this.top;
        }

        public Bitmap getWaterMarkBitmap() {
            if (!viewEffective()) {
                return null;
            }
            if (this.waterMarkBitmap == null) {
                this.waterMarkBitmap = WMBaseUtil.convertViewToBitmap(this.mWaterMarkView);
            }
            return this.waterMarkBitmap;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean viewEffective() {
            if (this.mWaterMarkView == null) {
                this.mWaterMarkView = this.mWaterMark.getViewForCurrentWaterMarkHolder();
            }
            if (this.mWaterMarkView == null || this.mWaterMarkView.getWidth() * this.mWaterMarkView.getHeight() == 0 || this.width * this.height == 0.0f) {
                return false;
            }
            return true;
        }

        public String getWaterMarkName() {
            return this.name;
        }

        public void release() {
            this.mWaterMarkView = null;
            this.mWaterMark.setBeConvertToBitmap(false);
            this.mWaterMark = null;
            if (this.waterMarkBitmap != null && !this.waterMarkBitmap.isRecycled()) {
                this.waterMarkBitmap.recycle();
                this.waterMarkBitmap = null;
            }
        }

        public boolean isResourceReady() {
            if (this.mWaterMark == null) {
                return false;
            }
            return this.mWaterMark.isResourceReady();
        }
    }

    public interface WatermarkFullScreenViewShowStatusCallBack {
        void setFullScreenViewShowStatus(boolean z);
    }

    public interface WatermarkLocalLibPageShowStatusCallBack {
        void setLocalLibPageShowStatus(boolean z);
    }

    public static boolean isWatermarkSupport() {
        return WMFileProcessor.getInstance().isWatermarkSupport();
    }

    public void setWatermarkBaseView(WMComponent component) {
        if (component != null) {
            this.mWMComponent = component;
            if (this.mWMComponent != null) {
                this.mWMComponent.setWatermarkDelegate(this);
            }
        }
    }

    public void setAppIDInHealthPlatform(int appID) {
        this.mAppIDInHealthPlatform = appID;
    }

    public int getAppIDInHealthPlatform() {
        return this.mAppIDInHealthPlatform;
    }

    public WMComponent getWatermarkBaseView() {
        return this.mWMComponent;
    }

    public void setCurrentWMAvailableCallBack(CurrentWMAvailableCallBack temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setCurrentWMAvailableCallBack(temp);
        }
    }

    public void setWatermarkLocalLibPageShowStatusCallBack(WatermarkLocalLibPageShowStatusCallBack temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWatermarkLocalLibPageShowStatusCallBack(temp);
        }
    }

    public void setWatermarkFullScreenViewShowStatusCallBack(WatermarkFullScreenViewShowStatusCallBack temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWatermarkFullScreenViewShowStatusCallBack(temp);
        }
    }

    public void setExitWatermarkBySelfCallBack(ExitWatermarkBySelfCallBack temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setExitWatermarkBySelfCallBack(temp);
        }
    }

    public void setLocationSettingDelegate(LocationSettingDelegate temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setLocationSettingDelegate(temp);
        }
    }

    public void setTouchEventDelegateCallBack(TouchEventDelegateCallBack temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setTouchEventDelegateCallBack(temp);
        }
    }

    public void resume() {
        if (this.mWMComponent != null) {
            this.mWMComponent.resume();
        }
    }

    public void pause() {
        if (this.mWMComponent != null) {
            this.mWMComponent.pause();
        }
    }

    public void hide() {
        if (this.mWMComponent != null) {
            this.mWMComponent.onHide();
        }
    }

    public void show() {
        if (this.mWMComponent != null) {
            this.mWMComponent.onShow();
        }
    }

    public void initWatermarkOrientationStatus(int orientation, int type) {
        if (this.mWMComponent != null) {
            this.mWMComponent.initOrientationStatus(orientation, type);
        }
    }

    public void setOrientation(int orientation, int type) {
        if (this.mWMComponent != null) {
            this.mWMComponent.onOrientationChanged(orientation, type);
        }
    }

    public void setToken(String token) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setToken(token);
        }
    }

    public String getToken() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getToken();
        }
        return null;
    }

    public void setWMShowWMLocalLibButton(View temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMShowWMLocalLibButton(temp);
        }
    }

    public View getWMShowWMLocalLibButton() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMShowWMLocalLibButton();
        }
        return null;
    }

    public void setWMLocalLibMenuBaseView(ViewGroup temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMLocalLibMenuBaseView(temp);
        }
    }

    public void setWMLocalLibBaseView(WMLocalLibBaseView temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMLocalLibBaseView(temp);
        }
    }

    public WMLocalLibBaseView getWMLocalLibBaseView() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMLocalLibBaseView();
        }
        return null;
    }

    public void setWMCategoryListView(WMCategoryListView temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMCategoryListView(temp);
        }
    }

    public WMCategoryListView getWMCategoryListView() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMCategoryListView();
        }
        return null;
    }

    public void setWMDotListView(WMDotListView temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMDotListView(temp);
        }
    }

    public WMDotListView getWMDotListView() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMDotListView();
        }
        return null;
    }

    public void setWMLocalLibPager(WMLocalLibPager temp) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setWMLocalLibPager(temp);
        }
    }

    public WMLocalLibPager getWMLocalLibPager() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMLocalLibPager();
        }
        return null;
    }

    public void setIgnoreUserOperateEventStatus(boolean ignore) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setIgnoreUserOperateEventStatus(ignore);
        }
    }

    public boolean getIgnoreUserOperateEventStatus() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getIgnoreUserOperateEventStatus();
        }
        return false;
    }

    public boolean canConsWatermarkPic() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.canConsWatermarkPic();
        }
        return true;
    }

    public ArrayList<WaterMarkHolder> getCurrentWaterMarkHolderList() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getmCurrentWaterMarkHolder();
        }
        return null;
    }

    public WaterMarkHolder getCurrentWaterMarkHolder() {
        if (this.mWMComponent == null) {
            return null;
        }
        ArrayList<WaterMarkHolder> tmpList = this.mWMComponent.getmCurrentWaterMarkHolder();
        if (tmpList == null || tmpList.isEmpty()) {
            return null;
        }
        return (WaterMarkHolder) tmpList.get(0);
    }

    public int[] getWMLocation() {
        if (this.mWMComponent != null) {
            return this.mWMComponent.getWMLocation();
        }
        return new int[0];
    }

    public boolean processEvent(MotionEvent event) {
        if (this.mWMComponent != null) {
            return this.mWMComponent.processEvent(event);
        }
        return false;
    }

    public void refreshAllView() {
        if (this.mWMComponent != null) {
            this.mWMComponent.judgeIfPrepareDisplayWatermarkView();
        }
    }

    public boolean onBackPressed() {
        if (this.mWMComponent == null) {
            return false;
        }
        return this.mWMComponent.onBackPressed();
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        if (this.mWMComponent != null) {
            return this.mWMComponent.superDispatchTouchEvent(event);
        }
        return false;
    }

    public void setUseTouchEventDelegateStatus(boolean use) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setUseTouchEventDelegateStatus(use);
        }
    }

    public void setCanShowWatermarkData(boolean show) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setCanShowWatermarkData(show);
        }
    }

    public void setCanShowWhenLocked(boolean show) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setCanShowWhenLocked(show);
        }
    }

    public void setAbsoluteLayout(int[] paddingAndWidthHeight, boolean isOnViewPager) {
        if (this.mWMComponent != null && paddingAndWidthHeight != null) {
            this.mWMComponent.setAbsoluteLayout(paddingAndWidthHeight, isOnViewPager);
        }
    }

    public void locationSettingStatusChanged(boolean on) {
        if (this.mWMComponent != null) {
            this.mWMComponent.locationSettingStatusChanged(on);
        }
    }

    public void setIsDMSupported(boolean supported) {
        WMCustomConfigurationUtil.setIsDMSupported(supported);
    }

    public static synchronized void clearAllUserCacheData(Context context) {
        synchronized (WatermarkDelegate.class) {
            if (context == null) {
                return;
            }
            WMWatermarkListData.getInstance(context).clearData();
            WMLogicData.getInstance(context).clearData();
            WMShowRectData.getInstance(context).clearData();
        }
    }

    public static void clearAllUserCacheAndSettingData(Context context) {
        if (context != null) {
            WMSettingData.getInstance(context).clearData();
            clearAllUserCacheData(context);
        }
    }

    public boolean isResourceReady() {
        if (getCurrentWaterMarkHolder() == null) {
            return false;
        }
        return getCurrentWaterMarkHolder().isResourceReady();
    }

    public void setShouldHideSoftKeyboardWhenLayoutChanged(boolean shouldHide) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setShouldHideSoftKeyboard(shouldHide);
        }
    }

    public void setDialogCallback(DialogCallback dialogCallback) {
        if (this.mWMComponent != null) {
            this.mWMComponent.setDialogCallback(dialogCallback);
        }
    }

    public Dialog getNetWorkDialog() {
        return null;
    }
}
