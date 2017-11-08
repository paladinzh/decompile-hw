package com.android.keyguard.hwlockscreen.magazine;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.android.keyguard.R$string;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea.IHwkeyguardBottomView;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea.IOnProgressChangeListener;
import com.huawei.hwtransition.control.LiftTransition;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.data.KeyguardInfo;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.BigPicture;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.support.magazine.MagazineWallpaper.IMagazineLoadCallback;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.LocalLinkTextView;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.HwTintImageView;
import com.huawei.keyguard.view.KgViewUtils;
import com.huawei.keyguard.view.WallpaperPagerAdapter;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import com.huawei.keyguard.view.widget.MagazineSwitchViewPager;
import com.huawei.keyguard.widget.StepCounterView;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;

public class MagazineDetailView extends LinearLayout implements IHwkeyguardBottomView, Callback {
    private boolean isThemeWallpaper = true;
    private WallpaperPagerAdapter mAdapter;
    private TextView mChargeAndOwnerinfo;
    private int mDateHeight = 0;
    private View mDateView = null;
    private View mDistanceView;
    private View mDividerView;
    private View mDualDateView = null;
    private HwTintImageView mDynamicFlagIcon;
    private HwTintImageView mDynamicNoTitleIcon;
    private MagazineSimpleTitleView mDynamicNoTitleView;
    private TextView mFyuseFlagView;
    GestureDetector mGestureDetector = new GestureDetector(this.mContext, new SimpleGestureListener());
    private ArrayList<View> mInVisibleViews = new ArrayList();
    private float mInitTranY = 0.0f;
    private boolean mIsDisPatchConfirmed = false;
    private boolean mIsPagerIdle = true;
    private boolean mIsPagerIdleWhenKeyDown = true;
    private float mLastProgress;
    private float mLiftRange;
    private LiftTransition mLiftTransition;
    private MagazineControlView mMagazineControlView = null;
    private ImageView mMagazineTitleIcon;
    private View mNoTitleFyuseView;
    private IOnProgressChangeListener mOnProgressChangeListener = null;
    private String mOwnerInfo;
    private MagazineSwitchViewPager mPager;
    OnPageChangeListener mPagerChangeListener = new OnPageChangeListener() {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            MagazineDetailView.this.mAdapter.switchPagerPic(position);
        }

        public void onPageScrollStateChanged(int i) {
            if (i != 0) {
                MagazineDetailView.this.mIsPagerIdle = false;
                MagazineUtils.setScrollingBlockFlag(true);
                return;
            }
            MagazineDetailView.this.mIsPagerIdle = true;
            MagazineUtils.setScrollingBlockFlag(false);
        }
    };
    private int mPagerGetState = 0;
    private Runnable mRangeChecker = new Runnable() {
        public void run() {
            MagazineDetailView.this.checkLiftRange();
        }
    };
    private String mSeparator;
    private StepCounterView mStepCounterView = null;
    private TextView mTextView;
    private boolean mTipsNeedShow = true;
    private View mTipsView;
    private MagazineSimpleTitleView mTitleView;
    private int mTouchMode = 0;
    private float x1 = 0.0f;
    private float x2 = 0.0f;
    private float y1 = 0.0f;
    private float y2 = 0.0f;

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (MagazineDetailView.this.mLiftTransition != null) {
                HwLog.d("MagazineDetailView", "onSingleTapUp");
                MagazineDetailView.this.mLiftTransition.reset();
            }
            MagazineDetailView.this.setLift(0.0f);
            MagazineDetailView.this.mIsPagerIdle = true;
            MagazineDetailView.this.mAdapter.setLiftState(false);
            return true;
        }
    }

    public MagazineDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLiftTransition = new LiftTransition(context.getApplicationContext(), this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (MagazineSimpleTitleView) findViewById(R$id.magazine_simple_title);
        this.mDividerView = findViewById(R$id.divider_simple);
        this.mDateView = findViewById(R$id.date_view);
        initFyuseFlagView();
        this.mMagazineTitleIcon = (ImageView) findViewById(R$id.magazine_simple_title_media_icon);
        this.mMagazineControlView = (MagazineControlView) findViewById(R$id.magazine_control_view);
        ((LocalLinkTextView) findViewById(R$id.magazine_info_detail)).setResizeListener(this.mRangeChecker);
        this.mStepCounterView = (StepCounterView) findViewById(R$id.step_counter_view);
        this.mChargeAndOwnerinfo = (TextView) findViewById(R$id.charging_ownerinfo);
        this.mSeparator = getResources().getString(R$string.kg_text_message_separator);
        this.mMagazineControlView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (MagazineDetailView.this.mInitTranY != ((float) MagazineDetailView.this.mMagazineControlView.getHeight())) {
                    HwLog.w("MagazineDetailView", "LayoutChanged and height range will be rechecked");
                    MagazineDetailView.this.post(MagazineDetailView.this.mRangeChecker);
                }
            }
        });
        setVisibility(4);
        this.mTipsNeedShow = MagazineUtils.getFirstLiftDetailViewFlag(getContext());
        this.mAdapter = WallpaperPagerAdapter.getInst(getContext(), null);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppHandler.addListener(this);
        updateMagazineInfo(false);
        updateBatteryAndOwnerInfo();
        if (HwUnlockUtils.isTablet()) {
            resetCurrentPicture();
        }
        View v = getRootView().findViewWithTag("backdrop_pager");
        if (v instanceof MagazineSwitchViewPager) {
            this.mPager = (MagazineSwitchViewPager) v;
        }
        if (this.mPager != null) {
            this.mPager.addOnPageChangeListener(this.mPagerChangeListener);
        }
    }

    public void resetCurrentPicture() {
        BigPicture bigPicture = MagazineWallpaper.getInst(this.mContext).getWallPaper(0);
        if (bigPicture != null) {
            if (HwUnlockUtils.isLandscape(this.mContext)) {
                bigPicture.setBokehDrawable(BokehDrawable.create(this.mContext, bigPicture.getLandBitmap(), true, false));
            } else {
                bigPicture.setBokehDrawable(BokehDrawable.create(this.mContext, bigPicture.getBitmap(), true, false));
            }
            MagazineWallpaper.getInst(this.mContext).setCurrentPicture(bigPicture);
            IMagazineLoadCallback mCallback = KeyguardWallpaper.getInst(this.mContext).getCallback();
            if (mCallback != null) {
                mCallback.onMagazinePicLoaded(bigPicture);
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPager.removeOnPageChangeListener(this.mPagerChangeListener);
        setLift(0.0f);
        this.mInVisibleViews.clear();
        AppHandler.removeListener(this);
        if (this.mTipsView != null && this.mTipsView.getVisibility() == 0) {
            this.mTipsView.setVisibility(8);
            this.mTipsNeedShow = false;
            MagazineUtils.setFirstLiftDetailViewFlag(this.mContext, false);
        }
    }

    private void refreshDateHeight() {
        int i = 0;
        if (HwUnlockUtils.isDualClockEnabled(getContext())) {
            this.mDualDateView = findViewById(R$id.date);
            if (this.mDualDateView != null) {
                i = this.mDualDateView.getHeight();
            }
            this.mDateHeight = i;
            return;
        }
        if (this.mDateView != null) {
            i = this.mDateView.getHeight();
        }
        this.mDateHeight = i;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    private void initViewLayout() {
        if (this.mDividerView == null || this.mTitleView == null || this.mStepCounterView == null) {
            HwLog.e("MagazineDetailView", "initViewLayout, mDetialView or devider or title is null");
            return;
        }
        addInvisibleView(this.mDividerView);
        addInvisibleView(this.mStepCounterView);
        addInvisibleView(findViewById(R$id.event_notify_view));
        addInvisibleView(findViewById(R$id.charging_ownerinfo));
        addInvisibleView(findViewById(R$id.keyguard_status_view_face_palm));
    }

    private void addInvisibleView(View v) {
        if (v == null) {
            HwLog.w("MagazineDetailView", "addInvisibleView null view");
        }
        if (!this.mInVisibleViews.contains(v)) {
            this.mInVisibleViews.add(v);
        }
    }

    private void checkLiftRange() {
        float f = 0.0f;
        this.mInitTranY = (float) this.mMagazineControlView.getHeight();
        if (this.mInitTranY == 0.0f) {
            postDelayed(this.mRangeChecker, 100);
            return;
        }
        setVisibility(0);
        this.mLiftRange = -this.mInitTranY;
        this.mLiftTransition.setLiftRange(this.mLiftRange, this.mLiftRange / 2.0f);
        this.mLiftTransition.setHeight(this.mInitTranY, 1);
        if (getMode() != 1) {
            f = this.mLiftRange;
        }
        setLift(f);
    }

    public void setLift(float deltaX, float deltaY) {
    }

    public void setLiftReset() {
        if (this.mLiftTransition != null) {
            this.mLiftTransition.reset();
        }
        setLift(0.0f);
        resetPagerState();
        if (this.mMagazineControlView != null) {
            this.mMagazineControlView.updateContentDescription();
        }
    }

    public void setLift(float deltaY) {
        boolean isfyuseFile = false;
        setTranslationY(deltaY);
        float progress = Math.abs(deltaY) / 80.0f;
        if (progress > 0.99f) {
            progress = 1.0f;
        } else {
            this.mMagazineControlView.setButtonsClickable(false);
        }
        float alpha = 1.0f - progress;
        int visibility = ((double) alpha) < 0.1d ? 8 : 0;
        for (View v : this.mInVisibleViews) {
            if (v != null) {
                v.setAlpha(alpha);
                v.setVisibility(visibility);
            }
        }
        if (HwFyuseUtils.isCurrent3DWallpaper(getContext())) {
            isfyuseFile = HwFyuseUtils.getMagazineEnableStatus();
        }
        checkToSetDividerVisibility(isfyuseFile, visibility);
        if (this.mTitleView != null) {
            int h = this.mTitleView.getHeight();
            if (deltaY > ((float) (-h))) {
                this.mTitleView.refreshTextView(deltaY, h);
                if (this.mDynamicFlagIcon != null) {
                    this.mDynamicFlagIcon.refreshImageDrawble(deltaY, h);
                }
            } else {
                this.mTitleView.refreshTextView((float) (-h), h);
                if (this.mDynamicFlagIcon != null) {
                    this.mDynamicFlagIcon.refreshImageDrawble((float) (-h), h);
                }
            }
        }
        checkToSetFyuseFlagVisibility(deltaY, isfyuseFile);
        this.mLastProgress = progress;
        if (this.mOnProgressChangeListener != null) {
            this.mOnProgressChangeListener.onProgressChangeListener(this.mLastProgress);
        }
    }

    private void resetPagerState() {
        HwLog.d("MagazineDetailView", "resetPagerState");
        this.mAdapter.setLiftState(false);
        this.mPager.callSetCurrentItemInternal(this.mPager.getCurrentItem(), false, true);
        this.mIsPagerIdle = true;
        MagazineUtils.setScrollingBlockFlag(false);
        dismissTipsView();
    }

    public void onLiftAnimationEnd() {
        if (this.mLiftTransition != null) {
            if (this.mLiftTransition.isLifted()) {
                int magazinePicSize = MagazineWallpaper.getInst(this.mContext).getPictureSize();
                if (this.mTipsNeedShow && magazinePicSize > 1) {
                    this.mTipsView = View.inflate(this.mContext, R$layout.magazine_tips_view, null);
                    this.mTextView = (TextView) this.mTipsView.findViewById(R$id.txt);
                    this.mTextView.setText(R$string.emui50_keyguard_swipe_magazine_tips);
                    ((ViewGroup) getRootView()).addView(this.mTipsView);
                }
                if (this.mMagazineControlView != null) {
                    this.mMagazineControlView.setButtonsClickable(this.mLiftTransition.isLifted());
                }
                this.mAdapter.setLiftState(true);
                if (magazinePicSize < 2) {
                    this.mPager.setScanScroll(false);
                    this.mAdapter.setBackdropView(this.mAdapter.getCurrentPosition());
                } else {
                    this.mPager.setScanScroll(true);
                }
            } else {
                setLift(0.0f);
                this.mAdapter.setLiftState(false);
                this.mIsPagerIdle = true;
            }
        }
        doReporterSlideLift();
        reportSwipeup();
    }

    private void reportSwipeup() {
        if (this.mLiftTransition != null && this.mLiftTransition.isLifted() && 2 == this.mLiftTransition.getLiftMode()) {
            BigPictureInfo info = MagazineWallpaper.getInst(this.mContext).getPictureInfo(0);
            HwLockScreenReporter.reportAdEvent(this.mContext, info, EventType.SWIPEUP);
            if (info != null) {
                HwLockScreenReporter.statReport(this.mContext, 1003, "{picture:" + info.getPicName() + "}");
            }
        }
    }

    private void doReporterSlideLift() {
        if (this.mLiftTransition != null && this.mLiftTransition.isLifted()) {
            HwLockScreenReporter.reportMagazinePictureInfo(this.mContext, 130, 0);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        int touchMode = this.mTouchMode;
        if (KgViewUtils.isCancelAction(event)) {
            this.mTouchMode = 0;
        }
        switch (touchMode) {
            case 1:
                return super.dispatchTouchEvent(event);
            case 2:
                if (disPatchToViewPager(event)) {
                    return true;
                }
                return this.mLiftTransition.onTouchEvent(event, isInLiftView(event));
            case 4:
                super.dispatchTouchEvent(event);
                if (disPatchToViewPager(event)) {
                    return true;
                }
                return this.mLiftTransition.onTouchEvent(event, isInLiftView(event));
            default:
                return false;
        }
    }

    private boolean disPatchToViewPager(MotionEvent event) {
        if (!this.mTipsNeedShow || dismissTipsView()) {
            boolean isTouchOnControlView = event.getY() > ((float) (this.mMagazineControlView.getTop() + this.mMagazineControlView.findViewById(R$id.magazine_info_detail).getHeight())) && event.getY() < ((float) this.mMagazineControlView.getBottom());
            boolean isLift = this.mLiftTransition.isLifted();
            if (this.mGestureDetector.onTouchEvent(event) && !isTouchOnControlView && isLift) {
                return true;
            }
            if (isLift && (!isTouchOnControlView || !this.mIsPagerIdle)) {
                if (event.getAction() == 0) {
                    this.x1 = event.getX();
                    this.y1 = event.getY();
                    this.mIsPagerIdleWhenKeyDown = this.mIsPagerIdle;
                    this.mIsDisPatchConfirmed = false;
                    this.mPagerGetState = 0;
                    if (this.mIsPagerIdleWhenKeyDown) {
                        this.mPager.onTouchEvent(event);
                        this.mLiftTransition.onTouchEvent(event, isInLiftView(event));
                    }
                }
                if ((event.getAction() == 5 || event.getAction() == 6) && this.mPagerGetState == 0) {
                    this.mPager.onTouchEvent(event);
                }
                if (event.getAction() == 2) {
                    if (!this.mIsPagerIdleWhenKeyDown) {
                        return true;
                    }
                    if (!this.mIsDisPatchConfirmed) {
                        this.x2 = event.getX();
                        this.y2 = event.getY();
                        if (((double) (this.y2 - this.y1)) > ((double) Math.abs(this.x2 - this.x1)) * 0.6d) {
                            this.mPagerGetState = 1;
                            this.mIsDisPatchConfirmed = true;
                        } else {
                            this.mPagerGetState = 0;
                            this.mIsDisPatchConfirmed = true;
                            return this.mPager.onTouchEvent(event);
                        }
                    } else if (this.mPagerGetState == 0) {
                        return this.mPager.onTouchEvent(event);
                    }
                }
                if (event.getAction() == 1) {
                    if (this.mPagerGetState != 0) {
                        HwLog.d("MagazineDetailView", "ACTION_UP LIFT GET ");
                    } else if (this.mIsPagerIdleWhenKeyDown) {
                        return this.mPager.onTouchEvent(event);
                    } else {
                        return true;
                    }
                }
            } else if (isTouchOnControlView) {
                this.mIsPagerIdleWhenKeyDown = this.mIsPagerIdle;
                this.mPagerGetState = 1;
                this.mIsDisPatchConfirmed = true;
            }
            return false;
        }
        HwLog.d("MagazineDetailView", "mTipsView onTouch");
        return this.mTipsView.onTouchEvent(event);
    }

    private boolean isInLiftView(MotionEvent event) {
        return event.getRawY() > ((float) ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getHeight()) + this.mLiftRange;
    }

    public void setLiftMode(int liftMode) {
    }

    public void setTranslationY(float translationY) {
        super.setTranslationY(this.mInitTranY + translationY);
    }

    private int getMode() {
        int i = 1;
        if (getVisibility() != 0) {
            return 1;
        }
        if (this.mLiftTransition.isLifted()) {
            i = 2;
        }
        return i;
    }

    public boolean isInterestedEvent(MotionEvent event) {
        boolean z = true;
        if (event.getAction() != 0) {
            return false;
        }
        refreshDateHeight();
        this.mTouchMode = 0;
        if (KgViewUtils.isTouchInEffectView(event, this.mMagazineControlView)) {
            if (KgViewUtils.isTouchInEffectView(event, this.mMagazineControlView.findViewById(R$id.magazine_info_detail))) {
                this.mTouchMode = 4;
            } else {
                this.mTouchMode = 1;
            }
        } else if (this.mLastProgress > 0.01f || (event.getY() + ((float) this.mDateHeight)) + 80.0f > ((float) getHeight())) {
            this.mTouchMode = 2;
        }
        HwLog.v("MagazineDetailView", "Magazine interest " + this.mTouchMode + " - " + this.mLastProgress);
        if (this.mTouchMode <= 0) {
            z = false;
        }
        return z;
    }

    private void updateMagazineInfo(boolean isWallPaperChanged) {
        boolean z = true;
        BigPicture picture = null;
        if (!MagazineUtils.isUserCustomedWallpaper(this.mContext)) {
            picture = MagazineWallpaper.getInst(this.mContext).getWallPaper(0);
        }
        if (!(picture == null || picture.isSameDrawable(KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper(), this.mContext) || HwUnlockUtils.isLandscape(this.mContext))) {
            picture = null;
            HwLog.e("MagazineDetailView", "Skip update drawable as picture is mismatch");
        }
        BigPictureInfo bigPictureInfo = picture == null ? null : picture.getBigPictureInfo();
        updateStepNumInfo();
        updateMagazineInfo(bigPictureInfo, isWallPaperChanged);
        if (this.mMagazineControlView != null) {
            this.mMagazineControlView.updateMagazineInfo(bigPictureInfo);
            this.mMagazineControlView.setCustomedStatusView(MagazineUtils.isUserCustomedWallpaper(this.mContext), isDefaultMagazineWallpaper());
        }
        initViewLayout();
        if (this.mPager != null) {
            MagazineSwitchViewPager magazineSwitchViewPager = this.mPager;
            if (MagazineWallpaper.getInst(this.mContext).getPictureSize() <= 1) {
                z = false;
            }
            magazineSwitchViewPager.setScanScroll(z);
        }
    }

    private boolean isDefaultMagazineWallpaper() {
        boolean z = false;
        BigPicture bigPicture = MagazineWallpaper.getInst(this.mContext).getWallPaper(0);
        if (bigPicture == null) {
            return true;
        }
        if (!this.isThemeWallpaper) {
            return false;
        }
        boolean isSame = bigPicture.isSameDrawable(KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper(), this.mContext);
        if (isSame) {
            this.isThemeWallpaper = false;
        }
        if (!isSame) {
            z = true;
        }
        return z;
    }

    private void updateMagazineInfo(BigPictureInfo picInfo, boolean isWallpaperChanged) {
        int i = 0;
        if (this.mTitleView != null && this.mDividerView != null) {
            boolean showTitle;
            if (picInfo == null || picInfo.getDescriptionInfo() == null) {
                showTitle = false;
            } else {
                showTitle = true;
            }
            showTitle = showTitle && !TextUtils.isEmpty(picInfo.getTitle());
            boolean isFyuseFile = false;
            if (picInfo != null) {
                isFyuseFile = picInfo.isFyuseFormatPic() && HwFyuseUtils.getMagazineEnableStatus() && !MagazineUtils.isUserCustomedWallpaper(this.mContext);
            }
            if (showTitle) {
                int i2;
                this.mTitleView.setText(picInfo.getTitle() + (TextUtils.isEmpty(picInfo.getCpName()) ? BuildConfig.FLAVOR : " / ") + picInfo.getCpName());
                this.mTitleView.setVisibility(0);
                if (HwUnlockUtils.isKeyguardAudioVideoEnable() && this.mMagazineTitleIcon != null) {
                    Drawable drawable = MagazineUtils.getKeyguardResTitleIcon(this.mContext, picInfo.getBigPackagename());
                    if (drawable != null) {
                        this.mMagazineTitleIcon.setVisibility(0);
                        this.mMagazineTitleIcon.setImageDrawable(drawable);
                    } else {
                        this.mMagazineTitleIcon.setVisibility(8);
                    }
                }
                this.mInVisibleViews.add(this.mDividerView);
                View view = this.mDividerView;
                if (this.mLiftTransition.isLifted()) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                view.setVisibility(i2);
                setViewVisiblity(this.mNoTitleFyuseView, 8);
                View view2 = this.mDynamicFlagIcon;
                if (!isFyuseFile) {
                    i = 8;
                }
                setViewVisiblity(view2, i);
                setViewVisiblity(this.mDistanceView, 8);
            } else {
                this.mInVisibleViews.remove(this.mDividerView);
                this.mTitleView.setVisibility(8);
                this.mMagazineTitleIcon.setVisibility(8);
                setViewVisiblity(this.mDynamicFlagIcon, 8);
                refreshShowFyuseFlag(picInfo, isFyuseFile);
            }
            if (!isWallpaperChanged) {
                post(this.mRangeChecker);
            }
        }
    }

    private void updateBatteryAndOwnerInfo() {
        if (this.mChargeAndOwnerinfo == null) {
            HwLog.w("MagazineDetailView", "mChargeAndOwnerinfo is null");
            return;
        }
        StringBuilder msgBuilder = new StringBuilder();
        if (BatteryStateInfo.getInst().showBatteryInfo()) {
            msgBuilder.append(BatteryStateInfo.getInst().getBatteryInfo2(this.mContext));
        }
        this.mOwnerInfo = KeyguardInfo.getInst(this.mContext).getDeviceInfo(OsUtils.getCurrentUser());
        if (!TextUtils.isEmpty(this.mOwnerInfo)) {
            if (msgBuilder.length() > 0) {
                msgBuilder.append(this.mSeparator);
            }
            msgBuilder.append(this.mOwnerInfo);
        }
        if (msgBuilder.length() > 0) {
            this.mChargeAndOwnerinfo.setText(msgBuilder.toString());
            this.mChargeAndOwnerinfo.setVisibility(0);
        } else {
            this.mChargeAndOwnerinfo.setVisibility(8);
            this.mChargeAndOwnerinfo.setText(BuildConfig.FLAVOR);
        }
    }

    private void updateStepNumInfo() {
        if (this.mStepCounterView != null) {
            boolean z;
            boolean showStep = OsUtils.isOwner() && OsUtils.getGlobalInt(this.mContext, "step_count_settings", 1) == 1;
            StepCounterView stepCounterView = this.mStepCounterView;
            if (showStep) {
                z = false;
            } else {
                z = true;
            }
            stepCounterView.setPermanentHide(z);
            if (showStep) {
                addInvisibleView(this.mStepCounterView);
            } else {
                this.mInVisibleViews.remove(this.mStepCounterView);
            }
            StepCounterInfo.getInst().setStepInfoShowEnable(showStep);
            int stepNum = StepCounterInfo.getInst().getStepsCount();
            if (!showStep || stepNum <= 0) {
                showStep = false;
            } else {
                showStep = true;
            }
            if (showStep) {
                this.mStepCounterView.setVisibility(0);
                this.mStepCounterView.setText(Integer.toString(stepNum));
            } else {
                this.mStepCounterView.setVisibility(8);
            }
        }
    }

    private boolean dismissTipsView() {
        if (this.mTipsView != null && this.mTipsView.getVisibility() == 0) {
            this.mTipsView.setVisibility(8);
            ((ViewGroup) getRootView()).removeView(this.mTipsView);
            this.mTipsNeedShow = false;
            HwLog.v("MagazineDetailView", "dismissTipsView removeView");
            MagazineUtils.setFirstLiftDetailViewFlag(this.mContext, false);
        }
        return this.mTipsNeedShow;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.isThemeWallpaper = true;
                break;
            case 10:
                updateBatteryAndOwnerInfo();
                updateStepNumInfo();
                if (this.mTipsNeedShow) {
                    dismissTipsView();
                    break;
                }
                break;
            case 15:
                boolean isMagaineWallPaper = HwFyuseUtils.isMagaineWallPaper(getContext());
                boolean isAutoSwitchMagazine = MagazineUtils.isAutoSwitchMagazine(getContext(), false);
                if (isMagaineWallPaper && !isAutoSwitchMagazine) {
                    updateMagazineInfo(true);
                    break;
                }
            case 21:
                HwLog.v("MagazineDetailView", "Wallpaper changed");
                updateMagazineInfo(true);
                break;
            case 100:
                updateBatteryAndOwnerInfo();
                break;
            case 101:
                updateBatteryAndOwnerInfo();
                break;
        }
        return false;
    }

    public void addProgressChangeListener(IOnProgressChangeListener iListener) {
        this.mOnProgressChangeListener = iListener;
    }

    public void onThemeStlyeChange() {
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mNoTitleFyuseView != null && this.mNoTitleFyuseView.getVisibility() == 0 && this.mFyuseFlagView != null) {
            this.mFyuseFlagView.setText(R$string.fyuse_picture_flag);
        }
    }

    private void initFyuseFlagView() {
        this.mDynamicFlagIcon = (HwTintImageView) findViewById(R$id.magazine_fyuse_flag_icon);
        this.mNoTitleFyuseView = findViewById(R$id.fyuse_no_title_flag);
        this.mDynamicNoTitleIcon = (HwTintImageView) findViewById(R$id.magazine_fyuse_title_icon);
        this.mDistanceView = findViewById(R$id.divide_view);
        Drawable drawable = getResources().getDrawable(R$drawable.ic_unlock_fyuse_flag_icon);
        if (this.mDynamicNoTitleIcon != null) {
            this.mDynamicNoTitleIcon.setImageDrawable(drawable);
        }
        if (this.mDynamicFlagIcon != null) {
            this.mDynamicFlagIcon.setImageDrawable(drawable);
        }
        this.mDynamicNoTitleView = (MagazineSimpleTitleView) findViewById(R$id.magazine_fyuse_title);
        this.mFyuseFlagView = (TextView) findViewById(R$id.magazine_fyuse_title);
    }

    private void refreshShowFyuseFlag(BigPictureInfo picInfo, boolean isFyuseFile) {
        int i;
        int i2 = 0;
        View view = this.mNoTitleFyuseView;
        if (isFyuseFile) {
            i = 0;
        } else {
            i = 8;
        }
        setViewVisiblity(view, i);
        setViewVisiblity(this.mDynamicFlagIcon, 8);
        if (isFyuseFile) {
            if (this.mFyuseFlagView != null) {
                this.mFyuseFlagView.setText(R$string.fyuse_picture_flag);
            }
            view = this.mDividerView;
            if (this.mLiftTransition.isLifted()) {
                i = 8;
            } else {
                i = 0;
            }
            setViewVisiblity(view, i);
            boolean hasNoContent = true;
            if (!(picInfo == null || this.mMagazineControlView == null)) {
                hasNoContent = this.mMagazineControlView.isDetailEmpty(picInfo.getContent());
            }
            View view2 = this.mDistanceView;
            if (!hasNoContent) {
                i2 = 8;
            }
            setViewVisiblity(view2, i2);
            return;
        }
        setViewVisiblity(this.mDividerView, 8);
        setViewVisiblity(this.mDistanceView, 8);
    }

    private void checkToSetDividerVisibility(boolean isfyuseFile, int timeVisibility) {
        boolean isTitleViewGone;
        if (this.mTitleView == null || this.mTitleView.getVisibility() != 8) {
            isTitleViewGone = false;
        } else {
            isTitleViewGone = true;
        }
        if (!isTitleViewGone) {
            return;
        }
        if (isfyuseFile && timeVisibility == 0) {
            setViewVisiblity(this.mDividerView, 0);
        } else {
            setViewVisiblity(this.mDividerView, 8);
        }
    }

    private void checkToSetFyuseFlagVisibility(float deltaY, boolean isfyuseFile) {
        boolean isTitleViewGone = false;
        if (isfyuseFile) {
            if (this.mTitleView != null && this.mTitleView.getVisibility() == 8) {
                isTitleViewGone = true;
            }
            if (!isTitleViewGone) {
                setViewVisiblity(this.mDistanceView, 8);
            } else if (!(this.mDynamicNoTitleView == null || this.mDynamicNoTitleIcon == null)) {
                int viewH = this.mDynamicNoTitleView.getHeight();
                if (deltaY > ((float) (-viewH))) {
                    this.mDynamicNoTitleView.refreshTextView(deltaY, viewH);
                    this.mDynamicNoTitleIcon.refreshImageDrawble(deltaY, viewH);
                } else {
                    this.mDynamicNoTitleView.refreshTextView((float) (-viewH), viewH);
                    this.mDynamicNoTitleIcon.refreshImageDrawble((float) (-viewH), viewH);
                }
            }
        }
    }

    private void setViewVisiblity(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }
}
