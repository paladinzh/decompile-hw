package com.huawei.keyguard.cover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import com.android.huawei.coverscreen.HwCustCoverSwitchPanel;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.cover.widget.CoverMusicView;
import com.huawei.keyguard.cover.widget.CoverMusicView.CoverMuiscCallback;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;

public class CoverSwitchPanel extends RelativeLayout implements OnGestureListener, CoverMuiscCallback {
    private static final int[] RES = new int[]{R$id.navigation_bar_clock, R$id.navigation_bar_music};
    private boolean enableSwitchScreen;
    private int mAllWidth;
    private CoverMusicView mCoverMusicView;
    private int mCurrentScreen;
    private GestureDetector mGestureDetector;
    private HwCustCoverSwitchPanel mHwCoverSwitchPanelCust;
    private boolean mIsClicked;
    private boolean mIsPlayingMusic;
    private int mMaxScrollWidth;
    private int mPreX;
    private BroadcastReceiver mReceiver;
    private final Handler mScreenOnHandler;
    private Scroller mScroller;
    private int mSingleWidth;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private boolean needReportToBd;

    public CoverSwitchPanel(Context context) {
        this(context, null);
    }

    public CoverSwitchPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsClicked = false;
        this.needReportToBd = true;
        this.mScreenOnHandler = new Handler();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, final Intent intent) {
                if (intent == null) {
                    HwLog.w("CoverSwitchPanel", "onReceive, the intent is null!");
                } else {
                    CoverSwitchPanel.this.mScreenOnHandler.post(new Runnable() {
                        public void run() {
                            String action = intent.getAction();
                            HwLog.i("CoverSwitchPanel", "cover receive intent action = " + action);
                            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                                CoverSwitchPanel.this.resetView();
                            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                                HwLockScreenReporter.report(CoverSwitchPanel.this.mContext, 146, BuildConfig.FLAVOR);
                            }
                        }
                    });
                }
            }
        };
        init();
        initHwCust();
    }

    private void initHwCust() {
        this.mHwCoverSwitchPanelCust = (HwCustCoverSwitchPanel) HwCustUtils.createObj(HwCustCoverSwitchPanel.class, new Object[]{getContext()});
    }

    public void init() {
        this.mContext = getContext();
        this.mScroller = new Scroller(this.mContext);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View v = findViewById(R$id.cover_music_view);
        if (v instanceof CoverMusicView) {
            this.mCoverMusicView = (CoverMusicView) v;
        }
        this.enableSwitchScreen = getResources().getBoolean(R$bool.coverscreen_view_switch);
        this.mGestureDetector = new GestureDetector(getContext(), this);
        this.mSingleWidth = CoverViewManager.getCoverWindowSize(this.mContext).width();
        HwLog.d("CoverSwitchPanel", "CoverSwitchPanel onFinishInflate mSingleWidth =" + this.mSingleWidth);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        getContext().registerReceiver(this.mReceiver, filter);
        this.mIsPlayingMusic = MusicInfo.getInst().needShowMusicView();
        if (this.mCoverMusicView != null) {
            this.mCoverMusicView.setCoverMusicCallback(this);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mReceiver != null) {
            getContext().unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        this.mScroller = null;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void resetView() {
        this.mIsPlayingMusic = MusicInfo.getInst().needShowMusicView();
        if (this.mCoverMusicView == null || !this.mIsPlayingMusic) {
            onShowClockView();
        } else {
            onShowMuiscView();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        HwLog.d("CoverSwitchPanel", "onInterceptTouchEvent:" + ev.getAction());
        int x = (int) ev.getX();
        switch (ev.getAction()) {
            case 0:
                this.mPreX = x;
                this.mIsClicked = true;
                if (!(this.mScroller == null || this.mScroller.isFinished())) {
                    this.mScroller.abortAnimation();
                    scrollTo(this.mScroller.getCurrX(), 0);
                    break;
                }
            case 2:
                return Math.abs(x - this.mPreX) >= this.mTouchSlop;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        HwLog.d("CoverSwitchPanel", "onTouchEvent:" + event.getAction());
        if (!this.enableSwitchScreen || !this.mIsPlayingMusic) {
            return true;
        }
        if (this.mGestureDetector == null) {
            HwLog.w("CoverSwitchPanel", "onTouchEvent mGestureDetector is null, no guesture is supported");
        } else if (this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        switch (event.getAction()) {
            case 1:
            case 3:
            case 6:
                this.mIsClicked = false;
                this.mVelocityTracker.computeCurrentVelocity(2);
                this.needReportToBd = false;
                snapToDestination();
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
            case 2:
                if (this.mIsClicked) {
                    int deltaX = this.mPreX - x;
                    this.mPreX = x;
                    scrollTo(getScrollX() + deltaX, 0);
                    break;
                }
                break;
        }
        return true;
    }

    public void scrollTo(int x, int y) {
        if (this.mAllWidth == 0) {
            super.scrollTo(x, y);
        } else if (x <= 0) {
            super.scrollTo(0, y);
        } else if (x > this.mMaxScrollWidth) {
            super.scrollTo(this.mMaxScrollWidth, y);
        } else {
            super.scrollTo(x, y);
        }
    }

    public void computeScroll() {
        if (this.mScroller != null && this.mScroller.computeScrollOffset()) {
            scrollTo(this.mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        int count = getChildCount();
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }
            this.mSingleWidth = getWidth();
            this.mAllWidth = this.mSingleWidth * count;
            this.mMaxScrollWidth = (count - 1) * this.mSingleWidth;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        if (getChildCount() != 0) {
            drawScrollingChild(canvas);
        }
    }

    private void snapToDestination() {
        float whichScreen;
        HwLog.i("CoverSwitchPanel", "snapToDestination getScrollX()=" + getScrollX() + "mSingleWidth=" + this.mSingleWidth + "whichScreen=" + (((float) (getScrollX() + (this.mSingleWidth >> 1))) / ((float) this.mSingleWidth)));
        if (this.mCurrentScreen == 0) {
            if (((float) getScrollX()) > ((float) this.mSingleWidth) * 0.45f) {
                whichScreen = 1.0f;
            } else {
                whichScreen = 0.0f;
            }
        } else if (((float) (this.mSingleWidth - getScrollX())) > ((float) this.mSingleWidth) * 0.45f) {
            whichScreen = 0.0f;
        } else {
            whichScreen = 1.0f;
        }
        int index = (int) whichScreen;
        snapToScreen(index);
        setNavigationBar(index, this.mIsPlayingMusic);
    }

    private void setNavigationBar(int index, boolean show) {
        ViewGroup parent = (ViewGroup) getParent();
        for (int i = 0; i < RES.length; i++) {
            View view = parent.findViewById(RES[i]);
            if (view instanceof ImageView) {
                int i2;
                int navigationBarForBrightSrcId = R$drawable.navigation_bar_bright;
                int navigationBarForGraySrcId = R$drawable.navigation_bar_gray;
                if (this.mHwCoverSwitchPanelCust != null) {
                    navigationBarForBrightSrcId = this.mHwCoverSwitchPanelCust.getNavigationBarResForBright(navigationBarForBrightSrcId);
                    navigationBarForGraySrcId = this.mHwCoverSwitchPanelCust.getNavigationBarResForGray(navigationBarForGraySrcId);
                }
                ImageView imageView = (ImageView) view;
                if (index != i) {
                    navigationBarForBrightSrcId = navigationBarForGraySrcId;
                }
                imageView.setImageResource(navigationBarForBrightSrcId);
                ImageView imageView2 = (ImageView) view;
                if (show) {
                    i2 = 0;
                } else {
                    i2 = 4;
                }
                imageView2.setVisibility(i2);
            }
        }
        refreshCoverHomeBackground(index);
        if (index == 0 && this.needReportToBd) {
            HwLockScreenReporter.report(this.mContext, 145, "{Slide To Destination: clock}");
        }
        this.needReportToBd = true;
    }

    private void refreshCoverHomeBackground(int index) {
        View root = getRootView();
        if (root != null) {
            ViewGroup coverHomeView = (ViewGroup) root.findViewById(R$id.cover_home);
            if (coverHomeView != null) {
                View coverMusicMaskView = coverHomeView.findViewById(R$id.cover_music_mask);
                if (coverMusicMaskView != null) {
                    if (index == 1) {
                        coverMusicMaskView.setVisibility(0);
                    } else if (this.mHwCoverSwitchPanelCust == null) {
                        coverMusicMaskView.setVisibility(8);
                    } else if (this.mHwCoverSwitchPanelCust.isCoverClockViewNeedMask()) {
                        coverMusicMaskView.setVisibility(0);
                    } else {
                        coverMusicMaskView.setVisibility(8);
                    }
                }
                if (index == 0) {
                    Drawable coverDrawable = CoverCfg.getCoverWallpaper();
                    if (coverDrawable != null) {
                        coverHomeView.setBackground(coverDrawable);
                    } else {
                        coverHomeView.setBackgroundResource(33751074);
                    }
                } else if (index == 1) {
                    View coverMusicView = root.findViewById(R$id.cover_music_view);
                    if (coverMusicView != null && (coverMusicView instanceof CoverMusicView)) {
                        ((CoverMusicView) coverMusicView).refreshCoverHomeBackgroud();
                    }
                }
            }
        }
    }

    private void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 200);
    }

    private void snapToScreen(int whichScreen, int duration) {
        HwLog.d("CoverSwitchPanel", "MusicCoverView snapTo: " + whichScreen);
        int delta = (whichScreen * this.mSingleWidth) - getScrollX();
        if (this.mScroller != null) {
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
            }
            this.mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
        }
        if (whichScreen < 0) {
            whichScreen += getChildCount();
        }
        this.mCurrentScreen = whichScreen;
        invalidate();
        CoverViewManager.getInstance(this.mContext).adjustStatusBarClockView();
    }

    private void drawScrollingChild(Canvas canvas) {
        int child = getScrollX() / this.mSingleWidth;
        int count = getChildCount();
        long drawingTime = getDrawingTime();
        if (child < 0) {
            drawChild(canvas, getChildAt(0), drawingTime);
            return;
        }
        if (child < count) {
            canvas.save();
            canvas.clipRect(0.0f, 0.0f, (float) getScrollX(), (float) getHeight(), Op.DIFFERENCE);
            drawChild(canvas, getChildAt(child), drawingTime);
            canvas.restore();
        }
        if (child + 1 < count) {
            canvas.save();
            canvas.clipRect((float) getScrollX(), 0.0f, (float) this.mSingleWidth, (float) getHeight(), Op.DIFFERENCE);
            drawChild(canvas, getChildAt(child + 1), drawingTime);
            canvas.restore();
        }
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.mIsPlayingMusic) {
            return true;
        }
        float distance = 0.0f;
        if (!(e1 == null || e2 == null)) {
            distance = Math.abs(e1.getX() - e2.getX());
        }
        if (Math.abs(velocityX) > 8800.0f * (0.45f - (distance / ((float) this.mSingleWidth))) && distance > ((float) this.mSingleWidth) * 0.225f) {
            int index = this.mCurrentScreen;
            if (velocityX > 0.0f) {
                index--;
            } else {
                index++;
            }
            if (index >= 0 && index < 2) {
                snapToScreen(index);
                setNavigationBar(index, this.mIsPlayingMusic);
                return true;
            }
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void onShowMuiscView() {
        snapToScreen(1, 0);
        this.mIsPlayingMusic = true;
        setNavigationBar(1, this.mIsPlayingMusic);
    }

    public void onShowClockView() {
        snapToScreen(0, 0);
        this.mIsPlayingMusic = false;
        setNavigationBar(0, this.mIsPlayingMusic);
    }

    public boolean isShowClockView() {
        return this.mCurrentScreen == 1;
    }
}
