package com.huawei.watermark.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.huawei.watermark.WatermarkDelegate;
import com.huawei.watermark.WatermarkDelegate.CurrentWMAvailableCallBack;
import com.huawei.watermark.WatermarkDelegate.DialogCallback;
import com.huawei.watermark.WatermarkDelegate.ExitWatermarkBySelfCallBack;
import com.huawei.watermark.WatermarkDelegate.LocationSettingDelegate;
import com.huawei.watermark.WatermarkDelegate.TouchEventDelegateCallBack;
import com.huawei.watermark.WatermarkDelegate.WaterMarkHolder;
import com.huawei.watermark.WatermarkDelegate.WatermarkFullScreenViewShowStatusCallBack;
import com.huawei.watermark.WatermarkDelegate.WatermarkLocalLibPageShowStatusCallBack;
import com.huawei.watermark.controller.callback.WMCurrentWaterMarkAvailableCallBack;
import com.huawei.watermark.manager.WMManager;
import com.huawei.watermark.manager.WMManager.WMWaterMarkMoveStatusListener;
import com.huawei.watermark.ui.baseview.viewpager.WMViewPager.OnPageChangeListener;
import com.huawei.watermark.ui.watermarklib.WMCategoryListView;
import com.huawei.watermark.ui.watermarklib.WMDotListView;
import com.huawei.watermark.ui.watermarklib.WMLocalLib;
import com.huawei.watermark.ui.watermarklib.WMLocalLibBaseView;
import com.huawei.watermark.ui.watermarklib.WMLocalLibPager;
import com.huawei.watermark.wmdata.WMFileProcessor;
import com.huawei.watermark.wmdata.WMFileProcessor.OnWatermarkDataInitStatusListener;
import com.huawei.watermark.wmdata.wmcache.WaterMarkCache;
import com.huawei.watermark.wmdata.wmlistdata.WMWatermarkListData;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmdata.wmlogicdata.WMShowRectData;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMBitmapFactory;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.util.ArrayList;
import java.util.HashMap;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class WMComponent extends RelativeLayout implements WMCurrentWaterMarkAvailableCallBack {
    public static final int ORI_0 = 0;
    public static final int ORI_180 = 180;
    public static final int ORI_270 = 270;
    public static final int ORI_90 = 90;
    public static final String SP_SUPPORT_LANGUAGE_TYPE = "support_language_type";
    private static final String TAG = WMComponent.class.getSimpleName();
    private ImageView arrowBottom;
    private ImageView arrowLeft;
    private ImageView arrowRight;
    private ImageView arrowTop;
    private int currentItem;
    private long lastShowHeadTipsTime = 0;
    private int mBottomPadding;
    private boolean mCanShowWMSetByAPP = true;
    CurrentWMAvailableCallBack mCurrentWMAvailableCallBack = null;
    private HashMap<String, String> mEventCacheWhenEventCannotTakeEffect = new HashMap();
    ExitWatermarkBySelfCallBack mExitWatermarkBySelfCallBack = null;
    private int mImageHeight;
    private int mImageWidth;
    public boolean mIsPause = false;
    private int mLeftPadding;
    private int mNowSupportLanguageType = -1;
    private int mOrientationStatus = 0;
    private int mOrientationType = 1;
    public boolean mPreparedDisplayWatermarkView = false;
    private int mRightPadding;
    private boolean mScanWMFileFinish = false;
    public boolean mShowLocalLibMenu = false;
    private int mStayAtStartEndCount = 0;
    private int mTopPadding;
    TouchEventDelegateCallBack mTouchEventDelegateCallBack = null;
    private boolean mUseTouchEventDelegate = false;
    private FrameLayout mWMBaseLayout;
    private WMLocalLib mWMLocalLib;
    private View mWMLocalLibShowButton;
    private WMPager mWMPager;
    private WMWaterMarkMoveStatusListener mWMWaterMarkMoveStatusListener = new WMWaterMarkMoveStatusListener() {
        public void setWaterMarkMoveStatus(int status) {
            if (WMComponent.this.mWMPager == null) {
                return;
            }
            if (status == 1) {
                WMComponent.this.mWMPager.disableScroll();
            } else {
                WMComponent.this.mWMPager.enableScroll();
            }
        }
    };
    private WMManager mWMmanager;
    private WatermarkDelegate mWatermarkDelegate = null;
    WatermarkLocalLibPageShowStatusCallBack mWatermarkLocalLibPageShowStatusCallBack = null;
    private int mWatermarkOrientationStatus = 0;
    private WMPagerAdapter mWmPagerAdapter;
    private boolean setLayoutOnViewPager = false;

    public WMComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mNowSupportLanguageType = WMWatermarkListData.getInstance(context).getIntValue(SP_SUPPORT_LANGUAGE_TYPE, -1);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mWMBaseLayout = (FrameLayout) findViewById(WMResourceUtil.getId(getContext(), "wm_base_layout"));
        this.mWMLocalLib = new WMLocalLib(this);
        this.mWMmanager = new WMManager(getContext());
        this.mWMmanager.setWMAvailableCallBack(this);
        this.mWMmanager.addWaterMarkMoveStatusListener(this.mWMWaterMarkMoveStatusListener);
    }

    private void handlePrevious() {
        this.currentItem = this.mWMPager.getCurrentItemIfNeedReverse();
        if (this.currentItem == 0) {
            showLocalLibMenu();
        } else {
            this.mWMPager.setCurrentItem(this.currentItem - 1);
        }
    }

    private void handleNext() {
        this.currentItem = this.mWMPager.getCurrentItemIfNeedReverse();
        if (this.mWmPagerAdapter != null) {
            if (this.currentItem == this.mWmPagerAdapter.getCount() - 1) {
                showLocalLibMenu();
            } else {
                this.mWMPager.setCurrentItem(this.currentItem + 1);
            }
        }
    }

    private boolean getWatermarkPreviewSizeInitFinish() {
        if (this.mWMmanager != null) {
            return this.mWMmanager.getWatermarkPreviewSizeInitFinish();
        }
        return false;
    }

    public void setAbsoluteLayout(final int[] paddingAndWidthHeight, final boolean ifOnViewPager) {
        post(new Runnable() {
            public void run() {
                WMComponent.this.setAbsoluteLayoutOnUiThread(paddingAndWidthHeight, ifOnViewPager);
            }
        });
    }

    public void setAbsoluteLayoutOnUiThread(int[] paddingAndWidthHeight, boolean ifOnViewPager) {
        this.setLayoutOnViewPager = ifOnViewPager;
        if (this.mWMmanager != null && paddingAndWidthHeight != null && !this.mIsPause) {
            boolean hasChanged;
            if (this.setLayoutOnViewPager) {
                int leftPadding = paddingAndWidthHeight[0];
                int topPadding = paddingAndWidthHeight[1];
                int rightPadding = paddingAndWidthHeight[2];
                int bottomPadding = paddingAndWidthHeight[3];
                int width = paddingAndWidthHeight[4];
                int height = paddingAndWidthHeight[5];
                hasChanged = (this.mLeftPadding == leftPadding && this.mTopPadding == topPadding && this.mRightPadding == rightPadding && this.mBottomPadding == bottomPadding && this.mImageWidth == width) ? this.mImageHeight != height : true;
                if (hasChanged) {
                    this.mLeftPadding = leftPadding;
                    this.mTopPadding = topPadding;
                    this.mRightPadding = rightPadding;
                    this.mBottomPadding = bottomPadding;
                    this.mImageWidth = width;
                    this.mImageHeight = height;
                    this.mWMmanager.setLayout(width, height);
                    if (this.mWMPager != null) {
                        LayoutParams params = new LayoutParams(this.mImageWidth, this.mImageHeight);
                        params.setMargins(this.mLeftPadding, this.mTopPadding, this.mRightPadding, this.mBottomPadding);
                        this.mWMPager.setLayoutParams(params);
                    }
                }
            } else {
                hasChanged = this.mWMmanager.setLayout(paddingAndWidthHeight);
            }
            if (hasChanged) {
                clearWatermarkViewpagerViewAndData();
                WaterMarkCache.getInstance().release();
                judgeIfPrepareDisplayWatermarkView();
            }
        }
    }

    private void resetAbsoluteLayout() {
        if (this.setLayoutOnViewPager) {
            this.mLeftPadding = 0;
            this.mTopPadding = 0;
            this.mRightPadding = 0;
            this.mBottomPadding = 0;
            this.mImageWidth = 0;
            this.mImageHeight = 0;
        } else if (this.mWMmanager != null) {
            this.mWMmanager.resetAbsoluteLayout();
        }
    }

    public void initOrientationStatus(int orientation, int type) {
        this.mOrientationStatus = orientationValue(orientation);
        this.mOrientationType = type;
        if (WMBaseUtil.containType(type, 1)) {
            this.mWatermarkOrientationStatus = this.mOrientationStatus;
        }
        if (WMBaseUtil.containType(type, 16) && this.mWMLocalLib != null) {
            this.mWMLocalLib.initOrientationChanged(this.mOrientationStatus, type);
        }
    }

    public void initView() {
        initContentView();
    }

    public void initWatermarkDataAndLibData() {
        if (initWatermarkData() == 3) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    WMComponent.this.categoryDataPrepared();
                    WMComponent.this.allWatermarkDataPrepared();
                }
            });
        } else {
            this.mScanWMFileFinish = false;
        }
    }

    private void categoryDataPrepared() {
        refreshWMLocalLibData(1);
    }

    private void allWatermarkDataPrepared() {
        this.mScanWMFileFinish = true;
        refreshWMLocalLibData(2);
        judgeIfPrepareDisplayWatermarkView();
    }

    public void refreshAllView() {
        if (this.mPreparedDisplayWatermarkView) {
            refreshWMViewPager();
        }
    }

    public void setCanShowWatermarkData(boolean show) {
        this.mCanShowWMSetByAPP = show;
        judgeIfPrepareDisplayWatermarkView();
    }

    public void setCanShowWhenLocked(boolean show) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setCanShowWhenLocked(show);
        }
    }

    public void judgeIfPrepareDisplayWatermarkView() {
        if (!this.mIsPause) {
            boolean preparedDisplayWatermarkView = this.mPreparedDisplayWatermarkView;
            if (this.mCanShowWMSetByAPP && this.mScanWMFileFinish && getWatermarkPreviewSizeInitFinish()) {
                this.mPreparedDisplayWatermarkView = true;
            }
            if (this.mWmPagerAdapter != null) {
                this.mWmPagerAdapter.setCountIsZero(!this.mPreparedDisplayWatermarkView);
            }
            if (this.mWMLocalLibShowButton != null) {
                post(new Runnable() {
                    public void run() {
                        WMComponent.this.mWMLocalLibShowButton.setVisibility(WMComponent.this.mPreparedDisplayWatermarkView ? 0 : 4);
                    }
                });
            }
            if (this.mPreparedDisplayWatermarkView) {
                if (ifHasWatermarkToShow()) {
                    boolean fromHideToShow = false;
                    if (!preparedDisplayWatermarkView && this.mPreparedDisplayWatermarkView) {
                        fromHideToShow = true;
                    }
                    if (fromHideToShow) {
                        post(new Runnable() {
                            public void run() {
                                WMComponent.this.setCurrentItemAndCallOnPageSelected(WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(WMComponent.this.getContext(), WMComponent.this.getToken()));
                            }
                        });
                    } else {
                        post(new Runnable() {
                            public void run() {
                                WMComponent.this.refreshAllView();
                            }
                        });
                    }
                } else {
                    setWMAvailable(true);
                }
            }
        }
    }

    public boolean ifHasWatermarkToShow() {
        if (WMFileProcessor.getInstance().getNowTypeWMCount(getContext(), getToken()) > 0) {
            return true;
        }
        return false;
    }

    public void locationSettingStatusChanged(boolean on) {
        if (this.mWMmanager != null) {
            this.mWMmanager.locationSettingStatusChanged(on);
        }
    }

    public int initWatermarkData() {
        return WMFileProcessor.getInstance().scanAssetsInitWatermarkData(getContext(), new OnWatermarkDataInitStatusListener() {
            public void onInitStart() {
            }

            public void onInitFinishCategoryData() {
                ((Activity) WMComponent.this.getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        WMComponent.this.categoryDataPrepared();
                    }
                });
            }

            public void onInitFinish() {
                ((Activity) WMComponent.this.getContext()).runOnUiThread(new Runnable() {
                    public void run() {
                        WMComponent.this.allWatermarkDataPrepared();
                    }
                });
            }
        });
    }

    private void initContentView() {
        postDelayed(new Runnable() {
            public void run() {
                WMComponent.this.initWatermarkViewPager();
                WMComponent.this.initWatermarkLocallibView();
                WMComponent.this.showArrows(WMComponent.this.mOrientationStatus, WMComponent.this.mOrientationType);
            }
        }, 500);
    }

    private void releaseContentView() {
        releaseWatermarkViewPager();
        releaseWatermarkLocallibView();
    }

    private void initWatermarkLocallibView() {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.initView();
        }
    }

    private void releaseWatermarkLocallibView() {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.release();
        }
    }

    private void initWatermarkViewPager() {
        this.mWMPager = new WMPager(getContext());
        LayoutParams fl = getLayoutParamsByWH();
        if (checkPaddingValue()) {
            fl.setMargins(this.mLeftPadding, this.mTopPadding, this.mRightPadding, this.mBottomPadding);
        }
        initWatermarkAdapter();
        this.mWMPager.setLayoutParams(fl);
        this.mWMPager.setUseTouchEventDelegateStatus(this.mUseTouchEventDelegate);
        this.mWMPager.onOrientationChanged(this.mWatermarkOrientationStatus);
        this.mWMPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {
                if (WMComponent.this.canShowLocalLibMenu(i, v, i2)) {
                    WMComponent wMComponent = WMComponent.this;
                    wMComponent.mStayAtStartEndCount = wMComponent.mStayAtStartEndCount + 1;
                    if (WMComponent.this.mStayAtStartEndCount >= 6) {
                        WMComponent.this.swipeAtHeadFoot();
                        WMComponent.this.mStayAtStartEndCount = 0;
                        if (WMComponent.this.mTouchEventDelegateCallBack != null) {
                            WMComponent.this.mTouchEventDelegateCallBack.clearSuperDelegate();
                            return;
                        }
                        return;
                    }
                    return;
                }
                WMComponent.this.mStayAtStartEndCount = 0;
            }

            public void onPageSelected(int i) {
                if (WMComponent.this.mWmPagerAdapter != null && WMComponent.this.mWMmanager != null && !WMComponent.this.mWmPagerAdapter.getCountIsZero()) {
                    i = WMComponent.this.mWmPagerAdapter.getPosition(i);
                    WMFileProcessor.getInstance().setNowWatermarkInCategoryIndex(WMComponent.this.getContext(), WMComponent.this.getToken(), i);
                    WMComponent.this.mWMmanager.onPageSelected(i);
                }
            }

            public void onPageScrollStateChanged(int i) {
                if (WMComponent.this.mTouchEventDelegateCallBack != null) {
                    WMComponent.this.checkScrollState(i);
                }
            }
        });
        if (this.mWMBaseLayout != null) {
            this.mWMBaseLayout.addView(this.mWMPager);
            View arrowsLayout = LayoutInflater.from(getContext()).inflate(R.layout.wm_arrows, this, false);
            this.mWMBaseLayout.addView(arrowsLayout);
            this.arrowLeft = (ImageView) arrowsLayout.findViewById(WMResourceUtil.getId(getContext(), "arrow_left"));
            this.arrowRight = (ImageView) arrowsLayout.findViewById(WMResourceUtil.getId(getContext(), "arrow_right"));
            this.arrowTop = (ImageView) arrowsLayout.findViewById(WMResourceUtil.getId(getContext(), "arrow_top"));
            this.arrowBottom = (ImageView) arrowsLayout.findViewById(WMResourceUtil.getId(getContext(), "arrow_bottom"));
            final boolean isRTL = WMUIUtil.isLayoutDirectionRTL(getContext());
            this.arrowLeft.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    WMComponent.this.arrowLeftClickEvent(isRTL);
                }
            });
            this.arrowRight.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    WMComponent.this.arrowRightClickEvent(isRTL);
                }
            });
            this.arrowTop.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    WMComponent.this.arrowTopClickEvent(isRTL);
                }
            });
            this.arrowBottom.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    WMComponent.this.arrowBottomClickEvent(isRTL);
                }
            });
        }
    }

    private void checkScrollState(int i) {
        switch (i) {
            case 1:
            case 2:
                this.mTouchEventDelegateCallBack.setSuperDelegate();
                return;
            default:
                this.mTouchEventDelegateCallBack.clearSuperDelegate();
                return;
        }
    }

    private void arrowBottomClickEvent(boolean isRTL) {
        if (this.mOrientationStatus == 90) {
            handlePreviousAccordingDirection(isRTL);
        }
        if (this.mOrientationStatus == 270) {
            handleNextAccordingDirection(isRTL);
        }
    }

    private void arrowTopClickEvent(boolean isRTL) {
        if (this.mOrientationStatus == 90) {
            handleNextAccordingDirection(isRTL);
        }
        if (this.mOrientationStatus == 270) {
            handlePreviousAccordingDirection(isRTL);
        }
    }

    private void arrowRightClickEvent(boolean isRTL) {
        if (this.mOrientationType == 16) {
            handleNextAccordingDirection(isRTL);
            return;
        }
        if (this.mOrientationStatus == 0) {
            handleNextAccordingDirection(isRTL);
        }
        if (this.mOrientationStatus == 180) {
            handlePreviousAccordingDirection(isRTL);
        }
    }

    private void arrowLeftClickEvent(boolean isRTL) {
        if (this.mOrientationType == 16) {
            handlePreviousAccordingDirection(isRTL);
            return;
        }
        if (this.mOrientationStatus == 0) {
            handlePreviousAccordingDirection(isRTL);
        }
        if (this.mOrientationStatus == 180) {
            handleNextAccordingDirection(isRTL);
        }
    }

    private boolean checkPaddingValue() {
        return (this.mLeftPadding == 0 && this.mTopPadding == 0 && this.mRightPadding == 0 && this.mBottomPadding == 0) ? false : true;
    }

    @NonNull
    private LayoutParams getLayoutParamsByWH() {
        int w = -1;
        if (this.mImageWidth != 0) {
            w = this.mImageWidth;
        }
        int h = -1;
        if (!(this.mImageWidth == 0 && this.mImageHeight == 0)) {
            h = this.mImageHeight;
        }
        return new LayoutParams(w, h);
    }

    private void handlePreviousAccordingDirection(boolean rTL) {
        if (rTL) {
            handleNext();
        } else {
            handlePrevious();
        }
    }

    private void handleNextAccordingDirection(boolean rTL) {
        if (rTL) {
            handlePrevious();
        } else {
            handleNext();
        }
    }

    private void releaseWatermarkViewPager() {
        if (this.mWMLocalLibShowButton != null) {
            this.mWMLocalLibShowButton.setVisibility(4);
        }
        if (this.mWMBaseLayout != null) {
            this.mWMBaseLayout.removeAllViews();
        }
        if (this.mWMPager != null) {
            this.mWMPager.removeAllViews();
            this.mWMPager = null;
        }
        this.mWmPagerAdapter = null;
    }

    public void refreshWMViewPager() {
        if (!(this.mIsPause || this.mWmPagerAdapter == null)) {
            this.mWmPagerAdapter.notifyDataSetChanged();
        }
    }

    public void initWatermarkAdapter() {
        if (!this.mIsPause && this.mWmPagerAdapter == null) {
            this.mWmPagerAdapter = new WMPagerAdapter(getContext(), this.mWMmanager, this.mWMPager, !this.mPreparedDisplayWatermarkView);
            this.mWMPager.setAdapter(this.mWmPagerAdapter);
            if (WMFileProcessor.getInstance().getFinishInitWatermarkData()) {
                setCurrentItemAndCallOnPageSelected(WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(getContext(), getToken()));
            }
        }
    }

    private void resetEventWhenEventCanTakeEffect() {
        resetOrientationEventWhenEventCanTakeEffect();
    }

    private void resetOrientationEventWhenEventCanTakeEffect() {
        if (this.mEventCacheWhenEventCannotTakeEffect.containsKey("ORIENTATION_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY") && this.mEventCacheWhenEventCannotTakeEffect.containsKey("ORIENTATIONTYPE_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY")) {
            final int orientation = Integer.parseInt((String) this.mEventCacheWhenEventCannotTakeEffect.get("ORIENTATION_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY"));
            final int type = Integer.parseInt((String) this.mEventCacheWhenEventCannotTakeEffect.get("ORIENTATIONTYPE_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY"));
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                onOrientationChanged(orientation, type);
            } else {
                post(new Runnable() {
                    public void run() {
                        WMComponent.this.onOrientationChanged(orientation, type);
                    }
                });
            }
            this.mEventCacheWhenEventCannotTakeEffect.remove("ORIENTATION_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY");
            this.mEventCacheWhenEventCannotTakeEffect.remove("ORIENTATIONTYPE_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY");
        }
    }

    public void setIgnoreUserOperateEventStatus(boolean ignore) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setIgnoreUserOperateEventStatus(ignore);
            if (this.mWMLocalLibShowButton != null) {
                this.mWMLocalLibShowButton.setClickable(!ignore);
            }
            if (!ignore) {
                resetEventWhenEventCanTakeEffect();
            }
        }
    }

    public boolean getIgnoreUserOperateEventStatus() {
        if (this.mWMmanager != null) {
            return this.mWMmanager.getIgnoreUserOperateEventStatus();
        }
        return false;
    }

    public boolean canConsWatermarkPic() {
        if (this.mWMmanager != null) {
            return this.mWMmanager.canConsWatermarkPic();
        }
        return true;
    }

    public ArrayList<WaterMarkHolder> getmCurrentWaterMarkHolder() {
        if (this.mWMmanager != null) {
            return this.mWMmanager.getmCurrentWaterMarkHolder();
        }
        return null;
    }

    public int[] getWMLocation() {
        if (this.mWMPager == null) {
            return new int[0];
        }
        int[] location = new int[2];
        if (this.setLayoutOnViewPager) {
            this.mWMPager.getLocationOnScreen(location);
        } else {
            getLocationOnScreen(location);
        }
        return location;
    }

    public boolean processEvent(MotionEvent event) {
        if (this.mWMmanager != null) {
            return this.mWMmanager.processEvent(event);
        }
        return false;
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        if (this.mWMPager == null || this.mWMmanager == null || this.mWMmanager.getIgnoreUserOperateEventStatus()) {
            return false;
        }
        return this.mWMPager.superDispatchTouchEvent(event);
    }

    public int orientationValue(int orientation) {
        if ((orientation >= 0 && orientation < 45) || (orientation >= SmsCheckResult.ESCT_315 && orientation < 360)) {
            return 0;
        }
        if (orientation >= 45 && orientation < 135) {
            return 90;
        }
        if (orientation >= 135 && orientation < 225) {
            return 180;
        }
        if (orientation < 225 || orientation >= SmsCheckResult.ESCT_315) {
            return 0;
        }
        return 270;
    }

    private void onOrientationChangedWatermarkViewPager() {
        this.mWatermarkOrientationStatus = this.mOrientationStatus;
        if (this.mWMmanager != null) {
            this.mWMmanager.onOrientationChanged(this.mWatermarkOrientationStatus);
        }
        if (this.mWMPager != null) {
            this.mWMPager.onOrientationChanged(this.mWatermarkOrientationStatus);
        }
        if (this.mWmPagerAdapter != null) {
            this.mWmPagerAdapter.notifyDataSetChanged();
        }
    }

    private void onOrientationChangedWMLocalView() {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.onOrientationChanged(this.mOrientationStatus);
        }
    }

    public void onOrientationChanged(int orientation, int type) {
        if (this.mWMmanager != null) {
            if (this.mWMmanager.getIgnoreUserOperateEventStatus() || getVisibility() != 0) {
                this.mEventCacheWhenEventCannotTakeEffect.put("ORIENTATION_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY", "" + orientation);
                this.mEventCacheWhenEventCannotTakeEffect.put("ORIENTATIONTYPE_LAST_VALUE_WHEN_EVENT_CANNOT_TAKE_EFFECT_KEY", "" + type);
                return;
            }
            int orientationstatustemp = orientationValue(orientation);
            boolean orientationChanged = false;
            if (this.mOrientationStatus != orientationstatustemp) {
                orientationChanged = true;
            }
            this.mOrientationStatus = orientationstatustemp;
            this.mOrientationType = type;
            showArrows(this.mOrientationStatus, this.mOrientationType);
            if (orientationChanged) {
                if (WMBaseUtil.containType(type, 1)) {
                    onOrientationChangedWatermarkViewPager();
                }
                if (WMBaseUtil.containType(type, 16)) {
                    onOrientationChangedWMLocalView();
                }
            }
        }
    }

    private void showArrows(int orientationStatus, int type) {
        Log.d(TAG, String.format("orientation: %d, type: %d", new Object[]{Integer.valueOf(orientationStatus), Integer.valueOf(type)}));
        if (type == 16) {
            onlyShowLeftRightArrows(0);
            return;
        }
        switch (orientationStatus) {
            case 0:
                onlyShowLeftRightArrows(0);
                break;
            case ORI_90 /*90*/:
                onlyShowTopBottomArrows(90);
                break;
            case 180:
                onlyShowLeftRightArrows(180);
                break;
            case 270:
                onlyShowTopBottomArrows(270);
                break;
        }
    }

    private void onlyShowLeftRightArrows(int orientation) {
        if (this.arrowLeft != null && this.arrowRight != null && this.arrowTop != null && this.arrowBottom != null) {
            this.arrowLeft.setVisibility(0);
            this.arrowRight.setVisibility(0);
            this.arrowTop.setVisibility(4);
            this.arrowBottom.setVisibility(4);
            if (orientation == 0) {
                this.arrowLeft.setContentDescription(getContext().getString(R.string.accessubility_watermark_previous));
                this.arrowRight.setContentDescription(getContext().getString(R.string.accessubility_watermark_next));
            }
            if (orientation == 180) {
                this.arrowLeft.setContentDescription(getContext().getString(R.string.accessubility_watermark_next));
                this.arrowRight.setContentDescription(getContext().getString(R.string.accessubility_watermark_previous));
            }
        }
    }

    private void onlyShowTopBottomArrows(int orientation) {
        if (this.arrowLeft != null && this.arrowRight != null && this.arrowTop != null && this.arrowBottom != null) {
            this.arrowLeft.setVisibility(4);
            this.arrowRight.setVisibility(4);
            this.arrowTop.setVisibility(0);
            this.arrowBottom.setVisibility(0);
            if (orientation == 90) {
                this.arrowBottom.setContentDescription(getContext().getString(R.string.accessubility_watermark_previous));
                this.arrowTop.setContentDescription(getContext().getString(R.string.accessubility_watermark_next));
            }
            if (orientation == 270) {
                this.arrowBottom.setContentDescription(getContext().getString(R.string.accessubility_watermark_next));
                this.arrowTop.setContentDescription(getContext().getString(R.string.accessubility_watermark_previous));
            }
        }
    }

    private void refreshWMLocalLibData(int type) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.refreshData(type);
        }
    }

    private boolean canShowLocalLibMenu(int i, float v, int i2) {
        if (getVisibility() != 0 || this.mShowLocalLibMenu) {
            return false;
        }
        if ((i == 0 || i == WMFileProcessor.getInstance().getNowTypeWMCount(getContext(), getToken()) - 1) && v == 0.0f && i2 == 0) {
            return true;
        }
        return false;
    }

    public void showLocalLibMenu() {
        if (!this.mIsPause && !this.mShowLocalLibMenu && this.mWMLocalLib != null) {
            this.mShowLocalLibMenu = true;
            WMCategoryListView categoryListView = this.mWMLocalLib.getWMCategoryListView();
            if (categoryListView != null) {
                categoryListView.setLayoutParams();
            }
            if (this.mWatermarkLocalLibPageShowStatusCallBack != null) {
                this.mWatermarkLocalLibPageShowStatusCallBack.setLocalLibPageShowStatus(true);
            }
            if (this.mWMPager != null) {
                this.mWMPager.setVisibility(4);
            }
            if (this.mWMLocalLibShowButton != null) {
                this.mWMLocalLibShowButton.setVisibility(4);
            }
            this.mWMLocalLib.showView();
        }
    }

    public void hideLocalLibMenu(boolean needrefresh) {
        if (this.mWatermarkLocalLibPageShowStatusCallBack != null) {
            this.mWatermarkLocalLibPageShowStatusCallBack.setLocalLibPageShowStatus(false);
        }
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.hideView();
        }
        this.mWMPager.setVisibility(0);
        if (this.mWMLocalLibShowButton != null) {
            this.mWMLocalLibShowButton.setVisibility(0);
        }
        if (needrefresh) {
            clearWatermarkViewpagerViewAndData();
            refreshWatermarkViewpagerAfterWMChanged();
        }
        this.mShowLocalLibMenu = false;
    }

    private void swipeAtHeadFoot() {
        long timenow = System.currentTimeMillis();
        if (timenow - 200 > this.lastShowHeadTipsTime) {
            this.lastShowHeadTipsTime = timenow;
        }
        showLocalLibMenu();
    }

    public void onShow() {
        if (getVisibility() != 0) {
            setVisibility(0);
            resetEventWhenEventCanTakeEffect();
            if (this.mWMmanager != null) {
                this.mWMmanager.showDialog();
            }
        }
    }

    public void onHide() {
        if (getVisibility() != 4) {
            if (this.mTouchEventDelegateCallBack != null) {
                this.mTouchEventDelegateCallBack.clearSuperDelegate();
            }
            setVisibility(4);
        }
    }

    public void resume() {
        this.mPreparedDisplayWatermarkView = false;
        this.mEventCacheWhenEventCannotTakeEffect.clear();
        if (this.mWMmanager != null) {
            this.mWMmanager.onOrientationChanged(this.mWatermarkOrientationStatus);
            this.mWMmanager.resume();
        }
        initWatermarkDataAndLibData();
        setSupportLanguageStatus();
        post(new Runnable() {
            public void run() {
                WMComponent.this.initView();
            }
        });
        this.mIsPause = false;
    }

    public void pause() {
        this.mPreparedDisplayWatermarkView = false;
        this.mEventCacheWhenEventCannotTakeEffect.clear();
        if (this.mWMmanager != null) {
            this.mWMmanager.setFullScreenViewShowStatus(false);
        }
        post(new Runnable() {
            public void run() {
                WMComponent.this.mIsPause = true;
                if (WMComponent.this.mShowLocalLibMenu) {
                    WMComponent.this.hideLocalLibMenu(false);
                }
                WMComponent.this.releaseContentView();
                WMComponent.this.resetAbsoluteLayout();
                WMComponent.this.mWMmanager.pause();
                WaterMarkCache.getInstance().release();
                WMBitmapFactory.getInstance().release();
            }
        });
    }

    public boolean onBackPressed() {
        if (this.mWMLocalLib == null || !this.mWMLocalLib.isShow()) {
            return false;
        }
        hideLocalLibMenu(false);
        return true;
    }

    public void exitBySelf() {
        if (this.mExitWatermarkBySelfCallBack != null) {
            this.mExitWatermarkBySelfCallBack.exit();
        }
    }

    public void setUseTouchEventDelegateStatus(boolean use) {
        this.mUseTouchEventDelegate = use;
        if (this.mWMPager != null) {
            this.mWMPager.setUseTouchEventDelegateStatus(use);
        }
    }

    private void setSupportLanguageStatus() {
        changeSupportLanguageType(WMBaseUtil.getNowSupportLanguageType(getContext()));
    }

    private void changeSupportLanguageType(int typenow) {
        this.mNowSupportLanguageType = WMWatermarkListData.getInstance(getContext()).getIntValue(SP_SUPPORT_LANGUAGE_TYPE, -1);
        if (typenow != this.mNowSupportLanguageType) {
            clearUserEffectOnWaterMark(getContext());
            WMFileProcessor.getInstance().changeNowSupportLanguage(getContext(), getToken(), typenow);
            refreshWMLocalLibData(2);
            this.mNowSupportLanguageType = typenow;
            WMWatermarkListData.getInstance(getContext()).setIntValue(SP_SUPPORT_LANGUAGE_TYPE, typenow);
        }
    }

    private void clearUserEffectOnWaterMark(Context context) {
        WMLogicData.getInstance(context).clearData();
        WMShowRectData.getInstance(context).clearData();
    }

    public void clearWatermarkViewpagerViewAndData() {
        if (this.mWMPager != null) {
            this.mWMPager.removeAllViews();
        }
        if (this.mWMmanager != null) {
            this.mWMmanager.recyleCurrentWaterMarks();
        }
    }

    public void refreshWatermarkViewpagerAfterWMChanged() {
        if (this.mWmPagerAdapter != null) {
            this.mWmPagerAdapter.setCountIsZero(true);
            this.mWmPagerAdapter.notifyDataSetChanged();
            this.mWmPagerAdapter.setCountIsZero(false);
            setCurrentItemAndCallOnPageSelected(WMFileProcessor.getInstance().getNowWatermarkInCategoryIndex(getContext(), getToken()));
        }
    }

    private void setCurrentItemAndCallOnPageSelected(int newindex) {
        if (this.mWMPager != null) {
            int nowSelectedIndex = this.mWMPager.getCurrentItem();
            this.mWMPager.setCurrentItem(newindex);
            if (nowSelectedIndex == newindex) {
                OnPageChangeListener tempListener = this.mWMPager.getOnPageChangeListener();
                if (!(tempListener == null || this.mWmPagerAdapter == null)) {
                    tempListener.onPageSelected(this.mWmPagerAdapter.getPosition(newindex));
                }
            }
        }
    }

    public void setWatermarkDelegate(WatermarkDelegate temp) {
        this.mWatermarkDelegate = temp;
    }

    public WatermarkDelegate getWatermarkDelegate() {
        return this.mWatermarkDelegate;
    }

    public void setWMAvailable(boolean available) {
        if (this.mCurrentWMAvailableCallBack != null) {
            if (available) {
                this.mCurrentWMAvailableCallBack.currentWMAvailable();
            } else {
                this.mCurrentWMAvailableCallBack.currentWMUnAvailable();
            }
        }
    }

    public void setCurrentWMAvailableCallBack(CurrentWMAvailableCallBack temp) {
        this.mCurrentWMAvailableCallBack = temp;
    }

    public void setWatermarkLocalLibPageShowStatusCallBack(WatermarkLocalLibPageShowStatusCallBack temp) {
        this.mWatermarkLocalLibPageShowStatusCallBack = temp;
    }

    public void setWatermarkFullScreenViewShowStatusCallBack(WatermarkFullScreenViewShowStatusCallBack temp) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setWatermarkFullScreenViewShowStatusCallBack(temp);
        }
    }

    public void setExitWatermarkBySelfCallBack(ExitWatermarkBySelfCallBack temp) {
        this.mExitWatermarkBySelfCallBack = temp;
    }

    public void setLocationSettingDelegate(LocationSettingDelegate temp) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setLocationSettingDelegate(temp);
        }
    }

    public void setTouchEventDelegateCallBack(TouchEventDelegateCallBack temp) {
        if (temp == null && this.mTouchEventDelegateCallBack != null) {
            this.mTouchEventDelegateCallBack.clearSuperDelegate();
        }
        this.mTouchEventDelegateCallBack = temp;
    }

    public void setToken(String token) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setToken(token);
        }
    }

    public String getToken() {
        if (this.mWMmanager == null) {
            return "";
        }
        return this.mWMmanager.getToken();
    }

    public void setWMShowWMLocalLibButton(View temp) {
        this.mWMLocalLibShowButton = temp;
        this.mWMLocalLibShowButton.setContentDescription(getContext().getResources().getString(R.string.accessubility_watermark__entrance));
        this.mWMLocalLibShowButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WMComponent.this.showLocalLibMenu();
            }
        });
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(-2, -2);
        rl.addRule(12);
        if (WMBaseUtil.isHighResolutionTablet(getContext())) {
            rl.bottomMargin = WMBaseUtil.reparamsMarginOfDPI(getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_button_marginbottom_big), getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_button_marginbottom), getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_button_marginbottom_small), getContext());
        } else {
            rl.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_button_marginbottom);
        }
        if (WMUIUtil.isLayoutDirectionRTL(getContext())) {
            rl.addRule(9);
            rl.leftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_watermark_button_marginend);
        } else {
            rl.addRule(11);
            rl.rightMargin = getContext().getResources().getDimensionPixelSize(R.dimen.watermark_more_watermark_button_marginend);
        }
        this.mWMLocalLibShowButton.setLayoutParams(rl);
        addView(this.mWMLocalLibShowButton);
    }

    public View getWMShowWMLocalLibButton() {
        return this.mWMLocalLibShowButton;
    }

    public void setWMLocalLibMenuBaseView(ViewGroup temp) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.setWMLocalLibMenuBaseView(temp);
        }
    }

    public void setWMLocalLibBaseView(WMLocalLibBaseView temp) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.setWMLocalLibBaseView(temp);
        }
    }

    public WMLocalLibBaseView getWMLocalLibBaseView() {
        if (this.mWMLocalLib != null) {
            return this.mWMLocalLib.getWMLocalLibBaseView();
        }
        return null;
    }

    public void setWMCategoryListView(WMCategoryListView temp) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.setWMCategoryListView(temp);
        }
    }

    public WMCategoryListView getWMCategoryListView() {
        if (this.mWMLocalLib != null) {
            return this.mWMLocalLib.getWMCategoryListView();
        }
        return null;
    }

    public void setWMDotListView(WMDotListView temp) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.setWMDotListView(temp);
        }
    }

    public WMDotListView getWMDotListView() {
        if (this.mWMLocalLib != null) {
            return this.mWMLocalLib.getWMDotListView();
        }
        return null;
    }

    public void setWMLocalLibPager(WMLocalLibPager temp) {
        if (this.mWMLocalLib != null) {
            this.mWMLocalLib.setWMLocalLibPager(temp);
        }
    }

    public WMLocalLibPager getWMLocalLibPager() {
        if (this.mWMLocalLib != null) {
            return this.mWMLocalLib.getWMLocalLibPager();
        }
        return null;
    }

    public void setShouldHideSoftKeyboard(boolean shouldHide) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setShouldHideSoftKeyboard(shouldHide);
        }
    }

    public int getOrientation() {
        return this.mOrientationStatus;
    }

    public void setDialogCallback(DialogCallback dialogCallback) {
        if (this.mWMmanager != null) {
            this.mWMmanager.setDialogCallback(dialogCallback);
        }
    }
}
