package com.huawei.watermark.manager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.watermark.WatermarkDelegate.DialogCallback;
import com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate;
import com.huawei.watermark.WatermarkDelegate.WaterMarkHolder;
import com.huawei.watermark.WatermarkDelegate.WatermarkFullScreenViewShowStatusCallBack;
import com.huawei.watermark.controller.callback.WMCurrentWaterMarkAvailableCallBack;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMParser;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.manager.parse.util.WMAltitudeService;
import com.huawei.watermark.manager.parse.util.WMAltitudeService.AltitudeUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMHealthyReportService;
import com.huawei.watermark.manager.parse.util.WMHealthyReportService.HealthUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMLocationService;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.manager.parse.util.WMWeatherHelper;
import com.huawei.watermark.manager.parse.util.WMWeatherService;
import com.huawei.watermark.manager.parse.util.WMWeatherService.WeatherUpdateCallback;
import com.huawei.watermark.report.HwWatermarkReporter;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmdata.WMSettingData;
import com.huawei.watermark.wmdata.wmcache.WaterMarkCache;
import com.huawei.watermark.wmdata.wmlogicdata.WMShowRectData;
import com.huawei.watermark.wmutil.WMCustomConfigurationUtil;
import com.huawei.watermark.wmutil.WMDialogUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.util.ArrayList;

public class WMManager implements LogicDelegate {
    private static final String TAG = WMManager.class.getSimpleName();
    private DialogCallback dialogCallback;
    private int mBottomPadding;
    private boolean mCanShowDialog;
    private boolean mCanShowWhenLocked;
    private Context mContext;
    private ArrayList<WaterMark> mCurrentWaterMarkList = null;
    private boolean mIgnoreUserOperateEvent = false;
    private int mImageHeight;
    private int mImageWidth;
    private LayoutInflater mInflater;
    private boolean mIsTouched;
    private int mLeftPadding;
    private Dialog mNetWorkDialog;
    private int mOri = 0;
    private int mRightPadding;
    private boolean mShouldHideSoftKeyboard = true;
    private String mToken;
    private int mTopPadding;
    private WMAltitudeService mWMAltitudeService;
    private WMCurrentWaterMarkAvailableCallBack mWMAvailableCallBack;
    private WMHealthyReportService mWMHealthyReportService;
    private WMLocationService mWMLocationService;
    private WMWaterMarkMoveStatusListener mWMWaterMarkMoveStatusListener;
    private WMWeatherHelper mWMWeatherHelper;
    private WMWeatherService mWMWeatherService;
    private WatermarkFullScreenViewShowStatusCallBack mWatermarkFullScreenViewShowStatusCallBack;
    private WMParser mWmParser;

    private class OnWMTouchListener implements OnTouchListener {
        private float mInParentX;
        private float mInParentY;
        private float mRawX;
        private float mRawY;
        private WaterMark mWaterMark;

        private OnWMTouchListener(WaterMark wm) {
            this.mWaterMark = wm;
        }

        public boolean onTouch(View view, MotionEvent event) {
            if (WMManager.this.mIgnoreUserOperateEvent) {
                return true;
            }
            WMManager.this.mIsTouched = true;
            switch (event.getAction()) {
                case 0:
                    if (WMManager.this.mWMWaterMarkMoveStatusListener != null) {
                        WMManager.this.mWMWaterMarkMoveStatusListener.setWaterMarkMoveStatus(1);
                    }
                    this.mRawX = event.getRawX();
                    this.mRawY = event.getRawY();
                    this.mInParentX = view.getX();
                    this.mInParentY = view.getY();
                    break;
                case 1:
                    if (!isMoveEnough((int) view.getX(), (int) view.getY())) {
                        this.mWaterMark.onWaterMarkClicked(event.getX(), event.getY());
                    }
                    if (WMManager.this.mWMWaterMarkMoveStatusListener != null) {
                        WMManager.this.mWMWaterMarkMoveStatusListener.setWaterMarkMoveStatus(2);
                    }
                    WMManager.this.mIsTouched = false;
                    break;
                case 2:
                    locateView(event, view);
                    break;
                default:
                    if (WMManager.this.mWMWaterMarkMoveStatusListener != null) {
                        WMManager.this.mWMWaterMarkMoveStatusListener.setWaterMarkMoveStatus(2);
                    }
                    WMManager.this.mIsTouched = false;
                    break;
            }
            return true;
        }

        private void locateView(MotionEvent event, View view) {
            if (WMManager.this.mContext != null && ((View) view.getParent()) != null) {
                boolean z;
                float x = (event.getRawX() - this.mRawX) + this.mInParentX;
                float y = (event.getRawY() - this.mRawY) + this.mInParentY;
                if (x < 0.0f) {
                    x = 0.0f;
                }
                if (y < 0.0f) {
                    y = 0.0f;
                }
                float width = (float) view.getWidth();
                float height = (float) view.getHeight();
                if (WMManager.this.mOri == 90 || WMManager.this.mOri == 270) {
                    z = true;
                } else {
                    z = false;
                }
                float[] wh = WMUIUtil.getWH(width, height, z);
                int[] xy = WMUIUtil.rebasePosition(x, y, wh[0], wh[1], WMShowRectData.getInstance(WMManager.this.mContext).getWMViewpagerWidth(), WMShowRectData.getInstance(WMManager.this.mContext).getWMViewpagerHeight());
                x = (float) xy[0];
                y = (float) xy[1];
                view.setX(x);
                view.setY(y);
                WMShowRectData.getInstance(WMManager.this.mContext).setWMViewSizeData(this.mWaterMark.getLocationKey(), view.getWidth(), view.getHeight(), this.mWaterMark.getScale());
                WMShowRectData.getInstance(WMManager.this.mContext).setWMMovePositionData(this.mWaterMark.getLocationKey() + WMManager.this.getToken(), x + "|" + y);
            }
        }

        private boolean isMoveEnough(int x, int y) {
            return ((((int) this.mInParentX) - x) * (((int) this.mInParentX) - x)) + ((((int) this.mInParentY) - y) * (((int) this.mInParentY) - y)) > 16;
        }
    }

    public interface WMWaterMarkMoveStatusListener {
        void setWaterMarkMoveStatus(int i);
    }

    public WMManager(Context context) {
        this.mContext = context;
        this.mWmParser = new WMParser();
        this.mInflater = LayoutInflater.from(context);
        this.mCanShowDialog = false;
        this.mWMWeatherHelper = new WMWeatherHelper(this.mContext);
        this.mWMAltitudeService = new WMAltitudeService(this.mContext, this.mWMWeatherHelper);
        this.mWMLocationService = new WMLocationService(this.mContext);
        this.mWMWeatherService = new WMWeatherService(this.mContext, this.mWMWeatherHelper);
        this.mWMLocationService.addLocationEventListener(this.mWMWeatherHelper.getLocationEventListener());
        this.mWMHealthyReportService = new WMHealthyReportService(this.mContext);
    }

    public boolean getWatermarkPreviewSizeInitFinish() {
        if (this.mImageWidth == 0 || this.mImageHeight == 0) {
            return false;
        }
        return true;
    }

    public void showDialog() {
        if (this.mCanShowDialog) {
            showNetworkTips();
        }
    }

    private void showNetworkTips() {
        final Runnable positiveRunable = new Runnable() {
            public void run() {
                WMSettingData.getInstance(WMManager.this.mContext).setBooleanValue("HAS_SHOW_NET_WORK_TIPS", true);
                WMManager.this.startWMServiceAndConnectNet();
                WMManager.this.onUserInteraction();
                HwWatermarkReporter.reportConfirmLocationAndWeatherInWaterMarkMode(WMManager.this.mContext, "yes");
            }
        };
        final Runnable negativeRunable = new Runnable() {
            public void run() {
                if (WMManager.this.mNetWorkDialog != null) {
                    WMManager.this.mNetWorkDialog = null;
                    WMManager.this.onUserInteraction();
                    HwWatermarkReporter.reportConfirmLocationAndWeatherInWaterMarkMode(WMManager.this.mContext, "no");
                }
            }
        };
        WMComponent wmComponent = (WMComponent) ((Activity) this.mContext).findViewById(WMResourceUtil.getId(this.mContext, "wm_component"));
        if (wmComponent == null || wmComponent.getVisibility() != 0) {
            this.mCanShowDialog = true;
            return;
        }
        this.mCanShowDialog = false;
        wmComponent.post(new Runnable() {
            public void run() {
                if (WMManager.this.mContext != null && !((Activity) WMManager.this.mContext).isDestroyed()) {
                    if (WMCustomConfigurationUtil.isChineseZone()) {
                        WMManager.this.mNetWorkDialog = WMDialogUtil.showWaterMarkDialog((Activity) WMManager.this.mContext, positiveRunable, negativeRunable);
                    } else if (positiveRunable != null) {
                        positiveRunable.run();
                    }
                    if (WMManager.this.dialogCallback != null) {
                        WMManager.this.dialogCallback.onShow(WMManager.this.mNetWorkDialog);
                    }
                }
            }
        });
    }

    private void startWMServiceAndConnectNet() {
        if (this.mWMLocationService != null) {
            this.mWMLocationService.start();
        }
        if (this.mWMWeatherService != null) {
            this.mWMWeatherService.start();
        }
        if (this.dialogCallback != null) {
            this.dialogCallback.onRequestConnectNet();
        }
    }

    private void onUserInteraction() {
        if (this.mContext != null) {
            ((Activity) this.mContext).onUserInteraction();
        }
    }

    public void setIgnoreUserOperateEventStatus(boolean ignore) {
        this.mIgnoreUserOperateEvent = ignore;
    }

    public boolean getIgnoreUserOperateEventStatus() {
        return this.mIgnoreUserOperateEvent;
    }

    public void addWaterMarkMoveStatusListener(WMWaterMarkMoveStatusListener temp) {
        this.mWMWaterMarkMoveStatusListener = temp;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public String getToken() {
        if (WMStringUtil.isEmptyString(this.mToken)) {
            return "";
        }
        return this.mToken;
    }

    public void setLocationSettingDelegate(LocationSettingDelegate temp) {
        if (this.mWMLocationService != null) {
            this.mWMLocationService.setLocationSettingDelegate(temp);
        }
    }

    public void onOrientationChanged(int ori) {
        this.mOri = ori;
    }

    public synchronized boolean canConsWatermarkPic() {
        boolean z = false;
        synchronized (this) {
            if (!(this.mCurrentWaterMarkList == null || this.mCurrentWaterMarkList.isEmpty())) {
                z = true;
            }
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized ArrayList<WaterMarkHolder> getmCurrentWaterMarkHolder() {
        ArrayList<WaterMarkHolder> holderList = new ArrayList();
        if (this.mCurrentWaterMarkList != null && !this.mCurrentWaterMarkList.isEmpty()) {
            for (final WaterMark wm : this.mCurrentWaterMarkList) {
                WaterMarkHolder holder = new WaterMarkHolder(wm);
                WaterMark tmpwm = wm;
                holder.width = (float) this.mImageWidth;
                holder.height = (float) this.mImageHeight;
                holderList.add(holder);
                ((Activity) this.mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        wm.hideAnimationTips();
                    }
                });
            }
            return holderList;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized View getWaterMark(int position) {
        View wmView;
        ViewGroup waterMarkItem = (ViewGroup) this.mInflater.inflate(WMResourceUtil.getLayoutId(this.mContext, "wm_jar_watermark_item"), null);
        String filePath = WMFileProcessor.getInstance().getNowCategoryWmPathWithPosition(this.mContext, getToken(), position);
        ArrayList<WaterMark> wmList = WaterMarkCache.getInstance().getWaterMark(filePath);
        if (wmList == null || wmList.isEmpty()) {
            wmList = this.mWmParser.parse(this.mContext, filePath);
            if (wmList != null && !wmList.isEmpty()) {
                for (WaterMark wm : wmList) {
                    if (wm == null) {
                        return waterMarkItem;
                    }
                    wm.initBaseLogicData(this.mContext, this);
                }
                WaterMarkCache.getInstance().storeWaterMark(filePath, wmList);
            }
        }
        wmList = WaterMarkCache.getInstance().getWaterMark(filePath);
        ViewGroup viewGoup = (ViewGroup) waterMarkItem.findViewById(WMResourceUtil.getId(this.mContext, "water_mark_parent"));
        if (this.mLeftPadding == 0 && this.mTopPadding == 0) {
            if (this.mRightPadding == 0) {
                if (this.mBottomPadding != 0) {
                }
                for (WaterMark wm2 : wmList) {
                    wm2.setBeConvertToBitmap(false);
                    wm2.setDisplayRectSizeType(this.mImageWidth, this.mImageHeight);
                    wm2.onOrientationChanged(this.mOri);
                    wm2.setCanShowWhenLocked(this.mCanShowWhenLocked);
                    wm2.onScaleSizeChange(this.mContext, (float) this.mImageWidth, (float) this.mImageHeight);
                    wmView = wm2.getViewForShowOnViewpager(this.mContext);
                    if (wmView == null) {
                        return waterMarkItem;
                    }
                    wmView.setOnTouchListener(new OnWMTouchListener(wm2));
                    WMUIUtil.separateView(wmView);
                    wm2.setWMLayoutParams();
                    viewGoup.addView(wmView);
                }
                if (position == WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mContext, getToken())) {
                    this.mCurrentWaterMarkList = wmList;
                    if (this.mWMAvailableCallBack != null) {
                        this.mWMAvailableCallBack.setWMAvailable(true);
                    }
                }
            }
        }
        setPreviewLayout(viewGoup);
        for (WaterMark wm22 : wmList) {
            wm22.setBeConvertToBitmap(false);
            wm22.setDisplayRectSizeType(this.mImageWidth, this.mImageHeight);
            wm22.onOrientationChanged(this.mOri);
            wm22.setCanShowWhenLocked(this.mCanShowWhenLocked);
            wm22.onScaleSizeChange(this.mContext, (float) this.mImageWidth, (float) this.mImageHeight);
            wmView = wm22.getViewForShowOnViewpager(this.mContext);
            if (wmView == null) {
                return waterMarkItem;
            }
            wmView.setOnTouchListener(new OnWMTouchListener(wm22));
            WMUIUtil.separateView(wmView);
            wm22.setWMLayoutParams();
            viewGoup.addView(wmView);
        }
        if (position == WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mContext, getToken())) {
            this.mCurrentWaterMarkList = wmList;
            if (this.mWMAvailableCallBack != null) {
                this.mWMAvailableCallBack.setWMAvailable(true);
            }
        }
    }

    public boolean setLayout(int[] paddingAndWidthHeight) {
        boolean hasChanged = true;
        if (this.mContext == null || paddingAndWidthHeight == null) {
            return false;
        }
        int leftPadding = paddingAndWidthHeight[0];
        int topPadding = paddingAndWidthHeight[1];
        int rightPadding = paddingAndWidthHeight[2];
        int bottomPadding = paddingAndWidthHeight[3];
        int width = paddingAndWidthHeight[4];
        int height = paddingAndWidthHeight[5];
        if (this.mLeftPadding == leftPadding && this.mTopPadding == topPadding && this.mRightPadding == rightPadding && this.mBottomPadding == bottomPadding && this.mImageWidth == width && this.mImageHeight == height) {
            hasChanged = false;
        }
        if (!hasChanged) {
            return false;
        }
        if (!(width == 0 || this.mImageWidth == 0 || width == this.mImageWidth)) {
            WMShowRectData.getInstance(this.mContext).updateViewSizeAndPositionByNewScale(((float) width) / ((float) this.mImageWidth));
        }
        this.mLeftPadding = leftPadding;
        this.mTopPadding = topPadding;
        this.mRightPadding = rightPadding;
        this.mBottomPadding = bottomPadding;
        this.mImageWidth = width;
        this.mImageHeight = height;
        WMShowRectData.getInstance(this.mContext).setWMViewpagerWidth(width);
        WMShowRectData.getInstance(this.mContext).setWMViewpagerHeight(height);
        return hasChanged;
    }

    public boolean setLayout(int width, int height) {
        if (this.mContext == null) {
            return false;
        }
        boolean hasChanged = (this.mImageWidth == width && this.mImageHeight == height) ? false : true;
        if (!(width == 0 || this.mImageWidth == 0 || width == this.mImageWidth)) {
            WMShowRectData.getInstance(this.mContext).updateViewSizeAndPositionByNewScale(((float) width) / ((float) this.mImageWidth));
        }
        this.mLeftPadding = 0;
        this.mTopPadding = 0;
        this.mRightPadding = 0;
        this.mBottomPadding = 0;
        this.mImageWidth = width;
        this.mImageHeight = height;
        WMShowRectData.getInstance(this.mContext).setWMViewpagerWidth(width);
        WMShowRectData.getInstance(this.mContext).setWMViewpagerHeight(height);
        return hasChanged;
    }

    public void resetAbsoluteLayout() {
        this.mLeftPadding = 0;
        this.mTopPadding = 0;
        this.mRightPadding = 0;
        this.mBottomPadding = 0;
        this.mImageWidth = 0;
        this.mImageHeight = 0;
    }

    private void setPreviewLayout(ViewGroup viewGroup) {
        LayoutParams params = new LayoutParams(this.mImageWidth, this.mImageHeight);
        params.setMargins(this.mLeftPadding, this.mTopPadding, this.mRightPadding, this.mBottomPadding);
        viewGroup.setLayoutParams(params);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onPageSelected(int index) {
        ArrayList<WaterMark> wmlist = WaterMarkCache.getInstance().getWaterMark(WMFileProcessor.getInstance().getNowCategoryWmPathWithPosition(this.mContext, getToken(), index));
        this.mCurrentWaterMarkList = wmlist;
        if (wmlist != null && !wmlist.isEmpty()) {
            for (WaterMark wm : wmlist) {
                wm.showAnimationTips();
            }
        }
    }

    public void destroyWaterMark(int position) {
        ArrayList<WaterMark> wmlist = WaterMarkCache.getInstance().getWaterMark(WMFileProcessor.getInstance().getNowCategoryWmPathWithPosition(this.mContext, getToken(), position));
        if (wmlist != null && !wmlist.isEmpty()) {
            for (WaterMark wm : wmlist) {
                wm.destoryWaterMark();
            }
        }
    }

    public ArrayList<View> getWaterMarkRoot(int position) {
        ArrayList<WaterMark> wmlist = WaterMarkCache.getInstance().getWaterMark(WMFileProcessor.getInstance().getNowCategoryWmPathWithPosition(this.mContext, getToken(), position));
        ArrayList<View> viewlist = new ArrayList();
        if (wmlist == null || wmlist.isEmpty()) {
            return null;
        }
        for (WaterMark wm : wmlist) {
            if (wm.getWaterMarkRoot() != null) {
                viewlist.add(wm.getWaterMarkRoot());
            }
        }
        return viewlist;
    }

    public void resume() {
        if (this.mContext != null) {
            if (this.mWMAltitudeService != null) {
                this.mWMAltitudeService.start();
            }
            if (this.mWMHealthyReportService != null) {
                this.mWMHealthyReportService.start();
            }
            if (WMSettingData.getInstance(this.mContext).getBooleanValue("HAS_SHOW_NET_WORK_TIPS", false)) {
                this.mWMLocationService.start();
                this.mWMWeatherService.start();
            } else {
                showNetworkTips();
            }
        }
    }

    public void pause() {
        recyleCurrentWaterMarks();
        if (this.mWMLocationService != null) {
            this.mWMLocationService.release();
        }
        if (this.mWMWeatherService != null) {
            this.mWMWeatherService.release();
        }
        if (this.mWMAltitudeService != null) {
            this.mWMAltitudeService.release();
        }
        if (this.mWMHealthyReportService != null) {
            this.mWMHealthyReportService.release();
        }
        if (this.mNetWorkDialog != null) {
            WMComponent wmComponent = (WMComponent) ((Activity) this.mContext).findViewById(WMResourceUtil.getId(this.mContext, "wm_component"));
            if (wmComponent != null) {
                wmComponent.post(new Runnable() {
                    public void run() {
                        if (WMManager.this.mNetWorkDialog != null && WMManager.this.mContext != null && !((Activity) WMManager.this.mContext).isDestroyed()) {
                            WMManager.this.mNetWorkDialog.dismiss();
                            WMManager.this.mNetWorkDialog = null;
                        }
                    }
                });
            }
        }
    }

    public boolean processEvent(MotionEvent event) {
        return this.mIsTouched;
    }

    public synchronized void setWMAvailableCallBack(WMCurrentWaterMarkAvailableCallBack callback) {
        this.mWMAvailableCallBack = callback;
    }

    public void locationSettingStatusChanged(boolean on) {
        if (this.mWMLocationService != null) {
            this.mWMLocationService.locationSettingStatusChanged(on);
        }
    }

    public synchronized void setCanShowWhenLocked(boolean show) {
        this.mCanShowWhenLocked = show;
    }

    public synchronized void recyleCurrentWaterMarks() {
        final int position = WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(this.mContext, getToken());
        ArrayList<View> rootlist = getWaterMarkRoot(position);
        if (!(rootlist == null || rootlist.isEmpty())) {
            for (View root : rootlist) {
                root.setVisibility(4);
            }
        }
        ((Activity) this.mContext).runOnUiThread(new Runnable() {
            public void run() {
                WMManager.this.destroyWaterMark(position);
            }
        });
        this.mCurrentWaterMarkList = null;
        WaterMarkCache.getInstance().releaseWaterMark(WMFileProcessor.getInstance().getNowCategoryWmPathWithPosition(this.mContext, getToken(), position));
    }

    public void setWatermarkFullScreenViewShowStatusCallBack(WatermarkFullScreenViewShowStatusCallBack temp) {
        this.mWatermarkFullScreenViewShowStatusCallBack = temp;
    }

    public String getAPPToken() {
        return getToken();
    }

    public void addLocationUpdateCallback(LocationUpdateCallback locationUpdateCallback) {
        this.mWMLocationService.addLocationUpdateCallback(locationUpdateCallback);
    }

    public void addAltitudeUpdateCallback(AltitudeUpdateCallback altitudeUpdateCallback) {
        this.mWMAltitudeService.addAltitudeUpdateCallback(altitudeUpdateCallback);
    }

    public void addHealthUpdateCallback(HealthUpdateCallback healthUpdateCallback) {
        this.mWMHealthyReportService.addHealthUpdateCallback(healthUpdateCallback);
    }

    public void addWeatherUpdateCallback(WeatherUpdateCallback weatherUpdateCallback) {
        this.mWMWeatherService.addWeatherUpdateCallback(weatherUpdateCallback);
    }

    public void setFullScreenViewShowStatus(boolean status) {
        if (this.mWatermarkFullScreenViewShowStatusCallBack != null) {
            this.mWatermarkFullScreenViewShowStatusCallBack.setFullScreenViewShowStatus(status);
        }
    }

    public boolean getShouldHideSoftKeyboard() {
        return this.mShouldHideSoftKeyboard;
    }

    public void setShouldHideSoftKeyboard(boolean shouldHide) {
        this.mShouldHideSoftKeyboard = shouldHide;
    }

    public void setDialogCallback(DialogCallback dialogCallback) {
        this.dialogCallback = dialogCallback;
    }
}
