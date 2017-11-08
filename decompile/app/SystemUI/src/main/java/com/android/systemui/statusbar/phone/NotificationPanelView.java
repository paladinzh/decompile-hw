package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.AutoReinflateContainer.InflateListener;
import com.android.systemui.DejankUtils;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm.Result;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnEmptySpaceClickListener;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener;
import com.android.systemui.traffic.TrafficPanelViewContent;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.view.widget.ClockView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class NotificationPanelView extends HwPanelView implements OnHeightChangedListener, OnClickListener, OnOverscrollTopChangedListener, OnEmptySpaceClickListener, OnHeadsUpChangedListener {
    private static final Rect mDummyDirtyRect = new Rect(0, 0, 1, 1);
    private static final boolean mIsfactory = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private HwKeyguardAffordanceHelper mAfforanceHelper;
    private final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.setViewVisibility(NotificationPanelView.this.getBottomView(), 8);
        }
    };
    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mKeyguardStatusBar.setVisibility(4);
            NotificationPanelView.this.mKeyguardStatusBar.setAlpha(1.0f);
            NotificationPanelView.this.mKeyguardStatusBarAnimateAlpha = 1.0f;
        }
    };
    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mKeyguardStatusViewAnimating = false;
            NotificationPanelView.this.mKeyguardStatusView.setVisibility(8);
        }
    };
    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mKeyguardStatusViewAnimating = false;
        }
    };
    private boolean mAnimateNextTopPaddingChange;
    private boolean mBlockTouches;
    private int mClockAnimationTarget = -1;
    private ObjectAnimator mClockAnimator;
    private KeyguardClockPositionAlgorithm mClockPositionAlgorithm = new KeyguardClockPositionAlgorithm();
    private Result mClockPositionResult = new Result();
    private ClockView mClockView;
    private boolean mClosingWithAlphaFadeOut;
    private boolean mCollapsedOnDown;
    private boolean mConflictingQsExpansionGesture;
    private ImageView mDelete;
    private boolean mDozing;
    private boolean mDozingOnDown;
    private float mEmptyDragAmount;
    private boolean mExpandingFromHeadsUp;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private Runnable mHeadsUpExistenceChangedRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mHeadsUpAnimatingAway = false;
            HwLog.i(NotificationPanelView.TAG, "mHeadsUpExistenceChangedRunnable");
            NotificationPanelView.this.notifyBarPanelExpansionChanged();
        }
    };
    private HeadsUpTouchHelper mHeadsUpTouchHelper;
    private boolean mIgnoreIntrinsicPadding = false;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIntercepting;
    private boolean mIsExpanding;
    private boolean mIsExpansionFromHeadsUp;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private float mKeyguardStatusBarAnimateAlpha = 1.0f;
    private boolean mKeyguardStatusViewAnimating;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mLastAnnouncementWasQuickSettings;
    private String mLastCameraLaunchSource = "lockscreen_affordance";
    private int mLastOrientation = -1;
    private float mLastOverscroll;
    private float mLastTouchX;
    private float mLastTouchY;
    private Runnable mLaunchAnimationEndRunnable;
    private boolean mLaunchingAffordance;
    private boolean mListenForHeadsUp;
    private int mNavigationBarBottomHeight;
    private int mNotificationScrimWaitDistance;
    protected NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationsHeaderCollideDistance;
    private int mOldLayoutDirection;
    private boolean mPanelExpanded;
    private int mPositionMinSideMargin;
    private boolean mQsAnimatorExpand;
    private AutoReinflateContainer mQsAutoReinflateContainer;
    protected QSContainer mQsContainer;
    private boolean mQsExpandImmediate;
    private boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    private ValueAnimator mQsExpansionAnimator;
    protected boolean mQsExpansionEnabled = true;
    private boolean mQsExpansionFromOverscroll;
    protected float mQsExpansionHeight;
    private int mQsFalsingThreshold;
    private boolean mQsFullyExpanded;
    protected int mQsMaxExpansionHeight;
    protected int mQsMinExpansionHeight;
    private View mQsNavbarScrim;
    private int mQsPeekHeight;
    private boolean mQsScrimEnabled = true;
    private ValueAnimator mQsSizeChangeAnimator;
    private boolean mQsTouchAboveFalsingThreshold;
    private boolean mQsTracking;
    private boolean mShadeEmpty;
    private boolean mStackScrollerOverscrolling;
    private final AnimatorUpdateListener mStatusBarAnimateAlphaListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            NotificationPanelView.this.mKeyguardStatusBarAnimateAlpha = ((Float) animation.getAnimatedValue()).floatValue();
            NotificationPanelView.this.updateHeaderKeyguardAlpha();
        }
    };
    private int mStatusBarMinHeight;
    private int mStatusBarState;
    private int mTopPaddingAdjustment;
    private int mTrackingPointer;
    private boolean mTwoFingerQsExpandPossible;
    private boolean mUnlockIconActive;
    private int mUnlockMoveDistance;
    private final Runnable mUpdateHeader = new Runnable() {
        public void run() {
            NotificationPanelView.this.mQsContainer.getHeader().updateEverything();
        }
    };
    private VelocityTracker mVelocityTracker;

    public NotificationPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(true);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    public void setStatusBar(PhoneStatusBar bar) {
        this.mStatusBar = bar;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mKeyguardStatusView = (KeyguardStatusView) findViewById(R.id.keyguard_status_view);
        this.mClockView = (ClockView) findViewById(R.id.hw_clock_view);
        this.mNotificationContainerParent = (NotificationsQuickSettingsContainer) findViewById(R.id.notification_container_parent);
        this.mNotificationStackScroller = (NotificationStackScrollLayout) findViewById(R.id.notification_stack_scroller);
        this.mNotificationStackScroller.setOnHeightChangedListener(this);
        this.mNotificationStackScroller.setOverscrollTopChangedListener(this);
        this.mNotificationStackScroller.setOnEmptySpaceClickListener(this);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) findViewById(R.id.keyguard_bottom_area);
        this.mQsNavbarScrim = findViewById(R.id.qs_navbar_scrim);
        this.mAfforanceHelper = new HwKeyguardAffordanceHelper(this, getContext());
        this.mLastOrientation = getResources().getConfiguration().orientation;
        this.mDelete = (ImageView) findViewById(R.id.delete);
        this.mQsAutoReinflateContainer = (AutoReinflateContainer) findViewById(R.id.qs_auto_reinflate_container);
        this.mQsAutoReinflateContainer.addInflateListener(new InflateListener() {
            public void onInflated(View v) {
                NotificationPanelView.this.mQsContainer = (QSContainer) v.findViewById(R.id.quick_settings_container);
                NotificationPanelView.this.mQsContainer.setPanelView(NotificationPanelView.this);
                NotificationPanelView.this.mQsContainer.getHeader().findViewById(R.id.expand_indicator).setOnClickListener(NotificationPanelView.this);
                NotificationPanelView.this.mQsContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (bottom - top != oldBottom - oldTop) {
                            NotificationPanelView.this.onQsHeightChanged();
                        }
                    }
                });
                NotificationPanelView.this.mNotificationStackScroller.setQsContainer(NotificationPanelView.this.mQsContainer);
            }

            public void onAllViewsRemoved() {
            }
        });
    }

    protected void loadDimens() {
        super.loadDimens();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        this.mStatusBarMinHeight = getResources().getDimensionPixelSize(17104919);
        this.mQsPeekHeight = getResources().getDimensionPixelSize(R.dimen.qs_peek_height);
        this.mNotificationsHeaderCollideDistance = getResources().getDimensionPixelSize(R.dimen.header_notifications_collide_distance);
        this.mUnlockMoveDistance = getResources().getDimensionPixelOffset(R.dimen.unlock_move_distance);
        this.mClockPositionAlgorithm.loadDimens(getResources());
        this.mNotificationScrimWaitDistance = getResources().getDimensionPixelSize(R.dimen.notification_scrim_wait_distance);
        this.mQsFalsingThreshold = getResources().getDimensionPixelSize(R.dimen.qs_falsing_threshold);
        this.mPositionMinSideMargin = getResources().getDimensionPixelSize(R.dimen.notification_panel_min_side_margin);
    }

    public void updateResources() {
        int panelWidth = getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
        int panelGravity = getResources().getInteger(R.integer.notification_panel_layout_gravity);
        LayoutParams lp = (LayoutParams) this.mQsAutoReinflateContainer.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            this.mQsAutoReinflateContainer.setLayoutParams(lp);
            this.mQsContainer.post(this.mUpdateHeader);
        }
        lp = (LayoutParams) this.mNotificationStackScroller.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            this.mNotificationStackScroller.setLayoutParams(lp);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        blockBackdropAnimation(true);
        super.onLayout(changed, left, top, right, bottom);
        if (this.mInAnimation) {
            HwLog.w(TAG, "Layout update is skipped as in animation");
            blockBackdropAnimation(false);
            return;
        }
        int i;
        if (isKeyguardStatusViewVisiable()) {
            this.mKeyguardStatusView.setPivotX((float) (getWidth() / 2));
            this.mKeyguardStatusView.setPivotY(((TextView) this.mClockView.findViewById(R.id.clock_text)).getTextSize() * 0.34521484f);
        }
        int oldMaxHeight = this.mQsMaxExpansionHeight;
        if (this.mKeyguardShowing) {
            i = 0;
        } else {
            i = this.mQsContainer.getQsMinExpansionHeight();
        }
        this.mQsMinExpansionHeight = i;
        this.mQsMaxExpansionHeight = this.mQsContainer.getDesiredHeight();
        positionClockAndNotifications();
        if (this.mQsExpanded && this.mQsFullyExpanded) {
            this.mQsExpansionHeight = (float) this.mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false);
            requestPanelHeightUpdate();
            if (this.mQsMaxExpansionHeight != oldMaxHeight) {
                startQsSizeChangeAnimation(oldMaxHeight, this.mQsMaxExpansionHeight);
            }
        } else if (!this.mQsExpanded) {
            setQsExpansion(((float) this.mQsMinExpansionHeight) + this.mLastOverscroll);
        }
        updateStackHeight(getExpandedHeight());
        updateHeader();
        if (this.mQsSizeChangeAnimator == null) {
            this.mQsContainer.setHeightOverride(this.mQsContainer.getDesiredHeight());
        }
        updateMaxHeadsUpTranslation();
        blockBackdropAnimation(false);
    }

    private void startQsSizeChangeAnimation(int oldHeight, int newHeight) {
        if (this.mQsSizeChangeAnimator != null) {
            oldHeight = ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue();
            this.mQsSizeChangeAnimator.cancel();
        }
        this.mQsSizeChangeAnimator = ValueAnimator.ofInt(new int[]{oldHeight, newHeight});
        this.mQsSizeChangeAnimator.setDuration(300);
        this.mQsSizeChangeAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mQsSizeChangeAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationPanelView.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelView.this.requestPanelHeightUpdate();
                NotificationPanelView.this.mQsContainer.setHeightOverride(((Integer) NotificationPanelView.this.mQsSizeChangeAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                NotificationPanelView.this.mQsSizeChangeAnimator = null;
            }
        });
        this.mQsSizeChangeAnimator.start();
    }

    private void positionClockAndNotifications() {
        int stackScrollerPadding;
        boolean animate = this.mNotificationStackScroller.isAddOrRemoveAnimationPending();
        if (this.mStatusBarState != 1) {
            int bottom = this.mQsContainer.getHeader().getHeight();
            if (this.mStatusBarState == 0) {
                stackScrollerPadding = bottom + this.mQsPeekHeight;
            } else {
                stackScrollerPadding = this.mKeyguardStatusBar.getHeight();
            }
            this.mTopPaddingAdjustment = 0;
        } else if (isUseGgStatusView()) {
            this.mClockPositionAlgorithm.setup(this.mStatusBar.getMaxKeyguardNotifications(), getMaxPanelHeight(), getExpandedHeight(), this.mNotificationStackScroller.getNotGoneChildCount(), getHeight(), this.mKeyguardStatusView.getHeight(), this.mEmptyDragAmount);
            this.mClockPositionAlgorithm.run(this.mClockPositionResult);
            if (animate || this.mClockAnimator != null) {
                startClockAnimation(this.mClockPositionResult.clockY);
            } else {
                this.mKeyguardStatusView.setY((float) this.mClockPositionResult.clockY);
            }
            updateClock(this.mClockPositionResult.clockAlpha, this.mClockPositionResult.clockScale);
            stackScrollerPadding = this.mClockPositionResult.stackScrollerPadding;
            this.mTopPaddingAdjustment = this.mClockPositionResult.stackScrollerPaddingAdjustment;
        } else {
            stackScrollerPadding = calculateNotificationsPadding();
        }
        this.mNotificationStackScroller.setIntrinsicPadding(stackScrollerPadding);
        requestScrollerTopPaddingUpdate(animate);
    }

    private int calculateNotificationsPadding() {
        if (this.mNotificationStackScroller == null) {
            return (int) getResources().getDimension(R.dimen.notifications_padding_keyguard_default);
        }
        int i;
        int totalHeight = getHeight();
        int contentHeight = 0;
        int size = this.mNotificationStackScroller.getChildCount();
        int showNotiCount = 0;
        for (int i2 = 0; i2 < size; i2++) {
            View v = this.mNotificationStackScroller.getChildAt(i2);
            if ((v instanceof ExpandableNotificationRow) && v.getVisibility() == 0) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) v;
                if (row.getNumberOfNotificationChildren() > 0) {
                    contentHeight += row.getNotificationMinHeight();
                } else {
                    contentHeight += v.getHeight();
                }
                showNotiCount++;
            } else if ((v instanceof NotificationOverflowContainer) && v.getVisibility() == 0) {
                contentHeight += v.getHeight();
                showNotiCount++;
            } else if ((v instanceof TrafficPanelViewContent) && v.getVisibility() == 0) {
                contentHeight += v.getHeight();
                showNotiCount++;
            }
        }
        int keyguardaddPaddings = getResources().getDimensionPixelSize(R.dimen.notifications_padding_keyguard_add);
        int maxKeyguardSportMusicNotifiCount = HwKeyguardPolicy.getInst().getMaxKeyguardSportMusicNotifications();
        if (maxKeyguardSportMusicNotifiCount == -1 || showNotiCount < maxKeyguardSportMusicNotifiCount) {
            contentHeight += keyguardaddPaddings;
        } else {
            contentHeight = (int) (((double) contentHeight) - (((double) getResources().getDimensionPixelSize(R.dimen.notifications_padding_keyguard_default)) * 1.2d));
        }
        int padding = totalHeight - contentHeight;
        Log.w(TAG, "set notification panel padding = " + padding);
        if (padding > 0) {
            i = padding >> 1;
        } else {
            i = (int) getResources().getDimension(R.dimen.notifications_padding_keyguard_default);
        }
        return i;
    }

    public int computeMaxKeyguardNotifications(int maximum) {
        float minPadding = this.mClockPositionAlgorithm.getMinStackScrollerPadding(getHeight(), this.mKeyguardStatusView.getHeight());
        int notificationPadding = Math.max(1, getResources().getDimensionPixelSize(R.dimen.notification_divider_height));
        int overflowheight = getResources().getDimensionPixelSize(R.dimen.notification_summary_height);
        float availableSpace = ((((float) this.mNotificationStackScroller.getHeight()) - minPadding) - ((float) overflowheight)) - this.mNotificationStackScroller.getKeyguardBottomStackSize();
        int count = 0;
        for (int i = 0; i < this.mNotificationStackScroller.getChildCount(); i++) {
            ExpandableView child = (ExpandableView) this.mNotificationStackScroller.getChildAt(i);
            if (child instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) child;
                if (!(this.mGroupManager.isSummaryOfSuppressedGroup(row.getStatusBarNotification()) || !this.mStatusBar.shouldShowOnKeyguard(row.getStatusBarNotification()) || row.isRemoved())) {
                    availableSpace -= (float) (child.getMinHeight() + notificationPadding);
                    if (availableSpace < 0.0f || count >= maximum) {
                        return count;
                    }
                    count++;
                }
            }
        }
        return count;
    }

    private void startClockAnimation(int y) {
        if (this.mClockAnimationTarget != y) {
            this.mClockAnimationTarget = y;
            getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    NotificationPanelView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (NotificationPanelView.this.mClockAnimator != null) {
                        NotificationPanelView.this.mClockAnimator.removeAllListeners();
                        NotificationPanelView.this.mClockAnimator.cancel();
                    }
                    NotificationPanelView.this.mClockAnimator = ObjectAnimator.ofFloat(NotificationPanelView.this.mKeyguardStatusView, View.Y, new float[]{(float) NotificationPanelView.this.mClockAnimationTarget});
                    NotificationPanelView.this.mClockAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                    NotificationPanelView.this.mClockAnimator.setDuration(360);
                    NotificationPanelView.this.mClockAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            NotificationPanelView.this.mClockAnimator = null;
                            NotificationPanelView.this.mClockAnimationTarget = -1;
                        }
                    });
                    NotificationPanelView.this.mClockAnimator.start();
                    return true;
                }
            });
        }
    }

    private void updateClock(float alpha, float scale) {
        if (this.mInAnimation) {
            HwLog.w(TAG, "updateClock is skipped as in animation");
            return;
        }
        if (!this.mKeyguardStatusViewAnimating) {
            this.mKeyguardStatusView.setAlpha(alpha);
        }
        this.mKeyguardStatusView.setScaleX(scale);
        this.mKeyguardStatusView.setScaleY(scale);
    }

    public void animateToFullShade(long delay) {
        this.mAnimateNextTopPaddingChange = true;
        this.mNotificationStackScroller.goToFullShade(delay);
        requestLayout();
    }

    public void setQsExpansionEnabled(boolean qsExpansionEnabled) {
        this.mQsExpansionEnabled = qsExpansionEnabled;
        this.mQsContainer.setHeaderClickable(qsExpansionEnabled);
    }

    public void resetViews() {
        blockBackdropAnimation(true);
        this.mIsLaunchTransitionFinished = false;
        this.mBlockTouches = false;
        this.mUnlockIconActive = false;
        if (!this.mLaunchingAffordance) {
            this.mAfforanceHelper.reset(false);
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        closeQs();
        this.mStatusBar.dismissPopups();
        this.mNotificationStackScroller.setOverScrollAmount(0.0f, true, false, true);
        this.mNotificationStackScroller.resetScrollPosition();
        blockBackdropAnimation(false);
    }

    public void closeQs() {
        HwLog.i(TAG, "closeQs");
        cancelQsAnimation();
        setQsExpansion((float) this.mQsMinExpansionHeight);
    }

    public void animateCloseQs() {
        HwLog.i(TAG, "animateCloseQs");
        if (this.mQsExpansionAnimator != null) {
            if (this.mQsAnimatorExpand) {
                float height = this.mQsExpansionHeight;
                this.mQsExpansionAnimator.cancel();
                setQsExpansion(height);
            } else {
                return;
            }
        }
        flingSettings(0.0f, false);
    }

    public void expandWithQs() {
        if (this.mQsExpansionEnabled) {
            this.mQsExpandImmediate = true;
        }
        expand(true);
    }

    public void fling(float vel, boolean expand) {
        GestureRecorder gr = ((PhoneStatusBarView) this.mBar).mBar.getGestureRecorder();
        if (gr != null) {
            gr.tag("fling " + (vel > 0.0f ? "open" : "closed"), "notifications,v=" + vel);
        }
        super.fling(vel, expand);
    }

    protected void flingToHeight(float vel, boolean expand, float target, float collapseSpeedUpFactor, boolean expandBecauseOfFalsing) {
        boolean z;
        boolean z2 = false;
        this.mNotificationStackScroller.removeLongPressCallback();
        HeadsUpTouchHelper headsUpTouchHelper = this.mHeadsUpTouchHelper;
        if (expand) {
            z = false;
        } else {
            z = true;
        }
        headsUpTouchHelper.notifyFling(z);
        if (!expand && getFadeoutAlpha() == 1.0f) {
            z2 = true;
        }
        setClosingWithAlphaFadeout(z2);
        super.flingToHeight(vel, expand, target, collapseSpeedUpFactor, expandBecauseOfFalsing);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (event.getEventType() != 32) {
            return super.dispatchPopulateAccessibilityEventInternal(event);
        }
        event.getText().add(getKeyguardOrLockScreenString());
        this.mLastAnnouncementWasQuickSettings = false;
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (SystemUiUtil.allowLogEvent(event)) {
            HwLog.i(TAG, "onInterceptTouchEvent " + event + ", mBlockTouches=" + this.mBlockTouches);
        }
        if (this.mBlockTouches || this.mQsContainer.isCustomizing()) {
            return false;
        }
        if (this.mHeadsUpTouchHelper.onInterceptTouchEvent(event)) {
            this.mIsExpansionFromHeadsUp = true;
            MetricsLogger.count(this.mContext, "panel_open", 1);
            MetricsLogger.count(this.mContext, "panel_open_peek", 1);
            return true;
        } else if (isFullyCollapsed() || !onQsIntercept(event)) {
            return super.onInterceptTouchEvent(event);
        } else {
            return true;
        }
    }

    private boolean onQsIntercept(MotionEvent event) {
        boolean newIndex = true;
        if (mIsfactory && this.mKeyguardShowing) {
            return false;
        }
        int pointerIndex = event.findPointerIndex(this.mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            this.mTrackingPointer = event.getPointerId(0);
        }
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        switch (event.getActionMasked()) {
            case 0:
                this.mIntercepting = true;
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                initVelocityTracker();
                trackMovement(event);
                if (shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, 0.0f)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (this.mQsExpansionAnimator != null) {
                    onQsExpansionStarted();
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mQsTracking = true;
                    this.mIntercepting = false;
                    this.mNotificationStackScroller.removeLongPressCallback();
                    break;
                }
                break;
            case 1:
            case 3:
                trackMovement(event);
                if (this.mQsTracking) {
                    if (event.getActionMasked() != 3) {
                        newIndex = false;
                    }
                    flingQsWithCurrentVelocity(y, newIndex);
                    this.mQsTracking = false;
                }
                this.mIntercepting = false;
                break;
            case 2:
                float h = y - this.mInitialTouchY;
                trackMovement(event);
                if (this.mQsTracking) {
                    setQsExpansion(this.mInitialHeightOnTouch + h);
                    trackMovement(event);
                    this.mIntercepting = false;
                    return true;
                } else if (Math.abs(h) > ((float) this.mTouchSlop) && Math.abs(h) > Math.abs(x - this.mInitialTouchX) && shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, h)) {
                    this.mQsTracking = true;
                    onQsExpansionStarted();
                    notifyExpandingFinished();
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mInitialTouchY = y;
                    this.mInitialTouchX = x;
                    this.mIntercepting = false;
                    this.mNotificationStackScroller.removeLongPressCallback();
                    return true;
                }
            case 6:
                int upPointer = event.getPointerId(event.getActionIndex());
                if (this.mTrackingPointer == upPointer) {
                    int newIndex2;
                    if (event.getPointerId(0) != upPointer) {
                        newIndex2 = 0;
                    }
                    this.mTrackingPointer = event.getPointerId(newIndex2);
                    this.mInitialTouchX = event.getX(newIndex2);
                    this.mInitialTouchY = event.getY(newIndex2);
                    break;
                }
                break;
        }
        return false;
    }

    protected boolean isInContentBounds(float x, float y) {
        float stackScrollerX = this.mNotificationStackScroller.getX();
        if (this.mNotificationStackScroller.isBelowLastNotification(x - stackScrollerX, y) || stackScrollerX >= x || x >= ((float) this.mNotificationStackScroller.getWidth()) + stackScrollerX) {
            return false;
        }
        return true;
    }

    public void initDownStates(MotionEvent event) {
        boolean z = false;
        if (event.getActionMasked() == 0) {
            this.mQsTouchAboveFalsingThreshold = this.mQsFullyExpanded;
            this.mDozingOnDown = isDozing();
            this.mCollapsedOnDown = isFullyCollapsed();
            if (this.mCollapsedOnDown) {
                z = this.mHeadsUpManager.hasPinnedHeadsUp();
            }
            this.mListenForHeadsUp = z;
        }
    }

    public boolean isUnlockAvaile() {
        return (!this.mKeyguardShowing || this.mQsExpanded || this.mListenForHeadsUp || this.mQsContainer.isCustomizing()) ? false : true;
    }

    private void flingQsWithCurrentVelocity(float y, boolean isCancelMotionEvent) {
        boolean z = false;
        float vel = getCurrentVelocity();
        boolean expandsQs = flingExpandsQs(vel);
        if (expandsQs) {
            logQsSwipeDown(y);
        }
        if (expandsQs && !isCancelMotionEvent) {
            z = true;
        }
        flingSettings(vel, z);
    }

    private void logQsSwipeDown(float y) {
        int gesture;
        float vel = getCurrentVelocity();
        if (this.mStatusBarState == 1) {
            gesture = 8;
        } else {
            gesture = 9;
        }
        EventLogTags.writeSysuiLockscreenGesture(gesture, (int) ((y - this.mInitialTouchY) / this.mStatusBar.getDisplayDensity()), (int) (vel / this.mStatusBar.getDisplayDensity()));
    }

    private boolean flingExpandsQs(float vel) {
        boolean z = true;
        boolean z2 = false;
        if (isFalseTouch()) {
            return false;
        }
        if (Math.abs(vel) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            if (getQsExpansionFraction() <= 0.5f) {
                z = false;
            }
            return z;
        }
        if (vel > 0.0f && getQsExpansionHeight() > 0.125f) {
            z2 = true;
        }
        return z2;
    }

    private boolean isFalseTouch() {
        boolean z = false;
        if (!needsAntiFalsing()) {
            return false;
        }
        if (this.mFalsingManager.isClassiferEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        if (!this.mQsTouchAboveFalsingThreshold) {
            z = true;
        }
        return z;
    }

    public float getQsExpansionFraction() {
        return Math.min(1.0f, (this.mQsExpansionHeight - ((float) this.mQsMinExpansionHeight)) / ((float) (getTempQsMaxExpansion() - this.mQsMinExpansionHeight)));
    }

    public float getQsExpansionHeight() {
        if (this.mQsContainer.getQsPanel() == null || this.mQsContainer.getQsPanel().getBrightnessView() == null) {
            return Math.min(1.0f, this.mQsExpansionHeight / ((float) getTempQsMaxExpansion()));
        }
        return Math.min(1.0f, (this.mQsExpansionHeight - ((float) this.mQsContainer.getQsPanel().getBrightnessView().getHeight())) / ((float) getTempQsMaxExpansion()));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (SystemUiUtil.allowLogEvent(event)) {
            HwLog.i(TAG, "onTouchEvent::" + event.getActionMasked() + ", x=" + event.getX() + ", y=" + event.getY());
        }
        if (this.mKeyguardShowing && HwPhoneStatusBar.getInstance().getCallingLayout().isIsCalllinearlayouShowing()) {
            HwPhoneStatusBar.getInstance().getCallingLayout().onTouchEvent(event);
        }
        if (mIsfactory) {
            HwLog.i(TAG, "onTouchEvent::is in factory, return");
            return false;
        } else if (this.mBlockTouches || this.mQsContainer.isCustomizing()) {
            HwLog.i(TAG, "onTouchEvent::return because of mBlockTouches=" + this.mBlockTouches);
            return false;
        } else {
            if (this.mListenForHeadsUp && !this.mHeadsUpTouchHelper.isTrackingHeadsUp() && this.mHeadsUpTouchHelper.onInterceptTouchEvent(event)) {
                this.mIsExpansionFromHeadsUp = true;
                MetricsLogger.count(this.mContext, "panel_open_peek", 1);
            }
            this.mHeadsUpTouchHelper.onTouchEvent(event);
            if (this.mHeadsUpTouchHelper.isTrackingHeadsUp() || !handleQsTouch(event)) {
                if (event.getActionMasked() == 0 && isFullyCollapsed()) {
                    MetricsLogger.count(this.mContext, "panel_open", 1);
                    updateVerticalPanelPosition(event.getX());
                }
                super.onTouchEvent(event);
                if (event.getAction() == 3 || event.getAction() == 1) {
                    this.mQsTracking = false;
                }
                return true;
            }
            HwLog.i(TAG, "onTouchEvent::handleQsTouch use the event!");
            return true;
        }
    }

    private boolean handleQsTouch(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0 && getExpandedFraction() == 1.0f && this.mStatusBar.getBarState() != 1 && !this.mQsExpanded && this.mQsExpansionEnabled) {
            this.mQsTracking = true;
            this.mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = event.getX();
            this.mInitialTouchX = event.getY();
        }
        if (!isFullyCollapsed()) {
            handleQsDown(event);
        }
        if (!this.mQsExpandImmediate && this.mQsTracking) {
            onQsTouch(event);
            if (!this.mConflictingQsExpansionGesture) {
                return true;
            }
        }
        if (action == 3 || action == 1) {
            this.mConflictingQsExpansionGesture = false;
        }
        if (action == 0 && isFullyCollapsed() && this.mQsExpansionEnabled) {
            this.mTwoFingerQsExpandPossible = true;
        }
        if (this.mTwoFingerQsExpandPossible && isOpenQsEvent(event) && event.getY(event.getActionIndex()) < ((float) this.mStatusBarMinHeight)) {
            MetricsLogger.count(this.mContext, "panel_open_qs", 1);
            this.mQsExpandImmediate = true;
            requestPanelHeightUpdate();
            setListening(true);
        }
        return false;
    }

    private boolean isInQsArea(float x, float y) {
        if (x < this.mQsAutoReinflateContainer.getX() || x > this.mQsAutoReinflateContainer.getX() + ((float) this.mQsAutoReinflateContainer.getWidth())) {
            return false;
        }
        return y <= this.mNotificationStackScroller.getBottomMostNotificationBottom() || y <= this.mQsContainer.getY() + ((float) this.mQsContainer.getHeight());
    }

    private boolean isOpenQsEvent(MotionEvent event) {
        boolean mouseButtonClickDrag;
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        boolean twoFingerDrag = action == 5 ? pointerCount == 2 : false;
        boolean stylusButtonClickDrag;
        if (action != 0) {
            stylusButtonClickDrag = false;
        } else if (event.isButtonPressed(32)) {
            stylusButtonClickDrag = true;
        } else {
            stylusButtonClickDrag = event.isButtonPressed(64);
        }
        if (action != 0) {
            mouseButtonClickDrag = false;
        } else if (event.isButtonPressed(2)) {
            mouseButtonClickDrag = true;
        } else {
            mouseButtonClickDrag = event.isButtonPressed(4);
        }
        if (twoFingerDrag || r3) {
            return true;
        }
        return mouseButtonClickDrag;
    }

    private void handleQsDown(MotionEvent event) {
        if (event.getActionMasked() == 0 && shouldQuickSettingsIntercept(event.getX(), event.getY(), -1.0f)) {
            this.mFalsingManager.onQsDown();
            this.mQsTracking = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = event.getX();
            this.mInitialTouchX = event.getY();
            notifyExpandingFinished();
        }
    }

    protected boolean flingExpands(float vel, float vectorVel, float x, float y) {
        return super.flingExpands(vel, vectorVel, x, y);
    }

    protected boolean hasConflictingGestures() {
        return this.mStatusBar.getBarState() != 0;
    }

    protected boolean shouldGestureIgnoreXTouchSlop(float x, float y) {
        return !this.mAfforanceHelper.isOnAffordanceIcon(x, y);
    }

    private void onQsTouch(MotionEvent event) {
        boolean z = true;
        int newIndex = 0;
        int pointerIndex = event.findPointerIndex(this.mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            this.mTrackingPointer = event.getPointerId(0);
        }
        float y = event.getY(pointerIndex);
        float x = event.getX(pointerIndex);
        float h = y - this.mInitialTouchY;
        switch (event.getActionMasked()) {
            case 0:
                this.mQsTracking = true;
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                onQsExpansionStarted();
                this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                if (this.mKeyguardShowing) {
                    this.mInitialHeightOnTouch += (float) HwPhoneStatusBar.getInstance().getStatusBarHeight();
                }
                initVelocityTracker();
                trackMovement(event);
                return;
            case 1:
            case 3:
                this.mQsTracking = false;
                this.mTrackingPointer = -1;
                trackMovement(event);
                if (getQsExpansionFraction() != 0.0f || y >= this.mInitialTouchY) {
                    if (event.getActionMasked() != 3) {
                        z = false;
                    }
                    flingQsWithCurrentVelocity(y, z);
                }
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    return;
                }
                return;
            case 2:
                setQsExpansion(this.mInitialHeightOnTouch + h);
                if (h >= ((float) getFalsingThreshold())) {
                    this.mQsTouchAboveFalsingThreshold = true;
                }
                trackMovement(event);
                return;
            case 6:
                int upPointer = event.getPointerId(event.getActionIndex());
                if (this.mTrackingPointer == upPointer) {
                    if (event.getPointerId(0) == upPointer) {
                        newIndex = 1;
                    }
                    float newY = event.getY(newIndex);
                    float newX = event.getX(newIndex);
                    this.mTrackingPointer = event.getPointerId(newIndex);
                    this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                    this.mInitialTouchY = newY;
                    this.mInitialTouchX = newX;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mQsFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    public void onOverscrollTopChanged(float amount, boolean isRubberbanded) {
        float rounded;
        boolean z = false;
        cancelQsAnimation();
        if (!this.mQsExpansionEnabled) {
            amount = 0.0f;
        }
        if (amount >= 1.0f) {
            rounded = amount;
        } else {
            rounded = 0.0f;
        }
        if (rounded == 0.0f) {
            isRubberbanded = false;
        }
        setOverScrolling(isRubberbanded);
        if (rounded != 0.0f) {
            z = true;
        }
        this.mQsExpansionFromOverscroll = z;
        this.mLastOverscroll = rounded;
        updateQsState();
        setQsExpansion(((float) this.mQsMinExpansionHeight) + rounded);
    }

    public void flingTopOverscroll(float velocity, boolean open) {
        boolean z;
        this.mLastOverscroll = 0.0f;
        this.mQsExpansionFromOverscroll = false;
        setQsExpansion(this.mQsExpansionHeight);
        if (!this.mQsExpansionEnabled && open) {
            velocity = 0.0f;
        }
        if (open) {
            z = this.mQsExpansionEnabled;
        } else {
            z = false;
        }
        flingSettings(velocity, z, new Runnable() {
            public void run() {
                NotificationPanelView.this.mStackScrollerOverscrolling = false;
                NotificationPanelView.this.setOverScrolling(false);
                NotificationPanelView.this.updateQsState();
            }
        }, false);
    }

    private void setOverScrolling(boolean overscrolling) {
        this.mStackScrollerOverscrolling = overscrolling;
        this.mQsContainer.setOverscrolling(overscrolling);
    }

    private void onQsExpansionStarted() {
        HwLog.i(TAG, "onQsExpansionStarted");
        onQsExpansionStarted(0);
        HwPhoneStatusBar.getInstance().onQsExpansionStarted();
    }

    private void onQsExpansionStarted(int overscrollAmount) {
        HwLog.i(TAG, "onQsExpansionStarted" + overscrollAmount);
        cancelQsAnimation();
        cancelHeightAnimator();
        setQsExpansion(this.mQsExpansionHeight - ((float) overscrollAmount));
        requestPanelHeightUpdate();
        HwPhoneStatusBar.getInstance().onQsExpansionStarted();
    }

    private void setQsExpanded(boolean expanded) {
        if (this.mQsExpanded != expanded) {
            HwLog.v(TAG, "setQsExpanded " + expanded);
            if (this.mKeyguardShowing) {
                BDReporter.e(this.mContext, 358, "expanded:" + expanded);
                setListening(expanded);
            } else {
                BDReporter.e(this.mContext, 361, "expanded:" + expanded);
            }
            this.mQsExpanded = expanded;
            updateQsState();
            requestPanelHeightUpdate();
            this.mFalsingManager.setQsExpanded(expanded);
            this.mStatusBar.setQsExpanded(expanded);
            this.mNotificationContainerParent.setQsExpanded(expanded);
            HwPhoneStatusBar.getInstance().onQsExpanded(expanded);
        }
    }

    public void setBarState(int statusBarState, boolean keyguardFadingAway, boolean goingToFullShade) {
        int oldState = this.mStatusBarState;
        boolean keyguardShowing = statusBarState == 1;
        setKeyguardStatusViewVisibility(statusBarState, keyguardFadingAway, goingToFullShade);
        setKeyguardBottomAreaVisibility(statusBarState, goingToFullShade);
        this.mStatusBarState = statusBarState;
        this.mKeyguardShowing = keyguardShowing;
        HwLog.v(TAG, "setBarState mKeyguardShowing " + this.mKeyguardShowing + "; state = " + statusBarState + " - " + 1);
        this.mQsContainer.setKeyguardShowing(this.mKeyguardShowing);
        if (goingToFullShade || (oldState == 1 && statusBarState == 2)) {
            if (!HwKeyguardPolicy.getInst().showKeyguardStatusBarInbouncer()) {
                animateKeyguardStatusBarOut();
            }
            this.mQsContainer.animateHeaderSlidingIn(this.mStatusBarState == 2 ? 0 : this.mStatusBar.calculateGoingToFullShadeDelay());
        } else if (oldState == 2 && statusBarState == 1) {
            if (!HwPhoneStatusBar.getInstance().getCallingLayout().isIsCalllinearlayouShowing()) {
                animateKeyguardStatusBarIn(360);
            }
            this.mQsContainer.animateHeaderSlidingOut();
        } else {
            if (!HwPhoneStatusBar.getInstance().getCallingLayout().isIsCalllinearlayouShowing()) {
                this.mKeyguardStatusBar.setAlpha(1.0f);
                KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
                int i = (keyguardShowing || this.mStatusBar.isBouncerShowing()) ? 0 : 4;
                keyguardStatusBarView.setVisibility(i);
            }
            if (!(this.mKeyguardBottomArea == null || !keyguardShowing || oldState == this.mStatusBarState)) {
                this.mKeyguardBottomArea.updateLeftAffordance();
                this.mAfforanceHelper.updatePreviews();
            }
        }
        if (keyguardShowing) {
            updateDozingVisibilities(false);
        }
        resetVerticalPanelPosition();
        updateQsState();
        if (!this.mKeyguardShowing) {
            this.mNotificationStackScroller.setAlpha(1.0f);
        } else if (HwKeyguardPolicy.getInst().blockNotificationInKeyguard()) {
            this.mNotificationStackScroller.setAlpha(0.0f);
        } else {
            this.mNotificationStackScroller.setAlpha(1.0f);
        }
        HwLog.v(TAG, "setBarState mKeyguardShowing " + this.mKeyguardShowing + "; state = " + statusBarState + " - " + 1);
    }

    private void animateKeyguardStatusBarOut() {
        long keyguardFadingAwayDelay;
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{this.mKeyguardStatusBar.getAlpha(), 0.0f});
        anim.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        if (this.mStatusBar.isKeyguardFadingAway()) {
            keyguardFadingAwayDelay = this.mStatusBar.getKeyguardFadingAwayDelay();
        } else {
            keyguardFadingAwayDelay = 0;
        }
        anim.setStartDelay(keyguardFadingAwayDelay);
        if (this.mStatusBar.isKeyguardFadingAway()) {
            keyguardFadingAwayDelay = this.mStatusBar.getKeyguardFadingAwayDuration() / 2;
        } else {
            keyguardFadingAwayDelay = 360;
        }
        anim.setDuration(keyguardFadingAwayDelay);
        anim.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                NotificationPanelView.this.mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        anim.start();
    }

    private void animateKeyguardStatusBarIn(long duration) {
        this.mKeyguardStatusBar.setVisibility(0);
        this.mKeyguardStatusBar.setAlpha(0.0f);
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        anim.setDuration(duration);
        anim.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        anim.start();
    }

    private void setKeyguardBottomAreaVisibility(int statusBarState, boolean goingToFullShade) {
        View bottomView = getBottomView();
        if (bottomView != null) {
            bottomView.animate().cancel();
            if (goingToFullShade) {
                bottomView.animate().alpha(0.0f).setStartDelay(this.mStatusBar.getKeyguardFadingAwayDelay()).setDuration(this.mStatusBar.getKeyguardFadingAwayDuration() / 2).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardBottomAreaInvisibleEndRunnable).start();
            } else if (statusBarState == 1 || statusBarState == 2) {
                if (!this.mDozing) {
                    bottomView.setVisibility(0);
                }
                bottomView.setAlpha(1.0f);
            } else {
                bottomView.setVisibility(8);
                bottomView.setAlpha(1.0f);
            }
        }
    }

    private void setKeyguardStatusViewVisibility(int statusBarState, boolean keyguardFadingAway, boolean goingToFullShade) {
        if (this.mInAnimation) {
            HwLog.i(TAG, "setKeyguardStatusViewVisibility skipped as in animation");
            return;
        }
        if ((!keyguardFadingAway && this.mStatusBarState == 1 && statusBarState != 1) || goingToFullShade) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).setStartDelay(0).setDuration(160).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardStatusViewInvisibleEndRunnable);
            if (keyguardFadingAway) {
                this.mKeyguardStatusView.animate().setStartDelay(this.mStatusBar.getKeyguardFadingAwayDelay()).setDuration(this.mStatusBar.getKeyguardFadingAwayDuration() / 2).start();
            }
        } else if (this.mStatusBarState == 2 && statusBarState == 1 && isUseGgStatusView()) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.setAlpha(0.0f);
            this.mKeyguardStatusView.animate().alpha(1.0f).setStartDelay(0).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
        } else if (statusBarState == 1 && isUseGgStatusView()) {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = false;
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusView.setAlpha(1.0f);
        } else {
            this.mKeyguardStatusView.animate().cancel();
            this.mKeyguardStatusViewAnimating = false;
            this.mKeyguardStatusView.setVisibility(8);
            this.mKeyguardStatusView.setAlpha(1.0f);
        }
    }

    private void updateQsState() {
        boolean z;
        boolean qsNavbarVisible;
        int i = 0;
        this.mQsContainer.setExpanded(this.mQsExpanded);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        if (this.mStatusBarState == 1) {
            z = false;
        } else if (this.mQsExpanded) {
            z = this.mQsExpansionFromOverscroll;
        } else {
            z = true;
        }
        notificationStackScrollLayout.setScrollingEnabled(z);
        updateEmptyShadeView();
        if (this.mStatusBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            qsNavbarVisible = this.mQsScrimEnabled;
        } else {
            qsNavbarVisible = false;
        }
        View view = this.mQsNavbarScrim;
        if (!qsNavbarVisible) {
            i = 4;
        }
        view.setVisibility(i);
        if (this.mKeyguardUserSwitcher != null && this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            this.mKeyguardUserSwitcher.hideIfNotSimple(true);
        }
    }

    private void setQsExpansion(float height) {
        boolean z;
        height = Math.min(Math.max(height, (float) this.mQsMinExpansionHeight), (float) this.mQsMaxExpansionHeight);
        if (height != ((float) this.mQsMaxExpansionHeight) || this.mQsMaxExpansionHeight == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mQsFullyExpanded = z;
        if (height > ((float) this.mQsMinExpansionHeight) && !this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            setQsExpanded(true);
        } else if (height <= ((float) this.mQsMinExpansionHeight) && this.mQsExpanded) {
            setQsExpanded(false);
            if (!(!this.mLastAnnouncementWasQuickSettings || this.mTracking || isCollapsing())) {
                announceForAccessibility(getKeyguardOrLockScreenString());
                this.mLastAnnouncementWasQuickSettings = false;
            }
        }
        this.mQsExpansionHeight = height;
        updateQsExpansion();
        requestScrollerTopPaddingUpdate(false);
        if (this.mKeyguardShowing) {
            updateHeaderKeyguardAlpha();
        }
        if (this.mStatusBarState == 2 || this.mStatusBarState == 1) {
            updateKeyguardBottomAreaAlpha();
        }
        if (this.mStatusBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling && this.mQsScrimEnabled) {
            this.mQsNavbarScrim.setAlpha(getQsExpansionFraction());
        }
        if (!(height == 0.0f || !this.mQsFullyExpanded || this.mLastAnnouncementWasQuickSettings)) {
            announceForAccessibility(getContext().getString(R.string.accessibility_desc_quick_settings));
            this.mLastAnnouncementWasQuickSettings = true;
        }
        if (this.mQsFullyExpanded && this.mFalsingManager.shouldEnforceBouncer()) {
            this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
        updateNotificationTranslucency();
    }

    protected void updateQsExpansion() {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_updateQsExpansion");
        this.mQsContainer.setQsExpansion(getQsExpansionFraction(), getHeaderTranslation());
        PerfDebugUtils.endSystraceSection();
    }

    private String getKeyguardOrLockScreenString() {
        if (this.mQsContainer.isCustomizing()) {
            return getContext().getString(R.string.accessibility_desc_quick_settings_edit);
        }
        if (this.mStatusBarState == 1) {
            return getContext().getString(R.string.accessibility_desc_lock_screen);
        }
        return getContext().getString(R.string.accessibility_desc_notification_shade);
    }

    private float calculateQsTopPadding() {
        if (this.mKeyguardShowing && (this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted))) {
            int max;
            int maxNotifications = this.mClockPositionResult.stackScrollerPadding - this.mClockPositionResult.stackScrollerPaddingAdjustment;
            int maxQs = getTempQsMaxExpansion();
            if (this.mStatusBarState == 1) {
                max = Math.max(maxNotifications, maxQs);
            } else {
                max = maxQs;
            }
            return (float) ((int) interpolate(getExpandedFraction(), (float) this.mQsMinExpansionHeight, (float) max));
        } else if (this.mQsSizeChangeAnimator != null) {
            return (float) ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue();
        } else {
            if (this.mKeyguardShowing) {
                return interpolate(getQsExpansionFraction(), (float) this.mNotificationStackScroller.getIntrinsicPadding(), (float) this.mQsMaxExpansionHeight);
            }
            return this.mQsExpansionHeight;
        }
    }

    protected void requestScrollerTopPaddingUpdate(boolean animate) {
        boolean ignoreIntrinsicPadding = false;
        if (this.mKeyguardShowing && !this.mQsContainer.isCustomizing()) {
            ignoreIntrinsicPadding = ((float) this.mQsMinExpansionHeight) < this.mQsExpansionHeight && this.mQsExpansionHeight <= ((float) this.mQsMaxExpansionHeight);
            if (this.mIgnoreIntrinsicPadding != ignoreIntrinsicPadding) {
                this.mIgnoreIntrinsicPadding = ignoreIntrinsicPadding;
                Log.i(TAG, "requestScrollerTopPaddingUpdate: ignoreIntrinsicPadding=" + ignoreIntrinsicPadding);
            }
        }
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        float calculateQsTopPadding = calculateQsTopPadding();
        if (this.mAnimateNextTopPaddingChange) {
            animate = true;
        }
        if (!this.mKeyguardShowing) {
            ignoreIntrinsicPadding = false;
        } else if (this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            ignoreIntrinsicPadding = true;
        }
        notificationStackScrollLayout.updateTopPadding(calculateQsTopPadding, animate, ignoreIntrinsicPadding);
        this.mAnimateNextTopPaddingChange = false;
    }

    private void trackMovement(MotionEvent event) {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(event);
        }
        this.mLastTouchX = event.getX();
        this.mLastTouchY = event.getY();
    }

    private void initVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentVelocity() {
        if (this.mVelocityTracker == null) {
            return 0.0f;
        }
        this.mVelocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    private void cancelQsAnimation() {
        if (this.mQsExpansionAnimator != null) {
            this.mQsExpansionAnimator.cancel();
        }
    }

    private void flingSettings(float vel, boolean expand) {
        flingSettings(vel, expand, null, false);
    }

    private void flingSettings(float vel, boolean expand, final Runnable onFinishRunnable, boolean isClick) {
        HwLog.i(TAG, "flingSettings:" + vel + ", " + expand);
        float target = (float) (expand ? this.mQsMaxExpansionHeight : this.mQsMinExpansionHeight);
        if (target == this.mQsExpansionHeight) {
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        boolean belowFalsingThreshold = isFalseTouch();
        if (belowFalsingThreshold) {
            vel = 0.0f;
        }
        Animator animator = ValueAnimator.ofFloat(new float[]{this.mQsExpansionHeight, target});
        if (isClick) {
            animator.setInterpolator(Interpolators.TOUCH_RESPONSE);
            animator.setDuration(368);
        } else {
            this.mFlingAnimationUtils.apply(animator, this.mQsExpansionHeight, target, vel);
        }
        if (belowFalsingThreshold) {
            animator.setDuration(350);
        }
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationPanelView.this.setQsExpansion(((Float) animation.getAnimatedValue()).floatValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                NotificationPanelView.this.mQsExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
        this.mQsExpansionAnimator = animator;
        this.mQsAnimatorExpand = expand;
    }

    private boolean shouldQuickSettingsIntercept(float x, float y, float yDiff) {
        boolean z = false;
        if (!this.mQsExpansionEnabled || this.mCollapsedOnDown) {
            return false;
        }
        boolean isTalking = false;
        if (HwPhoneStatusBar.getInstance().getCallingLayout() != null) {
            isTalking = HwPhoneStatusBar.getInstance().getCallingLayout().isTalking();
        }
        View header = this.mKeyguardShowing ? this.mKeyguardStatusBar : this.mQsContainer;
        boolean onHeader = (x < this.mQsAutoReinflateContainer.getX() || x > this.mQsAutoReinflateContainer.getX() + ((float) this.mQsAutoReinflateContainer.getWidth()) || y < ((float) header.getTop()) || y > ((float) header.getBottom())) ? false : (isTalking && header.getTranslationY() > 0.0f) || !isTalking;
        if (!this.mQsExpanded) {
            return onHeader;
        }
        if (onHeader) {
            z = true;
        } else if (yDiff < 0.0f) {
            z = isInQsArea(x, y);
        }
        return z;
    }

    protected boolean isScrolledToBottom() {
        boolean z = true;
        if (isInSettings()) {
            return true;
        }
        if (this.mStatusBar.getBarState() != 1) {
            z = this.mNotificationStackScroller.isScrolledToBottom();
        }
        return z;
    }

    protected int getMaxPanelHeight() {
        int maxHeight;
        int min = this.mStatusBarMinHeight;
        if (this.mStatusBar.getBarState() != 1 && this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            min = Math.max(min, (int) ((((float) this.mQsMinExpansionHeight) + getOverExpansionAmount()) * 2.05f));
        }
        if (this.mQsExpandImmediate || this.mQsExpanded || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            maxHeight = calculatePanelHeightQsExpanded();
        } else {
            maxHeight = calculatePanelHeightShade();
        }
        return Math.max(maxHeight, min);
    }

    private boolean isInSettings() {
        return this.mQsExpanded;
    }

    protected void onHeightUpdated(float expandedHeight) {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_onHeightUpdated");
        if (!this.mQsExpanded || this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            positionClockAndNotifications();
        }
        if (this.mQsExpandImmediate || !(!this.mQsExpanded || this.mQsTracking || this.mQsExpansionAnimator != null || this.mQsExpansionFromOverscroll || this.mQsContainer.isCustomizing())) {
            float t;
            if (this.mKeyguardShowing) {
                t = expandedHeight / ((float) getMaxPanelHeight());
            } else {
                float panelHeightQsCollapsed = (float) (this.mNotificationStackScroller.getIntrinsicPadding() + this.mNotificationStackScroller.getLayoutMinHeight());
                t = (expandedHeight - panelHeightQsCollapsed) / (((float) calculatePanelHeightQsExpanded()) - panelHeightQsCollapsed);
            }
            setQsExpansion(((float) this.mQsMinExpansionHeight) + (((float) (getTempQsMaxExpansion() - this.mQsMinExpansionHeight)) * t));
        }
        updateStackHeight(expandedHeight);
        updateHeader();
        updateNotificationTranslucency();
        updatePanelExpanded();
        this.mNotificationStackScroller.setShadeExpanded(!isFullyCollapsed());
        PerfDebugUtils.endSystraceSection();
    }

    private void updatePanelExpanded() {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_updatePanelExpanded");
        boolean isExpanded = !isFullyCollapsed();
        if (this.mPanelExpanded != isExpanded) {
            this.mHeadsUpManager.setIsExpanded(isExpanded);
            this.mStatusBar.setPanelExpanded(isExpanded);
            this.mPanelExpanded = isExpanded;
        }
        PerfDebugUtils.endSystraceSection();
    }

    private int getTempQsMaxExpansion() {
        return this.mQsMaxExpansionHeight;
    }

    private int calculatePanelHeightShade() {
        return (int) (((float) ((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mTopPaddingAdjustment)) + this.mNotificationStackScroller.getTopPaddingOverflow());
    }

    private int calculatePanelHeightQsExpanded() {
        int i;
        float notificationHeight = (float) ((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mNotificationStackScroller.getTopPadding());
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0 && this.mShadeEmpty) {
            notificationHeight = (float) ((this.mNotificationStackScroller.getEmptyShadeViewHeight() + this.mNotificationStackScroller.getBottomStackPeekSize()) + this.mNotificationStackScroller.getBottomStackSlowDownHeight());
        }
        int maxQsHeight = this.mQsMaxExpansionHeight;
        if (this.mQsSizeChangeAnimator != null) {
            maxQsHeight = ((Integer) this.mQsSizeChangeAnimator.getAnimatedValue()).intValue();
        }
        if (this.mStatusBarState == 1) {
            i = this.mClockPositionResult.stackScrollerPadding - this.mTopPaddingAdjustment;
        } else {
            i = 0;
        }
        float totalHeight = ((float) Math.max(maxQsHeight, i)) + notificationHeight;
        if (totalHeight > ((float) this.mNotificationStackScroller.getHeight())) {
            totalHeight = Math.max((float) (this.mNotificationStackScroller.getLayoutMinHeight() + maxQsHeight), (float) this.mNotificationStackScroller.getHeight());
        }
        return (int) totalHeight;
    }

    private void updateNotificationTranslucency() {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_updateNotificationTranslucency");
        float alpha = 1.0f;
        if (!(!this.mClosingWithAlphaFadeOut || this.mExpandingFromHeadsUp || this.mHeadsUpManager.hasPinnedHeadsUp())) {
            alpha = getFadeoutAlpha();
        }
        if (!this.mKeyguardShowing) {
            this.mNotificationStackScroller.setAlpha(alpha);
        } else if (HwKeyguardPolicy.getInst().blockNotificationInKeyguard()) {
            this.mNotificationStackScroller.setAlpha(this.mQsExpansionHeight > 200.0f ? 1.0f : this.mQsExpansionHeight / 200.0f);
        }
        HwPhoneStatusBar.getInstance().updateTraffic();
        HwPhoneStatusBar.getInstance().updateBlurView();
        HwPhoneStatusBar.getInstance().updateClearAll();
        HwPhoneStatusBar.getInstance().updateCallingLayout();
        PerfDebugUtils.endSystraceSection();
    }

    private float getFadeoutAlpha() {
        return (float) Math.pow((double) Math.max(0.0f, Math.min((getNotificationsTopY() + ((float) this.mNotificationStackScroller.getFirstItemMinHeight())) / ((float) ((this.mQsMinExpansionHeight + this.mNotificationStackScroller.getBottomStackPeekSize()) - this.mNotificationStackScroller.getBottomStackSlowDownHeight())), 1.0f)), 0.75d);
    }

    protected float getOverExpansionAmount() {
        return this.mNotificationStackScroller.getCurrentOverScrollAmount(true);
    }

    protected float getOverExpansionPixels() {
        return this.mNotificationStackScroller.getCurrentOverScrolledPixels(true);
    }

    private void updateHeader() {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_updateHeader");
        if (this.mStatusBar.getBarState() == 1) {
            updateHeaderKeyguardAlpha();
        }
        updateQsExpansion();
        PerfDebugUtils.endSystraceSection();
    }

    protected float getHeaderTranslation() {
        if (this.mStatusBar.getBarState() == 1) {
            return 0.0f;
        }
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return Math.min(0.0f, (this.mExpandedHeight / 2.05f) - ((float) this.mQsMinExpansionHeight));
        }
        float stackTranslation = this.mNotificationStackScroller.getStackTranslation();
        float translation = stackTranslation;
        if (this.mHeadsUpManager.hasPinnedHeadsUp() || this.mIsExpansionFromHeadsUp) {
            translation = (((float) this.mNotificationStackScroller.getTopPadding()) + stackTranslation) - ((float) this.mQsMinExpansionHeight);
        }
        return Math.min(0.0f, translation);
    }

    private float getKeyguardContentsAlpha() {
        if (this.mKeyguardStatusBar == null) {
            Log.e(TAG, "getKeyguardContentsAlpha return 1 as KeyguardStatusBar = null");
            return 1.0f;
        }
        float alpha;
        if (this.mStatusBar.getBarState() == 1) {
            alpha = getNotificationsTopY() / ((float) (this.mKeyguardStatusBar.getHeight() + this.mNotificationsHeaderCollideDistance));
        } else {
            alpha = getNotificationsTopY() / ((float) this.mKeyguardStatusBar.getHeight());
        }
        return (float) Math.pow((double) MathUtils.constrain(alpha, 0.0f, 1.0f), 0.75d);
    }

    private void updateHeaderKeyguardAlpha() {
        if (this.mKeyguardStatusBar == null) {
            Log.e(TAG, "updateHeaderKeyguardAlpha skiped as KeyguardStatusBar = null");
        } else if (HwPhoneStatusBar.getInstance().getCallingLayout().isIsCalllinearlayouShowing()) {
            this.mKeyguardStatusBar.setAlpha(0.0f);
            this.mKeyguardStatusBar.setVisibility(4);
        } else {
            this.mKeyguardStatusBar.setAlpha(this.mKeyguardStatusBarAnimateAlpha * (1.0f - Math.min(1.0f, getQsExpansionFraction() * 2.0f)));
            int visibility = (this.mKeyguardStatusBar.getAlpha() == 0.0f || this.mDozing || this.mStatusBarState != 1) ? 4 : 0;
            this.mKeyguardStatusBar.setVisibility(visibility);
        }
    }

    private void updateKeyguardBottomAreaAlpha() {
        int i = 0;
        View bottom = getBottomView();
        if (bottom != null) {
            float alpha = Math.min(getKeyguardContentsAlpha(), 1.0f - getQsExpansionFraction());
            if (this.mStatusBarState == 2) {
                alpha = 0.0f;
            }
            bottom.setAlpha(alpha);
            if (alpha == 0.0f) {
                i = 4;
            }
            bottom.setImportantForAccessibility(i);
            if ((bottom.getVisibility() == 0 || this.mQsExpansionHeight == 0.0f) && getBokehChangeStatus()) {
                updateBackdropView(1.0f - alpha);
            }
        }
    }

    private float getNotificationsTopY() {
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return getExpandedHeight();
        }
        return this.mNotificationStackScroller.getNotificationsTopY();
    }

    protected void onExpandingStarted() {
        HwLog.i(TAG, "onExpandingStarted");
        super.onExpandingStarted();
        this.mNotificationStackScroller.onExpansionStarted();
        this.mIsExpanding = true;
        this.mQsExpandedWhenExpandingStarted = this.mQsFullyExpanded;
        if (this.mQsExpanded) {
            onQsExpansionStarted();
        }
        HwPhoneStatusBar.getInstance().onExpandingStarted();
    }

    protected void onExpandingFinished() {
        HwLog.i(TAG, "onExpandingFinished");
        super.onExpandingFinished();
        this.mNotificationStackScroller.onExpansionStopped();
        this.mHeadsUpManager.onExpandingFinished();
        this.mIsExpanding = false;
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable() {
                public void run() {
                    NotificationPanelView.this.setListening(false);
                }
            });
            postOnAnimation(new Runnable() {
                public void run() {
                    NotificationPanelView.this.getParent().invalidateChild(NotificationPanelView.this, NotificationPanelView.mDummyDirtyRect);
                }
            });
        } else {
            setListening(true);
        }
        this.mQsExpandImmediate = false;
        this.mTwoFingerQsExpandPossible = false;
        this.mIsExpansionFromHeadsUp = false;
        this.mNotificationStackScroller.setTrackingHeadsUp(false);
        this.mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    private void setListening(final boolean listening) {
        int i;
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_setListening_" + listening);
        Runnable anonymousClass18 = new Runnable() {
            public void run() {
                PerfDebugUtils.beginSystraceSection("NotificationPanelView_setListening_run_" + listening);
                NotificationPanelView.this.mQsContainer.setListening(listening);
                PerfDebugUtils.endSystraceSection();
            }
        };
        if (listening) {
            i = 100;
        } else {
            i = 10;
        }
        postDelayed(anonymousClass18, (long) i);
        PerfDebugUtils.endSystraceSection();
    }

    public void expand(boolean animate) {
        super.expand(animate);
        if (this.mStatusBar.getBarState() != 1) {
            setListening(true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void setOverExpansion(float overExpansion, boolean isPixels) {
        if (!(this.mConflictingQsExpansionGesture || this.mQsExpandImmediate || this.mStatusBar.getBarState() == 1)) {
            this.mNotificationStackScroller.setOnHeightChangedListener(null);
            if (isPixels) {
                this.mNotificationStackScroller.setOverScrolledPixels(overExpansion, true, false);
            } else {
                this.mNotificationStackScroller.setOverScrollAmount(overExpansion, true, false);
            }
            this.mNotificationStackScroller.setOnHeightChangedListener(this);
        }
    }

    protected void onTrackingStarted() {
        this.mFalsingManager.onTrackingStarted();
        super.onTrackingStarted();
        if (this.mQsFullyExpanded) {
            this.mQsExpandImmediate = true;
        }
        if (this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) {
            this.mAfforanceHelper.animateHideLeftRightIcon();
        }
        this.mNotificationStackScroller.onPanelTrackingStarted();
    }

    protected void onTrackingStopped(boolean expand) {
        this.mFalsingManager.onTrackingStopped();
        super.onTrackingStopped(expand);
        if (expand) {
            this.mNotificationStackScroller.setOverScrolledPixels(0.0f, true, true);
        }
        this.mNotificationStackScroller.onPanelTrackingStopped();
        if (expand && ((this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) && !this.mHintAnimationRunning)) {
            this.mAfforanceHelper.reset(true);
        }
        if (!expand) {
            if (this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) {
                KeyguardAffordanceView lockIcon = getLockIcon();
                if (lockIcon != null) {
                    lockIcon.setImageAlpha(0.0f, true, 100, Interpolators.FAST_OUT_LINEAR_IN, null);
                    lockIcon.setImageScale(2.0f, true, 100, Interpolators.FAST_OUT_LINEAR_IN);
                }
            }
        }
    }

    public void onHeightChanged(ExpandableView view, boolean needsAnimation) {
        if (view != null || !this.mQsExpanded) {
            requestPanelHeightUpdate();
        }
    }

    public void onReset(ExpandableView view) {
    }

    public void onQsHeightChanged() {
        this.mQsMaxExpansionHeight = this.mQsContainer.getDesiredHeight();
        if (this.mQsExpanded && this.mQsFullyExpanded) {
            this.mQsExpansionHeight = (float) this.mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false);
            requestPanelHeightUpdate();
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mAfforanceHelper.onConfigurationChanged();
        if (newConfig.orientation != this.mLastOrientation) {
            resetVerticalPanelPosition();
        }
        this.mLastOrientation = newConfig.orientation;
        this.mDelete.setContentDescription(this.mContext.getString(R.string.accessibility_delete_all_noti));
        HwLog.i(TAG, "visible=" + getVisibility() + ", translationX=" + getTranslationX() + "translationY=" + getTranslationY() + "left=" + getLeft() + ", right=" + getRight() + ", top=" + getTop() + ", bottom=" + getBottom());
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mNavigationBarBottomHeight = insets.getStableInsetBottom();
        updateMaxHeadsUpTranslation();
        return insets;
    }

    private void updateMaxHeadsUpTranslation() {
        this.mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), this.mNavigationBarBottomHeight);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        if (layoutDirection != this.mOldLayoutDirection) {
            this.mAfforanceHelper.onRtlPropertiesChanged();
            this.mOldLayoutDirection = layoutDirection;
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.expand_indicator) {
            onQsExpansionStarted();
            if (this.mQsExpanded) {
                BDReporter.c(this.mContext, 343);
                flingSettings(0.0f, false, null, true);
            } else if (this.mQsExpansionEnabled) {
                BDReporter.c(this.mContext, 342);
                EventLogTags.writeSysuiLockscreenGesture(10, 0, 0);
                flingSettings(0.0f, true, null, true);
            }
        }
    }

    public void expandNotificationPanelView() {
        onQsExpansionStarted();
        flingSettings(0.0f, true, null, true);
    }

    protected void startUnlockHintAnimation() {
        super.startUnlockHintAnimation();
        startHighlightIconAnimation(getCenterIcon());
    }

    private void startHighlightIconAnimation(final KeyguardAffordanceView icon) {
        if (icon == null) {
            Log.i(TAG, "HwKeyguard block startHighlightIconAnimation");
            return;
        }
        icon.setImageAlpha(1.0f, true, 200, Interpolators.FAST_OUT_SLOW_IN, new Runnable() {
            public void run() {
                icon.setImageAlpha(icon.getRestingAlpha(), true, 200, Interpolators.FAST_OUT_SLOW_IN, null);
            }
        });
    }

    public KeyguardAffordanceView getLeftIcon() {
        if (this.mKeyguardBottomArea == null) {
            return null;
        }
        KeyguardAffordanceView rightView;
        if (getLayoutDirection() == 1) {
            rightView = this.mKeyguardBottomArea.getRightView();
        } else {
            rightView = this.mKeyguardBottomArea.getLeftView();
        }
        return rightView;
    }

    public KeyguardAffordanceView getCenterIcon() {
        return this.mKeyguardBottomArea == null ? null : this.mKeyguardBottomArea.getLockIcon();
    }

    public KeyguardAffordanceView getRightIcon() {
        if (this.mKeyguardBottomArea == null) {
            return null;
        }
        KeyguardAffordanceView leftView;
        if (getLayoutDirection() == 1) {
            leftView = this.mKeyguardBottomArea.getLeftView();
        } else {
            leftView = this.mKeyguardBottomArea.getRightView();
        }
        return leftView;
    }

    public boolean needsAntiFalsing() {
        return this.mStatusBarState == 1;
    }

    protected float getPeekHeight() {
        if (this.mShadeEmpty) {
            return ((float) HwPhoneStatusBar.getInstance().getStatusBarHeight()) * 4.0f;
        }
        return (float) HwPhoneStatusBar.getInstance().getStatusBarHeight();
    }

    protected float getCannedFlingDurationFactor() {
        if (this.mQsExpanded) {
            return 0.7f;
        }
        return 0.6f;
    }

    protected boolean fullyExpandedClearAllVisible() {
        if (this.mNotificationStackScroller.isDismissViewNotGone() && this.mNotificationStackScroller.isScrolledToBottom() && !this.mQsExpandImmediate) {
            return true;
        }
        return false;
    }

    protected boolean isClearAllVisible() {
        return this.mNotificationStackScroller.isDismissViewVisible();
    }

    protected int getClearAllHeight() {
        return this.mNotificationStackScroller.getDismissViewHeight();
    }

    protected boolean isTrackingBlocked() {
        return this.mConflictingQsExpansionGesture ? this.mQsExpanded : false;
    }

    public boolean isQsExpanded() {
        return this.mQsExpanded;
    }

    public boolean isQsDetailShowing() {
        return this.mQsContainer.isShowingDetail();
    }

    public void closeQsDetail() {
        this.mQsContainer.getQsPanel().closeDetail();
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public boolean isLaunchTransitionFinished() {
        return this.mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return this.mIsLaunchTransitionRunning;
    }

    public void setLaunchTransitionEndRunnable(Runnable r) {
        this.mLaunchAnimationEndRunnable = r;
    }

    public void setEmptyDragAmount(float amount) {
        float factor = 0.8f;
        if (this.mNotificationStackScroller.getNotGoneChildCount() > 0) {
            factor = 0.4f;
        } else if (!this.mStatusBar.hasActiveNotifications()) {
            factor = 0.4f;
        }
        this.mEmptyDragAmount = amount * factor;
        positionClockAndNotifications();
    }

    private static float interpolate(float t, float start, float end) {
        return ((1.0f - t) * start) + (t * end);
    }

    public void setDozing(boolean dozing, boolean animate) {
        if (dozing != this.mDozing) {
            this.mDozing = dozing;
            if (this.mStatusBarState == 1) {
                updateDozingVisibilities(animate);
            }
        }
    }

    private void updateDozingVisibilities(boolean animate) {
        View bottomView = getBottomView();
        if (this.mDozing) {
            setViewVisibility(this.mKeyguardStatusBar, 4);
            setViewVisibility(bottomView, 4);
            return;
        }
        if (isUseGgStatusView()) {
            setViewVisibility(this.mKeyguardStatusBar, 0);
        }
        setViewVisibility(bottomView, 0);
        if (animate) {
            animateKeyguardStatusBarIn(700);
            if (isUseGgBottomView()) {
                this.mKeyguardBottomArea.startFinishDozeAnimation();
            }
        }
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public void setShadeEmpty(boolean shadeEmpty) {
        this.mShadeEmpty = shadeEmpty;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {
        boolean z = false;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        if (this.mShadeEmpty && !this.mQsExpanded) {
            z = true;
        }
        notificationStackScrollLayout.updateEmptyShadeView(z, true);
    }

    public void setQsScrimEnabled(boolean qsScrimEnabled) {
        boolean changed = this.mQsScrimEnabled != qsScrimEnabled;
        this.mQsScrimEnabled = qsScrimEnabled;
        if (changed) {
            updateQsState();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void onScreenTurningOn() {
        if (isUseGgStatusView()) {
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusView.refreshTime();
            return;
        }
        this.mKeyguardStatusView.setVisibility(8);
    }

    public void onEmptySpaceClicked(float x, float y) {
        onEmptySpaceClick(x);
    }

    protected boolean onMiddleClicked() {
        switch (this.mStatusBar.getBarState()) {
            case 0:
                post(this.mPostCollapseRunnable);
                return false;
            case 1:
                if (!this.mDozingOnDown) {
                    EventLogTags.writeSysuiLockscreenGesture(3, 0, 0);
                    startUnlockHintAnimation();
                }
                return true;
            case 2:
                if (!this.mQsExpanded) {
                    this.mStatusBar.goToKeyguard();
                }
                return true;
            default:
                return true;
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void onHeadsUpPinnedModeChanged(boolean inPinnedMode) {
        if (inPinnedMode) {
            this.mHeadsUpExistenceChangedRunnable.run();
            updateNotificationTranslucency();
            return;
        }
        this.mHeadsUpAnimatingAway = true;
        this.mNotificationStackScroller.runAfterAnimationFinished(this.mHeadsUpExistenceChangedRunnable);
    }

    public void onHeadsUpPinned(ExpandableNotificationRow headsUp) {
        this.mNotificationStackScroller.generateHeadsUpAnimation(headsUp, true);
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow headsUp) {
    }

    public void onHeadsUpStateChanged(Entry entry, boolean isHeadsUp) {
        this.mNotificationStackScroller.generateHeadsUpAnimation(entry.row, isHeadsUp);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        super.setHeadsUpManager(headsUpManager);
        this.mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManager, this.mNotificationStackScroller, this);
    }

    public void setTrackingHeadsUp(boolean tracking) {
        if (tracking) {
            this.mNotificationStackScroller.setTrackingHeadsUp(true);
            this.mExpandingFromHeadsUp = true;
        }
    }

    protected void onClosingFinished() {
        super.onClosingFinished();
        resetVerticalPanelPosition();
        setClosingWithAlphaFadeout(false);
    }

    private void setClosingWithAlphaFadeout(boolean closing) {
        this.mClosingWithAlphaFadeOut = closing;
        this.mNotificationStackScroller.forceNoOverlappingRendering(closing);
    }

    protected void updateVerticalPanelPosition(float x) {
        if (((float) this.mNotificationStackScroller.getWidth()) * 1.75f > ((float) getWidth()) || this.mNotificationStackScroller.getWidth() <= 0) {
            resetVerticalPanelPosition();
            return;
        }
        float leftMost = (float) (this.mPositionMinSideMargin + (this.mNotificationStackScroller.getWidth() / 2));
        float rightMost = (float) ((getWidth() - this.mPositionMinSideMargin) - (this.mNotificationStackScroller.getWidth() / 2));
        if (Math.abs(x - ((float) (getWidth() / 2))) < ((float) (this.mNotificationStackScroller.getWidth() / 4))) {
            x = (float) (getWidth() / 2);
        }
        setVerticalPanelTranslation(Math.min(rightMost, Math.max(leftMost, x)) - ((float) (this.mNotificationStackScroller.getLeft() + (this.mNotificationStackScroller.getWidth() / 2))));
    }

    private void resetVerticalPanelPosition() {
        setVerticalPanelTranslation(0.0f);
    }

    protected void setVerticalPanelTranslation(float translation) {
        this.mNotificationStackScroller.setTranslationX(translation);
        this.mQsAutoReinflateContainer.setTranslationX(translation);
    }

    protected void updateStackHeight(float stackHeight) {
        PerfDebugUtils.beginSystraceSection("NotificationPanelView_updateStackHeight");
        this.mNotificationStackScroller.setStackHeight(stackHeight);
        updateKeyguardBottomAreaAlpha();
        PerfDebugUtils.endSystraceSection();
    }

    public void setPanelScrimMinFraction(float minFraction) {
        this.mBar.panelScrimMinFractionChanged(minFraction);
    }

    public void clearNotificationEffects() {
        this.mStatusBar.clearNotificationEffects();
    }

    protected boolean isPanelVisibleBecauseOfHeadsUp() {
        return !this.mHeadsUpManager.hasPinnedHeadsUp() ? this.mHeadsUpAnimatingAway : true;
    }

    public boolean hasOverlappingRendering() {
        return !this.mDozing ? this.mStatusBar.isBouncerShowing() : false;
    }

    public void launchCamera(boolean animate, int source) {
        boolean z = true;
        if (source == 1) {
            this.mLastCameraLaunchSource = "power_double_tap";
        } else if (source == 0) {
            this.mLastCameraLaunchSource = "wiggle_gesture";
        } else {
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        if (isFullyCollapsed()) {
            animate = false;
        } else {
            this.mLaunchingAffordance = true;
            setLaunchingAffordance(true);
        }
        HwKeyguardAffordanceHelper hwKeyguardAffordanceHelper = this.mAfforanceHelper;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        hwKeyguardAffordanceHelper.launchAffordance(animate, z);
    }

    public void onAffordanceLaunchEnded() {
        this.mLaunchingAffordance = false;
        setLaunchingAffordance(false);
    }

    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.mNotificationStackScroller.setParentFadingOut(alpha != 1.0f);
    }

    private void setLaunchingAffordance(boolean launchingAffordance) {
        setLaunchingAffordance(getLeftIcon(), launchingAffordance);
        setLaunchingAffordance(getRightIcon(), launchingAffordance);
        setLaunchingAffordance(getCenterIcon(), launchingAffordance);
    }

    public boolean canCameraGestureBeLaunched(boolean keyguardIsShowing) {
        String packageToLaunch = null;
        ResolveInfo resolveInfo = this.mKeyguardBottomArea.resolveCameraIntent();
        if (!(resolveInfo == null || resolveInfo.activityInfo == null)) {
            packageToLaunch = resolveInfo.activityInfo.packageName;
        }
        if (packageToLaunch == null) {
            return false;
        }
        if ((keyguardIsShowing || !isForegroundApp(packageToLaunch)) && !this.mAfforanceHelper.isSwipingInProgress()) {
            return true;
        }
        return false;
    }

    private boolean isForegroundApp(String pkgName) {
        List<RunningTaskInfo> tasks = ((ActivityManager) getContext().getSystemService(ActivityManager.class)).getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        return pkgName.equals(((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName());
    }

    public void setGroupManager(NotificationGroupManager groupManager) {
        this.mGroupManager = groupManager;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NotificationPanel: ");
        pw.println("visible=" + getVisibility());
        pw.println("translationX=" + getTranslationX());
        pw.println("translationY=" + getTranslationY());
        pw.println("left=" + getLeft() + ", right=" + getRight() + ", top=" + getTop() + ", bottom=" + getBottom());
        this.mNotificationStackScroller.dump(fd, pw, args);
        this.mQsContainer.dump(fd, pw, args);
    }

    public boolean isFullyCollapsed() {
        return super.isFullyCollapsed();
    }

    public boolean getFastUnlockMode() {
        if (this.mStatusBar != null) {
            return this.mStatusBar.getFastUnlockMode();
        }
        return false;
    }

    public void collapsePanelViewWhenScreenShot() {
        onQsExpansionStarted();
        flingSettings(0.0f, false, null, true);
    }
}
