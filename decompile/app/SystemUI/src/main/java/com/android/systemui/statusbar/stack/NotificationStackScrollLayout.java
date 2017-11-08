package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AnimationUtils;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.SwipeHelper.Callback;
import com.android.systemui.SwipeHelper.LongPressListener;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.ExpandableView.OnHeightChangedListener;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.NotificationSettingsIconRow;
import com.android.systemui.statusbar.NotificationSettingsIconRow.SettingsIconRowListener;
import com.android.systemui.statusbar.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager.NotificationGroup;
import com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ScrollAdapter;
import com.android.systemui.traffic.TrafficPanelViewContent;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class NotificationStackScrollLayout extends ViewGroup implements Callback, ExpandHelper.Callback, ScrollAdapter, OnHeightChangedListener, OnGroupChangeListener, SettingsIconRowListener, ScrollContainer {
    private static final Property<NotificationStackScrollLayout, Float> BACKGROUND_FADE = new FloatProperty<NotificationStackScrollLayout>("backgroundFade") {
        public void setValue(NotificationStackScrollLayout object, float value) {
            object.setBackgroundFadeAmount(value);
        }

        public Float get(NotificationStackScrollLayout object) {
            return Float.valueOf(object.getBackgroundFadeAmount());
        }
    };
    private boolean mActivateNeedsAnimation;
    private int mActivePointerId;
    private ArrayList<View> mAddedHeadsUpChildren;
    private AmbientState mAmbientState;
    private boolean mAnimateNextBackgroundBottom;
    private boolean mAnimateNextBackgroundTop;
    private ArrayList<AnimationEvent> mAnimationEvents;
    private HashSet<Runnable> mAnimationFinishedRunnables;
    private boolean mAnimationRunning;
    private boolean mAnimationsEnabled;
    private Rect mBackgroundBounds;
    private float mBackgroundFadeAmount;
    private OnPreDrawListener mBackgroundUpdater;
    private ObjectAnimator mBottomAnimator;
    private int mBottomInset;
    private int mBottomStackPeekSize;
    private int mBottomStackSlowDownHeight;
    private boolean mChangePositionInProgress;
    private boolean mChildTransferInProgress;
    private ArrayList<View> mChildrenChangingPositions;
    private HashSet<View> mChildrenToAddAnimated;
    private ArrayList<View> mChildrenToRemoveAnimated;
    private boolean mChildrenUpdateRequested;
    private OnPreDrawListener mChildrenUpdater;
    private HashSet<View> mClearOverlayViewsWhenFinished;
    private int mCollapsedSize;
    private int mContentHeight;
    private boolean mContinuousShadowUpdate;
    private NotificationSettingsIconRow mCurrIconRow;
    private Rect mCurrentBounds;
    private int mCurrentStackHeight;
    private StackScrollState mCurrentStackScrollState;
    private int mDarkAnimationOriginIndex;
    private boolean mDarkNeedsAnimation;
    private float mDimAmount;
    private ValueAnimator mDimAnimator;
    private AnimatorListener mDimEndListener;
    private AnimatorUpdateListener mDimUpdateListener;
    private boolean mDimmedNeedsAnimation;
    private boolean mDisallowDismissInThisMotion;
    private boolean mDisallowScrollingInThisMotion;
    private boolean mDismissAllInProgress;
    private DismissView mDismissView;
    private boolean mDontClampNextScroll;
    private boolean mDontReportNextOverScroll;
    private int mDownX;
    private ArrayList<View> mDragAnimPendingChildren;
    private EmptyShadeView mEmptyShadeView;
    private Rect mEndAnimationRect;
    private boolean mEverythingNeedsAnimation;
    private ExpandHelper mExpandHelper;
    private View mExpandedGroupView;
    private boolean mExpandedInThisMotion;
    private boolean mExpandingNotification;
    private boolean mFadingOut;
    private FalsingManager mFalsingManager;
    private Runnable mFinishScrollingCallback;
    private ExpandableView mFirstVisibleBackgroundChild;
    private boolean mForceNoOverlappingRendering;
    private View mForcedScroll;
    private HashSet<View> mFromMoreCardAdditions;
    private View mGearExposedView;
    private boolean mGenerateChildOrderChangedEvent;
    private long mGoToFullShadeDelay;
    private boolean mGoToFullShadeNeedsAnimation;
    private boolean mGroupExpandedForMeasure;
    private NotificationGroupManager mGroupManager;
    private HashSet<Pair<ExpandableNotificationRow, Boolean>> mHeadsUpChangeAnimations;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHideSensitiveNeedsAnimation;
    private int mIncreasedPaddingBetweenElements;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mIntrinsicPadding;
    private boolean mIsBeingDragged;
    private boolean mIsExpanded;
    private boolean mIsExpansionChanging;
    private boolean mIsSwipeEvent;
    private boolean mIsUIBlocked;
    private int mLastMotionY;
    private float mLastSetStackHeight;
    private ExpandableView mLastVisibleBackgroundChild;
    private OnChildLocationsChangedListener mListener;
    private LongPressListener mLongPressListener;
    private int mMaxLayoutHeight;
    private float mMaxOverScroll;
    private int mMaxScrollAfterExpand;
    private int mMaximumVelocity;
    private float mMinTopOverScrollToEscape;
    private int mMinimumVelocity;
    private boolean mNeedViewResizeAnimation;
    private boolean mNeedsAnimation;
    private OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private OnHeightChangedListener mOnHeightChangedListener;
    private boolean mOnlyScrollingInThisMotion;
    private float mOverScrolledBottomPixels;
    private float mOverScrolledTopPixels;
    private int mOverflingDistance;
    private NotificationOverflowContainer mOverflowContainer;
    private OnOverscrollTopChangedListener mOverscrollTopChangedListener;
    private int mOwnScrollY;
    private int mPaddingBetweenElements;
    private boolean mPanelTracking;
    private boolean mParentFadingOut;
    private PhoneStatusBar mPhoneStatusBar;
    private boolean mPulsing;
    protected QSContainer mQsContainer;
    private Runnable mReclamp;
    private boolean mRequestViewResizeAnimationOnLayout;
    private ScrimController mScrimController;
    private boolean mScrollable;
    private boolean mScrolledToTopOnFirstDown;
    private OverScroller mScroller;
    private boolean mScrollingEnabled;
    private OnPreDrawListener mShadowUpdater;
    private ArrayList<View> mSnappedBackChildren;
    private final StackScrollAlgorithm mStackScrollAlgorithm;
    private float mStackTranslation;
    private Rect mStartAnimationRect;
    private final StackStateAnimator mStateAnimator;
    private NotificationSwipeHelper mSwipeHelper;
    private ArrayList<View> mSwipedOutViews;
    private boolean mSwipingInProgress;
    private int[] mTempInt2;
    private final ArrayList<Pair<ExpandableNotificationRow, Boolean>> mTmpList;
    private ArrayList<ExpandableView> mTmpSortedChildren;
    private ObjectAnimator mTopAnimator;
    private int mTopPadding;
    private boolean mTopPaddingNeedsAnimation;
    private float mTopPaddingOverflow;
    private boolean mTouchIsClick;
    private int mTouchSlop;
    private boolean mTrackingHeadsUp;
    private View mTranslatingParentView;
    private VelocityTracker mVelocityTracker;
    private Comparator<ExpandableView> mViewPositionComparator;

    public interface OnOverscrollTopChangedListener {
        void flingTopOverscroll(float f, boolean z);

        void onOverscrollTopChanged(float f, boolean z);
    }

    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClicked(float f, float f2);
    }

    public interface OnChildLocationsChangedListener {
        void onChildLocationsChanged(NotificationStackScrollLayout notificationStackScrollLayout);
    }

    static class AnimationEvent {
        static AnimationFilter[] FILTERS = new AnimationFilter[]{new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateDimmed().animateZ(), new AnimationFilter().animateShadowAlpha(), new AnimationFilter().animateShadowAlpha().animateHeight(), new AnimationFilter().animateZ(), new AnimationFilter().animateDimmed(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateDark().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateDimmed().animateZ().hasDelays(), new AnimationFilter().animateHideSensitive(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ().hasDelays(), new AnimationFilter().animateShadowAlpha().animateHeight().animateTopInset().animateY().animateZ(), new AnimationFilter().animateAlpha().animateShadowAlpha().animateDark().animateDimmed().animateHideSensitive().animateHeight().animateTopInset().animateY().animateZ()};
        static int[] LENGTHS = new int[]{464, 464, 360, 360, 360, 360, 220, 220, 360, 360, 448, 360, 360, 360, 650, 230, 230, 360, 360};
        final int animationType;
        final View changingView;
        int darkAnimationOriginIndex;
        final long eventStartTime;
        final AnimationFilter filter;
        boolean headsUpFromBottom;
        final long length;
        View viewAfterChangingView;

        AnimationEvent(View view, int type) {
            this(view, type, (long) LENGTHS[type]);
        }

        AnimationEvent(View view, int type, long length) {
            this.eventStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.changingView = view;
            this.animationType = type;
            this.filter = FILTERS[type];
            this.length = length;
        }

        static long combineLength(ArrayList<AnimationEvent> events) {
            long length = 0;
            int size = events.size();
            for (int i = 0; i < size; i++) {
                AnimationEvent event = (AnimationEvent) events.get(i);
                length = Math.max(length, event.length);
                if (event.animationType == 10) {
                    return event.length;
                }
            }
            return length;
        }
    }

    private class NotificationSwipeHelper extends SwipeHelper {
        private CheckForDrag mCheckForDrag;
        private Runnable mFalsingCheck = new Runnable() {
            public void run() {
                NotificationSwipeHelper.this.resetExposedGearView(true, true);
            }
        };
        private boolean mGearSnappedOnLeft;
        private boolean mGearSnappedTo;
        private Handler mHandler = new Handler();

        private final class CheckForDrag implements Runnable {
            private CheckForDrag() {
            }

            public void run() {
                if (NotificationStackScrollLayout.this.mTranslatingParentView != null) {
                    float translation = NotificationSwipeHelper.this.getTranslation(NotificationStackScrollLayout.this.mTranslatingParentView);
                    float absTransX = Math.abs(translation);
                    float bounceBackToGearWidth = NotificationSwipeHelper.this.getSpaceForGear(NotificationStackScrollLayout.this.mTranslatingParentView);
                    float notiThreshold = NotificationSwipeHelper.this.getSize(NotificationStackScrollLayout.this.mTranslatingParentView) * 0.4f;
                    if (NotificationStackScrollLayout.this.mCurrIconRow != null && ((!NotificationStackScrollLayout.this.mCurrIconRow.isVisible() || NotificationStackScrollLayout.this.mCurrIconRow.isIconLocationChange(translation)) && ((double) absTransX) >= ((double) bounceBackToGearWidth) * 0.4d && absTransX < notiThreshold)) {
                        boolean z;
                        NotificationSettingsIconRow -get2 = NotificationStackScrollLayout.this.mCurrIconRow;
                        if (translation > 0.0f) {
                            z = true;
                        } else {
                            z = false;
                        }
                        -get2.fadeInSettings(z, translation, notiThreshold);
                    }
                }
            }
        }

        public NotificationSwipeHelper(int swipeDirection, Callback callback, Context context) {
            super(swipeDirection, callback, context);
        }

        public void onDownUpdate(View currView) {
            NotificationStackScrollLayout.this.mTranslatingParentView = currView;
            cancelCheckForDrag();
            if (NotificationStackScrollLayout.this.mCurrIconRow != null) {
                NotificationStackScrollLayout.this.mCurrIconRow.setSnapping(false);
            }
            this.mCheckForDrag = null;
            NotificationStackScrollLayout.this.mCurrIconRow = null;
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            resetExposedGearView(true, false);
            if (currView instanceof ExpandableNotificationRow) {
                NotificationStackScrollLayout.this.mCurrIconRow = ((ExpandableNotificationRow) currView).getSettingsRow();
                NotificationStackScrollLayout.this.mCurrIconRow.setGearListener(NotificationStackScrollLayout.this);
            }
        }

        public void onMoveUpdate(View view, float translation, float delta) {
            boolean z = false;
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            if (NotificationStackScrollLayout.this.mCurrIconRow != null) {
                boolean locationChange;
                NotificationStackScrollLayout.this.mCurrIconRow.setSnapping(false);
                if (isTowardsGear(translation, this.mGearSnappedTo ? this.mGearSnappedOnLeft : NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft())) {
                    locationChange = false;
                } else {
                    locationChange = NotificationStackScrollLayout.this.mCurrIconRow.isIconLocationChange(translation);
                }
                if (locationChange) {
                    setSnappedToGear(false);
                    if (this.mHandler.hasCallbacks(this.mCheckForDrag)) {
                        NotificationStackScrollLayout.this.mCurrIconRow.setGearAlpha(0.0f);
                        NotificationSettingsIconRow -get2 = NotificationStackScrollLayout.this.mCurrIconRow;
                        if (translation > 0.0f) {
                            z = true;
                        }
                        -get2.setIconLocation(z);
                    } else {
                        this.mCheckForDrag = null;
                    }
                }
            }
            boolean areGutsExposed;
            if (view instanceof ExpandableNotificationRow) {
                areGutsExposed = ((ExpandableNotificationRow) view).areGutsExposed();
            } else {
                areGutsExposed = false;
            }
            if (!NotificationStackScrollLayout.isPinnedHeadsUp(view) && !r0) {
                checkForDrag();
            }
        }

        public void dismissChild(View view, float velocity, boolean useAccelerateInterpolator) {
            HwLog.i("StackScroller", "dismissChild: " + velocity);
            super.dismissChild(view, velocity, useAccelerateInterpolator);
            if (NotificationStackScrollLayout.this.mIsExpanded) {
                NotificationStackScrollLayout.this.handleChildDismissed(view);
            }
            handleGearCoveredOrDismissed();
        }

        public void snapChild(View animView, float targetLeft, float velocity) {
            HwLog.i("StackScroller", "snapChild: " + velocity);
            super.snapChild(animView, targetLeft, velocity);
            NotificationStackScrollLayout.this.onDragCancelled(animView);
            if (targetLeft == 0.0f) {
                handleGearCoveredOrDismissed();
            }
        }

        private void handleGearCoveredOrDismissed() {
            HwLog.i("StackScroller", "handleGearCoveredOrDismissed");
            cancelCheckForDrag();
            setSnappedToGear(false);
            if (NotificationStackScrollLayout.this.mGearExposedView != null && NotificationStackScrollLayout.this.mGearExposedView == NotificationStackScrollLayout.this.mTranslatingParentView) {
                NotificationStackScrollLayout.this.mGearExposedView = null;
            }
        }

        public boolean handleUpEvent(MotionEvent ev, View animView, float velocity, float translation) {
            boolean z = false;
            if (NotificationStackScrollLayout.this.mCurrIconRow == null) {
                cancelCheckForDrag();
                return false;
            }
            boolean gestureTowardsGear = isTowardsGear(velocity, NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft());
            boolean gestureFastEnough = Math.abs(velocity) > getEscapeVelocity();
            if (this.mGearSnappedTo && NotificationStackScrollLayout.this.mCurrIconRow.isVisible()) {
                if (this.mGearSnappedOnLeft == NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft()) {
                    boolean coveringGear = Math.abs(getTranslation(animView)) <= getSpaceForGear(animView) * 0.6f;
                    if (gestureTowardsGear || coveringGear) {
                        snapChild(animView, 0.0f, velocity);
                    } else if (isDismissGesture(ev)) {
                        if (!swipedFastEnough()) {
                            z = true;
                        }
                        dismissChild(animView, velocity, z);
                    } else {
                        snapToGear(animView, velocity);
                    }
                } else if ((gestureFastEnough || !swipedEnoughToShowGear(animView)) && (!gestureTowardsGear || swipedFarEnough())) {
                    dismissOrSnapBack(animView, velocity, ev);
                } else {
                    snapToGear(animView, velocity);
                }
            } else if ((gestureFastEnough || !swipedEnoughToShowGear(animView)) && !gestureTowardsGear) {
                dismissOrSnapBack(animView, velocity, ev);
            } else {
                snapToGear(animView, velocity);
            }
            return true;
        }

        private void dismissOrSnapBack(View animView, float velocity, MotionEvent ev) {
            if (isDismissGesture(ev)) {
                dismissChild(animView, velocity, !swipedFastEnough());
            } else {
                snapChild(animView, 0.0f, velocity);
            }
        }

        private void snapToGear(View animView, float velocity) {
            float target;
            float snapBackThreshold = getSpaceForGear(animView);
            if (NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft()) {
                target = snapBackThreshold;
            } else {
                target = -snapBackThreshold;
            }
            NotificationStackScrollLayout.this.mGearExposedView = NotificationStackScrollLayout.this.mTranslatingParentView;
            if (animView != null && (animView instanceof ExpandableNotificationRow)) {
                MetricsLogger.action(NotificationStackScrollLayout.this.mContext, 332, ((ExpandableNotificationRow) animView).getStatusBarNotification().getPackageName());
                if (SystemUiUtil.isMarketPlaceSbn(((ExpandableNotificationRow) animView).getStatusBarNotification())) {
                    resetExposedGearView(true, true);
                    return;
                }
            }
            if (NotificationStackScrollLayout.this.mCurrIconRow != null) {
                NotificationStackScrollLayout.this.mCurrIconRow.setSnapping(true);
                setSnappedToGear(true);
            }
            NotificationStackScrollLayout.this.onDragCancelled(animView);
            if (NotificationStackScrollLayout.this.mPhoneStatusBar.getBarState() == 1) {
                this.mHandler.removeCallbacks(this.mFalsingCheck);
                this.mHandler.postDelayed(this.mFalsingCheck, 4000);
            }
            super.snapChild(animView, target, velocity);
        }

        private boolean swipedEnoughToShowGear(View animView) {
            boolean z = true;
            if (NotificationStackScrollLayout.this.mTranslatingParentView == null) {
                return false;
            }
            float snapBackThreshold = getSpaceForGear(animView) * (NotificationStackScrollLayout.this.canChildBeDismissed(animView) ? 0.4f : 0.2f);
            float translation = getTranslation(animView);
            if (translation > 0.0f) {
            }
            float absTrans = Math.abs(translation);
            float notiThreshold = getSize(NotificationStackScrollLayout.this.mTranslatingParentView) * 0.4f;
            if (!NotificationStackScrollLayout.this.mCurrIconRow.isVisible()) {
                z = false;
            } else if (NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft()) {
                if (translation <= snapBackThreshold || translation > notiThreshold) {
                    z = false;
                }
            } else if (translation >= (-snapBackThreshold) || translation < (-notiThreshold)) {
                z = false;
            }
            return z;
        }

        public Animator getViewTranslationAnimator(View v, float target, AnimatorUpdateListener listener) {
            if (v instanceof ExpandableNotificationRow) {
                return ((ExpandableNotificationRow) v).getTranslateViewAnimator(target, listener);
            }
            return super.getViewTranslationAnimator(v, target, listener);
        }

        public void setTranslation(View v, float translate) {
            ((ExpandableView) v).setTranslation(translate);
        }

        public float getTranslation(View v) {
            return ((ExpandableView) v).getTranslation();
        }

        public void closeControlsIfOutsideTouch(MotionEvent ev) {
            View guts = NotificationStackScrollLayout.this.mPhoneStatusBar.getExposedGuts();
            View view = null;
            int height = 0;
            if (guts != null) {
                view = guts;
                height = guts.getActualHeight();
            } else if (!(NotificationStackScrollLayout.this.mCurrIconRow == null || !NotificationStackScrollLayout.this.mCurrIconRow.isVisible() || NotificationStackScrollLayout.this.mTranslatingParentView == null)) {
                view = NotificationStackScrollLayout.this.mTranslatingParentView;
                height = ((ExpandableView) NotificationStackScrollLayout.this.mTranslatingParentView).getActualHeight();
            }
            if (view != null) {
                int rx = (int) ev.getRawX();
                int ry = (int) ev.getRawY();
                NotificationStackScrollLayout.this.getLocationOnScreen(NotificationStackScrollLayout.this.mTempInt2);
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                int x = location[0] - NotificationStackScrollLayout.this.mTempInt2[0];
                int y = location[1] - NotificationStackScrollLayout.this.mTempInt2[1];
                if (!new Rect(x, y, view.getWidth() + x, y + height).contains(rx, ry)) {
                    NotificationStackScrollLayout.this.mPhoneStatusBar.dismissPopups(-1, -1, true, true);
                }
            }
        }

        private boolean isTowardsGear(float velocity, boolean onLeft) {
            boolean z = true;
            if (NotificationStackScrollLayout.this.mCurrIconRow == null) {
                return false;
            }
            if (!NotificationStackScrollLayout.this.mCurrIconRow.isVisible()) {
                z = false;
            } else if ((!onLeft || velocity > 0.0f) && (onLeft || velocity < 0.0f)) {
                z = false;
            }
            return z;
        }

        private void setSnappedToGear(boolean snapped) {
            boolean isIconOnLeft;
            boolean z = false;
            if (NotificationStackScrollLayout.this.mCurrIconRow != null) {
                isIconOnLeft = NotificationStackScrollLayout.this.mCurrIconRow.isIconOnLeft();
            } else {
                isIconOnLeft = false;
            }
            this.mGearSnappedOnLeft = isIconOnLeft;
            if (snapped && NotificationStackScrollLayout.this.mCurrIconRow != null) {
                z = true;
            }
            this.mGearSnappedTo = z;
        }

        private float getSpaceForGear(View view) {
            if (view instanceof ExpandableNotificationRow) {
                return ((ExpandableNotificationRow) view).getSpaceForGear();
            }
            return 0.0f;
        }

        private void checkForDrag() {
            if (this.mCheckForDrag == null || !this.mHandler.hasCallbacks(this.mCheckForDrag)) {
                this.mCheckForDrag = new CheckForDrag();
                this.mHandler.postDelayed(this.mCheckForDrag, 60);
            }
        }

        private void cancelCheckForDrag() {
            if (NotificationStackScrollLayout.this.mCurrIconRow != null) {
                NotificationStackScrollLayout.this.mCurrIconRow.cancelFadeAnimator();
            }
            this.mHandler.removeCallbacks(this.mCheckForDrag);
        }

        public void resetExposedGearView(boolean animate, boolean force) {
            if (NotificationStackScrollLayout.this.mGearExposedView != null && (force || NotificationStackScrollLayout.this.mGearExposedView != NotificationStackScrollLayout.this.mTranslatingParentView)) {
                View prevGearExposedView = NotificationStackScrollLayout.this.mGearExposedView;
                if (animate) {
                    Animator anim = getViewTranslationAnimator(prevGearExposedView, 0.0f, null);
                    if (anim != null) {
                        anim.start();
                    }
                } else if (NotificationStackScrollLayout.this.mGearExposedView instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) NotificationStackScrollLayout.this.mGearExposedView).resetTranslation();
                }
                NotificationStackScrollLayout.this.mGearExposedView = null;
                this.mGearSnappedTo = false;
            }
        }
    }

    public NotificationStackScrollLayout(Context context) {
        this(context, null);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCurrentStackHeight = Integer.MAX_VALUE;
        this.mBottomInset = 0;
        this.mCurrentStackScrollState = new StackScrollState(this);
        this.mAmbientState = new AmbientState();
        this.mChildrenToAddAnimated = new HashSet();
        this.mAddedHeadsUpChildren = new ArrayList();
        this.mChildrenToRemoveAnimated = new ArrayList();
        this.mSnappedBackChildren = new ArrayList();
        this.mDragAnimPendingChildren = new ArrayList();
        this.mChildrenChangingPositions = new ArrayList();
        this.mFromMoreCardAdditions = new HashSet();
        this.mAnimationEvents = new ArrayList();
        this.mSwipedOutViews = new ArrayList();
        this.mStateAnimator = new StackStateAnimator(this);
        this.mIsExpanded = true;
        this.mIsUIBlocked = false;
        this.mIsSwipeEvent = false;
        this.mChildrenUpdater = new OnPreDrawListener() {
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateForcedScroll();
                NotificationStackScrollLayout.this.updateChildren();
                NotificationStackScrollLayout.this.mChildrenUpdateRequested = false;
                NotificationStackScrollLayout.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mTempInt2 = new int[2];
        this.mAnimationFinishedRunnables = new HashSet();
        this.mClearOverlayViewsWhenFinished = new HashSet();
        this.mHeadsUpChangeAnimations = new HashSet();
        this.mTmpList = new ArrayList();
        this.mBackgroundUpdater = new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!(NotificationStackScrollLayout.this.mNeedsAnimation || NotificationStackScrollLayout.this.mChildrenUpdateRequested)) {
                    NotificationStackScrollLayout.this.updateBackground();
                }
                return true;
            }
        };
        this.mBackgroundBounds = new Rect();
        this.mStartAnimationRect = new Rect();
        this.mEndAnimationRect = new Rect();
        this.mCurrentBounds = new Rect(-1, -1, -1, -1);
        this.mBottomAnimator = null;
        this.mTopAnimator = null;
        this.mFirstVisibleBackgroundChild = null;
        this.mLastVisibleBackgroundChild = null;
        this.mTmpSortedChildren = new ArrayList();
        this.mDimEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                NotificationStackScrollLayout.this.mDimAnimator = null;
            }
        };
        this.mDimUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationStackScrollLayout.this.setDimAmount(((Float) animation.getAnimatedValue()).floatValue());
            }
        };
        this.mShadowUpdater = new OnPreDrawListener() {
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateViewShadows();
                return true;
            }
        };
        this.mViewPositionComparator = new Comparator<ExpandableView>() {
            public int compare(ExpandableView view, ExpandableView otherView) {
                float endY = view.getTranslationY() + ((float) view.getActualHeight());
                float otherEndY = otherView.getTranslationY() + ((float) otherView.getActualHeight());
                if (endY < otherEndY) {
                    return -1;
                }
                if (endY > otherEndY) {
                    return 1;
                }
                return 0;
            }
        };
        this.mBackgroundFadeAmount = 1.0f;
        this.mReclamp = new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.mScroller.startScroll(NotificationStackScrollLayout.this.mScrollX, NotificationStackScrollLayout.this.mOwnScrollY, 0, NotificationStackScrollLayout.this.getScrollRange() - NotificationStackScrollLayout.this.mOwnScrollY);
                NotificationStackScrollLayout.this.mDontReportNextOverScroll = true;
                NotificationStackScrollLayout.this.mDontClampNextScroll = true;
                NotificationStackScrollLayout.this.postInvalidateOnAnimation();
            }
        };
        this.mExpandHelper = new ExpandHelper(getContext(), this, getResources().getDimensionPixelSize(R.dimen.notification_min_height), getResources().getDimensionPixelSize(R.dimen.notification_max_height));
        this.mExpandHelper.setEventSource(this);
        this.mExpandHelper.setScrollAdapter(this);
        this.mSwipeHelper = new NotificationSwipeHelper(0, this, getContext());
        this.mSwipeHelper.setLongPressListener(this.mLongPressListener);
        this.mStackScrollAlgorithm = new StackScrollAlgorithm(context);
        initView(context);
        setWillNotDraw(false);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    public void onGearTouched(ExpandableNotificationRow row, int x, int y) {
        if (this.mLongPressListener != null) {
            MetricsLogger.action(this.mContext, 333, row.getStatusBarNotification().getPackageName());
            if (row instanceof ExpandableNotificationRow) {
                BDReporter.e(this.mContext, 353, "pkg:" + row.getStatusBarNotification().getPackageName());
            }
            this.mLongPressListener.onLongPress(row, x, y);
        }
    }

    public void onSettingsIconRowReset(ExpandableNotificationRow row) {
        if (this.mTranslatingParentView != null && row == this.mTranslatingParentView) {
            this.mSwipeHelper.setSnappedToGear(false);
            this.mGearExposedView = null;
            this.mTranslatingParentView = null;
        }
    }

    protected void onDraw(Canvas canvas) {
    }

    private void updateBackgroundDimming() {
        invalidate();
    }

    private void initView(Context context) {
        this.mScroller = new OverScroller(getContext());
        setDescendantFocusability(262144);
        setClipChildren(false);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mCollapsedSize = context.getResources().getDimensionPixelSize(R.dimen.notification_min_height);
        this.mBottomStackPeekSize = context.getResources().getDimensionPixelSize(R.dimen.bottom_stack_peek_amount);
        this.mStackScrollAlgorithm.initView(context);
        this.mPaddingBetweenElements = Math.max(1, context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height));
        this.mIncreasedPaddingBetweenElements = context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mBottomStackSlowDownHeight = this.mStackScrollAlgorithm.getBottomStackSlowDownLength();
        this.mMinTopOverScrollToEscape = (float) getResources().getDimensionPixelSize(R.dimen.min_top_overscroll_to_qs);
    }

    public void setDrawBackgroundAsSrc(boolean asSrc) {
        updateSrcDrawing();
    }

    private void updateSrcDrawing() {
        invalidate();
    }

    private void notifyHeightChangeListener(ExpandableView view) {
        if (this.mOnHeightChangedListener != null) {
            this.mOnHeightChangedListener.onHeightChanged(view, false);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        float centerX = ((float) getWidth()) / 2.0f;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            float width = (float) child.getMeasuredWidth();
            child.layout((int) (centerX - (width / 2.0f)), 0, (int) ((width / 2.0f) + centerX), (int) ((float) child.getMeasuredHeight()));
        }
        setMaxLayoutHeight(getHeight());
        updateContentHeight();
        clampScrollPosition();
        if (this.mRequestViewResizeAnimationOnLayout) {
            requestAnimationOnViewResize(null);
            this.mRequestViewResizeAnimationOnLayout = false;
        }
        requestChildrenUpdate();
        updateFirstAndLastBackgroundViews();
    }

    private void requestAnimationOnViewResize(ExpandableNotificationRow row) {
        if (!this.mAnimationsEnabled) {
            return;
        }
        if (this.mIsExpanded || (row != null && row.isPinned())) {
            this.mNeedViewResizeAnimation = true;
            this.mNeedsAnimation = true;
        }
    }

    public void updateSpeedBumpIndex(int newIndex) {
        this.mAmbientState.setSpeedBumpIndex(newIndex);
    }

    public void setChildLocationsChangedListener(OnChildLocationsChangedListener listener) {
        this.mListener = listener;
    }

    public int getChildLocation(View child) {
        StackViewState childViewState = this.mCurrentStackScrollState.getViewStateForView(child);
        if (childViewState == null) {
            return 0;
        }
        if (childViewState.gone) {
            return 64;
        }
        return childViewState.location;
    }

    private void setMaxLayoutHeight(int maxLayoutHeight) {
        this.mMaxLayoutHeight = maxLayoutHeight;
        updateAlgorithmHeightAndPadding();
    }

    private void updateAlgorithmHeightAndPadding() {
        this.mAmbientState.setLayoutHeight(getLayoutHeight());
        this.mAmbientState.setTopPadding(this.mTopPadding);
    }

    private void updateChildren() {
        updateScrollStateForAddedChildren();
        this.mAmbientState.setScrollY(this.mOwnScrollY);
        this.mStackScrollAlgorithm.getStackScrollState(this.mAmbientState, this.mCurrentStackScrollState);
        if (isCurrentlyAnimating() || this.mNeedsAnimation) {
            startAnimationToState();
        } else {
            applyCurrentState();
        }
    }

    private void updateScrollStateForAddedChildren() {
        if (!this.mChildrenToAddAnimated.isEmpty()) {
            for (int i = 0; i < getChildCount(); i++) {
                ExpandableView child = (ExpandableView) getChildAt(i);
                if (this.mChildrenToAddAnimated.contains(child)) {
                    int padding;
                    int startingPosition = getPositionInLinearLayout(child);
                    if (child.getIncreasedPaddingAmount() == 1.0f) {
                        padding = this.mIncreasedPaddingBetweenElements;
                    } else {
                        padding = this.mPaddingBetweenElements;
                    }
                    int childHeight = getIntrinsicHeight(child) + padding;
                    if (startingPosition < this.mOwnScrollY) {
                        this.mOwnScrollY += childHeight;
                    }
                }
            }
            clampScrollPosition();
        }
    }

    private void updateForcedScroll() {
        if (!(this.mForcedScroll == null || (this.mForcedScroll.hasFocus() && this.mForcedScroll.isAttachedToWindow()))) {
            this.mForcedScroll = null;
        }
        if (this.mForcedScroll != null) {
            ExpandableView expandableView = this.mForcedScroll;
            int positionInLinearLayout = getPositionInLinearLayout(expandableView);
            int outOfViewScroll = positionInLinearLayout + expandableView.getIntrinsicHeight();
            int targetScroll = Math.max(0, Math.min(targetScrollForView(expandableView, positionInLinearLayout), getScrollRange()));
            if (this.mOwnScrollY < targetScroll || outOfViewScroll < this.mOwnScrollY) {
                this.mOwnScrollY = targetScroll;
            }
        }
    }

    private void requestChildrenUpdate() {
        if (!this.mChildrenUpdateRequested) {
            getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
            this.mChildrenUpdateRequested = true;
            invalidate();
        }
    }

    private boolean isCurrentlyAnimating() {
        return this.mStateAnimator.isRunning();
    }

    private void clampScrollPosition() {
        int scrollRange = getScrollRange();
        if (scrollRange < this.mOwnScrollY) {
            this.mOwnScrollY = scrollRange;
        }
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    private void setTopPadding(int topPadding, boolean animate) {
        if (this.mTopPadding != topPadding) {
            this.mTopPadding = topPadding;
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            if (animate && this.mAnimationsEnabled && this.mIsExpanded) {
                this.mTopPaddingNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
            notifyHeightChangeListener(null);
        }
    }

    public void setStackHeight(float height) {
        boolean trackingHeadsUp;
        int normalUnfoldPositionStart;
        float paddingOffset;
        int stackHeight;
        boolean z = false;
        this.mLastSetStackHeight = height;
        if (height > 0.0f) {
            z = true;
        }
        setIsExpanded(z);
        int newStackHeight = (int) height;
        int minStackHeight = getLayoutMinHeight();
        if (this.mTrackingHeadsUp) {
            trackingHeadsUp = true;
        } else {
            trackingHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
        }
        if (trackingHeadsUp) {
            normalUnfoldPositionStart = this.mHeadsUpManager.getTopHeadsUpPinnedHeight();
        } else {
            normalUnfoldPositionStart = minStackHeight;
        }
        if (((float) (newStackHeight - this.mTopPadding)) - this.mTopPaddingOverflow >= ((float) normalUnfoldPositionStart) || getNotGoneChildCount() == 0) {
            paddingOffset = this.mTopPaddingOverflow;
            stackHeight = newStackHeight;
        } else {
            int translationY = newStackHeight - normalUnfoldPositionStart;
            paddingOffset = (float) (translationY - this.mTopPadding);
            stackHeight = (int) (height - ((float) (translationY - this.mTopPadding)));
        }
        if (stackHeight != this.mCurrentStackHeight) {
            this.mCurrentStackHeight = stackHeight;
            updateAlgorithmHeightAndPadding();
            requestChildrenUpdate();
        }
        setStackTranslation(paddingOffset);
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    private void setStackTranslation(float stackTranslation) {
        if (stackTranslation != this.mStackTranslation) {
            this.mStackTranslation = stackTranslation;
            this.mAmbientState.setStackTranslation(stackTranslation);
            requestChildrenUpdate();
        }
    }

    private int getLayoutHeight() {
        return Math.min(this.mMaxLayoutHeight, this.mCurrentStackHeight);
    }

    public int getFirstItemMinHeight() {
        ExpandableView firstChild = getFirstChildNotGone();
        return firstChild != null ? firstChild.getMinHeight() : this.mCollapsedSize;
    }

    public int getBottomStackPeekSize() {
        return this.mBottomStackPeekSize;
    }

    public int getBottomStackSlowDownHeight() {
        return this.mBottomStackSlowDownHeight;
    }

    public void setLongPressListener(LongPressListener listener, LongPressListener gutsListener) {
        this.mSwipeHelper.setLongPressListener(gutsListener);
        this.mLongPressListener = listener;
    }

    public void setQsContainer(ViewGroup qsContainer) {
        this.mQsContainer = (QSContainer) qsContainer;
    }

    public void onChildDismissed(View v) {
        HwLog.i("StackScroller", "onChildDismissed: " + v);
        ExpandableNotificationRow row = (ExpandableNotificationRow) v;
        if (!row.isDismissed()) {
            handleChildDismissed(v);
        }
        ViewGroup transientContainer = row.getTransientContainer();
        if (transientContainer != null) {
            transientContainer.removeTransientView(v);
        }
    }

    private void handleChildDismissed(View v) {
        HwLog.i("StackScroller", "handleChildDismissed");
        if (this.mDismissAllInProgress) {
            if (v instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) v).setDismissed(true, false);
            }
            return;
        }
        setSwipingInProgress(false);
        if (this.mDragAnimPendingChildren.contains(v)) {
            this.mDragAnimPendingChildren.remove(v);
        }
        this.mSwipedOutViews.add(v);
        this.mAmbientState.onDragFinished(v);
        updateContinuousShadowDrawing();
        if (v instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) v;
            if (row.isHeadsUp()) {
                this.mHeadsUpManager.addSwipedOutNotification(row.getStatusBarNotification().getKey());
            }
        }
        performDismiss(v, this.mGroupManager, false);
        this.mFalsingManager.onNotificationDismissed();
        if (this.mFalsingManager.shouldEnforceBouncer()) {
            this.mPhoneStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
    }

    public static void performDismiss(View v, NotificationGroupManager groupManager, boolean fromAccessibility) {
        if (v instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) v;
            if (groupManager.isOnlyChildInGroup(row.getStatusBarNotification())) {
                ExpandableNotificationRow groupSummary = groupManager.getLogicalGroupSummary(row.getStatusBarNotification());
                if (groupSummary != null && groupSummary.isClearable()) {
                    performDismiss(groupSummary, groupManager, fromAccessibility);
                }
            }
            row.setDismissed(true, fromAccessibility);
        }
        View veto = v.findViewById(R.id.veto);
        if (veto != null && veto.getVisibility() != 8) {
            veto.performClick();
        }
    }

    public void onChildSnappedBack(View animView, float targetLeft) {
        this.mAmbientState.onDragFinished(animView);
        updateContinuousShadowDrawing();
        if (this.mDragAnimPendingChildren.contains(animView)) {
            this.mDragAnimPendingChildren.remove(animView);
        } else {
            if (this.mAnimationsEnabled) {
                this.mSnappedBackChildren.add(animView);
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
        }
        if (this.mCurrIconRow != null && targetLeft == 0.0f) {
            this.mCurrIconRow.resetState();
            this.mCurrIconRow = null;
        }
    }

    public boolean updateSwipeProgress(View animView, boolean dismissable, float swipeProgress) {
        if (!this.mIsExpanded && isPinnedHeadsUp(animView) && canChildBeDismissed(animView)) {
            this.mScrimController.setTopHeadsUpDragAmount(animView, Math.min(Math.abs((swipeProgress / 2.0f) - 1.0f), 1.0f));
        }
        return true;
    }

    public void onBeginDrag(View v) {
        HwLog.i("StackScroller", "onBeginDrag: " + v);
        this.mFalsingManager.onNotificatonStartDismissing();
        setSwipingInProgress(true);
        this.mAmbientState.onBeginDrag(v);
        updateContinuousShadowDrawing();
        if (this.mAnimationsEnabled && (this.mIsExpanded || !isPinnedHeadsUp(v))) {
            this.mDragAnimPendingChildren.add(v);
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    public static boolean isPinnedHeadsUp(View v) {
        boolean z = false;
        if (!(v instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow row = (ExpandableNotificationRow) v;
        if (row.isHeadsUp()) {
            z = row.isPinned();
        }
        return z;
    }

    private boolean isHeadsUp(View v) {
        if (v instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) v).isHeadsUp();
        }
        return false;
    }

    public void onDragCancelled(View v) {
        HwLog.i("StackScroller", "onDragCancelled: " + v);
        this.mFalsingManager.onNotificatonStopDismissing();
        setSwipingInProgress(false);
    }

    public float getFalsingThresholdFactor() {
        return this.mPhoneStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
    }

    public View getChildAtPosition(MotionEvent ev) {
        View child = getChildAtPosition(ev.getX(), ev.getY());
        if (child instanceof TrafficPanelViewContent) {
            return null;
        }
        if (child instanceof ExpandableNotificationRow) {
            View parent = ((ExpandableNotificationRow) child).getNotificationParent();
            if (parent != null && parent.areChildrenExpanded()) {
                if (!(parent.areGutsExposed() || this.mGearExposedView == parent)) {
                    if (parent.getNotificationChildren().size() == 1 && parent.isClearable()) {
                    }
                }
                child = parent;
            }
        }
        return child;
    }

    public ExpandableView getClosestChildAtRawPosition(float touchX, float touchY) {
        getLocationOnScreen(this.mTempInt2);
        float localTouchY = touchY - ((float) this.mTempInt2[1]);
        ExpandableView closestChild = null;
        float minDist = Float.MAX_VALUE;
        int count = getChildCount();
        for (int childIdx = 0; childIdx < count; childIdx++) {
            ExpandableView slidingChild = (ExpandableView) getChildAt(childIdx);
            if (!(slidingChild.getVisibility() == 8 || (slidingChild instanceof StackScrollerDecorView))) {
                float childTop = slidingChild.getTranslationY();
                float dist = Math.min(Math.abs((childTop + ((float) slidingChild.getClipTopAmount())) - localTouchY), Math.abs((childTop + ((float) slidingChild.getActualHeight())) - localTouchY));
                if (dist < minDist) {
                    closestChild = slidingChild;
                    minDist = dist;
                }
            }
        }
        return closestChild;
    }

    public ExpandableView getChildAtRawPosition(float touchX, float touchY) {
        getLocationOnScreen(this.mTempInt2);
        return getChildAtPosition(touchX - ((float) this.mTempInt2[0]), touchY - ((float) this.mTempInt2[1]));
    }

    public ExpandableView getChildAtPosition(float touchX, float touchY) {
        int count = getChildCount();
        for (int childIdx = 0; childIdx < count; childIdx++) {
            ExpandableView slidingChild = (ExpandableView) getChildAt(childIdx);
            if (!(slidingChild.getVisibility() == 8 || (slidingChild instanceof StackScrollerDecorView))) {
                float childTop = slidingChild.getTranslationY();
                float top = childTop + ((float) slidingChild.getClipTopAmount());
                float bottom = childTop + ((float) slidingChild.getActualHeight());
                int right = getWidth();
                if (touchY >= top && touchY <= bottom && touchX >= 0.0f && touchX <= ((float) right)) {
                    if (!(slidingChild instanceof ExpandableNotificationRow)) {
                        return slidingChild;
                    }
                    ExpandableNotificationRow row = (ExpandableNotificationRow) slidingChild;
                    if (this.mIsExpanded || !row.isHeadsUp() || !row.isPinned() || this.mHeadsUpManager.getTopEntry().entry.row == row || this.mGroupManager.getGroupSummary(this.mHeadsUpManager.getTopEntry().entry.row.getStatusBarNotification()) == row) {
                        return row.getViewAtPosition(touchY - childTop);
                    }
                }
            }
        }
        return null;
    }

    public boolean canChildBeExpanded(View v) {
        if ((v instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) v).isExpandable() && !((ExpandableNotificationRow) v).areGutsExposed()) {
            return this.mIsExpanded || !((ExpandableNotificationRow) v).isPinned();
        } else {
            return false;
        }
    }

    public void setUserExpandedChild(View v, boolean userExpanded) {
        if (v instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) v;
            row.setUserExpanded(userExpanded, true);
            row.onExpandedByGesture(userExpanded);
        }
    }

    public void setExpansionCancelled(View v) {
        if (v instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) v).setGroupExpansionChanging(false);
        }
    }

    public void setUserLockedChild(View v, boolean userLocked) {
        if (v instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) v).setUserLocked(userLocked);
        }
        removeLongPressCallback();
        requestDisallowInterceptTouchEvent(true);
    }

    public void expansionStateChanged(boolean isExpanding) {
        this.mExpandingNotification = isExpanding;
        if (!this.mExpandedInThisMotion) {
            this.mMaxScrollAfterExpand = this.mOwnScrollY;
            this.mExpandedInThisMotion = true;
        }
    }

    public int getMaxExpandHeight(ExpandableView view) {
        int height = 0;
        int maxContentHeight = view.getMaxContentHeight();
        if (!view.isSummaryWithChildren()) {
            return maxContentHeight;
        }
        this.mGroupExpandedForMeasure = true;
        ExpandableNotificationRow row = (ExpandableNotificationRow) view;
        this.mGroupManager.toggleGroupExpansion(row.getStatusBarNotification());
        row.setForceUnlocked(true);
        this.mAmbientState.setLayoutHeight(this.mMaxLayoutHeight);
        this.mStackScrollAlgorithm.getStackScrollState(this.mAmbientState, this.mCurrentStackScrollState);
        this.mAmbientState.setLayoutHeight(getLayoutHeight());
        this.mGroupManager.toggleGroupExpansion(row.getStatusBarNotification());
        this.mGroupExpandedForMeasure = false;
        row.setForceUnlocked(false);
        StackViewState viewState = this.mCurrentStackScrollState.getViewStateForView(view);
        if (viewState != null) {
            height = viewState.height;
        }
        return Math.min(height, maxContentHeight);
    }

    public void setScrollingEnabled(boolean enable) {
        HwLog.i("StackScroller", "setScrollingEnabled: " + enable);
        this.mScrollingEnabled = enable;
    }

    public void lockScrollTo(View v) {
        if (this.mForcedScroll != v) {
            this.mForcedScroll = v;
            scrollTo(v);
        }
    }

    public boolean scrollTo(View v) {
        ExpandableView expandableView = (ExpandableView) v;
        int positionInLinearLayout = getPositionInLinearLayout(v);
        int targetScroll = targetScrollForView(expandableView, positionInLinearLayout);
        int outOfViewScroll = positionInLinearLayout + expandableView.getIntrinsicHeight();
        if (this.mOwnScrollY >= targetScroll && outOfViewScroll >= this.mOwnScrollY) {
            return false;
        }
        this.mScroller.startScroll(this.mScrollX, this.mOwnScrollY, 0, targetScroll - this.mOwnScrollY);
        this.mDontReportNextOverScroll = true;
        postInvalidateOnAnimation();
        return true;
    }

    private int targetScrollForView(ExpandableView v, int positionInLinearLayout) {
        return (((v.getIntrinsicHeight() + positionInLinearLayout) + getImeInset()) - getHeight()) + getTopPadding();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mBottomInset = insets.getSystemWindowInsetBottom();
        if (this.mOwnScrollY > getScrollRange()) {
            removeCallbacks(this.mReclamp);
            postDelayed(this.mReclamp, 50);
        } else if (this.mForcedScroll != null) {
            scrollTo(this.mForcedScroll);
        }
        return insets;
    }

    public void setExpandingEnabled(boolean enable) {
        this.mExpandHelper.setEnabled(enable);
    }

    private boolean isScrollingEnabled() {
        return this.mScrollingEnabled;
    }

    public boolean canChildBeDismissed(View v) {
        return StackScrollAlgorithm.canChildBeDismissed(v);
    }

    public boolean isAntiFalsingNeeded() {
        return this.mPhoneStatusBar.getBarState() == 1;
    }

    private void setSwipingInProgress(boolean isSwiped) {
        HwLog.i("StackScroller", "setSwipingInProgress: " + isSwiped);
        this.mSwipingInProgress = isSwiped;
        if (isSwiped) {
            requestDisallowInterceptTouchEvent(true);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mSwipeHelper.setDensityScale(getResources().getDisplayMetrics().density);
        this.mSwipeHelper.setPagingTouchSlop((float) ViewConfiguration.get(getContext()).getScaledPagingTouchSlop());
        initView(getContext());
    }

    public void dismissViewAnimated(View child, Runnable endRunnable, int delay, long duration) {
        this.mSwipeHelper.dismissChild(child, 0.0f, endRunnable, (long) delay, true, duration, true);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("StackScroller", "onTouchEvent: " + ev + ", mSwipingInProgress=" + this.mSwipingInProgress + ", mOnlyScrollingInThisMotion=" + this.mOnlyScrollingInThisMotion + ", mExpandingNotification=" + this.mExpandingNotification + ", mDisallowDismissInThisMotion=" + this.mDisallowDismissInThisMotion + ", mExpandedInThisMotion=" + this.mExpandedInThisMotion + ", mIsExpanded=" + this.mIsExpanded + ", mIsSwipeEvent=" + this.mIsSwipeEvent);
        }
        if (this.mQsContainer.isCustomizing()) {
            if (SystemUiUtil.allowLogEvent(ev)) {
                HwLog.i("StackScroller", "onTouchEvent: isCustomizing");
            }
            return false;
        } else if (ev.getY() > ((float) this.mQsContainer.getHeight()) || this.mIsSwipeEvent || this.mSwipingInProgress) {
            boolean isCancelOrUp = ev.getActionMasked() != 3 ? ev.getActionMasked() == 1 : true;
            handleEmptySpaceClick(ev);
            boolean expandWantsIt = false;
            if (!(this.mSwipingInProgress || this.mOnlyScrollingInThisMotion)) {
                if (isCancelOrUp) {
                    this.mExpandHelper.onlyObserveMovements(false);
                }
                boolean wasExpandingBefore = this.mExpandingNotification;
                expandWantsIt = this.mExpandHelper.onTouchEvent(ev);
                if (this.mExpandedInThisMotion && !this.mExpandingNotification && wasExpandingBefore && !this.mDisallowScrollingInThisMotion) {
                    dispatchDownEventToScroller(ev);
                }
            }
            boolean scrollerWantsIt = false;
            if (!(!this.mIsExpanded || this.mSwipingInProgress || this.mExpandingNotification || this.mDisallowScrollingInThisMotion)) {
                scrollerWantsIt = onScrollTouch(ev);
            }
            boolean horizontalSwipeWantsIt = false;
            if (!(this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion)) {
                horizontalSwipeWantsIt = this.mSwipeHelper.onTouchEvent(ev);
            }
            if (ev.getAction() == 1 || ev.getAction() == 3) {
                this.mIsSwipeEvent = false;
            }
            if (horizontalSwipeWantsIt && (ev.getAction() == 0 || ev.getAction() == 2)) {
                this.mIsSwipeEvent = true;
            }
            if (!(horizontalSwipeWantsIt || r3 || r0)) {
                z = super.onTouchEvent(ev);
            }
            return z;
        } else {
            if (SystemUiUtil.allowLogEvent(ev)) {
                HwLog.i("StackScroller", "onTouchEvent: y=" + ev.getY() + ", h=" + this.mQsContainer.getHeight());
            }
            return false;
        }
    }

    private void dispatchDownEventToScroller(MotionEvent ev) {
        MotionEvent downEvent = MotionEvent.obtain(ev);
        downEvent.setAction(0);
        onScrollTouch(downEvent);
        downEvent.recycle();
    }

    private boolean onScrollTouch(MotionEvent ev) {
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("StackScroller", "onScrollTouch: " + ev);
        }
        if (!isScrollingEnabled()) {
            if (SystemUiUtil.allowLogEvent(ev)) {
                HwLog.i("StackScroller", "onScrollTouch: scroll disabled");
            }
            return false;
        } else if (ev.getY() < ((float) this.mQsContainer.getBottom())) {
            if (SystemUiUtil.allowLogEvent(ev)) {
                HwLog.i("StackScroller", "onScrollTouch: y=" + ev.getY() + ", bottom=" + this.mQsContainer.getBottom() + ", top=" + this.mQsContainer.getTop());
            }
            return false;
        } else {
            this.mForcedScroll = null;
            initVelocityTrackerIfNotExists();
            this.mVelocityTracker.addMovement(ev);
            switch (ev.getAction() & 255) {
                case 0:
                    if (getChildCount() != 0 && isInContentBounds(ev)) {
                        setIsBeingDragged(!this.mScroller.isFinished());
                        if (!this.mScroller.isFinished()) {
                            this.mScroller.forceFinished(true);
                        }
                        this.mLastMotionY = (int) ev.getY();
                        this.mDownX = (int) ev.getX();
                        this.mActivePointerId = ev.getPointerId(0);
                        break;
                    }
                    return false;
                case 1:
                    if (this.mIsBeingDragged) {
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                        int initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                        if (shouldOverScrollFling(initialVelocity)) {
                            onOverScrollFling(true, initialVelocity);
                        } else if (getChildCount() > 0) {
                            if (Math.abs(initialVelocity) > this.mMinimumVelocity) {
                                if (getCurrentOverScrollAmount(true) == 0.0f || initialVelocity > 0) {
                                    fling(-initialVelocity);
                                } else {
                                    onOverScrollFling(false, initialVelocity);
                                }
                            } else if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                                postInvalidateOnAnimation();
                            }
                        }
                        this.mActivePointerId = -1;
                        endDrag();
                        break;
                    }
                    break;
                case 2:
                    int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                    if (activePointerIndex != -1) {
                        int y = (int) ev.getY(activePointerIndex);
                        int deltaY = this.mLastMotionY - y;
                        int xDiff = Math.abs(((int) ev.getX(activePointerIndex)) - this.mDownX);
                        int yDiff = Math.abs(deltaY);
                        if (!this.mIsBeingDragged && yDiff > this.mTouchSlop && yDiff > xDiff) {
                            setIsBeingDragged(true);
                            deltaY = deltaY > 0 ? deltaY - this.mTouchSlop : deltaY + this.mTouchSlop;
                        }
                        if (this.mIsBeingDragged) {
                            float scrollAmount;
                            this.mLastMotionY = y;
                            int range = getScrollRange();
                            if (this.mExpandedInThisMotion) {
                                range = Math.min(range, this.mMaxScrollAfterExpand);
                            }
                            if (deltaY < 0) {
                                scrollAmount = overScrollDown(deltaY);
                            } else {
                                scrollAmount = overScrollUp(deltaY, range);
                            }
                            if (scrollAmount != 0.0f) {
                                overScrollBy(0, (int) scrollAmount, 0, this.mOwnScrollY, 0, range, 0, getHeight() / 2, true);
                                break;
                            }
                        }
                    }
                    Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                    break;
                    break;
                case 3:
                    if (this.mIsBeingDragged && getChildCount() > 0) {
                        if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                            postInvalidateOnAnimation();
                        }
                        this.mActivePointerId = -1;
                        endDrag();
                        break;
                    }
                case 5:
                    int index = ev.getActionIndex();
                    this.mLastMotionY = (int) ev.getY(index);
                    this.mDownX = (int) ev.getX(index);
                    this.mActivePointerId = ev.getPointerId(index);
                    break;
                case 6:
                    onSecondaryPointerUp(ev);
                    try {
                        this.mLastMotionY = (int) ev.getY(ev.findPointerIndex(this.mActivePointerId));
                        this.mDownX = (int) ev.getX(ev.findPointerIndex(this.mActivePointerId));
                        break;
                    } catch (IllegalArgumentException e) {
                        Log.e("StackScroller", "findPointerIndex = -1");
                        break;
                    }
            }
            return true;
        }
    }

    private void onOverScrollFling(boolean open, int initialVelocity) {
        if (this.mOverscrollTopChangedListener != null) {
            this.mOverscrollTopChangedListener.flingTopOverscroll((float) initialVelocity, open);
        }
        this.mDontReportNextOverScroll = true;
        setOverScrollAmount(0.0f, true, false);
    }

    private float overScrollUp(int deltaY, int range) {
        deltaY = Math.max(deltaY, 0);
        float currentTopAmount = getCurrentOverScrollAmount(true);
        float newTopAmount = currentTopAmount - ((float) deltaY);
        if (currentTopAmount > 0.0f) {
            setOverScrollAmount(newTopAmount, true, false);
        }
        float scrollAmount = newTopAmount < 0.0f ? -newTopAmount : 0.0f;
        float newScrollY = ((float) this.mOwnScrollY) + scrollAmount;
        if (newScrollY <= ((float) range)) {
            return scrollAmount;
        }
        if (!this.mExpandedInThisMotion) {
            setOverScrolledPixels((getCurrentOverScrolledPixels(false) + newScrollY) - ((float) range), false, false);
        }
        this.mOwnScrollY = range;
        return 0.0f;
    }

    private float overScrollDown(int deltaY) {
        deltaY = Math.min(deltaY, 0);
        float currentBottomAmount = getCurrentOverScrollAmount(false);
        float newBottomAmount = currentBottomAmount + ((float) deltaY);
        if (currentBottomAmount > 0.0f) {
            setOverScrollAmount(newBottomAmount, false, false);
        }
        float scrollAmount = newBottomAmount < 0.0f ? newBottomAmount : 0.0f;
        float newScrollY = ((float) this.mOwnScrollY) + scrollAmount;
        if (newScrollY >= 0.0f) {
            return scrollAmount;
        }
        setOverScrolledPixels(getCurrentOverScrolledPixels(true) - newScrollY, true, false);
        this.mOwnScrollY = 0;
        return 0.0f;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int newPointerIndex = 0;
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            if (pointerIndex == 0) {
                newPointerIndex = 1;
            }
            this.mLastMotionY = (int) ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    public void setFinishScrollingCallback(Runnable runnable) {
        this.mFinishScrollingCallback = runnable;
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int oldX = this.mScrollX;
            int oldY = this.mOwnScrollY;
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                int range = getScrollRange();
                if (y >= 0 || oldY < 0) {
                    if (y > range && oldY <= range) {
                    }
                    if (this.mDontClampNextScroll) {
                        range = Math.max(range, oldY);
                    }
                    overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range, 0, (int) this.mMaxOverScroll, false);
                    onScrollChanged(this.mScrollX, this.mOwnScrollY, oldX, oldY);
                }
                float currVelocity = this.mScroller.getCurrVelocity();
                if (currVelocity >= ((float) this.mMinimumVelocity)) {
                    this.mMaxOverScroll = (Math.min(Math.abs(currVelocity), (float) this.mMaximumVelocity) / 1000.0f) * ((float) this.mOverflingDistance);
                }
                if (this.mDontClampNextScroll) {
                    range = Math.max(range, oldY);
                }
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range, 0, (int) this.mMaxOverScroll, false);
                onScrollChanged(this.mScrollX, this.mOwnScrollY, oldX, oldY);
            }
            postInvalidateOnAnimation();
            return;
        }
        this.mDontClampNextScroll = false;
        if (this.mFinishScrollingCallback != null) {
            this.mFinishScrollingCallback.run();
        }
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int newScrollY = scrollY + deltaY;
        int top = -maxOverScrollY;
        int bottom = maxOverScrollY + scrollRangeY;
        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }
        onOverScrolled(0, newScrollY, false, clampedY);
        return clampedY;
    }

    public void setOverScrolledPixels(float numPixels, boolean onTop, boolean animate) {
        setOverScrollAmount(getRubberBandFactor(onTop) * numPixels, onTop, animate, true);
    }

    public void setOverScrollAmount(float amount, boolean onTop, boolean animate) {
        setOverScrollAmount(amount, onTop, animate, true);
    }

    public void setOverScrollAmount(float amount, boolean onTop, boolean animate, boolean cancelAnimators) {
        setOverScrollAmount(amount, onTop, animate, cancelAnimators, isRubberbanded(onTop));
    }

    public void setOverScrollAmount(float amount, boolean onTop, boolean animate, boolean cancelAnimators, boolean isRubberbanded) {
        if (cancelAnimators) {
            this.mStateAnimator.cancelOverScrollAnimators(onTop);
        }
        setOverScrollAmountInternal(amount, onTop, animate, isRubberbanded);
    }

    private void setOverScrollAmountInternal(float amount, boolean onTop, boolean animate, boolean isRubberbanded) {
        amount = Math.max(0.0f, amount);
        if (animate) {
            this.mStateAnimator.animateOverScrollToAmount(amount, onTop, isRubberbanded);
            return;
        }
        setOverScrolledPixels(amount / getRubberBandFactor(onTop), onTop);
        this.mAmbientState.setOverScrollAmount(amount, onTop);
        if (onTop) {
            notifyOverscrollTopListener(amount, isRubberbanded);
        }
        requestChildrenUpdate();
    }

    private void notifyOverscrollTopListener(float amount, boolean isRubberbanded) {
        boolean z;
        ExpandHelper expandHelper = this.mExpandHelper;
        if (amount > 1.0f) {
            z = true;
        } else {
            z = false;
        }
        expandHelper.onlyObserveMovements(z);
        if (this.mDontReportNextOverScroll) {
            this.mDontReportNextOverScroll = false;
            return;
        }
        if (this.mOverscrollTopChangedListener != null) {
            this.mOverscrollTopChangedListener.onOverscrollTopChanged(amount, isRubberbanded);
        }
    }

    public void setOverscrollTopChangedListener(OnOverscrollTopChangedListener overscrollTopChangedListener) {
        this.mOverscrollTopChangedListener = overscrollTopChangedListener;
    }

    public float getCurrentOverScrollAmount(boolean top) {
        return this.mAmbientState.getOverScrollAmount(top);
    }

    public float getCurrentOverScrolledPixels(boolean top) {
        return top ? this.mOverScrolledTopPixels : this.mOverScrolledBottomPixels;
    }

    private void setOverScrolledPixels(float amount, boolean onTop) {
        if (onTop) {
            this.mOverScrolledTopPixels = amount;
        } else {
            this.mOverScrolledBottomPixels = amount;
        }
    }

    private void customScrollTo(int y) {
        this.mOwnScrollY = y;
        updateChildren();
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.mScroller.isFinished()) {
            customScrollTo(scrollY);
            scrollTo(scrollX, this.mScrollY);
            return;
        }
        int oldX = this.mScrollX;
        int oldY = this.mOwnScrollY;
        this.mScrollX = scrollX;
        this.mOwnScrollY = scrollY;
        if (clampedY) {
            springBack();
            return;
        }
        onScrollChanged(this.mScrollX, this.mOwnScrollY, oldX, oldY);
        invalidateParentIfNeeded();
        updateChildren();
        float overScrollTop = getCurrentOverScrollAmount(true);
        if (this.mOwnScrollY < 0) {
            notifyOverscrollTopListener((float) (-this.mOwnScrollY), isRubberbanded(true));
        } else {
            notifyOverscrollTopListener(overScrollTop, isRubberbanded(true));
        }
    }

    private void springBack() {
        int scrollRange = getScrollRange();
        boolean overScrolledTop = this.mOwnScrollY <= 0;
        boolean overScrolledBottom = this.mOwnScrollY >= scrollRange;
        if (overScrolledTop || overScrolledBottom) {
            boolean onTop;
            float newAmount;
            if (overScrolledTop) {
                onTop = true;
                newAmount = (float) (-this.mOwnScrollY);
                this.mOwnScrollY = 0;
                this.mDontReportNextOverScroll = true;
            } else {
                onTop = false;
                newAmount = (float) (this.mOwnScrollY - scrollRange);
                this.mOwnScrollY = scrollRange;
            }
            setOverScrollAmount(newAmount, onTop, false);
            setOverScrollAmount(0.0f, onTop, true);
            this.mScroller.forceFinished(true);
        }
    }

    private int getScrollRange() {
        int scrollRange = Math.max(0, ((getContentHeight() - this.mMaxLayoutHeight) + this.mBottomStackPeekSize) + this.mBottomStackSlowDownHeight);
        int imeInset = getImeInset();
        return scrollRange + Math.min(imeInset, Math.max(0, getContentHeight() - (getHeight() - imeInset)));
    }

    private int getImeInset() {
        return Math.max(0, this.mBottomInset - (getRootView().getHeight() - getHeight()));
    }

    public ExpandableView getFirstChildNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                return (ExpandableView) child;
            }
        }
        return null;
    }

    private View getFirstChildBelowTranlsationY(float translationY) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8 && child.getTranslationY() >= translationY) {
                return child;
            }
        }
        return null;
    }

    public View getLastChildNotGone() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                return child;
            }
        }
        return null;
    }

    public int getNotGoneChildCount() {
        int childCount = getChildCount();
        int count = 0;
        for (int i = 0; i < childCount; i++) {
            ExpandableView child = (ExpandableView) getChildAt(i);
            if (!(child.getVisibility() == 8 || child.willBeGone())) {
                count++;
            }
        }
        return count;
    }

    public int getContentHeight() {
        return this.mContentHeight;
    }

    private void updateContentHeight() {
        int height = 0;
        float previousIncreasedAmount = 0.0f;
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
                if (height != 0) {
                    height += (int) NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, Math.max(previousIncreasedAmount, increasedPaddingAmount));
                }
                previousIncreasedAmount = increasedPaddingAmount;
                height += expandableView.getIntrinsicHeight();
            }
        }
        this.mContentHeight = this.mTopPadding + height;
        updateScrollability();
    }

    private void updateScrollability() {
        boolean scrollable = getScrollRange() > 0;
        if (scrollable != this.mScrollable) {
            this.mScrollable = scrollable;
            setFocusable(scrollable);
        }
    }

    private void updateBackground() {
        if (!this.mAmbientState.isDark()) {
            updateBackgroundBounds();
            if (this.mCurrentBounds.equals(this.mBackgroundBounds)) {
                if (this.mBottomAnimator != null) {
                    this.mBottomAnimator.cancel();
                }
                if (this.mTopAnimator != null) {
                    this.mTopAnimator.cancel();
                }
            } else if (this.mAnimateNextBackgroundTop || this.mAnimateNextBackgroundBottom || areBoundsAnimating()) {
                startBackgroundAnimation();
            } else {
                this.mCurrentBounds.set(this.mBackgroundBounds);
                applyCurrentBackgroundBounds();
            }
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextBackgroundTop = false;
        }
    }

    private boolean areBoundsAnimating() {
        return (this.mBottomAnimator == null && this.mTopAnimator == null) ? false : true;
    }

    private void startBackgroundAnimation() {
        this.mCurrentBounds.left = this.mBackgroundBounds.left;
        this.mCurrentBounds.right = this.mBackgroundBounds.right;
        startBottomAnimation();
        startTopAnimation();
    }

    private void startTopAnimation() {
        int previousEndValue = this.mEndAnimationRect.top;
        int newEndValue = this.mBackgroundBounds.top;
        ObjectAnimator previousAnimator = this.mTopAnimator;
        if (previousAnimator != null && previousEndValue == newEndValue) {
            return;
        }
        if (this.mAnimateNextBackgroundTop) {
            if (previousAnimator != null) {
                previousAnimator.cancel();
            }
            ObjectAnimator animator = ObjectAnimator.ofInt(this, "backgroundTop", new int[]{this.mCurrentBounds.top, newEndValue});
            animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            animator.setDuration(360);
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    NotificationStackScrollLayout.this.mStartAnimationRect.top = -1;
                    NotificationStackScrollLayout.this.mEndAnimationRect.top = -1;
                    NotificationStackScrollLayout.this.mTopAnimator = null;
                }
            });
            animator.start();
            this.mStartAnimationRect.top = this.mCurrentBounds.top;
            this.mEndAnimationRect.top = newEndValue;
            this.mTopAnimator = animator;
        } else if (previousAnimator != null) {
            int previousStartValue = this.mStartAnimationRect.top;
            previousAnimator.getValues()[0].setIntValues(new int[]{previousStartValue, newEndValue});
            this.mStartAnimationRect.top = previousStartValue;
            this.mEndAnimationRect.top = newEndValue;
            previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
        } else {
            setBackgroundTop(newEndValue);
        }
    }

    private void startBottomAnimation() {
        int previousStartValue = this.mStartAnimationRect.bottom;
        int previousEndValue = this.mEndAnimationRect.bottom;
        int newEndValue = this.mBackgroundBounds.bottom;
        ObjectAnimator previousAnimator = this.mBottomAnimator;
        if (previousAnimator != null && previousEndValue == newEndValue) {
            return;
        }
        if (this.mAnimateNextBackgroundBottom) {
            if (previousAnimator != null) {
                previousAnimator.cancel();
            }
            ObjectAnimator animator = ObjectAnimator.ofInt(this, "backgroundBottom", new int[]{this.mCurrentBounds.bottom, newEndValue});
            animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            animator.setDuration(360);
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    NotificationStackScrollLayout.this.mStartAnimationRect.bottom = -1;
                    NotificationStackScrollLayout.this.mEndAnimationRect.bottom = -1;
                    NotificationStackScrollLayout.this.mBottomAnimator = null;
                }
            });
            animator.start();
            this.mStartAnimationRect.bottom = this.mCurrentBounds.bottom;
            this.mEndAnimationRect.bottom = newEndValue;
            this.mBottomAnimator = animator;
        } else if (previousAnimator != null) {
            previousAnimator.getValues()[0].setIntValues(new int[]{previousStartValue, newEndValue});
            this.mStartAnimationRect.bottom = previousStartValue;
            this.mEndAnimationRect.bottom = newEndValue;
            previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
        } else {
            setBackgroundBottom(newEndValue);
        }
    }

    private void setBackgroundTop(int top) {
        this.mCurrentBounds.top = top;
        applyCurrentBackgroundBounds();
    }

    public void setBackgroundBottom(int bottom) {
        this.mCurrentBounds.bottom = bottom;
        applyCurrentBackgroundBounds();
    }

    private void applyCurrentBackgroundBounds() {
        invalidate();
    }

    private void updateBackgroundBounds() {
        int bottom;
        this.mBackgroundBounds.left = (int) getX();
        this.mBackgroundBounds.right = (int) (getX() + ((float) getWidth()));
        if (!this.mIsExpanded) {
            this.mBackgroundBounds.top = 0;
            this.mBackgroundBounds.bottom = 0;
        }
        ExpandableView firstView = this.mFirstVisibleBackgroundChild;
        int top = 0;
        if (firstView != null) {
            int finalTranslationY = (int) StackStateAnimator.getFinalTranslationY(firstView);
            if (this.mAnimateNextBackgroundTop || ((this.mTopAnimator == null && this.mCurrentBounds.top == finalTranslationY) || (this.mTopAnimator != null && this.mEndAnimationRect.top == finalTranslationY))) {
                top = finalTranslationY;
            } else {
                top = (int) firstView.getTranslationY();
            }
        }
        ExpandableView lastView = this.mLastVisibleBackgroundChild;
        if (lastView != null) {
            int finalBottom = Math.min(((int) StackStateAnimator.getFinalTranslationY(lastView)) + StackStateAnimator.getFinalActualHeight(lastView), getHeight());
            if (this.mAnimateNextBackgroundBottom || ((this.mBottomAnimator == null && this.mCurrentBounds.bottom == finalBottom) || (this.mBottomAnimator != null && this.mEndAnimationRect.bottom == finalBottom))) {
                bottom = finalBottom;
            } else {
                bottom = Math.min((int) (lastView.getTranslationY() + ((float) lastView.getActualHeight())), getHeight());
            }
        } else {
            top = (int) (((float) this.mTopPadding) + this.mStackTranslation);
            bottom = top;
        }
        if (this.mPhoneStatusBar.getBarState() != 1) {
            this.mBackgroundBounds.top = (int) Math.max(((float) this.mTopPadding) + this.mStackTranslation, (float) top);
        } else {
            this.mBackgroundBounds.top = Math.max(0, top);
        }
        this.mBackgroundBounds.bottom = Math.min(getHeight(), Math.max(bottom, top));
    }

    private ExpandableView getLastChildWithBackground() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8 && ((child instanceof ActivatableNotificationView) || (child instanceof TrafficPanelViewContent))) {
                return (ExpandableView) child;
            }
        }
        return null;
    }

    private ExpandableView getFirstChildWithBackground() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8 && ((child instanceof ActivatableNotificationView) || (child instanceof TrafficPanelViewContent))) {
                return (ExpandableView) child;
            }
        }
        return null;
    }

    private void fling(int velocityY) {
        if (getChildCount() > 0) {
            int scrollRange = getScrollRange();
            float topAmount = getCurrentOverScrollAmount(true);
            float bottomAmount = getCurrentOverScrollAmount(false);
            if (velocityY < 0 && topAmount > 0.0f) {
                this.mOwnScrollY -= (int) topAmount;
                this.mDontReportNextOverScroll = true;
                setOverScrollAmount(0.0f, true, false);
                this.mMaxOverScroll = (((((float) Math.abs(velocityY)) / 1000.0f) * getRubberBandFactor(true)) * ((float) this.mOverflingDistance)) + topAmount;
            } else if (velocityY <= 0 || bottomAmount <= 0.0f) {
                this.mMaxOverScroll = 0.0f;
            } else {
                this.mOwnScrollY = (int) (((float) this.mOwnScrollY) + bottomAmount);
                setOverScrollAmount(0.0f, false, false);
                this.mMaxOverScroll = (((((float) Math.abs(velocityY)) / 1000.0f) * getRubberBandFactor(false)) * ((float) this.mOverflingDistance)) + bottomAmount;
            }
            int minScrollY = Math.max(0, scrollRange);
            if (this.mExpandedInThisMotion) {
                minScrollY = Math.min(minScrollY, this.mMaxScrollAfterExpand);
            }
            OverScroller overScroller = this.mScroller;
            int i = this.mScrollX;
            int i2 = this.mOwnScrollY;
            int i3 = (!this.mExpandedInThisMotion || this.mOwnScrollY < 0) ? 1073741823 : 0;
            overScroller.fling(i, i2, 1, velocityY, 0, 0, 0, minScrollY, 0, i3);
            postInvalidateOnAnimation();
        }
    }

    private boolean shouldOverScrollFling(int initialVelocity) {
        float topOverScroll = getCurrentOverScrollAmount(true);
        HwLog.i("StackScroller", "shouldOverScrollFling::topOverScroll=" + topOverScroll + ", initialVelocity=" + initialVelocity);
        if (!this.mScrolledToTopOnFirstDown || this.mExpandedInThisMotion || topOverScroll <= this.mMinTopOverScrollToEscape) {
            return false;
        }
        if (initialVelocity < 0) {
            return false;
        }
        return true;
    }

    public void updateTopPadding(float qsHeight, boolean animate, boolean ignoreIntrinsicPadding) {
        float start = qsHeight;
        float stackHeight = ((float) getHeight()) - qsHeight;
        int minStackHeight = getLayoutMinHeight();
        if (stackHeight <= ((float) minStackHeight)) {
            start = ((float) getHeight()) - ((float) minStackHeight);
            this.mTopPaddingOverflow = ((float) minStackHeight) - stackHeight;
        } else {
            this.mTopPaddingOverflow = 0.0f;
        }
        setTopPadding(ignoreIntrinsicPadding ? (int) start : clampPadding((int) start), animate);
        setStackHeight(this.mLastSetStackHeight);
    }

    public int getLayoutMinHeight() {
        int firstChildMinHeight;
        ExpandableView firstChild = getFirstChildNotGone();
        if (firstChild != null) {
            firstChildMinHeight = firstChild.getIntrinsicHeight();
        } else if (this.mEmptyShadeView != null) {
            firstChildMinHeight = this.mEmptyShadeView.getMinHeight();
        } else {
            firstChildMinHeight = this.mCollapsedSize;
        }
        if (getChildCount() == 0) {
            firstChildMinHeight = this.mCollapsedSize;
        }
        if (this.mOwnScrollY > 0) {
            firstChildMinHeight = Math.max(firstChildMinHeight - this.mOwnScrollY, this.mCollapsedSize);
        }
        return Math.min((this.mBottomStackPeekSize + firstChildMinHeight) + this.mBottomStackSlowDownHeight, this.mMaxLayoutHeight - this.mTopPadding);
    }

    public float getTopPaddingOverflow() {
        return this.mTopPaddingOverflow;
    }

    private int clampPadding(int desiredPadding) {
        return Math.max(desiredPadding, this.mIntrinsicPadding);
    }

    private float getRubberBandFactor(boolean onTop) {
        if (!onTop) {
            return 0.35f;
        }
        if (this.mExpandedInThisMotion) {
            return 0.15f;
        }
        if (this.mIsExpansionChanging || this.mPanelTracking) {
            return 0.21f;
        }
        if (this.mScrolledToTopOnFirstDown) {
            return 1.0f;
        }
        return 0.35f;
    }

    private boolean isRubberbanded(boolean onTop) {
        if (!onTop || this.mExpandedInThisMotion || this.mIsExpansionChanging || this.mPanelTracking || !this.mScrolledToTopOnFirstDown) {
            return true;
        }
        return false;
    }

    private void endDrag() {
        HwLog.i("StackScroller", "endDrag");
        setIsBeingDragged(false);
        recycleVelocityTracker();
        if (getCurrentOverScrollAmount(true) > 0.0f) {
            setOverScrollAmount(0.0f, true, true);
        }
        if (getCurrentOverScrollAmount(false) > 0.0f) {
            setOverScrollAmount(0.0f, false, true);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("StackScroller", "onInterceptTouchEvent: " + ev + ", mSwipingInProgress=" + this.mSwipingInProgress + ", mOnlyScrollingInThisMotion=" + this.mOnlyScrollingInThisMotion + ", mExpandingNotification=" + this.mExpandingNotification + ", mDisallowDismissInThisMotion=" + this.mDisallowDismissInThisMotion + ", mExpandedInThisMotion=" + this.mExpandedInThisMotion + ", isCustomizing=" + this.mQsContainer.isCustomizing() + ", isRemoteInputing=" + HwPhoneStatusBar.getInstance().isRemoteInputing() + ", mOwnScrollY=" + this.mOwnScrollY);
        }
        if (this.mQsContainer.isCustomizing()) {
            return true;
        }
        if (HwPhoneStatusBar.getInstance().isRemoteInputing()) {
            return super.onInterceptTouchEvent(ev);
        }
        initDownStates(ev);
        handleEmptySpaceClick(ev);
        boolean expandWantsIt = false;
        if (!(this.mSwipingInProgress || this.mOnlyScrollingInThisMotion)) {
            expandWantsIt = this.mExpandHelper.onInterceptTouchEvent(ev);
        }
        boolean scrollWantsIt = false;
        if (!(this.mSwipingInProgress || this.mExpandingNotification)) {
            scrollWantsIt = onInterceptTouchEventScroll(ev);
        }
        boolean swipeWantsIt = false;
        if (!(this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion)) {
            swipeWantsIt = this.mSwipeHelper.onInterceptTouchEvent(ev);
        }
        if (!(swipeWantsIt || r1 || r0)) {
            z = super.onInterceptTouchEvent(ev);
        }
        return z;
    }

    private void handleEmptySpaceClick(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case 1:
                if (this.mPhoneStatusBar.getBarState() != 1 && this.mTouchIsClick && isBelowLastNotification(this.mInitialTouchX, this.mInitialTouchY)) {
                    this.mOnEmptySpaceClickListener.onEmptySpaceClicked(this.mInitialTouchX, this.mInitialTouchY);
                    return;
                }
                return;
            case 2:
                if (!this.mTouchIsClick) {
                    return;
                }
                if (Math.abs(ev.getY() - this.mInitialTouchY) > ((float) this.mTouchSlop) || Math.abs(ev.getX() - this.mInitialTouchX) > ((float) this.mTouchSlop)) {
                    this.mTouchIsClick = false;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void initDownStates(MotionEvent ev) {
        if (ev.getAction() == 0) {
            boolean z;
            this.mExpandedInThisMotion = false;
            if (this.mScroller.isFinished()) {
                z = false;
            } else {
                z = true;
            }
            this.mOnlyScrollingInThisMotion = z;
            this.mDisallowScrollingInThisMotion = false;
            this.mDisallowDismissInThisMotion = false;
            this.mTouchIsClick = true;
            this.mInitialTouchX = ev.getX();
            this.mInitialTouchY = ev.getY();
        }
    }

    public void setChildTransferInProgress(boolean childTransferInProgress) {
        this.mChildTransferInProgress = childTransferInProgress;
    }

    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (!this.mChildTransferInProgress) {
            onViewRemovedInternal(child, this);
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (disallowIntercept) {
            this.mSwipeHelper.removeLongPressCallback();
        }
    }

    private void onViewRemovedInternal(View child, ViewGroup container) {
        if (!this.mChangePositionInProgress) {
            ExpandableView expandableView = (ExpandableView) child;
            expandableView.setOnHeightChangedListener(null);
            this.mCurrentStackScrollState.removeViewStateForView(child);
            updateScrollStateForRemovedChild(expandableView);
            if (!generateRemoveAnimation(child)) {
                this.mSwipedOutViews.remove(child);
            } else if (!this.mSwipedOutViews.contains(child)) {
                container.getOverlay().add(child);
            } else if (Math.abs(expandableView.getTranslation()) != ((float) expandableView.getWidth())) {
                container.addTransientView(child, 0);
                expandableView.setTransientContainer(container);
            }
            updateAnimationState(false, child);
            expandableView.setClipTopAmount(0);
            focusNextViewIfFocused(child);
        }
    }

    private void focusNextViewIfFocused(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) view;
            if (row.shouldRefocusOnDismiss()) {
                View nextView = row.getChildAfterViewWhenDismissed();
                if (nextView == null) {
                    float translationY;
                    View groupParentWhenDismissed = row.getGroupParentWhenDismissed();
                    if (groupParentWhenDismissed != null) {
                        translationY = groupParentWhenDismissed.getTranslationY();
                    } else {
                        translationY = view.getTranslationY();
                    }
                    nextView = getFirstChildBelowTranlsationY(translationY);
                }
                if (nextView != null) {
                    nextView.requestAccessibilityFocus();
                }
            }
        }
    }

    private boolean isChildInGroup(View child) {
        if (child instanceof ExpandableNotificationRow) {
            return this.mGroupManager.isChildInGroupWithSummary(((ExpandableNotificationRow) child).getStatusBarNotification());
        }
        return false;
    }

    private boolean generateRemoveAnimation(View child) {
        if (removeRemovedChildFromHeadsUpChangeAnimations(child)) {
            this.mAddedHeadsUpChildren.remove(child);
            return false;
        } else if (isClickedHeadsUp(child)) {
            this.mClearOverlayViewsWhenFinished.add(child);
            return true;
        } else if (!this.mIsExpanded || !this.mAnimationsEnabled || isChildInInvisibleGroup(child)) {
            return false;
        } else {
            if (this.mChildrenToAddAnimated.contains(child)) {
                this.mChildrenToAddAnimated.remove(child);
                this.mFromMoreCardAdditions.remove(child);
                return false;
            }
            this.mChildrenToRemoveAnimated.add(child);
            this.mNeedsAnimation = true;
            return true;
        }
    }

    private boolean isClickedHeadsUp(View child) {
        return HeadsUpManager.isClickedHeadsUpNotification(child);
    }

    private boolean removeRemovedChildFromHeadsUpChangeAnimations(View child) {
        boolean hasAddEvent = false;
        for (Pair<ExpandableNotificationRow, Boolean> eventPair : this.mHeadsUpChangeAnimations) {
            View row = eventPair.first;
            boolean isHeadsUp = ((Boolean) eventPair.second).booleanValue();
            if (child == row) {
                this.mTmpList.add(eventPair);
                hasAddEvent |= isHeadsUp;
            }
        }
        if (hasAddEvent) {
            this.mHeadsUpChangeAnimations.removeAll(this.mTmpList);
            ((ExpandableNotificationRow) child).setHeadsupDisappearRunning(false);
        }
        this.mTmpList.clear();
        return hasAddEvent;
    }

    private boolean isChildInInvisibleGroup(View child) {
        boolean z = false;
        if (child instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) child;
            ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary(row.getStatusBarNotification());
            if (!(groupSummary == null || groupSummary == row)) {
                if (row.getVisibility() == 4) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    private void updateScrollStateForRemovedChild(ExpandableView removedChild) {
        int startingPosition = getPositionInLinearLayout(removedChild);
        int childHeight = getIntrinsicHeight(removedChild) + ((int) NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, removedChild.getIncreasedPaddingAmount()));
        if (startingPosition + childHeight <= this.mOwnScrollY) {
            this.mOwnScrollY -= childHeight;
        } else if (startingPosition < this.mOwnScrollY) {
            this.mOwnScrollY = startingPosition;
        }
    }

    private int getIntrinsicHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view.getHeight();
    }

    private int getPositionInLinearLayout(View requestedView) {
        ExpandableNotificationRow expandableNotificationRow = null;
        ExpandableNotificationRow expandableNotificationRow2 = null;
        if (isChildInGroup(requestedView)) {
            expandableNotificationRow = (ExpandableNotificationRow) requestedView;
            expandableNotificationRow2 = expandableNotificationRow.getNotificationParent();
            requestedView = expandableNotificationRow2;
        }
        int position = 0;
        float previousIncreasedAmount = 0.0f;
        for (int i = 0; i < getChildCount(); i++) {
            boolean notGone;
            View child = (ExpandableView) getChildAt(i);
            if (child.getVisibility() != 8) {
                notGone = true;
            } else {
                notGone = false;
            }
            if (notGone) {
                float increasedPaddingAmount = child.getIncreasedPaddingAmount();
                if (position != 0) {
                    position += (int) NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, Math.max(previousIncreasedAmount, increasedPaddingAmount));
                }
                previousIncreasedAmount = increasedPaddingAmount;
            }
            if (child == requestedView) {
                if (expandableNotificationRow2 != null) {
                    position += expandableNotificationRow2.getPositionOfChild(expandableNotificationRow);
                }
                return position;
            }
            if (notGone) {
                position += getIntrinsicHeight(child);
            }
        }
        return 0;
    }

    public void onViewAdded(View child) {
        super.onViewAdded(child);
        onViewAddedInternal(child);
    }

    private void updateFirstAndLastBackgroundViews() {
        boolean z = true;
        ExpandableView firstChild = getFirstChildWithBackground();
        ExpandableView lastChild = getLastChildWithBackground();
        if (this.mAnimationsEnabled && this.mIsExpanded) {
            boolean z2;
            if (firstChild != this.mFirstVisibleBackgroundChild) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mAnimateNextBackgroundTop = z2;
            if (lastChild == this.mLastVisibleBackgroundChild) {
                z = false;
            }
            this.mAnimateNextBackgroundBottom = z;
        } else {
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
        }
        this.mFirstVisibleBackgroundChild = firstChild;
        this.mLastVisibleBackgroundChild = lastChild;
    }

    private void onViewAddedInternal(View child) {
        updateHideSensitiveForChild(child);
        ((ExpandableView) child).setOnHeightChangedListener(this);
        generateAddAnimation(child, false);
        updateAnimationState(child);
        updateChronometerForChild(child);
    }

    private void updateHideSensitiveForChild(View child) {
        if (child instanceof ExpandableView) {
            ((ExpandableView) child).setHideSensitiveForIntrinsicHeight(this.mAmbientState.isHideSensitive());
        }
    }

    public void notifyGroupChildRemoved(View row, ViewGroup childrenContainer) {
        onViewRemovedInternal(row, childrenContainer);
    }

    public void notifyGroupChildAdded(View row) {
        onViewAddedInternal(row);
    }

    public void setAnimationsEnabled(boolean animationsEnabled) {
        this.mAnimationsEnabled = animationsEnabled;
        updateNotificationAnimationStates();
    }

    private void updateNotificationAnimationStates() {
        boolean running = !this.mAnimationsEnabled ? this.mPulsing : true;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            running &= !this.mIsExpanded ? isPinnedHeadsUp(child) : 1;
            updateAnimationState(running, child);
        }
    }

    private void updateAnimationState(View child) {
        boolean isPinnedHeadsUp = (this.mAnimationsEnabled || this.mPulsing) ? !this.mIsExpanded ? isPinnedHeadsUp(child) : true : false;
        updateAnimationState(isPinnedHeadsUp, child);
    }

    private void updateAnimationState(boolean running, View child) {
        if (child instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) child).setIconAnimationRunning(running);
        }
    }

    public boolean isAddOrRemoveAnimationPending() {
        if (!this.mNeedsAnimation) {
            return false;
        }
        if (this.mChildrenToAddAnimated.isEmpty() && this.mChildrenToRemoveAnimated.isEmpty()) {
            return false;
        }
        return true;
    }

    public void generateAddAnimation(View child, boolean fromMoreCard) {
        HwLog.i("StackScroller", "generateAddAnimation:" + child.getTag() + ", " + fromMoreCard);
        if (this.mIsExpanded && this.mAnimationsEnabled && !this.mChangePositionInProgress) {
            this.mChildrenToAddAnimated.add(child);
            if (fromMoreCard) {
                this.mFromMoreCardAdditions.add(child);
            }
            this.mNeedsAnimation = true;
        }
        if (isHeadsUp(child) && !this.mChangePositionInProgress) {
            this.mAddedHeadsUpChildren.add(child);
            this.mChildrenToAddAnimated.remove(child);
        }
    }

    public void changeViewPosition(View child, int newIndex) {
        int currentIndex = indexOfChild(child);
        if (child != null && child.getParent() == this && currentIndex != newIndex) {
            this.mChangePositionInProgress = true;
            ((ExpandableView) child).setChangingPosition(true);
            removeView(child);
            addView(child, newIndex);
            ((ExpandableView) child).setChangingPosition(false);
            this.mChangePositionInProgress = false;
            if (this.mIsExpanded && this.mAnimationsEnabled && child.getVisibility() != 8) {
                this.mChildrenChangingPositions.add(child);
                this.mNeedsAnimation = true;
            }
        }
    }

    private void startAnimationToState() {
        if (this.mNeedsAnimation) {
            generateChildHierarchyEvents();
            this.mNeedsAnimation = false;
        }
        if (!this.mAnimationEvents.isEmpty() || isCurrentlyAnimating()) {
            setAnimationRunning(true);
            this.mStateAnimator.startAnimationForEvents(this.mAnimationEvents, this.mCurrentStackScrollState, this.mGoToFullShadeDelay);
            this.mAnimationEvents.clear();
            updateBackground();
            updateViewShadows();
        } else {
            applyCurrentState();
        }
        this.mGoToFullShadeDelay = 0;
    }

    private void generateChildHierarchyEvents() {
        generateHeadsUpAnimationEvents();
        generateChildRemovalEvents();
        generateChildAdditionEvents();
        generatePositionChangeEvents();
        generateSnapBackEvents();
        generateDragEvents();
        generateTopPaddingEvent();
        generateActivateEvent();
        generateDimmedEvent();
        generateHideSensitiveEvent();
        generateDarkEvent();
        generateGoToFullShadeEvent();
        generateViewResizeEvent();
        generateGroupExpansionEvent();
        generateAnimateEverythingEvent();
        this.mNeedsAnimation = false;
    }

    private void generateHeadsUpAnimationEvents() {
        for (Pair<ExpandableNotificationRow, Boolean> eventPair : this.mHeadsUpChangeAnimations) {
            ExpandableNotificationRow row = eventPair.first;
            boolean isHeadsUp = ((Boolean) eventPair.second).booleanValue();
            int type = 17;
            boolean onBottom = false;
            boolean pinnedAndClosed = row.isPinned() && !this.mIsExpanded;
            if (this.mIsExpanded || isHeadsUp) {
                StackViewState viewState = this.mCurrentStackScrollState.getViewStateForView(row);
                if (viewState != null) {
                    if (isHeadsUp && (this.mAddedHeadsUpChildren.contains(row) || pinnedAndClosed)) {
                        if (pinnedAndClosed || shouldHunAppearFromBottom(viewState)) {
                            type = 14;
                        } else {
                            type = 0;
                        }
                        onBottom = !pinnedAndClosed;
                    }
                }
            } else {
                if (row.wasJustClicked()) {
                    type = 16;
                } else {
                    type = 15;
                }
                if (row.isChildInGroup()) {
                    row.setHeadsupDisappearRunning(false);
                }
            }
            AnimationEvent event = new AnimationEvent(row, type);
            event.headsUpFromBottom = onBottom;
            this.mAnimationEvents.add(event);
        }
        this.mHeadsUpChangeAnimations.clear();
        this.mAddedHeadsUpChildren.clear();
    }

    private boolean shouldHunAppearFromBottom(StackViewState viewState) {
        if (viewState.yTranslation + ((float) viewState.height) < this.mAmbientState.getMaxHeadsUpTranslation()) {
            return false;
        }
        return true;
    }

    private void generateGroupExpansionEvent() {
        if (this.mExpandedGroupView != null) {
            this.mAnimationEvents.add(new AnimationEvent(this.mExpandedGroupView, 13));
            this.mExpandedGroupView = null;
        }
    }

    private void generateViewResizeEvent() {
        if (this.mNeedViewResizeAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 12));
        }
        this.mNeedViewResizeAnimation = false;
    }

    private void generateSnapBackEvents() {
        for (View child : this.mSnappedBackChildren) {
            this.mAnimationEvents.add(new AnimationEvent(child, 5));
        }
        this.mSnappedBackChildren.clear();
    }

    private void generateDragEvents() {
        for (View child : this.mDragAnimPendingChildren) {
            this.mAnimationEvents.add(new AnimationEvent(child, 4));
        }
        this.mDragAnimPendingChildren.clear();
    }

    private void generateChildRemovalEvents() {
        for (View child : this.mChildrenToRemoveAnimated) {
            int animationType;
            if (this.mSwipedOutViews.contains(child)) {
                animationType = 2;
            } else {
                animationType = 1;
            }
            AnimationEvent event = new AnimationEvent(child, animationType);
            event.viewAfterChangingView = getFirstChildBelowTranlsationY(child.getTranslationY());
            this.mAnimationEvents.add(event);
            this.mSwipedOutViews.remove(child);
        }
        this.mChildrenToRemoveAnimated.clear();
    }

    private void generatePositionChangeEvents() {
        for (View child : this.mChildrenChangingPositions) {
            this.mAnimationEvents.add(new AnimationEvent(child, 8));
        }
        this.mChildrenChangingPositions.clear();
        if (this.mGenerateChildOrderChangedEvent) {
            this.mAnimationEvents.add(new AnimationEvent(null, 8));
            this.mGenerateChildOrderChangedEvent = false;
        }
    }

    private void generateChildAdditionEvents() {
        for (View child : this.mChildrenToAddAnimated) {
            if (this.mFromMoreCardAdditions.contains(child)) {
                this.mAnimationEvents.add(new AnimationEvent(child, 0, 360));
            } else {
                this.mAnimationEvents.add(new AnimationEvent(child, 0));
            }
        }
        this.mChildrenToAddAnimated.clear();
        this.mFromMoreCardAdditions.clear();
    }

    private void generateTopPaddingEvent() {
        if (this.mTopPaddingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 3));
        }
        this.mTopPaddingNeedsAnimation = false;
    }

    private void generateActivateEvent() {
        if (this.mActivateNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 6));
        }
        this.mActivateNeedsAnimation = false;
    }

    private void generateAnimateEverythingEvent() {
        if (this.mEverythingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 18));
        }
        this.mEverythingNeedsAnimation = false;
    }

    private void generateDimmedEvent() {
        if (this.mDimmedNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 7));
        }
        this.mDimmedNeedsAnimation = false;
    }

    private void generateHideSensitiveEvent() {
        if (this.mHideSensitiveNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 11));
        }
        this.mHideSensitiveNeedsAnimation = false;
    }

    private void generateDarkEvent() {
        if (this.mDarkNeedsAnimation) {
            AnimationEvent ev = new AnimationEvent(null, 9);
            ev.darkAnimationOriginIndex = this.mDarkAnimationOriginIndex;
            this.mAnimationEvents.add(ev);
            startBackgroundFadeIn();
        }
        this.mDarkNeedsAnimation = false;
    }

    private void generateGoToFullShadeEvent() {
        if (this.mGoToFullShadeNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 10));
        }
        this.mGoToFullShadeNeedsAnimation = false;
    }

    private boolean onInterceptTouchEventScroll(MotionEvent ev) {
        if (!isScrollingEnabled()) {
            return false;
        }
        int action = ev.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        int y;
        switch (action & 255) {
            case 0:
                y = (int) ev.getY();
                this.mScrolledToTopOnFirstDown = isScrolledToTop();
                if (getChildAtPosition(ev.getX(), (float) y) != null) {
                    this.mLastMotionY = y;
                    this.mDownX = (int) ev.getX();
                    this.mActivePointerId = ev.getPointerId(0);
                    initOrResetVelocityTracker();
                    this.mVelocityTracker.addMovement(ev);
                    setIsBeingDragged(!this.mScroller.isFinished());
                    break;
                }
                setIsBeingDragged(false);
                recycleVelocityTracker();
                break;
            case 1:
            case 3:
                setIsBeingDragged(false);
                this.mActivePointerId = -1;
                recycleVelocityTracker();
                if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                    postInvalidateOnAnimation();
                    break;
                }
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        y = (int) ev.getY(pointerIndex);
                        int x = (int) ev.getX(pointerIndex);
                        int yDiff = Math.abs(y - this.mLastMotionY);
                        int xDiff = Math.abs(x - this.mDownX);
                        if (yDiff > this.mTouchSlop && yDiff > xDiff) {
                            setIsBeingDragged(true);
                            this.mLastMotionY = y;
                            this.mDownX = x;
                            initVelocityTrackerIfNotExists();
                            this.mVelocityTracker.addMovement(ev);
                            break;
                        }
                    }
                    Log.e("StackScroller", "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                    break;
                }
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    private boolean isInContentBounds(MotionEvent event) {
        return isInContentBounds(event.getY());
    }

    public boolean isInContentBounds(float y) {
        return y < ((float) (getHeight() - getEmptyBottomMargin()));
    }

    private void setIsBeingDragged(boolean isDragged) {
        HwLog.i("StackScroller", "setIsBeingDragged: " + isDragged);
        this.mIsBeingDragged = isDragged;
        if (isDragged) {
            requestDisallowInterceptTouchEvent(true);
            removeLongPressCallback();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            removeLongPressCallback();
        }
    }

    public void clearChildFocus(View child) {
        super.clearChildFocus(child);
        if (this.mForcedScroll == child) {
            this.mForcedScroll = null;
        }
    }

    public void requestDisallowLongPress() {
        removeLongPressCallback();
    }

    public void requestDisallowDismiss() {
        this.mDisallowDismissInThisMotion = true;
    }

    public void removeLongPressCallback() {
        this.mSwipeHelper.removeLongPressCallback();
    }

    public boolean isScrolledToTop() {
        return this.mOwnScrollY == 0;
    }

    public boolean isScrolledToBottom() {
        return this.mOwnScrollY >= getScrollRange();
    }

    public View getHostView() {
        return this;
    }

    public int getEmptyBottomMargin() {
        return Math.max(((this.mMaxLayoutHeight - this.mContentHeight) - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight, 0);
    }

    public float getKeyguardBottomStackSize() {
        return (float) (this.mBottomStackPeekSize + getResources().getDimensionPixelSize(R.dimen.bottom_stack_slow_down_length));
    }

    public void onExpansionStarted() {
        this.mIsExpansionChanging = true;
    }

    public void onExpansionStopped() {
        this.mIsExpansionChanging = false;
        if (!this.mIsExpanded) {
            this.mOwnScrollY = 0;
            this.mPhoneStatusBar.resetUserExpandedStates();
            clearTemporaryViews(this);
            for (int i = 0; i < getChildCount(); i++) {
                ExpandableView child = (ExpandableView) getChildAt(i);
                if (child instanceof ExpandableNotificationRow) {
                    clearTemporaryViews(((ExpandableNotificationRow) child).getChildrenContainer());
                }
            }
        }
    }

    private void clearTemporaryViews(ViewGroup viewGroup) {
        while (viewGroup != null && viewGroup.getTransientViewCount() != 0) {
            View view = viewGroup.getTransientView(0);
            if (view != null) {
                viewGroup.removeTransientView(view);
            }
        }
        if (viewGroup != null) {
            viewGroup.getOverlay().clear();
        }
    }

    public void onPanelTrackingStarted() {
        this.mPanelTracking = true;
    }

    public void onPanelTrackingStopped() {
        this.mPanelTracking = false;
    }

    public void resetScrollPosition() {
        this.mScroller.abortAnimation();
        this.mOwnScrollY = 0;
    }

    private void setIsExpanded(boolean isExpanded) {
        boolean changed = isExpanded != this.mIsExpanded;
        this.mIsExpanded = isExpanded;
        this.mStackScrollAlgorithm.setIsExpanded(isExpanded);
        if (changed) {
            HwLog.i("StackScroller", "setIsExpanded: " + isExpanded);
            if (!this.mIsExpanded) {
                this.mGroupManager.collapseAllGroups();
            }
            updateNotificationAnimationStates();
            updateChronometers();
        }
    }

    private void updateChronometers() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            updateChronometerForChild(getChildAt(i));
        }
    }

    private void updateChronometerForChild(View child) {
        if (child instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) child).setChronometerRunning(this.mIsExpanded);
        }
    }

    public void onHeightChanged(ExpandableView view, boolean needsAnimation) {
        updateContentHeight();
        updateScrollPositionOnExpandInBottom(view);
        clampScrollPosition();
        notifyHeightChangeListener(view);
        if (needsAnimation) {
            ExpandableNotificationRow row;
            if (view instanceof ExpandableNotificationRow) {
                row = (ExpandableNotificationRow) view;
            } else {
                row = null;
            }
            requestAnimationOnViewResize(row);
        }
        requestChildrenUpdate();
    }

    public void onReset(ExpandableView view) {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mRequestViewResizeAnimationOnLayout = true;
        }
        updateAnimationState(view);
        updateChronometerForChild(view);
    }

    private void updateScrollPositionOnExpandInBottom(ExpandableView view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableView row = (ExpandableNotificationRow) view;
            if (row.isUserLocked() && row != getFirstChildNotGone() && !row.isSummaryWithChildren()) {
                float endPosition = row.getTranslationY() + ((float) row.getActualHeight());
                if (row.isChildInGroup()) {
                    endPosition += row.getNotificationParent().getTranslationY();
                }
                int stackEnd = getStackEndPosition();
                if (endPosition > ((float) stackEnd)) {
                    this.mOwnScrollY = (int) (((float) this.mOwnScrollY) + (endPosition - ((float) stackEnd)));
                    this.mDisallowScrollingInThisMotion = true;
                }
            }
        }
    }

    private int getStackEndPosition() {
        return (((this.mMaxLayoutHeight - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight) + this.mPaddingBetweenElements) + ((int) this.mStackTranslation);
    }

    public void setOnHeightChangedListener(OnHeightChangedListener mOnHeightChangedListener) {
        this.mOnHeightChangedListener = mOnHeightChangedListener;
    }

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener listener) {
        this.mOnEmptySpaceClickListener = listener;
    }

    public void onChildAnimationFinished() {
        setAnimationRunning(false);
        requestChildrenUpdate();
        runAnimationFinishedRunnables();
        clearViewOverlays();
        clearHeadsUpDisappearRunning();
    }

    private void clearHeadsUpDisappearRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow row = (ExpandableNotificationRow) view;
                row.setHeadsupDisappearRunning(false);
                if (!row.isSummaryWithChildren()) {
                    continue;
                } else if (row.getNotificationChildren() != null) {
                    for (ExpandableNotificationRow child : row.getNotificationChildren()) {
                        child.setHeadsupDisappearRunning(false);
                    }
                } else {
                    return;
                }
            }
        }
    }

    private void clearViewOverlays() {
        for (View view : this.mClearOverlayViewsWhenFinished) {
            StackStateAnimator.removeFromOverlay(view);
        }
        this.mClearOverlayViewsWhenFinished.clear();
    }

    private void runAnimationFinishedRunnables() {
        for (Runnable runnable : this.mAnimationFinishedRunnables) {
            runnable.run();
        }
        this.mAnimationFinishedRunnables.clear();
    }

    public void setDimmed(boolean dimmed, boolean animate) {
        this.mAmbientState.setDimmed(dimmed);
        if (animate && this.mAnimationsEnabled) {
            this.mDimmedNeedsAnimation = true;
            this.mNeedsAnimation = true;
            animateDimmed(dimmed);
        } else {
            setDimAmount(dimmed ? 1.0f : 0.0f);
        }
        requestChildrenUpdate();
    }

    private void setDimAmount(float dimAmount) {
        this.mDimAmount = dimAmount;
        updateBackgroundDimming();
    }

    private void animateDimmed(boolean dimmed) {
        if (this.mDimAnimator != null) {
            this.mDimAnimator.cancel();
        }
        if ((dimmed ? 1.0f : 0.0f) != this.mDimAmount) {
            this.mDimAnimator = TimeAnimator.ofFloat(new float[]{this.mDimAmount, dimmed ? 1.0f : 0.0f});
            this.mDimAnimator.setDuration(220);
            this.mDimAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mDimAnimator.addListener(this.mDimEndListener);
            this.mDimAnimator.addUpdateListener(this.mDimUpdateListener);
            this.mDimAnimator.start();
        }
    }

    public void setHideSensitive(boolean hideSensitive, boolean animate) {
        if (hideSensitive != this.mAmbientState.isHideSensitive()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((ExpandableView) getChildAt(i)).setHideSensitiveForIntrinsicHeight(hideSensitive);
            }
            this.mAmbientState.setHideSensitive(hideSensitive);
            if (animate && this.mAnimationsEnabled) {
                this.mHideSensitiveNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
        }
    }

    public void setActivatedChild(ActivatableNotificationView activatedChild) {
        this.mAmbientState.setActivatedChild(activatedChild);
        if (this.mAnimationsEnabled) {
            this.mActivateNeedsAnimation = true;
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mAmbientState.getActivatedChild();
    }

    private void applyCurrentState() {
        this.mCurrentStackScrollState.apply();
        if (this.mListener != null) {
            this.mListener.onChildLocationsChanged(this);
        }
        runAnimationFinishedRunnables();
        setAnimationRunning(false);
        updateBackground();
        updateViewShadows();
    }

    private void updateViewShadows() {
        int i;
        for (i = 0; i < getChildCount(); i++) {
            ExpandableView child = (ExpandableView) getChildAt(i);
            if (child.getVisibility() != 8) {
                this.mTmpSortedChildren.add(child);
            }
        }
        Collections.sort(this.mTmpSortedChildren, this.mViewPositionComparator);
        ExpandableView previous = null;
        for (i = 0; i < this.mTmpSortedChildren.size(); i++) {
            ExpandableView expandableView = (ExpandableView) this.mTmpSortedChildren.get(i);
            float translationZ = expandableView.getTranslationZ();
            float diff = (previous == null ? translationZ : previous.getTranslationZ()) - translationZ;
            if (diff <= 0.0f || diff >= 0.1f) {
                expandableView.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            } else {
                expandableView.setFakeShadowIntensity(diff / 0.1f, previous.getOutlineAlpha(), (int) (((previous.getTranslationY() + ((float) previous.getActualHeight())) - expandableView.getTranslationY()) - ((float) previous.getExtraBottomPadding())), previous.getOutlineTranslation());
            }
            previous = expandableView;
        }
        this.mTmpSortedChildren.clear();
    }

    public void goToFullShade(long delay) {
        this.mDismissView.setInvisible();
        this.mEmptyShadeView.setInvisible();
        this.mGoToFullShadeNeedsAnimation = true;
        this.mGoToFullShadeDelay = delay;
        this.mNeedsAnimation = true;
        requestChildrenUpdate();
        HwPhoneStatusBar.getInstance().goToFullShade();
    }

    public void cancelExpandHelper() {
        this.mExpandHelper.cancel();
    }

    public void setIntrinsicPadding(int intrinsicPadding) {
        this.mIntrinsicPadding = intrinsicPadding;
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public float getNotificationsTopY() {
        return ((float) this.mTopPadding) + getStackTranslation();
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void setDark(boolean dark, boolean animate, PointF touchWakeUpScreenLocation) {
        this.mAmbientState.setDark(dark);
        if (animate && this.mAnimationsEnabled) {
            this.mDarkNeedsAnimation = true;
            this.mDarkAnimationOriginIndex = findDarkAnimationOriginIndex(touchWakeUpScreenLocation);
            this.mNeedsAnimation = true;
            setBackgroundFadeAmount(0.0f);
        } else if (!dark) {
            setBackgroundFadeAmount(1.0f);
        }
        requestChildrenUpdate();
        if (dark) {
            setWillNotDraw(true);
            this.mScrimController.setExcludedBackgroundArea(null);
            return;
        }
        updateBackground();
        setWillNotDraw(false);
    }

    private void setBackgroundFadeAmount(float fadeAmount) {
        this.mBackgroundFadeAmount = fadeAmount;
        updateBackgroundDimming();
    }

    public float getBackgroundFadeAmount() {
        return this.mBackgroundFadeAmount;
    }

    private void startBackgroundFadeIn() {
        int maxLength;
        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(this, BACKGROUND_FADE, new float[]{0.0f, 1.0f});
        if (this.mDarkAnimationOriginIndex == -1 || this.mDarkAnimationOriginIndex == -2) {
            maxLength = getNotGoneChildCount() - 1;
        } else {
            maxLength = Math.max(this.mDarkAnimationOriginIndex, (getNotGoneChildCount() - this.mDarkAnimationOriginIndex) - 1);
        }
        fadeAnimator.setStartDelay((long) (Math.max(0, maxLength) * 24));
        fadeAnimator.setDuration(360);
        fadeAnimator.setInterpolator(Interpolators.ALPHA_IN);
        fadeAnimator.start();
    }

    private int findDarkAnimationOriginIndex(PointF screenLocation) {
        if (screenLocation == null || screenLocation.y < ((float) this.mTopPadding) + this.mTopPaddingOverflow) {
            return -1;
        }
        if (screenLocation.y > getBottomMostNotificationBottom()) {
            return -2;
        }
        View child = getClosestChildAtRawPosition(screenLocation.x, screenLocation.y);
        if (child != null) {
            return getNotGoneIndex(child);
        }
        return -1;
    }

    private int getNotGoneIndex(View child) {
        int count = getChildCount();
        int notGoneIndex = 0;
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (child == v) {
                return notGoneIndex;
            }
            if (v.getVisibility() != 8) {
                notGoneIndex++;
            }
        }
        return -1;
    }

    public void setDismissView(DismissView dismissView) {
        int index = -1;
        if (this.mDismissView != null) {
            index = indexOfChild(this.mDismissView);
            removeView(this.mDismissView);
        }
        this.mDismissView = dismissView;
        addView(this.mDismissView, index);
    }

    public void setEmptyShadeView(EmptyShadeView emptyShadeView) {
        int index = -1;
        if (this.mEmptyShadeView != null) {
            index = indexOfChild(this.mEmptyShadeView);
            removeView(this.mEmptyShadeView);
        }
        this.mEmptyShadeView = emptyShadeView;
        addView(this.mEmptyShadeView, index);
    }

    public void updateEmptyShadeView(boolean visible, boolean animate) {
        int newVisibility;
        int oldVisibility = this.mEmptyShadeView.willBeGone() ? 8 : this.mEmptyShadeView.getVisibility();
        if (visible) {
            newVisibility = 0;
        } else {
            newVisibility = 8;
        }
        if (oldVisibility == newVisibility) {
            return;
        }
        if (newVisibility != 8) {
            if (this.mEmptyShadeView.willBeGone()) {
                this.mEmptyShadeView.cancelAnimation();
            } else {
                this.mEmptyShadeView.setInvisible();
            }
            this.mEmptyShadeView.setVisibility(newVisibility);
            this.mEmptyShadeView.setWillBeGone(false);
            updateContentHeight();
            notifyHeightChangeListener(this.mEmptyShadeView);
            return;
        }
        Runnable onFinishedRunnable = new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.mEmptyShadeView.setVisibility(8);
                NotificationStackScrollLayout.this.mEmptyShadeView.setWillBeGone(false);
                NotificationStackScrollLayout.this.updateContentHeight();
                NotificationStackScrollLayout.this.notifyHeightChangeListener(NotificationStackScrollLayout.this.mEmptyShadeView);
            }
        };
        if (this.mAnimationsEnabled && animate) {
            this.mEmptyShadeView.setWillBeGone(true);
            this.mEmptyShadeView.performVisibilityAnimation(false, onFinishedRunnable);
            return;
        }
        this.mEmptyShadeView.setInvisible();
        onFinishedRunnable.run();
    }

    public void setOverflowContainer(NotificationOverflowContainer overFlowContainer) {
        int index = -1;
        if (this.mOverflowContainer != null) {
            index = indexOfChild(this.mOverflowContainer);
            removeView(this.mOverflowContainer);
        }
        this.mOverflowContainer = overFlowContainer;
        addView(this.mOverflowContainer, index);
    }

    public void updateOverflowContainerVisibility(boolean visible) {
        int oldVisibility;
        int newVisibility;
        if (this.mOverflowContainer.willBeGone()) {
            oldVisibility = 8;
        } else {
            oldVisibility = this.mOverflowContainer.getVisibility();
        }
        if (visible) {
            newVisibility = 0;
        } else {
            newVisibility = 8;
        }
        if (oldVisibility != newVisibility) {
            Runnable onFinishedRunnable = new Runnable() {
                public void run() {
                    NotificationStackScrollLayout.this.mOverflowContainer.setVisibility(newVisibility);
                    NotificationStackScrollLayout.this.mOverflowContainer.setWillBeGone(false);
                    NotificationStackScrollLayout.this.updateContentHeight();
                    NotificationStackScrollLayout.this.notifyHeightChangeListener(NotificationStackScrollLayout.this.mOverflowContainer);
                }
            };
            if (!this.mAnimationsEnabled || !this.mIsExpanded) {
                this.mOverflowContainer.cancelAppearDrawing();
                onFinishedRunnable.run();
            } else if (newVisibility != 8) {
                this.mOverflowContainer.performAddAnimation(0, 360);
                this.mOverflowContainer.setVisibility(newVisibility);
                this.mOverflowContainer.setWillBeGone(false);
                updateContentHeight();
                notifyHeightChangeListener(this.mOverflowContainer);
            } else {
                this.mOverflowContainer.performRemoveAnimation(360, 0.0f, onFinishedRunnable);
                this.mOverflowContainer.setWillBeGone(true);
            }
        }
    }

    public void updateDismissView(boolean visible) {
        int newVisibility;
        int oldVisibility = this.mDismissView.willBeGone() ? 8 : this.mDismissView.getVisibility();
        if (visible) {
            newVisibility = 0;
        } else {
            newVisibility = 8;
        }
        if (oldVisibility == newVisibility) {
            return;
        }
        if (newVisibility != 8) {
            if (this.mDismissView.willBeGone()) {
                this.mDismissView.cancelAnimation();
            } else {
                this.mDismissView.setInvisible();
            }
            this.mDismissView.setVisibility(newVisibility);
            this.mDismissView.setWillBeGone(false);
            updateContentHeight();
            notifyHeightChangeListener(this.mDismissView);
            return;
        }
        Runnable dimissHideFinishRunnable = new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.mDismissView.setVisibility(8);
                NotificationStackScrollLayout.this.mDismissView.setWillBeGone(false);
                NotificationStackScrollLayout.this.updateContentHeight();
                NotificationStackScrollLayout.this.notifyHeightChangeListener(NotificationStackScrollLayout.this.mDismissView);
            }
        };
        if (this.mDismissView.isButtonVisible() && this.mIsExpanded && this.mAnimationsEnabled) {
            this.mDismissView.setWillBeGone(true);
            this.mDismissView.performVisibilityAnimation(false, dimissHideFinishRunnable);
            return;
        }
        dimissHideFinishRunnable.run();
    }

    public void setDismissAllInProgress(boolean dismissAllInProgress) {
        this.mDismissAllInProgress = dismissAllInProgress;
        this.mAmbientState.setDismissAllInProgress(dismissAllInProgress);
        handleDismissAllClipping();
    }

    private void handleDismissAllClipping() {
        int count = getChildCount();
        boolean previousChildWillBeDismissed = false;
        for (int i = 0; i < count; i++) {
            ExpandableView child = (ExpandableView) getChildAt(i);
            if (child.getVisibility() != 8) {
                if (this.mDismissAllInProgress && r3) {
                    child.setMinClipTopAmount(child.getClipTopAmount());
                } else {
                    child.setMinClipTopAmount(0);
                }
                previousChildWillBeDismissed = canChildBeDismissed(child);
            }
        }
    }

    public boolean isDismissViewNotGone() {
        return (this.mDismissView.getVisibility() == 8 || this.mDismissView.willBeGone()) ? false : true;
    }

    public boolean isDismissViewVisible() {
        return this.mDismissView.isVisible();
    }

    public int getDismissViewHeight() {
        return this.mDismissView.getHeight() + this.mPaddingBetweenElements;
    }

    public int getEmptyShadeViewHeight() {
        return this.mEmptyShadeView.getHeight();
    }

    public float getBottomMostNotificationBottom() {
        int count = getChildCount();
        float max = 0.0f;
        for (int childIdx = 0; childIdx < count; childIdx++) {
            ExpandableView child = (ExpandableView) getChildAt(childIdx);
            if (child.getVisibility() != 8) {
                float bottom = child.getTranslationY() + ((float) child.getActualHeight());
                if (bottom > max) {
                    max = bottom;
                }
            }
        }
        return getStackTranslation() + max;
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
    }

    public void setGroupManager(NotificationGroupManager groupManager) {
        this.mGroupManager = groupManager;
    }

    public void onGoToKeyguard() {
        requestAnimateEverything();
    }

    private void requestAnimateEverything() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mEverythingNeedsAnimation = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public boolean isBelowLastNotification(float touchX, float touchY) {
        boolean z = true;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            ExpandableView child = (ExpandableView) getChildAt(i);
            if (child.getVisibility() != 8) {
                float childTop = child.getY();
                if (childTop > touchY) {
                    return false;
                }
                boolean belowChild = touchY > ((float) child.getActualHeight()) + childTop;
                if (child == this.mDismissView) {
                    if (!(belowChild || this.mDismissView.isOnEmptySpace(touchX - this.mDismissView.getX(), touchY - childTop))) {
                        return false;
                    }
                } else if (child == this.mEmptyShadeView) {
                    return true;
                } else {
                    if (!belowChild) {
                        return false;
                    }
                }
            }
        }
        if (touchY <= ((float) this.mTopPadding) + this.mStackTranslation) {
            z = false;
        }
        return z;
    }

    public void onGroupExpansionChanged(final ExpandableNotificationRow changedRow, boolean expanded) {
        boolean animated = (this.mGroupExpandedForMeasure || !this.mAnimationsEnabled) ? false : !this.mIsExpanded ? changedRow.isPinned() : true;
        if (animated) {
            this.mExpandedGroupView = changedRow;
            this.mNeedsAnimation = true;
        }
        changedRow.setChildrenExpanded(expanded, animated);
        if (!this.mGroupExpandedForMeasure) {
            onHeightChanged(changedRow, false);
        }
        runAfterAnimationFinished(new Runnable() {
            public void run() {
                changedRow.onFinishedExpansionChange();
            }
        });
    }

    public void onGroupCreatedFromChildren(NotificationGroup group) {
        this.mPhoneStatusBar.requestNotificationUpdate();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setScrollable(this.mScrollable);
        event.setScrollX(this.mScrollX);
        event.setScrollY(this.mOwnScrollY);
        event.setMaxScrollX(this.mScrollX);
        event.setMaxScrollY(getScrollRange());
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int scrollRange = getScrollRange();
        if (scrollRange > 0) {
            info.setScrollable(true);
            if (this.mScrollY > 0) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_UP);
            }
            if (this.mScrollY < scrollRange) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_DOWN);
            }
        }
        info.setClassName(ScrollView.class.getName());
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        int direction = -1;
        switch (action) {
            case 4096:
            case 16908346:
                direction = 1;
                break;
            case 8192:
            case 16908344:
                break;
        }
        int targetScrollY = Math.max(0, Math.min(this.mOwnScrollY + (direction * (((((getHeight() - this.mPaddingBottom) - this.mTopPadding) - this.mPaddingTop) - this.mBottomStackPeekSize) - this.mBottomStackSlowDownHeight)), getScrollRange()));
        if (targetScrollY != this.mOwnScrollY) {
            this.mScroller.startScroll(this.mScrollX, this.mOwnScrollY, 0, targetScrollY - this.mOwnScrollY);
            postInvalidateOnAnimation();
            return true;
        }
        return false;
    }

    public void onGroupsChanged() {
        this.mPhoneStatusBar.requestNotificationUpdate();
    }

    public void generateChildOrderChangedEvent() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mGenerateChildOrderChangedEvent = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mAnimationFinishedRunnables.add(runnable);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
        this.mAmbientState.setHeadsUpManager(headsUpManager);
    }

    public void generateHeadsUpAnimation(ExpandableNotificationRow row, boolean isHeadsUp) {
        if (this.mAnimationsEnabled) {
            this.mHeadsUpChangeAnimations.add(new Pair(row, Boolean.valueOf(isHeadsUp)));
            this.mNeedsAnimation = true;
            if (!(this.mIsExpanded || isHeadsUp)) {
                row.setHeadsupDisappearRunning(true);
            }
            requestChildrenUpdate();
        }
    }

    public void setShadeExpanded(boolean shadeExpanded) {
        this.mAmbientState.setShadeExpanded(shadeExpanded);
        this.mStateAnimator.setShadeExpanded(shadeExpanded);
    }

    public void setHeadsUpBoundaries(int height, int bottomBarHeight) {
        this.mAmbientState.setMaxHeadsUpTranslation((float) (height - bottomBarHeight));
        this.mStateAnimator.setHeadsUpAppearHeightBottom(height);
        requestChildrenUpdate();
    }

    public void setTrackingHeadsUp(boolean trackingHeadsUp) {
        this.mTrackingHeadsUp = trackingHeadsUp;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
        this.mScrimController.setScrimBehindChangeRunnable(new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.updateBackgroundDimming();
            }
        });
    }

    public void forceNoOverlappingRendering(boolean force) {
        this.mForceNoOverlappingRendering = force;
    }

    public boolean hasOverlappingRendering() {
        return !this.mForceNoOverlappingRendering ? super.hasOverlappingRendering() : false;
    }

    public void setAnimationRunning(boolean animationRunning) {
        if (animationRunning != this.mAnimationRunning) {
            if (animationRunning) {
                getViewTreeObserver().addOnPreDrawListener(this.mBackgroundUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mBackgroundUpdater);
            }
            this.mAnimationRunning = animationRunning;
            updateContinuousShadowDrawing();
        }
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setPulsing(boolean pulsing) {
        this.mPulsing = pulsing;
        updateNotificationAnimationStates();
    }

    public void setFadingOut(boolean fadingOut) {
        if (fadingOut != this.mFadingOut) {
            this.mFadingOut = fadingOut;
            updateFadingState();
        }
    }

    public void setParentFadingOut(boolean fadingOut) {
        if (fadingOut != this.mParentFadingOut) {
            this.mParentFadingOut = fadingOut;
            updateFadingState();
        }
    }

    private void updateFadingState() {
        if (this.mFadingOut || this.mParentFadingOut || this.mAmbientState.isDark()) {
            this.mScrimController.setExcludedBackgroundArea(null);
        } else {
            applyCurrentBackgroundBounds();
        }
        updateSrcDrawing();
    }

    public void setAlpha(float alpha) {
        if (!this.mIsUIBlocked) {
            boolean z;
            super.setAlpha(alpha);
            if (alpha != 1.0f) {
                z = true;
            } else {
                z = false;
            }
            setFadingOut(z);
        }
    }

    public void removeViewStateForView(View view) {
        this.mCurrentStackScrollState.removeViewStateForView(view);
    }

    private void updateContinuousShadowDrawing() {
        boolean continuousShadowUpdate = !this.mAnimationRunning ? !this.mAmbientState.getDraggedViews().isEmpty() : true;
        if (continuousShadowUpdate != this.mContinuousShadowUpdate) {
            if (continuousShadowUpdate) {
                getViewTreeObserver().addOnPreDrawListener(this.mShadowUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mShadowUpdater);
            }
            this.mContinuousShadowUpdate = continuousShadowUpdate;
        }
    }

    public void resetExposedGearView(boolean animate, boolean force) {
        this.mSwipeHelper.resetExposedGearView(animate, force);
    }

    public void closeControlsIfOutsideTouch(MotionEvent ev) {
        this.mSwipeHelper.closeControlsIfOutsideTouch(ev);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("NotificationStackScrollLayout:");
        pw.println("visible=" + getVisibility());
        pw.println("translationX=" + getTranslationX());
        pw.println("translationY=" + getTranslationY());
        pw.println("left=" + getLeft() + ", right=" + getRight() + ", top=" + getTop() + ", bottom=" + getBottom());
        pw.print(" mCurrentStackHeight=" + this.mCurrentStackHeight);
        pw.print(" mContentHeight=" + this.mContentHeight);
        pw.print(" mLastSetStackHeight=" + this.mLastSetStackHeight);
        pw.print(" mMaxLayoutHeight=" + this.mMaxLayoutHeight);
        pw.print(" mBottomStackSlowDownHeight=" + this.mBottomStackSlowDownHeight);
        pw.print(" mTopPadding=" + this.mTopPadding);
        pw.print(" mStackTranslation=" + this.mStackTranslation);
        pw.print(" mIntrinsicPadding=" + this.mIntrinsicPadding);
        pw.print(" mTopPaddingOverflow=" + this.mTopPaddingOverflow);
        pw.print(" mCurrentBounds=" + this.mCurrentBounds);
        pw.print(" mBackgroundBounds=" + this.mBackgroundBounds);
        pw.println();
        this.mCurrentStackScrollState.dump(fd, pw, args);
        this.mAmbientState.dump(fd, pw, args);
    }

    public boolean pointInView(float localX, float localY, float slop) {
        return localX >= (-slop) && localY >= (-slop) && localX < ((float) (getRight() - getLeft())) + slop;
    }
}
