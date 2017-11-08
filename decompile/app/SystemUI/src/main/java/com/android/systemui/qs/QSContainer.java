package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.compat.ActivityInfoWrapper;
import com.android.systemui.qs.HwSuperpowerModeManager.ModeChangedCallback;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.HwQuickStatusBarHeader;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class QSContainer extends FrameLayout implements ModeChangedCallback {
    private final AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            QSContainer.this.mHeaderAnimating = false;
            QSContainer.this.updateQsState();
        }
    };
    private long mDelay;
    protected BaseStatusBarHeader mHeader;
    private boolean mHeaderAnimating;
    private int mHeightOverride = -1;
    private boolean mKeyguardShowing;
    private Configuration mLastConfig = new Configuration();
    private boolean mListening;
    private NotificationPanelView mPanelView;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    private View mQSLine;
    protected QSPanel mQSPanel;
    private boolean mQsExpanded;
    protected float mQsExpansion;
    private final Point mSizePoint = new Point();
    private boolean mStackScrollerOverscrolling;
    private final OnPreDrawListener mStartHeaderSlidingIn = new OnPreDrawListener() {
        public boolean onPreDraw() {
            QSContainer.this.getViewTreeObserver().removeOnPreDrawListener(this);
            QSContainer.this.animate().translationY(0.0f).setStartDelay(QSContainer.this.mDelay).setDuration(448).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSContainer.this.mAnimateHeaderSlidingInListener).start();
            QSContainer.this.setY((float) (-QSContainer.this.mHeader.getHeight()));
            return true;
        }
    };

    public QSContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        int i;
        super.onFinishInflate();
        this.mQSPanel = (QSPanel) findViewById(R.id.quick_settings_panel);
        this.mQSDetail = (QSDetail) findViewById(R.id.qs_detail);
        this.mHeader = (BaseStatusBarHeader) findViewById(R.id.header);
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader);
        this.mQSDetail.setQsContainer(this);
        this.mQSCustomizer = (QSCustomizer) findViewById(R.id.qs_customize);
        this.mQSCustomizer.setQsContainer(this);
        this.mQSLine = findViewById(R.id.qs_line);
        this.mQSDetail.getClipper().addCallback(this.mQSPanel);
        this.mQSDetail.getClipper().addCallback(this.mHeader);
        Resources resources = getResources();
        if (PerfAdjust.supportBlurBackgound()) {
            i = R.color.qs_panel_background;
        } else {
            i = R.color.qs_panel_background_lite;
        }
        setBackgroundColor(resources.getColor(i));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
        if (ActivityInfoWrapper.isThemeChanged(this.mLastConfig.updateFrom(newConfig))) {
            HwLog.i("QSContainer", "onConfigurationChanged:theme changed");
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateResources();
    }

    private void updateResources() {
        LayoutParams lp = (LayoutParams) this.mQSLine.getLayoutParams();
        lp.topMargin = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        this.mQSLine.setLayoutParams(lp);
        updateQsState();
    }

    public void setHost(QSTileHost qsh) {
        this.mQSPanel.setHost(qsh, this.mQSCustomizer);
        this.mHeader.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qsh);
    }

    public void setPanelView(NotificationPanelView panelView) {
        this.mPanelView = panelView;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mQSPanel.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), 0));
        int width = this.mQSPanel.getMeasuredWidth();
        int height = ((LayoutParams) this.mQSPanel.getLayoutParams()).topMargin + this.mQSPanel.getMeasuredHeight();
        if (this.mQSDetail.isShowingDetail()) {
            this.mQSDetail.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), 0));
            height = ((LayoutParams) this.mQSDetail.getLayoutParams()).topMargin + this.mQSDetail.getMeasuredHeight();
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
        getDisplay().getRealSize(this.mSizePoint);
        this.mQSCustomizer.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mSizePoint.y, 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBottom();
    }

    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public void setHeightOverride(int heightOverride) {
        this.mHeightOverride = heightOverride;
        updateBottom();
    }

    public int getDesiredHeight() {
        if (isCustomizing()) {
            return getHeight();
        }
        return getMeasuredHeight();
    }

    public void notifyCustomizeChanged() {
        int i;
        int i2 = 0;
        updateBottom();
        HwPhoneStatusBar.getInstance().updateClearAll();
        View view = this.mQSLine;
        if (this.mQSCustomizer.isShown() || this.mQSDetail.isShown()) {
            i = 4;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        QSPanel qSPanel = this.mQSPanel;
        if (this.mQSCustomizer.isCustomizing() || this.mQSDetail.isShowingDetail()) {
            i = 4;
        } else {
            i = 0;
        }
        qSPanel.setVisibility(i);
        BaseStatusBarHeader baseStatusBarHeader = this.mHeader;
        if (this.mQSCustomizer.isCustomizing() || this.mQSDetail.isShowingDetail()) {
            i2 = 4;
        }
        baseStatusBarHeader.setVisibility(i2);
        this.mPanelView.onQsHeightChanged();
    }

    private void updateBottom() {
        int height = calculateContainerHeight();
        setBottom(getTop() + height);
        this.mQSDetail.setBottom(getTop() + height);
    }

    protected int calculateContainerHeight() {
        int heightOverride = this.mHeightOverride != -1 ? this.mHeightOverride : getMeasuredHeight();
        if (this.mQSCustomizer.isCustomizing()) {
            return this.mQSCustomizer.getHeight();
        }
        return ((int) (this.mQsExpansion * ((float) (heightOverride - getQsMinExpansionHeight())))) + getQsMinExpansionHeight();
    }

    private void updateQsState() {
        int i = 4;
        int i2 = 0;
        HwLog.i("QSContainer", "updateQsState:" + this.mQsExpanded + ", mKeyguardShowing=" + this.mKeyguardShowing + ", " + this);
        if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            boolean z;
            if (!this.mQsExpanded && !this.mStackScrollerOverscrolling) {
                boolean z2 = this.mHeaderAnimating;
            }
            this.mQSPanel.setExpanded(this.mQsExpanded);
            this.mQSDetail.setExpanded(this.mQsExpanded);
            BaseStatusBarHeader baseStatusBarHeader = this.mHeader;
            int i3 = ((!this.mQsExpanded && this.mKeyguardShowing && !this.mHeaderAnimating) || this.mQSDetail.isShowingDetail() || this.mQSCustomizer.isCustomizing()) ? 4 : 0;
            baseStatusBarHeader.setVisibility(i3);
            baseStatusBarHeader = this.mHeader;
            if (this.mKeyguardShowing && !this.mHeaderAnimating) {
                z = true;
            } else if (!this.mQsExpanded || this.mStackScrollerOverscrolling) {
                z = false;
            } else {
                z = true;
            }
            baseStatusBarHeader.setExpanded(z);
            QSPanel qSPanel = this.mQSPanel;
            if ((this.mQsExpanded || !this.mKeyguardShowing || this.mHeaderAnimating) && !this.mQSDetail.isShowingDetail()) {
                i = 0;
            }
            qSPanel.setVisibility(i);
            if (this.mQsExpanded || !this.mKeyguardShowing || this.mHeaderAnimating) {
                i2 = 1;
            }
            setAlpha((float) i2);
        }
    }

    public BaseStatusBarHeader getHeader() {
        return this.mHeader;
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    public QSCustomizer getCustomizer() {
        return this.mQSCustomizer;
    }

    public boolean isShowingDetail() {
        return !this.mQSPanel.isShowingCustomize() ? this.mQSDetail.isShowingDetail() : true;
    }

    public void setHeaderClickable(boolean clickable) {
        this.mHeader.setClickable(clickable);
    }

    public void setExpanded(boolean expanded) {
        boolean isScreenOn = true;
        if (HwPhoneStatusBar.getInstance() != null) {
            isScreenOn = HwPhoneStatusBar.getInstance().isScreenOn();
        }
        this.mQsExpanded = expanded;
        QSPanel qSPanel = this.mQSPanel;
        if (!this.mListening) {
            isScreenOn = false;
        }
        qSPanel.setListening(isScreenOn);
        updateQsState();
    }

    public void setKeyguardShowing(boolean keyguardShowing) {
        Log.d("QSContainer", "setKeyguardShowing " + keyguardShowing + ", " + this);
        this.mKeyguardShowing = keyguardShowing;
        setAlpha((float) (keyguardShowing ? 0 : 1));
        updateQsState();
    }

    public void setOverscrolling(boolean stackScrollerOverscrolling) {
        this.mStackScrollerOverscrolling = stackScrollerOverscrolling;
        updateQsState();
    }

    public void setListening(boolean listening) {
        boolean isScreenOn = true;
        if (HwPhoneStatusBar.getInstance() != null) {
            isScreenOn = HwPhoneStatusBar.getInstance().isScreenOn();
        }
        this.mListening = listening;
        this.mHeader.setListening(listening);
        QSPanel qSPanel = this.mQSPanel;
        if (!this.mListening) {
            isScreenOn = false;
        }
        qSPanel.setListening(isScreenOn);
    }

    public void setQsExpansion(float expansion, float headerTranslation) {
        float f;
        this.mQsExpansion = expansion;
        float translationScaleY = expansion - 1.0f;
        if (!this.mHeaderAnimating) {
            if (this.mKeyguardShowing) {
                headerTranslation = translationScaleY * ((float) getHeight());
            }
            setTranslationY(headerTranslation);
        }
        BaseStatusBarHeader baseStatusBarHeader = this.mHeader;
        if (this.mKeyguardShowing) {
            f = 1.0f;
        } else {
            f = expansion;
        }
        baseStatusBarHeader.setExpansion(f);
        this.mQSPanel.setExpandHeight((int) (((float) this.mQSPanel.getMaxExpandHeight()) * expansion));
        this.mQSDetail.setFullyExpanded(expansion == 1.0f);
        updateBottom();
    }

    public void animateHeaderSlidingIn(long delay) {
        if (!this.mQsExpanded) {
            this.mHeaderAnimating = true;
            this.mDelay = delay;
            getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
        }
    }

    public void animateHeaderSlidingOut() {
        this.mHeaderAnimating = true;
        animate().y((float) (-this.mHeader.getHeight())).setStartDelay(0).setDuration(360).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                QSContainer.this.animate().setListener(null);
                QSContainer.this.mHeaderAnimating = false;
                QSContainer.this.updateQsState();
            }
        }).start();
    }

    public int getQsMinExpansionHeight() {
        if (isShowingDetail()) {
            return this.mQSDetail.getMeasuredHeight() + ((LayoutParams) this.mQSDetail.getLayoutParams()).topMargin;
        }
        return this.mQSPanel.getMinHeight() + ((LayoutParams) this.mQSPanel.getLayoutParams()).topMargin;
    }

    public void setCustomizerShowing(boolean isShowing) {
        HwPhoneStatusBar.getInstance().updateClearAll();
        View view = this.mQSLine;
        int i = (this.mQSCustomizer.isShown() || this.mQSDetail.isShown()) ? 4 : 0;
        view.setVisibility(i);
    }

    public void setDetailShowing(boolean show) {
        View view = this.mQSLine;
        int i = (this.mQSCustomizer.isShown() || this.mQSDetail.isShown()) ? 4 : 0;
        view.setVisibility(i);
    }

    public void onModeChanged(boolean isSuperpowerMode) {
        int i;
        int i2 = 8;
        View view = this.mQSPanel.mBrightnessView;
        if (isSuperpowerMode) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        QSPanel qSPanel = this.mQSPanel;
        if (!this.mQSDetail.isShowingDetail()) {
            i2 = 0;
        }
        qSPanel.setVisibility(i2);
        if (this.mHeader instanceof HwQuickStatusBarHeader) {
            ((HwQuickStatusBarHeader) this.mHeader).onModeChanged(isSuperpowerMode);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("QSContainer: ");
        pw.println("visible=" + getVisibility());
        pw.println("translationX=" + getTranslationX());
        pw.println("translationY=" + getTranslationY());
        pw.println("left=" + getLeft() + ", right=" + getRight() + ", top=" + getTop() + ", bottom=" + getBottom());
        this.mQSPanel.dump(fd, pw, args);
    }
}
